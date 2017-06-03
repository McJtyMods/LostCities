package mcjty.lostcities.config;

import net.minecraftforge.common.config.Configuration;

public class LostCityConfiguration {

    public static final String CATEGORY_LOSTCITY = "lostcity";
    public static int DEBRIS_TO_NEARBYCHUNK_FACTOR = 200;

    public static float VINE_CHANCE = 0.009f;

    public static int GROUNDLEVEL = 71;
    public static int WATERLEVEL_OFFSET = 8;

    public static float DESTROY_LONE_BLOCKS_FACTOR = .05f;
    public static float DESTROY_OR_MOVE_CHANCE = .4f;
    public static int DESTROY_SMALL_SECTIONS_SIZE = 50;

    public static float EXPLOSION_CHANCE = .005f;
    public static int EXPLOSION_MINRADIUS = 17;
    public static int EXPLOSION_MAXRADIUS = 80;
    public static int EXPLOSION_MINHEIGHT = 70;
    public static int EXPLOSION_MAXHEIGHT = 120;

    public static float MINI_EXPLOSION_CHANCE = .07f;
    public static int MINI_EXPLOSION_MINRADIUS = 5;
    public static int MINI_EXPLOSION_MAXRADIUS = 15;
    public static int MINI_EXPLOSION_MINHEIGHT = 60;
    public static int MINI_EXPLOSION_MAXHEIGHT = 100;

    public static float STYLE_CHANCE_CRACKED = 0.06f;
    public static float STYLE_CHANCE_MOSSY = 0.05f;

    public static float CITY_CHANCE = .02f;
    public static int CITY_MINRADIUS = 50;
    public static int CITY_MAXRADIUS = 128;
    public static float CITY_THRESSHOLD = .2f;

    public static float BUILDING_CHANCE = .3f;
    public static int BUILDING_MINFLOORS = 0;
    public static int BUILDING_MAXFLOORS = 9;
    public static int BUILDING_MINFLOORS_CHANCE = 4;
    public static int BUILDING_MAXFLOORS_CHANCE = 6;
    public static int BUILDING_MINCELLARS = 0;
    public static int BUILDING_MAXCELLARS = 4;
    public static float BUILDING_DOORWAYCHANCE = .6f;
    public static float LIBRARY_CHANCE = .1f;
    public static float DATACENTER_CHANCE = .1f;

    public static float CORRIDOR_CHANCE = .7f;
    public static float BRIDGE_CHANCE = .7f;
    public static float FOUNTAIN_CHANCE = .05f;
    public static float BUILDING2X2_CHANCE = .03f;


