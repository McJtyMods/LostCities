package mcjty.lostcities.worldgen.gen;

import mcjty.lostcities.api.ILostCities;
import mcjty.lostcities.worldgen.ChunkDriver;
import mcjty.lostcities.worldgen.LostCityTerrainFeature;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.Orientation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class Doors
{
    private static BlockState getDoor(Block door, boolean upper, boolean left, net.minecraft.core.Direction facing) {
        return door.defaultBlockState()
                .setValue(DoorBlock.HALF, upper ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER)
                .setValue(DoorBlock.HINGE, left ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT)
                .setValue(DoorBlock.FACING, facing);
    }

    public static void generateDoors(LostCityTerrainFeature feature, BuildingInfo info, int height, int f) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState filler = info.getCompiledPalette().get(info.getBuilding().getFillerBlock());
        ChunkDriver driver = feature.driver;

        height--;       // Start generating doors one below for the filler

        if (info.hasConnectionAtX(f + info.cellars)) {
            int x = 0;
            if (hasConnectionWithBuilding(f, info, info.getXmin())) {
                driver.setBlockRange(x, height, 6, height + 4, filler);
                driver.setBlockRange(x, height, 9, height + 4, filler);

                driver.current(x, height, 7).add(filler).add(air).add(air).add(filler);
                driver.current(x, height, 8).add(filler).add(air).add(air).add(filler);

            } else if (hasConnectionToTopOrOutside(f, info, info.getXmin())) {
                driver.setBlockRange(x, height, 6, height + 4, filler);
                driver.setBlockRange(x, height, 9, height + 4, filler);

                driver.current(x, height, 7)
                        .add(filler)
                        .add(getDoor(info.doorBlock, false, true, net.minecraft.core.Direction.EAST))
                        .add(getDoor(info.doorBlock, true, true, net.minecraft.core.Direction.EAST))
                        .add(filler);
                driver.current(x, height, 8)
                        .add(filler)
                        .add(getDoor(info.doorBlock, false, false, net.minecraft.core.Direction.EAST))
                        .add(getDoor(info.doorBlock, true, false, net.minecraft.core.Direction.EAST))
                        .add(filler);
            }
        }
        if (hasConnectionWithBuildingMax(f, info, info.getXmax(), Orientation.X)) {
            int x = 15;
            driver.setBlockRange(x, height, 6, height + 4, filler);
            driver.setBlockRange(x, height, 9, height + 4, filler);
            driver.current(x, height, 7).add(filler).add(air).add(air).add(filler);
            driver.current(x, height, 8).add(filler).add(air).add(air).add(filler);
        } else if (hasConnectionToTopOrOutside(f, info, info.getXmax()) && (info.getXmax().hasConnectionAtXFromStreet(f + info.getXmax().cellars))) {
            int x = 15;
            driver.setBlockRange(x, height, 6, height + 4, filler);
            driver.setBlockRange(x, height, 9, height + 4, filler);
            driver.current(x, height, 7)
                    .add(filler)
                    .add(getDoor(info.doorBlock, false, false, net.minecraft.core.Direction.WEST))
                    .add(getDoor(info.doorBlock, true, false, net.minecraft.core.Direction.WEST))
                    .add(filler);
            driver.current(x, height, 8)
                    .add(filler)
                    .add(getDoor(info.doorBlock, false, true, net.minecraft.core.Direction.WEST))
                    .add(getDoor(info.doorBlock, true, true, net.minecraft.core.Direction.WEST))
                    .add(filler);
        }
        if (info.hasConnectionAtZ(f + info.cellars)) {
            int z = 0;
            if (hasConnectionWithBuilding(f, info, info.getZmin())) {
                driver.setBlockRange(6, height, z, height + 4, filler);
                driver.setBlockRange(9, height, z, height + 4, filler);
                driver.current(7, height, z).add(filler).add(air).add(air).add(filler);
                driver.current(8, height, z).add(filler).add(air).add(air).add(filler);
            } else if (hasConnectionToTopOrOutside(f, info, info.getZmin())) {
                driver.setBlockRange(6, height, z, height + 4, filler);
                driver.setBlockRange(9, height, z, height + 4, filler);
                driver.current(7, height, z)
                        .add(filler)
                        .add(getDoor(info.doorBlock, false, true, net.minecraft.core.Direction.NORTH))
                        .add(getDoor(info.doorBlock, true, true, net.minecraft.core.Direction.NORTH))
                        .add(filler);
                driver.current(8, height, z)
                        .add(filler)
                        .add(getDoor(info.doorBlock, false, false, net.minecraft.core.Direction.NORTH))
                        .add(getDoor(info.doorBlock, true, false, net.minecraft.core.Direction.NORTH))
                        .add(filler);
            }
        }
        if (hasConnectionWithBuildingMax(f, info, info.getZmax(), Orientation.Z)) {
            int z = 15;
            driver.setBlockRange(6, height, z, height + 4, filler);
            driver.setBlockRange(9, height, z, height + 4, filler);
            driver.current(7, height, z).add(filler).add(air).add(air).add(filler);
            driver.current(8, height, z).add(filler).add(air).add(air).add(filler);
        } else if (hasConnectionToTopOrOutside(f, info, info.getZmax()) && (info.getZmax().hasConnectionAtZFromStreet(f + info.getZmax().cellars))) {
            int z = 15;
            driver.setBlockRange(6, height, z, height + 4, filler);
            driver.setBlockRange(9, height, z, height + 4, filler);
            driver.current(7, height, z)
                    .add(filler)
                    .add(getDoor(info.doorBlock, false, false, net.minecraft.core.Direction.SOUTH))
                    .add(getDoor(info.doorBlock, true, false, net.minecraft.core.Direction.SOUTH))
                    .add(filler);
            driver.current(8, height, z)
                    .add(filler)
                    .add(getDoor(info.doorBlock, false, true, net.minecraft.core.Direction.SOUTH))
                    .add(getDoor(info.doorBlock, true, true, net.minecraft.core.Direction.SOUTH))
                    .add(filler);
        }
    }

    private static boolean hasConnectionWithBuildingMax(int localLevel, BuildingInfo info, BuildingInfo info2, Orientation x) {
        if (info.isValidFloor(localLevel) && info.getFloor(localLevel).getMetaBoolean(ILostCities.META_DONTCONNECT)) {
            return false;
        }
        int globalLevel = info.localToGlobal(localLevel);
        int localAdjacent = info2.globalToLocal(globalLevel);
        if (info2.isValidFloor(localAdjacent) && info2.getFloor(localAdjacent).getMetaBoolean(ILostCities.META_DONTCONNECT)) {
            return false;
        }
        int level = localAdjacent + info2.cellars;
        return info2.hasBuilding && ((localAdjacent >= 0 && localAdjacent < info2.getNumFloors()) || (localAdjacent < 0 && (-localAdjacent) <= info2.cellars)) && info2.hasConnectionAt(level, x);
    }

    private static boolean hasConnectionToTopOrOutside(int localLevel, BuildingInfo info, BuildingInfo info2) {
        int globalLevel = info.localToGlobal(localLevel);
        int localAdjacent = info2.globalToLocal(globalLevel);
        if (info.getFloor(localLevel).getMetaBoolean(ILostCities.META_DONTCONNECT)) {
            return false;
        }
        return (info2.isCity && !info2.hasBuilding && localLevel == 0 && localAdjacent == 0) || (info2.hasBuilding && localAdjacent == info2.getNumFloors());
//        return (!info2.hasBuilding && localLevel == localAdjacent) || (info2.hasBuilding && localAdjacent == info2.getNumFloors());
    }

    private static boolean hasConnectionWithBuilding(int localLevel, BuildingInfo info, BuildingInfo info2) {
        int globalLevel = info.localToGlobal(localLevel);
        int localAdjacent = info2.globalToLocal(globalLevel);
        return info2.hasBuilding && ((localAdjacent >= 0 && localAdjacent < info2.getNumFloors()) || (localAdjacent < 0 && (-localAdjacent) <= info2.cellars));
    }
}
