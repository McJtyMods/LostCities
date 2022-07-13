package mcjty.lostcities.worldgen.lost.regassets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.worldgen.lost.cityassets.Scattered;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ScatteredRE implements IForgeRegistryEntry<ScatteredRE> {

    public static final Codec<ScatteredRE> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(Codec.STRING).optionalFieldOf("buildings").forGetter(l -> Optional.ofNullable(l.buildings)),
                    Codec.STRING.optionalFieldOf("multibuilding").forGetter(l -> Optional.ofNullable(l.multibuilding)),
                    StringRepresentable.fromEnum(Scattered.TerrainHeight::values, Scattered.TerrainHeight::byName).fieldOf("terrainheight").forGetter(l -> l.terrainheight),
                    StringRepresentable.fromEnum(Scattered.TerrainFix::values, Scattered.TerrainFix::byName).fieldOf("terrainfix").forGetter(l -> l.terrainfix),
                    Codec.INT.optionalFieldOf("heightoffset", 0).forGetter(l -> l.heightoffset)
            ).apply(instance, ScatteredRE::new));

    private ResourceLocation name;
    private final Scattered.TerrainHeight terrainheight;
    private final Scattered.TerrainFix terrainfix;
    private final int heightoffset;
    private final List<String> buildings;
    private final String multibuilding;

    public ScatteredRE(Optional<List<String>> buildings, Optional<String> multibuilding, Scattered.TerrainHeight terrainheight, Scattered.TerrainFix terrainfix,
                       int heightoffset) {
        this.buildings = buildings.orElse(null);
        this.multibuilding = multibuilding.orElse(null);
        this.terrainheight = terrainheight;
        this.terrainfix = terrainfix;
        this.heightoffset = heightoffset;
    }

    @Nullable
    public List<String> getBuildings() {
        return buildings;
    }

    @Nullable
    public String getMultibuilding() {
        return multibuilding;
    }

    public Scattered.TerrainHeight getTerrainheight() {
        return terrainheight;
    }

    public Scattered.TerrainFix getTerrainfix() {
        return terrainfix;
    }

    public int getHeightoffset() {
        return heightoffset;
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
