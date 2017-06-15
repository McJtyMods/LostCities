package mcjty.lostcities.proxy;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.config.LostCityConfiguration;
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

public abstract class CommonProxy {

    public static File modConfigDir;
    private Configuration mainConfig;

    public void preInit(FMLPreInitializationEvent e) {
        modConfigDir = e.getModConfigurationDirectory();
        mainConfig = new Configuration(new File(modConfigDir.getPath(), "lostcities.cfg"));
        readMainConfig();
        ModDimensions.init();
    }

    private void readMainConfig() {
        Configuration cfg = mainConfig;
        try {
            cfg.load();
            LostCityConfiguration.init(cfg);
        } catch (Exception e1) {
            FMLLog.log(Level.ERROR, e1, "Problem loading config file!");
        } finally {
            if (mainConfig.hasChanged()) {
                mainConfig.save();
            }
        }
    }

    public void init(FMLInitializationEvent e) {
    }

    public void postInit(FMLPostInitializationEvent e) {
        if (mainConfig.hasChanged()) {
            mainConfig.save();
        }

        mainConfig = null;


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
    }

}
