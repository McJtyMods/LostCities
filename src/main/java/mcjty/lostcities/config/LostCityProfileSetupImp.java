package mcjty.lostcities.config;

import mcjty.lostcities.api.ILostCityProfile;
import mcjty.lostcities.api.ILostCityProfileSetup;

public class LostCityProfileSetupImp implements ILostCityProfileSetup {

    @Override
    public ILostCityProfile createProfile(String name, String baseProfile) {
        LostCityProfile lcp = ProfileSetup.standardProfiles.get(baseProfile);
        if (lcp == null) {
            throw new RuntimeException("Unknown base profile '" + baseProfile + "'!");
        }
        LostCityProfile profile = new LostCityProfile(name, true);
        profile.copyFrom(lcp);
        ProfileSetup.standardProfiles.put(profile.getName(), profile);
        return profile;
    }
}
