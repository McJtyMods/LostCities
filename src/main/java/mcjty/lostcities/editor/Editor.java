package mcjty.lostcities.editor;

import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.cityassets.BuildingPart;
import mcjty.lostcities.worldgen.lost.cityassets.CompiledPalette;
import mcjty.lostcities.worldgen.lost.cityassets.Palette;
import net.minecraft.core.BlockPos;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Editor {

    public static void startEditing(BuildingPart part, ServerPlayer player, BlockPos start, ServerLevel level, IDimensionInfo dimInfo) {
        ChunkCoord coord = new ChunkCoord(dimInfo.getType(), start.getX() >> 4, start.getZ() >> 4);
        BuildingInfo info = BuildingInfo.getBuildingInfo(coord, dimInfo);
        CompiledPalette palette = info.getCompiledPalette();
        Palette partPalette = part.getLocalPalette(level);
        Palette buildingPalette = info.getBuilding().getLocalPalette(level);
        if (partPalette != null || buildingPalette != null) {
            palette = new CompiledPalette(palette, partPalette, buildingPalette);
        }

        EditorInfo editorInfo = EditorInfo.createEditorInfo(player.getUUID(), part.getName(), start);

        CompiledPalette finalPalette = palette;

        player.level().getServer().doRunTask(new TickTask(3, () -> {
            for (int y = 0; y < part.getSliceCount(); y++) {
                for (int x = 0; x < part.getXSize(); x++) {
                    for (int z = 0; z < part.getZSize(); z++) {
                        BlockPos pos = new BlockPos(info.chunkX * 16 + x, start.getY() + y, info.chunkZ * 16 + z);
                        Character character = part.getC(x, y, z);
                        BlockState state = finalPalette.get(character);
                        if (state != null) {
                            level.setBlock(pos, state, Block.UPDATE_ALL);
                        } else {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        }
                    }
                }
            }
            for (int y = 0; y < part.getSliceCount(); y++) {
                for (int x = 0; x < part.getXSize(); x++) {
                    for (int z = 0; z < part.getZSize(); z++) {
                        BlockPos pos = new BlockPos(info.chunkX * 16 + x, start.getY() + y, info.chunkZ * 16 + z);
                        Character character = part.getC(x, y, z);
                        if (finalPalette.get(character) != null) {
                            BlockState state = level.getBlockState(pos);
                            editorInfo.addPaletteEntry(character, state);
                        }
                    }
                }
            }
        }));
    }
}
