package mcjty.lostcities.varia;

import net.minecraft.world.chunk.ChunkPrimer;

import java.util.Arrays;

public class PrimerTools {
    public static void setBlockStateRange(ChunkPrimer primer, int s, int e, char c) {
        Arrays.fill(primer.data, s, e, c);
    }

    public static void setBlockStateRangeSafe(ChunkPrimer primer, int s, int e, char c) {
        if (e <= s) {
            return;
        }
        Arrays.fill(primer.data, s, e, c);
    }
}
