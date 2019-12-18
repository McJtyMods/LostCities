package mcjty.lostcities.dimensions.world;

import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.carver.CaveWorldCarver;
import net.minecraft.world.gen.feature.ProbabilityConfig;

import java.util.BitSet;
import java.util.Random;

public class LostGenCaves extends CaveWorldCarver {

    private final LostCityChunkGenerator provider;

    public LostGenCaves(LostCityChunkGenerator provider) {
        super(ProbabilityConfig::deserialize, 256);
        this.provider = provider;
    }

    @Override
    public boolean carve(IChunk chunkIn, Random rand, int seaLevel, int chunkX, int chunkZ, int originalX, int originalZ, BitSet carvingMask, ProbabilityConfig config) {
        int i = (this.func_222704_c() * 2 - 1) * 16;
        int j = rand.nextInt(rand.nextInt(rand.nextInt(this.func_222724_a()) + 1) + 1);

        for(int k = 0; k < j; ++k) {
            double d0 = (double)(chunkX * 16 + rand.nextInt(16));
            double d1 = (double) rand.nextInt(rand.nextInt(provider.getProfile().MAX_CAVE_HEIGHT - 8) + 8);
            double d2 = (double)(chunkZ * 16 + rand.nextInt(16));
            int l = 1;
            if (rand.nextInt(4) == 0) {
                double d3 = 0.5D;
                float f1 = 1.0F + rand.nextFloat() * 6.0F;
                this.func_222723_a(chunkIn, rand.nextLong(), seaLevel, originalX, originalZ, d0, d1, d2, f1, 0.5D, carvingMask);
                l += rand.nextInt(4);
            }

            for(int k1 = 0; k1 < l; ++k1) {
                float f = rand.nextFloat() * ((float)Math.PI * 2F);
                float f3 = (rand.nextFloat() - 0.5F) / 4.0F;
                float f2 = this.generateCaveRadius(rand);
                int i1 = i - rand.nextInt(i / 4);
                int j1 = 0;
                this.carveTunnel(chunkIn, rand.nextLong(), seaLevel, originalX, originalZ, d0, d1, d2, f2, f, f3, 0, i1, this.func_222725_b(), carvingMask);
            }
        }

        return true;
    }
}

