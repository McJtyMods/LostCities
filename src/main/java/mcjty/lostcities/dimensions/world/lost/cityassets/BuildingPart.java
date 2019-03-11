package mcjty.lostcities.dimensions.world.lost.cityassets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mcjty.lostcities.LostCities;
import mcjty.lostcities.api.ILostCityAsset;
import mcjty.lostcities.dimensions.world.lost.BuildingInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * A structure part
 */
public class BuildingPart implements IBuildingPart, ILostCityAsset {

    private String name;

    // Data per height level
    private String[] slices;

    // Dimension (should be less then 16x16)
    private int xSize;
    private int zSize;

    // Optimized version of this part which is organized in xSize*ySize vertical strings
    private char[][] vslices = null;

    private Palette localPalette = null;
    String refPaletteName;


    private Map<String, Object> metadata = new HashMap<>();

    public BuildingPart(JsonObject object) {
        readFromJSon(object);
    }

    public BuildingPart(String name, int xSize, int zSize, String[] slices) {
        this.name = name;
        this.slices = slices;
        this.xSize = xSize;
        this.zSize = zSize;
    }

    @Override
    public Character getMetaChar(String key) {
        return (Character) metadata.get(key);
    }

    @Override
    public Integer getMetaInteger(String key) {
        return (Integer) metadata.get(key);
    }
    @Override
    public boolean getMetaBoolean(String key) {
        Object o = metadata.get(key);
        return o instanceof Boolean ? (Boolean) o : false;
    }
    @Override
    public Float getMetaFloat(String key) {
        return (Float) metadata.get(key);
    }
    @Override
    public String getMetaString(String key) {
        return (String) metadata.get(key);
    }

    @Override
    public String getName() {
        return name;
    }


    /**
     * Vertical slices, organized by z*xSize+x
     */
    @Override
    public char[][] getVslices() {
        if (vslices == null) {
            vslices = new char[xSize * zSize][];
            for (int x = 0 ; x < xSize ; x++) {
                for (int z = 0 ; z < zSize ; z++) {
                    String vs = "";
                    boolean empty = true;
                    for (int y = 0; y < slices.length; y++) {
                        Character c = getC(x, y, z);
                        vs += c;
                        if (c != ' ') {
                            empty = false;
                        }
                    }
                    if (empty) {
                        vslices[z*xSize+x] = null;
                    } else {
                        vslices[z*xSize+x] = vs.toCharArray();
                    }
                }
            }
        }
        return vslices;
    }

    @Override
    public char[] getVSlice(int x, int z) {
        return getVslices()[z*xSize + x];
    }

    @Override
    public Palette getLocalPalette() {
        if (localPalette == null && refPaletteName != null) {
            localPalette = AssetRegistries.PALETTES.get(refPaletteName);
            if (localPalette == null) {
                LostCities.setup.getLogger().error("Could not find palette '" + refPaletteName + "'!");
                throw new RuntimeException("Could not find palette '" + refPaletteName + "'!");
            }
        }
        return localPalette;
    }

    @Override
    public void readFromJSon(JsonObject object) {
        name = object.get("name").getAsString();
        xSize = object.get("xsize").getAsInt();
        zSize = object.get("zsize").getAsInt();
        JsonArray sliceArray = object.get("slices").getAsJsonArray();
        slices = new String[sliceArray.size()];
        int i = 0;
        for (JsonElement element : sliceArray) {
            JsonArray a = element.getAsJsonArray();
            String slice = "";
            for (JsonElement el : a) {
                slice += el.getAsString();
            }
            slices[i++] = slice;
        }
        if (object.has("palette")) {
            if (object.get("palette").isJsonArray()) {
                JsonArray palette = object.get("palette").getAsJsonArray();
                localPalette = new Palette();
                localPalette.parsePaletteArray(palette);
            } else {
                refPaletteName = object.get("palette").getAsString();
            }
        }
        if (object.has("meta")) {
            JsonArray metaArray = object.get("meta").getAsJsonArray();
            for (JsonElement element : metaArray) {
                JsonObject o = element.getAsJsonObject();
                String key = o.get("key").getAsString();
                if (o.has("integer")) {
                    metadata.put(key, o.get("integer").getAsInt());
                } else if (o.has("float")) {
                    metadata.put(key, o.get("float").getAsFloat());
                } else if (o.has("boolean")) {
                    metadata.put(key, o.get("boolean").getAsBoolean());
                } else if (o.has("char")) {
                    metadata.put(key, o.get("char").getAsCharacter());
                } else if (o.has("character")) {
                    metadata.put(key, o.get("character").getAsCharacter());
                } else if (o.has("string")) {
                    metadata.put(key, o.get("string").getAsString());
                }
            }
        }
    }

    public JsonObject writeToJSon() {
        JsonObject object = new JsonObject();
        object.add("type", new JsonPrimitive("part"));
        object.add("name", new JsonPrimitive(name));
        object.add("xsize", new JsonPrimitive(xSize));
        object.add("zsize", new JsonPrimitive(zSize));
        JsonArray sliceArray = new JsonArray();
        for (String slice : slices) {
            JsonArray a = new JsonArray();
            while (!slice.isEmpty()) {
                String left = StringUtils.left(slice, xSize);
                a.add(new JsonPrimitive(left));
                slice = slice.substring(left.length());
            }
            sliceArray.add(a);
        }
        object.add("slices", sliceArray);

        JsonArray metaArray = new JsonArray();
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            JsonObject o = new JsonObject();
            o.add("key", new JsonPrimitive(entry.getKey()));
            Object v = entry.getValue();
            if (v instanceof Integer) {
                o.add("integer", new JsonPrimitive((Integer) v));
            } else if (v instanceof Float) {
                o.add("float", new JsonPrimitive((Float) v));
            } else if (v instanceof Boolean) {
                o.add("boolean", new JsonPrimitive((Boolean) v));
            } else if (v instanceof String) {
                o.add("string", new JsonPrimitive((String) v));
            }
            metaArray.add(o);
        }
        object.add("meta", metaArray);

        return object;
    }

    @Override
    public int getSliceCount() {
        return slices.length;
    }

    public String getSlice(int i) {
        return slices[i];
    }

    public String[] getSlices() {
        return slices;
    }

    @Override
    public int getXSize() {
        return xSize;
    }

    @Override
    public int getZSize() {
        return zSize;
    }

    public Character getPaletteChar(int x, int y, int z) {
        return slices[y].charAt(z * xSize + x);
    }

    public Character get(BuildingInfo info, int x, int y, int z) {
        return info.getCompiledPalette().get(slices[y].charAt(z * xSize + x));
    }

    public Character getC(int x, int y, int z) {
        return slices[y].charAt(z * xSize + x);
    }
}
