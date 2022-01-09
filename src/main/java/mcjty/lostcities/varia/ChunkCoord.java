package mcjty.lostcities.varia;

import mcjty.lostcities.worldgen.lost.Orientation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record ChunkCoord(ResourceKey<Level> dimension, int chunkX,
                         int chunkZ) {

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
}
