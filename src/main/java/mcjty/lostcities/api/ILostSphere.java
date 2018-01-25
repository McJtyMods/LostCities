package mcjty.lostcities.api;

import mcjty.lostcities.varia.ChunkCoord;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;

public interface ILostSphere {

    // Get the center chunk of this sphere
    ChunkCoord getCenter();

    // Get the center position of this sphere
    BlockPos getCenterPos();

    // The radius
    float getRadius();

    // If this biome is tied to a fixed biome
    @Nullable
    Biome getBiome();

    // Return true if this sphere is enabled. Always test for this
    boolean isEnabled();
}
