package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public record BlockMatcher(Optional<List<BlockState>> ifAll, Optional<List<BlockState>> ifAny, Optional<List<BlockState>> excluding) implements Predicate<BlockState> {
	public static final Codec<BlockMatcher> CODEC = RecordCodecBuilder.create(codec -> codec.group(
			BlockState.CODEC.listOf().optionalFieldOf("if_all").forGetter(BlockMatcher::ifAll),
			BlockState.CODEC.listOf().optionalFieldOf("if_any").forGetter(BlockMatcher::ifAny),
			BlockState.CODEC.listOf().optionalFieldOf("excluding").forGetter(BlockMatcher::excluding)
	).apply(codec, BlockMatcher::new));

	@Override
	public boolean test(BlockState state) {
		if (ifAll.isPresent() && !ifAll.get().stream().allMatch(s -> s.equals(state))) {
			return false;
		}

		if (ifAny.isPresent() && ifAny.get().stream().noneMatch(s -> s.equals(state))) {
			return false;
		}

		return excluding.isEmpty() || excluding.get().stream().noneMatch(s -> s.equals(state));
	}
}
