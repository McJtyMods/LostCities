package mcjty.lostcities.setup;

import com.google.common.collect.Lists;
import mcjty.lostcities.LostCities;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.config.ProfileSetup;
import mcjty.lostcities.data.LostData;
import mcjty.lostcities.worldgen.lost.regassets.data.Selectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.*;

public class Config {

    public static final String CATEGORY_PROFILES = "profiles";
    public static final String CATEGORY_GENERAL = "general";
    public static final boolean DEBUG = false;

    public static ModConfigSpec.ConfigValue<String> SPECIAL_BED_BLOCK;// = "minecraft:diamond_block";

    private static final String[] DEFAULT_DIMENSION_PROFILES = new String[] {
            "lostcities:lostcity=default"
    };
    private static final ModConfigSpec.ConfigValue<List<? extends String>> DIMENSION_PROFILES;
    private static Map<ResourceKey<Level>, String> dimensionProfileCache = null;

    // Profile as selected by the client
    public static String profileFromClient = null;
    public static String jsonFromClient = null;
    public static final ModConfigSpec.ConfigValue<String> SELECTED_PROFILE;
    public static final ModConfigSpec.ConfigValue<String> SELECTED_CUSTOM_JSON;
    public static final ModConfigSpec.IntValue TODO_QUEUE_SIZE;
    public static final ModConfigSpec.BooleanValue FORCE_SAPLING_GROWTH;

    private static final String[] DEF_AVOID_STRUCTURES = new String[] {
            "minecraft:mansion",
            "minecraft:jungle_pyramid",
            "minecraft:desert_pyramid",
            "minecraft:igloo",
            "minecraft:swamp_huts",
            "minecraft:pillager_outpost"
    };
    private static final ModConfigSpec.ConfigValue<List<? extends String>> AVOID_STRUCTURES;
    private static final Set<ResourceLocation> AVOID_STRUCTURES_SET = new HashSet<>();
    public static final ModConfigSpec.BooleanValue AVOID_STRUCTURES_ADJACENT;
    public static final ModConfigSpec.BooleanValue AVOID_VILLAGES_ADJACENT;
    public static final ModConfigSpec.BooleanValue AVOID_FLATTENING;

    public static void reset() {
        profileFromClient = null;
        jsonFromClient = null;
        dimensionProfileCache = null;
    }

    public static void resetProfileCache() {
        dimensionProfileCache = null;
    }

    // @todo BAD
    public static void registerLostCityDimension(ServerLevel level, ResourceKey<Level> type, String profile) {
        String profileForDimension = getProfileForDimension(level, type);
        if (profileForDimension == null) {
            dimensionProfileCache.put(type, profile);
        }
    }

