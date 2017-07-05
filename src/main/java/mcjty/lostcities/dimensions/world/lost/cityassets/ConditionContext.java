package mcjty.lostcities.dimensions.world.lost.cityassets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Predicate;

public class ConditionContext {
    private final int level;        // Global level in world with 0 being to lowest possible level where a building section can be
    private final int floor;        // Level of the building with 0 being the ground floor. floor == floorsAboveGround means the top of the building section
    private final int floorsBelowGround;    // 0 means nothing below ground
    private final int floorsAboveGround;    // 1 means 1 floor above ground
    private final String part;

    public ConditionContext(int level, int floor, int floorsBelowGround, int floorsAboveGround, String part) {
        this.level = level;
        this.floor = floor;
        this.floorsBelowGround = floorsBelowGround;
        this.floorsAboveGround = floorsAboveGround;
        this.part = part;
    }

    private static Predicate<ConditionContext> combine(Predicate<ConditionContext> orig, Predicate<ConditionContext> newTest) {
        if (orig == null) {
            return newTest;
        }
        return levelInfo -> orig.test(levelInfo) && newTest.test(levelInfo);
    }

    public static Predicate<ConditionContext> parseTest(JsonElement element) {
        Predicate<ConditionContext> test = null;
        JsonObject obj = element.getAsJsonObject();
        if (obj.has("top")) {
            boolean top = obj.get("top").getAsBoolean();
            if (top) {
                test = combine(test, levelInfo -> levelInfo.isTopOfBuilding());
            } else {
                test = combine(test, levelInfo -> !levelInfo.isTopOfBuilding());
            }
        }
        if (obj.has("ground")) {
            boolean ground = obj.get("ground").getAsBoolean();
            if (ground) {
                test = combine(test, levelInfo -> levelInfo.isGroundFloor());
            } else {
                test = combine(test, levelInfo -> !levelInfo.isGroundFloor());
            }
        }
        if (obj.has("inpart")) {
            String part = obj.get("inpart").getAsString();
            test = combine(test, context -> part.equals(context.getPart()));
        }
        if (obj.has("cellar")) {
            boolean cellar = obj.get("cellar").getAsBoolean();
            if (cellar) {
                test = combine(test, levelInfo -> levelInfo.isCellar());
            } else {
                test = combine(test, levelInfo -> !levelInfo.isCellar());
            }
        }
        if (obj.has("floor")) {
            int level = obj.get("floor").getAsInt();
            test = combine(test, levelInfo -> levelInfo.isFloor(level));
        }
        if (obj.has("range")) {
            String range = obj.get("range").getAsString();
            String[] split = StringUtils.split(range, ',');
            try {
                int l1 = Integer.parseInt(split[0]);
                int l2 = Integer.parseInt(split[1]);
                test = combine(test, levelInfo -> levelInfo.isRange(l1, l2));
            } catch (NumberFormatException e) {
                throw new RuntimeException("Bad range specification: <l1>,<l2>!");
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new RuntimeException("Bad range specification: <l1>,<l2>!");
            }
        }
        if (test == null) {
            test = levelInfo -> true;
        }
        return test;
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

    public boolean isFloor(int l) {
        return floor == l;
    }

    public boolean isRange(int l1, int l2) {
        return floor >= l1 && floor <= l2;
    }

    public String getPart() {
        return part;
    }
}
