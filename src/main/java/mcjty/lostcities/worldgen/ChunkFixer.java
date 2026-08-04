package mcjty.lostcities.worldgen;

import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.cityassets.WorldStyle;
import mcjty.lostcities.worldgen.lost.regassets.data.WorldSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.chunk.status.ChunkStatus;

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
            if (world.getChunk(coord.chunkX() + 1, coord.chunkZ()).getPersistedStatus().isOrAfter(ChunkStatus.FEATURES)) {
                BuildingInfo adjacent = info.getXmax();
                int bottom = Math.max(adjacent.getCityGroundLevel() + 3, adjacent.hasBuilding ? adjacent.getMaxHeight() : (adjacent.getCityGroundLevel() + 3));
                BlockState state = worldSettings.vineWest();
                for (int z = 0; z < 15; z++) {
                    for (int y = bottom; y < maxHeight; y++) {
                        if (random.nextFloat() < vineChance) {
                            createVineStrip(world, random, bottom, state, new BlockPos(cx + 16, y, cz + z), new BlockPos(cx + 15, y, cz + z));
                        }
                    }
                }
            }
        }
        if (info.getXmax().hasBuilding) {
            if (world.getChunk(chunkX + 1, chunkZ).getPersistedStatus().isOrAfter(ChunkStatus.FEATURES)) {
                BuildingInfo adjacent = info.getXmax();
                int bottom = Math.max(info.getCityGroundLevel() + 3, info.hasBuilding ? maxHeight : (info.getCityGroundLevel() + 3));
                BlockState state = worldSettings.vineEast();
                for (int z = 0; z < 15; z++) {
                    for (int y = bottom; y < (adjacent.getMaxHeight()); y++) {
                        if (random.nextFloat() < vineChance) {
                            createVineStrip(world, random, bottom, state, new BlockPos(cx + 15, y, cz + z), new BlockPos(cx + 16, y, cz + z));
                        }
                    }
                }
            }
        }

        if (info.hasBuilding) {
            if (world.getChunk(chunkX, chunkZ + 1).getPersistedStatus().isOrAfter(ChunkStatus.FEATURES)) {
                BuildingInfo adjacent = info.getZmax();
                int bottom = Math.max(adjacent.getCityGroundLevel() + 3, adjacent.hasBuilding ? adjacent.getMaxHeight() : (adjacent.getCityGroundLevel() + 3));
                BlockState state = worldSettings.vineNorth();
                for (int x = 0; x < 15; x++) {
                    for (int y = bottom; y < maxHeight; y++) {
                        if (random.nextFloat() < vineChance) {
                            createVineStrip(world, random, bottom, state, new BlockPos(cx + x, y, cz + 16), new BlockPos(cx + x, y, cz + 15));
                        }
                    }
                }
            }
        }
        if (info.getZmax().hasBuilding) {
            if (world.getChunk(chunkX, chunkZ + 1).getPersistedStatus().isOrAfter(ChunkStatus.FEATURES)) {
                BuildingInfo adjacent = info.getZmax();
                int bottom = Math.max(info.getCityGroundLevel() + 3, info.hasBuilding ? maxHeight : (info.getCityGroundLevel() + 3));
                BlockState state = worldSettings.vineSouth();
                for (int x = 0; x < 15; x++) {
                    for (int y = bottom; y < (adjacent.getMaxHeight()); y++) {
                        if (random.nextFloat() < vineChance) {
                            createVineStrip(world, random, bottom, state, new BlockPos(cx + x, y, cz + 15), new BlockPos(cx + x, y, cz + 16));
                        }
                    }
                }
            }
        }
    }

    private static void createVineStrip(LevelAccessor world, RandomSource random, int bottom, BlockState state, BlockPos pos, BlockPos vineHolderPos) {
        if (world.isEmptyBlock(vineHolderPos)) {
            return;
        }
        if (!world.isEmptyBlock(pos)) {
            return;
        }
        world.setBlock(pos, state, 0);
        pos = pos.below();
        while (pos.getY() >= bottom && random.nextFloat() < .8f) {
            if (!world.isEmptyBlock(pos)) {
                return;
            }
            world.setBlock(pos, state, 0);
            pos = pos.below();
        }
    }


    public static void fix(IDimensionInfo info, ChunkCoord coord) {
        RandomSource random = GenerationContext.current().random();
        random.setSeed(info.getSeed() ^ (long) coord.chunkX() * 341873128712L ^ (long) coord.chunkZ() * 132897987541L);
        generateVines(coord, info.getWorld(), info, random);
        executePostTodo(coord, info);
    }
}
