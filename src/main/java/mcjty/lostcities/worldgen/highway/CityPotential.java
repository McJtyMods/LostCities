package mcjty.lostcities.worldgen.highway;

@FunctionalInterface
public interface CityPotential {
    float getPotential(int chunkX, int chunkZ);
}
