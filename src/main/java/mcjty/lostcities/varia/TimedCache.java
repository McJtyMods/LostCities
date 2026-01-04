package mcjty.lostcities.varia;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntSupplier;

public class TimedCache<K, V> {

    private static class Entry<V> {
        private final V value;
        private long lastAccess;

        private Entry(V value, long lastAccess) {
            this.value = value;
            this.lastAccess = lastAccess;
        }
    }

    private final Map<K, Entry<V>> cache = new HashMap<>();
    private final IntSupplier ttlSecondsSupplier;
    private long nextCleanupAt;

    public TimedCache(IntSupplier ttlSecondsSupplier) {
        this.ttlSecondsSupplier = ttlSecondsSupplier;
        this.nextCleanupAt = System.currentTimeMillis();
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
            cache.remove(key);
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
        Entry<V> entry = cache.get(key);
        if (entry != null) {
            if (isExpired(entry, now)) {
                cache.remove(key);
            } else {
                entry.lastAccess = now;
                maybeCleanup(now);
                return entry.value;
            }
        }
        V value = supplier.apply(key);
        if (value != null) {
            cache.put(key, new Entry<>(value, now));
        }
        maybeCleanup(now);
        return value;
    }

    private boolean isExpired(Entry<V> entry, long now) {
        return now - entry.lastAccess >= getTtlMillis();
    }

    private void maybeCleanup(long now) {
        if (now < nextCleanupAt) {
            return;
        }
        cleanup(now);
        nextCleanupAt = now + getCleanupIntervalMillis();
    }

    private void cleanup(long now) {
        long ttlMillis = getTtlMillis();
        if (ttlMillis <= 0) {
            cache.clear();
            return;
        }
        Iterator<Map.Entry<K, Entry<V>>> iterator = cache.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<K, Entry<V>> entry = iterator.next();
            if (now - entry.getValue().lastAccess >= ttlMillis) {
                iterator.remove();
            }
        }
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
