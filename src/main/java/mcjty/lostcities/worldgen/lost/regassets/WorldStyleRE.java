package mcjty.lostcities.worldgen.lost.regassets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.worldgen.lost.regassets.data.CityBiomeMultiplier;
import mcjty.lostcities.worldgen.lost.regassets.data.CityStyleSelector;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class WorldStyleRE implements IForgeRegistryEntry<WorldStyleRE> {

    public static final Codec<WorldStyleRE> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("outsidestyle").forGetter(l -> l.outsideStyle),
                    Codec.list(CityStyleSelector.CODEC).fieldOf("citystyles").forGetter(l -> l.cityStyleSelectors),
                    Codec.list(CityBiomeMultiplier.CODEC).optionalFieldOf("citybiomemultipliers").forGetter(l -> Optional.ofNullable(l.cityBiomeMultipliers))
            ).apply(instance, WorldStyleRE::new));

    private ResourceLocation name;
    private final String outsideStyle;
    private final List<CityStyleSelector> cityStyleSelectors;
    private final List<CityBiomeMultiplier> cityBiomeMultipliers;

    public WorldStyleRE(String outsideStyle, List<CityStyleSelector> values, Optional<List<CityBiomeMultiplier>> cityBiomeMultipliers) {
        this.outsideStyle = outsideStyle;
        this.cityStyleSelectors = values;
        this.cityBiomeMultipliers = cityBiomeMultipliers.orElse(null);
    }

    public String getOutsideStyle() {
        return outsideStyle;
    }

    public List<CityStyleSelector> getCityStyleSelectors() {
        return cityStyleSelectors;
    }

    public List<CityBiomeMultiplier> getCityBiomeMultipliers() {
        return cityBiomeMultipliers;
    }

    @Override
    public WorldStyleRE setRegistryName(ResourceLocation name) {
        this.name = name;
        return this;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return name;
    }

    @Override
    public Class<WorldStyleRE> getRegistryType() {
        return WorldStyleRE.class;
    }

}
