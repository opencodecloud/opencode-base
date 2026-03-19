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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for ProtectedCache
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("ProtectedCache Tests")
class ProtectedCacheTest {

    private Cache<String, String> baseCache;
    private ProtectedCache<String, String> protectedCache;

    @BeforeEach
    void setUp() {
        baseCache = OpenCache.<String, String>builder()
                .maximumSize(100)
                .build();
        protectedCache = ProtectedCache.wrap(baseCache)
                .bloomFilter(1000, 0.01)
                .singleFlight(true)
                .negativeCache(Duration.ofSeconds(5))
                .build();
    }

    @AfterEach
    void tearDown() {
        protectedCache.shutdown();
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("wrap() creates builder")
        void wrapCreatesBuilder() {
            ProtectedCache.Builder<String, String> builder = ProtectedCache.wrap(baseCache);
            assertNotNull(builder);
        }

        @Test
        @DisplayName("wrap() with null throws exception")
        void wrapWithNullThrows() {
            assertThrows(NullPointerException.class, () -> ProtectedCache.wrap(null));
        }

        @Test
        @DisplayName("build with defaults")
        void buildWithDefaults() {
            ProtectedCache<String, String> cache = ProtectedCache.wrap(baseCache).build();
            assertNotNull(cache);
            cache.shutdown();
        }

        @Test
        @DisplayName("build with bloom filter disabled")
        void buildWithBloomFilterDisabled() {
            ProtectedCache<String, String> cache = ProtectedCache.wrap(baseCache)
                    .bloomFilter(false)
                    .build();
            assertNotNull(cache);
            cache.shutdown();
        }

        @Test
        @DisplayName("build with single flight disabled")
        void buildWithSingleFlightDisabled() {
            ProtectedCache<String, String> cache = ProtectedCache.wrap(baseCache)
                    .singleFlight(false)
                    .build();
            assertNotNull(cache);
            cache.shutdown();
        }

        @Test
        @DisplayName("build with custom bloom filter config")
        void buildWithCustomBloomFilter() {
            ProtectedCache<String, String> cache = ProtectedCache.wrap(baseCache)
                    .bloomFilter(100000, 0.001)
                    .build();
            assertNotNull(cache);
            cache.shutdown();
        }

        @Test
        @DisplayName("build without negative cache")
        void buildWithoutNegativeCache() {
            ProtectedCache<String, String> cache = ProtectedCache.wrap(baseCache)
                    .negativeCache(null)
                    .build();
            assertNotNull(cache);
            cache.shutdown();
        }
    }

    @Nested
    @DisplayName("Get Operations")
    class GetOperationsTests {

        @Test
        @DisplayName("get returns cached value")
        void getReturnsCachedValue() {
            baseCache.put("key", "value");
            assertEquals("value", protectedCache.get("key"));
        }

        @Test
        @DisplayName("get returns null for missing key")
        void getReturnsNullForMissing() {
            assertNull(protectedCache.get("nonexistent"));
        }

        @Test
        @DisplayName("get with loader loads value")
        void getWithLoaderLoads() {
            String result = protectedCache.get("key", k -> "loaded-" + k);
            assertEquals("loaded-key", result);
        }

        @Test
        @DisplayName("get with loader returns null and tracks negative")
        void getWithLoaderTracksNegative() {
            String result = protectedCache.get("missing", k -> null);
            assertNull(result);
            // Key should be negatively cached
            assertTrue(protectedCache.isKeyNegativelyCached("missing"));
        }

        @Test
        @DisplayName("negatively cached key returns null without loading")
        void negativelyCachedKeyReturnsNull() {
            AtomicInteger loadCount = new AtomicInteger(0);
            // First load returns null
            protectedCache.get("missing", k -> {
                loadCount.incrementAndGet();
                return null;
            });
            // Second call should not invoke loader
            protectedCache.get("missing", k -> {
                loadCount.incrementAndGet();
                return "value";
            });
            assertEquals(1, loadCount.get());
        }

