package mcjty.lostcities.worldgen.highway;

public record HighwaySegment(int startX, int startZ, int endX, int endZ, HighwayAxis axis) {
    public HighwaySegment {
        if (axis == HighwayAxis.X && startZ != endZ) {
            throw new IllegalArgumentException("X highway segment must have a constant Z coordinate");
        }
        if (axis == HighwayAxis.Z && startX != endX) {
            throw new IllegalArgumentException("Z highway segment must have a constant X coordinate");
        }
    }

    public boolean contains(int chunkX, int chunkZ) {
        return chunkX >= Math.min(startX, endX) && chunkX <= Math.max(startX, endX)
                && chunkZ >= Math.min(startZ, endZ) && chunkZ <= Math.max(startZ, endZ);
    }

    public int length() {
        return Math.abs(endX - startX) + Math.abs(endZ - startZ);
    }
}
