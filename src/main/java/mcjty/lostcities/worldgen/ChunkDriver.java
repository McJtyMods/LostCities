package mcjty.lostcities.worldgen;

import net.minecraft.block.*;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.WorldGenRegion;

import java.util.function.Predicate;

public class ChunkDriver {

    private WorldGenRegion region;
    private IChunk primer;
    private final BlockPos.Mutable current = new BlockPos.Mutable();
    private final BlockPos.Mutable pos = new BlockPos.Mutable();

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

    public ChunkDriver current(int x, int y, int z) {
        current.set(x, y, z);
        return this;
    }

    public ChunkDriver current(BlockPos i) {
        current.set(i);
        return this;
    }

    public BlockPos getCurrentCopy() {
        return current.immutable();
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
        pos.set(x, y, z);
        while (y < y2) {
            if (primer.getBlockState(pos) != state) {
                primer.setBlockState(pos, state, false);
            }
            y++;
            pos.setY(y);
        }
    }

    public void setBlockRange(int x, int y, int z, int y2, BlockState state, Predicate<BlockState> test) {
        pos.set(x, y, z);
        while (y < y2) {
            BlockState st = primer.getBlockState(pos);
            if (st != state && test.test(st)) {
                primer.setBlockState(pos, state, false);
            }
            y++;
            pos.setY(y);
        }
    }

    public void setBlockRangeSafe(int x, int y, int z, int y2, BlockState state) {
        pos.set(x, y, z);
        while (y < y2) {
            if (primer.getBlockState(pos) != state) {
                primer.setBlockState(pos, state, false);
            }
            y++;
            pos.setY(y);
        }
    }

    public void setBlockRangeSafe(int x, int y, int z, int y2, BlockState state, Predicate<BlockState> test) {
        pos.set(x, y, z);
        while (y < y2) {
            BlockState st = primer.getBlockState(pos);
            if (st != state && test.test(st)) {
                primer.setBlockState(pos, state, false);
            }
            y++;
            pos.setY(y);
        }
    }

    private BlockState updateAdjacent(BlockState state, Direction direction, BlockPos pos, IChunk thisChunk) {
        BlockState adjacent = region.getBlockState(pos);
        if (adjacent.getBlock() instanceof LadderBlock) {
            return adjacent;
        }
        BlockState newAdjacent = null;
        try {
            newAdjacent = adjacent.updateShape(direction, state, region, pos, pos.relative(direction));
        } catch (Exception e) {
            // We got an exception. For example for beehives there can potentially be a problem so in this case we just ignore it
            return adjacent;
        }
        if (newAdjacent != adjacent) {
            IChunk chunk = region.getChunk(pos);
            if (chunk == thisChunk || chunk.getStatus().isOrAfter(ChunkStatus.FULL)) {
                region.setBlock(pos, newAdjacent, 0);
            }
        }
        return newAdjacent;
    }

    public static boolean isBlockStairs(BlockState state) {
        return state.getBlock() instanceof StairsBlock;
    }

    private static boolean isDifferentStairs(BlockState state, IBlockReader worldIn, BlockPos pos, Direction face) {
        BlockState blockstate = worldIn.getBlockState(pos.relative(face));
        return !isBlockStairs(blockstate) || blockstate.getValue(StairsBlock.FACING) != state.getValue(StairsBlock.FACING) || blockstate.getValue(StairsBlock.HALF) != state.getValue(StairsBlock.HALF);
    }

