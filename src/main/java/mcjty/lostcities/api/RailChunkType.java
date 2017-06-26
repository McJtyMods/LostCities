package mcjty.lostcities.api;

/**
 * The type of a railway at this spot
 */
public enum RailChunkType {
    NONE(false),
    STATION_SURFACE(true),
    STATION_UNDERGROUND(true),
    STATION_EXTENSION_SURFACE(true),
    STATION_EXTENSION_UNDERGROUND(true),
    GOING_DOWN_TWO_FROM_SURFACE(false),
    GOING_DOWN_ONE_FROM_SURFACE(false),
    GOING_DOWN_FURTHER(false),
    HORIZONTAL(false),
    THREE_SPLIT(false),
    VERTICAL(false),
    DOUBLE_BEND(false),
    RAILS_END_HERE(false);

    private final boolean isStation;

    RailChunkType(boolean isStation) {
        this.isStation = isStation;
    }

    public boolean isStation() {
        return isStation;
    }
}
