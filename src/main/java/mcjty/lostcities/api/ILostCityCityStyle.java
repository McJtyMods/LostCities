package mcjty.lostcities.api;

public interface ILostCityCityStyle extends ILostCityAsset {

    String getStyle();

    Float getExplosionChance();

    int getStreetWidth();

    Integer getMinFloorCount();

    Integer getMinCellarCount();

    Integer getMaxFloorCount();

    Integer getMaxCellarCount();

    Character getStreetBlock();

    Character getStreetBaseBlock();

    Character getStreetVariantBlock();

    Character getRailMainBlock();

    Character getParkElevationBlock();

    Character getCorridorRoofBlock();

    Character getCorridorGlassBlock();

    Character getBorderBlock();

    Character getWallBlock();
}
