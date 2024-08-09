package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.varia.Tools;

import java.util.List;
import java.util.Optional;

public record StreetParts(List<String> full, List<String> straight, List<String> end, List<String> bend,
                          List<String> t, List<String> none, List<String> all) {

    public static final Codec<StreetParts> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Tools.listOrStringList("full", "street_full", StreetParts::full),
            Tools.listOrStringList("straight", "street_straight", StreetParts::straight),
            Tools.listOrStringList("end", "street_end", StreetParts::end),
            Tools.listOrStringList("bend", "street_bend", StreetParts::bend),
            Tools.listOrStringList("t", "street_t", StreetParts::t),
            Tools.listOrStringList("none", "street_none", StreetParts::none),
            Tools.listOrStringList("all", "street_all", StreetParts::all))
            .apply(instance, StreetParts::new)
    );

    public static final StreetParts DEFAULT = new StreetParts(
            List.of("street_full"),
            List.of("street_straight"),
            List.of("street_end"),
            List.of("street_bend"),
            List.of("street_t"),
            List.of("street_none"),
            List.of("street_all"));

    public Optional<StreetParts> get() {
        if (this == DEFAULT) {
            return Optional.empty();
        } else {
            return Optional.of(this);
        }
    }
}
