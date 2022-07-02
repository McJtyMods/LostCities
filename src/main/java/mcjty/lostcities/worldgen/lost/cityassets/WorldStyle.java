package mcjty.lostcities.worldgen.lost.cityassets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mcjty.lostcities.api.ILostCityAsset;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.Tools;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.BiomeInfo;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Predicate;

public class WorldStyle implements ILostCityAsset {

    private String name;
    private String outsideStyle;

    private final List<Pair<Predicate<Holder<Biome>>, Pair<Float, String>>> cityStyleSelector = new ArrayList<>();

    public WorldStyle(JsonObject object) {
        readFromJSon(object);
    }

    public WorldStyle(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void readFromJSon(JsonObject object) {
        name = object.get("name").getAsString();
        outsideStyle = object.get("outsidestyle").getAsString();
        JsonArray array = object.get("citystyles").getAsJsonArray();
        for (JsonElement element : array) {
            JsonObject o = element.getAsJsonObject();
            float factor = o.get("factor").getAsFloat();
            String building = o.get("citystyle").getAsString();
            Predicate<Holder<Biome>> predicate = biomeHolder -> true;
            if (o.has("biomes")) {
                JsonArray ar = o.get("biomes").getAsJsonArray();
                Set<ResourceLocation> biomes = new HashSet<>();
                for (JsonElement el : ar) {
                    Biome biome = ForgeRegistries.BIOMES.getValue(new ResourceLocation(el.getAsString()));
                    if (biome != null) {
                        biomes.add(Tools.getBiomeId(biome));
                    }
                }
                predicate = info -> hasBiomes(info, biomes);
            }
            cityStyleSelector.add(Pair.of(predicate, Pair.of(factor, building)));
        }
    }

    private boolean isValidBiome(Set<ResourceLocation> biomeSet, Holder<Biome> biome) {
        return biomeSet.contains(Tools.getBiomeId(biome.value()));
    }

    private boolean hasBiomes(Holder<Biome> biome, Set<ResourceLocation> biomeSet) {
        return isValidBiome(biomeSet, biome);
    }

    public String getOutsideStyle() {
        return outsideStyle;
    }

    public JsonObject writeToJSon() {
        JsonObject object = new JsonObject();
        object.add("type", new JsonPrimitive("worldstyle"));
        object.add("name", new JsonPrimitive(name));
        object.add("outsidestyle", new JsonPrimitive(outsideStyle));

        JsonArray array = new JsonArray();
        for (Pair<Predicate<Holder<Biome>>, Pair<Float, String>> pair : cityStyleSelector) {
            JsonObject o = new JsonObject();
            Pair<Float, String> ff = pair.getValue();
            o.add("factor", new JsonPrimitive(ff.getKey()));
            o.add("citystyle", new JsonPrimitive(ff.getValue()));
            array.add(o);
        }
        object.add("citystyles", array);
        return object;
    }


    public String getRandomCityStyle(IDimensionInfo provider, int chunkX, int chunkZ, Random random) {
        Holder<Biome> biome = BiomeInfo.getBiomeInfo(provider, new ChunkCoord(provider.getType(), chunkX, chunkZ)).getMainBiome();
        List<Pair<Float, String>> ct = new ArrayList<>();
        for (Pair<Predicate<Holder<Biome>>, Pair<Float, String>> pair : cityStyleSelector) {
            if (pair.getKey().test(biome)) {
                ct.add(pair.getValue());
            }
        }

        return Tools.getRandomFromList(random, ct);
    }
}
