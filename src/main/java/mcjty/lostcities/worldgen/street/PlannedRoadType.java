package mcjty.lostcities.worldgen.street;

public enum PlannedRoadType {
    NONE,
    TERTIARY,
    SECONDARY,
    PRIMARY;

    public static PlannedRoadType strongest(PlannedRoadType first, PlannedRoadType second) {
        return first.ordinal() >= second.ordinal() ? first : second;
    }
}
