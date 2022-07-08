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

    public List<ObjectSelector> getBuildingSelector() {
        return buildingSelector;
    }

    public List<ObjectSelector> getBridgeSelector() {
        return bridgeSelector;
    }

    public List<ObjectSelector> getParkSelector() {
        return parkSelector;
    }

    public List<ObjectSelector> getFountainSelector() {
        return fountainSelector;
    }

    public List<ObjectSelector> getStairSelector() {
        return stairSelector;
    }

    public List<ObjectSelector> getFrontSelector() {
        return frontSelector;
    }

    public List<ObjectSelector> getRailDungeonSelector() {
        return railDungeonSelector;
    }

    public List<ObjectSelector> getMultiBuildingSelector() {
        return multiBuildingSelector;
    }

    public Selectors(Optional<List<ObjectSelector>> buildingSelector,
                     Optional<List<ObjectSelector>> bridgeSelector,
                     Optional<List<ObjectSelector>> parkSelector,
                     Optional<List<ObjectSelector>> fountainSelector,
                     Optional<List<ObjectSelector>> stairSelector,
                     Optional<List<ObjectSelector>> frontSelector,
                     Optional<List<ObjectSelector>> railDungeonSelector,
                     Optional<List<ObjectSelector>> multiBuildingSelector) {
        this.buildingSelector = buildingSelector.isPresent() ? buildingSelector.get() : null;
        this.bridgeSelector = bridgeSelector.isPresent() ? bridgeSelector.get() : null;
        this.parkSelector = parkSelector.isPresent() ? parkSelector.get() : null;
        this.fountainSelector = fountainSelector.isPresent() ? fountainSelector.get() : null;
        this.stairSelector = stairSelector.isPresent() ? stairSelector.get() : null;
        this.frontSelector = frontSelector.isPresent() ? frontSelector.get() : null;
        this.railDungeonSelector = railDungeonSelector.isPresent() ? railDungeonSelector.get() : null;
        this.multiBuildingSelector = multiBuildingSelector.isPresent() ? multiBuildingSelector.get() : null;
    }
}
