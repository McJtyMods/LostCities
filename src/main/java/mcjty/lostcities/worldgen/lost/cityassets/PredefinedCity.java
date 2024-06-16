package mcjty.lostcities.worldgen.lost.cityassets;

import mcjty.lostcities.api.ILostCityAsset;
import mcjty.lostcities.worldgen.lost.regassets.PredefinedCityRE;
import mcjty.lostcities.worldgen.lost.regassets.data.DataTools;
import mcjty.lostcities.worldgen.lost.regassets.data.PredefinedBuilding;
import mcjty.lostcities.worldgen.lost.regassets.data.PredefinedStreet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class PredefinedCity implements ILostCityAsset {

    private final ResourceLocation name;
    private final ResourceKey<Level> dimension;
    private final int chunkX;
    private final int chunkZ;
    private final int radius;
    private final String cityStyle;
    private final List<PredefinedBuilding> predefinedBuildings = new ArrayList<>();
    private final List<PredefinedStreet> predefinedStreets = new ArrayList<>();

    public PredefinedCity(PredefinedCityRE object) {
        name = object.getRegistryName();
        dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(object.getDimension()));
        chunkX = object.getChunkX();
        chunkZ = object.getChunkZ();
        radius = object.getRadius();
        cityStyle = object.getCityStyle();
        if (object.getPredefinedBuildings() != null) {
            predefinedBuildings.addAll(object.getPredefinedBuildings());
        }
        if (object.getPredefinedStreets() != null) {
            predefinedStreets.addAll(object.getPredefinedStreets());
        }
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public int getRadius() {
        return radius;
    }

    public String getCityStyle() {
        return cityStyle;
    }

    public List<PredefinedBuilding> getPredefinedBuildings() {
        return predefinedBuildings;
    }

    public List<PredefinedStreet> getPredefinedStreets() {
        return predefinedStreets;
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
