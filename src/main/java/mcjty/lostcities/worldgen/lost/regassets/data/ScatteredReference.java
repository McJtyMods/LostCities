package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ScatteredReference {

    public static final Codec<ScatteredReference> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("name").forGetter(l -> l.name),
                    Codec.INT.fieldOf("weight").forGetter(l -> l.weight),
                    Codec.BOOL.optionalFieldOf("nearhighway").forGetter(l -> Optional.ofNullable(l.nearhighway)),
                    Codec.BOOL.optionalFieldOf("allowvoid").forGetter(l -> Optional.ofNullable(l.allowvoid)),
                    BiomeMatcher.CODEC.optionalFieldOf("biomes").forGetter(l -> Optional.ofNullable(l.biomeMatcher)),
                    Codec.INT.optionalFieldOf("maxheightdiff").forGetter(l -> Optional.ofNullable(l.maxheightdiff))
            ).apply(instance, ScatteredReference::new));

    private final String name;
    private final int weight;
    private final BiomeMatcher biomeMatcher;
    private final Integer maxheightdiff;
    private final Boolean allowvoid;
    private final Boolean nearhighway;

    public ScatteredReference(String name, int weight, Optional<Boolean> nearhighway, Optional<Boolean> allowvoid, Optional<BiomeMatcher> biomeMatcher, Optional<Integer> maxheightdiff) {
        this.name = name;
        this.weight = weight;
        this.nearhighway = nearhighway.orElse(null);
        this.allowvoid = allowvoid.orElse(null);
        this.biomeMatcher = biomeMatcher.orElse(null);
        this.maxheightdiff = maxheightdiff.orElse(null);
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    @Nullable
    public BiomeMatcher getBiomeMatcher() {
        return biomeMatcher;
    }

    public Integer getMaxheightdiff() {
        return maxheightdiff;
    }

    public boolean isAllowVoid() {
        return allowvoid != null && allowvoid;
    }

    public boolean isNearHighway() {
        return nearhighway != null && nearhighway;
    }
}
