package mcjty.lostcities.worldgen.lost;

import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.lost.cityassets.WorldStyle;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * This is a representation of a number of chunks (NxN) for the purpose of calculating multibuildings
 */
public class MultiChunk {

    // Multichunks are indexed by the chunk coordinates divided by the area size
    private static Map<ChunkCoord, MultiChunk> MULTICHUNKS = new HashMap<>();
    public static void cleanCache() {
        MULTICHUNKS.clear();
    }

    private final ChunkCoord coord;

    public MultiChunk(ChunkCoord coord) {
        this.coord = coord;
    }

    public static MultiChunk getOrCreate(WorldStyle style, ChunkCoord coord) {
        int areasize = style.getMultiSettings().areasize();
        ChunkCoord topleft = new ChunkCoord(coord.dimension(),
                Math.floorDiv(coord.chunkX(), areasize),
                Math.floorDiv(coord.chunkZ(), areasize));
        return MULTICHUNKS.computeIfAbsent(topleft, k -> new MultiChunk(topleft).calculateBuildings(style));
    }

    private MultiChunk calculateBuildings(WorldStyle style) {
        Random cityCenterRandom = new Random(coord.chunkX() * 797003437L + coord.chunkZ() * 295075153L);

        return this;
    }
}
