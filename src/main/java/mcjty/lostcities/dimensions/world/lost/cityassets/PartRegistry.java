package mcjty.lostcities.dimensions.world.lost.cityassets;

import java.util.HashMap;
import java.util.Map;

public class PartRegistry {

    private final Map<String, BuildingPart> parts = new HashMap<>();

    public void register(BuildingPart part) {
        parts.put(part.getName(), part);
    }

    public BuildingPart get(String name) {
        return parts.get(name);
    }

    public void reset() {
        parts.clear();;
    }
}
