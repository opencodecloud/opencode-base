package cloud.opencode.base.cache;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Tenant Cache - Multi-tenant cache with isolation and per-tenant limits
 * 租户缓存 - 带隔离和租户限制的多租户缓存
 *
 * <p>Provides tenant-isolated caching with per-tenant size limits, TTL defaults,
 * and statistics. Ideal for SaaS applications requiring cache isolation.</p>
 * <p>提供租户隔离的缓存，支持每租户容量限制、TTL 默认值和统计信息。
 * 适用于需要缓存隔离的 SaaS 应用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Tenant isolation - 租户隔离</li>
 *   <li>Per-tenant size limits - 每租户容量限制</li>
 *   <li>Per-tenant TTL defaults - 每租户 TTL 默认值</li>
 *   <li>Per-tenant statistics - 每租户统计信息</li>
 *   <li>Tenant quota enforcement - 租户配额执行</li>
 *   <li>Cross-tenant operations - 跨租户操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create tenant cache - 创建租户缓存
 * TenantCache<String, User> cache = TenantCache.builder("users")
 *     .defaultMaxSize(1000)
 *     .defaultTtl(Duration.ofMinutes(30))
 *     .tenantQuota("premium", 10000)
 *     .tenantQuota("free", 100)
 *     .build();
 *
 * // Operations with tenant context - 带租户上下文的操作
 * cache.put("tenant-1", "user:1", user1);
 * User user = cache.get("tenant-1", "user:1");
 *
 * // Using tenant-scoped view - 使用租户作用域视图
 * Cache<String, User> tenantView = cache.forTenant("tenant-1");
 * tenantView.put("user:2", user2);  // Automatically scoped to tenant-1
 *
 * // Get per-tenant stats - 获取每租户统计
 * CacheStats stats = cache.tenantStats("tenant-1");
 * }</pre>
 *
 * @param <K> the type of keys | 键类型
 * @param <V> the type of values | 值类型
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public class TenantCache<K, V> {

    private final String name;
    private final ConcurrentHashMap<String, Cache<K, V>> tenantCaches = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TenantConfig> tenantConfigs = new ConcurrentHashMap<>();

    private final long defaultMaxSize;
    private final Duration defaultTtl;
    private final Supplier<Cache<K, V>> cacheFactory;

    private TenantCache(Builder<K, V> builder) {
        this.name = builder.name;
        this.defaultMaxSize = builder.defaultMaxSize;
        this.defaultTtl = builder.defaultTtl;
        this.cacheFactory = builder.cacheFactory;

        // Copy tenant quotas
        this.tenantConfigs.putAll(builder.tenantConfigs);
    }

    /**
     * Create builder for TenantCache
     * 创建 TenantCache 构建器
     *
     * @param name  cache name | 缓存名称
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @return builder | 构建器
     */
    public static <K, V> Builder<K, V> builder(String name) {
        return new Builder<>(name);
    }

    // ==================== Tenant-Scoped Operations | 租户作用域操作 ====================

    /**
     * Get value for tenant and key
     * 获取租户和键对应的值
     *
     * @param tenantId tenant identifier | 租户标识
     * @param key      the key | 键
     * @return value or null | 值或 null
     */
    public V get(String tenantId, K key) {
        return getOrCreateTenantCache(tenantId).get(key);
    }

    /**
     * Get value with loader
     * 使用加载器获取值
     *
     * @param tenantId tenant identifier | 租户标识
     * @param key      the key | 键
     * @param loader   loader function | 加载函数
     * @return value | 值
     */
    public V get(String tenantId, K key, Function<? super K, ? extends V> loader) {
        return getOrCreateTenantCache(tenantId).get(key, loader);
    }

    /**
     * Put value for tenant
     * 为租户放入值
     *
     * @param tenantId tenant identifier | 租户标识
     * @param key      the key | 键
     * @param value    the value | 值
     */
    public void put(String tenantId, K key, V value) {
        Cache<K, V> cache = getOrCreateTenantCache(tenantId);
        synchronized (cache) {
            checkQuota(tenantId, cache);
            cache.put(key, value);
        }
    }

    /**
     * Put value with custom TTL
     * 使用自定义 TTL 放入值
     *
     * @param tenantId tenant identifier | 租户标识
     * @param key      the key | 键
     * @param value    the value | 值
     * @param ttl      time to live | 存活时间
     */
    public void put(String tenantId, K key, V value, Duration ttl) {
        Cache<K, V> cache = getOrCreateTenantCache(tenantId);
        synchronized (cache) {
            checkQuota(tenantId, cache);
            cache.putWithTtl(key, value, ttl);
        }
    }

    /**
     * Put all entries for tenant
     * 为租户批量放入
     *
     * @param tenantId tenant identifier | 租户标识
     * @param map      entries to put | 要放入的条目
     */
    public void putAll(String tenantId, Map<? extends K, ? extends V> map) {
        Cache<K, V> cache = getOrCreateTenantCache(tenantId);
        synchronized (cache) {
            checkQuota(tenantId, cache, map.size());
            cache.putAll(map);
        }
    }

    /**
     * Invalidate key for tenant
     * 使租户的键失效
     *
     * @param tenantId tenant identifier | 租户标识
     * @param key      the key | 键
     */
    public void invalidate(String tenantId, K key) {
        Cache<K, V> cache = tenantCaches.get(tenantId);
        if (cache != null) {
            cache.invalidate(key);
        }
    }

    /**
     * Invalidate all keys for tenant
     * 使租户的所有键失效
     *
     * @param tenantId tenant identifier | 租户标识
     */
    public void invalidateAll(String tenantId) {
        Cache<K, V> cache = tenantCaches.get(tenantId);
        if (cache != null) {
            cache.invalidateAll();
        }
    }

    /**
     * Invalidate all entries for all tenants
     * 使所有租户的所有条目失效
     */
    public void invalidateAll() {
        for (Cache<K, V> cache : tenantCaches.values()) {
            cache.invalidateAll();
        }
    }

    /**
     * Check if key exists for tenant
     * 检查租户的键是否存在
     *
     * @param tenantId tenant identifier | 租户标识
     * @param key      the key | 键
     * @return true if exists | 如果存在返回 true
     */
    public boolean containsKey(String tenantId, K key) {
        Cache<K, V> cache = tenantCaches.get(tenantId);
        return cache != null && cache.containsKey(key);
    }

    // ==================== Tenant View | 租户视图 ====================

    /**
     * Get tenant-scoped cache view
     * 获取租户作用域的缓存视图
     *
     * @param tenantId tenant identifier | 租户标识
     * @return tenant-scoped cache | 租户作用域缓存
     */
    public Cache<K, V> forTenant(String tenantId) {
        return new TenantScopedCache<>(this, tenantId);
    }

    // ==================== Tenant Management | 租户管理 ====================

    /**
     * Get all active tenant IDs
     * 获取所有活跃租户 ID
     *
     * @return set of tenant IDs | 租户 ID 集合
     */
    public Set<String> tenants() {
        return Set.copyOf(tenantCaches.keySet());
    }

    /**
     * Get tenant cache size
     * 获取租户缓存大小
     *
     * @param tenantId tenant identifier | 租户标识
     * @return cache size | 缓存大小
     */
    public long tenantSize(String tenantId) {
        Cache<K, V> cache = tenantCaches.get(tenantId);
        return cache != null ? cache.estimatedSize() : 0;
    }

    /**
     * Get tenant statistics
     * 获取租户统计信息
     *
     * @param tenantId tenant identifier | 租户标识
     * @return cache stats | 缓存统计
     */
    public CacheStats tenantStats(String tenantId) {
        Cache<K, V> cache = tenantCaches.get(tenantId);
        return cache != null ? cache.stats() : CacheStats.empty();
    }

    /**
     * Get aggregated stats across all tenants
     * 获取所有租户的聚合统计
     *
     * @return aggregated stats | 聚合统计
     */
    public CacheStats aggregatedStats() {
        CacheStats result = CacheStats.empty();
        for (Cache<K, V> cache : tenantCaches.values()) {
            result = result.plus(cache.stats());
        }
        return result;
    }

    /**
     * Remove tenant and all its cached data
     * 移除租户及其所有缓存数据
     *
     * @param tenantId tenant identifier | 租户标识
     */
    public void removeTenant(String tenantId) {
        Cache<K, V> cache = tenantCaches.remove(tenantId);
        if (cache != null) {
            cache.invalidateAll();
        }
    }

    /**
     * Set tenant quota
     * 设置租户配额
     *
     * @param tenantId tenant identifier | 租户标识
     * @param maxSize  maximum size | 最大容量
     */
    public void setTenantQuota(String tenantId, long maxSize) {
        tenantConfigs.put(tenantId, new TenantConfig(maxSize, null));
    }

    /**
     * Set tenant quota with TTL
     * 设置带 TTL 的租户配额
     *
     * @param tenantId tenant identifier | 租户标识
     * @param maxSize  maximum size | 最大容量
     * @param ttl      default TTL | 默认 TTL
     */
    public void setTenantQuota(String tenantId, long maxSize, Duration ttl) {
        tenantConfigs.put(tenantId, new TenantConfig(maxSize, ttl));
    }

    /**
     * Get cache name
     * 获取缓存名称
     *
     * @return cache name | 缓存名称
     */
    public String name() {
        return name;
    }

    /**
     * Get total size across all tenants
     * 获取所有租户的总大小
     *
     * @return total size | 总大小
     */
    public long totalSize() {
        return tenantCaches.values().stream()
                .mapToLong(Cache::estimatedSize)
                .sum();
    }

    /**
     * Clean up all tenant caches
     * 清理所有租户缓存
     */
    public void cleanUp() {
        for (Cache<K, V> cache : tenantCaches.values()) {
            cache.cleanUp();
        }
    }

    // ==================== Private Methods | 私有方法 ====================

    /** Maximum allowed length for a tenant identifier | 租户标识最大允许长度 */
    private static final int MAX_TENANT_ID_LENGTH = 256;

    /** Maximum number of tenants allowed | 最大允许的租户数量 */
    private static final int MAX_TENANTS = 10_000;

    private Cache<K, V> getOrCreateTenantCache(String tenantId) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        if (tenantId.length() > MAX_TENANT_ID_LENGTH) {
            throw new IllegalArgumentException(
                    "tenantId length exceeds maximum of " + MAX_TENANT_ID_LENGTH + ": " + tenantId.length());
        }
        return tenantCaches.computeIfAbsent(tenantId, id -> {
            // Check tenant quota inside computeIfAbsent to avoid TOCTOU race.
            // ConcurrentHashMap.size() is approximate but sufficient to prevent
            // significant over-provisioning.
            if (tenantCaches.size() >= MAX_TENANTS) {
                throw new IllegalStateException(
                        "Maximum tenant count exceeded: " + MAX_TENANTS);
            }
            return createTenantCache(id);
        });
    }

    private Cache<K, V> createTenantCache(String tenantId) {
        if (cacheFactory != null) {
            return cacheFactory.get();
        }

        TenantConfig config = tenantConfigs.getOrDefault(tenantId,
                new TenantConfig(defaultMaxSize, defaultTtl));

        return OpenCache.getOrCreate(name + ":" + tenantId, builder -> {
            builder.maximumSize(config.maxSize());
            if (config.ttl() != null) {
                builder.expireAfterWrite(config.ttl());
            } else if (defaultTtl != null) {
                builder.expireAfterWrite(defaultTtl);
            }
            builder.recordStats();
        });
    }

    private void checkQuota(String tenantId, Cache<K, V> cache) {
        checkQuota(tenantId, cache, 1);
    }

    private void checkQuota(String tenantId, Cache<K, V> cache, int additionalEntries) {
        TenantConfig config = tenantConfigs.get(tenantId);
        long maxSize = config != null ? config.maxSize() : defaultMaxSize;

        if (maxSize > 0 && cache.estimatedSize() + additionalEntries > maxSize) {
            throw new TenantQuotaExceededException(tenantId, maxSize, cache.estimatedSize());
        }
    }

    // ==================== Inner Classes | 内部类 ====================

    private record TenantConfig(long maxSize, Duration ttl) {
    }

    /**
     * Exception thrown when tenant quota is exceeded
     * 租户配额超限时抛出的异常
     */
    public static class TenantQuotaExceededException extends RuntimeException {
        /** private final String tenantId; */
        private final String tenantId;
        /** private final long quota; */
        private final long quota;
        /** private final long currentSize; */
        private final long currentSize;

        /**
         * TenantQuotaExceededException | TenantQuotaExceededException
         * @param tenantId the tenantId | tenantId
         * @param quota the quota | quota
         * @param currentSize the currentSize | currentSize
         */
        public TenantQuotaExceededException(String tenantId, long quota, long currentSize) {
            super(String.format("Tenant '%s' quota exceeded: limit=%d, current=%d",
                    tenantId, quota, currentSize));
            this.tenantId = tenantId;
            this.quota = quota;
            this.currentSize = currentSize;
        }

        /**
         * getTenantId | getTenantId
         * @return the result | 结果
         */
        public String getTenantId() {
            return tenantId;
        }

        /**
         * getQuota | getQuota
         * @return the result | 结果
         */
        public long getQuota() {
            return quota;
        }

        /**
         * getCurrentSize | getCurrentSize
         * @return the result | 结果
         */
        public long getCurrentSize() {
            return currentSize;
        }
    }

    /**
     * Tenant-scoped cache view
     * 租户作用域缓存视图
     */
    private static class TenantScopedCache<K, V> implements Cache<K, V> {
        private final TenantCache<K, V> parent;
        private final String tenantId;

        TenantScopedCache(TenantCache<K, V> parent, String tenantId) {
            this.parent = parent;
            this.tenantId = tenantId;
        }

        @Override
        public V get(K key) {
            return parent.get(tenantId, key);
        }

        @Override
        public V get(K key, Function<? super K, ? extends V> loader) {
            return parent.get(tenantId, key, loader);
        }

        @Override
        public Map<K, V> getAll(Iterable<? extends K> keys) {
            return parent.getOrCreateTenantCache(tenantId).getAll(keys);
        }

        @Override
        public Map<K, V> getAll(Iterable<? extends K> keys,
                                Function<? super Set<? extends K>, ? extends Map<K, V>> loader) {
            return parent.getOrCreateTenantCache(tenantId).getAll(keys, loader);
        }

        @Override
        public void put(K key, V value) {
            parent.put(tenantId, key, value);
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> map) {
            parent.putAll(tenantId, map);
        }

        @Override
        public boolean putIfAbsent(K key, V value) {
            Cache<K, V> cache = parent.getOrCreateTenantCache(tenantId);
            synchronized (cache) {
                parent.checkQuota(tenantId, cache);
                return cache.putIfAbsent(key, value);
            }
        }

        @Override
        public void putWithTtl(K key, V value, Duration ttl) {
            parent.put(tenantId, key, value, ttl);
        }

        @Override
        public void putAllWithTtl(Map<? extends K, ? extends V> map, Duration ttl) {
            Cache<K, V> cache = parent.getOrCreateTenantCache(tenantId);
            synchronized (cache) {
                parent.checkQuota(tenantId, cache, map.size());
                cache.putAllWithTtl(map, ttl);
            }
        }

        @Override
        public boolean putIfAbsentWithTtl(K key, V value, Duration ttl) {
            Cache<K, V> cache = parent.getOrCreateTenantCache(tenantId);
            synchronized (cache) {
                parent.checkQuota(tenantId, cache);
                return cache.putIfAbsentWithTtl(key, value, ttl);
            }
        }

        @Override
        public void invalidate(K key) {
            parent.invalidate(tenantId, key);
        }

        @Override
        public void invalidateAll(Iterable<? extends K> keys) {
            parent.getOrCreateTenantCache(tenantId).invalidateAll(keys);
        }

        @Override
        public void invalidateAll() {
            parent.invalidateAll(tenantId);
        }

        @Override
        public boolean containsKey(K key) {
            return parent.containsKey(tenantId, key);
        }

        @Override
        public long size() {
            return parent.tenantSize(tenantId);
        }

        @Override
        public long estimatedSize() {
            return parent.tenantSize(tenantId);
        }

        @Override
        public Set<K> keys() {
            return parent.getOrCreateTenantCache(tenantId).keys();
        }

        @Override
        public Collection<V> values() {
            return parent.getOrCreateTenantCache(tenantId).values();
        }

        @Override
        public Set<Map.Entry<K, V>> entries() {
            return parent.getOrCreateTenantCache(tenantId).entries();
        }

        @Override
        public ConcurrentMap<K, V> asMap() {
            return parent.getOrCreateTenantCache(tenantId).asMap();
        }

        @Override
        public CacheStats stats() {
            return parent.tenantStats(tenantId);
        }

        @Override
        public CacheMetrics metrics() {
            return parent.getOrCreateTenantCache(tenantId).metrics();
        }

        @Override
        public void cleanUp() {
            Cache<K, V> cache = parent.tenantCaches.get(tenantId);
            if (cache != null) {
                cache.cleanUp();
            }
        }

        @Override
        public AsyncCache<K, V> async() {
            return parent.getOrCreateTenantCache(tenantId).async();
        }

        @Override
        public String name() {
            return parent.name() + ":" + tenantId;
        }
    }

    /**
     * Builder for TenantCache
     * TenantCache 构建器
     *
     * @param <K> the key type | 键类型
     * @param <V> the value type | 值类型
     */
    public static class Builder<K, V> {
        private final String name;
        private long defaultMaxSize = 10000;
        private Duration defaultTtl;
        private Supplier<Cache<K, V>> cacheFactory;
        private final Map<String, TenantConfig> tenantConfigs = new ConcurrentHashMap<>();

        private Builder(String name) {
            this.name = Objects.requireNonNull(name, "name must not be null");
        }

        /**
         * Set default max size per tenant
         * 设置每租户默认最大容量
         *
         * @param maxSize max size | 最大容量
         * @return this builder | 此构建器
         */
        public Builder<K, V> defaultMaxSize(long maxSize) {
            this.defaultMaxSize = maxSize;
            return this;
        }

        /**
         * Set default TTL per tenant
         * 设置每租户默认 TTL
         *
         * @param ttl default TTL | 默认 TTL
         * @return this builder | 此构建器
         */
        public Builder<K, V> defaultTtl(Duration ttl) {
            this.defaultTtl = ttl;
            return this;
        }

        /**
         * Set tenant quota
         * 设置租户配额
         *
         * @param tenantId tenant identifier | 租户标识
         * @param maxSize  maximum size | 最大容量
         * @return this builder | 此构建器
         */
        public Builder<K, V> tenantQuota(String tenantId, long maxSize) {
            tenantConfigs.put(tenantId, new TenantConfig(maxSize, null));
            return this;
        }

        /**
         * Set tenant quota with TTL
         * 设置带 TTL 的租户配额
         *
         * @param tenantId tenant identifier | 租户标识
         * @param maxSize  maximum size | 最大容量
         * @param ttl      default TTL | 默认 TTL
         * @return this builder | 此构建器
         */
        public Builder<K, V> tenantQuota(String tenantId, long maxSize, Duration ttl) {
            tenantConfigs.put(tenantId, new TenantConfig(maxSize, ttl));
            return this;
        }

        /**
         * Set custom cache factory for creating tenant caches
         * 设置自定义缓存工厂以创建租户缓存
         *
         * @param factory cache factory | 缓存工厂
         * @return this builder | 此构建器
         */
        public Builder<K, V> cacheFactory(Supplier<Cache<K, V>> factory) {
            this.cacheFactory = factory;
            return this;
        }

        /**
         * Build the tenant cache
         * 构建租户缓存
         *
         * @return tenant cache | 租户缓存
         */
        public TenantCache<K, V> build() {
            return new TenantCache<>(this);
        }
    }
}
