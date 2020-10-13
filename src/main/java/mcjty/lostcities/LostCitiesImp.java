package mcjty.lostcities;

import mcjty.lostcities.api.ILostChunkGenerator;
import mcjty.lostcities.api.ILostCities;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class LostCitiesImp implements ILostCities {

    @Nullable
    @Override
    public ILostChunkGenerator getLostGenerator(RegistryKey<World> dimension) {
//        return WorldTypeTools.getChunkGenerator(dimension);
        return null;
    }
}