    private static StairsShape getShapeProperty(BlockState state, IBlockReader worldIn, BlockPos pos) {
        Direction direction = state.getValue(StairsBlock.FACING);
        BlockState blockstate = worldIn.getBlockState(pos.relative(direction));
        if (isBlockStairs(blockstate) && state.getValue(StairsBlock.HALF) == blockstate.getValue(StairsBlock.HALF)) {
            Direction direction1 = blockstate.getValue(StairsBlock.FACING);
            if (direction1.getAxis() != state.getValue(StairsBlock.FACING).getAxis() && isDifferentStairs(state, worldIn, pos, direction1.getOpposite())) {
                if (direction1 == direction.getCounterClockWise()) {
                    return StairsShape.OUTER_LEFT;
                }

                return StairsShape.OUTER_RIGHT;
            }
        }

        BlockState blockstate1 = worldIn.getBlockState(pos.relative(direction.getOpposite()));
        if (isBlockStairs(blockstate1) && state.getValue(StairsBlock.HALF) == blockstate1.getValue(StairsBlock.HALF)) {
            Direction direction2 = blockstate1.getValue(StairsBlock.FACING);
            if (direction2.getAxis() != state.getValue(StairsBlock.FACING).getAxis() && isDifferentStairs(state, worldIn, pos, direction2)) {
                if (direction2 == direction.getCounterClockWise()) {
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
        if (state.canOcclude()) {
            return true;
        }
        return !Block.isExceptionForConnection(state.getBlock());
    }

    private BlockState correct(BlockState state) {
        int cx = current.getX() + (primer.getPos().x << 4);
        int cy = current.getY();
        int cz = current.getZ() + (primer.getPos().z << 4);

        IChunk thisChunk = region.getChunk(cx >> 4, cz >> 4);
        BlockState westState = updateAdjacent(state, Direction.EAST, pos.set(cx - 1, cy, cz), thisChunk);
        BlockState eastState = updateAdjacent(state, Direction.WEST, pos.set(cx + 1, cy, cz), thisChunk);
        BlockState northState = updateAdjacent(state, Direction.SOUTH, pos.set(cx, cy, cz - 1), thisChunk);
        BlockState southState = updateAdjacent(state, Direction.NORTH, pos.set(cx, cy, cz + 1), thisChunk);

        if (state.getBlock() instanceof FourWayBlock) {
            state = state.setValue(FourWayBlock.WEST, canAttach(westState));
            state = state.setValue(FourWayBlock.EAST, canAttach(eastState));
            state = state.setValue(FourWayBlock.NORTH, canAttach(northState));
            state = state.setValue(FourWayBlock.SOUTH, canAttach(southState));
        } else if (state.getBlock() instanceof StairsBlock) {
            state = state.setValue(StairsBlock.SHAPE, getShapeProperty(state, region, pos.set(cx, cy, cz)));
        }
        return state;
    }

    public ChunkDriver blockImm(BlockState c) {
        primer.setBlockState(current, c, false);
        return this;
    }

//    private void validate() {
//        if (current.getX() < 0 || current.getY() < 0 || current.getZ() < 0) {
//            throw new RuntimeException("current: " + current.getX() + "," + current.getY() + "," + current.getZ());
//        }
//        if (current.getX() > 15 || current.getY() > 255 || current.getZ() > 15) {
//            throw new RuntimeException("current: " + current.getX() + "," + current.getY() + "," + current.getZ());
//        }
//    }

    public ChunkDriver block(BlockState c) {
//        validate();
        primer.setBlockState(current, correct(c), false);
        return this;
    }

    public ChunkDriver add(BlockState state) {
//        validate();
        primer.setBlockState(current, correct(state), false);
        incY();
        return this;
    }

    public BlockState getBlock() {
        return primer.getBlockState(current);
    }

    public BlockState getBlockDown() {
        return primer.getBlockState(pos.set(current.getX(), current.getY()-1, current.getZ()));
    }

    public BlockState getBlockEast() {
        return primer.getBlockState(pos.set(current.getX()+1, current.getY(), current.getZ()));
    }

    public BlockState getBlockWest() {
        return primer.getBlockState(pos.set(current.getX()-1, current.getY(), current.getZ()));
    }

    public BlockState getBlockSouth() {
        return primer.getBlockState(pos.set(current.getX(), current.getY(), current.getZ()+1));
    }

    public BlockState getBlockNorth() {
        return primer.getBlockState(pos.set(current.getX(), current.getY(), current.getZ()-1));
    }


    public BlockState getBlock(int x, int y, int z) {
        return primer.getBlockState(pos.set(x, y, z));
    }
}
