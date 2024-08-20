package mcjty.lostcities.worldgen.lost.regassets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.worldgen.lost.cityassets.ScatteredBuilding;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ScatteredRE implements IAsset<ScatteredRE> {

    public static final Codec<ScatteredRE> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(Codec.STRING).optionalFieldOf("buildings").forGetter(l -> Optional.ofNullable(l.buildings)),
                    Codec.STRING.optionalFieldOf("multibuilding").forGetter(l -> Optional.ofNullable(l.multibuilding)),
                    Codec.BOOL.optionalFieldOf("rotatable").forGetter(l -> Optional.ofNullable(l.rotatable)),
                    StringRepresentable.fromEnum(ScatteredBuilding.TerrainHeight::values).fieldOf("terrainheight").forGetter(l -> l.terrainheight),
                    StringRepresentable.fromEnum(ScatteredBuilding.TerrainFix::values).fieldOf("terrainfix").forGetter(l -> l.terrainfix),
                    Codec.INT.optionalFieldOf("heightoffset", 0).forGetter(l -> l.heightoffset)
            ).apply(instance, ScatteredRE::new));

    private ResourceLocation name;
    private final ScatteredBuilding.TerrainHeight terrainheight;
    private final ScatteredBuilding.TerrainFix terrainfix;
    private final int heightoffset;
    private final boolean rotatable;
    private final List<String> buildings;
    private final String multibuilding;

    public ScatteredRE(Optional<List<String>> buildings, Optional<String> multibuilding,
                       Optional<Boolean> rotatable,
                       ScatteredBuilding.TerrainHeight terrainheight, ScatteredBuilding.TerrainFix terrainfix,
                       int heightoffset) {
        this.buildings = buildings.orElse(null);
        this.multibuilding = multibuilding.orElse(null);
        this.rotatable = rotatable.orElse(false);
        this.terrainheight = terrainheight;
        this.terrainfix = terrainfix;
        this.heightoffset = heightoffset;
    }

    public boolean isRotatable() {
        return rotatable;
    }

    @Nullable
    public List<String> getBuildings() {
        return buildings;
    }

    @Nullable
    public String getMultibuilding() {
        return multibuilding;
    }

    public ScatteredBuilding.TerrainHeight getTerrainheight() {
        return terrainheight;
    }

    public ScatteredBuilding.TerrainFix getTerrainfix() {
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
    public ResourceLocation getRegistryName() {
        return name;
    }
}
