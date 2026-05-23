package mcjty.lostcities.worldgen.lost;

import mcjty.lostcities.api.RailChunkType;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.setup.Config;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.Counter;
import mcjty.lostcities.varia.TimedCache;
import mcjty.lostcities.varia.Tools;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.Building;
import mcjty.lostcities.worldgen.lost.cityassets.CityStyle;
import mcjty.lostcities.worldgen.lost.cityassets.MultiBuilding;
import mcjty.lostcities.worldgen.lost.regassets.data.MultiSettings;
import mcjty.lostcities.worldgen.lost.regassets.data.WorldSettings;
import org.jetbrains.annotations.NotNull;

import java.util.*;

// @todo handle predefined cities

/**
 * This is a representation of a number of chunks (NxN) for the purpose of calculating multibuildings
 */
public class MultiChunk {

    record MB(String name, int offsetX, int offsetZ) {}

    // Multichunks are indexed by the chunk coordinates divided by the area size
    private static final TimedCache<ChunkCoord, MultiChunk> MULTICHUNKS = new TimedCache<>(Config.CACHE_CLEANUP_SECONDS::get);
    public static void cleanCache() {
        MULTICHUNKS.clear();
    }

    private final ChunkCoord mc;    // This coordinate is divided by areasize
    private final ChunkCoord topleft;
    private final int areasize;
    private final MB[][] buildingGrid;

    public MultiChunk(ChunkCoord mc, int areasize) {
        this.mc = mc;
        this.topleft = new ChunkCoord(mc.dimension(), mc.chunkX() * areasize, mc.chunkZ() * areasize);
        this.areasize = areasize;
        this.buildingGrid = new MB[areasize][areasize];
        // Initialize to null
        for (int x = 0 ; x < areasize ; x++) {
            for (int z = 0 ; z < areasize ; z++) {
                buildingGrid[x][z] = null;
            }
        }
    }

    public static synchronized MultiChunk getOrCreate(IDimensionInfo provider, ChunkCoord coord) {
        int areasize = provider.getWorldStyle().getMultiSettings().areasize();
        ChunkCoord mc = getMultiCoord(coord, areasize);
        return MULTICHUNKS.computeIfAbsent(mc, k -> new MultiChunk(mc, areasize).calculateBuildings(provider));
    }

    public MB getMultiBuilding(ChunkCoord coord) {
        return buildingGrid[coord.chunkX() - topleft.chunkX()][coord.chunkZ() - topleft.chunkZ()];
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

        ChunkCoord topleft = new ChunkCoord(mc.dimension(), mc.chunkX() * areasize, mc.chunkZ() * areasize);
        int cityLevel = BuildingInfo.getCityLevel(topleft, provider);

        // Find all city styles in this multichunk and count them
        Counter<CityStyle> cityStyleCounter = new Counter<>();
        for (int x = 0 ; x < areasize ; x++) {
            for (int z = 0 ; z < areasize ; z++) {
                CityStyle cityStyle = City.getCityStyle(topleft.offset(x, z), provider, provider.getProfile());
                if (cityStyle == null) {
                    throw new RuntimeException("Cannot find city style for chunk: " + topleft.offset(x, z));
                }
                cityStyleCounter.add(cityStyle);
            }
        }

        // Get all the desired multibuildings based on the percentage of the city styles and the counter
        List<MultiBuilding> multiBuildings = new ArrayList<>();
        List<CityStyle> styleList = new ArrayList<>(cityStyleCounter.getMap().keySet());
        styleList.sort(Comparator.comparing(CityStyle::getName));
        List<CityStyle> styleForBuilding = new ArrayList<>();
        for (int i = 0 ; i < cnt ; i++) {
            CityStyle cityStyle = Tools.getRandomFromList(rand, styleList, style -> (float) cityStyleCounter.get(style));
            String multiBuilding = cityStyle.getRandomMultiBuilding(rand, topleft);
            MultiBuilding mb = AssetRegistries.MULTI_BUILDINGS.get(provider.getWorld(), multiBuilding);
            if (mb == null) {
                throw new RuntimeException("Cannot find multibuilding: " + multiBuilding);
            }
            multiBuildings.add(mb);
            styleForBuilding.add(cityStyle);
        }

        // Sort the multibuildings by size. Largest first
        multiBuildings.sort((b1, b2) -> Integer.compare(b2.getDimX() + b2.getDimZ(), b1.getDimX() + b1.getDimZ()));

        // For every building we want to place, try to find a spot
        for (int i = 0 ; i < multiBuildings.size() ; i++) {
            MultiBuilding mb = multiBuildings.get(i);
            // Find the maximum possible number of cellars for all buildings in this multibuilding
            int maxCellars = 0;
            for (String b : mb.getBuildingSet()) {
                Building building = AssetRegistries.BUILDINGS.get(provider.getWorld(), b);
                if (building == null) {
                    throw new RuntimeException("Cannot find building: " + b);
                }
                if (building.getMaxCellars() > maxCellars) {
                    maxCellars = building.getMaxCellars();
                }
            }
            int dimX = mb.getDimX();
            int dimZ = mb.getDimZ();
            // Try to find a spot with a number of attempts
            int attempts = settings.attempts();
            for (int att = 0 ; att < attempts ; att++) {
                int x = rand.nextInt(areasize - dimX + 1);
                int z = rand.nextInt(areasize - dimZ + 1);
                if (canPlaceBuilding(topleft, provider, provider.getProfile(), styleForBuilding.get(i), mb, cityLevel, maxCellars, x, z)) {
                    placeBuilding(mb, x, z);
                    break;
                }
            }
        }

        return this;
    }

