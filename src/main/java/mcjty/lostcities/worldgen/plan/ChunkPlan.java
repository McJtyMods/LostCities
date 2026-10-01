package mcjty.lostcities.worldgen.plan;

import mcjty.lostcities.api.LostChunkCharacteristics;
import mcjty.lostcities.worldgen.highway.HighwayInfo;
import mcjty.lostcities.worldgen.lost.BuildingInfo;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

final class ChunkPlan {

    private final ReentrantLock infrastructureLock = new ReentrantLock();
    private final ReentrantLock characteristicsLock = new ReentrantLock();
    private final ReentrantLock chunkLock = new ReentrantLock();

    private volatile HighwayInfo highway;
    private volatile LostChunkCharacteristics characteristics;
    private volatile BuildingInfo buildingInfo;

    HighwayInfo highway(Supplier<HighwayInfo> supplier) {
        HighwayInfo result = highway;
        if (result != null) {
            return result;
        }
        infrastructureLock.lock();
        try {
            if (highway == null) {
                highway = supplier.get();
            }
            return highway;
        } finally {
            infrastructureLock.unlock();
        }
    }

    LostChunkCharacteristics characteristics(Supplier<Staged<LostChunkCharacteristics>> supplier) {
        LostChunkCharacteristics result = characteristics;
        if (result != null) {
            return result;
        }
        characteristicsLock.lock();
        try {
            if (characteristics != null) {
                return characteristics;
            }
            Staged<LostChunkCharacteristics> staged = supplier.get();
            if (staged.cacheable()) {
                characteristics = staged.value();
            }
            return staged.value();
        } finally {
            characteristicsLock.unlock();
        }
    }

    BuildingInfo buildingInfo(Supplier<Staged<BuildingInfo>> supplier) {
        BuildingInfo result = buildingInfo;
        if (result != null) {
            return result;
        }
        chunkLock.lock();
        try {
            if (buildingInfo != null) {
                return buildingInfo;
            }
            Staged<BuildingInfo> staged = supplier.get();
            if (staged.cacheable()) {
                buildingInfo = staged.value();
            }
            return staged.value();
        } finally {
            chunkLock.unlock();
        }
    }
}
