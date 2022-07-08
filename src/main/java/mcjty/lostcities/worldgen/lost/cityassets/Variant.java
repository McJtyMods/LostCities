package mcjty.lostcities.worldgen.lost.cityassets;

import com.google.gson.JsonObject;
import mcjty.lostcities.api.ILostCityAsset;
import mcjty.lostcities.varia.Tools;
import mcjty.lostcities.worldgen.lost.regassets.VariantRE;
import mcjty.lostcities.worldgen.lost.regassets.data.BlockEntry;
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

    public Variant(VariantRE object) {
        name = object.getRegistryName().getPath(); // @todo temporary. Needs to be fully qualified
        for (BlockEntry entry : object.getBlocks()) {
            BlockState state = Tools.stringToState(entry.block());
            blocks.add(Pair.of(entry.random(), state));
        }
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
    }
}
