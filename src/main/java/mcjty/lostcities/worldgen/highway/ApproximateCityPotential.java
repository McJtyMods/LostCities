package mcjty.lostcities.worldgen.highway;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.worldgen.lost.CityRarityMap;

import java.util.Random;

/**
 * Remote, deterministic city-potential approximation for highway planning.
 *
 * For ordinary profiles this reproduces the coordinate-based city-center/radius
 * overlap and spawn-distance multiplier. Noise-city profiles use the same
 * CityRarityMap calculation. An optional modifier can apply deterministic
 * generator-aware constraints such as terrain height and biome multipliers
 * without introducing dependencies on BuildingInfo or final city decisions.
 */
public final class ApproximateCityPotential implements CityPotential {

    @FunctionalInterface
    public interface Modifier {
        float modify(int chunkX, int chunkZ, float potential);
    }

    private final double cityChance;
    private final int cityMinRadius;
    private final int cityMaxRadius;
    private final int spawnDistance1;
    private final int spawnDistance2;
    private final double spawnMultiplier1;
    private final double spawnMultiplier2;
    private final CityRarityMap rarityMap;
    private final Modifier modifier;

    public ApproximateCityPotential(long seed, LostCityProfile profile) {
        this(seed, profile, (chunkX, chunkZ, potential) -> potential);
    }

    public ApproximateCityPotential(long seed, LostCityProfile profile, Modifier modifier) {
        cityChance = profile.CITY_CHANCE;
        cityMinRadius = profile.CITY_MINRADIUS;
        cityMaxRadius = profile.CITY_MAXRADIUS;
        spawnDistance1 = profile.CITY_SPAWN_DISTANCE1;
        spawnDistance2 = profile.CITY_SPAWN_DISTANCE2;
        spawnMultiplier1 = profile.CITY_SPAWN_MULTIPLIER1;
        spawnMultiplier2 = profile.CITY_SPAWN_MULTIPLIER2;
        rarityMap = cityChance < 0
                ? new CityRarityMap(seed, profile.CITY_PERLIN_SCALE, profile.CITY_PERLIN_OFFSET, profile.CITY_PERLIN_INNERSCALE)
                : null;
        this.modifier = modifier;
    }

    @Override
    public float getPotential(int chunkX, int chunkZ) {
        float factor = cityChance < 0 ? rarityMap.getCityFactor(chunkX, chunkZ) : getCenterOverlap(chunkX, chunkZ);
        if (spawnDistance2 > 0) {
            double blockX = (double) chunkX * 16.0;
            double blockZ = (double) chunkZ * 16.0;
            double distance = Math.sqrt(blockX * blockX + blockZ * blockZ);
            double multiplier;
            if (distance <= spawnDistance1) {
                multiplier = spawnMultiplier1;
            } else if (distance >= spawnDistance2) {
                multiplier = spawnMultiplier2;
            } else {
                double position = (distance - spawnDistance1) / (spawnDistance2 - spawnDistance1);
                multiplier = spawnMultiplier1 + position * (spawnMultiplier2 - spawnMultiplier1);
            }
            factor *= (float) multiplier;
        }
        factor = modifier.modify(chunkX, chunkZ, factor);
        return Math.min(Math.max(factor, 0.0f), 1.0f);
    }

    private float getCenterOverlap(int chunkX, int chunkZ) {
        int offset = (cityMaxRadius + 15) / 16;
        float factor = 0.0f;
        for (int centerX = chunkX - offset; centerX <= chunkX + offset; centerX++) {
            for (int centerZ = chunkZ - offset; centerZ <= chunkZ + offset; centerZ++) {
                if (!isCityCenter(centerX, centerZ)) {
                    continue;
                }
                float radius = getCityRadius(centerX, centerZ);
                double dx = (double) (centerX - chunkX) * 16.0;
                double dz = (double) (centerZ - chunkZ) * 16.0;
                double squaredDistance = dx * dx + dz * dz;
                if (squaredDistance < radius * radius) {
                    factor += (float) ((radius - Math.sqrt(squaredDistance)) / radius);
                }
            }
        }
        return factor;
    }

    private boolean isCityCenter(int chunkX, int chunkZ) {
        Random random = new Random(chunkZ * 797003437L + chunkX * 295075153L);
        return random.nextDouble() < cityChance;
    }

    private float getCityRadius(int chunkX, int chunkZ) {
        Random random = new Random(chunkZ * 100001653L + chunkX * 295075153L);
        int range = Math.max(1, cityMaxRadius - cityMinRadius);
        return cityMinRadius + random.nextInt(range);
    }
}
