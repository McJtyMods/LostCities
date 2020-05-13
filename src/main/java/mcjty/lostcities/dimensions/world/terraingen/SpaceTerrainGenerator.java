package mcjty.lostcities.dimensions.world.terraingen;

import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.dimensions.world.LostCityChunkGenerator;
import mcjty.lostcities.dimensions.world.driver.IPrimerDriver;
import mcjty.lostcities.dimensions.world.driver.OptimizedDriver;
import mcjty.lostcities.dimensions.world.driver.SafeDriver;
import mcjty.lostcities.dimensions.world.lost.CitySphere;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

public class SpaceTerrainGenerator {
    private LostCityChunkGenerator provider;

    private NoiseGeneratorPerlin surfaceNoise;
    private double[] surfaceBuffer = new double[256];
    private IPrimerDriver driver;

    public void setup(World world, LostCityChunkGenerator provider) {
        this.provider = provider;
        this.surfaceNoise = new NoiseGeneratorPerlin(provider.rand, 4);
        driver = LostCityConfiguration.OPTIMIZED_CHUNKGEN ? new OptimizedDriver() : new SafeDriver();
    }


    public void generate(int chunkX, int chunkZ, ChunkPrimer primer, LostCitiesTerrainGenerator terrainGenerator) {
        driver.setPrimer(primer);

        // Find the city center and get the city style for the center of the city
        CitySphere sphere = CitySphere.getCitySphere(chunkX, chunkZ, provider);
        CitySphere.initSphere(sphere, provider);   // Make sure city sphere information is complete

        LostCityProfile profile = provider.getProfile();
        LostCityProfile profileOut = provider.getOutsideProfile();
        boolean outsideLandscape = profile.CITYSPHERE_LANDSCAPE_OUTSIDE;
        Character baseLiquid = terrainGenerator.liquidChar;
        Character baseChar = terrainGenerator.baseChar;
        char airChar = LostCitiesTerrainGenerator.airChar;

        this.surfaceBuffer = this.surfaceNoise.getRegion(this.surfaceBuffer, (chunkX * 16), (chunkZ * 16), 16, 16, 1.0 / 16.0, 1.0 / 16.0, 1.0D);

        if (sphere.isEnabled()) {
            float radius = sphere.getRadius();
            BlockPos cc = sphere.getCenterPos();
            int cx = cc.getX() - chunkX * 16;
            int cz = cc.getZ() - chunkZ * 16;
            fillSphere(cx, profile.GROUNDLEVEL, cz, (int) radius, sphere.getGlassBlock(), sphere.getBaseBlock(), sphere.getSideBlock(), baseLiquid, baseChar, outsideLandscape);
        } else if (outsideLandscape) {
            int waterLevel = profileOut.GROUNDLEVEL - profileOut.WATERLEVEL_OFFSET;
            for (int x = 0 ; x < 16 ; x++) {
                for (int z = 0 ; z < 16 ; z++) {
                    double vr = profile.CITYSPHERE_OUTSIDE_SURFACE_VARIATION < 0.01f ? 0 : surfaceBuffer[x + z * 16] / profile.CITYSPHERE_OUTSIDE_SURFACE_VARIATION;
                    driver.current(x, 0, z);
                    for (int y = 0; y <= Math.max(waterLevel, profileOut.GROUNDLEVEL + 30) ; y++) {
                        if (y == 0) {
                            driver.add(LostCitiesTerrainGenerator.bedrockChar);
                        } else if (y <= vr + profileOut.GROUNDLEVEL) {
                            driver.add(terrainGenerator.baseChar);
                        } else if (y < waterLevel) {
                            driver.add(baseLiquid);
                        } else {
                            driver.add(airChar);
                        }
                    }
                }
            }
        }
    }

