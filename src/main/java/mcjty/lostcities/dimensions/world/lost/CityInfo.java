package mcjty.lostcities.dimensions.world.lost;

import mcjty.lostcities.dimensions.world.lost.cityassets.Building;
import mcjty.lostcities.dimensions.world.lost.cityassets.CityStyle;
import mcjty.lostcities.dimensions.world.lost.cityassets.MultiBuilding;

public class CityInfo {
    public boolean isCity;
    public boolean couldHaveBuilding;   // True if this chunk could contain a building
    public int section;                 // -1 if single building, else part of multi building
    public int cityLevel;
    public CityStyle cityStyle;
    public MultiBuilding multiBuilding;
    public Building buildingType;
}
