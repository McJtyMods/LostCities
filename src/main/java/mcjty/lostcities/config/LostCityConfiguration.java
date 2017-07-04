package mcjty.lostcities.config;

import mcjty.lostcities.LostCities;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.util.*;

public class LostCityConfiguration {

    /*
    {"coordinateScale":375.0,"heightScale":6000.0,"lowerLimitScale":1025.0,"upperLimitScale":200.0,"depthNoiseScaleX":200.0,"depthNoiseScaleZ":200.0,"depthNoiseScaleExponent":0.5,"mainNoiseScaleX":80.0,"mainNoiseScaleY":160.0,"mainNoiseScaleZ":80.0,"baseSize":1.0,"stretchY":5.65,"biomeDepthWeight":1.0,"biomeDepthOffset":0.0,"biomeScaleWeight":1.0,"biomeScaleOffset":0.0,"seaLevel":47,"useCaves":true,"useDungeons":true,"dungeonChance":100,"useStrongholds":true,"useVillages":true,"useMineShafts":true,"useTemples":true,"useMonuments":true,"useRavines":true,"useWaterLakes":true,"waterLakeChance":1,"useLavaLakes":true,"lavaLakeChance":80,"useLavaOceans":false,"fixedBiome":-1,"biomeSize":1,"riverSize":5,"dirtSize":18,"dirtCount":8,"dirtMinHeight":0,"dirtMaxHeight":256,"gravelSize":22,"gravelCount":8,"gravelMinHeight":0,"gravelMaxHeight":256,"graniteSize":22,"graniteCount":8,"graniteMinHeight":95,"graniteMaxHeight":255,"dioriteSize":22,"dioriteCount":8,"dioriteMinHeight":95,"dioriteMaxHeight":255,"andesiteSize":22,"andesiteCount":8,"andesiteMinHeight":95,"andesiteMaxHeight":255,"coalSize":17,"coalCount":20,"coalMinHeight":125,"coalMaxHeight":255,"ironSize":9,"ironCount":23,"ironMinHeight":134,"ironMaxHeight":255,"goldSize":9,"goldCount":4,"goldMinHeight":175,"goldMaxHeight":255,"redstoneSize":8,"redstoneCount":8,"redstoneMinHeight":175,"redstoneMaxHeight":255,"diamondSize":8,"diamondCount":3,"diamondMinHeight":175,"diamondMaxHeight":255,"lapisSize":7,"lapisCount":2,"lapisCenterHeight":150,"lapisSpread":16}
     */
    public static final String CATEGORY_GENERAL = "general";

    public static final String ASSET_COMMENT = "List of asset libraries loaded in the specified order. " +
            "If the path starts with '/' it is going to be loaded directly from the classpath. If the path starts with '$' it is loaded from the config directory";
    public static final String WORLDTYPES_COMMENT = "List of other worldtypes (id) that this mod will try " +
            "to work with. The worldtype has to support the IChunkPrimerFactory API for this to work";
    public static final String PROFILES_COMMENT = "List of all supported profiles (used for world creation). Warning! Make sure there is always a 'default' profile!";

    public static final String[] DEFAULT_PROFILES = new String[]{"default", "nodamage", "rarecities", "onlycities", "tallbuildings", "safe", "ancient", "wasteland", "chisel", "atlantis", "realistic"};

    public static String[] ASSETS = new String[] {
            "/assets/lostcities/citydata/palette.json",
            "/assets/lostcities/citydata/palette_desert.json",
            "/assets/lostcities/citydata/palette_chisel.json",
            "/assets/lostcities/citydata/palette_chisel_desert.json",
            "/assets/lostcities/citydata/highwayparts.json",
            "/assets/lostcities/citydata/railparts.json",
            "/assets/lostcities/citydata/buildingparts.json",
            "/assets/lostcities/citydata/library.json",
            "$lostcities/userassets.json"
    };

    public static String[] ADAPTING_WORLDTYPES = new String[] {};

    public static int VERSION = 4;

    public static final Map<String, LostCityProfile> profiles = new HashMap<>();
    public static final Map<String, LostCityProfile> standardProfiles = new HashMap<>();

    public static String DIMENSION_PROFILE = "default";
    public static int DIMENSION_ID = 111;
    public static boolean DIMENSION_BOP = true;

    public static String[] init(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_GENERAL, "General settings");

        int oldVersion = 0;
        if (cfg.hasKey(CATEGORY_GENERAL, "version")) {
            oldVersion = cfg.getInt("version", CATEGORY_GENERAL, VERSION, 0, 10000, "Config version. Do not modify this manually!");
        }

