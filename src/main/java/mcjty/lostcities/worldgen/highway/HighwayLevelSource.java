package mcjty.lostcities.worldgen.highway;

@FunctionalInterface
public interface HighwayLevelSource {
    int getCityLevel(int chunkX, int chunkZ);
}
