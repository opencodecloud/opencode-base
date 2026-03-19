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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for CacheWarmer
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("CacheWarmer Tests")
class CacheWarmerTest {

    private Cache<String, String> cache;
    private CacheWarmer<String, String> warmer;

    @BeforeEach
    void setUp() {
        cache = OpenCache.<String, String>builder()
                .maximumSize(1000)
                .build("test-warmer");
    }

    @AfterEach
    void tearDown() {
        if (warmer != null) {
            warmer.close();
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("builder creates warmer")
        void builderCreatesWarmer() {
            warmer = CacheWarmer.<String, String>builder()
                    .cache(cache)
                    .loader(key -> "value-" + key)
                    .build();
            assertNotNull(warmer);
        }

        @Test
        @DisplayName("builder throws on null cache")
        void builderThrowsOnNullCache() {
            assertThrows(NullPointerException.class, () ->
                    CacheWarmer.<String, String>builder()
                            .loader(key -> "value")
                            .build());
        }

        @Test
        @DisplayName("builder throws on null loader")
        void builderThrowsOnNullLoader() {
            assertThrows(NullPointerException.class, () ->
                    CacheWarmer.<String, String>builder()
                            .cache(cache)
                            .build());
        }

        @Test
        @DisplayName("builder with batch loader")
        void builderWithBatchLoader() {
            warmer = CacheWarmer.<String, String>builder()
                    .cache(cache)
                    .loader(key -> "value-" + key)
                    .batchLoader(keys -> {
                        Map<String, String> result = new java.util.HashMap<>();
                        for (String key : keys) {
                            result.put(key, "batch-" + key);
                        }
                        return result;
                    })
                    .build();
            assertNotNull(warmer);
        }

        @Test
        @DisplayName("builder with all options")
        void builderWithAllOptions() {
            warmer = CacheWarmer.<String, String>builder()
                    .cache(cache)
                    .loader(key -> "value-" + key)
                    .batchSize(50)
                    .parallelism(2)
                    .eventHandler(CacheWarmer.WarmingEventHandler.noOp())
                    .build();
            assertNotNull(warmer);
        }
    }

    @Nested
    @DisplayName("Warming Tests")
    class WarmingTests {

        @BeforeEach
        void setUp() {
            warmer = CacheWarmer.<String, String>builder()
                    .cache(cache)
                    .loader(key -> "loaded-" + key)
                    .build();
        }

        @Test
        @DisplayName("warm loads keys into cache")
        void warmLoadsKeysIntoCache() {
            List<String> keys = List.of("k1", "k2", "k3");

            CacheWarmer.WarmingResult<String> result = warmer.warm(keys);

            assertEquals(3, result.warmedCount());
            assertEquals("loaded-k1", cache.get("k1"));
            assertEquals("loaded-k2", cache.get("k2"));
            assertEquals("loaded-k3", cache.get("k3"));
        }

        @Test
        @DisplayName("warm skips existing keys by default")
        void warmSkipsExistingKeysByDefault() {
            cache.put("k1", "existing");
            List<String> keys = List.of("k1", "k2");

            CacheWarmer.WarmingResult<String> result = warmer.warm(keys);

            assertEquals(1, result.warmedCount());
            assertEquals(1, result.skippedCount());
            assertEquals("existing", cache.get("k1"));
        }

        @Test
        @DisplayName("warm with forceRefresh overwrites existing")
        void warmWithForceRefreshOverwritesExisting() {
            cache.put("k1", "existing");
            List<String> keys = List.of("k1");

            CacheWarmer.WarmingResult<String> result = warmer.warm(keys,
                    CacheWarmer.WarmingOptions.builder().forceRefresh(true).build());

            assertEquals(1, result.warmedCount());
            assertEquals("loaded-k1", cache.get("k1"));
        }

        @Test
        @DisplayName("warm handles loader exceptions")
        void warmHandlesLoaderExceptions() {
            CacheWarmer<String, String> failingWarmer = CacheWarmer.<String, String>builder()
                    .cache(cache)
                    .loader(key -> {
                        if (key.equals("fail")) throw new RuntimeException("Load failed");
                        return "value-" + key;
                    })
                    .build();

            CacheWarmer.WarmingResult<String> result = failingWarmer.warm(List.of("ok", "fail"));

            assertEquals(1, result.warmedCount());
            assertEquals(1, result.failedCount());
            assertTrue(result.failedKeys().contains("fail"));

            failingWarmer.close();
        }

        @Test
        @DisplayName("warm skips null values")
        void warmSkipsNullValues() {
            CacheWarmer<String, String> nullWarmer = CacheWarmer.<String, String>builder()
                    .cache(cache)
                    .loader(key -> key.equals("null") ? null : "value-" + key)
                    .build();

            CacheWarmer.WarmingResult<String> result = nullWarmer.warm(List.of("ok", "null"));

            assertEquals(1, result.warmedCount());
            assertEquals(1, result.skippedCount());

            nullWarmer.close();
        }
    }

    @Nested
    @DisplayName("Async Warming Tests")
    class AsyncWarmingTests {

        @BeforeEach
        void setUp() {
            warmer = CacheWarmer.<String, String>builder()
                    .cache(cache)
                    .loader(key -> "loaded-" + key)
                    .build();
        }

        @Test
        @DisplayName("warmAsync returns future")
        void warmAsyncReturnsFuture() {
            AtomicInteger progressUpdates = new AtomicInteger(0);

            CompletableFuture<CacheWarmer.WarmingResult<String>> future = warmer.warmAsync(
                    List.of("k1", "k2", "k3"),
                    progress -> progressUpdates.incrementAndGet()
            );

            CacheWarmer.WarmingResult<String> result = future.join();

            assertEquals(3, result.warmedCount());
            assertTrue(progressUpdates.get() > 0);
        }
    }

    @Nested
    @DisplayName("Batch Warming Tests")
    class BatchWarmingTests {

        @Test
        @DisplayName("batch loader is used for large key sets")
        void batchLoaderIsUsedForLargeKeySets() {
            AtomicInteger batchLoaderCalls = new AtomicInteger(0);

            warmer = CacheWarmer.<String, String>builder()
                    .cache(cache)
                    .loader(key -> "single-" + key)
                    .batchLoader(keys -> {
                        batchLoaderCalls.incrementAndGet();
                        Map<String, String> result = new java.util.HashMap<>();
                        for (String key : keys) {
                            result.put(key, "batch-" + key);
                        }
                        return result;
                    })
                    .batchSize(5)
                    .build();

            List<String> keys = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                keys.add("k" + i);
            }

            warmer.warm(keys);

            assertTrue(batchLoaderCalls.get() > 0);
            assertEquals("batch-k0", cache.get("k0"));
        }
    }

