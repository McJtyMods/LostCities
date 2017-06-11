package mcjty.lostcities.dimensions.world;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.ChunkGeneratorEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

public class NormalTerrainGenerator implements BaseTerrainGenerator {
    private World world;
    protected LostCityChunkGenerator provider;

    protected final double[] heightMap;
    private double[] mainNoiseRegion;
    private double[] minLimitRegion;
    private double[] maxLimitRegion;
    private double[] depthRegion;

    private NoiseGeneratorOctaves minLimitPerlinNoise;
    private NoiseGeneratorOctaves maxLimitPerlinNoise;
    private NoiseGeneratorOctaves mainPerlinNoise;
    private NoiseGeneratorPerlin surfaceNoise;

    // A NoiseGeneratorOctaves used in generating terrain
    private NoiseGeneratorOctaves depthNoise;

    private final float[] biomeWeights;
    private double[] depthBuffer = new double[256];


    public NormalTerrainGenerator() {
        this.heightMap = new double[825];

        this.biomeWeights = new float[25];
        for (int j = -2; j <= 2; ++j) {
            for (int k = -2; k <= 2; ++k) {
                float f = (float) (10.0F / Math.sqrt((j * j + k * k) + 0.2F));
                this.biomeWeights[j + 2 + (k + 2) * 5] = f;
            }
        }
    }


    @Override
    public void setup(World world, LostCityChunkGenerator provider) {
        this.world = world;
        this.provider = provider;

        this.minLimitPerlinNoise = new NoiseGeneratorOctaves(provider.rand, 16);
        this.maxLimitPerlinNoise = new NoiseGeneratorOctaves(provider.rand, 16);
        this.mainPerlinNoise = new NoiseGeneratorOctaves(provider.rand, 8);
        this.surfaceNoise = new NoiseGeneratorPerlin(provider.rand, 4);
        NoiseGeneratorOctaves noiseGen5 = new NoiseGeneratorOctaves(provider.rand, 10);
        this.depthNoise = new NoiseGeneratorOctaves(provider.rand, 16);
        NoiseGeneratorOctaves mobSpawnerNoise = new NoiseGeneratorOctaves(provider.rand, 8);

        net.minecraftforge.event.terraingen.InitNoiseGensEvent.ContextOverworld ctx =
                new net.minecraftforge.event.terraingen.InitNoiseGensEvent.ContextOverworld(minLimitPerlinNoise, maxLimitPerlinNoise, mainPerlinNoise, surfaceNoise, noiseGen5, depthNoise, mobSpawnerNoise);
        ctx = net.minecraftforge.event.terraingen.TerrainGen.getModdedNoiseGenerators(world, provider.rand, ctx);
        this.minLimitPerlinNoise = ctx.getLPerlin1();
        this.maxLimitPerlinNoise = ctx.getLPerlin2();
        this.mainPerlinNoise = ctx.getPerlin();
        this.surfaceNoise = ctx.getHeight();
//        this.field_185983_b = ctx.getScale();
        this.depthNoise = ctx.getDepth();
//        this.field_185985_d = ctx.getForest();
    }

    protected void generateHeightmap(int chunkX4, int chunkY4, int chunkZ4) {
        ChunkGeneratorSettings settings = provider.getSettings();
        this.depthRegion = this.depthNoise.generateNoiseOctaves(this.depthRegion, chunkX4, chunkZ4, 5, 5, (double)settings.depthNoiseScaleX, (double)settings.depthNoiseScaleZ, (double)settings.depthNoiseScaleExponent);
        float f = settings.coordinateScale;
        float f1 = settings.heightScale;
        this.mainNoiseRegion = this.mainPerlinNoise.generateNoiseOctaves(this.mainNoiseRegion, chunkX4, chunkY4, chunkZ4, 5, 33, 5, (double)(f / settings.mainNoiseScaleX), (double)(f1 / settings.mainNoiseScaleY), (double)(f / settings.mainNoiseScaleZ));
        this.minLimitRegion = this.minLimitPerlinNoise.generateNoiseOctaves(this.minLimitRegion, chunkX4, chunkY4, chunkZ4, 5, 33, 5, (double)f, (double)f1, (double)f);
        this.maxLimitRegion = this.maxLimitPerlinNoise.generateNoiseOctaves(this.maxLimitRegion, chunkX4, chunkY4, chunkZ4, 5, 33, 5, (double)f, (double)f1, (double)f);

        int i = 0;
        int j = 0;

        if (provider.biomesForGeneration == null) {
            return;
        }

        for (int k = 0; k < 5; ++k) {
            for (int l = 0; l < 5; ++l) {
                float f2 = 0.0F;
                float f3 = 0.0F;
                float f4 = 0.0F;
                Biome Biome = provider.biomesForGeneration[k + 2 + (l + 2) * 10];

                for (int j1 = -2; j1 <= 2; ++j1) {
                    for (int k1 = - 2; k1 <=  2; ++k1) {
                        Biome Biome1 = provider.biomesForGeneration[k + j1 + 2 + (l + k1 + 2) * 10];
                        float f5 = provider.getSettings().biomeDepthOffSet + Biome1.getBaseHeight() * provider.getSettings().biomeDepthWeight;
                        float f6 = provider.getSettings().biomeScaleOffset + Biome1.getHeightVariation() * provider.getSettings().biomeScaleWeight;

                        if (provider.worldType == WorldType.AMPLIFIED && f5 > 0.0F) {
                            f5 = 1.0F + f5 * 2.0F;
                            f6 = 1.0F + f6 * 4.0F;
                        }

                        float f7 = biomeWeights[j1 + 2 + (k1 + 2) * 5] / (f5 + 2.0F);

                        if (Biome1.getBaseHeight() > Biome.getBaseHeight()) {
                            f7 /= 2.0F;
                        }

                        f2 += f6 * f7;
                        f3 += f5 * f7;
                        f4 += f7;
                    }
                }

                f2 /= f4;
                f3 /= f4;
                f2 = f2 * 0.9F + 0.1F;
                f3 = (f3 * 4.0F - 1.0F) / 8.0F;
                double d12 = this.depthRegion[j] / 8000.0D;

                if (d12 < 0.0D) {
                    d12 = -d12 * 0.3D;
                }

                d12 = d12 * 3.0D - 2.0D;

                if (d12 < 0.0D) {
                    d12 /= 2.0D;

                    if (d12 < -1.0D) {
                        d12 = -1.0D;
                    }

                    d12 /= 1.4D;
                    d12 /= 2.0D;
                } else {
                    if (d12 > 1.0D) {
                        d12 = 1.0D;
                    }

                    d12 /= 8.0D;
                }

                ++j;
                double d13 = f3;
                double d14 = f2;
                d13 += d12 * 0.2D;
                d13 = d13 * 8.5D / 8.0D;
                double d5 = 8.5D + d13 * 4.0D;

                for (int j2 = 0; j2 < 33; ++j2) {
                    double d6 = (j2 - d5) * 12.0D * 128.0D / 256.0D / d14;

                    if (d6 < 0.0D) {
                        d6 *= 4.0D;
                    }

                    double d7 = this.minLimitRegion[i] / 512.0D;
                    double d8 = this.maxLimitRegion[i] / 512.0D;
                    double d9 = (this.mainNoiseRegion[i] / 10.0D + 1.0D) / 2.0D;
                    double d10 = MathHelper.clamp(d7, d8, d9) - d6;

                    if (j2 > 29) {
                        double d11 = ((j2 - 29) / 3.0F);
                        d10 = d10 * (1.0D - d11) + -10.0D * d11;
                    }

                    this.heightMap[i] = d10;
                    ++i;
                }
            }
        }
    }

