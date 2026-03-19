package cloud.opencode.base.cache.model;

/**
 * Cache Entry - Represents a single cache entry with metadata
 * 缓存条目 - 表示带有元数据的单个缓存条目
 *
 * <p>Immutable record containing cache entry data and access statistics.</p>
 * <p>不可变记录，包含缓存条目数据和访问统计信息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Key-value storage - 键值存储</li>
 *   <li>Creation and access time tracking - 创建和访问时间跟踪</li>
 *   <li>Access count statistics - 访问次数统计</li>
 *   <li>Weight for capacity calculation - 容量计算权重</li>
 *   <li>Age and idle time calculation - 存活和空闲时间计算</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CacheEntry<String, User> entry = new CacheEntry<>(
 *     "user:1001", user, System.currentTimeMillis(),
 *     System.currentTimeMillis(), 0, 1);
 *
 * // Get age - 获取存活时间
 * long age = entry.age();
 *
 * // Get idle time - 获取空闲时间
 * long idle = entry.idleTime();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Partial (key cannot be null) - 空值安全: 部分（键不能为 null）</li>
 * </ul>
 *
 * @param <K> the type of key | 键类型
 * @param <V> the type of value | 值类型
 * @param key            the cache key | 缓存键
 * @param value          the cache value | 缓存值
 * @param createTime     creation time in milliseconds | 创建时间（毫秒）
 * @param lastAccessTime last access time in milliseconds | 最后访问时间（毫秒）
 * @param accessCount    number of times accessed | 访问次数
 * @param weight         weight for capacity calculation | 容量计算权重
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public record CacheEntry<K, V>(
        K key,
        V value,
        long createTime,
        long lastAccessTime,
        long accessCount,
        long weight
) {

    /**
     * Create entry with default weight of 1
     * 创建默认权重为 1 的条目
     *
     * @param key   the key | 键
     * @param value the value | 值
     */
    public CacheEntry(K key, V value) {
        this(key, value, System.currentTimeMillis(), System.currentTimeMillis(), 0, 1);
    }

    /**
     * Create entry with specified weight
     * 创建指定权重的条目
     *
     * @param key    the key | 键
     * @param value  the value | 值
     * @param weight the weight | 权重
     */
    public CacheEntry(K key, V value, long weight) {
        this(key, value, System.currentTimeMillis(), System.currentTimeMillis(), 0, weight);
    }

    /**
     * Get age in milliseconds (time since creation)
     * 获取存活时间（毫秒，自创建以来）
     *
     * @return age in milliseconds | 存活时间（毫秒）
     */
    public long age() {
        return System.currentTimeMillis() - createTime;
    }

    /**
     * Get idle time in milliseconds (time since last access)
     * 获取空闲时间（毫秒，自最后访问以来）
     *
     * @return idle time in milliseconds | 空闲时间（毫秒）
     */
    public long idleTime() {
        return System.currentTimeMillis() - lastAccessTime;
    }

    /**
     * Create new entry with updated access time and count
     * 创建更新访问时间和次数的新条目
     *
     * @return new entry with updated access info | 更新后的新条目
     */
    public CacheEntry<K, V> recordAccess() {
        return new CacheEntry<>(key, value, createTime, System.currentTimeMillis(), accessCount + 1, weight);
    }

    /**
     * Create new entry with updated value
     * 创建更新值的新条目
     *
     * @param newValue the new value | 新值
     * @return new entry with updated value | 更新值后的新条目
     */
    public CacheEntry<K, V> withValue(V newValue) {
        return new CacheEntry<>(key, newValue, createTime, System.currentTimeMillis(), accessCount, weight);
    }

    /**
     * Create new entry with updated weight
     * 创建更新权重的新条目
     *
     * @param newWeight the new weight | 新权重
     * @return new entry with updated weight | 更新权重后的新条目
     */
    public CacheEntry<K, V> withWeight(long newWeight) {
        return new CacheEntry<>(key, value, createTime, lastAccessTime, accessCount, newWeight);
    }
}
