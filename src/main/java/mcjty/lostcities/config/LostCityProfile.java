package mcjty.lostcities.config;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.config.Configuration;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LostCityProfile {

    public static final String CATEGORY_LOSTCITY = "lostcity";
    public static final String CATEGORY_STRUCTURES = "structures";
    public static final String CATEGORY_EXPLOSIONS = "explosions";
    public static final String CATEGORY_CITIES = "cities";

    private final String name;
    private final Optional<LostCityProfile> inheritFrom;

    private String description = "Default generation, common cities, explosions";
    private String worldStyle = "standard";

    public int DEBRIS_TO_NEARBYCHUNK_FACTOR = 200;

    public float VINE_CHANCE = 0.009f;
    public float CHANCE_OF_RANDOM_LEAFBLOCKS = .1f;

    public int GROUNDLEVEL = 71;
    public int WATERLEVEL_OFFSET = 8;
    public int WATERLEVEL = -1;

//    public int MIN_HIGHWAY_LENGTH = 10;
//    public int MAX_HIGHWAY_LENGTH = 50;
//    public float HIGHWAY_CHANCE = 1.0f/(50*5);

    public float DESTROY_LONE_BLOCKS_FACTOR = .05f;
    public float DESTROY_OR_MOVE_CHANCE = .4f;
    public int DESTROY_SMALL_SECTIONS_SIZE = 50;
    public boolean EXPLOSIONS_IN_CITIES_ONLY = true;

    public float EXPLOSION_CHANCE = .005f;
    public int EXPLOSION_MINRADIUS = 17;
    public int EXPLOSION_MAXRADIUS = 80;
    public int EXPLOSION_MINHEIGHT = 78;
    public int EXPLOSION_MAXHEIGHT = 128;

    public float MINI_EXPLOSION_CHANCE = .07f;
    public int MINI_EXPLOSION_MINRADIUS = 5;
    public int MINI_EXPLOSION_MAXRADIUS = 15;
    public int MINI_EXPLOSION_MINHEIGHT = 60;
    public int MINI_EXPLOSION_MAXHEIGHT = 100;

    public float STYLE_CHANCE_CRACKED = 0.06f;
    public float STYLE_CHANCE_MOSSY = 0.05f;

    public float CITY_CHANCE = .02f;
    public int CITY_MINRADIUS = 50;
    public int CITY_MAXRADIUS = 128;
    public float CITY_THRESSHOLD = .2f;

    public Float CITY_DEFAULT_BIOME_FACTOR = 1.0f;
    public String[] CITY_BIOME_FACTORS = new String[] { "river=0", "frozen_river=0", "ocean=.7", "frozen_ocean=.7", "deep_ocean=.4" };
    public Map<String, Float> biomeFactorMap = null;

    public float BUILDING_CHANCE = .3f;
    public int BUILDING_MINFLOORS = 0;
    public int BUILDING_MAXFLOORS = 9;
    public int BUILDING_MINFLOORS_CHANCE = 4;
    public int BUILDING_MAXFLOORS_CHANCE = 6;
    public int BUILDING_MINCELLARS = 0;
    public int BUILDING_MAXCELLARS = 4;
    public float BUILDING_DOORWAYCHANCE = .6f;
    public float LIBRARY_CHANCE = .1f;
    public float DATACENTER_CHANCE = .1f;

    public float CORRIDOR_CHANCE = .7f;
    public float BRIDGE_CHANCE = .7f;
    public float FOUNTAIN_CHANCE = .05f;
    public float BUILDING2X2_CHANCE = .03f;

    public int BEDROCK_LAYER = 1;

    public boolean GENERATE_VILLAGES = true;
    public boolean GENERATE_CAVES = true;
    public boolean GENERATE_RAVINES = true;
    public boolean GENERATE_MINESHAFTS = true;
    public boolean GENERATE_STRONGHOLDS = true;
    public boolean GENERATE_SCATTERED = true;
    public boolean GENERATE_OCEANMONUMENTS = true;

    public boolean PREVENT_VILLAGES_IN_CITIES = true;

    private String categoryLostcity;
    private String categoryStructures;
    private String categoryExplosions;
    private String categoryCities;

    public LostCityProfile(String name) {
        this.name = name;
        this.inheritFrom = Optional.empty();
    }

    public LostCityProfile(String name, LostCityProfile inheritFrom) {
        this.name = name;
        this.inheritFrom = Optional.ofNullable(inheritFrom);
    }

    public void init(Configuration cfg) {
        categoryLostcity = LostCityProfile.CATEGORY_LOSTCITY + "_" + name;
        categoryStructures = LostCityProfile.CATEGORY_STRUCTURES + "_" + name;
        categoryExplosions = LostCityProfile.CATEGORY_EXPLOSIONS + "_" + name;
        categoryCities = LostCityProfile.CATEGORY_CITIES + "_" + name;
        cfg.addCustomCategoryComment(categoryLostcity, "Settings related to the Lost City for the " + name + " profile");
        cfg.addCustomCategoryComment(categoryStructures, "Settings related to structure generation for the " + name + " profile");
        cfg.addCustomCategoryComment(categoryExplosions, "Settings related to explosions and damage for the " + name + " profile");
        cfg.addCustomCategoryComment(categoryCities, "Settings related to city generation for the " + name + " profile");

        initLostcity(cfg);
        initExplosions(cfg);
        initStructures(cfg);
        initCities(cfg);
    }

    private void initLostcity(Configuration cfg) {
        description = cfg.getString("description", categoryLostcity, inheritFrom.orElse(this).description, "The description of this profile");
        worldStyle = cfg.getString("worldStyle", categoryLostcity, inheritFrom.orElse(this).worldStyle, "The worldstyle used by this profile (defined in the assets)");

        STYLE_CHANCE_CRACKED = cfg.getFloat("styleChanceCracked", categoryLostcity, inheritFrom.orElse(this).STYLE_CHANCE_CRACKED, 0.0f, 1.0f, "The chance that a brick will be cracked");
        STYLE_CHANCE_MOSSY = cfg.getFloat("styleChanceMossy", categoryLostcity, inheritFrom.orElse(this).STYLE_CHANCE_MOSSY, 0.0f, 1.0f, "The chance that a brick will be mossy");

        VINE_CHANCE = cfg.getFloat("vineChance", categoryLostcity, inheritFrom.orElse(this).VINE_CHANCE, 0.0f, 1.0f, "The chance that a block on the outside of a building will be covered with a vine");
        CHANCE_OF_RANDOM_LEAFBLOCKS = cfg.getFloat("randomLeafBlockChance", categoryLostcity, inheritFrom.orElse(this).CHANCE_OF_RANDOM_LEAFBLOCKS, 0.0f, 1.0f, "Chance that leafblocks will be generated at the border of a building and a street");

        GROUNDLEVEL = cfg.getInt("groundLevel", categoryLostcity, inheritFrom.orElse(this).GROUNDLEVEL, 2, 256, "Ground level");
        WATERLEVEL_OFFSET = cfg.getInt("waterLevelOffset", categoryLostcity, inheritFrom.orElse(this).WATERLEVEL_OFFSET, 1, 30, "How much lower the water level is compared to the ground level (63)");
        WATERLEVEL = GROUNDLEVEL - WATERLEVEL_OFFSET;

        BUILDING_CHANCE = cfg.getFloat("buildingChance", categoryLostcity, inheritFrom.orElse(this).BUILDING_CHANCE, 0.0f, 1.0f, "The chance that a chunk in a city will have a building. Otherwise it will be a street");
        BUILDING_MINFLOORS = cfg.getInt("buildingMinFloors", categoryLostcity, inheritFrom.orElse(this).BUILDING_MINFLOORS, 0, 30, "The minimum number of floors (above ground) for a building (0 means the first floor only)");
        BUILDING_MAXFLOORS = cfg.getInt("buildingMaxFloors", categoryLostcity, inheritFrom.orElse(this).BUILDING_MAXFLOORS, 0, 30, "A cap for the amount of floors a city can have (above ground)");
        BUILDING_MINFLOORS_CHANCE = cfg.getInt("buildingMinFloorsChance", categoryLostcity, inheritFrom.orElse(this).BUILDING_MINFLOORS_CHANCE, 1, 30, "The amount of floors of a building is equal to: " +
                "MINFLOORS + random(MINFLOORS_CHANCE + (cityFactor + .1f) * (MAXFLOORS_CHANCE - MINFLOORS_CHANCE))");
        BUILDING_MAXFLOORS_CHANCE = cfg.getInt("buildingMaxFloorsChance", categoryLostcity, inheritFrom.orElse(this).BUILDING_MAXFLOORS_CHANCE, 1, 30, "The amount of floors of a building is equal to: " +
                "MINFLOORS + random(MINFLOORS_CHANCE + (cityFactor + .1f) * (MAXFLOORS_CHANCE - MINFLOORS_CHANCE))");

        BUILDING_MINCELLARS = cfg.getInt("buildingMinCellars", categoryLostcity, inheritFrom.orElse(this).BUILDING_MINCELLARS, 0, 7, "The minimum number of cellars (below ground). 0 means no cellar");
        BUILDING_MAXCELLARS = cfg.getInt("buildingMaxCellars", categoryLostcity, inheritFrom.orElse(this).BUILDING_MAXCELLARS, 0, 7, "The maximum number of cellars (below ground). 0 means no cellar");
        BUILDING_DOORWAYCHANCE = cfg.getFloat("buildingDoorwayChance", categoryLostcity, inheritFrom.orElse(this).BUILDING_DOORWAYCHANCE, 0.0f, 1.0f, "The chance that a doorway will be generated at a side of a building (on any level). Only when possible");
        LIBRARY_CHANCE = cfg.getFloat("libraryChance", categoryLostcity, inheritFrom.orElse(this).LIBRARY_CHANCE, 0.0f, 1.0f, "The chance that a 2x2 building will be a library");
        DATACENTER_CHANCE = cfg.getFloat("dataCenterChance", categoryLostcity, inheritFrom.orElse(this).DATACENTER_CHANCE, 0.0f, 1.0f, "The chance that a 2x2 building will be a data center");

        BUILDING2X2_CHANCE = cfg.getFloat("building2x2Chance", categoryLostcity, inheritFrom.orElse(this).BUILDING2X2_CHANCE, 0.0f, 1.0f, "The chance that a chunk can possibly be the top-left chunk of 2x2 building. " +
                "There actually being a 2x2 building also depends on the condition of those other chunks");
        CORRIDOR_CHANCE = cfg.getFloat("corridorChance", categoryLostcity, inheritFrom.orElse(this).CORRIDOR_CHANCE, 0.0f, 1.0f, "The chance that a chunk can possibly contain a corridor. " +
                "There actually being a corridor also depends on the presence of adjacent corridors");
        BRIDGE_CHANCE = cfg.getFloat("bridgeChance", categoryLostcity, inheritFrom.orElse(this).BRIDGE_CHANCE, 0.0f, 1.0f, "The chance that a chunk can possibly contain a bridge. " +
                "There actually being a bridge also depends on the presence of adjacent bridges and other conditions");

        FOUNTAIN_CHANCE = cfg.getFloat("fountainChance", categoryLostcity, inheritFrom.orElse(this).FOUNTAIN_CHANCE, 0.0f, 1.0f, "The chance that a street section contains a fountain");

//        MIN_HIGHWAY_LENGTH = cfg.getInt("minHighwayLength", categoryLostcity, inheritFrom.orElse(this).MIN_HIGHWAY_LENGTH, 0, 200,
//                "The minimum length of a highway (in chunks)");
//        MAX_HIGHWAY_LENGTH = cfg.getInt("maxHighwayLength", categoryLostcity, inheritFrom.orElse(this).MAX_HIGHWAY_LENGTH, 0, 1000,
//                "The maximum length of a highway (in chunks). Set to 0 to disable highways");

        BEDROCK_LAYER = cfg.getInt("bedrockLayer", categoryLostcity, inheritFrom.orElse(this).BEDROCK_LAYER, 0, 10,
                "The height of the bedrock layer that is generated at the bottom of some world types. Set to 0 to disable this and get default bedrock generation");
    }

    private void initCities(Configuration cfg) {
        CITY_CHANCE = cfg.getFloat("cityChance", categoryCities, inheritFrom.orElse(this).CITY_CHANCE, 0.0f, 1.0f, "The chance this chunk will be the center of a city");
        CITY_MINRADIUS = cfg.getInt("cityMinRadius", categoryCities, inheritFrom.orElse(this).CITY_MINRADIUS, 1, 10000, "The minimum radius of a city");
        CITY_MAXRADIUS = cfg.getInt("cityMaxRadius", categoryCities, inheritFrom.orElse(this).CITY_MAXRADIUS, 1, 10000, "The maximum radius of a city");
        CITY_THRESSHOLD = cfg.getFloat("cityThresshold", categoryCities, inheritFrom.orElse(this).CITY_THRESSHOLD, 0.0f, 1.0f, "The center and radius of a city define a sphere. " +
                "This thresshold indicates from which point a city is considered a city. " +
                "This is important for calculating where cities are based on overlapping city circles (where the city thressholds are added)");
        CITY_BIOME_FACTORS = cfg.getStringList("cityBiomeFactors", categoryCities, inheritFrom.orElse(this).CITY_BIOME_FACTORS, "List of biomes with a factor to affect the city factor in that biome. Using the value 0 you can disable city generation in biomes");
        CITY_DEFAULT_BIOME_FACTOR = cfg.getFloat("cityBiomeFactorDefault", categoryCities, inheritFrom.orElse(this).CITY_DEFAULT_BIOME_FACTOR, 0.0f, 1.0f, "The default biome factor which is used if your biome is not specified in 'cityBiomeFactors'");
    }

    private void initExplosions(Configuration cfg) {
        DEBRIS_TO_NEARBYCHUNK_FACTOR = cfg.getInt("debrisToNearbyChunkFactor", categoryExplosions, inheritFrom.orElse(this).DEBRIS_TO_NEARBYCHUNK_FACTOR, 1, 10000, "A factor that determines how much debris will overflow from nearby damaged chunks. Bigger numbers mean less debris");

        DESTROY_LONE_BLOCKS_FACTOR = cfg.getFloat("destroyLoneBlocksFactor", categoryExplosions, inheritFrom.orElse(this).DESTROY_LONE_BLOCKS_FACTOR, 0.0f, 1.0f, "When a section of blocks in in an explosion the generator will count the number of " +
                "blocks that are connected. The number of connections divided by the total number of blocks in a connected section is compared with this number. " +
                "If it is smaller then the section of blocks is destroyed or moved down with gravity");
        DESTROY_OR_MOVE_CHANCE = cfg.getFloat("destroyOrMoveChance", categoryExplosions, inheritFrom.orElse(this).DESTROY_OR_MOVE_CHANCE, 0.0f, 1.0f, "When a section of blocks is to be moved or destroyed " +
                "this chance gives the chance of removal (as opposed to moving with gravity)");
        DESTROY_SMALL_SECTIONS_SIZE = cfg.getInt("destroySmallSectionsSize", categoryExplosions, inheritFrom.orElse(this).DESTROY_SMALL_SECTIONS_SIZE, 1, 5000, "A section of blocks that is about to be moved or destroyed " +
                "is always destroyed if it is smaller then this size");

        EXPLOSION_CHANCE = cfg.getFloat("explosionChance", categoryExplosions, inheritFrom.orElse(this).EXPLOSION_CHANCE, 0.0f, 1.0f, "The chance that a chunk will contain an explosion");
        EXPLOSION_MINRADIUS = cfg.getInt("explosionMinRadius", categoryExplosions, inheritFrom.orElse(this).EXPLOSION_MINRADIUS, 1, 1000, "The minimum radius of an explosion");
        EXPLOSION_MAXRADIUS = cfg.getInt("explosionMaxRadius", categoryExplosions, inheritFrom.orElse(this).EXPLOSION_MAXRADIUS, 1, 3000, "The maximum radius of an explosion");
        EXPLOSION_MINHEIGHT = cfg.getInt("explosionMinHeight", categoryExplosions, inheritFrom.orElse(this).EXPLOSION_MINHEIGHT, 1, 256, "The minimum height of an explosion");
        EXPLOSION_MAXHEIGHT = cfg.getInt("explosionMaxHeight", categoryExplosions, inheritFrom.orElse(this).EXPLOSION_MAXHEIGHT, 1, 256, "The maximum height of an explosion");

        MINI_EXPLOSION_CHANCE = cfg.getFloat("miniExplosionChance", categoryExplosions, inheritFrom.orElse(this).MINI_EXPLOSION_CHANCE, 0.0f, 1.0f, "The chance that a chunk will contain a mini explosion");
        MINI_EXPLOSION_MINRADIUS = cfg.getInt("miniExplosionMinRadius", categoryExplosions, inheritFrom.orElse(this).MINI_EXPLOSION_MINRADIUS, 1, 1000, "The minimum radius of a mini explosion");
        MINI_EXPLOSION_MAXRADIUS = cfg.getInt("miniExplosionMaxRadius", categoryExplosions, inheritFrom.orElse(this).MINI_EXPLOSION_MAXRADIUS, 1, 3000, "The maximum radius of a mini explosion");
        MINI_EXPLOSION_MINHEIGHT = cfg.getInt("miniExplosionMinHeight", categoryExplosions, inheritFrom.orElse(this).MINI_EXPLOSION_MINHEIGHT, 1, 256, "The minimum height of a mini explosion");
        MINI_EXPLOSION_MAXHEIGHT = cfg.getInt("miniExplosionMaxHeight", categoryExplosions, inheritFrom.orElse(this).MINI_EXPLOSION_MAXHEIGHT, 1, 256, "The maximum height of a mini explosion");

        EXPLOSIONS_IN_CITIES_ONLY = cfg.getBoolean("explosionsInCitiesOnly", categoryExplosions, inheritFrom.orElse(this).EXPLOSIONS_IN_CITIES_ONLY,
                "If this is true the center of an explosion can only be in a city (the blast can still affect non-city chunks)");
    }

    private void initStructures(Configuration cfg) {
        GENERATE_OCEANMONUMENTS = cfg.get(categoryStructures, "generateOceanMonuments", inheritFrom.orElse(this).GENERATE_OCEANMONUMENTS, "Generate ocean monuments").getBoolean();
        GENERATE_SCATTERED = cfg.get(categoryStructures, "generateScattered", inheritFrom.orElse(this).GENERATE_SCATTERED, "Generate scattered features (swamphunts, desert temples, ...)").getBoolean();
        GENERATE_STRONGHOLDS = cfg.get(categoryStructures, "generateStrongholds", inheritFrom.orElse(this).GENERATE_STRONGHOLDS, "Generate strongholds").getBoolean();
        GENERATE_VILLAGES = cfg.get(categoryStructures, "generateVillages", inheritFrom.orElse(this).GENERATE_VILLAGES, "Generate villages").getBoolean();
        GENERATE_CAVES = cfg.get(categoryStructures, "generateCaves", inheritFrom.orElse(this).GENERATE_CAVES, "Generate caves").getBoolean();
        GENERATE_RAVINES = cfg.get(categoryStructures, "generateRavines", inheritFrom.orElse(this).GENERATE_RAVINES, "Generate ravines").getBoolean();
        GENERATE_MINESHAFTS = cfg.get(categoryStructures, "generateMineshafts", inheritFrom.orElse(this).GENERATE_MINESHAFTS, "Generate mineshafts").getBoolean();

        PREVENT_VILLAGES_IN_CITIES = cfg.get(categoryStructures, "preventVillagesInCities", inheritFrom.orElse(this).PREVENT_VILLAGES_IN_CITIES, "If true then an attempt will be made to prevent villages in cities. " +
                "Note that enabling this option will likely require a low city " +
                "density in order to actually get a reasonable chance for villages.").getBoolean();
    }

    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getWorldStyle() {
        return worldStyle;
    }

    public Map<String, Float> getBiomeFactorMap() {
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


}
