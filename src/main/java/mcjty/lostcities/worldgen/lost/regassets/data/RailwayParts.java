package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.varia.Tools;

import java.util.List;
import java.util.Optional;

public record RailwayParts(List<String> stationUnderground, List<String> stationOpen, List<String> stationOpenRoof,
                           List<String> stationUndergroundStairs, List<String> stationStaircase, List<String> stationStaircaseSurface,
                           List<String> railsHorizontal, List<String> railsHorizontalEnd, List<String> railsHorizontalWater,
                           List<String> railsVertical, List<String> railsVerticalWater,
                           List<String> rails3Split, List<String> railsBend, List<String> railsFlat,
                           List<String> railsDown1, List<String> railsDown2) {

    public static final Codec<RailwayParts> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Tools.listOrStringList("stationunderground", "station_underground", RailwayParts::stationUnderground),
                    Tools.listOrStringList("stationopen", "station_open", RailwayParts::stationOpen),
                    Tools.listOrStringList("stationopenroof", "station_openroof", RailwayParts::stationOpenRoof),
                    Tools.listOrStringList("stationundergroundstairs", "station_underground_stairs", RailwayParts::stationUndergroundStairs),
                    Tools.listOrStringList("stationstaircase", "station_staircase", RailwayParts::stationStaircase),
                    Tools.listOrStringList("stationstaircasesurface", "station_staircase_surface", RailwayParts::stationStaircaseSurface),
                    Tools.listOrStringList("railshorizontal", "rails_horizontal", RailwayParts::railsHorizontal),
                    Tools.listOrStringList("railshorizontalend", "rails_horizontal_end", RailwayParts::railsHorizontalEnd),
                    Tools.listOrStringList("railshorizontalwater", "rails_horizontal_water", RailwayParts::railsHorizontalWater),
                    Tools.listOrStringList("railsvertical", "rails_vertical", RailwayParts::railsVertical),
                    Tools.listOrStringList("railsverticalwater", "rails_vertical_water", RailwayParts::railsVerticalWater),
                    Tools.listOrStringList("rails3split", "rails_3split", RailwayParts::rails3Split),
                    Tools.listOrStringList("railsbend", "rails_bend", RailwayParts::railsBend),
                    Tools.listOrStringList("railsflat", "rails_flat", RailwayParts::railsFlat),
                    Tools.listOrStringList("railsdown1", "rails_down1", RailwayParts::railsDown1),
                    Tools.listOrStringList("railsdown2", "rails_down2", RailwayParts::railsDown2)
            ).apply(instance, RailwayParts::new));

    public static final RailwayParts DEFAULT = new RailwayParts(
            List.of("station_underground"),
            List.of("station_open"),
            List.of("station_openroof"),
            List.of("station_underground_stairs"),
            List.of("station_staircase"),
            List.of("station_staircase_surface"),
            List.of("rails_horizontal"),
            List.of("rails_horizontal_end"),
            List.of("rails_horizontal_water"),
            List.of("rails_vertical"),
            List.of("rails_vertical_water"),
            List.of("rails_3split"),
            List.of("rails_bend"),
            List.of("rails_flat"),
            List.of("rails_down1"),
            List.of("rails_down2"));

    public Optional<RailwayParts> get() {
        if (this == DEFAULT) {
            return Optional.empty();
        } else {
            return Optional.of(this);
        }
    }
}
