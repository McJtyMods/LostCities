package mcjty.lostcities.config;

import net.minecraftforge.common.config.Configuration;

public class WorldgenConfiguration {
    public static final String CATEGORY_WORLDGEN = "worldgen";

    public static int dungeonChance = 200;
    public static int volcanoChance = 60;
    public static int cavernHeightLimit = 1;        // 0 == 64, 1 == 128, 2 == 195, 3 == 256
    public static float randomFeatureChance = 0.4f;
    public static float randomLakeFluidChance = 0.2f;
    public static float randomOrbFluidChance = 0.2f;
    public static float randomOregenMaterialChance = 0.2f;
    public static float randomFeatureMaterialChance = 0.4f;
    public static float randomStructureChance = 0.2f;
    public static float randomEffectChance = 0.1f;
    public static float randomOceanLiquidChance = 0.2f;
    public static float randomBaseBlockChance = 0.3f;
    public static float randomSpecialSkyChance = 0.5f;
    public static float randomExtraMobsChance = 0.4f;
    public static float randomSpecialTimeChance = 0.5f;
    public static float randomWeatherChance = 0.8f;
    public static float randomControllerChance = 0.4f;
    public static int bedrockLayer = 1;
    public static boolean normalTerrainInheritsOverworld = false;
    public static int dimletParcelRarity = 2;
    public static int oreMinimumVeinSize = 5;
    public static int oreMaximumVeinSize = 8;
    public static int oreMaximumVeinCount = 3;
    public static int oreMinimumHeight = 2;
    public static int oreMaximumHeight = 40;

    public static int enableDimletsInRFToolsDungeons = 2;
    public static int uncraftableDimletsInRFToolsDungeons = 2;
    public static boolean enableDimletsInRFToolsFrames = true;

