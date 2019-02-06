package mcjty.lostcities.dimensions.world.terraingen;

import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.dimensions.world.LostCityChunkGenerator;
import mcjty.lostcities.dimensions.world.driver.IPrimerDriver;
import mcjty.lostcities.dimensions.world.driver.OptimizedDriver;
import mcjty.lostcities.dimensions.world.driver.SafeDriver;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraft.world.gen.NoiseGeneratorSimplex;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.ChunkGeneratorEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Random;

public class IslandTerrainGenerator {
    private LostCityChunkGenerator provider;
    private IPrimerDriver driver;

    private double[] densities;

    private NoiseGeneratorOctaves noiseGen1;
    private NoiseGeneratorOctaves noiseGen2;
    private NoiseGeneratorOctaves noiseGen3;
    private NoiseGeneratorOctaves noiseGen4;
    private NoiseGeneratorOctaves noiseGen5;

    private NoiseGeneratorPerlin surfaceNoise;

    private double[] stoneNoise = new double[256];
    private double[] noiseData1;
    private double[] noiseData2;
    private double[] noiseData3;
    private double[] noiseData4;
    private double[] noiseData5;
    private NoiseGeneratorSimplex islandNoise;          // @todo unusued for now

    public static final int NORMAL = 0;
    public static final int CHAOTIC = 1;
    public static final int PLATEAUS = 3;
    public static final int ISLANDS = 4;

    private final int type;
    private final double topFactor;
    private final double botFactor;
    private final int bottomOffset;

    public IslandTerrainGenerator(int type) {
        this.type = type;
        switch (type) {
            case PLATEAUS:
                topFactor = -1000.0D;
                botFactor = -300.0D;
                break;
            case ISLANDS:
                topFactor = -600.0D;
                botFactor = -200.0D;
                break;
            default:
                topFactor = -3000.0D;
                botFactor = -30.D;
                break;
        }
        if (type == PLATEAUS) {
            bottomOffset = 14;
        } else if (type == ISLANDS) {
            bottomOffset = 11;
        } else {
            bottomOffset = 8;
        }
    }

    public void setup(World world, LostCityChunkGenerator provider) {
        this.provider = provider;

        this.noiseGen1 = new NoiseGeneratorOctaves(provider.rand, 16);
        this.noiseGen2 = new NoiseGeneratorOctaves(provider.rand, 16);
        this.noiseGen3 = new NoiseGeneratorOctaves(provider.rand, 8);
        this.noiseGen4 = new NoiseGeneratorOctaves(provider.rand, 10);
        this.noiseGen5 = new NoiseGeneratorOctaves(provider.rand, 16);
        this.islandNoise = new NoiseGeneratorSimplex(provider.rand);

        this.surfaceNoise = new NoiseGeneratorPerlin(provider.rand, 4);     // Used only for biome decoration

        net.minecraftforge.event.terraingen.InitNoiseGensEvent.ContextEnd ctx =
                new net.minecraftforge.event.terraingen.InitNoiseGensEvent.ContextEnd(noiseGen1, noiseGen2, noiseGen3, noiseGen4, noiseGen5, islandNoise);
        ctx = net.minecraftforge.event.terraingen.TerrainGen.getModdedNoiseGenerators(world, provider.rand, ctx);

        this.noiseGen1 = ctx.getLPerlin1();
        this.noiseGen2 = ctx.getLPerlin2();
        this.noiseGen3 = ctx.getPerlin();
        this.noiseGen4 = ctx.getDepth();
        this.noiseGen5 = ctx.getScale();
        this.islandNoise = ctx.getIsland();

        driver = LostCityConfiguration.OPTIMIZED_CHUNKGEN ? new OptimizedDriver() : new SafeDriver();
    }

    /**
     * generates a subset of the level's terrain data. Takes 7 arguments: the [empty] noise array, the position, and the
     * size.
     */
    private double[] initializeNoiseField(double[] densities, int chunkX2, int chunkY2, int chunkZ2, int sizeX, int sizeY, int sizeZ) {
        ChunkGeneratorEvent.InitNoiseField event = new ChunkGeneratorEvent.InitNoiseField(provider, densities, chunkX2, chunkY2, chunkZ2, sizeX, sizeY, sizeZ);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() == Event.Result.DENY) {
            return event.getNoisefield();
        }

        if (densities == null) {
            densities = new double[sizeX * sizeY * sizeZ];
        }

        boolean shallowOcean = false; // @todo?

