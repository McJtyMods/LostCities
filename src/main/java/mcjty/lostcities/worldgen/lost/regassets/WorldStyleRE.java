package mcjty.lostcities.worldgen.lost.regassets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.worldgen.lost.regassets.data.CityStyleSelector;
import mcjty.lostcities.worldgen.lost.regassets.data.ConditionPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WorldStyleRE implements IForgeRegistryEntry<WorldStyleRE> {

    public static final Codec<WorldStyleRE> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("outsidestyle").forGetter(l -> l.outsideStyle),
                    Codec.list(CityStyleSelector.CODEC).fieldOf("citystyles").forGetter(l -> l.cityStyleSelectors)
            ).apply(instance, WorldStyleRE::new));

    private ResourceLocation name;
    private String outsideStyle;
    private List<CityStyleSelector> cityStyleSelectors;

    public WorldStyleRE(String outsideStyle, List<CityStyleSelector> values) {
        this.outsideStyle = outsideStyle;
        this.cityStyleSelectors = values;
    }

    public String getOutsideStyle() {
        return outsideStyle;
    }

    public List<CityStyleSelector> getCityStyleSelectors() {
        return cityStyleSelectors;
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
