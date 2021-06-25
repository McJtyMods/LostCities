package mcjty.lostcities;

import mcjty.lostcities.api.*;
import mcjty.lostcities.setup.Registration;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class LostCitiesImp implements ILostCities {

    private final Map<RegistryKey<World>, LostCityInformation> info = new HashMap<>();

    @Nullable
    @Override
    public ILostCityInformation getLostInfo(World world) {
        IDimensionInfo dimensionInfo = Registration.LOSTCITY_FEATURE.getDimensionInfo((ISeedReader) world);
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
