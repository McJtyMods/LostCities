package mcjty.lostcities.dimensions.world.terrain.lost;

import mcjty.lostcities.config.LostCityConfiguration;

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

    public static float getCityFactor(long seed, int chunkX, int chunkZ) {
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
        return factor;
    }

}
