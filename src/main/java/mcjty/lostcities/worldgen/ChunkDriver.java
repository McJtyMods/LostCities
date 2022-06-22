package mcjty.lostcities.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class ChunkDriver {

    private WorldGenRegion region;
    private ChunkAccess primer;
    private final BlockPos.MutableBlockPos current = new BlockPos.MutableBlockPos();
    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    private final Map<Long, BlockState> cache = new HashMap<>();

    private static int minY = Integer.MAX_VALUE;
    private static int maxY = Integer.MIN_VALUE;

    public void setPrimer(WorldGenRegion region, ChunkAccess primer) {
        this.region = region;
        this.primer = primer;
        cache.clear();
    }

    public void actuallyGenerate() {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(0, 0, 0);
        cache.forEach((pos, state) -> {
            mutable.set(pos);
            if (region.getBlockState(mutable) != state) {
                region.setBlock(mutable, state, 0);
            }
        });
        cache.clear();
    }

    private void setBlock(BlockPos p, BlockState state) {
        cache.put(p.asLong(), state);
    }

    private BlockState getBlock(BlockPos p) {
        long l = p.asLong();
        if (cache.containsKey(l)) {
            return cache.get(l);
        } else {
            return region.getBlockState(p);
        }
    }

    public WorldGenRegion getRegion() {
        return region;
    }

    public ChunkAccess getPrimer() {
        return primer;
    }

    public ChunkDriver current(int x, int y, int z) {
        current.set(x + (primer.getPos().x << 4), y, z + (primer.getPos().z << 4));
        return this;
    }

    public ChunkDriver currentAbsolute(BlockPos pos) {
        current.set(pos);
        return this;
    }

    public ChunkDriver currentRelative(BlockPos pos) {
        current(pos.getX(), pos.getY(), pos.getZ());
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
        BlockState air = Blocks.AIR.defaultBlockState();
        pos.set(x + (primer.getPos().x << 4), y, z + (primer.getPos().z << 4));
        while (y < y2) {
            setBlock(pos, state);
            y++;
            pos.setY(y);
        }
    }

    public void setBlockRangeToAir(int x, int y, int z, int y2) {
        BlockState air = Blocks.AIR.defaultBlockState();
        pos.set(x + (primer.getPos().x << 4), y, z + (primer.getPos().z << 4));
        while (y < y2) {
            setBlock(pos, air);
            y++;
            pos.setY(y);
        }
    }

    public void setBlockRangeToAir(int x, int y, int z, int y2, Predicate<BlockState> test) {
        BlockState air = Blocks.AIR.defaultBlockState();
        pos.set(x + (primer.getPos().x << 4), y, z + (primer.getPos().z << 4));
        while (y < y2) {
            BlockState st = getBlock(pos);
            if (st != air && test.test(st)) {
                setBlock(pos, air);
            }
            y++;
            pos.setY(y);
        }
    }

    public void setBlockRangeToAirSafe(int x, int y, int z, int y2) {
        BlockState air = Blocks.AIR.defaultBlockState();
        pos.set(x + (primer.getPos().x << 4), y, z + (primer.getPos().z << 4));
        while (y < y2) {
            setBlock(pos, air);
            y++;
            pos.setY(y);
        }
    }

    public void setBlockRangeSafe(int x, int y, int z, int y2, BlockState state) {
        pos.set(x + (primer.getPos().x << 4), y, z + (primer.getPos().z << 4));
        while (y < y2) {
            setBlock(pos, state);
            y++;
            pos.setY(y);
        }
    }

    public void setBlockRangeSafe(int x, int y, int z, int y2, BlockState state, Predicate<BlockState> test) {
        pos.set(x + (primer.getPos().x << 4), y, z + (primer.getPos().z << 4));
        while (y < y2) {
            BlockState st = getBlock(pos);
            if (st != state && test.test(st)) {
                setBlock(pos, state);
            }
            y++;
            pos.setY(y);
        }
    }

    public void setBlockRangeToAirSafe(int x, int y, int z, int y2, Predicate<BlockState> test) {
        BlockState air = Blocks.AIR.defaultBlockState();
        pos.set(x + (primer.getPos().x << 4), y, z + (primer.getPos().z << 4));
        while (y < y2) {
            BlockState st = getBlock(pos);
            if (st != air && test.test(st)) {
                setBlock(pos, air);
            }
            y++;
            pos.setY(y);
        }
    }

    private BlockState updateAdjacent(BlockState state, Direction direction, BlockPos pos, ChunkAccess thisChunk) {
        BlockState adjacent = getBlock(pos);
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
            ChunkAccess chunk = region.getChunk(pos);
            if (chunk == thisChunk || chunk.getStatus().isOrAfter(ChunkStatus.FULL)) {
                setBlock(pos, newAdjacent);
            }
        }
        return newAdjacent;
    }

    public static boolean isBlockStairs(BlockState state) {
        return state.getBlock() instanceof StairBlock;
    }

    private static boolean isDifferentStairs(BlockState state, BlockGetter worldIn, BlockPos pos, Direction face) {
        BlockState blockstate = worldIn.getBlockState(pos.relative(face));
        return !isBlockStairs(blockstate) || blockstate.getValue(StairBlock.FACING) != state.getValue(StairBlock.FACING) || blockstate.getValue(StairBlock.HALF) != state.getValue(StairBlock.HALF);
    }

    private static StairsShape getShapeProperty(BlockState state, BlockGetter worldIn, BlockPos pos) {
        Direction direction = state.getValue(StairBlock.FACING);
        BlockState blockstate = worldIn.getBlockState(pos.relative(direction));
        if (isBlockStairs(blockstate) && state.getValue(StairBlock.HALF) == blockstate.getValue(StairBlock.HALF)) {
            Direction direction1 = blockstate.getValue(StairBlock.FACING);
            if (direction1.getAxis() != state.getValue(StairBlock.FACING).getAxis() && isDifferentStairs(state, worldIn, pos, direction1.getOpposite())) {
                if (direction1 == direction.getCounterClockWise()) {
                    return StairsShape.OUTER_LEFT;
                }

                return StairsShape.OUTER_RIGHT;
            }
        }

        BlockState blockstate1 = worldIn.getBlockState(pos.relative(direction.getOpposite()));
        if (isBlockStairs(blockstate1) && state.getValue(StairBlock.HALF) == blockstate1.getValue(StairBlock.HALF)) {
            Direction direction2 = blockstate1.getValue(StairBlock.FACING);
            if (direction2.getAxis() != state.getValue(StairBlock.FACING).getAxis() && isDifferentStairs(state, worldIn, pos, direction2)) {
                if (direction2 == direction.getCounterClockWise()) {
                    return StairsShape.INNER_LEFT;
                }

                return StairsShape.INNER_RIGHT;
            }
        }

        return StairsShape.STRAIGHT;
    }

    private static WallSide canAttachWall(BlockState state) {
        return canAttach(state) ? WallSide.LOW : WallSide.NONE;
    }

    private static boolean canAttach(BlockState state) {
        if (state.isAir()) {
            return false;
        }
        if (state.canOcclude()) {
            return true;
        }
        return !Block.isExceptionForConnection(state);
    }

    private BlockState correct(BlockState state) {
        int cx = current.getX();
        int cy = current.getY();
        int cz = current.getZ();

        ChunkAccess thisChunk = region.getChunk(cx >> 4, cz >> 4);
        BlockState westState = updateAdjacent(state, Direction.EAST, pos.set(cx - 1, cy, cz), thisChunk);
        BlockState eastState = updateAdjacent(state, Direction.WEST, pos.set(cx + 1, cy, cz), thisChunk);
        BlockState northState = updateAdjacent(state, Direction.SOUTH, pos.set(cx, cy, cz - 1), thisChunk);
        BlockState southState = updateAdjacent(state, Direction.NORTH, pos.set(cx, cy, cz + 1), thisChunk);

        if (state.getBlock() instanceof CrossCollisionBlock) {
            state = state.setValue(CrossCollisionBlock.WEST, canAttach(westState));
            state = state.setValue(CrossCollisionBlock.EAST, canAttach(eastState));
            state = state.setValue(CrossCollisionBlock.NORTH, canAttach(northState));
            state = state.setValue(CrossCollisionBlock.SOUTH, canAttach(southState));
        } else if (state.getBlock() instanceof WallBlock) {
            state = state.setValue(WallBlock.WEST_WALL, canAttachWall(westState));
            state = state.setValue(WallBlock.EAST_WALL, canAttachWall(eastState));
            state = state.setValue(WallBlock.NORTH_WALL, canAttachWall(northState));
            state = state.setValue(WallBlock.SOUTH_WALL, canAttachWall(southState));
        } else if (state.getBlock() instanceof StairBlock) {
            state = state.setValue(StairBlock.SHAPE, getShapeProperty(state, region, pos.set(cx, cy, cz)));
        }
        return state;
    }

    public ChunkDriver blockImm(BlockState c) {
        setBlock(pos, c);
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
        setBlock(current, c);
        return this;
    }

    public ChunkDriver add(BlockState state) {
//        validate();
        setBlock(current, state);
        incY();
        return this;
    }

    public BlockState getBlock() {
        return getBlock(current);
    }

    public BlockState getBlockDown() {
        return getBlock(pos.set(current.getX(), current.getY()-1, current.getZ()));
    }

    public BlockState getBlockEast() {
        return getBlock(pos.set(current.getX()+1, current.getY(), current.getZ()));
    }

    public BlockState getBlockWest() {
        return getBlock(pos.set(current.getX()-1, current.getY(), current.getZ()));
    }

    public BlockState getBlockSouth() {
        return getBlock(pos.set(current.getX(), current.getY(), current.getZ()+1));
    }

    public BlockState getBlockNorth() {
        return getBlock(pos.set(current.getX(), current.getY(), current.getZ()-1));
    }


    public BlockState getBlock(int x, int y, int z) {
        return getBlock(pos.set(x + (primer.getPos().x << 4), y, z + (primer.getPos().z << 4)));
    }
}
