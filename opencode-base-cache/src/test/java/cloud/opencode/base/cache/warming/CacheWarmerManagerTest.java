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

package cloud.opencode.base.cache.warming;

import cloud.opencode.base.cache.Cache;
import cloud.opencode.base.cache.OpenCache;
import cloud.opencode.base.cache.spi.CacheWarmer;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for CacheWarmerManager
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("CacheWarmerManager Tests")
class CacheWarmerManagerTest {

    private CacheWarmerManager manager;
    private Cache<String, String> testCache;

    @BeforeEach
    void setUp() {
        manager = CacheWarmerManager.getInstance();
        testCache = OpenCache.<String, String>builder()
                .maximumSize(1000)
                .build("warmer-manager-test-" + System.nanoTime())
                ;
        // Clean up any previously registered warmers
        manager.getRegisteredCaches().forEach(manager::unregister);
    }

    @AfterEach
    void tearDown() {
        manager.getRegisteredCaches().forEach(manager::unregister);
        manager.setListener(null);
    }

    @Nested
    @DisplayName("Singleton Tests")
    class SingletonTests {

        @Test
        @DisplayName("getInstance returns same instance")
        void getInstanceReturnsSameInstance() {
            CacheWarmerManager instance1 = CacheWarmerManager.getInstance();
            CacheWarmerManager instance2 = CacheWarmerManager.getInstance();

            assertSame(instance1, instance2);
        }
    }

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("register adds warmer")
        void registerAddsWarmer() {
            CacheWarmer<String, String> warmer = () -> Map.of("k1", "v1");

            manager.register("test-cache", testCache, warmer);

            assertTrue(manager.getRegisteredCaches().contains("test-cache"));
        }

        @Test
        @DisplayName("register with priority")
        void registerWithPriority() {
            CacheWarmer<String, String> warmer = () -> Map.of("k1", "v1");

            manager.register("test-cache", testCache, warmer, 10);

            assertTrue(manager.getRegisteredCaches().contains("test-cache"));
        }

        @Test
        @DisplayName("register returns manager for chaining")
        void registerReturnsManagerForChaining() {
            CacheWarmer<String, String> warmer = () -> Map.of("k1", "v1");

            CacheWarmerManager result = manager.register("test-cache", testCache, warmer);

            assertSame(manager, result);
        }

        @Test
        @DisplayName("unregister removes warmer")
        void unregisterRemovesWarmer() {
            CacheWarmer<String, String> warmer = () -> Map.of("k1", "v1");
            manager.register("test-cache", testCache, warmer);

            manager.unregister("test-cache");

            assertFalse(manager.getRegisteredCaches().contains("test-cache"));
        }

