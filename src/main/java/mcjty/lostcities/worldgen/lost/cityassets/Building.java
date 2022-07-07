package mcjty.lostcities.worldgen.lost.cityassets;

import com.google.gson.JsonObject;
import mcjty.lostcities.api.ILostCityBuilding;
import mcjty.lostcities.setup.ModSetup;
import mcjty.lostcities.worldgen.lost.regassets.BuildingRE;
import net.minecraft.world.level.CommonLevelAccessor;
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
    public Palette getLocalPalette(CommonLevelAccessor level) {
        if (localPalette == null && refPaletteName != null) {
            localPalette = AssetRegistries.PALETTES.get(level, refPaletteName);
            if (localPalette == null) {
                ModSetup.getLogger().error("Could not find palette '" + refPaletteName + "'!");
                throw new RuntimeException("Could not find palette '" + refPaletteName + "'!");
            }
        }
        return localPalette;
    }

    @Override
    public void readFromJSon(JsonObject object) {
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
