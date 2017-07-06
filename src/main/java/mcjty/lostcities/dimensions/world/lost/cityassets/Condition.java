package mcjty.lostcities.dimensions.world.lost.cityassets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mcjty.lostcities.varia.Tools;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class Condition implements IAsset {

    private String name;

    private final List<Pair<Predicate<ConditionContext>, Pair<Float, String>>> valueSelector = new ArrayList<>();

    public Condition(JsonObject object) {
        readFromJSon(object);
    }

    public Condition(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void readFromJSon(JsonObject object) {
        name = object.get("name").getAsString();
        JsonArray array = object.get("values").getAsJsonArray();
        for (JsonElement element : array) {
            JsonObject o = element.getAsJsonObject();
            float factor = o.get("factor").getAsFloat();
            String value = o.get("value").getAsString();
            Predicate<ConditionContext> test = ConditionContext.parseTest(element);
            valueSelector.add(Pair.of(test, Pair.of(factor, value)));
        }
    }

    @Override
    public JsonObject writeToJSon() {
        JsonObject object = new JsonObject();
        object.add("type", new JsonPrimitive("condition"));
        object.add("name", new JsonPrimitive(name));
        return object;
    }


    public String getRandomValue(Random random, ConditionContext info) {
        List<Pair<Float, String>> values = new ArrayList<>();
        for (Pair<Predicate<ConditionContext>, Pair<Float, String>> pair : valueSelector) {
            if (pair.getLeft().test(info)) {
                values.add(pair.getRight());
            }
        }
        if (values.isEmpty()) {
            return null;
        }
        return Tools.getRandomFromList(random, values);
    }
}
