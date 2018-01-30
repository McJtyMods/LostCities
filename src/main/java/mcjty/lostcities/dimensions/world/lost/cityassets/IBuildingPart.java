package mcjty.lostcities.dimensions.world.lost.cityassets;

import mcjty.lostcities.dimensions.world.lost.BuildingInfo;

import java.util.Map;

public interface IBuildingPart {
    Character getMetaChar(String key);

    Integer getMetaInteger(String key);

    boolean getMetaBoolean(String key);

    Float getMetaFloat(String key);

    String getMetaString(String key);

    String getName();

    char[][] getVslices();

    char[] getVSlice(int x, int z);

    Palette getLocalPalette();

    int getSliceCount();

    int getXSize();

    int getZSize();

    String getMobID(BuildingInfo info, int x, int y, int z);

    String getLootTable(BuildingInfo info, int x, int y, int z);

    Map<String, Integer> getTorchOrientation(BuildingInfo info, int x, int y, int z);
}