        @Test
        @DisplayName("unregister returns manager for chaining")
        void unregisterReturnsManagerForChaining() {
            CacheWarmerManager result = manager.unregister("non-existent");

            assertSame(manager, result);
        }
    }

    @Nested
    @DisplayName("Warming Tests")
    class WarmingTests {

        @Test
        @DisplayName("warmAll warms all caches")
        void warmAllWarmsAllCaches() {
            Cache<String, String> cache1 = OpenCache.<String, String>builder()
                    .maximumSize(100)
                    .build("warm-test-1");
            Cache<String, String> cache2 = OpenCache.<String, String>builder()
                    .maximumSize(100)
                    .build("warm-test-2");

            manager.register("cache1", cache1, () -> Map.of("k1", "v1", "k2", "v2"));
            manager.register("cache2", cache2, () -> Map.of("k3", "v3"));

            CacheWarmerManager.WarmingResult result = manager.warmAll();

            assertEquals(2, result.successCount());
            assertEquals(3, result.totalEntries());
            assertEquals("v1", cache1.get("k1"));
            assertEquals("v3", cache2.get("k3"));
        }

        @Test
        @DisplayName("warmAll respects priority order")
        void warmAllRespectsPriorityOrder() {
            AtomicInteger order = new AtomicInteger(0);
            AtomicInteger cache1Order = new AtomicInteger();
            AtomicInteger cache2Order = new AtomicInteger();

            Cache<String, String> cache1 = OpenCache.<String, String>builder()
                    .maximumSize(100)
                    .build("priority-test-1");
            Cache<String, String> cache2 = OpenCache.<String, String>builder()
                    .maximumSize(100)
                    .build("priority-test-2");

            manager.register("low-priority", cache1, () -> {
                cache1Order.set(order.incrementAndGet());
                return Map.of("k1", "v1");
            }, 10);
            manager.register("high-priority", cache2, () -> {
                cache2Order.set(order.incrementAndGet());
                return Map.of("k2", "v2");
            }, 1);

            manager.warmAll();

            assertTrue(cache2Order.get() < cache1Order.get());
        }

        @Test
        @DisplayName("warm warms specific cache")
        void warmWarmsSpecificCache() {
            manager.register("specific-cache", testCache, () -> Map.of("k1", "v1"));

            CacheWarmerManager.CacheWarmingResult result = manager.warm("specific-cache");

            assertTrue(result.isSuccess());
            assertEquals(1, result.entriesLoaded());
            assertEquals("v1", testCache.get("k1"));
        }

        @Test
        @DisplayName("warm throws for unregistered cache")
        void warmThrowsForUnregisteredCache() {
            assertThrows(IllegalArgumentException.class, () -> manager.warm("non-existent"));
        }

        @Test
        @DisplayName("warmAllAsync warms asynchronously")
        void warmAllAsyncWarmsAsynchronously() {
            manager.register("async-cache", testCache, () -> Map.of("k1", "v1"));

            CompletableFuture<CacheWarmerManager.WarmingResult> future = manager.warmAllAsync();
            CacheWarmerManager.WarmingResult result = future.join();

            assertEquals(1, result.successCount());
            assertEquals("v1", testCache.get("k1"));
        }

        @Test
        @DisplayName("warmAsync warms specific cache asynchronously")
        void warmAsyncWarmsSpecificCacheAsynchronously() {
            manager.register("async-specific", testCache, () -> Map.of("k1", "v1"));

            CompletableFuture<CacheWarmerManager.CacheWarmingResult> future = manager.warmAsync("async-specific");
            CacheWarmerManager.CacheWarmingResult result = future.join();

            assertTrue(result.isSuccess());
            assertEquals("v1", testCache.get("k1"));
        }

        @Test
        @DisplayName("warmAllParallel warms in parallel")
        void warmAllParallelWarmsInParallel() {
            Cache<String, String> cache1 = OpenCache.<String, String>builder()
                    .maximumSize(100)
                    .build("parallel-test-1");
            Cache<String, String> cache2 = OpenCache.<String, String>builder()
                    .maximumSize(100)
                    .build("parallel-test-2");

            manager.register("parallel1", cache1, () -> Map.of("k1", "v1"));
            manager.register("parallel2", cache2, () -> Map.of("k2", "v2"));

            CacheWarmerManager.WarmingResult result = manager.warmAllParallel(2);

            assertEquals(2, result.successCount());
            assertEquals("v1", cache1.get("k1"));
            assertEquals("v2", cache2.get("k2"));
        }

        @Test
        @DisplayName("warming skips disabled warmers")
        void warmingSkipsDisabledWarmers() {
            CacheWarmer<String, String> disabledWarmer = new CacheWarmer<>() {
                @Override
                public Map<String, String> warmUp() {
                    return Map.of("k1", "v1");
                }

                @Override
                public boolean isEnabled() {
                    return false;
                }
            };

            manager.register("disabled-cache", testCache, disabledWarmer);

            CacheWarmerManager.WarmingResult result = manager.warmAll();

            assertEquals(0, result.totalEntries());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("warming handles errors gracefully")
        void warmingHandlesErrorsGracefully() {
            CacheWarmer<String, String> failingWarmer = () -> {
                throw new RuntimeException("Test error");
            };

            manager.register("failing-cache", testCache, failingWarmer);

            CacheWarmerManager.WarmingResult result = manager.warmAll();

            assertEquals(1, result.failureCount());
            assertFalse(result.failures().isEmpty());
        }

        @Test
        @DisplayName("CacheWarmingResult captures error")
        void cacheWarmingResultCapturesError() {
            RuntimeException error = new RuntimeException("Test error");
            CacheWarmer<String, String> failingWarmer = () -> {
                throw error;
            };

            manager.register("error-cache", testCache, failingWarmer);

            CacheWarmerManager.CacheWarmingResult result = manager.warm("error-cache");

            assertFalse(result.isSuccess());
            assertNotNull(result.error());
        }
    }

    @Nested
    @DisplayName("Listener Tests")
    class ListenerTests {

        @Test
        @DisplayName("listener receives warming events")
        void listenerReceivesWarmingEvents() {
            AtomicBoolean startedCalled = new AtomicBoolean(false);
            AtomicBoolean completedCalled = new AtomicBoolean(false);
            AtomicInteger entriesLoaded = new AtomicInteger(0);

            manager.setListener(new CacheWarmerManager.WarmingListener() {
                @Override
                public void onWarmingStarted(String cacheName) {
                    startedCalled.set(true);
                }

                @Override
                public void onWarmingCompleted(String cacheName, int entries, Duration duration) {
                    completedCalled.set(true);
                    entriesLoaded.set(entries);
                }
            });

            manager.register("listener-cache", testCache, () -> Map.of("k1", "v1", "k2", "v2"));
            manager.warm("listener-cache");

            assertTrue(startedCalled.get());
            assertTrue(completedCalled.get());
            assertEquals(2, entriesLoaded.get());
        }

        @Test
        @DisplayName("listener receives failure events")
        void listenerReceivesFailureEvents() {
            AtomicBoolean failedCalled = new AtomicBoolean(false);

            manager.setListener(new CacheWarmerManager.WarmingListener() {
                @Override
                public void onWarmingFailed(String cacheName, Throwable error) {
                    failedCalled.set(true);
                }
            });

            manager.register("failing-listener-cache", testCache, () -> {
                throw new RuntimeException("Test error");
            });

            manager.warm("failing-listener-cache");

            assertTrue(failedCalled.get());
        }

        @Test
        @DisplayName("setListener returns manager for chaining")
        void setListenerReturnsManagerForChaining() {
            CacheWarmerManager result = manager.setListener(new CacheWarmerManager.WarmingListener() {});

            assertSame(manager, result);
        }
    }

    @Nested
    @DisplayName("Metrics Tests")
    class MetricsTests {

        @Test
        @DisplayName("getMetrics returns metrics")
        void getMetricsReturnsMetrics() {
            manager.register("metrics-cache", testCache, () -> Map.of("k1", "v1"));
            manager.warm("metrics-cache");

            CacheWarmerManager.WarmingMetrics metrics = manager.getMetrics();

            assertTrue(metrics.registeredCaches() >= 1);
            assertTrue(metrics.totalEntriesWarmed() >= 1);
        }

        @Test
        @DisplayName("WarmingMetrics totalWarmingTime converts correctly")
        void warmingMetricsTotalWarmingTimeConvertsCorrectly() {
            manager.register("time-cache", testCache, () -> Map.of("k1", "v1"));
            manager.warm("time-cache");

            CacheWarmerManager.WarmingMetrics metrics = manager.getMetrics();

            assertNotNull(metrics.totalWarmingTime());
            assertTrue(metrics.totalWarmingTimeNanos() >= 0);
        }

        @Test
        @DisplayName("getRegisteredCaches returns unmodifiable set")
        void getRegisteredCachesReturnsUnmodifiableSet() {
            manager.register("unmodifiable-cache", testCache, () -> Map.of("k1", "v1"));

            Set<String> caches = manager.getRegisteredCaches();

            assertThrows(UnsupportedOperationException.class, () -> caches.add("new-cache"));
        }
    }

    @Nested
    @DisplayName("WarmingResult Tests")
    class WarmingResultTests {

        @Test
        @DisplayName("WarmingResult calculates totals correctly")
        void warmingResultCalculatesTotalsCorrectly() {
            Cache<String, String> cache1 = OpenCache.<String, String>builder()
                    .maximumSize(100)
                    .build("result-test-1");
            Cache<String, String> cache2 = OpenCache.<String, String>builder()
                    .maximumSize(100)
                    .build("result-test-2");

            manager.register("result1", cache1, () -> Map.of("k1", "v1", "k2", "v2"));
            manager.register("result2", cache2, () -> {
                throw new RuntimeException("Test error");
            });

            CacheWarmerManager.WarmingResult result = manager.warmAll();

            assertEquals(2, result.results().size());
            assertEquals(1, result.successCount());
            assertEquals(1, result.failureCount());
            assertEquals(2, result.totalEntries());
            assertNotNull(result.totalDuration());
        }

        @Test
        @DisplayName("WarmingResult failures returns failed results")
        void warmingResultFailuresReturnsFailedResults() {
            manager.register("fail-result", testCache, () -> {
                throw new RuntimeException("Test error");
            });

            CacheWarmerManager.WarmingResult result = manager.warmAll();

            assertEquals(1, result.failures().size());
            assertFalse(result.failures().get(0).isSuccess());
        }
    }

    @Nested
    @DisplayName("CacheWarmingResult Tests")
    class CacheWarmingResultTests {

        @Test
        @DisplayName("CacheWarmingResult record accessors work")
        void cacheWarmingResultRecordAccessorsWork() {
            CacheWarmerManager.CacheWarmingResult result =
                    new CacheWarmerManager.CacheWarmingResult("test", 10, Duration.ofMillis(100), null);

            assertEquals("test", result.cacheName());
            assertEquals(10, result.entriesLoaded());
            assertEquals(Duration.ofMillis(100), result.duration());
            assertNull(result.error());
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("CacheWarmingResult isSuccess returns false on error")
        void cacheWarmingResultIsSuccessReturnsFalseOnError() {
            CacheWarmerManager.CacheWarmingResult result =
                    new CacheWarmerManager.CacheWarmingResult("test", 0, Duration.ZERO, new RuntimeException());

            assertFalse(result.isSuccess());
        }
    }

    @Nested
    @DisplayName("WarmingListener Default Methods Tests")
    class WarmingListenerDefaultMethodsTests {

        @Test
        @DisplayName("default methods do nothing")
        void defaultMethodsDoNothing() {
            CacheWarmerManager.WarmingListener listener = new CacheWarmerManager.WarmingListener() {};

            // Should not throw
            assertDoesNotThrow(() -> {
                listener.onWarmingStarted("test");
                listener.onWarmingCompleted("test", 10, Duration.ZERO);
                listener.onWarmingFailed("test", new RuntimeException());
            });
        }
    }
}
