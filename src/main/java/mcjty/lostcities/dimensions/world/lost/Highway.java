package mcjty.lostcities.dimensions.world.lost;

import mcjty.lostcities.dimensions.world.LostCityChunkGenerator;
import mcjty.lostcities.varia.ChunkCoord;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

public class Highway {

    private static NoiseGeneratorPerlin perlinX = null;
    private static NoiseGeneratorPerlin perlinZ = null;
    private static Map<ChunkCoord, Integer> xHighwayLevelCache = new HashMap<>();
    private static Map<ChunkCoord, Integer> zHighwayLevelCache = new HashMap<>();


    private static void makePerlin(long seed) {
        if (perlinX == null) {
            Random random = new Random(seed);
            perlinX = new NoiseGeneratorPerlin(random, 4);
        }
        if (perlinZ == null) {
            Random random = new Random(seed ^ 879190747L);
            perlinZ = new NoiseGeneratorPerlin(random, 4);
        }
    }

    public static void cleanCache() {
        perlinX = null;
        perlinZ = null;
        xHighwayLevelCache.clear();
        zHighwayLevelCache.clear();
    }

    /**
     * Returns -1 if there is no highway in X direction that goes through this chunk.
     * Returns 0 or 1 if there is a highway (at that city level) going through this chunk.
     */
    public static int getXHighwayLevel(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        return getHighwayLevel(provider, Highway.xHighwayLevelCache, cp -> hasXHighway(cp, provider), Orientation.X, new ChunkCoord(chunkX, chunkZ));
    }

    /**
     * Returns -1 if there is no highway in Z direction that goes through this chunk.
     * Returns 0 or 1 if there is a highway (at that city level) going through this chunk.
     */
    public static int getZHighwayLevel(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        return getHighwayLevel(provider, Highway.zHighwayLevelCache, cp -> hasZHighway(cp, provider), Orientation.Z, new ChunkCoord(chunkX, chunkZ));
    }

    private static int getHighwayLevel(LostCityChunkGenerator provider, Map<ChunkCoord, Integer> cache, Function<ChunkCoord, Boolean> hasHighway, Orientation orientation, ChunkCoord cp) {
        if (cache.containsKey(cp)) {
            return cache.get(cp);
        }

        // Highways can only occur at chunkZ that is a multiple of 8
        if ((cp.getCoord(orientation.getOpposite()) & 7) != 0) {
            cache.put(cp, -1);
            return -1;
        }


        makePerlin(provider.seed);
        if (hasHighway.apply(cp)) {
            // This is part of a highway. Find the left-most chunk that is still part of this highway
            ChunkCoord lower = cp.lower(orientation);
            while (hasHighway.apply(lower)) {
                lower = lower.lower(orientation);
            }
            lower = lower.higher(orientation);     // This is now where the highway starts

            // Find the right-most chunk that is still part of this highway
            ChunkCoord higher = cp.higher(orientation);
            while (hasHighway.apply(higher)) {
                higher = higher.higher(orientation);
            }
            higher = higher.lower(orientation);     // This is now where the highway ends

            int level = -1;
            if (higher.getCoord(orientation)-lower.getCoord(orientation) >= 5) {
                boolean valid;
                if (provider.profile.HIGHWAY_REQUIRES_TWO_CITIES) {
                    valid = BuildingInfo.isCityRaw(lower.getChunkX(), lower.getChunkZ(), provider) && BuildingInfo.isCityRaw(higher.getChunkX(), higher.getChunkZ(), provider);
                } else {
                    valid = BuildingInfo.isCityRaw(lower.getChunkX(), lower.getChunkZ(), provider) || BuildingInfo.isCityRaw(higher.getChunkX(), higher.getChunkZ(), provider);
                }
                if (valid) {
                    // We have at least one city. Valid highway:
                    switch (provider.profile.HIGHWAY_LEVEL_FROM_CITIES_MODE) {
                        case 0:
                            level = BuildingInfo.getCityLevel(lower.getChunkX(), lower.getChunkZ(), provider);
                            break;
                        case 1:
                            level = Math.min(BuildingInfo.getCityLevel(lower.getChunkX(), lower.getChunkZ(), provider),
                                    BuildingInfo.getCityLevel(higher.getChunkX(), higher.getChunkZ(), provider));
                            break;
                        case 2:
                            level = Math.max(BuildingInfo.getCityLevel(lower.getChunkX(), lower.getChunkZ(), provider),
                                    BuildingInfo.getCityLevel(higher.getChunkX(), higher.getChunkZ(), provider));
                            break;
                        case 3:
                            level = (BuildingInfo.getCityLevel(lower.getChunkX(), lower.getChunkZ(), provider) +
                                    BuildingInfo.getCityLevel(higher.getChunkX(), higher.getChunkZ(), provider)) / 2;
                            break;
                        default:
                            throw new RuntimeException("Bad value for 'highwayLevelFromCities'!");
                    }
                    for (ChunkCoord cc = lower; cc.getCoord(orientation) <= higher.getCoord(orientation); cc = cc.higher(orientation)) {
                        cache.put(cc, level);
                    }
                }
            }
            return level;

        }

        cache.put(cp, -1);
        return -1;
    }

    private static boolean hasXHighway(ChunkCoord cp, LostCityChunkGenerator provider) {
        return perlinX.getValue(cp.getChunkX() / provider.profile.HIGHWAY_MAINPERLIN_SCALE, cp.getChunkZ() / provider.profile.HIGHWAY_SECONDARYPERLIN_SCALE)
                > provider.profile.HIGHWAY_PERLIN_FACTOR;
    }

    private static boolean hasZHighway(ChunkCoord cp, LostCityChunkGenerator provider) {
        return perlinZ.getValue(cp.getChunkX() / provider.profile.HIGHWAY_SECONDARYPERLIN_SCALE, cp.getChunkZ() / provider.profile.HIGHWAY_MAINPERLIN_SCALE)
                > provider.profile.HIGHWAY_PERLIN_FACTOR;
    }

}
