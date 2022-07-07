package mcjty.lostcities.worldgen.lost.cityassets;

import mcjty.lostcities.api.ILostCityAsset;
import mcjty.lostcities.api.ILostCityAssetRegistry;
import net.minecraft.world.level.CommonLevelAccessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbstractAssetRegistry<T extends ILostCityAsset> implements ILostCityAssetRegistry<T>  {

    private final Map<String, T> assets = new HashMap<>();
    private final List<String> assetNames = new ArrayList<>();

    public void register(T building) {
        assets.put(building.getName(), building);
        assetNames.add(building.getName());
    }

    public <S extends ILostCityAsset> ILostCityAssetRegistry<S> cast() {
        return (ILostCityAssetRegistry<S>) this;
    }

    @Override
    public T get(CommonLevelAccessor level, String name) {
        if (name == null) {
            return null;
        }
        T t = assets.get(name);
        if (t != null) {
            t.init();
        }
        return t;
    }

    public T get(int i) {
        T t = assets.get(assetNames.get(i));
        if (t != null) {
            t.init();
        }
        return t;
    }

    @Override
    public Iterable<T> getIterable() {
        return assets.values();
    }

    public int getCount() {
        return assets.size();
    }

    public String getName(int i) {
        return assetNames.get(i);
    }

    public void reset() {
        assets.clear();
    }
}
