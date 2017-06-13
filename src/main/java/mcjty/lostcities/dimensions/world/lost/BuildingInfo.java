package mcjty.lostcities.dimensions.world.lost;

import mcjty.lostcities.dimensions.world.LostCityChunkGenerator;
import mcjty.lostcities.dimensions.world.lost.cityassets.*;
import mcjty.lostcities.varia.Counter;
import mcjty.lostcities.varia.QualityRandom;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class BuildingInfo {
    public final int chunkX;
    public final int chunkZ;
    public final long seed;
    public final LostCityChunkGenerator provider;

    public final boolean isCity;
    public final boolean hasBuilding;
    public final int building2x2Section;    // -1 for not, 0 for top left, 1 for top right, 2 for bottom left, 3 for bottom right

    public final MultiBuilding multiBuilding;
    public final Building buildingType;
    public final BuildingPart fountainType;
    public final BuildingPart parkType;
    public final BuildingPart bridgeType;
    public final BuildingPart stairType;
    private final float stairPriority;      // A random number that indicates if this chunk should get a stair if there are competing stairs around it. The highest wins
    public final StreetType streetType;
    private final int floors;
    public final int floorsBelowGround;
    public final BuildingPart[] floorTypes;
    public final boolean[] connectionAtX;
    public final boolean[] connectionAtZ;

    public final int cityLevel;         // The first floor of buildings starts at groundLevel + cityLevel * 6

    public final boolean xBridge;       // A boolean indicating that this chunk is a candidate for holding a bridge (no guarantee)
    public final boolean zBridge;       // A boolean indicating that this chunk is a candidate for holding a bridge (no guarantee)

    public final boolean xRailCorridor; // A boolean indicating that this chunk is a candidate for holding a corridor (no guarantee)
    public final boolean zRailCorridor; // A boolean indicating that this chunk is a candidate for holding a corridor (no guarantee)

    public final Block doorBlock;

    // Transient info that is calculated on demand
    private BuildingInfo xmin = null;
    private BuildingInfo xmax = null;
    private BuildingInfo zmin = null;
    private BuildingInfo zmax = null;
    private DamageArea damageArea = null;
    private Palette palette = null;
    private CompiledPalette compiledPalette = null;

    private boolean xBridgeTypeCalculated = false;
    private boolean zBridgeTypeCalculated = false;
    private BuildingPart xBridgeType = null;
    private BuildingPart zBridgeType = null;

    private boolean stairsCalculated = false;
    private Direction stairDirection;
    private boolean actualStairsCalculated = false;
    private Direction actualStairDirection;

    // A list of todo's for mob spawners and other things
    private final List<Pair<BlockPos, String>> mobSpawnerTodo = new ArrayList<>();
    private final List<BlockPos> chestTodo = new ArrayList<>();

    // BuildingInfo cache
    private static Map<Pair<Integer, Integer>, BuildingInfo> buildingInfoMap = new HashMap<>();
    private static Map<Pair<Integer, Integer>, Boolean> isCityMap = new HashMap<>();
    private static Map<Pair<Integer, Integer>, Boolean> hasBuildingMap = new HashMap<>();
    private static Map<Pair<Integer, Integer>, CityStyle> cityStyleCache = new HashMap<>();

    public void addChestTodo(BlockPos pos) {
        chestTodo.add(pos);
    }

    public List<BlockPos> getChestTodo() {
        return chestTodo;
    }

    public void clearChestTodo() {
        chestTodo.clear();
    }

    public void addSpawnerTodo(BlockPos pos, String mobId) {
        mobSpawnerTodo.add(Pair.of(pos, mobId));
    }

    public List<Pair<BlockPos, String>> getMobSpawnerTodo() {
        return mobSpawnerTodo;
    }

    public void clearMobSpawnerTodo() {
        mobSpawnerTodo.clear();
    }

    public CompiledPalette getCompiledPalette() {
        if (compiledPalette == null) {
            compiledPalette = new CompiledPalette(palette);
        }
        return compiledPalette;
    }

    public DamageArea getDamageArea() {
        if (damageArea == null) {
            damageArea = new DamageArea(seed, chunkX, chunkZ, provider);
        }
        return damageArea;
    }

    public Set<ChunkPos> findConnectedStreets() {
        Set<ChunkPos> streets = new HashSet<>();
        Queue<ChunkPos> todo = new ArrayDeque<>();
        todo.add(new ChunkPos(chunkX, chunkZ));
        while (!todo.isEmpty()) {
            ChunkPos cp = todo.poll();
            if (isCity(cp.chunkXPos, cp.chunkZPos, seed, provider) && !hasBuilding(cp.chunkXPos, cp.chunkZPos, seed, provider) && !streets.contains(cp)) {
                streets.add(cp);
                todo.add(new ChunkPos(cp.chunkXPos-1, cp.chunkZPos));
                todo.add(new ChunkPos(cp.chunkXPos+1, cp.chunkZPos));
                todo.add(new ChunkPos(cp.chunkXPos, cp.chunkZPos-1));
                todo.add(new ChunkPos(cp.chunkXPos, cp.chunkZPos+1));
            }
        }
        return streets;
    }

    public CityStyle getCityStyle() {
        Pair<Integer, Integer> key = Pair.of(chunkX, chunkZ);
        if (!cityStyleCache.containsKey(key)) {
            CityStyle cityStyle;
            // If this is a street we find all other street chunks connected to this and pick the cityStyle
            // that represents the majority. This is to prevent streets from switching style randomly if two
            // different styled cities mix
            if (isCity && !hasBuilding) {
                Set<ChunkPos> connectedStreets = findConnectedStreets();
                Counter<String> counter = new Counter<>();
                for (ChunkPos cp : connectedStreets) {
                    cityStyle = City.getCityStyle(seed, cp.chunkXPos, cp.chunkZPos, provider);
                    counter.add(cityStyle.getName());
                }
                cityStyle = AssetRegistries.CITYSTYLES.get(counter.getMostOccuring());
            } else {
                cityStyle = City.getCityStyle(seed, chunkX, chunkZ, provider);
            }
            cityStyleCache.put(key, cityStyle);
            return cityStyle;
        }
        return cityStyleCache.get(key);
    }

    private void createPalette(Random rand) {
        Style style;
        if (!isCity) {
            style = AssetRegistries.STYLES.get("outside");
        } else {
            style = AssetRegistries.STYLES.get(getCityStyle().getStyle());
        }
        palette = style.getRandomPalette(provider, rand);
    }

    // x between 0 and 15, z between 0 and 15
    public BuildingInfo getAdjacent(int x, int z) {
        if (x == 0) {
            return getXmin();
        } else if (x == 15) {
            return getXmax();
        } else if (z == 0) {
            return getZmin();
        } else if (z == 15) {
            return getZmax();
        } else {
            return null;
        }
    }

    public BuildingInfo getXmin() {
        if (xmin == null) {
            xmin = getBuildingInfo(chunkX - 1, chunkZ, seed, provider);
        }
        return xmin;
    }

    public BuildingInfo getXmax() {
        if (xmax == null) {
            xmax = getBuildingInfo(chunkX + 1, chunkZ, seed, provider);
        }
        return xmax;
    }

    public BuildingInfo getZmin() {
        if (zmin == null) {
            zmin = getBuildingInfo(chunkX, chunkZ - 1, seed, provider);
        }
        return zmin;
    }

    public BuildingInfo getZmax() {
        if (zmax == null) {
            zmax = getBuildingInfo(chunkX, chunkZ + 1, seed, provider);
        }
        return zmax;
    }

    public int getMaxHeight() {
        return hasBuilding ? (getCityGroundLevel() + floors * 6) : getCityGroundLevel();
    }

    public int getCityGroundLevel() {
        return provider.profile.GROUNDLEVEL + cityLevel * 6;
    }

    public int getNumFloors() {
        return floors;
    }

    public BuildingPart getFloor(int l) {
        return floorTypes[l + floorsBelowGround];
    }

    private Building getBuilding() {
        return buildingType;
    }

    public static boolean isCity(int chunkX, int chunkZ, long seed, LostCityChunkGenerator provider) {
        Pair<Integer, Integer> key = Pair.of(chunkX, chunkZ);
        if (isCityMap.containsKey(key)) {
            return isCityMap.get(key);
        } else {
            float cityFactor = City.getCityFactor(seed, chunkX, chunkZ, provider);
            boolean isCity = cityFactor > provider.profile.CITY_THRESSHOLD;
            isCityMap.put(key, isCity);
            return isCity;
        }
    }

    public static boolean hasBuilding(int chunkX, int chunkZ, long seed, LostCityChunkGenerator provider) {
        Pair<Integer, Integer> key = Pair.of(chunkX, chunkZ);
        if (hasBuildingMap.containsKey(key)) {
            return hasBuildingMap.get(key);
        }
        boolean isCity = isCity(chunkX, chunkZ, seed, provider);

        int section;
        if (isTopLeftOf2x2Building(chunkX, chunkZ, seed, provider)) {
            section = 0;
        } else if (isTopLeftOf2x2Building(chunkX - 1, chunkZ, seed, provider)) {
            section = 1;
        } else if (isTopLeftOf2x2Building(chunkX, chunkZ - 1, seed, provider)) {
            section = 2;
        } else if (isTopLeftOf2x2Building(chunkX - 1, chunkZ - 1, seed, provider)) {
            section = 3;
        } else {
            section = -1;
        }

        Random rand = getBuildingRandom(chunkX, chunkZ, seed);
        float bc = rand.nextFloat();
        boolean b = section >= 0 || (isCity && (chunkX != 0 || chunkZ != 0) && bc < provider.profile.BUILDING_CHANCE);
        hasBuildingMap.put(key, b);
        return b;
    }

    private BuildingInfo calculateTopLeft() {
        switch (building2x2Section) {
            case 0:
                return this;
            case 1:
                return getXmin();
            case 2:
                return getZmin();
            case 3:
                return getXmin().getZmin();
            default:
                throw new RuntimeException("What!");
        }
    }

    private static boolean isCandidateForTopLeftOf2x2Building(int chunkX, int chunkZ, long seed, LostCityChunkGenerator provider) {
        if (chunkX == 0 && chunkZ == 0) {
            return false;
        }
        boolean isCity = isCity(chunkX, chunkZ, seed, provider);
        if (isCity) {
            Random rand = getBuildingRandom(chunkX, chunkZ, seed);
            return rand.nextFloat() < provider.profile.BUILDING2X2_CHANCE;
        } else {
            return false;
        }
    }

    private static boolean isTopLeftOf2x2Building(int chunkX, int chunkZ, long seed, LostCityChunkGenerator provider) {
        if (isCandidateForTopLeftOf2x2Building(chunkX, chunkZ, seed, provider) &&
                !isCandidateForTopLeftOf2x2Building(chunkX - 1, chunkZ, seed, provider) &&
                !isCandidateForTopLeftOf2x2Building(chunkX - 1, chunkZ - 1, seed, provider) &&
                !isCandidateForTopLeftOf2x2Building(chunkX, chunkZ - 1, seed, provider) &&

                !isCandidateForTopLeftOf2x2Building(chunkX + 1, chunkZ - 1, seed, provider) &&
                !isCandidateForTopLeftOf2x2Building(chunkX + 1, chunkZ, seed, provider) &&
                !isCandidateForTopLeftOf2x2Building(chunkX + 1, chunkZ + 1, seed, provider) &&
                !isCandidateForTopLeftOf2x2Building(chunkX, chunkZ + 1, seed, provider) &&
                !isCandidateForTopLeftOf2x2Building(chunkX - 1, chunkZ + 1, seed, provider)
                ) {
            return isCity(chunkX + 1, chunkZ, seed, provider) && isCity(chunkX + 1, chunkZ + 1, seed, provider) && isCity(chunkX, chunkZ + 1, seed, provider);
        } else {
            return false;
        }
    }

    public static void cleanBuildingInfoCache() {
        buildingInfoMap.clear();
        isCityMap.clear();
        hasBuildingMap.clear();
        cityStyleCache.clear();
    }

    public static BuildingInfo getBuildingInfo(int chunkX, int chunkZ, long seed, LostCityChunkGenerator provider) {
        Pair<Integer, Integer> key = Pair.of(chunkX, chunkZ);
        if (buildingInfoMap.containsKey(key)) {
            return buildingInfoMap.get(key);
        }
        BuildingInfo info = new BuildingInfo(chunkX, chunkZ, seed, provider);
        buildingInfoMap.put(key, info);
        return info;
    }

    private BuildingInfo(int chunkX, int chunkZ, long seed, LostCityChunkGenerator provider) {
        this.provider = provider;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.seed = seed;
        isCity = isCity(chunkX, chunkZ, seed, provider);

        if (isTopLeftOf2x2Building(chunkX, chunkZ, seed, provider)) {
            building2x2Section = 0;
        } else if (isTopLeftOf2x2Building(chunkX - 1, chunkZ, seed, provider)) {
            building2x2Section = 1;
        } else if (isTopLeftOf2x2Building(chunkX, chunkZ - 1, seed, provider)) {
            building2x2Section = 2;
        } else if (isTopLeftOf2x2Building(chunkX - 1, chunkZ - 1, seed, provider)) {
            building2x2Section = 3;
        } else {
            building2x2Section = -1;
        }

        Random rand = getBuildingRandom(chunkX, chunkZ, seed);
        float bc = rand.nextFloat();
        hasBuilding = building2x2Section >= 0 || (isCity && (chunkX != 0 || chunkZ != 0) && bc < provider.profile.BUILDING_CHANCE);

        // In a 2x2 building we copy all information from the top-left chunk
        if (building2x2Section >= 1) {
            BuildingInfo topleft = calculateTopLeft();
            multiBuilding = topleft.multiBuilding;
            if (multiBuilding != null) {
                switch (building2x2Section) {
                    case 1:
                        buildingType = AssetRegistries.BUILDINGS.get(multiBuilding.get(1, 0));
                        break;
                    case 2:
                        buildingType = AssetRegistries.BUILDINGS.get(multiBuilding.get(0, 1));
                        break;
                    case 3:
                        buildingType = AssetRegistries.BUILDINGS.get(multiBuilding.get(1, 1));
                        break;
                    default:
                        throw new RuntimeException("What 2!");
                }
            } else {
                buildingType = topleft.buildingType;
            }
            cityLevel = topleft.cityLevel;
            streetType = topleft.streetType;
            fountainType = topleft.fountainType;
            parkType = topleft.parkType;
            floors = topleft.floors;
            floorsBelowGround = topleft.floorsBelowGround;
            doorBlock = topleft.doorBlock;
            bridgeType = topleft.bridgeType;
            stairType = topleft.stairType;
            stairPriority = topleft.stairPriority;
            palette = topleft.palette;
            compiledPalette = topleft.getCompiledPalette();
        } else {
            CityStyle cs = getCityStyle();
            if (building2x2Section == 0) {
                multiBuilding = AssetRegistries.MULTI_BUILDINGS.get(cs.getRandomMultiBuilding(provider, rand));
                buildingType = AssetRegistries.BUILDINGS.get(multiBuilding.get(0, 0));
            } else {
                multiBuilding = null;
                buildingType = AssetRegistries.BUILDINGS.get(cs.getRandomBuilding(provider, rand));
            }

            // @todo: average out nearby biomes?
            Biome[] biomes = provider.worldObj.getBiomeProvider().getBiomesForGeneration(null, (chunkX - 1) * 4 - 2, chunkZ * 4 - 2, 10, 10);
            float height = 0.0f;
            for (Biome biome : biomes) {
                height += biome.getBaseHeight();
            }
            height /= biomes.length;
            if (height < 0.3f) {
                cityLevel = 0;
            } else if (height < 0.6f) {
                cityLevel = 1;
            } else if (height < 2) {
                cityLevel = 2;
            } else {
                cityLevel = 3;
            }

            if (rand.nextDouble() < .2f) {
                streetType = StreetType.values()[rand.nextInt(StreetType.values().length)];
            } else {
                streetType = StreetType.NORMAL;
            }
            if (rand.nextFloat() < provider.profile.FOUNTAIN_CHANCE) {
                fountainType = AssetRegistries.PARTS.get(cs.getRandomFountain(provider, rand));
            } else {
                fountainType = null;
            }
            parkType = AssetRegistries.PARTS.get(cs.getRandomPark(provider, rand));
            float cityFactor = City.getCityFactor(seed, chunkX, chunkZ, provider);
            int f = provider.profile.BUILDING_MINFLOORS + rand.nextInt((int) (provider.profile.BUILDING_MINFLOORS_CHANCE + (cityFactor + .1f) * (provider.profile.BUILDING_MAXFLOORS_CHANCE - provider.profile.BUILDING_MINFLOORS_CHANCE)));
            if (f > provider.profile.BUILDING_MAXFLOORS) {
                f = provider.profile.BUILDING_MAXFLOORS;
            }
            floors = f + 1;
            int maxcellars = provider.profile.BUILDING_MAXCELLARS + cityLevel;
            floorsBelowGround = provider.profile.BUILDING_MINCELLARS + ((maxcellars <= 0) ? 0 : rand.nextInt(maxcellars));
            doorBlock = getRandomDoor(rand);
            bridgeType = AssetRegistries.PARTS.get(cs.getRandomBridge(provider, rand));
            stairType = AssetRegistries.PARTS.get(cs.getRandomStair(provider, rand));
            stairPriority = rand.nextFloat();
            createPalette(rand);
        }

        floorTypes = new BuildingPart[floors + floorsBelowGround + 1];
        connectionAtX = new boolean[floors + floorsBelowGround + 1];
        connectionAtZ = new boolean[floors + floorsBelowGround + 1];
        Building building = getBuilding();
        for (int i = 0; i <= floors + floorsBelowGround; i++) {
            String randomPart = building.getRandomPart(rand, new Building.LevelInfo(0 /*todo*/, i - floorsBelowGround, floorsBelowGround, floors));
            floorTypes[i] = AssetRegistries.PARTS.get(randomPart);
            connectionAtX[i] = isCity(chunkX - 1, chunkZ, seed, provider) && (rand.nextFloat() < provider.profile.BUILDING_DOORWAYCHANCE);
            connectionAtZ[i] = isCity(chunkX, chunkZ - 1, seed, provider) && (rand.nextFloat() < provider.profile.BUILDING_DOORWAYCHANCE);
        }

        if (hasBuilding && floorsBelowGround > 0) {
            xRailCorridor = false;
            zRailCorridor = false;
        } else {
            xRailCorridor = rand.nextFloat() < provider.profile.CORRIDOR_CHANCE;
            zRailCorridor = rand.nextFloat() < provider.profile.CORRIDOR_CHANCE;
        }

        if (isCity) {
            xBridge = false;
            zBridge = false;
        } else {
            xBridge = rand.nextFloat() < provider.profile.BRIDGE_CHANCE;
            zBridge = rand.nextFloat() < provider.profile.BRIDGE_CHANCE;
        }
    }

    private Block getRandomDoor(Random rand) {
        Block doorBlock;
        switch (rand.nextInt(7)) {
            case 0:
                doorBlock = Blocks.BIRCH_DOOR;
                break;
            case 1:
                doorBlock = Blocks.ACACIA_DOOR;
                break;
            case 2:
                doorBlock = Blocks.DARK_OAK_DOOR;
                break;
            case 3:
                doorBlock = Blocks.SPRUCE_DOOR;
                break;
            case 4:
                doorBlock = Blocks.OAK_DOOR;
                break;
            case 5:
                doorBlock = Blocks.JUNGLE_DOOR;
                break;
            case 6:
                doorBlock = Blocks.IRON_DOOR;
                break;
            default:
                doorBlock = Blocks.OAK_DOOR;
        }
        return doorBlock;
    }

    public boolean isStreetSection() {
        return isCity && !hasBuilding;
    }

    public boolean isElevatedParkSection() {
        if (!isStreetSection()) {
            return false;
        }
        if (!getXmin().isStreetSection()) {
            return false;
        }
        if (!getXmax().isStreetSection()) {
            return false;
        }
        if (!getZmin().isStreetSection()) {
            return false;
        }
        if (!getZmax().isStreetSection()) {
            return false;
        }
        int cnt = 0;
        cnt += getXmin().getZmin().isStreetSection() ? 1 : 0;
        cnt += getXmin().getZmax().isStreetSection() ? 1 : 0;
        cnt += getXmax().getZmin().isStreetSection() ? 1 : 0;
        cnt += getXmax().getZmax().isStreetSection() ? 1 : 0;
        return cnt >= 3;
    }

    private Direction getStairDirection() {
        if (!stairsCalculated) {
            stairsCalculated = true;
            if (streetType != StreetType.PARK && !hasBuilding && isCity) {
                if (cityLevel == getXmin().cityLevel - 1 && !getXmin().hasBuilding && getXmin().isCity) {
                    stairDirection = Direction.XMIN;
                } else if (cityLevel == getXmax().cityLevel - 1 && !getXmax().hasBuilding && getXmax().isCity) {
                    stairDirection = Direction.XMAX;
                } else if (cityLevel == getZmin().cityLevel - 1 && !getZmin().hasBuilding && getZmin().isCity) {
                    stairDirection = Direction.ZMIN;
                } else if (cityLevel == getZmax().cityLevel - 1 && !getZmax().hasBuilding && getZmax().isCity) {
                    stairDirection = Direction.ZMAX;
                } else {
                    stairDirection = null;
                }
            } else {
                stairDirection = null;
            }
        }
        return stairDirection;
    }

    // This returns the actual stair direction. It keeps track if there are stair chunks around
    // it those have higher stair priority
    public Direction getActualStairDirection() {
        if (!actualStairsCalculated) {
            actualStairsCalculated = true;
            actualStairDirection = getStairDirection();
            if (actualStairDirection != null) {
                for (int cx = -1; cx <= 1; cx++) {
                    for (int cz = -1; cz <= 1; cz++) {
                        if (cx != 0 || cz != 0) {
                            BuildingInfo adjacent = getBuildingInfo(chunkX + cx, chunkZ + cz, seed, provider);
                            if (adjacent.getStairDirection() != null && adjacent.stairPriority > stairPriority) {
                                actualStairDirection = null;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return actualStairDirection;
    }


    public BuildingPart hasBridge(LostCityChunkGenerator provider, Orientation orientation) {
        switch (orientation) {
            case X:
                return hasXBridge(provider);
            case Z:
                return hasZBridge(provider);
        }
        return null;
    }

    // To prevent adjacent bridges of the same direction we give the bridges at even chunk Z coordinates higher priority
    public BuildingPart hasXBridge(LostCityChunkGenerator provider) {
        if (xBridgeTypeCalculated) {
            return xBridgeType;
        }
        xBridgeTypeCalculated = true;
        xBridgeType = null;

        if (!xBridge) {
            return null;
        }
        if (!isSuitableForBridge(provider, this)) {
            return null;
        }
        if (chunkZ % 2 != 0 && (getZmin().hasXBridge(provider) != null || getZmax().hasXBridge(provider) != null)) {
            return null;
        }
        BuildingPart bt = bridgeType;
        BuildingInfo i = getXmin();
        while ((!i.isCity) && i.xBridge && isSuitableForBridge(provider, i)) {
            if (chunkZ % 2 != 0 && (i.getZmin().hasXBridge(provider) != null || i.getZmax().hasXBridge(provider) != null)) {
                return null;
            }
            bt = i.bridgeType;
            i = i.getXmin();
        }
        if ((!i.isCity) || i.hasBuilding || i.cityLevel > 0) {  // @todo support bridges at higher levels?
            return null;
        }
        i = getXmax();
        while ((!i.isCity) && i.xBridge && isSuitableForBridge(provider, i)) {
            if (chunkZ % 2 != 0 && (i.getZmin().hasXBridge(provider) != null || i.getZmax().hasXBridge(provider) != null)) {
                return null;
            }
            i = i.getXmax();
        }
        if ((!i.isCity) || i.hasBuilding || i.cityLevel > 0) {
            return null;
        }
        xBridgeType = bt;
        return bt;
    }

    // To prevent adjacent bridges of the same direction we give the bridges at even chunk X coordinates higher priority
    public BuildingPart hasZBridge(LostCityChunkGenerator provider) {
        if (zBridgeTypeCalculated) {
            return zBridgeType;
        }
        zBridgeTypeCalculated = true;
        zBridgeType = null;

        if (!zBridge) {
            return null;
        }
        if (!isSuitableForBridge(provider, this)) {
            return null;
        }
        if (hasXBridge(provider) != null) {
            return null;
        }

        if (chunkX % 2 != 0 && (getXmin().hasZBridge(provider) != null || getXmax().hasZBridge(provider) != null)) {
            return null;
        }

        BuildingPart bt = bridgeType;
        BuildingInfo i = getZmin();
        while ((!i.isCity) && i.zBridge && isSuitableForBridge(provider, i)) {
            if (i.hasXBridge(provider) != null) {
                return null;
            }
            if (chunkX % 2 != 0 && (i.getXmin().hasZBridge(provider) != null || i.getXmax().hasZBridge(provider) != null)) {
                return null;
            }

            bt = i.bridgeType;
            i = i.getZmin();
        }
        if ((!i.isCity) || i.hasBuilding || i.cityLevel > 0) {
            return null;
        }
        i = getZmax();
        while ((!i.isCity) && i.zBridge && isSuitableForBridge(provider, i)) {
            if (i.hasXBridge(provider) != null) {
                return null;
            }
            if (chunkX % 2 != 0 && (i.getXmin().hasZBridge(provider) != null || i.getXmax().hasZBridge(provider) != null)) {
                return null;
            }
            i = i.getZmax();
        }
        if ((!i.isCity) || i.hasBuilding || i.cityLevel > 0) {
            return null;
        }
        zBridgeType = bt;
        return bt;
    }

    private boolean isSuitableForBridge(LostCityChunkGenerator provider, BuildingInfo i) {
        return i.cityLevel < cityLevel || LostCitiesTerrainGenerator.isWaterBiome(provider, i.chunkX, i.chunkZ);
    }


    public boolean hasXCorridor() {
        if (!xRailCorridor) {
            return false;
        }
        BuildingInfo i = getXmin();
        while (i.canRailGoThrough() && i.xRailCorridor) {
            i = i.getXmin();
        }
        if ((!i.hasBuilding) || i.floorsBelowGround == 0) {
            return false;
        }
        i = getXmax();
        while (i.canRailGoThrough() && i.xRailCorridor) {
            i = i.getXmax();
        }
        return !((!i.hasBuilding) || i.floorsBelowGround == 0);
    }

    public boolean hasZCorridor() {
        if (!zRailCorridor) {
            return false;
        }
        BuildingInfo i = getZmin();
        while (i.canRailGoThrough() && i.zRailCorridor) {
            i = i.getZmin();
        }
        if ((!i.hasBuilding) || i.floorsBelowGround == 0) {
            return false;
        }
        i = getZmax();
        while (i.canRailGoThrough() && i.zRailCorridor) {
            i = i.getZmax();
        }
        return !((!i.hasBuilding) || i.floorsBelowGround == 0);
    }

    // Return true if it is possible for a rail section to go through here
    public boolean canRailGoThrough() {
        if (!isCity) {
            // There is no city here so no passing possible
            return false;
        }
        if (!hasBuilding) {
            // There is no building here but we have a city so we can pass
            return true;
        }
        // Otherwise we can only pass if this building has no floors below ground
        return floorsBelowGround == 0;
    }

    // Return true if it is possible for a water corridor to go through here
    public boolean canWaterCorridorGoThrough() {
        if (!isCity) {
            // There is no city here so no passing possible
            return false;
        }
        if (!hasBuilding) {
            // There is no building here but we have a city so we can pass
            return true;
        }
        // Otherwise we can only pass if this building has at most one floor below ground
        return floorsBelowGround <= 1;
    }

    // Return true if the road from a neighbouring chunk can extend into this chunk
    public boolean doesRoadExtendTo() {
        boolean b = isCity && !hasBuilding;
        if (b) {
            return !isElevatedParkSection();
        }
        return false;
    }

    // Return true if there can be a road connection between the two given chunks
    public static boolean hasRoadConnection(BuildingInfo i1, BuildingInfo i2) {
        if (!i1.doesRoadExtendTo()) {
            return false;
        }
        if (!i2.doesRoadExtendTo()) {
            return false;
        }
        if (Math.abs(i1.cityLevel - i2.cityLevel) <= 0 /* @todo temporary, should be <= 1 */) {
            // We allow a road difference of 1 maximum
            return true;
        }
        return false;
    }

    public static Random getBuildingRandom(int chunkX, int chunkZ, long seed) {
        Random rand = new QualityRandom(seed + chunkZ * 341873128712L + chunkX * 132897987541L);
        rand.nextFloat();
        rand.nextFloat();
        return rand;
    }

    // Convert a local building level to a global one (where cityLevel == 0)
    public int localToGlobal(int l) {
        return l + cityLevel;
    }

    public int globalToLocal(int l) {
        return l - cityLevel;
    }

    public boolean hasConnectionAt(int level, Orientation orientation) {
        switch (orientation) {
            case X:
                return hasConnectionAtX(level);
            case Z:
                return hasConnectionAtZ(level);
        }
        throw new IllegalStateException("Cannot happen!");
    }

    // This checks if there can be a connection at minX
    public boolean hasConnectionAtX(int level) {
        if (!isCity) {
            return false;
        }
        if (building2x2Section == 1 || building2x2Section == 3) {
            return false;
        }
        if (level < 0 || level >= connectionAtX.length) {
            return false;
        }
        return connectionAtX[level];
    }

    // This checks if there can be a connection at minZ
    public boolean hasConnectionAtZ(int level) {
        if (!isCity) {
            return false;
        }
        if (building2x2Section == 2 || building2x2Section == 3) {
            return false;
        }
        if (level < 0 || level >= connectionAtZ.length) {
            return false;
        }
        return connectionAtZ[level];
    }

    enum StreetType {
        NORMAL,
        FULL,
        PARK
    }
}
