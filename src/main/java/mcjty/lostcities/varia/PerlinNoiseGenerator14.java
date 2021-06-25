package mcjty.lostcities.varia;

import net.minecraft.world.gen.INoiseGenerator;
import net.minecraft.world.gen.SimplexNoiseGenerator;

import java.util.Random;

// @todo 1.15 copy from 1.14: use the 1.15 version!
public class PerlinNoiseGenerator14 implements INoiseGenerator {
   private final SimplexNoiseGenerator[] noiseLevels;
   private final int levels;

   public PerlinNoiseGenerator14(Random seed, int levelsIn) {
      this.levels = levelsIn;
      this.noiseLevels = new SimplexNoiseGenerator[levelsIn];

      for(int i = 0; i < levelsIn; ++i) {
         this.noiseLevels[i] = new SimplexNoiseGenerator(seed);
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

   public double getSurfaceNoiseValue(double x, double y, double z, double p_215460_7_) {
      return this.noiseAt(x, y, true) * 0.55D;
   }
}