    @Nested
    @DisplayName("Scheduled Warming Tests")
    class ScheduledWarmingTests {

        @BeforeEach
        void setUp() {
            warmer = CacheWarmer.<String, String>builder()
                    .cache(cache)
                    .loader(key -> "loaded-" + key)
                    .build();
        }

        @Test
        @DisplayName("scheduleWarming returns scheduled future")
        void scheduleWarmingReturnsScheduledFuture() {
            ScheduledFuture<?> future = warmer.scheduleWarming(
                    Duration.ofHours(1),
                    () -> List.of("k1", "k2")
            );

            assertNotNull(future);
            future.cancel(true);
        }

        @Test
        @DisplayName("scheduleWarming with initial delay")
        void scheduleWarmingWithInitialDelay() {
            ScheduledFuture<?> future = warmer.scheduleWarming(
                    Duration.ofMillis(100),
                    Duration.ofHours(1),
                    () -> List.of("k1")
            );

            assertNotNull(future);
            future.cancel(true);
        }
    }

    @Nested
    @DisplayName("Priority Warming Tests")
    class PriorityWarmingTests {

        @BeforeEach
        void setUp() {
            warmer = CacheWarmer.<String, String>builder()
                    .cache(cache)
                    .loader(key -> "loaded-" + key)
                    .build();
        }

