package mcjty.lostcities.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;

import javax.annotation.Nullable;
import java.util.function.Predicate;

import static net.minecraft.world.level.chunk.LevelChunkSection.*;

public class ChunkDriver {

    private LevelAccessor region;
    private ChunkAccess primer;
    private final BlockPos.MutableBlockPos current = new BlockPos.MutableBlockPos();
    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
//    private final Long2ObjectOpenHashMap<BlockState> cache = new Long2ObjectOpenHashMap<>();
    private SectionCache cache;
    private int cx;
    private int cz;

    public void setPrimer(LevelAccessor region, ChunkAccess primer) {
        this.region = region;
        this.primer = primer;
        if (primer != null) {
            cache = new SectionCache(region, primer.getPos().x << 4, primer.getPos().z << 4);
            this.cx = primer.getPos().x;
            this.cz = primer.getPos().z;
        }
    }

    public void actuallyGenerate(ChunkAccess chunk) {
        BulkSectionAccess bulk = new BulkSectionAccess(region);
        cache.generate(bulk);
        bulk.close();

        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        for (int x = 0 ; x < 16 ; x++) {
            for (int z = 0 ; z < 16 ; z++) {
                int y = cache.heightmap[x][z];
                if (y > Integer.MIN_VALUE) {
                    chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.MOTION_BLOCKING).update(x, y, z, bedrock);
                    chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES).update(x, y, z, bedrock);
                    chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR).update(x, y, z, bedrock);
                    chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE).update(x, y, z, bedrock);
                }
            }
        }

        cache.clear();
    }

    private void setBlock(BlockPos p, BlockState state) {
        if (state != null) {
            cache.put(p, state);
        }
    }

    // This version of getBlock() is less optimal but it will work for different chunks
    private BlockState getBlockSafe(BlockPos p) {
        if (isThisChunk(p)) {
            return getBlock(p);
        }
        // During parallel world generation a neighbouring chunk may not be part of (or ready in)
        // the current WorldGenRegion. Reading it would throw "Requested chunk unavailable during
        // world generation" and kill chunk generation. Treat unavailable neighbours as air:
        // only cosmetic connection states (fences, walls, stairs) depend on these reads
        if (!region.hasChunk(p.getX() >> 4, p.getZ() >> 4)) {
            return Blocks.AIR.defaultBlockState();
        }
        try {
            return region.getBlockState(p);
        } catch (RuntimeException e) {
            return Blocks.AIR.defaultBlockState();
        }
    }

    private BlockState getBlock(BlockPos p) {
        BlockState state = cache.get(p);
        if (state == null) {
            state = region.getBlockState(p);
            cache.put(p, state);
        }
        return state;
    }

    public LevelAccessor getRegion() {
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

    public BlockPos.MutableBlockPos getCurrent() {
        return current;
    }

    public void incY() {
        current.setY(current.getY()+1);
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
        cache.putRange(x + (primer.getPos().x << 4), z + (primer.getPos().z << 4), y, y2-1, state);
    }

    public void setBlockRange(int x, int y, int z, int y2, BlockState state, Predicate<BlockState> test) {
        cache.putRange(x + (primer.getPos().x << 4), z + (primer.getPos().z << 4), y, y2-1, state, test);
    }

    public void setBlockRangeToAir(int x, int y, int z, int y2) {
        cache.putRange(x + (primer.getPos().x << 4), z + (primer.getPos().z << 4), y, y2-1, Blocks.AIR.defaultBlockState());
    }

    public void setBlockRangeToAir(int x, int y, int z, int y2, Predicate<BlockState> test) {
        cache.putRange(x + (primer.getPos().x << 4), z + (primer.getPos().z << 4), y, y2-1, Blocks.AIR.defaultBlockState(), test);
    }

    private boolean isThisChunk(BlockPos pos) {
        int px = pos.getX() >> 4;
        int pz = pos.getZ() >> 4;
        return px == cx && pz == cz;
    }

    private BlockState updateAdjacent(BlockState state, Direction direction, BlockPos pos, ChunkAccess thisChunk) {
        BlockState adjacent = getBlockSafe(pos);
        if (adjacent.getBlock() instanceof LadderBlock) {
            return adjacent;
        }
        BlockState newAdjacent = null;
        try {
            newAdjacent = adjacent.updateShape(region, region, pos, direction, pos.relative(direction), state, region.getRandom());
        } catch (Exception e) {
            // We got an exception. For example for beehives there can potentially be a problem so in this case we just ignore it
            return adjacent;
        }
        if (newAdjacent != adjacent) {
            // The adjacent position may be outside the available region during parallel world
            // generation. In that case just skip the (cosmetic) update
            if (!region.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
                return adjacent;
            }
            ChunkAccess chunk;
            try {
                chunk = region.getChunk(pos);
            } catch (RuntimeException e) {
                return adjacent;
            }
            if (chunk == thisChunk) {
                setBlock(pos, newAdjacent);
            } else if (chunk.getPersistedStatus().isOrAfter(ChunkStatus.FULL)) {
                region.setBlock(pos, newAdjacent, Block.UPDATE_CLIENTS);
            }
        }
        return newAdjacent;
    }

    public static boolean isBlockStairs(BlockState state) {
        return state.getBlock() instanceof StairBlock;
    }

    private boolean isDifferentStairs(BlockState state, BlockPos pos, Direction face) {
        BlockPos relative = pos.relative(face);
        BlockState blockstate = getBlockSafe(relative);
        return !isBlockStairs(blockstate) || blockstate.getValue(StairBlock.FACING) != state.getValue(StairBlock.FACING) || blockstate.getValue(StairBlock.HALF) != state.getValue(StairBlock.HALF);
    }

    private StairsShape getShapeProperty(BlockState state, BlockPos pos) {
        Direction direction = state.getValue(StairBlock.FACING);
        BlockPos relative = pos.relative(direction);
        BlockState blockstate = getBlockSafe(relative);
        if (isBlockStairs(blockstate) && state.getValue(StairBlock.HALF) == blockstate.getValue(StairBlock.HALF)) {
            Direction direction1 = blockstate.getValue(StairBlock.FACING);
            if (direction1.getAxis() != state.getValue(StairBlock.FACING).getAxis() && isDifferentStairs(state, pos, direction1.getOpposite())) {
                if (direction1 == direction.getCounterClockWise()) {
                    return StairsShape.OUTER_LEFT;
                }

                return StairsShape.OUTER_RIGHT;
            }
        }

        BlockPos relativeOpposite = pos.relative(direction.getOpposite());
        BlockState blockstate1 = getBlockSafe(relativeOpposite);
        if (isBlockStairs(blockstate1) && state.getValue(StairBlock.HALF) == blockstate1.getValue(StairBlock.HALF)) {
            Direction direction2 = blockstate1.getValue(StairBlock.FACING);
            if (direction2.getAxis() != state.getValue(StairBlock.FACING).getAxis() && isDifferentStairs(state, pos, direction2)) {
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
            state = state.setValue(WallBlock.WEST, canAttachWall(westState));
            state = state.setValue(WallBlock.EAST, canAttachWall(eastState));
            state = state.setValue(WallBlock.NORTH, canAttachWall(northState));
            state = state.setValue(WallBlock.SOUTH, canAttachWall(southState));
        } else if (state.getBlock() instanceof StairBlock) {
            state = state.setValue(StairBlock.SHAPE, getShapeProperty(state, pos.set(cx, cy, cz)));
        } else if (state.getBlock() instanceof StructureVoidBlock){
            //like an alpha channel - but for parts! Uses whatever block was previously there instead of changing it!
            return null;
        }
        return state;
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
        setBlock(current, correct(c));
        return this;
    }

    public ChunkDriver add(BlockState state) {
//        validate();
        setBlock(current, correct(state));
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

    private static class S {
        private final BlockState[] section = new BlockState[SECTION_SIZE];
        private boolean isEmpty = true;
    }

    private static class SectionCache {
        private final int minY;
        private final int maxY;
        private final int cx;
        private final int cz;
        private final S[] cache;
        private final int[][] heightmap = new int[16][16];

        private SectionCache(LevelAccessor level, int cx, int cz) {
            minY = level.getMinY();
            maxY = level.getMaxY() + 1;
            this.cx = cx;
            this.cz = cz;
            cache = new S[(maxY - minY) / SECTION_HEIGHT];
            clear();
        }

        // Puts a range of blockstates starting at pos and ending at y2 (inclusive)
        private void putRange(int x, int z, int y1, int y2, BlockState state) {
            if (state == null) {
                return;
            }
            int ystart = y1;
            int px = x & 0xf;
            int pz = z & 0xf;
            boolean isAir = state.isAir();
            boolean dirty = false;
            while (y1 <= y2) {
                int sectionIdx = (y1 - minY) / SECTION_HEIGHT;
                int idx = (px << 8) + ((y1 & 0xf) << 4) + pz;

                if (cache[sectionIdx].section[idx] != state) {
                    dirty = true;
                    cache[sectionIdx].section[idx] = state;
                    if (!isAir) {
                        cache[sectionIdx].isEmpty = false;
                    }
                }
                y1++;
            }

            // Now update the heightmap
            if (dirty) {
                if (!isAir) {
                    if (heightmap[px][pz] < y2) {
                        heightmap[px][pz] = y2;
                    }
                } else {
                    // If state is air we need to recalculate the heightmap
                    fixHeightmapForAir(ystart, px, pz);
                }
            }
        }

        // Puts a range of blockstates starting at pos and ending at y2 (inclusive)
        private void putRange(int x, int z, int y1, int y2, BlockState state, Predicate<BlockState> test) {
            if (state == null) {
                return;
            }
            int ystart = y1;
            int px = x & 0xf;
            int pz = z & 0xf;
            boolean isAir = state.isAir();
            boolean dirty = false;
            while (y1 <= y2) {
                int sectionIdx = (y1 - minY) / SECTION_HEIGHT;
                int idx = (px << 8) + ((y1 & 0xf) << 4) + pz;

                BlockState st = cache[sectionIdx].section[idx];
                if (st != state && st != null && test.test(st)) {
                    dirty = true;
                    cache[sectionIdx].section[idx] = state;
                    if (!isAir) {
                        cache[sectionIdx].isEmpty = false;
                    }
                }
                y1++;
            }

            // Now update the heightmap
            if (dirty) {
                if (!isAir) {
                    if (heightmap[px][pz] < y2) {
                        heightmap[px][pz] = y2;
                    }
                } else {
                    // If state is air we need to recalculate the heightmap
                    fixHeightmapForAir(ystart, px, pz);
                }
            }
        }

        private void put(BlockPos pos, BlockState state) {
            int sectionIdx = (pos.getY() - minY) / SECTION_HEIGHT;
            int px = pos.getX() & 0xf;
            int pz = pos.getZ() & 0xf;
            int idx = (px << 8) + ((pos.getY() & 0xf) << 4) + pz;
            if (cache[sectionIdx].section[idx] == state) {
                return;
            }
            cache[sectionIdx].section[idx] = state;
            if (!state.isAir()) {
                cache[sectionIdx].isEmpty = false;
                if (heightmap[px][pz] < pos.getY()) {
                    heightmap[px][pz] = pos.getY();
                }
            } else {
                // If state is air we need to recalculate the heightmap
                fixHeightmapForAir(pos.getY(), px, pz);
            }
        }

        private void fixHeightmapForAir(int y1, int px, int pz) {
            if (heightmap[px][pz] >= y1) {
                int y = Math.max(heightmap[px][pz], y1);
                while (y >= minY) {
                    int si = (y - minY) / SECTION_HEIGHT;
                    int i = (px << 8) + ((y & 0xf) << 4) + pz;
                    BlockState st = cache[si].section[i];
                    if (st != null && !st.isAir()) {
                        heightmap[px][pz] = y;
                        return;
                    }
                    y--;
                }
                heightmap[px][pz] = Integer.MIN_VALUE;
            }
        }

        @Nullable
        private BlockState get(BlockPos pos) {
            int sectionIdx = (pos.getY() - minY) / SECTION_HEIGHT;
            int idx = ((pos.getX() & 0xf) << 8) + ((pos.getY() & 0xf) << 4) + ((pos.getZ() & 0xf));
            return cache[sectionIdx].section[idx];
        }

        private void generate(BulkSectionAccess bulk) {
            for (int si = 0 ; si < (maxY - minY) / SECTION_HEIGHT ; si++) {
                S c = cache[si];
                if (!c.isEmpty) {
                    int cy = si * SECTION_HEIGHT + minY;
                    LevelChunkSection section = bulk.getSection(new BlockPos(cx, cy, cz));
                    if (section == null) {
                        throw new RuntimeException("This cannot happen: " + si);
                    }
                    int i = 0;
                    for (int x = 0 ; x < SECTION_WIDTH ; x++) {
                        for (int y = 0 ; y < SECTION_HEIGHT ; y++) {
                            for (int z = 0 ; z < SECTION_WIDTH ; z++) {
                                BlockState state = c.section[i++];
                                if (state != null) {
                                    section.setBlockState(x, y, z, state, false);
                                }
                            }
                        }
                    }
                }
            }
        }

        private void clear() {
            for (int si = 0 ; si < (maxY - minY) / SECTION_HEIGHT ; si++) {
                cache[si] = new S();
            }
            for (int x = 0 ; x < 16 ; x++) {
                for (int z = 0 ; z < 16 ; z++) {
                    heightmap[x][z] = Integer.MIN_VALUE;
                }
            }
        }
    }
}
