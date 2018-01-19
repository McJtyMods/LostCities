package mcjty.lostcities.dimensions.world.lost;

import mcjty.lostcities.dimensions.world.lost.cityassets.IBuildingPart;
import mcjty.lostcities.dimensions.world.lost.cityassets.Palette;

public class AdaptedBuildingPart implements IBuildingPart {

    private final IBuildingPart parent;

    public AdaptedBuildingPart(IBuildingPart parent) {
        this.parent = parent;
    }

    @Override
    public String getName() {
        return parent.getName();
    }

    @Override
    public char[][] getVslices() {
        return parent.getVslices();
    }

    @Override
    public char[] getVSlice(int x, int z) {
        return new char[0];
    }

    @Override
    public Palette getLocalPalette() {
        return null;
    }

    @Override
    public int getSliceCount() {
        return 0;
    }

    @Override
    public int getXSize() {
        return 0;
    }

    @Override
    public int getZSize() {
        return 0;
    }

    @Override
    public String getMobID(BuildingInfo info, int x, int y, int z) {
        return null;
    }

    @Override
    public String getLootTable(BuildingInfo info, int x, int y, int z) {
        return null;
    }
}
