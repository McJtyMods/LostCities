package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public record HighwayParts(List<String> tunnel, List<String> open, List<String> bridge, List<String> tunnelBi, List<String> openBi, List<String> bridgeBi) {

    public static final Codec<HighwayParts> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.either(Codec.STRING, Codec.STRING.listOf())
                            .optionalFieldOf("tunnel", Either.left("highway_tunnel"))
                            .xmap(either -> either.map(List::of, Function.identity()), list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list))
                            .forGetter(HighwayParts::tunnel),
                    Codec.either(Codec.STRING, Codec.STRING.listOf())
                            .optionalFieldOf("open", Either.left("highway_open"))
                            .xmap(either -> either.map(List::of, Function.identity()), list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list))
                            .forGetter(HighwayParts::open),
                    Codec.either(Codec.STRING, Codec.STRING.listOf())
                            .optionalFieldOf("bridge", Either.left("highway_bridge"))
                            .xmap(either -> either.map(List::of, Function.identity()), list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list))
                            .forGetter(HighwayParts::bridge),
                    Codec.either(Codec.STRING, Codec.STRING.listOf())
                            .optionalFieldOf("tunnel_bi", Either.left("highway_tunnel_bi"))
                            .xmap(either -> either.map(List::of, Function.identity()), list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list))
                            .forGetter(HighwayParts::tunnelBi),
                    Codec.either(Codec.STRING, Codec.STRING.listOf())
                            .optionalFieldOf("open_bi", Either.left("highway_open_bi"))
                            .xmap(either -> either.map(List::of, Function.identity()), list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list))
                            .forGetter(HighwayParts::openBi),
                    Codec.either(Codec.STRING, Codec.STRING.listOf())
                            .optionalFieldOf("bridge_bi", Either.left("highway_bridge_bi"))
                            .xmap(either -> either.map(List::of, Function.identity()), list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list))
                            .forGetter(HighwayParts::bridgeBi)
            ).apply(instance, HighwayParts::new));

    public static final HighwayParts DEFAULT = new HighwayParts(
            List.of("highway_tunnel"),
            List.of("highway_open"),
            List.of("highway_bridge"),
            List.of("highway_tunnel_bi"),
            List.of("highway_open_bi"),
            List.of("highway_bridge_bi"));

    public Optional<HighwayParts> get() {
        if (this == DEFAULT) {
            return Optional.empty();
        } else {
            return Optional.of(this);
        }
    }

    public List<String> tunnel(boolean bi) {
        return bi ? tunnelBi : tunnel;
    }

    public List<String> open(boolean bi) {
        return bi ? openBi : open;
    }

    public List<String> bridge(boolean bi) {
        return bi ? bridgeBi : bridge;
    }
}
