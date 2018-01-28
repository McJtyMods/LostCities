package mcjty.lostcities.dimensions.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class LostWorldSingleBiomeProvider extends BiomeProvider {
    /**
     * The biome generator object.
     */
    private final Biome biome;

    public LostWorldSingleBiomeProvider(Biome biomeIn) {
        this.biome = biomeIn;
    }

    /**
     * Returns the biome generator
     */

    @Override
    public Biome getBiome(BlockPos pos) {
        return this.biome;
    }

    @Override
    public Biome[] getBiomesForGeneration(Biome[] biomes, int x, int z, int width, int height) {
        if (biomes == null || biomes.length < width * height) {
            biomes = new Biome[width * height];
        }

        Arrays.fill(biomes, 0, width * height, this.biome);
        return biomes;
    }

    @Override
    public Biome[] getBiomes(@Nullable Biome[] oldBiomeList, int x, int z, int width, int depth) {
        if (oldBiomeList == null || oldBiomeList.length < width * depth) {
            oldBiomeList = new Biome[width * depth];
        }

        Arrays.fill(oldBiomeList, 0, width * depth, this.biome);
        return oldBiomeList;
    }

    @Override
    public Biome[] getBiomes(@Nullable Biome[] listToReuse, int x, int z, int width, int length, boolean cacheFlag) {
        return this.getBiomes(listToReuse, x, z, width, length);
    }

    @Override
    @Nullable
    public BlockPos findBiomePosition(int x, int z, int range, List<Biome> biomes, Random random) {
        return biomes.contains(this.biome) ? new BlockPos(x - range + random.nextInt(range * 2 + 1), 0, z - range + random.nextInt(range * 2 + 1)) : null;
    }

    @Override
    public boolean areBiomesViable(int x, int z, int radius, List<Biome> allowed) {
        return allowed.contains(this.biome);
    }

    @Override
    public boolean isFixedBiome() {
        return true;
    }

    @Override
    public Biome getFixedBiome() {
        return this.biome;
    }
}