package mcjty.lostcities;

import mcjty.lostcities.api.ILostCities;
import mcjty.lostcities.api.ILostCitiesPre;
import mcjty.lostcities.datagen.DataGenerators;
import mcjty.lostcities.network.PacketRequestProfile;
import mcjty.lostcities.network.PacketReturnProfileToClient;
import mcjty.lostcities.setup.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Supplier;

@Mod(LostCities.MODID)
public class LostCities {
    public static final String MODID = "lostcities";

    public static final Logger LOGGER = LogManager.getLogger(LostCities.MODID);

    public static final ModSetup setup = new ModSetup();
    public static LostCities instance;
    public static final LostCitiesImp lostCitiesImp = new LostCitiesImp();

    public LostCities(ModContainer container, IEventBus bus, Dist dist) {
        instance = this;

        Registration.init(bus);
        CustomRegistries.init(bus);

        Path configPath = FMLPaths.CONFIGDIR.get();
        File dir = new File(configPath + File.separator + "lostcities");
        dir.mkdirs();

        container.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG, "lostcities/client.toml");
        container.registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG, "lostcities/common.toml");
        container.registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);

        bus.addListener(setup::init);
        bus.addListener(this::onRegisterPayloadHandler);
        bus.addListener(this::processIMC);
        bus.addListener(this::onConstructModEvent);
        bus.addListener(CustomRegistries::onDataPackRegistry);
        bus.addListener(DataGenerators::gatherData);

        if (dist.isClient()) {
            bus.addListener(ClientSetup::init);
        }
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    private void onRegisterPayloadHandler(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MODID)
                .versioned("1.0")
                .optional();
        registrar.playToClient(PacketReturnProfileToClient.TYPE, PacketReturnProfileToClient.CODEC, PacketReturnProfileToClient::handle);
        registrar.playToServer(PacketRequestProfile.TYPE, PacketRequestProfile.CODEC, PacketRequestProfile::handle);
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