    public static void init(Configuration cfg) {
        dungeonChance = cfg.get(CATEGORY_WORLDGEN, "dungeonChance", dungeonChance,
                "The chance for a dungeon to spawn in a chunk. Higher numbers mean less chance (1 in 'dungeonChance' chance)").getInt();
        volcanoChance = cfg.get(CATEGORY_WORLDGEN, "volcanoChance", volcanoChance,
                "The chance for a volcano to spawn in a chunk (with the volcano feature dimlet). Higher numbers mean less chance (1 in 'volcanoChance' chance)").getInt();
        cavernHeightLimit = cfg.get(CATEGORY_WORLDGEN, "cavernHeightLimit", cavernHeightLimit,
                "Maximum height of the caverns. 0=64, 1=128, 2=196, 3=256").getInt();

        randomFeatureChance = (float) cfg.get(CATEGORY_WORLDGEN, "randomFeatureChance", randomFeatureChance,
                "The chance that every specific feature gets randomly selected in worldgen (tendrils, caves, lakes, oregen, ...)").getDouble();
        randomLakeFluidChance = (float) cfg.get(CATEGORY_WORLDGEN, "randomLakeFluidChance", randomLakeFluidChance,
                "The chance that random fluid liquids are selected for lakes").getDouble();
        randomOrbFluidChance = (float) cfg.get(CATEGORY_WORLDGEN, "randomOrbFluidChance", randomOrbFluidChance,
                "The chance that random fluid liquids are selected for liquid orbs").getDouble();
        randomOregenMaterialChance = (float) cfg.get(CATEGORY_WORLDGEN, "randomOregenMaterialChance", randomOregenMaterialChance,
                "The chance that random blocks are selected for extra oregen feature").getDouble();
        randomFeatureMaterialChance = (float) cfg.get(CATEGORY_WORLDGEN, "randomFeatureMaterialChance", randomFeatureMaterialChance,
                "The chance that random blocks are selected for landscape features (tendrils, canyons, ...)").getDouble();
        randomStructureChance = (float) cfg.get(CATEGORY_WORLDGEN, "randomStructureChance", randomStructureChance,
                "The chance that every specific structure gets randomly selected in worldgen (village, nether fortress, ...)").getDouble();
        randomEffectChance = (float) cfg.get(CATEGORY_WORLDGEN, "randomEffectChance", randomEffectChance,
                "The chance that an effect gets randomly selected in worldgen (poison, regeneration, ...)").getDouble();
        randomOceanLiquidChance = (float) cfg.get(CATEGORY_WORLDGEN, "randomOceanLiquidChance", randomOceanLiquidChance,
                "The chance that a non-water block is selected for oceans and seas").getDouble();
        randomBaseBlockChance = (float) cfg.get(CATEGORY_WORLDGEN, "randomBaseBlockChance", randomBaseBlockChance,
                "The chance that a non-stone block is selected for the main terrain").getDouble();
        randomSpecialSkyChance = (float) cfg.get(CATEGORY_WORLDGEN, "randomSpecialSkyChance", randomSpecialSkyChance,
                "The chance that special sky features are selected").getDouble();
        randomExtraMobsChance = (float) cfg.get(CATEGORY_WORLDGEN, "randomExtraMobsChance", randomExtraMobsChance,
                "The chance that extra specific mobs will spawn").getDouble();
        randomSpecialTimeChance = (float) cfg.get(CATEGORY_WORLDGEN, "randomSpecialTimeChance", randomSpecialTimeChance,
                "The chance that default time features are selected").getDouble();
        randomWeatherChance = (float) cfg.get(CATEGORY_WORLDGEN, "randomWeatherChance", randomWeatherChance,
                "The chance that default weather features are selected").getDouble();
        randomControllerChance = (float) cfg.get(CATEGORY_WORLDGEN, "randomControllerChance", randomControllerChance,
                "The chance that a random biome controller is selected").getDouble();
        dimletParcelRarity = cfg.get(CATEGORY_WORLDGEN, "dimletParcelRarity", dimletParcelRarity,
                "The chance that you get a dimlet parcel in a dungeon chest").getInt();

        bedrockLayer = cfg.get(CATEGORY_WORLDGEN, "bedrockLayer", bedrockLayer,
                "The height of the bedrock layer that is generated at the bottom of some world types. Set to 0 to disable this and get default bedrock generation").getInt();
        normalTerrainInheritsOverworld = cfg.get(CATEGORY_WORLDGEN, "normalTerrainInheritsOverworld", normalTerrainInheritsOverworld,
                "Set this to true if you want terrains with dimlet 'normal' to generate like the overworld (i.e. amplified if the overworld is amplified)").getBoolean();
        oreMinimumVeinSize = cfg.get(CATEGORY_WORLDGEN, "oreMinimumVeinSize", oreMinimumVeinSize,
                "Minimum vein size of dimensional shard ores").getInt();
        oreMaximumVeinSize = cfg.get(CATEGORY_WORLDGEN, "oreMaximumVeinSize", oreMaximumVeinSize,
                "Maximum vein size of dimensional shard ores").getInt();
        oreMaximumVeinCount = cfg.get(CATEGORY_WORLDGEN, "oreMaximumVeinCount", oreMaximumVeinCount,
                "Maximum number of veins for dimensional shard ores").getInt();
        oreMinimumHeight = cfg.get(CATEGORY_WORLDGEN, "oreMinimumHeight", oreMinimumHeight,
                "Minimum y level for dimensional shard ores").getInt();
        oreMaximumHeight = cfg.get(CATEGORY_WORLDGEN, "oreMaximumHeight", oreMaximumHeight,
                "Maximum y level for dimensional shard ores").getInt();

        enableDimletsInRFToolsDungeons = cfg.get(CATEGORY_WORLDGEN, "enableDimletsInRFToolsDungeons", enableDimletsInRFToolsDungeons,
                "The maximum number of random dimlets that can be generated in rftools dungeons (set to 0 to disable this)").getInt();
        uncraftableDimletsInRFToolsDungeons = cfg.get(CATEGORY_WORLDGEN, "uncraftableDimletsInRFToolsDungeons", uncraftableDimletsInRFToolsDungeons,
                "The number of uncraftable dimlets that will always be generated in rftools dungeons (set to 0 to disable this, this value is independ from enableDimletsInRFToolsDungeons)").getInt();
        enableDimletsInRFToolsFrames = cfg.get(CATEGORY_WORLDGEN, "enableDimletsInRFToolsFrames", enableDimletsInRFToolsFrames,
                "If true then item frames in rftools dungeons will contain dimlets. Otherwise they will contain dimlet parts").getBoolean();
    }

}
