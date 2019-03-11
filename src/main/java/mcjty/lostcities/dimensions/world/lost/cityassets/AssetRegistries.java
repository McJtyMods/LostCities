package mcjty.lostcities.dimensions.world.lost.cityassets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mcjty.lostcities.LostCities;
import mcjty.lostcities.varia.Counter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
    public static final AbstractAssetRegistry<PredefinedSphere> PREDEFINED_SPHERES = new AbstractAssetRegistry<>();

    public static final void reset() {
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

    public static void showStatistics() {
        Counter<Character> counterLocal = new Counter<>();
        Counter<Character> counterGlobal = new Counter<>();
        Map<Character, Set<String>> usersPerCharacter = new HashMap<>();

        for (BuildingPart part : PARTS.getIterable()) {
            Palette localPalette = part.getLocalPalette();
            Map<Character, Object> palette = Collections.emptyMap();
            if (localPalette != null) {
                palette = localPalette.palette;
            }
            for (int x = 0 ; x < part.getXSize() ; x++) {
                for (int z = 0 ; z < part.getZSize() ; z++) {
                    char[] slice = part.getVSlice(x, z);
                    if (slice != null) {
                        for (char c : slice) {
                            if (palette.containsKey(c)) {
                                counterLocal.add(c);
                            } else {
                                counterGlobal.add(c);
                            }
                            add(usersPerCharacter, c, part.getName());
                        }
                    }
                }
            }
        }
        List<Map.Entry<Character, Integer>> global = new ArrayList<>(counterGlobal.getMap().entrySet());
        List<Map.Entry<Character, Integer>> local = new ArrayList<>(counterLocal.getMap().entrySet());
        global.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        local.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        LostCities.setup.getLogger().info("############################################################################");
        LostCities.setup.getLogger().info("Global palette entries");
        printMap(usersPerCharacter, global);
        LostCities.setup.getLogger().info("----------------------------------------------------------------------------");
        LostCities.setup.getLogger().info("Local palette entries");
        printMap(usersPerCharacter, local);
        LostCities.setup.getLogger().info("----------------------------------------------------------------------------");

        printChars("Global: ", counterGlobal);
        printChars("Local: ", counterLocal);

        LostCities.setup.getLogger().info("############################################################################");
    }

    private static void printChars(String prefix, Counter<Character> counter) {
        List<Character> chars = new ArrayList<>(counter.getMap().keySet());
        chars.sort((o1, o2) -> o1.compareTo(o2));
        String s = "";
        for (Character character : chars) {
            s += character;
        }
        LostCities.setup.getLogger().info(prefix + s);
    }

    private static void printMap(Map<Character, Set<String>> usersPerCharacter, List<Map.Entry<Character, Integer>> map) {
        for (Map.Entry<Character, Integer> entry : map) {
            Set<String> users = usersPerCharacter.get(entry.getKey());
            String s = "    " + entry.getKey() + ": " + entry.getValue() + " Uses";
            if (users.size() < 10) {
                s += ", Used by: ";
                for (String user : users) {
                    s += user + ",";
                }
            } else {
                s += ", Used " + users.size() + " times";
            }
            LostCities.setup.getLogger().info(s);
        }
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
