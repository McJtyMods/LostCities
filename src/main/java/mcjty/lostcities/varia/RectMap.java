package mcjty.lostcities.varia;

import mcjty.lostcities.api.MultiPos;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.MultiBuilding;
import mcjty.lostcities.worldgen.lost.cityassets.Scattered;
import mcjty.lostcities.worldgen.lost.cityassets.WorldStyle;
import mcjty.lostcities.worldgen.lost.regassets.data.ScatteredReference;
import net.minecraft.world.level.CommonLevelAccessor;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A 2 dimensional map where you can allocate rectangular areas.
 * Maximum supported area is 2x2
 */
public class RectMap {

    private final float chunkIsTopLeftChance;   // Chance a chunk is the top left of something
    private final long seed;
    private final List<WeightEntry> weightEntries = new ArrayList<>();

    private static record WeightEntry(float weight, int w, int h) {
    }

    public RectMap(long seed, CommonLevelAccessor levelAccessor, WorldStyle worldStyle) {
        this.seed = seed;
        this.chunkIsTopLeftChance = worldStyle.getScatterChance();
        int totalweight = 0;
        for (ScatteredReference reference : worldStyle.getScatteredReferences()) {
            totalweight += reference.getWeight();
        }

        for (ScatteredReference reference : worldStyle.getScatteredReferences()) {
            String scatterName = reference.getName();
            Scattered scattered = AssetRegistries.SCATTERED.get(levelAccessor, scatterName);
            if (scattered == null) {
                throw new RuntimeException("Cannot find scattered '" + scatterName + "'!");
            }
            int w;
            int h;
            if (scattered.getMultibuilding() != null) {
                MultiBuilding multiBuilding = AssetRegistries.MULTI_BUILDINGS.get(levelAccessor, scattered.getMultibuilding());
                if (multiBuilding == null) {
                    throw new RuntimeException("Cannot find multibuilding '" + scattered.getMultibuilding() + "'!");
                }
                w = multiBuilding.getDimX();
                h = multiBuilding.getDimZ();
            } else {
                w = h = 1;
            }

            weightEntries.add(new WeightEntry((float) reference.getWeight() / totalweight, w, h));
        }
    }

    /**
     * Check if a given position is part of an area
     */
    @Nullable
    public MultiPos isAreaPart(int x, int z) {
        for (int dx = -1 ; dx <= 0 ; dx++) {
            for (int dz = -1 ; dz <= 0 ; dz++) {
                if (isTopLeftSafe(x+dx, z+dz)) {
                    Pair<Integer, Integer> size = getTopLeftSize(x + dx, z + dz);
                    if ((-dx) < size.getLeft() && (-dz) < size.getRight()) {
                        return new MultiPos(-dx, -dz, size.getLeft(), size.getRight());
                    }
                }
            }
        }
        return null;
    }

    // Give a coordinate that is a valid top-left coordinate. Return the size of the area here
    private Pair<Integer, Integer> getTopLeftSize(int x, int z) {
        Random random = getRandom(x, z, 87464459617L, 17463457939L);
        return Pair.of(random.nextInt(2) + 1, random.nextInt(2) + 1);
    }

    /**
     * Check if a coordinate is a top left chunk purely based on chance without
     * checking if it is actually possible given nearby other chunks
     */
    private boolean isTopLeftRaw(int x, int z) {
        return getRandom(x, z, 374464457363L, 87464457403L).nextFloat() < chunkIsTopLeftChance;
    }

    /**
     * Check if a coordinate is a top left chunk while also checking no other
     * top left chunk might interphere
     */
    private boolean isTopLeftSafe(int x, int z) {
        if (!isTopLeftRaw(x, z)) {
            return false;
        }
        for (int dx = -1 ; dx <= 1 ; dx++) {
            for (int dz = -1 ; dz <= 1 ; dz++) {
                if (dx < 0 || dz < 0) {
                    if (isTopLeftRaw(x+dx, z+dz)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private Random getRandom(int x, int z, long l1, long l2) {
        Random rand = new QualityRandom(seed + z * l1 + x * l2);
        rand.nextFloat();
        rand.nextFloat();
        return rand;
    }


}
