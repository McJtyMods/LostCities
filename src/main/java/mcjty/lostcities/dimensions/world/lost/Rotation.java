package mcjty.lostcities.dimensions.world.lost;

public enum Rotation {
    ROTATE_NONE(net.minecraft.util.Rotation.NONE),
    ROTATE_90(net.minecraft.util.Rotation.CLOCKWISE_90),
    ROTATE_180(net.minecraft.util.Rotation.CLOCKWISE_180),
    ROTATE_270(net.minecraft.util.Rotation.COUNTERCLOCKWISE_90);

    private final net.minecraft.util.Rotation mcRotation;

    Rotation(net.minecraft.util.Rotation mcRotation) {
        this.mcRotation = mcRotation;
    }

    public net.minecraft.util.Rotation getMcRotation() {
        return mcRotation;
    }

    public Rotation getOpposite() {
        switch (this) {
            case ROTATE_NONE:
                return ROTATE_NONE;
            case ROTATE_270:
                return ROTATE_90;
            case ROTATE_180:
                return ROTATE_180;
            case ROTATE_90:
                return ROTATE_270;
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
        }
        throw new IllegalStateException("Cannot happen!");
    }

    int rotateW(int w, int h) {
        switch (this) {
            case ROTATE_NONE:
            case ROTATE_180:
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
                return h;
            case ROTATE_90:
            case ROTATE_270:
                return w;
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
