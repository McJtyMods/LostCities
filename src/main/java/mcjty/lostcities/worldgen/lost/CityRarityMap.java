package mcjty.lostcities.worldgen.lost;

import mcjty.lostcities.varia.PerlinNoiseGenerator14;

import java.util.Random;

public class CityRarityMap {

    private final PerlinNoiseGenerator14 perlinCity;

    public CityRarityMap(long seed) {
        perlinCity = new PerlinNoiseGenerator14(seed, 4);
    }

    public float getCityFactor(int cx, int cz) {
        double scale = 20.0;
        double offset = 1;
        double factor = perlinCity.getValue(cx / scale, cz / scale) / 5.0 - offset;
        if (factor < 0) {
            factor = 0;
        }
//        System.out.println("factor = " + factor);
//        return factor
//                > profile.HIGHWAY_PERLIN_FACTOR;
        return (float) factor;
    }

    public static void main(String[] args) {
        CityRarityMap map = new CityRarityMap(13432432);
        for (int y = 0 ; y < 80 ; y++) {
            String s = "";
            for (int x = 0 ; x < 120 ; x++) {
                float factor = map.getCityFactor(x, y);
//                float factor = debugFactor(x, y);
                int f = (int)(factor * 10);
                String c = f == 0 ? " " : f >= 10 ? "*" : Integer.toString(f);
                s += c;
            }
            System.out.println(s);
        }
    }






    public static float debugFactor(int chunkX, int chunkZ) {
        int CITY_MINRADIUS = 50;
        int CITY_MAXRADIUS = 128;

        float factor = 0;
        int offset = (CITY_MAXRADIUS+15) / 16;
        for (int cx = chunkX - offset; cx <= chunkX + offset; cx++) {
            for (int cz = chunkZ - offset; cz <= chunkZ + offset; cz++) {
                if (debugisCityCenter(cx, cz)) {
                    Random rand = new Random(123546 + cx * 100001653L + cz * 295075153L);
                    rand.nextFloat();
                    rand.nextFloat();
                    int cityRange = CITY_MAXRADIUS - CITY_MINRADIUS;
                    float radius = CITY_MINRADIUS + rand.nextInt(cityRange);
                    float sqdist = (cx * 16 - chunkX * 16) * (cx * 16 - chunkX * 16) + (cz * 16 - chunkZ * 16) * (cz * 16 - chunkZ * 16);
                    if (sqdist < radius * radius) {
                        float dist = (float) Math.sqrt(sqdist);
                        factor += (radius - dist) / radius;
                    }
                }
            }
        }

        float foundFactor = 1.0f;
        return Math.min(Math.max(factor * foundFactor, 0), 1);
    }

    public static boolean debugisCityCenter(int chunkX, int chunkZ) {
        double CITY_CHANCE = .01;
        Random rand = new Random(1234569 + chunkZ * 797003437L + chunkX * 295075153L);
        rand.nextFloat();
        rand.nextFloat();
        return rand.nextDouble() < CITY_CHANCE;
    }
}
