package mcjty.lostcities.worldgen.lost.cityassets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mcjty.lostcities.api.ILostCityMultiBuilding;

public class MultiBuilding implements ILostCityMultiBuilding {

    private String name;
    private int dimX;
    private int dimZ;
    private String[][] buildings;

    public MultiBuilding(JsonObject object) {
        readFromJSon(object);
    }

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

    @Override
    public String getBuilding(int x, int z) {
        return buildings[x][z];
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
        name = object.get("name").getAsString();
        dimX = object.get("dimx").getAsInt();
        dimZ = object.get("dimz").getAsInt();
        JsonArray buildingArray = object.get("buildings").getAsJsonArray();
        buildings = new String[dimX][dimZ];
        for (int z = 0 ; z < dimZ ; z++) {
            JsonArray ar = buildingArray.get(z).getAsJsonArray();
            for (int x = 0 ; x < dimX ; x++) {
                buildings[z][x] = ar.get(x).getAsString();
            }
        }
    }

    public JsonObject writeToJSon() {
        JsonObject object = new JsonObject();
        object.add("type", new JsonPrimitive("multibuilding"));
        object.add("name", new JsonPrimitive(name));
        object.add("dimx", new JsonPrimitive(dimX));
        object.add("dimz", new JsonPrimitive(dimZ));
        JsonArray buildingArray = new JsonArray();
        for (String[] b : buildings) {
            JsonArray a = new JsonArray();
            for (String s : b) {
                a.add(new JsonPrimitive(s));
            }
            buildingArray.add(a);
        }
        object.add("buildings", buildingArray);
        return object;
    }
}
