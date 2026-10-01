package mcjty.lostcities.varia;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.IntSupplier;

public class TimedCache<K, V> {

    private static class Entry<V> {
        private final V value;
        private volatile long lastAccess;
        private final AtomicInteger activeUsers = new AtomicInteger();

        private Entry(V value, long lastAccess) {
            this.value = value;
            this.lastAccess = lastAccess;
        }
    }

    private final ConcurrentMap<K, Entry<V>> cache = new ConcurrentHashMap<>();
    private final IntSupplier ttlSecondsSupplier;
    private final AtomicLong nextCleanupAt;

    public TimedCache(IntSupplier ttlSecondsSupplier) {
        this.ttlSecondsSupplier = ttlSecondsSupplier;
        this.nextCleanupAt = new AtomicLong(System.currentTimeMillis());
    }

    public void clear() {
        cache.clear();
    }

    public V get(K key) {
        long now = System.currentTimeMillis();
        Entry<V> entry = cache.get(key);
        if (entry == null) {
            maybeCleanup(now);
            return null;
        }
        if (isExpired(entry, now)) {
            cache.remove(key, entry);
            maybeCleanup(now);
            return null;
        }
        entry.lastAccess = now;
        maybeCleanup(now);
        return entry.value;
    }

    public void put(K key, V value) {
        long now = System.currentTimeMillis();
        cache.put(key, new Entry<>(value, now));
        maybeCleanup(now);
    }

    public V computeIfAbsent(K key, Function<K, V> supplier) {
        long now = System.currentTimeMillis();
        Entry<V> cached = cache.get(key);
        if (cached != null && !isExpired(cached, now)) {
            cached.lastAccess = now;
            maybeCleanup(now);
            return cached.value;
        }
        Entry<V> entry = cache.compute(key, (k, current) -> {
            if (current != null && !isExpired(current, now)) {
                current.lastAccess = now;
                return current;
            }
            V value = supplier.apply(k);
            return value == null ? null : new Entry<>(value, now);
        });
        maybeCleanup(now);
        return entry == null ? null : entry.value;
    }

    public <R> R withPinnedValue(K key, Function<K, V> supplier, Function<V, R> action) {
        long now = System.currentTimeMillis();
        Entry<V> entry = cache.compute(key, (k, current) -> {
            if (current == null || isExpired(current, now) && current.activeUsers.get() == 0) {
                V value = supplier.apply(k);
                current = value == null ? null : new Entry<>(value, now);
            }
            if (current != null) {
                current.lastAccess = now;
                current.activeUsers.incrementAndGet();
            }
            return current;
        });
        if (entry == null) {
            maybeCleanup(now);
            return null;
        }
        try {
            return action.apply(entry.value);
        } finally {
            entry.lastAccess = System.currentTimeMillis();
            entry.activeUsers.decrementAndGet();
            maybeCleanup(entry.lastAccess);
        }
    }

    private boolean isExpired(Entry<V> entry, long now) {
        return now - entry.lastAccess >= getTtlMillis();
    }

    private void maybeCleanup(long now) {
        long scheduledAt = nextCleanupAt.get();
        if (now < scheduledAt || !nextCleanupAt.compareAndSet(scheduledAt, now + getCleanupIntervalMillis())) {
            return;
        }
        cleanup(now);
    }

    private void cleanup(long now) {
        long ttlMillis = getTtlMillis();
        if (ttlMillis <= 0) {
            cache.forEach((key, ignored) -> cache.computeIfPresent(key,
                    (k, entry) -> entry.activeUsers.get() == 0 ? null : entry));
            return;
        }
        cache.forEach((key, ignored) -> cache.computeIfPresent(key, (k, entry) ->
                now - entry.lastAccess >= ttlMillis && entry.activeUsers.get() == 0 ? null : entry));
    }

    private long getCleanupIntervalMillis() {
        long ttlMillis = getTtlMillis();
        return Math.max(1000L, ttlMillis / 2);
    }

    private long getTtlMillis() {
        int ttlSeconds = ttlSecondsSupplier.getAsInt();
        if (ttlSeconds <= 0) {
            return 0L;
        }
        return ttlSeconds * 1000L;
    }
}
