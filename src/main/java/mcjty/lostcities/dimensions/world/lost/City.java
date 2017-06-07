package mcjty.lostcities.dimensions.world.lost;

import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.dimensions.world.LostCityChunkGenerator;
import mcjty.lostcities.dimensions.world.lost.cityassets.AssetRegistries;
import mcjty.lostcities.dimensions.world.lost.cityassets.CityStyle;
import mcjty.lostcities.varia.GeometryTools;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;

import java.util.Map;
import java.util.Random;

/**
 * A city is defined as a big sphere. Buildings are where the radius is less then 70%
 */
public class City {

    private static boolean isCityCenter(long seed, int chunkX, int chunkZ) {
        Random rand = new Random(seed + chunkZ * 797003437L + chunkX * 295075153L);
        rand.nextFloat();
        rand.nextFloat();
        return rand.nextFloat() < LostCityConfiguration.CITY_CHANCE;
    }

    private static float getCityRadius(long seed, int chunkX, int chunkZ) {
        Random rand = new Random(seed + chunkZ * 100001653L + chunkX * 295075153L);
        rand.nextFloat();
        rand.nextFloat();
        return LostCityConfiguration.CITY_MINRADIUS + rand.nextInt(LostCityConfiguration.CITY_MAXRADIUS - LostCityConfiguration.CITY_MINRADIUS);
    }

    public static CityStyle getCityStyle(long seed, int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        return AssetRegistries.CITYSTYLES.get("standard");  // @todo
    }

    // Call this only for a valid city center (isCityCenter for these parameters returns true)
//    public static int getCityLevel(long seed, int chunkX, int chunkZ, LostCityChunkGenerator provider) {
//        float r = getCityRadius(seed, chunkX, chunkZ);
//        int offset = (int) ((r+15) / 16);
//        float minheight = 1000000;
//        float maxheight = -1000000;
//        int centerX = chunkX * 16 + 8;
//        int centerZ = chunkZ * 16 + 8;
//        float sqr = r*r;
//        for (int cx = chunkX - offset ; cx <= chunkX + offset ; cx++) {
//            for (int cz = chunkZ - offset ; cz <= chunkZ + offset ; cz++) {
//                GeometryTools.AxisAlignedBB2D box = new GeometryTools.AxisAlignedBB2D(cx * 16, cz * 16, cx * 16 + 15, cz * 16 + 15);
//                double sq = GeometryTools.squaredDistanceBoxPoint(box, centerX, centerZ);
//                if (sq < sqr) {
//                    Biome[] biomes = provider.worldObj.getBiomeProvider().getBiomesForGeneration(null, (cx - 1) * 4 - 2, cz * 4 - 2, 10, 10);
//                    for (Biome biome : biomes) {
//                        biome.getBaseHeight()
//                    }
//                }
//            }
//        }
//    }

    public static float getCityFactor(long seed, int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        float factor = 0;
        int offset = (LostCityConfiguration.CITY_MAXRADIUS+15) / 16;
        for (int cx = chunkX - offset; cx <= chunkX + offset; cx++) {
            for (int cz = chunkZ - offset; cz <= chunkZ + offset; cz++) {
                if (isCityCenter(seed, cx, cz)) {
                    float radius = getCityRadius(seed, cx, cz);
                    float sqdist = (cx * 16 - chunkX * 16) * (cx * 16 - chunkX * 16) + (cz * 16 - chunkZ * 16) * (cz * 16 - chunkZ * 16);
                    if (sqdist < radius * radius) {
                        float dist = (float) Math.sqrt(sqdist);
                        factor += (radius - dist) / radius;
                    }
                }
            }
        }

        Float foundFactor = null;
        Biome[] biomes = provider.worldObj.getBiomeProvider().getBiomesForGeneration(null, (chunkX - 1) * 4 - 2, chunkZ * 4 - 2, 10, 10);
        for (Biome biome : biomes) {
            Map<String, Float> map = LostCityConfiguration.getBiomeFactorMap();
            ResourceLocation object = Biome.REGISTRY.getNameForObject(biome);
            Float f = map.get(object.toString());
            if (f != null) {
                foundFactor = f;
                break;
            }
        }

        if (foundFactor == null) {
            factor = factor * LostCityConfiguration.CITY_DEFAULT_BIOME_FACTOR;
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
