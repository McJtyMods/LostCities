package mcjty.lostcities.worldgen.lost;

import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.IDimensionInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

import java.util.HashMap;
import java.util.Map;

public class BiomeInfo {

    private static final Map<ChunkCoord, BiomeInfo> BIOME_INFO_MAP = new HashMap<>();

    private Holder<Biome> mainBiome;

    public static void cleanCache() {
        BIOME_INFO_MAP.clear();
    }

    public static BiomeInfo getBiomeInfo(IDimensionInfo provider, ChunkCoord coord) {
        if (!BIOME_INFO_MAP.containsKey(coord)) {
            BiomeInfo info = new BiomeInfo();
            int chunkX = coord.chunkX();
            int chunkZ = coord.chunkZ();
            info.mainBiome = provider.getBiome(new BlockPos((chunkX << 4) + 8, 65, (chunkZ << 4) + 8));
            BIOME_INFO_MAP.put(coord, info);
        }
        return BIOME_INFO_MAP.get(coord);
    }

    public Holder<Biome> getMainBiome() {
        return mainBiome;
    }
}
