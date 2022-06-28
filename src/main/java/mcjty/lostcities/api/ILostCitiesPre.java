package mcjty.lostcities.api;

import java.util.function.Consumer;

/**
 * This API is separate from ILostCities and is meant to be used during
 * mod construction. It allows another mod to register new standard profiles right
 * before the FMLCommonSetupEvent
 */
public interface ILostCitiesPre {

    /**
     * Register code that will be executed when Lost Cities is ready to register profiles.
     * In this callback you can be sure that all standard profiles are initialized and
     * it gives you a chance to register your own profiles
     */
    void registerProfileSetupCallback(Consumer<ILostCityProfileSetup> runnable);
}