    private void dump() {
        // Make a debug dump of the grid in this multichunk with each building a different character
        Map<String, String> charMap = new HashMap<>();
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        System.out.println("################################");
        System.out.println("mc = " + mc);
        for (int z = 0 ; z < areasize ; z++) {
            for (int x = 0 ; x < areasize ; x++) {
                MB building = buildingGrid[x][z];
                if (building == null) {
                    System.out.print(" ");
                } else {
                    String s = charMap.get(building.name);
                    if (s == null) {
                        s = chars.substring(0, 1);
                        chars = chars.substring(1);
                        charMap.put(building.name, s);
                    }
                    System.out.print(s);
                }
            }
            System.out.println();
        }
    }

    private boolean canPlaceBuilding(ChunkCoord topleft, IDimensionInfo provider, LostCityProfile profile, CityStyle buildingCityStyle, MultiBuilding building,
                                     int cityLevel, int maxCellars, int x, int z) {
        int partlevel = provider.getWorldStyle().getWorldSettings().railPartHeight6();
        int correctStyle = 0;
        for (int xx = 0 ; xx < building.getDimX() ; xx++) {
            for (int zz = 0 ; zz < building.getDimZ() ; zz++) {
                if (buildingGrid[x+xx][z+zz] != null) {
                    return false;
                }
                ChunkCoord coord = topleft.offset(x + xx, z + zz);
                if (City.isChunkOccupied(provider, coord)) {
                    return false;
                }
                Railway.RailChunkInfo railChunkInfo = Railway.getRailChunkType(coord, provider, profile);
                RailChunkType type = railChunkInfo.getType();
                boolean atSurface = type.isSurface() || type.isStation();

                if (atSurface || !BuildingInfo.isCityRaw(coord, provider, profile) || BuildingInfo.hasHighway(coord, provider, profile)) {
                    return false;
                }
                WorldSettings.RailwayAvoidance avoidance = provider.getWorldStyle().getWorldSettings().railwayAvoidance();
                if (type != RailChunkType.NONE && avoidance != WorldSettings.RailwayAvoidance.BLOCK_RAILWAY) {
                    int level = railChunkInfo.getLevel();
                    int max = Math.min(cityLevel - level - partlevel, maxCellars);
                    if (max < maxCellars) {
                        return false;
                    }
                }
                CityStyle cityStyle = City.getCityStyle(coord, provider, profile);
                if (Objects.equals(cityStyle, buildingCityStyle)) {
                    correctStyle++;
                }
            }
        }
        // Sufficient chunks need to be the correct cityStyle
        float correctStyleFactor = provider.getWorldStyle().getMultiSettings().correctStyleFactor();
        if (correctStyle < building.getDimX() * building.getDimZ() * correctStyleFactor) {
            return false;
        }
        return true;
    }

    private void placeBuilding(MultiBuilding building, int x, int z) {
        for (int xx = 0 ; xx < building.getDimX() ; xx++) {
            for (int zz = 0 ; zz < building.getDimZ() ; zz++) {
                buildingGrid[x+xx][z+zz] = new MB(building.getName(), xx, zz);
            }
        }
    }
}