    public static void init(Configuration cfg) {
        STYLE_CHANCE_CRACKED = cfg.getFloat("styleChanceCracked", CATEGORY_LOSTCITY, STYLE_CHANCE_CRACKED, 0.0f, 1.0f, "The chance that a brick will be cracked");
        STYLE_CHANCE_MOSSY = cfg.getFloat("styleChanceMossy", CATEGORY_LOSTCITY, STYLE_CHANCE_MOSSY, 0.0f, 1.0f, "The chance that a brick will be mossy");

        VINE_CHANCE = cfg.getFloat("vineChance", CATEGORY_LOSTCITY, VINE_CHANCE, 0.0f, 1.0f, "The chance that a block on the outside of a building will be covered with a vine");

        GROUNDLEVEL = cfg.getInt("groundLevel", CATEGORY_LOSTCITY, GROUNDLEVEL, 2, 256, "Ground level");
        WATERLEVEL_OFFSET = cfg.getInt("waterLevelOffset", CATEGORY_LOSTCITY, WATERLEVEL_OFFSET, 1, 30, "How much lower the water level is compared to the ground level (63)");

        DEBRIS_TO_NEARBYCHUNK_FACTOR = cfg.getInt("debrisToNearbyChunkFactor", CATEGORY_LOSTCITY, DEBRIS_TO_NEARBYCHUNK_FACTOR, 1, 10000, "A factor that determines how much debris will overflow from nearby damaged chunks. Bigger numbers mean less debris");

        DESTROY_LONE_BLOCKS_FACTOR = cfg.getFloat("destroyLoneBlocksFactor", CATEGORY_LOSTCITY, DESTROY_LONE_BLOCKS_FACTOR, 0.0f, 1.0f, "When a section of blocks in in an explosion the generator will count the number of " +
                "blocks that are connected. The number of connections divided by the total number of blocks in a connected section is compared with this number. " +
                "If it is smaller then the section of blocks is destroyed or moved down with gravity");
        DESTROY_OR_MOVE_CHANCE = cfg.getFloat("destroyOrMoveChance", CATEGORY_LOSTCITY, DESTROY_OR_MOVE_CHANCE, 0.0f, 1.0f, "When a section of blocks is to be moved or destroyed " +
                "this chance gives the chance of remval (as opposed to moving with gravity)");
        DESTROY_SMALL_SECTIONS_SIZE = cfg.getInt("destroySmallSectionsSize", CATEGORY_LOSTCITY, DESTROY_SMALL_SECTIONS_SIZE, 1, 5000, "A section of blocks that is about to be moved or destroyed " +
                "is always destroyed if it is smaller then this size");

        EXPLOSION_CHANCE = cfg.getFloat("explosionChance", CATEGORY_LOSTCITY, EXPLOSION_CHANCE, 0.0f, 1.0f, "The chance that a chunk will contain an explosion");
        EXPLOSION_MINRADIUS = cfg.getInt("explosionMinRadius", CATEGORY_LOSTCITY, EXPLOSION_MINRADIUS, 1, 1000, "The minimum radius of an explosion");
        EXPLOSION_MAXRADIUS = cfg.getInt("explosionMaxRadius", CATEGORY_LOSTCITY, EXPLOSION_MAXRADIUS, 1, 3000, "The maximum radius of an explosion");
        EXPLOSION_MINHEIGHT = cfg.getInt("explosionMinHeight", CATEGORY_LOSTCITY, EXPLOSION_MINHEIGHT, 1, 256, "The minimum height of an explosion");
        EXPLOSION_MAXHEIGHT = cfg.getInt("explosionMaxHeight", CATEGORY_LOSTCITY, EXPLOSION_MAXHEIGHT, 1, 256, "The maximum height of an explosion");

        MINI_EXPLOSION_CHANCE = cfg.getFloat("miniExplosionChance", CATEGORY_LOSTCITY, MINI_EXPLOSION_CHANCE, 0.0f, 1.0f, "The chance that a chunk will contain a mini explosion");
        MINI_EXPLOSION_MINRADIUS = cfg.getInt("miniExplosionMinRadius", CATEGORY_LOSTCITY, MINI_EXPLOSION_MINRADIUS, 1, 1000, "The minimum radius of a mini explosion");
        MINI_EXPLOSION_MAXRADIUS = cfg.getInt("miniExplosionMaxRadius", CATEGORY_LOSTCITY, MINI_EXPLOSION_MAXRADIUS, 1, 3000, "The maximum radius of a mini explosion");
        MINI_EXPLOSION_MINHEIGHT = cfg.getInt("miniExplosionMinHeight", CATEGORY_LOSTCITY, MINI_EXPLOSION_MINHEIGHT, 1, 256, "The minimum height of a mini explosion");
        MINI_EXPLOSION_MAXHEIGHT = cfg.getInt("miniExplosionMaxHeight", CATEGORY_LOSTCITY, MINI_EXPLOSION_MAXHEIGHT, 1, 256, "The maximum height of a mini explosion");

        CITY_CHANCE = cfg.getFloat("cityChance", CATEGORY_LOSTCITY, CITY_CHANCE, 0.0f, 1.0f, "The chance this chunk will be the center of a city");
        CITY_MINRADIUS = cfg.getInt("cityMinRadius", CATEGORY_LOSTCITY, CITY_MINRADIUS, 1, 1000, "The minimum radius of a city");
        CITY_MAXRADIUS = cfg.getInt("cityMaxRadius", CATEGORY_LOSTCITY, CITY_MAXRADIUS, 1, 2000, "The maximum radius of a city");
        CITY_THRESSHOLD = cfg.getFloat("cityThresshold", CATEGORY_LOSTCITY, CITY_THRESSHOLD, 0.0f, 1.0f, "The center and radius of a city define a sphere. " +
                "This thresshold indicates from which point a city is considered a city. " +
                "This is important for calculating where cities are based on overlapping city circles (where the city thressholds are added)");

        BUILDING_CHANCE = cfg.getFloat("buildingChance", CATEGORY_LOSTCITY, BUILDING_CHANCE, 0.0f, 1.0f, "The chance that a chunk in a city will have a building. Otherwise it will be a street");
        BUILDING_MINFLOORS = cfg.getInt("buildingMinFloors", CATEGORY_LOSTCITY, BUILDING_MINFLOORS, 0, 30, "The minimum number of floors (above ground) for a building (0 means the first floor only)");
        BUILDING_MAXFLOORS = cfg.getInt("buildingMaxFloors", CATEGORY_LOSTCITY, BUILDING_MAXFLOORS, 0, 30, "A cap for the amount of floors a city can have (above ground)");
        BUILDING_MINFLOORS_CHANCE = cfg.getInt("buildingMinFloorsChance", CATEGORY_LOSTCITY, BUILDING_MINFLOORS_CHANCE, 1, 30, "The amount of floors of a building is equal to: " +
                "MINFLOORS + random(MINFLOORS_CHANCE + (cityFactor + .1f) * (MAXFLOORS_CHANCE - MINFLOORS_CHANCE))");
        BUILDING_MAXFLOORS_CHANCE = cfg.getInt("buildingMaxFloorsChance", CATEGORY_LOSTCITY, BUILDING_MAXFLOORS_CHANCE, 1, 30, "The amount of floors of a building is equal to: " +
                "MINFLOORS + random(MINFLOORS_CHANCE + (cityFactor + .1f) * (MAXFLOORS_CHANCE - MINFLOORS_CHANCE))");

        BUILDING_MINCELLARS = cfg.getInt("buildingMinCellars", CATEGORY_LOSTCITY, BUILDING_MINCELLARS, 0, 7, "The minimum number of cellars (below ground). 0 means no cellar");
        BUILDING_MAXCELLARS = cfg.getInt("buildingMaxCellars", CATEGORY_LOSTCITY, BUILDING_MAXCELLARS, 0, 7, "The maximum number of cellars (below ground). 0 means no cellar");
        BUILDING_DOORWAYCHANCE = cfg.getFloat("buildingDoorwayChance", CATEGORY_LOSTCITY, BUILDING_DOORWAYCHANCE, 0.0f, 1.0f, "The chance that a doorway will be generated at a side of a building (on any level). Only when possible");
        LIBRARY_CHANCE = cfg.getFloat("libraryChance", CATEGORY_LOSTCITY, LIBRARY_CHANCE, 0.0f, 1.0f, "The chance that a 2x2 building will be a library");
        DATACENTER_CHANCE = cfg.getFloat("dataCenterChance", CATEGORY_LOSTCITY, DATACENTER_CHANCE, 0.0f, 1.0f, "The chance that a 2x2 building will be a data center");

        BUILDING2X2_CHANCE = cfg.getFloat("building2x2Chance", CATEGORY_LOSTCITY, BUILDING2X2_CHANCE, 0.0f, 1.0f, "The chance that a chunk can possibly be the top-left chunk of 2x2 building. " +
                "There actually being a 2x2 building also depends on the condition of those other chunks");
        CORRIDOR_CHANCE = cfg.getFloat("corridorChance", CATEGORY_LOSTCITY, CORRIDOR_CHANCE, 0.0f, 1.0f, "The chance that a chunk can possibly contain a corridor. " +
                "There actually being a corridor also depends on the presence of adjacent corridors");
        BRIDGE_CHANCE = cfg.getFloat("bridgeChance", CATEGORY_LOSTCITY, BRIDGE_CHANCE, 0.0f, 1.0f, "The chance that a chunk can possibly contain a bridge. " +
                "There actually being a bridge also depends on the presence of adjacent bridges and other conditions");

        FOUNTAIN_CHANCE = cfg.getFloat("fountainChance", CATEGORY_LOSTCITY, FOUNTAIN_CHANCE, 0.0f, 1.0f, "The chance that a street section contains a fountain");
    }
}
