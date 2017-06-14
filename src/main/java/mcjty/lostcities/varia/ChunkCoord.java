package mcjty.lostcities.varia;

import mcjty.lostcities.dimensions.world.lost.Orientation;

public class ChunkCoord {
    private final int chunkX;
    private final int chunkZ;

    public ChunkCoord(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
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
                return new ChunkCoord(chunkX-1, chunkZ);
            case Z:
                return new ChunkCoord(chunkX, chunkZ-1);
        }
        throw new IllegalArgumentException("Cannot happen!");
    }

    public ChunkCoord higher(Orientation o) {
        switch (o) {
            case X:
                return new ChunkCoord(chunkX+1, chunkZ);
            case Z:
                return new ChunkCoord(chunkX, chunkZ+1);
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

        if (chunkX != that.chunkX) return false;
        if (chunkZ != that.chunkZ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = chunkX;
        result = 31 * result + chunkZ;
        return result;
    }
}
