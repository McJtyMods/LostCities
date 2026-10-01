package mcjty.lostcities.worldgen.highway;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

final class BoundedFutureCache<K, V> {
    private final int maximumSize;
    private final ConcurrentHashMap<K, CompletableFuture<V>> values = new ConcurrentHashMap<>();
    private final AtomicBoolean trimming = new AtomicBoolean();

    BoundedFutureCache(int maximumSize) {
        if (maximumSize < 1) {
            throw new IllegalArgumentException("Maximum cache size must be positive");
        }
        this.maximumSize = maximumSize;
    }

    CompletableFuture<V> computeIfAbsent(K key, Function<K, CompletableFuture<V>> factory) {
        CompletableFuture<V> existing = values.get(key);
        if (existing != null) {
            return existing;
        }

        CompletableFuture<V> created = new CompletableFuture<>();
        existing = values.putIfAbsent(key, created);
        if (existing != null) {
            return existing;
        }

        CompletableFuture<V> scheduled;
        try {
            scheduled = Objects.requireNonNull(factory.apply(key), "Future factory returned null");
        } catch (Throwable e) {
            values.remove(key, created);
            created.completeExceptionally(e);
            return created;
        }

        scheduled.whenComplete((value, error) -> {
            if (error == null) {
                created.complete(value);
            } else {
                values.remove(key, created);
                created.completeExceptionally(error);
            }
            trim();
        });
        return created;
    }

    void clear() {
        values.clear();
    }

    int size() {
        return values.size();
    }

    private void trim() {
        if (values.size() <= maximumSize || !trimming.compareAndSet(false, true)) {
            return;
        }
        boolean retry;
        try {
            Iterator<Map.Entry<K, CompletableFuture<V>>> iterator = values.entrySet().iterator();
            while (values.size() > maximumSize && iterator.hasNext()) {
                Map.Entry<K, CompletableFuture<V>> entry = iterator.next();
                CompletableFuture<V> future = entry.getValue();
                if (future.isDone()) {
                    values.remove(entry.getKey(), future);
                }
            }
        } finally {
            trimming.set(false);
            retry = values.size() > maximumSize && values.values().stream().anyMatch(CompletableFuture::isDone);
        }
        if (retry) {
            trim();
        }
    }
}
