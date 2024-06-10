package mcjty.lostcities.worldgen.lost.cityassets;

import mcjty.lostcities.api.ILostCityAsset;
import mcjty.lostcities.api.ILostCityAssetRegistry;
import mcjty.lostcities.worldgen.lost.regassets.IAsset;
import mcjty.lostcities.worldgen.lost.regassets.data.DataTools;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.CommonLevelAccessor;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class RegistryAssetRegistry<T extends ILostCityAsset, R> implements ILostCityAssetRegistry<T>  {

    private final Map<ResourceLocation, T> assets = new HashMap<>();
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
        return get(level, DataTools.fromName(name));
    }

    @Nonnull
    public T getOrThrow(CommonLevelAccessor level, String name) {
        if (name == null) {
            throw new RuntimeException("Invalid name given to " + registryKey.registry() + " getOrThrow!");
        }
        T result = get(level, DataTools.fromName(name));
        if (result == null) {
            throw new RuntimeException("Can't find '" + name + "' in " + registryKey.registry() + "!");
        }
        return result;
    }

    @Override
    public T get(CommonLevelAccessor level, ResourceLocation name) {
        if (name == null) {
            return null;
        }
        T t = assets.get(name);
        if (t == null) {
            try {
                Registry<R> registry = level.registryAccess().registryOrThrow(registryKey);
                R value = registry.get(ResourceKey.create(registryKey, name));
                if (value instanceof IAsset asset) {
                    asset.setRegistryName(name);
                }
                t = assetConstructor.apply(value);
            } catch (Exception e) {
                throw new RuntimeException("Error getting resource " + name + "!", e);
            }
            assets.put(name, t);
        }
        if (t != null) {
            t.init(level);
        }
        return t;
    }

    public void loadAll(CommonLevelAccessor level) {
        Registry<R> registry = level.registryAccess().registryOrThrow(registryKey);
        for (R r : registry) {
            ResourceLocation name = registry.getKey(r);
            if (!assets.containsKey(name)) {
                if (r instanceof IAsset asset) {
                    asset.setRegistryName(name);
                }
                T t = assetConstructor.apply(r);
                assets.put(name, t);
            }
        }
    }

    @Override
    public Iterable<T> getIterable() {
        return assets.values();
    }

    public int getNumAssets(CommonLevelAccessor level) {
        return level.registryAccess().registryOrThrow(registryKey).size();
    }

    public void reset() {
        assets.clear();
    }
}
