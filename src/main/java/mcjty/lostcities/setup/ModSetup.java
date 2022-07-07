package mcjty.lostcities.setup;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.api.ILostCityProfileSetup;
import mcjty.lostcities.config.ProfileSetup;
import mcjty.lostcities.network.PacketHandler;
import mcjty.lostcities.worldgen.LostCityFeature;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
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

        readAssets();

        makeExamples();
    }

    private void makeExamples() {
        Path configPath = FMLPaths.CONFIGDIR.get();
        File dir = new File(configPath + File.separator + "lostcities" + File.separator + "examples");
        dir.mkdirs();
        File exampleConditions = new File(configPath + File.separator + "lostcities" + File.separator + "examples" + File.separator + "conditions.json");
        try {
            FileWriter writer = new FileWriter(exampleConditions);
            writer.append("// This is an example set of conditions that you can use and modify and put\n");
            writer.append("// into (for example) config/lostcities/userassets.json\n\n");
            InputStream inputstream = LostCities.class.getResourceAsStream("/assets/lostcities/citydata/conditions.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                writer.append(line + "\n");
            }

            br.close();
            writer.close();
        } catch (IOException e) {
            LostCities.logger.warn("Could not write examples!");
        }
    }

    private void readAssets() {
        AssetRegistries.reset();
        for (String path : Config.ASSETS.get()) {
            if (path.startsWith("/")) {
                try(InputStream inputstream = LostCities.class.getResourceAsStream(path)) {
                    if (inputstream != null) {
                        AssetRegistries.load(inputstream, path);
                    } else {
                        LostCities.logger.error("Can't load asset file '" + path + "'!");
                    }
                } catch (IOException ex) {
                    LostCities.logger.error("Can't load asset file '" + path + "'!");
                }
            } else if (path.startsWith("$")) {
                Path configPath = FMLPaths.CONFIGDIR.get();
                File file = new File(configPath + File.separator + path.substring(1));
                AssetRegistries.load(file);
            } else {
                throw new RuntimeException("Invalid path for lostcity resource in 'assets' config!");
            }
        }
    }
}
