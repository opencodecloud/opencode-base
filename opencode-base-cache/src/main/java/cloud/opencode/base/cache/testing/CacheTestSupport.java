package cloud.opencode.base.cache.testing;

import cloud.opencode.base.cache.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * Cache Test Support - Testing utilities for cache unit tests
 * 缓存测试支持 - 缓存单元测试工具
 *
 * <p>Provides mock cache implementation and test utilities for unit testing
 * cache-dependent code without real cache infrastructure.</p>
 * <p>提供模拟缓存实现和测试工具，用于对依赖缓存的代码进行单元测试，
 * 无需真实的缓存基础设施。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>MockCache - Simple in-memory mock | 简单内存模拟</li>
 *   <li>TestClock - Controllable time for expiration testing | 可控时间用于过期测试</li>
 *   <li>RecordingCache - Records all operations | 记录所有操作</li>
 *   <li>Assertion helpers - 断言辅助方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create mock cache - 创建模拟缓存
 * MockCache<String, User> cache = CacheTestSupport.mockCache();
 * cache.put("user:1", user);
 * assertThat(cache.get("user:1")).isEqualTo(user);
 *
 * // Test expiration with controllable clock - 使用可控时钟测试过期
 * TestClock clock = CacheTestSupport.testClock();
 * MockCache<String, String> cache = CacheTestSupport.mockCache(clock);
 * cache.putWithTtl("key", "value", Duration.ofMinutes(5));
 *
 * clock.advance(Duration.ofMinutes(6));
 * assertThat(cache.get("key")).isNull();  // Expired
 *
 * // Record operations - 记录操作
 * RecordingCache<String, User> recording = CacheTestSupport.recordingCache();
 * recording.put("user:1", user);
 * assertThat(recording.operations()).contains(
 *     new CacheOperation.Put("user:1", user)
 * );
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (MockCache uses ConcurrentHashMap) - 线程安全: 是（MockCache 使用 ConcurrentHashMap）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public final class CacheTestSupport {

    private CacheTestSupport() {
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create a simple mock cache
     * 创建简单模拟缓存
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return mock cache | 模拟缓存
     */
    public static <K, V> MockCache<K, V> mockCache() {
        return new MockCache<>();
    }

    /**
     * Create mock cache with controllable clock
     * 创建带可控时钟的模拟缓存
     *
     * @param clock test clock | 测试时钟
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @return mock cache | 模拟缓存
     */
    public static <K, V> MockCache<K, V> mockCache(TestClock clock) {
        return new MockCache<>(clock);
    }

    /**
     * Create mock cache with name
     * 创建带名称的模拟缓存
     *
     * @param name  cache name | 缓存名称
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @return mock cache | 模拟缓存
     */
    public static <K, V> MockCache<K, V> mockCache(String name) {
        return new MockCache<>(name);
    }

    /**
     * Create a recording cache that tracks operations
     * 创建记录操作的缓存
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return recording cache | 记录缓存
     */
    public static <K, V> RecordingCache<K, V> recordingCache() {
        return new RecordingCache<>();
    }

    /**
     * Create a test clock
     * 创建测试时钟
     *
     * @return test clock | 测试时钟
     */
    public static TestClock testClock() {
        return new TestClock();
    }

    /**
     * Create a test clock with initial time
     * 创建带初始时间的测试时钟
     *
     * @param initialTime initial time | 初始时间
     * @return test clock | 测试时钟
     */
    public static TestClock testClock(Instant initialTime) {
        return new TestClock(initialTime);
    }

    // ==================== Test Clock | 测试时钟 ====================

    /**
     * Controllable clock for testing time-dependent cache behavior
     * 用于测试时间相关缓存行为的可控时钟
     */
    public static class TestClock {
        private final AtomicLong currentTimeMillis;

        public TestClock() {
            this(Instant.now());
        }

        public TestClock(Instant initialTime) {
            this.currentTimeMillis = new AtomicLong(initialTime.toEpochMilli());
        }

        /**
         * Get current time in milliseconds
         * 获取当前时间（毫秒）
         *
         * @return current time | 当前时间
         */
        public long millis() {
            return currentTimeMillis.get();
        }

        /**
         * Advance time by duration
         * 按时长推进时间
         *
         * @param duration duration to advance | 推进时长
         */
        public void advance(Duration duration) {
            currentTimeMillis.addAndGet(duration.toMillis());
        }

        /**
         * Set time to specific instant
         * 设置为特定时刻
         *
         * @param instant the instant | 时刻
         */
        public void setTime(Instant instant) {
            currentTimeMillis.set(instant.toEpochMilli());
        }

        /**
         * Set time in milliseconds
         * 设置时间（毫秒）
         *
         * @param millis time in milliseconds | 毫秒时间
         */
        public void setTime(long millis) {
            currentTimeMillis.set(millis);
        }
    }

    // ==================== Mock Cache | 模拟缓存 ====================

    /**
     * Simple mock cache implementation for testing
     * 用于测试的简单模拟缓存实现
     */
    public static class MockCache<K, V> implements Cache<K, V> {

        private final String name;
        private final ConcurrentHashMap<K, Entry<V>> store = new ConcurrentHashMap<>();
        private final TestClock clock;
        private final AtomicLong hitCount = new AtomicLong();
        private final AtomicLong missCount = new AtomicLong();

        public MockCache() {
            this("mock-cache", new TestClock());
        }

        public MockCache(String name) {
            this(name, new TestClock());
        }

        public MockCache(TestClock clock) {
            this("mock-cache", clock);
        }

        public MockCache(String name, TestClock clock) {
            this.name = name;
            this.clock = clock;
        }

        @Override
        public V get(K key) {
            Entry<V> entry = store.get(key);
            if (entry == null) {
                missCount.incrementAndGet();
                return null;
            }
            if (entry.isExpired(clock.millis())) {
                store.remove(key);
                missCount.incrementAndGet();
                return null;
            }
            hitCount.incrementAndGet();
            return entry.value;
        }

        @Override
        public V get(K key, Function<? super K, ? extends V> loader) {
            V value = get(key);
            if (value != null) {
                return value;
            }
            value = loader.apply(key);
            if (value != null) {
                put(key, value);
            }
            return value;
        }

        @Override
        public Map<K, V> getAll(Iterable<? extends K> keys) {
            Map<K, V> result = new LinkedHashMap<>();
            for (K key : keys) {
                V value = get(key);
                if (value != null) {
                    result.put(key, value);
                }
            }
            return result;
        }

        @Override
        public Map<K, V> getAll(Iterable<? extends K> keys,
                                Function<? super Set<? extends K>, ? extends Map<K, V>> loader) {
            Map<K, V> result = new LinkedHashMap<>();
            Set<K> missing = new LinkedHashSet<>();
            for (K key : keys) {
                V value = get(key);
                if (value != null) {
                    result.put(key, value);
                } else {
                    missing.add(key);
                }
            }
            if (!missing.isEmpty()) {
                Map<K, V> loaded = loader.apply(missing);
                putAll(loaded);
                result.putAll(loaded);
            }
            return result;
        }

        @Override
        public void put(K key, V value) {
            store.put(key, new Entry<>(value, Long.MAX_VALUE));
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> map) {
            for (Map.Entry<? extends K, ? extends V> e : map.entrySet()) {
                put(e.getKey(), e.getValue());
            }
        }

        @Override
        public boolean putIfAbsent(K key, V value) {
            return store.putIfAbsent(key, new Entry<>(value, Long.MAX_VALUE)) == null;
        }

        @Override
        public void putWithTtl(K key, V value, Duration ttl) {
            long expireAt = clock.millis() + ttl.toMillis();
            store.put(key, new Entry<>(value, expireAt));
        }

        @Override
        public void putAllWithTtl(Map<? extends K, ? extends V> map, Duration ttl) {
            long expireAt = clock.millis() + ttl.toMillis();
            for (Map.Entry<? extends K, ? extends V> e : map.entrySet()) {
                store.put(e.getKey(), new Entry<>(e.getValue(), expireAt));
            }
        }

        @Override
        public boolean putIfAbsentWithTtl(K key, V value, Duration ttl) {
            long expireAt = clock.millis() + ttl.toMillis();
            return store.putIfAbsent(key, new Entry<>(value, expireAt)) == null;
        }

        @Override
        public void invalidate(K key) {
            store.remove(key);
        }

        @Override
        public void invalidateAll(Iterable<? extends K> keys) {
            for (K key : keys) {
                store.remove(key);
            }
        }

        @Override
        public void invalidateAll() {
            store.clear();
        }

        @Override
        public boolean containsKey(K key) {
            Entry<V> entry = store.get(key);
            if (entry == null) return false;
            if (entry.isExpired(clock.millis())) {
                store.remove(key);
                return false;
            }
            return true;
        }

        @Override
        public long size() {
            cleanUp();
            return store.size();
        }

        @Override
        public long estimatedSize() {
            return store.size();
        }

        @Override
        public Set<K> keys() {
            return Set.copyOf(store.keySet());
        }

        @Override
        public Collection<V> values() {
            List<V> result = new ArrayList<>();
            for (Entry<V> entry : store.values()) {
                if (!entry.isExpired(clock.millis())) {
                    result.add(entry.value);
                }
            }
            return result;
        }

        @Override
        public Set<Map.Entry<K, V>> entries() {
            Set<Map.Entry<K, V>> result = new LinkedHashSet<>();
            long now = clock.millis();
            for (Map.Entry<K, Entry<V>> e : store.entrySet()) {
                if (!e.getValue().isExpired(now)) {
                    result.add(Map.entry(e.getKey(), e.getValue().value));
                }
            }
            return result;
        }

        @Override
        public ConcurrentMap<K, V> asMap() {
            ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();
            long now = clock.millis();
            for (Map.Entry<K, Entry<V>> e : store.entrySet()) {
                if (!e.getValue().isExpired(now)) {
                    map.put(e.getKey(), e.getValue().value);
                }
            }
            return map;
        }

        @Override
        public CacheStats stats() {
            return CacheStats.of(hitCount.get(), missCount.get(), 0, 0, 0, 0, 0);
        }

        @Override
        public CacheMetrics metrics() {
            return null;
        }

        @Override
        public void cleanUp() {
            long now = clock.millis();
            store.entrySet().removeIf(e -> e.getValue().isExpired(now));
        }

        @Override
        public AsyncCache<K, V> async() {
            throw new UnsupportedOperationException("MockCache does not support async");
        }

        @Override
        public String name() {
            return name;
        }

        /**
         * Get the test clock
         * 获取测试时钟
         *
         * @return test clock | 测试时钟
         */
        public TestClock clock() {
            return clock;
        }

        /**
         * Reset all data and statistics
         * 重置所有数据和统计
         */
        public void reset() {
            store.clear();
            hitCount.set(0);
            missCount.set(0);
        }

        private record Entry<V>(V value, long expireAt) {
            boolean isExpired(long now) {
                return now >= expireAt;
            }
        }
    }

    // ==================== Recording Cache | 记录缓存 ====================

    /**
     * Cache that records all operations for verification
     * 记录所有操作以供验证的缓存
     */
    public static class RecordingCache<K, V> implements Cache<K, V> {

        private final MockCache<K, V> delegate = new MockCache<>();
        private final List<CacheOperation<K, V>> operations = Collections.synchronizedList(new ArrayList<>());

        @Override
        public V get(K key) {
            operations.add(new CacheOperation.Get<>(key));
            return delegate.get(key);
        }

        @Override
        public V get(K key, Function<? super K, ? extends V> loader) {
            operations.add(new CacheOperation.GetWithLoader<>(key));
            return delegate.get(key, loader);
        }

        @Override
        public Map<K, V> getAll(Iterable<? extends K> keys) {
            operations.add(new CacheOperation.GetAll<>(keys));
            return delegate.getAll(keys);
        }

        @Override
        public Map<K, V> getAll(Iterable<? extends K> keys,
                                Function<? super Set<? extends K>, ? extends Map<K, V>> loader) {
            operations.add(new CacheOperation.GetAll<>(keys));
            return delegate.getAll(keys, loader);
        }

        @Override
        public void put(K key, V value) {
            operations.add(new CacheOperation.Put<>(key, value));
            delegate.put(key, value);
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> map) {
            operations.add(new CacheOperation.PutAll<>(map));
            delegate.putAll(map);
        }

        @Override
        public boolean putIfAbsent(K key, V value) {
            operations.add(new CacheOperation.PutIfAbsent<>(key, value));
            return delegate.putIfAbsent(key, value);
        }

        @Override
        public void putWithTtl(K key, V value, Duration ttl) {
            operations.add(new CacheOperation.PutWithTtl<>(key, value, ttl));
            delegate.putWithTtl(key, value, ttl);
        }

        @Override
        public void putAllWithTtl(Map<? extends K, ? extends V> map, Duration ttl) {
            operations.add(new CacheOperation.PutAllWithTtl<>(map, ttl));
            delegate.putAllWithTtl(map, ttl);
        }

        @Override
        public boolean putIfAbsentWithTtl(K key, V value, Duration ttl) {
            operations.add(new CacheOperation.PutIfAbsentWithTtl<>(key, value, ttl));
            return delegate.putIfAbsentWithTtl(key, value, ttl);
        }

        @Override
        public void invalidate(K key) {
            operations.add(new CacheOperation.Invalidate<>(key));
            delegate.invalidate(key);
        }

        @Override
        public void invalidateAll(Iterable<? extends K> keys) {
            operations.add(new CacheOperation.InvalidateAll<>(keys));
            delegate.invalidateAll(keys);
        }

        @Override
        public void invalidateAll() {
            operations.add(new CacheOperation.InvalidateAll<>(null));
            delegate.invalidateAll();
        }

        @Override
        public boolean containsKey(K key) {
            return delegate.containsKey(key);
        }

        @Override
        public long size() {
            return delegate.size();
        }

        @Override
        public long estimatedSize() {
            return delegate.estimatedSize();
        }

        @Override
        public Set<K> keys() {
            return delegate.keys();
        }

        @Override
        public Collection<V> values() {
            return delegate.values();
        }

        @Override
        public Set<Map.Entry<K, V>> entries() {
            return delegate.entries();
        }

        @Override
        public ConcurrentMap<K, V> asMap() {
            return delegate.asMap();
        }

        @Override
        public CacheStats stats() {
            return delegate.stats();
        }

        @Override
        public CacheMetrics metrics() {
            return delegate.metrics();
        }

        @Override
        public void cleanUp() {
            delegate.cleanUp();
        }

        @Override
        public AsyncCache<K, V> async() {
            return delegate.async();
        }

        @Override
        public String name() {
            return "recording-cache";
        }

        /**
         * Get all recorded operations
         * 获取所有记录的操作
         *
         * @return list of operations | 操作列表
         */
        public List<CacheOperation<K, V>> operations() {
            return List.copyOf(operations);
        }

        /**
         * Clear recorded operations
         * 清除记录的操作
         */
        public void clearOperations() {
            operations.clear();
        }

        /**
         * Get operation count
         * 获取操作计数
         *
         * @return operation count | 操作计数
         */
        public int operationCount() {
            return operations.size();
        }

        /**
         * Reset cache and operations
         * 重置缓存和操作
         */
        public void reset() {
            delegate.reset();
            operations.clear();
        }
    }

    // ==================== Cache Operations | 缓存操作 ====================

    /**
     * Sealed interface for cache operations
     * 缓存操作密封接口
     */
    public sealed interface CacheOperation<K, V> {

        record Get<K, V>(K key) implements CacheOperation<K, V> {
        }

        record GetWithLoader<K, V>(K key) implements CacheOperation<K, V> {
        }

        record GetAll<K, V>(Iterable<? extends K> keys) implements CacheOperation<K, V> {
        }

        record Put<K, V>(K key, V value) implements CacheOperation<K, V> {
        }

        record PutAll<K, V>(Map<? extends K, ? extends V> map) implements CacheOperation<K, V> {
        }

        record PutIfAbsent<K, V>(K key, V value) implements CacheOperation<K, V> {
        }

        record PutWithTtl<K, V>(K key, V value, Duration ttl) implements CacheOperation<K, V> {
        }

        record PutAllWithTtl<K, V>(Map<? extends K, ? extends V> map, Duration ttl) implements CacheOperation<K, V> {
        }

        record PutIfAbsentWithTtl<K, V>(K key, V value, Duration ttl) implements CacheOperation<K, V> {
        }

        record Invalidate<K, V>(K key) implements CacheOperation<K, V> {
        }

        record InvalidateAll<K, V>(Iterable<? extends K> keys) implements CacheOperation<K, V> {
        }
    }
}
