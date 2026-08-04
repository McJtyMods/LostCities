package mcjty.lostcities.worldgen;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.setup.Config;
import mcjty.lostcities.worldgen.highway.HighwayHub;
import mcjty.lostcities.worldgen.highway.HighwayHubPersistence;
import mcjty.lostcities.worldgen.highway.HighwayPlannerSettings;
import mcjty.lostcities.worldgen.highway.HubKey;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Persistent cache of the expensive inter-city highway hub decision. Routes and
 * per-chunk highway information remain deterministic derived data.
 */
public class LostCityHighwayData extends SavedData {

    public static final String NAME = "LostCityHighwayData";
    private static final int FORMAT_VERSION = 2;
    private static final long HUB_ALGORITHM_VERSION = 3L;

    private static final String VERSION_KEY = "version";
    private static final String DIMENSIONS_KEY = "dimensions";
    private static final String SIGNATURE_KEY = "signature";
    private static final String HUBS_KEY = "hubs";
    private static final String CELL_X_KEY = "cellX";
    private static final String CELL_Z_KEY = "cellZ";
    private static final String HAS_HUB_KEY = "hasHub";
    private static final String CHUNK_X_KEY = "chunkX";
    private static final String CHUNK_Z_KEY = "chunkZ";
    private static final String POTENTIAL_KEY = "potential";
    private static final String CITY_LEVEL_KEY = "cityLevel";

    private final Map<String, DimensionHubData> dimensions = new HashMap<>();

    public LostCityHighwayData() {
    }

    public LostCityHighwayData(CompoundTag tag, HolderLookup.Provider provider) {
        if (tag.getInt(VERSION_KEY) != FORMAT_VERSION) {
            return;
        }
        CompoundTag dimensionTags = tag.getCompound(DIMENSIONS_KEY);
        for (String dimensionId : dimensionTags.getAllKeys()) {
            if (!dimensionTags.contains(dimensionId, Tag.TAG_COMPOUND)) {
                continue;
            }
            CompoundTag dimensionTag = dimensionTags.getCompound(dimensionId);
            DimensionHubData dimension = new DimensionHubData(dimensionTag.getLong(SIGNATURE_KEY));
            ListTag hubs = dimensionTag.getList(HUBS_KEY, Tag.TAG_COMPOUND);
            for (Tag value : hubs) {
                CompoundTag hubTag = (CompoundTag) value;
                HubKey key = new HubKey(hubTag.getInt(CELL_X_KEY), hubTag.getInt(CELL_Z_KEY));
                Optional<HighwayHub> hub = hubTag.getBoolean(HAS_HUB_KEY)
                        ? Optional.of(new HighwayHub(key, hubTag.getInt(CHUNK_X_KEY), hubTag.getInt(CHUNK_Z_KEY),
                        hubTag.getInt(POTENTIAL_KEY), hubTag.getInt(CITY_LEVEL_KEY)))
                        : Optional.empty();
                dimension.hubs.put(key, hub);
            }
            dimensions.put(dimensionId, dimension);
        }
    }

