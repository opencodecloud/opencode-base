package cloud.opencode.base.cache;

import cloud.opencode.base.cache.config.CacheConfig;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * CacheManager Test
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
class CacheManagerTest {

    private CacheManager manager;

    @BeforeEach
    void setup() {
        manager = CacheManager.getInstance();
        manager.reset();
    }

    @AfterEach
    void tearDown() {
        manager.reset();
    }

    @Test
    void shouldBeSingleton() {
        CacheManager instance1 = CacheManager.getInstance();
        CacheManager instance2 = CacheManager.getInstance();

        assertThat(instance1).isSameAs(instance2);
    }

    // ==================== Cache Creation ====================

    @Test
    void shouldGetOrCreateCache() {
        Cache<String, String> cache = manager.getOrCreateCache("test");

        assertThat(cache).isNotNull();
    }

    @Test
    void shouldReturnSameCacheForSameName() {
        Cache<String, String> cache1 = manager.getOrCreateCache("test");
        Cache<String, String> cache2 = manager.getOrCreateCache("test");

        assertThat(cache1).isSameAs(cache2);
    }

    @Test
    void shouldGetOrCreateCacheWithConfig() {
        Cache<String, String> cache = manager.getOrCreateCache("configured", config -> config
                .maximumSize(500)
                .expireAfterWrite(Duration.ofMinutes(10)));

        assertThat(cache).isNotNull();
    }

    @Test
    void shouldCreateCacheWithConfig() {
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .maximumSize(100)
                .recordStats()
                .build();

        Cache<String, String> cache = manager.createCache("created", config);

        assertThat(cache).isNotNull();
    }

    @Test
    void shouldReplaceExistingCacheOnCreate() {
        Cache<String, String> cache1 = manager.getOrCreateCache("replace");
        cache1.put("key", "value1");

        CacheConfig<String, String> config = CacheConfig.defaultConfig();
        Cache<String, String> cache2 = manager.createCache("replace", config);

        assertThat(cache2).isNotSameAs(cache1);
        assertThat(cache2.get("key")).isNull(); // New cache should be empty
    }

    // ==================== Cache Query ====================

    @Test
    void shouldGetExistingCache() {
        manager.getOrCreateCache("existing");

        Optional<Cache<String, String>> cache = manager.getCache("existing");

        assertThat(cache).isPresent();
    }

    @Test
    void shouldReturnEmptyForMissingCache() {
        Optional<Cache<String, String>> cache = manager.getCache("missing");

        assertThat(cache).isEmpty();
    }

    @Test
    void shouldGetAllCacheNames() {
        manager.getOrCreateCache("c1");
        manager.getOrCreateCache("c2");
        manager.getOrCreateCache("c3");

        Set<String> names = manager.getCacheNames();

        assertThat(names).containsExactlyInAnyOrder("c1", "c2", "c3");
    }

