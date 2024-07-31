package mcjty.lostcities.varia;

import net.minecraft.core.BlockPos;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.BiConsumer;

public class TodoQueue<T> {

    private final Queue<Todo<T>> queue = new ArrayDeque<>();

    private record Todo<D>(BlockPos pos, D data) {
    }

    public void add(BlockPos pos, T data) {
        queue.add(new Todo<>(pos, data));
    }

    public T get() {
        Todo<T> todo = queue.poll();
        return todo == null ? null : todo.data;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public void forEach(BiConsumer<BlockPos, T> consumer) {
        queue.forEach(todo -> consumer.accept(todo.pos, todo.data));
    }

    // Execute a BiConsumer on the N first elements in the queue
    public int forEach(int n, BiConsumer<BlockPos, T> consumer) {
        int cnt = 0;
        for (int i = 0; i < n; i++) {
            Todo<T> todo = queue.poll();
            if (todo == null) {
                break;
            }
            consumer.accept(todo.pos, todo.data);
            cnt++;
        }
        return cnt;
    }
}
