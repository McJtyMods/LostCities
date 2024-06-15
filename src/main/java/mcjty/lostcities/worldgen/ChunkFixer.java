package mcjty.lostcities.worldgen;

import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.Random;

public class ChunkFixer {


    private static void executePostTodo(ChunkCoord coord, IDimensionInfo provider) {
        BuildingInfo info = BuildingInfo.getBuildingInfo(coord, provider);
        info.getPostTodo().forEach((pos, runnable) -> runnable.run());
        info.clearPostTodo();
    }

    private static void generateVines(ChunkCoord coord, LevelAccessor world, IDimensionInfo provider) {
        float vineChance = provider.getProfile().VINE_CHANCE;
        if (vineChance < 0.000001) {
            return;
        }
        int chunkX = coord.chunkX();
        int chunkZ = coord.chunkZ();
        int cx = chunkX * 16;
        int cz = chunkZ * 16;
        BuildingInfo info = BuildingInfo.getBuildingInfo(coord, provider);

        int maxHeight = info.getMaxHeight();

        if (info.hasBuilding) {
            if (world.getChunk(coord.chunkX() + 1, coord.chunkZ()).getPersistedStatus().isOrAfter(ChunkStatus.FEATURES)) {
                BuildingInfo adjacent = info.getXmax();
                int bottom = Math.max(adjacent.getCityGroundLevel() + 3, adjacent.hasBuilding ? adjacent.getMaxHeight() : (adjacent.getCityGroundLevel() + 3));
                for (int z = 0; z < 15; z++) {
                    for (int y = bottom; y < maxHeight; y++) {
                        if (world.getRandom().nextFloat() < vineChance) {
                            createVineStrip(world, bottom, VineBlock.WEST, new BlockPos(cx + 16, y, cz + z), new BlockPos(cx + 15, y, cz + z));
                        }
                    }
                }
            }
        }
        if (info.getXmax().hasBuilding) {
            if (world.getChunk(chunkX + 1, chunkZ).getStatus().isOrAfter(ChunkStatus.FEATURES)) {
                BuildingInfo adjacent = info.getXmax();
                int bottom = Math.max(info.getCityGroundLevel() + 3, info.hasBuilding ? maxHeight : (info.getCityGroundLevel() + 3));
                for (int z = 0; z < 15; z++) {
                    for (int y = bottom; y < (adjacent.getMaxHeight()); y++) {
                        if (world.getRandom().nextFloat() < vineChance) {
                            createVineStrip(world, bottom, VineBlock.EAST, new BlockPos(cx + 15, y, cz + z), new BlockPos(cx + 16, y, cz + z));
                        }
                    }
                }
            }
        }

        if (info.hasBuilding) {
            if (world.getChunk(chunkX, chunkZ + 1).getStatus().isOrAfter(ChunkStatus.FEATURES)) {
                BuildingInfo adjacent = info.getZmax();
                int bottom = Math.max(adjacent.getCityGroundLevel() + 3, adjacent.hasBuilding ? adjacent.getMaxHeight() : (adjacent.getCityGroundLevel() + 3));
                for (int x = 0; x < 15; x++) {
                    for (int y = bottom; y < maxHeight; y++) {
                        if (world.getRandom().nextFloat() < vineChance) {
                            createVineStrip(world, bottom, VineBlock.NORTH, new BlockPos(cx + x, y, cz + 16), new BlockPos(cx + x, y, cz + 15));
                        }
                    }
                }
            }
        }
        if (info.getZmax().hasBuilding) {
            if (world.getChunk(chunkX, chunkZ + 1).getStatus().isOrAfter(ChunkStatus.FEATURES)) {
                BuildingInfo adjacent = info.getZmax();
                int bottom = Math.max(info.getCityGroundLevel() + 3, info.hasBuilding ? maxHeight : (info.getCityGroundLevel() + 3));
                for (int x = 0; x < 15; x++) {
                    for (int y = bottom; y < (adjacent.getMaxHeight()); y++) {
                        if (world.getRandom().nextFloat() < vineChance) {
                            createVineStrip(world, bottom, VineBlock.SOUTH, new BlockPos(cx + x, y, cz + 15), new BlockPos(cx + x, y, cz + 16));
                        }
                    }
                }
            }
        }
    }

    private static void createVineStrip(LevelAccessor world, int bottom, BooleanProperty direction, BlockPos pos, BlockPos vineHolderPos) {
        if (world.isEmptyBlock(vineHolderPos)) {
            return;
        }
        if (!world.isEmptyBlock(pos)) {
            return;
        }
        BlockState state = Blocks.VINE.defaultBlockState().setValue(direction, true);
        world.setBlock(pos, state, 0);
        pos = pos.below();
        while (pos.getY() >= bottom && world.getRandom().nextFloat() < .8f) {
            if (!world.isEmptyBlock(pos)) {
                return;
            }
            world.setBlock(pos, state, 0);
            pos = pos.below();
        }
    }


    public static void fix(IDimensionInfo info, ChunkCoord coord) {
        generateVines(coord, info.getWorld(), info);
        executePostTodo(coord, info);
    }
}
