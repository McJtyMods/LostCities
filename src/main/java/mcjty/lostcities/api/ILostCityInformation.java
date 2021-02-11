package mcjty.lostcities.api;

/**
 * Lost City information for a specific dimension. To get access to this you have to
 * call ILostCities.getLostInfo(dimensionId);
 */
public interface ILostCityInformation {

    /**
     * Get information about a chunk. This is an efficient function as it is cached and
     * only created once every session
     */
    ILostChunkInfo getChunkInfo(int chunkX, int chunkZ);

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
