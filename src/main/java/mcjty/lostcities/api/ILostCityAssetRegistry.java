package mcjty.lostcities.api;

public interface ILostCityAssetRegistry<T extends ILostCityAsset> {

    T get(String name);

    Iterable<T> getIterable();
}
