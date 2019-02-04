package mcjty.lostcities.dimensions.world.driver;

public interface IIndex {

    void decY();

    void incX();

    void incY();
    void incY(int amount);

    void incZ();

    IIndex up();

    IIndex down();

    IIndex north();

    IIndex west();

    IIndex south();

    IIndex east();

    int getX();

    int getY();

    int getZ();

    IIndex copy();
}
