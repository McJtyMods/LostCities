package mcjty.lostcities.worldgen.highway;

import java.util.List;

public record HighwayInfo(int xLevel, int zLevel, Classification classification, List<RouteHit> routeHits) {
    public static final HighwayInfo NONE = new HighwayInfo(-1, -1, Classification.NONE, List.of());

    public HighwayInfo {
        routeHits = List.copyOf(routeHits);
    }

    public boolean hasHighway() {
        return xLevel >= 0 || zLevel >= 0;
    }

    public enum Classification {
        NONE,
        X_HIGHWAY,
        Z_HIGHWAY,
        SAME_LEVEL_INTERSECTION,
        MULTI_LEVEL_INTERSECTION
    }

    public record RouteHit(HighwayRoute route, boolean xAxis, boolean zAxis, boolean bend) { }
}
