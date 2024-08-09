package mcjty.lostcities.worldgen.lost;

import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.CityStyle;
import mcjty.lostcities.worldgen.lost.cityassets.MultiBuilding;
import mcjty.lostcities.worldgen.lost.cityassets.WorldStyle;
import mcjty.lostcities.worldgen.lost.regassets.data.MultiSettings;
import mcjty.lostcities.worldgen.lost.regassets.data.ObjectSelector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This is a representation of a number of chunks (NxN) for the purpose of calculating multibuildings
 */
public class MultiChunk {

    // Multichunks are indexed by the chunk coordinates divided by the area size
    private static Map<ChunkCoord, MultiChunk> MULTICHUNKS = new HashMap<>();
    public static void cleanCache() {
        MULTICHUNKS.clear();
    }

    private final ChunkCoord mc;
    private final int areasize;
    private final String[][] buildingGrid;

    public MultiChunk(ChunkCoord mc, int areasize) {
        this.mc = mc;
        this.areasize = areasize;
        this.buildingGrid = new String[areasize][areasize];
        // Initialize to null
        for (int x = 0 ; x < areasize ; x++) {
            for (int z = 0 ; z < areasize ; z++) {
                buildingGrid[x][z] = null;
            }
        }
    }

    public static MultiChunk getOrCreate(IDimensionInfo provider, WorldStyle style, ChunkCoord coord) {
        int areasize = style.getMultiSettings().areasize();
        ChunkCoord topleft = getMultiCoord(coord, areasize);
        return MULTICHUNKS.computeIfAbsent(topleft, k -> new MultiChunk(topleft, areasize).calculateBuildings(provider));
    }

    private static @NotNull ChunkCoord getMultiCoord(ChunkCoord coord, int areasize) {
        return new ChunkCoord(coord.dimension(),
                Math.floorDiv(coord.chunkX(), areasize),
                Math.floorDiv(coord.chunkZ(), areasize));
    }

    private MultiChunk calculateBuildings(IDimensionInfo provider) {
        Random rand = new Random(mc.chunkX() * 797013493L + mc.chunkZ() * 295085213L);

        // Determine how many multibuildings we want to place in this multichunk
        MultiSettings settings = provider.getWorldStyle().getMultiSettings();
        int min = settings.minimum();
        int max = settings.maximum();
        int cnt = min + rand.nextInt(max - min + 1);
        if (cnt <= 0) {
            // No buildings, early exit
            return this;
        }

        // Get all possible multibuildings for all the chunks in this multichunk
        Set<ObjectSelector> multiBuildingSelectors = new HashSet<>();
        for (int x = 0 ; x < areasize ; x++) {
            for (int z = 0 ; z < areasize ; z++) {
                ChunkCoord cc = new ChunkCoord(mc.dimension(), mc.chunkX() * areasize + x, mc.chunkZ() * areasize + z);
                CityStyle cityStyle = City.getCityStyle(cc, provider, provider.getProfile());
                if (cityStyle != null) {
                    multiBuildingSelectors.addAll(cityStyle.getMultiBuildingSelector());
                }
            }
        }

        // Make a list from these selectors with the largest buildings first
        List<ObjectSelector> sorted = new ArrayList<>(multiBuildingSelectors);
        sorted.sort((b1, b2) -> {
            MultiBuilding building1 = AssetRegistries.MULTI_BUILDINGS.get(provider.getWorld(), b1.value());
            if (building1 == null) {
                throw new RuntimeException("Cannot find multibuilding: " + b1.value());
            }
            MultiBuilding building2 = AssetRegistries.MULTI_BUILDINGS.get(provider.getWorld(), b2.value());
            if (building2 == null) {
                throw new RuntimeException("Cannot find multibuilding: " + b2.value());
            }
            return Integer.compare(building2.getDimX() + building2.getDimZ(), building1.getDimX() + building1.getDimZ());
        });

        // For every building we want to place, try to find a spot
        for (int i = 0 ; i < cnt ; i++) {
        }

        return this;
    }
}
