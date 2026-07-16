package mcjty.lostcities.worldgen.street;

import java.util.List;

public record PlannedStreetInfo(
        PlannedRoadType roadType,
        boolean north,
        boolean south,
        boolean west,
        boolean east,
        int primaryBlockX,
        int primaryBlockZ,
        int primaryWestX,
        int primaryNorthZ,
        int primaryEastX,
        int primarySouthZ,
        double density,
        List<Integer> secondaryRoadsX,
        List<Integer> secondaryRoadsZ,
        TertiaryRoadSegment tertiarySegment
) {
    public boolean isRoad() {
        return roadType != PlannedRoadType.NONE;
    }

    public boolean connects(RoadDirection direction) {
        return switch (direction) {
            case NORTH -> north;
            case SOUTH -> south;
            case WEST -> west;
            case EAST -> east;
        };
    }
}
