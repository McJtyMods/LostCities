package mcjty.lostcities.dimensions.world.lost.cityassets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class Building implements IAsset {

    private String name;

    private int minFloors = -1;         // -1 means default from level
    private int minCellars = -1;        // -1 means default frmo level
    private int maxFloors = -1;         // -1 means default from level
    private int maxCellars = -1;        // -1 means default frmo level

    private final List<Pair<Predicate<LevelInfo>, String>> parts = new ArrayList<>();
    private final List<String> partNames = new ArrayList<>();

    public Building(JsonObject object) {
        readFromJSon(object);
    }

    public Building(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void readFromJSon(JsonObject object) {
        name = object.get("name").getAsString();
        JsonArray partArray = object.get("parts").getAsJsonArray();
        parts.clear();
        partNames.clear();
        for (JsonElement element : partArray) {
            String partName = element.getAsJsonObject().get("part").getAsString();
            Predicate<LevelInfo> test = levelInfo -> true;
            if (element.getAsJsonObject().has("top")) {
                boolean top = element.getAsJsonObject().get("top").getAsBoolean();
                if (top) {
                    test = levelInfo -> levelInfo.isTopOfBuilding();
                } else {
                    test = levelInfo -> !levelInfo.isTopOfBuilding();
                }
            }
            if (element.getAsJsonObject().has("ground")) {
                boolean ground = element.getAsJsonObject().get("ground").getAsBoolean();
                if (ground) {
                    test = levelInfo -> levelInfo.isGroundFloor();
                } else {
                    test = levelInfo -> !levelInfo.isGroundFloor();
                }
            }
            if (element.getAsJsonObject().has("cellar")) {
                boolean cellar = element.getAsJsonObject().get("cellar").getAsBoolean();
                if (cellar) {
                    test = levelInfo -> levelInfo.isCellar();
                } else {
                    test = levelInfo -> !levelInfo.isCellar();
                }
            }
            if (element.getAsJsonObject().has("level")) {
                int level = element.getAsJsonObject().get("level").getAsInt();
                test = levelInfo -> levelInfo.isLevel(level);
            }
            if (element.getAsJsonObject().has("range")) {
                String range = element.getAsJsonObject().get("range").getAsString();
                String[] split = StringUtils.split(range, ',');
                try {
                    int l1 = Integer.parseInt(split[0]);
                    int l2 = Integer.parseInt(split[1]);
                    test = levelInfo -> levelInfo.isRange(l1, l2);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Bad range specification: <l1>,<l2>!");
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new RuntimeException("Bad range specification: <l1>,<l2>!");
                }
            }
            if (element.getAsJsonObject().has("minfloors")) {
                minFloors = element.getAsJsonObject().get("minfloors").getAsInt();
            }
            if (element.getAsJsonObject().has("mincellars")) {
                minCellars = element.getAsJsonObject().get("mincellars").getAsInt();
            }
            if (element.getAsJsonObject().has("maxfloors")) {
                maxFloors = element.getAsJsonObject().get("maxfloors").getAsInt();
            }
            if (element.getAsJsonObject().has("maxcellars")) {
                maxCellars = element.getAsJsonObject().get("maxcellars").getAsInt();
            }
            addPart(test, partName);
        }
    }

    @Override
    public JsonObject writeToJSon() {
        JsonObject object = new JsonObject();
        object.add("type", new JsonPrimitive("building"));
        object.add("name", new JsonPrimitive(name));
        JsonArray partArray = new JsonArray();
        for (Pair<Predicate<LevelInfo>, String> part : parts) {
            JsonObject partObject = new JsonObject();
            partObject.add("test", new JsonPrimitive("@todo"));
            partObject.add("part", new JsonPrimitive(part.getRight()));
            partArray.add(partObject);
        }
        object.add("parts", partArray);
        return object;
    }


    public Building addPart(Predicate<LevelInfo> test, String partName) {
        parts.add(Pair.of(test, partName));
        if (!partNames.contains(partName)) {
            partNames.add(partName);
        }
        return this;
    }

    public int getMaxFloors() {
        return maxFloors;
    }

    public int getMaxCellars() {
        return maxCellars;
    }

    public int getMinFloors() {
        return minFloors;
    }

    public int getMinCellars() {
        return minCellars;
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
        private final int floor;        // Level of the building with 0 being the ground floor. floor == floorsAboveGround means the top of the building section
        private final int floorsBelowGround;    // 0 means nothing below ground
        private final int floorsAboveGround;    // 1 means 1 floor above ground

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
            return floor >= floorsAboveGround;
        }

        public boolean isCellar() {
            return floor < 0;
        }

        public boolean isLevel(int l) {
            return level == l;
        }

        public boolean isRange(int l1, int l2) {
            return level >= l1 && level <= l2;
        }
    }
}
