package mcjty.lostcities.dimensions;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.dimensions.world.LostCityWorldGenerator;
import mcjty.lostcities.dimensions.world.LostWorldType;
import mcjty.lostcities.dimensions.world.LostWorldTypeATG;
import mcjty.lostcities.dimensions.world.LostWorldTypeBOP;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModDimensions {

    public static LostWorldType worldType;
    public static LostWorldTypeBOP worldTypeBOP;
    public static LostWorldTypeATG worldTypeATG;

    public static void init() {
        GameRegistry.registerWorldGenerator(new LostCityWorldGenerator(), 1000);
        worldType = new LostWorldType();
        if (LostCities.biomesoplenty) {
            worldTypeBOP = new LostWorldTypeBOP();
        }
        if (LostCities.atg) {
            worldTypeATG = new LostWorldTypeATG();
        }
    }

}
