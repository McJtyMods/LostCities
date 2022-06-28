package mcjty.lostcities;

import mcjty.lostcities.api.ILostCitiesPre;
import mcjty.lostcities.api.ILostCityProfileSetup;

import java.util.function.Consumer;

public class LostCitiesPreImp implements ILostCitiesPre {

    @Override
    public void registerProfileSetupCallback(Consumer<ILostCityProfileSetup> runnable) {
        LostCities.setup.profileSetups.add(runnable);
    }
}
