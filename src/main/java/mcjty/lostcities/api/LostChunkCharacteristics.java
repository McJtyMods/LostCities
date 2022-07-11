package mcjty.lostcities.api;

public class LostChunkCharacteristics {
    public boolean isCity;
    public boolean couldHaveBuilding;   // True if this chunk could contain a building
    public MultiPos multiPos;           // Equal to SINGLE if a single building
    public int cityLevel;               // 0 is lowest city level
    public ILostCityCityStyle cityStyle;
    public ILostCityMultiBuilding multiBuilding;
    public ILostCityBuilding buildingType;
}
