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

    private final Holder<Biome> mainBiome;

    private BiomeInfo(Holder<Biome> mainBiome) {
        this.mainBiome = mainBiome;
    }

    public static void cleanCache() {
        BIOME_INFO_CACHE.clear();
    }

    public static BiomeInfo getBiomeInfo(IDimensionInfo provider, ChunkCoord coord) {
        return BIOME_INFO_CACHE.computeIfAbsent(coord, key -> {
            ChunkHeightmap heightmap = provider.getHeightmap(key);
            int chunkX = key.chunkX();
            int chunkZ = key.chunkZ();
            Holder<Biome> biome = provider.getBiome(new BlockPos((chunkX << 4) + 8, heightmap.getHeight(), (chunkZ << 4) + 8));
            return new BiomeInfo(biome);
        });
    }

    public Holder<Biome> getMainBiome() {
        return mainBiome;
    }
}
