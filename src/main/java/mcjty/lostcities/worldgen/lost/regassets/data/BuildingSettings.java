package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * For a city style this object represents settings for buildings
 */
public class BuildingSettings {
    private final Integer minFloorCount;
    private final Integer minCellarCount;
    private final Integer maxFloorCount;
    private final Integer maxCellarCount;
    private final Float buildingChance;   // Optional build chance override

    public static final Codec<BuildingSettings> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.optionalFieldOf("minfloors").forGetter(l -> Optional.ofNullable(l.minFloorCount)),
                    Codec.INT.optionalFieldOf("mincellars").forGetter(l -> Optional.ofNullable(l.minCellarCount)),
                    Codec.INT.optionalFieldOf("maxfloors").forGetter(l -> Optional.ofNullable(l.maxFloorCount)),
                    Codec.INT.optionalFieldOf("maxcellars").forGetter(l -> Optional.ofNullable(l.maxCellarCount)),
                    Codec.FLOAT.optionalFieldOf("buildingchance").forGetter(l -> Optional.ofNullable(l.buildingChance))
            ).apply(instance, BuildingSettings::new));

    public Integer getMinFloorCount() {
        return minFloorCount;
    }

    public Integer getMinCellarCount() {
        return minCellarCount;
    }

    public Integer getMaxFloorCount() {
        return maxFloorCount;
    }

    public Integer getMaxCellarCount() {
        return maxCellarCount;
    }

    public Float getBuildingChance() {
        return buildingChance;
    }

    public BuildingSettings(Optional<Integer> minFloorCount,
                            Optional<Integer> minCellarCount,
                            Optional<Integer> maxFloorCount,
                            Optional<Integer> maxCellarCount,
                            Optional<Float> buildingChance) {
        this.minFloorCount = minFloorCount.isPresent() ? minFloorCount.get() : null;
        this.minCellarCount = minCellarCount.isPresent() ? minCellarCount.get() : null;
        this.maxFloorCount = maxFloorCount.isPresent() ? maxFloorCount.get() : null;
        this.maxCellarCount = maxCellarCount.isPresent() ? maxCellarCount.get() : null;
        this.buildingChance = buildingChance.isPresent() ? buildingChance.get() : null;
    }
}
