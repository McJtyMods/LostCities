package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Represents an object with a factor indicating how likely this object is relative to others in the same list
 */
public record ObjectSelector(float factor, String value) {

    public static final Codec<ObjectSelector> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("factor").forGetter(ObjectSelector::factor),
                    Codec.STRING.fieldOf("value").forGetter(ObjectSelector::value)
            ).apply(instance, ObjectSelector::new));
}
