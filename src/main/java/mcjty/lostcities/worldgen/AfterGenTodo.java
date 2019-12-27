package mcjty.lostcities.worldgen;

import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Maintain things that have to be generated after worldgen
 */
public class AfterGenTodo {

    private static class AfterGenTodoDimension {
        private List<Consumer<World>> todo = new ArrayList<>();

        public void handleTodo(World world) {
            if (todo.isEmpty()) {
                return;
            }
            int size = todo.size();
            // Do 10% at a time
            int count = size / 10;
            if (count < 1) {
                count = 1;
            }
            for (int i = 0 ; i < count ; i++) {
                Consumer<World> runnable = todo.remove(0);
                runnable.accept(world);
            }
        }

        public void addTodo(Consumer<World> runnable) {
            todo.add(runnable);
        }
    }

    private static Map<DimensionType, AfterGenTodoDimension> todoPerDimension = new HashMap<>();

    public static void addTodo(DimensionType type, Consumer<World> runnable) {
        if (!todoPerDimension.containsKey(type)) {
            todoPerDimension.put(type, new AfterGenTodoDimension());
        }
        todoPerDimension.get(type).addTodo(runnable);
    }

    public static void handleTodo(World world) {
        AfterGenTodoDimension todoDimension = todoPerDimension.get(world.getDimension().getType());
        if (todoDimension != null) {
            todoDimension.handleTodo(world);
        }
    }
}
