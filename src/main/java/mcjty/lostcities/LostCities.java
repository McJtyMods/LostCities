package mcjty.lostcities;

import mcjty.lostcities.setup.ClientSetup;
import mcjty.lostcities.setup.Config;
import mcjty.lostcities.setup.ModSetup;
import mcjty.lostcities.worldgen.lost.*;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.Logger;

@Mod(LostCities.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class LostCities {
    public static final String MODID = "lostcities";

    public static Logger logger = null; // @todo 1.14

    public static ModSetup setup = new ModSetup();
    public static LostCities instance;
    public static LostCitiesImp lostCitiesImp = new LostCitiesImp();

    public LostCities() {
        instance = this;

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);

        FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLCommonSetupEvent event) -> setup.init(event));
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init);

        Config.loadConfig(Config.CLIENT_CONFIG, FMLPaths.CONFIGDIR.get().resolve("lostcities-client.toml"));
        Config.loadConfig(Config.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve("lostcities-common.toml"));
        Config.loadConfig(Config.SERVER_CONFIG, FMLPaths.CONFIGDIR.get().resolve("lostcities-server.toml"));
    }

    public static Logger getLogger() {
        return logger;
    }

    // @todo 1.14
//    @Mod.EventHandler
//    public void preInit(FMLPreInitializationEvent e) {
//        setup.preInit(e);
//        proxy.preInit(e);
//    }
//
//    @Mod.EventHandler
//    public void init(FMLInitializationEvent e) {
//        setup.init(e);
//        proxy.init(e);
//    }
//
//    @Mod.EventHandler
//    public void postInit(FMLPostInitializationEvent e) {
//        setup.postInit(e);
//        proxy.postInit(e);
//    }
//
//    @Mod.EventHandler
//    public void serverLoad(FMLServerStartingEvent event) {
//        event.registerServerCommand(new CommandDebug());
//        event.registerServerCommand(new CommandExportBuilding());
//        event.registerServerCommand(new CommandExportPart());
//        event.registerServerCommand(new CommandBuildPart());
//        cleanCaches();
//    }
//
//    @Mod.EventHandler
//    public void serverStopped(FMLServerStoppedEvent event) {
//        cleanCaches();
//        WorldTypeTools.cleanChunkGeneratorMap();
//    }

    private void cleanCaches() {
        BuildingInfo.cleanCache();
        Highway.cleanCache();
        Railway.cleanCache();
        BiomeInfo.cleanCache();
        City.cleanCache();
        CitySphere.cleanCache();
//        WorldTypeTools.cleanCache();
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
