package mcjty.lostcities.api;

public interface ILostCityMultiBuilding extends ILostCityAsset {

    // Get the name of the building for the given chunk relative to the top-left chunk of the multibuilding
    String getBuilding(int x, int z);

    int getDimX();

    int getDimZ();
}
