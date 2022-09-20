package mcjty.lostcities.config;

import mcjty.lostcities.api.ILostCityProfile;
import mcjty.lostcities.api.ILostCityProfileSetup;

public class LostCityProfileSetupImp implements ILostCityProfileSetup {

    @Override
    public ILostCityProfile createProfile(String name, String baseProfile) {
        LostCityProfile lcp = ProfileSetup.STANDARD_PROFILES.get(baseProfile);
        if (lcp == null) {
            throw new RuntimeException("Unknown base profile '" + baseProfile + "'!");
        }
        LostCityProfile profile = new LostCityProfile(name, true);
        profile.copyFrom(lcp);
        ProfileSetup.STANDARD_PROFILES.put(profile.getName(), profile);
        return profile;
    }
}
