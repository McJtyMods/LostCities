package mcjty.lostcities.dimensions.world.lost;

import mcjty.lostcities.dimensions.world.LostCityChunkGenerator;
import mcjty.lostcities.varia.ChunkCoord;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CitySphere {

    private static Map<ChunkCoord, CitySphere> citySphereCache = new HashMap<>();

    private final boolean enabled;
    private char glassBlock;
    private char baseBlock;
    private char sideBlock;

    private CitySphere(boolean enabled) {
        this.enabled = enabled;
    }

    public void setBlocks(char glassBlock, char baseBlock, char sideBlock) {
        this.glassBlock = glassBlock;
        this.baseBlock = baseBlock;
        this.sideBlock = sideBlock;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public char getGlassBlock() {
        return glassBlock;
    }

    public char getBaseBlock() {
        return baseBlock;
    }

    public char getSideBlock() {
        return sideBlock;
    }

    public static void cleanCache() {
        citySphereCache.clear();
    }

    /**
     * Return a (possibly cached) city sphere which affects this chunk. That means it will
     * go find the city sphere center chunk and get data from that
     */
    @Nonnull
    public static CitySphere getCitySphere(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        ChunkCoord center = getCityCenterForSpace(chunkX, chunkZ, provider);
        if (!citySphereCache.containsKey(center)) {
            chunkX = center.getChunkX();
            chunkZ = center.getChunkZ();
            Random rand = new Random(provider.seed + chunkX * 961744153L + chunkZ * 837971201L);
            rand.nextFloat();
            rand.nextFloat();
            CitySphere citySphere;
            // This information is for city spheres. This information is only relevant
            // in the chunk representing the center of the city
            if (City.isCitySphereCenterCandidate(chunkX, chunkZ)) {
                boolean enabled = rand.nextFloat() < provider.profile.CITYSPHERE_CHANCE;
                citySphere = new CitySphere(enabled);
            } else {
                citySphere = new CitySphere(false);
            }
            citySphereCache.put(center, citySphere);
        }
        return citySphereCache.get(center);
    }

    /**
     * This returns the center of the city in case we're in a space type world
     */
    public static ChunkCoord getCityCenterForSpace(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        int cx = (chunkX & ~0xf) + 8;
        int cz = (chunkZ & ~0xf) + 8;
        return new ChunkCoord(provider.dimensionId, cx, cz);
    }

}
