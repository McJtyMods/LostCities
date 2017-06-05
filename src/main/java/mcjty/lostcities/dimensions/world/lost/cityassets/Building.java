package mcjty.lostcities.dimensions.world.lost.cityassets;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class Building {

    private final String name;

    private final List<Pair<Predicate<LevelInfo>, String>> parts = new ArrayList<>();
    private final List<String> partNames = new ArrayList<>();

    public Building(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addPart(Predicate<LevelInfo> test, String partName) {
        parts.add(Pair.of(test, partName));
        if (!partNames.contains(partName)) {
            partNames.add(partName);
        }
    }

    public String getPartName(int index) {
        return partNames.get(index);
    }

    public int getPartCount() {
        return partNames.size();
    }

    public String getRandomPart(Random random, LevelInfo info) {
        List<String> partNames = new ArrayList<>();
        for (Pair<Predicate<LevelInfo>, String> pair : parts) {
            if (pair.getLeft().test(info)) {
                partNames.add(pair.getRight());
            }
        }
        if (partNames.isEmpty()) {
            return null;
        }
        return partNames.get(random.nextInt(partNames.size()));
    }

    public static class LevelInfo {
        private final int level;        // Global level in world with 0 being to lowest possible level where a building section can be
        private final int floor;        // Level of the building with 0 being the ground floor. floor == floorsAboveGround+1 means the top of the building section
        private final int floorsBelowGround;    // 0 means nothing below ground
        private final int floorsAboveGround;    // 0 means 1 floor above ground

        public LevelInfo(int level, int floor, int floorsBelowGround, int floorsAboveGround) {
            this.level = level;
            this.floor = floor;
            this.floorsBelowGround = floorsBelowGround;
            this.floorsAboveGround = floorsAboveGround;
        }

        public int getLevel() {
            return level;
        }

        public int getFloor() {
            return floor;
        }

        public int getFloorsBelowGround() {
            return floorsBelowGround;
        }

        public int getFloorsAboveGround() {
            return floorsAboveGround;
        }

        public boolean isGroundFloor() {
            return floor == 0;
        }

        public boolean isTopOfBuilding() {
            return floor >= floorsAboveGround+1;
        }
    }
}
