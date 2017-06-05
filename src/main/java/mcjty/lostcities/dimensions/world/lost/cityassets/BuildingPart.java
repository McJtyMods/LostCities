package mcjty.lostcities.dimensions.world.lost.cityassets;

/**
 * A section of a building. Can be either a complete floor or part of a floor.
 */
public class BuildingPart {

    private final String name;

    // Data per height level
    private final String[] slices;

    // Dimension (should be less then 16x16)
    private final int width;
    private final int height;

    public BuildingPart(String name, int width, int height, String[] slices) {
        this.name = name;
        this.slices = slices;
        this.width = width;
        this.height = height;
    }

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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
