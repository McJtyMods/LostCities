package mcjty.lostcities.worldgen;

import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.cityassets.WorldStyle;
import mcjty.lostcities.worldgen.lost.regassets.data.WorldSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;

public class ChunkFixer {


    private static void executePostTodo(ChunkCoord coord, IDimensionInfo provider) {
        BuildingInfo info = BuildingInfo.getBuildingInfo(coord, provider);
        info.getPostTodo().forEach((pos, runnable) -> runnable.run());
        info.clearPostTodo();
    }

    private static void generateVines(ChunkCoord coord, LevelAccessor world, IDimensionInfo provider, RandomSource random) {
        float vineChance = provider.getProfile().VINE_CHANCE;
        if (vineChance < 0.000001) {
            return;
        }
        int chunkX = coord.chunkX();
        int chunkZ = coord.chunkZ();
        int cx = chunkX << 4;
        int cz = chunkZ << 4;
        BuildingInfo info = BuildingInfo.getBuildingInfo(coord, provider);

        int maxHeight = info.getMaxHeight();

        WorldStyle worldStyle = provider.getWorldStyle();
        WorldSettings worldSettings = worldStyle.getWorldSettings();
        if (info.hasBuilding) {
            if (world.getChunk(coord.chunkX() + 1, coord.chunkZ()).getStatus().isOrAfter(ChunkStatus.FEATURES)) {
                BuildingInfo adjacent = info.getXmax();
                int bottom = Math.max(adjacent.getCityGroundLevel() + 3, adjacent.hasBuilding ? adjacent.getMaxHeight() : (adjacent.getCityGroundLevel() + 3));
                BlockState state = worldSettings.vineWest();
                for (int z = 0; z < 15; z++) {
                    for (int y = bottom; y < maxHeight; y++) {
                        if (random.nextFloat() < vineChance) {
                            createVineStrip(world, random, bottom, state, new BlockPos(cx + 16, y, cz + z));
                        }
                    }
                }
                removeUnsupportedVines(world, state.getBlock(), cx + 16, cz, cx + 16, cz + 14, bottom, maxHeight);
            }
        }
        if (info.getXmax().hasBuilding) {
            if (world.getChunk(chunkX + 1, chunkZ).getStatus().isOrAfter(ChunkStatus.FEATURES)) {
                BuildingInfo adjacent = info.getXmax();
                int bottom = Math.max(info.getCityGroundLevel() + 3, info.hasBuilding ? maxHeight : (info.getCityGroundLevel() + 3));
                BlockState state = worldSettings.vineEast();
                for (int z = 0; z < 15; z++) {
                    for (int y = bottom; y < (adjacent.getMaxHeight()); y++) {
                        if (random.nextFloat() < vineChance) {
                            createVineStrip(world, random, bottom, state, new BlockPos(cx + 15, y, cz + z));
                        }
                    }
                }
                removeUnsupportedVines(world, state.getBlock(), cx + 15, cz, cx + 15, cz + 14, bottom, adjacent.getMaxHeight());
            }
        }

        if (info.hasBuilding) {
            if (world.getChunk(chunkX, chunkZ + 1).getStatus().isOrAfter(ChunkStatus.FEATURES)) {
                BuildingInfo adjacent = info.getZmax();
                int bottom = Math.max(adjacent.getCityGroundLevel() + 3, adjacent.hasBuilding ? adjacent.getMaxHeight() : (adjacent.getCityGroundLevel() + 3));
                BlockState state = worldSettings.vineNorth();
                for (int x = 0; x < 15; x++) {
                    for (int y = bottom; y < maxHeight; y++) {
                        if (random.nextFloat() < vineChance) {
                            createVineStrip(world, random, bottom, state, new BlockPos(cx + x, y, cz + 16));
                        }
                    }
                }
                removeUnsupportedVines(world, state.getBlock(), cx, cz + 16, cx + 14, cz + 16, bottom, maxHeight);
            }
        }
        if (info.getZmax().hasBuilding) {
            if (world.getChunk(chunkX, chunkZ + 1).getStatus().isOrAfter(ChunkStatus.FEATURES)) {
                BuildingInfo adjacent = info.getZmax();
                int bottom = Math.max(info.getCityGroundLevel() + 3, info.hasBuilding ? maxHeight : (info.getCityGroundLevel() + 3));
                BlockState state = worldSettings.vineSouth();
                for (int x = 0; x < 15; x++) {
                    for (int y = bottom; y < (adjacent.getMaxHeight()); y++) {
                        if (random.nextFloat() < vineChance) {
                            createVineStrip(world, random, bottom, state, new BlockPos(cx + x, y, cz + 15));
                        }
                    }
                }
                removeUnsupportedVines(world, state.getBlock(), cx, cz + 15, cx + 14, cz + 15, bottom, adjacent.getMaxHeight());
            }
        }
    }

