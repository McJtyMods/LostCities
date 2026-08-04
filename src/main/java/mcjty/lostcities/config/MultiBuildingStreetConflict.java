package mcjty.lostcities.config;

import mcjty.lostcities.worldgen.street.PlannedRoadType;

import java.util.Locale;

public enum MultiBuildingStreetConflict {
    BLOCK_ALL,
    OVERRIDE_MINOR,
    OVERRIDE_ALL;

    public boolean roadBlocks(PlannedRoadType roadType) {
        return switch (this) {
            case BLOCK_ALL -> roadType != PlannedRoadType.NONE;
            case OVERRIDE_MINOR -> roadType == PlannedRoadType.PRIMARY;
            case OVERRIDE_ALL -> false;
        };
    }

    public static MultiBuildingStreetConflict byName(String name) {
        try {
            return valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown multi-building street conflict policy '" + name + "'", e);
        }
    }
}
