package mcjty.lostcities.api;

public class LostChunkCharacteristics {
    public boolean isCity;
    public boolean couldHaveBuilding;   // True if this chunk could contain a building
    public int section;                 // -1 if single building, else part of multi building (index of sub building)
    public int cityLevel;               // 0 is lowest city level
    public ILostCityCityStyle cityStyle;
    public ILostCityMultiBuilding multiBuilding;
    public ILostCityBuilding buildingType;
}
