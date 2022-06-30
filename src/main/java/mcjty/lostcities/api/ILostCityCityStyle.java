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

    /**
     * If this returns null it will be the default minecraft:grass_block
     */
    Character getGrassBlock();

    /**
     * If this returns null it will be the default minecraft:iron_bars
     */
    Character getIronbarsBlock();

    /**
     * If this returns null it will be the default minecraft:glowstone
     */
    Character getGlowstoneBlock();
}
