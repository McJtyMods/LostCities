package mcjty.lostcities.dimensions.world.lost;

import net.minecraft.block.BlockRailBase;
import net.minecraft.block.state.IBlockState;

public enum Transform {
    ROTATE_NONE(net.minecraft.util.Rotation.NONE),
    ROTATE_90(net.minecraft.util.Rotation.CLOCKWISE_90),
    ROTATE_180(net.minecraft.util.Rotation.CLOCKWISE_180),
    ROTATE_270(net.minecraft.util.Rotation.COUNTERCLOCKWISE_90),
    MIRROR_X(net.minecraft.util.Rotation.CLOCKWISE_180);

    private final net.minecraft.util.Rotation mcRotation;

    Transform(net.minecraft.util.Rotation mcRotation) {
        this.mcRotation = mcRotation;
    }

    public net.minecraft.util.Rotation getMcRotation() {
        return mcRotation;
    }

    public Transform getOpposite() {
        switch (this) {
            case ROTATE_NONE:
                return ROTATE_NONE;
            case ROTATE_270:
                return ROTATE_90;
            case ROTATE_180:
                return ROTATE_180;
            case ROTATE_90:
                return ROTATE_270;
            case MIRROR_X:
                return MIRROR_X;
        }
        throw new IllegalStateException("Cannot happen!");
    }

    public int rotateX(int x, int z) {
        switch (this) {
            case ROTATE_NONE:
                return x;
            case ROTATE_90:
                return 15-z;
            case ROTATE_180:
                return 15-x;
            case ROTATE_270:
                return z;
            case MIRROR_X:
                return 15-x;
        }
        throw new IllegalStateException("Cannot happen!");
    }

    public int rotateZ(int x, int z) {
        switch (this) {
            case ROTATE_NONE:
                return z;
            case ROTATE_90:
                return x;
            case ROTATE_180:
                return 15-z;
            case ROTATE_270:
                return 15-x;
            case MIRROR_X:
                return z;
        }
        throw new IllegalStateException("Cannot happen!");
    }

    int rotateW(int w, int h) {
        switch (this) {
            case ROTATE_NONE:
            case ROTATE_180:
            case MIRROR_X:
                return w;
            case ROTATE_90:
            case ROTATE_270:
                return h;
        }
        throw new IllegalStateException("Cannot happen!");
    }

    int rotateH(int w, int h) {
        switch (this) {
            case ROTATE_NONE:
            case ROTATE_180:
            case MIRROR_X:
                return h;
            case ROTATE_90:
            case ROTATE_270:
                return w;
        }
        throw new IllegalStateException("Cannot happen!");
    }

    public BlockRailBase.EnumRailDirection transform(BlockRailBase.EnumRailDirection shape) {
        if (this == ROTATE_NONE) {
            return shape;
        }
        switch (shape) {
            case NORTH_SOUTH:
                return (this == ROTATE_90 || this == ROTATE_270) ? BlockRailBase.EnumRailDirection.EAST_WEST : shape;
            case EAST_WEST:
                return (this == ROTATE_90 || this == ROTATE_270) ? BlockRailBase.EnumRailDirection.NORTH_SOUTH : shape;
            case ASCENDING_EAST:
                switch (this) {
                    case ROTATE_90:
                        return BlockRailBase.EnumRailDirection.ASCENDING_SOUTH;
                    case ROTATE_180:
                        return BlockRailBase.EnumRailDirection.ASCENDING_WEST;
                    case ROTATE_270:
                        return BlockRailBase.EnumRailDirection.ASCENDING_NORTH;
                    case MIRROR_X:
                        return BlockRailBase.EnumRailDirection.ASCENDING_WEST;
                }
                break;
            case ASCENDING_WEST:
                switch (this) {
                    case ROTATE_90:
                        return BlockRailBase.EnumRailDirection.ASCENDING_NORTH;
                    case ROTATE_180:
                        return BlockRailBase.EnumRailDirection.ASCENDING_EAST;
                    case ROTATE_270:
                        return BlockRailBase.EnumRailDirection.ASCENDING_SOUTH;
                    case MIRROR_X:
                        return BlockRailBase.EnumRailDirection.ASCENDING_EAST;
                }
                break;
            case ASCENDING_NORTH:
                switch (this) {
                    case ROTATE_90:
                        return BlockRailBase.EnumRailDirection.ASCENDING_EAST;
                    case ROTATE_180:
                        return BlockRailBase.EnumRailDirection.ASCENDING_SOUTH;
                    case ROTATE_270:
                        return BlockRailBase.EnumRailDirection.ASCENDING_WEST;
                    case MIRROR_X:
                        return BlockRailBase.EnumRailDirection.ASCENDING_SOUTH;
                }
                break;
            case ASCENDING_SOUTH:
                switch (this) {
                    case ROTATE_90:
                        return BlockRailBase.EnumRailDirection.ASCENDING_WEST;
                    case ROTATE_180:
                        return BlockRailBase.EnumRailDirection.ASCENDING_NORTH;
                    case ROTATE_270:
                        return BlockRailBase.EnumRailDirection.ASCENDING_EAST;
                    case MIRROR_X:
                        return BlockRailBase.EnumRailDirection.ASCENDING_NORTH;
                }
                break;
            case SOUTH_EAST:
                switch (this) {
                    case ROTATE_90:
                        return BlockRailBase.EnumRailDirection.SOUTH_WEST;
                    case ROTATE_180:
                        return BlockRailBase.EnumRailDirection.NORTH_WEST;
                    case ROTATE_270:
                        return BlockRailBase.EnumRailDirection.NORTH_EAST;
                    case MIRROR_X:
                        return BlockRailBase.EnumRailDirection.SOUTH_WEST;
                }
                break;
            case SOUTH_WEST:
                switch (this) {
                    case ROTATE_90:
                        return BlockRailBase.EnumRailDirection.NORTH_WEST;
                    case ROTATE_180:
                        return BlockRailBase.EnumRailDirection.NORTH_EAST;
                    case ROTATE_270:
                        return BlockRailBase.EnumRailDirection.SOUTH_EAST;
                    case MIRROR_X:
                        return BlockRailBase.EnumRailDirection.SOUTH_EAST;
                }
                break;
            case NORTH_WEST:
                switch (this) {
                    case ROTATE_90:
                        return BlockRailBase.EnumRailDirection.NORTH_EAST;
                    case ROTATE_180:
                        return BlockRailBase.EnumRailDirection.SOUTH_EAST;
                    case ROTATE_270:
                        return BlockRailBase.EnumRailDirection.SOUTH_WEST;
                    case MIRROR_X:
                        return BlockRailBase.EnumRailDirection.NORTH_EAST;
                }
                break;
            case NORTH_EAST:
                switch (this) {
                    case ROTATE_90:
                        return BlockRailBase.EnumRailDirection.SOUTH_EAST;
                    case ROTATE_180:
                        return BlockRailBase.EnumRailDirection.SOUTH_WEST;
                    case ROTATE_270:
                        return BlockRailBase.EnumRailDirection.NORTH_WEST;
                    case MIRROR_X:
                        return BlockRailBase.EnumRailDirection.NORTH_WEST;
                }
                break;
        }
        throw new IllegalStateException("Cannot happen!");
    }

    public static void main(String[] args) {
        int x;
        int z;
        x = 4; z = 4;
        System.out.println("x,z = " + x +","+z + " -> " +ROTATE_90.rotateX(x, z) +","+ROTATE_90.rotateZ(x,z));
    }
}
