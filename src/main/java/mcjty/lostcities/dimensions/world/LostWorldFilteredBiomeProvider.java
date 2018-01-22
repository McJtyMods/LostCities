package mcjty.lostcities.dimensions.world;

import mcjty.lostcities.config.LandscapeType;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.dimensions.world.lost.CitySphere;
import mcjty.lostcities.varia.ChunkCoord;
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


    @Override
    public Biome getBiome(BlockPos pos) {
        LostCityProfile profile = getProvider().getProfile();
        Biome originalBiome = original.getBiome(pos);

        if (profile.LANDSCAPE_TYPE == LandscapeType.SPACE && profile.CITYSPHERE_LANDSCAPE_OUTSIDE) {
            int chunkX = (pos.getX()) >> 4;
            int chunkZ = (pos.getZ()) >> 4;
            ChunkCoord cityCenter = CitySphere.getCityCenterForSpace(chunkX, chunkZ, provider);
            CitySphere sphere = CitySphere.getCitySphereAtCenter(cityCenter, provider);
            if (sphere.isEnabled()) {
                float radius = CitySphere.getSphereRadius(cityCenter.getChunkX(), cityCenter.getChunkZ(), provider);
                double sqradiusOffset = (radius - 2) * (radius - 2);
                int cx = cityCenter.getChunkX() * 16 + 8;
                int cz = cityCenter.getChunkZ() * 16 + 8;
                if (CitySphere.squaredDistance(cx, cz, pos.getX(), pos.getZ()) > sqradiusOffset) {
                    return outsideTranslator.translate(originalBiome);
                }
            }
        }
        return biomeTranslator.translate(originalBiome);
    }

    private void translateList(Biome[] biomes, int topx, int topz, int width, int height) {
        LostCityProfile profile = getProvider().getProfile();
        if (profile.LANDSCAPE_TYPE == LandscapeType.SPACE && profile.CITYSPHERE_LANDSCAPE_OUTSIDE) {
            int chunkX = (topx) >> 4;
            int chunkZ = (topz) >> 4;
            ChunkCoord cityCenter = CitySphere.getCityCenterForSpace(chunkX, chunkZ, provider);
            CitySphere sphere = CitySphere.getCitySphereAtCenter(cityCenter, provider);
            if (sphere.isEnabled()) {
                float radius = CitySphere.getSphereRadius(cityCenter.getChunkX(), cityCenter.getChunkZ(), provider);
                double sqradiusOffset = (radius - 2) * (radius - 2);
                int cx = cityCenter.getChunkX() * 16 + 8;
                int cz = cityCenter.getChunkZ() * 16 + 8;
                for (int z = 0; z < height; z++) {
                    for (int x = 0; x < width; x++) {
                        int i = x + z * width;
                        if (CitySphere.squaredDistance(cx, cz, topx+x, topz+z) > sqradiusOffset) {
                            biomes[i] = outsideTranslator.translate(biomes[i]);
                        } else {
                            biomes[i] = biomeTranslator.translate(biomes[i]);
                        }
                    }
                }
            }
            return;
        }
    }

    @Override
    public Biome[] getBiomesForGeneration(Biome[] biomes, int x, int z, int width, int height) {
        biomes = original.getBiomesForGeneration(biomes, x, z, width, height);
        translateList(biomes, x, z, width, height);
        return biomes;
    }

    @Override
    public Biome[] getBiomes(@Nullable Biome[] oldBiomeList, int x, int z, int width, int depth) {
        oldBiomeList = original.getBiomes(oldBiomeList, x, z, width, depth);
        translateList(oldBiomeList, x, z, width, depth);
        return oldBiomeList;
    }

    public Biome[] getBiomesAlternate(@Nullable Biome[] oldBiomeList, int x, int z, int width, int depth) {
        oldBiomeList = original.getBiomes(oldBiomeList, x, z, width, depth);
        for (int i = 0 ; i < oldBiomeList.length ; i++) {
            oldBiomeList[i] = outsideTranslator.translate(oldBiomeList[i]);
        }
        return oldBiomeList;
    }

    @Override
    public Biome[] getBiomes(@Nullable Biome[] listToReuse, int x, int z, int width, int length, boolean cacheFlag) {
        return this.getBiomes(listToReuse, x, z, width, length);
    }

    @Override
    @Nullable
    public BlockPos findBiomePosition(int x, int z, int range, List<Biome> biomes, Random random) {
        return original.findBiomePosition(x, z, range, biomes, random);
    }

    @Override
    public boolean areBiomesViable(int x, int z, int radius, List<Biome> allowed) {
        return true;
    }

    @Override
    public boolean isFixedBiome() {
        return false;
    }

    @Override
    public Biome getFixedBiome() {
        return null;
    }
}