package mcjty.lostcities.dimensions.world.driver;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.ChunkPrimer;

import java.util.Objects;

public class SafeDriver implements IPrimerDriver {

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
        IBlockState state = Block.BLOCK_STATE_IDS.getByValue(c);
        while (y < y2) {
            primer.setBlockState(x, y, z, state);
            y++;
        }
    }

    @Override
    public void setBlockStateRangeSafe(int x, int y, int z, int y2, char c) {
        IBlockState state = Block.BLOCK_STATE_IDS.getByValue(c);
        while (y < y2) {
            primer.setBlockState(x, y, z, state);
            y++;
        }
    }

    @Override
    public void setBlockStateRange(IIndex index, int y2, char c) {
        Index i = (Index) index;
        setBlockStateRange(i.x, i.y, i.z, y2, c);
    }

    @Override
    public void setBlockStateRangeSafe(IIndex index, int y2, char c) {
        Index i = (Index) index;
        setBlockStateRangeSafe(i.x, i.y, i.z, y2, c);
    }

    @Override
    public void setBlockState(IIndex index, char c) {
        Index i = (Index) index;
        IBlockState state = Block.BLOCK_STATE_IDS.getByValue(c);
        primer.setBlockState(i.x, i.y, i.z, state);
    }

    @Override
    public void setBlockState(IIndex index, IBlockState c) {
        Index i = (Index) index;
        primer.setBlockState(i.x, i.y, i.z, c);
    }

    @Override
    public void setBlockState(int x, int y, int z, char c) {
        IBlockState state = Block.BLOCK_STATE_IDS.getByValue(c);
        primer.setBlockState(x, y, z, state);
    }

    @Override
    public void setBlockState(int x, int y, int z, IBlockState c) {
        primer.setBlockState(x, y, z, c);
    }

    @Override
    public IBlockState getBlockState(IIndex index) {
        Index i = (Index) index;
        return primer.getBlockState(i.x, i.y, i.z);
    }

    @Override
    public char getBlockChar(IIndex index) {
        Index i = (Index) index;
        return (char) Block.BLOCK_STATE_IDS.get(primer.getBlockState(i.x, i.y, i.z));
    }


    @Override
    public char getBlockChar(int x, int y, int z) {
        return (char) Block.BLOCK_STATE_IDS.get(primer.getBlockState(x, y, z));
    }

    @Override
    public IBlockState getBlockState(int x, int y, int z) {
        return primer.getBlockState(x, y, z);
    }

    @Override
    public IIndex getIndex(int x, int y, int z) {
        return new Index(x, y, z);
    }


    private class Index implements IIndex {
        private int x;
        private int y;
        private int z;

        public Index(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public IIndex up() {
            return new Index(x, y+1, z);
        }

        @Override
        public IIndex down() {
            return new Index(x, y-1, z);
        }

        @Override
        public IIndex north() {
            return new Index(x, y, z-1);
        }

        @Override
        public IIndex south() {
            return new Index(x, y, z+1);
        }

        @Override
        public IIndex west() {
            return new Index(x-1, y, z);
        }

        @Override
        public IIndex east() {
            return new Index(x+1, y, z);
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public int getZ() {
            return z;
        }

        @Override
        public void decY() {
            y--;
        }

        @Override
        public void incX() {
            x++;
        }

        @Override
        public void incY() {
            y++;
        }

        @Override
        public void incY(int amount) {
            y += amount;
        }

        @Override
        public void incZ() {
            z++;
        }

        @Override
        public IIndex copy() {
            return new Index(x, y, z);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Index index = (Index) o;
            return x == index.x &&
                    y == index.y &&
                    z == index.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }
    }
}
