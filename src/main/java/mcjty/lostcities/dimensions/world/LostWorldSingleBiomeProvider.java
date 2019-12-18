package mcjty.lostcities.dimensions.world;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.feature.structure.Structure;

import javax.annotation.Nullable;
import java.util.*;

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
    public Biome[] getBiomes(int x, int z, int width, int length, boolean cacheFlag) {
        // @todo 1.14: is this right?
        Biome[] biomes = new Biome[width * length];
        Arrays.fill(biomes, 0, width * length, this.biome);
        return biomes;
    }

    @Override
    @Nullable
    public BlockPos findBiomePosition(int x, int z, int range, List<Biome> biomes, Random random) {
        return biomes.contains(this.biome) ? new BlockPos(x - range + random.nextInt(range * 2 + 1), 0, z - range + random.nextInt(range * 2 + 1)) : null;
    }

    @Override
    public Biome getBiome(int x, int y) {
        return biome;
    }

    @Override
    public Set<Biome> getBiomesInSquare(int centerX, int centerZ, int sideLength) {
        return Collections.singleton(biome);
    }

    @Override
    public boolean hasStructure(Structure<?> structureIn) {
        return false;
    }

    @Override
    public Set<BlockState> getSurfaceBlocks() {
        return Collections.emptySet();
    }

    //    @Override
//    public boolean areBiomesViable(int x, int z, int radius, List<Biome> allowed) {
//        return allowed.contains(this.biome);
//    }

//    @Override
//    public boolean isFixedBiome() {
//        return true;
//    }

//    @Override
//    public Biome getFixedBiome() {
//        return this.biome;
//    }
}