        @Test
        @DisplayName("getAll returns cached values")
        void getAllReturnsCachedValues() {
            baseCache.put("a", "1");
            baseCache.put("b", "2");
            Map<String, String> result = protectedCache.getAll(List.of("a", "b"));
            assertEquals("1", result.get("a"));
            assertEquals("2", result.get("b"));
        }

        @Test
        @DisplayName("getAll with loader")
        void getAllWithLoader() {
            Map<String, String> result = protectedCache.getAll(List.of("a", "b"),
                    keys -> Map.of("a", "loaded-a", "b", "loaded-b"));
            assertEquals("loaded-a", result.get("a"));
            assertEquals("loaded-b", result.get("b"));
        }
    }

    @Nested
    @DisplayName("Put Operations")
    class PutOperationsTests {

        @Test
        @DisplayName("put stores value")
        void putStoresValue() {
            protectedCache.put("key", "value");
            assertEquals("value", baseCache.get("key"));
        }

        @Test
        @DisplayName("put clears negative cache")
        void putClearsNegativeCache() {
            // Create negative cache entry
            protectedCache.get("key", k -> null);
            assertTrue(protectedCache.isKeyNegativelyCached("key"));
            // Put should clear it
            protectedCache.put("key", "value");
            assertFalse(protectedCache.isKeyNegativelyCached("key"));
        }

        @Test
        @DisplayName("putAll stores values")
        void putAllStoresValues() {
            protectedCache.putAll(Map.of("a", "1", "b", "2"));
            assertEquals("1", baseCache.get("a"));
            assertEquals("2", baseCache.get("b"));
        }

        @Test
        @DisplayName("putAll clears negative cache entries")
        void putAllClearsNegativeCache() {
            protectedCache.get("a", k -> null);
            protectedCache.get("b", k -> null);
            protectedCache.putAll(Map.of("a", "1", "b", "2"));
            assertFalse(protectedCache.isKeyNegativelyCached("a"));
            assertFalse(protectedCache.isKeyNegativelyCached("b"));
        }

        @Test
        @DisplayName("putIfAbsent stores if absent")
        void putIfAbsentStoresIfAbsent() {
            assertTrue(protectedCache.putIfAbsent("key", "value"));
            assertFalse(protectedCache.putIfAbsent("key", "other"));
        }

        @Test
        @DisplayName("putWithTtl stores with TTL")
        void putWithTtlStores() {
            protectedCache.putWithTtl("key", "value", Duration.ofMinutes(5));
            assertEquals("value", protectedCache.get("key"));
        }

        @Test
        @DisplayName("putAllWithTtl stores with TTL")
        void putAllWithTtlStores() {
            protectedCache.putAllWithTtl(Map.of("a", "1"), Duration.ofMinutes(5));
            assertEquals("1", protectedCache.get("a"));
        }

        @Test
        @DisplayName("putIfAbsentWithTtl stores if absent with TTL")
        void putIfAbsentWithTtlStores() {
            assertTrue(protectedCache.putIfAbsentWithTtl("key", "value", Duration.ofMinutes(5)));
            assertFalse(protectedCache.putIfAbsentWithTtl("key", "other", Duration.ofMinutes(5)));
        }
    }

    @Nested
    @DisplayName("Compute Operations")
    class ComputeOperationsTests {

        @Test
        @DisplayName("computeIfPresent updates existing")
        void computeIfPresentUpdates() {
            protectedCache.put("key", "value");
            String result = protectedCache.computeIfPresent("key", (k, v) -> v.toUpperCase());
            assertEquals("VALUE", result);
        }

        @Test
        @DisplayName("compute creates or updates")
        void computeCreatesOrUpdates() {
            String result = protectedCache.compute("key", (k, v) -> "computed");
            assertEquals("computed", result);
        }

        @Test
        @DisplayName("getAndRemove returns and removes")
        void getAndRemoveReturnsAndRemoves() {
            protectedCache.put("key", "value");
            assertEquals("value", protectedCache.getAndRemove("key"));
            assertFalse(protectedCache.containsKey("key"));
        }

        @Test
        @DisplayName("replace updates existing")
        void replaceUpdatesExisting() {
            protectedCache.put("key", "old");
            assertEquals("old", protectedCache.replace("key", "new"));
            assertEquals("new", protectedCache.get("key"));
        }

