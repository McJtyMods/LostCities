package mcjty.lostcities.setup;


import mcjty.lostcities.LostCities;
import mcjty.lostcities.commands.BuildingArgumentType;
import mcjty.lostcities.commands.PartArgumentType;
import mcjty.lostcities.worldgen.LostCityFeature;
import mcjty.lostcities.worldgen.LostCitySphereFeature;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Registration {

    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, LostCities.MODID);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, LostCities.MODID);

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        FEATURES.register(bus);
        ARGUMENT_TYPES.register(bus);
    }

    public static final RegistryObject<LostCityFeature> LOSTCITY_FEATURE = FEATURES.register("lostcity", LostCityFeature::new);
    public static final RegistryObject<LostCitySphereFeature> LOSTCITY_SPHERE_FEATURE = FEATURES.register("spheres", LostCitySphereFeature::new);

    public static final RegistryObject<ArgumentTypeInfo<?, ?>> BUILDING_ARGUMENT_TYPE = ARGUMENT_TYPES.register("building_argument_type",
            () -> ArgumentTypeInfos.registerByClass(BuildingArgumentType.class,
                    SingletonArgumentInfo.contextFree(BuildingArgumentType::building)));
    public static final RegistryObject<ArgumentTypeInfo<?, ?>> PART_ARGUMENT_TYPE = ARGUMENT_TYPES.register("part_argument_type",
            () -> ArgumentTypeInfos.registerByClass(PartArgumentType.class,
                    SingletonArgumentInfo.contextFree(PartArgumentType::part)));

    public static final ResourceLocation LOSTCITY = new ResourceLocation(LostCities.MODID, "lostcity");

    public static final ResourceKey<DimensionType> DIMENSION_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, LOSTCITY);
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION, LOSTCITY);
}
