package mcjty.lostcities.dimensions;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.dimensions.world.*;
import mcjty.lostcities.setup.ModSetup;
import net.minecraft.world.DimensionType;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.DimensionManager;
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
    public static Map<Integer, String> dimensionProfileMap = new HashMap<>();

    public static void init() {
        worldType = new LostWorldType();
        if (ModSetup.biomesoplenty) {
            worldTypeBOP = new LostWorldTypeBOP();
        }
        for (String worldtype : LostCityConfiguration.ADAPTING_WORLDTYPES) {
            worldTypeAdapterList.add(new LostWorldTypeAdapter(worldtype));
        }

        if (LostCityConfiguration.DIMENSION_ID != -1) {
            lostDimensionType = DimensionType.register(LostCities.MODID, "_lost", LostCityConfiguration.DIMENSION_ID, LostWorldProvider.class, false);
            DimensionManager.registerDimension(LostCityConfiguration.DIMENSION_ID, lostDimensionType);
            dimensionProfileMap.put(LostCityConfiguration.DIMENSION_ID, LostCityConfiguration.DIMENSION_PROFILE);
        }

        for (String dimInfo : LostCityConfiguration.ADDITIONAL_DIMENSIONS) {
            String[] split = StringUtils.split(dimInfo, ':');
            int id = Integer.parseInt(split[0]);
            String profile = split[1];
            if (lostDimensionType == null) {
                lostDimensionType = DimensionType.register(LostCities.MODID, "_lost", id, LostWorldProvider.class, false);
            }
            DimensionManager.registerDimension(id, lostDimensionType);
            dimensionProfileMap.put(id, profile);
        }

        MapGenStructureIO.registerStructure(LostWoodlandMansion.Start.class, "LostMansion");
    }

}
