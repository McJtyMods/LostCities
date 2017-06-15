package mcjty.lostcities.config;

import net.minecraftforge.common.config.Configuration;

import java.util.HashMap;
import java.util.Map;

public class LostCityConfiguration {

    public static final String CATEGORY_GENERAL = "general";

    public static String[] ASSETS = new String[] {
            "/assets/lostcities/citydata/palette.json",
            "/assets/lostcities/citydata/palette_desert.json",
            "/assets/lostcities/citydata/highwayparts.json",
            "/assets/lostcities/citydata/library.json",
            "$lostcities/userassets.json"
    };

    public static final Map<String, LostCityProfile> profiles = new HashMap<>();
    public static final Map<String, LostCityProfile> standardProfiles = new HashMap<>();

    public static void init(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_GENERAL, "General settings");

        ASSETS = cfg.getStringList("assets", CATEGORY_GENERAL, ASSETS, "List of asset libraries loaded in the specified order. " +
                "If the path starts with '/' it is going to be loaded directly from the classpath. If the path starts with '$' it is loaded from the config directory");

        initStandardProfiles();
        String[] profileList = cfg.getStringList("profiles", CATEGORY_GENERAL,
                new String[]{"default", "nodamage", "rarecities", "onlycities", "tallbuildings"}, "List of all supported profiles (used for world creation). Warning! Make sure there is always a 'default' profile!");
        for (String name : profileList) {
            LostCityProfile profile = new LostCityProfile(name, standardProfiles.get(name));
            profile.init(cfg);
            profiles.put(name, profile);
        }
    }

    private static void initStandardProfiles() {
        LostCityProfile profile = new LostCityProfile("default");
        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("nodamage");
        profile.setDescription("Like default but no explosion damage");
        profile.EXPLOSION_CHANCE = 0;
        profile.MINI_EXPLOSION_CHANCE = 0;
        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("rarecities");
        profile.setDescription("Cities are rare");
        profile.CITY_CHANCE = 0.002f;
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
        standardProfiles.put(profile.getName(), profile);

    }

}
