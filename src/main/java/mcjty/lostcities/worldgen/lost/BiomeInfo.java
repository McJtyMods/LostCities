package mcjty.lostcities.worldgen.lost;

import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.Tools;
import mcjty.lostcities.worldgen.ChunkHeightmap;
import mcjty.lostcities.worldgen.IDimensionInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;

import java.util.HashMap;
import java.util.Map;

public class BiomeInfo {

    private static final Map<ChunkCoord, BiomeInfo> BIOME_INFO_MAP = new HashMap<>();

    private Holder<Biome> mainBiome;

    public static void cleanCache() {
        BIOME_INFO_MAP.clear();
    }

    private static void cleanCacheUnloaded(WorldGenLevel level, ResourceKey<Level> dimension) {
        Tools.cleanCacheMap(level, dimension, BIOME_INFO_MAP);
    }

    private static int cleanCacheCounter = Tools.CACHE_CLEANUP_TIMER;

    public static BiomeInfo getBiomeInfo(IDimensionInfo provider, ChunkCoord coord) {
        cleanCacheCounter--;
        if (cleanCacheCounter < 0) {
            cleanCacheCounter = Tools.CACHE_CLEANUP_TIMER;
            cleanCacheUnloaded(provider.getWorld(), provider.dimension());
        }
        if (!BIOME_INFO_MAP.containsKey(coord)) {
            BiomeInfo info = new BiomeInfo();
            ChunkHeightmap heightmap = provider.getHeightmap(coord);
            int chunkX = coord.chunkX();
            int chunkZ = coord.chunkZ();
            info.mainBiome = provider.getBiome(new BlockPos((chunkX << 4) + 8, heightmap.getHeight(), (chunkZ << 4) + 8));
            BIOME_INFO_MAP.put(coord, info);
        }
        return BIOME_INFO_MAP.get(coord);
    }

    public Holder<Biome> getMainBiome() {
        return mainBiome;
    }
}
