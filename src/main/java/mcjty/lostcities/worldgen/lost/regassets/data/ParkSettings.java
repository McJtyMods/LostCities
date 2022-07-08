package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * For a city style this object represents settings for parks
 */
public class ParkSettings {
    private final Character parkElevationBlock;
    private final Character grassBlock;

    public static final Codec<ParkSettings> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("elevation").forGetter(l -> DataTools.toNullable(l.parkElevationBlock)),
                    Codec.STRING.optionalFieldOf("grass").forGetter(l -> DataTools.toNullable(l.grassBlock))
            ).apply(instance, ParkSettings::new));


    public ParkSettings(Optional<String> parkElevationBlock,
                        Optional<String> grassBlock) {
        this.parkElevationBlock = DataTools.getNullableChar(parkElevationBlock);
        this.grassBlock = DataTools.getNullableChar(grassBlock);
    }
}
