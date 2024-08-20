package mcjty.lostcities.worldgen.gen;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.LostCityTerrainFeature;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.CitySphere;
import mcjty.lostcities.worldgen.lost.Transform;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.BuildingPart;
import mcjty.lostcities.worldgen.lost.regassets.data.MonorailParts;

public class Monorails {

    public static void generateMonorails(LostCityTerrainFeature feature, BuildingInfo info) {
        LostCityProfile profile = info.profile;
        IDimensionInfo provider = info.provider;
        MonorailParts monoRailParts = provider.getWorldStyle().getPartSelector().monoRailParts();
        Transform transform;
        boolean horiz = info.hasHorizontalMonorail();
        boolean vert = info.hasVerticalMonorail();
        if (horiz && vert) {
            if (!CitySphere.intersectsWithCitySphere(info.coord, provider)) {
                BuildingPart part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), monoRailParts.both());
                feature.generatePart(info, part, Transform.ROTATE_NONE, 0, profile.GROUNDLEVEL + profile.CITYSPHERE_MONORAIL_HEIGHT_OFFSET, 0, LostCityTerrainFeature.HardAirSetting.WATERLEVEL);
            }
            return;
        } else if (horiz) {
            transform = Transform.ROTATE_90;
        } else if (vert) {
            transform = Transform.ROTATE_NONE;
        } else {
            return;
        }
        BuildingPart part;

        if (CitySphere.fullyInsideCitySpere(info.coord, provider)) {
            // If there is a non-enclosed monorail nearby we generate a station
            if (hasNonStationMonoRail(info.getXmin())) {
                part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), monoRailParts.station());
                Character borderBlock = info.getCityStyle().getBorderBlock();
                transform = Transform.MIRROR_90_X; // flip
                feature.fillToGround(info, profile.GROUNDLEVEL + profile.CITYSPHERE_MONORAIL_HEIGHT_OFFSET, borderBlock);
            } else if (hasNonStationMonoRail(info.getXmax())) {
                part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), monoRailParts.station());
                Character borderBlock = info.getCityStyle().getBorderBlock();
                transform = Transform.ROTATE_90;
                feature.fillToGround(info, profile.GROUNDLEVEL + profile.CITYSPHERE_MONORAIL_HEIGHT_OFFSET, borderBlock);
            } else if (hasNonStationMonoRail(info.getZmin())) {
                part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), monoRailParts.station());
                Character borderBlock = info.getCityStyle().getBorderBlock();
                transform = Transform.ROTATE_NONE;
                feature.fillToGround(info, profile.GROUNDLEVEL + profile.CITYSPHERE_MONORAIL_HEIGHT_OFFSET, borderBlock);
            } else if (hasNonStationMonoRail(info.getZmax())) {
                part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), monoRailParts.station());
                Character borderBlock = info.getCityStyle().getBorderBlock();
                transform = Transform.MIRROR_Z; // flip
                feature.fillToGround(info, profile.GROUNDLEVEL + profile.CITYSPHERE_MONORAIL_HEIGHT_OFFSET, borderBlock);
            } else {
                return;
            }
        } else {
            part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), monoRailParts.vertical());
        }

        feature.generatePart(info, part, transform, 0, profile.GROUNDLEVEL + profile.CITYSPHERE_MONORAIL_HEIGHT_OFFSET, 0, LostCityTerrainFeature.HardAirSetting.WATERLEVEL);
    }

    private static boolean hasNonStationMonoRail(BuildingInfo info) {
        return info.hasMonorail() && !CitySphere.fullyInsideCitySpere(info.coord, info.provider);
    }
}
