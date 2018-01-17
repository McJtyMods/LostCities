package mcjty.lostcities.dimensions.world;

import mcjty.lostcities.dimensions.world.lost.BuildingInfo;
import mcjty.lostcities.dimensions.world.lost.City;
import mcjty.lostcities.dimensions.world.lost.cityassets.CityStyle;
import mcjty.lostcities.varia.ChunkCoord;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

public class SpaceTerrainGenerator {
    private LostCityChunkGenerator provider;

    public void setup(World world, LostCityChunkGenerator provider) {
        this.provider = provider;
    }


    public void generate(int chunkX, int chunkZ, ChunkPrimer primer) {
        BuildingInfo info = BuildingInfo.getBuildingInfo(chunkX, chunkZ, provider);
        CityStyle cityStyle = info.getCityStyle();

        char glassBlock = info.getCompiledPalette().get(cityStyle.getSphereGlassBlock());
        char baseBlock = info.getCompiledPalette().get(cityStyle.getSphereBlock());
        char sideBlock = info.getCompiledPalette().get(cityStyle.getSphereSideBlock());
        char air = LostCitiesTerrainGenerator.airChar;
        // Find the city center
        ChunkCoord cityCenter = City.getCityCenterForSpace(chunkX, chunkZ, provider);
        int cx = cityCenter.getChunkX();
        int cz = cityCenter.getChunkZ();
        float radius = City.getCityRadius(cx, cz, provider) * 1.3f;     // @todo 1.3 should be config
        fillSphere(primer, (cx-chunkX)*16+8, provider.profile.GROUNDLEVEL, (cz-chunkZ)*16+8, (int) radius, glassBlock, baseBlock, sideBlock);
    }

    private void fillSphere(ChunkPrimer primer, int centerx, int centery, int centerz, int radius,
                            char glass, char block, char sideBlock) {
        double sqradius = radius * radius;

        for (int x = 0 ; x < 16 ; x++) {
            double dxdx = (x-centerx) * (x-centerx);
            for (int z = 0 ; z < 16 ; z++) {
                double dzdz = (z-centerz) * (z-centerz);
                int index = (x * 16 + z) * 256;
                for (int y = Math.max(centery-radius, 0) ; y <= Math.min(centery+radius, 255) ; y++) {
                    double dydy = (y-centery) * (y-centery);
                    double sqdist = dxdx + dydy + dzdz;
                    if (sqdist <= sqradius) {
                        if (y > centery) {
                            if (Math.sqrt(sqdist) >= radius-2) {
                                primer.data[index + y] = glass;
                            }
                        } else {
                            if (Math.sqrt(sqdist) >= radius-2) {
                                primer.data[index + y] = sideBlock;
                            } else {
                                primer.data[index + y] = block;
                            }
                        }
                    }
                }
            }
        }
    }


    public void replaceBlocksForBiome(int chunkX, int chunkZ, ChunkPrimer primer, Biome[] Biomes) {
//        double d0 = 0.03125D;
//        this.stoneNoise = this.surfaceNoise.getRegion(this.stoneNoise, (chunkX * 16), (chunkZ * 16), 16, 16, 0.0625D, 0.0625D, 1.0D);
//
        for (int k = 0; k < 16; ++k) {
            for (int l = 0; l < 16; ++l) {
                Biome Biome = Biomes[l + k * 16];
                genBiomeTerrain(Biome, primer, chunkX * 16 + k, chunkZ * 16 + l);
            }
        }
    }

    public final void genBiomeTerrain(Biome Biome, ChunkPrimer primer, int x, int z) {
        char air = LostCitiesTerrainGenerator.airChar;
        char baseBlock = LostCitiesTerrainGenerator.baseChar;
        char baseLiquid = air;//@LostCitiesTerrainGenerator.liquidChar;

        int topLevel = provider.profile.GROUNDLEVEL;

        char fillerBlock = (char) Block.BLOCK_STATE_IDS.get(Biome.fillerBlock);
        char topBlock = (char) Block.BLOCK_STATE_IDS.get(Biome.topBlock);
        char block = topBlock;
        char block1 = fillerBlock;

        int cx = x & 15;
        int cz = z & 15;

        // Index of the bottom of the column.
        int bottomIndex = ((cz * 16) + cx) * 256;

        for (int height = 255; height >= 0; --height) {
            int index = bottomIndex + height;

            char currentBlock = primer.data[index];
            if (currentBlock != air) {
                if (currentBlock == baseBlock) {
                    if (height >= (topLevel - 4) && height <= (topLevel + 1)) {
                        block = topBlock;
                        block1 = fillerBlock;
                    }

                    if (height < topLevel && (block == air)) {
                        if (Biome.getTemperature(new BlockPos(x, height, z)) < 0.15F) {
                            block = (char) Block.BLOCK_STATE_IDS.get(Blocks.ICE.getDefaultState());
                        } else {
                            block = baseBlock;  // No liquid since we are floating
                        }
                    }

                    if (height >= (topLevel - 1)) {
                        primer.data[index] = block;
                    } else if (height < (topLevel - 6)) {
                        block = air;
                        block1 = baseBlock;
                        primer.data[index] = fillerBlock;
                    } else {
                        primer.data[index] = block1;
                    }
                }
            }
        }
    }
}