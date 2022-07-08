package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record PredefinedBuilding(String building, int relChunkX, int relChunkZ, boolean multi,
                                 boolean preventRuins) {

    public static final Codec<PredefinedBuilding> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("building").forGetter(PredefinedBuilding::building),
                    Codec.INT.fieldOf("chunkx").forGetter(PredefinedBuilding::relChunkX),
                    Codec.INT.fieldOf("chunkz").forGetter(PredefinedBuilding::relChunkZ),
                    Codec.BOOL.optionalFieldOf("multi", false).forGetter(PredefinedBuilding::multi),
                    Codec.BOOL.optionalFieldOf("preventruins", false).forGetter(PredefinedBuilding::preventRuins)
            ).apply(instance, PredefinedBuilding::new));

}
