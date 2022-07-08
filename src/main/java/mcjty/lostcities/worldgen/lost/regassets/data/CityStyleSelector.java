package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * A selector for a citystyle (for a worldstyle)
 */
public record CityStyleSelector(float factor, String citystyle, BiomeMatcher biomeMatcher) {

    public static final Codec<CityStyleSelector> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("factor").forGetter(CityStyleSelector::factor),
                    Codec.STRING.fieldOf("citystyle").forGetter(CityStyleSelector::citystyle),
                    BiomeMatcher.CODEC.optionalFieldOf("biomes").forGetter(l -> Optional.ofNullable(l.biomeMatcher))
            ).apply(instance, (factor, citystyle, biomes) -> new CityStyleSelector(factor, citystyle, biomes.orElse(null))));
}
