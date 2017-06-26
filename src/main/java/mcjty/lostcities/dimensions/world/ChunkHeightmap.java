package mcjty.lostcities.dimensions.world;

import net.minecraft.block.Block;
import net.minecraft.world.chunk.ChunkPrimer;

/**
 * A heightmap for a chunk
 */
public class ChunkHeightmap {
    private byte heightmap[] = new byte[16*16];

    public ChunkHeightmap(ChunkPrimer primer) {
        char air = LostCitiesTerrainGenerator.airChar;
        for (int x = 0 ; x < 16 ; x++) {
            for (int z = 0 ; z < 16 ; z++) {
                int index = (x << 12) | (z << 8);
                int y = 255;
                while (y > 0 && primer.data[index + y] == air) {
                    y--;
                }
                heightmap[z*16+x] = (byte) y;
            }
        }
    }

    public int getHeight(int x, int z) {
        return heightmap[z*16+x] & 0xff;
    }
}
