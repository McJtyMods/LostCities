package mcjty.lostcities.dimensions.world.terraingen;

import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.dimensions.world.LostCityChunkGenerator;
import mcjty.lostcities.dimensions.world.driver.IPrimerDriver;
import mcjty.lostcities.dimensions.world.driver.OptimizedDriver;
import mcjty.lostcities.dimensions.world.driver.SafeDriver;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.ChunkGeneratorEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

public class CavernTerrainGenerator {
    private LostCityChunkGenerator provider;
    private IPrimerDriver driver;

    /** A NoiseGeneratorOctaves used in generating nether terrain */
    private NoiseGeneratorOctaves netherNoiseGen1;
    private NoiseGeneratorOctaves netherNoiseGen2;
    private NoiseGeneratorOctaves netherNoiseGen3;
    /** Determines whether something other than nettherack can be generated at a location */
    private NoiseGeneratorOctaves netherrackExculsivityNoiseGen;
    private NoiseGeneratorOctaves netherNoiseGen6;
    private NoiseGeneratorOctaves netherNoiseGen7;

    private double[] noiseField;
    /** Holds the noise used to determine whether something other than the baseblock can be generated at a location */
    private double[] baseBlockExclusivityNoise = new double[256];
    private double[] noiseData1;
    private double[] noiseData2;
    private double[] noiseData3;
    private double[] noiseData4;
    private double[] noiseData5;

    public void setup(World world, LostCityChunkGenerator provider) {
        this.provider = provider;

        this.netherNoiseGen1 = new NoiseGeneratorOctaves(provider.rand, 16);
        this.netherNoiseGen2 = new NoiseGeneratorOctaves(provider.rand, 16);
        this.netherNoiseGen3 = new NoiseGeneratorOctaves(provider.rand, 8);
        /* Determines whether slowsand or gravel can be generated at a location */
        NoiseGeneratorOctaves slowsandGravelNoiseGen = new NoiseGeneratorOctaves(provider.rand, 4);
        this.netherrackExculsivityNoiseGen = new NoiseGeneratorOctaves(provider.rand, 4);
        this.netherNoiseGen6 = new NoiseGeneratorOctaves(provider.rand, 10);
        this.netherNoiseGen7 = new NoiseGeneratorOctaves(provider.rand, 16);

        net.minecraftforge.event.terraingen.InitNoiseGensEvent.ContextHell ctx =
                new net.minecraftforge.event.terraingen.InitNoiseGensEvent.ContextHell(netherNoiseGen1, netherNoiseGen2, netherNoiseGen3,
                        slowsandGravelNoiseGen, netherrackExculsivityNoiseGen, netherNoiseGen6, netherNoiseGen7);
        ctx = net.minecraftforge.event.terraingen.TerrainGen.getModdedNoiseGenerators(world, provider.rand, ctx);
        this.netherNoiseGen1 = ctx.getLPerlin1();
        this.netherNoiseGen2 = ctx.getLPerlin2();
        this.netherNoiseGen3 = ctx.getPerlin();
        slowsandGravelNoiseGen = ctx.getPerlin2();
        this.netherrackExculsivityNoiseGen = ctx.getPerlin3();
        this.netherNoiseGen6 = ctx.getScale();
        this.netherNoiseGen7 = ctx.getDepth();
        driver = LostCityConfiguration.OPTIMIZED_CHUNKGEN ? new OptimizedDriver() : new SafeDriver();
    }

    /**
     * generates a subset of the level's terrain data. Takes 7 arguments: the [empty] noise array, the position, and the
     * size.
     */
    private double[] initializeNoiseField(double[] noiseField, int x, int y, int z, int sx, int sy, int sz) {
        ChunkGeneratorEvent.InitNoiseField event = new ChunkGeneratorEvent.InitNoiseField(provider, noiseField, x, y, z, sx, sy, sz);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() == Event.Result.DENY) {
            return event.getNoisefield();
        }

        int syr = 16;

        if (noiseField == null) {
            noiseField = new double[sx * sy * sz];
        }

