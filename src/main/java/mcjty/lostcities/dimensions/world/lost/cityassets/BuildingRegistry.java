package mcjty.lostcities.dimensions.world.lost.cityassets;

import java.util.*;

public class BuildingRegistry {

    private final Map<String, Building> buildings = new HashMap<>();
    private final List<String> buildingNames = new ArrayList<>();

    public void register(Building building) {
        buildings.put(building.getName(), building);
        buildingNames.add(building.getName());
    }

    public Building get(String name) {
        return buildings.get(name);
    }

    public Building get(int i) { return buildings.get(buildingNames.get(i)); }

    public int getBuildingCount() {
        return buildings.size();
    }

    public String getBuildingName(int i) {
        return buildingNames.get(i);
    }

    public void reset() {
        buildings.clear();;
    }
}
