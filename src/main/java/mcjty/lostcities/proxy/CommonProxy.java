package mcjty.lostcities.proxy;

import mcjty.lostcities.ForgeEventHandlers;
import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.dimensions.ModDimensions;
import mcjty.lostcities.dimensions.world.lost.cityassets.AssetRegistries;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Level;

import java.io.File;

public abstract class CommonProxy {

    public static File modConfigDir;
    private Configuration mainConfig;

    public void preInit(FMLPreInitializationEvent e) {
        modConfigDir = e.getModConfigurationDirectory();
        mainConfig = new Configuration(new File(modConfigDir.getPath(), "lostcities.cfg"));
        readMainConfig();

//        SimpleNetworkWrapper network = PacketHandler.registerMessages(LostCities.MODID, "lostcities");
//        RFToolsDimMessages.registerNetworkMessages(network);
        AssetRegistries.init();
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
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
    }

    public void postInit(FMLPostInitializationEvent e) {
        if (mainConfig.hasChanged()) {
            mainConfig.save();
        }

        mainConfig = null;
    }

}
