package mcjty.lostcities.worldgen.street;

public enum RoadDirection {
    NORTH(0, -1),
    SOUTH(0, 1),
    WEST(-1, 0),
    EAST(1, 0);

    private final int stepX;
    private final int stepZ;

    RoadDirection(int stepX, int stepZ) {
        this.stepX = stepX;
        this.stepZ = stepZ;
    }

    public int stepX() {
        return stepX;
    }

    public int stepZ() {
        return stepZ;
    }
}
