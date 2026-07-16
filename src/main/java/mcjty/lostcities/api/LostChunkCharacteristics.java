package mcjty.lostcities.api;

import net.minecraft.resources.ResourceLocation;
import mcjty.lostcities.worldgen.street.PlannedRoadType;

public class LostChunkCharacteristics {
    public boolean isCity;
    public boolean couldHaveBuilding;   // True if this chunk could contain a building
    public MultiPos multiPos;           // Equal to SINGLE if a single building
    public int cityLevel;               // 0 is lowest city level
    public ResourceLocation cityStyleId;
    public ILostCityCityStyle cityStyle;
    public ResourceLocation multiBuildingId;
    public ILostCityMultiBuilding multiBuilding;
    public ResourceLocation buildingTypeId;
    public ILostCityBuilding buildingType;
    public PlannedRoadType rawPlannedRoadType = PlannedRoadType.NONE;
    public PlannedRoadType plannedRoadType = PlannedRoadType.NONE;
}
