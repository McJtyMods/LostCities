package mcjty.lostcities.dimensions;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.dimensions.world.ChunkHeightmap;
import mcjty.lostcities.dimensions.world.driver.IPrimerDriver;
import mcjty.lostcities.dimensions.world.lost.cityassets.WorldStyle;
import net.minecraft.world.IWorld;

import java.util.Random;

public interface IDimensionInfo {
    long getSeed();

    IWorld getWorld();

    LostCityProfile getProfile();

    LostCityProfile getOutsideProfile();

    WorldStyle getWorldStyle();

    ChunkHeightmap getHeightmap(int chunkX, int chunkZ);

    Random getRandom();

    ICityCarver getCarver();
}