        Property versionProperty = new Property("version", Integer.toString(VERSION), Property.Type.INTEGER);
        versionProperty.setComment("Config version. Do not modify this manually!");
        cfg.getCategory(CATEGORY_GENERAL).put("version", versionProperty);

        initStandardProfiles();

        String[] profileList;

        if (oldVersion != VERSION) {
            LostCities.logger.info("Upgrading Lost Cities config from " + oldVersion + " to " + VERSION + "!");
            String[] configuredAssets = cfg.getStringList("assets", CATEGORY_GENERAL, ASSETS, ASSET_COMMENT);
            List<String> mergedAssets = new ArrayList<>();
            Collections.addAll(mergedAssets, ASSETS);
            for (String asset : configuredAssets) {
                if (!mergedAssets.contains(asset)) {
                    mergedAssets.add(asset);
                }
            }
            cfg.getCategory(CATEGORY_GENERAL).remove("assets");
            ASSETS = cfg.getStringList("assets", CATEGORY_GENERAL, mergedAssets.toArray(new String[mergedAssets.size()]), ASSET_COMMENT);


            String[] defaultValues = DEFAULT_PROFILES;
            profileList = cfg.getStringList("profiles", CATEGORY_GENERAL,
                    defaultValues, PROFILES_COMMENT);
            List<String> mergedProfiles = new ArrayList<>();
            Collections.addAll(mergedProfiles, defaultValues);
            for (String profile : profileList) {
                if (!mergedProfiles.contains(profile)) {
                    mergedProfiles.add(profile);
                }
            }
            cfg.getCategory(CATEGORY_GENERAL).remove("profiles");
            profileList = cfg.getStringList("profiles", CATEGORY_GENERAL,
                    mergedProfiles.toArray(new String[mergedProfiles.size()]), PROFILES_COMMENT);
        } else {
            ASSETS = cfg.getStringList("assets", CATEGORY_GENERAL, ASSETS, ASSET_COMMENT);

            profileList = cfg.getStringList("profiles", CATEGORY_GENERAL,
                    DEFAULT_PROFILES, PROFILES_COMMENT);

        }

        ADAPTING_WORLDTYPES = cfg.getStringList("adaptingWorldTypes", CATEGORY_GENERAL, ADAPTING_WORLDTYPES, WORLDTYPES_COMMENT);

        DIMENSION_PROFILE = cfg.getString("dimensionProfile", CATEGORY_GENERAL, DIMENSION_PROFILE, "The 'profile' to use for generation of the Lost City dimension");
        DIMENSION_ID = cfg.getInt("dimensionId", CATEGORY_GENERAL, DIMENSION_ID, -10000, 10000, "The 'ID' of the Lost City Dimension. Set to -1 if you don't want this dimension");
        DIMENSION_BOP = cfg.getBoolean("dimensionBoP", CATEGORY_GENERAL, DIMENSION_BOP, "If true and if Biomes O Plenty is present the dimension will use BoP biomes");

