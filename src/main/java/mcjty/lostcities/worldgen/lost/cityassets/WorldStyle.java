package mcjty.lostcities.worldgen.lost.cityassets;

import mcjty.lostcities.api.ILostCityAsset;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.Tools;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.BiomeInfo;
import mcjty.lostcities.worldgen.lost.regassets.WorldStyleRE;
import mcjty.lostcities.worldgen.lost.regassets.data.CityBiomeMultiplier;
import mcjty.lostcities.worldgen.lost.regassets.data.CityStyleSelector;
import mcjty.lostcities.worldgen.lost.regassets.data.DataTools;
import mcjty.lostcities.worldgen.lost.regassets.data.ScatteredReference;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class WorldStyle implements ILostCityAsset {

    private final ResourceLocation name;
    private final String outsideStyle;

    private final List<Pair<Predicate<Holder<Biome>>, Pair<Float, String>>> cityStyleSelector = new ArrayList<>();
    private final List<Pair<Predicate<Holder<Biome>>, Float>> cityBiomeMultiplier = new ArrayList<>();
    private final List<ScatteredReference> scatteredReferences = new ArrayList<>();

    public WorldStyle(WorldStyleRE object) {
        name = object.getRegistryName();
        outsideStyle = object.getOutsideStyle();
        for (CityStyleSelector selector : object.getCityStyleSelectors()) {
            Predicate<Holder<Biome>> predicate = biomeHolder -> true;
            if (selector.biomeMatcher() != null) {
                predicate = selector.biomeMatcher();
            }
            cityStyleSelector.add(Pair.of(predicate, Pair.of(selector.factor(), selector.citystyle())));
        }
        if (object.getCityBiomeMultipliers() != null) {
            for (CityBiomeMultiplier multiplier : object.getCityBiomeMultipliers()) {
                cityBiomeMultiplier.add(Pair.of(multiplier.biomeMatcher(), multiplier.multiplier()));
            }
        }
        if (object.getScatteredReferences() != null) {
            for (ScatteredReference reference : object.getScatteredReferences()) {
                scatteredReferences.add(reference);
            }
        }
    }

    @Override
    public String getName() {
        return DataTools.toName(name);
    }

    @Override
    public ResourceLocation getId() {
        return name;
    }

    public String getOutsideStyle() {
        return outsideStyle;
    }

    public float getCityChanceMultiplier(IDimensionInfo provider, int chunkX, int chunkZ) {
        Holder<Biome> biome = BiomeInfo.getBiomeInfo(provider, new ChunkCoord(provider.getType(), chunkX, chunkZ)).getMainBiome();
        for (Pair<Predicate<Holder<Biome>>, Float> pair : cityBiomeMultiplier) {
            if (pair.getLeft().test(biome)) {
                return pair.getRight();
            }
        }
        return 1.0f;
    }

    public String getRandomCityStyle(IDimensionInfo provider, int chunkX, int chunkZ, Random random) {
        Holder<Biome> biome = BiomeInfo.getBiomeInfo(provider, new ChunkCoord(provider.getType(), chunkX, chunkZ)).getMainBiome();
        List<Pair<Float, String>> ct = new ArrayList<>();
        for (Pair<Predicate<Holder<Biome>>, Pair<Float, String>> pair : cityStyleSelector) {
            if (pair.getKey().test(biome)) {
                ct.add(pair.getValue());
            }
        }

        Pair<Float, String> randomFromList = Tools.getRandomFromList(random, ct, Pair::getLeft);
        if (randomFromList == null) {
            return null;
        } else {
            return randomFromList.getRight();
        }
    }

    public List<ScatteredReference> getScatteredReferences() {
        return scatteredReferences;
    }
}
