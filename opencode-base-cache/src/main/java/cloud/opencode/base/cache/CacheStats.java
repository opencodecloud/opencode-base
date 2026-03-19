package cloud.opencode.base.cache;

/**
 * Cache Statistics Snapshot Interface - Immutable cache performance metrics
 * 缓存统计快照接口 - 不可变的缓存性能指标
 *
 * <p>Provides comprehensive cache statistics for monitoring and optimization.</p>
 * <p>提供完整的缓存统计信息，用于监控和优化。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Hit/Miss statistics - 命中/未命中统计</li>
 *   <li>Load success/failure count - 加载成功/失败计数</li>
 *   <li>Eviction statistics - 淘汰统计</li>
 *   <li>Rate calculations - 比率计算</li>
 *   <li>Delta comparison - 增量比较</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CacheStats stats = cache.stats();
 *
 * // Get hit rate - 获取命中率
 * double hitRate = stats.hitRate();
 *
 * // Get request count - 获取请求总数
 * long requests = stats.requestCount();
 *
 * // Compare snapshots - 比较快照
 * CacheStats before = cache.stats();
 * // ... operations ...
 * CacheStats after = cache.stats();
 * CacheStats delta = after.minus(before);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public interface CacheStats {

    /**
     * Get hit count
     * 获取命中次数
     *
     * @return hit count | 命中次数
     */
    long hitCount();

    /**
     * Get miss count
     * 获取未命中次数
     *
     * @return miss count | 未命中次数
     */
    long missCount();

    /**
     * Get successful load count
     * 获取加载成功次数
     *
     * @return load success count | 加载成功次数
     */
    long loadSuccessCount();

    /**
     * Get failed load count
     * 获取加载失败次数
     *
     * @return load failure count | 加载失败次数
     */
    long loadFailureCount();

    /**
     * Get total load time in nanoseconds
     * 获取总加载时间（纳秒）
     *
     * @return total load time | 总加载时间
     */
    long totalLoadTime();

    /**
     * Get eviction count
     * 获取淘汰次数
     *
     * @return eviction count | 淘汰次数
     */
    long evictionCount();

    /**
     * Get eviction weight
     * 获取淘汰权重
     *
     * @return eviction weight | 淘汰权重
     */
    long evictionWeight();

    /**
     * Get total request count (hits + misses)
     * 获取请求总数（命中 + 未命中）
     *
     * @return request count | 请求总数
     */
    default long requestCount() {
        return hitCount() + missCount();
    }

    /**
     * Get hit rate (0.0 to 1.0)
     * 获取命中率（0.0 到 1.0）
     *
     * @return hit rate | 命中率
     */
    default double hitRate() {
        long total = requestCount();
        return total == 0 ? 1.0 : (double) hitCount() / total;
    }

    /**
     * Get miss rate (0.0 to 1.0)
     * 获取未命中率（0.0 到 1.0）
     *
     * @return miss rate | 未命中率
     */
    default double missRate() {
        return 1.0 - hitRate();
    }

    /**
     * Get total load count
     * 获取总加载次数
     *
     * @return total load count | 总加载次数
     */
    default long loadCount() {
        return loadSuccessCount() + loadFailureCount();
    }

    /**
     * Get load failure rate
     * 获取加载失败率
     *
     * @return load failure rate | 加载失败率
     */
    default double loadFailureRate() {
        long total = loadCount();
        return total == 0 ? 0.0 : (double) loadFailureCount() / total;
    }

    /**
     * Get load success rate
     * 获取加载成功率
     *
     * @return load success rate (0.0 to 1.0) | 加载成功率（0.0 到 1.0）
     * @since V2.0.1
     */
    default double loadSuccessRate() {
        return 1.0 - loadFailureRate();
    }

    /**
     * Get average load time in nanoseconds
     * 获取平均加载时间（纳秒）
     *
     * @return average load time | 平均加载时间
     */
    default double averageLoadPenalty() {
        long total = loadCount();
        return total == 0 ? 0.0 : (double) totalLoadTime() / total;
    }

    /**
     * Subtract another stats snapshot to get delta
     * 减去另一个统计快照获取增量
     *
     * @param other the other stats | 另一个统计
     * @return delta stats | 增量统计
     */
    CacheStats minus(CacheStats other);

    /**
     * Add another stats snapshot
     * 加上另一个统计快照
     *
     * @param other the other stats | 另一个统计
     * @return combined stats | 合并后的统计
     */
    CacheStats plus(CacheStats other);

    /**
     * Get empty stats instance
     * 获取空统计实例
     *
     * @return empty stats | 空统计
     */
    static CacheStats empty() {
        return DefaultCacheStats.EMPTY;
    }

    /**
     * Create stats instance
     * 创建统计实例
     *
     * @param hitCount         hit count | 命中次数
     * @param missCount        miss count | 未命中次数
     * @param loadSuccessCount load success count | 加载成功次数
     * @param loadFailureCount load failure count | 加载失败次数
     * @param totalLoadTime    total load time | 总加载时间
     * @param evictionCount    eviction count | 淘汰次数
     * @param evictionWeight   eviction weight | 淘汰权重
     * @return new stats instance | 新统计实例
     */
    static CacheStats of(long hitCount, long missCount, long loadSuccessCount,
                         long loadFailureCount, long totalLoadTime,
                         long evictionCount, long evictionWeight) {
        return new DefaultCacheStats(hitCount, missCount, loadSuccessCount,
                loadFailureCount, totalLoadTime, evictionCount, evictionWeight);
    }
}

/**
 * Default CacheStats implementation using record
 * 默认 CacheStats 实现（使用 record）
 */
record DefaultCacheStats(
        long hitCount,
        long missCount,
        long loadSuccessCount,
        long loadFailureCount,
        long totalLoadTime,
        long evictionCount,
        long evictionWeight
) implements CacheStats {

    static final CacheStats EMPTY = new DefaultCacheStats(0, 0, 0, 0, 0, 0, 0);

    @Override
    public CacheStats minus(CacheStats other) {
        return new DefaultCacheStats(
                Math.max(0, hitCount - other.hitCount()),
                Math.max(0, missCount - other.missCount()),
                Math.max(0, loadSuccessCount - other.loadSuccessCount()),
                Math.max(0, loadFailureCount - other.loadFailureCount()),
                Math.max(0, totalLoadTime - other.totalLoadTime()),
                Math.max(0, evictionCount - other.evictionCount()),
                Math.max(0, evictionWeight - other.evictionWeight())
        );
    }

    @Override
    public CacheStats plus(CacheStats other) {
        return new DefaultCacheStats(
                hitCount + other.hitCount(),
                missCount + other.missCount(),
                loadSuccessCount + other.loadSuccessCount(),
                loadFailureCount + other.loadFailureCount(),
                totalLoadTime + other.totalLoadTime(),
                evictionCount + other.evictionCount(),
                evictionWeight + other.evictionWeight()
        );
    }
}
