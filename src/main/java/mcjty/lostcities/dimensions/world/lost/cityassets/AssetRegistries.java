package mcjty.lostcities.dimensions.world.lost.cityassets;

import mcjty.lostcities.dimensions.world.lost.data.FloorsData;

public class AssetRegistries {

    public static final PartRegistry PARTS = new PartRegistry();
    public static final BuildingRegistry BUILDINGS = new BuildingRegistry();

    public static final void reset() {
        System.out.println("AssetRegistries.reset");
        PARTS.reset();
        BUILDINGS.reset();
    }

    public static void init() {
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

        Building building1 = new Building("building1");
        building1.addPart(levelInfo -> true, FloorsData.BUILDING1_1.getName());
        building1.addPart(levelInfo -> true, FloorsData.BUILDING1_2.getName());
        building1.addPart(levelInfo -> true, FloorsData.BUILDING1_3.getName());
        building1.addPart(levelInfo -> true, FloorsData.BUILDING1_4.getName());
        building1.addPart(levelInfo -> true, FloorsData.BUILDING1_5.getName());
        building1.addPart(levelInfo -> true, FloorsData.BUILDING1_6.getName());
        building1.addPart(levelInfo -> true, FloorsData.BUILDING1_7.getName());
        building1.addPart(levelInfo -> true, FloorsData.BUILDING1_8.getName());
        building1.addPart(levelInfo -> true, FloorsData.BUILDING1_9.getName());
        BUILDINGS.register(building1);

        Building building2 = new Building("building2");
        building2.addPart(levelInfo -> true, FloorsData.BUILDING2_1.getName());
        building2.addPart(levelInfo -> true, FloorsData.BUILDING2_2.getName());
        building2.addPart(levelInfo -> true, FloorsData.BUILDING2_3.getName());
        building2.addPart(levelInfo -> true, FloorsData.BUILDING2_4.getName());
        BUILDINGS.register(building2);

        Building building3 = new Building("building3");
        building3.addPart(levelInfo -> true, FloorsData.BUILDING3_1.getName());
        building3.addPart(levelInfo -> true, FloorsData.BUILDING3_2.getName());
        building3.addPart(levelInfo -> true, FloorsData.BUILDING3_3.getName());
        building3.addPart(levelInfo -> true, FloorsData.BUILDING3_4.getName());
        BUILDINGS.register(building3);
    }
}