        return profileList;
    }

    private static void initStandardProfiles() {
        LostCityProfile profile = new LostCityProfile("default");
        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("nodamage");
        profile.setDescription("Like default but no explosion damage");
        profile.EXPLOSION_CHANCE = 0;
        profile.MINI_EXPLOSION_CHANCE = 0;
        profile.RUINS = false;
        profile.RUBBLELAYER = false;
        profile.PREVENT_LAKES_RAVINES_IN_CITIES = true;
        profile.MAX_CAVE_HEIGHT = 64;
        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("rarecities");
        profile.setDescription("Cities are rare");
        profile.CITY_CHANCE = 0.002f;
        profile.RUINS = false;
        profile.HIGHWAY_REQUIRES_TWO_CITIES = false;
        profile.RAILWAYS_CAN_END = true;
        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("onlycities");
        profile.setDescription("The entire world is a city");
        profile.CITY_CHANCE = 0.2f;
        profile.CITY_MAXRADIUS = 256;
        profile.CITY_BIOME_FACTORS = new String[] { "river=.5", "frozen_river=.5", "ocean=.7", "frozen_ocean=.7", "deep_ocean=.6" };
        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("tallbuildings");
        profile.setDescription("Very tall buildings (performance heavy)");
        profile.BUILDING_MINFLOORS = 4;
        profile.BUILDING_MINFLOORS_CHANCE = 8;
        profile.BUILDING_MAXFLOORS_CHANCE = 15;
        profile.BUILDING_MAXFLOORS = 20;
        profile.DEBRIS_TO_NEARBYCHUNK_FACTOR = 175;
        profile.DESTROY_LONE_BLOCKS_FACTOR = 0.08f;
        profile.DESTROY_OR_MOVE_CHANCE = 0.2f;
        profile.EXPLOSION_CHANCE = 0.008f;
        profile.EXPLOSION_MAXHEIGHT = 256;
        profile.EXPLOSION_MAXRADIUS = 60;
        profile.EXPLOSION_MINHEIGHT = 130;
        profile.MINI_EXPLOSION_CHANCE = 0.09f;
        profile.MINI_EXPLOSION_MAXHEIGHT = 256;
        profile.MINI_EXPLOSION_MAXRADIUS = 14;
        profile.MINI_EXPLOSION_MINRADIUS = 3;
        profile.RUIN_CHANCE = 0.01f;
        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("safe");
        profile.setDescription("Safe mode: no spawners, lighting but no loot");
        profile.GENERATE_SPAWNERS = false;
        profile.GENERATE_LIGHTING = true;
        profile.GENERATE_LOOT = false;
        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("ancient");
        profile.setDescription("Ancient jungle city, vines and leafs, ruined buildings");
        profile.THICKNESS_OF_RANDOM_LEAFBLOCKS = 6;
        profile.CHANCE_OF_RANDOM_LEAFBLOCKS = 0.05f;
        profile.VINE_CHANCE = 0.1f;
        profile.EXPLOSION_CHANCE = 0;
        profile.MINI_EXPLOSION_CHANCE = 0;
//        profile.MINI_EXPLOSION_MAXRADIUS = 10;
        profile.RUBBLELAYER = true;
        profile.RUBBLE_DIRT_SCALE = 2.0f;
        profile.RUBBLE_LEAVE_SCALE = 2.0f;
        profile.RUINS = true;
        profile.RUIN_CHANCE = 0.9f;
        profile.RUIN_MINLEVEL_PERCENT = 0.0f;
        profile.RUIN_MAXLEVEL_PERCENT = 0.8f;
        profile.ALLOWED_BIOME_FACTORS = new String[] { "jungle=1", "jungle_hills=1", "jungle_edge=2", "ocean=8", "beaches=20", "river=5" };
        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("wasteland");
        profile.setDescription("Wasteland, no water, bare land (can use BOP biomes)");
        profile.WATERLEVEL_OFFSET = 70;
        profile.GENERATE_LAKES = false;
        profile.GENERATE_OCEANMONUMENTS = false;
        profile.VINE_CHANCE = 0.003f;
        profile.CHANCE_OF_RANDOM_LEAFBLOCKS = 0.01f;
        profile.RUBBLELAYER = true;
        profile.RUBBLE_DIRT_SCALE = 2.0f;
        profile.RUBBLE_LEAVE_SCALE = 0.0f;
        profile.RUINS = true;
        profile.RUIN_CHANCE = 0.5f;
        profile.RUIN_MINLEVEL_PERCENT = 0.5f;
        profile.RUIN_MAXLEVEL_PERCENT = 0.9f;
        profile.AVOID_FOLIAGE = true;
        profile.ALLOWED_BIOME_FACTORS = new String[] { "desert=1", "desert_hills=1", "stone_beach=1", "dead_forest=1", "gravel_beach=1", "outback=1", "volcanic_island=1", "wasteland=.3" };
        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("atlantis");
        profile.setDescription("Drowned cities, raised waterlevel");
        profile.WATERLEVEL_OFFSET = -20;
        profile.RUIN_CHANCE = 0.1f;
        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("chisel");
        profile.setDescription("Use Chisel blocks (only if chisel is available!)");
        profile.setWorldStyle("chisel");
        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("realistic");
        profile.setDescription("Realistic worldgen (similar to Quark's)");
        profile.GENERATOR_OPTIONS = "{\"coordinateScale\":175.0,\"heightScale\":75.0,\"lowerLimitScale\":512.0,\"upperLimitScale\":512.0,\"depthNoiseScaleX\":200.0,\"depthNoiseScaleZ\":200.0,\"depthNoiseScaleExponent\":0.5,\"mainNoiseScaleX\":165.0,\"mainNoiseScaleY\":106.61267,\"mainNoiseScaleZ\":165.0,\"baseSize\":8.267606,\"stretchY\":13.387607,\"biomeDepthWeight\":1.2,\"biomeDepthOffset\":0.2,\"biomeScaleWeight\":3.4084506,\"biomeScaleOffset\":0.0,\"seaLevel\":63,\"useCaves\":true,\"useDungeons\":true,\"dungeonChance\":7,\"useStrongholds\":true,\"useVillages\":true,\"useMineShafts\":true,\"useTemples\":true,\"useMonuments\":true,\"useRavines\":true,\"useWaterLakes\":true,\"waterLakeChance\":49,\"useLavaLakes\":true,\"lavaLakeChance\":80,\"useLavaOceans\":false,\"fixedBiome\":-1,\"biomeSize\":4,\"riverSize\":5,\"dirtSize\":33,\"dirtCount\":10,\"dirtMinHeight\":0,\"dirtMaxHeight\":256,\"gravelSize\":33,\"gravelCount\":8,\"gravelMinHeight\":0,\"gravelMaxHeight\":256,\"graniteSize\":33,\"graniteCount\":10,\"graniteMinHeight\":0,\"graniteMaxHeight\":80,\"dioriteSize\":33,\"dioriteCount\":10,\"dioriteMinHeight\":0,\"dioriteMaxHeight\":80,\"andesiteSize\":33,\"andesiteCount\":10,\"andesiteMinHeight\":0,\"andesiteMaxHeight\":80,\"coalSize\":17,\"coalCount\":20,\"coalMinHeight\":0,\"coalMaxHeight\":128,\"ironSize\":9,\"ironCount\":20,\"ironMinHeight\":0,\"ironMaxHeight\":64,\"goldSize\":9,\"goldCount\":2,\"goldMinHeight\":0,\"goldMaxHeight\":32,\"redstoneSize\":8,\"redstoneCount\":8,\"redstoneMinHeight\":0,\"redstoneMaxHeight\":16,\"diamondSize\":8,\"diamondCount\":1,\"diamondMinHeight\":0,\"diamondMaxHeight\":16,\"lapisSize\":7,\"lapisCount\":1,\"lapisCenterHeight\":16,\"lapisSpread\":16}";
        standardProfiles.put(profile.getName(), profile);

//        profile = new LostCityProfile("islands");
//        profile.setDescription("Floating islands!");
//        profile.GENERATOR_OPTIONS = "{\"coordinateScale\":375.0,\"heightScale\":6000.0,\"lowerLimitScale\":1025.0,\"upperLimitScale\":200.0,\"depthNoiseScaleX\":200.0,\"depthNoiseScaleZ\":200.0,\"depthNoiseScaleExponent\":0.5,\"mainNoiseScaleX\":80.0,\"mainNoiseScaleY\":160.0,\"mainNoiseScaleZ\":80.0,\"baseSize\":1.0,\"stretchY\":5.65,\"biomeDepthWeight\":1.0,\"biomeDepthOffset\":0.0,\"biomeScaleWeight\":1.0,\"biomeScaleOffset\":0.0,\"seaLevel\":47,\"useCaves\":true,\"useDungeons\":true,\"dungeonChance\":100,\"useStrongholds\":true,\"useVillages\":true,\"useMineShafts\":true,\"useTemples\":true,\"useMonuments\":true,\"useRavines\":true,\"useWaterLakes\":true,\"waterLakeChance\":1,\"useLavaLakes\":true,\"lavaLakeChance\":80,\"useLavaOceans\":false,\"fixedBiome\":-1,\"biomeSize\":1,\"riverSize\":5,\"dirtSize\":18,\"dirtCount\":8,\"dirtMinHeight\":0,\"dirtMaxHeight\":256,\"gravelSize\":22,\"gravelCount\":8,\"gravelMinHeight\":0,\"gravelMaxHeight\":256,\"graniteSize\":22,\"graniteCount\":8,\"graniteMinHeight\":95,\"graniteMaxHeight\":255,\"dioriteSize\":22,\"dioriteCount\":8,\"dioriteMinHeight\":95,\"dioriteMaxHeight\":255,\"andesiteSize\":22,\"andesiteCount\":8,\"andesiteMinHeight\":95,\"andesiteMaxHeight\":255,\"coalSize\":17,\"coalCount\":20,\"coalMinHeight\":125,\"coalMaxHeight\":255,\"ironSize\":9,\"ironCount\":23,\"ironMinHeight\":134,\"ironMaxHeight\":255,\"goldSize\":9,\"goldCount\":4,\"goldMinHeight\":175,\"goldMaxHeight\":255,\"redstoneSize\":8,\"redstoneCount\":8,\"redstoneMinHeight\":175,\"redstoneMaxHeight\":255,\"diamondSize\":8,\"diamondCount\":3,\"diamondMinHeight\":175,\"diamondMaxHeight\":255,\"lapisSize\":7,\"lapisCount\":2,\"lapisCenterHeight\":150,\"lapisSpread\":16}";
//        standardProfiles.put(profile.getName(), profile);

    }

}
