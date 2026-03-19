package cloud.opencode.base.cache.spring;

import cloud.opencode.base.cache.Cache;
import cloud.opencode.base.cache.CacheManager;
import cloud.opencode.base.cache.CacheStats;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cache Health Indicator for Spring Boot Actuator
 * 缓存健康指示器，用于 Spring Boot Actuator
 *
 * <p>Provides health status and detailed metrics for all registered caches.
 * Integrates with Spring Boot Actuator for monitoring and alerting.</p>
 * <p>为所有注册的缓存提供健康状态和详细指标。
 * 与 Spring Boot Actuator 集成，用于监控和告警。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Overall cache health status - 整体缓存健康状态</li>
 *   <li>Per-cache detailed metrics - 每个缓存的详细指标</li>
 *   <li>Hit rate threshold alerting - 命中率阈值告警</li>
 *   <li>Memory pressure detection - 内存压力检测</li>
 * </ul>
 *
 * <p><strong>Health Status Rules | 健康状态规则:</strong></p>
 * <ul>
 *   <li>UP: All caches healthy, hit rate above threshold</li>
 *   <li>DEGRADED: Some caches below hit rate threshold or high eviction</li>
 *   <li>DOWN: Cache manager unavailable or critical errors</li>
 * </ul>
 *
 * <p><strong>Usage with Spring Boot | Spring Boot 使用:</strong></p>
 * <pre>{@code
 * // Auto-configured when spring-boot-actuator is on classpath
 * // Access at: GET /actuator/health/cache
 *
 * // Manual configuration:
 * @Bean
 * public CacheHealthIndicator cacheHealthIndicator(CacheManager cacheManager) {
 *     return new CacheHealthIndicator(cacheManager)
 *         .hitRateThreshold(0.5)
 *         .evictionRateThreshold(0.3);
 * }
 * }</pre>
 *
 * <p><strong>Response Example | 响应示例:</strong></p>
 * <pre>{@code
 * {
 *   "status": "UP",
 *   "details": {
 *     "cacheCount": 3,
 *     "totalEntries": 15000,
 *     "overallHitRate": 0.85,
 *     "caches": {
 *       "users": {
 *         "status": "UP",
 *         "size": 5000,
 *         "hitRate": 0.92,
 *         "missRate": 0.08,
 *         "evictionCount": 120
 *       },
 *       "products": {...}
 *     }
 *   }
 * }
 * }</pre>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create health indicator
 * CacheHealthIndicator indicator = new CacheHealthIndicator(CacheManager.getInstance());
 * Map<String, Object> health = indicator.health();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see CacheManager
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V2.0.4
 */
public class CacheHealthIndicator {

    /**
     * Health status enumeration
     * 健康状态枚举
     */
    public enum Status {
        /** Healthy - all caches operating normally */
        UP,
        /** Degraded - some caches below threshold but functional */
        DEGRADED,
        /** Down - critical issues detected */
        DOWN,
        /** Unknown - unable to determine status */
        UNKNOWN
    }

    private final CacheManager cacheManager;
    private double hitRateThreshold = 0.3;  // Warn if hit rate below 30%
    private double evictionRateThreshold = 0.5;  // Warn if eviction rate above 50%
    private long maxSizeWarningThreshold = 100_000;  // Warn if cache exceeds 100k entries

