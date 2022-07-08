package mcjty.lostcities.worldgen.lost.cityassets;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.api.ILostCityAsset;
import mcjty.lostcities.api.ILostCityAssetRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.CommonLevelAccessor;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class RegistryAssetRegistry<T extends ILostCityAsset, R extends IForgeRegistryEntry<R>> implements ILostCityAssetRegistry<T>  {

    private final Map<String, T> assets = new HashMap<>();
    private final List<String> assetNames = new ArrayList<>();
    private final ResourceKey<Registry<R>> registryKey;
    private final Function<R, T> assetConstructor;

    public <S extends ILostCityAsset> ILostCityAssetRegistry<S> cast() {
        return (ILostCityAssetRegistry<S>) this;
    }

    public RegistryAssetRegistry(ResourceKey<Registry<R>> registryKey, Function<R, T> assetConstructor) {
        this.registryKey = registryKey;
        this.assetConstructor = assetConstructor;
    }

    @Override
    public T get(CommonLevelAccessor level, String name) {
        if (name == null) {
            return null;
        }
        T t = assets.get(name);
        if (t == null) {
            Registry<R> registry = level.registryAccess().registryOrThrow(registryKey);
            R value = registry.get(ResourceKey.create(registryKey, new ResourceLocation(LostCities.MODID, name))); // @todo temporary
            value.setRegistryName(new ResourceLocation(LostCities.MODID, name));    // @todo needed?
            t = assetConstructor.apply(value);
            assets.put(name, t);
        }
        if (t != null) {
            t.init(level);
        }
        return t;
    }

    @Override
    public Iterable<T> getIterable() {
        return assets.values();
    }

    public void reset() {
        assets.clear();
    }
}
