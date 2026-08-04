package mcjty.lostcities.worldgen.lost.cityassets;

import mcjty.lostcities.config.LostCityProfile;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable city-local settings after inherited city-style values have been
 * resolved against their profile fallbacks. Floor and cellar fields retain
 * their historical constraint semantics rather than becoming replacements.
 */
public record EffectiveCitySettings(
        float buildingChance,
        @Nullable Integer minFloorConstraint,
        @Nullable Integer maxFloorConstraint,
        @Nullable Integer minCellarConstraint,
        @Nullable Integer maxCellarConstraint,
        float parkChance,
        float openLotParkChance,
        float fountainChance,
        float frontChance,
        float corridorChance,
        boolean avoidFoliage,
        boolean parkBorder,
        boolean parkElevation,
        int parkStreetThreshold) {

    public static EffectiveCitySettings resolve(LostCityProfile profile, CityStyle style) {
        return new EffectiveCitySettings(
                valueOr(style.getBuildingChance(), profile.BUILDING_CHANCE),
                style.getMinFloorCount(),
                style.getMaxFloorCount(),
                style.getMinCellarCount(),
                style.getMaxCellarCount(),
                valueOr(style.getParkChance(), profile.PARK_CHANCE),
                valueOr(style.getOpenLotParkChance(), profile.OPEN_LOT_PARK_CHANCE),
                valueOr(style.getFountainChance(), profile.FOUNTAIN_CHANCE),
                valueOr(style.getFrontChance(), profile.BUILDING_FRONTCHANCE),
                valueOr(style.getCorridorChance(), profile.CORRIDOR_CHANCE),
                valueOr(style.getAvoidFoliage(), profile.AVOID_FOLIAGE),
                valueOr(style.getParkBorder(), profile.PARK_BORDER),
                valueOr(style.getParkElevation(), profile.PARK_ELEVATION),
                valueOr(style.getParkStreetThreshold(), profile.PARK_STREET_THRESHOLD));
    }

    public int constrainMinimumFloors(int floors) {
        return minFloorConstraint == null ? floors : Math.max(floors, minFloorConstraint);
    }

    public int constrainMaximumFloors(int floors) {
        return maxFloorConstraint == null ? floors : Math.min(floors, maxFloorConstraint);
    }

    public int constrainMaximumCellars(int cellars) {
        int result = maxCellarConstraint == null ? cellars : Math.min(cellars, maxCellarConstraint);
        return minCellarConstraint == null ? result : Math.max(result, minCellarConstraint);
    }

    private static float valueOr(@Nullable Float value, float fallback) {
        return value == null ? fallback : value;
    }

    private static int valueOr(@Nullable Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private static boolean valueOr(@Nullable Boolean value, boolean fallback) {
        return value == null ? fallback : value;
    }
}
