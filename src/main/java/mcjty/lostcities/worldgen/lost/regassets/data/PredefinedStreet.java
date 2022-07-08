package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record PredefinedStreet(int relChunkX, int relChunkZ) {

    public static final Codec<PredefinedStreet> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("chunkx").forGetter(PredefinedStreet::relChunkX),
                    Codec.INT.fieldOf("chunkz").forGetter(PredefinedStreet::relChunkZ)
            ).apply(instance, PredefinedStreet::new));

}
