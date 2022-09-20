package mcjty.lostcities.varia;

import net.minecraft.util.RandomSource;

import java.util.Arrays;

public class NoiseGeneratorPerlin {
    private final NoiseGeneratorSimplex[] noiseLevels;
    private final int levels;

    public NoiseGeneratorPerlin(RandomSource seed, int levelsIn) {
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

    public double[] getRegion(double[] buffer, double x, double z, int xWidth, int zWidth, double xScale, double zScale, double factor) {
        if (buffer != null && buffer.length >= xWidth * zWidth) {
            Arrays.fill(buffer, 0.0D);
        } else {
            buffer = new double[xWidth * zWidth];
        }

        double d1 = 1.0D;
        double d0 = 1.0D;

        for (int j = 0; j < this.levels; ++j) {
            this.noiseLevels[j].add(buffer, x, z, xWidth, zWidth, xScale * d0 * d1, zScale * d0 * d1, 0.55D / d1);
            d0 *= factor;
            d1 *= 0.5D;
        }

        return buffer;
    }

}