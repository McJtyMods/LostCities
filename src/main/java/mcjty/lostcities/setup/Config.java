package mcjty.lostcities.setup;

import com.google.common.collect.Lists;
import mcjty.lostcities.LostCities;
import mcjty.lostcities.config.ProfileSetup;
import mcjty.lostcities.config.LostCityProfile;
import net.minecraft.core.Registry;
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

    public static final String[] DEFAULT_ASSETS = new String[] {
            "/assets/lostcities/citydata/conditions.json",
            "/assets/lostcities/citydata/palette.json",
            "/assets/lostcities/citydata/palette_desert.json",
            "/assets/lostcities/citydata/highwayparts.json",
            "/assets/lostcities/citydata/railparts.json",
            "/assets/lostcities/citydata/monorailparts.json",
            "/assets/lostcities/citydata/buildingparts.json",
            "/assets/lostcities/citydata/library.json",
            "$lostcities/userassets.json"
    };
    public final static ForgeConfigSpec.ConfigValue<List<? extends String>> ASSETS;

    public static final String[] BLOCKS_REQUIRING_LIGHTING_UPDATES = new String[] {
            "minecraft:glowstone",
            "minecraft:redstone_torch",
            "minecraft:lit_pumpkin",
            "minecraft:magma"
    };
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

    public static void reset() {
        profileFromClient = null;
        jsonFromClient = null;
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
                    LostCities.getLogger().error("Bad format for config value: '" + dp +"'!");
                } else {
                    ResourceKey<Level> dimensionType = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(split[0]));
                    String profileName = split[1];
                    LostCityProfile profile = ProfileSetup.standardProfiles.get(profileName);
                    if (profile != null) {
                        dimensionProfileCache.put(dimensionType, profileName);
                    } else {
                        LostCities.getLogger().error("Cannot find profile: " + profileName + " for dimension " + split[0] + "!");
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
                    if (!ProfileSetup.standardProfiles.containsKey("customized")) {
                        ProfileSetup.standardProfiles.put("customized", new LostCityProfile("customized", false));
                    }
                    ProfileSetup.standardProfiles.get("customized").copyFrom(profile);
                }
            }

            String profile = getProfileForDimension(Level.OVERWORLD);
            if (profile != null && !profile.isEmpty()) {
                if (ProfileSetup.standardProfiles.get(profile).GENERATE_NETHER) {
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

        ASSETS = COMMON_BUILDER
                .comment("A list of assets that Lost Cities will use to load city data. Paths starting with '/' are relative to the Lost City resource pack. Paths starting with '$' are relative to the main config directory")
                .defineList("assets", Lists.newArrayList(DEFAULT_ASSETS), s -> s instanceof String);

        SELECTED_PROFILE = SERVER_BUILDER.define("selectedProfile", "<CHECK>"); // Default is dummy value that tells the system to check in profileFromClient
        SELECTED_CUSTOM_JSON = SERVER_BUILDER.define("selectedCustomJson", "");

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
