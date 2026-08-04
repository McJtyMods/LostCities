package mcjty.lostcities.worldgen.street;

import mcjty.lostcities.config.LostCityProfile;

public record StreetPlannerSettings(
        int primarySpacingX,
        int primarySpacingZ,
        float primaryOptionalChance,
        int primaryForceEvery,
        int secondaryMinCountX,
        int secondaryMaxCountX,
        int secondaryMinCountZ,
        int secondaryMaxCountZ,
        int minimumRoadSeparation,
        int minimumEdgeDistance,
        float tertiaryChance,
        int tertiaryMinLength,
        int tertiaryMaxLength
) {
    public StreetPlannerSettings {
        if (primarySpacingX < 8 || primarySpacingZ < 8) {
            throw new IllegalArgumentException("Primary road candidate spacing must be at least 8 chunks");
        }
        if (primaryOptionalChance < 0 || primaryOptionalChance > 1
                || primaryForceEvery < 1 || primaryForceEvery > 16) {
            throw new IllegalArgumentException("Invalid primary road activation settings");
        }
        if (secondaryMinCountX < 0 || secondaryMinCountZ < 0
                || secondaryMinCountX > secondaryMaxCountX || secondaryMinCountZ > secondaryMaxCountZ) {
            throw new IllegalArgumentException("Invalid secondary road count range");
        }
        if (minimumRoadSeparation < 2 || minimumEdgeDistance < 2) {
            throw new IllegalArgumentException("Road separation and edge distance must be at least 2 chunks");
        }
        if (tertiaryChance < 0 || tertiaryChance > 1 || tertiaryMinLength < 1 || tertiaryMinLength > tertiaryMaxLength) {
            throw new IllegalArgumentException("Invalid tertiary road settings");
        }
    }

    public static StreetPlannerSettings fromProfile(LostCityProfile profile) {
        return new StreetPlannerSettings(
                profile.PRIMARY_ROAD_SPACING_X,
                profile.PRIMARY_ROAD_SPACING_Z,
                profile.PRIMARY_ROAD_OPTIONAL_CHANCE,
                profile.PRIMARY_ROAD_FORCE_EVERY,
                profile.SECONDARY_ROAD_MIN_COUNT_X,
                profile.SECONDARY_ROAD_MAX_COUNT_X,
                profile.SECONDARY_ROAD_MIN_COUNT_Z,
                profile.SECONDARY_ROAD_MAX_COUNT_Z,
                profile.MINIMUM_ROAD_SEPARATION,
                profile.MINIMUM_ROAD_EDGE_DISTANCE,
                profile.TERTIARY_ROAD_CHANCE,
                profile.TERTIARY_ROAD_MIN_LENGTH,
                profile.TERTIARY_ROAD_MAX_LENGTH);
    }
}
