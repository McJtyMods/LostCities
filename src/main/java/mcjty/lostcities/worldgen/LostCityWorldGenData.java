package mcjty.lostcities.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.config.HighwayGenerationMode;
import mcjty.lostcities.config.StreetGenerationMode;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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

    private static final Codec<StreetGenerationMode> STREET_MODE_CODEC = Codec.STRING.xmap(
            StreetGenerationMode::byName, StreetGenerationMode::name);
    private static final Codec<HighwayGenerationMode> HIGHWAY_MODE_CODEC = Codec.STRING.xmap(
            HighwayGenerationMode::byName, HighwayGenerationMode::name);
    private static final Codec<LostCityWorldGenData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf(NEW_WORLD_KEY).forGetter(data -> data.newWorldStreetModes),
            Codec.BOOL.fieldOf(NEW_WORLD_HIGHWAY_KEY).forGetter(data -> data.newWorldHighwayModes),
            Codec.unboundedMap(Codec.STRING, STREET_MODE_CODEC).fieldOf(STREET_MODES_KEY)
                    .forGetter(LostCityWorldGenData::streetModesSnapshot),
            Codec.unboundedMap(Codec.STRING, HIGHWAY_MODE_CODEC).fieldOf(HIGHWAY_MODES_KEY)
                    .forGetter(LostCityWorldGenData::highwayModesSnapshot)
    ).apply(instance, LostCityWorldGenData::new));
    private static final SavedDataType<LostCityWorldGenData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("lostcities", "lostcity_worldgen_data"),
            LostCityWorldGenData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

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

    private LostCityWorldGenData(boolean newWorldStreetModes, boolean newWorldHighwayModes,
                                 Map<String, StreetGenerationMode> streetModes,
                                 Map<String, HighwayGenerationMode> highwayModes) {
        this.newWorldStreetModes = newWorldStreetModes;
        this.newWorldHighwayModes = newWorldHighwayModes;
        this.streetModes.putAll(streetModes);
        this.highwayModes.putAll(highwayModes);
    }

    @Nonnull
    public static LostCityWorldGenData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            throw new IllegalStateException("Cannot access Lost Cities world generation data without an overworld");
        }
        SavedDataStorage storage = level.getServer().getDataStorage();
        return storage.computeIfAbsent(TYPE);
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
        return getStreetMode(dimension.identifier().toString(), requestedMode);
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
        return getHighwayMode(dimension.identifier().toString(), requestedMode);
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

    private synchronized Map<String, StreetGenerationMode> streetModesSnapshot() {
        return new TreeMap<>(streetModes);
    }

    private synchronized Map<String, HighwayGenerationMode> highwayModesSnapshot() {
        return new TreeMap<>(highwayModes);
    }

}
