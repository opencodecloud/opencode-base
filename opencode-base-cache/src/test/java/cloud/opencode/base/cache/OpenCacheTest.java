package cloud.opencode.base.cache;

import cloud.opencode.base.cache.spi.EvictionPolicy;
import cloud.opencode.base.cache.spi.ExpiryPolicy;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenCache Facade Test
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
class OpenCacheTest {

    @BeforeEach
    void setup() {
        CacheManager.getInstance().reset();
    }

    // ==================== Cache Creation ====================

    @Test
    void shouldGetOrCreateCache() {
        Cache<String, String> cache = OpenCache.getOrCreate("test");

        assertThat(cache).isNotNull();
        assertThat(cache.name()).isEqualTo("test");
    }

    @Test
    void shouldReturnSameCacheForSameName() {
        Cache<String, String> cache1 = OpenCache.getOrCreate("test");
        Cache<String, String> cache2 = OpenCache.getOrCreate("test");

        assertThat(cache1).isSameAs(cache2);
    }

    @Test
    void shouldGetOrCreateCacheWithConfig() {
        Cache<String, String> cache = OpenCache.getOrCreate("configured", config -> config
                .maximumSize(100)
                .expireAfterWrite(Duration.ofMinutes(10))
                .recordStats());

        assertThat(cache).isNotNull();

        cache.put("key", "value");
        cache.get("key");

        assertThat(cache.stats().hitCount()).isEqualTo(1);
    }

    @Test
    void shouldBuildCacheWithBuilder() {
        Cache<String, String> cache = OpenCache.<String, String>builder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofHours(1))
                .recordStats()
                .build("builderCache");

