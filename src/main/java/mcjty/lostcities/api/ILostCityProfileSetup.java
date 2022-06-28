package mcjty.lostcities.api;

/**
 * Using this interface you can register new profiles. See ILostCitiesPre for
 * more information
 */
public interface ILostCityProfileSetup {

    /**
     * Create a new profile based on an existing one
     */
    ILostCityProfile createProfile(String name, String baseProfile);
}
