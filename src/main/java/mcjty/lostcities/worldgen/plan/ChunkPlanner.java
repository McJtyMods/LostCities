package mcjty.lostcities.worldgen.plan;

import mcjty.lostcities.api.LostChunkCharacteristics;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.setup.Config;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.TimedCache;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.highway.HighwayInfo;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.Highway;

public final class ChunkPlanner {

    private static final TimedCache<ChunkCoord, ChunkPlan> PLANS = new TimedCache<>(Config.CACHE_CLEANUP_SECONDS::get);
    private static final TimedCache<ChunkCoord, Integer> CITY_LEVELS = new TimedCache<>(Config.CACHE_CLEANUP_SECONDS::get);

    private ChunkPlanner() {
    }

    public static void clear() {
        PLANS.clear();
        CITY_LEVELS.clear();
    }

    public static int cityLevel(ChunkCoord coord, IDimensionInfo provider) {
        if (provider.getWorld() == null) {
            return BuildingInfo.computeCityLevel(coord, provider);
        }
        Integer cached = CITY_LEVELS.get(coord);
        if (cached != null) {
            return cached;
        }
        int level = BuildingInfo.computeCityLevel(coord, provider);
        CITY_LEVELS.put(coord, level);
        return level;
    }

    public static HighwayInfo highway(ChunkCoord coord, IDimensionInfo provider, LostCityProfile profile) {
        return PLANS.withPinnedValue(coord, key -> new ChunkPlan(),
                plan -> plan.highway(() -> Highway.computeHighwayInfo(coord, provider, profile)));
    }

    public static LostChunkCharacteristics characteristics(ChunkCoord coord, IDimensionInfo provider) {
        return PLANS.withPinnedValue(coord, key -> new ChunkPlan(),
                plan -> plan.characteristics(() -> BuildingInfo.computeChunkCharacteristics(coord, provider)));
    }

    public static BuildingInfo buildingInfo(ChunkCoord coord, IDimensionInfo provider) {
        return PLANS.withPinnedValue(coord, key -> new ChunkPlan(),
                plan -> plan.buildingInfo(() -> BuildingInfo.computeBuildingInfo(coord, provider)));
    }
}
