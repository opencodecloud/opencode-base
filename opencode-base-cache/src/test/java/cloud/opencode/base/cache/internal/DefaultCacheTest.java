package cloud.opencode.base.cache.internal;

import cloud.opencode.base.cache.*;
import cloud.opencode.base.cache.config.CacheConfig;
import cloud.opencode.base.cache.model.RemovalCause;
import cloud.opencode.base.cache.spi.EvictionPolicy;
import cloud.opencode.base.cache.spi.RemovalListener;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * DefaultCache Test
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
class DefaultCacheTest {

    @BeforeEach
    void setup() {
        CacheManager.getInstance().reset();
    }

    // ==================== Basic Operations ====================

    @Test
    void shouldPutAndGet() {
        Cache<String, String> cache = createCache("test");

        cache.put("key", "value");

        assertThat(cache.get("key")).isEqualTo("value");
    }

    @Test
    void shouldReturnNullForMissing() {
        Cache<String, String> cache = createCache("test");

        assertThat(cache.get("missing")).isNull();
    }

    @Test
    void shouldComputeIfAbsent() {
        Cache<String, String> cache = createCache("test");

        String value = cache.get("key", k -> "computed-" + k);

        assertThat(value).isEqualTo("computed-key");
        assertThat(cache.get("key")).isEqualTo("computed-key");
    }

    @Test
    void shouldNotRecomputeExisting() {
        Cache<String, String> cache = createCache("test");
        cache.put("key", "existing");

        AtomicInteger loadCount = new AtomicInteger(0);
        String value = cache.get("key", k -> {
            loadCount.incrementAndGet();
            return "computed";
        });

        assertThat(value).isEqualTo("existing");
        assertThat(loadCount.get()).isEqualTo(0);
    }

