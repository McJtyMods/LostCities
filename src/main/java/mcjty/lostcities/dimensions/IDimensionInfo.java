package mcjty.lostcities.dimensions;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.dimensions.world.ChunkHeightmap;
import mcjty.lostcities.dimensions.world.LostCityTerrainFeature;
import mcjty.lostcities.dimensions.world.lost.cityassets.WorldStyle;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;

import java.util.Random;

public interface IDimensionInfo {
    void setWorld(IWorld world);

    long getSeed();

    IWorld getWorld();

    DimensionType getType();

    LostCityProfile getProfile();

    LostCityProfile getOutsideProfile();

    WorldStyle getWorldStyle();

    Random getRandom();

    LostCityTerrainFeature getFeature();

    ChunkHeightmap getHeightmap(int chunkX, int chunkZ);

    Biome[] getBiomes(int chunkX, int chunkZ);
}
