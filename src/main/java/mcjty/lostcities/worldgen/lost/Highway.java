package mcjty.lostcities.worldgen.lost;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.config.HighwayGenerationMode;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.PerlinNoiseGenerator14;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.highway.HighwayInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class Highway {

    private static final Map<ResourceKey<Level>, HighwayNoise> HIGHWAY_NOISE = new ConcurrentHashMap<>();
    private static final Map<ChunkCoord, Integer> X_HIGHWAY_LEVEL_CACHE = new ConcurrentHashMap<>();
    private static final Map<ChunkCoord, Integer> Z_HIGHWAY_LEVEL_CACHE = new ConcurrentHashMap<>();

    private static HighwayNoise getNoise(IDimensionInfo provider) {
        return HIGHWAY_NOISE.computeIfAbsent(provider.getType(),
                key -> new HighwayNoise(new PerlinNoiseGenerator14(provider.getSeed(), 4),
                        new PerlinNoiseGenerator14(provider.getSeed(), 4)));
    }

    public static void cleanCache() {
        HIGHWAY_NOISE.clear();
        X_HIGHWAY_LEVEL_CACHE.clear();
        Z_HIGHWAY_LEVEL_CACHE.clear();
    }

    public static boolean hasHighway(ChunkCoord coord, IDimensionInfo provider, LostCityProfile profile) {
        if (provider.getHighwayGenerationMode() == HighwayGenerationMode.INTERCITY_NETWORK_V1) {
            return getHighwayInfo(coord, provider, profile).hasHighway();
        }
        if (getXHighwayLevel(coord, provider, profile) >= 0) {
            return true;
        }
        if (getZHighwayLevel(coord, provider, profile) >= 0) {
            return true;
        }
        return false;
    }

    /**
     * Returns -1 if there is no highway in X direction that goes through this chunk.
     * Returns 0 or 1 if there is a highway (at that city level) going through this chunk.
     */
    public static int getXHighwayLevel(ChunkCoord coord, IDimensionInfo provider, LostCityProfile profile) {
        if (provider.getHighwayGenerationMode() == HighwayGenerationMode.INTERCITY_NETWORK_V1) {
            return getHighwayInfo(coord, provider, profile).xLevel();
        }
        HighwayNoise noise = getNoise(provider);
        return getHighwayLevel(provider, profile, Highway.X_HIGHWAY_LEVEL_CACHE,
                cp -> hasXHighway(cp, profile, noise.x()), Orientation.X, coord);
    }

    /**
     * Returns -1 if there is no highway in Z direction that goes through this chunk.
     * Returns 0 or 1 if there is a highway (at that city level) going through this chunk.
     */
    public static int getZHighwayLevel(ChunkCoord coord, IDimensionInfo provider, LostCityProfile profile) {
        if (provider.getHighwayGenerationMode() == HighwayGenerationMode.INTERCITY_NETWORK_V1) {
            return getHighwayInfo(coord, provider, profile).zLevel();
        }
        HighwayNoise noise = getNoise(provider);
        return getHighwayLevel(provider, profile, Highway.Z_HIGHWAY_LEVEL_CACHE,
                cp -> hasZHighway(cp, profile, noise.z()), Orientation.Z, coord);
    }

    /**
     * Mode-aware facade used by diagnostics and by the legacy-compatible X/Z
     * level methods. Rendering and all clearance consumers continue to query
     * this class rather than depending directly on a planner implementation.
     */
    public static HighwayInfo getHighwayInfo(ChunkCoord coord, IDimensionInfo provider, LostCityProfile profile) {
        if (provider.getHighwayGenerationMode() == HighwayGenerationMode.INTERCITY_NETWORK_V1) {
            // Preserve the existing hard city-sphere exclusion without making
            // it part of canonical hub/route planning.
            if (provider.getWorld() != null && (profile.isSpace() || profile.isSpheres())
                    && CitySphere.intersectsWithCitySphere(coord, provider)) {
                return HighwayInfo.NONE;
            }
            return provider.getHighwayPlanner().getHighwayInfo(coord.chunkX(), coord.chunkZ());
        }
        HighwayNoise noise = getNoise(provider);
        int xLevel = getHighwayLevel(provider, profile, Highway.X_HIGHWAY_LEVEL_CACHE,
                cp -> hasXHighway(cp, profile, noise.x()), Orientation.X, coord);
        int zLevel = getHighwayLevel(provider, profile, Highway.Z_HIGHWAY_LEVEL_CACHE,
                cp -> hasZHighway(cp, profile, noise.z()), Orientation.Z, coord);
        HighwayInfo.Classification classification;
        if (xLevel >= 0 && zLevel >= 0) {
            classification = xLevel == zLevel ? HighwayInfo.Classification.SAME_LEVEL_INTERSECTION
                    : HighwayInfo.Classification.MULTI_LEVEL_INTERSECTION;
        } else if (xLevel >= 0) {
            classification = HighwayInfo.Classification.X_HIGHWAY;
        } else if (zLevel >= 0) {
            classification = HighwayInfo.Classification.Z_HIGHWAY;
        } else {
            classification = HighwayInfo.Classification.NONE;
        }
        return new HighwayInfo(xLevel, zLevel, classification, java.util.List.of());
    }

    private static int getHighwayLevel(IDimensionInfo provider, LostCityProfile profile, Map<ChunkCoord, Integer> cache, Function<ChunkCoord, Boolean> hasHighway, Orientation orientation, ChunkCoord cp) {
        Integer cached = cache.get(cp);
        if (cached != null) {
            return cached;
        }

        // Highways can only occur at chunkZ that is a multiple of 8
        int mask = profile.HIGHWAY_DISTANCE_MASK;
        if (mask <= 0) {
            cache.put(cp, -1);
            return -1;
        }

        if ((cp.getCoord(orientation.getOpposite()) & mask) != 0) {
            cache.put(cp, -1);
            return -1;
        }

        // Disable highways that intersect with cityspheres
        if ((provider.getProfile().isSpace() || provider.getProfile().isSpheres()) && CitySphere.intersectsWithCitySphere(cp, provider)) {
            cache.put(cp, -1);
            return -1;
        }

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
                if (profile.HIGHWAY_REQUIRES_TWO_CITIES) {
                    valid = BuildingInfo.isCityRaw(lower, provider, profile) && BuildingInfo.isCityRaw(higher, provider, profile);
                } else {
                    valid = BuildingInfo.isCityRaw(lower, provider, profile) || BuildingInfo.isCityRaw(higher, provider, profile);
                }
                if (valid) {
                    // We have at least one city. Valid highway:
                    level = switch (profile.HIGHWAY_LEVEL_FROM_CITIES_MODE) {
                        case 0 -> BuildingInfo.getCityLevel(lower, provider);
                        case 1 -> Math.min(BuildingInfo.getCityLevel(lower, provider),
                                BuildingInfo.getCityLevel(higher, provider));
                        case 2 -> Math.max(BuildingInfo.getCityLevel(lower, provider),
                                BuildingInfo.getCityLevel(higher, provider));
                        case 3 -> (BuildingInfo.getCityLevel(lower, provider) +
                                BuildingInfo.getCityLevel(higher, provider)) / 2;
                        case 4 -> profile.HIGHWAY_NETWORK_LEVEL;
                        default -> throw new RuntimeException("Bad value for 'highwayLevelFromCities'!");
                    };
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

    private static boolean hasXHighway(ChunkCoord cp, LostCityProfile profile, PerlinNoiseGenerator14 noise) {
        return noise.getValue(cp.chunkX() / profile.HIGHWAY_MAINPERLIN_SCALE, cp.chunkZ() / profile.HIGHWAY_SECONDARYPERLIN_SCALE)
                > profile.HIGHWAY_PERLIN_FACTOR;
    }

    private static boolean hasZHighway(ChunkCoord cp, LostCityProfile profile, PerlinNoiseGenerator14 noise) {
        return noise.getValue(cp.chunkX() / profile.HIGHWAY_SECONDARYPERLIN_SCALE, cp.chunkZ() / profile.HIGHWAY_MAINPERLIN_SCALE)
                > profile.HIGHWAY_PERLIN_FACTOR;
    }

    private record HighwayNoise(PerlinNoiseGenerator14 x, PerlinNoiseGenerator14 z) {
    }

}
