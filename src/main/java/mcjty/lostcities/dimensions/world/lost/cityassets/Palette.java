package mcjty.lostcities.dimensions.world.lost.cityassets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mcjty.lostcities.api.ILostCityAsset;
import mcjty.lostcities.varia.Tools;
import net.minecraft.block.state.IBlockState;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A palette of materials as used by building parts
 */
public class Palette implements ILostCityAsset {

    private String name;
    final Map<Character, Object> palette = new HashMap<>();
    private final Map<IBlockState, IBlockState> damaged = new HashMap<>();
    private final Map<Character, String> mobIds = new HashMap<>(); // For spawners
    private final Map<Character, String> lootTables = new HashMap<>(); // For chests
    private final Map<Character, Map<String, Integer>> torchOrientations = new HashMap<>(); // For torches

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
        lootTables.putAll(other.lootTables);
        torchOrientations.putAll(other.torchOrientations);
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

    public Map<Character, String> getLootTables() {
        return lootTables;
    }

    public Map<Character, Map<String, Integer>> getTorchOrientations() {
        return torchOrientations;
    }

    public Map<Character, Object> getPalette() {
        return palette;
    }

    @Override
    public void readFromJSon(JsonObject object) {
        name = object.get("name").getAsString();
        JsonArray paletteArray = object.get("palette").getAsJsonArray();
        parsePaletteArray(paletteArray);
    }

    public void parsePaletteArray(JsonArray paletteArray) {
        for (JsonElement element : paletteArray) {
            JsonObject o = element.getAsJsonObject();
            Object value = null;
            Character c = o.get("char").getAsCharacter();
            IBlockState dmg = null;
            if (o.has("damaged")) {
                dmg = Tools.stringToState(o.get("damaged").getAsString());
            }
            if (o.has("mob")) {
                mobIds.put(c, o.get("mob").getAsString());
            }
            if (o.has("loot")) {
                lootTables.put(c, o.get("loot").getAsString());
            }
            if (o.has("facing")) {
                Map<String, Integer> or = new HashMap<>();
                JsonObject torchObj = o.get("facing").getAsJsonObject();
                getOrientation(or, torchObj, "north");
                getOrientation(or, torchObj, "south");
                getOrientation(or, torchObj, "west");
                getOrientation(or, torchObj, "east");
                getOrientation(or, torchObj, "up");
                torchOrientations.put(c, or);
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
                List<Pair<Integer, IBlockState>> blocks = new ArrayList<>();
                for (JsonElement el : array) {
                    JsonObject ob = el.getAsJsonObject();
                    Integer f = ob.get("random").getAsInt();
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

    private void getOrientation(Map<String, Integer> or, JsonObject torchObj, String orientation) {
        if (torchObj.has(orientation)) {
            or.put(orientation, torchObj.get(orientation).getAsInt());
        } else {
            or.put(orientation, 0);
        }
    }

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
            if (lootTables.containsKey(entry.getKey())) {
                o.add("loot", new JsonPrimitive(lootTables.get(entry.getKey())));
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

    @SafeVarargs
    private final Palette addMappingViaState(char c, Pair<Integer, IBlockState>... randomBlocks) {
        palette.put(c, randomBlocks);
        return this;
    }
}
