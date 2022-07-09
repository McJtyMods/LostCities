package mcjty.lostcities.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.CommonLevelAccessor;

public interface ILostCityAssetRegistry<T extends ILostCityAsset> {

    T get(CommonLevelAccessor level, String name);

    T get(CommonLevelAccessor level, ResourceLocation name);

    Iterable<T> getIterable();
}
