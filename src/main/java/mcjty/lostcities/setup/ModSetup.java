package mcjty.lostcities.setup;

import mcjty.lostcities.api.ILostCityProfileSetup;
import mcjty.lostcities.config.ProfileSetup;
import mcjty.lostcities.network.PacketHandler;
import mcjty.lostcities.worldgen.LostCityFeature;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ModSetup {

    public static Logger logger = null;

    public final List<Consumer<ILostCityProfileSetup>> profileSetups = new ArrayList<>();

    public static Logger getLogger() {
        return logger;
    }


    public void init(FMLCommonSetupEvent e) {
        logger = LogManager.getLogger();

        ProfileSetup.setupProfiles();

        PacketHandler.registerMessages("lostcities");
        LostCityFeature.registerConfiguredFeatures();

        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
        // @todo 1.14
//        MinecraftForge.TERRAIN_GEN_BUS.register(new TerrainEventHandlers());

        // @todo 1.14
//        LootTableList.register(new ResourceLocation(LostCities.MODID, "chests/lostcitychest"));
//        LootTableList.register(new ResourceLocation(LostCities.MODID, "chests/raildungeonchest"));

        AssetRegistries.reset();
    }
}
