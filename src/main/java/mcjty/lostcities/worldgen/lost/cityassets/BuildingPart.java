package mcjty.lostcities.worldgen.lost.cityassets;

import com.google.gson.JsonObject;
import mcjty.lostcities.api.ILostCityAsset;
import mcjty.lostcities.setup.ModSetup;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.regassets.BuildingPartRE;
import net.minecraft.world.level.CommonLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

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

    private final Map<String, Object> metadata = new HashMap<>();

    public BuildingPart(BuildingPartRE object) {
        name = object.getRegistryName().getPath(); // @todo temporary. Needs to be fully qualified
        xSize = object.getxSize();
        zSize = object.getzSize();
        slices = object.getSlices();
        if (object.getLocalPalette() != null) {
            localPalette = new Palette("__local__" + name);
            localPalette.parsePaletteArray(object.getLocalPalette()); // @todo get the full palette instead
        } else if (object.getRefPaletteName() != null) {
            refPaletteName = object.getRefPaletteName();
        }
        if (object.getMetadata() != null) {
            for (BuildingPartRE.PartMeta meta : object.getMetadata()) {
                String key = meta.getKey();
                if (meta.getI() != null) {
                    metadata.put(key, meta.getI());
                } else if (meta.getF() != null) {
                    metadata.put(key, meta.getF());
                } else if (meta.getBool() != null) {
                    metadata.put(key, meta.getBool());
                } else if (meta.getChr() != null) {
                    metadata.put(key, meta.getChr().charAt(0));
                } else if (meta.getBool()) {
                    metadata.put(key, meta.getBool());
                }
            }
        }
    }

    public Map<String, Object> getMetadata() {
        return metadata;
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
                    StringBuilder vs = new StringBuilder();
                    boolean empty = true;
                    for (int y = 0; y < slices.length; y++) {
                        Character c = getC(x, y, z);
                        vs.append(c);
                        if (c != ' ') {
                            empty = false;
                        }
                    }
                    if (empty) {
                        vslices[z*xSize+x] = null;
                    } else {
                        vslices[z*xSize+x] = vs.toString().toCharArray();
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
    public Palette getLocalPalette(CommonLevelAccessor level) {
        if (localPalette == null && refPaletteName != null) {
            localPalette = AssetRegistries.PALETTES.get(level, refPaletteName);
            if (localPalette == null) {
                ModSetup.getLogger().error("Could not find palette '" + refPaletteName + "'!");
                throw new RuntimeException("Could not find palette '" + refPaletteName + "'!");
            }
        }
        return localPalette;
    }

    @Override
    public void readFromJSon(JsonObject object) {
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

    public BlockState get(BuildingInfo info, int x, int y, int z) {
        return info.getCompiledPalette().get(slices[y].charAt(z * xSize + x));
    }

    public Character getC(int x, int y, int z) {
        return slices[y].charAt(z * xSize + x);
    }
}
