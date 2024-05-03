package mcjty.lostcities;

import mcjty.lostcities.api.*;
import mcjty.lostcities.setup.Config;
import mcjty.lostcities.setup.Registration;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class LostCitiesImp implements ILostCities {

    private final Map<ResourceKey<Level>, LostCityInformation> info = new HashMap<>();

    public void cleanUp() {
        info.clear();
    }

    @Nullable
    @Override
    public ILostCityInformation getLostInfo(Level world) {
        IDimensionInfo dimensionInfo = Registration.LOSTCITY_FEATURE.get().getDimensionInfo((WorldGenLevel) world);
        if (dimensionInfo != null) {
            if (!info.containsKey(world.dimension())) {
                LostCityInformation gen = new LostCityInformation(dimensionInfo);
                info.put(world.dimension(), gen);
            }
            return info.get(world.dimension());
        }
        return null;
    }

    @Override
    public void registerDimension(ResourceKey<Level> key, String profile) {
        Config.registerLostCityDimension(key, profile);
    }

    public record LostCityInformation(IDimensionInfo dimensionInfo) implements ILostCityInformation {

        @Override
        public ILostChunkInfo getChunkInfo(int chunkX, int chunkZ) {
            ChunkCoord coord = new ChunkCoord(dimensionInfo.getType(), chunkX, chunkZ);
            return BuildingInfo.getBuildingInfo(coord, dimensionInfo);
        }

        @Override
        public ILostSphere getSphere(int x, int y, int z) {
            if (dimensionInfo.getProfile().isSpheres() || dimensionInfo.getProfile().isSpace()) {
                return BuildingInfo.getSphereInt(x, y, z, dimensionInfo);
            } else {
                return null;
            }
        }

        @Override
        public ILostSphere getSphere(int x, int z) {
            if (dimensionInfo.getProfile().isSpheres() || dimensionInfo.getProfile().isSpace()) {
                return BuildingInfo.getSphereInt(x, z, dimensionInfo);
            } else {
                return null;
            }
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
