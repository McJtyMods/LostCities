package mcjty.lostcities.worldgen;

import mcjty.lostcities.config.BiomeSelectionStrategy;
import mcjty.lostcities.setup.Config;
import mcjty.lostcities.setup.ModSetup;
import mcjty.lostcities.varia.Tools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class BiomeTranslator {

    private final String[] allowedBiomeFactors;
    private final String[] manualBiomeMappings;
    private final BiomeSelectionStrategy strategy;
    private final Map<ResourceLocation, Biome> translationMap = new HashMap<>();

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

    private static List<Pair<ResourceLocation, Biome>> parseManualBiomes(String[] manualBiomeMappings) {
        List<Pair<ResourceLocation, Biome>> mapping = new ArrayList<>();
        for (String s : manualBiomeMappings) {
            String[] split = StringUtils.split(s, '=');
            String biomeId = split[0];
            String destBiomeId = split[1];
            Biome biome = findBiome(biomeId);
            Biome destBiome = findBiome(destBiomeId);
            if (biome != null && destBiome != null) {
                mapping.add(Pair.of(new ResourceLocation(biomeId), destBiome));
            } else if (biome == null) {
                ModSetup.getLogger().warn("Could not find biome '" + biomeId + "'!");
            } else if (destBiome == null) {
                ModSetup.getLogger().warn("Could not find biome '" + destBiomeId + "'!");
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
                ModSetup.getLogger().warn("Could not find biome '" + biomeId + "'!");
            }
        }
        return biomes;
    }

    private static Biome findBiome(String biomeId) {
        return ForgeRegistries.BIOMES.getValue(new ResourceLocation(biomeId));
    }

    private void dumpTranslationMap() {
        ModSetup.getLogger().info("Dumping biome mapping");
        for (Map.Entry<ResourceLocation, Biome> entry : translationMap.entrySet()) {
            ResourceLocation biomeKey = ForgeRegistries.BIOMES.getKey(entry.getValue());
            ModSetup.getLogger().info("biome: " + entry.getKey() + " -> " + entry.getValue().getRegistryName().toString() + " (" + biomeKey.toString() + ")");
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
                bestFit = ForgeRegistries.BIOMES.getValue(Biomes.PLAINS.getRegistryName());
            }
            translationMap.put(Tools.getBiomeId(biome), bestFit);
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
                bestFit.add(ForgeRegistries.BIOMES.getValue(Biomes.PLAINS.getRegistryName()));
            }

            if (bestFit.size() == 1) {
                translationMap.put(biome.getRegistryName(), bestFit.get(0));
            } else {
                // Fixed seed based on biome name so that we have a good chance of getting the same back in case of new biomes
                ResourceLocation biomeId = Tools.getBiomeId(biome);
                if (biomeId != null) {
                    long seed = biomeId.hashCode();
                    Random random = new Random(seed);
                    translationMap.put(biomeId, bestFit.get(random.nextInt(bestFit.size())));
                }
            }
        }
    }

    public Biome translate(Biome biome) {
        if (translationMap.isEmpty()) {
            List<Pair<Float, Biome>> biomes = parseBiomes(allowedBiomeFactors);
            switch (strategy) {
                case ORIGINAL -> generateTranslationMapOriginal(biomes);
                case RANDOMIZED -> generateTranslationMapNG(biomes, 0.2f);
                case VARIED -> generateTranslationMapNG(biomes, 1.0f);
            }
            List<Pair<ResourceLocation, Biome>> manualMappings = parseManualBiomes(manualBiomeMappings);
            for (Pair<ResourceLocation, Biome> pair : manualMappings) {
                translationMap.put(pair.getKey(), pair.getValue());
            }
            if (Config.DEBUG) {
                dumpTranslationMap();
            }
        }
        return translationMap.get(Tools.getBiomeId(biome));
    }

    private static double calculateBiomeDistance(Biome a, Biome b) {
        if (a == b) {
            return -1000;
        }
        float dr = a.getDownfall() - b.getDownfall();
        float dt = a.getBaseTemperature() - b.getBaseTemperature();
        // @todo 1.18
//        float dv = a.getDepth() - b.getDepth();
//        float dh = a.getScale() - b.getScale();
//        return Math.sqrt(dr * dr + dt * dt + dv * dv + dh * dh);
        return Math.sqrt(dr * dr + dt * dt);
    }


}
