package mcjty.lostcities.dimensions.world.lost.cityassets;

import mcjty.lostcities.dimensions.world.lost.data.DataCenterData;
import mcjty.lostcities.dimensions.world.lost.data.FloorsData;
import mcjty.lostcities.dimensions.world.lost.data.LibraryData;
import mcjty.lostcities.dimensions.world.lost.data.RoofTopsData;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import org.apache.commons.lang3.tuple.Pair;

public class AssetRegistries {

    public static final AbstractAssetRegistry<BuildingPart> PARTS = new AbstractAssetRegistry<>();
    public static final AbstractAssetRegistry<Building> BUILDINGS = new AbstractAssetRegistry<>();
    public static final AbstractAssetRegistry<CityStyle> CITYSTYLES = new AbstractAssetRegistry<>();
    public static final AbstractAssetRegistry<MultiBuilding> MULTI_BUILDINGS = new AbstractAssetRegistry<>();
    public static final AbstractAssetRegistry<Style> STYLES = new AbstractAssetRegistry<>();

    public static final void reset() {
        System.out.println("AssetRegistries.reset");
        PARTS.reset();
        BUILDINGS.reset();
        CITYSTYLES.reset();
        MULTI_BUILDINGS.reset();
        STYLES.reset();
    }

    public static void init() {
        reset();
        System.out.println("AssetRegistries.init");

        PARTS.register(FloorsData.BUILDING1_1);
        PARTS.register(FloorsData.BUILDING1_2);
        PARTS.register(FloorsData.BUILDING1_3);
        PARTS.register(FloorsData.BUILDING1_4);
        PARTS.register(FloorsData.BUILDING1_5);
        PARTS.register(FloorsData.BUILDING1_6);
        PARTS.register(FloorsData.BUILDING1_7);
        PARTS.register(FloorsData.BUILDING1_8);
        PARTS.register(FloorsData.BUILDING1_9);
        PARTS.register(FloorsData.BUILDING2_1);
        PARTS.register(FloorsData.BUILDING2_2);
        PARTS.register(FloorsData.BUILDING2_3);
        PARTS.register(FloorsData.BUILDING2_4);
        PARTS.register(FloorsData.BUILDING3_1);
        PARTS.register(FloorsData.BUILDING3_2);
        PARTS.register(FloorsData.BUILDING3_3);
        PARTS.register(FloorsData.BUILDING3_4);

        PARTS.register(RoofTopsData.TOP1X1_1);
        PARTS.register(RoofTopsData.TOP1X1_2);
        PARTS.register(RoofTopsData.TOP1X1_3);
        PARTS.register(RoofTopsData.TOP1X1_4);

        PARTS.register(LibraryData.LIBRARY00_1);
        PARTS.register(LibraryData.LIBRARY01_1);
        PARTS.register(LibraryData.LIBRARY01_2);
        PARTS.register(LibraryData.LIBRARY01_3);
        PARTS.register(LibraryData.LIBRARY10_1);
        PARTS.register(LibraryData.LIBRARY10_2);
        PARTS.register(LibraryData.LIBRARY10_3);
        PARTS.register(LibraryData.LIBRARY10_4);
        PARTS.register(LibraryData.LIBRARY10_5);
        PARTS.register(LibraryData.LIBRARY11_1);
        PARTS.register(LibraryData.LIBRARY11_2);
        PARTS.register(LibraryData.TOP_LIBRARY00_1);
        PARTS.register(LibraryData.TOP_LIBRARY00_2);
        PARTS.register(LibraryData.TOP_LIBRARY_1);
        PARTS.register(LibraryData.TOP_LIBRARY_2);
        PARTS.register(LibraryData.TOP_LIBRARY_3);
        PARTS.register(LibraryData.TOP_LIBRARY_4);

        PARTS.register(DataCenterData.CENTER00_1);
        PARTS.register(DataCenterData.CENTER00_2);
        PARTS.register(DataCenterData.CENTER00_3);
        PARTS.register(DataCenterData.CENTER01_1);
        PARTS.register(DataCenterData.CENTER01_2);
        PARTS.register(DataCenterData.CENTER10_1);
        PARTS.register(DataCenterData.CENTER10_2);
        PARTS.register(DataCenterData.CENTER10_3);
        PARTS.register(DataCenterData.CENTER10_4);
        PARTS.register(DataCenterData.CENTER11_1);
        PARTS.register(DataCenterData.TOP_CENTER00_1);
        PARTS.register(DataCenterData.TOP_CENTER00_2);
        PARTS.register(DataCenterData.TOP_CENTER_1);
        PARTS.register(DataCenterData.TOP_CENTER_2);
        PARTS.register(DataCenterData.TOP_CENTER_3);
        PARTS.register(DataCenterData.TOP_CENTER_4);

        createBuilding1();
        createBuilding2();
        createBuilding3();
        createDataCenter();
        createLibrary();

        MULTI_BUILDINGS.register(new MultiBuilding("library", 2, 2)
                .set(0, 0, "library00")
                .set(1, 0, "library10")
                .set(0, 1, "library01")
                .set(1, 1, "library11"));
        MULTI_BUILDINGS.register(new MultiBuilding("center", 2, 2)
                .set(0, 0, "center00")
                .set(1, 0, "center10")
                .set(0, 1, "center01")
                .set(1, 1, "center11"));
        MULTI_BUILDINGS.register(new MultiBuilding("multi1", 2, 2)
                .set(0, 0, "building1")
                .set(1, 0, "building1")
                .set(0, 1, "building1")
                .set(1, 1, "building1"));
        MULTI_BUILDINGS.register(new MultiBuilding("multi2", 2, 2)
                .set(0, 0, "building2")
                .set(1, 0, "building2")
                .set(0, 1, "building2")
                .set(1, 1, "building2"));
        MULTI_BUILDINGS.register(new MultiBuilding("multi3", 2, 2)
                .set(0, 0, "building3")
                .set(1, 0, "building3")
                .set(0, 1, "building3")
                .set(1, 1, "building3"));

        //noinspection unchecked
        CITYSTYLES.register(new CityStyle("standard")
                .addBuilding(cityInfo -> Pair.of(.4f, "building1"))
                .addBuilding(cityInfo -> Pair.of(.4f, "building2"))
                .addBuilding(cityInfo -> Pair.of(.2f, "building3"))
                .addMultiBuilding(cityInfo -> Pair.of(.3f, "multi1"))
                .addMultiBuilding(cityInfo -> Pair.of(.3f, "multi2"))
                .addMultiBuilding(cityInfo -> Pair.of(.2f, "multi3"))
                .addMultiBuilding(cityInfo -> Pair.of(.1f, "library"))
                .addMultiBuilding(cityInfo -> Pair.of(.1f, "center"))
                .addRandomStyleChoice(Pair.of(1.0f, "bricks"), Pair.of(1.0f, "bricks_cyan"), Pair.of(1.0f, "bricks_gray"), Pair.of(1.0f, "bricks_silver"))
                .addRandomStyleChoice(Pair.of(1.0f, "street"))
                .addRandomStyleChoice(Pair.of(1.0f, "glass_white"), Pair.of(1.0f, "glass_gray"), Pair.of(1.0f, "glass_light_blue"), Pair.of(1.0f, "glass_blue"),
                        Pair.of(1.0f, "glass_pane_white"), Pair.of(1.0f, "glass_pane_gray"), Pair.of(1.0f, "glass_pane_light_blue"), Pair.of(1.0f, "glass_pane_blue"),
                        Pair.of(1.0f, "glass"), Pair.of(1.0f, "glass_pane"))
        );

        STYLES.register(new Style("outside")
                .register("quartz", Blocks.QUARTZ_BLOCK.getDefaultState(), true)
                .register("bricks", Blocks.STONEBRICK.getDefaultState(), true)
                .register("bricks_variant", Blocks.DOUBLE_STONE_SLAB.getDefaultState(), true)
                .register("bricks_cracked", Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CRACKED), true)
                .register("bricks_mossy", Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.MOSSY), true)
                .register("bricks_monster", Blocks.STONEBRICK.getDefaultState(), true));
        STYLES.register(new Style("bricks")
                .register("quartz", Blocks.QUARTZ_BLOCK.getDefaultState(), true)
                .register("bricks", Blocks.STONEBRICK.getDefaultState(), true)
                .register("bricks_variant", Blocks.DOUBLE_STONE_SLAB.getDefaultState(), true)
                .register("bricks_cracked", Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CRACKED), true)
                .register("bricks_mossy", Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.MOSSY), true)
                .register("bricks_monster", Blocks.MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, BlockSilverfish.EnumType.STONEBRICK), true));
        STYLES.register(new Style("bricks_cyan")
                .register("quartz", Blocks.QUARTZ_BLOCK.getDefaultState(), true)
                .register("bricks", Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.CYAN), true)
                .register("bricks_variant", Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.BLACK), true)
                .register("bricks_cracked", Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.CYAN), true)
                .register("bricks_mossy", Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.CYAN), true)
                .register("bricks_monster", Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.CYAN), true));
        STYLES.register(new Style("bricks_gray")
                .register("quartz", Blocks.QUARTZ_BLOCK.getDefaultState(), true)
                .register("bricks", Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY), true)
                .register("bricks_variant", Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.BLACK), true)
                .register("bricks_cracked", Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY), true)
                .register("bricks_mossy", Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY), true)
                .register("bricks_monster", Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY), true));
        STYLES.register(new Style("bricks_silver")
                .register("quartz", Blocks.QUARTZ_BLOCK.getDefaultState(), true)
                .register("bricks", Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.SILVER), true)
                .register("bricks_variant", Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.BROWN), true)
                .register("bricks_cracked", Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.SILVER), true)
                .register("bricks_mossy", Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.SILVER), true)
                .register("bricks_monster", Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.SILVER), true));

        STYLES.register(new Style("glass_white")
                .register("glass", Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.WHITE))
                .register("glass_full", Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.WHITE)));
        STYLES.register(new Style("glass_gray")
                .register("glass", Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.GRAY))
                .register("glass_full", Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.GRAY)));
        STYLES.register(new Style("glass_light_blue")
                .register("glass", Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.LIGHT_BLUE))
                .register("glass_full", Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.LIGHT_BLUE)));
        STYLES.register(new Style("glass_blue")
                .register("glass", Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.BLUE))
                .register("glass_full", Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.BLUE)));
        STYLES.register(new Style("glass_pane_white")
                .register("glass", Blocks.STAINED_GLASS_PANE.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.WHITE))
                .register("glass_full", Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.WHITE)));
        STYLES.register(new Style("glass_pane_gray")
                .register("glass", Blocks.STAINED_GLASS_PANE.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.GRAY))
                .register("glass_full", Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.GRAY)));
        STYLES.register(new Style("glass_pane_light_blue")
                .register("glass", Blocks.STAINED_GLASS_PANE.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.LIGHT_BLUE))
                .register("glass_full", Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.LIGHT_BLUE)));
        STYLES.register(new Style("glass_pane_blue")
                .register("glass", Blocks.STAINED_GLASS_PANE.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.BLUE))
                .register("glass_full", Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.BLUE)));
        STYLES.register(new Style("glass")
                .register("glass", Blocks.GLASS.getDefaultState())
                .register("glass_full", Blocks.GLASS.getDefaultState()));
        STYLES.register(new Style("glass_pane")
                .register("glass", Blocks.GLASS_PANE.getDefaultState())
                .register("glass_full", Blocks.GLASS.getDefaultState()));

        STYLES.register(new Style("street")
                .register("street", Blocks.DOUBLE_STONE_SLAB.getDefaultState(), true)
                .register("street2", Blocks.BRICK_BLOCK.getDefaultState(), true));
    }

    private static void createDataCenter() {
        Building center00 = new Building("center00")
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), DataCenterData.CENTER00_1.getName())
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), DataCenterData.CENTER00_2.getName())
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), DataCenterData.CENTER00_3.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), DataCenterData.TOP_CENTER00_1.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), DataCenterData.TOP_CENTER00_2.getName());
        AssetRegistries.BUILDINGS.register(center00);

        Building center01 = new Building("center01")
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), DataCenterData.CENTER01_1.getName())
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), DataCenterData.CENTER01_2.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), DataCenterData.TOP_CENTER_1.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), DataCenterData.TOP_CENTER_2.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), DataCenterData.TOP_CENTER_3.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), DataCenterData.TOP_CENTER_4.getName());
        AssetRegistries.BUILDINGS.register(center01);

        Building center10 = new Building("center10")
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), DataCenterData.CENTER10_1.getName())
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), DataCenterData.CENTER10_2.getName())
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), DataCenterData.CENTER10_3.getName())
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), DataCenterData.CENTER10_4.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), DataCenterData.TOP_CENTER_1.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), DataCenterData.TOP_CENTER_2.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), DataCenterData.TOP_CENTER_3.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), DataCenterData.TOP_CENTER_4.getName());
        AssetRegistries.BUILDINGS.register(center10);

        Building center11 = new Building("center11")
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), DataCenterData.CENTER11_1.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), DataCenterData.TOP_CENTER_1.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), DataCenterData.TOP_CENTER_2.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), DataCenterData.TOP_CENTER_3.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), DataCenterData.TOP_CENTER_4.getName());
        AssetRegistries.BUILDINGS.register(center11);
    }

    private static void createLibrary() {
        Building library00 = new Building("library00")
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), LibraryData.LIBRARY00_1.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), LibraryData.TOP_LIBRARY00_1.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), LibraryData.TOP_LIBRARY00_2.getName());
        AssetRegistries.BUILDINGS.register(library00);

        Building library01 = new Building("library01")
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), LibraryData.LIBRARY01_1.getName())
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), LibraryData.LIBRARY01_2.getName())
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), LibraryData.LIBRARY01_3.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), LibraryData.TOP_LIBRARY_1.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), LibraryData.TOP_LIBRARY_2.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), LibraryData.TOP_LIBRARY_3.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), LibraryData.TOP_LIBRARY_4.getName());
        AssetRegistries.BUILDINGS.register(library01);

        Building library10 = new Building("library10")
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), LibraryData.LIBRARY10_1.getName())
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), LibraryData.LIBRARY10_2.getName())
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), LibraryData.LIBRARY10_3.getName())
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), LibraryData.LIBRARY10_4.getName())
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), LibraryData.LIBRARY10_5.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), LibraryData.TOP_LIBRARY_1.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), LibraryData.TOP_LIBRARY_2.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), LibraryData.TOP_LIBRARY_3.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), LibraryData.TOP_LIBRARY_4.getName());
        AssetRegistries.BUILDINGS.register(library10);

        Building library11 = new Building("library11")
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), LibraryData.LIBRARY11_1.getName())
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), LibraryData.LIBRARY11_2.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), LibraryData.TOP_LIBRARY_1.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), LibraryData.TOP_LIBRARY_2.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), LibraryData.TOP_LIBRARY_3.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), LibraryData.TOP_LIBRARY_4.getName());
        AssetRegistries.BUILDINGS.register(library11);
    }

    private static void createBuilding3() {
        Building building3 = new Building("building3")
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING3_1.getName())
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING3_2.getName())
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING3_3.getName())
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING3_4.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), RoofTopsData.TOP1X1_1.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), RoofTopsData.TOP1X1_2.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), RoofTopsData.TOP1X1_3.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), RoofTopsData.TOP1X1_4.getName());
        BUILDINGS.register(building3);
    }

    private static void createBuilding2() {
        Building building2 = new Building("building2")
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING2_1.getName())
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING2_2.getName())
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING2_3.getName())
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING2_4.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), RoofTopsData.TOP1X1_1.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), RoofTopsData.TOP1X1_2.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), RoofTopsData.TOP1X1_3.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), RoofTopsData.TOP1X1_4.getName());
        BUILDINGS.register(building2);
    }

    private static void createBuilding1() {
        Building building1 = new Building("building1")
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING1_1.getName())
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING1_2.getName())
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING1_3.getName())
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING1_4.getName())
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING1_5.getName())
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING1_6.getName())
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING1_7.getName())
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING1_8.getName())
                .addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING1_9.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), RoofTopsData.TOP1X1_1.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), RoofTopsData.TOP1X1_2.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), RoofTopsData.TOP1X1_3.getName())
                .addPart(levelInfo -> levelInfo.isTopOfBuilding(), RoofTopsData.TOP1X1_4.getName());
        BUILDINGS.register(building1);
    }
}
