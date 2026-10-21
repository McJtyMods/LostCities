package mcjty.lostcities.setup;


import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.LostCities;
import mcjty.lostcities.worldgen.LostCityFeature;
import mcjty.lostcities.worldgen.LostCitySphereFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class Registration {

    public static final DeferredRegister<MapCodec<? extends Feature>> FEATURES = DeferredRegister.create(Registries.FEATURE_TYPE, LostCities.MODID);
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, LostCities.MODID);

    private static final MapCodec<Boolean> BOOLEAN_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.fieldOf("value").forGetter(b -> b)
    ).apply(instance, (value) -> value));

    public static final Supplier<AttachmentType<Boolean>> ATTACHMENT_TYPE_SPAWNSET = ATTACHMENT_TYPES.register("spawnset", () -> AttachmentType.builder(() -> false)
            .serialize(BOOLEAN_CODEC)
            .copyOnDeath()
            .build());

    public static void init(IEventBus bus) {
        FEATURES.register(bus);
        ATTACHMENT_TYPES.register(bus);
    }

    public static final Supplier<LostCityFeature> LOSTCITY_FEATURE = Suppliers.memoize(LostCityFeature::new);
    private static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<LostCityFeature>> LOSTCITY_FEATURE_TYPE = FEATURES.register("lostcity", () -> LostCityFeature.CODEC);
    public static final Supplier<LostCitySphereFeature> LOSTCITY_SPHERE_FEATURE = Suppliers.memoize(LostCitySphereFeature::new);
    private static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<LostCitySphereFeature>> LOSTCITY_SPHERE_FEATURE_TYPE = FEATURES.register("spheres", () -> LostCitySphereFeature.CODEC);

    public static final Identifier LOSTCITY = Identifier.fromNamespaceAndPath(LostCities.MODID, "lostcity");

    public static final ResourceKey<DimensionType> DIMENSION_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, LOSTCITY);
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION, LOSTCITY);
}
