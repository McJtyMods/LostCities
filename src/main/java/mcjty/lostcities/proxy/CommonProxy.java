package mcjty.lostcities.proxy;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.dimensions.ModDimensions;
import mcjty.lostcities.dimensions.world.lost.cityassets.AssetRegistries;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class CommonProxy {

    public static File modConfigDir;
    private Configuration mainConfig;
    private List<Configuration> profileConfigs = new ArrayList<>();

    public void preInit(FMLPreInitializationEvent e) {
        modConfigDir = e.getModConfigurationDirectory();
        mainConfig = new Configuration(new File(modConfigDir.getPath() + File.separator + "lostcities", "general.cfg"));
        readMainConfig();
        ModDimensions.init();
    }

    private void readMainConfig() {
        Configuration cfg = mainConfig;
        try {
            cfg.load();
            String[] profileList = LostCityConfiguration.init(cfg);

            for (String name : profileList) {
                LostCityProfile profile = new LostCityProfile(name, LostCityConfiguration.standardProfiles.get(name));
                Configuration profileCfg = new Configuration(new File(modConfigDir.getPath() + File.separator + "lostcities", "profile_" + name + ".cfg"));
                profileCfg.load();
                profile.init(profileCfg);
                LostCityConfiguration.profiles.put(name, profile);
                profileConfigs.add(profileCfg);
            }

        } catch (Exception e1) {
            FMLLog.log(Level.ERROR, e1, "Problem loading config file!");
        } finally {
            saveConfigs();
        }
    }

    private void saveConfigs() {
        if (mainConfig.hasChanged()) {
            mainConfig.save();
        }
        for (Configuration config : profileConfigs) {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }

    public void init(FMLInitializationEvent e) {
    }

    public void postInit(FMLPostInitializationEvent e) {
        saveConfigs();
        mainConfig = null;
        profileConfigs.clear();

        AssetRegistries.reset();
        for (String path : LostCityConfiguration.ASSETS) {
            if (path.startsWith("/")) {
                InputStream inputstream = LostCities.class.getResourceAsStream(path);
                AssetRegistries.load(inputstream, path);
            } else if (path.startsWith("$")) {
                File file = new File(modConfigDir.getPath() + File.separator + path.substring(1));
                AssetRegistries.load(file);
            } else {
                throw new RuntimeException("Invalid path for lostcity resource in 'assets' config!");
            }
        }

        System.out.println("Asset parts loaded: " + AssetRegistries.PARTS.getCount());
    }

}
