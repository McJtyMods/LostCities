package mcjty.lostcities.dimensions.world.lost.data;

import mcjty.lostcities.dimensions.world.lost.BuildingInfo;
import mcjty.lostcities.dimensions.world.lost.LostCitiesTerrainGenerator;
import net.minecraft.block.state.IBlockState;

public class Level {
    private final String[] floor;

    public Level(String[] floor) {
        this.floor = floor;
    }

    public String[] getFloor() {
        return floor;
    }

    public IBlockState get(BuildingInfo info, int x, int y, int z) {
//        return LostCitiesTerrainGenerator.getMapping().get(floor[y].charAt(z * 16 + x)).apply(info);
        return LostCitiesTerrainGenerator.getPalette().get(floor[y].charAt(z * 16 + x), info);
    }

    public Character getC(int x, int y, int z) {
        return floor[y].charAt(z * 16 + x);
    }
}