    @Nonnull
    public static LostCityHighwayData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            throw new IllegalStateException("Cannot access Lost Cities highway data without an overworld");
        }
        DimensionDataStorage storage = overworld.getDataStorage();
        return storage.computeIfAbsent(new Factory<>(LostCityHighwayData::new, LostCityHighwayData::new), NAME);
    }

    /**
     * Returns a dimension-scoped view. A changed signature invalidates only that
     * dimension's old performance cache; it never changes a calculated result.
     */
    public synchronized HighwayHubPersistence forDimension(ResourceKey<Level> dimension, long signature) {
        String dimensionId = dimension.location().toString();
        DimensionHubData data = dimensions.get(dimensionId);
        if (data == null || data.signature != signature) {
            dimensions.put(dimensionId, new DimensionHubData(signature));
            setDirty();
        }
        return new HighwayHubPersistence() {
            @Override
            public Lookup get(HubKey key) {
                return lookup(dimensionId, signature, key);
            }

            @Override
            public void put(HubKey key, Optional<HighwayHub> hub) {
                store(dimensionId, signature, key, hub);
            }
        };
    }

    private synchronized HighwayHubPersistence.Lookup lookup(String dimensionId, long signature, HubKey key) {
        DimensionHubData dimension = dimensions.get(dimensionId);
        if (dimension == null || dimension.signature != signature || !dimension.hubs.containsKey(key)) {
            return HighwayHubPersistence.Lookup.missing();
        }
        return HighwayHubPersistence.Lookup.known(dimension.hubs.get(key));
    }

    private synchronized void store(String dimensionId, long signature, HubKey key, Optional<HighwayHub> hub) {
        DimensionHubData dimension = dimensions.get(dimensionId);
        if (dimension == null || dimension.signature != signature) {
            dimension = new DimensionHubData(signature);
            dimensions.put(dimensionId, dimension);
        }
        if (!hub.equals(dimension.hubs.put(key, hub))) {
            setDirty();
        }
    }

    /**
     * Fingerprints all profile inputs used by hub discovery. The world-style ID
     * covers selection of the biome-multiplier asset; format/algorithm revisions
     * cover code changes to the calculation itself.
     */
    public static long createCacheSignature(long seed, LostCityProfile profile, LostCityProfile outsideProfile,
                                            HighwayPlannerSettings settings, ResourceLocation worldStyleId) {
        long hash = 0xcbf29ce484222325L;
        hash = hash(hash, FORMAT_VERSION);
        hash = hash(hash, HUB_ALGORITHM_VERSION);
        hash = hash(hash, seed);
        hash = hash(hash, settings.planningCellSize());
        hash = hash(hash, settings.hubSampleSpacing());
        hash = hash(hash, Float.floatToIntBits(settings.hubMinimumPotential()));
        hash = hash(hash, Double.doubleToLongBits(profile.CITY_CHANCE));
        hash = hash(hash, profile.CITY_MINRADIUS);
        hash = hash(hash, profile.CITY_MAXRADIUS);
        hash = hash(hash, profile.CITY_SPAWN_DISTANCE1);
        hash = hash(hash, profile.CITY_SPAWN_DISTANCE2);
        hash = hash(hash, Double.doubleToLongBits(profile.CITY_SPAWN_MULTIPLIER1));
        hash = hash(hash, Double.doubleToLongBits(profile.CITY_SPAWN_MULTIPLIER2));
        hash = hash(hash, Double.doubleToLongBits(profile.CITY_PERLIN_SCALE));
        hash = hash(hash, Double.doubleToLongBits(profile.CITY_PERLIN_OFFSET));
        hash = hash(hash, Double.doubleToLongBits(profile.CITY_PERLIN_INNERSCALE));
        hash = hash(hash, profile.CITY_MINHEIGHT);
        hash = hash(hash, profile.CITY_MAXHEIGHT);
        hash = hashCityLevelSettings(hash, profile);
        hash = hashCityLevelSettings(hash, outsideProfile);
        hash = hash(hash, Config.HEIGHT_SAMPLE_SIZE.get());
        return hashString(hash, worldStyleId.toString());
    }

    private static long hashCityLevelSettings(long hash, LostCityProfile profile) {
        if (profile == null) {
            return hash(hash, 0);
        }
        hash = hash(hash, 1);
        hash = hash(hash, profile.LANDSCAPE_TYPE.ordinal());
        hash = hash(hash, profile.USE_AVG_HEIGHTMAP ? 1 : 0);
        hash = hash(hash, profile.CITY_LEVEL0_HEIGHT);
        hash = hash(hash, profile.CITY_LEVEL1_HEIGHT);
        hash = hash(hash, profile.CITY_LEVEL2_HEIGHT);
        hash = hash(hash, profile.CITY_LEVEL3_HEIGHT);
        hash = hash(hash, profile.CITY_LEVEL4_HEIGHT);
        hash = hash(hash, profile.CITY_LEVEL5_HEIGHT);
        hash = hash(hash, profile.CITY_LEVEL6_HEIGHT);
        return hash(hash, profile.CITY_LEVEL7_HEIGHT);
    }

    private static long hash(long current, long value) {
        current ^= value;
        return current * 0x100000001b3L;
    }

    private static long hashString(long current, String value) {
        for (int i = 0; i < value.length(); i++) {
            current = hash(current, value.charAt(i));
        }
        return current;
    }

    @Override
    public synchronized CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putInt(VERSION_KEY, FORMAT_VERSION);
        CompoundTag dimensionTags = new CompoundTag();
        new TreeMap<>(dimensions).forEach((dimensionId, dimension) -> {
            CompoundTag dimensionTag = new CompoundTag();
            dimensionTag.putLong(SIGNATURE_KEY, dimension.signature);
            ListTag hubs = new ListTag();
            new TreeMap<>(dimension.hubs).forEach((key, hub) -> {
                CompoundTag hubTag = new CompoundTag();
                hubTag.putInt(CELL_X_KEY, key.planningCellX());
                hubTag.putInt(CELL_Z_KEY, key.planningCellZ());
                hubTag.putBoolean(HAS_HUB_KEY, hub.isPresent());
                hub.ifPresent(value -> {
                    hubTag.putInt(CHUNK_X_KEY, value.chunkX());
                    hubTag.putInt(CHUNK_Z_KEY, value.chunkZ());
                    hubTag.putInt(POTENTIAL_KEY, value.potentialScore());
                    hubTag.putInt(CITY_LEVEL_KEY, value.cityLevel());
                });
                hubs.add(hubTag);
            });
            dimensionTag.put(HUBS_KEY, hubs);
            dimensionTags.put(dimensionId, dimensionTag);
        });
        tag.put(DIMENSIONS_KEY, dimensionTags);
        return tag;
    }

    private static final class DimensionHubData {
        private final long signature;
        private final Map<HubKey, Optional<HighwayHub>> hubs = new HashMap<>();

        private DimensionHubData(long signature) {
            this.signature = signature;
        }
    }
}
