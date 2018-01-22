package mcjty.lostcities.dimensions.world;

import mcjty.lostcities.config.LandscapeType;
import mcjty.lostcities.dimensions.world.lost.CitySphere;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class LostWorldFilteredBiomeProvider extends BiomeProvider {

    private final World world;
    private final BiomeProvider original;
    private LostCityChunkGenerator provider;

    private final BiomeTranslator biomeTranslator;
    private final BiomeTranslator outsideTranslator;

    public LostWorldFilteredBiomeProvider(World world, BiomeProvider original, String[] allowedBiomeFactors, String[] outsideBiomeFactors) {
        this.world = world;
        this.original = original;
        biomeTranslator = new BiomeTranslator(allowedBiomeFactors);
        outsideTranslator = new BiomeTranslator(outsideBiomeFactors.length == 0 ? allowedBiomeFactors : outsideBiomeFactors);
    }


    private LostCityChunkGenerator getProvider() {
        if (provider == null) {
            provider = (LostCityChunkGenerator) ((WorldServer)world).getChunkProvider().chunkGenerator;
        }
        return provider;
    }

    private boolean useOutside(int x, int z) {
        if (getProvider().getProfile().LANDSCAPE_TYPE == LandscapeType.SPACE && !CitySphere.intersectsWithCitySphere(x>>4, z>>4, getProvider())) {
            return true;
        } else {
            return false;
        }
    }


    public Biome getBiome(BlockPos pos) {
        if (useOutside(pos.getX(), pos.getZ())) {
            return outsideTranslator.translate(original.getBiome(pos));
        }
        return biomeTranslator.translate(original.getBiome(pos));
    }

    private void translateList(Biome[] biomes, int x, int z) {
        if (useOutside(x, z)) {
            for (int i = 0 ; i < biomes.length ; i++) {
                biomes[i] = outsideTranslator.translate(biomes[i]);
            }
        } else {
            for (int i = 0; i < biomes.length; i++) {
                biomes[i] = biomeTranslator.translate(biomes[i]);
            }
        }
    }

    public Biome[] getBiomesForGeneration(Biome[] biomes, int x, int z, int width, int height) {
        biomes = original.getBiomesForGeneration(biomes, x, z, width, height);
        translateList(biomes, x, z);
        return biomes;
    }

    public Biome[] getBiomes(@Nullable Biome[] oldBiomeList, int x, int z, int width, int depth) {
        oldBiomeList = original.getBiomes(oldBiomeList, x, z, width, depth);
        translateList(oldBiomeList, x, z);
        return oldBiomeList;
    }

    public Biome[] getBiomes(@Nullable Biome[] listToReuse, int x, int z, int width, int length, boolean cacheFlag) {
        return this.getBiomes(listToReuse, x, z, width, length);
    }

    @Nullable
    public BlockPos findBiomePosition(int x, int z, int range, List<Biome> biomes, Random random) {
        return original.findBiomePosition(x, z, range, biomes, random);
    }

    public boolean areBiomesViable(int x, int z, int radius, List<Biome> allowed) {
        return true;
    }

    public boolean isFixedBiome() {
        return false;
    }

    public Biome getFixedBiome() {
        return null;
    }
}