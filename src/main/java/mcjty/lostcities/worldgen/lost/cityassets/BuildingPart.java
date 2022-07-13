package mcjty.lostcities.worldgen.lost.cityassets;

import mcjty.lostcities.api.ILostCityAsset;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.regassets.BuildingPartRE;
import mcjty.lostcities.worldgen.lost.regassets.data.DataTools;
import mcjty.lostcities.worldgen.lost.regassets.data.PartMeta;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.CommonLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

/**
 * A structure part
 */
public class BuildingPart implements IBuildingPart, ILostCityAsset {

    private final ResourceLocation name;

    // Data per height level
    private final String[] slices;

    // Dimension (should be less then 16x16)
    private final int xSize;
    private final int zSize;

    // Optimized version of this part which is organized in xSize*ySize vertical strings
    private char[][] vslices = null;

    private Palette localPalette = null;
    String refPaletteName;

    private final Map<String, Object> metadata = new HashMap<>();

    public BuildingPart(BuildingPartRE object) {
        name = object.getRegistryName();
        xSize = object.getxSize();
        zSize = object.getzSize();
        slices = object.getSlices();
        if (object.getLocalPalette() != null) {
            localPalette = new Palette("__local__" + name.getPath());
            localPalette.parsePaletteArray(object.getLocalPalette()); // @todo get the full palette instead
        } else if (object.getRefPaletteName() != null) {
            refPaletteName = object.getRefPaletteName();
        }
        if (object.getMetadata() != null) {
            for (PartMeta meta : object.getMetadata()) {
                String key = meta.key();
                if (meta.i() != null) {
                    metadata.put(key, meta.i());
                } else if (meta.f() != null) {
                    metadata.put(key, meta.f());
                } else if (meta.bool() != null) {
                    metadata.put(key, meta.bool());
                } else if (meta.chr() != null) {
                    metadata.put(key, meta.chr().charAt(0));
                } else if (meta.str() != null) {
                    metadata.put(key, meta.str());
                }
            }
        }
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
        return DataTools.toName(name);
    }

    @Override
    public ResourceLocation getId() {
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
            localPalette = AssetRegistries.PALETTES.getOrThrow(level, refPaletteName);
        }
        return localPalette;
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
