package mcjty.lostcities.worldgen.lost;

import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.IDimensionInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

import java.util.HashMap;
import java.util.Map;

public class BiomeInfo {

    private static Map<ChunkCoord, BiomeInfo> biomeInfoMap = new HashMap<>();

    private Biome mainBiome;

    public static void cleanCache() {
        biomeInfoMap.clear();
    }

    public static BiomeInfo getBiomeInfo(IDimensionInfo provider, ChunkCoord coord) {
        if (!biomeInfoMap.containsKey(coord)) {
            BiomeInfo info = new BiomeInfo();
            int chunkX = coord.getChunkX();
            int chunkZ = coord.getChunkZ();
            info.mainBiome = provider.getBiome(new BlockPos((chunkX << 4) + 8, 65, (chunkZ << 4) + 8));
            if (info.mainBiome == null) {
                System.out.println("BiomeInfo.getBiomeInfo");
            }
            biomeInfoMap.put(coord, info);
        }
        return biomeInfoMap.get(coord);
    }

    public Biome getMainBiome() {
        return mainBiome;
    }
}
