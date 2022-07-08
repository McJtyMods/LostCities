package mcjty.lostcities.worldgen.lost.cityassets;

import com.google.gson.JsonObject;
import mcjty.lostcities.api.ILostCityMultiBuilding;
import mcjty.lostcities.worldgen.lost.regassets.MultiBuildingRE;

import java.util.List;

public class MultiBuilding implements ILostCityMultiBuilding {

    private String name;
    private int dimX;
    private int dimZ;
    private List<List<String>> buildings;

    public MultiBuilding(MultiBuildingRE object) {
        name = object.getRegistryName().getPath(); // @todo temporary. Needs to be fully qualified
        this.dimX = object.getDimX();
        this.dimZ = object.getDimZ();
        this.buildings = object.getBuildings();
    }

    public MultiBuilding set(int x, int z, String building) {
        buildings.get(x).set(z, building);
//        buildings[x][z] = building;
        return this;
    }

    @Override
    public String getBuilding(int x, int z) {
        return buildings.get(x).get(z);
//        return buildings[x][z];
    }

    @Override
    public int getDimX() {
        return dimX;
    }

    @Override
    public int getDimZ() {
        return dimZ;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void readFromJSon(JsonObject object) {
    }
}
