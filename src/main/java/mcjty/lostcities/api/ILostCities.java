package mcjty.lostcities.api;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;

/**
 * Main interface for this mod. Use this to get city information
 * Get a reference to an implementation of this interface by calling:
 *         FMLInterModComms.sendFunctionMessage("lostcities", "getLostCities", "<whatever>.YourClass$GetLostCities");
 */
public interface ILostCities {

    String LOSTCITIES = "lostcities";
    // IMC message for getting ILostCities
    String GET_LOST_CITIES = "getLostCities";
    // IMC message for getting ILostCitiesPre
    String GET_LOST_CITIES_PRE = "getLostCitiesPre";

    /**
     * Get Lost City information for a given dimension. Returns null if the dimension doesn't support Lost Cities
     */
    @Nullable
    ILostCityInformation getLostInfo(Level world);

    /**
     * Register a lost city profile with a dimension. Note that this is not remembered!
     * You need to do this again after loading your world. Preferably in the chunkGenerator
     * (for example in buildSurface)
     */
    void registerDimension(ResourceKey<Level> key, String profile);

    /**
     * Load an additional asset file
     */
    void loadAsset(File file);

    /**
     * Load an additional asset file
     */
    void loadAsset(InputStream input, String path);
}
