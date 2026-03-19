package cloud.opencode.base.cache.ttl;

import cloud.opencode.base.cache.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * Variable TTL Cache - Cache with per-entry TTL based on policies
 * 可变 TTL 缓存 - 基于策略的每条目 TTL 缓存
 *
 * <p>Allows different TTLs for different entries based on key patterns,
 * value types, or custom logic. Supports TTL decay for cold data.</p>
 * <p>允许根据键模式、值类型或自定义逻辑为不同条目设置不同的 TTL。
 * 支持冷数据的 TTL 衰减。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Per-entry TTL - 每条目 TTL</li>
 *   <li>Pattern-based TTL - 基于模式的 TTL</li>
 *   <li>Value-based TTL - 基于值的 TTL</li>
 *   <li>TTL decay support - TTL 衰减支持</li>
 *   <li>Access tracking - 访问跟踪</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create with pattern-based TTL
 * VariableTtlCache<String, User> cache = VariableTtlCache.<String, User>wrap(baseCache)
 *     .ttlPolicy(TtlPolicy.<String, User>builder()
 *         .pattern("session:*", Duration.ofHours(1))
 *         .pattern("user:*", Duration.ofMinutes(30))
 *         .defaultTtl(Duration.ofMinutes(10))
 *         .build())
 *     .build();
 *
 * // Create with decay
 * VariableTtlCache<String, User> cache = VariableTtlCache.<String, User>wrap(baseCache)
 *     .ttlPolicy(TtlPolicy.fixed(Duration.ofHours(1)))
 *     .decayPolicy(TtlDecayPolicy.linear(
 *         Duration.ofHours(1),
 *         Duration.ofMinutes(5),
 *         10))
 *     .build();
 * }</pre>
 *
 * @param <K> key type | 键类型
 * @param <V> value type | 值类型
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial (null values not allowed) - 空值安全: 部分（不允许 null 值）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V2.0.5
 */
public class VariableTtlCache<K, V> implements Cache<K, V> {

    private final Cache<K, V> delegate;
    private final TtlPolicy<K, V> ttlPolicy;
    private final TtlDecayPolicy decayPolicy;
    private final ConcurrentMap<K, AtomicLong> accessCounts;
    private final boolean trackAccess;

