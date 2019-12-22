package mcjty.lostcities.dimensions.world.driver;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FourWayBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.WorldGenRegion;

import java.util.Objects;

public class PrimerDriver {

    private WorldGenRegion region;
    private IChunk primer;
    private final BlockPos.MutableBlockPos current = new BlockPos.MutableBlockPos();
    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    public void setPrimer(WorldGenRegion region, IChunk primer) {
        this.region = region;
        this.primer = primer;
    }

    public WorldGenRegion getRegion() {
        return region;
    }

    public IChunk getPrimer() {
        return primer;
    }

    public PrimerDriver current(int x, int y, int z) {
        current.setPos(x, y, z);
        return this;
    }

    public PrimerDriver current(IIndex index) {
        Index i = (Index) index;
        current.setPos(i.x, i.y, i.z);
        return this;
    }

    public IIndex getCurrent() {
        return new Index(current.getX(), current.getY(), current.getZ());
    }

    public void incY() {
        current.setY(current.getY()+1);
    }

    public void incY(int amount) {
        current.setY(current.getY()+amount);
    }

    public void decY() {
        current.setY(current.getY()-1);
    }

    public void incX() {
        current.setX(current.getX()+1);
    }

    public void incZ() {
        current.setZ(current.getZ()+1);
    }

    public int getX() {
        return current.getX();
    }

    public int getY() {
        return current.getY();
    }

    public int getZ() {
        return current.getZ();
    }

    public void setBlockRange(int x, int y, int z, int y2, BlockState state) {
        pos.setPos(x, y, z);
        while (y < y2) {
            if (primer.getBlockState(pos) != state) {
                primer.setBlockState(pos, state, false);
            }
            y++;
            pos.setY(y);
        }
    }

    public void setBlockRangeSafe(int x, int y, int z, int y2, BlockState state) {
        pos.setPos(x, y, z);
        while (y < y2) {
            if (primer.getBlockState(pos) != state) {
                primer.setBlockState(pos, state, false);
            }
            y++;
            pos.setY(y);
        }
    }

    private BlockState updateAdjacent(BlockState state, Direction direction, BlockPos pos) {
        BlockState adjacent = region.getBlockState(pos);
        BlockState newAdjacent = adjacent.getBlock().updatePostPlacement(adjacent, direction, state, region, pos, current);
        if (newAdjacent != adjacent) {
            region.setBlockState(pos, newAdjacent, 0);
        }
        return newAdjacent;
    }

    public static boolean isBlockStairs(BlockState state) {
        return state.getBlock() instanceof StairsBlock;
    }

    private static boolean isDifferentStairs(BlockState state, IBlockReader worldIn, BlockPos pos, Direction face) {
        BlockState blockstate = worldIn.getBlockState(pos.offset(face));
        return !isBlockStairs(blockstate) || blockstate.get(StairsBlock.FACING) != state.get(StairsBlock.FACING) || blockstate.get(StairsBlock.HALF) != state.get(StairsBlock.HALF);
    }

    private static StairsShape getShapeProperty(BlockState state, IBlockReader worldIn, BlockPos pos) {
        Direction direction = state.get(StairsBlock.FACING);
        BlockState blockstate = worldIn.getBlockState(pos.offset(direction));
        if (isBlockStairs(blockstate) && state.get(StairsBlock.HALF) == blockstate.get(StairsBlock.HALF)) {
            Direction direction1 = blockstate.get(StairsBlock.FACING);
            if (direction1.getAxis() != state.get(StairsBlock.FACING).getAxis() && isDifferentStairs(state, worldIn, pos, direction1.getOpposite())) {
                if (direction1 == direction.rotateYCCW()) {
                    return StairsShape.OUTER_LEFT;
                }

                return StairsShape.OUTER_RIGHT;
            }
        }

        BlockState blockstate1 = worldIn.getBlockState(pos.offset(direction.getOpposite()));
        if (isBlockStairs(blockstate1) && state.get(StairsBlock.HALF) == blockstate1.get(StairsBlock.HALF)) {
            Direction direction2 = blockstate1.get(StairsBlock.FACING);
            if (direction2.getAxis() != state.get(StairsBlock.FACING).getAxis() && isDifferentStairs(state, worldIn, pos, direction2)) {
                if (direction2 == direction.rotateYCCW()) {
                    return StairsShape.INNER_LEFT;
                }

                return StairsShape.INNER_RIGHT;
            }
        }

        return StairsShape.STRAIGHT;
    }

    private static boolean canAttach(BlockState state) {
        if (state.isAir()) {
            return false;
        }
        if (state.isSolid()) {
            return true;
        }
        return !Block.cannotAttach(state.getBlock());
    }

    private BlockState correct(BlockState state) {
        int cx = current.getX() + primer.getPos().x * 16;
        int cy = current.getY();
        int cz = current.getZ() + primer.getPos().z * 16;

        BlockState westState = updateAdjacent(state, Direction.EAST, pos.setPos(cx - 1, cy, cz));
        BlockState eastState = updateAdjacent(state, Direction.WEST, pos.setPos(cx + 1, cy, cz));
        BlockState northState = updateAdjacent(state, Direction.SOUTH, pos.setPos(cx, cy, cz - 1));
        BlockState southState = updateAdjacent(state, Direction.NORTH, pos.setPos(cx, cy, cz + 1));

        if (state.getBlock() instanceof FourWayBlock) {
            state = state.with(FourWayBlock.WEST, canAttach(westState));
            state = state.with(FourWayBlock.EAST, canAttach(eastState));
            state = state.with(FourWayBlock.NORTH, canAttach(northState));
            state = state.with(FourWayBlock.SOUTH, canAttach(southState));
        } else if (state.getBlock() instanceof StairsBlock) {
            state = state.with(StairsBlock.SHAPE, getShapeProperty(state, region, pos.setPos(cx, cy, cz)));
        }
        return state;
    }

    public PrimerDriver blockImm(BlockState c) {
        primer.setBlockState(current, c, false);
        return this;
    }

    public PrimerDriver block(BlockState c) {
        primer.setBlockState(current, correct(c), false);
        return this;
    }

    public PrimerDriver add(BlockState state) {
        primer.setBlockState(current, correct(state), false);
        incY();
        return this;
    }

    public BlockState getBlock() {
        return primer.getBlockState(current);
    }

    public BlockState getBlockDown() {
        return primer.getBlockState(pos.setPos(current.getX(), current.getY()-1, current.getZ()));
    }

    public BlockState getBlockEast() {
        return primer.getBlockState(pos.setPos(current.getX()+1, current.getY(), current.getZ()));
    }

    public BlockState getBlockWest() {
        return primer.getBlockState(pos.setPos(current.getX()-1, current.getY(), current.getZ()));
    }

    public BlockState getBlockSouth() {
        return primer.getBlockState(pos.setPos(current.getX(), current.getY(), current.getZ()+1));
    }

    public BlockState getBlockNorth() {
        return primer.getBlockState(pos.setPos(current.getX(), current.getY(), current.getZ()-1));
    }


    public BlockState getBlock(int x, int y, int z) {
        return primer.getBlockState(pos.setPos(x, y, z));
    }

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

    public PrimerDriver copy() {
        PrimerDriver driver = new PrimerDriver();
        driver.current.setPos(current);
        driver.primer = primer;
        driver.region = region;
        return driver;
    }
}
