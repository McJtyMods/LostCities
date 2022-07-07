package mcjty.lostcities.setup;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.worldgen.lost.regassets.BuildingPartRE;
import mcjty.lostcities.worldgen.lost.regassets.BuildingRE;
import mcjty.lostcities.worldgen.lost.regassets.PaletteRE;
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
    public static final Supplier<IForgeRegistry<BuildingRE>> BUILDING_REGISTRY = BUILDING_DEFERRED_REGISTER.makeRegistry(BuildingRE.class, () -> new RegistryBuilder<BuildingRE>().dataPackRegistry(BuildingRE.CODEC));

    public static final ResourceKey<Registry<PaletteRE>> PALETTE_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostCities.MODID, "palettes"));
    public static final DeferredRegister<PaletteRE> PALETTE_DEFERRED_REGISTER = DeferredRegister.create(PALETTE_REGISTRY_KEY, LostCities.MODID);
    public static final Supplier<IForgeRegistry<PaletteRE>> PALETTE_REGISTRY = PALETTE_DEFERRED_REGISTER.makeRegistry(PaletteRE.class, () -> new RegistryBuilder<PaletteRE>().dataPackRegistry(PaletteRE.CODEC));

    public static final ResourceKey<Registry<BuildingPartRE>> PART_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostCities.MODID, "parts"));
    public static final DeferredRegister<BuildingPartRE> PART_DEFERRED_REGISTER = DeferredRegister.create(PART_REGISTRY_KEY, LostCities.MODID);
    public static final Supplier<IForgeRegistry<BuildingPartRE>> PART_REGISTRY = PART_DEFERRED_REGISTER.makeRegistry(BuildingPartRE.class, () -> new RegistryBuilder<BuildingPartRE>().dataPackRegistry(BuildingPartRE.CODEC));

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BUILDING_DEFERRED_REGISTER.register(bus);
        PALETTE_DEFERRED_REGISTER.register(bus);
        PART_DEFERRED_REGISTER.register(bus);
    }

}
