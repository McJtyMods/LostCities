package mcjty.lostcities.varia;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

// Preserve the double precision and optional offsets used by existing city and highway layouts.
class LegacySimplexNoise {
    protected static final int[][] GRADIENT = new int[][]{
        {1, 1, 0},
        {-1, 1, 0},
        {1, -1, 0},
        {-1, -1, 0},
        {1, 0, 1},
        {-1, 0, 1},
        {1, 0, -1},
        {-1, 0, -1},
        {0, 1, 1},
        {0, -1, 1},
        {0, 1, -1},
        {0, -1, -1},
        {1, 1, 0},
        {0, -1, 1},
        {-1, 1, 0},
        {0, -1, -1}
    };
    private static final double SQRT_3 = Math.sqrt(3.0);
    private static final double F2 = 0.5 * (SQRT_3 - 1.0);
    private static final double G2 = (3.0 - SQRT_3) / 6.0;
    private final int[] p = new int[512];
    public final double xo;
    public final double yo;
    public final double zo;

    public LegacySimplexNoise(RandomSource random) {
        this.xo = random.nextDouble() * 256.0;
        this.yo = random.nextDouble() * 256.0;
        this.zo = random.nextDouble() * 256.0;
        int i = 0;

        while (i < 256) {
            this.p[i] = i++;
        }

        for (int ix = 0; ix < 256; ix++) {
            int offset = random.nextInt(256 - ix);
            int tmp = this.p[ix];
            this.p[ix] = this.p[offset + ix];
            this.p[offset + ix] = tmp;
        }
    }

    private int p(int x) {
        return this.p[x & 0xFF];
    }

    protected static double dot(int[] g, double x, double y, double z) {
        return g[0] * x + g[1] * y + g[2] * z;
    }

    private double getCornerNoise3D(int index, double x, double y, double z, double base) {
        double t0 = base - x * x - y * y - z * z;
        double n0;
        if (t0 < 0.0) {
            n0 = 0.0;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * dot(GRADIENT[index], x, y, z);
        }

        return n0;
    }

    public double getValue(double xin, double yin) {
        double s = (xin + yin) * F2;
        int i = Mth.floor(xin + s);
        int j = Mth.floor(yin + s);
        double t = (i + j) * G2;
        double X0 = i - t;
        double Y0 = j - t;
        double x0 = xin - X0;
        double y0 = yin - Y0;
        int i1;
        int j1;
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        } else {
            i1 = 0;
            j1 = 1;
        }

        double x1 = x0 - i1 + G2;
        double y1 = y0 - j1 + G2;
        double x2 = x0 - 1.0 + 2.0 * G2;
        double y2 = y0 - 1.0 + 2.0 * G2;
        int ii = i & 0xFF;
        int jj = j & 0xFF;
        int gi0 = this.p(ii + this.p(jj)) % 12;
        int gi1 = this.p(ii + i1 + this.p(jj + j1)) % 12;
        int gi2 = this.p(ii + 1 + this.p(jj + 1)) % 12;
        double n0 = this.getCornerNoise3D(gi0, x0, y0, 0.0, 0.5);
        double n1 = this.getCornerNoise3D(gi1, x1, y1, 0.0, 0.5);
        double n2 = this.getCornerNoise3D(gi2, x2, y2, 0.0, 0.5);
        return 70.0 * (n0 + n1 + n2);
    }

}
