package mcjty.lostcities.worldgen.highway;

import java.util.List;

public record HighwayRoute(
        HighwayConnectionKey key,
        HighwayHub firstHub,
        HighwayHub secondHub,
        List<HighwaySegment> segments,
        int highwayLevel,
        RouteShape shape,
        int routeLength,
        long cityPenalty
) {
    public HighwayRoute {
        segments = List.copyOf(segments);
    }

    public boolean contains(int chunkX, int chunkZ) {
        return segments.stream().anyMatch(segment -> segment.contains(chunkX, chunkZ));
    }

    public enum RouteShape {
        STRAIGHT,
        HORIZONTAL_THEN_VERTICAL,
        VERTICAL_THEN_HORIZONTAL
    }
}
