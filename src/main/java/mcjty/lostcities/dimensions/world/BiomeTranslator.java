package mcjty.lostcities.dimensions.world;

import mcjty.lostcities.LostCities;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BiomeTranslator {

    private final String[] allowedBiomeFactors;

    public BiomeTranslator(String[] allowedBiomeFactors) {
        this.allowedBiomeFactors = allowedBiomeFactors;
    }

    private List<Pair<Float,Biome>> biomes = null;
    private final Map<String, Biome> translationMap = new HashMap<>();

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

    public Biome translate(Biome biome) {
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
        float dt = a.getDefaultTemperature() - b.getDefaultTemperature();
        float dv = a.getHeightVariation() - b.getHeightVariation();
        float dh = a.getBaseHeight() - b.getBaseHeight();

        return Math.sqrt(dr * dr + dt * dt + dv * dv + dh * dh);
    }


}
