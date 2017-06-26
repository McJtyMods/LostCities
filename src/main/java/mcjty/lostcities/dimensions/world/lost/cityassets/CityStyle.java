package mcjty.lostcities.dimensions.world.lost.cityassets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mcjty.lostcities.dimensions.world.LostCityChunkGenerator;
import mcjty.lostcities.varia.Tools;
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
    private final List<Pair<Float, String>> frontSelector = new ArrayList<>();
    private final List<Pair<Float, String>> railDungeonSelector = new ArrayList<>();
    private final List<Pair<Float, String>> multiBuildingSelector = new ArrayList<>();
    private String style;

    private int streetWidth;
    private Character streetBlock;
    private Character streetBaseBlock;
    private Character streetVariantBlock;
    private Character parkElevationBlock;
    private Character corridorRoofBlock;
    private Character corridorGlassBlock;
    private Character railMainBlock;
    private Character borderBlock;
    private Character wallBlock;

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

    public Character getStreetBaseBlock() {
        return streetBaseBlock;
    }

    public Character getStreetVariantBlock() {
        return streetVariantBlock;
    }

    public Character getRailMainBlock() {
        return railMainBlock;
    }

    public Character getParkElevationBlock() {
        return parkElevationBlock;
    }

    public Character getCorridorRoofBlock() {
        return corridorRoofBlock;
    }

    public Character getCorridorGlassBlock() {
        return corridorGlassBlock;
    }

    public Character getBorderBlock() {
        return borderBlock;
    }

    public Character getWallBlock() {
        return wallBlock;
    }

    @Override
    public void readFromJSon(JsonObject object) {
        name = object.get("name").getAsString();
        style = object.get("style").getAsString();
        if (object.has("streetblocks")) {
            JsonObject s = object.get("streetblocks").getAsJsonObject();
            borderBlock = s.get("border").getAsCharacter();
            wallBlock = s.get("wall").getAsCharacter();
            streetBlock = s.get("street").getAsCharacter();
            streetVariantBlock = s.get("streetvariant").getAsCharacter();
            streetBaseBlock = s.get("streetbase").getAsCharacter();
            streetWidth = s.get("width").getAsInt();
        }
        if (object.has("railblocks")) {
            JsonObject s = object.get("railblocks").getAsJsonObject();
            railMainBlock = s.get("railmain").getAsCharacter();
        }
        if (object.has("parkblocks")) {
            JsonObject s = object.get("parkblocks").getAsJsonObject();
            parkElevationBlock = s.get("elevation").getAsCharacter();
        }
        if (object.has("corridorblocks")) {
            JsonObject s = object.get("corridorblocks").getAsJsonObject();
            corridorRoofBlock = s.get("roof").getAsCharacter();
            corridorGlassBlock = s.get("glass").getAsCharacter();
        }
        JsonArray array = getArraySafe(object, "buildings");
        for (JsonElement element : array) {
            float factor = element.getAsJsonObject().get("factor").getAsFloat();
            String building = element.getAsJsonObject().get("building").getAsString();
            buildingSelector.add(Pair.of(factor, building));
        }
        array = getArraySafe(object, "multibuildings");
        for (JsonElement element : array) {
            float factor = element.getAsJsonObject().get("factor").getAsFloat();
            String building = element.getAsJsonObject().get("multibuilding").getAsString();
            multiBuildingSelector.add(Pair.of(factor, building));
        }
        array = getArraySafe(object, "parks");
        for (JsonElement element : array) {
            float factor = element.getAsJsonObject().get("factor").getAsFloat();
            String park = element.getAsJsonObject().get("park").getAsString();
            parkSelector.add(Pair.of(factor, park));
        }
        array = getArraySafe(object, "fountains");
        for (JsonElement element : array) {
            float factor = element.getAsJsonObject().get("factor").getAsFloat();
            String fountain = element.getAsJsonObject().get("fountain").getAsString();
            fountainSelector.add(Pair.of(factor, fountain));
        }
        array = getArraySafe(object, "stairs");
        for (JsonElement element : array) {
            float factor = element.getAsJsonObject().get("factor").getAsFloat();
            String fountain = element.getAsJsonObject().get("stair").getAsString();
            stairSelector.add(Pair.of(factor, fountain));
        }
        array = getArraySafe(object, "fronts");
        for (JsonElement element : array) {
            float factor = element.getAsJsonObject().get("factor").getAsFloat();
            String fountain = element.getAsJsonObject().get("front").getAsString();
            frontSelector.add(Pair.of(factor, fountain));
        }
        array = getArraySafe(object, "bridges");
        for (JsonElement element : array) {
            float factor = element.getAsJsonObject().get("factor").getAsFloat();
            String fountain = element.getAsJsonObject().get("bridge").getAsString();
            bridgeSelector.add(Pair.of(factor, fountain));
        }
        array = getArraySafe(object, "raildungeons");
        for (JsonElement element : array) {
            float factor = element.getAsJsonObject().get("factor").getAsFloat();
            String fountain = element.getAsJsonObject().get("dungeon").getAsString();
            railDungeonSelector.add(Pair.of(factor, fountain));
        }
    }

    public JsonArray getArraySafe(JsonObject object, String key) {
        if (object.has(key)) {
            return object.get(key).getAsJsonArray();
        } else {
            return new JsonArray(); // Empty array
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
        s.add("streetbase", new JsonPrimitive(streetBaseBlock));
        s.add("width", new JsonPrimitive(streetWidth));
        object.add("streetblocks", s);

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
        for (Pair<Float, String> pair : frontSelector) {
            JsonObject o = new JsonObject();
            o.add("factor", new JsonPrimitive(pair.getKey()));
            o.add("front", new JsonPrimitive(pair.getValue()));
            array.add(o);
        }
        object.add("fronts", array);

        array = new JsonArray();
        for (Pair<Float, String> pair : bridgeSelector) {
            JsonObject o = new JsonObject();
            o.add("factor", new JsonPrimitive(pair.getKey()));
            o.add("bridge", new JsonPrimitive(pair.getValue()));
            array.add(o);
        }
        object.add("bridges", array);

        array = new JsonArray();
        for (Pair<Float, String> pair : railDungeonSelector) {
            JsonObject o = new JsonObject();
            o.add("factor", new JsonPrimitive(pair.getKey()));
            o.add("dungeon", new JsonPrimitive(pair.getValue()));
            array.add(o);
        }
        object.add("raildungeons", array);

        return object;
    }

    public String getRandomStair(LostCityChunkGenerator provider, Random random) {
        return Tools.getRandomFromList(provider, random, stairSelector);
    }

    public String getRandomFront(LostCityChunkGenerator provider, Random random) {
        return Tools.getRandomFromList(provider, random, frontSelector);
    }

    public String getRandomRailDungeon(LostCityChunkGenerator provider, Random random) {
        return Tools.getRandomFromList(provider, random, railDungeonSelector);
    }

    public String getRandomPark(LostCityChunkGenerator provider, Random random) {
        return Tools.getRandomFromList(provider, random, parkSelector);
    }

    public String getRandomBridge(LostCityChunkGenerator provider, Random random) {
        return Tools.getRandomFromList(provider, random, bridgeSelector);
    }

    public String getRandomFountain(LostCityChunkGenerator provider, Random random) {
        return Tools.getRandomFromList(provider, random, fountainSelector);
    }

    public String getRandomBuilding(LostCityChunkGenerator provider, Random random) {
        return Tools.getRandomFromList(provider, random, buildingSelector);
    }

    public String getRandomMultiBuilding(LostCityChunkGenerator provider, Random random) {
        return Tools.getRandomFromList(provider, random, multiBuildingSelector);
    }
}
