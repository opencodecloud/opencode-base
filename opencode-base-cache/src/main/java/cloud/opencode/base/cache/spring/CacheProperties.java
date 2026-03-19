package cloud.opencode.base.cache.spring;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cache Properties - Configuration properties for cache auto-configuration
 * 缓存属性 - 缓存自动配置的配置属性
 *
 * <p>Properties can be configured in application.yml or application.properties:</p>
 * <p>属性可以在 application.yml 或 application.properties 中配置：</p>
 *
 * <pre>{@code
 * # application.yml
 * opencode:
 *   cache:
 *     enabled: true
 *     allow-null-values: true
 *     default:
 *       maximum-size: 10000
 *       expire-after-write: 30m
 *       expire-after-access: 10m
 *       record-stats: true
 *     caches:
 *       users:
 *         maximum-size: 5000
 *         expire-after-write: 1h
 *       products:
 *         maximum-size: 50000
 *         expire-after-write: 24h
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Default cache configuration - 默认缓存配置</li>
 *   <li>Per-cache named configuration - 每缓存命名配置</li>
 *   <li>Spring property binding compatible - Spring 属性绑定兼容</li>
 *   <li>Null-value caching option - 空值缓存选项</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CacheProperties props = new CacheProperties();
 * props.setEnabled(true);
 * props.getDefaults().setMaximumSize(10000L);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (mutable configuration bean) - 线程安全: 否（可变配置 Bean）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public class CacheProperties {

    /**
     * Property prefix for Spring configuration binding
     */
    public static final String PREFIX = "opencode.cache";

    /**
     * Enable cache auto-configuration
     * 启用缓存自动配置
     */
    private boolean enabled = true;

    /**
     * Allow storing null values
     * 允许存储 null 值
     */
    private boolean allowNullValues = true;

    /**
     * Default cache configuration
     * 默认缓存配置
     */
    private CacheSpec defaultSpec = new CacheSpec();

    /**
     * Named cache configurations
     * 命名缓存配置
     */
    private Map<String, CacheSpec> caches = new LinkedHashMap<>();

    // ==================== Getters and Setters ====================

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAllowNullValues() {
        return allowNullValues;
    }

    public void setAllowNullValues(boolean allowNullValues) {
        this.allowNullValues = allowNullValues;
    }

    public CacheSpec getDefaultSpec() {
        return defaultSpec;
    }

    public void setDefaultSpec(CacheSpec defaultSpec) {
        this.defaultSpec = defaultSpec;
    }

    public Map<String, CacheSpec> getCaches() {
        return caches;
    }

    public void setCaches(Map<String, CacheSpec> caches) {
        this.caches = caches != null ? new LinkedHashMap<>(caches) : new LinkedHashMap<>();
    }

    /**
     * Add a named cache configuration
     * 添加命名缓存配置
     *
     * @param name the cache name | 缓存名称
     * @param spec the cache specification | 缓存规格
     * @return this for chaining | 返回 this 以支持链式调用
     */
    public CacheProperties addCache(String name, CacheSpec spec) {
        this.caches.put(name, spec);
        return this;
    }

    /**
     * Remove a named cache configuration
     * 删除命名缓存配置
     *
     * @param name the cache name | 缓存名称
     * @return this for chaining | 返回 this 以支持链式调用
     */
    public CacheProperties removeCache(String name) {
        this.caches.remove(name);
        return this;
    }

    /**
     * Clear all named cache configurations
     * 清除所有命名缓存配置
     *
     * @return this for chaining | 返回 this 以支持链式调用
     */
    public CacheProperties clearCaches() {
        this.caches.clear();
        return this;
    }

    /**
     * Cache specification for individual cache configuration
     * 单个缓存配置的缓存规格
     */
    public static class CacheSpec {

        /**
         * Maximum number of entries
         * 最大条目数
         */
        private long maximumSize = 10000;

        /**
         * Maximum total weight
         * 最大总权重
         */
        private long maximumWeight = -1;

        /**
         * Time-to-live after write
         * 写入后的存活时间
         */
        private Duration expireAfterWrite;

        /**
         * Time-to-live after last access
         * 最后访问后的存活时间
         */
        private Duration expireAfterAccess;

        /**
         * Refresh after write duration
         * 写入后的刷新时间
         */
        private Duration refreshAfterWrite;

        /**
         * Initial capacity
         * 初始容量
         */
        private int initialCapacity = 16;

        /**
         * Concurrency level
         * 并发级别
         */
        private int concurrencyLevel = 16;

        /**
         * Record statistics
         * 记录统计
         */
        private boolean recordStats = false;

        /**
         * Use virtual threads
         * 使用虚拟线程
         */
        private boolean useVirtualThreads = true;

        /**
         * Eviction policy (lru, lfu, fifo, wtinylfu)
         * 淘汰策略
         */
        private String evictionPolicy = "lru";

        /**
         * Enable metrics export (separate from recordStats)
         * 启用指标导出（与 recordStats 分开）
         *
         * @since V2.0.1
         */
        private boolean metricsEnabled = false;

        // ==================== Getters and Setters ====================

        public long getMaximumSize() {
            return maximumSize;
        }

        public void setMaximumSize(long maximumSize) {
            this.maximumSize = maximumSize;
        }

        public long getMaximumWeight() {
            return maximumWeight;
        }

        public void setMaximumWeight(long maximumWeight) {
            this.maximumWeight = maximumWeight;
        }

        public Duration getExpireAfterWrite() {
            return expireAfterWrite;
        }

        public void setExpireAfterWrite(Duration expireAfterWrite) {
            this.expireAfterWrite = expireAfterWrite;
        }

        public Duration getExpireAfterAccess() {
            return expireAfterAccess;
        }

        public void setExpireAfterAccess(Duration expireAfterAccess) {
            this.expireAfterAccess = expireAfterAccess;
        }

        public Duration getRefreshAfterWrite() {
            return refreshAfterWrite;
        }

        public void setRefreshAfterWrite(Duration refreshAfterWrite) {
            this.refreshAfterWrite = refreshAfterWrite;
        }

        public int getInitialCapacity() {
            return initialCapacity;
        }

        public void setInitialCapacity(int initialCapacity) {
            this.initialCapacity = initialCapacity;
        }

        public int getConcurrencyLevel() {
            return concurrencyLevel;
        }

        public void setConcurrencyLevel(int concurrencyLevel) {
            this.concurrencyLevel = concurrencyLevel;
        }

        public boolean isRecordStats() {
            return recordStats;
        }

        public void setRecordStats(boolean recordStats) {
            this.recordStats = recordStats;
        }

        public boolean isUseVirtualThreads() {
            return useVirtualThreads;
        }

        public void setUseVirtualThreads(boolean useVirtualThreads) {
            this.useVirtualThreads = useVirtualThreads;
        }

        public String getEvictionPolicy() {
            return evictionPolicy;
        }

        public void setEvictionPolicy(String evictionPolicy) {
            this.evictionPolicy = evictionPolicy;
        }

        public boolean isMetricsEnabled() {
            return metricsEnabled;
        }

        public void setMetricsEnabled(boolean metricsEnabled) {
            this.metricsEnabled = metricsEnabled;
        }

        /**
         * Validate the cache specification
         * 验证缓存规格
         *
         * @throws IllegalArgumentException if configuration is invalid | 如果配置无效抛出异常
         * @since V2.0.1
         */
        public void validate() {
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
            if (expireAfterWrite != null && expireAfterWrite.isNegative()) {
                throw new IllegalArgumentException("expireAfterWrite cannot be negative: " + expireAfterWrite);
            }
            if (expireAfterAccess != null && expireAfterAccess.isNegative()) {
                throw new IllegalArgumentException("expireAfterAccess cannot be negative: " + expireAfterAccess);
            }
            if (refreshAfterWrite != null && refreshAfterWrite.isNegative()) {
                throw new IllegalArgumentException("refreshAfterWrite cannot be negative: " + refreshAfterWrite);
            }
            if (evictionPolicy != null && !isValidEvictionPolicy(evictionPolicy)) {
                throw new IllegalArgumentException("Invalid eviction policy: " + evictionPolicy +
                        ". Valid values are: lru, lfu, fifo, wtinylfu, w-tinylfu");
            }
        }

        private boolean isValidEvictionPolicy(String policy) {
            return switch (policy.toLowerCase()) {
                case "lru", "lfu", "fifo", "wtinylfu", "w-tinylfu" -> true;
                default -> false;
            };
        }
    }

    /**
     * Validate all cache configurations
     * 验证所有缓存配置
     *
     * @throws IllegalArgumentException if any configuration is invalid | 如果任何配置无效抛出异常
     * @since V2.0.1
     */
    public void validate() {
        if (defaultSpec != null) {
            try {
                defaultSpec.validate();
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Default cache config invalid: " + e.getMessage(), e);
            }
        }
        for (Map.Entry<String, CacheSpec> entry : caches.entrySet()) {
            try {
                entry.getValue().validate();
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Cache '" + entry.getKey() + "' config invalid: " + e.getMessage(), e);
            }
        }
    }
}
