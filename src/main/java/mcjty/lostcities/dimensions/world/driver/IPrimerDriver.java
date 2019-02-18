package mcjty.lostcities.dimensions.world.driver;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.ChunkPrimer;

public interface IPrimerDriver {

    void setPrimer(ChunkPrimer primer);

    ChunkPrimer getPrimer();

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

    void setBlockRange(int x, int y, int z, int y2, char c);

    void setBlockRangeSafe(int x, int y, int z, int y2, char c);

    /// Set a block at the current position
    IPrimerDriver block(char c);

    /// Set a block at the current position
    IPrimerDriver block(IBlockState c);

    /// Set a block at the current position and increase the height with 1
    IPrimerDriver add(char c);

    char getBlock();

    char getBlockDown();
    char getBlockEast();
    char getBlockWest();
    char getBlockSouth();
    char getBlockNorth();

    char getBlock(int x, int y, int z);

    IPrimerDriver copy();

}
