package mcjty.lostcities.varia;

import mcjty.lostcities.worldgen.lost.Orientation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record ChunkCoord(ResourceKey<Level> dimension, int chunkX, int chunkZ) {

    public ChunkCoord offset(int dx, int dz) {
        return new ChunkCoord(dimension, chunkX + dx, chunkZ + dz);
    }

    public ChunkCoord east() {
        return new ChunkCoord(dimension, chunkX + 1, chunkZ);
    }

    public ChunkCoord west() {
        return new ChunkCoord(dimension, chunkX - 1, chunkZ);
    }

    public ChunkCoord north() {
        return new ChunkCoord(dimension, chunkX, chunkZ - 1);
    }

    public ChunkCoord south() {
        return new ChunkCoord(dimension, chunkX, chunkZ + 1);
    }

    public ChunkCoord northWest() {
        return new ChunkCoord(dimension, chunkX - 1, chunkZ - 1);
    }

    public ChunkCoord northEast() {
        return new ChunkCoord(dimension, chunkX + 1, chunkZ - 1);
    }

    public ChunkCoord southWest() {
        return new ChunkCoord(dimension, chunkX - 1, chunkZ + 1);
    }

    public ChunkCoord southEast() {
        return new ChunkCoord(dimension, chunkX + 1, chunkZ + 1);
    }

    public ChunkCoord lower(Orientation o) {
        return switch (o) {
            case X -> new ChunkCoord(dimension, chunkX - 1, chunkZ);
            case Z -> new ChunkCoord(dimension, chunkX, chunkZ - 1);
        };
    }

    public ChunkCoord higher(Orientation o) {
        return switch (o) {
            case X -> new ChunkCoord(dimension, chunkX + 1, chunkZ);
            case Z -> new ChunkCoord(dimension, chunkX, chunkZ + 1);
        };
    }

    public int getCoord(Orientation o) {
        return switch (o) {
            case X -> chunkX;
            case Z -> chunkZ;
        };
    }

    @Override
    public String toString() {
        return "ChunkCoord{" +
                "dimension=" + dimension +
                ", chunkX=" + chunkX +
                ", chunkZ=" + chunkZ +
                '}';
    }

    public ChunkCoord neighbour(Direction direction) {
        return switch (direction) {
            case NORTH -> north();
            case SOUTH -> south();
            case WEST -> west();
            case EAST -> east();
            default -> this;
        };
    }
}
