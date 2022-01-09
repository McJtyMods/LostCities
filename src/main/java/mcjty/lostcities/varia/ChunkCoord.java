package mcjty.lostcities.varia;

import mcjty.lostcities.worldgen.lost.Orientation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class ChunkCoord {
    private final ResourceKey<Level> dimension;
    private final int chunkX;
    private final int chunkZ;

    public ChunkCoord(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        this.dimension = dimension;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public ChunkCoord lower(Orientation o) {
        switch (o) {
            case X:
                return new ChunkCoord(dimension, chunkX-1, chunkZ);
            case Z:
                return new ChunkCoord(dimension, chunkX, chunkZ-1);
        }
        throw new IllegalArgumentException("Cannot happen!");
    }

    public ChunkCoord higher(Orientation o) {
        switch (o) {
            case X:
                return new ChunkCoord(dimension, chunkX+1, chunkZ);
            case Z:
                return new ChunkCoord(dimension, chunkX, chunkZ+1);
        }
        throw new IllegalArgumentException("Cannot happen!");
    }

    public int getCoord(Orientation o) {
        switch (o) {
            case X:
                return chunkX;
            case Z:
                return chunkZ;
        }
        throw new IllegalArgumentException("Cannot happen!");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChunkCoord that = (ChunkCoord) o;

        if (dimension != that.dimension) return false;
        if (chunkX != that.chunkX) return false;
        if (chunkZ != that.chunkZ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = dimension.getRegistryName().hashCode();
        result = 31 * result + chunkX;
        result = 31 * result + chunkZ;
        return result;
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
