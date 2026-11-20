package mcjty.lostcities.setup;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.worldgen.IDimensionInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelData;

import javax.annotation.Nullable;

public final class CavernSpawn {

    // Keep the player's head below the roof at Y=127 in the shorter Lost Worlds cave preset.
    private static final int MAX_FLOOR_Y = 124;
    private static final int ALTERNATIVE_RADIUS = 16;

    private CavernSpawn() {
    }

    @Nullable
    public static BlockPos resolveWorldSpawn(ServerLevel level) {
        IDimensionInfo info = Registration.LOSTCITY_FEATURE.get().getDimensionInfo(level);
        if (info == null || !info.getProfile().isCavern()) {
            return null;
        }
        LevelData.RespawnData effective = level.getRespawnData();
        BlockPos spawn = effective.pos();
        if (!isSafe(level, spawn)) {
            spawn = findAlternative(level, info.getProfile(), spawn);
            level.setRespawnData(LevelData.RespawnData.of(level.dimension(), spawn, effective.yaw(), effective.pitch()));
        }
        return spawn;
    }

    private static boolean isSafe(Level level, BlockPos feet) {
        BlockPos floor = feet.below();
        if (floor.getY() < level.getMinY() || floor.getY() > maxFloorY(level)
                || !level.getWorldBorder().isWithinBounds(feet)) {
            return false;
        }
        BlockState state = level.getBlockState(floor);
        return !state.is(Blocks.BEDROCK) && !state.is(Blocks.MAGMA_BLOCK)
                && Block.isFaceFull(state.getCollisionShape(level, floor), Direction.UP)
                && level.getBlockState(feet).isAir() && level.getBlockState(feet.above()).isAir();
    }

    @Nullable
    static BlockPos findInColumn(Level level, int x, int z, int preferredFloorY, boolean searchBelow) {
        if (!level.getWorldBorder().isWithinBounds(new BlockPos(x, 0, z))) {
            return null;
        }
        int minY = level.getMinY();
        int maxY = maxFloorY(level);
        int startY = Mth.clamp(preferredFloorY, minY, maxY);
        BlockPos.MutableBlockPos feet = new BlockPos.MutableBlockPos();
        for (int y = startY; y <= maxY; y++) {
            if (isSafe(level, feet.set(x, y + 1, z))) {
                return feet.immutable();
            }
        }
        if (searchBelow) {
            for (int y = startY - 1; y >= minY; y--) {
                if (isSafe(level, feet.set(x, y + 1, z))) {
                    return feet.immutable();
                }
            }
        }
        return null;
    }

    static BlockPos findAlternative(ServerLevel level, LostCityProfile profile, BlockPos origin) {
        int preferredFloorY = profile.GROUNDLEVEL - 5;
        for (BlockPos column : BlockPos.withinManhattan(origin, ALTERNATIVE_RADIUS, 0, ALTERNATIVE_RADIUS)) {
            BlockPos spawn = findInColumn(level, column.getX(), column.getZ(), preferredFloorY, true);
            if (spawn != null) {
                ModSetup.getLogger().warn("Using a nearby cavern spawn at {}", spawn.toShortString());
                return spawn;
            }
        }

        int minY = level.getMinY() + 1;
        int maxY = Math.min(MAX_FLOOR_Y + 1, level.getMaxY() - 2);
        BlockPos center = origin.atY(Mth.clamp(preferredFloorY + 1, minY, maxY));
        for (BlockPos candidate : BlockPos.withinManhattan(center, 2, maxY - minY, 2)) {
            if (candidate.getY() >= minY && candidate.getY() <= maxY && buildShelter(level, candidate)) {
                ModSetup.getLogger().warn("No safe cavern spawn found nearby. Built a spawn shelter at {}", candidate.toShortString());
                return candidate.immutable();
            }
        }
        throw new IllegalStateException("No room for a safe cavern spawn shelter near " + origin.toShortString());
    }

    private static boolean buildShelter(ServerLevel level, BlockPos feet) {
        Iterable<BlockPos> room = BlockPos.betweenClosed(feet.offset(-2, -1, -2), feet.offset(2, 2, 2));
        for (BlockPos pos : room) {
            if (!level.getWorldBorder().isWithinBounds(pos)) {
                return false;
            }
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.BEDROCK) || state.hasBlockEntity()) {
                return false;
            }
        }
        for (BlockPos pos : room) {
            boolean interior = Math.abs(pos.getX() - feet.getX()) < 2 && Math.abs(pos.getZ() - feet.getZ()) < 2
                    && (pos.getY() == feet.getY() || pos.getY() == feet.getY() + 1);
            level.setBlock(pos, (interior ? Blocks.AIR : Blocks.COBBLESTONE).defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        return isSafe(level, feet);
    }

    private static int maxFloorY(Level level) {
        return Math.min(MAX_FLOOR_Y, level.getMaxY() - 2);
    }
}
