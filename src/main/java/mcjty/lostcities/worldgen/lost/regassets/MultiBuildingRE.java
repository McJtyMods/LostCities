package mcjty.lostcities.worldgen.lost.regassets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.worldgen.lost.regassets.data.ConditionPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MultiBuildingRE implements IForgeRegistryEntry<MultiBuildingRE> {

    public static final Codec<MultiBuildingRE> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("dimx").forGetter(l -> l.dimX),
                    Codec.INT.fieldOf("dimz").forGetter(l -> l.dimZ),
                    Codec.list(Codec.list(Codec.STRING)).fieldOf("buildings").forGetter(l -> l.buildings)
            ).apply(instance, MultiBuildingRE::new));

    private ResourceLocation name;
    private final int dimX;
    private final int dimZ;
    private final List<List<String>> buildings;

    public MultiBuildingRE(int dimX, int dimZ, List<List<String>> buildings) {
        this.dimX = dimX;
        this.dimZ = dimZ;
        this.buildings = buildings;
    }

    public int getDimX() {
        return dimX;
    }

    public int getDimZ() {
        return dimZ;
    }

    public List<List<String>> getBuildings() {
        return buildings;
    }

    @Override
    public MultiBuildingRE setRegistryName(ResourceLocation name) {
        this.name = name;
        return this;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return name;
    }

    @Override
    public Class<MultiBuildingRE> getRegistryType() {
        return MultiBuildingRE.class;
    }

}
