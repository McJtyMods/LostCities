package mcjty.lostcities.dimensions.world.driver;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.ChunkPrimer;

import java.util.Arrays;
import java.util.Objects;

public class OptimizedDriver implements IPrimerDriver {

    private ChunkPrimer primer;

    @Override
    public void setPrimer(ChunkPrimer primer) {
        this.primer = primer;
    }

    @Override
    public ChunkPrimer getPrimer() {
        return primer;
    }

    @Override
    public void setBlockStateRange(int x, int y, int z, int y2, char c) {
        int s = getBlockIndex(x, y, z);
        int e = s + y2-y;
        Arrays.fill(primer.data, s, e, c);
    }

    @Override
    public void setBlockStateRangeSafe(int x, int y, int z, int y2, char c) {
        if (y2 <= y) {
            return;
        }
        int s = getBlockIndex(x, y, z);
        int e = s + y2-y;
        Arrays.fill(primer.data, s, e, c);
    }

    @Override
    public void setBlockStateRange(IIndex index, int y2, char c) {
        int s = ((Index) index).index;
        int e = s + y2-index.getY();
        Arrays.fill(primer.data, s, e, c);
    }

    @Override
    public void setBlockStateRangeSafe(IIndex index, int y2, char c) {
        int y = index.getY();
        if (y2 <= y) {
            return;
        }
        int s = ((Index) index).index;
        int e = s + y2-y;
        Arrays.fill(primer.data, s, e, c);
    }

    @Override
    public void setBlockState(IIndex index, char c) {
        primer.data[((Index) index).index] = c;
    }

    @Override
    public void setBlockState(IIndex index, IBlockState c) {
        primer.data[((Index) index).index] = (char) Block.BLOCK_STATE_IDS.get(c);
    }

    @Override
    public void setBlockState(int x, int y, int z, char c) {
        primer.data[getBlockIndex(x, y, z)] = c;
    }

    @Override
    public void setBlockState(int x, int y, int z, IBlockState c) {
        primer.data[getBlockIndex(x, y, z)] = (char) Block.BLOCK_STATE_IDS.get(c);
    }

    @Override
    public IBlockState getBlockState(IIndex index) {
        return Block.BLOCK_STATE_IDS.getByValue(primer.data[((Index)index).index]);
    }

    @Override
    public char getBlockChar(IIndex index) {
        return primer.data[((Index)index).index];
    }

    @Override
    public IBlockState getBlockState(int x, int y, int z) {
        return Block.BLOCK_STATE_IDS.getByValue(primer.data[getBlockIndex(x, y, z)]);
    }

    @Override
    public char getBlockChar(int x, int y, int z) {
        return primer.data[getBlockIndex(x, y, z)];
    }

    @Override
    public IIndex getIndex(int x, int y, int z) {
        return new Index(getBlockIndex(x, y, z));
    }

    private static int getBlockIndex(int x, int y, int z) {
        return x << 12 | z << 8 | y;
    }

    private class Index implements IIndex {
        private int index;

        public Index(int index) {
            this.index = index;
        }

        @Override
        public void decY() {
            index--;
        }

        @Override
        public void incX() {
            index += 1<<12;
        }

        @Override
        public void incY() {
            index++;
        }

        @Override
        public void incY(int amount) {
            index += amount;
        }

        @Override
        public void incZ() {
            index += 1<<8;
        }

        @Override
        public IIndex up() {
            return new Index(index+1);
        }

        @Override
        public IIndex down() {
            return new Index(index-1);
        }

        @Override
        public IIndex north() {
            return new Index(index-(1<<8));
        }

        @Override
        public IIndex west() {
            return new Index(index-(1<<12));
        }

        @Override
        public IIndex south() {
            return new Index(index+(1<<8));
        }

        @Override
        public IIndex east() {
            return new Index(index+(1<<12));
        }

        @Override
        public int getX() {
            return (index >> 12) & 0xf;
        }

        @Override
        public int getY() {
            return index & 0xff;
        }

        @Override
        public int getZ() {
            return (index >> 8) & 0xf;
        }

        @Override
        public IIndex copy() {
            return new Index(index);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Index index1 = (Index) o;
            return index == index1.index;
        }

        @Override
        public int hashCode() {
            return Objects.hash(index);
        }
    }

}