        double d0 = 684.412D;
        double d1 = 2053.236D;
        this.noiseData4 = this.netherNoiseGen6.generateNoiseOctaves(this.noiseData4, x, y, z, sx, 1, sz, 1.0D, 0.0D, 1.0D);
        this.noiseData5 = this.netherNoiseGen7.generateNoiseOctaves(this.noiseData5, x, y, z, sx, 1, sz, 100.0D, 0.0D, 100.0D);
        this.noiseData1 = this.netherNoiseGen3.generateNoiseOctaves(this.noiseData1, x, y, z, sx, sy, sz, d0 / 80.0D, d1 / 60.0D, d0 / 80.0D);
        this.noiseData2 = this.netherNoiseGen1.generateNoiseOctaves(this.noiseData2, x, y, z, sx, sy, sz, d0, d1, d0);
        this.noiseData3 = this.netherNoiseGen2.generateNoiseOctaves(this.noiseData3, x, y, z, sx, sy, sz, d0, d1, d0);
        int k1 = 0;
        double[] adouble1 = new double[sy];
        int i2;

        for (i2 = 0; i2 < sy; ++i2) {
            adouble1[i2] = Math.cos(i2 * Math.PI * 6.0D / syr) * 2.0D;
            double d2 = i2;

            if (i2 > syr) {
                d2 = 0;
            } else if (i2 > syr / 2) {
                d2 = (syr - 1 - i2);
            }

            if (d2 < 4.0D) {
                d2 = 4.0D - d2;
                adouble1[i2] -= d2 * d2 * d2 * 10.0D;
            }
        }

        for (i2 = 0; i2 < sx; ++i2) {
            for (int k2 = 0; k2 < sz; ++k2) {
                double d4 = 0.0D;

                for (int j2 = 0; j2 < sy; ++j2) {
                    double d6;
                    double d7 = adouble1[j2];
                    double d8 = this.noiseData2[k1] / 512.0D;
                    double d9 = this.noiseData3[k1] / 512.0D;
                    double d10 = (this.noiseData1[k1] / 10.0D + 1.0D) / 2.0D;

                    if (d10 < 0.0D) {
                        d6 = d8;
                    } else if (d10 > 1.0D) {
                        d6 = d9;
                    } else {
                        d6 = d8 + (d9 - d8) * d10;
                    }

                    d6 -= d7;
                    double d11;

                    if (j2 > sy - 4) {
                        d11 = ((j2 - (sy - 4)) / 3.0F);
                        d6 = d6 * (1.0D - d11) + -10.0D * d11;
                    }

                    if (j2 < d4) {
                        d11 = (d4 - j2) / 4.0D;

                        if (d11 < 0.0D) {
                            d11 = 0.0D;
                        }

                        if (d11 > 1.0D) {
                            d11 = 1.0D;
                        }

                        d6 = d6 * (1.0D - d11) + -10.0D * d11;
                    }

                    noiseField[k1] = d6;
                    ++k1;
                }
            }
        }

