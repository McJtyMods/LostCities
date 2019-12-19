package mcjty.lostcities.dimensions.world.driver;

import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunk;

public interface IPrimerDriver {

    void setPrimer(IChunk primer);

    IChunk getPrimer();

    IPrimerDriver current(int x, int y, int z);

    IPrimerDriver current(IIndex index);

    /// Return a copy of the current position
    IIndex getCurrent();

    /// Increment the height of the current position
    void incY();
    /// Increment the height of the current position
    void incY(int amount);
    /// Decrement the height of the current position
    void decY();

    void incX();

    void incZ();

    int getX();

    int getY();

    int getZ();

    IIndex getIndex(int x, int y, int z);

    void setBlockRange(int x, int y, int z, int y2, BlockState c);

    void setBlockRangeSafe(int x, int y, int z, int y2, BlockState c);

    /// Set a block at the current position
    IPrimerDriver block(BlockState c);

    /// Set a block at the current position and increase the height with 1
    IPrimerDriver add(BlockState c);

    BlockState getBlock();

    BlockState getBlockDown();
    BlockState getBlockEast();
    BlockState getBlockWest();
    BlockState getBlockSouth();
    BlockState getBlockNorth();

    BlockState getBlock(int x, int y, int z);

    IPrimerDriver copy();

}
