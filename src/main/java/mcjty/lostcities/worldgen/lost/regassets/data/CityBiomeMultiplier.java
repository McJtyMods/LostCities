package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CityBiomeMultiplier(float multiplier, BiomeMatcher biomeMatcher) {

    public static final Codec<CityBiomeMultiplier> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("multiplier").forGetter(CityBiomeMultiplier::multiplier),
                    BiomeMatcher.CODEC.fieldOf("biomes").forGetter(l -> l.biomeMatcher)
            ).apply(instance, CityBiomeMultiplier::new));
}
