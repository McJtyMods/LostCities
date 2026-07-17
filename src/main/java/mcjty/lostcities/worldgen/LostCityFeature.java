package mcjty.lostcities.worldgen;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.config.ProfileSetup;
import mcjty.lostcities.setup.Config;
import mcjty.lostcities.setup.ForgeEventHandlers;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.common.Tags;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

public class LostCityFeature extends Feature<NoneFeatureConfiguration> {

    /**
     * On dedicated servers the dimensionInfo cache is no problem. The server starts only once
     * and will have the correct dimension info and for the clients it doesn't matter.
     * However, to make sure that on a single player world this cache is cleared when the player
     * exits the world and creates a new one we keep a static flag which is incremented whenever
     * the player exits the world. That is then used to help clear this cache
     */
    private final Map<ResourceKey<Level>, IDimensionInfo> dimensionInfo = new ConcurrentHashMap<>();
    private static final int GENERATION_LOCK_STRIPES = 4096;
    private final ReentrantLock[] generationLocks = createGenerationLocks();
    private final ReentrantReadWriteLock lifecycleLock = new ReentrantReadWriteLock();
    public static volatile int globalDimensionInfoDirtyCounter = 0;
    private int dimensionInfoDirtyCounter = -1;

    public LostCityFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        if (level instanceof WorldGenRegion region) {
            return runWithDimensionInfo(level, diminfo -> {
                ChunkPos center = region.getCenter();
                Holder<Biome> biome = region.getBiome(center.getMiddleBlockPosition(60));
                if (biome.is(Tags.Biomes.IS_VOID)) {
                    return false;
                }

                int chunkX = center.x;
                int chunkZ = center.z;
                try {
                    diminfo.getFeature().generate(region, region.getChunk(chunkX, chunkZ));
                } catch (Exception e) {
                    LostCities.getLogger().error("Error generating chunk {},{}: {}", chunkX, chunkZ, e.getMessage(), e);
                    e.printStackTrace();
                    ErrorLogger.logChunkInfo(chunkX, chunkZ, diminfo);
                    ErrorLogger.report("There was an error generating a chunk. See log for details!");
                }
                return true;
            });
        }
        return false;
    }

    /**
     * Run Lost Cities generation using the shared lifecycle and local chunk-neighbourhood boundary.
     * Companion generation features should use this method instead of retaining an
     * {@link IDimensionInfo} beyond the callback.
     *
     * <p>Generation holds a lifecycle read lock. Nearby chunks use stable striped locks because
     * post-processing can touch blocks across a chunk edge; non-overlapping chunks in the same
     * dimension may generate concurrently. Cleanup waits for all active generation calls.</p>
     */
    public boolean runWithDimensionInfo(WorldGenLevel world, GenerationAction action) {
        lockCurrentLifecycleForReading();
        try {
            IDimensionInfo diminfo = getOrCreateDimensionInfo(world);
            if (diminfo == null) {
                return false;
            }
            if (world instanceof WorldGenRegion region) {
                ChunkPos center = region.getCenter();
                return runWithChunkNeighborhoodLocks(world.getLevel().dimension(), center.x, center.z, () -> action.generate(diminfo));
            }
            return action.generate(diminfo);
        } finally {
            lifecycleLock.readLock().unlock();
        }
    }

    @FunctionalInterface
    public interface GenerationAction {
        boolean generate(IDimensionInfo dimensionInfo);
    }

    private boolean runWithChunkNeighborhoodLocks(ResourceKey<Level> dimension, int chunkX, int chunkZ, GenerationActionWithoutInfo action) {
        int[] stripes = new int[9];
        int count = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int stripe = getGenerationLockStripe(dimension, chunkX + dx, chunkZ + dz);
                boolean duplicate = false;
                for (int i = 0; i < count; i++) {
                    if (stripes[i] == stripe) {
                        duplicate = true;
                        break;
                    }
                }
                if (!duplicate) {
                    stripes[count++] = stripe;
                }
            }
        }
        Arrays.sort(stripes, 0, count);
        for (int i = 0; i < count; i++) {
            generationLocks[stripes[i]].lock();
        }
        try {
            return action.generate();
        } finally {
            for (int i = count - 1; i >= 0; i--) {
                generationLocks[stripes[i]].unlock();
            }
        }
    }

    private static int getGenerationLockStripe(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        int hash = dimension.location().hashCode();
        hash = 31 * hash + chunkX;
        hash = 31 * hash + chunkZ;
        hash ^= hash >>> 16;
        return hash & (GENERATION_LOCK_STRIPES - 1);
    }

    private static ReentrantLock[] createGenerationLocks() {
        ReentrantLock[] locks = new ReentrantLock[GENERATION_LOCK_STRIPES];
        Arrays.setAll(locks, ignored -> new ReentrantLock());
        return locks;
    }

    @FunctionalInterface
    private interface GenerationActionWithoutInfo {
        boolean generate();
    }

    @Nullable
    public IDimensionInfo getDimensionInfo(WorldGenLevel world) {
        lockCurrentLifecycleForReading();
        try {
            return getOrCreateDimensionInfo(world);
        } finally {
            lifecycleLock.readLock().unlock();
        }
    }

    /**
     * Acquire and retain the lifecycle read lock, refreshing all world-bound caches first when
     * needed. The counter is checked while holding the read lock so cleanup cannot slip between
     * the freshness check and the generation callback.
     */
    private void lockCurrentLifecycleForReading() {
        while (true) {
            lifecycleLock.readLock().lock();
            if (globalDimensionInfoDirtyCounter == dimensionInfoDirtyCounter) {
                return;
            }
            lifecycleLock.readLock().unlock();
            lifecycleLock.writeLock().lock();
            try {
                if (globalDimensionInfoDirtyCounter != dimensionInfoDirtyCounter) {
                    cleanUpInternal();
                }
            } finally {
                lifecycleLock.writeLock().unlock();
            }
        }
    }

    @Nullable
    private IDimensionInfo getOrCreateDimensionInfo(WorldGenLevel world) {
        ResourceKey<Level> type = world.getLevel().dimension();
        String profileName = Config.getProfileForDimension(type);
        if (profileName != null) {
            LostCityProfile profile = ProfileSetup.STANDARD_PROFILES.get(profileName);
            if (profile == null) {
                return null;
            }
            IDimensionInfo info = dimensionInfo.computeIfAbsent(type, key -> {
                LostCityProfile outsideProfile = profile.CITYSPHERE_OUTSIDE_PROFILE == null ? null : ProfileSetup.STANDARD_PROFILES.get(profile.CITYSPHERE_OUTSIDE_PROFILE);
                return new DefaultDimensionInfo(world, profile, outsideProfile);
            });
            info.setWorld(world);
            return info;
        }
        return null;
    }

    public void cleanUp() {
        lifecycleLock.writeLock().lock();
        try {
            cleanUpInternal();
        } finally {
            lifecycleLock.writeLock().unlock();
        }
    }

    private void cleanUpInternal() {
        LostCities.lostCitiesImp.cleanUp();
        ForgeEventHandlers.cleanUp();
        AssetRegistries.reset();
        dimensionInfo.clear();
        dimensionInfoDirtyCounter = globalDimensionInfoDirtyCounter;
    }
}
