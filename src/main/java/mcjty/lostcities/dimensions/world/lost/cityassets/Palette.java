package mcjty.lostcities.dimensions.world.lost.cityassets;

import mcjty.lostcities.dimensions.world.lost.BuildingInfo;
import mcjty.lostcities.dimensions.world.lost.LostCitiesTerrainGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A palette of materials as used by building parts
 */
public class Palette implements IAsset {

    private final String name;

    public Palette(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    final Map<Character, Object> palette = new HashMap<>();

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
