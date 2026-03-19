package cloud.opencode.base.cache.resilience;

import cloud.opencode.base.cache.Cache;
import cloud.opencode.base.cache.AsyncCache;
import cloud.opencode.base.cache.CacheStats;
import cloud.opencode.base.cache.distributed.DistributedCache;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

/**
 * GracefulDegradation Test — 优雅降级测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("GracefulDegradation - 优雅降级")
class GracefulDegradationTest {

    // ==================== 测试用简易 Cache 实现 ====================

    /**
     * Minimal in-memory Cache implementation for testing.
     */
    static class StubCache<K, V> implements Cache<K, V> {
        private final ConcurrentHashMap<K, V> store = new ConcurrentHashMap<>();

        @Override public V get(K key) { return store.get(key); }
        @Override public V get(K key, Function<? super K, ? extends V> loader) {
            return store.computeIfAbsent(key, loader);
        }
        @Override public Map<K, V> getAll(Iterable<? extends K> keys) {
            Map<K, V> result = new HashMap<>();
            for (K k : keys) { V v = store.get(k); if (v != null) result.put(k, v); }
            return result;
        }
        @Override public Map<K, V> getAll(Iterable<? extends K> keys,
                Function<? super Set<? extends K>, ? extends Map<K, V>> loader) {
            return getAll(keys);
        }
        @Override public void put(K key, V value) { store.put(key, value); }
        @Override public void putAll(Map<? extends K, ? extends V> map) { store.putAll(map); }
        @Override public boolean putIfAbsent(K key, V value) {
            return store.putIfAbsent(key, value) == null;
        }
        @Override public void putWithTtl(K key, V value, Duration ttl) { store.put(key, value); }
        @Override public void putAllWithTtl(Map<? extends K, ? extends V> map, Duration ttl) { store.putAll(map); }
        @Override public boolean putIfAbsentWithTtl(K key, V value, Duration ttl) {
            return store.putIfAbsent(key, value) == null;
        }
        @Override public void invalidate(K key) { store.remove(key); }
        @Override public void invalidateAll(Iterable<? extends K> keys) { for (K k : keys) store.remove(k); }
        @Override public void invalidateAll() { store.clear(); }
        @Override public boolean containsKey(K key) { return store.containsKey(key); }
        @Override public long size() { return store.size(); }
        @Override public long estimatedSize() { return store.size(); }
        @Override public Set<K> keys() { return Set.copyOf(store.keySet()); }
        @Override public Collection<V> values() { return List.copyOf(store.values()); }
        @Override public Set<Map.Entry<K, V>> entries() { return Set.copyOf(store.entrySet()); }
        @Override public ConcurrentMap<K, V> asMap() { return store; }
        @Override public CacheStats stats() { return null; }
        @Override public void cleanUp() {}
        @Override public AsyncCache<K, V> async() { return null; }
        @Override public String name() { return "stub"; }
    }

    /**
     * Minimal DistributedCache stub for testing. Can be configured to throw on access.
     */
    static class StubDistributedCache<K, V> implements DistributedCache<K, V> {
        private final ConcurrentHashMap<K, V> store = new ConcurrentHashMap<>();
        private volatile boolean shouldFail = false;

        void setShouldFail(boolean shouldFail) { this.shouldFail = shouldFail; }
        void putDirect(K key, V value) { store.put(key, value); }

        private void checkFailure() {
            if (shouldFail) throw new RuntimeException("distributed cache unavailable");
        }

        @Override public Optional<V> get(K key) { checkFailure(); return Optional.ofNullable(store.get(key)); }
        @Override public V getOrLoad(K key, Function<K, V> loader, Duration ttl) {
            checkFailure();
            return store.computeIfAbsent(key, loader);
        }
        @Override public Map<K, V> getAll(Collection<K> keys) { checkFailure(); return Map.of(); }
        @Override public void put(K key, V value) { checkFailure(); store.put(key, value); }
        @Override public void put(K key, V value, Duration ttl) { checkFailure(); store.put(key, value); }
        @Override public void putAll(Map<K, V> entries) { checkFailure(); store.putAll(entries); }
        @Override public void putAll(Map<K, V> entries, Duration ttl) { checkFailure(); store.putAll(entries); }
        @Override public boolean putIfAbsent(K key, V value, Duration ttl) {
            checkFailure();
            return store.putIfAbsent(key, value) == null;
        }
        @Override public boolean remove(K key) { checkFailure(); return store.remove(key) != null; }
        @Override public long removeAll(Collection<K> keys) { return 0; }
        @Override public boolean exists(K key) { checkFailure(); return store.containsKey(key); }
        @Override public Optional<Duration> getTtl(K key) { return Optional.empty(); }
        @Override public boolean setTtl(K key, Duration ttl) { return false; }
        @Override public CompletableFuture<Optional<V>> getAsync(K key) {
            return CompletableFuture.completedFuture(get(key));
        }
        @Override public CompletableFuture<Map<K, V>> getAllAsync(Collection<K> keys) {
            return CompletableFuture.completedFuture(Map.of());
        }
        @Override public CompletableFuture<Void> putAsync(K key, V value, Duration ttl) {
            put(key, value, ttl);
            return CompletableFuture.completedFuture(null);
        }
        @Override public CompletableFuture<Boolean> removeAsync(K key) {
            return CompletableFuture.completedFuture(remove(key));
        }
        @Override public long increment(K key, long delta) { return 0; }
        @Override public boolean compareAndSwap(K key, V expectedValue, V newValue, Duration ttl) { return false; }
        @Override public Set<K> keys(String pattern) { return Set.of(); }
        @Override public ScanResult<K> scan(String pattern, String cursor, int count) {
            return new ScanResult<>(Set.of(), "0", true);
        }
        @Override public long removeByPattern(String pattern) { return 0; }
        @Override public Optional<DistributedLock> tryLock(K lockKey, Duration ttl) { return Optional.empty(); }
        @Override public Optional<DistributedLock> lock(K lockKey, Duration ttl, Duration waitTime) { return Optional.empty(); }
        @Override public void publish(String channel, String message) {}
        @Override public Subscription subscribe(String channel, Consumer<String> handler) { return null; }
        @Override public cloud.opencode.base.cache.distributed.DistributedCacheStats stats() { return null; }
        @Override public boolean isHealthy() { return !shouldFail; }
        @Override public String name() { return "stub-distributed"; }
        @Override public void close() {}
    }

    // ==================== 创建与配置验证 ====================

    @Nested
    @DisplayName("创建与配置验证")
    class CreationAndConfigValidationTests {

        @Test
        @DisplayName("默认配置创建实例，初始状态为 NORMAL")
        void shouldCreateWithDefaultsInNormalState() {
            try (var degradation = GracefulDegradation.<String, String>create()) {
                assertThat(degradation.state()).isEqualTo(GracefulDegradation.State.NORMAL);
            }
        }

        @Test
        @DisplayName("默认配置参数正确: failureThreshold=5, recoveryWindow=30s, healthCheckInterval=10s")
        void shouldHaveCorrectDefaults() {
            GracefulDegradation.Config defaults = GracefulDegradation.Config.defaults();

            assertThat(defaults.failureThreshold()).isEqualTo(5);
            assertThat(defaults.recoveryWindow()).isEqualTo(Duration.ofSeconds(30));
            assertThat(defaults.healthCheckInterval()).isEqualTo(Duration.ofSeconds(10));
        }

        @Test
        @DisplayName("自定义配置创建实例")
        void shouldCreateWithCustomConfig() {
            GracefulDegradation.Config config = new GracefulDegradation.Config(
                    3, Duration.ofSeconds(10), Duration.ofSeconds(5));

            try (var degradation = GracefulDegradation.<String, String>create(config)) {
                assertThat(degradation.state()).isEqualTo(GracefulDegradation.State.NORMAL);
            }
        }

        @Test
        @DisplayName("failureThreshold < 1 时抛出 IllegalArgumentException")
        void shouldRejectFailureThresholdLessThanOne() {
            assertThatThrownBy(() ->
                    new GracefulDegradation.Config(0, Duration.ofSeconds(30), Duration.ofSeconds(10))
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("failureThreshold");
        }

        @Test
        @DisplayName("recoveryWindow 为 null 时抛出 NullPointerException")
        void shouldRejectNullRecoveryWindow() {
            assertThatThrownBy(() ->
                    new GracefulDegradation.Config(5, null, Duration.ofSeconds(10))
            ).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("recoveryWindow");
        }

        @Test
        @DisplayName("recoveryWindow 为零时抛出 IllegalArgumentException")
        void shouldRejectZeroRecoveryWindow() {
            assertThatThrownBy(() ->
                    new GracefulDegradation.Config(5, Duration.ZERO, Duration.ofSeconds(10))
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("recoveryWindow");
        }

        @Test
        @DisplayName("recoveryWindow 为负值时抛出 IllegalArgumentException")
        void shouldRejectNegativeRecoveryWindow() {
            assertThatThrownBy(() ->
                    new GracefulDegradation.Config(5, Duration.ofSeconds(-1), Duration.ofSeconds(10))
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("recoveryWindow");
        }

        @Test
        @DisplayName("healthCheckInterval 为 null 时抛出 NullPointerException")
        void shouldRejectNullHealthCheckInterval() {
            assertThatThrownBy(() ->
                    new GracefulDegradation.Config(5, Duration.ofSeconds(30), null)
            ).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("healthCheckInterval");
        }

        @Test
        @DisplayName("healthCheckInterval 为零时抛出 IllegalArgumentException")
        void shouldRejectZeroHealthCheckInterval() {
            assertThatThrownBy(() ->
                    new GracefulDegradation.Config(5, Duration.ofSeconds(30), Duration.ZERO)
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("healthCheckInterval");
        }

        @Test
        @DisplayName("healthCheckInterval 为负值时抛出 IllegalArgumentException")
        void shouldRejectNegativeHealthCheckInterval() {
            assertThatThrownBy(() ->
                    new GracefulDegradation.Config(5, Duration.ofSeconds(30), Duration.ofSeconds(-5))
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("healthCheckInterval");
        }
    }

    // ==================== NORMAL 状态操作 ====================

    @Nested
    @DisplayName("NORMAL 状态 - 正常操作")
    class NormalStateTests {

        private StubCache<String, String> localCache;
        private StubDistributedCache<String, String> distributedCache;
        private GracefulDegradation<String, String> degradation;

        @BeforeEach
        void setUp() {
            localCache = new StubCache<>();
            distributedCache = new StubDistributedCache<>();
            degradation = GracefulDegradation.create();
        }

        @AfterEach
        void tearDown() {
            degradation.close();
        }

        @Test
        @DisplayName("get 从分布式缓存获取值")
        void shouldGetFromDistributedCache() {
            distributedCache.putDirect("key1", "distributed-value");

            Optional<String> result = degradation.get("key1", localCache, distributedCache);

            assertThat(result).isPresent().contains("distributed-value");
        }

        @Test
        @DisplayName("get 分布式缓存未命中时回退到本地缓存")
        void shouldFallbackToLocalWhenDistributedMisses() {
            localCache.put("key1", "local-value");

            Optional<String> result = degradation.get("key1", localCache, distributedCache);

            assertThat(result).isPresent().contains("local-value");
        }

        @Test
        @DisplayName("get 两个缓存都未命中时返回空")
        void shouldReturnEmptyWhenBothMiss() {
            Optional<String> result = degradation.get("missing", localCache, distributedCache);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("put 同时写入本地和分布式缓存")
        void shouldPutToBothCaches() {
            degradation.put("key1", "value1", Duration.ofMinutes(5), localCache, distributedCache);

            assertThat(localCache.get("key1")).isEqualTo("value1");
            assertThat(distributedCache.get("key1")).isPresent().contains("value1");
        }
    }

    // ==================== 降级行为 ====================

    @Nested
    @DisplayName("降级行为 - 达到失败阈值后进入 DEGRADED")
    class DegradationTests {

        private StubCache<String, String> localCache;
        private StubDistributedCache<String, String> distributedCache;

        @BeforeEach
        void setUp() {
            localCache = new StubCache<>();
            distributedCache = new StubDistributedCache<>();
        }

        @Test
        @DisplayName("达到失败阈值后状态变为 DEGRADED")
        void shouldTransitionToDegradedAfterThreshold() {
            GracefulDegradation.Config config = new GracefulDegradation.Config(
                    3, Duration.ofSeconds(30), Duration.ofSeconds(10));

            try (var degradation = GracefulDegradation.<String, String>create(config)) {
                distributedCache.setShouldFail(true);
                localCache.put("key", "local-fallback");

                // Trigger 3 failures (threshold = 3)
                for (int i = 0; i < 3; i++) {
                    degradation.get("key", localCache, distributedCache);
                }

                assertThat(degradation.state()).isEqualTo(GracefulDegradation.State.DEGRADED);
            }
        }

        @Test
        @DisplayName("DEGRADED 状态下 get 仅从本地缓存读取")
        void shouldReadFromLocalOnlyWhenDegraded() {
            GracefulDegradation.Config config = new GracefulDegradation.Config(
                    2, Duration.ofSeconds(30), Duration.ofSeconds(10));

            try (var degradation = GracefulDegradation.<String, String>create(config)) {
                localCache.put("key", "local-value");
                distributedCache.setShouldFail(true);

                // Trigger degradation (2 failures)
                for (int i = 0; i < 2; i++) {
                    degradation.get("key", localCache, distributedCache);
                }

                assertThat(degradation.state()).isEqualTo(GracefulDegradation.State.DEGRADED);

                // Now get should read from local only (distributed still failing, but should not be called)
                Optional<String> result = degradation.get("key", localCache, distributedCache);
                assertThat(result).isPresent().contains("local-value");
            }
        }

        @Test
        @DisplayName("DEGRADED 状态下 put 仅写入本地缓存")
        void shouldWriteToLocalOnlyWhenDegraded() {
            GracefulDegradation.Config config = new GracefulDegradation.Config(
                    2, Duration.ofSeconds(30), Duration.ofSeconds(10));

            try (var degradation = GracefulDegradation.<String, String>create(config)) {
                distributedCache.setShouldFail(true);

                // Trigger degradation
                for (int i = 0; i < 2; i++) {
                    degradation.get("k" + i, localCache, distributedCache);
                }

                assertThat(degradation.state()).isEqualTo(GracefulDegradation.State.DEGRADED);

                // Put in degraded mode - only goes to local
                degradation.put("newKey", "newValue", Duration.ofMinutes(5), localCache, distributedCache);

                assertThat(localCache.get("newKey")).isEqualTo("newValue");
                // Distributed should not have the value (it's failing, but even conceptually it was not attempted)
            }
        }

        @Test
        @DisplayName("失败次数未达到阈值时保持 NORMAL")
        void shouldStayNormalBelowThreshold() {
            GracefulDegradation.Config config = new GracefulDegradation.Config(
                    5, Duration.ofSeconds(30), Duration.ofSeconds(10));

            try (var degradation = GracefulDegradation.<String, String>create(config)) {
                distributedCache.setShouldFail(true);

                // Only 4 failures, threshold is 5
                for (int i = 0; i < 4; i++) {
                    degradation.get("k" + i, localCache, distributedCache);
                }

                assertThat(degradation.state()).isEqualTo(GracefulDegradation.State.NORMAL);
            }
        }

        @Test
        @DisplayName("成功操作重置连续失败计数器")
        void shouldResetFailureCountOnSuccess() {
            GracefulDegradation.Config config = new GracefulDegradation.Config(
                    3, Duration.ofSeconds(30), Duration.ofSeconds(10));

            try (var degradation = GracefulDegradation.<String, String>create(config)) {
                distributedCache.setShouldFail(true);

                // 2 failures
                degradation.get("k1", localCache, distributedCache);
                degradation.get("k2", localCache, distributedCache);

                // 1 success resets the counter
                distributedCache.setShouldFail(false);
                degradation.get("k3", localCache, distributedCache);

                // 2 more failures - still below threshold of 3
                distributedCache.setShouldFail(true);
                degradation.get("k4", localCache, distributedCache);
                degradation.get("k5", localCache, distributedCache);

                assertThat(degradation.state()).isEqualTo(GracefulDegradation.State.NORMAL);
            }
        }
    }

    // ==================== 统计跟踪 ====================

    @Nested
    @DisplayName("统计跟踪 - Stats")
    class StatsTrackingTests {

        @Test
        @DisplayName("初始统计为零")
        void shouldStartWithZeroStats() {
            try (var degradation = GracefulDegradation.<String, String>create()) {
                GracefulDegradation.Stats stats = degradation.stats();

                assertThat(stats.totalRequests()).isZero();
                assertThat(stats.degradedRequests()).isZero();
                assertThat(stats.failoverCount()).isZero();
                assertThat(stats.currentState()).isEqualTo(GracefulDegradation.State.NORMAL);
                assertThat(stats.lastFailover()).isNull();
            }
        }

        @Test
        @DisplayName("正常请求增加 totalRequests")
        void shouldTrackTotalRequests() {
            try (var degradation = GracefulDegradation.<String, String>create()) {
                StubCache<String, String> local = new StubCache<>();
                StubDistributedCache<String, String> distributed = new StubDistributedCache<>();

                degradation.get("k1", local, distributed);
                degradation.put("k2", "v2", Duration.ofMinutes(1), local, distributed);

                assertThat(degradation.stats().totalRequests()).isEqualTo(2);
            }
        }

        @Test
        @DisplayName("降级后记录 degradedRequests 和 failoverCount")
        void shouldTrackDegradedRequestsAndFailovers() {
            GracefulDegradation.Config config = new GracefulDegradation.Config(
                    2, Duration.ofSeconds(30), Duration.ofSeconds(10));

            try (var degradation = GracefulDegradation.<String, String>create(config)) {
                StubCache<String, String> local = new StubCache<>();
                StubDistributedCache<String, String> distributed = new StubDistributedCache<>();
                distributed.setShouldFail(true);

                // 2 requests to trigger degradation
                degradation.get("k1", local, distributed);
                degradation.get("k2", local, distributed);

                assertThat(degradation.state()).isEqualTo(GracefulDegradation.State.DEGRADED);

                // 1 more request in degraded mode
                degradation.get("k3", local, distributed);

                GracefulDegradation.Stats stats = degradation.stats();
                assertThat(stats.totalRequests()).isEqualTo(3);
                assertThat(stats.degradedRequests()).isEqualTo(1);
                assertThat(stats.failoverCount()).isEqualTo(1);
                assertThat(stats.currentState()).isEqualTo(GracefulDegradation.State.DEGRADED);
                assertThat(stats.lastFailover()).isNotNull();
                assertThat(stats.lastFailover()).isBeforeOrEqualTo(Instant.now());
            }
        }
    }

    // ==================== 重置 ====================

    @Nested
    @DisplayName("重置 - reset 操作")
    class ResetTests {

        @Test
        @DisplayName("reset 将 DEGRADED 状态恢复为 NORMAL")
        void shouldResetDegradedToNormal() {
            GracefulDegradation.Config config = new GracefulDegradation.Config(
                    2, Duration.ofSeconds(30), Duration.ofSeconds(10));

            try (var degradation = GracefulDegradation.<String, String>create(config)) {
                StubCache<String, String> local = new StubCache<>();
                StubDistributedCache<String, String> distributed = new StubDistributedCache<>();
                distributed.setShouldFail(true);

                // Trigger degradation
                degradation.get("k1", local, distributed);
                degradation.get("k2", local, distributed);
                assertThat(degradation.state()).isEqualTo(GracefulDegradation.State.DEGRADED);

                // Reset
                degradation.reset();

                assertThat(degradation.state()).isEqualTo(GracefulDegradation.State.NORMAL);
            }
        }

        @Test
        @DisplayName("reset 后可以再次正常使用分布式缓存")
        void shouldWorkNormallyAfterReset() {
            GracefulDegradation.Config config = new GracefulDegradation.Config(
                    2, Duration.ofSeconds(30), Duration.ofSeconds(10));

            try (var degradation = GracefulDegradation.<String, String>create(config)) {
                StubCache<String, String> local = new StubCache<>();
                StubDistributedCache<String, String> distributed = new StubDistributedCache<>();
                distributed.setShouldFail(true);

                // Trigger degradation
                degradation.get("k1", local, distributed);
                degradation.get("k2", local, distributed);
                assertThat(degradation.state()).isEqualTo(GracefulDegradation.State.DEGRADED);

                // Reset and fix distributed cache
                degradation.reset();
                distributed.setShouldFail(false);
                distributed.putDirect("k3", "distributed-value");

                Optional<String> result = degradation.get("k3", local, distributed);
                assertThat(result).isPresent().contains("distributed-value");
                assertThat(degradation.state()).isEqualTo(GracefulDegradation.State.NORMAL);
            }
        }
    }

    // ==================== 关闭 ====================

    @Nested
    @DisplayName("关闭 - close 操作")
    class CloseTests {

        @Test
        @DisplayName("close 不抛出异常")
        void shouldCloseWithoutErrors() {
            var degradation = GracefulDegradation.<String, String>create();

            assertThatNoException().isThrownBy(degradation::close);
        }

        @Test
        @DisplayName("多次 close 不抛出异常")
        void shouldHandleMultipleCloses() {
            var degradation = GracefulDegradation.<String, String>create();

            assertThatNoException().isThrownBy(() -> {
                degradation.close();
                degradation.close();
            });
        }

        @Test
        @DisplayName("降级状态下 close 不抛出异常")
        void shouldCloseInDegradedState() {
            GracefulDegradation.Config config = new GracefulDegradation.Config(
                    1, Duration.ofSeconds(30), Duration.ofSeconds(10));
            var degradation = GracefulDegradation.<String, String>create(config);

            StubCache<String, String> local = new StubCache<>();
            StubDistributedCache<String, String> distributed = new StubDistributedCache<>();
            distributed.setShouldFail(true);

            // Trigger degradation
            degradation.get("k1", local, distributed);
            assertThat(degradation.state()).isEqualTo(GracefulDegradation.State.DEGRADED);

            assertThatNoException().isThrownBy(degradation::close);
        }
    }

    // ==================== State 枚举 ====================

    @Nested
    @DisplayName("State 枚举")
    class StateEnumTests {

        @Test
        @DisplayName("枚举包含三个状态: NORMAL, DEGRADED, RECOVERING")
        void shouldHaveThreeStates() {
            GracefulDegradation.State[] states = GracefulDegradation.State.values();

            assertThat(states).containsExactly(
                    GracefulDegradation.State.NORMAL,
                    GracefulDegradation.State.DEGRADED,
                    GracefulDegradation.State.RECOVERING
            );
        }
    }
}