        double d0 = 684.412D;
        double d1 = 684.412D;
        this.noiseData4 = this.noiseGen4.generateNoiseOctaves(this.noiseData4, chunkX2, chunkZ2, sizeX, sizeZ, 1.121D, 1.121D, 0.5D);
        this.noiseData5 = this.noiseGen5.generateNoiseOctaves(this.noiseData5, chunkX2, chunkZ2, sizeX, sizeZ, 200.0D, 200.0D, 0.5D);
        d0 *= 2.0D;
        this.noiseData1 = this.noiseGen3.generateNoiseOctaves(this.noiseData1, chunkX2, chunkY2, chunkZ2, sizeX, sizeY, sizeZ, d0 / 80.0D, d1 / 160.0D, d0 / 80.0D);
        this.noiseData2 = this.noiseGen1.generateNoiseOctaves(this.noiseData2, chunkX2, chunkY2, chunkZ2, sizeX, sizeY, sizeZ, d0, d1, d0);
        this.noiseData3 = this.noiseGen2.generateNoiseOctaves(this.noiseData3, chunkX2, chunkY2, chunkZ2, sizeX, sizeY, sizeZ, d0, d1, d0);
        int k1 = 0;

        Random random = new Random(chunkX2 * 13 + chunkY2 * 157 + chunkZ2 * 13883);
        random.nextFloat();

        for (int x = 0; x < sizeX; ++x) {
            for (int z = 0; z < sizeZ; ++z) {

                float f2 = 0.0f;
                switch (type) {
                    case NORMAL: {
                        float xx = (x + chunkX2) / 1.0F;
                        float zz = (z + chunkZ2) / 1.0F;

                        f2 = 100.0F - (float) Math.sqrt(xx * xx + zz * zz) * 8.0F;

                        if (f2 > 80.0F) {
                            f2 = 80.0F;
                        } else if (f2 < -100.0F) {
                            f2 = -100.0F;
                        }
                        break;
                    }
                    case CHAOTIC:
                        f2 = 0.0F;
                        break;
                    case PLATEAUS:
                        f2 = -5.0f;
                        break;
                    case ISLANDS:
                        f2 = -20.0f;
                        break;
                }

                for (int y = 0; y < sizeY; ++y) {
                    double d5 = 0.0D;

                    double d7 = this.noiseData2[k1] / 512.0D;
                    double d8 = this.noiseData3[k1] / 512.0D;
                    double d9 = (this.noiseData1[k1] / 10.0D + 1.0D) / 2.0D;

                    if (d9 < 0.0D) {
                        d5 = d7;
                    }
                    else if (d9 > 1.0D) {
                        d5 = d8;
                    } else {
                        d5 = d7 + (d8 - d7) * d9;
                    }

                    d5 -= 8.0D;
                    d5 += f2;
                    int b0 = 2;
                    double d10;

                    if (y > ((sizeY / 2) - b0)) {
                        d10 = ((y - (sizeY / 2 - b0)) / 64.0F);

                        if (d10 < 0.0D) {
                            d10 = 0.0D;
                        } else if (d10 > 1.0D) {
                            d10 = 1.0D;
                        }

                        d5 = d5 * (1.0D - d10) + topFactor * d10;
                    }

                    b0 = bottomOffset;

                    if (y < b0) {
                        d10 = ((b0 - y) / (b0 - 1.0F));
                        d5 = d5 * (1.0D - d10) + botFactor * d10;
                    }

                    if (shallowOcean && y == 0) {
                        densities[k1] = 100.0f;
                    } else if (shallowOcean && y == 1) {
                        densities[k1] = 50.0f + (random.nextFloat() * 200.0f);
                    } else {
                        densities[k1] = d5;
                    }
                    ++k1;
                }
            }
        }

