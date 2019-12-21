package mcjty.lostcities;

import mcjty.lostcities.api.ILostChunkGenerator;
import mcjty.lostcities.api.ILostCities;
import net.minecraft.world.dimension.DimensionType;

import javax.annotation.Nullable;

public class LostCitiesImp implements ILostCities {

    @Nullable
    @Override
    public ILostChunkGenerator getLostGenerator(DimensionType dimension) {
//        return WorldTypeTools.getChunkGenerator(dimension);
        return null;
    }
}
