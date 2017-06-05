package mcjty.lostcities.dimensions.world.lost.cityassets;

import mcjty.lostcities.dimensions.world.lost.data.DataCenterData;
import mcjty.lostcities.dimensions.world.lost.data.FloorsData;
import mcjty.lostcities.dimensions.world.lost.data.LibraryData;
import mcjty.lostcities.dimensions.world.lost.data.RoofTopsData;

public class AssetRegistries {

    public static final PartRegistry PARTS = new PartRegistry();
    public static final BuildingRegistry BUILDINGS = new BuildingRegistry();

    public static final void reset() {
        System.out.println("AssetRegistries.reset");
        PARTS.reset();
        BUILDINGS.reset();
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

        // @todo, currently order is important. That has to change!
        createBuilding1();
        createBuilding2();
        createBuilding3();
        createDataCenter();
        createLibrary();
    }

    private static void createDataCenter() {
        Building center00 = new Building("center00");
        center00.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), DataCenterData.CENTER00_1.getName());
        center00.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), DataCenterData.CENTER00_2.getName());
        center00.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), DataCenterData.CENTER00_3.getName());
        center00.addPart(levelInfo -> levelInfo.isTopOfBuilding(), DataCenterData.TOP_CENTER00_1.getName());
        center00.addPart(levelInfo -> levelInfo.isTopOfBuilding(), DataCenterData.TOP_CENTER00_2.getName());
        AssetRegistries.BUILDINGS.register(center00);

        Building center01 = new Building("center01");
        center01.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), DataCenterData.CENTER01_1.getName());
        center01.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), DataCenterData.CENTER01_2.getName());
        center01.addPart(levelInfo -> levelInfo.isTopOfBuilding(), DataCenterData.TOP_CENTER_1.getName());
        center01.addPart(levelInfo -> levelInfo.isTopOfBuilding(), DataCenterData.TOP_CENTER_2.getName());
        center01.addPart(levelInfo -> levelInfo.isTopOfBuilding(), DataCenterData.TOP_CENTER_3.getName());
        center01.addPart(levelInfo -> levelInfo.isTopOfBuilding(), DataCenterData.TOP_CENTER_4.getName());
        AssetRegistries.BUILDINGS.register(center01);

        Building center10 = new Building("center10");
        center10.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), DataCenterData.CENTER10_1.getName());
        center10.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), DataCenterData.CENTER10_2.getName());
        center10.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), DataCenterData.CENTER10_3.getName());
        center10.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), DataCenterData.CENTER10_4.getName());
        center10.addPart(levelInfo -> levelInfo.isTopOfBuilding(), DataCenterData.TOP_CENTER_1.getName());
        center10.addPart(levelInfo -> levelInfo.isTopOfBuilding(), DataCenterData.TOP_CENTER_2.getName());
        center10.addPart(levelInfo -> levelInfo.isTopOfBuilding(), DataCenterData.TOP_CENTER_3.getName());
        center10.addPart(levelInfo -> levelInfo.isTopOfBuilding(), DataCenterData.TOP_CENTER_4.getName());
        AssetRegistries.BUILDINGS.register(center10);

        Building center11 = new Building("center11");
        center11.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), DataCenterData.CENTER11_1.getName());
        center11.addPart(levelInfo -> levelInfo.isTopOfBuilding(), DataCenterData.TOP_CENTER_1.getName());
        center11.addPart(levelInfo -> levelInfo.isTopOfBuilding(), DataCenterData.TOP_CENTER_2.getName());
        center11.addPart(levelInfo -> levelInfo.isTopOfBuilding(), DataCenterData.TOP_CENTER_3.getName());
        center11.addPart(levelInfo -> levelInfo.isTopOfBuilding(), DataCenterData.TOP_CENTER_4.getName());
        AssetRegistries.BUILDINGS.register(center11);
    }

    private static void createLibrary() {
        Building library00 = new Building("library00");
        library00.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), LibraryData.LIBRARY00_1.getName());
        library00.addPart(levelInfo -> levelInfo.isTopOfBuilding(), LibraryData.TOP_LIBRARY00_1.getName());
        library00.addPart(levelInfo -> levelInfo.isTopOfBuilding(), LibraryData.TOP_LIBRARY00_2.getName());
        AssetRegistries.BUILDINGS.register(library00);

        Building library01 = new Building("library01");
        library01.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), LibraryData.LIBRARY01_1.getName());
        library01.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), LibraryData.LIBRARY01_2.getName());
        library01.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), LibraryData.LIBRARY01_3.getName());
        library01.addPart(levelInfo -> levelInfo.isTopOfBuilding(), LibraryData.TOP_LIBRARY_1.getName());
        library01.addPart(levelInfo -> levelInfo.isTopOfBuilding(), LibraryData.TOP_LIBRARY_2.getName());
        library01.addPart(levelInfo -> levelInfo.isTopOfBuilding(), LibraryData.TOP_LIBRARY_3.getName());
        library01.addPart(levelInfo -> levelInfo.isTopOfBuilding(), LibraryData.TOP_LIBRARY_4.getName());
        AssetRegistries.BUILDINGS.register(library01);

        Building library10 = new Building("library10");
        library10.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), LibraryData.LIBRARY10_1.getName());
        library10.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), LibraryData.LIBRARY10_2.getName());
        library10.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), LibraryData.LIBRARY10_3.getName());
        library10.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), LibraryData.LIBRARY10_4.getName());
        library10.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), LibraryData.LIBRARY10_5.getName());
        library10.addPart(levelInfo -> levelInfo.isTopOfBuilding(), LibraryData.TOP_LIBRARY_1.getName());
        library10.addPart(levelInfo -> levelInfo.isTopOfBuilding(), LibraryData.TOP_LIBRARY_2.getName());
        library10.addPart(levelInfo -> levelInfo.isTopOfBuilding(), LibraryData.TOP_LIBRARY_3.getName());
        library10.addPart(levelInfo -> levelInfo.isTopOfBuilding(), LibraryData.TOP_LIBRARY_4.getName());
        AssetRegistries.BUILDINGS.register(library10);

        Building library11 = new Building("library11");
        library11.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), LibraryData.LIBRARY11_1.getName());
        library11.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), LibraryData.LIBRARY11_2.getName());
        library11.addPart(levelInfo -> levelInfo.isTopOfBuilding(), LibraryData.TOP_LIBRARY_1.getName());
        library11.addPart(levelInfo -> levelInfo.isTopOfBuilding(), LibraryData.TOP_LIBRARY_2.getName());
        library11.addPart(levelInfo -> levelInfo.isTopOfBuilding(), LibraryData.TOP_LIBRARY_3.getName());
        library11.addPart(levelInfo -> levelInfo.isTopOfBuilding(), LibraryData.TOP_LIBRARY_4.getName());
        AssetRegistries.BUILDINGS.register(library11);
    }

    private static void createBuilding3() {
        Building building3 = new Building("building3");
        building3.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING3_1.getName());
        building3.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING3_2.getName());
        building3.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING3_3.getName());
        building3.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING3_4.getName());
        building3.addPart(levelInfo -> levelInfo.isTopOfBuilding(), RoofTopsData.TOP1X1_1.getName());
        building3.addPart(levelInfo -> levelInfo.isTopOfBuilding(), RoofTopsData.TOP1X1_2.getName());
        building3.addPart(levelInfo -> levelInfo.isTopOfBuilding(), RoofTopsData.TOP1X1_3.getName());
        building3.addPart(levelInfo -> levelInfo.isTopOfBuilding(), RoofTopsData.TOP1X1_4.getName());
        BUILDINGS.register(building3);
    }

    private static void createBuilding2() {
        Building building2 = new Building("building2");
        building2.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING2_1.getName());
        building2.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING2_2.getName());
        building2.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING2_3.getName());
        building2.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING2_4.getName());
        building2.addPart(levelInfo -> levelInfo.isTopOfBuilding(), RoofTopsData.TOP1X1_1.getName());
        building2.addPart(levelInfo -> levelInfo.isTopOfBuilding(), RoofTopsData.TOP1X1_2.getName());
        building2.addPart(levelInfo -> levelInfo.isTopOfBuilding(), RoofTopsData.TOP1X1_3.getName());
        building2.addPart(levelInfo -> levelInfo.isTopOfBuilding(), RoofTopsData.TOP1X1_4.getName());
        BUILDINGS.register(building2);
    }

    private static void createBuilding1() {
        Building building1 = new Building("building1");
        building1.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING1_1.getName());
        building1.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING1_2.getName());
        building1.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING1_3.getName());
        building1.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING1_4.getName());
        building1.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING1_5.getName());
        building1.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING1_6.getName());
        building1.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING1_7.getName());
        building1.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING1_8.getName());
        building1.addPart(levelInfo -> !levelInfo.isTopOfBuilding(), FloorsData.BUILDING1_9.getName());
        building1.addPart(levelInfo -> levelInfo.isTopOfBuilding(), RoofTopsData.TOP1X1_1.getName());
        building1.addPart(levelInfo -> levelInfo.isTopOfBuilding(), RoofTopsData.TOP1X1_2.getName());
        building1.addPart(levelInfo -> levelInfo.isTopOfBuilding(), RoofTopsData.TOP1X1_3.getName());
        building1.addPart(levelInfo -> levelInfo.isTopOfBuilding(), RoofTopsData.TOP1X1_4.getName());
        BUILDINGS.register(building1);
    }
}
