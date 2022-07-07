package mcjty.lostcities.worldgen.lost.regassets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A structure part
 */
public class BuildingPartRE implements IForgeRegistryEntry<BuildingPartRE> {

    public static class PartMeta {
        private String key;
        private Boolean bool;
        private String chr;
        private String str;
        private Integer i;
        private Float f;

        public String getKey() {
            return key;
        }

        public Boolean getBool() {
            return bool;
        }

        public String getChr() {
            return chr;
        }

        public String getStr() {
            return str;
        }

        public Integer getI() {
            return i;
        }

        public Float getF() {
            return f;
        }

        public PartMeta(String key, Optional<Boolean> bool, Optional<String> chr, Optional<String> str,
                        Optional<Integer> i, Optional<Float> f) {
            this.key = key;
            this.bool = bool.isPresent() ? bool.get() : null;
            this.chr = chr.isPresent() ? chr.get() : null;
            this.str = str.isPresent() ? str.get() : null;
            this.i = i.isPresent() ? i.get() : null;
            this.f = f.isPresent() ? f.get() : null;
        }
    }

    public static final Codec<PartMeta> CODEC_PARTMETA = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("key").forGetter(l -> l.key),
                    Codec.BOOL.optionalFieldOf("boolean").forGetter(l -> Optional.ofNullable(l.bool)),
                    Codec.STRING.optionalFieldOf("char").forGetter(l -> Optional.ofNullable(l.chr)),
                    Codec.STRING.optionalFieldOf("string").forGetter(l -> Optional.ofNullable(l.str)),
                    Codec.INT.optionalFieldOf("integer").forGetter(l -> Optional.ofNullable(l.i)),
                    Codec.FLOAT.optionalFieldOf("float").forGetter(l -> Optional.ofNullable(l.f))
            ).apply(instance, PartMeta::new));

    public static final Codec<BuildingPartRE> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("xsize").forGetter(l -> l.xSize),
                    Codec.INT.fieldOf("zsize").forGetter(l -> l.zSize),
                    Codec.list(Codec.list(Codec.STRING)).fieldOf("slices").forGetter(BuildingPartRE::createSlices),
                    Codec.STRING.optionalFieldOf("refpalette").forGetter(l -> Optional.ofNullable(l.refPaletteName)),
                    PaletteRE.CODEC.optionalFieldOf("palette").forGetter(l -> Optional.ofNullable(l.localPalette)),
                    Codec.list(CODEC_PARTMETA).optionalFieldOf("meta").forGetter(l -> Optional.ofNullable(l.metadata))
            ).apply(instance, BuildingPartRE::new));

    private ResourceLocation name;

    // Data per height level
    private String[] slices;

    // Dimension (should be less then 16x16)
    private int xSize;
    private int zSize;

    private PaletteRE localPalette = null;
    private String refPaletteName;

    private List<PartMeta> metadata;

    public BuildingPartRE(int xSize, int zSize, List<List<String>> slices, Optional<String> refpalette, Optional<PaletteRE> locpalette,
                          Optional<List<PartMeta>> metadata) {
        this.slices = new String[slices.size()];
        int idx = 0;
        for (List<String> slice : slices) {
            StringBuilder builder = new StringBuilder();
            for (String s : slice) {
                builder.append(s);
            }
            this.slices[idx++] = builder.toString();
        }
        this.xSize = xSize;
        this.zSize = zSize;
        this.refPaletteName = refpalette.isPresent() ? refpalette.get() : null;
        this.localPalette = locpalette.isPresent() ? locpalette.get() : null;
        this.metadata = metadata.isPresent() ? metadata.get() : null;
    }

    private List<List<String>> createSlices() {
        List<List<String>> result = new ArrayList<>();
        for (String slice : slices) {
            List<String> s = new ArrayList<>();
            for (int z = 0 ; z < zSize ; z++) {
                String sub = slice.substring(z * xSize, z * xSize + xSize);
                s.add(sub);
            }
            result.add(s);
        }
        return result;
    }

    public List<PartMeta> getMetadata() {
        return metadata;
    }

    public String[] getSlices() {
        return slices;
    }

    public int getxSize() {
        return xSize;
    }

    public int getzSize() {
        return zSize;
    }

    public PaletteRE getLocalPalette() {
        return localPalette;
    }

    public String getRefPaletteName() {
        return refPaletteName;
    }

    @Override
    public BuildingPartRE setRegistryName(ResourceLocation name) {
        this.name = name;
        return this;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return name;
    }

    @Override
    public Class<BuildingPartRE> getRegistryType() {
        return BuildingPartRE.class;
    }
}
