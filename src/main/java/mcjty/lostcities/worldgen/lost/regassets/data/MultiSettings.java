package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record MultiSettings(int areasize, int minimum, int maximum, float correctStyleFactor, int attempts) {

    public static final Codec<MultiSettings> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("areasize").forGetter(l -> l.areasize),
                    Codec.INT.fieldOf("minimum").forGetter(l -> l.minimum),
                    Codec.INT.fieldOf("maximum").forGetter(l -> l.maximum),
                    Codec.FLOAT.optionalFieldOf("correctstylefactor", 0.8f).forGetter(l -> l.correctStyleFactor),
                    Codec.INT.optionalFieldOf("attempts", 50).forGetter(l -> l.attempts)
            ).apply(instance, MultiSettings::new));

    public static final MultiSettings DEFAULT = new MultiSettings(10, 1, 5, 0.8f, 50);

    public Optional<MultiSettings> get() {
        if (this == DEFAULT) {
            return Optional.empty();
        } else {
            return Optional.of(this);
        }
    }

}
