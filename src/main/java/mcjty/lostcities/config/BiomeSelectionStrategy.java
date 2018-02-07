package mcjty.lostcities.config;

import java.util.HashMap;
import java.util.Map;

public enum BiomeSelectionStrategy {
    ORIGINAL("original"),
    RANDOMIZED("randomized"),
    VARIED("varied");

    private final String name;

    private static final Map<String, BiomeSelectionStrategy> NAME_TO_TYPE = new HashMap<>();

    static {
        for (BiomeSelectionStrategy type : BiomeSelectionStrategy.values()) {
            NAME_TO_TYPE.put(type.getName(), type);
        }
    }

    BiomeSelectionStrategy(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static BiomeSelectionStrategy getTypeByName(String name) {
        return NAME_TO_TYPE.get(name);
    }

}