    private VariableTtlCache(Cache<K, V> delegate, TtlPolicy<K, V> ttlPolicy,
                            TtlDecayPolicy decayPolicy, boolean trackAccess) {
        this.delegate = delegate;
        this.ttlPolicy = ttlPolicy;
        this.decayPolicy = decayPolicy;
        this.trackAccess = trackAccess;
        this.accessCounts = trackAccess ? new ConcurrentHashMap<>() : null;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create builder to wrap an existing cache
     * 创建构建器以包装现有缓存
     *
     * @param cache base cache | 基础缓存
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @return builder | 构建器
     */
    public static <K, V> Builder<K, V> wrap(Cache<K, V> cache) {
        return new Builder<>(cache);
    }

    // ==================== Cache Operations | 缓存操作 ====================

    @Override
    public V get(K key) {
        V value = delegate.get(key);
        if (value != null && trackAccess) {
            incrementAccess(key);
        }
        return value;
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> loader) {
        V value = delegate.get(key, k -> {
            V loaded = loader.apply(k);
            return loaded;
        });
        if (value != null && trackAccess) {
            incrementAccess(key);
        }
        return value;
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys) {
        Map<K, V> result = delegate.getAll(keys);
        if (trackAccess) {
            for (K key : result.keySet()) {
                incrementAccess(key);
            }
        }
        return result;
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys,
                           Function<? super Set<? extends K>, ? extends Map<K, V>> loader) {
        Map<K, V> result = delegate.getAll(keys, loader);
        if (trackAccess) {
            for (K key : result.keySet()) {
                incrementAccess(key);
            }
        }
        return result;
    }

    @Override
    public void put(K key, V value) {
        Duration ttl = calculateTtl(key, value);
        if (ttl != null) {
            delegate.putWithTtl(key, value, ttl);
        } else {
            delegate.put(key, value);
        }
        if (trackAccess) {
            resetAccess(key);
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
        Duration ttl = calculateTtl(key, value);
        boolean result;
        if (ttl != null) {
            result = delegate.putIfAbsentWithTtl(key, value, ttl);
        } else {
            result = delegate.putIfAbsent(key, value);
        }
        if (result && trackAccess) {
            resetAccess(key);
        }
        return result;
    }

    @Override
    public void putWithTtl(K key, V value, Duration ttl) {
        // Explicit TTL overrides policy
        delegate.putWithTtl(key, value, ttl);
        if (trackAccess) {
            resetAccess(key);
        }
    }

    @Override
    public void putAllWithTtl(Map<? extends K, ? extends V> map, Duration ttl) {
        delegate.putAllWithTtl(map, ttl);
        if (trackAccess) {
            for (K key : map.keySet()) {
                resetAccess(key);
            }
        }
    }

    @Override
    public boolean putIfAbsentWithTtl(K key, V value, Duration ttl) {
        boolean result = delegate.putIfAbsentWithTtl(key, value, ttl);
        if (result && trackAccess) {
            resetAccess(key);
        }
        return result;
    }

    @Override
    public void invalidate(K key) {
        delegate.invalidate(key);
        if (trackAccess) {
            accessCounts.remove(key);
        }
    }

    @Override
    public void invalidateAll(Iterable<? extends K> keys) {
        delegate.invalidateAll(keys);
        if (trackAccess) {
            for (K key : keys) {
                accessCounts.remove(key);
            }
        }
    }

    @Override
    public void invalidateAll() {
        delegate.invalidateAll();
        if (trackAccess) {
            accessCounts.clear();
        }
    }

    @Override
    public boolean containsKey(K key) {
        return delegate.containsKey(key);
    }

    @Override
    public long size() {
        return delegate.size();
    }

    @Override
    public long estimatedSize() {
        return delegate.estimatedSize();
    }

    @Override
    public Set<K> keys() {
        return delegate.keys();
    }

    @Override
    public Collection<V> values() {
        return delegate.values();
    }

    @Override
    public Set<Map.Entry<K, V>> entries() {
        return delegate.entries();
    }

    @Override
    public ConcurrentMap<K, V> asMap() {
        return delegate.asMap();
    }

    @Override
    public CacheStats stats() {
        return delegate.stats();
    }

    @Override
    public CacheMetrics metrics() {
        return delegate.metrics();
    }

    @Override
    public void cleanUp() {
        delegate.cleanUp();
    }

    @Override
    public AsyncCache<K, V> async() {
        return delegate.async();
    }

    @Override
    public String name() {
        return delegate.name();
    }

    // ==================== TTL Calculation | TTL 计算 ====================

    private Duration calculateTtl(K key, V value) {
        Duration baseTtl = ttlPolicy.calculateTtl(key, value);
        if (baseTtl == null || decayPolicy == null) {
            return baseTtl;
        }
        long accessCount = getAccessCount(key);
        return decayPolicy.calculateDecayedTtl(accessCount);
    }

    // ==================== Access Tracking | 访问跟踪 ====================

    private void incrementAccess(K key) {
        accessCounts.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
    }

    private void resetAccess(K key) {
        accessCounts.put(key, new AtomicLong(0));
    }

    private long getAccessCount(K key) {
        AtomicLong count = accessCounts.get(key);
        return count != null ? count.get() : 0;
    }

    /**
     * Get access count for a key
     * 获取键的访问计数
     *
     * @param key the key | 键
     * @return access count | 访问计数
     */
    public long accessCount(K key) {
        if (!trackAccess) {
            return 0;
        }
        return getAccessCount(key);
    }

    /**
     * Get all access counts
     * 获取所有访问计数
     *
     * @return map of key to access count | 键到访问计数的映射
     */
    public Map<K, Long> allAccessCounts() {
        if (!trackAccess) {
            return Map.of();
        }
        Map<K, Long> result = new LinkedHashMap<>();
        for (Map.Entry<K, AtomicLong> entry : accessCounts.entrySet()) {
            result.put(entry.getKey(), entry.getValue().get());
        }
        return result;
    }

    /**
     * Get the effective TTL for an existing entry
     * 获取现有条目的有效 TTL
     *
     * @param key the key | 键
     * @return effective TTL or null if not found | 有效 TTL，如果未找到则为 null
     */
    public Duration getEffectiveTtl(K key) {
        V value = delegate.get(key);
        if (value == null) {
            return null;
        }
        return calculateTtl(key, value);
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for VariableTtlCache
     * VariableTtlCache 构建器
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     */
    public static class Builder<K, V> {
        private final Cache<K, V> cache;
        private TtlPolicy<K, V> ttlPolicy = TtlPolicy.fixed(Duration.ofMinutes(10));
        private TtlDecayPolicy decayPolicy = null;
        private boolean trackAccess = false;

        Builder(Cache<K, V> cache) {
            this.cache = Objects.requireNonNull(cache, "cache cannot be null");
        }

        /**
         * Set TTL policy
         * 设置 TTL 策略
         *
         * @param policy the TTL policy | TTL 策略
         * @return this builder | 此构建器
         */
        public Builder<K, V> ttlPolicy(TtlPolicy<K, V> policy) {
            this.ttlPolicy = Objects.requireNonNull(policy, "policy cannot be null");
            return this;
        }

        /**
         * Set TTL decay policy
         * 设置 TTL 衰减策略
         *
         * @param policy the decay policy | 衰减策略
         * @return this builder | 此构建器
         */
        public Builder<K, V> decayPolicy(TtlDecayPolicy policy) {
            this.decayPolicy = policy;
            this.trackAccess = (policy != null);
            return this;
        }

        /**
         * Enable access tracking (automatically enabled with decay)
         * 启用访问跟踪（使用衰减时自动启用）
         *
         * @return this builder | 此构建器
         */
        public Builder<K, V> trackAccess() {
            this.trackAccess = true;
            return this;
        }

        /**
         * Build the variable TTL cache
         * 构建可变 TTL 缓存
         *
         * @return variable TTL cache | 可变 TTL 缓存
         */
        public VariableTtlCache<K, V> build() {
            return new VariableTtlCache<>(cache, ttlPolicy, decayPolicy, trackAccess);
        }
    }
}
