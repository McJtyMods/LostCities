package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.varia.Tools;

import java.util.List;
import java.util.Optional;

public record HighwayParts(List<String> tunnel, List<String> open, List<String> bridge, List<String> tunnelBi,
                           List<String> openBi, List<String> bridgeBi) {

    public static final Codec<HighwayParts> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Tools.listOrStringList("tunnel", "highway_tunnel", HighwayParts::tunnel),
            Tools.listOrStringList("open", "highway_open", HighwayParts::open),
            Tools.listOrStringList("bridge", "highway_bridge", HighwayParts::bridge),
            Tools.listOrStringList("tunnel_bi", "highway_tunnel_bi", HighwayParts::tunnelBi),
            Tools.listOrStringList("open_bi", "highway_open_bi", HighwayParts::openBi),
            Tools.listOrStringList("bridge_bi", "highway_bridge_bi", HighwayParts::bridgeBi))
            .apply(instance, HighwayParts::new)
    );

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
