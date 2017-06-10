package mcjty.lostcities.dimensions.world.lost.cityassets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mcjty.lostcities.dimensions.world.lost.BuildingInfo;
import mcjty.lostcities.dimensions.world.lost.LostCitiesTerrainGenerator;
import mcjty.lostcities.varia.Tools;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A palette of materials as used by building parts
 */
public class Palette implements IAsset {

    private String name;
    final Map<Character, Object> palette = new HashMap<>();
    final Map<IBlockState, IBlockState> damaged = new HashMap<>();
    final Map<Character, String> mobIds = new HashMap<>(); // For spawners

    public Palette() {
    }

    public Palette(JsonObject object) {
        readFromJSon(object);
    }

    public Palette(String name) {
        this.name = name;
    }

    public void merge(Palette other) {
        palette.putAll(other.palette);
        damaged.putAll(other.damaged);
        mobIds.putAll(other.mobIds);
    }

    @Override
    public String getName() {
        return name;
    }

    public Map<IBlockState, IBlockState> getDamaged() {
        return damaged;
    }

    public Map<Character, String> getMobIds() {
        return mobIds;
    }

    @Override
    public void readFromJSon(JsonObject object) {
        name = object.get("name").getAsString();
        JsonArray paletteArray = object.get("palette").getAsJsonArray();
        for (JsonElement element : paletteArray) {
            JsonObject o = element.getAsJsonObject();
            Object value = null;
            Character c = o.get("char").getAsCharacter();
            IBlockState dmg = null;
            String mobId = null;
            if (o.has("damaged")) {
                dmg = Tools.stringToState(o.get("damaged").getAsString());
            }
            if (o.has("mob")) {
                mobIds.put(c, o.get("mob").getAsString());
            }
            if (o.has("block")) {
                String block = o.get("block").getAsString();
                IBlockState state = Tools.stringToState(block);
                palette.put(c, state);
                if (dmg != null) {
                    damaged.put(state, dmg);
                }
            } else if (o.has("frompalette")) {
                value = o.get("frompalette").getAsString();
                palette.put(c, value);
            } else if (o.has("blocks")) {
                JsonArray array = o.get("blocks").getAsJsonArray();
                List<Pair<Float, IBlockState>> blocks = new ArrayList<>();
                for (JsonElement el : array) {
                    JsonObject ob = el.getAsJsonObject();
                    Float f = ob.get("factor").getAsFloat();
                    String block = ob.get("block").getAsString();
                    IBlockState state = Tools.stringToState(block);
                    blocks.add(Pair.of(f, state));
                    if (dmg != null) {
                        damaged.put(state, dmg);
                    }
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
                o.add("block", new JsonPrimitive(Tools.stateToString(state)));
                if (damaged.containsKey(state)) {
                    o.add("damaged", new JsonPrimitive(Tools.stateToString(damaged.get(state))));
                }
            } else if (entry.getValue() instanceof String) {
                o.add("frompalette", new JsonPrimitive((String) entry.getValue()));
            } else {
                o.add("test", new JsonPrimitive("@todo"));
            }
            if (mobIds.containsKey(entry.getKey())) {
                o.add("mob", new JsonPrimitive(mobIds.get(entry.getKey())));
            }
            array.add(o);
        }
        object.add("palette", array);
        return object;
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
        Supplier<IBlockState> function = () -> {
            float r = LostCitiesTerrainGenerator.globalRandom.nextFloat();
            for (Pair<Float, IBlockState> pair : randomBlocks) {
                r -= pair.getKey();
                if (r <= 0) {
                    return pair.getRight();
                }
            }
            return LostCitiesTerrainGenerator.air;
        };
        palette.put(c, function);
        return this;
    }
}
