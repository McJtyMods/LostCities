package mcjty.lostcities.setup;

import mcjty.lostcities.api.ILostCityProfileSetup;
import mcjty.lostcities.config.ProfileSetup;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
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

    public void preInit() {
        logger = LogManager.getLogger();
        ProfileSetup.setupProfiles();
    }

    public void init() {
        ForgeEventHandlers.register();
        // @todo 1.14
//        MinecraftForge.TERRAIN_GEN_BUS.register(new TerrainEventHandlers());

        // @todo 1.14
//        LootTableList.register(Identifier.fromNamespaceAndPath(LostCities.MODID, "chests/lostcitychest"));
//        LootTableList.register(Identifier.fromNamespaceAndPath(LostCities.MODID, "chests/raildungeonchest"));

        AssetRegistries.reset();
    }
}
