package mcjty.lostcities.worldgen.lost.cityassets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mcjty.lostcities.api.ILostCityBuilding;
import mcjty.lostcities.setup.ModSetup;
import mcjty.lostcities.worldgen.lost.regassets.BuildingRE;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class Building implements ILostCityBuilding {

    private String name;

    private int minFloors = -1;         // -1 means default from level
    private int minCellars = -1;        // -1 means default frmo level
    private int maxFloors = -1;         // -1 means default from level
    private int maxCellars = -1;        // -1 means default frmo level
    private char fillerBlock;           // Block used to fill/close areas. Usually the block of the building itself
    private Character rubbleBlock;      // Block used for destroyed building rubble
    private float prefersLonely = 0.0f; // The chance this this building is alone. If 1.0f this building wants to be alone all the time

    private Palette localPalette = null;
    String refPaletteName;

    private final List<Pair<Predicate<ConditionContext>, String>> parts = new ArrayList<>();
    private final List<Pair<Predicate<ConditionContext>, String>> parts2 = new ArrayList<>();

    public Building(JsonObject object) {
        readFromJSon(object);
    }

    public Building(BuildingRE object) {
        name = object.getRegistryName().getPath(); // @todo temporary. Needs to be fully qualified
        minFloors = object.getMinFloors();
        minCellars = object.getMinCellars();
        maxFloors = object.getMaxFloors();
        maxCellars = object.getMaxCellars();
        prefersLonely = object.getPrefersLonely();
        fillerBlock = object.getFillerBlock();
        rubbleBlock = object.getRubbleBlock();
        if (object.getLocalPalette() != null) {
            localPalette = new Palette();
            localPalette.parsePaletteArray(object.getLocalPalette()); // @todo get the full palette instead
        } else if (object.getRefPaletteName() != null) {
            refPaletteName = object.getRefPaletteName();
        }

        readParts(this.parts, object.getParts());
        readParts(this.parts2, object.getParts2());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Palette getLocalPalette() {
        if (localPalette == null && refPaletteName != null) {
            localPalette = AssetRegistries.PALETTES.get(null, refPaletteName);  // @todo REG
            if (localPalette == null) {
                ModSetup.getLogger().error("Could not find palette '" + refPaletteName + "'!");
                throw new RuntimeException("Could not find palette '" + refPaletteName + "'!");
            }
        }
        return localPalette;
    }

    @Override
    public void readFromJSon(JsonObject object) {
        name = object.get("name").getAsString();

        if (object.has("minfloors")) {
            minFloors = object.get("minfloors").getAsInt();
        }
        if (object.has("mincellars")) {
            minCellars = object.get("mincellars").getAsInt();
        }
        if (object.has("maxfloors")) {
            maxFloors = object.get("maxfloors").getAsInt();
        }
        if (object.has("maxcellars")) {
            maxCellars = object.get("maxcellars").getAsInt();
        }
        if (object.has("preferslonely")) {
            prefersLonely = object.get("preferslonely").getAsFloat();
        }
        if (object.has("filler")) {
            fillerBlock = object.get("filler").getAsCharacter();
        } else {
            throw new RuntimeException("'filler' is required for building '" + name + "'!");
        }
        if (object.has("rubble")) {
            rubbleBlock = object.get("rubble").getAsCharacter();
        }

        if (object.has("palette")) {
            if (object.get("palette").isJsonArray()) {
                JsonArray palette = object.get("palette").getAsJsonArray();
                localPalette = new Palette();
                localPalette.parsePaletteArray(palette);
            } else {
                refPaletteName = object.get("palette").getAsString();
            }
        }

        readParts(object, this.parts, "parts");
        readParts(object, this.parts2, "parts2");
    }

    public void readParts(List<Pair<Predicate<ConditionContext>, String>> p, List<BuildingRE.PartRef> partRefs) {
        p.clear();
        if (partRefs == null) {
            return;
        }
        for (BuildingRE.PartRef partRef : partRefs) {
            String partName = partRef.getPart();
            Predicate<ConditionContext> test = ConditionContext.parseTest(partRef);
            addPart(test, partName, p);
        }
    }

    public void readParts(JsonObject object, List<Pair<Predicate<ConditionContext>, String>> p, String partSection) {
        p.clear();
        if (!object.has(partSection)) {
            return;
        }
        JsonArray partArray = object.get(partSection).getAsJsonArray();
        for (JsonElement element : partArray) {
            String partName = element.getAsJsonObject().get("part").getAsString();
            Predicate<ConditionContext> test = ConditionContext.parseTest(element);
            addPart(test, partName, p);
        }
    }

    public JsonObject writeToJSon() {
        JsonObject object = new JsonObject();
        object.add("type", new JsonPrimitive("building"));
        object.add("name", new JsonPrimitive(name));
        JsonArray partArray = new JsonArray();
        for (Pair<Predicate<ConditionContext>, String> part : parts) {
            JsonObject partObject = new JsonObject();
            partObject.add("test", new JsonPrimitive("@todo"));
            partObject.add("part", new JsonPrimitive(part.getRight()));
            partArray.add(partObject);
        }
        object.add("parts", partArray);
        return object;
    }


    public Building addPart(Predicate<ConditionContext> test, String partName,
                            List<Pair<Predicate<ConditionContext>, String>> parts) {
        parts.add(Pair.of(test, partName));
        return this;
    }

    @Override
    public float getPrefersLonely() {
        return prefersLonely;
    }

    @Override
    public int getMaxFloors() {
        return maxFloors;
    }

    @Override
    public int getMaxCellars() {
        return maxCellars;
    }

    @Override
    public int getMinFloors() {
        return minFloors;
    }

    @Override
    public int getMinCellars() {
        return minCellars;
    }

    @Override
    public char getFillerBlock() {
        return fillerBlock;
    }

    @Nullable
    @Override
    public Character getRubbleBlock() {
        return rubbleBlock;
    }

    public String getRandomPart(Random random, ConditionContext info) {
        List<String> partNames = new ArrayList<>();
        for (Pair<Predicate<ConditionContext>, String> pair : parts) {
            if (pair.getLeft().test(info)) {
                partNames.add(pair.getRight());
            }
        }
        if (partNames.isEmpty()) {
            return null;
        }
        return partNames.get(random.nextInt(partNames.size()));
    }

    public String getRandomPart2(Random random, ConditionContext info) {
        List<String> partNames = new ArrayList<>();
        for (Pair<Predicate<ConditionContext>, String> pair : parts2) {
            if (pair.getLeft().test(info)) {
                partNames.add(pair.getRight());
            }
        }
        if (partNames.isEmpty()) {
            return null;
        }
        return partNames.get(random.nextInt(partNames.size()));
    }

}
