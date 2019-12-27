package mcjty.lostcities.worldgen.lost;

import mcjty.lostcities.api.ILostExplosion;
import net.minecraft.util.math.BlockPos;

public class Explosion implements ILostExplosion {
    private final int radius;
    private final int sqradius;
    private final BlockPos center;

    public Explosion(int radius, BlockPos center) {
        this.radius = radius;
        this.center = center;
        sqradius = radius * radius;
    }

    @Override
    public int getRadius() {
        return radius;
    }

    public int getSqradius() {
        return sqradius;
    }

    @Override
    public BlockPos getCenter() {
        return center;
    }
}
