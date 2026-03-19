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
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for TimeoutCache
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("TimeoutCache Tests")
class TimeoutCacheTest {

    private Cache<String, String> baseCache;
    private TimeoutCache<String, String> timeoutCache;

    @BeforeEach
    void setUp() {
        baseCache = OpenCache.<String, String>builder()
                .maximumSize(100)
                .build();
        timeoutCache = TimeoutCache.wrap(baseCache)
                .defaultTimeout(Duration.ofSeconds(5))
                .build();
    }

    @AfterEach
    void tearDown() {
        timeoutCache.shutdown();
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("wrap() creates builder")
        void wrapCreatesBuilder() {
            TimeoutCache.Builder<String, String> builder = TimeoutCache.wrap(baseCache);
            assertNotNull(builder);
        }

        @Test
        @DisplayName("wrap() with null throws exception")
        void wrapWithNullThrows() {
            assertThrows(NullPointerException.class, () -> TimeoutCache.wrap(null));
        }

        @Test
        @DisplayName("defaultTimeout sets timeout")
        void defaultTimeoutSetsTimeout() {
            TimeoutCache<String, String> cache = TimeoutCache.wrap(baseCache)
                    .defaultTimeout(Duration.ofSeconds(10))
                    .build();
            assertEquals(Duration.ofSeconds(10), cache.getDefaultTimeout());
            cache.shutdown();
        }

        @Test
        @DisplayName("executor sets custom executor")
        void executorSetsCustomExecutor() {
            ExecutorService executor = Executors.newFixedThreadPool(2);
            try {
                TimeoutCache<String, String> cache = TimeoutCache.wrap(baseCache)
                        .executor(executor)
                        .build();
                assertNotNull(cache);
                cache.shutdown();
            } finally {
                executor.shutdown();
            }
        }

        @Test
        @DisplayName("null timeout throws exception")
        void nullTimeoutThrows() {
            assertThrows(NullPointerException.class, () ->
                    TimeoutCache.wrap(baseCache).defaultTimeout(null));
        }
    }

    @Nested
    @DisplayName("Basic Operations")
    class BasicOperationsTests {

        @Test
        @DisplayName("get returns cached value")
        void getReturnsCachedValue() {
            baseCache.put("key", "value");
            assertEquals("value", timeoutCache.get("key"));
        }

        @Test
        @DisplayName("get returns null for missing")
        void getReturnsNullForMissing() {
            assertNull(timeoutCache.get("nonexistent"));
        }

        @Test
        @DisplayName("get with loader loads value")
        void getWithLoaderLoads() {
            String value = timeoutCache.get("key", k -> "loaded-" + k);
            assertEquals("loaded-key", value);
        }

        @Test
        @DisplayName("put stores value")
        void putStoresValue() {
            timeoutCache.put("key", "value");
            assertEquals("value", baseCache.get("key"));
        }

        @Test
        @DisplayName("putAll stores values")
        void putAllStoresValues() {
            timeoutCache.putAll(Map.of("a", "1", "b", "2"));
            assertEquals("1", baseCache.get("a"));
            assertEquals("2", baseCache.get("b"));
        }

        @Test
        @DisplayName("putIfAbsent stores if absent")
        void putIfAbsentStores() {
            assertTrue(timeoutCache.putIfAbsent("key", "value"));
            assertFalse(timeoutCache.putIfAbsent("key", "other"));
        }
    }

    @Nested
    @DisplayName("Timeout Operations")
    class TimeoutOperationsTests {

        @Test
        @DisplayName("getWithTimeout succeeds within timeout")
        void getWithTimeoutSucceeds() {
            String value = timeoutCache.getWithTimeout("key",
                    k -> "loaded",
                    Duration.ofSeconds(5));
            assertEquals("loaded", value);
        }

        @Test
        @DisplayName("getWithTimeout throws on timeout")
        void getWithTimeoutThrowsOnTimeout() {
            assertThrows(TimeoutCache.CacheTimeoutException.class, () ->
                    timeoutCache.getWithTimeout("key",
                            k -> {
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                                return "value";
                            },
                            Duration.ofMillis(100)));
        }

        @Test
        @DisplayName("getAllWithTimeout succeeds within timeout")
        void getAllWithTimeoutSucceeds() {
            Map<String, String> result = timeoutCache.getAllWithTimeout(
                    List.of("a", "b"),
                    keys -> {
                        Map<String, String> map = new java.util.HashMap<>();
                        for (String key : keys) {
                            map.put(key, "loaded-" + key);
                        }
                        return map;
                    },
                    Duration.ofSeconds(5));
            assertEquals("loaded-a", result.get("a"));
            assertEquals("loaded-b", result.get("b"));
        }

