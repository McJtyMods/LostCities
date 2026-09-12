package mcjty.lostcities.worldgen.lost.regassets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.worldgen.lost.regassets.data.*;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class CityStyleRE implements IAsset<CityStyleRE> {

    public static final Codec<CityStyleRE> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.optionalFieldOf("explosionchance").forGetter(l -> Optional.ofNullable(l.explosionChance)),
                    Codec.STRING.optionalFieldOf("style").forGetter(l -> Optional.ofNullable(l.style)),
                    Codec.STRING.optionalFieldOf("inherit").forGetter(l -> Optional.ofNullable(l.inherit)),
                    Codec.STRING.listOf().optionalFieldOf("stuff_tags").forGetter(l -> Optional.ofNullable(l.stuffTags)),
                    Codec.STRING.optionalFieldOf("bridgesupport").forGetter(l -> DataTools.toNullable(l.bridgeSupport)),
                    Codec.STRING.optionalFieldOf("bridgesupportpart").forGetter(l -> Optional.ofNullable(l.bridgeSupportPart)),
                    CityProfileOverrides.CODEC.optionalFieldOf("profile_overrides").forGetter(l -> Optional.ofNullable(l.profileOverrides)),
                    GeneralSettings.CODEC.optionalFieldOf("generalblocks").forGetter(l -> Optional.ofNullable(l.generalSettings)),
                    BuildingSettings.CODEC.optionalFieldOf("buildingsettings").forGetter(l -> Optional.ofNullable(l.buildingSettings)),
                    CorridorSettings.CODEC.optionalFieldOf("corridorblocks").forGetter(l -> Optional.ofNullable(l.corridorSettings)),
                    ParkSettings.CODEC.optionalFieldOf("parkblocks").forGetter(l -> Optional.ofNullable(l.parkSettings)),
                    RailSettings.CODEC.optionalFieldOf("railblocks").forGetter(l -> Optional.ofNullable(l.railSettings)),
                    SphereSettings.CODEC.optionalFieldOf("sphereblocks").forGetter(l -> Optional.ofNullable(l.sphereSettings)),
                    StreetSettings.CODEC.optionalFieldOf("streetblocks").forGetter(l -> Optional.ofNullable(l.streetSettings)),
                    Selectors.CODEC.optionalFieldOf("selectors").forGetter(l -> Optional.ofNullable(l.selectors))
            ).apply(instance, CityStyleRE::new));

    private Identifier name;

    private final Float explosionChance;
    private final String style;
    private final String inherit;

    private final List<String> stuffTags;
    private final Character bridgeSupport;
    private final String bridgeSupportPart;
    private final CityProfileOverrides profileOverrides;

    private final GeneralSettings generalSettings;
    private final BuildingSettings buildingSettings;
    private final CorridorSettings corridorSettings;
    private final ParkSettings parkSettings;
    private final RailSettings railSettings;
    private final SphereSettings sphereSettings;
    private final StreetSettings streetSettings;

    private final Selectors selectors;

    public CityStyleRE(
            Optional<Float> explosionChance,
            Optional<String> style,
            Optional<String> inherit,
            Optional<List<String>> stuffTags,
            Optional<String> bridgeSupport,
            Optional<String> bridgeSupportPart,
            Optional<CityProfileOverrides> profileOverrides,
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
        this.stuffTags = stuffTags.orElse(null);
        this.bridgeSupport = DataTools.getNullableChar(bridgeSupport);
        this.bridgeSupportPart = bridgeSupportPart.map(String::intern).orElse(null);
        this.profileOverrides = profileOverrides.orElse(null);
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

    @Nullable
    public List<String> getStuffTags() {
        return stuffTags;
    }

    @Nullable
    public Character getBridgeSupport() {
        return bridgeSupport;
    }

    @Nullable
    public String getBridgeSupportPart() {
        return bridgeSupportPart;
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

    public Optional<CityProfileOverrides> getProfileOverrides() {
        return Optional.ofNullable(profileOverrides);
    }

    public Optional<BuildingSettings> getBuildingSettings() {
        return Optional.ofNullable(buildingSettings);
    }

    public Optional<CorridorSettings> getCorridorSettings() {
        return Optional.ofNullable(corridorSettings);
    }

    public Optional<ParkSettings> getParkSettings() { return Optional.ofNullable(parkSettings); }

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
    public CityStyleRE setRegistryName(Identifier name) {
        this.name = name;
        return this;
    }

    @Nullable
    public Identifier getRegistryName() {
        return name;
    }
}
