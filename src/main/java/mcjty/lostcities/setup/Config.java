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
import java.util.Objects;

public class Config {
    public static final String CATEGORY_PROFILES = "profiles";

    private static final String[] defaultDimensionProfiles = new String[] {
            "minecraft:overworld=default",
            "minecraft:the_nether=cavern"
    };

    private static ForgeConfigSpec.ConfigValue<List<? extends String>> DIMENSION_PROFILES;
    private static Map<DimensionType, String> dimensionProfileCache = null;

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
        }
        return dimensionProfileCache.get(type);
    }

    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

    static {
        COMMON_BUILDER.comment("General settings").push(CATEGORY_PROFILES);
        CLIENT_BUILDER.comment("General settings").push(CATEGORY_PROFILES);

        DIMENSION_PROFILES = COMMON_BUILDER
                .comment("A list of dimensions with associated city generation profiles")
                .defineList("dimensionProfiles", Lists.newArrayList(Config.defaultDimensionProfiles), s -> s instanceof String);

        COMMON_BUILDER.pop();
        CLIENT_BUILDER.pop();

        COMMON_CONFIG = COMMON_BUILDER.build();
        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

    public static ForgeConfigSpec COMMON_CONFIG;
    public static ForgeConfigSpec CLIENT_CONFIG;

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
