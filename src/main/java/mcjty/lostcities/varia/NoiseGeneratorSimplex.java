package mcjty.lostcities.varia;

import java.util.Random;

public class NoiseGeneratorSimplex {
    private static final int[][] GRAD_3 = new int[][]{{1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0}, {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1}, {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}};
    public static final double SQRT_3 = Math.sqrt(3.0D);
    private final int[] p;
    public final double xo;
    public final double yo;
    public final double zo;
    private static final double F2 = 0.5D * (SQRT_3 - 1.0D);
    private static final double G2 = (3.0D - SQRT_3) / 6.0D;

    public NoiseGeneratorSimplex(Random seed) {
        this.p = new int[512];
        this.xo = seed.nextDouble() * 256.0D;
        this.yo = seed.nextDouble() * 256.0D;
        this.zo = seed.nextDouble() * 256.0D;

        for (int i = 0; i < 256; this.p[i] = i++) {
        }

        for (int l = 0; l < 256; ++l) {
            int j = seed.nextInt(256 - l) + l;
            int k = this.p[l];
            this.p[l] = this.p[j];
            this.p[j] = k;
            this.p[l + 256] = this.p[l];
        }
    }

    private static int fastFloor(double value) {
        return value > 0.0D ? (int) value : (int) value - 1;
    }

    private static double dot(int[] vector, double x, double y) {
        return vector[0] * x + vector[1] * y;
    }

    public double getValue(double x, double y) {
        double d3 = 0.5D * (SQRT_3 - 1.0D);
        double d4 = (x + y) * d3;
        int i = fastFloor(x + d4);
        int j = fastFloor(y + d4);
        double d5 = (3.0D - SQRT_3) / 6.0D;
        double d6 = (i + j) * d5;
        double d7 = i - d6;
        double d8 = j - d6;
        double d9 = x - d7;
        double d10 = y - d8;
        int k;
        int l;

        if (d9 > d10) {
            k = 1;
            l = 0;
        } else {
            k = 0;
            l = 1;
        }

        double d11 = d9 - k + d5;
        double d12 = d10 - l + d5;
        double d13 = d9 - 1.0D + 2.0D * d5;
        double d14 = d10 - 1.0D + 2.0D * d5;
        int i1 = i & 255;
        int j1 = j & 255;
        int k1 = this.p[i1 + this.p[j1]] % 12;
        int l1 = this.p[i1 + k + this.p[j1 + l]] % 12;
        int i2 = this.p[i1 + 1 + this.p[j1 + 1]] % 12;
        double d15 = 0.5D - d9 * d9 - d10 * d10;
        double d0;

        if (d15 < 0.0D) {
            d0 = 0.0D;
        } else {
            d15 = d15 * d15;
            d0 = d15 * d15 * dot(GRAD_3[k1], d9, d10);
        }

        double d16 = 0.5D - d11 * d11 - d12 * d12;
        double d1;

        if (d16 < 0.0D) {
            d1 = 0.0D;
        } else {
            d16 = d16 * d16;
            d1 = d16 * d16 * dot(GRAD_3[l1], d11, d12);
        }

        double d17 = 0.5D - d13 * d13 - d14 * d14;
        double d2;

        if (d17 < 0.0D) {
            d2 = 0.0D;
        } else {
            d17 = d17 * d17;
            d2 = d17 * d17 * dot(GRAD_3[i2], d13, d14);
        }

        return 70.0D * (d0 + d1 + d2);
    }

    public void add(double[] buffer, double x, double z, int xWidth, int zWidth, double xScale, double zScale, double factor) {
        int i = 0;

        for (int zz = 0; zz < zWidth; ++zz) {
            double d0 = (z + zz) * zScale + this.yo;

            for (int xx = 0; xx < xWidth; ++xx) {
                double d1 = (x + xx) * xScale + this.xo;
                double d5 = (d1 + d0) * F2;
                int l = fastFloor(d1 + d5);
                int i1 = fastFloor(d0 + d5);
                double d6 = (l + i1) * G2;
                double d7 = l - d6;
                double d8 = i1 - d6;
                double d9 = d1 - d7;
                double d10 = d0 - d8;
                int j1;
                int k1;

                if (d9 > d10) {
                    j1 = 1;
                    k1 = 0;
                } else {
                    j1 = 0;
                    k1 = 1;
                }

                double d11 = d9 - j1 + G2;
                double d12 = d10 - k1 + G2;
                double d13 = d9 - 1.0D + 2.0D * G2;
                double d14 = d10 - 1.0D + 2.0D * G2;
                int l1 = l & 255;
                int i2 = i1 & 255;
                int j2 = this.p[l1 + this.p[i2]] % 12;
                int k2 = this.p[l1 + j1 + this.p[i2 + k1]] % 12;
                int l2 = this.p[l1 + 1 + this.p[i2 + 1]] % 12;
                double d15 = 0.5D - d9 * d9 - d10 * d10;
                double d2;

                if (d15 < 0.0D) {
                    d2 = 0.0D;
                } else {
                    d15 = d15 * d15;
                    d2 = d15 * d15 * dot(GRAD_3[j2], d9, d10);
                }

                double d16 = 0.5D - d11 * d11 - d12 * d12;
                double d3;

                if (d16 < 0.0D) {
                    d3 = 0.0D;
                } else {
                    d16 = d16 * d16;
                    d3 = d16 * d16 * dot(GRAD_3[k2], d11, d12);
                }

                double d17 = 0.5D - d13 * d13 - d14 * d14;
                double d4;

                if (d17 < 0.0D) {
                    d4 = 0.0D;
                } else {
                    d17 = d17 * d17;
                    d4 = d17 * d17 * dot(GRAD_3[l2], d13, d14);
                }

                int i3 = i++;
                buffer[i3] += 70.0D * (d2 + d3 + d4) * factor;
            }
        }
    }
}