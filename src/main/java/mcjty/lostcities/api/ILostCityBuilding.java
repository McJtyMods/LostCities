package mcjty.lostcities.api;

public interface ILostCityBuilding extends ILostCityAsset {

    /**
     * The chance this this building is alone. If 1.0f this building wants to be alone all the time. If 0.0f (default)
     * then the building does not care.
     */
    float getPrefersLonely();

    /**
     * Maximum number of floors for this type of building.
     */
    int getMaxFloors();

    /**
     * Maximum number of cellars for this type of building.
     */
    int getMaxCellars();

    int getMinFloors();

    int getMinCellars();

    /**
     * The filler block (from the palette) used to do procedural generation of extra
     * features (like the blocks around a door)
     */
    char getFillerBlock();
}
