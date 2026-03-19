package cloud.opencode.base.cache.util;

import cloud.opencode.base.cache.*;
import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;

/**
 * CacheUtil Test
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
class CacheUtilTest {

    @BeforeEach
    void setup() {
        CacheManager.getInstance().reset();
    }

    // ==================== Key Generation Tests ====================

    @Test
    void shouldGenerateSimpleKey() {
        String key = CacheUtil.key("user", 123, "tenant1");
        assertThat(key).isEqualTo("user:123:tenant1");
    }

    @Test
    void shouldGenerateKeyWithPrefixOnly() {
        String key = CacheUtil.key("user");
        assertThat(key).isEqualTo("user");
    }

    @Test
    void shouldGenerateKeyWithNullParts() {
        String key = CacheUtil.key("user", (Object[]) null);
        assertThat(key).isEqualTo("user");
    }

    @Test
    void shouldGenerateKeyWithEmptyParts() {
        String key = CacheUtil.key("user", new Object[0]);
        assertThat(key).isEqualTo("user");
    }

    @Test
    void shouldGenerateKeyWithMultipleParts() {
        String key = CacheUtil.key("cache", "region", "tenant", "id", 12345);
        assertThat(key).isEqualTo("cache:region:tenant:id:12345");
    }

    @Test
    void shouldGenerateHashKey() {
        String key = CacheUtil.hashKey("user", "very", "long", "key", "parts");
        assertThat(key).startsWith("user:");
        assertThat(key).doesNotContain("very");
        assertThat(key).doesNotContain("long");
    }

    @Test
    void shouldGenerateDifferentHashKeysForDifferentInput() {
        String key1 = CacheUtil.hashKey("user", "a", "b");
        String key2 = CacheUtil.hashKey("user", "c", "d");
        assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    void shouldGenerateSameHashKeysForSameInput() {
        String key1 = CacheUtil.hashKey("user", "a", "b");
        String key2 = CacheUtil.hashKey("user", "a", "b");
        assertThat(key1).isEqualTo(key2);
    }

    // ==================== Cache Warming Tests ====================

    @Test
    void shouldWarmUpCache() {
        Cache<String, String> cache = OpenCache.getOrCreate("warmup");
        Map<String, String> data = Map.of("k1", "v1", "k2", "v2", "k3", "v3");

        CacheUtil.warmUp(cache, data);

        assertThat(cache.get("k1")).isEqualTo("v1");
        assertThat(cache.get("k2")).isEqualTo("v2");
        assertThat(cache.get("k3")).isEqualTo("v3");
    }

    @Test
    void shouldWarmUpAsyncCache() throws Exception {
        Cache<String, String> cache = OpenCache.getOrCreate("asyncWarmup");
        AsyncCache<String, String> asyncCache = cache.async();
        Map<String, String> data = Map.of("k1", "v1", "k2", "v2");

        CompletableFuture<Void> future = CacheUtil.warmUpAsync(asyncCache, data);
        future.get();

        assertThat(cache.get("k1")).isEqualTo("v1");
        assertThat(cache.get("k2")).isEqualTo("v2");
    }

    // ==================== Statistics Formatting Tests ====================

    @Test
    void shouldFormatStats() {
        CacheStats stats = CacheStats.of(100, 20, 10, 2, 1000000, 5, 5);
        String formatted = CacheUtil.formatStats(stats);

        assertThat(formatted).contains("Requests: 120");
        assertThat(formatted).contains("Hits: 100");
        assertThat(formatted).contains("Misses: 20");
        assertThat(formatted).contains("Evictions: 5");
    }

    @Test
    void shouldFormatStatsWithZeroValues() {
        CacheStats stats = CacheStats.empty();
        String formatted = CacheUtil.formatStats(stats);

        assertThat(formatted).contains("Requests: 0");
        assertThat(formatted).contains("Hits: 0");
    }

    @Test
    void shouldCompareStats() {
        CacheStats before = CacheStats.of(100, 20, 10, 2, 1000000, 5, 5);
        CacheStats after = CacheStats.of(150, 30, 15, 3, 1500000, 8, 8);

        String delta = CacheUtil.compareStats(before, after);

        assertThat(delta).contains("Requests: 60");
        assertThat(delta).contains("Hits: 50");
    }

    @Test
    void shouldFormatStatsAsJson() {
        CacheStats stats = CacheStats.of(100, 20, 10, 2, 1000000, 5, 5);
        String json = CacheUtil.formatStatsJson(stats);

        assertThat(json).contains("\"requests\":120");
        assertThat(json).contains("\"hits\":100");
        assertThat(json).contains("\"misses\":20");
        assertThat(json).contains("\"hitRate\":");
        assertThat(json).contains("\"missRate\":");
        assertThat(json).contains("\"loadSuccess\":10");
        assertThat(json).contains("\"loadFailure\":2");
        assertThat(json).contains("\"evictions\":5");
    }

    // ==================== Utility Methods Tests ====================

    @Test
    void shouldCalculateOptimalCacheSize() {
        // 100MB memory, 1KB average entry = 71680 entries (70% of 102400)
        long size = CacheUtil.optimalCacheSize(100, 1024);
        assertThat(size).isEqualTo(71680);
    }

    @Test
    void shouldCalculateOptimalCacheSizeForSmallMemory() {
        // 10MB memory, 100 bytes = 10 * 1024 * 1024 * 0.7 / 100 = 73400 entries
        long size = CacheUtil.optimalCacheSize(10, 100);
        assertThat(size).isEqualTo(73400);
    }

    @Test
    void shouldCalculateTtl() {
        // Max staleness 60s, update frequency 30s -> TTL = 15s (half of update)
        long ttl = CacheUtil.calculateTtl(60, 30);
        assertThat(ttl).isEqualTo(15);
    }

    @Test
    void shouldCalculateTtlWithHighFrequency() {
        // Max staleness 60s, update frequency 120s -> TTL = 60s (min of max staleness)
        long ttl = CacheUtil.calculateTtl(60, 120);
        assertThat(ttl).isEqualTo(60);
    }

    @Test
    void shouldCalculateTtlWithMinimumOne() {
        // Very low values should return at least 1
        long ttl = CacheUtil.calculateTtl(1, 1);
        assertThat(ttl).isGreaterThanOrEqualTo(1);
    }
}
