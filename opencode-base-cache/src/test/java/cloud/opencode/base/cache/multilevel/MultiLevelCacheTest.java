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

package cloud.opencode.base.cache.multilevel;

import cloud.opencode.base.cache.Cache;
import cloud.opencode.base.cache.OpenCache;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for MultiLevelCache
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("MultiLevelCache Tests")
class MultiLevelCacheTest {

    private Cache<String, String> l1Cache;
    private Cache<String, String> l2Cache;
    private Cache<String, String> l3Cache;

    @BeforeEach
    void setUp() {
        l1Cache = OpenCache.<String, String>builder()
                .maximumSize(100)
                .build("l1-" + System.nanoTime());
        l2Cache = OpenCache.<String, String>builder()
                .maximumSize(500)
                .build("l2-" + System.nanoTime());
        l3Cache = OpenCache.<String, String>builder()
                .maximumSize(1000)
                .build("l3-" + System.nanoTime());
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("builder creates cache")
        void builderCreatesCache() {
            MultiLevelCache<String, String> cache = MultiLevelCache.<String, String>builder()
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L1")
                            .cache(l1Cache)
                            .build())
                    .build();

            assertNotNull(cache);
        }

        @Test
        @DisplayName("builder throws for no levels")
        void builderThrowsForNoLevels() {
            assertThrows(IllegalArgumentException.class, () ->
                    MultiLevelCache.<String, String>builder().build());
        }

        @Test
        @DisplayName("builder sets name")
        void builderSetsName() {
            MultiLevelCache<String, String> cache = MultiLevelCache.<String, String>builder()
                    .name("my-multi-cache")
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L1")
                            .cache(l1Cache)
                            .build())
                    .build();

