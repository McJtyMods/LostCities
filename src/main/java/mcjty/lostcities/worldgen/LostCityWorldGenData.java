package mcjty.lostcities.worldgen;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.config.HighwayGenerationMode;
import mcjty.lostcities.config.StreetGenerationMode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Per-world generation choices stored in the overworld data storage.
 *
 * Absence of this SavedData means the world predates this version and therefore
 * resolves to LEGACY. Only CreateSpawnPosition marks a genuinely new world as
 * eligible to select profile-requested versioned modes.
 */
public class LostCityWorldGenData extends SavedData {

    public static final String NAME = "LostCityWorldGenData";
    private static final String NEW_WORLD_KEY = "newWorldStreetModes";
    private static final String NEW_WORLD_HIGHWAY_KEY = "newWorldHighwayModes";
    private static final String STREET_MODES_KEY = "streetModes";
    private static final String HIGHWAY_MODES_KEY = "highwayModes";

    private boolean newWorldStreetModes;
    private boolean newWorldHighwayModes;
    private final Map<String, StreetGenerationMode> streetModes = new HashMap<>();
    private final Map<String, HighwayGenerationMode> highwayModes = new HashMap<>();

    public LostCityWorldGenData() {
        // A default-constructed instance means no SavedData existed on disk. It
        // must behave as an old world until the new-world lifecycle event marks it.
        newWorldStreetModes = false;
        newWorldHighwayModes = false;
    }

    public LostCityWorldGenData(CompoundTag tag) {
        newWorldStreetModes = tag.getBoolean(NEW_WORLD_KEY);
        newWorldHighwayModes = tag.getBoolean(NEW_WORLD_HIGHWAY_KEY);
        CompoundTag modes = tag.getCompound(STREET_MODES_KEY);
        for (String dimension : modes.getAllKeys()) {
            if (modes.contains(dimension, Tag.TAG_STRING)) {
                String value = modes.getString(dimension);
                try {
                    streetModes.put(dimension, StreetGenerationMode.byName(value));
                } catch (IllegalArgumentException e) {
                    LostCities.getLogger().error("Unknown persisted street mode '{}' for {}; using LEGACY", value, dimension);
                    streetModes.put(dimension, StreetGenerationMode.LEGACY);
                }
            }
        }
        CompoundTag highwayModeTag = tag.getCompound(HIGHWAY_MODES_KEY);
        for (String dimension : highwayModeTag.getAllKeys()) {
            if (highwayModeTag.contains(dimension, Tag.TAG_STRING)) {
                String value = highwayModeTag.getString(dimension);
                try {
                    highwayModes.put(dimension, HighwayGenerationMode.byName(value));
                } catch (IllegalArgumentException e) {
                    LostCities.getLogger().error("Unknown persisted highway mode '{}' for {}; using LEGACY", value, dimension);
                    highwayModes.put(dimension, HighwayGenerationMode.LEGACY);
                }
            }
        }
    }

    @Nonnull
    public static LostCityWorldGenData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            throw new IllegalStateException("Cannot access Lost Cities world generation data without an overworld");
        }
        DimensionDataStorage storage = overworld.getDataStorage();
        return storage.computeIfAbsent(LostCityWorldGenData::new, LostCityWorldGenData::new, NAME);
    }

    public static void initializeNewWorld(ServerLevel level) {
        LostCityWorldGenData data = get(level);
        data.markNewWorld();
    }

    void markNewWorld() {
        if (!newWorldStreetModes || !newWorldHighwayModes) {
            newWorldStreetModes = true;
            newWorldHighwayModes = true;
            setDirty();
        }
    }

    public synchronized StreetGenerationMode getStreetMode(ResourceKey<Level> dimension,
                                                             StreetGenerationMode requestedMode) {
        return getStreetMode(dimension.location().toString(), requestedMode);
    }

    synchronized StreetGenerationMode getStreetMode(String dimensionId, StreetGenerationMode requestedMode) {
        StreetGenerationMode persisted = streetModes.get(dimensionId);
        if (persisted != null) {
            return persisted;
        }
        StreetGenerationMode selected = resolveUnpersistedMode(newWorldStreetModes, requestedMode);
        if (newWorldStreetModes) {
            streetModes.put(dimensionId, selected);
            setDirty();
        }
        return selected;
    }

    /** Pure compatibility rule, exposed for focused tests. */
    public static StreetGenerationMode resolveUnpersistedMode(boolean initializedAsNewWorld,
                                                               StreetGenerationMode requestedMode) {
        return initializedAsNewWorld ? requestedMode : StreetGenerationMode.LEGACY;
    }

    public synchronized HighwayGenerationMode getHighwayMode(ResourceKey<Level> dimension,
                                                               HighwayGenerationMode requestedMode) {
        return getHighwayMode(dimension.location().toString(), requestedMode);
    }

    synchronized HighwayGenerationMode getHighwayMode(String dimensionId, HighwayGenerationMode requestedMode) {
        HighwayGenerationMode persisted = highwayModes.get(dimensionId);
        if (persisted != null) {
            return persisted;
        }
        HighwayGenerationMode selected = resolveUnpersistedHighwayMode(newWorldHighwayModes, requestedMode);
        if (newWorldHighwayModes) {
            highwayModes.put(dimensionId, selected);
            setDirty();
        }
        return selected;
    }

    /** Pure highway compatibility rule, exposed for focused tests. */
    public static HighwayGenerationMode resolveUnpersistedHighwayMode(boolean initializedAsNewWorld,
                                                                       HighwayGenerationMode requestedMode) {
        return initializedAsNewWorld ? requestedMode : HighwayGenerationMode.LEGACY;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean(NEW_WORLD_KEY, newWorldStreetModes);
        tag.putBoolean(NEW_WORLD_HIGHWAY_KEY, newWorldHighwayModes);
        CompoundTag modes = new CompoundTag();
        streetModes.forEach((dimension, mode) -> modes.putString(dimension, mode.name()));
        tag.put(STREET_MODES_KEY, modes);
        CompoundTag highwayModeTag = new CompoundTag();
        highwayModes.forEach((dimension, mode) -> highwayModeTag.putString(dimension, mode.name()));
        tag.put(HIGHWAY_MODES_KEY, highwayModeTag);
        return tag;
    }
}
