package mcjty.lostcities.config;

import java.util.HashMap;
import java.util.Map;

public enum LandscapeType {
    DEFAULT("default"),
    FLOATING("floating"),
    SPACE("space"),
    CAVERN("cavern");

    private final String name;

    private static final Map<String, LandscapeType> NAME_TO_TYPE = new HashMap<>();

    static {
        for (LandscapeType type : LandscapeType.values()) {
            NAME_TO_TYPE.put(type.getName(), type);
        }
    }

    LandscapeType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static LandscapeType getTypeByName(String name) {
        return NAME_TO_TYPE.get(name);
    }
}
