package mcjty.lostcities.worldgen.lost.regassets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.worldgen.lost.regassets.data.BiomeMatcher;
import mcjty.lostcities.worldgen.lost.regassets.data.BlockMatcher;
import mcjty.lostcities.worldgen.lost.regassets.data.ResourceLocationMatcher;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class StuffSettingsRE implements IAsset<StuffSettingsRE> {

    public static final Codec<StuffSettingsRE> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("column").forGetter(l -> l.column),
                    Codec.INT.fieldOf("minheight").forGetter(l -> l.minheight),
                    Codec.INT.fieldOf("maxheight").forGetter(l -> l.maxheight),
                    Codec.INT.fieldOf("mincount").forGetter(l -> l.mincount),
                    Codec.INT.fieldOf("maxcount").forGetter(l -> l.maxcount),
                    Codec.INT.fieldOf("attempts").forGetter(l -> l.attempts),
                    Codec.BOOL.optionalFieldOf("inbuilding").forGetter(l -> Optional.ofNullable(l.inbuilding)),
                    Codec.BOOL.optionalFieldOf("seesky").forGetter(l -> Optional.ofNullable(l.seesky)),
                    BiomeMatcher.CODEC.optionalFieldOf("biomes").forGetter(l -> Optional.ofNullable(l.biomeMatcher)),
                    BlockMatcher.CODEC.optionalFieldOf("blocks").forGetter(l -> Optional.ofNullable(l.blockMatcher)),
                    BlockMatcher.CODEC.optionalFieldOf("upperblocks").forGetter(l -> Optional.ofNullable(l.upperBlockMatcher)),
                    ResourceLocationMatcher.CODEC.optionalFieldOf("buildings").forGetter(l -> Optional.ofNullable(l.buildingMatcher))
            ).apply(instance, StuffSettingsRE::new));

    private ResourceLocation name;
    private final String column;
    private final int minheight;
    private final int maxheight;
    private final int mincount;
    private final int maxcount;
    private final int attempts;
    private final Boolean inbuilding;
    private final Boolean seesky;
    private final BiomeMatcher biomeMatcher;
    private final BlockMatcher blockMatcher;
    private final BlockMatcher upperBlockMatcher;
    private final ResourceLocationMatcher buildingMatcher;

    public StuffSettingsRE(String column, int minheight, int maxheight, int mincount, int maxcount, int attempts,
                           Optional<Boolean> inbuilding, Optional<Boolean> seesky,
                           Optional<BiomeMatcher> biomeMatcher, Optional<BlockMatcher> blockMatcher,
                           Optional<BlockMatcher> upperBlockMatcher,
                           Optional<ResourceLocationMatcher> buildingMatcher) {
        this.column = column;
        this.minheight = minheight;
        this.maxheight = maxheight;
        this.mincount = mincount;
        this.maxcount = maxcount;
        this.attempts = attempts;
        this.inbuilding = inbuilding.orElse(null);
        this.seesky = seesky.orElse(null);
        this.biomeMatcher = biomeMatcher.orElse(BiomeMatcher.ANY);
        this.blockMatcher = blockMatcher.orElse(BlockMatcher.ANY);
        this.upperBlockMatcher = upperBlockMatcher.orElse(BlockMatcher.ANY);
        this.buildingMatcher = buildingMatcher.orElse(ResourceLocationMatcher.ANY);
    }

    public String getColumn() {
        return column;
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

    public BiomeMatcher getBiomeMatcher() {
        return biomeMatcher;
    }

    public BlockMatcher getBlockMatcher() {
        return blockMatcher;
    }

    public BlockMatcher getUpperBlockMatcher() {
        return upperBlockMatcher;
    }

    public ResourceLocationMatcher getBuildingMatcher() {
        return buildingMatcher;
    }

    public Boolean isInBuilding() {
        return inbuilding;
    }

    public Boolean isSeesky() {
        return seesky;
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
