package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * A block together with a weight (relative to 128) for a palette
 */
public record BlockEntry(int random, String block) {

    public static final Codec<BlockEntry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("random").forGetter(BlockEntry::random),
                    Codec.STRING.fieldOf("block").forGetter(BlockEntry::block)
            ).apply(instance, BlockEntry::new));
}
