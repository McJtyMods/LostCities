package mcjty.lostcities.dimensions.world.lost.cityassets;

public class MultiBuilding implements IAsset {

    private final String name;
    private final int dimX;
    private final int dimZ;
    private final String[][] buildings;

    public MultiBuilding(String name, int dimX, int dimZ) {
        this.name = name;
        this.dimX = dimX;
        this.dimZ = dimZ;
        buildings = new String[dimX][dimZ];
    }

    public MultiBuilding set(int x, int z, String building) {
        buildings[x][z] = building;
        return this;
    }

    public String get(int x, int z) {
        return buildings[x][z];
    }

    @Override
    public String getName() {
        return name;
    }
}
