package cloud.opencode.base.cache.event;

import cloud.opencode.base.cache.model.RemovalCause;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Cache Event - Represents events that occur in a cache
 * 缓存事件 - 表示缓存中发生的事件
 *
 * <p>Provides a type-safe representation of cache events including
 * put, get, remove, expire, evict, load, and clear operations.</p>
 * <p>提供缓存事件的类型安全表示，包括放入、获取、移除、过期、淘汰、加载和清除操作。</p>
 *
 * <p><strong>Event Types | 事件类型:</strong></p>
 * <ul>
 *   <li>PUT - Entry added or updated | 条目添加或更新</li>
 *   <li>GET - Entry accessed (hit or miss) | 条目访问（命中或未命中）</li>
 *   <li>REMOVE - Entry explicitly removed | 条目显式移除</li>
 *   <li>EXPIRE - Entry expired | 条目过期</li>
 *   <li>EVICT - Entry evicted due to size limit | 条目因容量限制被淘汰</li>
 *   <li>LOAD - Entry loaded from loader | 条目从加载器加载</li>
 *   <li>CLEAR - Cache cleared | 缓存清除</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create put event - 创建放入事件
 * CacheEvent<String, User> event = CacheEvent.put("users", "user1", user);
 *
 * // Create eviction event - 创建淘汰事件
 * CacheEvent<String, User> event = CacheEvent.evict("users", "user2", oldUser, RemovalCause.SIZE);
 *
 * // Pattern matching - 模式匹配
 * switch (event.type()) {
 *     case PUT -> handlePut(event);
 *     case EVICT -> handleEviction(event);
 *     default -> log(event);
 * }
 * }</pre>
 *
 * @param <K> key type | 键类型
 * @param <V> value type | 值类型
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type-safe event representation - 类型安全事件表示</li>
 *   <li>Multiple event types (PUT, GET, REMOVE, EXPIRE, EVICT, LOAD, CLEAR) - 多种事件类型</li>
 *   <li>Timestamp and metadata support - 时间戳和元数据支持</li>
 *   <li>Immutable record - 不可变记录</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Partial (key required, value optional) - 空值安全: 部分（键必需，值可选）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public record CacheEvent<K, V>(
        EventType type,
        String cacheName,
        K key,
        V value,
        V oldValue,
        RemovalCause removalCause,
        boolean isHit,
        Instant timestamp
) {

    /**
     * Cache event types
     * 缓存事件类型
     */
    public enum EventType {
        /**
         * Entry added or updated
         * 条目添加或更新
         */
        PUT,

        /**
         * Entry accessed
         * 条目访问
         */
        GET,

        /**
         * Entry explicitly removed
         * 条目显式移除
         */
        REMOVE,

        /**
         * Entry expired
         * 条目过期
         */
        EXPIRE,

        /**
         * Entry evicted due to size/weight limit
         * 条目因容量/权重限制被淘汰
         */
        EVICT,

        /**
         * Entry loaded from loader
         * 条目从加载器加载
         */
        LOAD,

        /**
         * Cache cleared
         * 缓存清除
         */
        CLEAR
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create PUT event
     * 创建 PUT 事件
     *
     * @param cacheName cache name | 缓存名称
     * @param key       the key | 键
     * @param value     the new value | 新值
     * @param <K>       key type | 键类型
     * @param <V>       value type | 值类型
     * @return event | 事件
     */
    public static <K, V> CacheEvent<K, V> put(String cacheName, K key, V value) {
        return put(cacheName, key, value, null);
    }

    /**
     * Create PUT event with old value
     * 创建带旧值的 PUT 事件
     *
     * @param cacheName cache name | 缓存名称
     * @param key       the key | 键
     * @param value     the new value | 新值
     * @param oldValue  the old value | 旧值
     * @param <K>       key type | 键类型
     * @param <V>       value type | 值类型
     * @return event | 事件
     */
    public static <K, V> CacheEvent<K, V> put(String cacheName, K key, V value, V oldValue) {
        return new CacheEvent<>(EventType.PUT, cacheName, key, value, oldValue,
                null, false, Instant.now());
    }

    /**
     * Create GET event (hit)
     * 创建 GET 事件（命中）
     *
     * @param cacheName cache name | 缓存名称
     * @param key       the key | 键
     * @param value     the value | 值
     * @param <K>       key type | 键类型
     * @param <V>       value type | 值类型
     * @return event | 事件
     */
    public static <K, V> CacheEvent<K, V> getHit(String cacheName, K key, V value) {
        return new CacheEvent<>(EventType.GET, cacheName, key, value, null,
                null, true, Instant.now());
    }

    /**
     * Create GET event (miss)
     * 创建 GET 事件（未命中）
     *
     * @param cacheName cache name | 缓存名称
     * @param key       the key | 键
     * @param <K>       key type | 键类型
     * @param <V>       value type | 值类型
     * @return event | 事件
     */
    public static <K, V> CacheEvent<K, V> getMiss(String cacheName, K key) {
        return new CacheEvent<>(EventType.GET, cacheName, key, null, null,
                null, false, Instant.now());
    }

    /**
     * Create REMOVE event
     * 创建 REMOVE 事件
     *
     * @param cacheName cache name | 缓存名称
     * @param key       the key | 键
     * @param oldValue  the removed value | 被移除的值
     * @param <K>       key type | 键类型
     * @param <V>       value type | 值类型
     * @return event | 事件
     */
    public static <K, V> CacheEvent<K, V> remove(String cacheName, K key, V oldValue) {
        return new CacheEvent<>(EventType.REMOVE, cacheName, key, null, oldValue,
                RemovalCause.EXPLICIT, false, Instant.now());
    }

    /**
     * Create EXPIRE event
     * 创建 EXPIRE 事件
     *
     * @param cacheName cache name | 缓存名称
     * @param key       the key | 键
     * @param oldValue  the expired value | 过期的值
     * @param <K>       key type | 键类型
     * @param <V>       value type | 值类型
     * @return event | 事件
     */
    public static <K, V> CacheEvent<K, V> expire(String cacheName, K key, V oldValue) {
        return new CacheEvent<>(EventType.EXPIRE, cacheName, key, null, oldValue,
                RemovalCause.EXPIRED, false, Instant.now());
    }

    /**
     * Create EVICT event
     * 创建 EVICT 事件
     *
     * @param cacheName cache name | 缓存名称
     * @param key       the key | 键
     * @param oldValue  the evicted value | 被淘汰的值
     * @param cause     removal cause | 移除原因
     * @param <K>       key type | 键类型
     * @param <V>       value type | 值类型
     * @return event | 事件
     */
    public static <K, V> CacheEvent<K, V> evict(String cacheName, K key, V oldValue, RemovalCause cause) {
        return new CacheEvent<>(EventType.EVICT, cacheName, key, null, oldValue,
                cause, false, Instant.now());
    }

    /**
     * Create LOAD event
     * 创建 LOAD 事件
     *
     * @param cacheName cache name | 缓存名称
     * @param key       the key | 键
     * @param value     the loaded value | 加载的值
     * @param <K>       key type | 键类型
     * @param <V>       value type | 值类型
     * @return event | 事件
     */
    public static <K, V> CacheEvent<K, V> load(String cacheName, K key, V value) {
        return new CacheEvent<>(EventType.LOAD, cacheName, key, value, null,
                null, false, Instant.now());
    }

    /**
     * Create CLEAR event
     * 创建 CLEAR 事件
     *
     * @param cacheName cache name | 缓存名称
     * @param <K>       key type | 键类型
     * @param <V>       value type | 值类型
     * @return event | 事件
     */
    public static <K, V> CacheEvent<K, V> clear(String cacheName) {
        return new CacheEvent<>(EventType.CLEAR, cacheName, null, null, null,
                null, false, Instant.now());
    }

    // ==================== Convenience Methods | 便利方法 ====================

    /**
     * Get optional old value
     * 获取可选的旧值
     *
     * @return optional old value | 可选的旧值
     */
    public Optional<V> optionalOldValue() {
        return Optional.ofNullable(oldValue);
    }

    /**
     * Get optional value
     * 获取可选的值
     *
     * @return optional value | 可选的值
     */
    public Optional<V> optionalValue() {
        return Optional.ofNullable(value);
    }

    /**
     * Get optional removal cause
     * 获取可选的移除原因
     *
     * @return optional removal cause | 可选的移除原因
     */
    public Optional<RemovalCause> optionalRemovalCause() {
        return Optional.ofNullable(removalCause);
    }

    /**
     * Check if this is a write event (PUT, REMOVE, CLEAR)
     * 检查是否为写事件 (PUT, REMOVE, CLEAR)
     *
     * @return true if write event | 如果是写事件返回 true
     */
    public boolean isWriteEvent() {
        return type == EventType.PUT || type == EventType.REMOVE || type == EventType.CLEAR;
    }

    /**
     * Check if this is a removal event (REMOVE, EXPIRE, EVICT)
     * 检查是否为移除事件 (REMOVE, EXPIRE, EVICT)
     *
     * @return true if removal event | 如果是移除事件返回 true
     */
    public boolean isRemovalEvent() {
        return type == EventType.REMOVE || type == EventType.EXPIRE || type == EventType.EVICT;
    }
}
