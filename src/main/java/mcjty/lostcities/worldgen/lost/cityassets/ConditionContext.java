package mcjty.lostcities.worldgen.lost.cityassets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mcjty.lostcities.worldgen.lost.regassets.BuildingRE;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Predicate;

public abstract class ConditionContext {
    private final int level;        // Global level in world with 0 being to lowest possible level where a building section can be
    private final int floor;        // Level of the building with 0 being the ground floor. floor == floorsAboveGround means the top of the building section
    private final int floorsBelowGround;    // 0 means nothing below ground
    private final int floorsAboveGround;    // 1 means 1 floor above ground
    private final String part;
    private final String building;
    private final int chunkX;
    private final int chunkZ;

    public ConditionContext(int level, int floor, int floorsBelowGround, int floorsAboveGround, String part, String building, int chunkX, int chunkZ) {
        this.level = level;
        this.floor = floor;
        this.floorsBelowGround = floorsBelowGround;
        this.floorsAboveGround = floorsAboveGround;
        this.part = part;
        this.building = building;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    private static Predicate<ConditionContext> combine(Predicate<ConditionContext> orig, Predicate<ConditionContext> newTest) {
        if (orig == null) {
            return newTest;
        }
        return levelInfo -> orig.test(levelInfo) && newTest.test(levelInfo);
    }

    public static Predicate<ConditionContext> parseTest(BuildingRE.PartRef element) {
        Predicate<ConditionContext> test = null;
        if (element.getTop() != null) {
            boolean top = element.getTop();
            if (top) {
                test = combine(test, ConditionContext::isTopOfBuilding);
            } else {
                test = combine(test, levelInfo -> !levelInfo.isTopOfBuilding());
            }
        }
        if (element.getGround() != null) {
            boolean ground = element.getGround();
            if (ground) {
                test = combine(test, ConditionContext::isGroundFloor);
            } else {
                test = combine(test, levelInfo -> !levelInfo.isGroundFloor());
            }
        }
        if (element.getIsbuilding() != null) {
            boolean ground = element.getIsbuilding();
            if (ground) {
                test = combine(test, ConditionContext::isBuilding);
            } else {
                test = combine(test, levelInfo -> !levelInfo.isBuilding());
            }
        }
        if (element.getIssphere() != null) {
            boolean ground = element.getIssphere();
            if (ground) {
                test = combine(test, ConditionContext::isSphere);
            } else {
                test = combine(test, levelInfo -> !levelInfo.isSphere());
            }
        }
        if (element.getChunkx() != null) {
            int chunkX = element.getChunkx();
            test = combine(test, context -> chunkX == context.getChunkX());
        }
        if (element.getChunkz() != null) {
            int chunkZ = element.getChunkz();
            test = combine(test, context -> chunkZ == context.getChunkZ());
        }
        if (element.getInpart() != null) {
            String part = element.getInpart();
            test = combine(test, context -> part.equals(context.getPart()));
        }
        if (element.getInbuilding() != null) {
            String building = element.getInbuilding();
            test = combine(test, context -> building.equals(context.getBuilding()));
        }
        if (element.getInbiome() != null) {
            String biome = element.getInbiome();
            test = combine(test, context -> biome.equals(context.getBiome().toString()));
        }
        if (element.getCellar() != null) {
            boolean cellar = element.getCellar();
            if (cellar) {
                test = combine(test, ConditionContext::isCellar);
            } else {
                test = combine(test, levelInfo -> !levelInfo.isCellar());
            }
        }
        if (element.getFloor() != null) {
            int level = element.getFloor();
            test = combine(test, levelInfo -> levelInfo.isFloor(level));
        }
        if (element.getRange() != null) {
            String range = element.getRange();
            String[] split = StringUtils.split(range, ',');
            try {
                int l1 = Integer.parseInt(split[0]);
                int l2 = Integer.parseInt(split[1]);
                test = combine(test, levelInfo -> levelInfo.isRange(l1, l2));
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                throw new RuntimeException("Bad range specification: <l1>,<l2>!");
            }
        }
        if (test == null) {
            test = conditionContext -> true;
        }
        return test;
    }

    public static Predicate<ConditionContext> parseTest(JsonElement element) {
        Predicate<ConditionContext> test = null;
        JsonObject obj = element.getAsJsonObject();
        if (obj.has("top")) {
            boolean top = obj.get("top").getAsBoolean();
            if (top) {
                test = combine(test, ConditionContext::isTopOfBuilding);
            } else {
                test = combine(test, levelInfo -> !levelInfo.isTopOfBuilding());
            }
        }
        if (obj.has("ground")) {
            boolean ground = obj.get("ground").getAsBoolean();
            if (ground) {
                test = combine(test, ConditionContext::isGroundFloor);
            } else {
                test = combine(test, levelInfo -> !levelInfo.isGroundFloor());
            }
        }
        if (obj.has("isbuilding")) {
            boolean ground = obj.get("isbuilding").getAsBoolean();
            if (ground) {
                test = combine(test, ConditionContext::isBuilding);
            } else {
                test = combine(test, levelInfo -> !levelInfo.isBuilding());
            }
        }
        if (obj.has("issphere")) {
            boolean ground = obj.get("issphere").getAsBoolean();
            if (ground) {
                test = combine(test, ConditionContext::isSphere);
            } else {
                test = combine(test, levelInfo -> !levelInfo.isSphere());
            }
        }
        if (obj.has("chunkx")) {
            int chunkX = obj.get("chunkx").getAsInt();
            test = combine(test, context -> chunkX == context.getChunkX());
        }
        if (obj.has("chunkz")) {
            int chunkZ = obj.get("chunkz").getAsInt();
            test = combine(test, context -> chunkZ == context.getChunkZ());
        }
        if (obj.has("inpart")) {
            String part = obj.get("inpart").getAsString();
            test = combine(test, context -> part.equals(context.getPart()));
        }
        if (obj.has("inbuilding")) {
            String building = obj.get("inbuilding").getAsString();
            test = combine(test, context -> building.equals(context.getBuilding()));
        }
        if (obj.has("inbiome")) {
            String biome = obj.get("inbiome").getAsString();
            test = combine(test, context -> biome.equals(context.getBiome().toString()));
        }
        if (obj.has("cellar")) {
            boolean cellar = obj.get("cellar").getAsBoolean();
            if (cellar) {
                test = combine(test, ConditionContext::isCellar);
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
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                throw new RuntimeException("Bad range specification: <l1>,<l2>!");
            }
        }
        if (test == null) {
            test = conditionContext -> true;
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

    public boolean isBuilding() {
        return !"<none>".equals(building);
    }

    public abstract boolean isSphere();

    public abstract ResourceLocation getBiome();

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

    public String getBuilding() {
        return building;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }
}
