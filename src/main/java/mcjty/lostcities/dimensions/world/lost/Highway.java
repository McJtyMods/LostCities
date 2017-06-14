package mcjty.lostcities.dimensions.world.lost;

import mcjty.lostcities.dimensions.world.LostCityChunkGenerator;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Highway {

    private static NoiseGeneratorPerlin perlin = null;
    private static Map<Pair<Integer, Integer>, Integer> xHighwayLevelCache = new HashMap<>();


    private static void makePerlin(long seed) {
        if (perlin == null) {
            Random random = new Random(seed);
            perlin = new NoiseGeneratorPerlin(random, 4);
        }
    }

    public static void cleanCache() {
        perlin = null;
        xHighwayLevelCache.clear();
    }

    /**
     * Returns -1 if there is no highway in X direction that goes through this chunk.
     * Returns 0 or 1 if there is a highway (at that city level) going through this chunk.
     */
    public static int getXHighwayLevel(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        Pair<Integer, Integer> key = Pair.of(chunkX, chunkZ);
        if (xHighwayLevelCache.containsKey(key)) {
            return xHighwayLevelCache.get(key);
        }

//        if (provider.profile.MAX_HIGHWAY_LENGTH <= 0) {
//            return -1;
//        }

        // X Highways can only occur at chunkZ that is a multiple of 4
        if ((chunkZ & 3) != 0) {
            xHighwayLevelCache.put(key, -1);
            return -1;
        }

        makePerlin(provider.seed);
        double v = perlin.getValue(chunkX / 50.0, chunkZ / 50.0);
        if (v > 3) {
            // This is part of a highway. Find the left-most chunk that is still part of this highway
            int left = chunkX-1;
            while (perlin.getValue(left / 50.0, chunkZ / 50.0) > 3) {
                left--;
            }
            left++;   // This is now where the highway starts

            // Find the right-most chunk that is still part of this highway
            int right = chunkX+1;
            while (perlin.getValue(right / 50.0, chunkZ / 50.0) > 3) {
                right++;
            }
            right--;   // This is now where the highway ends

            int level = -1;
            if (right-left >= 5) {
                if (BuildingInfo.isCity(left, chunkZ, provider) && BuildingInfo.isCity(right, chunkZ, provider)) {
                    // We have a city at both sides. Valid highway:
                    level = BuildingInfo.getCityLevel(left, chunkZ, provider);
                }
            }
            for (int x = left ; x <= right ; x++) {
                xHighwayLevelCache.put(Pair.of(x, chunkZ), level);
            }
            return level;

        }

        xHighwayLevelCache.put(key, -1);
        return -1;
    }

}
