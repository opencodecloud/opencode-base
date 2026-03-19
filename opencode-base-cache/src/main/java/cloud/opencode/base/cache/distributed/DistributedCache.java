/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.cache.distributed;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Distributed Cache Interface - Abstraction for remote caching systems
 * 分布式缓存接口 - 远程缓存系统抽象
 *
 * <p>Provides a unified interface for distributed caching backends like Redis,
 * Memcached, Hazelcast, etc. All operations are designed to work across network
 * boundaries with proper error handling and timeout support.</p>
 * <p>为 Redis、Memcached、Hazelcast 等分布式缓存后端提供统一接口。
 * 所有操作都设计为跨网络边界工作，具有适当的错误处理和超时支持。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Synchronous and asynchronous APIs - 同步和异步 API</li>
 *   <li>TTL support for all entries - 所有条目的 TTL 支持</li>
 *   <li>Batch operations - 批量操作</li>
 *   <li>Atomic operations (CAS, increment) - 原子操作</li>
 *   <li>Key pattern scanning - 键模式扫描</li>
 *   <li>Distributed locking - 分布式锁</li>
 *   <li>Pub/Sub for cache invalidation - 用于缓存失效的发布/订阅</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Using Redis implementation
 * DistributedCache<String, User> cache = RedisCache.<String, User>builder()
 *     .connection(redisClient)
 *     .keyPrefix("user:")
 *     .defaultTtl(Duration.ofHours(1))
 *     .serializer(new JsonSerializer<>(User.class))
 *     .build();
 *
 * // Basic operations
 * cache.put("user:1001", user, Duration.ofHours(2));
 * Optional<User> user = cache.get("user:1001");
 *
 * // Async operations
 * cache.getAsync("user:1001")
 *     .thenAccept(user -> process(user));
 *
 * // Atomic operations
 * long newValue = cache.increment("counter:views", 1);
 *
 * // Distributed lock
 * try (var lock = cache.lock("resource:123", Duration.ofSeconds(30))) {
 *     // Critical section
 * }
 * }</pre>
 *
 * @param <K> the key type - 键类型
 * @param <V> the value type - 值类型
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Implementation-dependent - 空值安全: 取决于实现</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see DistributedCacheConfig
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public interface DistributedCache<K, V> extends AutoCloseable {

    // ==================== Basic Operations | 基本操作 ====================

    /**
     * Gets a value by key.
     * 根据键获取值。
     *
     * @param key the key - 键
     * @return Optional containing the value if present - 包含值的 Optional（如果存在）
     */
    Optional<V> get(K key);

    /**
     * Gets a value by key, or loads it using the provided function if absent.
     * 根据键获取值，如果不存在则使用提供的函数加载。
     *
     * @param key the key - 键
     * @param loader the loader function - 加载函数
     * @param ttl the TTL for loaded value - 加载值的 TTL
     * @return the value - 值
     */
    V getOrLoad(K key, Function<K, V> loader, Duration ttl);

    /**
     * Gets multiple values by keys.
     * 根据多个键获取多个值。
     *
     * @param keys the keys - 键集合
     * @return map of existing key-value pairs - 存在的键值对映射
     */
    Map<K, V> getAll(Collection<K> keys);

    /**
     * Puts a value with default TTL.
     * 使用默认 TTL 放入值。
     *
     * @param key the key - 键
     * @param value the value - 值
     */
    void put(K key, V value);

    /**
     * Puts a value with specific TTL.
     * 使用指定 TTL 放入值。
     *
     * @param key the key - 键
     * @param value the value - 值
     * @param ttl the time-to-live - 存活时间
     */
    void put(K key, V value, Duration ttl);

    /**
     * Puts multiple key-value pairs with default TTL.
     * 使用默认 TTL 放入多个键值对。
     *
     * @param entries the entries - 条目映射
     */
    void putAll(Map<K, V> entries);

    /**
     * Puts multiple key-value pairs with specific TTL.
     * 使用指定 TTL 放入多个键值对。
     *
     * @param entries the entries - 条目映射
     * @param ttl the time-to-live - 存活时间
     */
    void putAll(Map<K, V> entries, Duration ttl);

    /**
     * Puts a value only if the key doesn't exist.
     * 仅在键不存在时放入值。
     *
     * @param key the key - 键
     * @param value the value - 值
     * @param ttl the time-to-live - 存活时间
     * @return true if put successfully - 成功放入返回 true
     */
    boolean putIfAbsent(K key, V value, Duration ttl);

    /**
     * Removes a key.
     * 删除键。
     *
     * @param key the key - 键
     * @return true if removed - 删除成功返回 true
     */
    boolean remove(K key);

    /**
     * Removes multiple keys.
     * 删除多个键。
     *
     * @param keys the keys - 键集合
     * @return count of removed keys - 删除的键数量
     */
    long removeAll(Collection<K> keys);

    /**
     * Checks if a key exists.
     * 检查键是否存在。
     *
     * @param key the key - 键
     * @return true if exists - 存在返回 true
     */
    boolean exists(K key);

    /**
     * Gets the remaining TTL for a key.
     * 获取键的剩余 TTL。
     *
     * @param key the key - 键
     * @return Optional containing TTL, empty if key doesn't exist - 包含 TTL 的 Optional，键不存在则为空
     */
    Optional<Duration> getTtl(K key);

    /**
     * Updates the TTL for an existing key.
     * 更新现有键的 TTL。
     *
     * @param key the key - 键
     * @param ttl the new TTL - 新的 TTL
     * @return true if updated - 更新成功返回 true
     */
    boolean setTtl(K key, Duration ttl);

    // ==================== Async Operations | 异步操作 ====================

    /**
     * Gets a value asynchronously.
     * 异步获取值。
     *
     * @param key the key - 键
     * @return CompletableFuture with Optional value - 包含 Optional 值的 CompletableFuture
     */
    CompletableFuture<Optional<V>> getAsync(K key);

    /**
     * Gets multiple values asynchronously.
     * 异步获取多个值。
     *
     * @param keys the keys - 键集合
     * @return CompletableFuture with map of values - 包含值映射的 CompletableFuture
     */
    CompletableFuture<Map<K, V>> getAllAsync(Collection<K> keys);

    /**
     * Puts a value asynchronously.
     * 异步放入值。
     *
     * @param key the key - 键
     * @param value the value - 值
     * @param ttl the TTL - 存活时间
     * @return CompletableFuture that completes when done - 完成时完成的 CompletableFuture
     */
    CompletableFuture<Void> putAsync(K key, V value, Duration ttl);

    /**
     * Removes a key asynchronously.
     * 异步删除键。
     *
     * @param key the key - 键
     * @return CompletableFuture with removal result - 包含删除结果的 CompletableFuture
     */
    CompletableFuture<Boolean> removeAsync(K key);

    // ==================== Atomic Operations | 原子操作 ====================

    /**
     * Atomically increments a numeric value.
     * 原子地增加数值。
     *
     * @param key the key - 键
     * @param delta the increment amount - 增量
     * @return the new value - 新值
     */
    long increment(K key, long delta);

    /**
     * Atomically decrements a numeric value.
     * 原子地减少数值。
     *
     * @param key the key - 键
     * @param delta the decrement amount - 减量
     * @return the new value - 新值
     */
    default long decrement(K key, long delta) {
        return increment(key, -delta);
    }

    /**
     * Compare-and-swap operation.
     * 比较并交换操作。
     *
     * @param key the key - 键
     * @param expectedValue the expected current value - 期望的当前值
     * @param newValue the new value - 新值
     * @param ttl the TTL for new value - 新值的 TTL
     * @return true if swapped - 交换成功返回 true
     */
    boolean compareAndSwap(K key, V expectedValue, V newValue, Duration ttl);

    // ==================== Key Operations | 键操作 ====================

    /**
     * Gets all keys matching a pattern.
     * 获取匹配模式的所有键。
     *
     * <p>Pattern syntax depends on the backend (e.g., Redis uses glob patterns).</p>
     * <p>模式语法取决于后端（例如 Redis 使用 glob 模式）。</p>
     *
     * @param pattern the pattern - 模式
     * @return set of matching keys - 匹配的键集合
     */
    Set<K> keys(String pattern);

    /**
     * Scans keys matching a pattern with cursor-based pagination.
     * 使用基于游标的分页扫描匹配模式的键。
     *
     * @param pattern the pattern - 模式
     * @param cursor the cursor (empty for first scan) - 游标（第一次扫描为空）
     * @param count hint for number of keys to return - 返回键数量的提示
     * @return scan result with keys and next cursor - 包含键和下一个游标的扫描结果
     */
    ScanResult<K> scan(String pattern, String cursor, int count);

    /**
     * Removes all keys matching a pattern.
     * 删除匹配模式的所有键。
     *
     * @param pattern the pattern - 模式
     * @return count of removed keys - 删除的键数量
     */
    long removeByPattern(String pattern);

    // ==================== Distributed Locking | 分布式锁 ====================

    /**
     * Acquires a distributed lock.
     * 获取分布式锁。
     *
     * @param lockKey the lock key - 锁键
     * @param ttl the lock TTL (auto-release time) - 锁 TTL（自动释放时间）
     * @return the lock handle, or empty if lock not acquired - 锁句柄，未获取则为空
     */
    Optional<DistributedLock> tryLock(K lockKey, Duration ttl);

    /**
     * Acquires a distributed lock, waiting up to the specified time.
     * 获取分布式锁，最多等待指定时间。
     *
     * @param lockKey the lock key - 锁键
     * @param ttl the lock TTL - 锁 TTL
     * @param waitTime maximum time to wait - 最大等待时间
     * @return the lock handle, or empty if timeout - 锁句柄，超时则为空
     */
    Optional<DistributedLock> lock(K lockKey, Duration ttl, Duration waitTime);

    // ==================== Pub/Sub for Invalidation | 发布/订阅用于失效 ====================

    /**
     * Publishes a cache invalidation message.
     * 发布缓存失效消息。
     *
     * @param channel the channel - 频道
     * @param message the message - 消息
     */
    void publish(String channel, String message);

    /**
     * Subscribes to cache invalidation messages.
     * 订阅缓存失效消息。
     *
     * @param channel the channel - 频道
     * @param handler the message handler - 消息处理器
     * @return subscription handle - 订阅句柄
     */
    Subscription subscribe(String channel, java.util.function.Consumer<String> handler);

    // ==================== Statistics | 统计 ====================

    /**
     * Gets cache statistics.
     * 获取缓存统计。
     *
     * @return the statistics - 统计信息
     */
    DistributedCacheStats stats();

    /**
     * Checks if the cache is connected and healthy.
     * 检查缓存是否已连接且健康。
     *
     * @return true if healthy - 健康返回 true
     */
    boolean isHealthy();

    /**
     * Gets the cache name.
     * 获取缓存名称。
     *
     * @return the name - 名称
     */
    String name();

    // ==================== Lifecycle | 生命周期 ====================

    /**
     * Closes the cache connection.
     * 关闭缓存连接。
     */
    @Override
    void close();

    // ==================== Nested Types | 嵌套类型 ====================

    /**
     * Result of a scan operation.
     * 扫描操作的结果。
     *
     * @param <K> the key type - 键类型
     */
    record ScanResult<K>(Set<K> keys, String nextCursor, boolean finished) {}

    /**
     * Handle for a distributed lock.
     * 分布式锁的句柄。
     */
    interface DistributedLock extends AutoCloseable {
        /**
         * Gets the lock key.
         * 获取锁键。
         */
        Object key();

        /**
         * Gets the lock token (for verification).
         * 获取锁令牌（用于验证）。
         */
        String token();

        /**
         * Extends the lock TTL.
         * 延长锁 TTL。
         */
        boolean extend(Duration ttl);

        /**
         * Releases the lock.
         * 释放锁。
         */
        @Override
        void close();
    }

    /**
     * Handle for a subscription.
     * 订阅的句柄。
     */
    interface Subscription extends AutoCloseable {
        /**
         * Gets the channel.
         * 获取频道。
         */
        String channel();

        /**
         * Checks if active.
         * 检查是否活跃。
         */
        boolean isActive();

        /**
         * Unsubscribes.
         * 取消订阅。
         */
        @Override
        void close();
    }
}
