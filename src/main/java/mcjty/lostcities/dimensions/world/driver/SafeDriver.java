package mcjty.lostcities.dimensions.world.driver;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.IChunk;

import java.util.Objects;

// @todo OPTIMIZE!!!!!!
public class SafeDriver implements IPrimerDriver {

    private IChunk primer;
    private final BlockPos.MutableBlockPos current = new BlockPos.MutableBlockPos();

    @Override
    public void setPrimer(IChunk primer) {
        this.primer = primer;
    }

    @Override
    public IChunk getPrimer() {
        return primer;
    }

    @Override
    public IPrimerDriver current(int x, int y, int z) {
        current.setPos(x, y, z);
        return this;
    }

    @Override
    public IPrimerDriver current(IIndex index) {
        Index i = (Index) index;
        current.setPos(i.x, i.y, i.z);
        return this;
    }

    @Override
    public IIndex getCurrent() {
        return new Index(current.getX(), current.getY(), current.getZ());
    }

    @Override
    public void incY() {
        current.setY(current.getY()+1);
    }

    @Override
    public void incY(int amount) {
        current.setY(current.getY()+amount);
    }

    @Override
    public void decY() {
        current.setY(current.getY()-1);
    }

    @Override
    public void incX() {
        current.setX(current.getX()+1);
    }

    @Override
    public void incZ() {
        current.setZ(current.getZ()+1);
    }

    @Override
    public int getX() {
        return current.getX();
    }

    @Override
    public int getY() {
        return current.getY();
    }

    @Override
    public int getZ() {
        return current.getZ();
    }

    @Override
    public void setBlockRange(int x, int y, int z, int y2, char c) {
        BlockState state = Block.BLOCK_STATE_IDS.getByValue(c);
        while (y < y2) {
            primer.setBlockState(new BlockPos(x, y, z), state, false);
            y++;
        }
    }

    // @todo OPTIMIZE!!!!!!
    // @todo use mutable blockpos?
    @Override
    public void setBlockRangeSafe(int x, int y, int z, int y2, char c) {
        BlockState state = Block.BLOCK_STATE_IDS.getByValue(c);
        while (y < y2) {
            primer.setBlockState(new BlockPos(x, y, z), state, false);
            y++;
        }
    }

    @Override
    public IPrimerDriver block(char c) {
        BlockState state = Block.BLOCK_STATE_IDS.getByValue(c);
        primer.setBlockState(current, state, false);
        return this;
    }

    @Override
    public IPrimerDriver block(BlockState c) {
        primer.setBlockState(current, c, false);
        return this;
    }

    @Override
    public IPrimerDriver add(char c) {
        BlockState state = Block.BLOCK_STATE_IDS.getByValue(c);
        primer.setBlockState(current, state, false);
        incY();
        return this;
    }

    @Override
    public char getBlock() {
        return (char) Block.BLOCK_STATE_IDS.get(primer.getBlockState(current));
    }

    @Override
    public char getBlockDown() {
        return (char) Block.BLOCK_STATE_IDS.get(primer.getBlockState(new BlockPos(current.getX(), current.getY()-1, current.getZ())));
    }

    @Override
    public char getBlockEast() {
        return (char) Block.BLOCK_STATE_IDS.get(primer.getBlockState(new BlockPos(current.getX()+1, current.getY(), current.getZ())));
    }

    @Override
    public char getBlockWest() {
        return (char) Block.BLOCK_STATE_IDS.get(primer.getBlockState(new BlockPos(current.getX()-1, current.getY(), current.getZ())));
    }

    @Override
    public char getBlockSouth() {
        return (char) Block.BLOCK_STATE_IDS.get(primer.getBlockState(new BlockPos(current.getX(), current.getY(), current.getZ()+1)));
    }

    @Override
    public char getBlockNorth() {
        return (char) Block.BLOCK_STATE_IDS.get(primer.getBlockState(new BlockPos(current.getX(), current.getY(), current.getZ()-1)));
    }


    @Override
    public char getBlock(int x, int y, int z) {
        return (char) Block.BLOCK_STATE_IDS.get(primer.getBlockState(new BlockPos(x, y, z)));
    }

    @Override
    public IIndex getIndex(int x, int y, int z) {
        return new Index(x, y, z);
    }


    private class Index implements IIndex {
        private final int x;
        private final int y;
        private final int z;

        Index(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
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

    @Override
    public IPrimerDriver copy() {
        SafeDriver driver = new SafeDriver();
        driver.current.setPos(current);
        driver.primer = primer;
        return driver;
    }
}
