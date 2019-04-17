package mcjty.lostcities.dimensions.world;

import mcjty.lostcities.config.BiomeSelectionStrategy;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.dimensions.world.lost.CitySphere;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

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
            provider = WorldTypeTools.getChunkGenerator(world.provider.getDimension());
        }
        return provider;
    }


    @Override
    public Biome getBiome(BlockPos pos) {
        LostCityProfile profile = WorldTypeTools.getProfile(world);
        Biome originalBiome = original.getBiome(pos);
        if (!(world instanceof WorldServer)) {
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
        if (!(world instanceof WorldServer)) {
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
    public Biome[] getBiomesForGeneration(Biome[] biomes, int x, int z, int width, int height) {
        biomes = original.getBiomesForGeneration(biomes, x, z, width, height);
        translateList(biomes, x, z, width, height);
        return biomes;
    }

    @Override
    public Biome[] getBiomes(@Nullable Biome[] oldBiomeList, int x, int z, int width, int depth) {
        oldBiomeList = original.getBiomes(oldBiomeList, x, z, width, depth);
        translateList(oldBiomeList, x, z, width, depth);
        return oldBiomeList;
    }

    public Biome[] getBiomesAlternate(@Nullable Biome[] oldBiomeList, int x, int z, int width, int depth) {
        oldBiomeList = original.getBiomes(oldBiomeList, x, z, width, depth);
        for (int i = 0 ; i < oldBiomeList.length ; i++) {
            oldBiomeList[i] = outsideTranslator.translate(oldBiomeList[i]);
        }
        return oldBiomeList;
    }

    @Override
    public Biome[] getBiomes(@Nullable Biome[] listToReuse, int x, int z, int width, int length, boolean cacheFlag) {
        return this.getBiomes(listToReuse, x, z, width, length);
    }

    @Override
    @Nullable
    public BlockPos findBiomePosition(int x, int z, int range, List<Biome> biomes, Random random) {
        return original.findBiomePosition(x, z, range, biomes, random);
    }

    @Override
    public boolean areBiomesViable(int x, int z, int radius, List<Biome> allowed) {
        boolean result = false;
        Biome[] biomeList = getBiomes(null, x, z, radius, radius);
        for (int i = 0 ; i < biomeList.length ; i++) {
            if ( allowed.contains(biomeList[i]) ) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public boolean isFixedBiome() {
        return false;
    }

    @Override
    public Biome getFixedBiome() {
        return null;
    }
}