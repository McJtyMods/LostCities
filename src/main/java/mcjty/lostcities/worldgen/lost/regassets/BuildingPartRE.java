package mcjty.lostcities.worldgen.lost.regassets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.worldgen.lost.regassets.data.PartMeta;
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

    public static final Codec<BuildingPartRE> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("xsize").forGetter(l -> l.xSize),
                    Codec.INT.fieldOf("zsize").forGetter(l -> l.zSize),
                    Codec.list(Codec.list(Codec.STRING)).fieldOf("slices").forGetter(BuildingPartRE::createSlices),
                    Codec.STRING.optionalFieldOf("refpalette").forGetter(l -> Optional.ofNullable(l.refPaletteName)),
                    PaletteRE.CODEC.optionalFieldOf("palette").forGetter(l -> Optional.ofNullable(l.localPalette)),
                    Codec.list(PartMeta.CODEC).optionalFieldOf("meta").forGetter(l -> Optional.ofNullable(l.metadata))
            ).apply(instance, BuildingPartRE::new));

    private ResourceLocation name;

    // Data per height level
    private final String[] slices;

    // Dimension (should be less then 16x16)
    private final int xSize;
    private final int zSize;

    private PaletteRE localPalette = null;
    private final String refPaletteName;

    private final List<PartMeta> metadata;

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
        this.refPaletteName = refpalette.orElse(null);
        this.localPalette = locpalette.orElse(null);
        this.metadata = metadata.orElse(null);
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
