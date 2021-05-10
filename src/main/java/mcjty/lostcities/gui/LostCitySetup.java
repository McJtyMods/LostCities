package mcjty.lostcities.gui;

import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.config.LostCityProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LostCitySetup {

    public static final LostCitySetup CLIENT_SETUP = new LostCitySetup(() -> {});

    private List<String> profiles = null;

    private String profile = null;
    private LostCityProfile customizedProfile = null;

    private final Runnable refreshPreview;

    public LostCitySetup(Runnable refreshPreview) {
        this.refreshPreview = refreshPreview;
    }

    public LostCityProfile getCustomizedProfile() {
        return customizedProfile;
    }

    public void reset() {
        profiles = null;
        profile = null;
        customizedProfile = null;
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
        refreshPreview.run();
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
        LostCityConfiguration.standardProfiles.put("customized", customizedProfile);
        profiles.add("customized");
        customizedProfile.copyFrom(original);
        profile = "customized";
        refreshPreview.run();
    }

    public Optional<LostCityProfile> get() {
        if (profile == null) {
            return Optional.empty();
        } else if ("customized".equals(profile)) {
            return Optional.ofNullable(customizedProfile);
        } else {
            return Optional.of(LostCityConfiguration.standardProfiles.get(profile));
        }
    }

//    public Optional<Configuration> getConfig() {
//        if (profile == null) {
//            return Optional.empty();
//        } else if ("customized".equals(profile)) {
//
//        } else {
//
//        }
//    }

    public void toggleProfile(/* @todo 1.16 WorldType worldType */) {
        if (profiles == null) {
            String preferedProfile = "default";
            // @todo 1.16
//            if ("lc_cavern".equals(worldType.getName())) {
//                preferedProfile = "cavern";
//            }
            profiles = new ArrayList<>(LostCityConfiguration.standardProfiles.keySet());
            String finalPreferedProfile = preferedProfile;
            profiles.sort((o1, o2) -> {
                if (finalPreferedProfile.equals(o1)) {
                    return -1;
                }
                if (finalPreferedProfile.equals(o2)) {
                    return 1;
                }
                return o1.compareTo(o2);
            });
        }

        if (profile == null) {
//            if (customizedProfile != null) {
//                profile = "customized";
//            } else {
                profile = profiles.get(0);
//            }
        } else {
            int i = profiles.indexOf(profile);
            if (i == -1 || i >= profiles.size()-1) {
                profile = null;
            } else {
                profile = profiles.get(i+1);
            }
        }
        refreshPreview.run();
    }
}
