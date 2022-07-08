package mcjty.lostcities.api;

import com.google.gson.JsonObject;
import net.minecraft.world.level.CommonLevelAccessor;

public interface ILostCityAsset {

    // Called after the asset is fetched from the registry
    default void init(CommonLevelAccessor level) {}

    String getName();

    void readFromJSon(JsonObject object);
}
