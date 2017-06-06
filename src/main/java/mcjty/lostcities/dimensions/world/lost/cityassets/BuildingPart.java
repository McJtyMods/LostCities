package mcjty.lostcities.dimensions.world.lost.cityassets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mcjty.lostcities.dimensions.world.lost.BuildingInfo;
import net.minecraft.block.state.IBlockState;
import org.apache.commons.lang3.StringUtils;

/**
 * A section of a building. Can be either a complete floor or part of a floor.
 */
public class BuildingPart implements IAsset {

    private String name;

    // Data per height level
    private String[] slices;

    // Dimension (should be less then 16x16)
    private int xSize;
    private int zSize;

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
    public String getName() {
        return name;
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
    }

    @Override
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

        return object;
    }

    public int getSliceCount() {
        return slices.length;
    }

    public String getSlice(int i) {
        return slices[i];
    }

    public String[] getSlices() {
        return slices;
    }

    public int getXSize() {
        return xSize;
    }

    public int getZSize() {
        return zSize;
    }

    public IBlockState get(BuildingInfo info, int x, int y, int z) {
        return info.getCompiledPalette().get(slices[y].charAt(z * 16 + x), info);
    }

    public Character getC(int x, int y, int z) {
        return slices[y].charAt(z * xSize + x);
    }
}
