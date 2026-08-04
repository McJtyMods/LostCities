package mcjty.lostcities.worldgen.gen;

import mcjty.lostcities.api.ILostCities;
import mcjty.lostcities.worldgen.ChunkDriver;
import mcjty.lostcities.worldgen.LostCityTerrainFeature;
import mcjty.lostcities.worldgen.highway.HighwayAxis;
import mcjty.lostcities.worldgen.highway.HighwayInfo;
import mcjty.lostcities.worldgen.highway.HighwaySegment;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.Highway;
import mcjty.lostcities.worldgen.lost.Transform;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.BuildingPart;
import mcjty.lostcities.worldgen.lost.regassets.data.HighwayParts;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class Highways {
    public static void generateHighways(LostCityTerrainFeature feature, BuildingInfo info) {
        HighwayInfo highwayInfo = Highway.getHighwayInfo(info.coord, info.provider, info.profile);
        int levelX = highwayInfo.xLevel();
        int levelZ = highwayInfo.zLevel();
        if (levelX == levelZ && levelX >= 0) {
            Junction junction = getJunction(highwayInfo, info.coord.chunkX(), info.coord.chunkZ(), levelX);
            generateHighwayPart(feature, info, levelX, junction.transform(), info.getXmax(), info.getZmax(), junction.type());
        } else if (levelX >= 0 && levelZ >= 0) {
            // There are two highways on different level. Make sure the lowest one is done first because it
            // will clear out what is above it
            if (levelX < levelZ) {
                generateHighwayPart(feature, info, levelX, Transform.ROTATE_NONE, info.getZmin(), info.getZmax(), PartType.STRAIGHT);
                generateHighwayPart(feature, info, levelZ, Transform.ROTATE_90, info.getXmax(), info.getXmax(), PartType.STRAIGHT);
            } else {
                generateHighwayPart(feature, info, levelZ, Transform.ROTATE_90, info.getXmax(), info.getXmax(), PartType.STRAIGHT);
                generateHighwayPart(feature, info, levelX, Transform.ROTATE_NONE, info.getZmin(), info.getZmax(), PartType.STRAIGHT);
            }
        } else {
            if (levelX >= 0) {
                generateHighwayPart(feature, info, levelX, Transform.ROTATE_NONE, info.getZmin(), info.getZmax(), PartType.STRAIGHT);
            } else if (levelZ >= 0) {
                generateHighwayPart(feature, info, levelZ, Transform.ROTATE_90, info.getXmax(), info.getXmax(), PartType.STRAIGHT);
            }
        }
    }

    public static boolean isClearableAboveHighway(BlockState st) {
        return !st.is(BlockTags.LEAVES) && !st.is(BlockTags.LOGS);
    }

    private static void generateHighwayPart(LostCityTerrainFeature feature, BuildingInfo info, int level, Transform transform,
                                            BuildingInfo adjacent1, BuildingInfo adjacent2, PartType partType) {
        ChunkDriver driver = feature.getDriver();
        int highwayGroundLevel = info.groundLevel + level * LostCityTerrainFeature.FLOORHEIGHT;
        HighwayParts highwayParts = info.provider.getWorldStyle().getPartSelector().highwayParts();

        BuildingPart part;
        if (info.isTunnel(level)) {
            // We know we need a tunnel
            part = AssetRegistries.PARTS.getOrThrow(info.provider.getWorld(), feature.getRandomPart(getTunnelParts(highwayParts, partType)));
            feature.generatePart(info, part, transform, 0, highwayGroundLevel, 0, LostCityTerrainFeature.HardAirSetting.WATERLEVEL);
        } else {
            if (info.isCity && level <= adjacent1.cityLevel && level <= adjacent2.cityLevel && adjacent1.isCity && adjacent2.isCity) {
                // Simple highway in the city
                part = AssetRegistries.PARTS.getOrThrow(info.provider.getWorld(), feature.getRandomPart(getOpenParts(highwayParts, partType)));
                int height = feature.generatePart(info, part, transform, 0, highwayGroundLevel, 0, LostCityTerrainFeature.HardAirSetting.WATERLEVEL);
                // Clear a bit more above the highway
                if (!info.profile.isCavern()) {
                    int clearheight = 15;
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            feature.clearRange(info, x, z, height, height + clearheight, info.waterLevel > info.groundLevel,
                                    Highways::isClearableAboveHighway);
                        }
                    }
                }
            } else {
                part = AssetRegistries.PARTS.getOrThrow(info.provider.getWorld(), feature.getRandomPart(getBridgeParts(highwayParts, partType)));
                int height = feature.generatePart(info, part, transform, 0, highwayGroundLevel, 0, LostCityTerrainFeature.HardAirSetting.WATERLEVEL);
                // Clear a bit more above the highway
                if (!info.profile.isCavern()) {
                    int clearheight = 15;
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            feature.clearRange(info, x, z, height, height + clearheight, info.waterLevel > info.groundLevel,
                                    Highways::isClearableAboveHighway);
                        }
                    }
                }
            }
        }

        Character support = part.getMetaChar(ILostCities.META_SUPPORT);
        if (info.profile.HIGHWAY_SUPPORTS && support != null) {
            BlockState sup = info.getCompiledPalette().get(support);
            if (sup == null) {
                throw new RuntimeException("Cannot find support block '" + support + "' for highway part '" + part.getName() + "'!");
            }
            generateSupport(driver, sup, transform, highwayGroundLevel, 0, 15);
            generateSupport(driver, sup, transform, highwayGroundLevel, 0, 0);
            if (partType == PartType.BEND || partType == PartType.T_JUNCTION) {
                generateSupport(driver, sup, transform, highwayGroundLevel, 15, 0);
            }
        }

        if (info.provider.getWorldStyle().getScatteredSettings() != null) {
            Set<Direction> openings = Scattered.getHighwayRailingOpenings(feature, info.coord,
                    info.provider.getWorldStyle().getScatteredSettings(), highwayGroundLevel);
            for (Direction opening : openings) {
                clearRailing(feature, info, opening, highwayGroundLevel, part.getSliceCount());
            }
        }
    }

    private static void clearRailing(LostCityTerrainFeature feature, BuildingInfo info, Direction opening,
                                     int highwayGroundLevel, int partHeight) {
        for (int i = 0; i < 16; i++) {
            int x = opening == Direction.WEST ? 0 : opening == Direction.EAST ? 15 : i;
            int z = opening == Direction.NORTH ? 0 : opening == Direction.SOUTH ? 15 : i;
            feature.clearRange(info, x, z, highwayGroundLevel + 1, highwayGroundLevel + partHeight,
                    false, state -> state.is(Blocks.IRON_BARS));
        }
    }

    private static void generateSupport(ChunkDriver driver, BlockState support, Transform transform,
                                        int highwayGroundLevel, int x, int z) {
        driver.current(transform.rotateX(x, z), highwayGroundLevel - 1, transform.rotateZ(x, z));
        for (int y = 0; y < 40; y++) {
            if (LostCityTerrainFeature.isEmpty(driver.getBlock())) {
                driver.block(support);
            } else {
                break;
            }
            driver.decY();
        }
    }

    private static List<String> getTunnelParts(HighwayParts parts, PartType type) {
        return switch (type) {
            case STRAIGHT -> parts.tunnel();
            case CROSSING -> parts.tunnelBi();
            case BEND -> parts.tunnelBend();
            case T_JUNCTION -> parts.tunnelT();
        };
    }

    private static List<String> getOpenParts(HighwayParts parts, PartType type) {
        return switch (type) {
            case STRAIGHT -> parts.open();
            case CROSSING -> parts.openBi();
            case BEND -> parts.openBend();
            case T_JUNCTION -> parts.openT();
        };
    }

    private static List<String> getBridgeParts(HighwayParts parts, PartType type) {
        return switch (type) {
            case STRAIGHT -> parts.bridge();
            case CROSSING -> parts.bridgeBi();
            case BEND -> parts.bridgeBend();
            case T_JUNCTION -> parts.bridgeT();
        };
    }

    private static Junction getJunction(HighwayInfo highwayInfo, int chunkX, int chunkZ, int level) {
        Set<Connection> connections = EnumSet.noneOf(Connection.class);
        for (HighwayInfo.RouteHit hit : highwayInfo.routeHits()) {
            if (hit.route().highwayLevel() != level) {
                continue;
            }
            for (HighwaySegment segment : hit.route().segments()) {
                if (!segment.contains(chunkX, chunkZ)) {
                    continue;
                }
                if (segment.axis() == HighwayAxis.X) {
                    if (chunkX > Math.min(segment.startX(), segment.endX())) {
                        connections.add(Connection.WEST);
                    }
                    if (chunkX < Math.max(segment.startX(), segment.endX())) {
                        connections.add(Connection.EAST);
                    }
                } else {
                    if (chunkZ > Math.min(segment.startZ(), segment.endZ())) {
                        connections.add(Connection.NORTH);
                    }
                    if (chunkZ < Math.max(segment.startZ(), segment.endZ())) {
                        connections.add(Connection.SOUTH);
                    }
                }
            }
        }

        if (connections.size() == 2 && !areOpposite(connections)) {
            return new Junction(PartType.BEND, bendTransform(connections));
        }
        if (connections.size() == 3) {
            return new Junction(PartType.T_JUNCTION, tTransform(connections));
        }
        return new Junction(PartType.CROSSING, Transform.ROTATE_NONE);
    }

    private static boolean areOpposite(Set<Connection> connections) {
        return connections.contains(Connection.NORTH) && connections.contains(Connection.SOUTH)
                || connections.contains(Connection.WEST) && connections.contains(Connection.EAST);
    }

    // The unrotated bend connects west to south.
    private static Transform bendTransform(Set<Connection> connections) {
        if (connections.contains(Connection.WEST) && connections.contains(Connection.SOUTH)) {
            return Transform.ROTATE_NONE;
        } else if (connections.contains(Connection.NORTH) && connections.contains(Connection.WEST)) {
            return Transform.ROTATE_90;
        } else if (connections.contains(Connection.NORTH) && connections.contains(Connection.EAST)) {
            return Transform.ROTATE_180;
        } else {
            return Transform.ROTATE_270;
        }
    }

    // The unrotated T connects west, east and south.
    private static Transform tTransform(Set<Connection> connections) {
        if (!connections.contains(Connection.NORTH)) {
            return Transform.ROTATE_NONE;
        } else if (!connections.contains(Connection.EAST)) {
            return Transform.ROTATE_90;
        } else if (!connections.contains(Connection.SOUTH)) {
            return Transform.ROTATE_180;
        } else {
            return Transform.ROTATE_270;
        }
    }

    private enum PartType {
        STRAIGHT,
        CROSSING,
        BEND,
        T_JUNCTION
    }

    private enum Connection {
        NORTH,
        EAST,
        SOUTH,
        WEST
    }

    private record Junction(PartType type, Transform transform) {
    }
}
