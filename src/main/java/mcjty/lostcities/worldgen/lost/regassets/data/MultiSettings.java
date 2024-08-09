package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record MultiSettings(int areasize, int minimum, int maximum) {

    public static final Codec<MultiSettings> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("areasize").forGetter(l -> l.areasize),
                    Codec.INT.fieldOf("minimum").forGetter(l -> l.minimum),
                    Codec.INT.fieldOf("maximum").forGetter(l -> l.maximum)
            ).apply(instance, MultiSettings::new));

    public static final MultiSettings DEFAULT = new MultiSettings(10, 1, 5);

    public Optional<MultiSettings> get() {
        if (this == DEFAULT) {
            return Optional.empty();
        } else {
            return Optional.of(this);
        }
    }

}
