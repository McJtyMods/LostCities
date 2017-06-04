package mcjty.lostcities.dimensions.world.lost;

import net.minecraft.util.math.BlockPos;

public class Explosion {
    private final int radius;
    private final int sqradius;
    private final BlockPos center;

    public Explosion(int radius, BlockPos center) {
        this.radius = radius;
        this.center = center;
        sqradius = radius * radius;
    }

    public int getRadius() {
        return radius;
    }

    public int getSqradius() {
        return sqradius;
    }

    public BlockPos getCenter() {
        return center;
    }
}
