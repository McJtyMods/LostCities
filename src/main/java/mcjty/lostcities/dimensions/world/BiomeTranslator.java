package mcjty.lostcities.dimensions.world;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.config.BiomeSelectionStrategy;
import mcjty.lostcities.config.LostCityConfiguration;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class BiomeTranslator {

    private final String[] allowedBiomeFactors;
    private final String[] manualBiomeMappings;
    private final BiomeSelectionStrategy strategy;
    private final Map<String, Biome> translationMap = new HashMap<>();

    public BiomeTranslator(String[] allowedBiomeFactors, String[] manualBiomeMappings, BiomeSelectionStrategy strategy) {
        for(String s : allowedBiomeFactors) {
            if(s.indexOf('=') == -1) {
                throw new IllegalArgumentException("Biome factor missing equals sign: " + s);
            }
        }
        this.allowedBiomeFactors = allowedBiomeFactors;
        for(String s : manualBiomeMappings) {
            if(s.indexOf('=') == -1) {
                throw new IllegalArgumentException("Biome mapping missing equals sign: " + s);
            }
        }
        this.manualBiomeMappings = manualBiomeMappings;
        this.strategy = strategy;
    }

    private static List<Pair<String, Biome>> parseManualBiomes(String[] manualBiomeMappings) {
        List<Pair<String, Biome>> mapping = new ArrayList<>();
        for (String s : manualBiomeMappings) {
            String[] split = StringUtils.split(s, '=');
            String biomeId = split[0];
            String destBiomeId = split[1];
            Biome biome = findBiome(biomeId);
            Biome destBiome = findBiome(destBiomeId);
            if (biome != null && destBiome != null) {
                mapping.add(Pair.of(biome.biomeName, destBiome));
            } else if (biome == null) {
                LostCities.setup.getLogger().warn("Could not find biome '" + biomeId + "'!");
            } else if (destBiome == null) {
                LostCities.setup.getLogger().warn("Could not find biome '" + destBiomeId + "'!");
            }
        }
        return mapping;
    }



    public static List<Pair<Float, Biome>> parseBiomes(String[] allowedBiomeFactors) {
        List<Pair<Float, Biome>> biomes = new ArrayList<>();
        for (String s : allowedBiomeFactors) {
            String[] split = StringUtils.split(s, '=');
            float f = Float.parseFloat(split[1]);
            String biomeId = split[0];
            Biome biome = findBiome(biomeId);
            if (biome != null) {
                biomes.add(Pair.of(f, biome));
            } else {
                LostCities.setup.getLogger().warn("Could not find biome '" + biomeId + "'!");
            }
        }
        return biomes;
    }

    private static Biome findBiome(String biomeId) {
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
        return biome;
    }

    private void dumpTranslationMap() {
        LostCities.setup.getLogger().info("Dumping biome mapping");
        for (Map.Entry<String, Biome> entry : translationMap.entrySet()) {
            ResourceLocation biomeKey = ForgeRegistries.BIOMES.getKey(entry.getValue());
            LostCities.setup.getLogger().info("biome: " + entry.getKey() + " -> " + entry.getValue().biomeName + " (" + biomeKey.toString() + ")");
        }
    }

    private void generateTranslationMapOriginal(List<Pair<Float, Biome>> biomes) {
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

    private void generateTranslationMapNG(List<Pair<Float, Biome>> biomes, float samenessFactor) {
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
            List<Pair<Float, Biome>> biomes = parseBiomes(allowedBiomeFactors);
            switch (strategy) {
                case ORIGINAL:
                    generateTranslationMapOriginal(biomes);
                    break;
                case RANDOMIZED:
                    generateTranslationMapNG(biomes, 0.2f);
                    break;
                case VARIED:
                    generateTranslationMapNG(biomes, 1.0f);
                    break;
            }
            List<Pair<String, Biome>> manualMappings = parseManualBiomes(manualBiomeMappings);
            for (Pair<String, Biome> pair : manualMappings) {
                translationMap.put(pair.getKey(), pair.getValue());
            }
            if (LostCityConfiguration.DEBUG) {
                dumpTranslationMap();
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
