package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * For a city style this object represents settings for city spheres
 */
public class SphereSettings {
    private final Character sphereBlock;          // Used for 'space' landscape type
    private final Character sphereSideBlock;      // Used for 'space' landscape type
    private final Character sphereGlassBlock;     // Used for 'space' landscape type

    public static final Codec<SphereSettings> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("inner").forGetter(l -> DataTools.toNullable(l.sphereBlock)),
                    Codec.STRING.optionalFieldOf("border").forGetter(l -> DataTools.toNullable(l.sphereSideBlock)),
                    Codec.STRING.optionalFieldOf("glass").forGetter(l -> DataTools.toNullable(l.sphereGlassBlock))
            ).apply(instance, SphereSettings::new));

    public Character getSphereBlock() {
        return sphereBlock;
    }

    public Character getSphereSideBlock() {
        return sphereSideBlock;
    }

    public Character getSphereGlassBlock() {
        return sphereGlassBlock;
    }

    public SphereSettings(Optional<String> sphereBlock,
                          Optional<String> sphereSideBlock,
                          Optional<String> sphereGlassBlock) {
        this.sphereBlock = DataTools.getNullableChar(sphereBlock);
        this.sphereSideBlock = DataTools.getNullableChar(sphereSideBlock);
        this.sphereGlassBlock = DataTools.getNullableChar(sphereGlassBlock);
    }
}
