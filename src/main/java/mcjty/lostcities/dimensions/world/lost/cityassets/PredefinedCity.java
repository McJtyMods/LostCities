package mcjty.lostcities.dimensions.world.lost.cityassets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.List;

public class PredefinedCity implements IAsset {

    private String name;
    private int dimension;
    private int chunkX;
    private int chunkZ;
    private int radius;
    private String cityStyle;
    private final List<PredefinedBuilding> predefinedBuildings = new ArrayList<>();
    private final List<PredefinedStreet> predefinedStreets = new ArrayList<>();

    public PredefinedCity(JsonObject object) {
        readFromJSon(object);
    }

    public PredefinedCity(String name) {
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
        radius = object.get("radius").getAsInt();
        if (object.has("citystyle")) {
            cityStyle = object.get("citystyle").getAsString();
        }
        JsonArray buildings = getArraySafe(object, "buildings");
        for (JsonElement element : buildings) {
            JsonObject o = element.getAsJsonObject();
            boolean multi;
            String building;
            if (o.has("building")) {
                building = o.get("building").getAsString();
                multi = false;
            } else {
                building = o.get("multibuilding").getAsString();
                multi = true;
            }
            boolean preventRuins = o.has("preventruins") && o.get("preventruins").getAsBoolean();
            int relChunkX = o.get("chunkx").getAsInt();
            int relChunkZ = o.get("chunkz").getAsInt();
            PredefinedBuilding b = new PredefinedBuilding(building, relChunkX, relChunkZ, multi, preventRuins);
            predefinedBuildings.add(b);
        }
        JsonArray streets = getArraySafe(object, "streets");
        for (JsonElement element : streets) {
            JsonObject o = element.getAsJsonObject();
            int relChunkX = o.get("chunkx").getAsInt();
            int relChunkZ = o.get("chunkz").getAsInt();
            PredefinedStreet b = new PredefinedStreet(relChunkX, relChunkZ);
            predefinedStreets.add(b);
        }
    }

    private JsonArray getArraySafe(JsonObject object, String key) {
        if (object.has(key)) {
            return object.get(key).getAsJsonArray();
        } else {
            return new JsonArray(); // Empty array
        }
    }

    @Override
    public JsonObject writeToJSon() {
        JsonObject object = new JsonObject();
        object.add("type", new JsonPrimitive("city"));
        object.add("name", new JsonPrimitive(name));
        return object;
    }

    public List<PredefinedBuilding> getPredefinedBuildings() {
        return predefinedBuildings;
    }

    public List<PredefinedStreet> getPredefinedStreets() {
        return predefinedStreets;
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

    public int getRadius() {
        return radius;
    }

    public String getCityStyle() {
        return cityStyle;
    }

    public static class PredefinedStreet {
        private final int relChunkX;
        private final int relChunkZ;

        public PredefinedStreet(int relChunkX, int relChunkZ) {
            this.relChunkX = relChunkX;
            this.relChunkZ = relChunkZ;
        }

        public int getRelChunkX() {
            return relChunkX;
        }

        public int getRelChunkZ() {
            return relChunkZ;
        }
    }

    public static class PredefinedBuilding {
        private final String building;
        private final int relChunkX;
        private final int relChunkZ;
        private final boolean multi;
        private final boolean preventRuins;

        public PredefinedBuilding(String building, int relChunkX, int relChunkZ, boolean multi, boolean preventRuins) {
            this.building = building;
            this.relChunkX = relChunkX;
            this.relChunkZ = relChunkZ;
            this.multi = multi;
            this.preventRuins = preventRuins;
        }

        public String getBuilding() {
            return building;
        }

        public int getRelChunkX() {
            return relChunkX;
        }

        public int getRelChunkZ() {
            return relChunkZ;
        }

        public boolean isMulti() {
            return multi;
        }

        public boolean isPreventRuins() {
            return preventRuins;
        }
    }
}
