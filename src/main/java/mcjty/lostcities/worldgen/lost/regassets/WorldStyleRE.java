package mcjty.lostcities.worldgen.lost.regassets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.worldgen.lost.regassets.data.*;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public class WorldStyleRE implements IAsset<WorldStyleRE> {

    public static final Codec<WorldStyleRE> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("outsidestyle").forGetter(l -> l.outsideStyle),
                    MultiSettings.CODEC.optionalFieldOf("multisettings").forGetter(l -> l.multiSettings.get()),
                    WorldSettings.CODEC.optionalFieldOf("settings").forGetter(l -> l.worldSettings.get()),
                    CitySphereSettings.CODEC.optionalFieldOf("cityspheres").forGetter(l -> Optional.ofNullable(l.citysphereSettings)),
                    ScatteredSettings.CODEC.optionalFieldOf("scattered").forGetter(l -> Optional.ofNullable(l.scatteredSettings)),
                    PartSelector.CODEC.optionalFieldOf("parts").forGetter(l -> l.partSelector.get()),
                    Codec.STRING.optionalFieldOf("bridgesupport").forGetter(l -> DataTools.toNullable(l.bridgeSupport)),
                    Codec.STRING.optionalFieldOf("highwaysupport").forGetter(l -> DataTools.toNullable(l.highwaySupport)),
                    Codec.STRING.optionalFieldOf("bridgesupportpart").forGetter(l -> Optional.ofNullable(l.bridgeSupportPart)),
                    Codec.STRING.optionalFieldOf("highwaysupportpart").forGetter(l -> Optional.ofNullable(l.highwaySupportPart)),
                    Codec.list(CityStyleSelector.CODEC).fieldOf("citystyles").forGetter(l -> l.cityStyleSelectors),
                    Codec.list(CityBiomeMultiplier.CODEC).optionalFieldOf("citybiomemultipliers").forGetter(l -> Optional.ofNullable(l.cityBiomeMultipliers))
            ).apply(instance, WorldStyleRE::new));

    private Identifier name;
    private final String outsideStyle;
    private final MultiSettings multiSettings;
    private final WorldSettings worldSettings;
    private final ScatteredSettings scatteredSettings;
    private final CitySphereSettings citysphereSettings;
    @Nonnull private final PartSelector partSelector;
    private final Character bridgeSupport;
    private final Character highwaySupport;
    private final String bridgeSupportPart;
    private final String highwaySupportPart;
    private final List<CityStyleSelector> cityStyleSelectors;
    private final List<CityBiomeMultiplier> cityBiomeMultipliers;

    public WorldStyleRE(String outsideStyle,
                        Optional<MultiSettings> multiSettings,
                        Optional<WorldSettings> worldSettings,
                        Optional<CitySphereSettings> citysphereSettings,
                        Optional<ScatteredSettings> scatteredSettings,
                        Optional<PartSelector> partSelector,
                        Optional<String> bridgeSupport,
                        Optional<String> highwaySupport,
                        Optional<String> bridgeSupportPart,
                        Optional<String> highwaySupportPart,
                        List<CityStyleSelector> cityStyleSelector,
                        Optional<List<CityBiomeMultiplier>> cityBiomeMultipliers) {
        this.outsideStyle = outsideStyle;
        this.multiSettings = multiSettings.orElse(MultiSettings.DEFAULT);
        this.worldSettings = worldSettings.orElse(WorldSettings.DEFAULT);
        this.citysphereSettings = citysphereSettings.orElse(null);
        this.scatteredSettings = scatteredSettings.orElse(null);
        this.partSelector = partSelector.orElse(PartSelector.DEFAULT);
        this.bridgeSupport = DataTools.getNullableChar(bridgeSupport);
        this.highwaySupport = DataTools.getNullableChar(highwaySupport);
        this.bridgeSupportPart = bridgeSupportPart.map(String::intern).orElse(null);
        this.highwaySupportPart = highwaySupportPart.map(String::intern).orElse(null);
        this.cityStyleSelectors = cityStyleSelector;
        this.cityBiomeMultipliers = cityBiomeMultipliers.orElse(null);
    }

    public String getOutsideStyle() {
        return outsideStyle;
    }

    @Nonnull
    public PartSelector getPartSelector() {
        return partSelector;
    }

    @Nullable
    public Character getBridgeSupport() {
        return bridgeSupport;
    }

    @Nullable
    public Character getHighwaySupport() {
        return highwaySupport;
    }

    @Nullable
    public String getBridgeSupportPart() {
        return bridgeSupportPart;
    }

    @Nullable
    public String getHighwaySupportPart() {
        return highwaySupportPart;
    }

    public CitySphereSettings getCitysphereSettings() {
        return citysphereSettings;
    }

    @Nullable
    public ScatteredSettings getScatteredSettings() {
        return scatteredSettings;
    }

    public List<CityStyleSelector> getCityStyleSelectors() {
        return cityStyleSelectors;
    }

    public List<CityBiomeMultiplier> getCityBiomeMultipliers() {
        return cityBiomeMultipliers;
    }

    @Nonnull
    public MultiSettings getMultiSettings() {
        return multiSettings;
    }

    @Nonnull
    public WorldSettings getWorldSettings() {
        return worldSettings;
    }

    @Override
    public WorldStyleRE setRegistryName(Identifier name) {
        this.name = name;
        return this;
    }

    @Nullable
    public Identifier getRegistryName() {
        return name;
    }
}
