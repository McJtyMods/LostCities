package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ScatteredReference {

    public static final Codec<ScatteredReference> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("name").forGetter(l -> l.name),
                    Codec.FLOAT.fieldOf("chance").forGetter(l -> l.chance),
                    BiomeMatcher.CODEC.optionalFieldOf("biomes").forGetter(l -> Optional.ofNullable(l.biomeMatcher))
            ).apply(instance, ScatteredReference::new));

    private final String name;
    private final float chance;
    private final BiomeMatcher biomeMatcher;

    public ScatteredReference(String name, float chance, Optional<BiomeMatcher> biomeMatcher) {
        this.name = name;
        this.chance = chance;
        this.biomeMatcher = biomeMatcher.orElse(null);
    }

    public String getName() {
        return name;
    }

    public float getChance() {
        return chance;
    }

    @Nullable
    public BiomeMatcher getBiomeMatcher() {
        return biomeMatcher;
    }
}