    public static String getProfileForDimension(ServerLevel level, ResourceKey<Level> type) {
        if (dimensionProfileCache == null) {
            dimensionProfileCache = new HashMap<>();
            LostCities.getLogger().info("Creating profile cache...");
            for (String dp : DIMENSION_PROFILES.get()) {
              LostCities.getLogger().info("Creating profile cache: "+dp);
                String[] split = dp.split("=");
                if (split.length != 2) {
                    LostCities.getLogger().error("Bad format for config value: '{}'!", dp);
                } else {
                    ResourceKey<Level> dimensionType = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(split[0]));
                    String profileName = split[1];
                    LostCities.getLogger().info("Looking up : "+profileName);
                    LostCityProfile profile = ProfileSetup.STANDARD_PROFILES.get(profileName);
                    if (profile != null) {
                        dimensionProfileCache.put(dimensionType, profileName);
                    } else {
                        LostCities.getLogger().error("Cannot find profile: {} for dimension {}!", profileName, split[0]);
                    }
                }
            }

            LostCities.getLogger().info("Profile cache is: "+dimensionProfileCache);

            LostData data = LostData.getData(level);
            String selectedProfile = "";
            String selectedJson = "";
            if (Config.profileFromClient != null && !Config.profileFromClient.isEmpty()) {
                if (Config.jsonFromClient != null && !Config.jsonFromClient.isEmpty()) {
                    selectedJson = Config.jsonFromClient;
                }
                selectedProfile = Config.profileFromClient;
                // Remember the profile selected by the client in SavedData
                data.setProfile(selectedProfile, selectedJson);
            } else {
                // Check if SavedData has a profile selected
                selectedProfile = data.getSelectedProfile();
                selectedJson = data.getSelectedJson();
            }

            LostCities.getLogger().info("Selected Profile is: "+selectedProfile);
            LostCities.getLogger().info("Selected Profile JSON is: "+selectedJson);

            if (!selectedProfile.isEmpty()) {
                dimensionProfileCache.put(Level.OVERWORLD, selectedProfile);
                if (!selectedJson.isEmpty()) {
                    LostCityProfile profile = new LostCityProfile("customized", selectedJson);
                    if (!ProfileSetup.STANDARD_PROFILES.containsKey("customized")) {
                        ProfileSetup.STANDARD_PROFILES.put("customized", new LostCityProfile("customized", false));
                    }
                    ProfileSetup.STANDARD_PROFILES.get("customized").copyFrom(profile);
                }
            }

            String profile = getProfileForDimension(level, Level.OVERWORLD);
            LostCities.getLogger().info("Profile for Dimension "+Level.OVERWORLD+" is: "+profile);
            
            if (profile != null && !profile.isEmpty()) {
                if (ProfileSetup.STANDARD_PROFILES.get(profile).GENERATE_NETHER) {
                    dimensionProfileCache.put(Level.NETHER, "cavern");
                }
            }
        }
        return dimensionProfileCache.get(type);
    }

    public static boolean isAvoidedStructure(ResourceLocation id) {
        if (AVOID_STRUCTURES_SET.isEmpty()) {
            for (String s : AVOID_STRUCTURES.get()) {
                AVOID_STRUCTURES_SET.add(ResourceLocation.parse(s));
            }
        }
        return AVOID_STRUCTURES_SET.contains(id);
    }

    private static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();

    static {
        COMMON_BUILDER.comment("General settings").push(CATEGORY_PROFILES);
        CLIENT_BUILDER.comment("General settings").push(CATEGORY_PROFILES);
        SERVER_BUILDER.comment("General settings").push(CATEGORY_PROFILES);

        DIMENSION_PROFILES = COMMON_BUILDER
                .comment("A list of dimensions with associated city generation profiles (format <dimensionid>=<profilename>")
                .defineList("dimensionsWithProfiles", Lists.newArrayList(Config.DEFAULT_DIMENSION_PROFILES), s -> s instanceof String);

        SPECIAL_BED_BLOCK = SERVER_BUILDER
                .comment("Block to put underneath a bed so that it qualifies as a teleporter bed")
                .define("specialBedBlock", "minecraft:diamond_block");

        SELECTED_PROFILE = SERVER_BUILDER.define("selectedProfile", "");
        SELECTED_CUSTOM_JSON = SERVER_BUILDER.define("selectedCustomJson", "");
        TODO_QUEUE_SIZE = SERVER_BUILDER.comment("The size of the todo queues for the lost city generator").defineInRange("todoQueueSize", 20, 1, 100000);
        FORCE_SAPLING_GROWTH = SERVER_BUILDER.comment("If this is true then saplings will grow into trees during generation. This is more expensive").define("forceSaplingGrowth", true);
        AVOID_STRUCTURES = SERVER_BUILDER
                .comment("List of structures to avoid when generating cities (for example to avoid generating a city in a woodland mansion)")
                .defineList("avoidStructures", Lists.newArrayList(DEF_AVOID_STRUCTURES), s -> s instanceof String);
        AVOID_STRUCTURES_ADJACENT = SERVER_BUILDER
                .comment("If true then also avoid generating the structures mentioned in 'avoidStructures' in chunks adjacent to the chunk with the structure")
                .define("avoidStructuresAdjacent", false);
        AVOID_VILLAGES_ADJACENT = SERVER_BUILDER
                .comment("If true then also avoid generating cities in chunks adjacent to the chunks with villages")
                .define("avoidVillagesAdjacent", false);
        AVOID_FLATTENING = SERVER_BUILDER
                .comment("If true then avoid flattening the terrain around the city in case there was a structure that was avoided")
                .define("avoidFlattening", true);

        SERVER_BUILDER.pop();
        COMMON_BUILDER.pop();
        CLIENT_BUILDER.pop();

        COMMON_CONFIG = COMMON_BUILDER.build();
        CLIENT_CONFIG = CLIENT_BUILDER.build();
        SERVER_CONFIG = SERVER_BUILDER.build();
    }

    public static final ModConfigSpec COMMON_CONFIG;
    public static final ModConfigSpec CLIENT_CONFIG;
    public static final ModConfigSpec SERVER_CONFIG;

}
