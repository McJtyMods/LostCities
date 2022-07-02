package mcjty.lostcities.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import mcjty.lostcities.LostCities;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class LostCityConfiguration {

    /*
    {"coordinateScale":375.0,"heightScale":6000.0,"lowerLimitScale":1025.0,"upperLimitScale":200.0,"depthNoiseScaleX":200.0,"depthNoiseScaleZ":200.0,"depthNoiseScaleExponent":0.5,"mainNoiseScaleX":80.0,"mainNoiseScaleY":160.0,"mainNoiseScaleZ":80.0,"baseSize":1.0,"stretchY":5.65,"biomeDepthWeight":1.0,"biomeDepthOffset":0.0,"biomeScaleWeight":1.0,"biomeScaleOffset":0.0,"seaLevel":47,"useCaves":true,"useDungeons":true,"dungeonChance":100,"useStrongholds":true,"useVillages":true,"useMineShafts":true,"useTemples":true,"useMonuments":true,"useRavines":true,"useWaterLakes":true,"waterLakeChance":1,"useLavaLakes":true,"lavaLakeChance":80,"useLavaOceans":false,"fixedBiome":-1,"biomeSize":1,"riverSize":5,"dirtSize":18,"dirtCount":8,"dirtMinHeight":0,"dirtMaxHeight":256,"gravelSize":22,"gravelCount":8,"gravelMinHeight":0,"gravelMaxHeight":256,"graniteSize":22,"graniteCount":8,"graniteMinHeight":95,"graniteMaxHeight":255,"dioriteSize":22,"dioriteCount":8,"dioriteMinHeight":95,"dioriteMaxHeight":255,"andesiteSize":22,"andesiteCount":8,"andesiteMinHeight":95,"andesiteMaxHeight":255,"coalSize":17,"coalCount":20,"coalMinHeight":125,"coalMaxHeight":255,"ironSize":9,"ironCount":23,"ironMinHeight":134,"ironMaxHeight":255,"goldSize":9,"goldCount":4,"goldMinHeight":175,"goldMaxHeight":255,"redstoneSize":8,"redstoneCount":8,"redstoneMinHeight":175,"redstoneMaxHeight":255,"diamondSize":8,"diamondCount":3,"diamondMinHeight":175,"diamondMaxHeight":255,"lapisSize":7,"lapisCount":2,"lapisCenterHeight":150,"lapisSpread":16}
     */
    public static final String CATEGORY_GENERAL = "general";

    public static final String ADDITIONAL_DIMENSIONS_COMMENT = "List of additional Lost City dimensions. Format '<id>:<profile>'";
    public static final String LIGHTING_UPDATE_COMMENT = "List of blocks for which a lighting update is needed";
    public static final String ASSET_COMMENT = "List of asset libraries loaded in the specified order. " +
            "If the path starts with '/' it is going to be loaded directly from the classpath. If the path starts with '$' it is loaded from the config directory";
    public static final String WORLDTYPES_COMMENT = "List of other worldtypes (id) that this mod will try " +
            "to work with. The worldtype has to support the IChunkPrimerFactory API for this to work";
    public static final String PROFILES_COMMENT = "List of all supported profiles (used for world creation). Warning! Make sure there is always a 'default' profile!";
    public static final String PRIVATE_PROFILES_COMMENT = "List of privatep profiles that cannot be selected by the player but are only used as a child profile of another one";

    public static final String[] DEFAULT_PROFILES = new String[]{"default", "cavern", "nodamage", "rarecities", "floating", "space", "waterbubbles", "biosphere", "onlycities", "tallbuildings", "safe", "ancient", "wasteland", "chisel", "atlantis", "realistic"};
    public static final String[] PRIVATE_PROFILES = new String[]{"bio_wasteland", "water_empty"};

    public static final String[] BLOCKS_REQUIRING_LIGHTING_UPDATES = new String[] {
            "minecraft:glowstone",
            "minecraft:redstone_torch",
            "minecraft:lit_pumpkin",
            "minecraft:magma"
    };

    public static String[] ADDITIONAL_DIMENSIONS = new String[] {

    };

    public static String[] ADAPTING_WORLDTYPES = new String[] {};

    public static int VERSION = 7;

    public static final Map<String, LostCityProfile> profiles = new HashMap<>();
    public static final Map<String, LostCityProfile> standardProfiles = new HashMap<>();

    public static String DIMENSION_PROFILE = "default";
    public static String DEFAULT_PROFILE = "default";
//    public static RegistryKey<World> DIMENSION_ID;// @todo 1.14 = 111; 1.16
    public static boolean DIMENSION_BOP = true;

    public static final boolean DEBUG = false;

    public static ForgeConfigSpec.ConfigValue<String> SPECIAL_BED_BLOCK;// = "minecraft:diamond_block";




    public static void init(ForgeConfigSpec.Builder SERVER_BUILDER) {
        SERVER_BUILDER.comment("General settings").push(CATEGORY_GENERAL);

        SPECIAL_BED_BLOCK = SERVER_BUILDER
                .comment("Block to put underneath a bed so that it qualifies as a teleporter bed")
                .define("specialBedBlock", "minecraft:diamond_block");

        SERVER_BUILDER.pop();
    }

    private static void initStandardProfiles() {
        LostCityProfile profile;

//        profile = new LostCityProfile("customized", false);
//        profile.setDescription("Customized profile");
//        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("default", true);
        profile.setIconFile("textures/gui/icon_default.png");
        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("cavern", true);
        profile.setDescription("This is a cavern type world (like the nether)");
        profile.setExtraDescription("There are lights in the building but the outside is very dark. Warning! This is pretty heavy on performance!");
        profile.setIconFile("textures/gui/icon_cavern.png");
        profile.LANDSCAPE_TYPE = LandscapeType.CAVERN;
        profile.HORIZON = 128;
        profile.FOG_RED = 0.0f;
        profile.FOG_GREEN = 0.0f;
        profile.FOG_BLUE = 0.0f;
        profile.FOG_DENSITY = 0.02f;
        profile.EXPLOSION_CHANCE = 0;
        profile.MINI_EXPLOSION_CHANCE = 0;
        profile.GENERATE_LIGHTING = true;
        profile.GENERATE_LAKES = false;
        profile.GENERATE_VILLAGES = false;
        profile.RAILWAYS_ENABLED = false;
        profile.GROUNDLEVEL = 40;
        profile.SEALEVEL = 32;
        profile.CITY_LEVEL0_HEIGHT = 40+4;
        profile.CITY_LEVEL1_HEIGHT = 40+12;
        profile.CITY_LEVEL2_HEIGHT = 40+20;
        profile.CITY_LEVEL3_HEIGHT = 40+28;
//        profile.setIconFile("textures/gui/icon_default.png");
        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("nodamage", true);
        profile.setDescription("Like default but no explosion damage");
        profile.setExtraDescription("Ruins and rubble are disabled and ravines are disabled in cities");
        profile.setIconFile("textures/gui/icon_nodamage.png");
        profile.EXPLOSION_CHANCE = 0;
        profile.MINI_EXPLOSION_CHANCE = 0;
        profile.RUIN_CHANCE = 0;
        profile.RUBBLELAYER = false;
        profile.PREVENT_LAKES_RAVINES_IN_CITIES = true;
        profile.MAX_CAVE_HEIGHT = 64;
        standardProfiles.put(profile.getName(), profile);

//        profile = new LostCityProfile("floating", true);
//        profile.setDescription("Cities on floating islands");
//        profile.setExtraDescription("Note! No villages or strongholds in this profile!");
//        profile.setIconFile("textures/gui/icon_floating.png");
//        profile.CITY_CHANCE = 0.03f;
//        profile.LANDSCAPE_TYPE = LandscapeType.FLOATING;
//        profile.HORIZON = 0;
////        profile.WATERLEVEL_OFFSET = 70;
//        profile.BUILDING_MAXCELLARS = 1;
//        profile.RAILWAYS_CAN_END = true;
//        profile.RAILWAYS_ENABLED = false;
//        profile.HIGHWAY_DISTANCE_MASK = 15;
//        profile.GROUNDLEVEL = 50;
//        profile.CITY_LEVEL0_HEIGHT = 50;
//        profile.CITY_LEVEL1_HEIGHT = 56;
//        profile.CITY_LEVEL2_HEIGHT = 62;
//        profile.CITY_LEVEL3_HEIGHT = 68;
//        profile.GENERATE_MANSIONS = false;
//        profile.GENERATE_MINESHAFTS = false;
//        profile.GENERATE_OCEANMONUMENTS = false;
//        profile.GENERATE_SCATTERED = false;
//        profile.GENERATE_VILLAGES = false;
//        profile.GENERATE_STRONGHOLDS = false;
//        profile.AVOID_GENERATED_FOSSILS = true;
//        standardProfiles.put(profile.getName(), profile);

//        profile = new LostCityProfile("space", true);
//        profile.setDescription("Cities in floating glass bubbles");
//        profile.setExtraDescription("Note! No villages or strongholds in this profile!");
//        profile.setIconFile("textures/gui/icon_space.png");
//        profile.LANDSCAPE_TYPE = LandscapeType.SPACE;
//        profile.HORIZON = 0;
////        profile.WATERLEVEL_OFFSET = 70;
//        profile.RAILWAYS_CAN_END = true;
//        profile.RAILWAYS_ENABLED = false;
//        profile.RAILWAY_STATIONS_ENABLED = false;
//        profile.HIGHWAY_DISTANCE_MASK = 0;
//        profile.BRIDGE_SUPPORTS = false;
//        profile.HIGHWAY_SUPPORTS = false;
//        profile.RUBBLELAYER = false;
//        profile.GROUNDLEVEL = 60;
//        profile.EXPLOSION_CHANCE = 0.0001f;
//        profile.MINI_EXPLOSION_CHANCE = 0.001f;
//        profile.CITY_CHANCE = 0.6f;
//        profile.CITY_MAXRADIUS = 90;
//        profile.CITY_THRESHOLD = .05f;
//        profile.CITY_LEVEL0_HEIGHT = 60;
//        profile.CITY_LEVEL1_HEIGHT = 66;
//        profile.CITY_LEVEL2_HEIGHT = 72;
//        profile.CITY_LEVEL3_HEIGHT = 78;
//        profile.GENERATE_MANSIONS = false;
//        profile.GENERATE_MINESHAFTS = false;
//        profile.GENERATE_OCEANMONUMENTS = false;
//        profile.GENERATE_SCATTERED = false;
//        profile.GENERATE_VILLAGES = false;
//        profile.GENERATE_STRONGHOLDS = false;
//        profile.AVOID_GENERATED_FOSSILS = true;
//        profile.BUILDING_CHANCE = .3f;
//        profile.GENERATE_LIGHTING = true;
//        standardProfiles.put(profile.getName(), profile);

//        profile = new LostCityProfile("waterbubbles", true);
//        profile.setDescription("Cities in drowned glass bubbles");
//        profile.setExtraDescription("Note! No villages or strongholds in this profile!");
//        profile.setIconFile("textures/gui/icon_bubbles.png");
//        profile.LANDSCAPE_TYPE = LandscapeType.SPACE;
//        profile.HORIZON = 90;
////        profile.WATERLEVEL_OFFSET = 8;
//        profile.CITYSPHERE_LANDSCAPE_OUTSIDE = true;
//        profile.CITYSPHERE_OUTSIDE_PROFILE = "water_empty";
//        profile.RAILWAYS_CAN_END = true;
//        profile.RAILWAYS_ENABLED = false;
//        profile.RAILWAY_STATIONS_ENABLED = false;
//        profile.HIGHWAY_DISTANCE_MASK = 0;
//        profile.RUBBLELAYER = false;
//        profile.GROUNDLEVEL = 60;
//        profile.EXPLOSION_CHANCE = 0;
//        profile.MINI_EXPLOSION_CHANCE = 0;
//        profile.CITY_CHANCE = 0.6f;
//        profile.CITY_MAXRADIUS = 90;
//        profile.CITY_THRESHOLD = .05f;
//        profile.CITY_LEVEL0_HEIGHT = 60;
//        profile.CITY_LEVEL1_HEIGHT = 66;
//        profile.CITY_LEVEL2_HEIGHT = 72;
//        profile.CITY_LEVEL3_HEIGHT = 78;
//        profile.GENERATE_MANSIONS = false;
//        profile.GENERATE_MINESHAFTS = false;
//        profile.GENERATE_OCEANMONUMENTS = false;
//        profile.GENERATE_SCATTERED = false;
//        profile.GENERATE_VILLAGES = false;
//        profile.GENERATE_STRONGHOLDS = false;
//        profile.BUILDING_CHANCE = .3f;
//        profile.GENERATE_LIGHTING = true;
//        standardProfiles.put(profile.getName(), profile);

//        profile = new LostCityProfile("biosphere", true);
//        profile.setDescription("Jungles in big glass bubbles on a barren landscape");
//        profile.setExtraDescription("This profile works best with Biomes O Plenty");
//        profile.setIconFile("textures/gui/icon_biosphere.png");
//        profile.LANDSCAPE_TYPE = LandscapeType.SPACE;
//        profile.HORIZON = 30;
//        profile.CITYSPHERE_LANDSCAPE_OUTSIDE = true;
//        profile.ALLOWED_BIOME_FACTORS = new String[] { "jungle=1", "jungle_hills=1", "jungle_edge=2" };
//        profile.CITYSPHERE_MONORAIL_CHANCE = 0.0f;
//        profile.CITYSPHERE_OUTSIDE_PROFILE = "bio_wasteland";
//        profile.CITYSPHERE_OUTSIDE_SURFACE_VARIATION = 0.5f;
//        profile.SPAWN_BIOME = "jungle";
//        profile.EXPLOSION_CHANCE = 0.0f;
//        profile.MINI_EXPLOSION_CHANCE = 0.01f;
//        profile.MINI_EXPLOSION_MINHEIGHT = 60;
//        profile.MINI_EXPLOSION_MAXHEIGHT = 75;
//        profile.MINI_EXPLOSION_MINRADIUS = 5;
//        profile.MINI_EXPLOSION_MAXRADIUS = 10;
////        profile.WATERLEVEL_OFFSET = 70;
//        profile.RAILWAYS_CAN_END = true;
//        profile.RAILWAYS_ENABLED = false;
//        profile.RAILWAY_STATIONS_ENABLED = false;
//        profile.HIGHWAY_DISTANCE_MASK = 0;
//        profile.RUIN_CHANCE = 0.7f;
//        profile.RUIN_MINLEVEL_PERCENT = 0.3f;
//        profile.RUIN_MAXLEVEL_PERCENT = 0.8f;
//        profile.RUBBLELAYER = false;
//        profile.GROUNDLEVEL = 60;
//        profile.CITYSPHERE_CHANCE = 0.4f;
////        profile.CITY_CHANCE = 0.3f;       // @EXP
//        profile.CITY_CHANCE = 0.6f;
//
//        profile.CITY_MAXRADIUS = 90;
//        profile.CITY_THRESHOLD = .05f;
//        profile.CITY_LEVEL0_HEIGHT = 60;
//        profile.CITY_LEVEL1_HEIGHT = 66;
//        profile.CITY_LEVEL2_HEIGHT = 72;
//        profile.CITY_LEVEL3_HEIGHT = 78;
//        profile.GENERATE_MANSIONS = false;
//        profile.GENERATE_MINESHAFTS = false;
//        profile.GENERATE_OCEANMONUMENTS = false;
//        profile.GENERATE_SCATTERED = false;
//        profile.GENERATE_VILLAGES = false;
//        profile.GENERATE_STRONGHOLDS = false;
//        profile.BUILDING_CHANCE = .3f;
//        profile.GENERATE_LIGHTING = true;
//        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("rarecities", true);
        profile.setDescription("Cities are rare");
        profile.setIconFile("textures/gui/icon_rarecities.png");
        profile.CITY_CHANCE = 0.001;
        profile.RUIN_CHANCE = 0;
        profile.HIGHWAY_REQUIRES_TWO_CITIES = false;
        profile.RAILWAYS_CAN_END = true;
        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("onlycities", true);
        profile.setDescription("The entire world is a city");
        profile.setIconFile("textures/gui/icon_onlycities.png");
        profile.CITY_CHANCE = 0.2;
        profile.CITY_MAXRADIUS = 256;
        profile.CITY_BIOME_FACTORS = new String[] { "river=.5", "frozen_river=.5", "ocean=.7", "frozen_ocean=.7", "deep_ocean=.6" };
        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("tallbuildings", true);
        profile.setDescription("Very tall buildings (performance heavy)");
        profile.setIconFile("textures/gui/icon_tallbuildings.png");
        profile.BUILDING_MINFLOORS = 4;
        profile.BUILDING_MINFLOORS_CHANCE = 8;
        profile.BUILDING_MAXFLOORS_CHANCE = 15;
        profile.BUILDING_MAXFLOORS = 19;
        profile.DEBRIS_TO_NEARBYCHUNK_FACTOR = 175;
        profile.DESTROY_LONE_BLOCKS_FACTOR = 0.08f;
        profile.DESTROY_OR_MOVE_CHANCE = 0.2f;
        profile.EXPLOSION_CHANCE = 0.006f;
        profile.EXPLOSION_MAXHEIGHT = 256;
        profile.EXPLOSION_MAXRADIUS = 60;
        profile.EXPLOSION_MINHEIGHT = 130;
        profile.MINI_EXPLOSION_CHANCE = 0.09f;
        profile.MINI_EXPLOSION_MAXHEIGHT = 256;
        profile.MINI_EXPLOSION_MAXRADIUS = 14;
        profile.MINI_EXPLOSION_MINRADIUS = 3;
        profile.RUIN_CHANCE = 0.01f;
        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("safe", true);
        profile.setDescription("Safe mode: no spawners, lighting but no loot");
        profile.setIconFile("textures/gui/icon_safe.png");
        profile.GENERATE_SPAWNERS = false;
        profile.GENERATE_LIGHTING = true;
        profile.GENERATE_LOOT = false;
        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("ancient", true);
        profile.setDescription("Ancient jungle city, vines and leafs, ruined buildings");
//        profile.setExtraDescription("Note! This disables many biomes like deserts, plains, extreme hills, ...");
        profile.setIconFile("textures/gui/icon_ancient.png");
        profile.THICKNESS_OF_RANDOM_LEAFBLOCKS = 6;
        profile.CHANCE_OF_RANDOM_LEAFBLOCKS = 0.05f;
        profile.VINE_CHANCE = 0.1f;
        profile.EXPLOSION_CHANCE = 0;
        profile.MINI_EXPLOSION_CHANCE = 0;
//        profile.MINI_EXPLOSION_MAXRADIUS = 10;
        profile.RUBBLELAYER = true;
        profile.RUBBLE_DIRT_SCALE = 2.0f;
        profile.RUBBLE_LEAVE_SCALE = 2.0f;
        profile.RUIN_CHANCE = 0.9f;
        profile.RUIN_MINLEVEL_PERCENT = 0.0f;
        profile.RUIN_MAXLEVEL_PERCENT = 0.9f;
        profile.ALLOWED_BIOME_FACTORS = new String[] { "jungle=1", "jungle_hills=1", "jungle_edge=2", "ocean=8", "beaches=20", "river=5" };
        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("wasteland", true);
        profile.setDescription("Wasteland, no water, bare land");
        profile.setExtraDescription("This profile works best with Biomes O Plenty");
        profile.setIconFile("textures/gui/icon_wasteland.png");
//        profile.WATERLEVEL_OFFSET = 70;
        profile.GENERATE_LAKES = false;
        profile.GENERATE_OCEANMONUMENTS = false;
        profile.VINE_CHANCE = 0.003f;
        profile.CHANCE_OF_RANDOM_LEAFBLOCKS = 0.01f;
        profile.RUBBLELAYER = true;
        profile.RUBBLE_DIRT_SCALE = 2.0f;
        profile.RUBBLE_LEAVE_SCALE = 0.0f;
        profile.RUIN_CHANCE = 0.5f;
        profile.RUIN_MINLEVEL_PERCENT = 0.5f;
        profile.RUIN_MAXLEVEL_PERCENT = 0.9f;
        profile.AVOID_WATER = true;
        profile.AVOID_FOLIAGE = true;
        profile.AVOID_GENERATED_LAKE_WATER = true;
        profile.AVOID_GENERATED_LILYPADS = true;
        profile.AVOID_GENERATED_FLOWERS = true;
        profile.AVOID_GENERATED_REEDS = true;
        profile.ALLOWED_BIOME_FACTORS = new String[] { "desert=1", "desert_hills=1", "stone_beach=1", "dead_forest=1", "gravel_beach=1", "outback=1", "volcanic_island=1", "wasteland=.3" };
        standardProfiles.put(profile.getName(), profile);

//        profile = new LostCityProfile("atlantis", true);
//        profile.setDescription("Drowned cities, raised waterlevel");
//        profile.setIconFile("textures/gui/icon_atlantis.png");
////        profile.WATERLEVEL_OFFSET = -20;
//        profile.RUIN_CHANCE = 0.1f;
//        standardProfiles.put(profile.getName(), profile);
//
//        profile = new LostCityProfile("chisel", true);
//        profile.setDescription("Use Chisel blocks (only if chisel is available!)");
//        profile.setIconFile("textures/gui/icon_chisel.png");
//        profile.setWorldStyle("chisel");
//        standardProfiles.put(profile.getName(), profile);
//
//        profile = new LostCityProfile("realistic", true);
//        profile.setDescription("Realistic worldgen (similar to Quark's)");
//        profile.setIconFile("textures/gui/icon_realistic.png");
//        profile.GENERATOR_OPTIONS = "{\"coordinateScale\":175.0,\"heightScale\":75.0,\"lowerLimitScale\":512.0,\"upperLimitScale\":512.0,\"depthNoiseScaleX\":200.0,\"depthNoiseScaleZ\":200.0,\"depthNoiseScaleExponent\":0.5,\"mainNoiseScaleX\":165.0,\"mainNoiseScaleY\":106.61267,\"mainNoiseScaleZ\":165.0,\"baseSize\":8.267606,\"stretchY\":13.387607,\"biomeDepthWeight\":1.2,\"biomeDepthOffset\":0.2,\"biomeScaleWeight\":3.4084506,\"biomeScaleOffset\":0.0,\"seaLevel\":63,\"useCaves\":true,\"useDungeons\":true,\"dungeonChance\":7,\"useStrongholds\":true,\"useVillages\":true,\"useMineShafts\":true,\"useTemples\":true,\"useMonuments\":true,\"useRavines\":true,\"useWaterLakes\":true,\"waterLakeChance\":49,\"useLavaLakes\":true,\"lavaLakeChance\":80,\"useLavaOceans\":false,\"fixedBiome\":-1,\"biomeSize\":4,\"riverSize\":5,\"dirtSize\":33,\"dirtCount\":10,\"dirtMinHeight\":0,\"dirtMaxHeight\":256,\"gravelSize\":33,\"gravelCount\":8,\"gravelMinHeight\":0,\"gravelMaxHeight\":256,\"graniteSize\":33,\"graniteCount\":10,\"graniteMinHeight\":0,\"graniteMaxHeight\":80,\"dioriteSize\":33,\"dioriteCount\":10,\"dioriteMinHeight\":0,\"dioriteMaxHeight\":80,\"andesiteSize\":33,\"andesiteCount\":10,\"andesiteMinHeight\":0,\"andesiteMaxHeight\":80,\"coalSize\":17,\"coalCount\":20,\"coalMinHeight\":0,\"coalMaxHeight\":128,\"ironSize\":9,\"ironCount\":20,\"ironMinHeight\":0,\"ironMaxHeight\":64,\"goldSize\":9,\"goldCount\":2,\"goldMinHeight\":0,\"goldMaxHeight\":32,\"redstoneSize\":8,\"redstoneCount\":8,\"redstoneMinHeight\":0,\"redstoneMaxHeight\":16,\"diamondSize\":8,\"diamondCount\":1,\"diamondMinHeight\":0,\"diamondMaxHeight\":16,\"lapisSize\":7,\"lapisCount\":1,\"lapisCenterHeight\":16,\"lapisSpread\":16}";
//        standardProfiles.put(profile.getName(), profile);

//        profile = new LostCityProfile("water_empty", false);
//        profile.setDescription("Private empty terrain for waterbubbles");
////        profile.WATERLEVEL_OFFSET = -80;
//        profile.RAILWAYS_ENABLED = false;
//        profile.RAILWAY_STATIONS_ENABLED = false;
//        profile.HIGHWAY_DISTANCE_MASK = 0;
//        profile.RUBBLELAYER = false;
//        profile.GROUNDLEVEL = 10;
//        profile.EXPLOSION_CHANCE = 0;
//        profile.MINI_EXPLOSION_CHANCE = 0;
//        profile.CITY_CHANCE = 0.0f;
//        profile.GENERATE_MANSIONS = false;
//        profile.GENERATE_MINESHAFTS = false;
//        profile.GENERATE_OCEANMONUMENTS = false;
//        profile.GENERATE_SCATTERED = false;
//        profile.GENERATE_VILLAGES = false;
//        profile.GENERATE_STRONGHOLDS = false;
//        profile.BUILDING_CHANCE = 0.0f;
//        standardProfiles.put(profile.getName(), profile);

//        profile = new LostCityProfile("bio_wasteland", false);
//        profile.setDescription("Private wasteland for biospheres");
//        profile.GROUNDLEVEL = 40;
////        profile.WATERLEVEL_OFFSET = 70;
//        profile.AVOID_WATER = true;
//        profile.CITY_CHANCE = 0.008f;
//        profile.CITY_MINRADIUS = 30;
//        profile.CITY_MAXRADIUS = 80;
//        profile.GENERATE_LAKES = false;
//        profile.GENERATE_OCEANMONUMENTS = false;
//        profile.VINE_CHANCE = 0.0f;
//        profile.CHANCE_OF_RANDOM_LEAFBLOCKS = 0.0f;
//        profile.RUBBLELAYER = true;
//        profile.RUBBLE_DIRT_SCALE = 2.0f;
//        profile.RUBBLE_LEAVE_SCALE = 0.0f;
//        profile.BUILDING_MAXCELLARS = 1;
//        profile.RUIN_CHANCE = 1.0f;
//        profile.RUIN_MINLEVEL_PERCENT = 0.1f;
//        profile.RUIN_MAXLEVEL_PERCENT = 0.4f;
//        profile.AVOID_FOLIAGE = true;
//        profile.AVOID_GENERATED_LAKE_WATER = true;
//        profile.AVOID_GENERATED_LILYPADS = true;
//        profile.AVOID_GENERATED_FLOWERS = true;
//        profile.AVOID_GENERATED_REEDS = true;
//        profile.GENERATE_LAKES = false;
//        profile.GENERATE_MANSIONS = false;
//        profile.GENERATE_MINESHAFTS = true;
//        profile.GENERATE_OCEANMONUMENTS = false;
//        profile.GENERATE_SCATTERED = true;
//        profile.GENERATE_VILLAGES = false;
//        profile.GENERATE_STRONGHOLDS = true;
//        profile.ALLOWED_BIOME_FACTORS = new String[] { "stone_beach=1", "dead_forest=1", "outback=1", "volcanic_island=1", "wasteland=.3" };
//        standardProfiles.put(profile.getName(), profile);

    }

    public static void setupProfiles() {
        Path path = FMLPaths.CONFIGDIR.get();
        Path profileDir = Paths.get(path.toString(), "lostcities/profiles");

        LostCities.getLogger().info("Creating standard profiles into 'config/lostcities/profiles'");

        initStandardProfiles();
        LostCityProfileSetupImp setupImp = new LostCityProfileSetupImp();
        LostCities.setup.profileSetups.forEach(consumer -> {
            consumer.accept(setupImp);
        });

        new File(profileDir.toString()).mkdirs();
        for (Map.Entry<String, LostCityProfile> entry : standardProfiles.entrySet()) {
            String name = entry.getKey();
            if (!"customized".equals(name)) {
                LostCityProfile profile = entry.getValue();
                JsonObject jsonObject = profile.toJson(true);
                Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
                try {
                    try (PrintWriter writer = new PrintWriter(new File(profileDir.toString(), name + ".json"))) {
                        writer.print(gson.toJson(jsonObject));
                        writer.flush();
                    }
                } catch (FileNotFoundException e) {
                    LostCities.getLogger().error("Couldn't save profile '" + name + "'!");
                }
            }
        }

        LostCities.getLogger().info("Reading existing profiles from 'config/lostcities/profiles'");
        readProfiles(profileDir);
    }

    private static void readProfiles(Path profileDir) {
        File[] files = new File(profileDir.toString()).listFiles((dir, name) -> name.endsWith(".json"));
        for (File file : files) {
            String name = file.getName();
            try {
                String json = FileUtils.readFileToString(file, "UTF-8");
                String[] split = name.split("\\.");
                LostCityProfile profile = new LostCityProfile(split[0], json);
                standardProfiles.put(split[0], profile);
            } catch (IOException e) {
                LostCities.getLogger().error("Couldn't read profile '" + name + "'!");
                return;
            }
        }

    }
}
