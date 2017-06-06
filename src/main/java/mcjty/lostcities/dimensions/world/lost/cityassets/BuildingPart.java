package mcjty.lostcities.dimensions.world.lost.cityassets;

import mcjty.lostcities.dimensions.world.lost.BuildingInfo;
import net.minecraft.block.state.IBlockState;

/**
 * A section of a building. Can be either a complete floor or part of a floor.
 */
public class BuildingPart implements IAsset {

    private final String name;

    // Data per height level
    private final String[] slices;

    // Dimension (should be less then 16x16)
    private final int xSize;
    private final int zSize;

    public BuildingPart(String name, int xSize, int zSize, String[] slices) {
        this.name = name;
        this.slices = slices;
        this.xSize = xSize;
        this.zSize = zSize;
    }

    @Override
    public String getName() {
        return name;
    }

    public int getSliceCount() {
        return slices.length;
    }

    public String getSlice(int i) {
        return slices[i];
    }

    public String[] getSlices() {
        return slices;
    }

    public int getXSize() {
        return xSize;
    }

    public int getZSize() {
        return zSize;
    }

    public IBlockState get(BuildingInfo info, int x, int y, int z) {
        return info.getCompiledPalette().get(slices[y].charAt(z * 16 + x), info);
    }

    public Character getC(int x, int y, int z) {
        return slices[y].charAt(z * xSize + x);
    }
}
