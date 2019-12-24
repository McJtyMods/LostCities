package mcjty.lostcities.gui;

import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.config.LostCityProfile;
import sun.security.pkcs11.wrapper.Constants;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class LostCitySetup {

    public static final LostCitySetup CLIENT_SETUP = new LostCitySetup();

    private static final List<String> PROFILES = Arrays.asList("default", "nodamage", "rarecities", "onlycities", "tallbuildings", "safe",
            "ancient", "wasteland", "atlantis", "realistic");

    private String profile = null;
    private LostCityProfile customizedProfile = null;

    public LostCityProfile getCustomizedProfile() {
        return customizedProfile;
    }

    public boolean isCustomizable() {
        if (profile == null) {
            return false;
        }
        if ("customized".equals(profile)) {
            return false;
        }
        return true;
    }

    public String getProfile() {
        return profile;
    }

    public String getProfileLabel() {
        return profile == null ? "Disabled" : profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public void copyFrom(LostCitySetup other) {
        this.profile = other.profile;
        this.customizedProfile = other.customizedProfile;
    }

    public void customize() {
        if (profile == null) {
            throw new IllegalStateException("Cannot happen!");
        }
        customizedProfile = new LostCityProfile("customized", false);
        LostCityProfile original = LostCityConfiguration.standardProfiles.get(profile);
        customizedProfile.copyFrom(original);
        profile = "customized";
    }

    private Optional<LostCityProfile> get() {
        if (profile == null) {
            return Optional.empty();
        } else if ("customized".equals(profile)) {
            return Optional.ofNullable(customizedProfile);
        } else {
            return Optional.of(LostCityConfiguration.standardProfiles.get(profile));
        }
    }

    public void toggleProfile() {
        if (profile == null) {
            if (customizedProfile != null) {
                profile = "customized";
            } else {
                profile = PROFILES.get(0);
            }
        } else if ("customized".equals(profile)) {
            profile = PROFILES.get(0);
        } else {
            int i = PROFILES.indexOf(profile);
            if (i == -1 || i >= PROFILES.size()-1) {
                profile = null;
            } else {
                profile = PROFILES.get(i+1);
            }
        }
    }

    private static <T extends Comparable<T>> T constrain(T value, T min, T max) {
        if (value.compareTo(min) < 0) {
            return min;
        }
        if (value.compareTo(max) > 0) {
            return max;
        }
        return value;
    }

    public void setFloatValue(String s, BiConsumer<LostCityProfile, Float> setter, float minValue, float maxValue) {
        float value = 0;
        try {
            value = Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return;
        }
        float finalValue = value;
        get().ifPresent(p -> setter.accept(p, constrain(finalValue, minValue, maxValue)));
    }

    public void setIntValue(String s, BiConsumer<LostCityProfile, Integer> setter, int minValue, int maxValue) {
        int value = 0;
        try {
            value = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return;
        }
        int finalValue = value;
        get().ifPresent(p -> setter.accept(p, constrain(finalValue, minValue, maxValue)));
    }

    public String getRarity() {
        return get().map(p -> Float.toString(p.CITY_CHANCE)).orElse("n.a.");
    }

    public void setRarity(String s) {
        setFloatValue(s, (p,f) -> p.CITY_CHANCE = f, 0.0f, 1.0f);
    }

    public String getMinSize() {
        return get().map(p -> Integer.toString(p.CITY_MINRADIUS)).orElse("n.a.");
    }

    public void setMinSize(String s) {
        setIntValue(s, (p,f) -> p.CITY_MINRADIUS = f, 1, 1000);
    }

    public String getMaxSizeLabel() {
        return get().map(p -> Integer.toString(p.CITY_MAXRADIUS)).orElse("n.a.");
    }

    public void setMaxSize(String s) {
        setIntValue(s, (p,f) -> p.CITY_MAXRADIUS = f, 1, 1000);
    }


    public String getMinFloors() {
        return get().map(p -> Integer.toString(p.BUILDING_MINFLOORS)).orElse("n.a.");
    }

    public void setMinFloors(String s) {
        setIntValue(s, (p,f) -> p.BUILDING_MINFLOORS = f, 0, 30);
    }

    public String getMaxFloors() {
        return get().map(p -> Integer.toString(p.BUILDING_MAXFLOORS)).orElse("n.a.");
    }

    public void setMaxFloors(String s) {
        setIntValue(s, (p,f) -> p.BUILDING_MAXFLOORS = f, 0, 30);
    }

    public String getMinFloorsChance() {
        return get().map(p -> Integer.toString(p.BUILDING_MINFLOORS_CHANCE)).orElse("n.a.");
    }

    public void setMinFloorsChance(String s) {
        setIntValue(s, (p,f) -> p.BUILDING_MINFLOORS_CHANCE = f, 1, 30);
    }

    public String getMaxFloorsChance() {
        return get().map(p -> Integer.toString(p.BUILDING_MAXFLOORS_CHANCE)).orElse("n.a.");
    }

    public void setMaxFloorsChance(String s) {
        setIntValue(s, (p,f) -> p.BUILDING_MAXFLOORS_CHANCE = f, 1, 30);
    }
}
