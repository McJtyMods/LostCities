package mcjty.lostcities.api;

/**
 * Lost City information for a specific dimension. To get access to this you have to
 * call ILostCities.getLostInfo(dimensionId);
 */
public interface ILostCityInformation {

    /**
     * Get information about a chunk. This is an efficient function as it is cached and
     * only created once every session. Don't call this during worldgen!
     */
    ILostChunkInfo getChunkInfo(int chunkX, int chunkZ);

    /**
     * Get the sphere that contains the given location. Doesn't depend on biomes so you can
     * use this during biome generation
     */
    ILostSphere getSphere(int x, int y, int z);

    /**
     * Get the sphere that contains the given location. Doesn't depend on biomes so you can
     * use this during biome generation. This version ignores y
     */
    ILostSphere getSphere(int x, int z);

    /**
     * Convert a 'level' (like a city level) to a real height. This is basically
     * groundLevel + 6*level. Note that for buildings this will actually point
     * to the height at which the first blocks of a floor are. So actual usable
     * empty space will be at getRealHeight()+1
     */
    int getRealHeight(int level);

    ILostCityAssetRegistry<ILostCityBuilding> getBuildings();
    ILostCityAssetRegistry<ILostCityMultiBuilding> getMultiBuildings();
    ILostCityAssetRegistry<ILostCityCityStyle> getCityStyles();
}