    /**
     * Create health indicator with cache manager
     * 使用缓存管理器创建健康指示器
     *
     * @param cacheManager the cache manager | 缓存管理器
     */
    public CacheHealthIndicator(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Set hit rate threshold for warnings
     * 设置命中率警告阈值
     *
     * @param threshold the threshold (0.0 to 1.0) | 阈值（0.0 到 1.0）
     * @return this indicator | 此指示器
     */
    public CacheHealthIndicator hitRateThreshold(double threshold) {
        if (threshold < 0 || threshold > 1) {
            throw new IllegalArgumentException("Threshold must be between 0 and 1");
        }
        this.hitRateThreshold = threshold;
        return this;
    }

    /**
     * Set eviction rate threshold for warnings
     * 设置驱逐率警告阈值
     *
     * @param threshold the threshold (0.0 to 1.0) | 阈值（0.0 到 1.0）
     * @return this indicator | 此指示器
     */
    public CacheHealthIndicator evictionRateThreshold(double threshold) {
        if (threshold < 0 || threshold > 1) {
            throw new IllegalArgumentException("Threshold must be between 0 and 1");
        }
        this.evictionRateThreshold = threshold;
        return this;
    }

    /**
     * Set max size warning threshold
     * 设置最大大小警告阈值
     *
     * @param maxSize the max size | 最大大小
     * @return this indicator | 此指示器
     */
    public CacheHealthIndicator maxSizeWarningThreshold(long maxSize) {
        this.maxSizeWarningThreshold = maxSize;
        return this;
    }

    /**
     * Get health status
     * 获取健康状态
     *
     * @return health result | 健康结果
     */
    public HealthResult health() {
        try {
            Map<String, CacheHealth> cacheHealthMap = new LinkedHashMap<>();
            long totalEntries = 0;
            long totalHits = 0;
            long totalMisses = 0;
            int degradedCount = 0;
            int downCount = 0;

            for (String cacheName : cacheManager.getCacheNames()) {
                Cache<?, ?> cache = cacheManager.<Object, Object>getCache(cacheName).orElse(null);
                if (cache == null) {
                    continue;
                }

                CacheHealth cacheHealth = evaluateCacheHealth(cache);
                cacheHealthMap.put(cacheName, cacheHealth);

                totalEntries += cacheHealth.size;
                totalHits += cacheHealth.hitCount;
                totalMisses += cacheHealth.missCount;

                if (cacheHealth.status == Status.DEGRADED) {
                    degradedCount++;
                } else if (cacheHealth.status == Status.DOWN) {
                    downCount++;
                }
            }

            // Calculate overall status
            Status overallStatus;
            if (downCount > 0) {
                overallStatus = Status.DOWN;
            } else if (degradedCount > 0) {
                overallStatus = Status.DEGRADED;
            } else {
                overallStatus = Status.UP;
            }

            double overallHitRate = (totalHits + totalMisses) > 0
                    ? (double) totalHits / (totalHits + totalMisses)
                    : 1.0;

            return new HealthResult(
                    overallStatus,
                    cacheHealthMap.size(),
                    totalEntries,
                    overallHitRate,
                    cacheHealthMap
            );

        } catch (Exception e) {
            return new HealthResult(Status.DOWN, 0, 0, 0, Map.of(), e.getMessage());
        }
    }

    private CacheHealth evaluateCacheHealth(Cache<?, ?> cache) {
        try {
            CacheStats stats = cache.stats();
            long size = cache.estimatedSize();

            long hitCount = stats.hitCount();
            long missCount = stats.missCount();
            long evictionCount = stats.evictionCount();
            long loadCount = stats.loadSuccessCount() + stats.loadFailureCount();

            double hitRate = stats.hitRate();
            double evictionRate = loadCount > 0 ? (double) evictionCount / loadCount : 0;

            // Determine status
            Status status = Status.UP;
            String warning = null;

            if (hitRate < hitRateThreshold && (hitCount + missCount) > 100) {
                status = Status.DEGRADED;
                warning = "Hit rate below threshold: " + String.format("%.2f", hitRate);
            } else if (evictionRate > evictionRateThreshold && evictionCount > 100) {
                status = Status.DEGRADED;
                warning = "High eviction rate: " + String.format("%.2f", evictionRate);
            } else if (size > maxSizeWarningThreshold) {
                status = Status.DEGRADED;
                warning = "Cache size exceeds threshold: " + size;
            }

            return new CacheHealth(
                    status,
                    size,
                    hitCount,
                    missCount,
                    evictionCount,
                    hitRate,
                    1.0 - hitRate,
                    warning
            );

        } catch (Exception e) {
            return new CacheHealth(Status.DOWN, 0, 0, 0, 0, 0, 0, e.getMessage());
        }
    }

    // ==================== Result Classes ====================

    /**
     * Overall health result
     * 整体健康结果
     *
     * @param status the health status | 健康状态
     * @param cacheCount the number of caches | 缓存数
     * @param totalEntries the total number of entries | 总条目数
     * @param overallHitRate the overall hit rate | 总体命中率
     * @param caches per-cache health details | 各缓存健康详情
     * @param error the error message, if any | 错误消息（如有）
     */
    public record HealthResult(
            Status status,
            int cacheCount,
            long totalEntries,
            double overallHitRate,
            Map<String, CacheHealth> caches,
            String error
    ) {
        /**
         * Creates a HealthResult without error | 创建无错误的健康结果
         *
         * @param status the health status | 健康状态
         * @param cacheCount the number of caches | 缓存数
         * @param totalEntries the total entries | 总条目数
         * @param overallHitRate the overall hit rate | 总体命中率
         * @param caches per-cache health details | 各缓存健康详情
         */
        public HealthResult(Status status, int cacheCount, long totalEntries,
                           double overallHitRate, Map<String, CacheHealth> caches) {
            this(status, cacheCount, totalEntries, overallHitRate, caches, null);
        }

        /**
         * Check if healthy
         * 检查是否健康
         *
         * @return true if UP | UP 返回 true
         */
        public boolean isHealthy() {
            return status == Status.UP;
        }

        /**
         * Convert to map for JSON serialization
         * 转换为 Map 用于 JSON 序列化
         *
         * @return map representation | Map 表示
         */
        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("status", status.name());
            map.put("cacheCount", cacheCount);
            map.put("totalEntries", totalEntries);
            map.put("overallHitRate", String.format("%.4f", overallHitRate));

            if (error != null) {
                map.put("error", error);
            }

            Map<String, Object> cachesMap = new LinkedHashMap<>();
            for (Map.Entry<String, CacheHealth> entry : caches.entrySet()) {
                cachesMap.put(entry.getKey(), entry.getValue().toMap());
            }
            map.put("caches", cachesMap);

            return map;
        }
    }

    /**
     * Individual cache health
     * 单个缓存健康
     *
     * @param status the health status | 健康状态
     * @param size the cache size | 缓存大小
     * @param hitCount the hit count | 命中次数
     * @param missCount the miss count | 未命中次数
     * @param evictionCount the eviction count | 淘汰次数
     * @param hitRate the hit rate | 命中率
     * @param missRate the miss rate | 未命中率
     * @param warning the warning message, if any | 警告消息（如有）
     */
    public record CacheHealth(
            Status status,
            long size,
            long hitCount,
            long missCount,
            long evictionCount,
            double hitRate,
            double missRate,
            String warning
    ) {
        /**
         * Convert to map for JSON serialization
         * 转换为 Map 用于 JSON 序列化
         *
         * @return map representation | Map 表示
         */
        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("status", status.name());
            map.put("size", size);
            map.put("hitCount", hitCount);
            map.put("missCount", missCount);
            map.put("evictionCount", evictionCount);
            map.put("hitRate", String.format("%.4f", hitRate));
            map.put("missRate", String.format("%.4f", missRate));

            if (warning != null) {
                map.put("warning", warning);
            }

            return map;
        }
    }
}
