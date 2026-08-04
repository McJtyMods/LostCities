package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * For a city style this object represents settings for streets
 */
public class StreetSettings {
    private final Float fountainChance;
    private final Float frontChance;
    private final Integer streetWidth;
    private final Character streetBlock;
    private final Character streetBaseBlock;
    private final Character streetVariantBlock;
    private final Character borderBlock;
    private final Character wallBlock;
    private final StreetParts parts;
    private final StreetParts largeParts;
    private final StreetParts tertiaryParts;

    public static final Codec<StreetSettings> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.optionalFieldOf("fountainchance").forGetter(l -> Optional.ofNullable(l.fountainChance)),
                    Codec.FLOAT.optionalFieldOf("frontchance").forGetter(l -> Optional.ofNullable(l.frontChance)),
                    Codec.INT.optionalFieldOf("width").forGetter(l -> Optional.ofNullable(l.streetWidth)),
                    Codec.STRING.optionalFieldOf("street").forGetter(l -> DataTools.toNullable(l.streetBlock)),
                    Codec.STRING.optionalFieldOf("streetbase").forGetter(l -> DataTools.toNullable(l.streetBaseBlock)),
                    Codec.STRING.optionalFieldOf("streetvariant").forGetter(l -> DataTools.toNullable(l.streetVariantBlock)),
                    Codec.STRING.optionalFieldOf("border").forGetter(l -> DataTools.toNullable(l.borderBlock)),
                    Codec.STRING.optionalFieldOf("wall").forGetter(l -> DataTools.toNullable(l.wallBlock)),
                    StreetParts.CODEC.optionalFieldOf("parts").forGetter(l -> l.parts.get()),
                    StreetParts.CODEC.optionalFieldOf("largeparts").forGetter(l -> l.largeParts.get()),
                    StreetParts.CODEC.optionalFieldOf("tertiaryparts").forGetter(l -> l.tertiaryParts.get())
            ).apply(instance, StreetSettings::new));

    public Float getFountainChance() {
        return fountainChance;
    }

    public Float getFrontChance() {
        return frontChance;
    }

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

    public StreetParts getParts() {
        return parts;
    }

    public StreetParts getLargeParts() {
        return largeParts;
    }

    public StreetParts getTertiaryParts() {
        return tertiaryParts;
    }

    public StreetSettings(Optional<Float> fountainChance,
                          Optional<Float> frontChance,
                          Optional<Integer> streetWidth,
                          Optional<String> streetBlock,
                          Optional<String> streetBaseBlock,
                          Optional<String> streetVariantBlock,
                          Optional<String> borderBlock,
                          Optional<String> wallBlock,
                          Optional<StreetParts> parts,
                          Optional<StreetParts> largeParts,
                          Optional<StreetParts> tertiaryParts) {
        this.fountainChance = fountainChance.orElse(null);
        this.frontChance = frontChance.orElse(null);
        this.streetWidth = streetWidth.orElse(null);
        this.streetBlock = DataTools.getNullableChar(streetBlock);
        this.streetBaseBlock = DataTools.getNullableChar(streetBaseBlock);
        this.streetVariantBlock = DataTools.getNullableChar(streetVariantBlock);
        this.borderBlock = DataTools.getNullableChar(borderBlock);
        this.wallBlock = DataTools.getNullableChar(wallBlock);
        this.parts = parts.orElse(StreetParts.DEFAULT);
        this.largeParts = largeParts.orElse(StreetParts.DEFAULT);
        this.tertiaryParts = tertiaryParts.orElse(StreetParts.DEFAULT);
    }
}
