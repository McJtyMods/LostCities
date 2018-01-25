package mcjty.lostcities.dimensions.world.terraingen;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.dimensions.world.LostCityChunkGenerator;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.ChunkGeneratorEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

public class CavernTerrainGenerator {
    private LostCityChunkGenerator provider;

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

    public void generate(int chunkX, int chunkZ, ChunkPrimer primer) {
        char baseBlock = LostCitiesTerrainGenerator.baseChar;
        char air = LostCitiesTerrainGenerator.airChar;
        char baseLiquid = LostCitiesTerrainGenerator.liquidChar;

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
                            int index = ((x + (x4 * 4)) << 12) | ((0 + (z4 * 4)) << 8) | height;
                            short maxheight = 256;
                            double d14 = 0.25D;
                            double d15 = d10;
                            double d16 = (d11 - d10) * d14;

                            for (int z = 0; z < 4; ++z) {
                                if (d15 > 0.0D) {
                                    primer.data[index] = baseBlock;
                                } else if (height < liquidlevel) {
                                    primer.data[index] = baseLiquid;
                                } else {
                                    primer.data[index] = air;
                                }

                                index += maxheight;
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



    public void replaceBlocksForBiome(int chunkX, int chunkZ, ChunkPrimer primer, Biome[] Biomes) {
        char baseBlock = LostCitiesTerrainGenerator.baseChar;
        char air = LostCitiesTerrainGenerator.airChar;
        char baseLiquid = LostCitiesTerrainGenerator.liquidChar;

        byte b0 = 64;
        double d0 = 0.03125D;
        this.baseBlockExclusivityNoise = this.netherrackExculsivityNoiseGen.generateNoiseOctaves(this.baseBlockExclusivityNoise, chunkX * 16, chunkZ * 16, 0, 16, 16, 1, d0 * 2.0D, d0 * 2.0D, d0 * 2.0D);

        for (int k = 0; k < 16; ++k) {
            for (int l = 0; l < 16; ++l) {
                int i1 = (int)(this.baseBlockExclusivityNoise[k + l * 16] / 3.0D + 3.0D + provider.rand.nextDouble() * 0.25D);
                int j1 = -1;
                char block = baseBlock;

                for (int k1 = 255; k1 >= 0; --k1) {
                    int l1 = (l * 16 + k) * 256 + k1;

                    if (k1 < 1) {
                        primer.data[l1] = LostCitiesTerrainGenerator.bedrockChar;
                    } else if (k1 < 255 - provider.rand.nextInt(5) && k1 > provider.rand.nextInt(5)) {
                        char block2 = primer.data[l1];

                        if (block2 != air) {
                            if (block2 == baseBlock) {
                                if (j1 == -1) {
                                    if (i1 <= 0) {
                                        block = air;
                                    } else if (k1 >= b0 - 4 && k1 <= b0 + 1) {
                                        block = baseBlock;
                                    }

                                    if (k1 < b0 && block == air) {
                                        block = baseLiquid;
                                    }

                                    j1 = i1;

                                    if (k1 >= b0 - 1) {
                                        primer.data[l1] = block;
                                    } else {
                                        primer.data[l1] = baseBlock;
                                    }
                                }
                                else if (j1 > 0) {
                                    --j1;
                                    primer.data[l1] = baseBlock;
                                }
                            }
                        }
                        else {
                            j1 = -1;
                        }
//                    } else if (heightsetting == CavernHeight.HEIGHT_256) {
//                         Only use a bedrock ceiling if the height is 256.
//                        BaseTerrainGenerator.setBlockState(primer, l1, Blocks.BEDROCK.getDefaultState());
                    }
                }
            }
        }
    }

}
