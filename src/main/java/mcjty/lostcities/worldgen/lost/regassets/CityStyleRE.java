package mcjty.lostcities.worldgen.lost.regassets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.worldgen.lost.cityassets.CityStyle;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CityStyleRE implements IForgeRegistryEntry<CityStyleRE> {

    private ResourceLocation name;

    public static final Codec<CityStyle.PartSelector> PART_SELECTOR_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("factor").forGetter(CityStyle.PartSelector::getFactor),
                    Codec.STRING.fieldOf("value").forGetter(CityStyle.PartSelector::getValue)
            ).apply(instance, CityStyle.PartSelector::new));

    public static final Codec<Selectors> SELECTORS_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(PART_SELECTOR_CODEC).optionalFieldOf("buildings").forGetter(l -> Optional.ofNullable(l.buildingSelector)),
                    Codec.list(PART_SELECTOR_CODEC).optionalFieldOf("bridges").forGetter(l -> Optional.ofNullable(l.bridgeSelector)),
                    Codec.list(PART_SELECTOR_CODEC).optionalFieldOf("parks").forGetter(l -> Optional.ofNullable(l.parkSelector)),
                    Codec.list(PART_SELECTOR_CODEC).optionalFieldOf("fountains").forGetter(l -> Optional.ofNullable(l.fountainSelector)),
                    Codec.list(PART_SELECTOR_CODEC).optionalFieldOf("stairs").forGetter(l -> Optional.ofNullable(l.stairSelector)),
                    Codec.list(PART_SELECTOR_CODEC).optionalFieldOf("fronts").forGetter(l -> Optional.ofNullable(l.frontSelector)),
                    Codec.list(PART_SELECTOR_CODEC).optionalFieldOf("dungeons").forGetter(l -> Optional.ofNullable(l.railDungeonSelector)),
                    Codec.list(PART_SELECTOR_CODEC).optionalFieldOf("multibuildings").forGetter(l -> Optional.ofNullable(l.multiBuildingSelector))
            ).apply(instance, Selectors::new));

    public static final Codec<BuildingSettings> BUILDING_SETTINGS_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.optionalFieldOf("minfloors").forGetter(l -> Optional.ofNullable(l.minFloorCount)),
                    Codec.INT.optionalFieldOf("mincellars").forGetter(l -> Optional.ofNullable(l.minCellarCount)),
                    Codec.INT.optionalFieldOf("maxfloors").forGetter(l -> Optional.ofNullable(l.maxFloorCount)),
                    Codec.INT.optionalFieldOf("maxcellars").forGetter(l -> Optional.ofNullable(l.maxCellarCount)),
                    Codec.FLOAT.optionalFieldOf("buildingchance").forGetter(l -> Optional.ofNullable(l.buildingChance))
            ).apply(instance, BuildingSettings::new));

    public static class Selectors {
        private List<CityStyle.PartSelector> buildingSelector;
        private List<CityStyle.PartSelector> bridgeSelector;
        private List<CityStyle.PartSelector> parkSelector;
        private List<CityStyle.PartSelector> fountainSelector;
        private List<CityStyle.PartSelector> stairSelector;
        private List<CityStyle.PartSelector> frontSelector;
        private List<CityStyle.PartSelector> railDungeonSelector;
        private List<CityStyle.PartSelector> multiBuildingSelector;

        public List<CityStyle.PartSelector> getBuildingSelector() {
            return buildingSelector;
        }

        public List<CityStyle.PartSelector> getBridgeSelector() {
            return bridgeSelector;
        }

        public List<CityStyle.PartSelector> getParkSelector() {
            return parkSelector;
        }

        public List<CityStyle.PartSelector> getFountainSelector() {
            return fountainSelector;
        }

        public List<CityStyle.PartSelector> getStairSelector() {
            return stairSelector;
        }

        public List<CityStyle.PartSelector> getFrontSelector() {
            return frontSelector;
        }

        public List<CityStyle.PartSelector> getRailDungeonSelector() {
            return railDungeonSelector;
        }

        public List<CityStyle.PartSelector> getMultiBuildingSelector() {
            return multiBuildingSelector;
        }

        public Selectors(Optional<List<CityStyle.PartSelector>> buildingSelector,
                         Optional<List<CityStyle.PartSelector>> bridgeSelector,
                         Optional<List<CityStyle.PartSelector>> parkSelector,
                         Optional<List<CityStyle.PartSelector>> fountainSelector,
                         Optional<List<CityStyle.PartSelector>> stairSelector,
                         Optional<List<CityStyle.PartSelector>> frontSelector,
                         Optional<List<CityStyle.PartSelector>> railDungeonSelector,
                         Optional<List<CityStyle.PartSelector>> multiBuildingSelector) {
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

    public static class BuildingSettings {
        private Integer minFloorCount;
        private Integer minCellarCount;
        private Integer maxFloorCount;
        private Integer maxCellarCount;
        private Float buildingChance;   // Optional build chance override

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

    public static class StreetSettings {
        private Integer streetWidth;
        private Character streetBlock;
        private Character streetBaseBlock;
        private Character streetVariantBlock;
        private Character borderBlock;
        private Character wallBlock;
    }

    public static class ParkSettings {
        private Character parkElevationBlock;
        private Character grassBlock;
    }

    public static class CorridorSettings {
        private Character corridorRoofBlock;
        private Character corridorGlassBlock;
    }

    public static class GeneralSettings {
        private Character ironbarsBlock;
        private Character glowstoneBlock;
    }

    public static class RailSettings {
        private Character railMainBlock;
    }

    public static class SphereSettings {
        private Character sphereBlock;          // Used for 'space' landscape type
        private Character sphereSideBlock;      // Used for 'space' landscape type
        private Character sphereGlassBlock;     // Used for 'space' landscape type
    }

    private Float explosionChance;
    private String style;
    private String inherit;

    public CityStyleRE() {
    }

    @Override
    public CityStyleRE setRegistryName(ResourceLocation name) {
        this.name = name;
        return this;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return name;
    }

    @Override
    public Class<CityStyleRE> getRegistryType() {
        return CityStyleRE.class;
    }
}
