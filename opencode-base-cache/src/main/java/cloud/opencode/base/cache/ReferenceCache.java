package cloud.opencode.base.cache;

import cloud.opencode.base.cache.model.RemovalCause;
import cloud.opencode.base.cache.spi.RemovalListener;
import cloud.opencode.base.cache.spi.StatsCounter;

import java.lang.ref.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Reference Cache - Memory-sensitive cache using weak/soft references
 * 引用缓存 - 使用弱引用/软引用的内存敏感缓存
 *
 * <p>Provides a cache that automatically releases entries when memory pressure
 * increases. Supports both weak references (collected on any GC) and soft
 * references (collected only when memory is low).</p>
 * <p>提供在内存压力增加时自动释放条目的缓存。支持弱引用（任何 GC 时回收）和
 * 软引用（仅在内存不足时回收）。</p>
 *
 * <p><strong>Reference Types | 引用类型:</strong></p>
 * <ul>
 *   <li>WEAK - Entries collected on any GC | 任何 GC 时回收条目</li>
 *   <li>SOFT - Entries collected when memory is low | 内存不足时回收条目</li>
 *   <li>WEAK_KEYS - Only keys use weak references | 仅键使用弱引用</li>
 *   <li>SOFT_VALUES - Only values use soft references | 仅值使用软引用</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Soft reference cache (memory-sensitive) - 软引用缓存（内存敏感）
 * Cache<String, byte[]> blobCache = ReferenceCache.<String, byte[]>builder("blobs")
 *     .referenceType(ReferenceType.SOFT)
 *     .removalListener((k, v, cause) -> log.info("Evicted: {}", k))
 *     .build();
 *
 * // Weak key cache (for canonicalization) - 弱键缓存（用于规范化）
 * Cache<ClassLoader, Metadata> metaCache = ReferenceCache.<ClassLoader, Metadata>builder("meta")
 *     .referenceType(ReferenceType.WEAK_KEYS)
 *     .build();
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) average - 时间复杂度: 平均 O(1)</li>
 *   <li>Memory: Adapts to available memory - 内存: 适应可用内存</li>
 * </ul>
 *
 * @param <K> key type | 键类型
 * @param <V> value type | 值类型
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Weak and soft reference support - 弱引用和软引用支持</li>
 *   <li>Automatic GC-based eviction - 基于 GC 的自动淘汰</li>
 *   <li>Memory-sensitive caching - 内存敏感缓存</li>
 *   <li>Removal listener support - 移除监听器支持</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses ConcurrentHashMap) - 线程安全: 是（使用 ConcurrentHashMap）</li>
 *   <li>Null-safe: Partial (null values not allowed) - 空值安全: 部分（不允许 null 值）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public final class ReferenceCache<K, V> implements Cache<K, V> {

    private static final System.Logger LOGGER = System.getLogger(ReferenceCache.class.getName());

    /**
     * Reference type for cache entries
     * 缓存条目的引用类型
     */
    public enum ReferenceType {
        /**
         * Both keys and values use weak references
         * 键和值都使用弱引用
         */
        WEAK,

        /**
         * Both keys and values use soft references
         * 键和值都使用软引用
         */
        SOFT,

        /**
         * Only keys use weak references, values are strong
         * 仅键使用弱引用，值是强引用
         */
        WEAK_KEYS,

        /**
         * Only values use soft references, keys are strong
         * 仅值使用软引用，键是强引用
         */
        SOFT_VALUES
    }

    private final String name;
    private final ConcurrentHashMap<Object, EntryReference<K, V>> store;
    private final ReferenceQueue<Object> referenceQueue;
    private final ReferenceType referenceType;
    private final RemovalListener<K, V> removalListener;
    private final StatsCounter statsCounter;
    private final ScheduledExecutorService cleanupExecutor;

    private ReferenceCache(Builder<K, V> builder) {
        this.name = builder.name;
        this.store = new ConcurrentHashMap<>();
        this.referenceQueue = new ReferenceQueue<>();
        this.referenceType = builder.referenceType;
        this.removalListener = builder.removalListener;
        this.statsCounter = builder.recordStats ? StatsCounter.concurrent() : StatsCounter.disabled();

        // Start background cleanup thread
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r ->
                Thread.ofVirtual().name("reference-cache-cleanup-" + name).unstarted(r));
        this.cleanupExecutor.scheduleAtFixedRate(this::processReferenceQueue, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * Create builder
     * 创建构建器
     *
     * @param name cache name | 缓存名称
     * @param <K>  key type | 键类型
     * @param <V>  value type | 值类型
     * @return builder | 构建器
     */
    public static <K, V> Builder<K, V> builder(String name) {
        return new Builder<>(name);
    }

    // ==================== Basic Operations ====================

    @Override
    public V get(K key) {
        Objects.requireNonNull(key, "key cannot be null");
        processReferenceQueue();

        Object lookupKey = wrapKeyForLookup(key);
        EntryReference<K, V> ref = store.get(lookupKey);

        if (ref == null) {
            statsCounter.recordMisses(1);
            return null;
        }

        V value = ref.getValue();
        if (value == null) {
            // Reference was cleared
            store.remove(lookupKey, ref);
            statsCounter.recordMisses(1);
            return null;
        }

        statsCounter.recordHits(1);
        return value;
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> loader) {
        V value = get(key);
        if (value != null) {
            return value;
        }

        long startTime = System.nanoTime();
        try {
            value = loader.apply(key);
            if (value != null) {
                put(key, value);
                statsCounter.recordLoadSuccess(System.nanoTime() - startTime);
            }
            return value;
        } catch (Exception e) {
            statsCounter.recordLoadFailure(System.nanoTime() - startTime);
            throw e;
        }
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys) {
        Map<K, V> result = new LinkedHashMap<>();
        for (K key : keys) {
            V value = get(key);
            if (value != null) {
                result.put(key, value);
            }
        }
        return result;
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys,
                            Function<? super Set<? extends K>, ? extends Map<K, V>> loader) {
        Map<K, V> result = new LinkedHashMap<>();
        Set<K> missingKeys = new LinkedHashSet<>();

        for (K key : keys) {
            V value = get(key);
            if (value != null) {
                result.put(key, value);
            } else {
                missingKeys.add(key);
            }
        }

        if (!missingKeys.isEmpty()) {
            Map<K, V> loaded = loader.apply(missingKeys);
            putAll(loaded);
            result.putAll(loaded);
        }

        return result;
    }

    @Override
    public void put(K key, V value) {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(value, "value cannot be null");
        processReferenceQueue();

        Object wrappedKey = wrapKey(key);
        EntryReference<K, V> oldRef = store.put(wrappedKey, createEntry(key, value));

        if (oldRef != null) {
            V oldValue = oldRef.getValue();
            if (oldValue != null && removalListener != null) {
                removalListener.onRemoval(key, oldValue, RemovalCause.REPLACED);
            }
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(value, "value cannot be null");
        processReferenceQueue();

        Object wrappedKey = wrapKey(key);
        EntryReference<K, V> existing = store.putIfAbsent(wrappedKey, createEntry(key, value));
        return existing == null || existing.getValue() == null;
    }

    @Override
    public void putWithTtl(K key, V value, Duration ttl) {
        // Reference cache doesn't support TTL - just delegate to put
        put(key, value);
    }

    @Override
    public void putAllWithTtl(Map<? extends K, ? extends V> map, Duration ttl) {
        putAll(map);
    }

    @Override
    public boolean putIfAbsentWithTtl(K key, V value, Duration ttl) {
        return putIfAbsent(key, value);
    }

    // ==================== Compute Operations ====================

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(remappingFunction, "remappingFunction cannot be null");
        processReferenceQueue();

        Object wrappedKey = wrapKeyForLookup(key);
        @SuppressWarnings("unchecked")
        V[] result = (V[]) new Object[1];
        store.compute(wrappedKey, (k, existingRef) -> {
            if (existingRef == null) {
                return null;
            }
            V oldValue = existingRef.getValue();
            if (oldValue == null) {
                return null; // Reference was cleared
            }
            V newValue = remappingFunction.apply(key, oldValue);
            if (newValue == null) {
                if (removalListener != null) {
                    removalListener.onRemoval(key, oldValue, RemovalCause.EXPLICIT);
                }
                return null;
            }
            result[0] = newValue;
            return createEntry(key, newValue);
        });
        return result[0];
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(remappingFunction, "remappingFunction cannot be null");
        processReferenceQueue();

        Object wrappedKey = wrapKeyForLookup(key);
        @SuppressWarnings("unchecked")
        V[] result = (V[]) new Object[1];
        store.compute(wrappedKey, (k, existingRef) -> {
            V oldValue = (existingRef != null) ? existingRef.getValue() : null;
            V newValue = remappingFunction.apply(key, oldValue);
            if (newValue == null) {
                if (oldValue != null && removalListener != null) {
                    removalListener.onRemoval(key, oldValue, RemovalCause.EXPLICIT);
                }
                return null;
            }
            result[0] = newValue;
            return createEntry(key, newValue);
        });
        return result[0];
    }

    @Override
    public V getAndRemove(K key) {
        Objects.requireNonNull(key, "key cannot be null");
        processReferenceQueue();

        Object lookupKey = wrapKeyForLookup(key);
        EntryReference<K, V> removed = store.remove(lookupKey);
        if (removed != null) {
            V value = removed.getValue();
            if (value != null && removalListener != null) {
                removalListener.onRemoval(key, value, RemovalCause.EXPLICIT);
            }
            return value;
        }
        return null;
    }

    @Override
    public V replace(K key, V value) {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(value, "value cannot be null");
        processReferenceQueue();

        Object wrappedKey = wrapKeyForLookup(key);
        @SuppressWarnings("unchecked")
        V[] result = (V[]) new Object[1];
        store.computeIfPresent(wrappedKey, (k, existingRef) -> {
            V oldValue = existingRef.getValue();
            if (oldValue == null) {
                return null; // Reference was cleared
            }
            result[0] = oldValue;
            return createEntry(key, value);
        });
        return result[0];
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(newValue, "newValue cannot be null");
        processReferenceQueue();

        Object wrappedKey = wrapKeyForLookup(key);
        boolean[] replaced = new boolean[1];
        store.computeIfPresent(wrappedKey, (k, existingRef) -> {
            V current = existingRef.getValue();
            if (Objects.equals(current, oldValue)) {
                replaced[0] = true;
                return createEntry(key, newValue);
            }
            return existingRef;
        });
        return replaced[0];
    }

    // ==================== Invalidation ====================

    @Override
    public void invalidate(K key) {
        Objects.requireNonNull(key, "key cannot be null");
        Object lookupKey = wrapKeyForLookup(key);
        EntryReference<K, V> removed = store.remove(lookupKey);

        if (removed != null) {
            V value = removed.getValue();
            if (value != null && removalListener != null) {
                removalListener.onRemoval(key, value, RemovalCause.EXPLICIT);
            }
        }
    }

    @Override
    public void invalidateAll(Iterable<? extends K> keys) {
        for (K key : keys) {
            invalidate(key);
        }
    }

    @Override
    public void invalidateAll() {
        for (Object key : store.keySet()) {
            EntryReference<K, V> ref = store.remove(key);
            if (ref != null && removalListener != null) {
                K originalKey = ref.getKey();
                V value = ref.getValue();
                if (originalKey != null && value != null) {
                    removalListener.onRemoval(originalKey, value, RemovalCause.EXPLICIT);
                }
            }
        }
    }

    // ==================== Query Operations ====================

    @Override
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    @Override
    public long size() {
        processReferenceQueue();
        return store.size();
    }

    @Override
    public long estimatedSize() {
        return store.size();
    }

    @Override
    public Set<K> keys() {
        processReferenceQueue();
        Set<K> result = new LinkedHashSet<>();
        for (EntryReference<K, V> ref : store.values()) {
            K key = ref.getKey();
            if (key != null && ref.getValue() != null) {
                result.add(key);
            }
        }
        return result;
    }

    @Override
    public Collection<V> values() {
        processReferenceQueue();
        List<V> result = new ArrayList<>();
        for (EntryReference<K, V> ref : store.values()) {
            V value = ref.getValue();
            if (value != null) {
                result.add(value);
            }
        }
        return result;
    }

    @Override
    public Set<Map.Entry<K, V>> entries() {
        processReferenceQueue();
        Set<Map.Entry<K, V>> result = new LinkedHashSet<>();
        for (EntryReference<K, V> ref : store.values()) {
            K key = ref.getKey();
            V value = ref.getValue();
            if (key != null && value != null) {
                result.add(Map.entry(key, value));
            }
        }
        return result;
    }

    @Override
    public ConcurrentMap<K, V> asMap() {
        return new ReferenceCacheMapView();
    }

    // ==================== Statistics & Management ====================

    @Override
    public CacheStats stats() {
        return statsCounter.snapshot();
    }

    @Override
    public void cleanUp() {
        processReferenceQueue();
    }

    @Override
    public AsyncCache<K, V> async() {
        return new AsyncReferenceCache();
    }

    @Override
    public String name() {
        return name;
    }

    /**
     * Close the cache and release resources
     * 关闭缓存并释放资源
     */
    public void close() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "ReferenceCache cleanup executor shutdown interrupted", e);
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        invalidateAll();
    }

    // ==================== Private Methods ====================

    private void processReferenceQueue() {
        Reference<?> ref;
        while ((ref = referenceQueue.poll()) != null) {
            if (ref instanceof EntryReference<?, ?> entryRef) {
                @SuppressWarnings("unchecked")
                EntryReference<K, V> typedRef = (EntryReference<K, V>) entryRef;
                Object key = typedRef.getKeyForRemoval();
                if (key != null) {
                    store.remove(key, typedRef);
                    statsCounter.recordEviction(1);

                    K originalKey = typedRef.getKey();
                    V value = typedRef.getValue();
                    if (removalListener != null && originalKey != null) {
                        removalListener.onRemoval(originalKey, value, RemovalCause.COLLECTED);
                    }
                }
            }
        }
    }

    private Object wrapKey(K key) {
        return switch (referenceType) {
            case WEAK, WEAK_KEYS -> new WeakKeyReference<>(key, referenceQueue);
            default -> key;
        };
    }

    private Object wrapKeyForLookup(K key) {
        return switch (referenceType) {
            case WEAK, WEAK_KEYS -> new LookupKey<>(key);
            default -> key;
        };
    }

    private EntryReference<K, V> createEntry(K key, V value) {
        return switch (referenceType) {
            case WEAK -> new WeakEntryReference<>(key, value, referenceQueue);
            case SOFT -> new SoftEntryReference<>(key, value, referenceQueue);
            case WEAK_KEYS -> new WeakKeyEntryReference<>(key, value, referenceQueue);
            case SOFT_VALUES -> new SoftValueEntryReference<>(key, value, referenceQueue);
        };
    }

    // ==================== Reference Classes ====================

    private interface EntryReference<K, V> {
        K getKey();
        V getValue();
        Object getKeyForRemoval();
    }

    private static class WeakEntryReference<K, V> extends WeakReference<V> implements EntryReference<K, V> {
        private final WeakReference<K> keyRef;
        private final int keyHash;

        WeakEntryReference(K key, V value, ReferenceQueue<Object> queue) {
            super(value, queue);
            this.keyRef = new WeakReference<>(key);
            this.keyHash = key.hashCode();
        }

        @Override
        public K getKey() {
            return keyRef.get();
        }

        @Override
        public V getValue() {
            return get();
        }

        @Override
        public Object getKeyForRemoval() {
            return new LookupKey<>(keyRef.get(), keyHash);
        }
    }

    private static class SoftEntryReference<K, V> extends SoftReference<V> implements EntryReference<K, V> {
        private final SoftReference<K> keyRef;
        private final int keyHash;

        SoftEntryReference(K key, V value, ReferenceQueue<Object> queue) {
            super(value, queue);
            this.keyRef = new SoftReference<>(key);
            this.keyHash = key.hashCode();
        }

        @Override
        public K getKey() {
            return keyRef.get();
        }

        @Override
        public V getValue() {
            return get();
        }

        @Override
        public Object getKeyForRemoval() {
            return new LookupKey<>(keyRef.get(), keyHash);
        }
    }

    private static class WeakKeyEntryReference<K, V> implements EntryReference<K, V> {
        private final WeakReference<K> keyRef;
        private final V value;
        private final int keyHash;

        WeakKeyEntryReference(K key, V value, ReferenceQueue<Object> queue) {
            this.keyRef = new WeakReference<>(key, queue);
            this.value = value;
            this.keyHash = key.hashCode();
        }

        @Override
        public K getKey() {
            return keyRef.get();
        }

        @Override
        public V getValue() {
            return getKey() != null ? value : null;
        }

        @Override
        public Object getKeyForRemoval() {
            return new LookupKey<>(keyRef.get(), keyHash);
        }
    }

    private static class SoftValueEntryReference<K, V> extends SoftReference<V> implements EntryReference<K, V> {
        private final K key;

        SoftValueEntryReference(K key, V value, ReferenceQueue<Object> queue) {
            super(value, queue);
            this.key = key;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return get();
        }

        @Override
        public Object getKeyForRemoval() {
            return key;
        }
    }

    private static class WeakKeyReference<K> extends WeakReference<K> {
        private final int hash;

        WeakKeyReference(K key, ReferenceQueue<Object> queue) {
            super(key, queue);
            this.hash = key.hashCode();
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj instanceof WeakKeyReference<?> other) {
                Object thisKey = get();
                Object otherKey = other.get();
                return thisKey != null && Objects.equals(thisKey, otherKey);
            }
            if (obj instanceof LookupKey<?> lookup) {
                Object thisKey = get();
                return thisKey != null && Objects.equals(thisKey, lookup.key);
            }
            return false;
        }
    }

    private record LookupKey<K>(K key, int hash) {
        LookupKey(K key) {
            this(key, key != null ? key.hashCode() : 0);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj instanceof LookupKey<?> other) {
                return Objects.equals(key, other.key);
            }
            if (obj instanceof WeakKeyReference<?> weakRef) {
                return Objects.equals(key, weakRef.get());
            }
            return Objects.equals(key, obj);
        }
    }

    // ==================== View Classes ====================

    private class ReferenceCacheMapView extends AbstractMap<K, V> implements ConcurrentMap<K, V> {
        @Override
        public V get(Object key) {
            @SuppressWarnings("unchecked")
            K k = (K) key;
            return ReferenceCache.this.get(k);
        }

        @Override
        public V put(K key, V value) {
            Objects.requireNonNull(key, "key cannot be null");
            Objects.requireNonNull(value, "value cannot be null");
            processReferenceQueue();
            Object wrappedKey = wrapKeyForLookup(key);
            @SuppressWarnings("unchecked")
            V[] result = (V[]) new Object[1];
            store.compute(wrappedKey, (k, existingRef) -> {
                if (existingRef != null) {
                    V oldValue = existingRef.getValue();
                    result[0] = oldValue;
                    if (oldValue != null && removalListener != null) {
                        removalListener.onRemoval(key, oldValue, RemovalCause.REPLACED);
                    }
                }
                return createEntry(key, value);
            });
            return result[0];
        }

        @Override
        public V remove(Object key) {
            @SuppressWarnings("unchecked")
            K k = (K) key;
            return ReferenceCache.this.getAndRemove(k);
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return ReferenceCache.this.entries();
        }

        @Override
        public V putIfAbsent(K key, V value) {
            V existing = get(key);
            if (existing == null) {
                ReferenceCache.this.putIfAbsent(key, value);
                return null;
            }
            return existing;
        }

        @Override
        public boolean remove(Object key, Object value) {
            @SuppressWarnings("unchecked")
            K k = (K) key;
            processReferenceQueue();
            Object wrappedKey = wrapKeyForLookup(k);
            boolean[] removed = new boolean[1];
            store.computeIfPresent(wrappedKey, (wk, existingRef) -> {
                V current = existingRef.getValue();
                if (Objects.equals(current, value)) {
                    removed[0] = true;
                    if (current != null && removalListener != null) {
                        removalListener.onRemoval(k, current, RemovalCause.EXPLICIT);
                    }
                    return null; // remove entry
                }
                return existingRef; // keep entry
            });
            return removed[0];
        }

        @Override
        public boolean replace(K key, V oldValue, V newValue) {
            return ReferenceCache.this.replace(key, oldValue, newValue);
        }

        @Override
        public V replace(K key, V value) {
            return ReferenceCache.this.replace(key, value);
        }
    }

    private class AsyncReferenceCache implements AsyncCache<K, V> {
        private static final Executor VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

        @Override
        public CompletableFuture<V> getAsync(K key) {
            return CompletableFuture.supplyAsync(() -> get(key), VIRTUAL_EXECUTOR);
        }

        @Override
        public CompletableFuture<V> getAsync(K key,
                                              BiFunction<? super K, ? super Executor, ? extends CompletableFuture<V>> loader) {
            return getAsync(key).thenCompose(v -> {
                if (v != null) return CompletableFuture.completedFuture(v);
                return loader.apply(key, VIRTUAL_EXECUTOR).thenApply(loaded -> {
                    if (loaded != null) put(key, loaded);
                    return loaded;
                });
            });
        }

        @Override
        public CompletableFuture<Map<K, V>> getAllAsync(Iterable<? extends K> keys) {
            return CompletableFuture.supplyAsync(() -> getAll(keys), VIRTUAL_EXECUTOR);
        }

        @Override
        public CompletableFuture<Void> putAsync(K key, V value) {
            return CompletableFuture.runAsync(() -> put(key, value), VIRTUAL_EXECUTOR);
        }

        @Override
        public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> map) {
            return CompletableFuture.runAsync(() -> putAll(map), VIRTUAL_EXECUTOR);
        }

        @Override
        public CompletableFuture<Void> invalidateAsync(K key) {
            return CompletableFuture.runAsync(() -> invalidate(key), VIRTUAL_EXECUTOR);
        }

        @Override
        public CompletableFuture<Void> invalidateAllAsync(Iterable<? extends K> keys) {
            return CompletableFuture.runAsync(() -> invalidateAll(keys), VIRTUAL_EXECUTOR);
        }

        @Override
        public Cache<K, V> sync() {
            return ReferenceCache.this;
        }
    }

    // ==================== Builder ====================

    /**
     * Builder for ReferenceCache
     *
     * @param <K> the key type | 键类型
     * @param <V> the value type | 值类型
     */
    public static class Builder<K, V> {
        private final String name;
        private ReferenceType referenceType = ReferenceType.SOFT;
        private RemovalListener<K, V> removalListener;
        private boolean recordStats = false;

        Builder(String name) {
            this.name = Objects.requireNonNull(name, "name cannot be null");
        }

        /**
         * Set reference type
         * 设置引用类型
         *
         * @param referenceType the reference type | 引用类型
         * @return this builder | 此构建器
         */
        public Builder<K, V> referenceType(ReferenceType referenceType) {
            this.referenceType = referenceType;
            return this;
        }

        /**
         * Set removal listener
         * 设置移除监听器
         *
         * @param listener the listener | 监听器
         * @return this builder | 此构建器
         */
        public Builder<K, V> removalListener(RemovalListener<K, V> listener) {
            this.removalListener = listener;
            return this;
        }

        /**
         * Enable statistics recording
         * 启用统计记录
         *
         * @return this builder | 此构建器
         */
        public Builder<K, V> recordStats() {
            this.recordStats = true;
            return this;
        }

        /**
         * Build the cache
         * 构建缓存
         *
         * @return cache | 缓存
         */
        public ReferenceCache<K, V> build() {
            return new ReferenceCache<>(this);
        }
    }
}
