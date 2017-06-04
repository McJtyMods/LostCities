package mcjty.lostcities.dimensions.world.lost;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenInfo {
    private final Map<BlockPos,Integer> spawnerType = new HashMap<>();
    private final List<BlockPos> chest = new ArrayList<>();
    private final List<BlockPos> modularStorages = new ArrayList<>();
    private final List<BlockPos> randomFeatures = new ArrayList<>();
    private final List<BlockPos> randomRFToolsMachines = new ArrayList<>();

    public void addSpawnerType(BlockPos p, int type) {
        spawnerType.put(p, type);
    }

    public void addChest(BlockPos p) {
        chest.add(p);
    }

    public void addModularStorage(BlockPos p) {
        modularStorages.add(p);
    }

    public void addRandomFeatures(BlockPos p) {
        randomFeatures.add(p);
    }

    public void addRandomRFToolsMachine(BlockPos p) {
        randomRFToolsMachines.add(p);
    }

    public Map<BlockPos, Integer> getSpawnerType() {
        return spawnerType;
    }

    public List<BlockPos> getChest() {
        return chest;
    }

    public List<BlockPos> getModularStorages() {
        return modularStorages;
    }

    public List<BlockPos> getRandomFeatures() {
        return randomFeatures;
    }

    public List<BlockPos> getRandomRFToolsMachines() {
        return randomRFToolsMachines;
    }
}
