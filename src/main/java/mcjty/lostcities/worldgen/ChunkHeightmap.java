package mcjty.lostcities.worldgen;

import mcjty.lostcities.config.LandscapeType;

/**
 * A heightmap for a chunk
 */
public class ChunkHeightmap {
    private int height;
    private final LandscapeType type;
    private final int groundLevel;

    public ChunkHeightmap(LandscapeType type, int groundLevel) {
        this.groundLevel = groundLevel;
        this.type = type;
        height = Short.MIN_VALUE;
    }

    public void update(int y) {
        int current = height;
        if (y <= current) {
            return;
        }

        if (type == LandscapeType.CAVERN) {
            // Here we try to find the height inside the cavern itself. Ignoring the top layer
            int base = Math.max(groundLevel - 20, 1);
            if (y > 100 || y < base) {
                return;
            }
            if (y == 100) {
                y = 127;
            }
        }
        height = y;
    }

    public int getHeight() {
        return height;
    }
}
