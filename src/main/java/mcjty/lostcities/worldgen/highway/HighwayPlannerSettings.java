package mcjty.lostcities.worldgen.highway;

import mcjty.lostcities.config.LostCityProfile;

public record HighwayPlannerSettings(
        int planningCellSize,
        int hubSampleSpacing,
        float hubMinimumPotential,
        int hubSearchRadiusCells,
        int minimumHubDistance,
        int maximumHubDistance,
        int maximumConnectionsPerHub,
        int minimumRouteLength,
        float routeCityPenalty,
        int levelFromCitiesMode,
        int networkLevel
) {
    public HighwayPlannerSettings {
        if (planningCellSize < 32 || planningCellSize > 512) {
            throw new IllegalArgumentException("Highway planning-cell size must be between 32 and 512 chunks");
        }
        if (hubSampleSpacing < 1 || hubSampleSpacing > planningCellSize) {
            throw new IllegalArgumentException("Highway hub sample spacing must be positive and no larger than the planning cell");
        }
        if (hubMinimumPotential < 0.0f || hubMinimumPotential > 1.0f) {
            throw new IllegalArgumentException("Highway hub minimum potential must be between 0 and 1");
        }
        if (hubSearchRadiusCells < 0 || hubSearchRadiusCells > 8) {
            throw new IllegalArgumentException("Highway hub search radius must be between 0 and 8 cells");
        }
        if (minimumHubDistance < 0 || minimumHubDistance > maximumHubDistance) {
            throw new IllegalArgumentException("Highway minimum hub distance must not exceed the maximum");
        }
        if (maximumHubDistance > 4096) {
            throw new IllegalArgumentException("Highway maximum hub distance must not exceed 4096 chunks");
        }
        if (maximumConnectionsPerHub < 1 || maximumConnectionsPerHub > 8) {
            throw new IllegalArgumentException("Highway maximum connection degree must be between 1 and 8");
        }
        if (minimumRouteLength < 0 || routeCityPenalty < 0.0f || routeCityPenalty > 1000.0f) {
            throw new IllegalArgumentException("Invalid highway route length or city penalty");
        }
        if (levelFromCitiesMode < 0 || levelFromCitiesMode > 4 || networkLevel < 0 || networkLevel > 32) {
            throw new IllegalArgumentException("Invalid highway level mode or fixed network level");
        }
    }

    public static HighwayPlannerSettings fromProfile(LostCityProfile profile) {
        return new HighwayPlannerSettings(
                profile.HIGHWAY_PLANNING_CELL_SIZE,
                profile.HIGHWAY_HUB_SAMPLE_SPACING,
                profile.HIGHWAY_HUB_MINIMUM_POTENTIAL,
                profile.HIGHWAY_HUB_SEARCH_RADIUS_CELLS,
                profile.HIGHWAY_MINIMUM_HUB_DISTANCE,
                profile.HIGHWAY_MAXIMUM_HUB_DISTANCE,
                profile.HIGHWAY_MAXIMUM_CONNECTIONS_PER_HUB,
                profile.HIGHWAY_MINIMUM_ROUTE_LENGTH,
                profile.HIGHWAY_ROUTE_CITY_PENALTY,
                profile.HIGHWAY_LEVEL_FROM_CITIES_MODE,
                profile.HIGHWAY_NETWORK_LEVEL);
    }
}
