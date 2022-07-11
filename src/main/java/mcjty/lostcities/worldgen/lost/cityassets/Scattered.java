package mcjty.lostcities.worldgen.lost.cityassets;

import mcjty.lostcities.api.ILostCityAsset;
import mcjty.lostcities.worldgen.lost.regassets.ScatteredRE;
import mcjty.lostcities.worldgen.lost.regassets.data.DataTools;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class Scattered implements ILostCityAsset {

    private final ResourceLocation name;
    private final List<String> buildings;

    public Scattered(ScatteredRE object) {
        name = object.getRegistryName();
        this.buildings = object.getBuildings();
    }

    public List<String> getBuildings() {
        return buildings;
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
