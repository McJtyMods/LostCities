package mcjty.lostcities.varia;

import java.util.Arrays;
import java.util.Random;

public class NoiseGeneratorPerlin {
    private final NoiseGeneratorSimplex[] noiseLevels;
    private final int levels;

    public NoiseGeneratorPerlin(Random seed, int levelsIn) {
        this.levels = levelsIn;
        this.noiseLevels = new NoiseGeneratorSimplex[levelsIn];

        for (int i = 0; i < levelsIn; ++i) {
            this.noiseLevels[i] = new NoiseGeneratorSimplex(seed);
        }
    }

    public double getValue(double x, double y) {
        double d0 = 0.0D;
        double d1 = 1.0D;

        for (int i = 0; i < this.levels; ++i) {
            d0 += this.noiseLevels[i].getValue(x * d1, y * d1) / d1;
            d1 /= 2.0D;
        }

        return d0;
    }

    public double[] getRegion(double[] buffer, double p_151599_2_, double p_151599_4_, int p_151599_6_, int p_151599_7_, double p_151599_8_, double p_151599_10_, double p_151599_12_) {
        if (buffer != null && buffer.length >= p_151599_6_ * p_151599_7_) {
            Arrays.fill(buffer, 0.0D);
        } else {
            buffer = new double[p_151599_6_ * p_151599_7_];
        }

        double d1 = 1.0D;
        double d0 = 1.0D;

        for (int j = 0; j < this.levels; ++j) {
            this.noiseLevels[j].add(buffer, p_151599_2_, p_151599_4_, p_151599_6_, p_151599_7_, p_151599_8_ * d0 * d1, p_151599_10_ * d0 * d1, 0.55D / d1);
            d0 *= p_151599_12_;
            d1 *= 0.5D;
        }

        return buffer;
    }

}