package mcjty.lostcities.setup;


import mcjty.lostcities.LostCities;
import mcjty.lostcities.worldgen.LostCityFeature;
import mcjty.lostcities.worldgen.LostCitySphereFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class Registration {

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, LostCities.MODID);

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        FEATURES.register(bus);
    }

    public static final Supplier<LostCityFeature> LOSTCITY_FEATURE = FEATURES.register("lostcity", LostCityFeature::new);
    public static final Supplier<LostCitySphereFeature> LOSTCITY_SPHERE_FEATURE = FEATURES.register("spheres", LostCitySphereFeature::new);

    public static final ResourceLocation LOSTCITY = new ResourceLocation(LostCities.MODID, "lostcity");

    public static final ResourceKey<DimensionType> DIMENSION_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, LOSTCITY);
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION, LOSTCITY);
}
