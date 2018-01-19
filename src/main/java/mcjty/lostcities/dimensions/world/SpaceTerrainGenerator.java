package mcjty.lostcities.dimensions.world;

import mcjty.lostcities.dimensions.world.lost.BuildingInfo;
import mcjty.lostcities.dimensions.world.lost.City;
import mcjty.lostcities.dimensions.world.lost.CitySphere;
import mcjty.lostcities.varia.ChunkCoord;
import net.minecraft.block.Block;
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
        ChunkCoord cityCenter = CitySphere.getCityCenterForSpace(chunkX, chunkZ, provider);
        BuildingInfo info = BuildingInfo.getBuildingInfo(cityCenter.getChunkX(), cityCenter.getChunkZ(), provider);
        CitySphere sphere = info.getCitySphere();

        boolean flooded = provider.profile.CITYSPHERE_FLOODED;
        Character baseLiquid = flooded ? LostCitiesTerrainGenerator.liquidChar : null;
        int waterLevel = provider.profile.GROUNDLEVEL - provider.profile.WATERLEVEL_OFFSET;

        if (sphere.isEnabled()) {
            this.surfaceBuffer = this.surfaceNoise.getRegion(this.surfaceBuffer, (chunkX * 16), (chunkZ * 16), 16, 16, 1.0 / 16.0, 1.0 / 16.0, 1.0D);
            int cx = cityCenter.getChunkX();
            int cz = cityCenter.getChunkZ();
            float radius = City.getCityRadius(cx, cz, provider) * provider.profile.CITYSPHERE_FACTOR;
            fillSphere(primer, (cx - chunkX) * 16 + 8, provider.profile.GROUNDLEVEL, (cz - chunkZ) * 16 + 8, (int) radius, sphere.getGlassBlock(), sphere.getBaseBlock(), sphere.getSideBlock(), baseLiquid);
        } else if (flooded) {
            this.surfaceBuffer = this.surfaceNoise.getRegion(this.surfaceBuffer, (chunkX * 16), (chunkZ * 16), 16, 16, 1.0 / 16.0, 1.0 / 16.0, 1.0D);
            for (int x = 0 ; x < 16 ; x++) {
                for (int z = 0 ; z < 16 ; z++) {
                    double vr = provider.profile.CITYSPHERE_SURFACE_VARIATION < 0.01f ? 0 : surfaceBuffer[x + z * 16] / provider.profile.CITYSPHERE_SURFACE_VARIATION;
                    int index = (x * 16 + z) * 256;
                    for (int y = 0 ; y <= waterLevel ; y++) {
                        if (y == 0) {
                            primer.data[index++] = LostCitiesTerrainGenerator.bedrockChar;
                        } else if (y <= vr+10) {
                            primer.data[index++] = LostCitiesTerrainGenerator.baseChar;
                        } else {
                            primer.data[index++] = baseLiquid;
                        }
                    }
                }
            }
        }
    }

    private void fillSphere(ChunkPrimer primer, int centerx, int centery, int centerz, int radius,
                            char glass, char block, char sideBlock, Character liquidChar) {
        double sqradius = radius * radius;
        double sqradiusOffset = (radius-2) * (radius-2);
        int waterLevel = provider.profile.GROUNDLEVEL - provider.profile.WATERLEVEL_OFFSET;

        for (int x = 0 ; x < 16 ; x++) {
            double dxdx = (x-centerx) * (x-centerx);
            for (int z = 0 ; z < 16 ; z++) {
                double dzdz = (z-centerz) * (z-centerz);
                int index = (x * 16 + z) * 256;
                double vr = provider.profile.CITYSPHERE_SURFACE_VARIATION < 0.01f ? 0 : surfaceBuffer[x + z * 16] / provider.profile.CITYSPHERE_SURFACE_VARIATION;
                if (liquidChar != null) {
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
                                }
                            }
                        } else if (y <= vr+10) {
                            primer.data[index + y] = LostCitiesTerrainGenerator.baseChar;
                        } else if (y <= waterLevel) {
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
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    public void replaceBlocksForBiome(int chunkX, int chunkZ, ChunkPrimer primer, Biome[] Biomes) {
        ChunkCoord cityCenter = CitySphere.getCityCenterForSpace(chunkX, chunkZ, provider);
        BuildingInfo info = BuildingInfo.getBuildingInfo(cityCenter.getChunkX(), cityCenter.getChunkZ(), provider);
        CitySphere sphere = info.getCitySphere();

        for (int k = 0; k < 16; ++k) {
            for (int l = 0; l < 16; ++l) {
                Biome Biome = Biomes[l + k * 16];
                genBiomeTerrain(Biome, primer, chunkX * 16 + k, chunkZ * 16 + l, sphere.getGlassBlock());
            }
        }
    }

    public final void genBiomeTerrain(Biome Biome, ChunkPrimer primer, int x, int z, char glassBlock) {
        char air = LostCitiesTerrainGenerator.airChar;
        char baseBlock = LostCitiesTerrainGenerator.baseChar;

        int topLevel = provider.profile.GROUNDLEVEL;

        char fillerBlock = (char) Block.BLOCK_STATE_IDS.get(Biome.fillerBlock);
        char topBlock = (char) Block.BLOCK_STATE_IDS.get(Biome.topBlock);

        int cx = x & 15;
        int cz = z & 15;

        int bottomIndex = ((cz * 16) + cx) * 256;

        int cnt = 0;
        for (int y = topLevel + 20 ; y >= topLevel - 20 ; y--) {
            int index = bottomIndex + y;
            if (primer.data[index] == air || primer.data[index] == glassBlock) {
                // Do nothing
            } else if (primer.data[index] == baseBlock) {
                if (cnt == 0) {
                    primer.data[index] = topBlock;
                } else if (cnt < 3) {
                    primer.data[index] = fillerBlock;
                } else {
                    break;
                }
                cnt++;
            } else {
                // Stop
                break;
            }
        }
    }
}