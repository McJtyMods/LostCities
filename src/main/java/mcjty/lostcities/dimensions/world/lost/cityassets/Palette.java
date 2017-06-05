package mcjty.lostcities.dimensions.world.lost.cityassets;

import mcjty.lostcities.dimensions.world.lost.BuildingInfo;
import mcjty.lostcities.dimensions.world.lost.LostCitiesTerrainGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A palette of materials as used by building parts
 */
public class Palette {

    final Map<Character, Object> palette = new HashMap<>();

    private void addFunctionMapping(char c, Function<BuildingInfo, IBlockState> function) {
        palette.put(c, function);
    }

    public void addMapping(char c, IBlockState state) {
        palette.put(c, state);
    }

    public void addMapping(char c, Block block) {
        IBlockState state = block.getDefaultState();
        palette.put(c, state);
    }

    public void addMapping(char c, String styledBlock) {
        palette.put(c, styledBlock);
    }

    public void addMappingViaState(char c, Pair<Float, IBlockState>... randomBlocks) {
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
    }

    public void addMappingViaStyle(char c, Pair<Float, String>... randomBlocks) {
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
    }

//    public IBlockState get(char c, BuildingInfo info) {
//        try {
//            return palette.get(c).apply(info);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
}
