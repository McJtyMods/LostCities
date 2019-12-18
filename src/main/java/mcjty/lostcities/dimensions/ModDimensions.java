package mcjty.lostcities.dimensions;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.dimensions.world.LostWorldProvider;
import mcjty.lostcities.dimensions.world.LostWorldType;
import mcjty.lostcities.dimensions.world.LostWorldTypeAdapter;
import mcjty.lostcities.dimensions.world.LostWorldTypeBOP;
import mcjty.lostcities.setup.ModSetup;
import net.minecraft.world.dimension.DimensionType;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModDimensions {

    public static LostWorldType worldType;
    public static LostWorldTypeBOP worldTypeBOP;
    public static List<LostWorldTypeAdapter> worldTypeAdapterList = new ArrayList<>();

    public static DimensionType lostDimensionType;
    public static Map<DimensionType, String> dimensionProfileMap = new HashMap<>();

    public static void init() {
        worldType = new LostWorldType();
        if (ModSetup.biomesoplenty) {
            worldTypeBOP = new LostWorldTypeBOP();
        }
        for (String worldtype : LostCityConfiguration.ADAPTING_WORLDTYPES) {
            worldTypeAdapterList.add(new LostWorldTypeAdapter(worldtype));
        }

        if (LostCityConfiguration.DIMENSION_ID != null) {
            // @todo 1.14
//            lostDimensionType = DimensionType.register(LostCities.MODID, "_lost", LostCityConfiguration.DIMENSION_ID, LostWorldProvider.class, false);
//            DimensionManager.registerDimension(LostCityConfiguration.DIMENSION_ID, lostDimensionType);
            dimensionProfileMap.put(LostCityConfiguration.DIMENSION_ID, LostCityConfiguration.DIMENSION_PROFILE);
        }

        for (String dimInfo : LostCityConfiguration.ADDITIONAL_DIMENSIONS) {
            String[] split = StringUtils.split(dimInfo, ':');
            int id = Integer.parseInt(split[0]);
            String profile = split[1];
            if (lostDimensionType == null) {
                // @todo .14
//                lostDimensionType = DimensionType.register(LostCities.MODID, "_lost", id, LostWorldProvider.class, false);
            }
            // @todo 1.14
//            DimensionManager.registerDimension(id, lostDimensionType);
//            dimensionProfileMap.put(id, profile);
        }

        // @todo 1.14
//        MapGenStructureIO.registerStructure(LostWoodlandMansion.Start.class, "LostMansion");
    }

}
