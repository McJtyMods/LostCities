package mcjty.lostcities.dimensions.world.lost.cityassets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mcjty.lostcities.dimensions.world.lost.BuildingInfo;
import mcjty.lostcities.dimensions.world.lost.LostCitiesTerrainGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A palette of materials as used by building parts
 */
public class Palette implements IAsset {

    private String name;
    final Map<Character, Object> palette = new HashMap<>();


    public Palette(JsonObject object) {
        readFromJSon(object);
    }

    public Palette(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void readFromJSon(JsonObject object) {
        name = object.get("name").getAsString();
        JsonArray paletteArray = object.get("palette").getAsJsonArray();
        for (JsonElement element : paletteArray) {
            JsonObject o = element.getAsJsonObject();
            Object value = null;
            Character c = o.get("char").getAsCharacter();
            if (o.has("block")) {
                String block = o.get("block").getAsString();
                int meta = o.get("meta").getAsInt();
                value = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(block));
                if (value == null) {
                    // @todo
                    throw new RuntimeException("Cannot find block '" + block + "'!");
                }
                palette.put(c, ((Block)value).getStateFromMeta(meta));
            } else if (o.has("style")) {
                value = o.get("style").getAsString();
                palette.put(c, value);
            } else if (o.has("styles")) {
                JsonArray array = o.get("styles").getAsJsonArray();
                List<Pair<Float, String>> styles = new ArrayList<>();
                for (JsonElement el : array) {
                    JsonObject ob = el.getAsJsonObject();
                    Float f = ob.get("factor").getAsFloat();
                    String style = ob.get("style").getAsString();
                    styles.add(Pair.of(f, style));
                }
                addMappingViaStyle(c, styles.toArray(new Pair[styles.size()]));
            } else if (o.has("blocks")) {
                JsonArray array = o.get("blocks").getAsJsonArray();
                List<Pair<Float, IBlockState>> blocks = new ArrayList<>();
                for (JsonElement el : array) {
                    JsonObject ob = el.getAsJsonObject();
                    Float f = ob.get("factor").getAsFloat();
                    String block = ob.get("block").getAsString();
                    int meta = ob.get("meta").getAsInt();
                    Block b = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(block));
                    blocks.add(Pair.of(f, b.getStateFromMeta(meta)));
                }
                addMappingViaState(c, blocks.toArray(new Pair[blocks.size()]));

            } else {
                throw new RuntimeException("Illegal palette!");
            }
        }
    }

    @Override
    public JsonObject writeToJSon() {
        JsonObject object = new JsonObject();
        object.add("type", new JsonPrimitive("palette"));
        object.add("name", new JsonPrimitive(name));
        JsonArray array = new JsonArray();
        for (Map.Entry<Character, Object> entry : palette.entrySet()) {
            JsonObject o = new JsonObject();
            o.add("char", new JsonPrimitive(entry.getKey()));
            if (entry.getValue() instanceof IBlockState) {
                IBlockState state = (IBlockState) entry.getValue();
                o.add("block", new JsonPrimitive(state.getBlock().getRegistryName().toString()));
                o.add("meta", new JsonPrimitive(state.getBlock().getMetaFromState(state)));
            } else if (entry.getValue() instanceof String) {
                o.add("style", new JsonPrimitive((String) entry.getValue()));
            } else {
                o.add("test", new JsonPrimitive("@todo"));
            }
            array.add(o);
        }
        object.add("palette", array);
        return object;
    }

    private void addFunctionMapping(char c, Function<BuildingInfo, IBlockState> function) {
        palette.put(c, function);
    }

    public Palette addMapping(char c, IBlockState state) {
        palette.put(c, state);
        return this;
    }

    public Palette addMapping(char c, Block block) {
        IBlockState state = block.getDefaultState();
        palette.put(c, state);
        return this;
    }

    public Palette addMapping(char c, String styledBlock) {
        palette.put(c, styledBlock);
        return this;
    }

    public Palette addMappingViaState(char c, Pair<Float, IBlockState>... randomBlocks) {
        addFunctionMapping(c, info -> {
            float r = LostCitiesTerrainGenerator.globalRandom.nextFloat();
            for (Pair<Float, IBlockState> pair : randomBlocks) {
                r -= pair.getKey();
                if (r <= 0) {
                    return pair.getRight();
                }
            }
            return LostCitiesTerrainGenerator.air;
        });
        return this;
    }

    public Palette addMappingViaStyle(char c, Pair<Float, String>... randomBlocks) {
        addFunctionMapping(c, info -> {
            float r = LostCitiesTerrainGenerator.globalRandom.nextFloat();
            for (Pair<Float, String> pair : randomBlocks) {
                r -= pair.getKey();
                if (r <= 0) {
                    return info.getStyle().get(pair.getRight());
                }
            }
            return LostCitiesTerrainGenerator.air;
        });
        return this;
    }
}
