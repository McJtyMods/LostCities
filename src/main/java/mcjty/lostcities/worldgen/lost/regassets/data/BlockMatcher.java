package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BlockMatcher implements Predicate<BlockState> {
    private final Optional<List<String>> ifAll;
    private final Optional<List<String>> ifAny;
    private final Optional<List<String>> excluding;
    private final Predicate<BlockState> predicate;

    public static final Codec<BlockMatcher> CODEC = RecordCodecBuilder.create(codec -> codec.group(
            Codec.STRING.listOf().optionalFieldOf("if_all").forGetter(BlockMatcher::getIfAll),
            Codec.STRING.listOf().optionalFieldOf("if_any").forGetter(BlockMatcher::getIfAny),
            Codec.STRING.listOf().optionalFieldOf("excluding").forGetter(BlockMatcher::getExcluding)
    ).apply(codec, BlockMatcher::new));

    private Predicate<BlockState> getStatePredicate(String matcher) {
        if (matcher.startsWith("#")) {
            TagKey<Block> tagKey = TagKey.create(Registries.BLOCK, ResourceLocation.parse(matcher.substring(1)));
            return state -> state.is(tagKey);
        } else {
            Block b = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(matcher));
            return state -> state.getBlock() == b;
        }
    }

    public boolean isAny() {
        return ifAll.isEmpty() && ifAny.isEmpty() && excluding.isEmpty();
    }

    private Predicate<BlockState> getNotStatePredicate(String matcher) {
        if (matcher.startsWith("#")) {
            TagKey<Block> tagKey = TagKey.create(Registries.BLOCK, ResourceLocation.parse(matcher.substring(1)));
            return state -> !state.is(tagKey);
        } else {
            Block b = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(matcher));
            return state -> state.getBlock() != b;
        }
    }

    public BlockMatcher(Optional<List<String>> ifAll, Optional<List<String>> ifAny, Optional<List<String>> excluding) {
        this.ifAll = ifAll;
        this.ifAny = ifAny;
        this.excluding = excluding;
        Predicate<BlockState> p = null;
        if (ifAll.isPresent()) {
            for (String s : ifAll.get()) {
                Predicate<BlockState> q = getStatePredicate(s);
                if (p == null) {
                    p = q;
                } else {
                    p = p.and(q);
                }
            }
        }
        if (ifAny.isPresent()) {
            Predicate<BlockState> q = null;
            for (String s : ifAny.get()) {
                Predicate<BlockState> sp = getStatePredicate(s);
                if (q == null) {
					q = sp;
				} else {
					q = q.or(sp);
				}
            }
            if (p == null) {
                p = q;
            } else if (q != null) {
                p = p.and(q);
            }
        }
        if (excluding.isPresent()) {
            for (String s : excluding.get()) {
                Predicate<BlockState> sp = getNotStatePredicate(s);
                if (p == null) {
                    p = sp;
                } else {
                    p = p.and(sp);
                }
            }
        }
        this.predicate = p == null ? (state) -> true : p;
    }

    private Optional<List<String>> getIfAll() {
        return ifAll;
    }

    private Optional<List<String>> getIfAny() {
        return ifAny;
    }

    private Optional<List<String>> getExcluding() {
        return excluding;
    }

    public static final BlockMatcher ANY = new BlockMatcher(Optional.empty(), Optional.empty(), Optional.empty()) {
        @Override
        public boolean test(BlockState state) {
            return true;
        }
    };

    @Override
    public boolean test(BlockState state) {
        return predicate.test(state);
    }
}
