package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * A selector for parts for monorail and railstation
 */
public record PartSelector(MonorailParts monoRailParts, HighwayParts highwayParts, RailwayParts railwayParts) {

    public static final Codec<PartSelector> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    MonorailParts.CODEC.optionalFieldOf("monorails").forGetter(l -> l.monoRailParts.get()),
                    HighwayParts.CODEC.optionalFieldOf("highways").forGetter(l -> l.highwayParts.get()),
                    RailwayParts.CODEC.optionalFieldOf("railways").forGetter(l -> l.railwayParts.get())
            ).apply(instance, (monorails, highways, railways) -> new PartSelector(
                    monorails.orElse(MonorailParts.DEFAULT),
                    highways.orElse(HighwayParts.DEFAULT),
                    railways.orElse(RailwayParts.DEFAULT))));

    public static final PartSelector DEFAULT = new PartSelector(
            MonorailParts.DEFAULT,
            HighwayParts.DEFAULT,
            RailwayParts.DEFAULT);

    public Optional<PartSelector> get() {
        if (this == DEFAULT) {
            return Optional.empty();
        } else {
            return Optional.of(this);
        }
    }

}
