package mcjty.lostcities.api;

import com.google.gson.JsonObject;

public interface ILostCityAsset {

    // Called after the asset is fetched from the registry
    default void init() {}

    String getName();

    void readFromJSon(JsonObject object);
}
