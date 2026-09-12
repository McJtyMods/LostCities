package mcjty.lostcities.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mcjty.lostcities.LostCities;
import mcjty.lostcities.api.ILostCityProfile;
import mcjty.lostcities.setup.ModSetup;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class LostCityProfile implements ILostCityProfile {

    public static final String CATEGORY_LOSTCITY = "lostcity";
    public static final String CATEGORY_EXPLOSIONS = "explosions";
    public static final String CATEGORY_CITIES = "cities";
    public static final String CATEGORY_CITY_SPHERES = "cityspheres";
    public static final String CATEGORY_CLIENT = "client";
    public static final int MIN_RAILWAY_SPACING = 4;
    public static final int MAX_RAILWAY_SPACING = 128;
    public static final int DEFAULT_RAILWAY_SPACING = 10;

    private final String name;
    private final boolean isPublic;

    private String description = "Default generation, common cities, explosions";
    private String extraDescription = "";
    private String warning = "";
    private String worldStyle = "standard";
    private String iconFile = "";
    private Identifier icon;

    public int DEBRIS_TO_NEARBYCHUNK_FACTOR = 200;

    private String LIQUID_BLOCK = "minecraft:water";
    private BlockState liquidBlock = null;

    private String BASE_BLOCK = "minecraft:stone";
    private BlockState baseBlock = null;

    public float VINE_CHANCE = 0.009f;
    public float CHANCE_OF_RANDOM_LEAFBLOCKS = .1f;
    public int THICKNESS_OF_RANDOM_LEAFBLOCKS = 2;
    public boolean AVOID_FOLIAGE = false;

    public float SCATTERED_CHANCE_MULTIPLIER = 1.0f;

    public boolean RUBBLELAYER = true;
    public float RUBBLE_DIRT_SCALE = 3.0f;
    public float RUBBLE_LEAVE_SCALE = 6.0f;

    public float RUIN_CHANCE = 0.05f;
    public float RUIN_MINLEVEL_PERCENT = 0.8f;
    public float RUIN_MAXLEVEL_PERCENT = 1.0f;

    public int GROUNDLEVEL = 71;
    public int SEALEVEL = -1;   // If -1 just use default

    public boolean HIGHWAY_REQUIRES_TWO_CITIES = true;
    public int HIGHWAY_LEVEL_FROM_CITIES_MODE = 3;
    public float HIGHWAY_MAINPERLIN_SCALE = 50.0f;
    public float HIGHWAY_SECONDARYPERLIN_SCALE = 10.0f;
    public float HIGHWAY_PERLIN_FACTOR = 2.0f;
    public int HIGHWAY_DISTANCE_MASK = 7;
    public boolean HIGHWAY_SUPPORTS = true;

    // INTERCITY_NETWORK_V1 is requested for new worlds. The actual mode is
    // selected once and persisted independently from the street mode.
    public HighwayGenerationMode HIGHWAY_GENERATION_MODE = HighwayGenerationMode.INTERCITY_NETWORK_V1;
    public int HIGHWAY_PLANNING_CELL_SIZE = 128;
    public int HIGHWAY_HUB_SAMPLE_SPACING = 16;
    public float HIGHWAY_HUB_MINIMUM_POTENTIAL = .20f;
    public int HIGHWAY_HUB_SEARCH_RADIUS_CELLS = 2;
    public int HIGHWAY_MINIMUM_HUB_DISTANCE = 64;
    public int HIGHWAY_MAXIMUM_HUB_DISTANCE = 320;
    public int HIGHWAY_MAXIMUM_CONNECTIONS_PER_HUB = 2;
    public int HIGHWAY_MINIMUM_ROUTE_LENGTH = 40;
    public float HIGHWAY_ROUTE_CITY_PENALTY = 1.0f;
    public int HIGHWAY_NETWORK_LEVEL = 0;

    public float RAILWAY_DUNGEON_CHANCE = .01f;
    public boolean RAILWAYS_CAN_END = false;
    public boolean RAILWAYS_ENABLED = true;
    public boolean RAILWAY_STATIONS_ENABLED = true;
    public boolean RAILWAY_SURFACE_STATIONS_ENABLED = true;
    public int RAILWAY_LEVEL_OFFSET = 0;
    public int RAILWAY_SPACING_NORTH_SOUTH = DEFAULT_RAILWAY_SPACING;
    public int RAILWAY_SPACING_EAST_WEST = DEFAULT_RAILWAY_SPACING;

    public boolean EXPLOSIONS_IN_CITIES_ONLY = true;

    public boolean EDITMODE = false;

    public boolean GENERATE_NETHER = false;
    public boolean GENERATE_SPAWNERS = true;
    public boolean GENERATE_LOOT = true;
    public boolean GENERATE_LIGHTING = false;
    public boolean AVOID_WATER = false;

    public float EXPLOSION_CHANCE = .002f;
    public int EXPLOSION_MINRADIUS = 15;
    public int EXPLOSION_MAXRADIUS = 35;
    public int EXPLOSION_MINHEIGHT = 75;
    public int EXPLOSION_MAXHEIGHT = 90;

    public float MINI_EXPLOSION_CHANCE = .03f;
    public int MINI_EXPLOSION_MINRADIUS = 5;
    public int MINI_EXPLOSION_MAXRADIUS = 12;
    public int MINI_EXPLOSION_MINHEIGHT = 60;
    public int MINI_EXPLOSION_MAXHEIGHT = 100;

    public double CITY_CHANCE = .01;    // If this is -1 we use perlin noise for rarity
    public int CITY_MINRADIUS = 50;     // Only used for cityChance type
    public int CITY_MAXRADIUS = 128;    // Only used for cityChance type

    public double CITY_PERLIN_SCALE = 3;       // Used if CITY_CHANCE == -1
    public double CITY_PERLIN_INNERSCALE = .1; // Used if CITY_CHANCE == -1
    public double CITY_PERLIN_OFFSET = .1;     // Used if CITY_CHANCE == -1

    // This threshold is used for both the cityChance variation as the perlin variation
    public float CITY_THRESHOLD = .2f;

    // These four values are used to modify the city factor based on distance from spawn
    // If CITY_SPAWN_DISTANCE2 is 0 then this is disabled
    // The city factor will be multiplied with CITY_SPAWN_MULTIPLIER1 at CITY_SPAWN_DISTANCE1
    // The city factor will be multiplied with CITY_SPAWN_MULTIPLIER2 at CITY_SPAWN_DISTANCE2
    // In between these two distances the factor is linearly interpolated
    // Closer to spawn then CITY_SPAWN_DISTANCE1, CITY_SPAWN_MULTIPLIER1 is used
    // Further from spawn then CITY_SPAWN_DISTANCE2, CITY_SPAWN_MULTIPLIER2 is used
    public int CITY_SPAWN_DISTANCE1 = 0;
    public int CITY_SPAWN_DISTANCE2 = 0;
    public double CITY_SPAWN_MULTIPLIER1 = 1.0;
    public double CITY_SPAWN_MULTIPLIER2 = 1.0;

    // This threshold is used to select another citystyle if the city factor goes below this
    public float CITY_STYLE_THRESHOLD = -1f;
    public String CITY_STYLE_ALTERNATIVE = "";

    public boolean CITY_AVOID_VOID = true;

    public boolean CITYSPHERE_32GRID = false;
    public float CITYSPHERE_FACTOR = 1.2f;
    public float CITYSPHERE_CHANCE = 0.7f;
    public float CITYSPHERE_SURFACE_VARIATION = 1.0f;
    public float CITYSPHERE_OUTSIDE_SURFACE_VARIATION = 1.0f;
    public float CITYSPHERE_MONORAIL_CHANCE = 0.8f;
    public int CITYSPHERE_CLEARABOVE = 0;
    public int CITYSPHERE_CLEARBELOW = 0;
    public boolean CITYSPHERE_CLEARABOVE_UNTIL_AIR = false;
    public boolean CITYSPHERE_CLEARBELOW_UNTIL_AIR = false;

    public int CITYSPHERE_OUTSIDE_GROUNDLEVEL = -1; // DEPRECATED

    public String CITYSPHERE_OUTSIDE_PROFILE = "";
    public boolean CITYSPHERE_ONLY_PREDEFINED = false;
    public int CITYSPHERE_MONORAIL_HEIGHT_OFFSET = -2;

    public int CITY_LEVEL0_HEIGHT = 75;
    public int CITY_LEVEL1_HEIGHT = 83;
    public int CITY_LEVEL2_HEIGHT = 91;
    public int CITY_LEVEL3_HEIGHT = 99;
    public int CITY_LEVEL4_HEIGHT = 107;
    public int CITY_LEVEL5_HEIGHT = 115;
    public int CITY_LEVEL6_HEIGHT = 123;
    public int CITY_LEVEL7_HEIGHT = 131;
    public int CITY_MINHEIGHT = 50;
    public int CITY_MAXHEIGHT = 150;

    public int OCEAN_CORRECTION_BORDER = 4;

    public int TERRAIN_FIX_LOWER_MIN_OFFSET = -4;
    public int TERRAIN_FIX_LOWER_MAX_OFFSET = -3;
    public int TERRAIN_FIX_UPPER_MIN_OFFSET = -1;
    public int TERRAIN_FIX_UPPER_MAX_OFFSET = 1;

    public float CHEST_WITHOUT_LOOT_CHANCE = .2f;
    public float BUILDING_WITHOUT_LOOT_CHANCE = .2f;
    public float BUILDING_CHANCE = .3f;
    public int BUILDING_MINFLOORS = 0;
    public int BUILDING_MAXFLOORS = 8;
    public int BUILDING_MINFLOORS_CHANCE = 4;
    public int BUILDING_MAXFLOORS_CHANCE = 6;
    public int BUILDING_MINCELLARS = 0;
    public int BUILDING_MAXCELLARS = 3;
    public float BUILDING_DOORWAYCHANCE = .6f;
    public float BUILDING_FRONTCHANCE = .2f;

    public float PARK_CHANCE = .2f;
    public float OPEN_LOT_PARK_CHANCE = .8f;
    public float CORRIDOR_CHANCE = .7f;
    public float BRIDGE_CHANCE = .7f;
    public float PLANNED_PRIMARY_BRIDGE_CHANCE = 1.0f;
    public int PLANNED_PRIMARY_BRIDGE_MAX_LENGTH = 12;
    public float FOUNTAIN_CHANCE = .05f;

    public boolean BRIDGE_SUPPORTS = true;
    public boolean PARK_ELEVATION = true;
    public boolean PARK_BORDER = true;
    public int PARK_STREET_THRESHOLD = 3;

    // HIERARCHICAL_GRID_V1 is requested for new worlds. The actual mode is
    // selected once and persisted in LostCityWorldGenData.
    public StreetGenerationMode STREET_GENERATION_MODE = StreetGenerationMode.HIERARCHICAL_GRID_V1;
    public int PRIMARY_ROAD_SPACING_X = 8;
    public int PRIMARY_ROAD_SPACING_Z = 8;
    public float PRIMARY_ROAD_OPTIONAL_CHANCE = .45f;
    public int PRIMARY_ROAD_FORCE_EVERY = 4;
    public int SECONDARY_ROAD_MIN_COUNT_X = 0;
    public int SECONDARY_ROAD_MAX_COUNT_X = 2;
    public int SECONDARY_ROAD_MIN_COUNT_Z = 0;
    public int SECONDARY_ROAD_MAX_COUNT_Z = 2;
    public int MINIMUM_ROAD_SEPARATION = 4;
    public int MINIMUM_ROAD_EDGE_DISTANCE = 3;
    public float TERTIARY_ROAD_CHANCE = .40f;
    public int TERTIARY_ROAD_MIN_LENGTH = 2;
    public int TERTIARY_ROAD_MAX_LENGTH = 5;
    public MultiBuildingStreetConflict MULTI_BUILDING_STREET_CONFLICT = MultiBuildingStreetConflict.OVERRIDE_MINOR;

    public boolean MULTI_USE_CORNER = false;
    public boolean USE_AVG_HEIGHTMAP = false;

    public int BEDROCK_LAYER = 1;

    public float HORIZON = -1f;
    public float FOG_RED = -1.0f;
    public float FOG_GREEN = -1.0f;
    public float FOG_BLUE = -1.0f;
    public float FOG_DENSITY = -1.0f;

    public String SPAWN_BIOME = "";
    public String SPAWN_CITY = "";
    public String SPAWN_SPHERE = "";
    public boolean SPAWN_NOT_IN_BUILDING = false;
    public boolean FORCE_SPAWN_IN_BUILDING = false;
    public String[] FORCE_SPAWN_BUILDINGS = new String[0];
    public String[] FORCE_SPAWN_PARTS = new String[0];
    public int SPAWN_CHECK_RADIUS = 200;
    public int SPAWN_RADIUS_INCREASE = 100;
    public int SPAWN_CHECK_ATTEMPTS = 20000;

    public LandscapeType LANDSCAPE_TYPE = LandscapeType.DEFAULT;

    public LostCityProfile(String name, boolean isPublic) {
        this.name = name;
        this.isPublic = isPublic;
    }

    public LostCityProfile(String name, String json) {
        this.name = name;
        Configuration config = new Configuration();
        JsonElement element = JsonParser.parseString(json);
        JsonObject root = element.getAsJsonObject();
        config.fromJson(root);
        this.isPublic = !root.has("public") || root.getAsJsonPrimitive("public").getAsBoolean();
        init(config);
    }

    public void setIconFile(String iconFile) {
        this.iconFile = iconFile;
    }

    public Identifier getIcon() {
        if (icon != null) {
            return icon;
        }
        if (iconFile == null || iconFile.isEmpty()) {
            return null;
        }
        icon = Identifier.fromNamespaceAndPath(LostCities.MODID, iconFile);
        return icon;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void init(Configuration cfg) {
        cfg.addCustomCategoryComment(LostCityProfile.CATEGORY_LOSTCITY, "Settings related to the Lost City for the " + name + " profile");
        cfg.addCustomCategoryComment(LostCityProfile.CATEGORY_EXPLOSIONS, "Settings related to explosions and damage for the " + name + " profile");
        cfg.addCustomCategoryComment(LostCityProfile.CATEGORY_CITIES, "Settings related to city generation for the " + name + " profile");
        cfg.addCustomCategoryComment(LostCityProfile.CATEGORY_CITY_SPHERES, "Settings related to city sphere generation for the " + name + " profile");
        cfg.addCustomCategoryComment(LostCityProfile.CATEGORY_CLIENT, "Client side settings for the " + name + " profile");

        initLostcity(cfg);
        initExplosions(cfg);
        initCities(cfg);
        initCitySpheres(cfg);
        initClient(cfg);
    }

    public String getCategoryCitySpheres() {
        return LostCityProfile.CATEGORY_CITY_SPHERES;
    }

    public String getCategoryLostcity() {
        return LostCityProfile.CATEGORY_LOSTCITY;
    }

    private void initClient(Configuration cfg) {
        HORIZON = cfg.getFloat("horizon", LostCityProfile.CATEGORY_CLIENT, HORIZON, -1f, 256f, "This is used client-side (but only if the client has this mod) to set the height of the horizon");
        FOG_RED = cfg.getFloat("fogRed", LostCityProfile.CATEGORY_CLIENT, FOG_RED, -1f, 1f, "This is used client-side (but only if the client has this mod) for the fog color");
        FOG_GREEN = cfg.getFloat("fogGreen", LostCityProfile.CATEGORY_CLIENT, FOG_GREEN, -1f, 1f, "This is used client-side (but only if the client has this mod) for the fog color");
        FOG_BLUE = cfg.getFloat("fogBlue", LostCityProfile.CATEGORY_CLIENT, FOG_BLUE, -1f, 1f, "This is used client-side (but only if the client has this mod) for the fog color");
        FOG_DENSITY = cfg.getFloat("fogDensity", LostCityProfile.CATEGORY_CLIENT, FOG_DENSITY, -1f, 1f, "This is used client-side (but only if the client has this mod) for the fog density");
    }

    private void initCitySpheres(Configuration cfg) {
        CITYSPHERE_FACTOR = cfg.getFloat("citySphereFactor", LostCityProfile.CATEGORY_CITY_SPHERES, CITYSPHERE_FACTOR, 0.1f, 10.0f, "Only used in 'space' landscape. This factor will be multiplied with the radius of the city to calculate the radius of the outer sphere");
        CITYSPHERE_CHANCE = cfg.getFloat("citySphereChance", LostCityProfile.CATEGORY_CITY_SPHERES, CITYSPHERE_CHANCE, 0.0f, 1.0f, "The chance that a city sphere will be generated");
        CITYSPHERE_CLEARABOVE = cfg.getInt("citySphereClearAbove", LostCityProfile.CATEGORY_CITY_SPHERES, CITYSPHERE_CLEARABOVE, 0, 1024, "Number of blocks to clear above the top city sphere glass (0 is disabled)");
        CITYSPHERE_CLEARABOVE_UNTIL_AIR = cfg.getBoolean("citySphereClearAboveUntilAir", LostCityProfile.CATEGORY_CITY_SPHERES, CITYSPHERE_CLEARABOVE_UNTIL_AIR, "If enabled this will additionally clear blocks above what is already cleared by CLEARBOVE until air is reached");
        CITYSPHERE_CLEARBELOW = cfg.getInt("citySphereClearBelow", LostCityProfile.CATEGORY_CITY_SPHERES, CITYSPHERE_CLEARBELOW, 0, 1024, "Number of blocks to clear below the top city sphere (0 is disabled)");
        CITYSPHERE_CLEARBELOW_UNTIL_AIR = cfg.getBoolean("citySphereClearBelowUntilAir", LostCityProfile.CATEGORY_CITY_SPHERES, CITYSPHERE_CLEARBELOW_UNTIL_AIR, "If enabled this will additionally clear blocks below what is already cleared by CLEARBELOW until air is reached");
        CITYSPHERE_SURFACE_VARIATION = cfg.getFloat("sphereSurfaceVariation", LostCityProfile.CATEGORY_CITY_SPHERES, CITYSPHERE_SURFACE_VARIATION, 0.0f, 1.0f, "Smaller numbers make the surface inside a city sphere more varied");
        CITYSPHERE_OUTSIDE_SURFACE_VARIATION = cfg.getFloat("outsideSurfaceVariation", LostCityProfile.CATEGORY_CITY_SPHERES, CITYSPHERE_OUTSIDE_SURFACE_VARIATION, 0.0f, 1.0f, "Smaller numbers make the surface outside a city sphere more varied");
        CITYSPHERE_MONORAIL_CHANCE = cfg.getFloat("monorailChance", LostCityProfile.CATEGORY_CITY_SPHERES, CITYSPHERE_MONORAIL_CHANCE, 0.0f, 1.0f, "The chance that a city will have a monorail connection in a certain direction. There will only be an actual connection if there is a city in that direction that also wants a monorail");
        CITYSPHERE_ONLY_PREDEFINED = cfg.getBoolean("onlyPredefined", LostCityProfile.CATEGORY_CITY_SPHERES, CITYSPHERE_ONLY_PREDEFINED,
                "If this is true then only predefined spheres are generated");
        CITYSPHERE_OUTSIDE_GROUNDLEVEL = cfg.getInt("outsideGroundLevel", LostCityProfile.CATEGORY_CITY_SPHERES, CITYSPHERE_OUTSIDE_GROUNDLEVEL, -1, 256, "Ground level for outside city spheres (DEPRECATED, USE GROUNDLEVEL OF OTHER PROFILE)");
        CITYSPHERE_OUTSIDE_PROFILE = cfg.getString("outsideProfile", LostCityProfile.CATEGORY_CITY_SPHERES, CITYSPHERE_OUTSIDE_PROFILE, "An optional profile to use for the outside world");
        CITYSPHERE_MONORAIL_HEIGHT_OFFSET = cfg.getInt("monorailOffset", LostCityProfile.CATEGORY_CITY_SPHERES, CITYSPHERE_MONORAIL_HEIGHT_OFFSET, -100, 100, "Offset compared to main height");
        CITYSPHERE_32GRID = cfg.getBoolean("grid32", LostCityProfile.CATEGORY_CITY_SPHERES, CITYSPHERE_32GRID, "If this is true then city spheres are aligned to a 32x32 grid. Otherwise they are aligned to a 16x16 grid");
    }

    private void initLostcity(Configuration cfg) {
        description = cfg.getString("description", LostCityProfile.CATEGORY_LOSTCITY, description, "The description of this profile");
        extraDescription = cfg.getString("extraDescription", LostCityProfile.CATEGORY_LOSTCITY, extraDescription, "Additional information");
        warning = cfg.getString("warning", LostCityProfile.CATEGORY_LOSTCITY, warning, "Warning!");
        worldStyle = cfg.getString("worldStyle", LostCityProfile.CATEGORY_LOSTCITY, worldStyle, "The worldstyle used by this profile (defined in the assets)");
        iconFile = cfg.getString("icon", LostCityProfile.CATEGORY_LOSTCITY, iconFile, "The icon to use in the configuration screen (64x64)");

        LIQUID_BLOCK = cfg.getString("liquidBlock", LostCityProfile.CATEGORY_LOSTCITY, LIQUID_BLOCK, "Block to use as a liquid");
        BASE_BLOCK = cfg.getString("baseBlock", LostCityProfile.CATEGORY_LOSTCITY, BASE_BLOCK, "Block to use as the worldgen base");

        SPAWN_BIOME = cfg.getString("spawnBiome", LostCityProfile.CATEGORY_LOSTCITY, SPAWN_BIOME, "When this is set the player will always spawn in the given biome");
        SPAWN_CITY = cfg.getString("spawnCity", LostCityProfile.CATEGORY_LOSTCITY, SPAWN_CITY, "When this is set the player will always spawn in the given predefined city");
        SPAWN_SPHERE = cfg.getString("spawnSphere", LostCityProfile.CATEGORY_LOSTCITY, SPAWN_SPHERE, "When this is set the player will always spawn in the given predefined sphere. If you use <in> the player will always spawn in a random sphere. If you use <out> the player will always spawn outside a sphere");
        SPAWN_NOT_IN_BUILDING = cfg.getBoolean("spawnNotInBuilding", LostCityProfile.CATEGORY_LOSTCITY, SPAWN_NOT_IN_BUILDING, "If this is true the player will not spawn in a building. This can be used in combination with the other spawn settings");
        FORCE_SPAWN_IN_BUILDING = cfg.getBoolean("forceSpawnInBuilding", LostCityProfile.CATEGORY_LOSTCITY, FORCE_SPAWN_IN_BUILDING, "If this is true the player will spawn in a building. This can be used in combination with the other spawn settings");
        FORCE_SPAWN_BUILDINGS = cfg.getStringList("forceSpawnBuildings", LostCityProfile.CATEGORY_LOSTCITY, FORCE_SPAWN_BUILDINGS, "A list of buildings that the player will spawn in if FORCE_SPAWN_IN_BUILDING is true. If this is empty then any building will do. This can be used in combination with 'forceSpawnParts'");
        FORCE_SPAWN_PARTS = cfg.getStringList("forceSpawnParts", LostCityProfile.CATEGORY_LOSTCITY, FORCE_SPAWN_PARTS, "A list of parts that the player will spawn in if FORCE_SPAWN_IN_BUILDING is true. If this is empty then any part will do. This can be used in combination with 'forceSpawnBuildings'");
        SPAWN_CHECK_RADIUS = cfg.getInt("spawnCheckRadius", LostCityProfile.CATEGORY_LOSTCITY, SPAWN_CHECK_RADIUS, 1, 100000, "The start radius to check for a valid spawn location. The search spirals outward from world center and expands by spawnRadiusIncrease if it couldn't find a valid spot");
        SPAWN_RADIUS_INCREASE = cfg.getInt("spawnRadiusIncrease", LostCityProfile.CATEGORY_LOSTCITY, SPAWN_RADIUS_INCREASE, 1, 100000, "The radius increase to use if the spawn check radius is not large enough to find a valid spawn location");
        SPAWN_CHECK_ATTEMPTS = cfg.getInt("spawnCheckAttempts", LostCityProfile.CATEGORY_LOSTCITY, SPAWN_CHECK_ATTEMPTS, 1, 1000000, "The number of chunks to check while looking for a valid spawn location. If this is not enough then this will error");

        SCATTERED_CHANCE_MULTIPLIER = cfg.getFloat("scatteredChanceMultiplier", LostCityProfile.CATEGORY_LOSTCITY, SCATTERED_CHANCE_MULTIPLIER, 0.0f, 100.0f, "Multiplier for the chance a scattered building will generate. With 0 all scattered buildings are disabled");

        TERRAIN_FIX_LOWER_MIN_OFFSET = cfg.getInt("terrainFixLowerMinOffset", LostCityProfile.CATEGORY_LOSTCITY, TERRAIN_FIX_LOWER_MIN_OFFSET, -40, 40, "To fix terrain adjacent to cities, this is the minimum offset (relative to city base level) for the lower mesh used to potentially raise the terrain");
        TERRAIN_FIX_LOWER_MAX_OFFSET = cfg.getInt("terrainFixLowerMaxOffset", LostCityProfile.CATEGORY_LOSTCITY, TERRAIN_FIX_LOWER_MAX_OFFSET, -40, 40, "To fix terrain adjacent to cities, this is the maximum offset (relative to city base level) for the lower mesh used to potentially raise the terrain");
        TERRAIN_FIX_UPPER_MIN_OFFSET = cfg.getInt("terrainFixUpperMinOffset", LostCityProfile.CATEGORY_LOSTCITY, TERRAIN_FIX_UPPER_MIN_OFFSET, -40, 40, "To fix terrain adjacent to cities, this is the minimum offset (relative to city base level) for the upper mesh used to potentially lower the terrain");
        TERRAIN_FIX_UPPER_MAX_OFFSET = cfg.getInt("terrainFixUpperMaxOffset", LostCityProfile.CATEGORY_LOSTCITY, TERRAIN_FIX_UPPER_MAX_OFFSET, -40, 40, "To fix terrain adjacent to cities, this is the maximum offset (relative to city base level) for the upper mesh used to potentially lower the terrain");

        VINE_CHANCE = cfg.getFloat("vineChance", LostCityProfile.CATEGORY_LOSTCITY, VINE_CHANCE, 0.0f, 1.0f, "The chance that a block on the outside of a building will be covered with a vine");
        CHANCE_OF_RANDOM_LEAFBLOCKS = cfg.getFloat("randomLeafBlockChance", LostCityProfile.CATEGORY_LOSTCITY, CHANCE_OF_RANDOM_LEAFBLOCKS, 0.0f, 1.0f, "Chance that leafblocks will be generated at the border of a building and a street");
        THICKNESS_OF_RANDOM_LEAFBLOCKS = cfg.getInt("randomLeafBlockThickness", LostCityProfile.CATEGORY_LOSTCITY, THICKNESS_OF_RANDOM_LEAFBLOCKS, 1, 8,
                "Frequency of leafblocks as seen from the sides of buildings");
        AVOID_FOLIAGE = cfg.getBoolean("avoidFoliage", LostCityProfile.CATEGORY_LOSTCITY, AVOID_FOLIAGE,
                "If this is true then parks will have no foliage (trees and flowers currently)");

        String type = cfg.getString("landscapeType", LostCityProfile.CATEGORY_LOSTCITY, LANDSCAPE_TYPE.getName(),
                "Type of landscape",
                new String[] {
                        LandscapeType.DEFAULT.getName(),
                        LandscapeType.FLOATING.getName(),
                        LandscapeType.SPACE.getName(),
                        LandscapeType.CAVERN.getName() });
        LANDSCAPE_TYPE = LandscapeType.getTypeByName(type);
        if (LANDSCAPE_TYPE == null) {
            throw new RuntimeException("Bad landscape type: " + type + "!");
        }

        RUBBLELAYER = cfg.getBoolean("rubbleLayer", LostCityProfile.CATEGORY_LOSTCITY, RUBBLELAYER,
                "If this is true an alternative way to generate dirt/stone/sand + leave blocks is used that makes the city appear more overgrown");
        RUBBLE_DIRT_SCALE = cfg.getFloat("rubbleDirtScale", LostCityProfile.CATEGORY_LOSTCITY, RUBBLE_DIRT_SCALE, 0.0f, 100.0f,
                "The scale of the dirt layer. Smaller values make the layer larger. Use 0 to disable");
        RUBBLE_LEAVE_SCALE = cfg.getFloat("rubbleLeaveScale", LostCityProfile.CATEGORY_LOSTCITY, RUBBLE_LEAVE_SCALE, 0.0f, 100.0f,
                "The scale of the leave layer. Smaller values make the layer larger. Use 0 to disable");

        RUIN_CHANCE = cfg.getFloat("ruinChance", LostCityProfile.CATEGORY_LOSTCITY, RUIN_CHANCE, 0.0f, 1.0f,
                "This gives the chance that a building is ruined");
        RUIN_MINLEVEL_PERCENT = cfg.getFloat("ruinMinlevelPercent", LostCityProfile.CATEGORY_LOSTCITY, RUIN_MINLEVEL_PERCENT, 0.0f, 1.0f,
                "If a building is ruined this indicates the minimum start height for the ruin destruction layer");
        RUIN_MAXLEVEL_PERCENT = cfg.getFloat("ruinMaxlevelPercent", LostCityProfile.CATEGORY_LOSTCITY, RUIN_MAXLEVEL_PERCENT, 0.0f, 1.0f,
                "If a building is ruined this indicates the maximum start height for the ruin destruction layer");

        GROUNDLEVEL = cfg.getInt("groundLevel", LostCityProfile.CATEGORY_LOSTCITY, GROUNDLEVEL, 2, 256, "Ground level");
        SEALEVEL = cfg.getInt("seaLevel", LostCityProfile.CATEGORY_LOSTCITY, SEALEVEL, -1, 256, "Sea level (-1 is default)");

        CHEST_WITHOUT_LOOT_CHANCE = cfg.getFloat("chestWithoutLootChance", LostCityProfile.CATEGORY_LOSTCITY, CHEST_WITHOUT_LOOT_CHANCE, 0.0f, 1.0f, "The chance that a chest will have no loot");
        BUILDING_WITHOUT_LOOT_CHANCE = cfg.getFloat("buildingWithoutLootChance", LostCityProfile.CATEGORY_LOSTCITY, BUILDING_WITHOUT_LOOT_CHANCE, 0.0f, 1.0f, "The chance that a building will have no loot and no spawners");
        BUILDING_CHANCE = cfg.getFloat("buildingChance", LostCityProfile.CATEGORY_LOSTCITY, BUILDING_CHANCE, 0.0f, 1.0f, "The chance that a chunk in a city will have a building. Otherwise it will be a street");
        BUILDING_MINFLOORS = cfg.getInt("buildingMinFloors", LostCityProfile.CATEGORY_LOSTCITY, BUILDING_MINFLOORS, 0, 60, "The minimum number of floors (above ground) for a building (0 means the first floor only)");
        BUILDING_MAXFLOORS = cfg.getInt("buildingMaxFloors", LostCityProfile.CATEGORY_LOSTCITY, BUILDING_MAXFLOORS, 0, 60, "A cap for the amount of floors a city can have (above ground)");
        BUILDING_MINFLOORS_CHANCE = cfg.getInt("buildingMinFloorsChance", LostCityProfile.CATEGORY_LOSTCITY, BUILDING_MINFLOORS_CHANCE, 1, 60, "The amount of floors of a building is equal to: " +
                "MINFLOORS + random(MINFLOORS_CHANCE + (cityFactor + .1f) * (MAXFLOORS_CHANCE - MINFLOORS_CHANCE))");
        BUILDING_MAXFLOORS_CHANCE = cfg.getInt("buildingMaxFloorsChance", LostCityProfile.CATEGORY_LOSTCITY, BUILDING_MAXFLOORS_CHANCE, 1, 60, "The amount of floors of a building is equal to: " +
                "MINFLOORS + random(MINFLOORS_CHANCE + (cityFactor + .1f) * (MAXFLOORS_CHANCE - MINFLOORS_CHANCE))");

        BUILDING_MINCELLARS = cfg.getInt("buildingMinCellars", LostCityProfile.CATEGORY_LOSTCITY, BUILDING_MINCELLARS, 0, 20, "The minimum number of cellars (below ground). 0 means no cellar");
        BUILDING_MAXCELLARS = cfg.getInt("buildingMaxCellars", LostCityProfile.CATEGORY_LOSTCITY, BUILDING_MAXCELLARS, 0, 20, "The maximum number of cellars (below ground). 0 means no cellar");
        BUILDING_DOORWAYCHANCE = cfg.getFloat("buildingDoorwayChance", LostCityProfile.CATEGORY_LOSTCITY, BUILDING_DOORWAYCHANCE, 0.0f, 1.0f, "The chance that a doorway will be generated at a side of a building (on any level). Only when possible");
        BUILDING_FRONTCHANCE = cfg.getFloat("buildingFrontChance", LostCityProfile.CATEGORY_LOSTCITY, BUILDING_FRONTCHANCE, 0.0f, 1.0f, "The chance that a building will have a 'front' part if this is possible (i.e. adjacent street)");
        PARK_CHANCE = cfg.getFloat("parkChance", LostCityProfile.CATEGORY_LOSTCITY, PARK_CHANCE, 0.0f, 1.0f, "The chance that a non-building section can be a park section");
        OPEN_LOT_PARK_CHANCE = cfg.getFloat("openLotParkChance", LostCityProfile.CATEGORY_LOSTCITY, OPEN_LOT_PARK_CHANCE, 0.0f, 1.0f,
                "The chance that a hierarchical open lot gets a park part");

        CORRIDOR_CHANCE = cfg.getFloat("corridorChance", LostCityProfile.CATEGORY_LOSTCITY, CORRIDOR_CHANCE, 0.0f, 1.0f, "The chance that a chunk can possibly contain a corridor. " +
                "There actually being a corridor also depends on the presence of adjacent corridors");
        BRIDGE_CHANCE = cfg.getFloat("bridgeChance", LostCityProfile.CATEGORY_LOSTCITY, BRIDGE_CHANCE, 0.0f, 1.0f, "The chance that a chunk can possibly contain a bridge. " +
                "There actually being a bridge also depends on the presence of adjacent bridges and other conditions");
        PLANNED_PRIMARY_BRIDGE_CHANCE = cfg.getFloat("plannedPrimaryBridgeChance", LostCityProfile.CATEGORY_LOSTCITY, PLANNED_PRIMARY_BRIDGE_CHANCE,
                0.0f, 1.0f, "Chance for an entire eligible hierarchical primary-road water crossing to become a bridge");
        PLANNED_PRIMARY_BRIDGE_MAX_LENGTH = cfg.getInt("plannedPrimaryBridgeMaxLength", LostCityProfile.CATEGORY_LOSTCITY, PLANNED_PRIMARY_BRIDGE_MAX_LENGTH,
                1, 64, "Maximum water-gap length in chunks for a hierarchical primary-road bridge");
        BRIDGE_SUPPORTS = cfg.getBoolean("bridgeSupports", LostCityProfile.CATEGORY_LOSTCITY, BRIDGE_SUPPORTS,
                "If true bridges get supports when needed. You can disable this if you have bridges that span void chunks");
        PARK_ELEVATION = cfg.getBoolean("parkElevation", LostCityProfile.CATEGORY_LOSTCITY, PARK_ELEVATION,
                "If true parks get an extra layer of elevation");
        PARK_BORDER = cfg.getBoolean("parkBorder", LostCityProfile.CATEGORY_LOSTCITY, PARK_BORDER,
                "If true parks border will use the street block as base.");
        PARK_STREET_THRESHOLD = cfg.getInt("parkStreetThreshold", LostCityProfile.CATEGORY_LOSTCITY, PARK_STREET_THRESHOLD,
                0, 8, "Determines how many surrounding street it needs to generate a park, based on current chunk's park chance.");

        String streetMode = cfg.getString("streetGenerationMode", LostCityProfile.CATEGORY_LOSTCITY, STREET_GENERATION_MODE.name(),
                "Street layout requested when a new world is initialized. Existing worlds keep their persisted mode.",
                new String[] { StreetGenerationMode.LEGACY.name(), StreetGenerationMode.HIERARCHICAL_GRID_V1.name() });
        STREET_GENERATION_MODE = StreetGenerationMode.byName(streetMode);
        PRIMARY_ROAD_SPACING_X = cfg.getInt("primaryRoadSpacingX", LostCityProfile.CATEGORY_LOSTCITY, PRIMARY_ROAD_SPACING_X,
                8, 128, "Horizontal chunk spacing of candidate north/south primary-road corridors");
        PRIMARY_ROAD_SPACING_Z = cfg.getInt("primaryRoadSpacingZ", LostCityProfile.CATEGORY_LOSTCITY, PRIMARY_ROAD_SPACING_Z,
                8, 128, "Vertical chunk spacing of candidate east/west primary-road corridors");
        PRIMARY_ROAD_OPTIONAL_CHANCE = cfg.getFloat("primaryRoadOptionalChance", LostCityProfile.CATEGORY_LOSTCITY, PRIMARY_ROAD_OPTIONAL_CHANCE,
                0.0f, 1.0f, "Chance that a non-forced candidate primary-road corridor is enabled");
        PRIMARY_ROAD_FORCE_EVERY = cfg.getInt("primaryRoadForceEvery", LostCityProfile.CATEGORY_LOSTCITY, PRIMARY_ROAD_FORCE_EVERY,
                1, 16, "Force every Nth candidate primary-road corridor to cap the maximum gap");
        SECONDARY_ROAD_MIN_COUNT_X = cfg.getInt("secondaryRoadMinCountX", LostCityProfile.CATEGORY_LOSTCITY, SECONDARY_ROAD_MIN_COUNT_X,
                0, 128, "Minimum internal north/south secondary roads in a primary block");
        SECONDARY_ROAD_MAX_COUNT_X = cfg.getInt("secondaryRoadMaxCountX", LostCityProfile.CATEGORY_LOSTCITY, SECONDARY_ROAD_MAX_COUNT_X,
                0, 128, "Maximum internal north/south secondary roads in a primary block");
        SECONDARY_ROAD_MIN_COUNT_Z = cfg.getInt("secondaryRoadMinCountZ", LostCityProfile.CATEGORY_LOSTCITY, SECONDARY_ROAD_MIN_COUNT_Z,
                0, 128, "Minimum internal east/west secondary roads in a primary block");
        SECONDARY_ROAD_MAX_COUNT_Z = cfg.getInt("secondaryRoadMaxCountZ", LostCityProfile.CATEGORY_LOSTCITY, SECONDARY_ROAD_MAX_COUNT_Z,
                0, 128, "Maximum internal east/west secondary roads in a primary block");
        MINIMUM_ROAD_SEPARATION = cfg.getInt("minimumRoadSeparation", LostCityProfile.CATEGORY_LOSTCITY, MINIMUM_ROAD_SEPARATION,
                2, 32, "Minimum chunk distance between parallel secondary roads");
        MINIMUM_ROAD_EDGE_DISTANCE = cfg.getInt("minimumRoadEdgeDistance", LostCityProfile.CATEGORY_LOSTCITY, MINIMUM_ROAD_EDGE_DISTANCE,
                2, 32, "Minimum chunk distance between a secondary road and its bounding primary road");
        TERTIARY_ROAD_CHANCE = cfg.getFloat("tertiaryRoadChance", LostCityProfile.CATEGORY_LOSTCITY, TERTIARY_ROAD_CHANCE,
                0.0f, 1.0f, "Chance for a subdivided block to contain one short tertiary access road");
        TERTIARY_ROAD_MIN_LENGTH = cfg.getInt("tertiaryRoadMinLength", LostCityProfile.CATEGORY_LOSTCITY, TERTIARY_ROAD_MIN_LENGTH,
                1, 16, "Minimum tertiary access-road length in chunks");
        TERTIARY_ROAD_MAX_LENGTH = cfg.getInt("tertiaryRoadMaxLength", LostCityProfile.CATEGORY_LOSTCITY, TERTIARY_ROAD_MAX_LENGTH,
                1, 32, "Maximum tertiary access-road length in chunks");
        String multiConflict = cfg.getString("multiBuildingStreetConflict", LostCityProfile.CATEGORY_LOSTCITY, MULTI_BUILDING_STREET_CONFLICT.name(),
                "How random multi-buildings interact with planned roads (ignored by legacy street generation)",
                new String[] { MultiBuildingStreetConflict.BLOCK_ALL.name(), MultiBuildingStreetConflict.OVERRIDE_MINOR.name(), MultiBuildingStreetConflict.OVERRIDE_ALL.name() });
        MULTI_BUILDING_STREET_CONFLICT = MultiBuildingStreetConflict.byName(multiConflict);

        if (SECONDARY_ROAD_MIN_COUNT_X > SECONDARY_ROAD_MAX_COUNT_X || SECONDARY_ROAD_MIN_COUNT_Z > SECONDARY_ROAD_MAX_COUNT_Z) {
            throw new IllegalArgumentException("Secondary road minimum counts cannot exceed maximum counts");
        }
        if (TERTIARY_ROAD_MIN_LENGTH > TERTIARY_ROAD_MAX_LENGTH) {
            throw new IllegalArgumentException("tertiaryRoadMinLength cannot exceed tertiaryRoadMaxLength");
        }
        if (PRIMARY_ROAD_SPACING_X < 8 || PRIMARY_ROAD_SPACING_X > 128
                || PRIMARY_ROAD_SPACING_Z < 8 || PRIMARY_ROAD_SPACING_Z > 128) {
            throw new IllegalArgumentException("Primary road candidate spacing must be between 8 and 128 chunks");
        }
        if (PRIMARY_ROAD_OPTIONAL_CHANCE < 0 || PRIMARY_ROAD_OPTIONAL_CHANCE > 1
                || PRIMARY_ROAD_FORCE_EVERY < 1 || PRIMARY_ROAD_FORCE_EVERY > 16) {
            throw new IllegalArgumentException("Invalid primary road activation chance or forced interval");
        }
        if (PLANNED_PRIMARY_BRIDGE_CHANCE < 0 || PLANNED_PRIMARY_BRIDGE_CHANCE > 1
                || PLANNED_PRIMARY_BRIDGE_MAX_LENGTH < 1 || PLANNED_PRIMARY_BRIDGE_MAX_LENGTH > 64) {
            throw new IllegalArgumentException("Invalid planned primary bridge chance or maximum length");
        }
        if (SECONDARY_ROAD_MIN_COUNT_X < 0 || SECONDARY_ROAD_MAX_COUNT_X > 128
                || SECONDARY_ROAD_MIN_COUNT_Z < 0 || SECONDARY_ROAD_MAX_COUNT_Z > 128) {
            throw new IllegalArgumentException("Secondary road counts must be between 0 and 128");
        }
        if (MINIMUM_ROAD_SEPARATION < 2 || MINIMUM_ROAD_SEPARATION > 32
                || MINIMUM_ROAD_EDGE_DISTANCE < 2 || MINIMUM_ROAD_EDGE_DISTANCE > 32) {
            throw new IllegalArgumentException("Road separation and edge distance must be between 2 and 32 chunks");
        }
        if (TERTIARY_ROAD_CHANCE < 0 || TERTIARY_ROAD_CHANCE > 1
                || TERTIARY_ROAD_MIN_LENGTH < 1 || TERTIARY_ROAD_MAX_LENGTH > 32) {
            throw new IllegalArgumentException("Invalid tertiary road chance or length");
        }

        FOUNTAIN_CHANCE = cfg.getFloat("fountainChance", LostCityProfile.CATEGORY_LOSTCITY, FOUNTAIN_CHANCE, 0.0f, 1.0f, "The chance that a street section contains a fountain");

        RAILWAY_DUNGEON_CHANCE = cfg.getFloat("railwayDungeonChance", LostCityProfile.CATEGORY_LOSTCITY, RAILWAY_DUNGEON_CHANCE, 0.0f, 1.0f,
                "The chance that a chunk next to a railway will have a railway dungeon");
        RAILWAYS_CAN_END = cfg.getBoolean("railwaysCanEnd", LostCityProfile.CATEGORY_LOSTCITY, RAILWAYS_CAN_END,
                "If true the a place where a station would have been if there was a city above will have an 'ending' rail part if one side of the 'station' has no connections. Useful in case cities are rare");
        RAILWAYS_ENABLED = cfg.getBoolean("railwaysEnabled", LostCityProfile.CATEGORY_LOSTCITY, RAILWAYS_ENABLED,
                "If true then railways are enabled. If false they are not (but stations will still generate)");
        RAILWAY_STATIONS_ENABLED = cfg.getBoolean("railwayStationsEnabled", LostCityProfile.CATEGORY_LOSTCITY, RAILWAY_STATIONS_ENABLED,
                "If true then railway stations are enabled");
        RAILWAY_SURFACE_STATIONS_ENABLED = cfg.getBoolean("railwaySurfaceStationsEnabled", LostCityProfile.CATEGORY_LOSTCITY, RAILWAY_SURFACE_STATIONS_ENABLED,
                "If true then railway surface station are enabled, otherwise there'll only be underground stations.");
        RAILWAY_LEVEL_OFFSET = cfg.getInt("railwayLevelOffset", LostCityProfile.CATEGORY_LOSTCITY, RAILWAY_LEVEL_OFFSET, -8, 2,
                "Vertical offset of the underground railway in multiples of 6 blocks. 0 keeps the railway at its original level");
        RAILWAY_SPACING_NORTH_SOUTH = cfg.getInt("railwaySpacingNorthSouth", LostCityProfile.CATEGORY_LOSTCITY, RAILWAY_SPACING_NORTH_SOUTH, MIN_RAILWAY_SPACING, MAX_RAILWAY_SPACING,
                "North-south distance in chunks between east-west railway tracks. Must be an even number");
        RAILWAY_SPACING_EAST_WEST = cfg.getInt("railwaySpacingEastWest", LostCityProfile.CATEGORY_LOSTCITY, RAILWAY_SPACING_EAST_WEST, MIN_RAILWAY_SPACING, MAX_RAILWAY_SPACING,
                "East-west distance in chunks between north-south railway tracks. Must be an even number");
        if (RAILWAY_SPACING_NORTH_SOUTH < MIN_RAILWAY_SPACING || RAILWAY_SPACING_NORTH_SOUTH > MAX_RAILWAY_SPACING
                || RAILWAY_SPACING_EAST_WEST < MIN_RAILWAY_SPACING || RAILWAY_SPACING_EAST_WEST > MAX_RAILWAY_SPACING
                || (RAILWAY_SPACING_NORTH_SOUTH & 1) != 0 || (RAILWAY_SPACING_EAST_WEST & 1) != 0) {
            throw new IllegalArgumentException("Railway spacing must be an even number from " + MIN_RAILWAY_SPACING
                    + " through " + MAX_RAILWAY_SPACING + " chunks");
        }

        MULTI_USE_CORNER = cfg.getBoolean("multiUseCorner", LostCityProfile.CATEGORY_LOSTCITY, MULTI_USE_CORNER,
                "Determine whether the multibuilding should use surrounding average level, or just top left corner level.");
        USE_AVG_HEIGHTMAP = cfg.getBoolean("useAvgHeightmap", LostCityProfile.CATEGORY_LOSTCITY, USE_AVG_HEIGHTMAP,
                "City normal level generation is now dependent on surrounding heightmap and get an average of the surrounding heightmap. This uses extra performance since it needs to retrieve surrounding city heightmaps.");

        HIGHWAY_REQUIRES_TWO_CITIES = cfg.getBoolean("highwayRequiresTwoCities", LostCityProfile.CATEGORY_LOSTCITY, HIGHWAY_REQUIRES_TWO_CITIES,
                "If true then a highway will only generate if both sides have a valid city. If false then one city is sufficient");
        HIGHWAY_LEVEL_FROM_CITIES_MODE = cfg.getInt("highwayLevelFromCities", LostCityProfile.CATEGORY_LOSTCITY, HIGHWAY_LEVEL_FROM_CITIES_MODE,
                0, 4, "0 (take height from the first endpoint city), 1 (take minimum height from both cities), 2 (take maximum height from both cities), 3 (take average height), 4 (use highwayNetworkLevel)");
        HIGHWAY_DISTANCE_MASK = cfg.getInt("highwayDistanceMask", LostCityProfile.CATEGORY_LOSTCITY, HIGHWAY_DISTANCE_MASK,
                0, Integer.MAX_VALUE, "Mask to control how far highways can generate. Must be a power of 2 (minus 1). If 0 there are no highways at all");
        HIGHWAY_MAINPERLIN_SCALE = cfg.getFloat("highwayMainPerlinScale", LostCityProfile.CATEGORY_LOSTCITY, HIGHWAY_MAINPERLIN_SCALE, 1.0f, 1000.0f,
                "For highways on a certain axis, this value is used to scale the perlin noise generator on the main axis. Increasing this value will increase the frequency of highways but make them smaller");
        HIGHWAY_SECONDARYPERLIN_SCALE = cfg.getFloat("highwaySecondaryPerlinScale", LostCityProfile.CATEGORY_LOSTCITY, HIGHWAY_SECONDARYPERLIN_SCALE, 1.0f, 1000.0f,
                "For highways on a certain axis, this value is used to scale the perlin noise generator on the secondary axis. Increasing this value will increase the variation of nearby highways");
        HIGHWAY_PERLIN_FACTOR = cfg.getFloat("highwayPerlinFactor", LostCityProfile.CATEGORY_LOSTCITY, HIGHWAY_PERLIN_FACTOR, -100, 100,
                "The highway perlin noise is compared to this value. Setting this to 0 would give 50% chance of a highway being at a spot. Note that highways only generate on chunks a multiple of 8. Setting this very high will prevent highways from generating");
        HIGHWAY_SUPPORTS = cfg.getBoolean("highwaySupports", LostCityProfile.CATEGORY_LOSTCITY, HIGHWAY_SUPPORTS,
                "If true highways get supports when needed. You can disable this if you have highways that span void chunks");

        String highwayMode = cfg.getString("highwayGenerationMode", LostCityProfile.CATEGORY_LOSTCITY, HIGHWAY_GENERATION_MODE.name(),
                "Highway layout requested when a new world is initialized. Existing worlds keep their independently persisted mode.",
                new String[] { HighwayGenerationMode.LEGACY.name(), HighwayGenerationMode.INTERCITY_NETWORK_V1.name() });
        HIGHWAY_GENERATION_MODE = HighwayGenerationMode.byName(highwayMode);
        HIGHWAY_PLANNING_CELL_SIZE = cfg.getInt("highwayPlanningCellSize", LostCityProfile.CATEGORY_LOSTCITY, HIGHWAY_PLANNING_CELL_SIZE,
                32, 512, "Size in chunks of one INTERCITY_NETWORK_V1 hub-planning cell");
        HIGHWAY_HUB_SAMPLE_SPACING = cfg.getInt("highwayHubSampleSpacing", LostCityProfile.CATEGORY_LOSTCITY, HIGHWAY_HUB_SAMPLE_SPACING,
                1, 512, "Chunk spacing of deterministic city-potential samples inside a highway planning cell");
        HIGHWAY_HUB_MINIMUM_POTENTIAL = cfg.getFloat("highwayHubMinimumPotential", LostCityProfile.CATEGORY_LOSTCITY, HIGHWAY_HUB_MINIMUM_POTENTIAL,
                0.0f, 1.0f, "Minimum height- and biome-adjusted city-potential score required to create a highway hub");
        HIGHWAY_HUB_SEARCH_RADIUS_CELLS = cfg.getInt("highwayHubSearchRadiusCells", LostCityProfile.CATEGORY_LOSTCITY, HIGHWAY_HUB_SEARCH_RADIUS_CELLS,
                0, 8, "Bounded planning-cell radius in which a highway hub considers other hubs");
        HIGHWAY_MINIMUM_HUB_DISTANCE = cfg.getInt("highwayMinimumHubDistance", LostCityProfile.CATEGORY_LOSTCITY, HIGHWAY_MINIMUM_HUB_DISTANCE,
                0, 4096, "Minimum Euclidean chunk distance between connected highway hubs");
        HIGHWAY_MAXIMUM_HUB_DISTANCE = cfg.getInt("highwayMaximumHubDistance", LostCityProfile.CATEGORY_LOSTCITY, HIGHWAY_MAXIMUM_HUB_DISTANCE,
                0, 4096, "Maximum Euclidean chunk distance between connected highway hubs");
        HIGHWAY_MAXIMUM_CONNECTIONS_PER_HUB = cfg.getInt("highwayMaximumConnectionsPerHub", LostCityProfile.CATEGORY_LOSTCITY, HIGHWAY_MAXIMUM_CONNECTIONS_PER_HUB,
                1, 8, "Maximum accepted INTERCITY_NETWORK_V1 connections incident to one hub");
        HIGHWAY_MINIMUM_ROUTE_LENGTH = cfg.getInt("highwayMinimumRouteLength", LostCityProfile.CATEGORY_LOSTCITY, HIGHWAY_MINIMUM_ROUTE_LENGTH,
                0, 4096, "Minimum Manhattan length in chunks of an inter-city highway route");
        HIGHWAY_ROUTE_CITY_PENALTY = cfg.getFloat("highwayRouteCityPenalty", LostCityProfile.CATEGORY_LOSTCITY, HIGHWAY_ROUTE_CITY_PENALTY,
                0.0f, 1000.0f, "Weight applied to approximate city-potential samples when choosing an L-route bend");
        HIGHWAY_NETWORK_LEVEL = cfg.getInt("highwayNetworkLevel", LostCityProfile.CATEGORY_LOSTCITY, HIGHWAY_NETWORK_LEVEL,
                0, 32, "Fixed highway level used when highwayLevelFromCities is 4");

        if (HIGHWAY_HUB_SAMPLE_SPACING > HIGHWAY_PLANNING_CELL_SIZE) {
            throw new IllegalArgumentException("highwayHubSampleSpacing cannot exceed highwayPlanningCellSize");
        }
        if (HIGHWAY_MINIMUM_HUB_DISTANCE > HIGHWAY_MAXIMUM_HUB_DISTANCE) {
            throw new IllegalArgumentException("highwayMinimumHubDistance cannot exceed highwayMaximumHubDistance");
        }

        BEDROCK_LAYER = cfg.getInt("bedrockLayer", LostCityProfile.CATEGORY_LOSTCITY, BEDROCK_LAYER, 0, 10,
                "The height of the bedrock layer that is generated at the bottom of some world types. Set to 0 to disable this and get default bedrock generation");

        EDITMODE = cfg.getBoolean("editMode", LostCityProfile.CATEGORY_LOSTCITY, EDITMODE,
                "If true then this world is in edit mode");

        GENERATE_NETHER = cfg.getBoolean("generateNether", LostCityProfile.CATEGORY_LOSTCITY, GENERATE_NETHER,
                "If true then generate a cavern type world in the Nether");
        GENERATE_SPAWNERS = cfg.getBoolean("generateSpawners", LostCityProfile.CATEGORY_LOSTCITY, GENERATE_SPAWNERS,
                "If true then the buildings will be full of spawners");
        GENERATE_LOOT = cfg.getBoolean("generateLoot", LostCityProfile.CATEGORY_LOSTCITY, GENERATE_LOOT,
                "If true the chests in the buildings will contain loot");
        GENERATE_LIGHTING = cfg.getBoolean("generateLighting", LostCityProfile.CATEGORY_LOSTCITY, GENERATE_LIGHTING,
                "If true then there will be minimal lighting in the buildings");
        AVOID_WATER = cfg.getBoolean("avoidWater", LostCityProfile.CATEGORY_LOSTCITY, AVOID_WATER,
                "If true then all water will be avoided (replaced with air)");
    }

    private void initCities(Configuration cfg) {
        CITY_CHANCE = cfg.getDouble("cityChance", LostCityProfile.CATEGORY_CITIES, CITY_CHANCE, -1.0, 1.0, "The chance this chunk will be the center of a city (use -1 for perlin noise variant)");
        CITY_SPAWN_DISTANCE1 = cfg.getInt("citySpawnDistance1", LostCityProfile.CATEGORY_CITIES, CITY_SPAWN_DISTANCE1, 0, 10000000, "At this distance from spawn (in blocks) the city factor will be equal to CITY_SPAWN_MULTIPLIER1");
        CITY_SPAWN_DISTANCE2 = cfg.getInt("citySpawnDistance2", LostCityProfile.CATEGORY_CITIES, CITY_SPAWN_DISTANCE2, 0, 10000000, "At this distance from spawn (in blocks) the city factor will be equal to CITY_SPAWN_MULTIPLIER2. If this is 0 then no scaling is done");
        CITY_SPAWN_MULTIPLIER1 = cfg.getDouble("citySpawnMultiplier1", LostCityProfile.CATEGORY_CITIES, CITY_SPAWN_MULTIPLIER1, 0, 1, "City factor at CITY_SPAWN_DISTANCE1");
        CITY_SPAWN_MULTIPLIER2 = cfg.getDouble("citySpawnMultiplier2", LostCityProfile.CATEGORY_CITIES, CITY_SPAWN_MULTIPLIER2, 0, 1, "City factor at CITY_SPAWN_DISTANCE2");
        CITY_MINRADIUS = cfg.getInt("cityMinRadius", LostCityProfile.CATEGORY_CITIES, CITY_MINRADIUS, 1, 2000, "The minimum radius of a city");
        CITY_MAXRADIUS = cfg.getInt("cityMaxRadius", LostCityProfile.CATEGORY_CITIES, CITY_MAXRADIUS, 1, 2000, "The maximum radius of a city");
        CITY_PERLIN_SCALE = cfg.getDouble("cityPerlinScale", LostCityProfile.CATEGORY_CITIES, CITY_PERLIN_SCALE, -1000000, 1000000, "The scale for the city rarity perlin map");
        CITY_PERLIN_OFFSET = cfg.getDouble("cityPerlinOffset", LostCityProfile.CATEGORY_CITIES, CITY_PERLIN_OFFSET, -1000000, 1000000, "The offset for the city rarity perlin map");
        CITY_PERLIN_INNERSCALE = cfg.getDouble("cityPerlinInnerScale", LostCityProfile.CATEGORY_CITIES, CITY_PERLIN_INNERSCALE, -1000000, 1000000, "The internal scale for the city rarity perlin map");
        CITY_THRESHOLD = cfg.getFloat("cityThreshold", LostCityProfile.CATEGORY_CITIES, CITY_THRESHOLD, 0.0f, 1.0f, "The center and radius of a city define a sphere. " +
                "This threshold indicates from which point a city is considered a city. " +
                "This is important for calculating where cities are based on overlapping city circles (where the city thresholds are added)");
        CITY_STYLE_THRESHOLD = cfg.getFloat("cityStyleThreshold", LostCityProfile.CATEGORY_CITIES, CITY_STYLE_THRESHOLD, 0.0f, 1.0f, "A city factor below this threshold will use the city style " +
                "specified in 'cityStyleAlternative'");
        CITY_STYLE_ALTERNATIVE = cfg.getString("cityStyleAlternative", LostCityProfile.CATEGORY_CITIES, CITY_STYLE_ALTERNATIVE, "Alternative city style. Used with cityStyleThreshold");
        CITY_AVOID_VOID = cfg.getBoolean("cityAvoidVoid", LostCityProfile.CATEGORY_CITIES, CITY_AVOID_VOID, "Only used with floating landscape type: if true an additional detection is done to see if the chunk is void and in that case the city isn't generated there. Otherwise you might get city chunks on the border of islands which sometimes looks weird");

        CITY_LEVEL0_HEIGHT = cfg.getInt("cityLevel0Height", LostCityProfile.CATEGORY_CITIES, CITY_LEVEL0_HEIGHT, 1, 384,
                "Below this chunk height cities will be level 0");
        CITY_LEVEL1_HEIGHT = cfg.getInt("cityLevel1Height", LostCityProfile.CATEGORY_CITIES, CITY_LEVEL1_HEIGHT, 1, 384,
                "Below this chunk height cities will be level 1");
        CITY_LEVEL2_HEIGHT = cfg.getInt("cityLevel2Height", LostCityProfile.CATEGORY_CITIES, CITY_LEVEL2_HEIGHT, 1, 384,
                "Below this chunk height cities will be level 2");
        CITY_LEVEL3_HEIGHT = cfg.getInt("cityLevel3Height", LostCityProfile.CATEGORY_CITIES, CITY_LEVEL3_HEIGHT, 1, 384,
                "Below this chunk height cities will be level 3");
        CITY_LEVEL4_HEIGHT = cfg.getInt("cityLevel4Height", LostCityProfile.CATEGORY_CITIES, CITY_LEVEL4_HEIGHT, 1, 384,
                "Below this chunk height cities will be level 4");
        CITY_LEVEL5_HEIGHT = cfg.getInt("cityLevel5Height", LostCityProfile.CATEGORY_CITIES, CITY_LEVEL5_HEIGHT, 1, 384,
                "Below this chunk height cities will be level 5");
        CITY_LEVEL6_HEIGHT = cfg.getInt("cityLevel6Height", LostCityProfile.CATEGORY_CITIES, CITY_LEVEL6_HEIGHT, 1, 384,
                "Below this chunk height cities will be level 6");
        CITY_LEVEL7_HEIGHT = cfg.getInt("cityLevel7Height", LostCityProfile.CATEGORY_CITIES, CITY_LEVEL7_HEIGHT, 1, 384,
                "Below this chunk height cities will be level 7");
        CITY_MINHEIGHT = cfg.getInt("cityMinHeight", LostCityProfile.CATEGORY_CITIES, CITY_MINHEIGHT, -1024, 2048,
                "Below this height cities will not be generated");
        CITY_MAXHEIGHT = cfg.getInt("cityMaxHeight", LostCityProfile.CATEGORY_CITIES, CITY_MAXHEIGHT, -1024, 2048,
                "Above this height cities will not be generated");

        OCEAN_CORRECTION_BORDER = cfg.getInt("oceanCorrectionBorder", LostCityProfile.CATEGORY_CITIES, OCEAN_CORRECTION_BORDER, -255, 255,
                "Terrain correction offset that is used for chunks adjacent to city chunks that are in ocean biomes");
    }

    private void initExplosions(Configuration cfg) {
        DEBRIS_TO_NEARBYCHUNK_FACTOR = cfg.getInt("debrisToNearbyChunkFactor", LostCityProfile.CATEGORY_EXPLOSIONS, DEBRIS_TO_NEARBYCHUNK_FACTOR, 1, 10000, "A factor that determines how much debris will overflow from nearby damaged chunks. Bigger numbers mean less debris");

        EXPLOSION_CHANCE = cfg.getFloat("explosionChance", LostCityProfile.CATEGORY_EXPLOSIONS, EXPLOSION_CHANCE, 0.0f, 1.0f, "The chance that a chunk will contain an explosion");
        EXPLOSION_MINRADIUS = cfg.getInt("explosionMinRadius", LostCityProfile.CATEGORY_EXPLOSIONS, EXPLOSION_MINRADIUS, 1, 1000, "The minimum radius of an explosion");
        EXPLOSION_MAXRADIUS = cfg.getInt("explosionMaxRadius", LostCityProfile.CATEGORY_EXPLOSIONS, EXPLOSION_MAXRADIUS, 1, 3000, "The maximum radius of an explosion");
        EXPLOSION_MINHEIGHT = cfg.getInt("explosionMinHeight", LostCityProfile.CATEGORY_EXPLOSIONS, EXPLOSION_MINHEIGHT, 1, 256, "The minimum height of an explosion");
        EXPLOSION_MAXHEIGHT = cfg.getInt("explosionMaxHeight", LostCityProfile.CATEGORY_EXPLOSIONS, EXPLOSION_MAXHEIGHT, 1, 256, "The maximum height of an explosion");

        MINI_EXPLOSION_CHANCE = cfg.getFloat("miniExplosionChance", LostCityProfile.CATEGORY_EXPLOSIONS, MINI_EXPLOSION_CHANCE, 0.0f, 1.0f, "The chance that a chunk will contain a mini explosion");
        MINI_EXPLOSION_MINRADIUS = cfg.getInt("miniExplosionMinRadius", LostCityProfile.CATEGORY_EXPLOSIONS, MINI_EXPLOSION_MINRADIUS, 1, 1000, "The minimum radius of a mini explosion");
        MINI_EXPLOSION_MAXRADIUS = cfg.getInt("miniExplosionMaxRadius", LostCityProfile.CATEGORY_EXPLOSIONS, MINI_EXPLOSION_MAXRADIUS, 1, 3000, "The maximum radius of a mini explosion");
        MINI_EXPLOSION_MINHEIGHT = cfg.getInt("miniExplosionMinHeight", LostCityProfile.CATEGORY_EXPLOSIONS, MINI_EXPLOSION_MINHEIGHT, 1, 256, "The minimum height of a mini explosion");
        MINI_EXPLOSION_MAXHEIGHT = cfg.getInt("miniExplosionMaxHeight", LostCityProfile.CATEGORY_EXPLOSIONS, MINI_EXPLOSION_MAXHEIGHT, 1, 256, "The maximum height of a mini explosion");

        EXPLOSIONS_IN_CITIES_ONLY = cfg.getBoolean("explosionsInCitiesOnly", LostCityProfile.CATEGORY_EXPLOSIONS, EXPLOSIONS_IN_CITIES_ONLY,
                "If this is true the center of an explosion can only be in a city (the blast can still affect non-city chunks)");
    }

    public String getName() {
        return name;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getExtraDescription() {
        return extraDescription;
    }

    public void setExtraDescription(String extraDescription) {
        this.extraDescription = extraDescription;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    @Override
    public void setWorldStyle(String worldStyle) {
        this.worldStyle = worldStyle;
    }

    public String getWorldStyle() {
        return worldStyle;
    }

    @Override
    public void setCityChancle(double chance) {
        this.CITY_CHANCE = chance;
    }

    @Override
    public void setRuinChance(float chance, float minPercent, float maxPercent) {
        this.RUIN_CHANCE = chance;
        this.RUIN_MINLEVEL_PERCENT = minPercent;
        this.RUIN_MAXLEVEL_PERCENT = maxPercent;
    }

    @Override
    public void setGroundLevel(int level) {
        this.GROUNDLEVEL = level;
    }

    @Override
    public void setCityLevelHeights(int l0, int l1, int l2, int l3) {
        this.CITY_LEVEL0_HEIGHT = l0;
        this.CITY_LEVEL1_HEIGHT = l1;
        this.CITY_LEVEL2_HEIGHT = l2;
        this.CITY_LEVEL3_HEIGHT = l3;
        this.CITY_LEVEL4_HEIGHT = l3 + 30;
        this.CITY_LEVEL5_HEIGHT = l3 + 40;
        this.CITY_LEVEL6_HEIGHT = l3 + 50;
        this.CITY_LEVEL7_HEIGHT = l3 + 60;
    }

    @Override
    public void setCityLevelHeights(int l0, int l1, int l2, int l3, int l4, int l5, int l6, int l7) {
        this.CITY_LEVEL0_HEIGHT = l0;
        this.CITY_LEVEL1_HEIGHT = l1;
        this.CITY_LEVEL2_HEIGHT = l2;
        this.CITY_LEVEL3_HEIGHT = l3;
        this.CITY_LEVEL4_HEIGHT = l4;
        this.CITY_LEVEL5_HEIGHT = l5;
        this.CITY_LEVEL6_HEIGHT = l6;
        this.CITY_LEVEL7_HEIGHT = l7;
    }

    @Override
    public void setOceanCorrectionBorder(int border) {
        this.OCEAN_CORRECTION_BORDER = border;
    }

    public boolean isDefault() {
        return LANDSCAPE_TYPE == LandscapeType.DEFAULT;
    }

    public boolean isFloating() {
        return LANDSCAPE_TYPE == LandscapeType.FLOATING;
    }

    public boolean isSpace() {
        return LANDSCAPE_TYPE == LandscapeType.SPACE;
    }

    public boolean isCavern() { return LANDSCAPE_TYPE == LandscapeType.CAVERN || LANDSCAPE_TYPE == LandscapeType.CAVERNSPHERES; }

    public boolean isVoidSpheres() {
        return LANDSCAPE_TYPE == LandscapeType.SPHERES;
    }

    public boolean isSpheres() {
        return LANDSCAPE_TYPE == LandscapeType.SPHERES || LANDSCAPE_TYPE == LandscapeType.CAVERNSPHERES;
    }


    public BlockState getLiquidBlock() {
        if (liquidBlock == null) {
            Optional<Holder.Reference<Block>> b = BuiltInRegistries.BLOCK.get(Identifier.parse(LIQUID_BLOCK));
            if (b.isEmpty()) {
                ModSetup.getLogger().error("Bad liquid block: {}!", LIQUID_BLOCK);
                liquidBlock = Blocks.WATER.defaultBlockState();
            } else {
                liquidBlock = b.get().value().defaultBlockState();
            }
        }
        return liquidBlock;
    }

    public BlockState getBaseBlock() {
        if (baseBlock == null) {
            Optional<Holder.Reference<Block>>  b = BuiltInRegistries.BLOCK.get(Identifier.parse(BASE_BLOCK));
            if (b.isEmpty()) {
                ModSetup.getLogger().error("Bad base block: {}!", BASE_BLOCK);
                baseBlock = Blocks.STONE.defaultBlockState();
            } else {
                baseBlock = b.get().value().defaultBlockState();
            }
        }
        return baseBlock;
    }

    public void copyFrom(LostCityProfile other) {
        Configuration config = new Configuration();
        other.init(config);
        copyFromConfiguration(config);
    }

    public JsonObject toJson(boolean readonly) {
        Configuration config = new Configuration();
        init(config);
        JsonObject root = config.toJson(readonly);
        root.addProperty("public", isPublic);
        return root;
    }

    public void toBytes(FriendlyByteBuf buf) {
        JsonObject jsonObject = toJson(false);
        buf.writeUtf(jsonObject.toString());
    }

    public Configuration toConfiguration() {
        Configuration config = new Configuration();
        init(config);
        return config;
    }

    public void copyFromConfiguration(Configuration config) {
        init(config);
        icon = null;
        liquidBlock = null;
        baseBlock = null;
    }
}
