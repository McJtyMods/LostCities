package mcjty.lostcities;

import mcjty.lostcities.api.*;
import mcjty.lostcities.setup.Registration;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class LostCitiesImp implements ILostCities {

    private final Map<ResourceKey<Level>, LostCityInformation> info = new HashMap<>();

    @Nullable
    @Override
    public ILostCityInformation getLostInfo(Level world) {
        IDimensionInfo dimensionInfo = Registration.LOSTCITY_FEATURE.getDimensionInfo((WorldGenLevel) world);
        if (dimensionInfo != null) {
            if (!info.containsKey(world.dimension())) {
                LostCityInformation gen = new LostCityInformation(dimensionInfo);
                info.put(world.dimension(), gen);
            }
            return info.get(world.dimension());
        }
        return null;
    }

    public static class LostCityInformation implements ILostCityInformation {

        private final IDimensionInfo dimensionInfo;

        public LostCityInformation(IDimensionInfo dimensionInfo) {
            this.dimensionInfo = dimensionInfo;
        }

        @Override
        public ILostChunkInfo getChunkInfo(int chunkX, int chunkZ) {
            return BuildingInfo.getBuildingInfo(chunkX, chunkZ, dimensionInfo);
        }

        @Override
        public int getRealHeight(int level) {
            return dimensionInfo.getProfile().GROUNDLEVEL + level * 6;
        }

        @Override
        public ILostCityAssetRegistry<ILostCityBuilding> getBuildings() {
            return AssetRegistries.BUILDINGS.cast();
        }

        @Override
        public ILostCityAssetRegistry<ILostCityMultiBuilding> getMultiBuildings() {
            return AssetRegistries.MULTI_BUILDINGS.cast();
        }

        @Override
        public ILostCityAssetRegistry<ILostCityCityStyle> getCityStyles() {
            return AssetRegistries.CITYSTYLES.cast();
        }
    }
}
