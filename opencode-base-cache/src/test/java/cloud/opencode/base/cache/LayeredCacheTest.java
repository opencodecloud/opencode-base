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

package cloud.opencode.base.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for LayeredCache
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("LayeredCache Tests")
class LayeredCacheTest {

    private Cache<String, String> l1Cache;
    private Cache<String, String> l2Cache;
    private LayeredCache<String, String> layeredCache;

    @BeforeEach
    void setUp() {
        l1Cache = OpenCache.<String, String>builder()
                .maximumSize(10)
                .build();
        l2Cache = OpenCache.<String, String>builder()
                .maximumSize(100)
                .build();
        layeredCache = LayeredCache.of(l1Cache, l2Cache);
    }

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodsTests {

        @Test
        @DisplayName("of() creates layered cache")
        void ofCreatesLayeredCache() {
            LayeredCache<String, String> cache = LayeredCache.of(l1Cache, l2Cache);
            assertNotNull(cache);
        }

        @Test
        @DisplayName("builder() creates builder")
        void builderCreatesBuilder() {
            LayeredCache.Builder<String, String> builder = LayeredCache.builder(l1Cache, l2Cache);
            assertNotNull(builder);
        }

        @Test
        @DisplayName("null L1 throws exception")
        void nullL1Throws() {
            assertThrows(NullPointerException.class,
                    () -> LayeredCache.of(null, l2Cache));
        }

        @Test
        @DisplayName("null L2 throws exception")
        void nullL2Throws() {
            assertThrows(NullPointerException.class,
                    () -> LayeredCache.of(l1Cache, null));
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("writeStrategy sets strategy")
        void writeStrategySetsStrategy() {
            LayeredCache<String, String> cache = LayeredCache.builder(l1Cache, l2Cache)
                    .writeStrategy(LayeredCache.WriteStrategy.WRITE_BACK)
                    .build();
            assertEquals(LayeredCache.WriteStrategy.WRITE_BACK, cache.writeStrategy());
        }

        @Test
        @DisplayName("promoteOnL2Hit sets promotion")
        void promoteOnL2HitSetsPromotion() {
            LayeredCache<String, String> cache = LayeredCache.builder(l1Cache, l2Cache)
                    .promoteOnL2Hit(false)
                    .build();
            assertFalse(cache.isPromoteOnL2Hit());
        }

        @Test
        @DisplayName("name sets cache name")
        void nameSetsName() {
            LayeredCache<String, String> cache = LayeredCache.builder(l1Cache, l2Cache)
                    .name("my-layered-cache")
                    .build();
            assertEquals("my-layered-cache", cache.name());
        }
    }

    @Nested
    @DisplayName("Get Operations")
    class GetOperationsTests {

        @Test
        @DisplayName("get from L1 returns immediately")
        void getFromL1Returns() {
            l1Cache.put("key", "l1-value");
            assertEquals("l1-value", layeredCache.get("key"));
        }

        @Test
        @DisplayName("get from L2 returns and promotes")
        void getFromL2ReturnsAndPromotes() {
            l2Cache.put("key", "l2-value");
            assertEquals("l2-value", layeredCache.get("key"));
            // Should be promoted to L1
            assertEquals("l2-value", l1Cache.get("key"));
        }

        @Test
        @DisplayName("get from L2 without promotion")
        void getFromL2WithoutPromotion() {
            LayeredCache<String, String> cache = LayeredCache.builder(l1Cache, l2Cache)
                    .promoteOnL2Hit(false)
                    .build();
            l2Cache.put("key", "l2-value");
            assertEquals("l2-value", cache.get("key"));
            assertNull(l1Cache.get("key")); // Not promoted
        }

        @Test
        @DisplayName("get with loader loads and stores")
        void getWithLoaderLoads() {
            String value = layeredCache.get("key", k -> "loaded-" + k);
            assertEquals("loaded-key", value);
            assertTrue(l1Cache.containsKey("key"));
            assertTrue(l2Cache.containsKey("key"));
        }

        @Test
        @DisplayName("getAll from both layers")
        void getAllFromBothLayers() {
            l1Cache.put("a", "l1-a");
            l2Cache.put("b", "l2-b");

            Map<String, String> result = layeredCache.getAll(List.of("a", "b", "c"));
            assertEquals("l1-a", result.get("a"));
            assertEquals("l2-b", result.get("b"));
            assertFalse(result.containsKey("c"));
        }

        @Test
        @DisplayName("getAll with loader")
        void getAllWithLoader() {
            l1Cache.put("a", "existing");
            Map<String, String> result = layeredCache.getAll(List.of("a", "b"),
                    keys -> {
                        Map<String, String> loaded = new java.util.HashMap<>();
                        for (String key : keys) {
                            loaded.put(key, "loaded-" + key);
                        }
                        return loaded;
                    });

            assertEquals("existing", result.get("a"));
            assertEquals("loaded-b", result.get("b"));
        }
    }

    @Nested
    @DisplayName("Put Operations")
    class PutOperationsTests {

        @Test
        @DisplayName("put with WRITE_THROUGH writes to both")
        void putWriteThroughWritesBoth() {
            layeredCache.put("key", "value");
            assertEquals("value", l1Cache.get("key"));
            assertEquals("value", l2Cache.get("key"));
        }

        @Test
        @DisplayName("put with WRITE_BACK writes to L1 only")
        void putWriteBackWritesL1Only() {
            LayeredCache<String, String> cache = LayeredCache.builder(l1Cache, l2Cache)
                    .writeStrategy(LayeredCache.WriteStrategy.WRITE_BACK)
                    .build();
            cache.put("key", "value");
            assertEquals("value", l1Cache.get("key"));
            assertNull(l2Cache.get("key"));
        }

        @Test
        @DisplayName("put with L1_ONLY writes to L1 only")
        void putL1OnlyWritesL1Only() {
            LayeredCache<String, String> cache = LayeredCache.builder(l1Cache, l2Cache)
                    .writeStrategy(LayeredCache.WriteStrategy.L1_ONLY)
                    .build();
            cache.put("key", "value");
            assertEquals("value", l1Cache.get("key"));
            assertNull(l2Cache.get("key"));
        }

        @Test
        @DisplayName("putAll writes to appropriate layers")
        void putAllWritesToLayers() {
            layeredCache.putAll(Map.of("a", "1", "b", "2"));
            assertEquals("1", l1Cache.get("a"));
            assertEquals("1", l2Cache.get("a"));
            assertEquals("2", l1Cache.get("b"));
            assertEquals("2", l2Cache.get("b"));
        }

        @Test
        @DisplayName("putIfAbsent stores if absent")
        void putIfAbsentStores() {
            assertTrue(layeredCache.putIfAbsent("key", "value"));
            assertFalse(layeredCache.putIfAbsent("key", "other"));
            assertEquals("value", layeredCache.get("key"));
        }

        @Test
        @DisplayName("putWithTtl stores with TTL")
        void putWithTtlStores() {
            layeredCache.putWithTtl("key", "value", Duration.ofMinutes(5));
            assertEquals("value", layeredCache.get("key"));
        }

        @Test
        @DisplayName("putAllWithTtl stores all with TTL")
        void putAllWithTtlStores() {
            layeredCache.putAllWithTtl(Map.of("a", "1", "b", "2"), Duration.ofMinutes(5));
            assertEquals("1", layeredCache.get("a"));
            assertEquals("2", layeredCache.get("b"));
        }

        @Test
        @DisplayName("putIfAbsentWithTtl stores if absent with TTL")
        void putIfAbsentWithTtlStores() {
            assertTrue(layeredCache.putIfAbsentWithTtl("key", "value", Duration.ofMinutes(5)));
            assertFalse(layeredCache.putIfAbsentWithTtl("key", "other", Duration.ofMinutes(5)));
        }
    }

    @Nested
    @DisplayName("Invalidation Operations")
    class InvalidationOperationsTests {

        @Test
        @DisplayName("invalidate removes from both layers")
        void invalidateRemovesFromBoth() {
            layeredCache.put("key", "value");
            layeredCache.invalidate("key");
            assertNull(l1Cache.get("key"));
            assertNull(l2Cache.get("key"));
        }

        @Test
        @DisplayName("invalidate with L1_ONLY removes from L1 only")
        void invalidateL1OnlyRemovesL1Only() {
            LayeredCache<String, String> cache = LayeredCache.builder(l1Cache, l2Cache)
                    .writeStrategy(LayeredCache.WriteStrategy.L1_ONLY)
                    .build();
            l1Cache.put("key", "value");
            l2Cache.put("key", "value");
            cache.invalidate("key");
            assertNull(l1Cache.get("key"));
            assertEquals("value", l2Cache.get("key")); // L2 unchanged
        }

        @Test
        @DisplayName("invalidateAll removes from both layers")
        void invalidateAllRemovesFromBoth() {
            layeredCache.put("a", "1");
            layeredCache.put("b", "2");
            layeredCache.invalidateAll(List.of("a", "b"));
            assertFalse(layeredCache.containsKey("a"));
            assertFalse(layeredCache.containsKey("b"));
        }

        @Test
        @DisplayName("invalidateAll clears both layers")
        void invalidateAllClearsBoth() {
            layeredCache.put("a", "1");
            layeredCache.put("b", "2");
            layeredCache.invalidateAll();
            assertEquals(0, l1Cache.estimatedSize());
            assertEquals(0, l2Cache.estimatedSize());
        }
    }

    @Nested
    @DisplayName("Query Operations")
    class QueryOperationsTests {

        @Test
        @DisplayName("containsKey checks both layers")
        void containsKeyChecksBoth() {
            l2Cache.put("key", "value");
            assertTrue(layeredCache.containsKey("key"));

            l1Cache.put("other", "value");
            assertTrue(layeredCache.containsKey("other"));

            assertFalse(layeredCache.containsKey("nonexistent"));
        }

        @Test
        @DisplayName("size returns combined size")
        void sizeReturnsCombined() {
            l1Cache.put("a", "1");
            l2Cache.put("b", "2");
            assertEquals(2, layeredCache.size());
        }

        @Test
        @DisplayName("estimatedSize returns combined size")
        void estimatedSizeReturnsCombined() {
            l1Cache.put("a", "1");
            l2Cache.put("b", "2");
            assertEquals(2, layeredCache.estimatedSize());
        }

        @Test
        @DisplayName("keys returns combined keys")
        void keysReturnsCombined() {
            l1Cache.put("a", "1");
            l2Cache.put("b", "2");
            Set<String> keys = layeredCache.keys();
            assertTrue(keys.contains("a"));
            assertTrue(keys.contains("b"));
        }

        @Test
        @DisplayName("values returns combined values")
        void valuesReturnsCombined() {
            l1Cache.put("a", "1");
            l2Cache.put("b", "2");
            assertTrue(layeredCache.values().contains("1"));
            assertTrue(layeredCache.values().contains("2"));
        }

        @Test
        @DisplayName("entries returns combined entries")
        void entriesReturnsCombined() {
            l1Cache.put("a", "1");
            l2Cache.put("b", "2");
            assertEquals(2, layeredCache.entries().size());
        }

        @Test
        @DisplayName("asMap returns combined map")
        void asMapReturnsCombined() {
            l1Cache.put("a", "1");
            l2Cache.put("b", "2");
            Map<String, String> map = layeredCache.asMap();
            assertEquals("1", map.get("a"));
            assertEquals("2", map.get("b"));
        }
    }

    @Nested
    @DisplayName("Layer Access")
    class LayerAccessTests {

        @Test
        @DisplayName("l1() returns L1 cache")
        void l1ReturnsL1Cache() {
            assertSame(l1Cache, layeredCache.l1());
        }

        @Test
        @DisplayName("l2() returns L2 cache")
        void l2ReturnsL2Cache() {
            assertSame(l2Cache, layeredCache.l2());
        }

        @Test
        @DisplayName("writeStrategy() returns strategy")
        void writeStrategyReturnsStrategy() {
            assertEquals(LayeredCache.WriteStrategy.WRITE_THROUGH, layeredCache.writeStrategy());
        }

        @Test
        @DisplayName("isPromoteOnL2Hit() returns flag")
        void isPromoteOnL2HitReturnsFlag() {
            assertTrue(layeredCache.isPromoteOnL2Hit());
        }
    }

    @Nested
    @DisplayName("Flush and Warmup")
    class FlushAndWarmupTests {

        @Test
        @DisplayName("flush copies L1 to L2 for WRITE_BACK")
        void flushCopiesL1ToL2() {
            LayeredCache<String, String> cache = LayeredCache.builder(l1Cache, l2Cache)
                    .writeStrategy(LayeredCache.WriteStrategy.WRITE_BACK)
                    .build();
            l1Cache.put("key", "value");
            cache.flush();
            assertEquals("value", l2Cache.get("key"));
        }

        @Test
        @DisplayName("flush does nothing for WRITE_THROUGH")
        void flushDoesNothingForWriteThrough() {
            l1Cache.put("key", "value");
            assertDoesNotThrow(() -> layeredCache.flush());
        }

        @Test
        @DisplayName("warmUp copies from L2 to L1")
        void warmUpCopiesFromL2ToL1() {
            l2Cache.put("a", "1");
            l2Cache.put("b", "2");
            layeredCache.warmUp(List.of("a", "b"));
            assertEquals("1", l1Cache.get("a"));
            assertEquals("2", l1Cache.get("b"));
        }
    }

    @Nested
    @DisplayName("Statistics")
    class StatisticsTests {

        @Test
        @DisplayName("stats returns combined stats")
        void statsReturnsCombined() {
            layeredCache.get("key", k -> "value"); // Trigger load
            CacheStats stats = layeredCache.stats();
            assertNotNull(stats);
        }

        @Test
        @DisplayName("layeredStats returns detailed stats")
        void layeredStatsReturnsDetailed() {
            layeredCache.put("key", "value");
            LayeredCache.LayeredCacheStats stats = layeredCache.layeredStats();
            assertNotNull(stats.l1Stats());
            assertNotNull(stats.l2Stats());
        }

        @Test
        @DisplayName("layeredStats hit rates")
        void layeredStatsHitRates() {
            layeredCache.put("key", "value");
            layeredCache.get("key"); // Hit
            LayeredCache.LayeredCacheStats stats = layeredCache.layeredStats();
            assertTrue(stats.l1HitRate() >= 0);
            assertTrue(stats.l2HitRate() >= 0);
            assertTrue(stats.overallHitRate() >= 0);
        }
    }

    @Nested
    @DisplayName("Async Operations")
    class AsyncOperationsTests {

        @Test
        @DisplayName("async() returns async view")
        void asyncReturnsView() {
            AsyncCache<String, String> async = layeredCache.async();
            assertNotNull(async);
        }

        @Test
        @DisplayName("async operations work correctly")
        void asyncOperationsWork() throws Exception {
            AsyncCache<String, String> async = layeredCache.async();
            async.putAsync("key", "value").get();
            assertEquals("value", async.getAsync("key").get());
        }
    }

    @Nested
    @DisplayName("Other Operations")
    class OtherOperationsTests {

        @Test
        @DisplayName("cleanUp cleans both layers")
        void cleanUpCleansBoth() {
            layeredCache.put("key", "value");
            assertDoesNotThrow(() -> layeredCache.cleanUp());
        }

        @Test
        @DisplayName("name returns cache name")
        void nameReturnsName() {
            assertNotNull(layeredCache.name());
            assertTrue(layeredCache.name().contains("layered"));
        }
    }
}
