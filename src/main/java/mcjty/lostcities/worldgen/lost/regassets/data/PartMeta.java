package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * Represents metadata that can be associated with a building part
 */
public record PartMeta(String key, Boolean bool, String chr, String str,
                       Integer i, Float f) {

    public static final Codec<PartMeta> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("key").forGetter(l -> l.key),
                    Codec.BOOL.optionalFieldOf("boolean").forGetter(l -> Optional.ofNullable(l.bool)),
                    Codec.STRING.optionalFieldOf("char").forGetter(l -> Optional.ofNullable(l.chr)),
                    Codec.STRING.optionalFieldOf("string").forGetter(l -> Optional.ofNullable(l.str)),
                    Codec.INT.optionalFieldOf("integer").forGetter(l -> Optional.ofNullable(l.i)),
                    Codec.FLOAT.optionalFieldOf("float").forGetter(l -> Optional.ofNullable(l.f))
            ).apply(instance, PartMeta::create));

    public static PartMeta create(String key, Optional<Boolean> bool, Optional<String> chr, Optional<String> str,
                                  Optional<Integer> i, Optional<Float> f) {
        return new PartMeta(key,
                bool.orElse(null),
                chr.orElse(null),
                str.orElse(null),
                i.orElse(null),
                f.orElse(null));
    }
}
