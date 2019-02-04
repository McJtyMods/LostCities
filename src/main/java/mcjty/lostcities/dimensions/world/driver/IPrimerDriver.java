package mcjty.lostcities.dimensions.world.driver;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.ChunkPrimer;

public interface IPrimerDriver {

    void setPrimer(ChunkPrimer primer);

    ChunkPrimer getPrimer();

    IIndex getIndex(int x, int y, int z);

    void setBlockStateRange(int x, int y, int z, int y2, char c);

    void setBlockStateRangeSafe(int x, int y, int z, int y2, char c);

    void setBlockStateRange(IIndex index, int y2, char c);

    void setBlockStateRangeSafe(IIndex index, int y2, char c);

    void setBlockState(IIndex index, char c);

    void setBlockState(IIndex index, IBlockState c);

    void setBlockState(int x, int y, int z, char c);

    void setBlockState(int x, int y, int z, IBlockState c);

    char getBlockChar(IIndex index);

    IBlockState getBlockState(IIndex index);

    char getBlockChar(int x, int y, int z);

    IBlockState getBlockState(int x, int y, int z);

}