    @Test
    void shouldReturnImmutableCacheNames() {
        manager.getOrCreateCache("test");

        Set<String> names = manager.getCacheNames();

        assertThatThrownBy(() -> names.add("new"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    // ==================== Cache Removal ====================

    @Test
    void shouldRemoveCache() {
        Cache<String, String> cache = manager.getOrCreateCache("toRemove");
        cache.put("key", "value");

        manager.removeCache("toRemove");

        assertThat(manager.getCache("toRemove")).isEmpty();
    }

    @Test
    void shouldInvalidateAllOnRemove() {
        Cache<String, String> cache = manager.getOrCreateCache("toRemove");
        cache.put("key", "value");

        manager.removeCache("toRemove");

        // Cache instance should be invalidated
        assertThat(cache.estimatedSize()).isEqualTo(0);
    }

    @Test
    void shouldHandleRemoveNonExistent() {
        // Should not throw
        manager.removeCache("nonexistent");
    }

    // ==================== Statistics ====================

    @Test
    void shouldGetAllStats() {
        Cache<String, String> cache1 = manager.getOrCreateCache("s1", c -> c.recordStats());
        Cache<String, String> cache2 = manager.getOrCreateCache("s2", c -> c.recordStats());

        cache1.put("k", "v");
        cache1.get("k"); // hit
        cache2.get("missing"); // miss

        Map<String, CacheStats> stats = manager.getAllStats();

        assertThat(stats).hasSize(2);
        assertThat(stats.get("s1").hitCount()).isEqualTo(1);
        assertThat(stats.get("s2").missCount()).isEqualTo(1);
    }

    @Test
    void shouldGetCombinedStats() {
        Cache<String, String> cache1 = manager.getOrCreateCache("c1", c -> c.recordStats());
        Cache<String, String> cache2 = manager.getOrCreateCache("c2", c -> c.recordStats());

        cache1.put("k", "v");
        cache1.get("k"); // hit
        cache1.get("k"); // hit
        cache2.put("k", "v");
        cache2.get("missing"); // miss

        CacheStats combined = manager.getCombinedStats();

        assertThat(combined.hitCount()).isEqualTo(2);
        assertThat(combined.missCount()).isEqualTo(1);
    }

    // ==================== Bulk Operations ====================

    @Test
    void shouldCleanUpAllCaches() {
        Cache<String, String> cache1 = manager.getOrCreateCache("c1");
        Cache<String, String> cache2 = manager.getOrCreateCache("c2");

        cache1.put("k1", "v1");
        cache2.put("k2", "v2");

        manager.cleanUpAll();

        // Caches should still exist
        assertThat(manager.getCacheNames()).hasSize(2);
    }

    @Test
    void shouldInvalidateAll() {
        Cache<String, String> cache1 = manager.getOrCreateCache("c1");
        Cache<String, String> cache2 = manager.getOrCreateCache("c2");

        cache1.put("k1", "v1");
        cache2.put("k2", "v2");

        manager.invalidateAll();

        assertThat(cache1.estimatedSize()).isEqualTo(0);
        assertThat(cache2.estimatedSize()).isEqualTo(0);
    }

    // ==================== Shutdown ====================

    @Test
    void shouldShutdown() {
        manager.getOrCreateCache("test");

        manager.shutdown();

        assertThat(manager.isShutdown()).isTrue();
        assertThat(manager.getCacheNames()).isEmpty();
    }

    @Test
    void shouldRejectOperationsAfterShutdown() {
        manager.shutdown();

        assertThatThrownBy(() -> manager.getOrCreateCache("test"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shut down");
    }

    @Test
    void shouldRejectCreateAfterShutdown() {
        manager.shutdown();

        assertThatThrownBy(() -> manager.createCache("test", CacheConfig.defaultConfig()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ==================== Reset ====================

    @Test
    void shouldReset() {
        manager.getOrCreateCache("test");
        manager.shutdown();

        manager.reset();

        assertThat(manager.isShutdown()).isFalse();
        assertThat(manager.getCacheNames()).isEmpty();

        // Should work again
        Cache<String, String> cache = manager.getOrCreateCache("new");
        assertThat(cache).isNotNull();
    }

    // ==================== Thread Safety ====================

    @Test
    void shouldBeThreadSafe() throws Exception {
        int threadCount = 10;
        int operationsPerThread = 100;

        java.util.concurrent.CountDownLatch startLatch = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch doneLatch = new java.util.concurrent.CountDownLatch(threadCount);

        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threadCount);

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < operationsPerThread; i++) {
                        String name = "cache-" + threadId + "-" + i;
                        Cache<String, String> cache = manager.getOrCreateCache(name);
                        cache.put("key", "value");
                        manager.getCache(name);
                    }
                } catch (Exception e) {
                    // Ignore
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(10, java.util.concurrent.TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(manager.getCacheNames()).hasSize(threadCount * operationsPerThread);
    }
}
