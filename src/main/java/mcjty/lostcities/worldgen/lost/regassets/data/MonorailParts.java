package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record MonorailParts(String both, String vertical, String station) {

    public static final Codec<MonorailParts> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("both", "monorails_both").forGetter(MonorailParts::both),
                    Codec.STRING.optionalFieldOf("vertical", "monorails_vertical").forGetter(MonorailParts::vertical),
                    Codec.STRING.optionalFieldOf("station", "monorails_station").forGetter(MonorailParts::station)
            ).apply(instance, MonorailParts::new));

    public static final MonorailParts DEFAULT = new MonorailParts("monorails_both", "monorails_vertical", "monorails_station");

    public Optional<MonorailParts> get() {
        if (this == DEFAULT) {
            return Optional.empty();
        } else {
            return Optional.of(this);
        }
    }
}
