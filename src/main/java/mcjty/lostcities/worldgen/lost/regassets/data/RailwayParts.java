package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record RailwayParts(String stationUnderground, String stationOpen, String stationOpenRoof,
                           String stationUndergroundStairs, String stationStaircase, String stationStaircaseSurface,
                           String railsHorizontal, String railsHorizontalEnd, String railsHorizontalWater,
                           String railsVertical, String railsVerticalWater,
                           String rails3Split, String railsBend, String railsFlat,
                           String railsDown1, String railsDown2) {

    public static final Codec<RailwayParts> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("stationunderground", "station_underground").forGetter(RailwayParts::stationUnderground),
                    Codec.STRING.optionalFieldOf("stationopen", "station_open").forGetter(RailwayParts::stationOpen),
                    Codec.STRING.optionalFieldOf("stationopenroof", "station_openroof").forGetter(RailwayParts::stationOpenRoof),
                    Codec.STRING.optionalFieldOf("stationundergroundstairs", "station_underground_stairs").forGetter(RailwayParts::stationUndergroundStairs),
                    Codec.STRING.optionalFieldOf("stationstaircase", "station_staircase").forGetter(RailwayParts::stationStaircase),
                    Codec.STRING.optionalFieldOf("stationstaircasesurface", "station_staircase_surface").forGetter(RailwayParts::stationStaircaseSurface),
                    Codec.STRING.optionalFieldOf("railshorizontal", "rails_horizontal").forGetter(RailwayParts::railsHorizontal),
                    Codec.STRING.optionalFieldOf("railshorizontalend", "rails_horizontal_end").forGetter(RailwayParts::railsHorizontalEnd),
                    Codec.STRING.optionalFieldOf("railshorizontalwater", "rails_horizontal_water").forGetter(RailwayParts::railsHorizontalWater),
                    Codec.STRING.optionalFieldOf("railsvertical", "rails_vertical").forGetter(RailwayParts::railsVertical),
                    Codec.STRING.optionalFieldOf("railsverticalwater", "rails_vertical_water").forGetter(RailwayParts::railsVerticalWater),
                    Codec.STRING.optionalFieldOf("rails3split", "rails_3split").forGetter(RailwayParts::rails3Split),
                    Codec.STRING.optionalFieldOf("railsbend", "rails_bend").forGetter(RailwayParts::railsBend),
                    Codec.STRING.optionalFieldOf("railsflat", "rails_flat").forGetter(RailwayParts::railsFlat),
                    Codec.STRING.optionalFieldOf("railsdown1", "rails_down1").forGetter(RailwayParts::railsDown1),
                    Codec.STRING.optionalFieldOf("railsdown2", "rails_down2").forGetter(RailwayParts::railsDown2)
            ).apply(instance, RailwayParts::new));

    public static final RailwayParts DEFAULT = new RailwayParts(
            "station_underground",
            "station_open",
            "station_openroof",
            "station_underground_stairs",
            "station_staircase",
            "station_staircase_surface",
            "rails_horizontal",
            "rails_horizontal_end",
            "rails_horizontal_water",
            "rails_vertical",
            "rails_vertical_water",
            "rails_3split",
            "rails_bend",
            "rails_flat",
            "rails_down1",
            "rails_down2");

    public Optional<RailwayParts> get() {
        if (this == DEFAULT) {
            return Optional.empty();
        } else {
            return Optional.of(this);
        }
    }
}
