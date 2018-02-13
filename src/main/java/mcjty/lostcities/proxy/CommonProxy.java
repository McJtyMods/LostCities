package mcjty.lostcities.proxy;

import com.google.common.util.concurrent.ListenableFuture;
import mcjty.lostcities.ForgeEventHandlers;
import mcjty.lostcities.LostCities;
import mcjty.lostcities.TerrainEventHandlers;
import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.dimensions.ModDimensions;
import mcjty.lostcities.dimensions.world.lost.cityassets.AssetRegistries;
import mcjty.lostcities.network.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public abstract class CommonProxy {

    public static File modConfigDir;
    private Configuration mainConfig;
    private List<Configuration> profileConfigs = new ArrayList<>();

    public void preInit(FMLPreInitializationEvent e) {
        PacketHandler.registerMessages("lostcities");

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
            initProfiles(profileList, true);
            profileList = LostCityConfiguration.getPrivateProfiles(cfg);
            initProfiles(profileList, false);
        } catch (Exception e1) {
            FMLLog.log(Level.ERROR, e1, "Problem loading config file!");
        } finally {
            saveConfigs();
        }
    }

    private void initProfiles(String[] profileList, boolean isPublic) {
        for (String name : profileList) {
            LostCityProfile profile = new LostCityProfile(name, LostCityConfiguration.standardProfiles.get(name), isPublic);
            Configuration profileCfg = new Configuration(new File(modConfigDir.getPath() + File.separator + "lostcities", "profile_" + name + ".cfg"));
            profileCfg.load();
            profile.init(profileCfg);
            LostCityConfiguration.profiles.put(name, profile);
            profileConfigs.add(profileCfg);
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
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
        MinecraftForge.TERRAIN_GEN_BUS.register(new TerrainEventHandlers());
        LootTableList.register(new ResourceLocation(LostCities.MODID, "lostcitychest"));
        LootTableList.register(new ResourceLocation(LostCities.MODID, "raildungeonchest"));
    }

    public void postInit(FMLPostInitializationEvent e) {
        saveConfigs();
        mainConfig = null;
        profileConfigs.clear();

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
            LostCities.logger.info("Asset parts loaded: " + AssetRegistries.PARTS.getCount());
            AssetRegistries.showStatistics();
        }
    }

    public World getClientWorld() {
        throw new IllegalStateException("This should only be called from client side");
    }

    public EntityPlayer getClientPlayer() {
        throw new IllegalStateException("This should only be called from client side");
    }

    public <V> ListenableFuture<V> addScheduledTaskClient(Callable<V> callableToSchedule) {
        throw new IllegalStateException("This should only be called from client side");
    }

    public ListenableFuture<Object> addScheduledTaskClient(Runnable runnableToSchedule) {
        throw new IllegalStateException("This should only be called from client side");
    }
}
