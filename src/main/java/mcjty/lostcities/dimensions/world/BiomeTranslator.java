package mcjty.lostcities.dimensions.world;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.config.BiomeSelectionStrategy;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class BiomeTranslator {

    private final String[] allowedBiomeFactors;
    private final BiomeSelectionStrategy strategy;

    public BiomeTranslator(String[] allowedBiomeFactors, BiomeSelectionStrategy strategy) {
        this.allowedBiomeFactors = allowedBiomeFactors;
        this.strategy = strategy;
    }

    private List<Pair<Float,Biome>> biomes = null;
    private final Map<String, Biome> translationMap = new HashMap<>();

    private void parseAllowedBiomes() {
        if (biomes != null) {
            return;
        }
        biomes = parseBiomes(allowedBiomeFactors);
    }

    public static List<Pair<Float, Biome>> parseBiomes(String[] allowedBiomeFactors) {
        List<Pair<Float, Biome>> biomes = new ArrayList<>();
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
        return biomes;
    }

    private void generateTranslationMapOriginal() {
        for (Biome biome : ForgeRegistries.BIOMES) {
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
    }

    private void generateTranslationMapNG(float samenessFactor) {
        for (Biome biome : ForgeRegistries.BIOMES) {
            List<Biome> bestFit = new ArrayList<>();
            double bestDist = 1000000000.0;
            for (Pair<Float, Biome> pair : biomes) {
                Biome b = pair.getRight();
                double distance = calculateBiomeDistance(biome, b) * pair.getLeft();
                if (Math.abs(distance - bestDist) < samenessFactor) {
                    // Almost the same
                    bestFit.add(b);
                } else if (distance < bestDist) {
                    // Better
                    bestDist = distance;
                    bestFit.clear();
                    bestFit.add(b);
                }
            }
            if (bestFit.isEmpty()) {
                bestFit.add(Biomes.PLAINS);
            }

            if (bestFit.size() == 1) {
                translationMap.put(biome.biomeName, bestFit.get(0));
            } else {
                // Fixed seed based on biome name so that we have a good chance of getting the same back in case of new biomes
                long seed = biome.biomeName.hashCode();
                Random random = new Random(seed);
                random.nextFloat();
                random.nextFloat();
                translationMap.put(biome.biomeName, bestFit.get(random.nextInt(bestFit.size())));
            }
        }
    }

    public Biome translate(Biome biome) {
        if (translationMap.isEmpty()) {
            parseAllowedBiomes();
            switch (strategy) {
                case ORIGINAL:
                    generateTranslationMapOriginal();
                    break;
                case RANDOMIZED:
                    generateTranslationMapNG(0.01f);
                    break;
                case VARIED:
                    generateTranslationMapNG(1.0f);
                    break;
            }
        }
        return translationMap.get(biome.biomeName);
    }

    private static double calculateBiomeDistance(Biome a, Biome b) {
        if (a == b) {
            return -1000;
        }
        float dr = a.getRainfall() - b.getRainfall();
        float dt = a.getDefaultTemperature() - b.getDefaultTemperature();
        float dv = a.getHeightVariation() - b.getHeightVariation();
        float dh = a.getBaseHeight() - b.getBaseHeight();

        return Math.sqrt(dr * dr + dt * dt + dv * dv + dh * dh);
    }


}
