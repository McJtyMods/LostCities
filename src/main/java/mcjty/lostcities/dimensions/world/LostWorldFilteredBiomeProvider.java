package mcjty.lostcities.dimensions.world;

import mcjty.lostcities.config.BiomeSelectionStrategy;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.dimensions.world.lost.CitySphere;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class LostWorldFilteredBiomeProvider extends BiomeProvider {

    private final World world;
    private final BiomeProvider original;
    private LostCityChunkGenerator provider;

    private final BiomeTranslator biomeTranslator;
    private final BiomeTranslator outsideTranslator;

    public LostWorldFilteredBiomeProvider(World world, BiomeProvider original,
                                          String[] allowedBiomeFactors,
                                          String[] manualBiomeMappings,
                                          BiomeSelectionStrategy strategy,
                                          String[] outsideBiomeFactors,
                                          String[] outsideManualBiomeMappings,
                                          BiomeSelectionStrategy outsideStrategy) {
        this.world = world;
        this.original = original;
        biomeTranslator = new BiomeTranslator(allowedBiomeFactors, manualBiomeMappings, strategy);
        outsideTranslator = new BiomeTranslator(outsideBiomeFactors.length == 0 ? allowedBiomeFactors : outsideBiomeFactors,
                outsideManualBiomeMappings,
                outsideStrategy);
    }


    private LostCityChunkGenerator getProvider() {
        if (provider == null) {
            provider = WorldTypeTools.getChunkGenerator(world.getDimension().getType());
        }
        return provider;
    }


    @Override
    public Biome getBiome(BlockPos pos) {
        LostCityProfile profile = WorldTypeTools.getProfile(world);
        Biome originalBiome = original.getBiome(pos);
        if (!(world instanceof ServerWorld)) {
            return originalBiome;
        }

        if(getProvider() == null) {
            return originalBiome;
        }

        if (profile.isSpace() && profile.CITYSPHERE_LANDSCAPE_OUTSIDE) {
            int chunkX = (pos.getX()) >> 4;
            int chunkZ = (pos.getZ()) >> 4;
            CitySphere sphere = CitySphere.getCitySphere(chunkX, chunkZ, getProvider());
            if (sphere.isEnabled()) {
                float radius = sphere.getRadius();
                BlockPos cc = sphere.getCenterPos();
                double sqradiusOffset = (radius - 2) * (radius - 2);
                int cx = cc.getX();
                int cz = cc.getZ();
                if (CitySphere.squaredDistance(cx, cz, pos.getX(), pos.getZ()) > sqradiusOffset) {
                    return outsideTranslator.translate(originalBiome);
                } else if (sphere.getBiome() != null) {
                    return sphere.getBiome();
                }
            } else {
                return outsideTranslator.translate(originalBiome);
            }
        }
        return biomeTranslator.translate(originalBiome);
    }

    private void translateList(Biome[] biomes, int topx, int topz, int width, int height) {
        if (!(world instanceof ServerWorld)) {
            return;
        }

        if(getProvider() == null) {
            return;
        }

        LostCityProfile profile = WorldTypeTools.getProfile(world);
        if (profile.isSpace() && profile.CITYSPHERE_LANDSCAPE_OUTSIDE) {
            int chunkX = (topx) >> 4;
            int chunkZ = (topz) >> 4;
            CitySphere sphere = CitySphere.getCitySphere(chunkX, chunkZ, getProvider());
            if (sphere.isEnabled()) {
                float radius = sphere.getRadius();
                double sqradiusOffset = (radius - 2) * (radius - 2);
                BlockPos cc = sphere.getCenterPos();
                int cx = cc.getX();
                int cz = cc.getZ();
                for (int z = 0; z < height; z++) {
                    for (int x = 0; x < width; x++) {
                        int i = x + z * width;
                        if (CitySphere.squaredDistance(cx, cz, topx+x, topz+z) > sqradiusOffset) {
                            biomes[i] = outsideTranslator.translate(biomes[i]);
                        } else if (sphere.getBiome() != null) {
                            biomes[i] = sphere.getBiome();
                        } else {
                            biomes[i] = biomeTranslator.translate(biomes[i]);
                        }
                    }
                }
            } else {
                for (int i = 0 ; i < biomes.length ; i++) {
                    biomes[i] = outsideTranslator.translate(biomes[i]);
                }
            }
            return;
        }
        for (int i = 0 ; i < biomes.length ; i++) {
            biomes[i] = biomeTranslator.translate(biomes[i]);
        }
    }

    @Override
    public Biome[] getBiomes(int x, int z, int width, int length, boolean cacheFlag) {
        Biome[] biomes = original.getBiomes(x, z, width, length, cacheFlag);
        translateList(biomes, x, z, width, length);
        return biomes;
    }

    @Override
    public Biome getBiome(int x, int y) {
        Biome biome = original.getBiome(x, y);
        return biomeTranslator.translate(biome);
    }

    @Override
    public Set<Biome> getBiomesInSquare(int centerX, int centerZ, int sideLength) {
        Set<Biome> biomes = original.getBiomesInSquare(centerX, centerZ, sideLength);
        Set<Biome> newBiomes = new HashSet<>();
        for (Biome biome : biomes) {
            newBiomes.add(biomeTranslator.translate(biome));
        }
        return newBiomes;
    }

    @Override
    public boolean hasStructure(Structure<?> structureIn) {
        return original.hasStructure(structureIn);
    }

    @Override
    public Set<BlockState> getSurfaceBlocks() {
        return original.getSurfaceBlocks();
    }

    @Override
    public Biome getBiomeAtFactorFour(int factorFourX, int factorFourZ) {
        Biome biome = super.getBiomeAtFactorFour(factorFourX, factorFourZ);
        return biomeTranslator.translate(biome);
    }

    public Biome[] getBiomesAlternate(int x, int z, int width, int depth, boolean cacheFlag) {
        Biome[] biomes = original.getBiomes(x, z, width, depth, cacheFlag);
        for (int i = 0 ; i < biomes.length ; i++) {
            biomes[i] = outsideTranslator.translate(biomes[i]);
        }
        return biomes;
    }

    @Override
    @Nullable
    public BlockPos findBiomePosition(int x, int z, int range, List<Biome> biomes, Random random) {
        return original.findBiomePosition(x, z, range, biomes, random);
    }
}