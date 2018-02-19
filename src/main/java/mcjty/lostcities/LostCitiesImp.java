package mcjty.lostcities;

import mcjty.lostcities.api.ILostChunkGenerator;
import mcjty.lostcities.api.ILostCities;
import mcjty.lostcities.dimensions.world.WorldTypeTools;

import javax.annotation.Nullable;

public class LostCitiesImp implements ILostCities {

    @Nullable
    @Override
    public ILostChunkGenerator getLostGenerator(int dimension) {
        return WorldTypeTools.getChunkGenerator(dimension);
    }
}
