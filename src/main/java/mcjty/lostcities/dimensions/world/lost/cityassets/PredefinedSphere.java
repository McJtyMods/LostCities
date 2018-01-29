package mcjty.lostcities.dimensions.world.lost.cityassets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mcjty.lostcities.api.ILostCityAsset;

public class PredefinedSphere implements ILostCityAsset {

    private String name;
    private int dimension;
    private int chunkX;
    private int chunkZ;
    private int centerX;
    private int centerZ;
    private int radius;
    private String biome;

    public PredefinedSphere(JsonObject object) {
        readFromJSon(object);
    }

    public PredefinedSphere(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void readFromJSon(JsonObject object) {
        name = object.get("name").getAsString();
        dimension = object.get("dimension").getAsInt();
        chunkX = object.get("chunkx").getAsInt();
        chunkZ = object.get("chunkz").getAsInt();
        centerX = object.get("centerx").getAsInt();
        centerZ = object.get("centerz").getAsInt();
        radius = object.get("radius").getAsInt();
        if (object.has("biome")) {
            biome = object.get("biome").getAsString();
        } else {
            biome = null;
        }
    }

    private JsonArray getArraySafe(JsonObject object, String key) {
        if (object.has(key)) {
            return object.get(key).getAsJsonArray();
        } else {
            return new JsonArray(); // Empty array
        }
    }

    public JsonObject writeToJSon() {
        JsonObject object = new JsonObject();
        object.add("type", new JsonPrimitive("sphere"));
        object.add("name", new JsonPrimitive(name));
        return object;
    }

    public int getDimension() {
        return dimension;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterZ() {
        return centerZ;
    }

    public int getRadius() {
        return radius;
    }

    public String getBiome() {
        return biome;
    }
}