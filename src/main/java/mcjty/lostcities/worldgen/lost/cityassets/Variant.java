package mcjty.lostcities.worldgen.lost.cityassets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mcjty.lostcities.api.ILostCityAsset;
import mcjty.lostcities.varia.Tools;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * A block variant
 */
public class Variant implements ILostCityAsset {

    private String name;
    private final List<Pair<Integer, BlockState>> blocks = new ArrayList<>();

    public Variant() {
    }

    public Variant(JsonObject object) {
        readFromJSon(object);
    }

    public Variant(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public List<Pair<Integer, BlockState>> getBlocks() {
        return blocks;
    }

    @Override
    public void readFromJSon(JsonObject object) {
        name = object.get("name").getAsString();
        JsonArray blocksArray = object.get("blocks").getAsJsonArray();
        parseBlockArray(blocksArray);
    }

    public void parseBlockArray(JsonArray array) {
        for (JsonElement el : array) {
            JsonObject ob = el.getAsJsonObject();
            Integer f = ob.get("random").getAsInt();
            String block = ob.get("block").getAsString();
            BlockState state = Tools.stringToState(block);
            blocks.add(Pair.of(f, state));
        }
    }
}
