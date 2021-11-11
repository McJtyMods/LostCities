package mcjty.lostcities.dimensions.world.lost;

import mcjty.lostcities.dimensions.world.LostCityChunkGenerator;
import mcjty.lostcities.varia.ChunkCoord;
import net.minecraft.world.biome.Biome;

import java.util.HashMap;
import java.util.Map;

public class BiomeInfo {

    private static Map<ChunkCoord, BiomeInfo> biomeInfoMap = new HashMap<>();

    private Biome[] biomesForBiomeCheck = null;


    public static void cleanCache() {
        biomeInfoMap.clear();
    }

    public static BiomeInfo getBiomeInfo(LostCityChunkGenerator provider, ChunkCoord coord) {
        if (!biomeInfoMap.containsKey(coord)) {
            BiomeInfo info = new BiomeInfo();
            int chunkX = coord.getChunkX();
            int chunkZ = coord.getChunkZ();
            info.biomesForBiomeCheck = provider.worldObj.getBiomeProvider().getBiomesForGeneration(info.biomesForBiomeCheck, chunkX * 4 - 2, chunkZ * 4 - 2, 10, 10);
            biomeInfoMap.put(coord, info);
        }
        return biomeInfoMap.get(coord);
    }

    public Biome[] getBiomes() {
        return biomesForBiomeCheck;
    }
}
