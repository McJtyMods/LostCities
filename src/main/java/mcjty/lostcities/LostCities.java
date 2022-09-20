package mcjty.lostcities;

import mcjty.lostcities.api.ILostCities;
import mcjty.lostcities.api.ILostCitiesPre;
import mcjty.lostcities.setup.ClientSetup;
import mcjty.lostcities.setup.Config;
import mcjty.lostcities.setup.CustomRegistries;
import mcjty.lostcities.setup.ModSetup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Supplier;

@Mod(LostCities.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class LostCities {
    public static final String MODID = "lostcities";

    public static final Logger LOGGER = LogManager.getLogger(LostCities.MODID);

    public static final ModSetup setup = new ModSetup();
    public static LostCities instance;
    public static final LostCitiesImp lostCitiesImp = new LostCitiesImp();

    public LostCities() {
        instance = this;

        CustomRegistries.init();

        Path configPath = FMLPaths.CONFIGDIR.get();
        File dir = new File(configPath + File.separator + "lostcities");
        dir.mkdirs();

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG, "lostcities/client.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG, "lostcities/common.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(setup::init);
        bus.addListener(this::processIMC);
        bus.addListener(this::onConstructModEvent);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init);
        });
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    private void onConstructModEvent(FMLConstructModEvent event) {
        event.enqueueWork(() -> {
            event.getIMCStream(ILostCities.GET_LOST_CITIES_PRE::equals).forEach(message -> {
                Supplier<Function<ILostCitiesPre, Void>> supplier = message.getMessageSupplier();
                supplier.get().apply(new LostCitiesPreImp());
            });
        });
    }

    private void processIMC(final InterModProcessEvent event) {
        event.getIMCStream(ILostCities.GET_LOST_CITIES::equals).forEach(message -> {
            Supplier<Function<ILostCities, Void>> supplier = message.getMessageSupplier();
            supplier.get().apply(new LostCitiesImp());
        });
    }
}
