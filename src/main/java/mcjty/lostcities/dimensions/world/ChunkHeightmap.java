package mcjty.lostcities.dimensions.world;

import mcjty.lostcities.config.LandscapeType;
import net.minecraft.block.BlockState;

/**
 * A heightmap for a chunk
 */
public class ChunkHeightmap {
    private final byte heightmap[] = new byte[16*16];
    private final LandscapeType type;
    private final int groundLevel;
    private final BlockState baseState;

    public ChunkHeightmap(LandscapeType type, int groundLevel, BlockState baseState) {
        this.groundLevel = groundLevel;
        this.type = type;
        this.baseState = baseState;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                heightmap[z * 16 + x] = 0;
            }
        }
    }

    public void update(int x, int y, int z, BlockState state) {
        BlockState air = LostCityTerrainFeature.air;
        if (state == air) {
            return;
        }

        int current = heightmap[z * 16 + x] & 0xff;
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
                heightmap[z * 16 + x] = (byte) 128;
                return;
            }
            heightmap[z * 16 + x] = (byte) y;
        } else if (type == LandscapeType.SPACE) {
            // Here we ignore the glass from the spheres (we only look at the base state)
            if (state != baseState) {
                return;
            }
            heightmap[z * 16 + x] = (byte) y;
        } else {
            heightmap[z * 16 + x] = (byte) y;
        }
    }

    public int getHeight(int x, int z) {
        return heightmap[z*16+x] & 0xff;
    }

    public int getAverageHeight() {
        int cnt = 0;
        int y = 0;
        int yy;
        yy = getHeight(2, 2);
        if (yy > 5) {
            y += yy;
            cnt++;
        }
        yy = getHeight(13, 2);
        if (yy > 5) {
            y += yy;
            cnt++;
        }
        yy = getHeight(2, 13);
        if (yy > 5) {
            y += yy;
            cnt++;
        }
        yy = getHeight(13, 13);
        if (yy > 5) {
            y += yy;
            cnt++;
        }
        yy = getHeight(8, 8);
        if (yy > 5) {
            y += yy;
            cnt++;
        }
        if (cnt > 0) {
            return y / cnt;
        } else {
            return 0;
        }
    }

    public int getMinimumHeight() {
        int y = 255;
        int yy;
        yy = getHeight(2, 2);
        if (yy < y) {
            y = yy;
        }
        yy = getHeight(13, 2);
        if (yy < y) {
            y = yy;
        }
        yy = getHeight(2, 13);
        if (yy < y) {
            y = yy;
        }
        yy = getHeight(13, 13);
        if (yy < y) {
            y = yy;
        }
        yy = getHeight(8, 8);
        if (yy < y) {
            y = yy;
        }
        return y;
    }

}
