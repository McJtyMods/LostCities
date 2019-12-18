package mcjty.lostcities.config;

import java.util.Collection;

// @todo 1.14: dummy to get things to compile
public interface Configuration {
    void addCustomCategoryComment(String categoryLostcity, String s);

    float getFloat(String fogRed, String categoryClient, float fog_red, float v, float v1, String s);

    boolean getBoolean(String landscapeOutside, String categoryCitySpheres, boolean citysphere_landscape_outside, String s);

    int getInt(String outsideGroundLevel, String categoryCitySpheres, int citysphere_outside_groundlevel, int i, int i1, String s);

    String getString(String outsideProfile, String categoryCitySpheres, String citysphere_outside_profile, String s);

    String[] getStringList(String allowedBiomeFactors, String categoryLostcity, String[] allowed_biome_factors, String s);

    String getString(String biomeSelectionStrategy, String categoryLostcity, String name, String s, String[] strings);

    Configuration get(String categoryStructures, String generateOceanMonuments, boolean generate_oceanmonuments, String generate_ocean_monuments);

    boolean getBoolean();

    boolean hasKey(String categoryGeneral, String version);

    Collection<Object> getCategory(String categoryGeneral);

}
