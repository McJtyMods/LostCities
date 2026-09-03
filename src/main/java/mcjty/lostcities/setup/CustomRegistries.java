package mcjty.lostcities.setup;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.worldgen.lost.regassets.*;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public class CustomRegistries {

    public static final ResourceKey<Registry<BuildingRE>> BUILDING_REGISTRY_KEY = key("buildings");
    public static final ResourceKey<Registry<PaletteRE>> PALETTE_REGISTRY_KEY = key("palettes");
    public static final ResourceKey<Registry<BuildingPartRE>> PART_REGISTRY_KEY = key("parts");
    public static final ResourceKey<Registry<StyleRE>> STYLE_REGISTRY_KEY = key("styles");
    public static final ResourceKey<Registry<ConditionRE>> CONDITIONS_REGISTRY_KEY = key("conditions");
    public static final ResourceKey<Registry<CityStyleRE>> CITYSTYLES_REGISTRY_KEY = key("citystyles");
    public static final ResourceKey<Registry<MultiBuildingRE>> MULTIBUILDINGS_REGISTRY_KEY = key("multibuildings");
    public static final ResourceKey<Registry<VariantRE>> VARIANTS_REGISTRY_KEY = key("variants");
    public static final ResourceKey<Registry<WorldStyleRE>> WORLDSTYLES_REGISTRY_KEY = key("worldstyles");
    public static final ResourceKey<Registry<PredefinedCityRE>> PREDEFINEDCITIES_REGISTRY_KEY = key("predefinedcities");
    public static final ResourceKey<Registry<PredefinedSphereRE>> PREDEFINEDSPHERES_REGISTRY_KEY = key("predefinedspheres");
    public static final ResourceKey<Registry<ScatteredRE>> SCATTERED_REGISTRY_KEY = key("scattered");
    public static final ResourceKey<Registry<StuffSettingsRE>> STUFF_REGISTRY_KEY = key("stuff");

    private static <T> ResourceKey<Registry<T>> key(String name) {
        return ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(LostCities.MODID, name));
    }

    public static void init() {
        DynamicRegistries.register(BUILDING_REGISTRY_KEY, BuildingRE.CODEC);
        DynamicRegistries.register(PALETTE_REGISTRY_KEY, PaletteRE.CODEC);
        DynamicRegistries.register(PART_REGISTRY_KEY, BuildingPartRE.CODEC);
        DynamicRegistries.register(STYLE_REGISTRY_KEY, StyleRE.CODEC);
        DynamicRegistries.register(CONDITIONS_REGISTRY_KEY, ConditionRE.CODEC);
        DynamicRegistries.register(CITYSTYLES_REGISTRY_KEY, CityStyleRE.CODEC);
        DynamicRegistries.register(MULTIBUILDINGS_REGISTRY_KEY, MultiBuildingRE.CODEC);
        DynamicRegistries.register(VARIANTS_REGISTRY_KEY, VariantRE.CODEC);
        DynamicRegistries.register(WORLDSTYLES_REGISTRY_KEY, WorldStyleRE.CODEC);
        DynamicRegistries.register(PREDEFINEDCITIES_REGISTRY_KEY, PredefinedCityRE.CODEC);
        DynamicRegistries.register(PREDEFINEDSPHERES_REGISTRY_KEY, PredefinedSphereRE.CODEC);
        DynamicRegistries.register(SCATTERED_REGISTRY_KEY, ScatteredRE.CODEC);
        DynamicRegistries.register(STUFF_REGISTRY_KEY, StuffSettingsRE.CODEC);
    }
}
