package mcjty.lostcities.dimensions.world.lost.cityassets;

import com.google.gson.JsonObject;

public interface IAsset {

    String getName();

    void readFromJSon(JsonObject object);

    // Note that writing to json is just an approximization and way to quickly export code assets
    JsonObject writeToJSon();
}
