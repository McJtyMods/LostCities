package mcjty.lostcities.worldgen.lost.cityassets;

import com.google.gson.JsonObject;
import mcjty.lostcities.LostCities;
import mcjty.lostcities.api.ILostCityAsset;
import mcjty.lostcities.worldgen.lost.regassets.data.DataTools;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class PredefinedSphere implements ILostCityAsset {

    private ResourceLocation name;
    private ResourceKey<Level> dimension;
    private int chunkX;
    private int chunkZ;
    private int centerX;
    private int centerZ;
    private int radius;
    private String biome;

    public PredefinedSphere(JsonObject object) {
        readFromJSon(object);
    }

    @Override
    public String getName() {
        return DataTools.toName(name);
    }

    @Override
    public ResourceLocation getId() {
        return name;
    }

    public void readFromJSon(JsonObject object) {
        name = new ResourceLocation(LostCities.MODID, object.get("name").getAsString());    // @todo temporary
        dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(object.get("dimension").getAsString()));
        chunkX = object.get("chunkx").getAsInt();
        chunkZ = object.get("chunkz").getAsInt();
        centerX = object.get("centerx").getAsInt();
        centerZ = object.get("centerz").getAsInt();
        radius = object.get("radius").getAsInt();
        if (object.has("biome")) {
            biome = object.get("biome").getAsString();
        } else {
            biome = null;
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

    public int getCenterX() {
        return centerX;
    }

    public int getCenterZ() {
        return centerZ;
    }

    public int getRadius() {
        return radius;
    }

    public String getBiome() {
        return biome;
    }
}