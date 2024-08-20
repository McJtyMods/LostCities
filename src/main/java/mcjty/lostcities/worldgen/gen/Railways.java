package mcjty.lostcities.worldgen.gen;

import mcjty.lostcities.api.RailChunkType;
import mcjty.lostcities.worldgen.ChunkDriver;
import mcjty.lostcities.worldgen.ChunkHeightmap;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.LostCityTerrainFeature;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.Railway;
import mcjty.lostcities.worldgen.lost.Transform;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.BuildingPart;
import mcjty.lostcities.worldgen.lost.regassets.data.RailwayParts;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Railways {
    public static void generateRailwayDungeons(LostCityTerrainFeature feature, BuildingInfo info) {
        if (info.railDungeon == null) {
            return;
        }
        if (info.getZmin().getRailInfo().getType() == RailChunkType.HORIZONTAL ||
                info.getZmax().getRailInfo().getType() == RailChunkType.HORIZONTAL) {
            int height = info.groundLevel + Railway.RAILWAY_LEVEL_OFFSET * LostCityTerrainFeature.FLOORHEIGHT;
            feature.generatePart(info, info.railDungeon, Transform.ROTATE_NONE, 0, height, 0, LostCityTerrainFeature.HardAirSetting.AIR);
        }
    }

    public static void generateRailways(LostCityTerrainFeature feature, BuildingInfo info, Railway.RailChunkInfo railInfo, ChunkHeightmap heightmap) {
        IDimensionInfo provider = feature.provider;
        ChunkDriver driver = feature.driver;
        BlockState liquid = feature.liquid;
        BlockState air = Blocks.AIR.defaultBlockState();
        RailwayParts railwayParts = provider.getWorldStyle().getPartSelector().railwayParts();
        int height = info.groundLevel + railInfo.getLevel() * LostCityTerrainFeature.FLOORHEIGHT;
        RailChunkType type = railInfo.getType();
        BuildingPart part;
        Transform transform = Transform.ROTATE_NONE;
        boolean needsStaircase = false;
        boolean clearUpper = false;
        switch (type) {
            case NONE:
                return;
            case STATION_SURFACE:
            case STATION_EXTENSION_SURFACE:
                if (railInfo.getLevel() < info.cityLevel) {
                    // Even for a surface station extension we switch to underground if we are an extension
                    // that is at a spot where the city is higher then where the station is
                    part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), feature.getRandomPart(railwayParts.stationUnderground()));
                } else {
                    if (railInfo.getPart() != null) {
                        part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), feature.getRandomPart(railInfo.getPart()));
                    } else {
                        part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), feature.getRandomPart(railwayParts.stationOpen()));
                    }
                }
                clearUpper = true;
                break;
            case STATION_UNDERGROUND:
                part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), feature.getRandomPart(railwayParts.stationUndergroundStairs()));
                needsStaircase = true;
                break;
            case STATION_EXTENSION_UNDERGROUND:
                part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), feature.getRandomPart(railwayParts.stationUnderground()));
                break;
            case RAILS_END_HERE:
                part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), feature.getRandomPart(railwayParts.railsHorizontalEnd()));
                if (railInfo.getDirection() == Railway.RailDirection.EAST) {
                    transform = Transform.MIRROR_X;
                }
                break;
            case HORIZONTAL:
                part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), feature.getRandomPart(railwayParts.railsHorizontal()));

                // If the adjacent chunks are also horizontal we take a sample of the blocks around us to see if we are in water
                RailChunkType type1 = info.getXmin().getRailInfo().getType();
                RailChunkType type2 = info.getXmax().getRailInfo().getType();
                if (!type1.isStation() && !type2.isStation()) {
                    if (driver.getBlock(3, height + 2, 3) == liquid &&
                            driver.getBlock(12, height + 2, 3) == liquid &&
                            driver.getBlock(3, height + 2, 12) == liquid &&
                            driver.getBlock(12, height + 2, 12) == liquid &&
                            driver.getBlock(3, height + 4, 7) == liquid &&
                            driver.getBlock(12, height + 4, 8) == liquid) {
                        part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), feature.getRandomPart(railwayParts.railsHorizontalWater()));
                    }
                }
                break;
            case VERTICAL:
                part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), feature.getRandomPart(railwayParts.railsVertical()));
                if (driver.getBlock(3, height + 2, 3) == liquid &&
                        driver.getBlock(12, height + 2, 3) == liquid &&
                        driver.getBlock(3, height + 2, 12) == liquid &&
                        driver.getBlock(12, height + 2, 12) == liquid &&
                        driver.getBlock(3, height + 4, 7) == liquid &&
                        driver.getBlock(12, height + 4, 8) == liquid) {
                    part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), feature.getRandomPart(railwayParts.railsVerticalWater()));
                }
                if (railInfo.getDirection() == Railway.RailDirection.EAST) {
                    transform = Transform.MIRROR_X;
                }
                break;
            case THREE_SPLIT:
                part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), feature.getRandomPart(railwayParts.rails3Split()));
                if (railInfo.getDirection() == Railway.RailDirection.EAST) {
                    transform = Transform.MIRROR_X;
                }
                break;
            case GOING_DOWN_TWO_FROM_SURFACE:
            case GOING_DOWN_FURTHER:
                part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), feature.getRandomPart(railwayParts.railsDown2()));
                if (railInfo.getDirection() == Railway.RailDirection.EAST) {
                    transform = Transform.MIRROR_X;
                }
                break;
            case GOING_DOWN_ONE_FROM_SURFACE:
                part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), feature.getRandomPart(railwayParts.railsDown1()));
                if (railInfo.getDirection() == Railway.RailDirection.EAST) {
                    transform = Transform.MIRROR_X;
                }
                break;
            case DOUBLE_BEND:
                part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), feature.getRandomPart(railwayParts.railsBend()));
                if (railInfo.getDirection() == Railway.RailDirection.EAST) {
                    transform = Transform.MIRROR_X;
                }
                break;
            default:
                part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), feature.getRandomPart(railwayParts.railsFlat()));
                break;
        }
        int h = feature.generatePart(info, part, transform, 0, height, 0, LostCityTerrainFeature.HardAirSetting.AIR);
        if (clearUpper) {
            int maxh = heightmap.getHeight() + 4;
            if (h < maxh) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        feature.clearRange(info, x, z, h, maxh, false);
                    }
                }
            }
        }

        Character railMainBlock = info.getCityStyle().getRailMainBlock();
        BlockState rail = info.getCompiledPalette().get(railMainBlock);
        if (rail == null) {
            throw new RuntimeException("Cannot find rail block '" + railMainBlock + "' for type '" + type + "'!");
        }

        if (type == RailChunkType.HORIZONTAL) {
            // If there is a rail dungeon north or south we must make a connection here
            if (info.getZmin().railDungeon != null) {
                for (int z = 0; z < 4; z++) {
                    driver.current(6, height + 1, z).add(rail).add(air).add(air);
                    driver.current(7, height + 1, z).add(rail).add(air).add(air);
                }
                for (int z = 0; z < 3; z++) {
                    driver.current(5, height + 2, z).add(rail).add(rail).add(rail);
                    driver.current(6, height + 4, z).block(rail);
                    driver.current(7, height + 4, z).block(rail);
                    driver.current(8, height + 2, z).add(rail).add(rail).add(rail);
                }
            }

            if (info.getZmax().railDungeon != null) {
                for (int z = 0; z < 5; z++) {
                    driver.current(6, height + 1, 15 - z).add(rail).add(air).add(air);
                    driver.current(7, height + 1, 15 - z).add(rail).add(air).add(air);
                }
                for (int z = 0; z < 4; z++) {
                    driver.current(5, height + 2, 15 - z).add(rail).add(rail).add(rail);
                    driver.current(6, height + 4, 15 - z).block(rail);
                    driver.current(7, height + 4, 15 - z).block(rail);
                    driver.current(8, height + 2, 15 - z).add(rail).add(rail).add(rail);
                }
            }
        }

        if (railInfo.getRails() < 3) {
            // We may have to reduce number of rails
            int index;
            switch (railInfo.getType()) {
                case NONE:
                    break;
                case STATION_SURFACE:
                case STATION_UNDERGROUND:
                case STATION_EXTENSION_SURFACE:
                case STATION_EXTENSION_UNDERGROUND:
                case HORIZONTAL: {
                    if (railInfo.getRails() == 1) {
                        driver.current(0, height + 1, 5);
                        for (int x = 0; x < 16; x++) {
                            driver.block(rail).incX();
                        }
                        driver.current(0, height + 1, 9);
                        for (int x = 0; x < 16; x++) {
                            driver.block(rail).incX();
                        }
                    } else {
                        driver.current(0, height + 1, 7);
                        for (int x = 0; x < 16; x++) {
                            driver.block(rail).incX();
                        }
                    }
                    break;
                }
                case GOING_DOWN_TWO_FROM_SURFACE:
                case GOING_DOWN_ONE_FROM_SURFACE:
                case GOING_DOWN_FURTHER:
                    if (railInfo.getRails() == 1) {
                        for (int x = 0; x < 16; x++) {
                            for (int y = height + 1; y < height + part.getSliceCount(); y++) {
                                driver.current(x, y, 5);
                                if (feature.getRailStates().contains(driver.getBlock())) {
                                    driver.block(rail);
                                }
                                driver.current(x, y, 9);
                                if (feature.getRailStates().contains(driver.getBlock())) {
                                    driver.block(rail);
                                }
                            }
                        }
                    } else {
                        for (int x = 0; x < 16; x++) {
                            for (int y = height + 1; y < height + part.getSliceCount(); y++) {
                                driver.current(x, y, 7);
                                if (feature.getRailStates().contains(driver.getBlock())) {
                                    driver.block(rail);
                                }
                            }
                        }
                    }
                    break;
                case THREE_SPLIT:
                case VERTICAL:
                case DOUBLE_BEND:
                case RAILS_END_HERE:
                    break;
            }
        }

        if (needsStaircase) {
            part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), feature.getRandomPart(railwayParts.stationStaircase()));
            for (int i = railInfo.getLevel() + 1; i < info.cityLevel; i++) {
                height = info.groundLevel + i * LostCityTerrainFeature.FLOORHEIGHT;
                feature.generatePart(info, part, transform, 0, height, 0, LostCityTerrainFeature.HardAirSetting.AIR);
            }
            height = info.groundLevel + info.cityLevel * LostCityTerrainFeature.FLOORHEIGHT;
            part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), feature.getRandomPart(railwayParts.stationStaircaseSurface()));
            feature.generatePart(info, part, transform, 0, height, 0, LostCityTerrainFeature.HardAirSetting.AIR);
        }
    }
}
