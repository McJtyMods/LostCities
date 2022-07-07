package mcjty.lostcities.worldgen.lost.regassets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class BuildingRE implements IForgeRegistryEntry<BuildingRE> {

    public static final Codec<PartRef> CODEC_PARTREF = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("part").forGetter(l -> l.part),
                    Codec.BOOL.optionalFieldOf("top").forGetter(l -> Optional.ofNullable(l.top)),
                    Codec.BOOL.optionalFieldOf("ground").forGetter(l -> Optional.ofNullable(l.ground)),
                    Codec.BOOL.optionalFieldOf("cellar").forGetter(l -> Optional.ofNullable(l.cellar)),
                    Codec.BOOL.optionalFieldOf("isbuilding").forGetter(l -> Optional.ofNullable(l.isbuilding)),
                    Codec.BOOL.optionalFieldOf("issphere").forGetter(l -> Optional.ofNullable(l.issphere)),
                    Codec.INT.optionalFieldOf("floor").forGetter(l -> Optional.ofNullable(l.floor)),
                    Codec.INT.optionalFieldOf("chunkx").forGetter(l -> Optional.ofNullable(l.chunkx)),
                    Codec.INT.optionalFieldOf("chunkz").forGetter(l -> Optional.ofNullable(l.chunkz)),
                    Codec.STRING.optionalFieldOf("inpart").forGetter(l -> Optional.ofNullable(l.inpart)),
                    Codec.STRING.optionalFieldOf("inbuilding").forGetter(l -> Optional.ofNullable(l.inbuilding)),
                    Codec.STRING.optionalFieldOf("inbiome").forGetter(l -> Optional.ofNullable(l.inbiome)),
                    Codec.STRING.optionalFieldOf("range").forGetter(l -> Optional.ofNullable(l.range))
            ).apply(instance, PartRef::new));

    public static final Codec<BuildingRE> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("refpalette").forGetter(l -> Optional.ofNullable(l.refPaletteName)),
                    PaletteRE.CODEC.optionalFieldOf("palette").forGetter(l -> Optional.ofNullable(l.localPalette)),
                    Codec.STRING.fieldOf("filler").forGetter(l -> Character.toString(l.fillerBlock)),
                    Codec.STRING.optionalFieldOf("rubble").forGetter(l -> Optional.ofNullable(l.rubbleBlock)),
                    Codec.INT.optionalFieldOf("mincellars").forGetter(l -> l.minCellars == -1 ? Optional.<Integer>empty() : Optional.of(l.minCellars)),
                    Codec.INT.optionalFieldOf("minfloors").forGetter(l -> l.minFloors == -1 ? Optional.<Integer>empty() : Optional.of(l.minFloors)),
                    Codec.INT.optionalFieldOf("maxcellars").forGetter(l -> l.maxCellars == -1 ? Optional.<Integer>empty() : Optional.of(l.maxCellars)),
                    Codec.INT.optionalFieldOf("maxfloors").forGetter(l -> l.maxFloors == -1 ? Optional.<Integer>empty() : Optional.of(l.maxFloors)),
                    Codec.FLOAT.optionalFieldOf("preferslonely").forGetter(l -> l.prefersLonely == 0 ? Optional.<Float>empty() : Optional.of(l.prefersLonely)),
                    Codec.list(CODEC_PARTREF).fieldOf("parts").forGetter(l -> l.parts),
                    Codec.list(CODEC_PARTREF).optionalFieldOf("parts2").forGetter(l -> Optional.ofNullable(l.parts2))
            ).apply(instance, BuildingRE::new));


    private ResourceLocation name;

    private int minFloors = -1;         // -1 means default from level
    private int minCellars = -1;        // -1 means default frmo level
    private int maxFloors = -1;         // -1 means default from level
    private int maxCellars = -1;        // -1 means default frmo level
    private char fillerBlock;           // Block used to fill/close areas. Usually the block of the building itself
    private String rubbleBlock;         // Block used for destroyed building rubble
    private float prefersLonely = 0.0f; // The chance this this building is alone. If 1.0f this building wants to be alone all the time

    private PaletteRE localPalette = null;
    private String refPaletteName;

    private final List<PartRef> parts;
    private final List<PartRef> parts2;

    public BuildingRE(Optional<String> refpalette, Optional<PaletteRE> locpalette, String filler, Optional<String> rubble,
                      Optional<Integer> minCellars, Optional<Integer> minFloors, Optional<Integer> maxCellars, Optional<Integer> maxFloors, Optional<Float> prefersLonely,
                      List<PartRef> partRefs, Optional<List<PartRef>> partRefs2) {
        this.refPaletteName = refpalette.isPresent() ? refpalette.get() : null;
        this.localPalette = locpalette.isPresent() ? locpalette.get() : null;
        this.fillerBlock = filler.charAt(0);
        this.rubbleBlock = rubble.isPresent() ? rubble.get() : null;
        this.minCellars = minCellars.isPresent() ? minCellars.get() : -1;
        this.maxCellars = maxCellars.isPresent() ? maxCellars.get() : -1;
        this.minFloors = minFloors.isPresent() ? minFloors.get() : -1;
        this.maxFloors = maxFloors.isPresent() ? maxFloors.get() : -1;
        this.prefersLonely = prefersLonely.isPresent() ? prefersLonely.get() : 0.0f;
        this.parts = partRefs;
        this.parts2 = partRefs2.isPresent() ? partRefs2.get() : null;
    }

    @Override
    public BuildingRE setRegistryName(ResourceLocation name) {
        this.name = name;
        return this;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return name;
    }

    @Override
    public Class<BuildingRE> getRegistryType() {
        return BuildingRE.class;
    }

    public int getMinFloors() {
        return minFloors;
    }

    public int getMinCellars() {
        return minCellars;
    }

    public int getMaxFloors() {
        return maxFloors;
    }

    public int getMaxCellars() {
        return maxCellars;
    }

    public char getFillerBlock() {
        return fillerBlock;
    }

    public Character getRubbleBlock() {
        return rubbleBlock == null ? null : rubbleBlock.charAt(0);
    }

    public float getPrefersLonely() {
        return prefersLonely;
    }

    public PaletteRE getLocalPalette() {
        return localPalette;
    }

    public String getRefPaletteName() {
        return refPaletteName;
    }

    public List<PartRef> getParts() {
        return parts;
    }

    public List<PartRef> getParts2() {
        return parts2;
    }

    public static class PartRef {
        private String part;
        private Boolean top;
        private Boolean ground;
        private Boolean cellar;
        private Boolean isbuilding;
        private Boolean issphere;
        private Integer floor;
        private Integer chunkx;
        private Integer chunkz;
        private String inpart;
        private String inbuilding;
        private String inbiome;
        private String range;

        public String getPart() {
            return part;
        }

        public Boolean getTop() {
            return top;
        }

        public Boolean getGround() {
            return ground;
        }

        public Boolean getCellar() {
            return cellar;
        }

        public Boolean getIsbuilding() {
            return isbuilding;
        }

        public Boolean getIssphere() {
            return issphere;
        }

        public Integer getFloor() {
            return floor;
        }

        public Integer getChunkx() {
            return chunkx;
        }

        public Integer getChunkz() {
            return chunkz;
        }

        public String getInpart() {
            return inpart;
        }

        public String getInbuilding() {
            return inbuilding;
        }

        public String getInbiome() {
            return inbiome;
        }

        public String getRange() {
            return range;
        }

        public PartRef(String part,
                       Optional<Boolean> top,
                       Optional<Boolean> ground,
                       Optional<Boolean> cellar,
                       Optional<Boolean> isbuilding,
                       Optional<Boolean> issphere,
                       Optional<Integer> floor,
                       Optional<Integer> chunkx,
                       Optional<Integer> chunkz,
                       Optional<String> inpart,
                       Optional<String> inbuilding,
                       Optional<String> inbiome,
                       Optional<String> range) {
            this.part = part;
            this.top = top.isPresent() ? top.get() : null;
            this.ground = ground.isPresent() ? ground.get() : null;
            this.cellar = cellar.isPresent() ? cellar.get() : null;
            this.isbuilding = isbuilding.isPresent() ? isbuilding.get() : null;
            this.issphere = issphere.isPresent() ? issphere.get() : null;
            this.floor = floor.isPresent() ? floor.get() : null;
            this.chunkx = chunkx.isPresent() ? chunkx.get() : null;
            this.chunkz = chunkz.isPresent() ? chunkz.get() : null;
            this.inpart = inpart.isPresent() ? inpart.get() : null;
            this.inbuilding = inbuilding.isPresent() ? inbuilding.get() : null;
            this.inbiome = inbiome.isPresent() ? inbiome.get() : null;
            this.range = range.isPresent() ? range.get() : null;
        }
    }
}
