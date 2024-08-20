package mcjty.lostcities.worldgen.gen;

import mcjty.lostcities.worldgen.ChunkDriver;
import mcjty.lostcities.worldgen.LostCityTerrainFeature;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.cityassets.CompiledPalette;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;

public class Corridors {
    public static void generateCorridors(LostCityTerrainFeature feature, BuildingInfo info, boolean xRail, boolean zRail) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState base = info.profile.getBaseBlock();
        BlockState railx = Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.EAST_WEST);
        BlockState railz = Blocks.RAIL.defaultBlockState();
        ChunkDriver driver = feature.driver;

        Character corridorRoofBlock = info.getCityStyle().getCorridorRoofBlock();
        Character corridorGlassBlock = info.getCityStyle().getCorridorGlassBlock();
        CompiledPalette palette = info.getCompiledPalette();

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                BlockState b;
                if ((xRail && z >= 7 && z <= 10) || (zRail && x >= 7 && x <= 10)) {
                    int height = info.groundLevel - 6;
                    if (xRail && z == 10) {
                        b = railx;
                    } else if (zRail && x == 10) {
                        b = railz;
                    } else {
                        b = air;
                    }
                    driver.current(x, height, z).add(palette.get(corridorRoofBlock)).add(b).add(air).add(air);

                    if ((xRail && x == 7 && (z == 8 || z == 9)) || (zRail && z == 7 && (x == 8 || x == 9))) {
                        driver.add(palette.get(corridorGlassBlock));
                        BlockPos pos = driver.getCurrentCopy();
                        Character glowstoneChar = info.getCityStyle().getGlowstoneBlock();
                        BlockState glowstone = glowstoneChar == null ? Blocks.GLOWSTONE.defaultBlockState() : palette.get(glowstoneChar);
                        driver.add(glowstone);
                        LostCityTerrainFeature.updateNeeded(info, pos, Block.UPDATE_CLIENTS);
                    } else {
                        BlockState roof = palette.get(corridorRoofBlock);
                        driver.add(roof).add(roof);
                    }
                } else {
                    driver.setBlockRange(x, info.groundLevel - 5, z, info.getCityGroundLevel(), base);
                }
            }
        }
    }

    public static void generateCorridorConnections(ChunkDriver driver, BuildingInfo info) {
        if (info.getXmin().hasXCorridor()) {
            int x = 0;
            for (int z = 7; z <= 10; z++) {
                driver.setBlockRangeToAir(x, info.groundLevel - 5, z, info.groundLevel - 2);
            }
        }
        if (info.getXmax().hasXCorridor()) {
            int x = 15;
            for (int z = 7; z <= 10; z++) {
                driver.setBlockRangeToAir(x, info.groundLevel - 5, z, info.groundLevel - 2);
            }
        }
        if (info.getZmin().hasZCorridor()) {
            int z = 0;
            for (int x = 7; x <= 10; x++) {
                driver.setBlockRangeToAir(x, info.groundLevel - 5, z, info.groundLevel - 2);
            }
        }
        if (info.getZmax().hasZCorridor()) {
            int z = 15;
            for (int x = 7; x <= 10; x++) {
                driver.setBlockRangeToAir(x, info.groundLevel - 5, z, info.groundLevel - 2);
            }
        }
    }

}
