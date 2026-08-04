package mcjty.lostcities.config;

import java.util.Locale;

/**
 * Versioned street layout algorithms. Values are persisted in world SavedData;
 * never rename an existing constant without adding a migration.
 */
public enum StreetGenerationMode {
    LEGACY,
    HIERARCHICAL_GRID_V1;

    public static StreetGenerationMode byName(String name) {
        try {
            return valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown street generation mode '" + name + "'", e);
        }
    }
}
