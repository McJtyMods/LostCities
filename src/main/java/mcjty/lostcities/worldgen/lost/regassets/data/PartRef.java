package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * In a building this object represents a reference to a building part in combination with
 * conditions that indicate where the part can be used
 */
public class PartRef extends ConditionTest {

    public static final Codec<PartRef> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("part").forGetter(PartRef::getPart),
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
            ).apply(instance, PartRef::new));
    private final String part;

    public String getPart() {
        return part;
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
        super(top, ground, cellar, isbuilding, issphere, floor, chunkx, chunkz, inpart, inbuilding, inbiome, range);
        this.part = part;
    }
}
