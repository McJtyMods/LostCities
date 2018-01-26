package mcjty.lostcities.dimensions.world;

import mcjty.lostcities.dimensions.world.terraingen.LostCitiesTerrainGenerator;
import net.minecraft.world.chunk.ChunkPrimer;

/**
 * A heightmap for a chunk
 */
public class ChunkHeightmap {
    private byte heightmap[] = new byte[16*16];

    public ChunkHeightmap(ChunkPrimer primer, boolean cavern, int groundLevel) {
        char air = LostCitiesTerrainGenerator.airChar;

        if (cavern) {
            // Here we try to find the height inside the cavern itself. Ignoring the top layer
            int base = Math.max(groundLevel-20, 1);
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int index = (x << 12) | (z << 8);
                    int y = base;
                    while (y < 100 && primer.data[index + y] != air) {
                        y++;
                    }
                    if (y >= 100) {
                        y = 128;
                    }
                    if (y < 100) {
                        while (y > 0 && primer.data[index + y] == air) {
                            y--;
                        }
                    }
                    heightmap[z * 16 + x] = (byte) y;
                }
            }
        } else {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int index = (x << 12) | (z << 8);
                    int y = 255;
                    while (y > 0 && primer.data[index + y] == air) {
                        y--;
                    }
                    heightmap[z * 16 + x] = (byte) y;
                }
            }
        }
    }

    public int getHeight(int x, int z) {
        return heightmap[z*16+x] & 0xff;
    }
}
