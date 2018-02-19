package mcjty.lostcities.api;

import javax.annotation.Nullable;

/**
 * Main interface for this mod. Use this to get city information
 * Get a reference to an implementation of this interface by calling:
 *         FMLInterModComms.sendFunctionMessage("lostcities", "getLostCities", "<whatever>.YourClass$GetLostCities");
 */
public interface ILostCities {

    /**
     * If the given dimension is handled by Lost Cities then this will return the chunk generator. It is strongly
     * recommended to use this instead of the now deprecated technique of doing instanceof ILostChunkGenerator
     * as this is not compatible with Sponge
     */
    @Nullable
    ILostChunkGenerator getLostGenerator(int dimension);
}
