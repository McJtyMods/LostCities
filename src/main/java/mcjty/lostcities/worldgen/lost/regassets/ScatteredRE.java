package mcjty.lostcities.worldgen.lost.regassets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ScatteredRE implements IForgeRegistryEntry<ScatteredRE> {

    public static final Codec<ScatteredRE> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(Codec.STRING).fieldOf("buildings").forGetter(l -> l.buildings)
            ).apply(instance, ScatteredRE::new));

    private ResourceLocation name;
    private final List<String> buildings;

    public ScatteredRE(List<String> buildings) {
        this.buildings = buildings;
    }

    public List<String> getBuildings() {
        return buildings;
    }

    @Override
    public ScatteredRE setRegistryName(ResourceLocation name) {
        this.name = name;
        return this;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return name;
    }

    @Override
    public Class<ScatteredRE> getRegistryType() {
        return ScatteredRE.class;
    }

}
