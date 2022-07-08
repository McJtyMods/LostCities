package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * For a city style this object represents settings in general
 */
public class GeneralSettings {
    private final Character ironbarsBlock;
    private final Character glowstoneBlock;

    public static final Codec<GeneralSettings> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("ironbars").forGetter(l -> DataTools.toNullable(l.ironbarsBlock)),
                    Codec.STRING.optionalFieldOf("glowstone").forGetter(l -> DataTools.toNullable(l.glowstoneBlock))
            ).apply(instance, GeneralSettings::new));

    public Character getIronbarsBlock() {
        return ironbarsBlock;
    }

    public Character getGlowstoneBlock() {
        return glowstoneBlock;
    }

    public GeneralSettings(Optional<String> ironbarsBlock,
                           Optional<String> glowstoneBlock) {
        this.ironbarsBlock = DataTools.getNullableChar(ironbarsBlock);
        this.glowstoneBlock = DataTools.getNullableChar(glowstoneBlock);
    }
}