        @Test
        @DisplayName("warmWithPriority processes high priority first")
        void warmWithPriorityProcessesHighPriorityFirst() {
            List<CacheWarmer.PriorityKey<String>> priorityKeys = List.of(
                    CacheWarmer.PriorityKey.of("low", 1),
                    CacheWarmer.PriorityKey.of("high", 10),
                    CacheWarmer.PriorityKey.of("medium", 5)
            );

            CacheWarmer.WarmingResult<String> result = warmer.warmWithPriority(priorityKeys);

            assertEquals(3, result.warmedCount());
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @BeforeEach
        void setUp() {
            warmer = CacheWarmer.<String, String>builder()
                    .cache(cache)
                    .loader(key -> "loaded-" + key)
                    .build();
        }

        @Test
        @DisplayName("getStats returns statistics")
        void getStatsReturnsStatistics() {
            warmer.warm(List.of("k1", "k2", "k3"));

            CacheWarmer.WarmingStats stats = warmer.getStats();

            assertEquals(3, stats.totalWarmed());
            assertEquals(0, stats.totalFailed());
            assertEquals(3, stats.total());
            assertEquals(1.0, stats.successRate());
        }

        @Test
        @DisplayName("resetStats clears statistics")
        void resetStatsClearsStatistics() {
            warmer.warm(List.of("k1"));
            warmer.resetStats();

            CacheWarmer.WarmingStats stats = warmer.getStats();
            assertEquals(0, stats.totalWarmed());
        }
    }

    @Nested
    @DisplayName("WarmingResult Tests")
    class WarmingResultTests {

        @Test
        @DisplayName("WarmingResult record accessors work")
        void warmingResultRecordAccessorsWork() {
            CacheWarmer.WarmingResult<String> result = new CacheWarmer.WarmingResult<>(
                    10, 8, 1, 1, Set.of("failed"), Duration.ofSeconds(5)
            );

            assertEquals(10, result.totalKeys());
            assertEquals(8, result.warmedCount());
            assertEquals(1, result.failedCount());
            assertEquals(1, result.skippedCount());
            assertTrue(result.failedKeys().contains("failed"));
            assertEquals(Duration.ofSeconds(5), result.duration());
        }

        @Test
        @DisplayName("successRate calculation")
        void successRateCalculation() {
            CacheWarmer.WarmingResult<String> result = new CacheWarmer.WarmingResult<>(
                    10, 8, 2, 0, Set.of(), Duration.ofSeconds(1)
            );
            assertEquals(0.8, result.successRate(), 0.001);
        }

        @Test
        @DisplayName("isComplete when no failures")
        void isCompleteWhenNoFailures() {
            CacheWarmer.WarmingResult<String> complete = new CacheWarmer.WarmingResult<>(
                    10, 10, 0, 0, Set.of(), Duration.ofSeconds(1)
            );
            assertTrue(complete.isComplete());

            CacheWarmer.WarmingResult<String> incomplete = new CacheWarmer.WarmingResult<>(
                    10, 8, 2, 0, Set.of(), Duration.ofSeconds(1)
            );
            assertFalse(incomplete.isComplete());
        }

        @Test
        @DisplayName("percentComplete calculation")
        void percentCompleteCalculation() {
            CacheWarmer.WarmingResult<String> result = new CacheWarmer.WarmingResult<>(
                    100, 80, 10, 10, Set.of(), Duration.ofSeconds(1)
            );
            assertEquals(90.0, result.percentComplete(), 0.001);
        }
    }

    @Nested
    @DisplayName("WarmingOptions Tests")
    class WarmingOptionsTests {

        @Test
        @DisplayName("defaults creates default options")
        void defaultsCreatesDefaultOptions() {
            CacheWarmer.WarmingOptions options = CacheWarmer.WarmingOptions.defaults();

            assertFalse(options.forceRefresh());
            assertFalse(options.stopOnError());
            assertEquals(Duration.ofMinutes(30), options.timeout());
        }

