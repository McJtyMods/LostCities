package mcjty.lostcities.api;

/**
 * This is an interface from Lost Worlds which we put here to avoid a (circular) dependency on Lost Worlds
 * as that mod also depends on Lost Cities.
 */
public interface ILostWorldsChunkGenerator {
    // Get the sea level for this chunk generator. This can be different from the sea level of the noise settings as the noise settings
    // sea level is used inside spheres while this sea level is used outside.
    // Returns null if the sea level is not set.
    Integer getOuterSeaLevel();
}