    private static void createVineStrip(LevelAccessor world, RandomSource random, int bottom, BlockState state, BlockPos pos) {
        if (!world.isEmptyBlock(pos)) {
            return;
        }
        // A non-air neighbour is not necessarily a face that vines can attach to. In particular,
        // foliage and partial blocks can pass the old test and leave a client-visible floating vine
        // because worldgen uses no updates.
        if (!state.canSurvive(world, pos)) {
            return;
        }
        world.setBlock(pos, state, 0);
        pos = pos.below();
        while (pos.getY() >= bottom && random.nextFloat() < .8f) {
            if (!world.isEmptyBlock(pos)) {
                return;
            }
            if (!state.canSurvive(world, pos)) {
                return;
            }
            world.setBlock(pos, state, 0);
            pos = pos.below();
        }
    }

    /**
     * Remove invalid configured vines from a processed building boundary. Work from the top down
     * so an orphaned hanging strip is removed completely in this pass.
     */
    private static void removeUnsupportedVines(LevelAccessor world, Block vineBlock,
                                               int minX, int minZ, int maxX, int maxZ,
                                               int bottom, int top) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = top - 1; y >= bottom; y--) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    pos.set(x, y, z);
                    BlockState existing = world.getBlockState(pos);
                    if (existing.is(vineBlock) && !existing.canSurvive(world, pos)) {
                        world.removeBlock(pos, false);
                    }
                }
            }
        }
    }

    /**
     * Validate both sides of this chunk's boundary after all Lost Cities changes have been
     * committed. A city or transition chunk can remove the support of a vanilla vine stored in
     * its neighbour. That neighbour may already have completed generation, and bulk block changes
     * do not send cross-chunk shape updates at the FEATURES stage.
     */
    private static void removeUnsupportedBoundaryVines(ChunkCoord coord, LevelAccessor world) {
        int cx = coord.chunkX() << 4;
        int cz = coord.chunkZ() << 4;

        for (int x = cx - 1; x <= cx + 16; x++) {
            removeUnsupportedVinesInColumn(world, x, cz - 1);
            removeUnsupportedVinesInColumn(world, x, cz);
            removeUnsupportedVinesInColumn(world, x, cz + 15);
            removeUnsupportedVinesInColumn(world, x, cz + 16);
        }
        for (int z = cz + 1; z <= cz + 14; z++) {
            removeUnsupportedVinesInColumn(world, cx - 1, z);
            removeUnsupportedVinesInColumn(world, cx, z);
            removeUnsupportedVinesInColumn(world, cx + 15, z);
            removeUnsupportedVinesInColumn(world, cx + 16, z);
        }
    }

    private static void removeUnsupportedVinesInColumn(LevelAccessor world, int x, int z) {
        int top = Math.min(world.getMaxBuildHeight() - 1,
                world.getHeight(Heightmap.Types.WORLD_SURFACE, x, z));
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, top, z);
        for (int y = top; y >= world.getMinBuildHeight(); y--) {
            pos.setY(y);
            BlockState existing = world.getBlockState(pos);
            if (existing.getBlock() instanceof VineBlock && !existing.canSurvive(world, pos)) {
                world.removeBlock(pos, false);
            }
        }
    }


    public static void fix(IDimensionInfo info, ChunkCoord coord) {
        RandomSource random = GenerationContext.current().random();
        random.setSeed(info.getSeed() ^ (long) coord.chunkX() * 341873128712L ^ (long) coord.chunkZ() * 132897987541L);
        generateVines(coord, info.getWorld(), info, random);
        executePostTodo(coord, info);
        removeUnsupportedBoundaryVines(coord, info.getWorld());
    }
}
