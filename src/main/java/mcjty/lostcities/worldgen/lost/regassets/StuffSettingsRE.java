package mcjty.lostcities.worldgen.lost.regassets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.worldgen.lost.regassets.data.BiomeMatcher;
import mcjty.lostcities.worldgen.lost.regassets.data.BlockMatcher;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class StuffSettingsRE implements IAsset<StuffSettingsRE> {

    public static final Codec<StuffSettingsRE> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("blocks").forGetter(l -> l.blocks),
                    Codec.INT.fieldOf("minheight").forGetter(l -> l.minheight),
                    Codec.INT.fieldOf("maxheight").forGetter(l -> l.maxheight),
                    Codec.INT.fieldOf("mincount").forGetter(l -> l.mincount),
                    Codec.INT.fieldOf("maxcount").forGetter(l -> l.maxcount),
                    Codec.INT.fieldOf("attempts").forGetter(l -> l.attempts),
                    Codec.BOOL.optionalFieldOf("inbuilding").forGetter(l -> Optional.ofNullable(l.inbuilding)),
                    BiomeMatcher.CODEC.optionalFieldOf("biomes").forGetter(l -> Optional.ofNullable(l.biomeMatcher)),
                    BlockMatcher.CODEC.optionalFieldOf("biomes").forGetter(l -> Optional.ofNullable(l.blockMatcher))
            ).apply(instance, StuffSettingsRE::new));

    private ResourceLocation name;
    private final String blocks;
    private final int minheight;
    private final int maxheight;
    private final int mincount;
    private final int maxcount;
    private final int attempts;
    private final Boolean inbuilding;
    private final BiomeMatcher biomeMatcher;
    private final BlockMatcher blockMatcher;

    public StuffSettingsRE(String blocks, int minheight, int maxheight, int mincount, int maxcount, int attempts,
                           Optional<Boolean> inbuilding,
                           Optional<BiomeMatcher> biomeMatcher, Optional<BlockMatcher> blockMatcher) {
        this.blocks = blocks;
        this.minheight = minheight;
        this.maxheight = maxheight;
        this.mincount = mincount;
        this.maxcount = maxcount;
        this.attempts = attempts;
        this.inbuilding = inbuilding.orElse(null);
        this.biomeMatcher = biomeMatcher.orElse(null);
        this.blockMatcher = blockMatcher.orElse(null);
    }

    public String getBlocks() {
        return blocks;
    }

    public int getMinheight() {
        return minheight;
    }

    public int getMaxheight() {
        return maxheight;
    }

    public int getMincount() {
        return mincount;
    }

    public int getMaxcount() {
        return maxcount;
    }

    @Nullable
    public BiomeMatcher getBiomeMatcher() {
        return biomeMatcher;
    }

    public BlockMatcher getBlockMatcher() {
        return blockMatcher;
    }

    public Boolean isInBuilding() {
        return inbuilding;
    }

    public int getAttempts() {
        return attempts;
    }

    @Override
    public StuffSettingsRE setRegistryName(ResourceLocation name) {
        this.name = name;
        return this;
    }

    @Nullable
    public ResourceLocation getRegistryName() {
        return name;
    }
}
