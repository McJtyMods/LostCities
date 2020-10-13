package mcjty.lostcities.setup;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.config.ConfigSetup;
import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.network.PacketHandler;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldtypes.CavernWorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public class ModSetup {

    public static boolean chisel = false;
    public static boolean biomesoplenty = false;
    public static boolean atg = false;

    public static Logger logger = null;

    public static Logger getLogger() {
        return logger;
    }


    public void init(FMLCommonSetupEvent e) {
        logger = LogManager.getLogger();

        LostCityConfiguration.setupProfiles();

        PacketHandler.registerMessages("lostcities");

        // @todo 1.16
//        CavernWorldType.init();

        setupModCompat();

        ConfigSetup.init();
//        ModDimensions.init();

        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
        // @todo 1.14
//        MinecraftForge.TERRAIN_GEN_BUS.register(new TerrainEventHandlers());

        // @todo 1.14
//        LootTableList.register(new ResourceLocation(LostCities.MODID, "chests/lostcitychest"));
//        LootTableList.register(new ResourceLocation(LostCities.MODID, "chests/raildungeonchest"));

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
// @todo 1.14
//            } else if (path.startsWith("$")) {
//                File file = new File(modConfigDir.getPath() + File.separator + path.substring(1));
//                AssetRegistries.load(file);
            } else {
                throw new RuntimeException("Invalid path for lostcity resource in 'assets' config!");
            }
        }

        if (LostCityConfiguration.DEBUG) {
            // @todo 1.14
//            logger.info("Asset parts loaded: " + AssetRegistries.PARTS.getCount());
            AssetRegistries.showStatistics();
        }

        DeferredWorkQueue.runLater(() -> {
            for (Biome biome : ForgeRegistries.BIOMES) {
                // @todo 1.16
//                if (!BiomeDictionary.hasType(biome, BiomeDictionary.Type.VOID)) {
//                    biome.addFeature(GenerationStage.Decoration.RAW_GENERATION, Registration.LOSTCITY_FEATURE
//                            .withConfiguration(NoFeatureConfig.NO_FEATURE_CONFIG)
//                            .withPlacement(Placement.COUNT_RANGE.configure(new CountRangeConfig(1, 0, 0, 1)))
//                    );
//                }
            }
        });

    }

    private void setupModCompat() {
        chisel = ModList.get().isLoaded("chisel");
        biomesoplenty = ModList.get().isLoaded("biomesoplenty");
//        atg = Loader.isModLoaded("atg"); // @todo
    }

}
