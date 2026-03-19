package cloud.opencode.base.cache.config;

import cloud.opencode.base.cache.spi.*;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * Cache Configuration - Immutable cache configuration with Builder pattern
 * 缓存配置 - 不可变的缓存配置，使用 Builder 模式
 *
 * <p>Provides comprehensive cache configuration options including size limits,
 * expiration policies, eviction policies, and async execution settings.</p>
 * <p>提供全面的缓存配置选项，包括容量限制、过期策略、淘汰策略和异步执行设置。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Size/Weight limits - 容量/权重限制</li>
 *   <li>TTL/TTI expiration - TTL/TTI 过期</li>
 *   <li>Eviction policies (LRU/LFU/FIFO/W-TinyLFU) - 淘汰策略</li>
 *   <li>Removal listener - 移除监听器</li>
 *   <li>Statistics recording - 统计记录</li>
 *   <li>Virtual thread support - 虚拟线程支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CacheConfig<String, User> config = CacheConfig.<String, User>builder()
 *     .maximumSize(10000)
 *     .expireAfterWrite(Duration.ofMinutes(30))
 *     .expireAfterAccess(Duration.ofMinutes(10))
 *     .recordStats()
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @param <K> the type of keys | 键类型
 * @param <V> the type of values | 值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public final class CacheConfig<K, V> {

    private static final System.Logger LOGGER = System.getLogger(CacheConfig.class.getName());

    private final long maximumSize;
    private final long maximumWeight;
    private final Duration expireAfterWrite;
    private final Duration expireAfterAccess;
    private final Duration refreshAfterWrite;
    private final EvictionPolicy<K, V> evictionPolicy;
    private final ExpiryPolicy<K, V> expiryPolicy;
    private final CacheLoader<K, V> loader;
    private final RemovalListener<K, V> removalListener;
    private final boolean recordStats;
    private final boolean useVirtualThreads;
    private final Executor executor;
    private final int concurrencyLevel;
    private final int initialCapacity;
    private final RefreshAheadPolicy<K, V> refreshAheadPolicy;
    private final ValueWeigher<V> weigher;

    private CacheConfig(Builder<K, V> builder) {
        this.maximumSize = builder.maximumSize;
        this.maximumWeight = builder.maximumWeight;
        this.expireAfterWrite = builder.expireAfterWrite;
        this.expireAfterAccess = builder.expireAfterAccess;
        this.refreshAfterWrite = builder.refreshAfterWrite;
        this.evictionPolicy = builder.evictionPolicy;
        this.expiryPolicy = builder.expiryPolicy;
        this.loader = builder.loader;
        this.removalListener = builder.removalListener;
        this.recordStats = builder.recordStats;
        this.useVirtualThreads = builder.useVirtualThreads;
        this.executor = builder.executor;
        this.concurrencyLevel = builder.concurrencyLevel;
        this.initialCapacity = builder.initialCapacity;
        this.refreshAheadPolicy = builder.refreshAheadPolicy;
        this.weigher = builder.weigher;
    }

    // ==================== Getters ====================

    /**
     * Returns the maximum size | 返回最大大小
     *
     * @return maximum size | 最大大小
     */
    public long maximumSize() {
        return maximumSize;
    }

    /**
     * Returns the maximum weight | 返回最大权重
     *
     * @return maximum weight | 最大权重
     */
    public long maximumWeight() {
        return maximumWeight;
    }

    /**
     * Returns the expire-after-write duration | 返回写入后过期时间
     *
     * @return expire-after-write | 写入后过期时间
     */
    public Duration expireAfterWrite() {
        return expireAfterWrite;
    }

    /**
     * Returns the expire-after-access duration | 返回访问后过期时间
     *
     * @return expire-after-access | 访问后过期时间
     */
    public Duration expireAfterAccess() {
        return expireAfterAccess;
    }

    /**
     * Returns the refresh-after-write duration | 返回写入后刷新时间
     *
     * @return refresh-after-write | 写入后刷新时间
     */
    public Duration refreshAfterWrite() {
        return refreshAfterWrite;
    }

    /**
     * Returns the eviction policy | 返回淘汰策略
     *
     * @return eviction policy | 淘汰策略
     */
    public EvictionPolicy<K, V> evictionPolicy() {
        return evictionPolicy;
    }

    /**
     * Returns the expiry policy | 返回过期策略
     *
     * @return expiry policy | 过期策略
     */
    public ExpiryPolicy<K, V> expiryPolicy() {
        return expiryPolicy;
    }

    /**
     * Returns the cache loader | 返回缓存加载器
     *
     * @return cache loader | 缓存加载器
     */
    public CacheLoader<K, V> loader() {
        return loader;
    }

    /**
     * Returns the removal listener | 返回移除监听器
     *
     * @return removal listener | 移除监听器
     */
    public RemovalListener<K, V> removalListener() {
        return removalListener;
    }

    /**
     * Returns whether stats recording is enabled | 返回是否启用统计记录
     *
     * @return true if stats enabled | 启用统计返回 true
     */
    public boolean recordStats() {
        return recordStats;
    }

    /**
     * Returns whether virtual threads are used | 返回是否使用虚拟线程
     *
     * @return true if virtual threads | 使用虚拟线程返回 true
     */
    public boolean useVirtualThreads() {
        return useVirtualThreads;
    }

    /**
     * Returns the executor | 返回执行器
     *
     * @return executor | 执行器
     */
    public Executor executor() {
        return executor;
    }

    /**
     * Returns the concurrency level | 返回并发级别
     *
     * @return concurrency level | 并发级别
     */
    public int concurrencyLevel() {
        return concurrencyLevel;
    }

    /**
     * Returns the initial capacity | 返回初始容量
     *
     * @return initial capacity | 初始容量
     */
    public int initialCapacity() {
        return initialCapacity;
    }

    /**
     * Get refresh ahead policy
     * 获取提前刷新策略
     *
     * @return refresh ahead policy | 提前刷新策略
     */
    public RefreshAheadPolicy<K, V> refreshAheadPolicy() {
        return refreshAheadPolicy;
    }

    /**
     * Get value weigher
     * 获取值权重计算器
     *
     * @return value weigher | 值权重计算器
     */
    public ValueWeigher<V> weigher() {
        return weigher;
    }

    /**
     * Check if expiration is configured
     * 检查是否配置了过期
     *
     * @return true if expiration configured | 配置了过期返回 true
     */
    public boolean hasExpiration() {
        return expireAfterWrite != null || expireAfterAccess != null || expiryPolicy != null;
    }

    /**
     * Check if size limit is configured
     * 检查是否配置了容量限制
     *
     * @return true if size limit configured | 配置了容量限制返回 true
     */
    public boolean hasSizeLimit() {
        return maximumSize > 0 || maximumWeight > 0;
    }

    /**
     * Create new builder
     * 创建新的构建器
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return new builder | 新构建器
     */
    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    /**
     * Create default configuration
     * 创建默认配置
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return default config | 默认配置
     */
    public static <K, V> CacheConfig<K, V> defaultConfig() {
        return new Builder<K, V>().build();
    }

    /**
     * Cache Configuration Builder
     * 缓存配置构建器
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     */
    public static final class Builder<K, V> {

        /** Creates a new Builder instance | 创建新的构建器实例 */
        public Builder() {}

        private long maximumSize = 10000;
        private long maximumWeight = -1;
        private Duration expireAfterWrite;
        private Duration expireAfterAccess;
        private Duration refreshAfterWrite;
        private EvictionPolicy<K, V> evictionPolicy;
        private ExpiryPolicy<K, V> expiryPolicy;
        private CacheLoader<K, V> loader;
        private RemovalListener<K, V> removalListener;
        private boolean recordStats = false;
        private boolean useVirtualThreads = false;
        private Executor executor;
        private int concurrencyLevel = 16;
        private int initialCapacity = 16;
        private RefreshAheadPolicy<K, V> refreshAheadPolicy;
        private ValueWeigher<V> weigher;

        /**
         * Set maximum entry count
         * 设置最大条目数
         *
         * <p>Note: Setting maximumSize automatically clears maximumWeight
         * to avoid conflicts. Use one or the other.</p>
         *
         * @param size maximum size | 最大容量
         * @return this builder | 此构建器
         */
        public Builder<K, V> maximumSize(long size) {
            this.maximumSize = size;
            this.maximumWeight = -1;  // Clear weight limit when using size limit
            return this;
        }

        /**
         * Set maximum weight
         * 设置最大权重
         *
         * <p>Note: Setting maximumWeight automatically clears maximumSize
         * to avoid conflicts. Use one or the other.</p>
         *
         * @param weight maximum weight | 最大权重
         * @return this builder | 此构建器
         */
        public Builder<K, V> maximumWeight(long weight) {
            this.maximumWeight = weight;
            this.maximumSize = 0;  // Clear size limit when using weight limit
            return this;
        }

        /**
         * Set expiration after write (TTL)
         * 设置写入后过期时间（TTL）
         *
         * @param duration expiration duration | 过期时长
         * @return this builder | 此构建器
         */
        public Builder<K, V> expireAfterWrite(Duration duration) {
            this.expireAfterWrite = duration;
            return this;
        }

        /**
         * Set expiration after access (TTI)
         * 设置访问后过期时间（TTI）
         *
         * @param duration expiration duration | 过期时长
         * @return this builder | 此构建器
         */
        public Builder<K, V> expireAfterAccess(Duration duration) {
            this.expireAfterAccess = duration;
            return this;
        }

        /**
         * Set refresh after write duration
         * 设置写入后刷新时间
         *
         * @param duration refresh duration | 刷新时长
         * @return this builder | 此构建器
         */
        public Builder<K, V> refreshAfterWrite(Duration duration) {
            this.refreshAfterWrite = duration;
            return this;
        }

        /**
         * Set eviction policy
         * 设置淘汰策略
         *
         * @param policy eviction policy | 淘汰策略
         * @return this builder | 此构建器
         */
        public Builder<K, V> evictionPolicy(EvictionPolicy<K, V> policy) {
            this.evictionPolicy = policy;
            return this;
        }

        /**
         * Set expiry policy
         * 设置过期策略
         *
         * @param policy expiry policy | 过期策略
         * @return this builder | 此构建器
         */
        public Builder<K, V> expiryPolicy(ExpiryPolicy<K, V> policy) {
            this.expiryPolicy = policy;
            return this;
        }

        /**
         * Set cache loader
         * 设置缓存加载器
         *
         * @param loader cache loader | 缓存加载器
         * @return this builder | 此构建器
         */
        public Builder<K, V> loader(CacheLoader<K, V> loader) {
            this.loader = loader;
            return this;
        }

        /**
         * Set cache loader from function
         * 从函数设置缓存加载器
         *
         * @param loadFunc load function | 加载函数
         * @return this builder | 此构建器
         */
        public Builder<K, V> loader(Function<K, V> loadFunc) {
            this.loader = loadFunc::apply;
            return this;
        }

        /**
         * Set removal listener
         * 设置移除监听器
         *
         * @param listener removal listener | 移除监听器
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
         * Enable virtual threads for async operations
         * 启用虚拟线程用于异步操作
         *
         * @return this builder | 此构建器
         */
        public Builder<K, V> useVirtualThreads() {
            this.useVirtualThreads = true;
            return this;
        }

        /**
         * Set custom executor for async operations
         * 设置自定义执行器用于异步操作
         *
         * @param executor the executor | 执行器
         * @return this builder | 此构建器
         */
        public Builder<K, V> executor(Executor executor) {
            this.executor = executor;
            return this;
        }

        /**
         * Set concurrency level
         * 设置并发级别
         *
         * @param level concurrency level | 并发级别
         * @return this builder | 此构建器
         */
        public Builder<K, V> concurrencyLevel(int level) {
            this.concurrencyLevel = level;
            return this;
        }

        /**
         * Set initial capacity
         * 设置初始容量
         *
         * @param capacity initial capacity | 初始容量
         * @return this builder | 此构建器
         */
        public Builder<K, V> initialCapacity(int capacity) {
            this.initialCapacity = capacity;
            return this;
        }

        /**
         * Set refresh ahead policy for proactive cache refresh
         * 设置提前刷新策略用于主动缓存刷新
         *
         * @param policy refresh ahead policy | 提前刷新策略
         * @return this builder | 此构建器
         */
        public Builder<K, V> refreshAheadPolicy(RefreshAheadPolicy<K, V> policy) {
            this.refreshAheadPolicy = policy;
            return this;
        }

        /**
         * Set value weigher for memory-based eviction
         * 设置值权重计算器用于基于内存的淘汰
         *
         * @param weigher value weigher | 值权重计算器
         * @return this builder | 此构建器
         */
        public Builder<K, V> weigher(ValueWeigher<V> weigher) {
            this.weigher = weigher;
            return this;
        }

        /**
         * Build the configuration
         * 构建配置
         *
         * @return cache config | 缓存配置
         * @throws IllegalArgumentException if configuration is invalid | 配置无效时抛出异常
         */
        public CacheConfig<K, V> build() {
            validate();
            return new CacheConfig<>(this);
        }

        /**
         * Validate the configuration
         * 验证配置
         *
         * @throws IllegalArgumentException if configuration is invalid | 配置无效时抛出异常
         * @since V2.0.2
         */
        public void validate() {
            // Validate value ranges
            if (maximumSize < 0) {
                throw new IllegalArgumentException("maximumSize cannot be negative: " + maximumSize);
            }
            if (maximumWeight < -1) {
                throw new IllegalArgumentException("maximumWeight cannot be less than -1: " + maximumWeight);
            }
            if (initialCapacity < 0) {
                throw new IllegalArgumentException("initialCapacity cannot be negative: " + initialCapacity);
            }
            if (concurrencyLevel < 1) {
                throw new IllegalArgumentException("concurrencyLevel must be at least 1: " + concurrencyLevel);
            }

            // Validate duration values
            if (expireAfterWrite != null && expireAfterWrite.isNegative()) {
                throw new IllegalArgumentException("expireAfterWrite cannot be negative: " + expireAfterWrite);
            }
            if (expireAfterAccess != null && expireAfterAccess.isNegative()) {
                throw new IllegalArgumentException("expireAfterAccess cannot be negative: " + expireAfterAccess);
            }
            if (refreshAfterWrite != null && refreshAfterWrite.isNegative()) {
                throw new IllegalArgumentException("refreshAfterWrite cannot be negative: " + refreshAfterWrite);
            }

            // Validate mutually exclusive options
            if (maximumSize > 0 && maximumWeight > 0) {
                throw new IllegalArgumentException(
                        "Cannot set both maximumSize and maximumWeight. Use one or the other.");
            }

            // Validate weight requires weigher
            if (maximumWeight > 0 && weigher == null) {
                throw new IllegalArgumentException(
                        "maximumWeight requires a weigher. Use weigher() to set one.");
            }

            // Validate refresh requires expiration or TTL
            if (refreshAfterWrite != null && expireAfterWrite == null && expiryPolicy == null) {
                throw new IllegalArgumentException(
                        "refreshAfterWrite requires expireAfterWrite or expiryPolicy to be set.");
            }

            // V2.0.4: Enhanced validation - refresh should be less than expiration
            if (refreshAfterWrite != null && expireAfterWrite != null) {
                if (refreshAfterWrite.compareTo(expireAfterWrite) >= 0) {
                    throw new IllegalArgumentException(
                            "refreshAfterWrite (" + refreshAfterWrite + ") should be less than expireAfterWrite (" + expireAfterWrite + ")");
                }
            }

            // V2.0.4: Validate TTL zero duration
            if (expireAfterWrite != null && expireAfterWrite.isZero()) {
                throw new IllegalArgumentException(
                        "expireAfterWrite cannot be zero. Use invalidate() to remove entries immediately.");
            }
            if (expireAfterAccess != null && expireAfterAccess.isZero()) {
                throw new IllegalArgumentException(
                        "expireAfterAccess cannot be zero. Use invalidate() to remove entries immediately.");
            }

            // V2.0.4: Validate maximum size is reasonable
            if (maximumSize > 0 && maximumSize < 10) {
                // Allow but could be a mistake - very small cache
                LOGGER.log(System.Logger.Level.WARNING, "maximumSize=" + maximumSize + " is very small. Consider if this is intentional.");
            }

            // V2.0.4: Auto-adjust initial capacity if it exceeds maximum
            if (maximumSize > 0 && initialCapacity > maximumSize) {
                initialCapacity = (int) Math.min(initialCapacity, maximumSize);
            }

            // V2.0.4: Soft/weak references validation removed (fields not implemented)

            // Warn about conflicting expiration settings (allow but log)
            // expireAfterWrite/Access can coexist with expiryPolicy - expiryPolicy takes precedence
        }

        /**
         * Get validation diagnostics without throwing exceptions
         * 获取验证诊断信息，不抛出异常
         *
         * @return list of validation issues | 验证问题列表
         * @since V2.0.4
         */
        public java.util.List<String> diagnose() {
            java.util.List<String> issues = new java.util.ArrayList<>();

            // Check for potential issues
            if (maximumSize <= 0 && maximumWeight <= 0) {
                issues.add("INFO: No size limit configured. Cache may grow unbounded.");
            }

            if (expireAfterWrite == null && expireAfterAccess == null && expiryPolicy == null) {
                issues.add("INFO: No expiration configured. Entries will never expire automatically.");
            }

            if (!recordStats) {
                issues.add("INFO: Statistics not enabled. Use recordStats() to enable metrics.");
            }

            if (maximumSize > 1_000_000) {
                issues.add("WARN: Very large maximumSize (" + maximumSize + "). Consider memory implications.");
            }

            if (expireAfterWrite != null && expireAfterWrite.toMillis() < 1000) {
                issues.add("WARN: Very short expireAfterWrite (" + expireAfterWrite + "). May cause high churn.");
            }

            if (concurrencyLevel > 64) {
                issues.add("WARN: High concurrencyLevel (" + concurrencyLevel + "). May waste memory.");
            }

            return issues;
        }
    }
}
