package mcjty.lostcities.setup;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.worldgen.lost.regassets.*;
import mcjty.lostcities.worldgen.lost.regassets.StuffSettingsRE;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.minecraftforge.registries.DeferredRegister;

public class CustomRegistries {

    public static final ResourceKey<Registry<BuildingRE>> BUILDING_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostCities.MODID, "buildings"));
    public static final DeferredRegister<BuildingRE> BUILDING_DEFERRED_REGISTER = DeferredRegister.create(BUILDING_REGISTRY_KEY, LostCities.MODID);

    public static final ResourceKey<Registry<PaletteRE>> PALETTE_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostCities.MODID, "palettes"));
    public static final DeferredRegister<PaletteRE> PALETTE_DEFERRED_REGISTER = DeferredRegister.create(PALETTE_REGISTRY_KEY, LostCities.MODID);

    public static final ResourceKey<Registry<BuildingPartRE>> PART_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostCities.MODID, "parts"));
    public static final DeferredRegister<BuildingPartRE> PART_DEFERRED_REGISTER = DeferredRegister.create(PART_REGISTRY_KEY, LostCities.MODID);

    public static final ResourceKey<Registry<StyleRE>> STYLE_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostCities.MODID, "styles"));
    public static final DeferredRegister<StyleRE> STYLE_DEFERRED_REGISTER = DeferredRegister.create(STYLE_REGISTRY_KEY, LostCities.MODID);

    public static final ResourceKey<Registry<ConditionRE>> CONDITIONS_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostCities.MODID, "conditions"));
    public static final DeferredRegister<ConditionRE> CONDITIONS_DEFERRED_REGISTER = DeferredRegister.create(CONDITIONS_REGISTRY_KEY, LostCities.MODID);

    public static final ResourceKey<Registry<CityStyleRE>> CITYSTYLES_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostCities.MODID, "citystyles"));
    public static final DeferredRegister<CityStyleRE> CITYSTYLES_DEFERRED_REGISTER = DeferredRegister.create(CITYSTYLES_REGISTRY_KEY, LostCities.MODID);

    public static final ResourceKey<Registry<MultiBuildingRE>> MULTIBUILDINGS_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostCities.MODID, "multibuildings"));
    public static final DeferredRegister<MultiBuildingRE> MULTIBUILDINGS_DEFERRED_REGISTER = DeferredRegister.create(MULTIBUILDINGS_REGISTRY_KEY, LostCities.MODID);

    public static final ResourceKey<Registry<VariantRE>> VARIANTS_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostCities.MODID, "variants"));
    public static final DeferredRegister<VariantRE> VARIANTS_DEFERRED_REGISTER = DeferredRegister.create(VARIANTS_REGISTRY_KEY, LostCities.MODID);

    public static final ResourceKey<Registry<WorldStyleRE>> WORLDSTYLES_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostCities.MODID, "worldstyles"));
    public static final DeferredRegister<WorldStyleRE> WORLDSTYLES_DEFERRED_REGISTER = DeferredRegister.create(WORLDSTYLES_REGISTRY_KEY, LostCities.MODID);

    public static final ResourceKey<Registry<PredefinedCityRE>> PREDEFINEDCITIES_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostCities.MODID, "predefinedcites"));
    public static final DeferredRegister<PredefinedCityRE> PREDEFINEDCITIES_DEFERRED_REGISTER = DeferredRegister.create(PREDEFINEDCITIES_REGISTRY_KEY, LostCities.MODID);

    public static final ResourceKey<Registry<PredefinedSphereRE>> PREDEFINEDSPHERES_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostCities.MODID, "predefinedspheres"));
    public static final DeferredRegister<PredefinedSphereRE> PREDEFINEDSPHERES_DEFERRED_REGISTER = DeferredRegister.create(PREDEFINEDSPHERES_REGISTRY_KEY, LostCities.MODID);

    public static final ResourceKey<Registry<ScatteredRE>> SCATTERED_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostCities.MODID, "scattered"));
    public static final DeferredRegister<ScatteredRE> SCATTERED_DEFERRED_REGISTER = DeferredRegister.create(SCATTERED_REGISTRY_KEY, LostCities.MODID);

    public static final ResourceKey<Registry<StuffSettingsRE>> STUFF_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostCities.MODID, "stuff"));
    public static final DeferredRegister<StuffSettingsRE> STUFF_DEFERRED_REGISTER = DeferredRegister.create(STUFF_REGISTRY_KEY, LostCities.MODID);

    public static void init(IEventBus bus) {
        BUILDING_DEFERRED_REGISTER.register(bus);
        PALETTE_DEFERRED_REGISTER.register(bus);
        PART_DEFERRED_REGISTER.register(bus);
        STYLE_DEFERRED_REGISTER.register(bus);
        CONDITIONS_DEFERRED_REGISTER.register(bus);
        CITYSTYLES_DEFERRED_REGISTER.register(bus);
        MULTIBUILDINGS_DEFERRED_REGISTER.register(bus);
        VARIANTS_DEFERRED_REGISTER.register(bus);
        WORLDSTYLES_DEFERRED_REGISTER.register(bus);
        PREDEFINEDCITIES_DEFERRED_REGISTER.register(bus);
        PREDEFINEDSPHERES_DEFERRED_REGISTER.register(bus);
        SCATTERED_DEFERRED_REGISTER.register(bus);
        STUFF_DEFERRED_REGISTER.register(bus);
    }

    public static void onDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(BUILDING_REGISTRY_KEY, BuildingRE.CODEC);
        event.dataPackRegistry(PALETTE_REGISTRY_KEY, PaletteRE.CODEC);
        event.dataPackRegistry(PART_REGISTRY_KEY, BuildingPartRE.CODEC);
        event.dataPackRegistry(STYLE_REGISTRY_KEY, StyleRE.CODEC);
        event.dataPackRegistry(CONDITIONS_REGISTRY_KEY, ConditionRE.CODEC);
        event.dataPackRegistry(CITYSTYLES_REGISTRY_KEY, CityStyleRE.CODEC);
        event.dataPackRegistry(MULTIBUILDINGS_REGISTRY_KEY, MultiBuildingRE.CODEC);
        event.dataPackRegistry(VARIANTS_REGISTRY_KEY, VariantRE.CODEC);
        event.dataPackRegistry(WORLDSTYLES_REGISTRY_KEY, WorldStyleRE.CODEC);
        event.dataPackRegistry(PREDEFINEDCITIES_REGISTRY_KEY, PredefinedCityRE.CODEC);
        event.dataPackRegistry(PREDEFINEDSPHERES_REGISTRY_KEY, PredefinedSphereRE.CODEC);
        event.dataPackRegistry(SCATTERED_REGISTRY_KEY, ScatteredRE.CODEC);
        event.dataPackRegistry(STUFF_REGISTRY_KEY, StuffSettingsRE.CODEC);
    }
}
