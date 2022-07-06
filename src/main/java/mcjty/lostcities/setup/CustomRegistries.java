package mcjty.lostcities.setup;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.worldgen.lost.regassets.BuildingRE;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class CustomRegistries {

    public static final DeferredRegister<BuildingRE> BUILDING_DEFERRED_REGISTER = DeferredRegister.create(new ResourceLocation(LostCities.MODID, "buildings"), LostCities.MODID);
    public static final Supplier<IForgeRegistry<BuildingRE>> BUILDING_REGISTRY = BUILDING_DEFERRED_REGISTER.makeRegistry(BuildingRE.class, () -> new RegistryBuilder<BuildingRE>().dataPackRegistry(BuildingRE.CODEC));

    public static void init() {

    }

}
