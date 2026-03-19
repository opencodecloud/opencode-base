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

import cloud.opencode.base.cache.spi.RefreshAheadPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for RefreshAheadCache
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("RefreshAheadCache Tests")
class RefreshAheadCacheTest {

    private Cache<String, String> baseCache;
    private AtomicInteger loadCount;

    @BeforeEach
    void setUp() {
        baseCache = OpenCache.<String, String>builder()
                .maximumSize(100)
                .build();
        loadCount = new AtomicInteger(0);
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("wrap() creates builder")
        void wrapCreatesBuilder() {
            RefreshAheadCache.Builder<String, String> builder = RefreshAheadCache.wrap(baseCache);
            assertNotNull(builder);
        }

        @Test
        @DisplayName("wrap() with null throws exception")
        void wrapWithNullThrows() {
            assertThrows(NullPointerException.class, () -> RefreshAheadCache.wrap(null));
        }

        @Test
        @DisplayName("build without loader throws exception")
        void buildWithoutLoaderThrows() {
            assertThrows(NullPointerException.class, () ->
                    RefreshAheadCache.wrap(baseCache).build());
        }

        @Test
        @DisplayName("build with defaults")
        void buildWithDefaults() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();
            assertNotNull(cache);
        }

        @Test
        @DisplayName("build with custom policy")
        void buildWithCustomPolicy() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .refreshPolicy(RefreshAheadPolicy.percentageOfTtl(0.9))
                    .build();
            assertNotNull(cache);
        }

        @Test
        @DisplayName("build with custom executor")
        void buildWithCustomExecutor() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .executor(Executors.newFixedThreadPool(2))
                    .build();
            assertNotNull(cache);
        }

        @Test
        @DisplayName("build with custom TTL")
        void buildWithCustomTtl() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .ttl(Duration.ofMinutes(10))
                    .build();
            assertNotNull(cache);
        }
    }

    @Nested
    @DisplayName("Get Operations")
    class GetOperationsTests {

        @Test
        @DisplayName("get returns cached value")
        void getReturnsCachedValue() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            baseCache.put("key", "value");
            assertEquals("value", cache.get("key"));
        }

        @Test
        @DisplayName("get returns null for missing key")
        void getReturnsNullForMissing() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            assertNull(cache.get("nonexistent"));
        }

        @Test
        @DisplayName("get with loader loads and stores value")
        void getWithLoaderLoadsAndStores() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> {
                        loadCount.incrementAndGet();
                        return "loaded-" + k;
                    })
                    .build();

            String result = cache.get("key", k -> "custom-" + k);
            assertEquals("custom-key", result);
            assertTrue(cache.containsKey("key"));
        }

        @Test
        @DisplayName("getOrDefault returns default for missing")
        void getOrDefaultReturnsDefault() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            assertEquals("default", cache.getOrDefault("missing", "default"));
        }

        @Test
        @DisplayName("getOrDefault returns cached value")
        void getOrDefaultReturnsCached() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            cache.put("key", "value");
            assertEquals("value", cache.getOrDefault("key", "default"));
        }

        @Test
        @DisplayName("getAll returns cached values")
        void getAllReturnsCachedValues() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            cache.put("a", "1");
            cache.put("b", "2");
            Map<String, String> result = cache.getAll(List.of("a", "b"));
            assertEquals("1", result.get("a"));
            assertEquals("2", result.get("b"));
        }

        @Test
        @DisplayName("getAll with loader")
        void getAllWithLoader() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            Map<String, String> result = cache.getAll(List.of("a", "b"),
                    keys -> Map.of("a", "loaded-a", "b", "loaded-b"));
            assertEquals("loaded-a", result.get("a"));
            assertEquals("loaded-b", result.get("b"));
        }
    }

    @Nested
    @DisplayName("Put Operations")
    class PutOperationsTests {

        @Test
        @DisplayName("put stores value and tracks timestamp")
        void putStoresValueAndTracksTimestamp() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            cache.put("key", "value");
            assertEquals("value", cache.get("key"));
            assertEquals(1, cache.getRefreshStats().trackedEntries());
        }

        @Test
        @DisplayName("putAll stores values")
        void putAllStoresValues() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            cache.putAll(Map.of("a", "1", "b", "2"));
            assertEquals("1", cache.get("a"));
            assertEquals("2", cache.get("b"));
        }

        @Test
        @DisplayName("putIfAbsent stores if absent")
        void putIfAbsentStoresIfAbsent() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            assertTrue(cache.putIfAbsent("key", "value"));
            assertFalse(cache.putIfAbsent("key", "other"));
        }

        @Test
        @DisplayName("putWithTtl stores with TTL")
        void putWithTtlStores() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            cache.putWithTtl("key", "value", Duration.ofMinutes(5));
            assertEquals("value", cache.get("key"));
        }

        @Test
        @DisplayName("putAllWithTtl stores with TTL")
        void putAllWithTtlStores() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            cache.putAllWithTtl(Map.of("a", "1"), Duration.ofMinutes(5));
            assertEquals("1", cache.get("a"));
        }

        @Test
        @DisplayName("putIfAbsentWithTtl stores if absent with TTL")
        void putIfAbsentWithTtlStores() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            assertTrue(cache.putIfAbsentWithTtl("key", "value", Duration.ofMinutes(5)));
            assertFalse(cache.putIfAbsentWithTtl("key", "other", Duration.ofMinutes(5)));
        }
    }

    @Nested
    @DisplayName("Compute Operations")
    class ComputeOperationsTests {

        @Test
        @DisplayName("computeIfPresent updates existing")
        void computeIfPresentUpdates() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            cache.put("key", "value");
            String result = cache.computeIfPresent("key", (k, v) -> v.toUpperCase());
            assertEquals("VALUE", result);
        }

        @Test
        @DisplayName("compute creates or updates")
        void computeCreatesOrUpdates() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            String result = cache.compute("key", (k, v) -> "computed");
            assertEquals("computed", result);
        }

        @Test
        @DisplayName("compute removes on null result")
        void computeRemovesOnNull() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            cache.put("key", "value");
            cache.compute("key", (k, v) -> null);
            assertFalse(cache.containsKey("key"));
        }

        @Test
        @DisplayName("getAndRemove returns and removes")
        void getAndRemoveReturnsAndRemoves() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            cache.put("key", "value");
            assertEquals("value", cache.getAndRemove("key"));
            assertFalse(cache.containsKey("key"));
        }

        @Test
        @DisplayName("replace updates existing")
        void replaceUpdatesExisting() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            cache.put("key", "old");
            assertEquals("old", cache.replace("key", "new"));
        }

        @Test
        @DisplayName("replace with old value")
        void replaceWithOldValue() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            cache.put("key", "old");
            assertTrue(cache.replace("key", "old", "new"));
            assertFalse(cache.replace("key", "wrong", "newer"));
        }
    }

    @Nested
    @DisplayName("Invalidation Operations")
    class InvalidationOperationsTests {

        @Test
        @DisplayName("invalidate removes from cache and tracking")
        void invalidateRemovesFromCacheAndTracking() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            cache.put("key", "value");
            assertEquals(1, cache.getRefreshStats().trackedEntries());
            cache.invalidate("key");
            assertFalse(cache.containsKey("key"));
            assertEquals(0, cache.getRefreshStats().trackedEntries());
        }

        @Test
        @DisplayName("invalidateAll removes keys")
        void invalidateAllRemovesKeys() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            cache.putAll(Map.of("a", "1", "b", "2"));
            cache.invalidateAll(List.of("a", "b"));
            assertEquals(0, cache.size());
        }

        @Test
        @DisplayName("invalidateAll clears all")
        void invalidateAllClearsAll() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            cache.put("key", "value");
            cache.invalidateAll();
            assertEquals(0, cache.size());
            assertEquals(0, cache.getRefreshStats().trackedEntries());
        }
    }

    @Nested
    @DisplayName("Refresh Operations")
    class RefreshOperationsTests {

        @Test
        @DisplayName("forceRefresh reloads value")
        void forceRefreshReloadsValue() throws Exception {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> {
                        loadCount.incrementAndGet();
                        return "loaded-" + loadCount.get();
                    })
                    .build();

            cache.put("key", "old");
            CompletableFuture<String> future = cache.forceRefresh("key");
            String newValue = future.get();
            assertEquals("loaded-1", newValue);
        }

        @Test
        @DisplayName("getRefreshStats returns stats")
        void getRefreshStatsReturnsStats() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            cache.put("key", "value");
            RefreshAheadCache.RefreshStats stats = cache.getRefreshStats();
            assertNotNull(stats);
            assertEquals(1, stats.trackedEntries());
            assertEquals(0, stats.inFlightRefreshes());
        }

        @Test
        @DisplayName("isRefreshInProgress checks in-flight")
        void isRefreshInProgressChecks() throws Exception {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return "loaded-" + k;
                    })
                    .build();

            cache.put("key", "value");
            CompletableFuture<String> future = cache.forceRefresh("key");
            assertTrue(cache.isRefreshInProgress("key"));
            future.get();
            // After completion, should no longer be in progress
            Thread.sleep(150); // Wait for cleanup
            assertFalse(cache.isRefreshInProgress("key"));
        }

        @Test
        @DisplayName("cancelPendingRefresh cancels refresh")
        void cancelPendingRefreshCancels() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return "loaded-" + k;
                    })
                    .build();

            cache.put("key", "value");
            cache.forceRefresh("key");
            assertTrue(cache.cancelPendingRefresh("key"));
        }

        @Test
        @DisplayName("cancelAllPendingRefreshes cancels all")
        void cancelAllPendingRefreshesCancelsAll() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return "loaded-" + k;
                    })
                    .build();

            cache.put("a", "1");
            cache.put("b", "2");
            cache.forceRefresh("a");
            cache.forceRefresh("b");
            int count = cache.cancelAllPendingRefreshes();
            assertEquals(2, count);
        }

        @Test
        @DisplayName("getInFlightRefreshCount returns count")
        void getInFlightRefreshCountReturnsCount() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return "loaded-" + k;
                    })
                    .build();

            cache.put("key", "value");
            cache.forceRefresh("key");
            assertTrue(cache.getInFlightRefreshCount() > 0);
            cache.cancelAllPendingRefreshes();
        }
    }

    @Nested
    @DisplayName("Query Operations")
    class QueryOperationsTests {

        @Test
        @DisplayName("containsKey checks delegate")
        void containsKeyChecksDelegate() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            cache.put("key", "value");
            assertTrue(cache.containsKey("key"));
            assertFalse(cache.containsKey("nonexistent"));
        }

        @Test
        @DisplayName("size returns delegate size")
        void sizeReturnsDelegateSize() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            cache.put("a", "1");
            cache.put("b", "2");
            assertEquals(2, cache.size());
        }

        @Test
        @DisplayName("keys returns delegate keys")
        void keysReturnsDelegateKeys() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            cache.put("key", "value");
            assertTrue(cache.keys().contains("key"));
        }

        @Test
        @DisplayName("values returns delegate values")
        void valuesReturnsDelegateValues() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            cache.put("key", "value");
            assertTrue(cache.values().contains("value"));
        }

        @Test
        @DisplayName("entries returns delegate entries")
        void entriesReturnsDelegateEntries() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            cache.put("key", "value");
            assertEquals(1, cache.entries().size());
        }

        @Test
        @DisplayName("asMap returns delegate map")
        void asMapReturnsDelegateMap() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            cache.put("key", "value");
            assertEquals("value", cache.asMap().get("key"));
        }
    }

    @Nested
    @DisplayName("Other Operations")
    class OtherOperationsTests {

        @Test
        @DisplayName("stats returns delegate stats")
        void statsReturnsDelegateStats() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            assertNotNull(cache.stats());
        }

        @Test
        @DisplayName("cleanUp cleans delegate and timestamps")
        void cleanUpCleansDelegate() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            cache.put("key", "value");
            assertDoesNotThrow(() -> cache.cleanUp());
        }

        @Test
        @DisplayName("async returns async view")
        void asyncReturnsAsyncView() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            assertNotNull(cache.async());
        }

        @Test
        @DisplayName("name returns delegate name")
        void nameReturnsDelegateName() {
            RefreshAheadCache<String, String> cache = RefreshAheadCache.wrap(baseCache)
                    .loader(k -> "loaded-" + k)
                    .build();

            assertNotNull(cache.name());
        }
    }
}
