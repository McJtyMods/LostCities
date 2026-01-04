package mcjty.lostcities.worldgen.lost;

import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.setup.Config;
import mcjty.lostcities.varia.TimedCache;
import mcjty.lostcities.worldgen.ChunkHeightmap;
import mcjty.lostcities.worldgen.IDimensionInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

public class BiomeInfo {

    private static final TimedCache<ChunkCoord, BiomeInfo> BIOME_INFO_CACHE = new TimedCache<>(Config.CACHE_CLEANUP_SECONDS::get);

    private Holder<Biome> mainBiome;

    public static void cleanCache() {
        BIOME_INFO_CACHE.clear();
    }

    public static BiomeInfo getBiomeInfo(IDimensionInfo provider, ChunkCoord coord) {
        BiomeInfo info = BIOME_INFO_CACHE.get(coord);
        if (info == null) {
            info = new BiomeInfo();
            ChunkHeightmap heightmap = provider.getHeightmap(coord);
            int chunkX = coord.chunkX();
            int chunkZ = coord.chunkZ();
            info.mainBiome = provider.getBiome(new BlockPos((chunkX << 4) + 8, heightmap.getHeight(), (chunkZ << 4) + 8));
            BIOME_INFO_CACHE.put(coord, info);
            return info;
        }
        return info;
    }

    public Holder<Biome> getMainBiome() {
        return mainBiome;
    }
}
