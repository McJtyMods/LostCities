package mcjty.lostcities.dimensions.world.lost.cityassets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mcjty.lostcities.api.ILostCityAsset;
import mcjty.lostcities.dimensions.IDimensionInfo;
import mcjty.lostcities.dimensions.world.LostCityChunkGenerator;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Style implements ILostCityAsset {

    private String name;

    private final List<List<Pair<Float, String>>> randomPaletteChoices = new ArrayList<>();

    public Style(String name) {
        this.name = name;
    }

    public Style(JsonObject object) {
        readFromJSon(object);
    }

    @Override
    public void readFromJSon(JsonObject object) {
        name = object.get("name").getAsString();
        JsonArray array = object.get("randompalettes").getAsJsonArray();
        for (JsonElement element : array) {
            List<Pair<Float, String>> palettes = new ArrayList<>();
            for (JsonElement el : element.getAsJsonArray()) {
                float factor = el.getAsJsonObject().get("factor").getAsFloat();
                String style = el.getAsJsonObject().get("palette").getAsString();
                palettes.add(Pair.of(factor, style));
            }
            randomPaletteChoices.add(palettes);
        }
    }

    public JsonObject writeToJSon() {
        JsonObject object = new JsonObject();
        object.add("type", new JsonPrimitive("style"));
        object.add("name", new JsonPrimitive(name));
        JsonArray array = new JsonArray();
        for (List<Pair<Float, String>> list : randomPaletteChoices) {
            JsonArray a = new JsonArray();
            for (Pair<Float, String> pair : list) {
                JsonObject o = new JsonObject();
                o.add("factor", new JsonPrimitive(pair.getKey()));
                o.add("palette", new JsonPrimitive(pair.getValue()));
                a.add(o);
            }
            array.add(a);
        }
        object.add("randompalettes", array);
        return object;
    }

    @Override
    public String getName() {
        return name;
    }

    public Palette getRandomPalette(IDimensionInfo provider, Random random) {
        Palette palette = new Palette();
        for (List<Pair<Float, String>> pairs : randomPaletteChoices) {
            float totalweight = 0;
            for (Pair<Float, String> pair : pairs) {
                totalweight += pair.getKey();
            }
            float r = random.nextFloat() * totalweight;
            Palette tomerge = null;
            for (Pair<Float, String> pair : pairs) {
                r -= pair.getKey();
                if (r <= 0) {
                    tomerge = AssetRegistries.PALETTES.get(pair.getRight());
                    if (tomerge == null) {
                        throw new RuntimeException("Palette '" + pair.getRight() + "' is missing!");
                    }
                    break;
                }
            }
            palette.merge(tomerge);
        }

        return palette;
    }


}
