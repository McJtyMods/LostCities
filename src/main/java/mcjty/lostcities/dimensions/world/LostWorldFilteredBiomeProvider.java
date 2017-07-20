package mcjty.lostcities.dimensions.world;

import mcjty.lostcities.LostCities;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;

public class LostWorldFilteredBiomeProvider extends BiomeProvider {

    private final BiomeProvider original;
    private final String[] allowedBiomeFactors;
    private List<Pair<Float,Biome>> biomes = null;
    private final Map<String, Biome> translationMap = new HashMap<>();

    public LostWorldFilteredBiomeProvider(BiomeProvider original, String[] allowedBiomeFactors) {
        this.original = original;
        this.allowedBiomeFactors = allowedBiomeFactors;
    }

    private void parseAllowedBiomes() {
        if (biomes != null) {
            return;
        }
        biomes = new ArrayList<>();
        for (String s : allowedBiomeFactors) {
            String[] split = StringUtils.split(s, '=');
            float f = Float.parseFloat(split[1]);
            String biomeId = split[0];
            Biome biome = Biome.REGISTRY.getObject(new ResourceLocation(biomeId));
            if (biome == null) {
                for (Biome b : Biome.REGISTRY) {
                    ResourceLocation registryName = b.getRegistryName();
                    if (registryName != null && biomeId.equals(registryName.getResourcePath())) {
                        biome = b;
                        break;
                    }
                }
            }
            if (biome != null) {
                biomes.add(Pair.of(f, biome));
            } else {
                LostCities.logger.warn("Could not find biome '" + biomeId + "'!");
            }
        }
    }

    private Biome translate(Biome biome) {
        if (!translationMap.containsKey(biome.biomeName)) {
            parseAllowedBiomes();

            Biome bestFit = null;
            double bestDist = 1000000000.0;
            for (Pair<Float, Biome> pair : biomes) {
                Biome b = pair.getRight();
                double distance = calculateBiomeDistance(biome, b) * pair.getLeft();
                if (distance < bestDist) {
                    bestDist = distance;
                    bestFit = b;
                }
            }
            if (bestFit == null) {
                bestFit = Biomes.PLAINS;
            }
            translationMap.put(biome.biomeName, bestFit);
        }
        return translationMap.get(biome.biomeName);
    }

    private static double calculateBiomeDistance(Biome a, Biome b) {
        if (a == b) {
            return -1000;
        }
        float dr = a.getRainfall() - b.getRainfall();
        float dt = a.getTemperature() - b.getTemperature();
        float dv = a.getHeightVariation() - b.getHeightVariation();
        float dh = a.getBaseHeight() - b.getBaseHeight();

        return Math.sqrt(dr * dr + dt * dt + dv * dv + dh * dh);
    }


    public Biome getBiome(BlockPos pos) {
        return translate(original.getBiome(pos));
    }

    public Biome[] getBiomesForGeneration(Biome[] biomes, int x, int z, int width, int height) {
        biomes = original.getBiomesForGeneration(biomes, x, z, width, height);
        for (int i = 0 ; i < biomes.length ; i++) {
            biomes[i] = translate(biomes[i]);
        }
        return biomes;
    }

    public Biome[] getBiomes(@Nullable Biome[] oldBiomeList, int x, int z, int width, int depth) {
        oldBiomeList = original.getBiomes(oldBiomeList, x, z, width, depth);
        for (int i = 0 ; i < oldBiomeList.length ; i++) {
            oldBiomeList[i] = translate(oldBiomeList[i]);
        }
        return oldBiomeList;
    }

    public Biome[] getBiomes(@Nullable Biome[] listToReuse, int x, int z, int width, int length, boolean cacheFlag) {
        return this.getBiomes(listToReuse, x, z, width, length);
    }

    @Nullable
    public BlockPos findBiomePosition(int x, int z, int range, List<Biome> biomes, Random random) {
        return original.findBiomePosition(x, z, range, biomes, random);
    }

    public boolean areBiomesViable(int x, int z, int radius, List<Biome> allowed) {
        return true;
    }

    public boolean isFixedBiome() {
        return false;
    }

    public Biome getFixedBiome() {
        return null;
    }
}