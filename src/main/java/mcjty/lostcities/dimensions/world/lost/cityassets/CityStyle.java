package mcjty.lostcities.dimensions.world.lost.cityassets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mcjty.lostcities.dimensions.world.LostCityChunkGenerator;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CityStyle implements IAsset {

    private String name;

    private final List<Pair<Float, String>> buildingSelector = new ArrayList<>();
    private final List<Pair<Float, String>> bridgeSelector = new ArrayList<>();
    private final List<Pair<Float, String>> parkSelector = new ArrayList<>();
    private final List<Pair<Float, String>> fountainSelector = new ArrayList<>();
    private final List<Pair<Float, String>> stairSelector = new ArrayList<>();
    private final List<Pair<Float, String>> multiBuildingSelector = new ArrayList<>();
    private String style;

    private int streetWidth;
    private Character streetBlock;
    private Character streetVariantBlock;

    public CityStyle(JsonObject object) {
        readFromJSon(object);
    }

    public CityStyle(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getStyle() {
        return style;
    }

    public int getStreetWidth() {
        return streetWidth;
    }

    public Character getStreetBlock() {
        return streetBlock;
    }

    public Character getStreetVariantBlock() {
        return streetVariantBlock;
    }

    @Override
    public void readFromJSon(JsonObject object) {
        name = object.get("name").getAsString();
        style = object.get("style").getAsString();
        if (object.has("street")) {
            JsonObject s = object.get("street").getAsJsonObject();
            streetBlock = s.get("street").getAsCharacter();
            streetVariantBlock = s.get("streetvariant").getAsCharacter();
            streetWidth = s.get("width").getAsInt();
        }
        JsonArray array = object.get("buildings").getAsJsonArray();
        for (JsonElement element : array) {
            float factor = element.getAsJsonObject().get("factor").getAsFloat();
            String building = element.getAsJsonObject().get("building").getAsString();
            buildingSelector.add(Pair.of(factor, building));
        }
        array = object.get("multibuildings").getAsJsonArray();
        for (JsonElement element : array) {
            float factor = element.getAsJsonObject().get("factor").getAsFloat();
            String building = element.getAsJsonObject().get("multibuilding").getAsString();
            multiBuildingSelector.add(Pair.of(factor, building));
        }
        array = object.get("parks").getAsJsonArray();
        for (JsonElement element : array) {
            float factor = element.getAsJsonObject().get("factor").getAsFloat();
            String park = element.getAsJsonObject().get("park").getAsString();
            parkSelector.add(Pair.of(factor, park));
        }
        array = object.get("fountains").getAsJsonArray();
        for (JsonElement element : array) {
            float factor = element.getAsJsonObject().get("factor").getAsFloat();
            String fountain = element.getAsJsonObject().get("fountain").getAsString();
            fountainSelector.add(Pair.of(factor, fountain));
        }
        array = object.get("stairs").getAsJsonArray();
        for (JsonElement element : array) {
            float factor = element.getAsJsonObject().get("factor").getAsFloat();
            String fountain = element.getAsJsonObject().get("stair").getAsString();
            stairSelector.add(Pair.of(factor, fountain));
        }
        array = object.get("bridges").getAsJsonArray();
        for (JsonElement element : array) {
            float factor = element.getAsJsonObject().get("factor").getAsFloat();
            String fountain = element.getAsJsonObject().get("bridge").getAsString();
            bridgeSelector.add(Pair.of(factor, fountain));
        }
    }

    @Override
    public JsonObject writeToJSon() {
        JsonObject object = new JsonObject();
        object.add("type", new JsonPrimitive("citystyle"));
        object.add("name", new JsonPrimitive(name));
        object.add("style", new JsonPrimitive(style));

        JsonObject s = new JsonObject();
        s.add("street", new JsonPrimitive(streetBlock));
        s.add("streetvariant", new JsonPrimitive(streetVariantBlock));
        s.add("width", new JsonPrimitive(streetWidth));
        object.add("street", s);

        JsonArray array = new JsonArray();
        for (Pair<Float, String> pair : buildingSelector) {
            JsonObject o = new JsonObject();
            o.add("factor", new JsonPrimitive(pair.getKey()));
            o.add("building", new JsonPrimitive(pair.getValue()));
            array.add(o);
        }
        object.add("buildings", array);

        array = new JsonArray();
        for (Pair<Float, String> pair : multiBuildingSelector) {
            JsonObject o = new JsonObject();
            o.add("factor", new JsonPrimitive(pair.getKey()));
            o.add("multibuilding", new JsonPrimitive(pair.getValue()));
            array.add(o);
        }
        object.add("multibuildings", array);

        array = new JsonArray();
        for (Pair<Float, String> pair : parkSelector) {
            JsonObject o = new JsonObject();
            o.add("factor", new JsonPrimitive(pair.getKey()));
            o.add("park", new JsonPrimitive(pair.getValue()));
            array.add(o);
        }
        object.add("parks", array);

        array = new JsonArray();
        for (Pair<Float, String> pair : fountainSelector) {
            JsonObject o = new JsonObject();
            o.add("factor", new JsonPrimitive(pair.getKey()));
            o.add("fountain", new JsonPrimitive(pair.getValue()));
            array.add(o);
        }
        object.add("fountains", array);

        array = new JsonArray();
        for (Pair<Float, String> pair : stairSelector) {
            JsonObject o = new JsonObject();
            o.add("factor", new JsonPrimitive(pair.getKey()));
            o.add("stair", new JsonPrimitive(pair.getValue()));
            array.add(o);
        }
        object.add("stairs", array);

        array = new JsonArray();
        for (Pair<Float, String> pair : bridgeSelector) {
            JsonObject o = new JsonObject();
            o.add("factor", new JsonPrimitive(pair.getKey()));
            o.add("bridge", new JsonPrimitive(pair.getValue()));
            array.add(o);
        }
        object.add("bridges", array);

        return object;
    }

    public CityStyle addBuilding(float factor, String building) {
        buildingSelector.add(Pair.of(factor, building));
        return this;
    }

    public CityStyle addMultiBuilding(float factor, String multiBuilding) {
        multiBuildingSelector.add(Pair.of(factor, multiBuilding));
        return this;
    }

    private String getRandomFromList(LostCityChunkGenerator provider, Random random, List<Pair<Float, String>> list) {
        List<Pair<Float, String>> elements = new ArrayList<>();
        float totalweight = 0;
        for (Pair<Float, String> pair : list) {
            elements.add(pair);
            totalweight += pair.getKey();
        }
        float r = random.nextFloat() * totalweight;
        for (Pair<Float, String> pair : elements) {
            r -= pair.getKey();
            if (r <= 0) {
                return pair.getRight();
            }
        }
        return null;
    }


    public String getRandomStair(LostCityChunkGenerator provider, Random random) {
        return getRandomFromList(provider, random, stairSelector);
    }

    public String getRandomPark(LostCityChunkGenerator provider, Random random) {
        return getRandomFromList(provider, random, parkSelector);
    }

    public String getRandomBridge(LostCityChunkGenerator provider, Random random) {
        return getRandomFromList(provider, random, bridgeSelector);
    }

    public String getRandomFountain(LostCityChunkGenerator provider, Random random) {
        return getRandomFromList(provider, random, fountainSelector);
    }

    public String getRandomBuilding(LostCityChunkGenerator provider, Random random) {
        return getRandomFromList(provider, random, buildingSelector);
    }

    public String getRandomMultiBuilding(LostCityChunkGenerator provider, Random random) {
        return getRandomFromList(provider, random, multiBuildingSelector);
    }
}