        @Test
        @DisplayName("replace with old value")
        void replaceWithOldValue() {
            protectedCache.put("key", "old");
            assertTrue(protectedCache.replace("key", "old", "new"));
            assertFalse(protectedCache.replace("key", "wrong", "newer"));
        }
    }

    @Nested
    @DisplayName("Invalidation Operations")
    class InvalidationOperationsTests {

        @Test
        @DisplayName("invalidate removes from delegate")
        void invalidateRemovesFromDelegate() {
            protectedCache.put("key", "value");
            protectedCache.invalidate("key");
            assertFalse(baseCache.containsKey("key"));
        }

        @Test
        @DisplayName("invalidateAll removes keys")
        void invalidateAllRemovesKeys() {
            protectedCache.putAll(Map.of("a", "1", "b", "2"));
            protectedCache.invalidateAll(List.of("a", "b"));
            assertEquals(0, baseCache.size());
        }

        @Test
        @DisplayName("invalidateAll clears all")
        void invalidateAllClearsAll() {
            protectedCache.put("key", "value");
            // Add negative cache entry
            protectedCache.get("missing", k -> null);
            protectedCache.invalidateAll();
            assertEquals(0, baseCache.size());
            assertFalse(protectedCache.isKeyNegativelyCached("missing"));
        }
    }

    @Nested
    @DisplayName("Query Operations")
    class QueryOperationsTests {

        @Test
        @DisplayName("containsKey checks delegate")
        void containsKeyChecksDelegate() {
            baseCache.put("key", "value");
            assertTrue(protectedCache.containsKey("key"));
            assertFalse(protectedCache.containsKey("nonexistent"));
        }

        @Test
        @DisplayName("size returns delegate size")
        void sizeReturnsDelegateSize() {
            baseCache.put("a", "1");
            baseCache.put("b", "2");
            assertEquals(2, protectedCache.size());
        }

        @Test
        @DisplayName("estimatedSize returns delegate size")
        void estimatedSizeReturnsDelegateSize() {
            baseCache.put("key", "value");
            assertEquals(1, protectedCache.estimatedSize());
        }

        @Test
        @DisplayName("keys returns delegate keys")
        void keysReturnsDelegateKeys() {
            baseCache.put("key", "value");
            assertTrue(protectedCache.keys().contains("key"));
        }

        @Test
        @DisplayName("values returns delegate values")
        void valuesReturnsDelegateValues() {
            baseCache.put("key", "value");
            assertTrue(protectedCache.values().contains("value"));
        }

        @Test
        @DisplayName("entries returns delegate entries")
        void entriesReturnsDelegateEntries() {
            baseCache.put("key", "value");
            assertEquals(1, protectedCache.entries().size());
        }

        @Test
        @DisplayName("asMap returns delegate map")
        void asMapReturnsDelegateMap() {
            baseCache.put("key", "value");
            assertEquals("value", protectedCache.asMap().get("key"));
        }
    }

    @Nested
    @DisplayName("Protection Stats")
    class ProtectionStatsTests {

        @Test
        @DisplayName("getProtectionStats returns stats")
        void getProtectionStatsReturnsStats() {
            ProtectedCache.ProtectionStats stats = protectedCache.getProtectionStats();
            assertNotNull(stats);
            assertEquals(0, stats.negativeCacheEntries());
        }

        @Test
        @DisplayName("stats track negative cache entries")
        void statsTrackNegativeCacheEntries() {
            protectedCache.get("missing1", k -> null);
            protectedCache.get("missing2", k -> null);
            ProtectedCache.ProtectionStats stats = protectedCache.getProtectionStats();
            assertEquals(2, stats.negativeCacheEntries());
        }

        @Test
        @DisplayName("stats track bloom filter entries")
        void statsTrackBloomFilterEntries() {
            protectedCache.get("missing1", k -> null);
            ProtectedCache.ProtectionStats stats = protectedCache.getProtectionStats();
            assertTrue(stats.bloomFilterEntries() > 0);
        }