        return noiseField;
    }

    public void generate(int chunkX, int chunkZ, ChunkPrimer primer, LostCitiesTerrainGenerator terrainGenerator) {
        driver.setPrimer(primer);
        char baseBlock = terrainGenerator.baseChar;
        char air = LostCitiesTerrainGenerator.airChar;
        char baseLiquid = terrainGenerator.liquidChar;

        byte b0 = 4;
        LostCityProfile profile = provider.getProfile();
        int liquidlevel = profile.GROUNDLEVEL - profile.WATERLEVEL_OFFSET;
        int k = b0 + 1;
        byte b2 = 33;
        int l = b0 + 1;
        this.noiseField = this.initializeNoiseField(this.noiseField, chunkX * b0, 0, chunkZ * b0, k, b2, l);

        for (int x4 = 0; x4 < b0; ++x4) {
            for (int z4 = 0; z4 < b0; ++z4) {
                for (int height32 = 0; height32 < 16; ++height32) {
                    double d0 = 0.125D;
                    double d1 = this.noiseField[((x4 + 0) * l + z4 + 0) * b2 + height32 + 0];
                    double d2 = this.noiseField[((x4 + 0) * l + z4 + 1) * b2 + height32 + 0];
                    double d3 = this.noiseField[((x4 + 1) * l + z4 + 0) * b2 + height32 + 0];
                    double d4 = this.noiseField[((x4 + 1) * l + z4 + 1) * b2 + height32 + 0];
                    double d5 = (this.noiseField[((x4 + 0) * l + z4 + 0) * b2 + height32 + 1] - d1) * d0;
                    double d6 = (this.noiseField[((x4 + 0) * l + z4 + 1) * b2 + height32 + 1] - d2) * d0;
                    double d7 = (this.noiseField[((x4 + 1) * l + z4 + 0) * b2 + height32 + 1] - d3) * d0;
                    double d8 = (this.noiseField[((x4 + 1) * l + z4 + 1) * b2 + height32 + 1] - d4) * d0;

                    for (int h = 0; h < 8; ++h) {
                        double d9 = 0.25D;
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * d9;
                        double d13 = (d4 - d2) * d9;

                        int height = (height32 * 8) + h;

                        for (int x = 0; x < 4; ++x) {
                            driver.current((x + (x4 * 4)), height, (0 + (z4 * 4)));
                            short maxheight = 256;
                            double d14 = 0.25D;
                            double d15 = d10;
                            double d16 = (d11 - d10) * d14;

                            for (int z = 0; z < 4; ++z) {
                                if (d15 > 0.0D) {
                                    driver.block(baseBlock);
                                } else if (height < liquidlevel) {
                                    driver.block(baseLiquid);
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
        driver.setPrimer(primer);
        char baseBlock = terrainGenerator.baseChar;
        char air = LostCitiesTerrainGenerator.airChar;
        char baseLiquid = terrainGenerator.liquidChar;
        int bedrockLayer = provider.getProfile().BEDROCK_LAYER;

        byte groundLevel = (byte) provider.getProfile().GROUNDLEVEL;
        double d0 = 0.03125D;
        this.baseBlockExclusivityNoise = this.netherrackExculsivityNoiseGen.generateNoiseOctaves(this.baseBlockExclusivityNoise, chunkX * 16, chunkZ * 16, 0, 16, 16, 1, d0 * 2.0D, d0 * 2.0D, d0 * 2.0D);

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                Biome Biome = Biomes[z + x * 16];
                char fillerBlock = (char) Block.BLOCK_STATE_IDS.get(Biome.fillerBlock);
                char topBlock = (char) Block.BLOCK_STATE_IDS.get(Biome.topBlock);

                int l = (int)(this.baseBlockExclusivityNoise[x + z * 16] / 3.0D + 3.0D + provider.rand.nextDouble() * 0.25D);
                int k = -1;
                char block = topBlock;
                char block1 = fillerBlock;
                boolean foundAir = false;

                driver.current(x, 128, z);
                for (int y = 128; y >= 0; --y) {

                    if (y >= 128 - (provider.rand.nextInt(3)+bedrockLayer) || y <= (provider.rand.nextInt(3) + bedrockLayer)) {
                        driver.block(LostCitiesTerrainGenerator.bedrockChar);
                    } else if (y > 85) {
                        // Don't do anything at this height. We're most likely still processing cavern ceiling
                    } else if (!foundAir) {
                        if (driver.getBlock() == air) {
                            foundAir = true;
                        }
                    } else {
                        char currentBlock = driver.getBlock();

                        if (currentBlock != air) {
                            if (currentBlock == baseBlock) {
                                if (k == -1) {
                                    if (l <= 0) {
                                        block = air;
                                        block1 = baseBlock;
                                    } else if (y >= groundLevel - 6 && y <= groundLevel + 1) {
                                        block = topBlock;
                                        block1 = fillerBlock;
                                    }

                                    if (y < groundLevel && block == air) {
                                        block = baseLiquid;
                                    }

                                    k = l;

                                    if (y >= groundLevel - 1) {
                                        driver.block(block);
                                    } else if (y < (groundLevel-5) -l) {
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
                                        k = provider.rand.nextInt(4) + Math.max(0, y - groundLevel);
                                        block1 = (char) Block.BLOCK_STATE_IDS.get(Blocks.SANDSTONE.getDefaultState());
                                    }
                                }
                            }
                        } else {
                            k = -1;
                        }
                    }
                    driver.decY();
                }
            }
        }
    }

}
