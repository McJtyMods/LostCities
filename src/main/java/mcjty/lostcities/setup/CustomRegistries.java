package mcjty.lostcities.setup;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.worldgen.lost.regassets.*;
import mcjty.lostcities.worldgen.lost.regassets.StuffSettingsRE;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class CustomRegistries {

    public static final ResourceKey<Registry<BuildingRE>> BUILDING_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostCities.MODID, "buildings"));
    public static final DeferredRegister<BuildingRE> BUILDING_DEFERRED_REGISTER = DeferredRegister.create(BUILDING_REGISTRY_KEY, LostCities.MODID);
    public static final Supplier<IForgeRegistry<BuildingRE>> BUILDING_REGISTRY = BUILDING_DEFERRED_REGISTER.makeRegistry(() -> new RegistryBuilder<BuildingRE>().dataPackRegistry(BuildingRE.CODEC));

    public static final ResourceKey<Registry<PaletteRE>> PALETTE_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostCities.MODID, "palettes"));
    public static final DeferredRegister<PaletteRE> PALETTE_DEFERRED_REGISTER = DeferredRegister.create(PALETTE_REGISTRY_KEY, LostCities.MODID);
    public static final Supplier<IForgeRegistry<PaletteRE>> PALETTE_REGISTRY = PALETTE_DEFERRED_REGISTER.makeRegistry(() -> new RegistryBuilder<PaletteRE>().dataPackRegistry(PaletteRE.CODEC));

    public static final ResourceKey<Registry<BuildingPartRE>> PART_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostCities.MODID, "parts"));
    public static final DeferredRegister<BuildingPartRE> PART_DEFERRED_REGISTER = DeferredRegister.create(PART_REGISTRY_KEY, LostCities.MODID);
    public static final Supplier<IForgeRegistry<BuildingPartRE>> PART_REGISTRY = PART_DEFERRED_REGISTER.makeRegistry(() -> new RegistryBuilder<BuildingPartRE>().dataPackRegistry(BuildingPartRE.CODEC));

    public static final ResourceKey<Registry<StyleRE>> STYLE_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostCities.MODID, "styles"));
    public static final DeferredRegister<StyleRE> STYLE_DEFERRED_REGISTER = DeferredRegister.create(STYLE_REGISTRY_KEY, LostCities.MODID);
    public static final Supplier<IForgeRegistry<StyleRE>> STYLE_REGISTRY = STYLE_DEFERRED_REGISTER.makeRegistry(() -> new RegistryBuilder<StyleRE>().dataPackRegistry(StyleRE.CODEC));

    public static final ResourceKey<Registry<ConditionRE>> CONDITIONS_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostCities.MODID, "conditions"));
    public static final DeferredRegister<ConditionRE> CONDITIONS_DEFERRED_REGISTER = DeferredRegister.create(CONDITIONS_REGISTRY_KEY, LostCities.MODID);
    public static final Supplier<IForgeRegistry<ConditionRE>> CONDITIONS_REGISTRY = CONDITIONS_DEFERRED_REGISTER.makeRegistry(() -> new RegistryBuilder<ConditionRE>().dataPackRegistry(ConditionRE.CODEC));

    public static final ResourceKey<Registry<CityStyleRE>> CITYSTYLES_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostCities.MODID, "citystyles"));
    public static final DeferredRegister<CityStyleRE> CITYSTYLES_DEFERRED_REGISTER = DeferredRegister.create(CITYSTYLES_REGISTRY_KEY, LostCities.MODID);
    public static final Supplier<IForgeRegistry<CityStyleRE>> CITYSTYLES_REGISTRY = CITYSTYLES_DEFERRED_REGISTER.makeRegistry(() -> new RegistryBuilder<CityStyleRE>().dataPackRegistry(CityStyleRE.CODEC));

    public static final ResourceKey<Registry<MultiBuildingRE>> MULTIBUILDINGS_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostCities.MODID, "multibuildings"));
    public static final DeferredRegister<MultiBuildingRE> MULTIBUILDINGS_DEFERRED_REGISTER = DeferredRegister.create(MULTIBUILDINGS_REGISTRY_KEY, LostCities.MODID);
    public static final Supplier<IForgeRegistry<MultiBuildingRE>> MULTIBUILDINGS_REGISTRY = MULTIBUILDINGS_DEFERRED_REGISTER.makeRegistry(() -> new RegistryBuilder<MultiBuildingRE>().dataPackRegistry(MultiBuildingRE.CODEC));

    public static final ResourceKey<Registry<VariantRE>> VARIANTS_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostCities.MODID, "variants"));
    public static final DeferredRegister<VariantRE> VARIANTS_DEFERRED_REGISTER = DeferredRegister.create(VARIANTS_REGISTRY_KEY, LostCities.MODID);
    public static final Supplier<IForgeRegistry<VariantRE>> VARIANTS_REGISTRY = VARIANTS_DEFERRED_REGISTER.makeRegistry(() -> new RegistryBuilder<VariantRE>().dataPackRegistry(VariantRE.CODEC));

    public static final ResourceKey<Registry<WorldStyleRE>> WORLDSTYLES_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostCities.MODID, "worldstyles"));
    public static final DeferredRegister<WorldStyleRE> WORLDSTYLES_DEFERRED_REGISTER = DeferredRegister.create(WORLDSTYLES_REGISTRY_KEY, LostCities.MODID);
    public static final Supplier<IForgeRegistry<WorldStyleRE>> WORLDSTYLES_REGISTRY = WORLDSTYLES_DEFERRED_REGISTER.makeRegistry(() -> new RegistryBuilder<WorldStyleRE>().dataPackRegistry(WorldStyleRE.CODEC));

    public static final ResourceKey<Registry<PredefinedCityRE>> PREDEFINEDCITIES_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostCities.MODID, "predefinedcites"));
    public static final DeferredRegister<PredefinedCityRE> PREDEFINEDCITIES_DEFERRED_REGISTER = DeferredRegister.create(PREDEFINEDCITIES_REGISTRY_KEY, LostCities.MODID);
    public static final Supplier<IForgeRegistry<PredefinedCityRE>> PREDEFINEDCITIES_REGISTRY = PREDEFINEDCITIES_DEFERRED_REGISTER.makeRegistry(() -> new RegistryBuilder<PredefinedCityRE>().dataPackRegistry(PredefinedCityRE.CODEC));

    public static final ResourceKey<Registry<PredefinedSphereRE>> PREDEFINEDSPHERES_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostCities.MODID, "predefinedspheres"));
    public static final DeferredRegister<PredefinedSphereRE> PREDEFINEDSPHERES_DEFERRED_REGISTER = DeferredRegister.create(PREDEFINEDSPHERES_REGISTRY_KEY, LostCities.MODID);
    public static final Supplier<IForgeRegistry<PredefinedSphereRE>> PREDEFINEDSPHERES_REGISTRY = PREDEFINEDSPHERES_DEFERRED_REGISTER.makeRegistry(() -> new RegistryBuilder<PredefinedSphereRE>().dataPackRegistry(PredefinedSphereRE.CODEC));


    public static final ResourceKey<Registry<ScatteredRE>> SCATTERED_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostCities.MODID, "scattered"));
    public static final DeferredRegister<ScatteredRE> SCATTERED_DEFERRED_REGISTER = DeferredRegister.create(SCATTERED_REGISTRY_KEY, LostCities.MODID);
    public static final Supplier<IForgeRegistry<ScatteredRE>> SCATTERED_REGISTRY = SCATTERED_DEFERRED_REGISTER.makeRegistry(() -> new RegistryBuilder<ScatteredRE>().dataPackRegistry(ScatteredRE.CODEC));

    public static final ResourceKey<Registry<StuffSettingsRE>> STUFF_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostCities.MODID, "stuff"));
    public static final DeferredRegister<StuffSettingsRE> STUFF_DEFERRED_REGISTER = DeferredRegister.create(STUFF_REGISTRY_KEY, LostCities.MODID);

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
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

}
