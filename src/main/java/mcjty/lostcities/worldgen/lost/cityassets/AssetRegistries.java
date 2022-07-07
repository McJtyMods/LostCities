package mcjty.lostcities.worldgen.lost.cityassets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mcjty.lostcities.setup.CustomRegistries;
import mcjty.lostcities.setup.ModSetup;
import mcjty.lostcities.varia.Counter;
import mcjty.lostcities.worldgen.lost.regassets.BuildingRE;
import mcjty.lostcities.worldgen.lost.regassets.PaletteRE;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class AssetRegistries {

    public static final AbstractAssetRegistry<Variant> VARIANTS = new AbstractAssetRegistry<>();
    public static final AbstractAssetRegistry<Condition> CONDITIONS = new AbstractAssetRegistry<>();
    public static final AbstractAssetRegistry<WorldStyle> WORLDSTYLES = new AbstractAssetRegistry<>();
    public static final AbstractAssetRegistry<CityStyle> CITYSTYLES = new AbstractAssetRegistry<>();
    public static final AbstractAssetRegistry<BuildingPart> PARTS = new AbstractAssetRegistry<>();
    public static final RegistryAssetRegistry<Building, BuildingRE> BUILDINGS = new RegistryAssetRegistry<>(CustomRegistries.BUILDING_REGISTRY_KEY, Building::new);
    public static final AbstractAssetRegistry<MultiBuilding> MULTI_BUILDINGS = new AbstractAssetRegistry<>();
    public static final AbstractAssetRegistry<Style> STYLES = new AbstractAssetRegistry<>();
    public static final RegistryAssetRegistry<Palette, PaletteRE> PALETTES = new RegistryAssetRegistry<>(CustomRegistries.PALETTE_REGISTRY_KEY, Palette::new);
    public static final AbstractAssetRegistry<PredefinedCity> PREDEFINED_CITIES = new AbstractAssetRegistry<>();
    public static final AbstractAssetRegistry<PredefinedSphere> PREDEFINED_SPHERES = new AbstractAssetRegistry<>();

    public static void reset() {
        VARIANTS.reset();
        CONDITIONS.reset();
        WORLDSTYLES.reset();
        PARTS.reset();
        BUILDINGS.reset();
        CITYSTYLES.reset();
        MULTI_BUILDINGS.reset();
        STYLES.reset();
        PALETTES.reset();
        PREDEFINED_CITIES.reset();
        PREDEFINED_SPHERES.reset();
    }

    public static void load(File file) {
        try(FileInputStream in = new FileInputStream(file)) {
            load(in, file.getName());
        } catch (FileNotFoundException e) {
            // Not an error
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void add(Map<Character, Set<String>> map, Character character, String partName) {
        if (!map.containsKey(character)) {
            map.put(character, new HashSet<>());
        }
        map.get(character).add(partName);
    }

    public static void load(InputStream inputstream, String filename) {
        try(BufferedReader br = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8))) {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(br);
            for (JsonElement entry : element.getAsJsonArray()) {
                JsonObject object = entry.getAsJsonObject();
                String type = object.get("type").getAsString();
                if ("style".equals(type)) {
                    STYLES.register(new Style(object));
                } else if ("variant".equals(type)) {
                    VARIANTS.register(new Variant(object));
                } else if ("condition".equals(type)) {
                    CONDITIONS.register(new Condition(object));
                } else if ("citystyle".equals(type)) {
                    CITYSTYLES.register(new CityStyle(object));
                } else if ("part".equals(type)) {
                    PARTS.register(new BuildingPart(object));
//                } else if ("building".equals(type)) {
//                    BUILDINGS.register(new Building(object));
                } else if ("multibuilding".equals(type)) {
                    MULTI_BUILDINGS.register(new MultiBuilding(object));
                } else if ("worldstyle".equals(type)) {
                    WORLDSTYLES.register(new WorldStyle(object));
                } else if ("city".equals(type)) {
                    PREDEFINED_CITIES.register(new PredefinedCity(object));
                } else if ("sphere".equals(type)) {
                    PREDEFINED_SPHERES.register(new PredefinedSphere(object));
                } else {
                    throw new RuntimeException("Unknown type '" + type + " in " + filename + "'!");
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
