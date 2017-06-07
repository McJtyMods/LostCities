package mcjty.lostcities.dimensions.world.lost.cityassets;

import com.google.common.collect.Lists;
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
    private final List<Pair<Float, String>> parkSelector = new ArrayList<>();
    private final List<Pair<Float, String>> multiBuildingSelector = new ArrayList<>();
    private final List<List<Pair<Float, String>>> randomStyleChoices = new ArrayList<>();

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

    @Override
    public void readFromJSon(JsonObject object) {
        name = object.get("name").getAsString();
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
        array = object.get("randomstylechoices").getAsJsonArray();
        for (JsonElement element : array) {
            List<Pair<Float, String>> styles = new ArrayList<>();
            for (JsonElement el : element.getAsJsonArray()) {
                float factor = el.getAsJsonObject().get("factor").getAsFloat();
                String style = el.getAsJsonObject().get("style").getAsString();
                styles.add(Pair.of(factor, style));
            }
            randomStyleChoices.add(styles);
        }
    }

    @Override
    public JsonObject writeToJSon() {
        JsonObject object = new JsonObject();
        object.add("type", new JsonPrimitive("citystyle"));
        object.add("name", new JsonPrimitive(name));

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
        for (List<Pair<Float, String>> list : randomStyleChoices) {
            JsonArray a = new JsonArray();
            for (Pair<Float, String> pair : list) {
                JsonObject o = new JsonObject();
                o.add("factor", new JsonPrimitive(pair.getKey()));
                o.add("style", new JsonPrimitive(pair.getValue()));
                a.add(o);
            }
            array.add(a);
        }
        object.add("randomstylechoices", array);

        return object;
    }

    public CityStyle addPark(float factor, String park) {
        parkSelector.add(Pair.of(factor, park));
        return this;
    }

    public CityStyle addBuilding(float factor, String building) {
        buildingSelector.add(Pair.of(factor, building));
        return this;
    }

    public CityStyle addMultiBuilding(float factor, String multiBuilding) {
        multiBuildingSelector.add(Pair.of(factor, multiBuilding));
        return this;
    }

    public CityStyle addRandomStyleChoice(Pair<Float, String>... styles) {
        randomStyleChoices.add(Lists.newArrayList(styles));
        return this;
    }

    public Style getRandomStyle(LostCityChunkGenerator provider, Random random) {
        Style style = new Style();
        for (List<Pair<Float, String>> pairs : randomStyleChoices) {
            float totalweight = 0;
            for (Pair<Float, String> pair : pairs) {
                totalweight += pair.getKey();
            }
            float r = random.nextFloat() * totalweight;
            Style tomerge = null;
            for (Pair<Float, String> pair : pairs) {
                r -= pair.getKey();
                if (r <= 0) {
                    tomerge = AssetRegistries.STYLES.get(pair.getRight());
                    if (tomerge == null) {
                        throw new RuntimeException("Style '" + pair.getRight() + "' is missing!");
                    }
                    break;
                }
            }
            style.merge(tomerge);
        }

        return style;
    }

    public String getRandomPark(LostCityChunkGenerator provider, Random random) {
        List<Pair<Float, String>> parks = new ArrayList<>();
        float totalweight = 0;
        for (Pair<Float, String> pair : parkSelector) {
            parks.add(pair);
            totalweight += pair.getKey();
        }
        float r = random.nextFloat() * totalweight;
        for (Pair<Float, String> pair : parks) {
            r -= pair.getKey();
            if (r <= 0) {
                return pair.getRight();
            }
        }
        return null;
    }

    public String getRandomBuilding(LostCityChunkGenerator provider, Random random) {
        List<Pair<Float, String>> buildings = new ArrayList<>();
        float totalweight = 0;
        for (Pair<Float, String> pair : buildingSelector) {
            buildings.add(pair);
            totalweight += pair.getKey();
        }
        float r = random.nextFloat() * totalweight;
        for (Pair<Float, String> pair : buildings) {
            r -= pair.getKey();
            if (r <= 0) {
                return pair.getRight();
            }
        }
        return null;
    }

    public String getRandomMultiBuilding(LostCityChunkGenerator provider, Random random) {
        List<Pair<Float, String>> multiBuildings = new ArrayList<>();
        float totalweight = 0;
        for (Pair<Float, String> pair : multiBuildingSelector) {
            multiBuildings.add(pair);
            totalweight += pair.getKey();
        }
        float r = random.nextFloat() * totalweight;
        for (Pair<Float, String> pair : multiBuildings) {
            r -= pair.getKey();
            if (r <= 0) {
                return pair.getRight();
            }
        }
        return null;
    }
}
