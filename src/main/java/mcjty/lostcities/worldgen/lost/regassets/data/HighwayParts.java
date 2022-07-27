package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record HighwayParts(String tunnel, String open, String bridge, String tunnelBi, String openBi, String bridgeBi) {

    public static final Codec<HighwayParts> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("tunnel", "highway_tunnel").forGetter(HighwayParts::tunnel),
                    Codec.STRING.optionalFieldOf("open", "highway_open").forGetter(HighwayParts::open),
                    Codec.STRING.optionalFieldOf("bridge", "highway_bridge").forGetter(HighwayParts::bridge),
                    Codec.STRING.optionalFieldOf("tunnel_bi", "highway_tunnel_bi").forGetter(HighwayParts::tunnelBi),
                    Codec.STRING.optionalFieldOf("open_bi", "highway_open_bi").forGetter(HighwayParts::openBi),
                    Codec.STRING.optionalFieldOf("bridge_bi", "highway_bridge_bi").forGetter(HighwayParts::bridgeBi)
            ).apply(instance, HighwayParts::new));

    public static final HighwayParts DEFAULT = new HighwayParts("highway_tunnel", "highway_open", "highway_bridge",
            "highway_tunnel_bi", "highway_open_bi", "highway_bridge_bi");

    public Optional<HighwayParts> get() {
        if (this == DEFAULT) {
            return Optional.empty();
        } else {
            return Optional.of(this);
        }
    }

    public String tunnel(boolean bi) {
        return bi ? tunnelBi : tunnel;
    }

    public String open(boolean bi) {
        return bi ? openBi : open;
    }

    public String bridge(boolean bi) {
        return bi ? bridgeBi : bridge;
    }
}
