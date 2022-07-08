package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * For a city style this object represents settings for rails
 */
public class RailSettings {
    private Character railMainBlock;

    public static final Codec<RailSettings> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("railmain").forGetter(l -> DataTools.toNullable(l.railMainBlock))
            ).apply(instance, RailSettings::new));

    public Character getRailMainBlock() {
        return railMainBlock;
    }

    public RailSettings(Optional<String> railMainBlock) {
        this.railMainBlock = DataTools.getNullableChar(railMainBlock);
    }
}
