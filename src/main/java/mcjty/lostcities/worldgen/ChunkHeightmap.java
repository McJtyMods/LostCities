package mcjty.lostcities.worldgen;

import mcjty.lostcities.config.LandscapeType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A heightmap for a chunk
 */
public class ChunkHeightmap {
    private final short[] heightmap = new short[16*16];
    private final LandscapeType type;
    private final int groundLevel;
    private final BlockState baseState;
    private Integer maxHeight = null;
    private Integer minHeight = null;
    private Integer avgHeight = null;

    public ChunkHeightmap(LandscapeType type, int groundLevel, BlockState baseState) {
        this.groundLevel = groundLevel;
        this.type = type;
        this.baseState = baseState;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                heightmap[z * 16 + x] = Short.MIN_VALUE;
            }
        }
    }

    public void update(int x, int y, int z, BlockState state) {
        BlockState air = LostCityTerrainFeature.air;
        if (state == air) {
            return;
        }

        int current = heightmap[z * 16 + x];
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
                heightmap[z * 16 + x] = (byte) 127;
                return;
            }
            heightmap[z * 16 + x] = (short) y;
        } else if (type == LandscapeType.SPACE) {
            // Here we ignore the glass from the spheres (we only look at the base state)
            if (state != baseState) {
                return;
            }
            heightmap[z * 16 + x] = (short) y;
        } else {
            heightmap[z * 16 + x] = (short) y;
        }
    }

    public int getHeight(int x, int z) {
        return heightmap[z*16+x];
    }

    public void setHeight(int x, int z, int height) {
        heightmap[z*16+x] = (short) height;
    }

    private void calculateHeightInfo() {
        int max = Short.MIN_VALUE;
        int min = Short.MAX_VALUE;
        int avg = 0;
        for (int x = 0 ; x < 16 ; x++) {
            for (int z = 0 ; z < 16 ; z++) {
                int h = getHeight(x, z);
                if (h > max) {
                    max = h;
                }
                if (h < min) {
                    min = h;
                }
                avg += h;
            }
        }
        avgHeight = avg / 256;
        minHeight = min;
        maxHeight = max;
    }

    public int getAverageHeight() {
        if (avgHeight == null) {
            calculateHeightInfo();
        }
        return avgHeight;

    }

    public int getMinimumHeight() {
        if (minHeight == null) {
            calculateHeightInfo();
        }
        return minHeight;
    }

    public int getMaximumHeight() {
        if (maxHeight == null) {
            calculateHeightInfo();
        }
        return maxHeight;
    }

}
