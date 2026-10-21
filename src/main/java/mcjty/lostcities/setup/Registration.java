package mcjty.lostcities.setup;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.worldgen.LostCityFeature;
import mcjty.lostcities.worldgen.LostCitySphereFeature;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.function.Supplier;

public class Registration {

    private static LostCityFeature lostCityFeature;
    private static LostCitySphereFeature lostCitySphereFeature;

    public static final Supplier<LostCityFeature> LOSTCITY_FEATURE = () -> lostCityFeature;
    public static final Supplier<LostCitySphereFeature> LOSTCITY_SPHERE_FEATURE = () -> lostCitySphereFeature;

    public static void init() {
        lostCityFeature = new LostCityFeature();
        lostCitySphereFeature = new LostCitySphereFeature();
        Registry.register(BuiltInRegistries.FEATURE_TYPE,
                Identifier.fromNamespaceAndPath(LostCities.MODID, "lostcity"), LostCityFeature.CODEC);
        Registry.register(BuiltInRegistries.FEATURE_TYPE,
                Identifier.fromNamespaceAndPath(LostCities.MODID, "spheres"), LostCitySphereFeature.CODEC);
    }

    public static final Identifier LOSTCITY = Identifier.fromNamespaceAndPath(LostCities.MODID, "lostcity");
    public static final ResourceKey<DimensionType> DIMENSION_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, LOSTCITY);
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION, LOSTCITY);
}
