package mcjty.lostcities.setup;

import mcjty.lostcities.api.LostCityEvent;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.City;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.CityStyle;
import mcjty.lostcities.worldgen.lost.cityassets.MultiBuilding;
import mcjty.lostcities.worldgen.lost.regassets.data.DataTools;
import net.neoforged.bus.EventBus;
import net.neoforged.bus.ListenerList;
import net.neoforged.neoforge.common.NeoForge;

import java.lang.reflect.Method;
import java.util.Set;

final class SpawnBuildingPrefilter {
    private final IDimensionInfo provider;
    private final LostCityProfile profile;
    private final Set<String> buildings;
    private final boolean rawCityCheck;
    private final boolean cityStyleCheck;

    private SpawnBuildingPrefilter(IDimensionInfo provider, LostCityProfile profile, Set<String> buildings,
                                   boolean rawCityCheck, boolean cityStyleCheck) {
        this.provider = provider;
        this.profile = profile;
        this.buildings = buildings;
        this.rawCityCheck = rawCityCheck;
        this.cityStyleCheck = cityStyleCheck;
    }

    static SpawnBuildingPrefilter create(IDimensionInfo provider, LostCityProfile profile, Set<String> buildings) {
        if (buildings.isEmpty() || profile.isSpace() || profile.isSpheres() || hasCharacteristicsListeners()) {
            return new SpawnBuildingPrefilter(provider, profile, buildings, false, false);
        }
        try {
            AssetRegistries.MULTI_BUILDINGS.loadAll(provider.getWorld());
            for (MultiBuilding multi : AssetRegistries.MULTI_BUILDINGS.getIterable()) {
                for (String name : multi.getBuildingSet()) {
                    if (buildings.contains(DataTools.fromName(name).toString())) {
                        return new SpawnBuildingPrefilter(provider, profile, buildings, true, false);
                    }
                }
            }
        } catch (RuntimeException exception) {
            return new SpawnBuildingPrefilter(provider, profile, buildings, true, false);
        }
        return new SpawnBuildingPrefilter(provider, profile, buildings, true, true);
    }

    Result test(ChunkCoord coord) {
        if (!rawCityCheck) {
            return Result.PASS;
        }
        if (!BuildingInfo.isCityRaw(coord, provider, profile)) {
            return Result.NOT_CITY;
        }
        if (!cityStyleCheck) {
            return Result.PASS;
        }
        if (City.isChunkOccupied(provider, coord)) {
            return Result.PASS;
        }
        CityStyle style = City.getCityStyle(coord, provider, profile);
        if (style == null) {
            return Result.PASS;
        }
        return style.maySelectBuilding(buildings) ? Result.PASS : Result.STYLE_MISMATCH;
    }

    private static boolean hasCharacteristicsListeners() {
        try {
            // NeoForge keeps listener lists on each bus instead of on the event.
            // It has no public listener query; if inspection is unavailable, keep
            // the full search so integrations can still change characteristics.
            Method lookup = EventBus.class.getDeclaredMethod("getListenerList", Class.class);
            if (!lookup.trySetAccessible()) {
                return true;
            }
            ListenerList list = (ListenerList) lookup.invoke(NeoForge.EVENT_BUS, LostCityEvent.CharacteristicsEvent.class);
            return list.getListeners().length > 0;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return true;
        }
    }

    enum Result {
        PASS,
        NOT_CITY,
        STYLE_MISMATCH
    }
}
