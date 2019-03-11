package mcjty.lostcities.setup;

import mcjty.lostcities.ForgeEventHandlers;
import mcjty.lostcities.LostCities;
import mcjty.lostcities.TerrainEventHandlers;
import mcjty.lostcities.config.ConfigSetup;
import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.dimensions.ModDimensions;
import mcjty.lostcities.dimensions.world.lost.cityassets.AssetRegistries;
import mcjty.lostcities.network.PacketHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public class ModSetup {

    public static boolean chisel = false;
    public static boolean biomesoplenty = false;
    public static boolean atg = false;
    public static boolean neid = false;
    public static boolean jeid = false;

    private Logger logger;
    public static File modConfigDir;

    public void preInit(FMLPreInitializationEvent e) {
        logger = e.getModLog();
        PacketHandler.registerMessages("lostcities");

        setupModCompat();

        modConfigDir = e.getModConfigurationDirectory();
        ConfigSetup.init();
        ModDimensions.init();

        LootTableList.register(new ResourceLocation(LostCities.MODID, "chests/lostcitychest"));
        LootTableList.register(new ResourceLocation(LostCities.MODID, "chests/raildungeonchest"));
    }

    private void setupModCompat() {
        chisel = Loader.isModLoaded("chisel");
        biomesoplenty = Loader.isModLoaded("biomesoplenty") || Loader.isModLoaded("BiomesOPlenty");
//        atg = Loader.isModLoaded("atg"); // @todo
        neid = Loader.isModLoaded("neid");
        jeid = Loader.isModLoaded("jeid");
    }

    public Logger getLogger() {
        return logger;
    }

    public void init(FMLInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
        MinecraftForge.TERRAIN_GEN_BUS.register(new TerrainEventHandlers());
    }

    public void postInit(FMLPostInitializationEvent e) {
        ConfigSetup.postInit();
        ConfigSetup.profileConfigs.clear();

        AssetRegistries.reset();
        for (String path : LostCityConfiguration.ASSETS) {
            if (path.startsWith("/")) {
                try(InputStream inputstream = LostCities.class.getResourceAsStream(path)) {
                    AssetRegistries.load(inputstream, path);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            } else if (path.startsWith("$")) {
                File file = new File(modConfigDir.getPath() + File.separator + path.substring(1));
                AssetRegistries.load(file);
            } else {
                throw new RuntimeException("Invalid path for lostcity resource in 'assets' config!");
            }
        }

        if (LostCityConfiguration.DEBUG) {
            logger.info("Asset parts loaded: " + AssetRegistries.PARTS.getCount());
            AssetRegistries.showStatistics();
        }
    }
}
