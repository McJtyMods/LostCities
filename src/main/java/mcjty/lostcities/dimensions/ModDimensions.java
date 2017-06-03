package mcjty.lostcities.dimensions;

import mcjty.lostcities.dimensions.world.LostCityWorldGenerator;
import mcjty.lostcities.dimensions.world.LostWorldType;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModDimensions {

    public static LostWorldType worldType;

    public static void init() {
        GameRegistry.registerWorldGenerator(new LostCityWorldGenerator(), 1000);
        worldType = new LostWorldType();
    }

}