        @Test
        @DisplayName("isKeyNegativelyCached checks negative cache")
        void isKeyNegativelyCachedChecks() {
            assertFalse(protectedCache.isKeyNegativelyCached("key"));
            protectedCache.get("key", k -> null);
            assertTrue(protectedCache.isKeyNegativelyCached("key"));
        }

        @Test
        @DisplayName("clearNegativeCache clears entries")
        void clearNegativeCacheClearsEntries() {
            protectedCache.get("key", k -> null);
            assertTrue(protectedCache.isKeyNegativelyCached("key"));
            protectedCache.clearNegativeCache();
            assertFalse(protectedCache.isKeyNegativelyCached("key"));
        }

        @Test
        @DisplayName("mightContainInBloomFilter checks bloom filter")
        void mightContainInBloomFilterChecks() {
            assertFalse(protectedCache.mightContainInBloomFilter("key"));
            protectedCache.get("key", k -> null);
            assertTrue(protectedCache.mightContainInBloomFilter("key"));
        }

        @Test
        @DisplayName("resetProtectionStats clears negative cache")
        void resetProtectionStatsClearsNegativeCache() {
            protectedCache.get("key", k -> null);
            protectedCache.resetProtectionStats();
            assertFalse(protectedCache.isKeyNegativelyCached("key"));
        }
    }

    @Nested
    @DisplayName("Other Operations")
    class OtherOperationsTests {

        @Test
        @DisplayName("stats returns delegate stats")
        void statsReturnsDelegateStats() {
            assertNotNull(protectedCache.stats());
        }

        @Test
        @DisplayName("metrics returns delegate metrics")
        void metricsReturnsDelegateMetrics() {
            protectedCache.metrics(); // May be null
        }

        @Test
        @DisplayName("cleanUp cleans delegate and negative cache")
        void cleanUpCleans() {
            protectedCache.put("key", "value");
            assertDoesNotThrow(() -> protectedCache.cleanUp());
        }

        @Test
        @DisplayName("async returns async view")
        void asyncReturnsAsyncView() {
            assertNotNull(protectedCache.async());
        }

        @Test
        @DisplayName("name returns delegate name")
        void nameReturnsDelegateName() {
            assertNotNull(protectedCache.name());
        }

        @Test
        @DisplayName("shutdown stops cleanup scheduler")
        void shutdownStopsScheduler() {
            assertDoesNotThrow(() -> protectedCache.shutdown());
        }
    }

    @Nested
    @DisplayName("Single Flight Tests")
    class SingleFlightTests {

        @Test
        @DisplayName("concurrent loads are deduplicated")
        void concurrentLoadsAreDeduplicated() throws Exception {
            AtomicInteger loadCount = new AtomicInteger(0);

            // Create protected cache with single flight
            ProtectedCache<String, String> cache = ProtectedCache.wrap(baseCache)
                    .singleFlight(true)
                    .bloomFilter(false)
                    .build();

            // Multiple threads trying to load same key
            Thread[] threads = new Thread[10];
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread(() -> {
                    cache.get("key", k -> {
                        loadCount.incrementAndGet();
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return "loaded";
                    });
                });
            }

            for (Thread t : threads) t.start();
            for (Thread t : threads) t.join();

            // Should only load once due to single flight
            assertEquals(1, loadCount.get());
            cache.shutdown();
        }
    }

    @Nested
    @DisplayName("Negative Cache Expiry Tests")
    class NegativeCacheExpiryTests {

        @Test
        @DisplayName("negative cache entries expire")
        void negativeCacheEntriesExpire() throws Exception {
            // Create cache with short negative cache duration
            ProtectedCache<String, String> cache = ProtectedCache.wrap(baseCache)
                    .negativeCache(Duration.ofMillis(100))
                    .bloomFilter(false)
                    .singleFlight(false)
                    .build();

            // Create negative cache entry
            cache.get("key", k -> null);
            assertTrue(cache.isKeyNegativelyCached("key"));

            // Wait for expiry
            Thread.sleep(150);

            // Should no longer be negatively cached
            assertFalse(cache.isKeyNegativelyCached("key"));
            cache.shutdown();
        }
    }
}