        return densities;
    }

    public void generate(int chunkX, int chunkZ, ChunkPrimer primer, LostCitiesTerrainGenerator terrainGenerator) {
        driver.setPrimer(primer);
        char baseBlock = terrainGenerator.baseChar;
        char air = LostCitiesTerrainGenerator.airChar;

        byte b0 = 2;
        int k = b0 + 1;
        byte b1 = 33;
        int l = b0 + 1;
        this.densities = this.initializeNoiseField(this.densities, chunkX * b0, 0, chunkZ * b0, k, b1, l);

        for (int x2 = 0; x2 < b0; ++x2) {
            for (int z2 = 0; z2 < b0; ++z2) {
                for (int height32 = 0; height32 < 32; ++height32) {
                    double d0 = 0.25D;
                    double d1 = this.densities[((x2 + 0) * l + z2 + 0) * b1 + height32 + 0];
                    double d2 = this.densities[((x2 + 0) * l + z2 + 1) * b1 + height32 + 0];
                    double d3 = this.densities[((x2 + 1) * l + z2 + 0) * b1 + height32 + 0];
                    double d4 = this.densities[((x2 + 1) * l + z2 + 1) * b1 + height32 + 0];
                    double d5 = (this.densities[((x2 + 0) * l + z2 + 0) * b1 + height32 + 1] - d1) * d0;
                    double d6 = (this.densities[((x2 + 0) * l + z2 + 1) * b1 + height32 + 1] - d2) * d0;
                    double d7 = (this.densities[((x2 + 1) * l + z2 + 0) * b1 + height32 + 1] - d3) * d0;
                    double d8 = (this.densities[((x2 + 1) * l + z2 + 1) * b1 + height32 + 1] - d4) * d0;

                    for (int h = 0; h < 8; ++h) {
                        double d9 = 0.125D;
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * d9;
                        double d13 = (d4 - d2) * d9;

                        int height = (height32 * 4) + h;

                        for (int x = 0; x < 8; ++x) {
                            driver.current((x + (x2 * 8)), height, (0 + (z2 * 8)));
                            short maxheight = 256;
                            double d14 = 0.125D;
                            double d15 = d10;
                            double d16 = (d11 - d10) * d14;

                            for (int z = 0; z < 8; ++z) {
                                if (d15 > 0.0D) {
                                    driver.block(baseBlock);
                                } else {
                                    driver.block(air);
                                }

                                driver.incY(maxheight);
                                d15 += d16;
                            }

                            d10 += d12;
                            d11 += d13;
                        }

                        d1 += d5;
                        d2 += d6;
                        d3 += d7;
                        d4 += d8;
                    }
                }
            }
        }
    }


    public void replaceBlocksForBiome(int chunkX, int chunkZ, ChunkPrimer primer, Biome[] Biomes, LostCitiesTerrainGenerator terrainGenerator) {
//        ChunkProviderEvent.ReplaceBiomeBlocks event = new ChunkProviderEvent.ReplaceBiomeBlocks(provider, chunkX, chunkZ, aBlock, abyte, Biomes, world);
//        MinecraftForge.EVENT_BUS.post(event);
//        if (event.getResult() == Event.Result.DENY) {
//            return;
//        }

        double d0 = 0.03125D;
        this.stoneNoise = this.surfaceNoise.getRegion(this.stoneNoise, (chunkX * 16), (chunkZ * 16), 16, 16, 0.0625D, 0.0625D, 1.0D);

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                Biome Biome = Biomes[z + x * 16];
                genBiomeTerrain(Biome, primer, chunkX * 16 + x, chunkZ * 16 + z, this.stoneNoise[z + x * 16], terrainGenerator);
            }
        }
    }

    public final void genBiomeTerrain(Biome biome, ChunkPrimer primer, int x, int z, double noise, LostCitiesTerrainGenerator terrainGenerator) {
        driver.setPrimer(primer);
        char air = LostCitiesTerrainGenerator.airChar;
        char baseBlock = terrainGenerator.baseChar;
        char baseLiquid = air;//@LostCitiesTerrainGenerator.liquidChar;

        int topLevel = provider.getProfile().GROUNDLEVEL;

        char fillerBlock = (char) Block.BLOCK_STATE_IDS.get(biome.fillerBlock);
        char topBlock = (char) Block.BLOCK_STATE_IDS.get(biome.topBlock);
        char block = topBlock;
        char block1 = fillerBlock;

        int k = -1;
        int l = (int)(noise / 3.0D + 3.0D + provider.rand.nextDouble() * 0.25D);
        int cx = x & 15;
        int cz = z & 15;

        // Index of the bottom of the column.
        driver.current(cx, 255, cz);

        for (int height = 255; height >= 0; --height) {
            if (height <= 2) {
                driver.block(air);
            } else {
                char currentBlock = driver.getBlock();
                if (currentBlock == LostCitiesTerrainGenerator.bedrockChar && height <= 12) {
                    driver.block(air);
                    k = -1;
                } else {
                    if (currentBlock != air) {
                        if (currentBlock == baseBlock) {
                            if (k == -1) {
                                if (l <= 0) {
                                    block = air;
                                    block1 = baseBlock;
                                } else if (height >= (topLevel-4) && height <= (topLevel+1)) {
                                    block = topBlock;
                                    block1 = fillerBlock;
                                }

                                if (height < topLevel && (block == air)) { // @todo configure height!
                                    if (biome.getTemperature(new BlockPos(x, height, z)) < 0.15F) {
                                        block = (char) Block.BLOCK_STATE_IDS.get(Blocks.ICE.getDefaultState());
                                    } else {
                                        block = baseBlock;  // No liquid since we are floating
                                    }
                                }

                                k = l;

                                if (height >= (topLevel-1)) {
                                    driver.block(block);
                                } else if (height < (topLevel-6) - l) {
                                    block = air;
                                    block1 = baseBlock;
                                    driver.block(fillerBlock);
                                } else {
                                    driver.block(block1);
                                }
                            } else if (k > 0) {
                                --k;
                                driver.block(block1);

                                if (k == 0 && block1 == Block.BLOCK_STATE_IDS.get(Blocks.SAND.getDefaultState())) {
                                    k = provider.rand.nextInt(4) + Math.max(0, height - topLevel);
                                    block1 = (char) Block.BLOCK_STATE_IDS.get(Blocks.SANDSTONE.getDefaultState());
                                }
                            }
                        }
                    } else {
                        k = -1;
                    }
                }
            }
            driver.decY();
        }
    }

}