package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

/**
 * For a city style this object represents the possible objects for all types
 */
public class Selectors {
    private final List<ObjectSelector> buildingSelector;
    private final List<ObjectSelector> bridgeSelector;
    private final List<ObjectSelector> parkSelector;
    private final List<ObjectSelector> fountainSelector;
    private final List<ObjectSelector> stairSelector;
    private final List<ObjectSelector> frontSelector;
    private final List<ObjectSelector> railDungeonSelector;
    private final List<ObjectSelector> multiBuildingSelector;

    public static final Codec<Selectors> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(ObjectSelector.CODEC).optionalFieldOf("buildings").forGetter(l -> Optional.ofNullable(l.buildingSelector)),
                    Codec.list(ObjectSelector.CODEC).optionalFieldOf("bridges").forGetter(l -> Optional.ofNullable(l.bridgeSelector)),
                    Codec.list(ObjectSelector.CODEC).optionalFieldOf("parks").forGetter(l -> Optional.ofNullable(l.parkSelector)),
                    Codec.list(ObjectSelector.CODEC).optionalFieldOf("fountains").forGetter(l -> Optional.ofNullable(l.fountainSelector)),
                    Codec.list(ObjectSelector.CODEC).optionalFieldOf("stairs").forGetter(l -> Optional.ofNullable(l.stairSelector)),
                    Codec.list(ObjectSelector.CODEC).optionalFieldOf("fronts").forGetter(l -> Optional.ofNullable(l.frontSelector)),
                    Codec.list(ObjectSelector.CODEC).optionalFieldOf("raildungeons").forGetter(l -> Optional.ofNullable(l.railDungeonSelector)),
                    Codec.list(ObjectSelector.CODEC).optionalFieldOf("multibuildings").forGetter(l -> Optional.ofNullable(l.multiBuildingSelector))
            ).apply(instance, Selectors::new));

    public Optional<List<ObjectSelector>> getBuildingSelector() {
        return Optional.ofNullable(buildingSelector);
    }

    public Optional<List<ObjectSelector>> getBridgeSelector() { return Optional.ofNullable(bridgeSelector); }

    public Optional<List<ObjectSelector>> getParkSelector() {
        return Optional.ofNullable(parkSelector);
    }

    public Optional<List<ObjectSelector>> getFountainSelector() {
        return Optional.ofNullable(fountainSelector);
    }

    public Optional<List<ObjectSelector>> getStairSelector() {
        return Optional.ofNullable(stairSelector);
    }

    public Optional<List<ObjectSelector>> getFrontSelector() {
        return Optional.ofNullable(frontSelector);
    }

    public Optional<List<ObjectSelector>> getRailDungeonSelector() {
        return Optional.ofNullable(railDungeonSelector);
    }

    public Optional<List<ObjectSelector>> getMultiBuildingSelector() {
        return Optional.ofNullable(multiBuildingSelector);
    }

    public Selectors(Optional<List<ObjectSelector>> buildingSelector,
                     Optional<List<ObjectSelector>> bridgeSelector,
                     Optional<List<ObjectSelector>> parkSelector,
                     Optional<List<ObjectSelector>> fountainSelector,
                     Optional<List<ObjectSelector>> stairSelector,
                     Optional<List<ObjectSelector>> frontSelector,
                     Optional<List<ObjectSelector>> railDungeonSelector,
                     Optional<List<ObjectSelector>> multiBuildingSelector) {
        this.buildingSelector = buildingSelector.orElse(null);
        this.bridgeSelector = bridgeSelector.orElse(null);
        this.parkSelector = parkSelector.orElse(null);
        this.fountainSelector = fountainSelector.orElse(null);
        this.stairSelector = stairSelector.orElse(null);
        this.frontSelector = frontSelector.orElse(null);
        this.railDungeonSelector = railDungeonSelector.orElse(null);
        this.multiBuildingSelector = multiBuildingSelector.orElse(null);
    }
}