    @Test
    void shouldRejectNullKey() {
        Cache<String, String> cache = createCache("test");

        assertThatThrownBy(() -> cache.put(null, "value"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNullValue() {
        Cache<String, String> cache = createCache("test");

        assertThatThrownBy(() -> cache.put("key", null))
                .isInstanceOf(NullPointerException.class);
    }

    // ==================== Batch Operations ====================

    @Test
    void shouldGetAll() {
        Cache<String, String> cache = createCache("test");
        cache.put("k1", "v1");
        cache.put("k2", "v2");
        cache.put("k3", "v3");

        Map<String, String> result = cache.getAll(List.of("k1", "k2", "k4"));

        assertThat(result).hasSize(2);
        assertThat(result).containsEntry("k1", "v1");
        assertThat(result).containsEntry("k2", "v2");
        assertThat(result).doesNotContainKey("k4");
    }

    @Test
    void shouldGetAllWithLoader() {
        Cache<String, String> cache = createCache("test");
        cache.put("k1", "v1");

        Map<String, String> result = cache.getAll(
                List.of("k1", "k2", "k3"),
                keys -> {
                    Map<String, String> loaded = new HashMap<>();
                    for (var key : keys) {
                        loaded.put(key, "loaded-" + key);
                    }
                    return loaded;
                }
        );

        assertThat(result).hasSize(3);
        assertThat(result).containsEntry("k1", "v1");
        assertThat(result).containsEntry("k2", "loaded-k2");
        assertThat(result).containsEntry("k3", "loaded-k3");
    }

    @Test
    void shouldPutAll() {
        Cache<String, String> cache = createCache("test");

        cache.putAll(Map.of("k1", "v1", "k2", "v2", "k3", "v3"));

        assertThat(cache.get("k1")).isEqualTo("v1");
        assertThat(cache.get("k2")).isEqualTo("v2");
        assertThat(cache.get("k3")).isEqualTo("v3");
    }

    @Test
    void shouldPutIfAbsent() {
        Cache<String, String> cache = createCache("test");

        boolean put1 = cache.putIfAbsent("key", "value1");
        boolean put2 = cache.putIfAbsent("key", "value2");

        assertThat(put1).isTrue();
        assertThat(put2).isFalse();
        assertThat(cache.get("key")).isEqualTo("value1");
    }

    // ==================== Invalidation ====================

    @Test
    void shouldInvalidate() {
        Cache<String, String> cache = createCache("test");
        cache.put("key", "value");

        cache.invalidate("key");

        assertThat(cache.get("key")).isNull();
    }

    @Test
    void shouldInvalidateAll() {
        Cache<String, String> cache = createCache("test");
        cache.put("k1", "v1");
        cache.put("k2", "v2");
        cache.put("k3", "v3");

        cache.invalidateAll(List.of("k1", "k3"));

        assertThat(cache.get("k1")).isNull();
        assertThat(cache.get("k2")).isEqualTo("v2");
        assertThat(cache.get("k3")).isNull();
    }

    @Test
    void shouldInvalidateAllEntries() {
        Cache<String, String> cache = createCache("test");
        cache.put("k1", "v1");
        cache.put("k2", "v2");

        cache.invalidateAll();

        assertThat(cache.estimatedSize()).isEqualTo(0);
    }

    // ==================== Query Operations ====================

    @Test
    void shouldCheckContainsKey() {
        Cache<String, String> cache = createCache("test");
        cache.put("key", "value");

        assertThat(cache.containsKey("key")).isTrue();
        assertThat(cache.containsKey("missing")).isFalse();
    }

    @Test
    void shouldGetSize() {
        Cache<String, String> cache = createCache("test");
        cache.put("k1", "v1");
        cache.put("k2", "v2");

        assertThat(cache.size()).isEqualTo(2);
        assertThat(cache.estimatedSize()).isEqualTo(2);
    }

    @Test
    void shouldGetKeys() {
        Cache<String, String> cache = createCache("test");
        cache.put("k1", "v1");
        cache.put("k2", "v2");

        Set<String> keys = cache.keys();

        assertThat(keys).containsExactlyInAnyOrder("k1", "k2");
    }

    @Test
    void shouldGetValues() {
        Cache<String, String> cache = createCache("test");
        cache.put("k1", "v1");
        cache.put("k2", "v2");

        Collection<String> values = cache.values();

        assertThat(values).containsExactlyInAnyOrder("v1", "v2");
    }

    @Test
    void shouldGetEntries() {
        Cache<String, String> cache = createCache("test");
        cache.put("k1", "v1");
        cache.put("k2", "v2");

        Set<Map.Entry<String, String>> entries = cache.entries();

        assertThat(entries).hasSize(2);
    }

    @Test
    void shouldGetAsMap() {
        Cache<String, String> cache = createCache("test");
        cache.put("k1", "v1");

        ConcurrentMap<String, String> map = cache.asMap();

        assertThat(map.get("k1")).isEqualTo("v1");
    }

    @Test
    void shouldSupportMapOperations() {
        Cache<String, String> cache = createCache("test");
        ConcurrentMap<String, String> map = cache.asMap();

        map.put("k1", "v1");
        assertThat(cache.get("k1")).isEqualTo("v1");

        map.remove("k1");
        assertThat(cache.get("k1")).isNull();

        map.putIfAbsent("k2", "v2");
        assertThat(cache.get("k2")).isEqualTo("v2");

        map.replace("k2", "v2", "v3");
        assertThat(cache.get("k2")).isEqualTo("v3");

        assertThat(map.containsKey("k2")).isTrue();
        assertThat(map.remove("k2", "v3")).isTrue();
    }

    // ==================== Expiration ====================

    @Test
    void shouldExpireAfterWrite() throws Exception {
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .expireAfterWrite(Duration.ofMillis(100))
                .build();
        Cache<String, String> cache = new DefaultCache<>("expiring", config);

        cache.put("key", "value");
        assertThat(cache.get("key")).isEqualTo("value");

        Thread.sleep(150);
        assertThat(cache.get("key")).isNull();
    }

    @Test
    void shouldExpireAfterAccess() throws Exception {
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .expireAfterAccess(Duration.ofMillis(100))
                .build();
        Cache<String, String> cache = new DefaultCache<>("expiring", config);

        cache.put("key", "value");

        // Keep accessing within TTI
        for (int i = 0; i < 5; i++) {
            Thread.sleep(50);
            assertThat(cache.get("key")).isEqualTo("value");
        }

        // Stop accessing and wait for expiration
        Thread.sleep(150);
        assertThat(cache.get("key")).isNull();
    }

    @Test
    void shouldCleanUpExpiredEntries() throws Exception {
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .expireAfterWrite(Duration.ofMillis(50))
                .build();
        Cache<String, String> cache = new DefaultCache<>("cleanup", config);

        cache.put("k1", "v1");
        cache.put("k2", "v2");

        Thread.sleep(100);
        cache.cleanUp();

        assertThat(cache.estimatedSize()).isEqualTo(0);
    }

    // ==================== Eviction ====================

    @Test
    void shouldEvictOnMaxSize() {
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .maximumSize(2)
                .build();
        Cache<String, String> cache = new DefaultCache<>("evicting", config);

        cache.put("k1", "v1");
        cache.put("k2", "v2");
        cache.put("k3", "v3");

        assertThat(cache.estimatedSize()).isEqualTo(2);
    }

    @Test
    void shouldUseCustomEvictionPolicy() {
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .maximumSize(2)
                .evictionPolicy(EvictionPolicy.lru())
                .build();
        Cache<String, String> cache = new DefaultCache<>("lru", config);

        cache.put("k1", "v1");
        cache.put("k2", "v2");
        cache.get("k1"); // Access k1 to make it recently used
        cache.put("k3", "v3"); // Should evict k2

        assertThat(cache.containsKey("k1")).isTrue();
        assertThat(cache.containsKey("k3")).isTrue();
    }

    // ==================== RemovalListener ====================

    @Test
    void shouldNotifyRemovalListener() {
        AtomicReference<String> removedKey = new AtomicReference<>();
        AtomicReference<String> removedValue = new AtomicReference<>();
        AtomicReference<RemovalCause> removedCause = new AtomicReference<>();

        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .removalListener((k, v, c) -> {
                    removedKey.set(k);
                    removedValue.set(v);
                    removedCause.set(c);
                })
                .build();
        Cache<String, String> cache = new DefaultCache<>("listener", config);

        cache.put("key", "value");
        cache.invalidate("key");

        assertThat(removedKey.get()).isEqualTo("key");
        assertThat(removedValue.get()).isEqualTo("value");
        assertThat(removedCause.get()).isEqualTo(RemovalCause.EXPLICIT);
    }

    @Test
    void shouldNotifyOnReplace() {
        AtomicReference<RemovalCause> cause = new AtomicReference<>();

        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .removalListener((k, v, c) -> cause.set(c))
                .build();
        Cache<String, String> cache = new DefaultCache<>("replace", config);

        cache.put("key", "value1");
        cache.put("key", "value2");

        assertThat(cause.get()).isEqualTo(RemovalCause.REPLACED);
    }

    // ==================== Statistics ====================

    @Test
    void shouldRecordStats() {
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .recordStats()
                .build();
        Cache<String, String> cache = new DefaultCache<>("stats", config);

        cache.put("key", "value");
        cache.get("key"); // hit
        cache.get("missing"); // miss

        CacheStats stats = cache.stats();
        assertThat(stats.hitCount()).isEqualTo(1);
        assertThat(stats.missCount()).isEqualTo(1);
    }

    @Test
    void shouldNotRecordStatsWhenDisabled() {
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .build(); // recordStats not called
        Cache<String, String> cache = new DefaultCache<>("nostats", config);

        cache.put("key", "value");
        cache.get("key");
        cache.get("missing");

        CacheStats stats = cache.stats();
        assertThat(stats.hitCount()).isEqualTo(0);
        assertThat(stats.missCount()).isEqualTo(0);
    }

    // ==================== Async View ====================

    @Test
    void shouldProvideAsyncView() {
        Cache<String, String> cache = createCache("async");

        AsyncCache<String, String> async = cache.async();

        assertThat(async).isNotNull();
        assertThat(async.sync()).isEqualTo(cache);
    }

    // ==================== Metadata ====================

    @Test
    void shouldReturnCacheName() {
        Cache<String, String> cache = createCache("myCache");

        assertThat(cache.name()).isEqualTo("myCache");
    }

    // ==================== Helper Methods ====================

    private Cache<String, String> createCache(String name) {
        return new DefaultCache<>(name, CacheConfig.defaultConfig());
    }
}
