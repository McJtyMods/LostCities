package mcjty.lostcities.worldgen.lost.cityassets;

import net.minecraft.world.level.CommonLevelAccessor;

public interface IBuildingPart {
    Character getMetaChar(String key);

    Integer getMetaInteger(String key);

    boolean getMetaBoolean(String key);

    Float getMetaFloat(String key);

    String getMetaString(String key);

    String getName();

    char[][] getVslices();

    char[] getVSlice(int x, int z);

    Palette getLocalPalette(CommonLevelAccessor level);

    int getSliceCount();

    int getXSize();

    int getZSize();
}
