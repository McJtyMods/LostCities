package mcjty.lostcities.varia;

import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

// @todo 1.15 copy from 1.14: use the 1.15 version!
public class PerlinNoiseGenerator14 {
   private final SimplexNoise[] noiseLevels;
   private final int levels;

   public PerlinNoiseGenerator14(long seed, int levelsIn) {
      this.levels = levelsIn;
      this.noiseLevels = new SimplexNoise[levelsIn];

      for(int i = 0; i < levelsIn; ++i) {
         this.noiseLevels[i] = new SimplexNoise(new LegacyRandomSource(seed));
      }

   }

   public double getValue(double x, double z) {
      return this.noiseAt(x, z, false);
   }

   public double noiseAt(double x, double y, boolean useNoiseOffsets) {
      double d0 = 0.0D;
      double d1 = 1.0D;

      for(int i = 0; i < this.levels; ++i) {
         d0 += this.noiseLevels[i].getValue(x * d1 + (useNoiseOffsets ? this.noiseLevels[i].xo : 0.0D), y * d1 + (useNoiseOffsets ? this.noiseLevels[i].yo : 0.0D)) / d1;
         d1 /= 2.0D;
      }

      return d0;
   }

   public double getSurfaceNoiseValue(double x, double y, double z, double scale) {
      return this.noiseAt(x, y, true) * 0.55D;
   }
}