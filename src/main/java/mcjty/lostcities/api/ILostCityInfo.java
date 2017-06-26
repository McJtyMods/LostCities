package mcjty.lostcities.api;

public interface ILostCityInfo {

    /**
     * Get the radius of this city (in blocks)
     */
    float getCityRadius();

    /**
     * Get the name of the citystyle asset for this city
     */
    String getCityStyle();
}
