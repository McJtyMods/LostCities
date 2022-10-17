package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public class CitySphereSettings {

    public static final Codec<CitySphereSettings> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("centerpart").forGetter(l -> Optional.ofNullable(l.centerpart)),
                    Codec.STRING.optionalFieldOf("centertype").forGetter(CitySphereSettings::getOptionalCentertype),
                    Codec.STRING.optionalFieldOf("centerpartorigin").forGetter(CitySphereSettings::getOptionalCentertype),
                    Codec.INT.optionalFieldOf("centerpartoffset").forGetter(l -> l.centerpartOffset == 0 ? Optional.empty() : Optional.of(l.centerpartOffset))
            ).apply(instance, CitySphereSettings::new));

    private final String centerpart;
    private final CitySphereCenterType centertype;
    private final CitySpherePartOrigin centerpartOrigin;
    private final int centerpartOffset;

    public CitySphereSettings(Optional<String> centerpart, Optional<String> centertype, Optional<String> centerpartOrigin,
                              Optional<Integer> centerpartOffset) {
        this.centerpart = centerpart.orElse(null);
        this.centertype = centertype.map(l -> CitySphereCenterType.valueOf(l.toUpperCase())).orElse(CitySphereCenterType.DEFAULT);
        this.centerpartOrigin = centerpartOrigin.map(l -> CitySpherePartOrigin.valueOf(l.toUpperCase())).orElse(CitySpherePartOrigin.TOP);
        this.centerpartOffset = centerpartOffset.orElse(0);
    }

    public String getCenterpart() {
        return centerpart;
    }

    private Optional<String> getOptionalCentertype() {
        if (centertype == CitySphereCenterType.DEFAULT) {
            return Optional.empty();
        } else {
            return Optional.of(centertype.name().toLowerCase());
        }
    }

    public CitySphereCenterType getCenterType() {
        return centertype;
    }

    public CitySpherePartOrigin getCenterPartOrigin() {
        return centerpartOrigin;
    }

    public int getCenterPartOffset() {
        return centerpartOffset;
    }

    public enum CitySphereCenterType {
        DEFAULT,
        STREET,
        BUILDING,
        NORMAL
    }

    public enum CitySpherePartOrigin {
        FIXED,
        CENTER,
        FIRSTFLOOR,
        GROUND,
        TOP,
    }
}
