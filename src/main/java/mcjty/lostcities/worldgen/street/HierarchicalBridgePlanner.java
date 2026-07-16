package mcjty.lostcities.worldgen.street;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.config.StreetGenerationMode;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.LostCityTerrainFeature;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.Orientation;

/**
 * Deterministically resolves short water gaps along the raw primary-road field.
 * This is deliberately separate from {@link HierarchicalStreetPlanner}: the raw
 * mathematical street field remains independent of city and terrain queries.
 */
public final class HierarchicalBridgePlanner {

    private static final long BRIDGE_SPAN_SALT = 0x31d6a7f04c82be59L;
    private static final long BRIDGE_INTERSECTION_SALT = 0x67a14ce3b9052df8L;

    private HierarchicalBridgePlanner() {
    }

    public static PlannedBridgeInfo getBridgeInfo(BuildingInfo source, Orientation orientation) {
        if (source.provider.getStreetGenerationMode() != StreetGenerationMode.HIERARCHICAL_GRID_V1
                || source.isCity || !isPrimaryForOrientation(source.provider, source.coord, orientation)) {
            return null;
        }
        LostCityProfile profile = source.profile;
        if (!isGapChunk(source, source.coord, orientation)) {
            return null;
        }

        int maximumLength = profile.PLANNED_PRIMARY_BRIDGE_MAX_LENGTH;
        ChunkCoord minimumEndpoint = findEndpoint(source, source.coord, orientation, false, maximumLength);
        if (minimumEndpoint == null) {
            return null;
        }
        ChunkCoord maximumEndpoint = findEndpoint(source, source.coord, orientation, true, maximumLength);
        if (maximumEndpoint == null) {
            return null;
        }

        int gapLength = maximumEndpoint.getCoord(orientation) - minimumEndpoint.getCoord(orientation) - 1;
        if (gapLength < 1 || gapLength > maximumLength) {
            return null;
        }

        BuildingInfo minimumInfo = BuildingInfo.getBuildingInfo(minimumEndpoint, source.provider);
        BuildingInfo maximumInfo = BuildingInfo.getBuildingInfo(maximumEndpoint, source.provider);
        // The current bridge renderer is level with the base city surface. Higher
        // city levels need ramp assets and remain future work.
        if (!minimumInfo.isPrimaryRoad() || !maximumInfo.isPrimaryRoad()
                || minimumInfo.cityLevel != 0 || maximumInfo.cityLevel != 0) {
            return null;
        }

        long id = spanHash(source.provider, orientation, minimumEndpoint, maximumEndpoint, BRIDGE_SPAN_SALT);
        if (unitDouble(id) >= profile.PLANNED_PRIMARY_BRIDGE_CHANCE) {
            return null;
        }
        return new PlannedBridgeInfo(id, orientation, minimumEndpoint, maximumEndpoint, gapLength);
    }

    private static ChunkCoord findEndpoint(BuildingInfo source, ChunkCoord start, Orientation orientation,
                                           boolean positive, int maximumLength) {
        ChunkCoord cursor = start;
        for (int distance = 1; distance <= maximumLength + 1; distance++) {
            cursor = positive ? cursor.higher(orientation) : cursor.lower(orientation);
            if (isRawPrimaryCityEndpoint(source.provider, cursor)) {
                return cursor;
            }
            if (distance > maximumLength || !isGapChunk(source, cursor, orientation)) {
                return null;
            }
        }
        return null;
    }

    private static boolean isRawPrimaryCityEndpoint(IDimensionInfo provider, ChunkCoord coord) {
        LostCityProfile profile = BuildingInfo.getProfile(coord, provider);
        return BuildingInfo.isCityRaw(coord, provider, profile)
                && provider.getStreetPlanner().getRoadType(coord.chunkX(), coord.chunkZ()) == PlannedRoadType.PRIMARY;
    }

    private static boolean isGapChunk(BuildingInfo source, ChunkCoord coord, Orientation orientation) {
        IDimensionInfo provider = source.provider;
        LostCityProfile profile = BuildingInfo.getProfile(coord, provider);
        if (BuildingInfo.isCityRaw(coord, provider, profile) || !isPrimaryForOrientation(provider, coord, orientation)) {
            return false;
        }

        HierarchicalStreetPlanner planner = provider.getStreetPlanner();
        boolean intersection = planner.isHorizontalPrimary(coord.chunkZ()) && planner.isVerticalPrimary(coord.chunkX());
        if (intersection && orientation != preferredIntersectionOrientation(provider)) {
            return false;
        }

        // Biome tags catch oceans/rivers; the deterministic ocean-floor height
        // also catches inland lakes whose biome is plains or forest.
        return LostCityTerrainFeature.isWaterBiome(provider, coord)
                || provider.getHeightmap(coord).getHeight() < source.waterLevel;
    }

    private static boolean isPrimaryForOrientation(IDimensionInfo provider, ChunkCoord coord, Orientation orientation) {
        HierarchicalStreetPlanner planner = provider.getStreetPlanner();
        return orientation == Orientation.X
                ? planner.isHorizontalPrimary(coord.chunkZ())
                : planner.isVerticalPrimary(coord.chunkX());
    }

    private static Orientation preferredIntersectionOrientation(IDimensionInfo provider) {
        long value = provider.getSeed() ^ stableStringHash(provider.getType().location().toString()) ^ BRIDGE_INTERSECTION_SALT;
        return (mix64(value) & 1L) == 0 ? Orientation.X : Orientation.Z;
    }

    private static long spanHash(IDimensionInfo provider, Orientation orientation, ChunkCoord minimum, ChunkCoord maximum,
                                 long salt) {
        long value = provider.getSeed() ^ stableStringHash(provider.getType().location().toString()) ^ salt;
        value ^= mix64((long) minimum.chunkX() + 0x9e3779b97f4a7c15L);
        value ^= mix64((long) minimum.chunkZ() + 0xc2b2ae3d27d4eb4fL);
        value ^= mix64((long) maximum.chunkX() + 0x165667b19e3779f9L);
        value ^= mix64((long) maximum.chunkZ() + 0x85ebca77c2b2ae63L);
        value ^= orientation == Orientation.X ? 0x243f6a8885a308d3L : 0x13198a2e03707344L;
        return mix64(value);
    }

    private static double unitDouble(long hash) {
        return (hash >>> 11) * 0x1.0p-53;
    }

    private static long stableStringHash(String value) {
        long hash = 0xcbf29ce484222325L;
        for (int i = 0; i < value.length(); i++) {
            hash ^= value.charAt(i);
            hash *= 0x100000001b3L;
        }
        return mix64(hash);
    }

    private static long mix64(long value) {
        value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
        value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
        return value ^ (value >>> 31);
    }
}
