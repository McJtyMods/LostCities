package mcjty.lostcities.setup;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.google.common.collect.Lists;
import mcjty.lostcities.LostCities;
import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.config.LostCityProfile;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.ForgeConfigSpec;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {
    public static final String CATEGORY_PROFILES = "profiles";

    private static final String[] defaultDimensionProfiles = new String[] {
    };

    private static ForgeConfigSpec.ConfigValue<List<? extends String>> DIMENSION_PROFILES;
    private static Map<DimensionType, String> dimensionProfileCache = null;

    // Profile as selected by the client
    public static String profileFromClient = null;
    public static String jsonFromClient = null;
    public static ForgeConfigSpec.ConfigValue<String> SELECTED_PROFILE;
    public static ForgeConfigSpec.ConfigValue<String> SELECTED_CUSTOM_JSON;

    public static String getProfileForDimension(DimensionType type) {
        if (dimensionProfileCache == null) {
            dimensionProfileCache = new HashMap<>();
            for (String dp : DIMENSION_PROFILES.get()) {
                String[] split = dp.split("=");
                if (split.length != 2) {
                    LostCities.getLogger().error("Bad format for config value: '" + dp +"'!");
                } else {
                    DimensionType dimensionType = DimensionType.byName(new ResourceLocation(split[0]));
                    if (dimensionType != null) {
                        String profileName = split[1];
                        LostCityProfile profile = LostCityConfiguration.standardProfiles.get(profileName);
                        if (profile != null) {
                            dimensionProfileCache.put(dimensionType, profileName);
                        } else {
                            LostCities.getLogger().error("Cannot find profile: " + profileName + " for dimension " + split[0] + "!");
                        }
                    } else {
                        LostCities.getLogger().error("Cannot find dimension: " + split[0] + "!");
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
                dimensionProfileCache.put(DimensionType.OVERWORLD, selectedProfile);
                String json = Config.SELECTED_CUSTOM_JSON.get();
                if (json != null && !json.isEmpty()) {
                    LostCityProfile profile = new LostCityProfile("customized", json);
                    LostCityConfiguration.standardProfiles.get("customized").copyFrom(profile);
                }
            }

            String profile = getProfileForDimension(DimensionType.OVERWORLD);
            if (profile != null && !profile.isEmpty()) {
                if (LostCityConfiguration.standardProfiles.get(profile).GENERATE_NETHER) {
                    dimensionProfileCache.put(DimensionType.THE_NETHER, "cavern");
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
                .comment("A list of dimensions with associated city generation profiles")
                .defineList("dimensionProfiles", Lists.newArrayList(Config.defaultDimensionProfiles), s -> s instanceof String);

        SELECTED_PROFILE = SERVER_BUILDER.define("selectedProfile", "<CHECK>"); // Default is dummy value that tells the system to check in profileFromClient
        SELECTED_CUSTOM_JSON = SERVER_BUILDER.define("selectedCustomJson", "");

        SERVER_BUILDER.pop();
        COMMON_BUILDER.pop();
        CLIENT_BUILDER.pop();

        COMMON_CONFIG = COMMON_BUILDER.build();
        CLIENT_CONFIG = CLIENT_BUILDER.build();
        SERVER_CONFIG = SERVER_BUILDER.build();
    }

    public static ForgeConfigSpec COMMON_CONFIG;
    public static ForgeConfigSpec CLIENT_CONFIG;
    public static ForgeConfigSpec SERVER_CONFIG;

    public static void loadConfig(ForgeConfigSpec spec, Path path) {

        final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();

        configData.load();
        spec.setConfig(configData);
    }
}
