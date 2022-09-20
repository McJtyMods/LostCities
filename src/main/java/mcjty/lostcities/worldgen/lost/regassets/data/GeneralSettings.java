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
    private final Character leavesBlock;
    private final Character rubbleDirtBlock;

    public static final Codec<GeneralSettings> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("ironbars").forGetter(l -> DataTools.toNullable(l.ironbarsBlock)),
                    Codec.STRING.optionalFieldOf("glowstone").forGetter(l -> DataTools.toNullable(l.glowstoneBlock)),
                    Codec.STRING.optionalFieldOf("leaves").forGetter(l -> DataTools.toNullable(l.leavesBlock)),
                    Codec.STRING.optionalFieldOf("rubbledirt").forGetter(l -> DataTools.toNullable(l.rubbleDirtBlock))
            ).apply(instance, GeneralSettings::new));

    public Character getIronbarsBlock() {
        return ironbarsBlock;
    }

    public Character getGlowstoneBlock() {
        return glowstoneBlock;
    }

    public Character getLeavesBlock() {
        return leavesBlock;
    }

    public Character getRubbleDirtBlock() {
        return rubbleDirtBlock;
    }

    public GeneralSettings(Optional<String> ironbarsBlock,
                           Optional<String> glowstoneBlock,
                           Optional<String> leavesBlock,
                           Optional<String> rubbleDirtBlock) {
        this.ironbarsBlock = DataTools.getNullableChar(ironbarsBlock);
        this.glowstoneBlock = DataTools.getNullableChar(glowstoneBlock);
        this.leavesBlock = DataTools.getNullableChar(leavesBlock);
        this.rubbleDirtBlock = DataTools.getNullableChar(rubbleDirtBlock);
    }
}