        @Test
        @DisplayName("getAllWithTimeout throws on timeout")
        void getAllWithTimeoutThrowsOnTimeout() {
            assertThrows(TimeoutCache.CacheTimeoutException.class, () ->
                    timeoutCache.getAllWithTimeout(List.of("a"),
                            keys -> {
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                                return Map.of();
                            },
                            Duration.ofMillis(100)));
        }
    }

    @Nested
    @DisplayName("TTL Operations")
    class TtlOperationsTests {

        @Test
        @DisplayName("putWithTtl stores with TTL")
        void putWithTtlStores() {
            timeoutCache.putWithTtl("key", "value", Duration.ofMinutes(5));
            assertEquals("value", timeoutCache.get("key"));
        }

        @Test
        @DisplayName("putAllWithTtl stores all with TTL")
        void putAllWithTtlStores() {
            timeoutCache.putAllWithTtl(Map.of("a", "1"), Duration.ofMinutes(5));
            assertEquals("1", timeoutCache.get("a"));
        }

        @Test
        @DisplayName("putIfAbsentWithTtl stores if absent")
        void putIfAbsentWithTtlStores() {
            assertTrue(timeoutCache.putIfAbsentWithTtl("key", "value", Duration.ofMinutes(5)));
            assertFalse(timeoutCache.putIfAbsentWithTtl("key", "other", Duration.ofMinutes(5)));
        }
    }

    @Nested
    @DisplayName("Query Operations")
    class QueryOperationsTests {

        @Test
        @DisplayName("containsKey checks delegate")
        void containsKeyChecksDelegate() {
            baseCache.put("key", "value");
            assertTrue(timeoutCache.containsKey("key"));
            assertFalse(timeoutCache.containsKey("nonexistent"));
        }

        @Test
        @DisplayName("size returns delegate size")
        void sizeReturnsDelegateSize() {
            baseCache.put("a", "1");
            baseCache.put("b", "2");
            assertEquals(2, timeoutCache.size());
        }

        @Test
        @DisplayName("estimatedSize returns delegate size")
        void estimatedSizeReturnsDelegateSize() {
            baseCache.put("key", "value");
            assertEquals(1, timeoutCache.estimatedSize());
        }

        @Test
        @DisplayName("keys returns delegate keys")
        void keysReturnsDelegateKeys() {
            baseCache.put("a", "1");
            baseCache.put("b", "2");
            Set<String> keys = timeoutCache.keys();
            assertTrue(keys.contains("a"));
            assertTrue(keys.contains("b"));
        }

        @Test
        @DisplayName("values returns delegate values")
        void valuesReturnsDelegateValues() {
            baseCache.put("a", "1");
            assertTrue(timeoutCache.values().contains("1"));
        }

        @Test
        @DisplayName("entries returns delegate entries")
        void entriesReturnsDelegateEntries() {
            baseCache.put("a", "1");
            assertEquals(1, timeoutCache.entries().size());
        }

        @Test
        @DisplayName("asMap returns delegate map")
        void asMapReturnsDelegateMap() {
            baseCache.put("key", "value");
            assertEquals("value", timeoutCache.asMap().get("key"));
        }
    }

    @Nested
    @DisplayName("Compute Operations")
    class ComputeOperationsTests {

        @Test
        @DisplayName("getOrDefault returns default for missing")
        void getOrDefaultReturnsDefault() {
            assertEquals("default", timeoutCache.getOrDefault("key", "default"));
        }

        @Test
        @DisplayName("computeIfPresent updates existing")
        void computeIfPresentUpdates() {
            timeoutCache.put("key", "value");
            String result = timeoutCache.computeIfPresent("key", (k, v) -> v.toUpperCase());
            assertEquals("VALUE", result);
        }

        @Test
        @DisplayName("compute creates or updates")
        void computeCreatesOrUpdates() {
            String result = timeoutCache.compute("key", (k, v) -> "computed");
            assertEquals("computed", result);
        }

        @Test
        @DisplayName("getAndRemove returns and removes")
        void getAndRemoveReturnsAndRemoves() {
            timeoutCache.put("key", "value");
            assertEquals("value", timeoutCache.getAndRemove("key"));
            assertFalse(timeoutCache.containsKey("key"));
        }

        @Test
        @DisplayName("replace updates existing")
        void replaceUpdatesExisting() {
            timeoutCache.put("key", "old");
            assertEquals("old", timeoutCache.replace("key", "new"));
        }

