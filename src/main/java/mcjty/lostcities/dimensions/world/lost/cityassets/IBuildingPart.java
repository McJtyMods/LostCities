package mcjty.lostcities.dimensions.world.lost.cityassets;

import mcjty.lostcities.dimensions.world.lost.BuildingInfo;

public interface IBuildingPart {
    String getName();

    char[][] getVslices();

    char[] getVSlice(int x, int z);

    Palette getLocalPalette();

    int getSliceCount();

    int getXSize();

    int getZSize();

    String getMobID(BuildingInfo info, int x, int y, int z);

    String getLootTable(BuildingInfo info, int x, int y, int z);
}
