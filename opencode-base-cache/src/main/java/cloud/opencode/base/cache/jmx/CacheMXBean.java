package cloud.opencode.base.cache.jmx;

/**
 * Cache MXBean Interface - JMX management interface for cache monitoring
 * 缓存 MXBean 接口 - 用于缓存监控的 JMX 管理接口
 *
 * <p>Provides JMX-compatible interface for monitoring and managing cache instances
 * via JConsole, VisualVM, or other JMX clients.</p>
 * <p>提供与 JMX 兼容的接口，用于通过 JConsole、VisualVM 或其他 JMX 客户端
 * 监控和管理缓存实例。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Statistics monitoring - 统计监控</li>
 *   <li>Cache operations (clear, cleanup) - 缓存操作（清除、清理）</li>
 *   <li>Configuration inspection - 配置检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Access via JConsole or VisualVM
 * // MBean path: cloud.opencode.base.cache:type=Cache,name=<cacheName>
 *
 * // Programmatic access
 * CacheJmxRegistration.register(cache);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (interface contract) - 线程安全: 是（接口契约）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public interface CacheMXBean {

    // ==================== Identity | 身份标识 ====================

    /**
     * Get cache name
     * 获取缓存名称
     *
     * @return cache name | 缓存名称
     */
    String getName();

    // ==================== Statistics | 统计信息 ====================

    /**
     * Get current cache size (entry count)
     * 获取当前缓存大小（条目数）
     *
     * @return cache size | 缓存大小
     */
    long getSize();

    /**
     * Get hit count
     * 获取命中次数
     *
     * @return hit count | 命中次数
     */
    long getHitCount();

    /**
     * Get miss count
     * 获取未命中次数
     *
     * @return miss count | 未命中次数
     */
    long getMissCount();

    /**
     * Get total request count
     * 获取总请求次数
     *
     * @return request count | 请求次数
     */
    long getRequestCount();

    /**
     * Get hit ratio (0.0 to 1.0)
     * 获取命中率（0.0 到 1.0）
     *
     * @return hit ratio | 命中率
     */
    double getHitRatio();

    /**
     * Get miss ratio (0.0 to 1.0)
     * 获取未命中率（0.0 到 1.0）
     *
     * @return miss ratio | 未命中率
     */
    double getMissRatio();

    /**
     * Get eviction count
     * 获取淘汰次数
     *
     * @return eviction count | 淘汰次数
     */
    long getEvictionCount();

    /**
     * Get load success count
     * 获取加载成功次数
     *
     * @return load success count | 加载成功次数
     */
    long getLoadSuccessCount();

    /**
     * Get load failure count
     * 获取加载失败次数
     *
     * @return load failure count | 加载失败次数
     */
    long getLoadFailureCount();

    /**
     * Get average load time in milliseconds
     * 获取平均加载时间（毫秒）
     *
     * @return average load time | 平均加载时间
     */
    double getAverageLoadTimeMillis();

    // ==================== Latency Percentiles | 延迟百分位数 ====================

    /**
     * Get P50 (median) get latency in microseconds
     * 获取 P50（中位数）获取延迟（微秒）
     *
     * @return P50 latency | P50 延迟
     */
    double getGetLatencyP50Micros();

    /**
     * Get P95 get latency in microseconds
     * 获取 P95 获取延迟（微秒）
     *
     * @return P95 latency | P95 延迟
     */
    double getGetLatencyP95Micros();

    /**
     * Get P99 get latency in microseconds
     * 获取 P99 获取延迟（微秒）
     *
     * @return P99 latency | P99 延迟
     */
    double getGetLatencyP99Micros();

    // ==================== Operations | 操作 ====================

    /**
     * Clear all entries from cache
     * 清除缓存中的所有条目
     */
    void clear();

    /**
     * Perform cache cleanup (expired entries)
     * 执行缓存清理（过期条目）
     */
    void cleanup();

    /**
     * Reset statistics counters
     * 重置统计计数器
     */
    void resetStatistics();

    // ==================== Configuration | 配置 ====================

    /**
     * Get maximum size configuration
     * 获取最大容量配置
     *
     * @return maximum size | 最大容量
     */
    long getMaximumSize();

    /**
     * Check if statistics recording is enabled
     * 检查是否启用统计记录
     *
     * @return true if enabled | 如果启用返回 true
     */
    boolean isStatisticsEnabled();

    /**
     * Get expiration configuration as string
     * 获取过期配置字符串
     *
     * @return expiration config | 过期配置
     */
    String getExpirationConfig();
}
