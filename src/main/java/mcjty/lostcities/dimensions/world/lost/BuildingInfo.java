package mcjty.lostcities.dimensions.world.lost;

import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.dimensions.world.LostCityChunkGenerator;
import mcjty.lostcities.dimensions.world.lost.cityassets.*;
import mcjty.lostcities.dimensions.world.lost.data.BridgeData;
import mcjty.lostcities.dimensions.world.lost.data.FountainData;
import mcjty.lostcities.dimensions.world.lost.data.ParkData;
import mcjty.lostcities.varia.QualityRandom;
import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
    public final int fountainType;
    public final int parkType;
    public final int bridgeType;
    public final StreetType streetType;
    private final int floors;
    public final int floorsBelowGround;
    public final BuildingPart[] floorTypes;
    public final boolean[] connectionAtX;
    public final boolean[] connectionAtZ;
    public final int glassType;
    public final int glassColor;
    public final int buildingStyle;

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
    private Style style = null;
    private CityStyle cityStyle = null;
    private CompiledPalette compiledPalette = null;


    // BuildingInfo cache
    private static Map<Pair<Integer, Integer>, BuildingInfo> buildingInfoMap = new HashMap<>();

    public CompiledPalette getCompiledPalette() {
        if (compiledPalette == null) {
            compiledPalette = new CompiledPalette(getStyle(), LostCitiesTerrainGenerator.getPalette());
        }
        return compiledPalette;
    }

    public DamageArea getDamageArea() {
        if (damageArea == null) {
            damageArea = new DamageArea(seed, chunkX, chunkZ, provider);
        }
        return damageArea;
    }

    public CityStyle getCityStyle() {
        if (cityStyle == null) {
            cityStyle = City.getCityStyle(seed, chunkX, chunkZ, provider);
        }
        return cityStyle;
    }

    private void createStyle(Random rand) {
        if (!isCity) {
            style = new Style(
                    AssetRegistries.STYLES.get("street"),
                    AssetRegistries.STYLES.get("outside"),
                    AssetRegistries.STYLES.get("glass"));
        } else {
            style = getCityStyle().getRandomStyle(provider, rand);
            switch (glassType) {
                case 0:
                    style.register("glass_or_brick", style.get("glass"));
                    break;
                case 1:
                    style.register("glass_or_brick", style.get("street"), true);
                    break;
                case 2:
                    style.register("glass_or_brick", style.get("street2"), true);
                    break;
                case 3:
                    style.register("glass_or_brick", style.get("quartz"), true);
                    break;
                default:
                    style.register("glass_or_brick", style.get("glass"));
                    break;
            }
        }
    }

    public Style getStyle() {
        return style;
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
        return hasBuilding ? (LostCityConfiguration.GROUNDLEVEL + floors * 6) : LostCityConfiguration.GROUNDLEVEL;
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
        if (buildingInfoMap.containsKey(Pair.of(chunkX, chunkZ))) {
            return buildingInfoMap.get(Pair.of(chunkX, chunkZ)).isCity;
        } else {
            float cityFactor = City.getCityFactor(seed, chunkX, chunkZ, provider);
            return cityFactor > LostCityConfiguration.CITY_THRESSHOLD;
        }
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
            return rand.nextFloat() < LostCityConfiguration.BUILDING2X2_CHANCE;
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
        float cityFactor = City.getCityFactor(seed, chunkX, chunkZ, provider);
        isCity = cityFactor > LostCityConfiguration.CITY_THRESSHOLD;

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
        hasBuilding = building2x2Section >= 0 || (isCity && (chunkX != 0 || chunkZ != 0) && bc < LostCityConfiguration.BUILDING_CHANCE);

        // In a 2x2 building we copy all information from the top-left chunk
        if (building2x2Section >= 1) {
            BuildingInfo topleft = calculateTopLeft();
            multiBuilding = topleft.multiBuilding;
            if (multiBuilding != null) {
                switch(building2x2Section) {
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
            streetType = topleft.streetType;
            fountainType = topleft.fountainType;
            parkType = topleft.parkType;
            floors = topleft.floors;
            floorsBelowGround = topleft.floorsBelowGround;
            glassType = topleft.glassType;
            glassColor = topleft.glassColor;
            buildingStyle = topleft.buildingStyle;
            doorBlock = topleft.doorBlock;
            bridgeType = topleft.bridgeType;
            createStyle(rand);
        } else {
            if (building2x2Section == 0) {
                multiBuilding = AssetRegistries.MULTI_BUILDINGS.get(getCityStyle().getRandomMultiBuilding(provider, rand));
                buildingType = AssetRegistries.BUILDINGS.get(multiBuilding.get(0, 0));
            } else {
                multiBuilding = null;
                buildingType = AssetRegistries.BUILDINGS.get(getCityStyle().getRandomBuilding(provider, rand));
            }
            if (rand.nextDouble() < .2f) {
                streetType = StreetType.values()[rand.nextInt(StreetType.values().length)];
            } else {
                streetType = StreetType.NORMAL;
            }
            if (rand.nextFloat() < LostCityConfiguration.FOUNTAIN_CHANCE) {
                fountainType = rand.nextInt(FountainData.FOUNTAINS.length);
            } else {
                fountainType = -1;
            }
            parkType = rand.nextInt(ParkData.PARKS.length);
            int f = LostCityConfiguration.BUILDING_MINFLOORS + rand.nextInt((int) (LostCityConfiguration.BUILDING_MINFLOORS_CHANCE + (cityFactor + .1f) * (LostCityConfiguration.BUILDING_MAXFLOORS_CHANCE - LostCityConfiguration.BUILDING_MINFLOORS_CHANCE)));
            if (f > LostCityConfiguration.BUILDING_MAXFLOORS) {
                f = LostCityConfiguration.BUILDING_MAXFLOORS;
            }
            floors = f + 1;
            floorsBelowGround = LostCityConfiguration.BUILDING_MINCELLARS + (LostCityConfiguration.BUILDING_MAXCELLARS <= 0 ? 0 : rand.nextInt(LostCityConfiguration.BUILDING_MAXCELLARS));
            glassType = rand.nextInt(4);
            glassColor = rand.nextInt(5 + 5);
            buildingStyle = rand.nextInt(4);
            doorBlock = getRandomDoor(rand);
            bridgeType = rand.nextInt(BridgeData.BRIDGES.length);
            createStyle(rand);
        }

        floorTypes = new BuildingPart[floors + floorsBelowGround + 1];
        connectionAtX = new boolean[floors + floorsBelowGround + 1];
        connectionAtZ = new boolean[floors + floorsBelowGround + 1];
        Building building = getBuilding();
        for (int i = 0; i <= floors + floorsBelowGround; i++) {
            String randomPart = building.getRandomPart(rand, new Building.LevelInfo(0 /*todo*/, i - floorsBelowGround, floorsBelowGround, floors));
            floorTypes[i] = AssetRegistries.PARTS.get(randomPart);
            connectionAtX[i] = isCity(chunkX - 1, chunkZ, seed, provider) && (rand.nextFloat() < LostCityConfiguration.BUILDING_DOORWAYCHANCE);
            connectionAtZ[i] = isCity(chunkX, chunkZ - 1, seed, provider) && (rand.nextFloat() < LostCityConfiguration.BUILDING_DOORWAYCHANCE);
        }

        if (hasBuilding && floorsBelowGround > 0) {
            xRailCorridor = false;
            zRailCorridor = false;
        } else {
            xRailCorridor = rand.nextFloat() < LostCityConfiguration.CORRIDOR_CHANCE;
            zRailCorridor = rand.nextFloat() < LostCityConfiguration.CORRIDOR_CHANCE;
        }

        if (isCity) {
            xBridge = false;
            zBridge = false;
        } else {
            xBridge = rand.nextFloat() < LostCityConfiguration.BRIDGE_CHANCE;
            zBridge = rand.nextFloat() < LostCityConfiguration.BRIDGE_CHANCE;
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

    public int hasXBridge(LostCityChunkGenerator provider) {
        if (!xBridge) {
            return -1;
        }
        if (!LostCitiesTerrainGenerator.isWaterBiome(provider, chunkX, chunkZ)) {
            return -1;
        }
        int bt = bridgeType;
        BuildingInfo i = getXmin();
        while ((!i.isCity) && i.xBridge && LostCitiesTerrainGenerator.isWaterBiome(provider, i.chunkX, i.chunkZ)) {
            bt = i.bridgeType;
            i = i.getXmin();
        }
        if ((!i.isCity) || i.hasBuilding) {
            return -1;
        }
        i = getXmax();
        while ((!i.isCity) && i.xBridge && LostCitiesTerrainGenerator.isWaterBiome(provider, i.chunkX, i.chunkZ)) {
            i = i.getXmax();
        }
        if ((!i.isCity) || i.hasBuilding) {
            return -1;
        }
        return bt;
    }

    public int hasZBridge(LostCityChunkGenerator provider) {
        if (!zBridge) {
            return -1;
        }
        if (!LostCitiesTerrainGenerator.isWaterBiome(provider, chunkX, chunkZ)) {
            return -1;
        }
        if (hasXBridge(provider) >= 0) {
            return -1;
        }

        int bt = bridgeType;
        BuildingInfo i = getZmin();
        while ((!i.isCity) && i.zBridge && LostCitiesTerrainGenerator.isWaterBiome(provider, i.chunkX, i.chunkZ)) {
            if (i.hasXBridge(provider) >= 0) {
                return -1;
            }
            bt = i.bridgeType;
            i = i.getZmin();
        }
        if ((!i.isCity) || i.hasBuilding) {
            return -1;
        }
        i = getZmax();
        while ((!i.isCity) && i.zBridge && LostCitiesTerrainGenerator.isWaterBiome(provider, i.chunkX, i.chunkZ)) {
            if (i.hasXBridge(provider) >= 0) {
                return -1;
            }
            i = i.getZmax();
        }
        if ((!i.isCity) || i.hasBuilding) {
            return -1;
        }
        return bt;
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

    public static Random getBuildingRandom(int chunkX, int chunkZ, long seed) {
        Random rand = new QualityRandom(seed + chunkZ * 341873128712L + chunkX * 132897987541L);
        rand.nextFloat();
        rand.nextFloat();
        return rand;
    }

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
