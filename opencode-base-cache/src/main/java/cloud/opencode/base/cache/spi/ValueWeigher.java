package cloud.opencode.base.cache.spi;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

/**
 * Value Weigher - Calculate weight of cache entries for memory-based eviction
 * 值权重计算器 - 计算缓存条目权重用于基于内存的淘汰
 *
 * <p>Used to calculate the weight of cache values for memory-aware caching.
 * When cache reaches maximum weight, entries are evicted based on their weight.</p>
 * <p>用于计算缓存值的权重以实现内存感知的缓存。当缓存达到最大权重时，根据权重淘汰条目。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Custom weight calculation - 自定义权重计算</li>
 *   <li>Memory-based eviction support - 基于内存的淘汰支持</li>
 *   <li>Built-in weighers for common types - 常见类型的内置权重计算器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Fixed weight per entry - 每个条目固定权重
 * ValueWeigher<String> fixed = ValueWeigher.fixed(1);
 *
 * // String length as weight - 字符串长度作为权重
 * ValueWeigher<String> stringWeigher = ValueWeigher.stringLength();
 *
 * // Collection size as weight - 集合大小作为权重
 * ValueWeigher<List<User>> listWeigher = ValueWeigher.collectionSize();
 *
 * // Custom weigher - 自定义权重计算器
 * ValueWeigher<User> userWeigher = user -> user.getDataSize();
 *
 * // Use in cache config - 在缓存配置中使用
 * Cache<String, User> cache = OpenCache.builder()
 *     .maximumWeight(1_000_000)  // 1MB
 *     .weigher(user -> user.getSerializedSize())
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Weight must be non-negative - 权重必须非负</li>
 * </ul>
 *
 * @param <V> the type of values | 值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@FunctionalInterface
public interface ValueWeigher<V> {

    /**
     * Calculate the weight of a value
     * 计算值的权重
     *
     * @param value the value to weigh | 要计算权重的值
     * @return the weight (must be non-negative) | 权重（必须非负）
     */
    long weigh(V value);

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create a weigher with fixed weight per entry
     * 创建每个条目固定权重的权重计算器
     *
     * @param weight the fixed weight | 固定权重
     * @param <V>    the value type | 值类型
     * @return the weigher | 权重计算器
     */
    static <V> ValueWeigher<V> fixed(long weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("Weight must be non-negative");
        }
        return value -> weight;
    }

    /**
     * Create a weigher using string length
     * 创建使用字符串长度的权重计算器
     *
     * @return the weigher | 权重计算器
     */
    static ValueWeigher<String> stringLength() {
        return value -> value == null ? 0 : value.length();
    }

    /**
     * Create a weigher using byte array length
     * 创建使用字节数组长度的权重计算器
     *
     * @return the weigher | 权重计算器
     */
    static ValueWeigher<byte[]> byteArrayLength() {
        return value -> value == null ? 0 : value.length;
    }

    /**
     * Create a weigher using collection size
     * 创建使用集合大小的权重计算器
     *
     * @param <V> the collection type | 集合类型
     * @return the weigher | 权重计算器
     */
    static <V extends Collection<?>> ValueWeigher<V> collectionSize() {
        return value -> value == null ? 0 : value.size();
    }

    /**
     * Create a weigher using map size
     * 创建使用 Map 大小的权重计算器
     *
     * @param <V> the map type | Map 类型
     * @return the weigher | 权重计算器
     */
    static <V extends Map<?, ?>> ValueWeigher<V> mapSize() {
        return value -> value == null ? 0 : value.size();
    }

    /**
     * Create a weigher using array length
     * 创建使用数组长度的权重计算器
     *
     * @param <V> the array type | 数组类型
     * @return the weigher | 权重计算器
     */
    @SuppressWarnings("unchecked")
    static <V> ValueWeigher<V> arrayLength() {
        return value -> {
            if (value == null) return 0;
            if (value.getClass().isArray()) {
                return Array.getLength(value);
            }
            return 1;
        };
    }

    /**
     * Create a weigher that estimates memory size for common types
     * 创建估算常见类型内存大小的权重计算器
     *
     * <p>Estimates memory usage based on type:</p>
     * <ul>
     *   <li>String: length * 2 + 40 (approx char array + header)</li>
     *   <li>byte[]: length + 16 (array + header)</li>
     *   <li>Collection: size * 8 + 40 (references + header)</li>
     *   <li>Map: size * 16 + 40 (key + value refs + header)</li>
     *   <li>Other: 40 (minimum object overhead)</li>
     * </ul>
     *
     * @param <V> the value type | 值类型
     * @return the weigher | 权重计算器
     */
    static <V> ValueWeigher<V> estimatedMemory() {
        return value -> {
            if (value == null) return 0;

            return switch (value) {
                case String s -> s.length() * 2L + 40;
                case byte[] arr -> arr.length + 16L;
                case Collection<?> c -> c.size() * 8L + 40;
                case Map<?, ?> m -> m.size() * 16L + 40;
                default -> {
                    if (value.getClass().isArray()) {
                        yield Array.getLength(value) * 8L + 16;
                    }
                    yield 40L; // Minimum object overhead
                }
            };
        };
    }

    // ==================== Composition | 组合 ====================

    /**
     * Create a weigher that applies a multiplier
     * 创建应用乘数的权重计算器
     *
     * @param multiplier the multiplier | 乘数
     * @return the scaled weigher | 缩放后的权重计算器
     */
    default ValueWeigher<V> times(long multiplier) {
        if (multiplier < 0) {
            throw new IllegalArgumentException("Multiplier must be non-negative");
        }
        return value -> this.weigh(value) * multiplier;
    }

    /**
     * Create a weigher with minimum weight
     * 创建带最小权重的权重计算器
     *
     * @param minWeight the minimum weight | 最小权重
     * @return the bounded weigher | 带边界的权重计算器
     */
    default ValueWeigher<V> withMinimum(long minWeight) {
        return value -> Math.max(minWeight, this.weigh(value));
    }

    /**
     * Create a weigher with maximum weight
     * 创建带最大权重的权重计算器
     *
     * @param maxWeight the maximum weight | 最大权重
     * @return the bounded weigher | 带边界的权重计算器
     */
    default ValueWeigher<V> withMaximum(long maxWeight) {
        return value -> Math.min(maxWeight, this.weigh(value));
    }

    /**
     * Create a weigher bounded between min and max
     * 创建在最小和最大之间的权重计算器
     *
     * @param minWeight the minimum weight | 最小权重
     * @param maxWeight the maximum weight | 最大权重
     * @return the bounded weigher | 带边界的权重计算器
     */
    default ValueWeigher<V> bounded(long minWeight, long maxWeight) {
        return value -> {
            long weight = this.weigh(value);
            return Math.max(minWeight, Math.min(maxWeight, weight));
        };
    }

    // ==================== Key-Value Weigher | 键值权重计算器 ====================

    /**
     * Create a weigher that considers both key and value
     * 创建同时考虑键和值的权重计算器
     *
     * @param <K>          the key type | 键类型
     * @param <V>          the value type | 值类型
     * @param keyWeigher   the key weigher | 键权重计算器
     * @param valueWeigher the value weigher | 值权重计算器
     * @return the combined weigher | 组合的权重计算器
     */
    static <K, V> EntryWeigher<K, V> combined(ValueWeigher<K> keyWeigher, ValueWeigher<V> valueWeigher) {
        return (key, value) -> keyWeigher.weigh(key) + valueWeigher.weigh(value);
    }

    /**
     * Entry weigher interface for key-value pairs
     * 键值对的条目权重计算器接口
     *
     * @param <K> the key type | 键类型
     * @param <V> the value type | 值类型
     */
    @FunctionalInterface
    interface EntryWeigher<K, V> {
        /**
         * Calculate the weight of a key-value pair
         * 计算键值对的权重
         *
         * @param key   the key | 键
         * @param value the value | 值
         * @return the combined weight | 组合权重
         */
        long weigh(K key, V value);
    }
}
