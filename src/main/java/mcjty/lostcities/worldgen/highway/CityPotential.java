package mcjty.lostcities.worldgen.highway;

import net.minecraft.util.Mth;

@FunctionalInterface
public interface CityPotential {
    int SCORE_SCALE = 1_000_000;

    float getPotential(int chunkX, int chunkZ);

    default int getScoreWithUpperBound(int chunkX, int chunkZ, int requiredScore) {
        return score(getPotential(chunkX, chunkZ));
    }

    static int score(float potential) {
        return Math.round(Mth.clamp(potential, 0.0f, 1.0f) * SCORE_SCALE);
    }
}
