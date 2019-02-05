package mcjty.lostcities.dimensions.world.driver;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.ChunkPrimer;

public interface IPrimerDriver {

    void setPrimer(ChunkPrimer primer);

    ChunkPrimer getPrimer();

    void setCurrent(int x, int y, int z);

    /// Return a copy of the current position
    IIndex getCurrent();

    /// Increment the height of the current position
    void incY();
    /// Decrement the height of the current position
    void decY();


    IIndex getIndex(int x, int y, int z);

    void setBlockRange(int x, int y, int z, int y2, char c);

    void setBlockRangeSafe(int x, int y, int z, int y2, char c);

    void setBlockRange(IIndex index, int y2, char c);

    void setBlockRangeSafe(IIndex index, int y2, char c);

    /// Set a block at the current position and increase the height with 1
    IPrimerDriver addBlock(char c);

    /// Set a block at the current position and increase the height with 1
    IPrimerDriver addBlock(IBlockState c);

    void setBlock(IIndex index, char c);

    void setBlock(IIndex index, IBlockState c);

    void setBlock(int x, int y, int z, char c);

    void setBlock(int x, int y, int z, IBlockState c);

    char getBlockChar();

    char getBlockCharDown();

    char getBlockChar(IIndex index);

    IBlockState getBlockState(IIndex index);

    char getBlockChar(int x, int y, int z);

    IBlockState getBlockState(int x, int y, int z);

}