        @Test
        @DisplayName("replace with expected value")
        void replaceWithExpectedValue() {
            timeoutCache.put("key", "old");
            assertTrue(timeoutCache.replace("key", "old", "new"));
            assertFalse(timeoutCache.replace("key", "wrong", "newer"));
        }
    }

    @Nested
    @DisplayName("Invalidation Operations")
    class InvalidationOperationsTests {

        @Test
        @DisplayName("invalidate removes from delegate")
        void invalidateRemovesFromDelegate() {
            timeoutCache.put("key", "value");
            timeoutCache.invalidate("key");
            assertFalse(baseCache.containsKey("key"));
        }

        @Test
        @DisplayName("invalidateAll removes keys from delegate")
        void invalidateAllRemovesKeys() {
            timeoutCache.putAll(Map.of("a", "1", "b", "2"));
            timeoutCache.invalidateAll(List.of("a", "b"));
            assertEquals(0, baseCache.size());
        }

        @Test
        @DisplayName("invalidateAll clears delegate")
        void invalidateAllClearsDelegate() {
            timeoutCache.put("key", "value");
            timeoutCache.invalidateAll();
            assertEquals(0, baseCache.size());
        }
    }

    @Nested
    @DisplayName("Async Operations")
    class AsyncOperationsTests {

        @Test
        @DisplayName("async returns async view")
        void asyncReturnsAsyncView() {
            AsyncCache<String, String> async = timeoutCache.async();
            assertNotNull(async);
        }

        @Test
        @DisplayName("async getAsync works")
        void asyncGetAsyncWorks() throws Exception {
            timeoutCache.put("key", "value");
            AsyncCache<String, String> async = timeoutCache.async();
            assertEquals("value", async.getAsync("key").get());
        }

        @Test
        @DisplayName("async putAsync works")
        void asyncPutAsyncWorks() throws Exception {
            AsyncCache<String, String> async = timeoutCache.async();
            async.putAsync("key", "value").get();
            assertEquals("value", timeoutCache.get("key"));
        }

        @Test
        @DisplayName("async sync returns sync view")
        void asyncSyncReturnsSyncView() {
            AsyncCache<String, String> async = timeoutCache.async();
            assertNotNull(async.sync());
        }
    }

    @Nested
    @DisplayName("Other Operations")
    class OtherOperationsTests {

        @Test
        @DisplayName("stats returns delegate stats")
        void statsReturnsDelegateStats() {
            assertNotNull(timeoutCache.stats());
        }

        @Test
        @DisplayName("metrics returns delegate metrics")
        void metricsReturnsDelegateMetrics() {
            timeoutCache.metrics(); // May be null
        }

        @Test
        @DisplayName("cleanUp cleans delegate")
        void cleanUpCleansDelegate() {
            timeoutCache.put("key", "value");
            assertDoesNotThrow(() -> timeoutCache.cleanUp());
        }

        @Test
        @DisplayName("name returns delegate name")
        void nameReturnsDelegateName() {
            assertNotNull(timeoutCache.name());
        }

        @Test
        @DisplayName("getDelegate returns delegate")
        void getDelegateReturnsDelegate() {
            assertSame(baseCache, timeoutCache.getDelegate());
        }

        @Test
        @DisplayName("getDefaultTimeout returns timeout")
        void getDefaultTimeoutReturnsTimeout() {
            assertEquals(Duration.ofSeconds(5), timeoutCache.getDefaultTimeout());
        }

        @Test
        @DisplayName("shutdown shuts down executor")
        void shutdownShutsDownExecutor() {
            assertDoesNotThrow(() -> timeoutCache.shutdown());
        }

        @Test
        @DisplayName("getAll returns delegate results")
        void getAllReturnsDelegateResults() {
            baseCache.put("a", "1");
            baseCache.put("b", "2");
            Map<String, String> result = timeoutCache.getAll(List.of("a", "b"));
            assertEquals("1", result.get("a"));
            assertEquals("2", result.get("b"));
        }
    }

    @Nested
    @DisplayName("Exception Tests")
    class ExceptionTests {

        @Test
        @DisplayName("CacheTimeoutException contains message")
        void cacheTimeoutExceptionContainsMessage() {
            TimeoutCache.CacheTimeoutException ex = new TimeoutCache.CacheTimeoutException(
                    "Test timeout", new RuntimeException());
            assertTrue(ex.getMessage().contains("Test timeout"));
            assertNotNull(ex.getCause());
        }
    }
}
