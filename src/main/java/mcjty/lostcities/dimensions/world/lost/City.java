package mcjty.lostcities.dimensions.world.lost;

import mcjty.lostcities.dimensions.world.LostCityChunkGenerator;
import mcjty.lostcities.dimensions.world.lost.cityassets.AssetRegistries;
import mcjty.lostcities.dimensions.world.lost.cityassets.CityStyle;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.Tools;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A city is defined as a big sphere. Buildings are where the radius is less then 70%
 */
public class City {

    private static boolean isCityCenter(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        Random rand = new Random(provider.seed + chunkZ * 797003437L + chunkX * 295075153L);
        rand.nextFloat();
        rand.nextFloat();
        return rand.nextFloat() < provider.profile.CITY_CHANCE;
    }

    private static float getCityRadius(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        Random rand = new Random(provider.seed + chunkZ * 100001653L + chunkX * 295075153L);
        rand.nextFloat();
        rand.nextFloat();
        return provider.profile.CITY_MINRADIUS + rand.nextInt(provider.profile.CITY_MAXRADIUS - provider.profile.CITY_MINRADIUS);
    }

    // Call this on a city center to get the style of that city
    private static String getCityStyleForCityCenter(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        Random rand = new Random(provider.seed + chunkZ * 899809363L + chunkX * 256203221L);
        rand.nextFloat();
        rand.nextFloat();
        return provider.worldStyle.getRandomCityStyle(provider, chunkX, chunkZ, rand);
    }

    // Calculate the citystyle based on all surrounding cities
    public static CityStyle getCityStyle(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        Random rand = new Random(provider.seed + chunkZ * 593441843L + chunkX * 217645177L);
        rand.nextFloat();
        rand.nextFloat();

        int offset = (provider.profile.CITY_MAXRADIUS+15) / 16;
        List<Pair<Float, String>> styles = new ArrayList<>();
        for (int cx = chunkX - offset; cx <= chunkX + offset; cx++) {
            for (int cz = chunkZ - offset; cz <= chunkZ + offset; cz++) {
                if (isCityCenter(cx, cz, provider)) {
                    float radius = getCityRadius(cx, cz, provider);
                    float sqdist = (cx * 16 - chunkX * 16) * (cx * 16 - chunkX * 16) + (cz * 16 - chunkZ * 16) * (cz * 16 - chunkZ * 16);
                    if (sqdist < radius * radius) {
                        float dist = (float) Math.sqrt(sqdist);
                        float factor = (radius - dist) / radius;
                        styles.add(Pair.of(factor, getCityStyleForCityCenter(chunkX, chunkZ, provider)));
                    }
                }
            }
        }
        if (styles.isEmpty()) {
            return AssetRegistries.CITYSTYLES.get(provider.worldStyle.getRandomCityStyle(provider, chunkX, chunkZ, rand));
        }
        return AssetRegistries.CITYSTYLES.get(Tools.getRandomFromList(provider, rand, styles));
    }

    public static float getCityFactor(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        long seed = provider.seed;
        float factor = 0;
        int offset = (provider.profile.CITY_MAXRADIUS+15) / 16;
        for (int cx = chunkX - offset; cx <= chunkX + offset; cx++) {
            for (int cz = chunkZ - offset; cz <= chunkZ + offset; cz++) {
                if (isCityCenter(cx, cz, provider)) {
                    float radius = getCityRadius(cx, cz, provider);
                    float sqdist = (cx * 16 - chunkX * 16) * (cx * 16 - chunkX * 16) + (cz * 16 - chunkZ * 16) * (cz * 16 - chunkZ * 16);
                    if (sqdist < radius * radius) {
                        float dist = (float) Math.sqrt(sqdist);
                        factor += (radius - dist) / radius;
                    }
                }
            }
        }

        Float foundFactor = null;
        Biome[] biomes = BiomeInfo.getBiomeInfo(provider, new ChunkCoord(provider.dimensionId, chunkX, chunkZ)).getBiomes();

        if (biomes[55].getBaseHeight() > 4 || biomes[54].getBaseHeight() > 4 || biomes[56].getBaseHeight() > 4
                || biomes[5].getBaseHeight() > 4 || biomes[95].getBaseHeight() > 4) {
            return 0;   // These biomes are too high
        }


        for (Biome biome : biomes) {
            Map<String, Float> map = provider.profile.getBiomeFactorMap();
            ResourceLocation object = Biome.REGISTRY.getNameForObject(biome);
            Float f = map.get(object.toString());
            if (f != null) {
                foundFactor = f;
                break;
            }
        }

        if (foundFactor == null) {
            factor = factor * provider.profile.CITY_DEFAULT_BIOME_FACTOR;
        } else {
            factor = factor * foundFactor;
        }
        if (factor < 0) {
            return 0;
        } else if (factor > 1) {
            return 1;
        }
        return factor;
    }

}
