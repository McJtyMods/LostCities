package mcjty.lostcities.worldgen.street;

import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.lost.Orientation;

/** A deterministic primary-road bridge span between two effective city roads. */
public record PlannedBridgeInfo(
        long id,
        Orientation orientation,
        ChunkCoord minimumEndpoint,
        ChunkCoord maximumEndpoint,
        int gapLength
) {
}
