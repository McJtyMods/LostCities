package mcjty.lostcities.worldgen.gen;

import mcjty.lostcities.api.ILostCities;
import mcjty.lostcities.worldgen.ChunkDriver;
import mcjty.lostcities.worldgen.LostCityTerrainFeature;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.Highway;
import mcjty.lostcities.worldgen.lost.Transform;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.BuildingPart;
import mcjty.lostcities.worldgen.lost.regassets.data.HighwayParts;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;

public class Highways {
    public static void generateHighways(LostCityTerrainFeature feature, BuildingInfo info) {
        int levelX = Highway.getXHighwayLevel(info.coord, info.provider, info.profile);
        int levelZ = Highway.getZHighwayLevel(info.coord, info.provider, info.profile);
        if (levelX == levelZ && levelX >= 0) {
            // Crossing
            generateHighwayPart(feature, info, levelX, Transform.ROTATE_NONE, info.getXmax(), info.getZmax(), true);
        } else if (levelX >= 0 && levelZ >= 0) {
            // There are two highways on different level. Make sure the lowest one is done first because it
            // will clear out what is above it
            if (levelX == 0) {
                generateHighwayPart(feature, info, levelX, Transform.ROTATE_NONE, info.getZmin(), info.getZmax(), false);
                generateHighwayPart(feature, info, levelZ, Transform.ROTATE_90, info.getXmax(), info.getXmax(), false);
            } else {
                generateHighwayPart(feature, info, levelZ, Transform.ROTATE_90, info.getXmax(), info.getXmax(), false);
                generateHighwayPart(feature, info, levelX, Transform.ROTATE_NONE, info.getZmin(), info.getZmax(), false);
            }
        } else {
            if (levelX >= 0) {
                generateHighwayPart(feature, info, levelX, Transform.ROTATE_NONE, info.getZmin(), info.getZmax(), false);
            } else if (levelZ >= 0) {
                generateHighwayPart(feature, info, levelZ, Transform.ROTATE_90, info.getXmax(), info.getXmax(), false);
            }
        }
    }

    public static boolean isClearableAboveHighway(BlockState st) {
        return !st.is(BlockTags.LEAVES) && !st.is(BlockTags.LOGS);
    }

    private static void generateHighwayPart(LostCityTerrainFeature feature, BuildingInfo info, int level, Transform transform, BuildingInfo adjacent1, BuildingInfo adjacent2, boolean bidirectional) {
        ChunkDriver driver = feature.driver;
        int highwayGroundLevel = info.groundLevel + level * LostCityTerrainFeature.FLOORHEIGHT;
        HighwayParts highwayParts = info.provider.getWorldStyle().getPartSelector().highwayParts();

        BuildingPart part;
        if (info.isTunnel(level)) {
            // We know we need a tunnel
            part = AssetRegistries.PARTS.getOrThrow(info.provider.getWorld(), feature.getRandomPart(highwayParts.tunnel(bidirectional)));
            feature.generatePart(info, part, transform, 0, highwayGroundLevel, 0, LostCityTerrainFeature.HardAirSetting.WATERLEVEL);
        } else {
            if (info.isCity && level <= adjacent1.cityLevel && level <= adjacent2.cityLevel && adjacent1.isCity && adjacent2.isCity) {
                // Simple highway in the city
                part = AssetRegistries.PARTS.getOrThrow(info.provider.getWorld(), feature.getRandomPart(highwayParts.open(bidirectional)));
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
                part = AssetRegistries.PARTS.getOrThrow(info.provider.getWorld(), feature.getRandomPart(highwayParts.bridge(bidirectional)));
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
            int x1 = transform.rotateX(0, 15);
            int z1 = transform.rotateZ(0, 15);
            driver.current(x1, highwayGroundLevel - 1, z1);
            for (int y = 0; y < 40; y++) {
                if (LostCityTerrainFeature.isEmpty(driver.getBlock())) {
                    driver.block(sup);
                } else {
                    break;
                }
                driver.decY();
            }

            int x2 = transform.rotateX(0, 0);
            int z2 = transform.rotateZ(0, 0);
            driver.current(x2, highwayGroundLevel - 1, z2);
            for (int y = 0; y < 40; y++) {
                if (LostCityTerrainFeature.isEmpty(driver.getBlock())) {
                    driver.block(sup);
                } else {
                    break;
                }
                driver.decY();
            }
        }
    }
}
