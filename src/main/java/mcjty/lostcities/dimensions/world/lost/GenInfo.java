package mcjty.lostcities.dimensions.world.lost;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class GenInfo {
    private final List<BlockPos> randomFeatures = new ArrayList<>();

    public void addRandomFeatures(BlockPos p) {
        randomFeatures.add(p);
    }

    public List<BlockPos> getRandomFeatures() {
        return randomFeatures;
    }
}
