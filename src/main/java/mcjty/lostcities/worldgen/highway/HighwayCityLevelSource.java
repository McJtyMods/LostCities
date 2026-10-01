package mcjty.lostcities.worldgen.highway;

import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.plan.ChunkPlanner;

public final class HighwayCityLevelSource implements HighwayLevelSource {
    private final IDimensionInfo provider;

    public HighwayCityLevelSource(IDimensionInfo provider) {
        this.provider = provider;
    }

    @Override
    public int getCityLevel(int chunkX, int chunkZ) {
        return ChunkPlanner.cityLevel(new ChunkCoord(provider.getType(), chunkX, chunkZ), provider);
    }
}
