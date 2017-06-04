package mcjty.lostcities.dimensions.world.lost.cityassets;

/**
 * A section of a building. Can be either a complete floor or part of a floor.
 */
public class BuildingPart {

    // Data per height level
    private final String[] levels;

    // Dimension (should be less then 16x16)
    private final int width;
    private final int height;

    public BuildingPart(int width, int height, String[] levels) {
        this.levels = levels;
        this.width = width;
        this.height = height;
    }

    public int getLevelCount() {
        return levels.length;
    }

    public String getLevel(int i) {
        return levels[i];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
