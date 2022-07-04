package mcjty.lostcities.worldgen.lost;

import mcjty.lostcities.varia.PerlinNoiseGenerator14;

public class CityRarityMap {

    private final PerlinNoiseGenerator14 perlinCity;
    private final double scale;
    private final double offset;
    private final double innerScale;

    public CityRarityMap(long seed, double scale, double offset, double innerScale) {
        perlinCity = new PerlinNoiseGenerator14(seed, 4);
        this.scale = scale;
        this.offset = offset;
        this.innerScale = innerScale;
    }

    public float getCityFactor(int cx, int cz) {
        double factor = perlinCity.getValue(cx / scale, cz / scale) * innerScale - offset;
        if (factor < 0) {
            factor = 0;
        }
        return (float) factor;
    }
}
