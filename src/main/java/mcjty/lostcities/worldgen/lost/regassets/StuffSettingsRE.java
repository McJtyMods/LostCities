package mcjty.lostcities.worldgen.lost.regassets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.worldgen.lost.regassets.data.BiomeMatcher;
import mcjty.lostcities.worldgen.lost.regassets.data.BlockMatcher;
import mcjty.lostcities.worldgen.lost.regassets.data.ResourceLocationMatcher;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class StuffSettingsRE implements IAsset<StuffSettingsRE> {

    public static final Codec<StuffSettingsRE> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.listOf().optionalFieldOf("tags").forGetter(l -> l.tags.isEmpty() ? Optional.empty() : Optional.of(l.tags)),
                    Codec.STRING.fieldOf("column").forGetter(l -> l.column),
                    Codec.INT.optionalFieldOf("minheight").forGetter(l -> Optional.ofNullable(l.minheight)),
                    Codec.INT.optionalFieldOf("maxheight").forGetter(l -> Optional.ofNullable(l.maxheight)),
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
    private final List<String> tags;
    private final String column;
    private final Integer minheight;
    private final Integer maxheight;
    private final int mincount;
    private final int maxcount;
    private final int attempts;
    private final Boolean inbuilding;
    private final Boolean seesky;
    private final BiomeMatcher biomeMatcher;
    private final BlockMatcher blockMatcher;
    private final BlockMatcher upperBlockMatcher;
    private final ResourceLocationMatcher buildingMatcher;

    public StuffSettingsRE(Optional<List<String>> tags,
                           String column,
                           Optional<Integer> minheight, Optional<Integer> maxheight, int mincount, int maxcount, int attempts,
                           Optional<Boolean> inbuilding, Optional<Boolean> seesky,
                           Optional<BiomeMatcher> biomeMatcher, Optional<BlockMatcher> blockMatcher,
                           Optional<BlockMatcher> upperBlockMatcher,
                           Optional<ResourceLocationMatcher> buildingMatcher) {
        this.tags = tags.orElse(Collections.emptyList());
        this.column = column;
        this.minheight = minheight.orElse(null);
        this.maxheight = maxheight.orElse(null);
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

    public List<String> getTags() {
        return tags;
    }

    public String getColumn() {
        return column;
    }

    public Integer getMinheight() {
        return minheight;
    }

    public Integer getMaxheight() {
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
