package mcjty.lostcities.config;

import java.util.Locale;

/**
 * Versioned highway occupancy algorithms. Values are persisted in world
 * SavedData; never rename an existing constant without adding a migration.
 */
public enum HighwayGenerationMode {
    LEGACY,
    INTERCITY_NETWORK_V1;

    public static HighwayGenerationMode byName(String name) {
        try {
            return valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown highway generation mode '" + name + "'", e);
        }
    }
}
