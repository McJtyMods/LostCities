package mcjty.lostcities.api;

import net.minecraft.world.level.CommonLevelAccessor;

public interface ILostCityAssetRegistry<T extends ILostCityAsset> {

    T get(CommonLevelAccessor level, String name);

    Iterable<T> getIterable();
}
