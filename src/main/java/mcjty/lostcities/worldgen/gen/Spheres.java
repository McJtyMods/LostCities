package mcjty.lostcities.worldgen.gen;

import mcjty.lostcities.api.ILostWorldsChunkGenerator;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.Tools;
import mcjty.lostcities.worldgen.ChunkDriver;
import mcjty.lostcities.worldgen.ChunkFixer;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.LostCityTerrainFeature;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.CitySphere;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class Spheres {

    public static void generateSpheres(LostCityTerrainFeature feature, WorldGenRegion region, ChunkAccess chunk) {
        IDimensionInfo provider = feature.provider;
        LostCityProfile profile = feature.profile;
        ChunkDriver driver = feature.driver;
        // Do the city spheres
        if (profile.isSpace() || profile.isSpheres()) {
            LevelAccessor oldRegion = driver.getRegion();
            ChunkAccess oldChunk = driver.getPrimer();
            driver.setPrimer(region, chunk);

            int chunkX = chunk.getPos().x;
            int chunkZ = chunk.getPos().z;
            ChunkCoord coord = new ChunkCoord(provider.getType(), chunkX, chunkZ);

            CitySphere sphere = CitySphere.getCitySphere(coord, provider);
            CitySphere.initSphere(sphere, provider);   // Make sure city sphere information is complete
            if (sphere.isEnabled()) {
                float radius = sphere.getRadius();
                BlockPos cc = sphere.getCenterPos();
                int cx = cc.getX() - (chunkX << 4);
                int cz = cc.getZ() - (chunkZ << 4);
                fillSphere(feature, cx, profile.GROUNDLEVEL, cz, (int) radius, sphere.getGlassBlock(), sphere.getSideBlock());
            }

            if (profile.isSpace()) {
                BuildingInfo info = BuildingInfo.getBuildingInfo(coord, provider);
                Monorails.generateMonorails(feature, info);
            }

            driver.actuallyGenerate(chunk);
            driver.setPrimer(oldRegion, oldChunk);
            ChunkFixer.fix(provider, coord);
        }
    }

    private static void fillSphere(LostCityTerrainFeature feature, int centerx, int centery, int centerz, int radius,
                                   BlockState glass, BlockState sideBlock) {
        IDimensionInfo provider = feature.provider;
        ChunkDriver driver = feature.driver;
        LostCityProfile profile = feature.profile;
        BlockState air = Blocks.AIR.defaultBlockState();
        double sqradius = radius * radius;
        double sqradiusOffset = (radius - 2) * (radius - 2);
        double sqradiusOuter = (radius + 2) * (radius + 2);

        int minY = Math.max(provider.getWorld().getMinBuildHeight(), centery - radius - 1);
        int maxY = Math.min(provider.getWorld().getMaxBuildHeight(), centery + radius + 1);
        int seaLevel = Tools.getSeaLevel(provider.getWorld());
        ChunkGenerator generator;
        if (provider.getWorld() instanceof WorldGenRegion region) {
            generator = ((ServerChunkCache) region.getChunkSource()).getGenerator();
        } else {
            generator = ((ServerLevel) provider.getWorld()).getChunkSource().getGenerator();
        }
        int outerSeaLevel = -1000;
        if (generator instanceof ILostWorldsChunkGenerator lw) {
            Integer o = lw.getOuterSeaLevel();
            if (o != null) {
                outerSeaLevel = o;
            }
        }

        for (int x = 0; x < 16; x++) {
            double dxdx = (x - centerx) * (x - centerx);
            for (int z = 0; z < 16; z++) {
                double dzdz = (z - centerz) * (z - centerz);
                int bottom = Integer.MAX_VALUE;
                if (dxdx + dzdz <= sqradius) {
                    driver.current(x, minY, z);
                    for (int y = minY; y <= centery; y++) {
                        double dydy = (y - centery) * (y - centery);
                        double sqdist = dxdx + dydy + dzdz;
                        if (sqdist <= sqradius && sqdist >= sqradiusOffset) {
                            if (y < bottom) {
                                bottom = y - 1;
                            }
                            driver.block(sideBlock);
                        }
                        driver.incY();
                    }
                    for (int y = centery + 1; y < maxY; y++) {
                        double dydy = (y - centery) * (y - centery);
                        double sqdist = dxdx + dydy + dzdz;
                        if (sqdist <= sqradius) {
                            if (sqdist >= sqradiusOffset) {
                                driver.block(glass);
                            }
                        } else {
                            // Optionally clear above the sphere
                            int yy = y;
                            if (profile.CITYSPHERE_CLEARABOVE > 0) {
                                int mY = Math.min(provider.getWorld().getMaxBuildHeight(), y + profile.CITYSPHERE_CLEARABOVE);
                                while (yy <= mY) {
                                    driver.block(yy <= outerSeaLevel ? feature.liquid : air);
                                    driver.incY();
                                    yy++;
                                }
                            }
                            if (profile.CITYSPHERE_CLEARABOVE_UNTIL_AIR) {
                                // Clear until we hit air
                                while (driver.getBlock() != air) {
                                    driver.block(yy <= outerSeaLevel ? feature.liquid : air);
                                    driver.incY();
                                    yy++;
                                }
                            }
                            // Optionall clear below the sphere
                            yy = bottom;
                            if (profile.CITYSPHERE_CLEARBELOW > 0 && bottom != Integer.MAX_VALUE) {
                                driver.current(x, yy, z);
                                int mY = Math.max(provider.getWorld().getMinBuildHeight(), bottom - profile.CITYSPHERE_CLEARBELOW);
                                while (yy >= mY) {
                                    driver.block(yy <= outerSeaLevel ? feature.liquid : air);
                                    driver.decY();
                                    yy--;
                                }
                            }
                            if (profile.CITYSPHERE_CLEARBELOW_UNTIL_AIR && bottom != Integer.MAX_VALUE) {
                                // Clear until we hit air or go below build limit
                                driver.current(x, yy, z);
                                while (driver.getBlock() != (yy <= seaLevel ? feature.liquid : air) && yy > provider.getWorld().getMinBuildHeight()) {
                                    driver.block(yy <= outerSeaLevel ? feature.liquid : air);
                                    driver.decY();
                                    yy--;
                                }
                            }
                            break;
                        }
                        driver.incY();
                    }
                } else if (dxdx + dzdz <= sqradiusOuter) {
                    // If we are in a space profile then we clear the sphere area too
                    if (profile.isFloating() || profile.isSpace()) {
                        driver.current(x, minY, z);
                        for (int y = minY; y < maxY; y++) {
                            driver.block(y <= outerSeaLevel ? feature.liquid : air);
                            driver.incY();
                        }
                    }
                }
            }
        }
    }
}