    @Override
    public void generate(int chunkX, int chunkZ, ChunkPrimer primer) {
        IBlockState baseBlock = Blocks.STONE.getDefaultState(); // @todo provider.dimensionInformation.getBaseBlockForTerrain();
//        byte baseMeta = provider.dimensionInformation.getBaseBlockForTerrain().getMeta();
        Block baseLiquid = Blocks.WATER; // @todo provider.dimensionInformation.getFluidForTerrain();

        generateHeightmap(chunkX * 4, 0, chunkZ * 4);

        byte waterLevel = 63;
        for (int x4 = 0; x4 < 4; ++x4) {
            int l = x4 * 5;
            int i1 = (x4 + 1) * 5;

            for (int z4 = 0; z4 < 4; ++z4) {
                int k1 = (l + z4) * 33;
                int l1 = (l + z4 + 1) * 33;
                int i2 = (i1 + z4) * 33;
                int j2 = (i1 + z4 + 1) * 33;

                for (int height32 = 0; height32 < 32; ++height32) {
                    double d1 = heightMap[k1 + height32];
                    double d2 = heightMap[l1 + height32];
                    double d3 = heightMap[i2 + height32];
                    double d4 = heightMap[j2 + height32];
                    double d5 = (heightMap[k1 + height32 + 1] - d1) * 0.125D;
                    double d6 = (heightMap[l1 + height32 + 1] - d2) * 0.125D;
                    double d7 = (heightMap[i2 + height32 + 1] - d3) * 0.125D;
                    double d8 = (heightMap[j2 + height32 + 1] - d4) * 0.125D;

                    for (int h = 0; h < 8; ++h) {
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * 0.25D;
                        double d13 = (d4 - d2) * 0.25D;
                        int height = (height32 * 8) + h;

                        for (int x = 0; x < 4; ++x) {
                            int index = ((x + (x4 * 4)) << 12) | ((0 + (z4 * 4)) << 8) | height;
                            short maxheight = 256;
                            index -= maxheight;
                            double d16 = (d11 - d10) * 0.25D;
                            double d15 = d10 - d16;

                            for (int z = 0; z < 4; ++z) {
                                index += maxheight;
                                if ((d15 += d16) > 0.0D) {
                                    BaseTerrainGenerator.setBlockState(primer, index, baseBlock);
                                } else if (height < waterLevel) {
                                    BaseTerrainGenerator.setBlockState(primer, index, baseLiquid.getDefaultState());
                                }
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

    @Override
    public void replaceBlocksForBiome(int chunkX, int chunkZ, ChunkPrimer primer, Biome[] Biomes) {
        ChunkGeneratorEvent.ReplaceBiomeBlocks event = new ChunkGeneratorEvent.ReplaceBiomeBlocks(provider, chunkX, chunkZ, primer, world);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() == Event.Result.DENY) {
            return;
        }

        double d0 = 0.03125D;
        this.depthBuffer = this.surfaceNoise.getRegion(this.depthBuffer, (chunkX * 16), (chunkZ * 16), 16, 16, d0 * 2.0D, d0 * 2.0D, 1.0D);

        for (int k = 0; k < 16; ++k) {
            for (int l = 0; l < 16; ++l) {
                Biome Biome = Biomes[l + k * 16];
                Biome.genTerrainBlocks(world, provider.rand, primer, chunkX * 16 + k, chunkZ * 16 + l, this.depthBuffer[l + k * 16]);
            }
        }
    }

}
