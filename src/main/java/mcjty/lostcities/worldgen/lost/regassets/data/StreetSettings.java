package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * For a city style this object represents settings for streets
 */
public class StreetSettings {
    private final Integer streetWidth;
    private final Character streetBlock;
    private final Character streetBaseBlock;
    private final Character streetVariantBlock;
    private final Character borderBlock;
    private final Character wallBlock;

    public static final Codec<StreetSettings> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.optionalFieldOf("width").forGetter(l -> Optional.ofNullable(l.streetWidth)),
                    Codec.STRING.optionalFieldOf("street").forGetter(l -> DataTools.toNullable(l.streetBlock)),
                    Codec.STRING.optionalFieldOf("streetbase").forGetter(l -> DataTools.toNullable(l.streetBaseBlock)),
                    Codec.STRING.optionalFieldOf("streetvariant").forGetter(l -> DataTools.toNullable(l.streetVariantBlock)),
                    Codec.STRING.optionalFieldOf("border").forGetter(l -> DataTools.toNullable(l.borderBlock)),
                    Codec.STRING.optionalFieldOf("wall").forGetter(l -> DataTools.toNullable(l.wallBlock))
            ).apply(instance, StreetSettings::new));

    public Integer getStreetWidth() {
        return streetWidth;
    }

    public Character getStreetBlock() {
        return streetBlock;
    }

    public Character getStreetBaseBlock() {
        return streetBaseBlock;
    }

    public Character getStreetVariantBlock() {
        return streetVariantBlock;
    }

    public Character getBorderBlock() {
        return borderBlock;
    }

    public Character getWallBlock() {
        return wallBlock;
    }

    public StreetSettings(Optional<Integer> streetWidth,
                          Optional<String> streetBlock,
                          Optional<String> streetBaseBlock,
                          Optional<String> streetVariantBlock,
                          Optional<String> borderBlock,
                          Optional<String> wallBlock) {
        this.streetWidth = streetWidth.isPresent() ? streetWidth.get() : null;
        this.streetBlock = DataTools.getNullableChar(streetBlock);
        this.streetBaseBlock = DataTools.getNullableChar(streetBaseBlock);
        this.streetVariantBlock = DataTools.getNullableChar(streetVariantBlock);
        this.borderBlock = DataTools.getNullableChar(borderBlock);
        this.wallBlock = DataTools.getNullableChar(wallBlock);
    }
}
