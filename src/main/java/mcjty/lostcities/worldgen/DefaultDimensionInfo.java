package mcjty.lostcities.worldgen;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.config.HighwayGenerationMode;
import mcjty.lostcities.config.StreetGenerationMode;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.WorldStyle;
import mcjty.lostcities.worldgen.street.HierarchicalStreetPlanner;
import mcjty.lostcities.worldgen.street.StreetPlannerSettings;
import mcjty.lostcities.worldgen.highway.ApproximateCityPotential;
import mcjty.lostcities.worldgen.highway.HighwayPlannerSettings;
import mcjty.lostcities.worldgen.highway.IntercityHighwayPlanner;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class DefaultDimensionInfo implements IDimensionInfo {

    private volatile WorldGenLevel world;
    private final long seed;
    private final ResourceKey<Level> type;
    private final LostCityProfile profile;
    private final LostCityProfile profileOutside;
    private final WorldStyle style;
    private final StreetGenerationMode streetGenerationMode;
    private final HierarchicalStreetPlanner streetPlanner;
    private final HighwayGenerationMode highwayGenerationMode;
    private final IntercityHighwayPlanner highwayPlanner;

    private final ThreadLocal<Random> random;

    private final Registry<Biome> biomeRegistry;
    private final LostCityTerrainFeature feature;

    public DefaultDimensionInfo(WorldGenLevel world, LostCityProfile profile, LostCityProfile profileOutside) {
        this.world = world;
        this.seed = world.getSeed();
        this.type = world.getLevel().dimension();
        this.profile = profile;
        this.profileOutside = profileOutside;
        style = AssetRegistries.WORLDSTYLES.get(world, profile.getWorldStyle());
        streetGenerationMode = LostCityWorldGenData.get(world.getLevel()).getStreetMode(world.getLevel().dimension(), profile.STREET_GENERATION_MODE);
        streetPlanner = new HierarchicalStreetPlanner(world.getSeed(), world.getLevel().dimension().location().toString(), StreetPlannerSettings.fromProfile(profile));
        random = ThreadLocal.withInitial(() -> new Random(seed));
        RandomSource randomSource = new LegacyRandomSource(world.getSeed());
        feature = new LostCityTerrainFeature(this, profile, randomSource);
        feature.setupStates(profile);
        highwayGenerationMode = LostCityWorldGenData.get(world.getLevel()).getHighwayMode(world.getLevel().dimension(), profile.HIGHWAY_GENERATION_MODE);
        if (highwayGenerationMode == HighwayGenerationMode.INTERCITY_NETWORK_V1) {
            HighwayPlannerSettings highwaySettings = HighwayPlannerSettings.fromProfile(profile);
            long cacheSignature = LostCityHighwayData.createCacheSignature(world.getSeed(), profile, profileOutside,
                    highwaySettings, style.getId());
            highwayPlanner = new IntercityHighwayPlanner(world.getSeed(), world.getLevel().dimension().location().toString(),
                    highwaySettings,
                    new ApproximateCityPotential(world.getSeed(), profile, this::applyHighwayCityConstraints),
                    (chunkX, chunkZ) -> BuildingInfo.getCityLevel(new ChunkCoord(type, chunkX, chunkZ), this),
                    LostCityHighwayData.get(world.getLevel()).forDimension(world.getLevel().dimension(), cacheSignature));
        } else {
            highwayPlanner = null;
        }
        biomeRegistry = world.registryAccess().registryOrThrow(Registries.BIOME);
    }

    private float applyHighwayCityConstraints(int chunkX, int chunkZ, float potential) {
        if (potential <= 0.0001f) {
            return potential;
        }
        ChunkCoord coord = new ChunkCoord(type, chunkX, chunkZ);
        int height = getHeightmap(coord).getHeight();
        if (height < profile.CITY_MINHEIGHT || height > profile.CITY_MAXHEIGHT) {
            return 0.0f;
        }
        Holder<Biome> biome = getBiome(new BlockPos((chunkX << 4) + 8, height, (chunkZ << 4) + 8));
        return potential * style.getCityChanceMultiplier(biome);
    }

    @Override
    public void setWorld(WorldGenLevel world) {
        this.world = world;
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public WorldGenLevel getWorld() {
        WorldGenLevel activeWorld = GenerationContext.currentWorld();
        return activeWorld == null ? world : activeWorld;
    }

    @Override
    public ResourceKey<Level> getType() {
        return type;
    }

    @Override
    public LostCityProfile getProfile() {
        return profile;
    }

    @Override
    public LostCityProfile getOutsideProfile() {
        return profileOutside;
    }

    @Override
    public WorldStyle getWorldStyle() {
        return style;
    }

    @Override
    public StreetGenerationMode getStreetGenerationMode() {
        return streetGenerationMode;
    }

    @Override
    public HierarchicalStreetPlanner getStreetPlanner() {
        return streetPlanner;
    }

    @Override
    public HighwayGenerationMode getHighwayGenerationMode() {
        return highwayGenerationMode;
    }

    @Override
    public IntercityHighwayPlanner getHighwayPlanner() {
        if (highwayPlanner == null) {
            throw new IllegalStateException("The inter-city highway planner is unavailable in LEGACY mode");
        }
        return highwayPlanner;
    }

    @Override
    public Random getRandom() {
        return random.get();
    }

    @Override
    public LostCityTerrainFeature getFeature() {
        return feature;
    }

    @Override
    public ChunkHeightmap getHeightmap(int chunkX, int chunkZ) {
        ChunkCoord coord = new ChunkCoord(getType(), chunkX, chunkZ);
        return feature.getHeightmap(coord, getWorld());
    }

    @Override
    public ChunkHeightmap getHeightmap(ChunkCoord coord) {
        return feature.getHeightmap(coord, getWorld());
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
    public Holder<Biome> getBiome(BlockPos pos) {
        ChunkSource chunkProvider = getWorld().getChunkSource();
        if (chunkProvider instanceof ServerChunkCache) {
            ChunkGenerator generator = ((ServerChunkCache) chunkProvider).getGenerator();
            BiomeSource biomeProvider = generator.getBiomeSource();
            Climate.Sampler sampler = ((ServerChunkCache) chunkProvider).randomState().sampler();
            return biomeProvider.getNoiseBiome(pos.getX() >> 2, pos.getY() >> 2, pos.getZ() >> 2, sampler);
        }
        return biomeRegistry.getHolderOrThrow(Biomes.PLAINS);
    }

    @Nullable
    @Override
    public ResourceKey<Level> dimension() {
        return type;
    }
}
