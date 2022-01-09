package mcjty.lostcities.worldgen.lost;

import javax.annotation.Nonnull;

public enum Direction {
    XMIN,
    XMAX,
    ZMIN,
    ZMAX;

    public static final Direction[] VALUES = { XMIN, XMAX, ZMIN, ZMAX };

    public Orientation getOrientation() {
        return (this == XMIN || this == XMAX) ? Orientation.X : Orientation.Z;
    }

    // Rotation with xmin being 0
    public Transform getRotation() {
        return switch (this) {
            case XMIN -> Transform.ROTATE_NONE;
            case XMAX -> Transform.ROTATE_180;
            case ZMIN -> Transform.ROTATE_90;
            case ZMAX -> Transform.ROTATE_270;
        };
    }

    public Direction getOpposite() {
        return switch (this) {
            case XMIN -> XMAX;
            case XMAX -> XMIN;
            case ZMIN -> ZMAX;
            case ZMAX -> ZMIN;
        };
    }

    @Nonnull
    public BuildingInfo get(BuildingInfo info) {
        return switch (this) {
            case XMIN -> info.getXmin();
            case XMAX -> info.getXmax();
            case ZMIN -> info.getZmin();
            case ZMAX -> info.getZmax();
        };
    }

    public boolean atSide(int x, int z) {
        return switch (this) {
            case XMIN -> x == 0;
            case XMAX -> x == 15;
            case ZMIN -> z == 0;
            case ZMAX -> z == 15;
        };
    }
}
