package mcjty.lostcities.worldgen;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.lost.cityassets.WorldStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;

import javax.annotation.Nullable;
import java.util.Random;

public interface IDimensionInfo {
    void setWorld(WorldGenLevel world);

    long getSeed();

    WorldGenLevel getWorld();

    ResourceKey<Level> getType();

    LostCityProfile getProfile();

    LostCityProfile getOutsideProfile();

    WorldStyle getWorldStyle();

    Random getRandom();

    LostCityTerrainFeature getFeature();

    ChunkHeightmap getHeightmap(int chunkX, int chunkZ);

    ChunkHeightmap getHeightmap(ChunkCoord coord);

//    Biome[] getBiomes(int chunkX, int chunkZ);

    Holder<Biome> getBiome(BlockPos pos);

    @Nullable
    ResourceKey<Level> dimension();
}
