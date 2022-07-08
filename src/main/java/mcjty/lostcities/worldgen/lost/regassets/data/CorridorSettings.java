package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * For a city style this object represents settings for corridors
 */
public class CorridorSettings {
    private final Character corridorRoofBlock;
    private final Character corridorGlassBlock;

    public static final Codec<CorridorSettings> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("roof").forGetter(l -> DataTools.toNullable(l.corridorRoofBlock)),
                    Codec.STRING.optionalFieldOf("glass").forGetter(l -> DataTools.toNullable(l.corridorGlassBlock))
            ).apply(instance, CorridorSettings::new));

    public Character getCorridorRoofBlock() {
        return corridorRoofBlock;
    }

    public Character getCorridorGlassBlock() {
        return corridorGlassBlock;
    }

    public CorridorSettings(Optional<String> corridorRoofBlock,
                            Optional<String> corridorGlassBlock) {
        this.corridorRoofBlock = DataTools.getNullableChar(corridorRoofBlock);
        this.corridorGlassBlock = DataTools.getNullableChar(corridorGlassBlock);
    }
}
