package mcjty.lostcities.setup;

import com.google.common.collect.Lists;
import mcjty.lostcities.LostCities;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.config.ProfileSetup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {

    public static final String CATEGORY_PROFILES = "profiles";
    public static final String CATEGORY_GENERAL = "general";
    public static final boolean DEBUG = false;

    public static ForgeConfigSpec.ConfigValue<String> SPECIAL_BED_BLOCK;// = "minecraft:diamond_block";

    private static final String[] DEFAULT_DIMENSION_PROFILES = new String[] {
            "lostcities:lostcity=default"
    };
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> DIMENSION_PROFILES;
    private static Map<ResourceKey<Level>, String> dimensionProfileCache = null;

    // Profile as selected by the client
    public static String profileFromClient = null;
    public static String jsonFromClient = null;
    public static final ForgeConfigSpec.ConfigValue<String> SELECTED_PROFILE;
    public static final ForgeConfigSpec.ConfigValue<String> SELECTED_CUSTOM_JSON;
    public static final ForgeConfigSpec.IntValue TODO_QUEUE_SIZE;
    public static final ForgeConfigSpec.BooleanValue FORCE_SAPLING_GROWTH;

    public static void reset() {
        profileFromClient = null;
        jsonFromClient = null;
        dimensionProfileCache = null;
    }

    public static void resetProfileCache() {
        dimensionProfileCache = null;
    }

    // @todo BAD
    public static void registerLostCityDimension(ResourceKey<Level> type, String profile) {
        String profileForDimension = getProfileForDimension(type);
        if (profileForDimension == null) {
            dimensionProfileCache.put(type, profile);
        }
    }

    public static String getProfileForDimension(ResourceKey<Level> type) {
        if (dimensionProfileCache == null) {
            dimensionProfileCache = new HashMap<>();
            for (String dp : DIMENSION_PROFILES.get()) {
                String[] split = dp.split("=");
                if (split.length != 2) {
                    LostCities.getLogger().error("Bad format for config value: '{}'!", dp);
                } else {
                    ResourceKey<Level> dimensionType = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(split[0]));
                    String profileName = split[1];
                    LostCityProfile profile = ProfileSetup.STANDARD_PROFILES.get(profileName);
                    if (profile != null) {
                        dimensionProfileCache.put(dimensionType, profileName);
                    } else {
                        LostCities.getLogger().error("Cannot find profile: {} for dimension {}!", profileName, split[0]);
                    }
                }
            }
            String selectedProfile = Config.SELECTED_PROFILE.get();
            if ("<CHECK>".equals(selectedProfile)) {
                if (Config.profileFromClient != null && !Config.profileFromClient.isEmpty()) {
                    Config.SELECTED_PROFILE.set(Config.profileFromClient);
                    if (Config.jsonFromClient != null && !Config.jsonFromClient.isEmpty()) {
                        Config.SELECTED_CUSTOM_JSON.set(Config.jsonFromClient);
                    } else {
                        Config.SELECTED_CUSTOM_JSON.set("");
                    }
                    selectedProfile = Config.profileFromClient;
                } else {
                    Config.SELECTED_PROFILE.set("");
                    selectedProfile = "";
                }
            }
            if (!selectedProfile.isEmpty()) {
                dimensionProfileCache.put(Level.OVERWORLD, selectedProfile);
                String json = Config.SELECTED_CUSTOM_JSON.get();
                if (json != null && !json.isEmpty()) {
                    LostCityProfile profile = new LostCityProfile("customized", json);
                    if (!ProfileSetup.STANDARD_PROFILES.containsKey("customized")) {
                        ProfileSetup.STANDARD_PROFILES.put("customized", new LostCityProfile("customized", false));
                    }
                    ProfileSetup.STANDARD_PROFILES.get("customized").copyFrom(profile);
                }
            }

            String profile = getProfileForDimension(Level.OVERWORLD);
            if (profile != null && !profile.isEmpty()) {
                if (ProfileSetup.STANDARD_PROFILES.get(profile).GENERATE_NETHER) {
                    dimensionProfileCache.put(Level.NETHER, "cavern");
                }
            }
        }
        return dimensionProfileCache.get(type);
    }

    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();

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

        SELECTED_PROFILE = SERVER_BUILDER.define("selectedProfile", "<CHECK>"); // Default is dummy value that tells the system to check in profileFromClient
        SELECTED_CUSTOM_JSON = SERVER_BUILDER.define("selectedCustomJson", "");
        TODO_QUEUE_SIZE = SERVER_BUILDER.comment("The size of the todo queues for the lost city generator").defineInRange("todoQueueSize", 20, 1, 100000);
        FORCE_SAPLING_GROWTH = SERVER_BUILDER.comment("If this is true then saplings will grow into trees during generation. This is more expensive").define("forceSaplingGrowth", true);

        SERVER_BUILDER.pop();
        COMMON_BUILDER.pop();
        CLIENT_BUILDER.pop();

        COMMON_CONFIG = COMMON_BUILDER.build();
        CLIENT_CONFIG = CLIENT_BUILDER.build();
        SERVER_CONFIG = SERVER_BUILDER.build();
    }

    public static final ForgeConfigSpec COMMON_CONFIG;
    public static final ForgeConfigSpec CLIENT_CONFIG;
    public static final ForgeConfigSpec SERVER_CONFIG;

}
