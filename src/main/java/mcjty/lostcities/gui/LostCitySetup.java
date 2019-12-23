package mcjty.lostcities.gui;

import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.config.LostCityProfile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
        } else if (profile == "customized") {
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

    public String getRarityLabel() {
        return get().map(p -> Float.toString(p.CITY_CHANCE)).orElse("n.a.");
    }

    public void setRarity(String s) {
        get().ifPresent(p -> p.CITY_CHANCE = constrain(Float.parseFloat(s), 0.0f, 1.0f));
    }

    public String getMaxRadiusLabel() {
        return get().map(p -> Integer.toString(p.CITY_MAXRADIUS)).orElse("n.a.");
    }

    public void setMaxSizeLabel(String s) {
        get().ifPresent(p -> p.CITY_MAXRADIUS = constrain(Integer.parseInt(s), 1, 1000));
    }

    public String getMinRadiusLabel() {
        return get().map(p -> Integer.toString(p.CITY_MINRADIUS)).orElse("n.a.");
    }

    public void setMinSizeLabel(String s) {
        get().ifPresent(p -> p.CITY_MINRADIUS = constrain(Integer.parseInt(s), 1, 1000));
    }
}
