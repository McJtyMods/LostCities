package mcjty.lostcities.dimensions.world;

import com.google.common.collect.Lists;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.structure.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class LostWoodlandMansion extends MapGenStructure {

    private final int featureSpacing = 80;
    private final int minFeatureSeparation = 20;
    public static final List<Biome> ALLOWED_BIOMES = Arrays.<Biome>asList(Biomes.ROOFED_FOREST, Biomes.MUTATED_ROOFED_FOREST);
    private final LostCityChunkGenerator provider;

    public LostWoodlandMansion(LostCityChunkGenerator provider) {
        this.provider = provider;
    }

    @Override
    public String getStructureName() {
        return "LostMansion";
    }

    public boolean hasStructure(World world, int chunkX, int chunkZ) {
        this.world = world;
        return canSpawnStructureAtCoords(chunkX, chunkZ);
    }

    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
        int i = chunkX;
        int j = chunkZ;

        if (chunkX < 0) {
            i = chunkX - 79;
        }

        if (chunkZ < 0) {
            j = chunkZ - 79;
        }

        int k = i / 80;
        int l = j / 80;
        Random random = this.world.setRandomSeed(k, l, 10387319);
        k = k * 80;
        l = l * 80;
        k = k + (random.nextInt(60) + random.nextInt(60)) / 2;
        l = l + (random.nextInt(60) + random.nextInt(60)) / 2;

        if (chunkX == k && chunkZ == l) {
            boolean flag = this.world.getBiomeProvider().areBiomesViable(chunkX * 16 + 8, chunkZ * 16 + 8, 32, ALLOWED_BIOMES);

            if (flag) {
                return true;
            }
        }

        return false;
    }

    @Override
    public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean findUnexplored) {
        this.world = worldIn;
        BiomeProvider biomeprovider = worldIn.getBiomeProvider();
        return biomeprovider.isFixedBiome() && biomeprovider.getFixedBiome() != Biomes.ROOFED_FOREST ? null : findNearestStructurePosBySpacing(worldIn, this, pos, 80, 20, 10387319, true, 100, findUnexplored);
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ) {
        return new LostWoodlandMansion.Start(this.world, this.provider, this.rand, chunkX, chunkZ);
    }

    public static class Start extends StructureStart {
        private boolean isValid;

        public Start() {
        }

        public Start(World world, LostCityChunkGenerator provider, Random random, int chunkX, int chunkZ) {
            super(chunkX, chunkZ);
            this.create(world, provider, random, chunkX, chunkZ);
        }

        private void create(World world, LostCityChunkGenerator provider, Random random, int chunkX, int chunkZ) {
            Rotation rotation = Rotation.values()[random.nextInt(Rotation.values().length)];
            ChunkPrimer chunkprimer = provider.getChunkPrimer(chunkX, chunkZ, false);
            int i = 5;
            int j = 5;

            if (rotation == Rotation.CLOCKWISE_90) {
                i = -5;
            } else if (rotation == Rotation.CLOCKWISE_180) {
                i = -5;
                j = -5;
            } else if (rotation == Rotation.COUNTERCLOCKWISE_90) {
                j = -5;
            }

            int k = chunkprimer.findGroundBlockIdx(7, 7);
            int l = chunkprimer.findGroundBlockIdx(7, 7 + j);
            int i1 = chunkprimer.findGroundBlockIdx(7 + i, 7);
            int j1 = chunkprimer.findGroundBlockIdx(7 + i, 7 + j);
            int k1 = Math.min(Math.min(k, l), Math.min(i1, j1));

            if (k1 < 60) {
                this.isValid = false;
            } else {
                BlockPos blockpos = new BlockPos(chunkX * 16 + 8, k1 + 1, chunkZ * 16 + 8);
                List<WoodlandMansionPieces.MansionTemplate> list = Lists.<WoodlandMansionPieces.MansionTemplate>newLinkedList();
                WoodlandMansionPieces.generateMansion(world.getSaveHandler().getStructureTemplateManager(), blockpos, rotation, list, random);
                this.components.addAll(list);
                this.updateBoundingBox();
                this.isValid = true;
            }
        }

        /**
         * Keeps iterating Structure Pieces and spawning them until the checks tell it to stop
         */
        @Override
        public void generateStructure(World worldIn, Random rand, StructureBoundingBox structurebb) {
            super.generateStructure(worldIn, rand, structurebb);
            int i = this.boundingBox.minY;

            for (int j = structurebb.minX; j <= structurebb.maxX; ++j) {
                for (int k = structurebb.minZ; k <= structurebb.maxZ; ++k) {
                    BlockPos blockpos = new BlockPos(j, i, k);

                    if (!worldIn.isAirBlock(blockpos) && this.boundingBox.isVecInside(blockpos)) {
                        boolean flag = false;

                        for (StructureComponent structurecomponent : this.components) {
                            if (structurecomponent.getBoundingBox().isVecInside(blockpos)) {
                                flag = true;
                                break;
                            }
                        }

                        if (flag) {
                            for (int l = i - 1; l > 1; --l) {
                                BlockPos blockpos1 = new BlockPos(j, l, k);

                                if (!worldIn.isAirBlock(blockpos1) && !worldIn.getBlockState(blockpos1).getMaterial().isLiquid()) {
                                    break;
                                }

                                worldIn.setBlockState(blockpos1, Blocks.COBBLESTONE.getDefaultState(), 2);
                            }
                        }
                    }
                }
            }
        }

        /**
         * currently only defined for Villages, returns true if Village has more than 2 non-road components
         */
        @Override
        public boolean isSizeableStructure() {
            return this.isValid;
        }
    }
}