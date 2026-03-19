package cloud.opencode.base.cache.util;

import cloud.opencode.base.cache.AsyncCache;
import cloud.opencode.base.cache.Cache;
import cloud.opencode.base.cache.CacheStats;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Cache Utility Class - Helper methods for cache operations
 * 缓存工具类 - 缓存操作的辅助方法
 *
 * <p>Provides utility methods for key generation, cache warming, and statistics formatting.</p>
 * <p>提供键生成、缓存预热和统计格式化的工具方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Key generation (simple, hash) - 键生成（简单、哈希）</li>
 *   <li>Cache warming (sync, async) - 缓存预热（同步、异步）</li>
 *   <li>Statistics formatting and comparison - 统计格式化和比较</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Generate cache key - 生成缓存键
 * String key = CacheUtil.key("user", userId, tenantId);
 *
 * // Warm up cache - 预热缓存
 * CacheUtil.warmUp(cache, dataMap);
 *
 * // Format statistics - 格式化统计
 * String stats = CacheUtil.formatStats(cache.stats());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Null-safe: Partial - 空值安全: 部分</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: key()/hashKey() O(p) where p is the number of key parts; warmUp O(n) where n is the number of entries; formatStats O(1) - 时间复杂度: key()/hashKey() 为 O(p)，p为键部分数量；warmUp 为 O(n)，n为条目数量；formatStats 为 O(1)</li>
 *   <li>Space complexity: O(1) auxiliary space per operation - 空间复杂度: 每次操作辅助空间 O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public final class CacheUtil {

    private CacheUtil() {
    }

    // ==================== Key Generation | 键生成 ====================

    /**
     * Generate simple cache key with separator
     * 生成带分隔符的简单缓存键
     *
     * @param prefix prefix string | 前缀
     * @param parts  key parts | 键部分
     * @return generated key | 生成的键
     */
    public static String key(String prefix, Object... parts) {
        if (parts == null || parts.length == 0) {
            return prefix;
        }
        StringBuilder sb = new StringBuilder(prefix);
        for (Object part : parts) {
            sb.append(':').append(part);
        }
        return sb.toString();
    }

    /**
     * Generate hash-based key to avoid long keys
     * 生成基于哈希的键以避免过长的键
     *
     * @param prefix prefix string | 前缀
     * @param parts  key parts | 键部分
     * @return hash-based key | 基于哈希的键
     */
    public static String hashKey(String prefix, Object... parts) {
        String combined = Arrays.stream(parts)
                .map(String::valueOf)
                .reduce("", (a, b) -> a + ":" + b);
        return prefix + ":" + hash(combined);
    }

    /**
     * Generate batch keys with index
     * 生成带索引的批量键
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * // Generate keys: user:batch:0, user:batch:1, user:batch:2
     * String[] keys = CacheUtil.batchKeys("user:batch", 3);
     * }</pre>
     *
     * @param prefix prefix string | 前缀
     * @param count  number of keys to generate | 要生成的键数量
     * @return array of batch keys | 批量键数组
     * @since V2.0.1
     */
    public static String[] batchKeys(String prefix, int count) {
        String[] keys = new String[count];
        for (int i = 0; i < count; i++) {
            keys[i] = prefix + ":" + i;
        }
        return keys;
    }

    /**
     * Parse a composite key into its parts
     * 将复合键解析为各部分
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * String[] parts = CacheUtil.parseKey("user:1001:tenant:abc");
     * // Result: ["user", "1001", "tenant", "abc"]
     * }</pre>
     *
     * @param key the composite key | 复合键
     * @return array of key parts | 键部分数组
     * @since V2.0.1
     */
    public static String[] parseKey(String key) {
        if (key == null || key.isEmpty()) {
            return new String[0];
        }
        return key.split(":");
    }

    /**
     * Extract prefix from a composite key
     * 从复合键中提取前缀
     *
     * @param key the composite key | 复合键
     * @return the prefix (first part) | 前缀（第一部分）
     * @since V2.0.1
     */
    public static String extractPrefix(String key) {
        if (key == null || key.isEmpty()) {
            return "";
        }
        int idx = key.indexOf(':');
        return idx > 0 ? key.substring(0, idx) : key;
    }

    /**
     * Extract suffix from a composite key
     * 从复合键中提取后缀
     *
     * @param key the composite key | 复合键
     * @return the suffix (after first colon) | 后缀（第一个冒号之后）
     * @since V2.0.1
     */
    public static String extractSuffix(String key) {
        if (key == null || key.isEmpty()) {
            return "";
        }
        int idx = key.indexOf(':');
        return idx > 0 && idx < key.length() - 1 ? key.substring(idx + 1) : "";
    }

    // ==================== Cache Warming | 缓存预热 ====================

    /**
     * Warm up cache with data
     * 使用数据预热缓存
     *
     * @param cache the cache | 缓存
     * @param data  data to load | 要加载的数据
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     */
    public static <K, V> void warmUp(Cache<K, V> cache, Map<K, V> data) {
        cache.putAll(data);
    }

    /**
     * Async warm up cache with data
     * 异步使用数据预热缓存
     *
     * @param cache the async cache | 异步缓存
     * @param data  data to load | 要加载的数据
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @return completion future | 完成 Future
     */
    public static <K, V> CompletableFuture<Void> warmUpAsync(AsyncCache<K, V> cache, Map<K, V> data) {
        return CompletableFuture.allOf(
                data.entrySet().stream()
                        .map(e -> cache.putAsync(e.getKey(), e.getValue()))
                        .toArray(CompletableFuture[]::new)
        );
    }

    // ==================== Statistics | 统计 ====================

    /**
     * Format cache statistics
     * 格式化缓存统计
     *
     * @param stats the statistics | 统计
     * @return formatted string | 格式化字符串
     */
    public static String formatStats(CacheStats stats) {
        return String.format(
                "Requests: %d, Hits: %d (%.2f%%), Misses: %d (%.2f%%), " +
                        "AvgLoadTime: %.2fms, Evictions: %d",
                stats.requestCount(),
                stats.hitCount(), stats.hitRate() * 100,
                stats.missCount(), stats.missRate() * 100,
                stats.averageLoadPenalty() / 1_000_000.0,
                stats.evictionCount()
        );
    }

    /**
     * Compare two statistics snapshots
     * 比较两个统计快照
     *
     * @param before statistics before | 之前的统计
     * @param after  statistics after | 之后的统计
     * @return formatted delta string | 格式化的增量字符串
     */
    public static String compareStats(CacheStats before, CacheStats after) {
        CacheStats delta = after.minus(before);
        return formatStats(delta);
    }

    /**
     * Format statistics as JSON-like string
     * 格式化统计为类 JSON 字符串
     *
     * @param stats the statistics | 统计
     * @return JSON-like string | 类 JSON 字符串
     */
    public static String formatStatsJson(CacheStats stats) {
        return String.format(
                "{\"requests\":%d,\"hits\":%d,\"hitRate\":%.4f,\"misses\":%d," +
                        "\"missRate\":%.4f,\"loadSuccess\":%d,\"loadFailure\":%d," +
                        "\"avgLoadTimeMs\":%.2f,\"evictions\":%d}",
                stats.requestCount(),
                stats.hitCount(), stats.hitRate(),
                stats.missCount(), stats.missRate(),
                stats.loadSuccessCount(), stats.loadFailureCount(),
                stats.averageLoadPenalty() / 1_000_000.0,
                stats.evictionCount()
        );
    }

    // ==================== Utility Methods | 工具方法 ====================

    /**
     * Calculate optimal cache size based on memory
     * 基于内存计算最优缓存大小
     *
     * @param memoryMB    available memory in MB | 可用内存（MB）
     * @param avgEntrySizeBytes average entry size in bytes | 平均条目大小（字节）
     * @return optimal cache size | 最优缓存大小
     */
    public static long optimalCacheSize(long memoryMB, long avgEntrySizeBytes) {
        long totalBytes = memoryMB * 1024 * 1024;
        // Use 70% of available memory for cache
        return (long) (totalBytes * 0.7 / avgEntrySizeBytes);
    }

    /**
     * Calculate TTL based on data freshness requirements
     * 基于数据新鲜度要求计算 TTL
     *
     * @param maxStalenessSeconds max acceptable staleness in seconds | 最大可接受过期秒数
     * @param updateFrequencySeconds expected update frequency in seconds | 预期更新频率秒数
     * @return recommended TTL in seconds | 推荐的 TTL 秒数
     */
    public static long calculateTtl(long maxStalenessSeconds, long updateFrequencySeconds) {
        // TTL should be less than max staleness but accommodate update frequency
        return Math.min(maxStalenessSeconds, Math.max(updateFrequencySeconds / 2, 1));
    }

    // ==================== Private Helper Methods ====================

    private static String hash(String input) {
        int h = input.hashCode();
        h ^= (h >>> 16);
        h *= 0x85ebca6b;
        h ^= (h >>> 13);
        return Integer.toHexString(h);
    }
}