        assertThat(cache).isNotNull();
        assertThat(cache.name()).isEqualTo("builderCache");
    }

    @Test
    void shouldBuildAnonymousCache() {
        Cache<String, String> cache = OpenCache.<String, String>builder()
                .maximumSize(100)
                .build();

        assertThat(cache).isNotNull();
        assertThat(cache.name()).startsWith("anonymous-");
    }

    @Test
    void shouldBuildCacheWithAllOptions() {
        Cache<String, String> cache = OpenCache.<String, String>builder()
                .maximumSize(500)
                .expireAfterWrite(Duration.ofMinutes(30))
                .expireAfterAccess(Duration.ofMinutes(10))
                .evictionPolicy(OpenCache.lru())
                .recordStats()
                .useVirtualThreads()
                .build("fullConfig");

        assertThat(cache).isNotNull();
    }

    // ==================== Cache Query ====================

    @Test
    void shouldGetExistingCache() {
        OpenCache.getOrCreate("existing");

        var cache = OpenCache.<String, String>get("existing");

        assertThat(cache).isPresent();
    }

    @Test
    void shouldReturnEmptyForMissingCache() {
        var cache = OpenCache.get("nonexistent");

        assertThat(cache).isEmpty();
    }

    @Test
    void shouldGetAllCacheNames() {
        OpenCache.getOrCreate("cache1");
        OpenCache.getOrCreate("cache2");
        OpenCache.getOrCreate("cache3");

        Set<String> names = OpenCache.names();

        assertThat(names).containsExactlyInAnyOrder("cache1", "cache2", "cache3");
    }

    // ==================== Cache Management ====================

    @Test
    void shouldRemoveCache() {
        OpenCache.getOrCreate("toRemove");

        OpenCache.remove("toRemove");

        assertThat(OpenCache.get("toRemove")).isEmpty();
    }

    @Test
    void shouldCleanUpAllCaches() {
        Cache<String, String> cache1 = OpenCache.getOrCreate("c1");
        Cache<String, String> cache2 = OpenCache.getOrCreate("c2");

        cache1.put("k1", "v1");
        cache2.put("k2", "v2");

        OpenCache.cleanUpAll();

        // Should not throw and caches should still exist
        assertThat(OpenCache.names()).containsExactlyInAnyOrder("c1", "c2");
    }

    @Test
    void shouldGetGlobalStats() {
        Cache<String, String> cache1 = OpenCache.<String, String>builder()
                .recordStats()
                .build("stats1");
        Cache<String, String> cache2 = OpenCache.<String, String>builder()
                .recordStats()
                .build("stats2");

        cache1.put("k", "v");
        cache1.get("k");
        cache2.put("k", "v");
        cache2.get("missing");

        Map<String, CacheStats> allStats = OpenCache.stats();

        assertThat(allStats).containsKeys("stats1", "stats2");
        assertThat(allStats.get("stats1").hitCount()).isEqualTo(1);
        assertThat(allStats.get("stats2").missCount()).isEqualTo(1);
    }

    @Test
    void shouldShutdown() {
        Cache<String, String> cache = OpenCache.getOrCreate("shutdown");
        cache.put("key", "value");

        OpenCache.shutdown();

        assertThat(CacheManager.getInstance().isShutdown()).isTrue();

        // Reset for other tests
        CacheManager.getInstance().reset();
    }

    // ==================== Eviction Policy Factories ====================

    @Test
    void shouldCreateLruPolicy() {
        EvictionPolicy<String, String> policy = OpenCache.lru();

        assertThat(policy).isNotNull();
    }

    @Test
    void shouldCreateLfuPolicy() {
        EvictionPolicy<String, String> policy = OpenCache.lfu();

        assertThat(policy).isNotNull();
    }

    @Test
    void shouldCreateFifoPolicy() {
        EvictionPolicy<String, String> policy = OpenCache.fifo();

        assertThat(policy).isNotNull();
    }

    @Test
    void shouldCreateWTinyLfuPolicy() {
        EvictionPolicy<String, String> policy = OpenCache.wTinyLfu();

        assertThat(policy).isNotNull();
    }

    // ==================== Expiry Policy Factories ====================

    @Test
    void shouldCreateTtlPolicy() {
        ExpiryPolicy<String, String> policy = OpenCache.ttl(Duration.ofMinutes(30));

        assertThat(policy).isNotNull();
    }

    @Test
    void shouldCreateTtiPolicy() {
        ExpiryPolicy<String, String> policy = OpenCache.tti(Duration.ofMinutes(10));

        assertThat(policy).isNotNull();
    }

    @Test
    void shouldCreateCombinedPolicy() {
        ExpiryPolicy<String, String> policy = OpenCache.combined(
                Duration.ofHours(1), Duration.ofMinutes(30));

        assertThat(policy).isNotNull();
    }

    // ==================== Integration Tests ====================

    @Test
    void shouldWorkEndToEnd() {
        // Create cache with configuration
        Cache<String, String> cache = OpenCache.getOrCreate("users", config -> config
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(30))
                .evictionPolicy(OpenCache.lru())
                .recordStats());

        // Basic operations
        cache.put("user:1", "Alice");
        cache.put("user:2", "Bob");

        assertThat(cache.get("user:1")).isEqualTo("Alice");
        assertThat(cache.get("user:2")).isEqualTo("Bob");
        assertThat(cache.get("user:3")).isNull();

        // Compute if absent
        String user3 = cache.get("user:3", k -> "Charlie");
        assertThat(user3).isEqualTo("Charlie");

        // Check stats
        CacheStats stats = cache.stats();
        assertThat(stats.hitCount()).isGreaterThanOrEqualTo(2);
        assertThat(stats.missCount()).isGreaterThanOrEqualTo(1);

        // Batch operations
        cache.putAll(Map.of("user:4", "David", "user:5", "Eve"));
        assertThat(cache.estimatedSize()).isEqualTo(5);

        // Invalidation
        cache.invalidate("user:1");
        assertThat(cache.containsKey("user:1")).isFalse();

        // Get all names
        assertThat(OpenCache.names()).contains("users");
    }
}
