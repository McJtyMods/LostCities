package mcjty.lostcities.config;

import java.util.HashMap;
import java.util.Map;

public class ConfigSetup {

    private static Configuration mainConfig;
    public static Map<String, Configuration> profileConfigs = new HashMap<>();

    public static void init() {
        // @todo 1.14
//        mainConfig = new Configuration(new File(ModSetup.modConfigDir.getPath() + File.separator + "lostcities", "general.cfg"));
//        Configuration cfg = mainConfig;
//        try {
//            cfg.load();
//            String[] profileList = LostCityConfiguration.init(cfg);
//            initProfiles(profileList, true);
//            profileList = LostCityConfiguration.getPrivateProfiles(cfg);
//            initProfiles(profileList, false);
//
//            fixConfigs();
//        } catch (Exception e1) {
//            FMLLog.log(Level.ERROR, e1, "Problem loading config file!");
//        } finally {
//            if (mainConfig.hasChanged()) {
//                mainConfig.save();
//            }
//            for (Configuration config : profileConfigs.values()) {
//                if (config.hasChanged()) {
//                    config.save();
//                }
//            }
//        }
    }

    private static void initProfiles(String[] profileList, boolean isPublic) {
        // @todo 1.14
//        for (String name : profileList) {
//            LostCityProfile profile = new LostCityProfile(name, LostCityConfiguration.standardProfiles.get(name), isPublic);
//            Configuration profileCfg = new Configuration(new File(ModSetup.modConfigDir.getPath() + File.separator + "lostcities", "profile_" + name + ".cfg"));
//            profileCfg.load();
//            profile.init(profileCfg);
//            LostCityConfiguration.profiles.put(name, profile);
//            profileConfigs.put(name, profileCfg);
//        }
    }

    public static void postInit() {
        // @todo 1.14
//        if (mainConfig.hasChanged()) {
//            mainConfig.save();
//        }
//        for (Configuration config : profileConfigs.values()) {
//            if (config.hasChanged()) {
//                config.save();
//            }
//        }
    }
}
