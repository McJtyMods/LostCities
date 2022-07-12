package mcjty.lostcities.worldgen.lost;

import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.IDimensionInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

import java.util.HashMap;
import java.util.Map;

public class BiomeInfo {

    private static final Map<ChunkCoord, BiomeInfo> biomeInfoMap = new HashMap<>();

    private Holder<Biome> mainBiome;

    public static void cleanCache() {
        biomeInfoMap.clear();
    }

    public static BiomeInfo getBiomeInfo(IDimensionInfo provider, ChunkCoord coord) {
        if (!biomeInfoMap.containsKey(coord)) {
            BiomeInfo info = new BiomeInfo();
            int chunkX = coord.chunkX();
            int chunkZ = coord.chunkZ();
            info.mainBiome = provider.getBiome(new BlockPos((chunkX << 4) + 8, 65, (chunkZ << 4) + 8));
            biomeInfoMap.put(coord, info);
        }
        return biomeInfoMap.get(coord);
    }

    public static BiomeInfo getBiomeInfo(IDimensionInfo provider, int chunkX, int chunkZ) {
        return getBiomeInfo(provider, new ChunkCoord(provider.dimension(), chunkX, chunkZ));
    }

    public Holder<Biome> getMainBiome() {
        return mainBiome;
    }
}
