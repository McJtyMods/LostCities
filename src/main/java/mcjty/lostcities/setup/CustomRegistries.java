package mcjty.lostcities.setup;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.worldgen.lost.regassets.BuildingRE;
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

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BUILDING_DEFERRED_REGISTER.register(bus);
    }

}
