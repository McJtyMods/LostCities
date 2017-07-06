package mcjty.lostcities.dimensions.world.lost.cityassets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;

public class AssetRegistries {

    public static final AbstractAssetRegistry<Condition> CONDITIONS = new AbstractAssetRegistry<>();
    public static final AbstractAssetRegistry<WorldStyle> WORLDSTYLES = new AbstractAssetRegistry<>();
    public static final AbstractAssetRegistry<CityStyle> CITYSTYLES = new AbstractAssetRegistry<>();
    public static final AbstractAssetRegistry<BuildingPart> PARTS = new AbstractAssetRegistry<>();
    public static final AbstractAssetRegistry<Building> BUILDINGS = new AbstractAssetRegistry<>();
    public static final AbstractAssetRegistry<MultiBuilding> MULTI_BUILDINGS = new AbstractAssetRegistry<>();
    public static final AbstractAssetRegistry<Style> STYLES = new AbstractAssetRegistry<>();
    public static final AbstractAssetRegistry<Palette> PALETTES = new AbstractAssetRegistry<>();
    public static final AbstractAssetRegistry<PredefinedCity> PREDEFINED_CITIES = new AbstractAssetRegistry<>();

    public static final void reset() {
        System.out.println("AssetRegistries.reset");
        CONDITIONS.reset();
        WORLDSTYLES.reset();
        PARTS.reset();
        BUILDINGS.reset();
        CITYSTYLES.reset();
        MULTI_BUILDINGS.reset();
        STYLES.reset();
        PALETTES.reset();
        PREDEFINED_CITIES.reset();
    }

    public static void load(File file) {
        try {
            load(new FileInputStream(file), file.getName());
        } catch (FileNotFoundException e) {
            // Not an error
        }
    }

    public static void load(InputStream inputstream, String filename) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputstream, "UTF-8"));
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(br);
            for (JsonElement entry : element.getAsJsonArray()) {
                JsonObject object = entry.getAsJsonObject();
                String type = object.get("type").getAsString();
                if ("style".equals(type)) {
                    STYLES.register(new Style(object));
                } else if ("condition".equals(type)) {
                    CONDITIONS.register(new Condition(object));
                } else if ("palette".equals(type)) {
                    PALETTES.register(new Palette(object));
                } else if ("citystyle".equals(type)) {
                    CITYSTYLES.register(new CityStyle(object));
                } else if ("part".equals(type)) {
                    PARTS.register(new BuildingPart(object));
                } else if ("building".equals(type)) {
                    BUILDINGS.register(new Building(object));
                } else if ("multibuilding".equals(type)) {
                    MULTI_BUILDINGS.register(new MultiBuilding(object));
                } else if ("worldstyle".equals(type)) {
                    WORLDSTYLES.register(new WorldStyle(object));
                } else if ("city".equals(type)) {
                    PREDEFINED_CITIES.register(new PredefinedCity(object));
                } else {
                    throw new RuntimeException("Unknown type '" + type + " in " + filename + "'!");
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
