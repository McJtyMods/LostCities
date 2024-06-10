package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.Predicate;

public class ResourceLocationMatcher implements Predicate<ResourceLocation> {
    private final Set<ResourceLocation> ifAny;
    private final Set<ResourceLocation> excluding;

    public static final Codec<ResourceLocationMatcher> CODEC = RecordCodecBuilder.create(codec -> codec.group(
            Codec.STRING.listOf().optionalFieldOf("if_any").forGetter(ResourceLocationMatcher::getIfAny),
            Codec.STRING.listOf().optionalFieldOf("excluding").forGetter(ResourceLocationMatcher::getExcluding)
    ).apply(codec, ResourceLocationMatcher::new));

    public ResourceLocationMatcher(Optional<List<String>> ifAny, Optional<List<String>> excluding) {
        this.ifAny = new HashSet<>();
        ifAny.ifPresent(strings -> strings.forEach(s -> this.ifAny.add(DataTools.fromName(s))));
        this.excluding = new HashSet<>();
        excluding.ifPresent(strings -> strings.forEach(s -> this.excluding.add(DataTools.fromName(s))));
    }

    public boolean isAny() {
        return ifAny.isEmpty() && excluding.isEmpty();
    }

    private Optional<List<String>> getIfAny() {
        if (ifAny == null || ifAny.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(ifAny.stream().map(ResourceLocation::toString).toList());
        }
    }

    private Optional<List<String>> getExcluding() {
        if (excluding == null || excluding.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(excluding.stream().map(ResourceLocation::toString).toList());
        }
    }

    public static final ResourceLocationMatcher ANY = new ResourceLocationMatcher(Optional.empty(), Optional.empty()) {
        @Override
        public boolean test(ResourceLocation str) {
            return true;
        }
    };

    @Override
    public boolean test(ResourceLocation rl) {
        if (rl == null) {
            return false;
        }
        if (!ifAny.isEmpty() && !ifAny.contains(rl)) {
            return false;
        }
        return excluding.isEmpty() || !excluding.contains(rl);
    }
}
