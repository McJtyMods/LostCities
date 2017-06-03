package mcjty.lostcities.config;

import net.minecraftforge.common.config.Configuration;

public class GeneralConfiguration {
    public static final String CATEGORY_GENERAL = "general";

    public static boolean enableDimensionBuilderRecipe = true;
    public static boolean enableDynamicPhaseCost = false;
    public static float dynamicPhaseCostAmount = 0.05f;

    public static int spawnDimension = 0;           // Dimension to return too when power runs out
    public static boolean respawnSameDim = false;   // If true we first try to respawn in rftools dimension unless power is low.
    public static double brutalMobsFactor = 5.0f;   // How much stronger brutal mobs should be
    public static double strongMobsFactor = 2.0f;   // How much stronger brutal strong should be

    public static int bedBehaviour = 0;         // Behaviour when sleeping in an RFTools dimension: 0 = do nothing, 1 = explode, 2 = set spawn

	public static boolean randomizeSeed = false;

    public static int DIMENSIONMODULE_RFPERTICK = 6;

    // Server owner configs
    public static boolean voidOnly = false;
    public static boolean ownerDimletsNeeded = false;
    public static boolean dimensionBuilderNeedsOwner = false;
    public static boolean playersCanDeleteDimensions = false;
    public static boolean editorCanDeleteDimensions = false;
    public static boolean dimensionFolderIsDeletedWithSafeDel = true;
    public static int maxDimensionsPerPlayer = -1;

    public static int rftoolsProviderId = -1;

    public static float endermanDimletPartDrop = 0.02f;
    public static int minParcelContents = 3;
    public static int maxParcelContents = 6;

	public static void init(Configuration cfg) {
        enableDimensionBuilderRecipe = cfg.get(CATEGORY_GENERAL, "enableDimensionBuilderRecipe", enableDimensionBuilderRecipe,
                                               "Enable the dimension builder recipe.").getBoolean();
        enableDynamicPhaseCost = cfg.get(CATEGORY_GENERAL, "enableDynamicPhaseCost", enableDynamicPhaseCost,
                                         "Enable dynamic scaling of the Phase Field Generator cost based on world tick cost").getBoolean();
        dynamicPhaseCostAmount = (float) cfg.get(CATEGORY_GENERAL, "dynamicPhaseCostAmount", dynamicPhaseCostAmount,
                                                 "How much of the tick cost of the world is applied to the PFG cost, as a ratio from 0 to 1").getDouble();

        rftoolsProviderId = cfg.get(CATEGORY_GENERAL, "rftoolsProviderId", rftoolsProviderId,
                "The RFTools dimension provider id (-1 means try to find one automatically)").getInt();

        spawnDimension = cfg.get(CATEGORY_GENERAL, "spawnDimension", spawnDimension,
                "Dimension to respawn in after you get kicked out of an RFTools dimension").getInt();
        respawnSameDim = cfg.get(CATEGORY_GENERAL, "respawnRfToolsDimension", respawnSameDim,
                "If this flag is true the player will respawn in the rftools dimension when he dies (unless power runs out)").getBoolean();

        endermanDimletPartDrop = (float) cfg.get(CATEGORY_GENERAL, "endermanDimletPartDrop", endermanDimletPartDrop,
                "The chance that you get a dimlet parcel when killing an enderman. Set to 0 to disable").getDouble();
        minParcelContents = cfg.get(CATEGORY_GENERAL, "minParcelContents", minParcelContents,
                                    "The minimum amount of dimlet parts you get out of a dimlet parcel").getInt();
        maxParcelContents = cfg.get(CATEGORY_GENERAL, "maxParcelContents", maxParcelContents,
                                    "The maximum amount of dimlet parts you get out of a dimlet parcel").getInt();

        bedBehaviour = cfg.get(CATEGORY_GENERAL, "bedBehaviour", bedBehaviour,
                "Behaviour when sleeping in an RFTools dimension: 0 = do nothing, 1 = explode, 2 = set spawn").getInt();

        randomizeSeed = cfg.get(CATEGORY_GENERAL, "randomizeSeed", randomizeSeed,
                "Randomize the seed when the dimension is created").getBoolean();

        voidOnly = cfg.get(CATEGORY_GENERAL, "voidOnly", voidOnly,
                "Set this to true if you want to make sure RFTools can only create void dimensions").getBoolean();
        ownerDimletsNeeded = cfg.get(CATEGORY_GENERAL, "ownerDimletsNeeded", ownerDimletsNeeded,
                "If this is enabled (non-craftable) owner dimlets are required to construct dimension tabs. This is useful on servers where you want to limit the amount of dimensions a player can make").getBoolean();
        dimensionBuilderNeedsOwner = cfg.get(CATEGORY_GENERAL, "dimensionBuilderNeedsOwner", dimensionBuilderNeedsOwner,
                "If this is enabled then the dimension builder needs a correct owner before you can create dimensions with it").getBoolean();
        playersCanDeleteDimensions = cfg.get(CATEGORY_GENERAL, "playersCanDeleteDimensions", playersCanDeleteDimensions,
                "If this is enabled then regular players can delete their own dimensions using the /rftdim safedel <id> command").getBoolean();
        editorCanDeleteDimensions = cfg.get(CATEGORY_GENERAL, "editorCanDeleteDimensions", editorCanDeleteDimensions,
                "If this is enabled then a dimension editor can delete a dimension that is owned by the same player as the dimension editor's player. This works by sending over a block of TNT").getBoolean();
        dimensionFolderIsDeletedWithSafeDel = cfg.get(CATEGORY_GENERAL, "dimensionFolderIsDeletedWithSafeDel", dimensionFolderIsDeletedWithSafeDel,
                "If this is enabled the /rftdim safedel <id> command will also delete the DIM<id> folder. If false then this has to be done manually").getBoolean();
        maxDimensionsPerPlayer = cfg.get(CATEGORY_GENERAL, "maxDimensionsPerPlayer", maxDimensionsPerPlayer,
                "The maximum amount of dimensions per player. This requires that dimensions are build with an owned builder (dimensionBuilderNeedsOwner must be set). -1 means no maximum").getInt();


        brutalMobsFactor = cfg.get(CATEGORY_GENERAL, "brutalMobsFactor", brutalMobsFactor,
                "How much stronger mobs should be if spawned in a dimension with the brutal mobs dimlet").getDouble();
        strongMobsFactor = cfg.get(CATEGORY_GENERAL, "strongMobsFactor", strongMobsFactor,
                "How much stronger mobs should be if spawned in a dimension with the strong mobs dimlet").getDouble();

        DIMENSIONMODULE_RFPERTICK = cfg.get(CATEGORY_GENERAL, "dimensionRFPerTick", DIMENSIONMODULE_RFPERTICK,
                                      "RF per tick/per block for the dimension screen module").getInt();
    }

}
