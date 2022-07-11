package mcjty.lostcities.worldgen.lost.cityassets;

import mcjty.lostcities.api.ILostCityMultiBuilding;
import mcjty.lostcities.worldgen.lost.regassets.MultiBuildingRE;
import mcjty.lostcities.worldgen.lost.regassets.data.DataTools;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class MultiBuilding implements ILostCityMultiBuilding {

    private final ResourceLocation name;
    private final int dimX;
    private final int dimZ;
    private final List<List<String>> buildings;

    public MultiBuilding(MultiBuildingRE object) {
        name = object.getRegistryName();
        this.dimX = object.getDimX();
        this.dimZ = object.getDimZ();
        this.buildings = object.getBuildings();
    }

    @Override
    public String getBuilding(int x, int z) {
        return buildings.get(x).get(z);
    }

    @Override
    public int getDimX() {
        return dimX;
    }

    @Override
    public int getDimZ() {
        return dimZ;
    }

    @Override
    public String getName() {
        return DataTools.toName(name);
    }

    @Override
    public ResourceLocation getId() {
        return name;
    }
}
