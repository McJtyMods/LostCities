package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record WorldSettings(
        RailwayAvoidance railwayAvoidance,
        int railPartHeight6,
        BlockState vineWest,
        BlockState vineEast,
        BlockState vineSouth,
        BlockState vineNorth) {

    public enum RailwayAvoidance implements StringRepresentable {
        IGNORE("ignore"),
        BLOCK_RAILWAY("block_railway");

        private final String name;

        RailwayAvoidance(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static final Codec<WorldSettings> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    StringRepresentable.fromEnum(RailwayAvoidance::values).fieldOf("railwayavoidance").forGetter(l -> l.railwayAvoidance),
                    Codec.INT.optionalFieldOf("railpartheight6", 1).forGetter(l -> l.railPartHeight6),
                    BlockState.CODEC.optionalFieldOf("vinewest", getVine(VineBlock.WEST)).forGetter(l -> l.vineWest),
                    BlockState.CODEC.optionalFieldOf("vineeast", getVine(VineBlock.EAST)).forGetter(l -> l.vineEast),
                    BlockState.CODEC.optionalFieldOf("vinesouth", getVine(VineBlock.SOUTH)).forGetter(l -> l.vineSouth),
                    BlockState.CODEC.optionalFieldOf("vinenorth", getVine(VineBlock.NORTH)).forGetter(l -> l.vineNorth)
            ).apply(instance, WorldSettings::new));

    public static final WorldSettings DEFAULT = new WorldSettings(RailwayAvoidance.IGNORE, 1, getVine(VineBlock.WEST), getVine(VineBlock.EAST), getVine(VineBlock.SOUTH), getVine(VineBlock.NORTH));

    private static @NotNull BlockState getVine(BooleanProperty property) {
        return Blocks.VINE.defaultBlockState().setValue(property, true);
    }

    public Optional<WorldSettings> get() {
        if (this == DEFAULT) {
            return Optional.empty();
        } else {
            return Optional.of(this);
        }
    }

}
