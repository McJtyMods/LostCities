package mcjty.lostcities.worldgen.lost.regassets;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

public class CityStyleRE implements IForgeRegistryEntry<CityStyleRE> {

    private ResourceLocation name;

    public static class StreetSettings {
        private Integer streetWidth;
        private Character streetBlock;
        private Character streetBaseBlock;
        private Character streetVariantBlock;
        private Character borderBlock;
        private Character wallBlock;
    }

    public static class ParkSettings {
        private Character parkElevationBlock;
        private Character grassBlock;
    }

    public static class CorridorSettings {
        private Character corridorRoofBlock;
        private Character corridorGlassBlock;
    }

    public static class GeneralSettings {
        private Character ironbarsBlock;
        private Character glowstoneBlock;
    }

    public static class RailSettings {
        private Character railMainBlock;
    }

    public static class SphereSettings {
        private Character sphereBlock;          // Used for 'space' landscape type
        private Character sphereSideBlock;      // Used for 'space' landscape type
        private Character sphereGlassBlock;     // Used for 'space' landscape type
    }

    private Float explosionChance;
    private String style;
    private String inherit;

    public CityStyleRE() {
    }

    @Override
    public CityStyleRE setRegistryName(ResourceLocation name) {
        this.name = name;
        return this;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return name;
    }

    @Override
    public Class<CityStyleRE> getRegistryType() {
        return CityStyleRE.class;
    }
}
