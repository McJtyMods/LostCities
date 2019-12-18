package mcjty.lostcities;

import mcjty.lostcities.dimensions.world.WorldTypeTools;
import mcjty.lostcities.dimensions.world.lost.*;
import mcjty.lostcities.setup.ModSetup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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
        FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLCommonSetupEvent event) -> setup.init(event));
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
        WorldTypeTools.cleanCache();
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
