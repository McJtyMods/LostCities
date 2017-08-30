package mcjty.lostcities.dimensions;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.dimensions.world.*;
import net.minecraft.world.DimensionType;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;
import java.util.List;

public class ModDimensions {

    public static LostWorldType worldType;
    public static LostWorldTypeBOP worldTypeBOP;
    public static List<LostWorldTypeAdapter> worldTypeAdapterList = new ArrayList<>();

    public static DimensionType lostDimensionType;


    public static void init() {
        worldType = new LostWorldType();
        if (LostCities.biomesoplenty) {
            worldTypeBOP = new LostWorldTypeBOP();
        }
        for (String worldtype : LostCityConfiguration.ADAPTING_WORLDTYPES) {
            worldTypeAdapterList.add(new LostWorldTypeAdapter(worldtype));
        }

        if (LostCityConfiguration.DIMENSION_ID != -1) {
            lostDimensionType = DimensionType.register(LostCities.MODID, "_lost", LostCityConfiguration.DIMENSION_ID, LostWorldProvider.class, false);
            DimensionManager.registerDimension(LostCityConfiguration.DIMENSION_ID, lostDimensionType);
        }

        MapGenStructureIO.registerStructure(LostWoodlandMansion.Start.class, "LostMansion");

    }

}
