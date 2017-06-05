package mcjty.lostcities.config;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.config.Configuration;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class LostCityConfiguration {

    public static final String CATEGORY_LOSTCITY = "lostcity";
    public static final String CATEGORY_STRUCTURES = "structures";
    public static final String CATEGORY_EXPLOSIONS = "explosions";
    public static final String CATEGORY_CITIES = "cities";



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
    public static int EXPLOSION_MINHEIGHT = 78;
    public static int EXPLOSION_MAXHEIGHT = 128;

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

    public static Float CITY_DEFAULT_BIOME_FACTOR = 1.0f;
    public static String[] CITY_BIOME_FACTORS = new String[] { "river=0", "frozen_river=0", "ocean=.7", "frozen_ocean=.7", "deep_ocean=.4" };
    public static Map<String, Float> biomeFactorMap = null;

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

    public static int BEDROCK_LAYER = 1;

    public static boolean GENERATE_VILLAGES = true;
    public static boolean GENERATE_CAVES = true;
    public static boolean GENERATE_RAVINES = true;
    public static boolean GENERATE_MINESHAFTS = true;
    public static boolean GENERATE_STRONGHOLDS = true;
    public static boolean GENERATE_SCATTERED = true;
    public static boolean GENERATE_OCEANMONUMENTS = true;

    public static boolean PREVENT_VILLAGES_IN_CITIES = true;

    public static Map<String, Float> getBiomeFactorMap() {
        if (biomeFactorMap == null) {
            biomeFactorMap = new HashMap<>();
            for (String s : CITY_BIOME_FACTORS) {
                String[] split = StringUtils.split(s, '=');
                float f = Float.parseFloat(split[1]);
                String biomeId = split[0];
                Biome biome = Biome.REGISTRY.getObject(new ResourceLocation(biomeId));
                if (biome != null) {
                    biomeFactorMap.put(Biome.REGISTRY.getNameForObject(biome).toString(), f);
                }
            }
        }
        return biomeFactorMap;
    }


    public static void init(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_LOSTCITY, "Settings related to the Lost City");
        cfg.addCustomCategoryComment(CATEGORY_STRUCTURES, "Settings related to structure generation");
        cfg.addCustomCategoryComment(CATEGORY_EXPLOSIONS, "Settings related to explosions and damage");
        cfg.addCustomCategoryComment(CATEGORY_CITIES, "Settings related to city generation");

        initLostcity(cfg);
        initExplosions(cfg);
        initStructures(cfg);
        initCities(cfg);
    }

    private static void initLostcity(Configuration cfg) {
        STYLE_CHANCE_CRACKED = cfg.getFloat("styleChanceCracked", CATEGORY_LOSTCITY, STYLE_CHANCE_CRACKED, 0.0f, 1.0f, "The chance that a brick will be cracked");
        STYLE_CHANCE_MOSSY = cfg.getFloat("styleChanceMossy", CATEGORY_LOSTCITY, STYLE_CHANCE_MOSSY, 0.0f, 1.0f, "The chance that a brick will be mossy");

        VINE_CHANCE = cfg.getFloat("vineChance", CATEGORY_LOSTCITY, VINE_CHANCE, 0.0f, 1.0f, "The chance that a block on the outside of a building will be covered with a vine");

        GROUNDLEVEL = cfg.getInt("groundLevel", CATEGORY_LOSTCITY, GROUNDLEVEL, 2, 256, "Ground level");
        WATERLEVEL_OFFSET = cfg.getInt("waterLevelOffset", CATEGORY_LOSTCITY, WATERLEVEL_OFFSET, 1, 30, "How much lower the water level is compared to the ground level (63)");

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

        BEDROCK_LAYER = cfg.getInt("bedrockLayer", CATEGORY_LOSTCITY, BEDROCK_LAYER, 0, 10,
                "The height of the bedrock layer that is generated at the bottom of some world types. Set to 0 to disable this and get default bedrock generation");
    }

    private static void initCities(Configuration cfg) {
        CITY_CHANCE = cfg.getFloat("cityChance", CATEGORY_CITIES, CITY_CHANCE, 0.0f, 1.0f, "The chance this chunk will be the center of a city");
        CITY_MINRADIUS = cfg.getInt("cityMinRadius", CATEGORY_CITIES, CITY_MINRADIUS, 1, 1000, "The minimum radius of a city");
        CITY_MAXRADIUS = cfg.getInt("cityMaxRadius", CATEGORY_CITIES, CITY_MAXRADIUS, 1, 2000, "The maximum radius of a city");
        CITY_THRESSHOLD = cfg.getFloat("cityThresshold", CATEGORY_CITIES, CITY_THRESSHOLD, 0.0f, 1.0f, "The center and radius of a city define a sphere. " +
                "This thresshold indicates from which point a city is considered a city. " +
                "This is important for calculating where cities are based on overlapping city circles (where the city thressholds are added)");
        CITY_BIOME_FACTORS = cfg.getStringList("cityBiomeFactors", CATEGORY_CITIES, CITY_BIOME_FACTORS, "List of biomes with a factor to affect the city factor in that biome. Using the value 0 you can disable city generation in biomes");
        CITY_DEFAULT_BIOME_FACTOR = cfg.getFloat("cityBiomeFactorDefault", CATEGORY_CITIES, CITY_DEFAULT_BIOME_FACTOR, 0.0f, 1.0f, "The default biome factor which is used if your biome is not specified in 'cityBiomeFactors'");
    }

    private static void initExplosions(Configuration cfg) {
        DEBRIS_TO_NEARBYCHUNK_FACTOR = cfg.getInt("debrisToNearbyChunkFactor", CATEGORY_EXPLOSIONS, DEBRIS_TO_NEARBYCHUNK_FACTOR, 1, 10000, "A factor that determines how much debris will overflow from nearby damaged chunks. Bigger numbers mean less debris");

        DESTROY_LONE_BLOCKS_FACTOR = cfg.getFloat("destroyLoneBlocksFactor", CATEGORY_EXPLOSIONS, DESTROY_LONE_BLOCKS_FACTOR, 0.0f, 1.0f, "When a section of blocks in in an explosion the generator will count the number of " +
                "blocks that are connected. The number of connections divided by the total number of blocks in a connected section is compared with this number. " +
                "If it is smaller then the section of blocks is destroyed or moved down with gravity");
        DESTROY_OR_MOVE_CHANCE = cfg.getFloat("destroyOrMoveChance", CATEGORY_EXPLOSIONS, DESTROY_OR_MOVE_CHANCE, 0.0f, 1.0f, "When a section of blocks is to be moved or destroyed " +
                "this chance gives the chance of removal (as opposed to moving with gravity)");
        DESTROY_SMALL_SECTIONS_SIZE = cfg.getInt("destroySmallSectionsSize", CATEGORY_EXPLOSIONS, DESTROY_SMALL_SECTIONS_SIZE, 1, 5000, "A section of blocks that is about to be moved or destroyed " +
                "is always destroyed if it is smaller then this size");

        EXPLOSION_CHANCE = cfg.getFloat("explosionChance", CATEGORY_EXPLOSIONS, EXPLOSION_CHANCE, 0.0f, 1.0f, "The chance that a chunk will contain an explosion");
        EXPLOSION_MINRADIUS = cfg.getInt("explosionMinRadius", CATEGORY_EXPLOSIONS, EXPLOSION_MINRADIUS, 1, 1000, "The minimum radius of an explosion");
        EXPLOSION_MAXRADIUS = cfg.getInt("explosionMaxRadius", CATEGORY_EXPLOSIONS, EXPLOSION_MAXRADIUS, 1, 3000, "The maximum radius of an explosion");
        EXPLOSION_MINHEIGHT = cfg.getInt("explosionMinHeight", CATEGORY_EXPLOSIONS, EXPLOSION_MINHEIGHT, 1, 256, "The minimum height of an explosion");
        EXPLOSION_MAXHEIGHT = cfg.getInt("explosionMaxHeight", CATEGORY_EXPLOSIONS, EXPLOSION_MAXHEIGHT, 1, 256, "The maximum height of an explosion");

        MINI_EXPLOSION_CHANCE = cfg.getFloat("miniExplosionChance", CATEGORY_EXPLOSIONS, MINI_EXPLOSION_CHANCE, 0.0f, 1.0f, "The chance that a chunk will contain a mini explosion");
        MINI_EXPLOSION_MINRADIUS = cfg.getInt("miniExplosionMinRadius", CATEGORY_EXPLOSIONS, MINI_EXPLOSION_MINRADIUS, 1, 1000, "The minimum radius of a mini explosion");
        MINI_EXPLOSION_MAXRADIUS = cfg.getInt("miniExplosionMaxRadius", CATEGORY_EXPLOSIONS, MINI_EXPLOSION_MAXRADIUS, 1, 3000, "The maximum radius of a mini explosion");
        MINI_EXPLOSION_MINHEIGHT = cfg.getInt("miniExplosionMinHeight", CATEGORY_EXPLOSIONS, MINI_EXPLOSION_MINHEIGHT, 1, 256, "The minimum height of a mini explosion");
        MINI_EXPLOSION_MAXHEIGHT = cfg.getInt("miniExplosionMaxHeight", CATEGORY_EXPLOSIONS, MINI_EXPLOSION_MAXHEIGHT, 1, 256, "The maximum height of a mini explosion");
    }

    private static void initStructures(Configuration cfg) {
        GENERATE_OCEANMONUMENTS = cfg.get(CATEGORY_STRUCTURES, "generateOceanMonuments", GENERATE_OCEANMONUMENTS, "Generate ocean monuments").getBoolean();
        GENERATE_SCATTERED = cfg.get(CATEGORY_STRUCTURES, "generateScattered", GENERATE_SCATTERED, "Generate scattered features (swamphunts, desert temples, ...)").getBoolean();
        GENERATE_STRONGHOLDS = cfg.get(CATEGORY_STRUCTURES, "generateStrongholds", GENERATE_STRONGHOLDS, "Generate strongholds").getBoolean();
        GENERATE_VILLAGES = cfg.get(CATEGORY_STRUCTURES, "generateVillages", GENERATE_VILLAGES, "Generate villages").getBoolean();
        GENERATE_CAVES = cfg.get(CATEGORY_STRUCTURES, "generateCaves", GENERATE_CAVES, "Generate caves").getBoolean();
        GENERATE_RAVINES = cfg.get(CATEGORY_STRUCTURES, "generateRavines", GENERATE_RAVINES, "Generate ravines").getBoolean();
        GENERATE_MINESHAFTS = cfg.get(CATEGORY_STRUCTURES, "generateMineshafts", GENERATE_MINESHAFTS, "Generate mineshafts").getBoolean();

        PREVENT_VILLAGES_IN_CITIES = cfg.get(CATEGORY_STRUCTURES, "preventVillagesInCities", PREVENT_VILLAGES_IN_CITIES, "If true then an attempt will be made to prevent villages in cities. " +
                "Note that enabling this option will likely require a low city " +
                "density in order to actually get a reasonable chance for villages.").getBoolean();
    }
}
