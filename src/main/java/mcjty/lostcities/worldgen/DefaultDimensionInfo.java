package mcjty.lostcities.worldgen;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.WorldStyle;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerChunkProvider;

import java.util.Random;

public class DefaultDimensionInfo implements IDimensionInfo {

    private IWorld world;
    private final LostCityProfile profile;
    private final WorldStyle style;

    private final LostCityTerrainFeature feature;

    public DefaultDimensionInfo(IWorld world, LostCityProfile profile) {
        this.world = world;
        this.profile = profile;
        style = AssetRegistries.WORLDSTYLES.get("standard");
        feature = new LostCityTerrainFeature(this, profile, getRandom());
        feature.setupStates(profile);
    }

    @Override
    public void setWorld(IWorld world) {
        this.world = world;
    }

    @Override
    public long getSeed() {
        return world.getSeed();
    }

    @Override
    public IWorld getWorld() {
        return world;
    }

    @Override
    public DimensionType getType() {
        return world.getDimension().getType();
    }

    @Override
    public LostCityProfile getProfile() {
        return profile;
    }

    @Override
    public LostCityProfile getOutsideProfile() {
        return null;
    }

    @Override
    public WorldStyle getWorldStyle() {
        return style;
    }

    @Override
    public Random getRandom() {
        return world.getRandom();
    }

    @Override
    public LostCityTerrainFeature getFeature() {
        return feature;
    }

    @Override
    public ChunkHeightmap getHeightmap(int chunkX, int chunkZ) {
        return feature.getHeightmap(chunkX, chunkZ, getWorld());
    }

//    @Override
//    public Biome[] getBiomes(int chunkX, int chunkZ) {
//        AbstractChunkProvider chunkProvider = getWorld().getChunkProvider();
//        if (chunkProvider instanceof ServerChunkProvider) {
//            BiomeProvider biomeProvider = ((ServerChunkProvider) chunkProvider).getChunkGenerator().getBiomeProvider();
//            return biomeProvider.getBiomes((chunkX - 1) * 4 - 2, chunkZ * 4 - 2, 10, 10, false);
//        }
//    }
//
    @Override
    public Biome getBiome(BlockPos pos) {
        AbstractChunkProvider chunkProvider = getWorld().getChunkProvider();
        if (chunkProvider instanceof ServerChunkProvider) {
            BiomeProvider biomeProvider = ((ServerChunkProvider) chunkProvider).getChunkGenerator().getBiomeProvider();
            // @todo 1.15 check if thi sis correct!
            return biomeProvider.func_225526_b_(pos.getX(), pos.getY(), pos.getZ());
        }
        return Biomes.PLAINS;
    }
}
