package mcjty.lostcities.worldgen.street;

/** A short access road. The origin lies on an existing primary/secondary road. */
public record TertiaryRoadSegment(long id, int originX, int originZ, RoadDirection direction, int length) {
    public boolean contains(int chunkX, int chunkZ) {
        int dx = chunkX - originX;
        int dz = chunkZ - originZ;
        int distance = dx * direction.stepX() + dz * direction.stepZ();
        return distance >= 1 && distance <= length
                && dx * direction.stepZ() == dz * direction.stepX();
    }
}
