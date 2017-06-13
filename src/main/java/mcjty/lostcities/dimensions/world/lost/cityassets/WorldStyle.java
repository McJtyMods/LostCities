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

public class WorldStyle implements IAsset {

    private String name;

    private final List<Pair<Float, String>> cityStyleSelector = new ArrayList<>();

    public WorldStyle(JsonObject object) {
        readFromJSon(object);
    }

    public WorldStyle(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void readFromJSon(JsonObject object) {
        name = object.get("name").getAsString();
        JsonArray array = object.get("citystyles").getAsJsonArray();
        for (JsonElement element : array) {
            float factor = element.getAsJsonObject().get("factor").getAsFloat();
            String building = element.getAsJsonObject().get("citystyle").getAsString();
            cityStyleSelector.add(Pair.of(factor, building));
        }
    }

    @Override
    public JsonObject writeToJSon() {
        JsonObject object = new JsonObject();
        object.add("type", new JsonPrimitive("worldstyle"));
        object.add("name", new JsonPrimitive(name));

        JsonArray array = new JsonArray();
        for (Pair<Float, String> pair : cityStyleSelector) {
            JsonObject o = new JsonObject();
            o.add("factor", new JsonPrimitive(pair.getKey()));
            o.add("citystyle", new JsonPrimitive(pair.getValue()));
            array.add(o);
        }
        object.add("citystyles", array);
        return object;
    }

    public WorldStyle addCityStyle(float factor, String cityStyle){
        cityStyleSelector.add(Pair.of(factor, cityStyle));
        return this;
    }


    public String getRandomCityStyle(LostCityChunkGenerator provider, Random random) {
        return Tools.getRandomFromList(provider, random, cityStyleSelector);
    }
}
