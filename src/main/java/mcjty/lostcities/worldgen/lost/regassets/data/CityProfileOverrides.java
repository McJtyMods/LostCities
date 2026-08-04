package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * Optional profile-compatible values owned by a city style. Keep fields
 * nullable: absence means that the selected profile remains authoritative.
 */
public record CityProfileOverrides(Float openLotParkChance) {

    public static final Codec<CityProfileOverrides> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.floatRange(0.0f, 1.0f).optionalFieldOf("openLotParkChance")
                            .forGetter(overrides -> Optional.ofNullable(overrides.openLotParkChance))
            ).apply(instance, optionalOpenLotParkChance ->
                    new CityProfileOverrides(optionalOpenLotParkChance.orElse(null))));
}
