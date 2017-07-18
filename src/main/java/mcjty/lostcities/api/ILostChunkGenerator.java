package mcjty.lostcities.api;

/**
 * The chunk generator for Lost Cities implements this interface. To get access to this you can do:
 * WorldServer world = ...;
 * if (world.getChunkProvider().chunkGenerator instanceof ILostChunkGenerator) {
 *     ILostChunkGenerator lostGenerator = (ILostChunkGenerator) world.getChunkProvider().chunkGenerator;
 *     ...
 * }
 */
public interface ILostChunkGenerator {

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