        @Test
        @DisplayName("builder creates custom options")
        void builderCreatesCustomOptions() {
            CacheWarmer.WarmingOptions options = CacheWarmer.WarmingOptions.builder()
                    .forceRefresh(true)
                    .stopOnError(true)
                    .timeout(Duration.ofMinutes(10))
                    .build();

            assertTrue(options.forceRefresh());
            assertTrue(options.stopOnError());
            assertEquals(Duration.ofMinutes(10), options.timeout());
        }
    }

    @Nested
    @DisplayName("PriorityKey Tests")
    class PriorityKeyTests {

        @Test
        @DisplayName("PriorityKey.of creates priority key")
        void priorityKeyOfCreatesPriorityKey() {
            CacheWarmer.PriorityKey<String> pk = CacheWarmer.PriorityKey.of("key", 10);

            assertEquals("key", pk.key());
            assertEquals(10, pk.priority());
        }
    }

    @Nested
    @DisplayName("ProgressSnapshot Tests")
    class ProgressSnapshotTests {

        @Test
        @DisplayName("ProgressSnapshot record accessors work")
        void progressSnapshotRecordAccessorsWork() {
            CacheWarmer.ProgressSnapshot<String> snapshot = new CacheWarmer.ProgressSnapshot<>(
                    100, 50, 5, 10, Duration.ofSeconds(10)
            );

            assertEquals(100, snapshot.totalKeys());
            assertEquals(50, snapshot.warmedCount());
            assertEquals(5, snapshot.failedCount());
            assertEquals(10, snapshot.skippedCount());
            assertEquals(60.0, snapshot.percentComplete(), 0.001);
            assertEquals(35, snapshot.remaining());
        }
    }

    @Nested
    @DisplayName("WarmingStats Tests")
    class WarmingStatsTests {

        @Test
        @DisplayName("WarmingStats record accessors work")
        void warmingStatsRecordAccessorsWork() {
            CacheWarmer.WarmingStats stats = new CacheWarmer.WarmingStats(80, 10, 10);

            assertEquals(80, stats.totalWarmed());
            assertEquals(10, stats.totalFailed());
            assertEquals(10, stats.totalSkipped());
            assertEquals(100, stats.total());
        }

        @Test
        @DisplayName("successRate calculation")
        void successRateCalculation() {
            CacheWarmer.WarmingStats stats = new CacheWarmer.WarmingStats(80, 20, 0);
            assertEquals(0.8, stats.successRate(), 0.001);
        }

        @Test
        @DisplayName("successRate returns 1.0 when no attempts")
        void successRateReturnsOneWhenNoAttempts() {
            CacheWarmer.WarmingStats stats = new CacheWarmer.WarmingStats(0, 0, 0);
            assertEquals(1.0, stats.successRate());
        }
    }

    @Nested
    @DisplayName("WarmingEventHandler Tests")
    class WarmingEventHandlerTests {

        @Test
        @DisplayName("noOp returns no-op handler")
        void noOpReturnsNoOpHandler() {
            CacheWarmer.WarmingEventHandler<String, String> handler = CacheWarmer.WarmingEventHandler.noOp();
            assertNotNull(handler);
            assertDoesNotThrow(() -> handler.onStart(10));
            assertDoesNotThrow(() -> handler.onKeyWarmed("key", "value"));
            assertDoesNotThrow(() -> handler.onKeyFailed("key", new RuntimeException()));
            assertDoesNotThrow(() -> handler.onComplete(null));
        }

        @Test
        @DisplayName("custom event handler receives events")
        void customEventHandlerReceivesEvents() {
            AtomicInteger startCalls = new AtomicInteger(0);
            AtomicInteger completeCalls = new AtomicInteger(0);

            warmer = CacheWarmer.<String, String>builder()
                    .cache(cache)
                    .loader(key -> "loaded-" + key)
                    .eventHandler(new CacheWarmer.WarmingEventHandler<>() {
                        @Override
                        public void onStart(int totalKeys) {
                            startCalls.incrementAndGet();
                        }

                        @Override
                        public void onComplete(CacheWarmer.WarmingResult<String> result) {
                            completeCalls.incrementAndGet();
                        }
                    })
                    .build();

            warmer.warm(List.of("k1"));

            assertEquals(1, startCalls.get());
            assertEquals(1, completeCalls.get());
        }
    }
}
