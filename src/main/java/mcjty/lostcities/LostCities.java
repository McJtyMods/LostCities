package mcjty.lostcities;

import mcjty.lostcities.setup.ClientSetup;
import mcjty.lostcities.setup.Config;
import mcjty.lostcities.setup.ModSetup;
import mcjty.lostcities.worldgen.lost.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;

@Mod(LostCities.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class LostCities {
    public static final String MODID = "lostcities";

    public static Logger logger = LogManager.getLogger(LostCities.MODID);

    public static ModSetup setup = new ModSetup();
    public static LostCities instance;
    public static LostCitiesImp lostCitiesImp = new LostCitiesImp();

    public LostCities() {
        instance = this;

        Path configPath = FMLPaths.CONFIGDIR.get();
        File dir = new File(configPath + File.separator + "lostcities");
        dir.mkdirs();

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG, "lostcities/client.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG, "lostcities/common.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);

        FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLCommonSetupEvent event) -> setup.init(event));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init);
        });

//        Config.loadConfig(Config.CLIENT_CONFIG, FMLPaths.CONFIGDIR.get().resolve("lostcities-client.toml"));
//        Config.loadConfig(Config.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve("lostcities-common.toml"));
//        Config.loadConfig(Config.SERVER_CONFIG, FMLPaths.CONFIGDIR.get().resolve("lostcities-server.toml"));
    }

    public static Logger getLogger() {
        return logger;
    }

    private void cleanCaches() {
        BuildingInfo.cleanCache();
        Highway.cleanCache();
        Railway.cleanCache();
        BiomeInfo.cleanCache();
        City.cleanCache();
        CitySphere.cleanCache();
    }

    // @todo 1.14
//    @Mod.EventHandler
//    public void imcCallback(FMLInterModComms.IMCEvent event) {
//        for (FMLInterModComms.IMCMessage message : event.getMessages()) {
//            if (message.key.equalsIgnoreCase("getLostCities")) {
//                Optional<Function<ILostCities, Void>> value = message.getFunctionValue(ILostCities.class, Void.class);
//                if (value.isPresent()) {
//                    value.get().apply(lostCitiesImp);
//                } else {
//                    setup.getLogger().warn("Some mod didn't return a valid result with getLostCities!");
//                }
//            }
//        }
//    }
}
