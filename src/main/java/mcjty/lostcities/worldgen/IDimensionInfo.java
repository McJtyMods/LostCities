package mcjty.lostcities.worldgen;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.worldgen.lost.cityassets.WorldStyle;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Random;

public interface IDimensionInfo {
    void setWorld(ISeedReader world);

    long getSeed();

    ISeedReader getWorld();

    RegistryKey<World> getType();

    LostCityProfile getProfile();

    LostCityProfile getOutsideProfile();

    WorldStyle getWorldStyle();

    Random getRandom();

    LostCityTerrainFeature getFeature();

    ChunkHeightmap getHeightmap(int chunkX, int chunkZ);

//    Biome[] getBiomes(int chunkX, int chunkZ);

    Biome getBiome(BlockPos pos);
}