    private void fillSphere(int centerx, int centery, int centerz, int radius,
                            char glass, char block, char sideBlock, char liquidChar, char baseChar, boolean outsideLandscape) {
        double sqradius = radius * radius;
        double sqradiusOffset = (radius-2) * (radius-2);
        LostCityProfile profile = provider.getProfile();
        LostCityProfile profileOut = provider.getOutsideProfile();
        int waterLevelOut = profileOut.GROUNDLEVEL - profileOut.WATERLEVEL_OFFSET;
        int waterLevel = profile.GROUNDLEVEL - profile.WATERLEVEL_OFFSET;

        for (int x = 0 ; x < 16 ; x++) {
            double dxdx = (x-centerx) * (x-centerx);
            for (int z = 0 ; z < 16 ; z++) {
                double dzdz = (z-centerz) * (z-centerz);
                double vo = profile.CITYSPHERE_OUTSIDE_SURFACE_VARIATION < 0.01f ? 0 : surfaceBuffer[x + z * 16] / profile.CITYSPHERE_OUTSIDE_SURFACE_VARIATION;
                double vr = profile.CITYSPHERE_SURFACE_VARIATION < 0.01f ? 0 : surfaceBuffer[x + z * 16] / profile.CITYSPHERE_SURFACE_VARIATION;
                if (outsideLandscape) {
                    driver.current(x, 0, z);
                    for (int y = 0 ; y <= Math.max(Math.min(centery+radius, 255), waterLevel) ; y++) {
                        double dydy = (y-centery) * (y-centery);
                        double sqdist = dxdx + dydy + dzdz;
                        if (y == 0) {
                            driver.block(LostCitiesTerrainGenerator.bedrockChar);
                        } else if (sqdist <= sqradius) {
                            if (sqdist >= sqradiusOffset) {
                                if (y > centery) {
                                    driver.block(glass);
                                } else {
                                    driver.block(sideBlock);
                                }
                            } else {
                                if (y < centery + vr) {
                                    driver.block(block);
                                } else if (y < waterLevel) {
                                    driver.block(liquidChar);
                                }
                            }
                        } else if (y <= vo + profileOut.GROUNDLEVEL) {
                            driver.block(baseChar);
                        } else if (y < waterLevelOut) {
                            driver.block(liquidChar);
                        }
                        driver.incY();
                    }
                } else {
                    int starty = Math.max(centery - radius, 0);
                    driver.current(x, starty, z);
                    for (int y = starty; y <= Math.min(centery+radius, 255) ; y++) {
                        double dydy = (y-centery) * (y-centery);
                        double sqdist = dxdx + dydy + dzdz;
                        if (sqdist <= sqradius) {
                            if (sqdist >= sqradiusOffset) {
                                if (y > centery) {
                                    driver.block(glass);
                                } else {
                                    driver.block(sideBlock);
                                }
                            } else {
                                if (y < centery + vr) {
                                    driver.block(block);
                                } else if (y < waterLevel) {
                                    driver.block(liquidChar);
                                }
                            }
                        }
                        driver.incY();
                    }
                }
            }
        }
    }

    public void replaceBlocksForBiome(int chunkX, int chunkZ, ChunkPrimer primer, Biome[] biomes, LostCitiesTerrainGenerator terrainGenerator) {
        CitySphere sphere = CitySphere.getCitySphere(chunkX, chunkZ, provider);

        int outsideGround = provider.getOutsideProfile().GROUNDLEVEL;
        int outsideWater = outsideGround - provider.getOutsideProfile().WATERLEVEL_OFFSET;
        int groundlevel = provider.getProfile().GROUNDLEVEL;
        int water = groundlevel - provider.getProfile().WATERLEVEL_OFFSET;

        if (sphere.isEnabled()) {

            float radius = sphere.getRadius();
            BlockPos cc = sphere.getCenterPos();
            double sqradiusOffset = (radius - 2) * (radius - 2);
            int centerx = cc.getX() - chunkX * 16;
            int centerz = cc.getZ() - chunkZ * 16;

            for (int z = 0; z < 16; ++z) {
                double dzdz = (z - centerz) * (z - centerz);
                for (int x = 0; x < 16; ++x) {
                    double dxdx = (x - centerx) * (x - centerx);
                    double sqdist = dzdz + dxdx;
                    Biome biome = biomes[x + z * 16];
                    // Even though x and z seems swapped below this code is working nevertheless
                    if (sqdist >= sqradiusOffset) {
                        genBiomeTerrain(biome, primer, chunkX * 16 + z, chunkZ * 16 + x, outsideGround, outsideWater, terrainGenerator);
                    } else {
                        genBiomeTerrain(biome, primer, chunkX * 16 + z, chunkZ * 16 + x, groundlevel, water, terrainGenerator);
                    }
                }
            }
        } else {
            for (int z = 0; z < 16; ++z) {
                for (int x = 0; x < 16; ++x) {
                    Biome biome = biomes[x + z * 16];
                    genBiomeTerrain(biome, primer, chunkX * 16 + z, chunkZ * 16 + x, outsideGround, outsideWater, terrainGenerator);
                }
            }
        }

    }

    private void genBiomeTerrain(Biome Biome, ChunkPrimer primer, int x, int z, int topLevel, int waterLevel, LostCitiesTerrainGenerator terrainGenerator) {
        driver.setPrimer(primer);
        char air = LostCitiesTerrainGenerator.airChar;
        char baseBlock = terrainGenerator.baseChar;
        char gravelBlock = LostCitiesTerrainGenerator.gravelChar;

        char fillerBlock = (char) Block.BLOCK_STATE_IDS.get(Biome.fillerBlock);
        char topBlock = (char) Block.BLOCK_STATE_IDS.get(Biome.topBlock);

        int cx = x & 15;
        int cz = z & 15;

        int cnt = 0;
        for (int y = topLevel + 20 ; y >= topLevel - 8 ; y--) {
            driver.current(cx, y, cz);
            if (driver.getBlock() == baseBlock) {
                // @todo experiment with more realistic toppings for under water
//                if (y == waterLevel-2) {
//                    primer.data[index] = gravelBlock;
                if (y < waterLevel) {
                    driver.block(gravelBlock);
                } else if (cnt == 0) {
                    driver.block(topBlock);
                } else if (cnt < 3) {
                    driver.block(fillerBlock);
                } else {
                    break;
                }
                cnt++;
            }
        }
    }
}