package mcjty.lostcities.dimensions.world;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;

import java.util.Arrays;

/**
 * The base terrain generator.
 */
public interface BaseTerrainGenerator {
    IBlockState defaultState = Blocks.AIR.getDefaultState();

    static void setBlockState(ChunkPrimer primer, int index, IBlockState state) {
        primer.data[index] = (char) Block.BLOCK_STATE_IDS.get(state);
    }

    static void setBlockState(ChunkPrimer primer, int index, char state) {
        primer.data[index] = state;
    }

    // From 's' (inclusive) to 'e' (exclusive)
    static void setBlockStateRange(ChunkPrimer primer, int s, int e, IBlockState state) {
        Arrays.fill(primer.data, s, e, (char) Block.BLOCK_STATE_IDS.get(state));
    }

    static IBlockState getBlockState(ChunkPrimer primer, int index) {
        IBlockState iblockstate = Block.BLOCK_STATE_IDS.getByValue(primer.data[index]);
        return iblockstate == null ? defaultState : iblockstate;
    }
}