            assertEquals("my-multi-cache", cache.name());
        }

        @Test
        @DisplayName("builder sets write policy")
        void builderSetsWritePolicy() {
            MultiLevelCache<String, String> cache = MultiLevelCache.<String, String>builder()
                    .writePolicy(MultiLevelCache.WritePolicy.WRITE_FIRST)
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L1")
                            .cache(l1Cache)
                            .build())
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L2")
                            .cache(l2Cache)
                            .build())
                    .build();

            cache.put("key", "value");

            assertEquals("value", l1Cache.get("key"));
            assertNull(l2Cache.get("key"));
        }

        @Test
        @DisplayName("builder sets invalidation policy")
        void builderSetsInvalidationPolicy() {
            MultiLevelCache<String, String> cache = MultiLevelCache.<String, String>builder()
                    .invalidationPolicy(MultiLevelCache.InvalidationPolicy.INVALIDATE_FIRST)
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L1")
                            .cache(l1Cache)
                            .build())
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L2")
                            .cache(l2Cache)
                            .build())
                    .build();

            cache.put("key", "value");
            cache.invalidate("key");

            assertNull(l1Cache.get("key"));
            assertEquals("value", l2Cache.get("key"));
        }
    }

    @Nested
    @DisplayName("LevelConfig Tests")
    class LevelConfigTests {

        @Test
        @DisplayName("LevelConfig builder creates config")
        void levelConfigBuilderCreatesConfig() {
            MultiLevelCache.LevelConfig<String, String> config = MultiLevelCache.LevelConfig.<String, String>builder()
                    .name("L1")
                    .cache(l1Cache)
                    .ttl(Duration.ofMinutes(5))
                    .promoteOnHit(true)
                    .writeEnabled(true)
                    .build();

            assertEquals("L1", config.name());
            assertEquals(l1Cache, config.cache());
            assertEquals(Duration.ofMinutes(5), config.ttl());
            assertTrue(config.promoteOnHit());
            assertTrue(config.writeEnabled());
        }

        @Test
        @DisplayName("LevelConfig throws on null cache")
        void levelConfigThrowsOnNullCache() {
            assertThrows(NullPointerException.class, () ->
                    MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L1")
                            .build());
        }
    }

    @Nested
    @DisplayName("Basic Operations Tests")
    class BasicOperationsTests {

        private MultiLevelCache<String, String> cache;

        @BeforeEach
        void setUp() {
            cache = MultiLevelCache.<String, String>builder()
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L1")
                            .cache(l1Cache)
                            .build())
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L2")
                            .cache(l2Cache)
                            .build())
                    .build();
        }

        @Test
        @DisplayName("put stores in all levels with WRITE_ALL")
        void putStoresInAllLevels() {
            cache.put("key", "value");

            assertEquals("value", l1Cache.get("key"));
            assertEquals("value", l2Cache.get("key"));
        }

        @Test
        @DisplayName("get returns from first level")
        void getReturnsFromFirstLevel() {
            l1Cache.put("key", "l1-value");
            l2Cache.put("key", "l2-value");

            assertEquals("l1-value", cache.get("key"));
        }

        @Test
        @DisplayName("get falls through to second level")
        void getFallsThroughToSecondLevel() {
            l2Cache.put("key", "l2-value");

            assertEquals("l2-value", cache.get("key"));
        }

        @Test
        @DisplayName("get returns null when not found")
        void getReturnsNullWhenNotFound() {
            assertNull(cache.get("non-existent"));
        }

        @Test
        @DisplayName("get with loader loads and stores")
        void getWithLoaderLoadsAndStores() {
            String result = cache.get("key", k -> "loaded-" + k);

            assertEquals("loaded-key", result);
            assertEquals("loaded-key", l1Cache.get("key"));
        }

        @Test
        @DisplayName("invalidate removes from all levels with INVALIDATE_ALL")
        void invalidateRemovesFromAllLevels() {
            cache.put("key", "value");
            cache.invalidate("key");

            assertNull(l1Cache.get("key"));
            assertNull(l2Cache.get("key"));
        }

        @Test
        @DisplayName("invalidateAll removes all entries")
        void invalidateAllRemovesAllEntries() {
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            cache.invalidateAll();

            assertNull(l1Cache.get("k1"));
            assertNull(l2Cache.get("k1"));
        }

        @Test
        @DisplayName("containsKey checks all levels")
        void containsKeyChecksAllLevels() {
            l2Cache.put("key", "value");

            assertTrue(cache.containsKey("key"));
            assertFalse(cache.containsKey("non-existent"));
        }
    }

    @Nested
    @DisplayName("Promotion Tests")
    class PromotionTests {

        @Test
        @DisplayName("get promotes on hit when configured")
        void getPromotesOnHitWhenConfigured() {
            MultiLevelCache<String, String> cache = MultiLevelCache.<String, String>builder()
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L1")
                            .cache(l1Cache)
                            .build())
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L2")
                            .cache(l2Cache)
                            .promoteOnHit(true)
                            .build())
                    .build();

            l2Cache.put("key", "value");

            // Access from L2 should promote to L1
            cache.get("key");

            assertEquals("value", l1Cache.get("key"));
        }

        @Test
        @DisplayName("get does not promote when not configured")
        void getDoesNotPromoteWhenNotConfigured() {
            MultiLevelCache<String, String> cache = MultiLevelCache.<String, String>builder()
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L1")
                            .cache(l1Cache)
                            .build())
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L2")
                            .cache(l2Cache)
                            .promoteOnHit(false)
                            .build())
                    .build();

            l2Cache.put("key", "value");
            cache.get("key");

            assertNull(l1Cache.get("key"));
        }
    }

    @Nested
    @DisplayName("Batch Operations Tests")
    class BatchOperationsTests {

        private MultiLevelCache<String, String> cache;

        @BeforeEach
        void setUp() {
            cache = MultiLevelCache.<String, String>builder()
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L1")
                            .cache(l1Cache)
                            .build())
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L2")
                            .cache(l2Cache)
                            .promoteOnHit(true)
                            .build())
                    .build();
        }

        @Test
        @DisplayName("getAll retrieves from multiple levels")
        void getAllRetrievesFromMultipleLevels() {
            l1Cache.put("k1", "v1");
            l2Cache.put("k2", "v2");

            Map<String, String> result = cache.getAll(List.of("k1", "k2", "k3"));

            assertEquals(2, result.size());
            assertEquals("v1", result.get("k1"));
            assertEquals("v2", result.get("k2"));
        }

        @Test
        @DisplayName("getAll with loader loads missing")
        void getAllWithLoaderLoadsMissing() {
            cache.put("k1", "v1");

            Map<String, String> result = cache.getAll(List.of("k1", "k2"),
                    keys -> Map.of("k2", "loaded-k2"));

            assertEquals("v1", result.get("k1"));
            assertEquals("loaded-k2", result.get("k2"));
        }

        @Test
        @DisplayName("putAll stores in all levels")
        void putAllStoresInAllLevels() {
            cache.putAll(Map.of("k1", "v1", "k2", "v2"));

            assertEquals("v1", l1Cache.get("k1"));
            assertEquals("v2", l2Cache.get("k2"));
        }

        @Test
        @DisplayName("invalidateAll with keys removes specific keys")
        void invalidateAllWithKeysRemovesSpecificKeys() {
            cache.putAll(Map.of("k1", "v1", "k2", "v2", "k3", "v3"));

            cache.invalidateAll(List.of("k1", "k2"));

            assertNull(l1Cache.get("k1"));
            assertNull(l1Cache.get("k2"));
            assertEquals("v3", l1Cache.get("k3"));
        }
    }

    @Nested
    @DisplayName("TTL Operations Tests")
    class TtlOperationsTests {

        private MultiLevelCache<String, String> cache;

        @BeforeEach
        void setUp() {
            cache = MultiLevelCache.<String, String>builder()
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L1")
                            .cache(l1Cache)
                            .ttl(Duration.ofMinutes(5))
                            .build())
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L2")
                            .cache(l2Cache)
                            .ttl(Duration.ofMinutes(30))
                            .build())
                    .build();
        }

        @Test
        @DisplayName("putWithTtl respects level TTL")
        void putWithTtlRespectsLevelTtl() {
            cache.putWithTtl("key", "value", Duration.ofMinutes(60));

            // Should not throw
            assertEquals("value", cache.get("key"));
        }

        @Test
        @DisplayName("putAllWithTtl stores with TTL")
        void putAllWithTtlStoresWithTtl() {
            cache.putAllWithTtl(Map.of("k1", "v1", "k2", "v2"), Duration.ofMinutes(10));

            assertEquals("v1", cache.get("k1"));
            assertEquals("v2", cache.get("k2"));
        }

        @Test
        @DisplayName("putIfAbsent works correctly")
        void putIfAbsentWorksCorrectly() {
            assertTrue(cache.putIfAbsent("key", "value"));
            assertFalse(cache.putIfAbsent("key", "new-value"));
            assertEquals("value", cache.get("key"));
        }

        @Test
        @DisplayName("putIfAbsentWithTtl works correctly")
        void putIfAbsentWithTtlWorksCorrectly() {
            assertTrue(cache.putIfAbsentWithTtl("key", "value", Duration.ofMinutes(5)));
            assertFalse(cache.putIfAbsentWithTtl("key", "new-value", Duration.ofMinutes(5)));
            assertEquals("value", cache.get("key"));
        }
    }

    @Nested
    @DisplayName("Level Access Tests")
    class LevelAccessTests {

        @Test
        @DisplayName("levelCount returns number of levels")
        void levelCountReturnsNumberOfLevels() {
            MultiLevelCache<String, String> cache = MultiLevelCache.<String, String>builder()
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L1")
                            .cache(l1Cache)
                            .build())
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L2")
                            .cache(l2Cache)
                            .build())
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L3")
                            .cache(l3Cache)
                            .build())
                    .build();

            assertEquals(3, cache.levelCount());
        }

        @Test
        @DisplayName("getLevel returns specific level cache")
        void getLevelReturnsSpecificLevelCache() {
            MultiLevelCache<String, String> cache = MultiLevelCache.<String, String>builder()
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L1")
                            .cache(l1Cache)
                            .build())
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L2")
                            .cache(l2Cache)
                            .build())
                    .build();

            assertSame(l1Cache, cache.getLevel(0));
            assertSame(l2Cache, cache.getLevel(1));
        }
    }

    @Nested
    @DisplayName("Metrics Tests")
    class MetricsTests {

        @Test
        @DisplayName("getLevelMetrics returns metrics")
        void getLevelMetricsReturnsMetrics() {
            MultiLevelCache<String, String> cache = MultiLevelCache.<String, String>builder()
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L1")
                            .cache(l1Cache)
                            .build())
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L2")
                            .cache(l2Cache)
                            .promoteOnHit(true)
                            .build())
                    .build();

            // Generate some hits/misses
            cache.put("key", "value");
            cache.get("key"); // L1 hit
            l1Cache.invalidate("key");
            cache.get("key"); // L1 miss, L2 hit with promotion

            List<MultiLevelCache.LevelMetrics> metrics = cache.getLevelMetrics();

            assertEquals(2, metrics.size());
        }

        @Test
        @DisplayName("getMultiLevelStats returns aggregate stats")
        void getMultiLevelStatsReturnsAggregateStats() {
            MultiLevelCache<String, String> cache = MultiLevelCache.<String, String>builder()
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L1")
                            .cache(l1Cache)
                            .build())
                    .build();

            cache.put("key", "value");
            cache.get("key");

            MultiLevelCache.MultiLevelStats stats = cache.getMultiLevelStats();

            assertTrue(stats.totalHits() >= 1);
            assertNotNull(stats.levelStats());
        }

        @Test
        @DisplayName("LevelMetrics.Snapshot hitRate calculates correctly")
        void levelMetricsSnapshotHitRateCalculatesCorrectly() {
            MultiLevelCache.LevelMetrics.Snapshot snapshot = new MultiLevelCache.LevelMetrics.Snapshot("L1", 80, 20, 5);

            assertEquals(0.8, snapshot.hitRate(), 0.001);
        }

        @Test
        @DisplayName("LevelMetrics.Snapshot hitRate returns 0 for no requests")
        void levelMetricsSnapshotHitRateReturnsZeroForNoRequests() {
            MultiLevelCache.LevelMetrics.Snapshot snapshot = new MultiLevelCache.LevelMetrics.Snapshot("L1", 0, 0, 0);

            assertEquals(0.0, snapshot.hitRate());
        }

        @Test
        @DisplayName("MultiLevelStats overallHitRate calculates correctly")
        void multiLevelStatsOverallHitRateCalculatesCorrectly() {
            MultiLevelCache.MultiLevelStats stats = new MultiLevelCache.MultiLevelStats(80, 20, 5, Map.of());

            assertEquals(0.8, stats.overallHitRate(), 0.001);
        }

        @Test
        @DisplayName("MultiLevelStats overallHitRate returns 0 for no requests")
        void multiLevelStatsOverallHitRateReturnsZeroForNoRequests() {
            MultiLevelCache.MultiLevelStats stats = new MultiLevelCache.MultiLevelStats(0, 0, 0, Map.of());

            assertEquals(0.0, stats.overallHitRate());
        }
    }

    @Nested
    @DisplayName("Cache Interface Methods Tests")
    class CacheInterfaceMethodsTests {

        private MultiLevelCache<String, String> cache;

        @BeforeEach
        void setUp() {
            cache = MultiLevelCache.<String, String>builder()
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L1")
                            .cache(l1Cache)
                            .build())
                    .build();
        }

        @Test
        @DisplayName("size returns first level size")
        void sizeReturnsFirstLevelSize() {
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            assertEquals(2, cache.size());
        }

        @Test
        @DisplayName("estimatedSize returns sum of all levels")
        void estimatedSizeReturnsSumOfAllLevels() {
            MultiLevelCache<String, String> multiCache = MultiLevelCache.<String, String>builder()
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L1")
                            .cache(l1Cache)
                            .build())
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L2")
                            .cache(l2Cache)
                            .build())
                    .build();

            l1Cache.put("k1", "v1");
            l2Cache.put("k2", "v2");

            // Note: estimatedSize sums all levels
            assertTrue(multiCache.estimatedSize() >= 2);
        }

        @Test
        @DisplayName("keys returns all keys from all levels")
        void keysReturnsAllKeysFromAllLevels() {
            MultiLevelCache<String, String> multiCache = MultiLevelCache.<String, String>builder()
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L1")
                            .cache(l1Cache)
                            .build())
                    .level(MultiLevelCache.LevelConfig.<String, String>builder()
                            .name("L2")
                            .cache(l2Cache)
                            .build())
                    .build();

            l1Cache.put("k1", "v1");
            l2Cache.put("k2", "v2");

            Set<String> keys = multiCache.keys();

            assertTrue(keys.contains("k1"));
            assertTrue(keys.contains("k2"));
        }

        @Test
        @DisplayName("values returns from first level")
        void valuesReturnsFromFirstLevel() {
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            Collection<String> values = cache.values();

            assertEquals(2, values.size());
        }

        @Test
        @DisplayName("entries returns from first level")
        void entriesReturnsFromFirstLevel() {
            cache.put("k1", "v1");

            Set<Map.Entry<String, String>> entries = cache.entries();

            assertEquals(1, entries.size());
        }

        @Test
        @DisplayName("asMap returns from first level")
        void asMapReturnsFromFirstLevel() {
            cache.put("k1", "v1");

            assertNotNull(cache.asMap());
            assertEquals("v1", cache.asMap().get("k1"));
        }

        @Test
        @DisplayName("stats returns from first level")
        void statsReturnsFromFirstLevel() {
            assertNotNull(cache.stats());
        }

        @Test
        @DisplayName("cleanUp cleans all levels")
        void cleanUpCleansAllLevels() {
            cache.put("k1", "v1");

            assertDoesNotThrow(() -> cache.cleanUp());
        }
    }

    @Nested
    @DisplayName("WritePolicy Enum Tests")
    class WritePolicyEnumTests {

        @Test
        @DisplayName("WritePolicy values exist")
        void writePolicyValuesExist() {
            assertEquals(3, MultiLevelCache.WritePolicy.values().length);
            assertNotNull(MultiLevelCache.WritePolicy.WRITE_ALL);
            assertNotNull(MultiLevelCache.WritePolicy.WRITE_FIRST);
            assertNotNull(MultiLevelCache.WritePolicy.WRITE_THROUGH);
        }
    }

    @Nested
    @DisplayName("InvalidationPolicy Enum Tests")
    class InvalidationPolicyEnumTests {

        @Test
        @DisplayName("InvalidationPolicy values exist")
        void invalidationPolicyValuesExist() {
            assertEquals(3, MultiLevelCache.InvalidationPolicy.values().length);
            assertNotNull(MultiLevelCache.InvalidationPolicy.INVALIDATE_ALL);
            assertNotNull(MultiLevelCache.InvalidationPolicy.INVALIDATE_FIRST);
            assertNotNull(MultiLevelCache.InvalidationPolicy.CASCADE_DOWN);
        }
    }
}
