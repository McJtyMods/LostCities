package mcjty.lostcities.dimensions.world.lost;

public enum Orientation {
    X,
    Z;

    public Direction getMinDir() {
        return this == X ? Direction.XMIN : Direction.ZMIN;
    }

    public Direction getMaxDir() {
        return this == X ? Direction.XMAX : Direction.ZMAX;
    }

    public Orientation getOpposite() {
        return this == X ? Z : X;
    }
}
