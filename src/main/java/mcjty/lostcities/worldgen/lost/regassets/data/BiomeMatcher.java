package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Biome matcher kindly donated by Tslat (from https://github.com/Tslat/Advent-Of-Ascension/blob/1.19/source/content/world/gen/BiomeMatcher.java)
 */
public class BiomeMatcher implements Predicate<Holder<Biome>> {
	private final Optional<List<HolderSet<Biome>>> ifAll;
	private final Optional<List<HolderSet<Biome>>> ifAny;
	private final Optional<List<HolderSet<Biome>>> excluding;

	public static final Codec<BiomeMatcher> CODEC = RecordCodecBuilder.create(codec -> codec.group(
			Biome.LIST_CODEC.listOf().optionalFieldOf("if_all").forGetter(BiomeMatcher::getIfAll),
			Biome.LIST_CODEC.listOf().optionalFieldOf("if_any").forGetter(BiomeMatcher::getIfAny),
			Biome.LIST_CODEC.listOf().optionalFieldOf("excluding").forGetter(BiomeMatcher::getExcluding)
	).apply(codec, BiomeMatcher::new));

	public BiomeMatcher(Optional<List<HolderSet<Biome>>> ifAll, Optional<List<HolderSet<Biome>>> ifAny, Optional<List<HolderSet<Biome>>> excluding) {
		this.ifAll = ifAll;
		this.ifAny = ifAny;
		this.excluding = excluding;
	}

	private Optional<List<HolderSet<Biome>>> getIfAll() {
		return ifAll;
	}

	private Optional<List<HolderSet<Biome>>> getIfAny() {
		return ifAny;
	}

	private Optional<List<HolderSet<Biome>>> getExcluding() {
		return excluding;
	}

	public static final BiomeMatcher ANY = new BiomeMatcher(Optional.empty(), Optional.empty(), Optional.empty()) {
		@Override
		public boolean test(Holder<Biome> biome) {
			return true;
		}
	};

	@Override
	public boolean test(Holder<Biome> biome) {
		if (ifAll.isPresent() && !ifAll.get().stream().allMatch(set -> set.contains(biome))) {
			return false;
		}

		if (ifAny.isPresent() && ifAny.get().stream().noneMatch(set -> set.contains(biome))) {
			return false;
		}

		return excluding.isEmpty() || excluding.get().stream().noneMatch(set -> set.contains(biome));
	}
}
