package mcjty.lostcities.dimensions.world.terrain.lost;

import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.dimensions.world.LostCityChunkGenerator;
import mcjty.lostcities.dimensions.world.terrain.lost.data.*;
import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;

import java.util.Random;

public class BuildingInfo {
    public final int chunkX;
    public final int chunkZ;
    public final long seed;

    public final boolean isCity;
    public final boolean hasBuilding;
    public final int building2x2Section;    // -1 for not, 0 for top left, 1 for top right, 2 for bottom left, 3 for bottom right

    public final int buildingType;
    public final int fountainType;
    public final int parkType;
    public final int bridgeType;
    public final StreetType streetType;
    public final int floors;
    public final int floorsBelowGround;
    public final int[] floorTypes;
    public final boolean[] connectionAtX;
    public final boolean[] connectionAtZ;
    public final int topType;
    public final int glassType;
    public final int glassColor;
    public final int buildingStyle;
    public final boolean isLibrary;     // If true this is a library (only if it is also a 2x2 building)
    public final boolean isDataCenter;  // If true this is a data center (only if it is also a 2x2 building)

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

    public DamageArea getDamageArea() {
        if (damageArea == null) {
            damageArea = new DamageArea(seed, chunkX, chunkZ);
        }
        return damageArea;
    }

    public Style getStyle() {
        if (style != null) {
            return style;
        }
        style = new Style();

        if (!isCity) {
            style.bricks = Blocks.STONEBRICK.getDefaultState();
            style.bricks_variant = Blocks.DOUBLE_STONE_SLAB.getDefaultState();
            style.bricks_cracked = Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CRACKED);
            style.bricks_mossy = Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.MOSSY);
            return style;
        }

        style.street = Blocks.DOUBLE_STONE_SLAB.getDefaultState();
        style.street2 = Blocks.BRICK_BLOCK.getDefaultState();

        switch (glassColor) {
            case 0:
                style.glass = Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.WHITE);
                style.glass_full = style.glass;
                break;
            case 1:
                style.glass = Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.GRAY);
                style.glass_full = style.glass;
                break;
            case 2:
                style.glass = Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.LIGHT_BLUE);
                style.glass_full = style.glass;
                break;
            case 3:
                style.glass = Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.BLUE);
                style.glass_full = style.glass;
                break;
            case 4:
                style.glass = Blocks.STAINED_GLASS_PANE.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.WHITE);
                style.glass_full = Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.WHITE);
                break;
            case 5:
                style.glass = Blocks.STAINED_GLASS_PANE.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.GRAY);
                style.glass_full = Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.GRAY);
                break;
            case 6:
                style.glass = Blocks.STAINED_GLASS_PANE.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.LIGHT_BLUE);
                style.glass_full = Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.LIGHT_BLUE);
                break;
            case 7:
                style.glass = Blocks.STAINED_GLASS_PANE.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.BLUE);
                style.glass_full = Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.BLUE);
                break;
            case 8:
                style.glass = Blocks.GLASS_PANE.getDefaultState();
                style.glass_full = Blocks.GLASS.getDefaultState();
                break;
            default:
                style.glass = Blocks.GLASS.getDefaultState();
                style.glass_full = style.glass;
                break;
        }

        style.quartz = Blocks.QUARTZ_BLOCK.getDefaultState();

        switch (buildingStyle) {
            case 0:
                style.bricks = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.CYAN);
                style.bricks_variant = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.BLACK);
                style.bricks_cracked = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.CYAN);
                style.bricks_mossy = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.CYAN);
                style.bricks_monster = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.CYAN);
                break;
            case 1:
                style.bricks = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY);
                style.bricks_variant = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.BLACK);
                style.bricks_cracked = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY);
                style.bricks_mossy = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY);
                style.bricks_monster = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY);
                break;
            case 2:
                style.bricks = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.SILVER);
                style.bricks_variant = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.BROWN);
                style.bricks_cracked = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.SILVER);
                style.bricks_mossy = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.SILVER);
                style.bricks_monster = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.SILVER);
                break;
            default:
                style.bricks = Blocks.STONEBRICK.getDefaultState();
                style.bricks_variant = Blocks.DOUBLE_STONE_SLAB.getDefaultState();
                style.bricks_cracked = Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CRACKED);
                style.bricks_mossy = Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.MOSSY);
                style.bricks_monster = Blocks.MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, BlockSilverfish.EnumType.STONEBRICK);
                break;
        }
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
            xmin = new BuildingInfo(chunkX - 1, chunkZ, seed);
        }
        return xmin;
    }

    public BuildingInfo getXmax() {
        if (xmax == null) {
            xmax = new BuildingInfo(chunkX + 1, chunkZ, seed);
        }
        return xmax;
    }

    public BuildingInfo getZmin() {
        if (zmin == null) {
            zmin = new BuildingInfo(chunkX, chunkZ - 1, seed);
        }
        return zmin;
    }

    public BuildingInfo getZmax() {
        if (zmax == null) {
            zmax = new BuildingInfo(chunkX, chunkZ + 1, seed);
        }
        return zmax;
    }

    public int getMaxHeight() {
        return hasBuilding ? (69 + floors * 6) : 63;
    }

    public Level[] getFloorData() {
        if (isLibrary) {
            switch (building2x2Section) {
                case 0:
                    return LibraryData.LIBRARY00;
                case 1:
                    return LibraryData.LIBRARY10;
                case 2:
                    return LibraryData.LIBRARY01;
                case 3:
                    return LibraryData.LIBRARY11;
            }
        }
        if (isDataCenter) {
            switch (building2x2Section) {
                case 0:
                    return DataCenterData.CENTER00;
                case 1:
                    return DataCenterData.CENTER10;
                case 2:
                    return DataCenterData.CENTER01;
                case 3:
                    return DataCenterData.CENTER11;
            }
        }
        switch (buildingType) {
            case 0:
                return FloorsData.FLOORS;
            case 1:
                return FloorsData.FLOORS2;
            case 2:
                return FloorsData.FLOORS3;
        }
        return FloorsData.FLOORS;
    }

    public Level getTopData(int floortype) {
        if (isLibrary) {
            switch (building2x2Section) {
                case 0:
                    return LibraryData.TOPS_LIBRARY00[floortype];
                case 1:
                case 2:
                case 3:
                    return LibraryData.TOPS_LIBRARY[floortype];
                default:
                    return RoofTopsData.TOPS[floortype];
            }
        } else if (isDataCenter) {
            switch (building2x2Section) {
                case 0:
                    return DataCenterData.TOPS_CENTER00[floortype];
                case 1:
                case 2:
                case 3:
                    return DataCenterData.TOPS_CENTER[floortype];
                default:
                    return RoofTopsData.TOPS[floortype];
            }
        } else {
            return RoofTopsData.TOPS[floortype];
        }
    }


    // @todo not ideal
    public int getGenInfoIndex() {
        if (isLibrary) {
            return building2x2Section + 10;
        }
        if (isDataCenter) {
            return building2x2Section + 20;
        }
        return buildingType;
    }

    private static boolean isCity(int chunkX, int chunkZ, long seed) {
        float cityFactor = City.getCityFactor(seed, chunkX, chunkZ);
        return cityFactor > LostCityConfiguration.CITY_THRESSHOLD;
    }

    private static boolean isCandidateForTopLeftOf2x2Building(int chunkX, int chunkZ, long seed) {
        if (chunkX == 0 && chunkZ == 0) {
            return false;
        }
        float cityFactor = City.getCityFactor(seed, chunkX, chunkZ);
        if (cityFactor > LostCityConfiguration.CITY_THRESSHOLD) {
            Random rand = getBuildingRandom(chunkX, chunkZ, seed);
            return rand.nextFloat() < LostCityConfiguration.BUILDING2X2_CHANCE;
        } else {
            return false;
        }
    }

    private static boolean isTopLeftOf2x2Building(int chunkX, int chunkZ, long seed) {
        if (isCandidateForTopLeftOf2x2Building(chunkX, chunkZ, seed) &&
                !isCandidateForTopLeftOf2x2Building(chunkX - 1, chunkZ, seed) &&
                !isCandidateForTopLeftOf2x2Building(chunkX - 1, chunkZ - 1, seed) &&
                !isCandidateForTopLeftOf2x2Building(chunkX, chunkZ - 1, seed) &&

                !isCandidateForTopLeftOf2x2Building(chunkX + 1, chunkZ - 1, seed) &&
                !isCandidateForTopLeftOf2x2Building(chunkX + 1, chunkZ, seed) &&
                !isCandidateForTopLeftOf2x2Building(chunkX + 1, chunkZ + 1, seed) &&
                !isCandidateForTopLeftOf2x2Building(chunkX, chunkZ + 1, seed) &&
                !isCandidateForTopLeftOf2x2Building(chunkX - 1, chunkZ + 1, seed)
                ) {
            return isCity(chunkX + 1, chunkZ, seed) && isCity(chunkX + 1, chunkZ + 1, seed) && isCity(chunkX, chunkZ + 1, seed);
        } else {
            return false;
        }
    }

    public BuildingInfo(int chunkX, int chunkZ, long seed) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.seed = seed;
        float cityFactor = City.getCityFactor(seed, chunkX, chunkZ);
        isCity = cityFactor > LostCityConfiguration.CITY_THRESSHOLD;

        if (isTopLeftOf2x2Building(chunkX, chunkZ, seed)) {
            building2x2Section = 0;
        } else if (isTopLeftOf2x2Building(chunkX - 1, chunkZ, seed)) {
            building2x2Section = 1;
        } else if (isTopLeftOf2x2Building(chunkX, chunkZ - 1, seed)) {
            building2x2Section = 2;
        } else if (isTopLeftOf2x2Building(chunkX - 1, chunkZ - 1, seed)) {
            building2x2Section = 3;
        } else {
            building2x2Section = -1;
        }

        Random rand = getBuildingRandom(chunkX, chunkZ, seed);
        hasBuilding = building2x2Section >= 0 || (isCity && (chunkX != 0 || chunkZ != 0) && rand.nextFloat() < LostCityConfiguration.BUILDING_CHANCE);

        // In a 2x2 building we copy all information from the top-left chunk
        if (building2x2Section >= 1) {
            BuildingInfo topleft;
            switch (building2x2Section) {
                case 1:
                    topleft = getXmin();
                    break;
                case 2:
                    topleft = getZmin();
                    break;
                case 3:
                    topleft = getXmin().getZmin();
                    break;
                default:
                    throw new RuntimeException("What!");
            }
            isLibrary = topleft.isLibrary;
            isDataCenter = topleft.isDataCenter;
            buildingType = topleft.buildingType;
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
        } else {
            int bt = rand.nextInt(3);
            if (bt == 2) {
                // Make some types more rare
                if (rand.nextFloat() < .5f) {
                    bt = rand.nextInt(3);
                }
            }
            buildingType = bt;
            if (building2x2Section == 0) {
                isLibrary = rand.nextFloat() < LostCityConfiguration.LIBRARY_CHANCE;
            } else {
                isLibrary = false;
            }
            if (building2x2Section == 0 && !isLibrary) {
                isDataCenter = rand.nextFloat() < LostCityConfiguration.LIBRARY_CHANCE;
            } else {
                isDataCenter = false;
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
            floors = f;
            floorsBelowGround = LostCityConfiguration.BUILDING_MINCELLARS + (LostCityConfiguration.BUILDING_MAXCELLARS <= 0 ? 0 : rand.nextInt(LostCityConfiguration.BUILDING_MAXCELLARS));
            glassType = rand.nextInt(4);
            glassColor = rand.nextInt(5 + 5);
            buildingStyle = rand.nextInt(4);
            doorBlock = getRandomDoor(rand);
            bridgeType = rand.nextInt(BridgeData.BRIDGES.length);
        }

        if (isLibrary) {
            switch (building2x2Section) {
                case 0:
                    topType = rand.nextInt(LibraryData.TOPS_LIBRARY00.length);
                    break;
                case 1:
                case 2:
                case 3:
                    topType = rand.nextInt(LibraryData.TOPS_LIBRARY.length);
                    break;
                default:
                    topType = rand.nextInt(RoofTopsData.TOPS.length);
            }
        } else if (isDataCenter) {
            switch (building2x2Section) {
                case 0:
                    topType = rand.nextInt(DataCenterData.TOPS_CENTER00.length);
                    break;
                case 1:
                case 2:
                case 3:
                    topType = rand.nextInt(DataCenterData.TOPS_CENTER.length);
                    break;
                default:
                    topType = rand.nextInt(RoofTopsData.TOPS.length);
            }

        } else {
            topType = rand.nextInt(RoofTopsData.TOPS.length);
        }

        floorTypes = new int[floors + floorsBelowGround + 2];
        connectionAtX = new boolean[floors + floorsBelowGround + 2];
        connectionAtZ = new boolean[floors + floorsBelowGround + 2];
        for (int i = 0; i <= floors + floorsBelowGround + 1; i++) {
            floorTypes[i] = rand.nextInt(getFloorData().length);
            connectionAtX[i] = isCity(chunkX - 1, chunkZ, seed) ? (rand.nextFloat() < LostCityConfiguration.BUILDING_DOORWAYCHANCE) : false;
            connectionAtZ[i] = isCity(chunkX, chunkZ - 1, seed) ? (rand.nextFloat() < LostCityConfiguration.BUILDING_DOORWAYCHANCE) : false;
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
        if ((!i.hasBuilding) || i.floorsBelowGround == 0) {
            return false;
        }
        return true;
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
        if ((!i.hasBuilding) || i.floorsBelowGround == 0) {
            return false;
        }
        return true;
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
        Random rand = new Random(seed + chunkZ * 341873128712L + chunkX * 132897987541L);
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
