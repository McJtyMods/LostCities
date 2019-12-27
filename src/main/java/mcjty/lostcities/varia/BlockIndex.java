package mcjty.lostcities.varia;

import mcjty.lostcities.worldgen.lost.Orientation;

/**
 * An index of a block as used in the ChunkPrimer
 */
public class BlockIndex {

    private int index;

    public BlockIndex(int index) {
        this.index = index;
    }

    public BlockIndex(int x, int y, int z) {
        index = (x << 12) | (z << 8) + y;
    }

    public BlockIndex(int x, int y, int z, Orientation orientation) {
        switch (orientation) {
            case X:
                index = (x << 12) | (z << 8) + y;
                break;
            case Z:
                index = (z << 12) | (x << 8) + y;
                break;
        }
        throw new IllegalArgumentException("Cannot happen!");
    }

    public void incY() {
        index++;
    }

    public int getIndex() {
        return index;
    }

    public int getIndex(Orientation o, int amount) {
        return index;
    }
}
