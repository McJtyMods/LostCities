package mcjty.lostcities.api;

import mcjty.lostcities.varia.ChunkCoord;
import net.minecraft.core.BlockPos;

public interface ILostSphere {

    // Get the center chunk of this sphere
    ChunkCoord getCenter();

    // Get the center position of this sphere
    BlockPos getCenterPos();

    // The radius
    float getRadius();

    // Return true if this sphere is enabled. Always test for this
    boolean isEnabled();
}
