package mcjty.lostcities.api;

import mcjty.lostcities.worldgen.lost.cityassets.Palette;
import net.minecraft.world.level.CommonLevelAccessor;

import javax.annotation.Nullable;

public interface ILostCityBuilding extends ILostCityAsset {

    Palette getLocalPalette(CommonLevelAccessor level);

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
    
    /*
     * Get the permission to generate the door for the building
     */
    Boolean getAllowDoors();

    /**
     * The filler block (from the palette) used to do procedural generation of extra
     * features (like the blocks around a door)
     */
    char getFillerBlock();

    /**
     * The rubble block (from the palette) used to generate debris to adjacent
     * chunks when this building is destroyed. If this is null getFillerBlock()
     * should be used
     */
    @Nullable
    Character getRubbleBlock();
}
