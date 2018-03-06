package mcjty.lostcities.dimensions.world.terraingen;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.dimensions.world.LostCityChunkGenerator;
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

    public void setup(World world, LostCityChunkGenerator provider) {
        this.provider = provider;
        this.surfaceNoise = new NoiseGeneratorPerlin(provider.rand, 4);
    }


    public void generate(int chunkX, int chunkZ, ChunkPrimer primer) {
        // Find the city center and get the city style for the center of the city
        CitySphere sphere = CitySphere.getCitySphere(chunkX, chunkZ, provider);
        CitySphere.initSphere(sphere, provider);   // Make sure city sphere information is complete

        LostCityProfile profile = provider.getProfile();
        LostCityProfile profileOut = provider.getOutsideProfile();
        boolean outsideLandscape = profile.CITYSPHERE_LANDSCAPE_OUTSIDE;
        Character baseLiquid = LostCitiesTerrainGenerator.liquidChar;
        char airChar = LostCitiesTerrainGenerator.airChar;

        this.surfaceBuffer = this.surfaceNoise.getRegion(this.surfaceBuffer, (chunkX * 16), (chunkZ * 16), 16, 16, 1.0 / 16.0, 1.0 / 16.0, 1.0D);

        if (sphere.isEnabled()) {
            float radius = sphere.getRadius();
            BlockPos cc = sphere.getCenterPos();
            int cx = cc.getX() - chunkX * 16;
            int cz = cc.getZ() - chunkZ * 16;
            fillSphere(primer, cx, profile.GROUNDLEVEL, cz, (int) radius, sphere.getGlassBlock(), sphere.getBaseBlock(), sphere.getSideBlock(), baseLiquid, outsideLandscape);
        } else if (outsideLandscape) {
            int waterLevel = profileOut.GROUNDLEVEL - profileOut.WATERLEVEL_OFFSET;
            for (int x = 0 ; x < 16 ; x++) {
                for (int z = 0 ; z < 16 ; z++) {
                    double vr = profile.CITYSPHERE_OUTSIDE_SURFACE_VARIATION < 0.01f ? 0 : surfaceBuffer[x + z * 16] / profile.CITYSPHERE_OUTSIDE_SURFACE_VARIATION;
                    int index = (x * 16 + z) * 256;
                    for (int y = 0; y <= Math.max(waterLevel, profileOut.GROUNDLEVEL + 30) ; y++) {
                        if (y == 0) {
                            primer.data[index++] = LostCitiesTerrainGenerator.bedrockChar;
                        } else if (y <= vr + profileOut.GROUNDLEVEL) {
                            primer.data[index++] = LostCitiesTerrainGenerator.baseChar;
                        } else if (y <= waterLevel) {
                            primer.data[index++] = baseLiquid;
                        } else {
                            primer.data[index++] = airChar;
                        }
                    }
                }
            }
        }
    }

    private void fillSphere(ChunkPrimer primer, int centerx, int centery, int centerz, int radius,
                            char glass, char block, char sideBlock, char liquidChar, boolean outsideLandscape) {
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
                int index = (x * 16 + z) * 256;
                double vo = profile.CITYSPHERE_OUTSIDE_SURFACE_VARIATION < 0.01f ? 0 : surfaceBuffer[x + z * 16] / profile.CITYSPHERE_OUTSIDE_SURFACE_VARIATION;
                double vr = profile.CITYSPHERE_SURFACE_VARIATION < 0.01f ? 0 : surfaceBuffer[x + z * 16] / profile.CITYSPHERE_SURFACE_VARIATION;
                if (outsideLandscape) {
                    for (int y = 0 ; y <= Math.max(Math.min(centery+radius, 255), waterLevel) ; y++) {
                        double dydy = (y-centery) * (y-centery);
                        double sqdist = dxdx + dydy + dzdz;
                        if (y == 0) {
                            primer.data[index + y] = LostCitiesTerrainGenerator.bedrockChar;
                        } else if (sqdist <= sqradius) {
                            if (sqdist >= sqradiusOffset) {
                                if (y > centery) {
                                    primer.data[index + y] = glass;
                                } else {
                                    primer.data[index + y] = sideBlock;
                                }
                            } else {
                                if (y < centery + vr) {
                                    primer.data[index + y] = block;
                                } else if (y < waterLevel) {
                                    primer.data[index + y] = liquidChar;
                                }
                            }
                        } else if (y <= vo + profileOut.GROUNDLEVEL) {
                            primer.data[index + y] = LostCitiesTerrainGenerator.baseChar;
                        } else if (y <= waterLevelOut) {
                            primer.data[index + y] = liquidChar;
                        }
                    }
                } else {
                    for (int y = Math.max(centery-radius, 0) ; y <= Math.min(centery+radius, 255) ; y++) {
                        double dydy = (y-centery) * (y-centery);
                        double sqdist = dxdx + dydy + dzdz;
                        if (sqdist <= sqradius) {
                            if (sqdist >= sqradiusOffset) {
                                if (y > centery) {
                                    primer.data[index + y] = glass;
                                } else {
                                    primer.data[index + y] = sideBlock;
                                }
                            } else {
                                if (y < centery + vr) {
                                    primer.data[index + y] = block;
                                } else if (y < waterLevel) {
                                    primer.data[index + y] = liquidChar;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void replaceBlocksForBiome(int chunkX, int chunkZ, ChunkPrimer primer, Biome[] biomes) {
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
                        genBiomeTerrain(biome, primer, chunkX * 16 + z, chunkZ * 16 + x, outsideGround, outsideWater);
                    } else {
                        genBiomeTerrain(biome, primer, chunkX * 16 + z, chunkZ * 16 + x, groundlevel, water);
                    }
                }
            }
        } else {
            for (int z = 0; z < 16; ++z) {
                for (int x = 0; x < 16; ++x) {
                    Biome biome = biomes[x + z * 16];
                    genBiomeTerrain(biome, primer, chunkX * 16 + z, chunkZ * 16 + x, outsideGround, outsideWater);
                }
            }
        }

    }

    private void genBiomeTerrain(Biome Biome, ChunkPrimer primer, int x, int z, int topLevel, int waterLevel) {
        char air = LostCitiesTerrainGenerator.airChar;
        char baseBlock = LostCitiesTerrainGenerator.baseChar;
        char gravelBlock = LostCitiesTerrainGenerator.gravelChar;

        char fillerBlock = (char) Block.BLOCK_STATE_IDS.get(Biome.fillerBlock);
        char topBlock = (char) Block.BLOCK_STATE_IDS.get(Biome.topBlock);

        int cx = x & 15;
        int cz = z & 15;

        int bottomIndex = ((cz * 16) + cx) * 256;

        int cnt = 0;
        for (int y = topLevel + 20 ; y >= topLevel - 8 ; y--) {
            int index = bottomIndex + y;
            if (primer.data[index] == baseBlock) {
                // @todo experiment with more realistic toppings for under water
//                if (y == waterLevel-2) {
//                    primer.data[index] = gravelBlock;
                if (cnt == 0) {
                    primer.data[index] = topBlock;
                } else if (cnt < 3) {
                    primer.data[index] = fillerBlock;
                } else {
                    break;
                }
                cnt++;
            }
        }
    }
}