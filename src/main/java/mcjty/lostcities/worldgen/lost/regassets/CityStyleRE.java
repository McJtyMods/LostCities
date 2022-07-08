package mcjty.lostcities.worldgen.lost.regassets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.worldgen.lost.regassets.data.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CityStyleRE implements IForgeRegistryEntry<CityStyleRE> {

    public static final Codec<CityStyleRE> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.optionalFieldOf("explosionchance").forGetter(l -> Optional.ofNullable(l.explosionChance)),
                    Codec.STRING.optionalFieldOf("style").forGetter(l -> Optional.ofNullable(l.style)),
                    Codec.STRING.optionalFieldOf("inherit").forGetter(l -> Optional.ofNullable(l.inherit)),
                    GeneralSettings.CODEC.optionalFieldOf("generalblocks").forGetter(l -> Optional.ofNullable(l.generalSettings)),
                    BuildingSettings.CODEC.optionalFieldOf("buildingsettings").forGetter(l -> Optional.ofNullable(l.buildingSettings)),
                    CorridorSettings.CODEC.optionalFieldOf("corridorblocks").forGetter(l -> Optional.ofNullable(l.corridorSettings)),
                    ParkSettings.CODEC.optionalFieldOf("parkblocks").forGetter(l -> Optional.ofNullable(l.parkSettings)),
                    RailSettings.CODEC.optionalFieldOf("railblocks").forGetter(l -> Optional.ofNullable(l.railSettings)),
                    SphereSettings.CODEC.optionalFieldOf("sphereblocks").forGetter(l -> Optional.ofNullable(l.sphereSettings)),
                    StreetSettings.CODEC.optionalFieldOf("streetblocks").forGetter(l -> Optional.ofNullable(l.streetSettings)),
                    Selectors.CODEC.optionalFieldOf("selectors").forGetter(l -> Optional.ofNullable(l.selectors))
            ).apply(instance, CityStyleRE::new));

    private ResourceLocation name;

    private Float explosionChance;
    private String style;
    private String inherit;

    private GeneralSettings generalSettings;
    private BuildingSettings buildingSettings;
    private CorridorSettings corridorSettings;
    private ParkSettings parkSettings;
    private RailSettings railSettings;
    private SphereSettings sphereSettings;
    private StreetSettings streetSettings;

    private Selectors selectors;

    public CityStyleRE(
            Optional<Float> explosionChance,
            Optional<String> style,
            Optional<String> inherit,
            Optional<GeneralSettings> generalSettings,
            Optional<BuildingSettings> buildingSettings,
            Optional<CorridorSettings> corridorSettings,
            Optional<ParkSettings> parkSettings,
            Optional<RailSettings> railSettings,
            Optional<SphereSettings> sphereSettings,
            Optional<StreetSettings> streetSettings,
            Optional<Selectors> selectors) {
        this.explosionChance = explosionChance.orElse(null);
        this.style = style.orElse(null);
        this.inherit = inherit.orElse(null);
        this.generalSettings = generalSettings.orElse(null);
        this.buildingSettings = buildingSettings.orElse(null);
        this.corridorSettings = corridorSettings.orElse(null);
        this.parkSettings = parkSettings.orElse(null);
        this.railSettings = railSettings.orElse(null);
        this.sphereSettings = sphereSettings.orElse(null);
        this.streetSettings = streetSettings.orElse(null);
        this.selectors = selectors.orElse(null);
    }

    public Float getExplosionChance() {
        return explosionChance;
    }

    public String getStyle() {
        return style;
    }

    public String getInherit() {
        return inherit;
    }

    public Optional<GeneralSettings> getGeneralSettings() {
        return Optional.ofNullable(generalSettings);
    }

    public Optional<BuildingSettings> getBuildingSettings() {
        return Optional.ofNullable(buildingSettings);
    }

    public Optional<CorridorSettings> getCorridorSettings() {
        return Optional.ofNullable(corridorSettings);
    }

    public Optional<ParkSettings> getParkSettings() {
        return Optional.ofNullable(parkSettings);
    }

    public Optional<RailSettings> getRailSettings() {
        return Optional.ofNullable(railSettings);
    }

    public Optional<SphereSettings> getSphereSettings() {
        return Optional.ofNullable(sphereSettings);
    }

    public Optional<StreetSettings> getStreetSettings() {
        return Optional.ofNullable(streetSettings);
    }

    public Optional<Selectors> getSelectors() {
        return Optional.ofNullable(selectors);
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
