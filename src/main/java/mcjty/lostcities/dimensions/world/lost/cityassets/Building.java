package mcjty.lostcities.dimensions.world.lost.cityassets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class Building implements IAsset {

    private String name;

    private int minFloors = -1;         // -1 means default from level
    private int minCellars = -1;        // -1 means default frmo level
    private int maxFloors = -1;         // -1 means default from level
    private int maxCellars = -1;        // -1 means default frmo level
    private char fillerBlock;           // Block used to fill/close areas. Usually the block of the building itself

    private final List<Pair<Predicate<ConditionContext>, String>> parts = new ArrayList<>();
    private final List<Pair<Predicate<ConditionContext>, String>> parts2 = new ArrayList<>();

    public Building(JsonObject object) {
        readFromJSon(object);
    }

    public Building(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void readFromJSon(JsonObject object) {
        name = object.get("name").getAsString();

        if (object.getAsJsonObject().has("minfloors")) {
            minFloors = object.getAsJsonObject().get("minfloors").getAsInt();
        }
        if (object.getAsJsonObject().has("mincellars")) {
            minCellars = object.getAsJsonObject().get("mincellars").getAsInt();
        }
        if (object.getAsJsonObject().has("maxfloors")) {
            maxFloors = object.getAsJsonObject().get("maxfloors").getAsInt();
        }
        if (object.getAsJsonObject().has("maxcellars")) {
            maxCellars = object.getAsJsonObject().get("maxcellars").getAsInt();
        }
        if (object.getAsJsonObject().has("filler")) {
            fillerBlock = object.getAsJsonObject().get("filler").getAsCharacter();
        } else {
            throw new RuntimeException("'filler' is required for building '" + name + "'!");
        }

        readParts(object, this.parts, "parts");
        readParts(object, this.parts2, "parts2");
    }

    public void readParts(JsonObject object, List<Pair<Predicate<ConditionContext>, String>> p, String partSection) {
        p.clear();
        if (!object.has(partSection)) {
            return;
        }
        JsonArray partArray = object.get(partSection).getAsJsonArray();
        for (JsonElement element : partArray) {
            String partName = element.getAsJsonObject().get("part").getAsString();
            Predicate<ConditionContext> test = ConditionContext.parseTest(element);
            addPart(test, partName, p);
        }
    }

    @Override
    public JsonObject writeToJSon() {
        JsonObject object = new JsonObject();
        object.add("type", new JsonPrimitive("building"));
        object.add("name", new JsonPrimitive(name));
        JsonArray partArray = new JsonArray();
        for (Pair<Predicate<ConditionContext>, String> part : parts) {
            JsonObject partObject = new JsonObject();
            partObject.add("test", new JsonPrimitive("@todo"));
            partObject.add("part", new JsonPrimitive(part.getRight()));
            partArray.add(partObject);
        }
        object.add("parts", partArray);
        return object;
    }


    public Building addPart(Predicate<ConditionContext> test, String partName,
                            List<Pair<Predicate<ConditionContext>, String>> parts) {
        parts.add(Pair.of(test, partName));
        return this;
    }

    public int getMaxFloors() {
        return maxFloors;
    }

    public int getMaxCellars() {
        return maxCellars;
    }

    public int getMinFloors() {
        return minFloors;
    }

    public int getMinCellars() {
        return minCellars;
    }

    public char getFillerBlock() {
        return fillerBlock;
    }

    public String getRandomPart(Random random, ConditionContext info) {
        List<String> partNames = new ArrayList<>();
        for (Pair<Predicate<ConditionContext>, String> pair : parts) {
            if (pair.getLeft().test(info)) {
                partNames.add(pair.getRight());
            }
        }
        if (partNames.isEmpty()) {
            return null;
        }
        return partNames.get(random.nextInt(partNames.size()));
    }

    public String getRandomPart2(Random random, ConditionContext info) {
        List<String> partNames = new ArrayList<>();
        for (Pair<Predicate<ConditionContext>, String> pair : parts2) {
            if (pair.getLeft().test(info)) {
                partNames.add(pair.getRight());
            }
        }
        if (partNames.isEmpty()) {
            return null;
        }
        return partNames.get(random.nextInt(partNames.size()));
    }

}
