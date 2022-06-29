package mcjty.lostcities.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class GlobalTodo {

    private static Map<ResourceKey<Level>, Map<BlockPos, Consumer<ServerLevel>>> todo = new HashMap<>();

    public static void cleanCache() {
        todo.clear();
    }

    public static void addTodo(ResourceKey<Level> level, BlockPos pos, Consumer<ServerLevel> code) {
        todo.computeIfAbsent(level, lvl -> new HashMap<>()).put(pos, code);
    }

    public static void executeAndClearTodo(ServerLevel level) {
        Map<BlockPos, Consumer<ServerLevel>> removed = todo.remove(level.dimension());
        if (removed != null) {
            removed.forEach((pos, code) -> code.accept(level));
        }
    }
}
