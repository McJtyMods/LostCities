package mcjty.lostcities.worldgen.lost.regassets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ConditionRE implements IForgeRegistryEntry<ConditionRE> {

    public static class ConditionTest {
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

        public ConditionTest(
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

    public static class ConditionPart extends ConditionTest {
        private float factor;
        private String value;

        public float getFactor() {
            return factor;
        }

        public String getValue() {
            return value;
        }

        public ConditionPart(float factor, String value,
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
            super(top, ground, cellar, isbuilding, issphere, floor, chunkx, chunkz, inpart, inbuilding, inbiome, range);
            this.factor = factor;
            this.value = value;
        }
    }

    public static final Codec<ConditionPart> CONDITION_PART_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("factor").forGetter(l -> l.factor),
                    Codec.STRING.fieldOf("value").forGetter(l -> l.value),
                    Codec.BOOL.optionalFieldOf("top").forGetter(l -> Optional.ofNullable(l.getTop())),
                    Codec.BOOL.optionalFieldOf("ground").forGetter(l -> Optional.ofNullable(l.getGround())),
                    Codec.BOOL.optionalFieldOf("cellar").forGetter(l -> Optional.ofNullable(l.getCellar())),
                    Codec.BOOL.optionalFieldOf("isbuilding").forGetter(l -> Optional.ofNullable(l.getIsbuilding())),
                    Codec.BOOL.optionalFieldOf("issphere").forGetter(l -> Optional.ofNullable(l.getIssphere())),
                    Codec.INT.optionalFieldOf("floor").forGetter(l -> Optional.ofNullable(l.getFloor())),
                    Codec.INT.optionalFieldOf("chunkx").forGetter(l -> Optional.ofNullable(l.getChunkx())),
                    Codec.INT.optionalFieldOf("chunkz").forGetter(l -> Optional.ofNullable(l.getChunkz())),
                    Codec.STRING.optionalFieldOf("inpart").forGetter(l -> Optional.ofNullable(l.getInpart())),
                    Codec.STRING.optionalFieldOf("inbuilding").forGetter(l -> Optional.ofNullable(l.getInbuilding())),
                    Codec.STRING.optionalFieldOf("inbiome").forGetter(l -> Optional.ofNullable(l.getInbiome())),
                    Codec.STRING.optionalFieldOf("range").forGetter(l -> Optional.ofNullable(l.getRange()))
            ).apply(instance, ConditionPart::new));

    public static final Codec<ConditionRE> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(CONDITION_PART_CODEC).fieldOf("values").forGetter(l -> l.values)
            ).apply(instance, ConditionRE::new));

    private ResourceLocation name;

    private final List<ConditionPart> values;

    public ConditionRE(List<ConditionPart> values) {
        this.values = values;
    }

    public List<ConditionPart> getValues() {
        return values;
    }

    @Override
    public ConditionRE setRegistryName(ResourceLocation name) {
        this.name = name;
        return this;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return name;
    }

    @Override
    public Class<ConditionRE> getRegistryType() {
        return ConditionRE.class;
    }

}
