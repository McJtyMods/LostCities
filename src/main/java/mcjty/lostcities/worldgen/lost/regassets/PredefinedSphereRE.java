package mcjty.lostcities.worldgen.lost.regassets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

public class PredefinedSphereRE implements IForgeRegistryEntry<PredefinedSphereRE> {

    public static final Codec<PredefinedSphereRE> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("dimension").forGetter(l -> l.dimension),
                    Codec.INT.fieldOf("chunkx").forGetter(l -> l.chunkX),
                    Codec.INT.fieldOf("chunkz").forGetter(l -> l.chunkZ),
                    Codec.INT.fieldOf("centerx").forGetter(l -> l.centerX),
                    Codec.INT.fieldOf("centerz").forGetter(l -> l.centerZ),
                    Codec.INT.fieldOf("radius").forGetter(l -> l.radius),
                    Codec.STRING.fieldOf("biome").forGetter(l -> l.biome)
            ).apply(instance, PredefinedSphereRE::new));

    private ResourceLocation name;

    private final String dimension;
    private final int chunkX;
    private final int chunkZ;
    private final int centerX;
    private final int centerZ;
    private final int radius;
    private String biome;

    public PredefinedSphereRE(
            String dimension,
            int chunkX, int chunkZ, int centerX, int centerZ, int radius,
            String biome) {
        this.dimension = dimension;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.radius = radius;
        this.biome = biome;
    }

    public String getDimension() {
        return dimension;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterZ() {
        return centerZ;
    }

    public int getRadius() {
        return radius;
    }

    public String getBiome() {
        return biome;
    }

    @Override
    public PredefinedSphereRE setRegistryName(ResourceLocation name) {
        this.name = name;
        return this;
    }

    @Override
    public Class<PredefinedSphereRE> getRegistryType() {
        return PredefinedSphereRE.class;
    }

    @Nullable
    public ResourceLocation getRegistryName() {
        return name;
    }
}
