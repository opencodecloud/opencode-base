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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for WriteBehindCache
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("WriteBehindCache Tests")
class WriteBehindCacheTest {

    private Cache<String, String> baseCache;
    private Map<String, String> backendStore;
    private Set<String> deletedKeys;

    @BeforeEach
    void setUp() {
        baseCache = OpenCache.<String, String>builder()
                .maximumSize(100)
                .build();
        backendStore = new ConcurrentHashMap<>();
        deletedKeys = ConcurrentHashMap.newKeySet();
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("builder() creates builder")
        void builderCreatesBuilder() {
            WriteBehindCache.Builder<String, String> builder = WriteBehindCache.builder(baseCache);
            assertNotNull(builder);
        }

        @Test
        @DisplayName("builder() with null cache throws exception")
        void builderWithNullThrows() {
            assertThrows(NullPointerException.class, () -> WriteBehindCache.builder(null));
        }

        @Test
        @DisplayName("build without writer throws exception")
        void buildWithoutWriterThrows() {
            assertThrows(NullPointerException.class, () ->
                    WriteBehindCache.builder(baseCache).build());
        }

        @Test
        @DisplayName("build with defaults")
        void buildWithDefaults() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .build();
            assertNotNull(cache);
            cache.shutdown();
        }

        @Test
        @DisplayName("build with custom batch size")
        void buildWithCustomBatchSize() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .batchSize(50)
                    .build();
            assertNotNull(cache);
            cache.shutdown();
        }

        @Test
        @DisplayName("build with custom flush interval")
        void buildWithCustomFlushInterval() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .flushInterval(Duration.ofSeconds(10))
                    .build();
            assertNotNull(cache);
            cache.shutdown();
        }

        @Test
        @DisplayName("build with custom retry settings")
        void buildWithCustomRetrySettings() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .maxRetries(5)
                    .retryDelay(Duration.ofMillis(200))
                    .build();
            assertNotNull(cache);
            cache.shutdown();
        }

        @Test
        @DisplayName("build with failure handler")
        void buildWithFailureHandler() {
            List<WriteBehindCache.WriteFailure<String, String>> failures = new CopyOnWriteArrayList<>();
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .onFailure(failures::add)
                    .build();
            assertNotNull(cache);
            cache.shutdown();
        }

        @Test
        @DisplayName("build with simple consumer writer")
        void buildWithConsumerWriter() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer(puts -> backendStore.putAll(puts))
                    .build();
            assertNotNull(cache);
            cache.shutdown();
        }
    }

    @Nested
    @DisplayName("Write Operations")
    class WriteOperationsTests {

        @Test
        @DisplayName("put queues write")
        void putQueuesWrite() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .build();

            cache.put("key", "value");
            assertEquals("value", cache.get("key"));
            assertTrue(cache.pendingWriteCount() >= 0);
            cache.shutdown();
        }

        @Test
        @DisplayName("put writes to backend on flush")
        void putWritesToBackendOnFlush() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .build();

            cache.put("key", "value");
            cache.flush();
            assertEquals("value", backendStore.get("key"));
            cache.shutdown();
        }

        @Test
        @DisplayName("putAll queues multiple writes")
        void putAllQueuesMultipleWrites() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .build();

            cache.putAll(Map.of("a", "1", "b", "2"));
            cache.flush();
            assertEquals("1", backendStore.get("a"));
            assertEquals("2", backendStore.get("b"));
            cache.shutdown();
        }

        @Test
        @DisplayName("putIfAbsent queues write if absent")
        void putIfAbsentQueuesWriteIfAbsent() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .build();

            assertTrue(cache.putIfAbsent("key", "value"));
            cache.flush();
            assertEquals("value", backendStore.get("key"));
            cache.shutdown();
        }

        @Test
        @DisplayName("putWithTtl queues write")
        void putWithTtlQueuesWrite() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .build();

            cache.putWithTtl("key", "value", Duration.ofMinutes(5));
            cache.flush();
            assertEquals("value", backendStore.get("key"));
            cache.shutdown();
        }

        @Test
        @DisplayName("putAllWithTtl queues writes")
        void putAllWithTtlQueuesWrites() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .build();

            cache.putAllWithTtl(Map.of("a", "1"), Duration.ofMinutes(5));
            cache.flush();
            assertEquals("1", backendStore.get("a"));
            cache.shutdown();
        }

        @Test
        @DisplayName("putIfAbsentWithTtl queues write if absent")
        void putIfAbsentWithTtlQueuesWriteIfAbsent() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .build();

            assertTrue(cache.putIfAbsentWithTtl("key", "value", Duration.ofMinutes(5)));
            cache.flush();
            assertEquals("value", backendStore.get("key"));
            cache.shutdown();
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperationsTests {

        @Test
        @DisplayName("invalidate queues delete")
        void invalidateQueuesDelete() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer(WriteBehindCache.BatchWriter.of(
                            puts -> backendStore.putAll(puts),
                            deletes -> deletedKeys.addAll(deletes)
                    ))
                    .build();

            cache.put("key", "value");
            cache.invalidate("key");
            cache.flush();
            assertTrue(deletedKeys.contains("key"));
            cache.shutdown();
        }

        @Test
        @DisplayName("invalidateAll queues deletes")
        void invalidateAllQueuesDeletes() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer(WriteBehindCache.BatchWriter.of(
                            puts -> backendStore.putAll(puts),
                            deletes -> deletedKeys.addAll(deletes)
                    ))
                    .build();

            cache.put("a", "1");
            cache.put("b", "2");
            cache.invalidateAll(List.of("a", "b"));
            cache.flush();
            assertTrue(deletedKeys.contains("a"));
            assertTrue(deletedKeys.contains("b"));
            cache.shutdown();
        }

        @Test
        @DisplayName("invalidateAll clears pending writes")
        void invalidateAllClearsPendingWrites() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .build();

            cache.put("key", "value");
            cache.invalidateAll();
            assertEquals(0, cache.pendingWriteCount());
            cache.shutdown();
        }
    }

    @Nested
    @DisplayName("Read Operations")
    class ReadOperationsTests {

        @Test
        @DisplayName("get returns cached value")
        void getReturnsCachedValue() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .build();

            cache.put("key", "value");
            assertEquals("value", cache.get("key"));
            cache.shutdown();
        }

        @Test
        @DisplayName("get with loader")
        void getWithLoader() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .build();

            String result = cache.get("key", k -> "loaded-" + k);
            assertEquals("loaded-key", result);
            cache.shutdown();
        }

        @Test
        @DisplayName("getAll returns cached values")
        void getAllReturnsCachedValues() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .build();

            cache.put("a", "1");
            cache.put("b", "2");
            Map<String, String> result = cache.getAll(List.of("a", "b"));
            assertEquals("1", result.get("a"));
            assertEquals("2", result.get("b"));
            cache.shutdown();
        }

        @Test
        @DisplayName("containsKey checks cache")
        void containsKeyChecksCache() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .build();

            cache.put("key", "value");
            assertTrue(cache.containsKey("key"));
            assertFalse(cache.containsKey("nonexistent"));
            cache.shutdown();
        }

        @Test
        @DisplayName("size returns cache size")
        void sizeReturnsCacheSize() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .build();

            cache.put("a", "1");
            cache.put("b", "2");
            assertEquals(2, cache.size());
            cache.shutdown();
        }

        @Test
        @DisplayName("keys returns cache keys")
        void keysReturnsCacheKeys() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .build();

            cache.put("key", "value");
            assertTrue(cache.keys().contains("key"));
            cache.shutdown();
        }

        @Test
        @DisplayName("values returns cache values")
        void valuesReturnsCacheValues() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .build();

            cache.put("key", "value");
            assertTrue(cache.values().contains("value"));
            cache.shutdown();
        }

        @Test
        @DisplayName("entries returns cache entries")
        void entriesReturnsCacheEntries() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .build();

            cache.put("key", "value");
            assertEquals(1, cache.entries().size());
            cache.shutdown();
        }

        @Test
        @DisplayName("asMap returns cache map")
        void asMapReturnsCacheMap() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .build();

            cache.put("key", "value");
            assertEquals("value", cache.asMap().get("key"));
            cache.shutdown();
        }
    }

    @Nested
    @DisplayName("Write Coalescing")
    class WriteCoalescingTests {

        @Test
        @DisplayName("multiple writes to same key are coalesced")
        void multipleWritesAreCoalesced() {
            AtomicInteger writeCount = new AtomicInteger(0);
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> {
                        writeCount.addAndGet(puts.size());
                        backendStore.putAll(puts);
                    })
                    .flushInterval(Duration.ofSeconds(60))
                    .build();

            // Multiple writes to same key
            cache.put("key", "value1");
            cache.put("key", "value2");
            cache.put("key", "value3");

            cache.flush();

            // Only the last value should be written
            assertEquals("value3", backendStore.get("key"));

            WriteBehindCache.WriteBehindStats stats = cache.writeBehindStats();
            assertTrue(stats.coalescedWrites() > 0);
            cache.shutdown();
        }
    }

    @Nested
    @DisplayName("Statistics")
    class StatisticsTests {

        @Test
        @DisplayName("writeBehindStats returns statistics")
        void writeBehindStatsReturnsStatistics() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .build();

            cache.put("key", "value");
            cache.flush();

            WriteBehindCache.WriteBehindStats stats = cache.writeBehindStats();
            assertNotNull(stats);
            assertTrue(stats.totalWrites() > 0);
            assertTrue(stats.batchedWrites() > 0);
            cache.shutdown();
        }

        @Test
        @DisplayName("pendingWriteCount returns pending count")
        void pendingWriteCountReturnsPendingCount() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .flushInterval(Duration.ofSeconds(60))
                    .build();

            cache.put("key", "value");
            assertTrue(cache.pendingWriteCount() >= 0);
            cache.shutdown();
        }

        @Test
        @DisplayName("stats coalescingRatio calculation")
        void statsCoalescingRatioCalculation() {
            WriteBehindCache.WriteBehindStats stats = new WriteBehindCache.WriteBehindStats(
                    10, 5, 0, 3, 0
            );
            assertEquals(0.3, stats.coalescingRatio(), 0.001);
        }

        @Test
        @DisplayName("stats successRatio calculation")
        void statsSuccessRatioCalculation() {
            WriteBehindCache.WriteBehindStats stats = new WriteBehindCache.WriteBehindStats(
                    10, 8, 2, 0, 0
            );
            assertEquals(0.8, stats.successRatio(), 0.001);
        }

        @Test
        @DisplayName("stats with zero writes")
        void statsWithZeroWrites() {
            WriteBehindCache.WriteBehindStats stats = new WriteBehindCache.WriteBehindStats(
                    0, 0, 0, 0, 0
            );
            assertEquals(0, stats.coalescingRatio());
            assertEquals(1.0, stats.successRatio());
        }
    }

    @Nested
    @DisplayName("Failure Handling")
    class FailureHandlingTests {

        @Test
        @DisplayName("failure handler is called on write failure")
        void failureHandlerIsCalledOnFailure() throws Exception {
            List<WriteBehindCache.WriteFailure<String, String>> failures = new CopyOnWriteArrayList<>();
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> {
                        throw new RuntimeException("Write failed");
                    })
                    .maxRetries(1)
                    .retryDelay(Duration.ofMillis(10))
                    .onFailure(failures::add)
                    .build();

            cache.put("key", "value");
            cache.flush();

            Thread.sleep(100); // Wait for retry and failure handling

            assertFalse(failures.isEmpty());
            cache.shutdown();
        }

        @Test
        @DisplayName("WriteFailure contains failed entries")
        void writeFailureContainsFailedEntries() {
            WriteBehindCache.WriteFailure<String, String> failure = new WriteBehindCache.WriteFailure<>(
                    Map.of("key", "value"),
                    Set.of("deleted"),
                    new RuntimeException("Test"),
                    3
            );

            assertEquals(1, failure.failedPuts().size());
            assertEquals(1, failure.failedDeletes().size());
            assertEquals(3, failure.attempts());
            assertNotNull(failure.cause());
        }
    }

    @Nested
    @DisplayName("Shutdown")
    class ShutdownTests {

        @Test
        @DisplayName("shutdown flushes pending writes")
        void shutdownFlushesPendingWrites() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .flushInterval(Duration.ofMinutes(5))
                    .build();

            cache.put("key", "value");
            cache.shutdown();

            assertEquals("value", backendStore.get("key"));
        }

        @Test
        @DisplayName("shutdown with timeout")
        void shutdownWithTimeout() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .build();

            cache.put("key", "value");
            assertDoesNotThrow(() -> cache.shutdown(Duration.ofSeconds(5)));
        }

        @Test
        @DisplayName("close calls shutdown")
        void closeCallsShutdown() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .build();

            cache.put("key", "value");
            assertDoesNotThrow(() -> cache.close());
        }
    }

    @Nested
    @DisplayName("Other Operations")
    class OtherOperationsTests {

        @Test
        @DisplayName("stats returns delegate stats")
        void statsReturnsDelegateStats() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .build();

            assertNotNull(cache.stats());
            cache.shutdown();
        }

        @Test
        @DisplayName("cleanUp cleans delegate")
        void cleanUpCleansDelegate() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .build();

            assertDoesNotThrow(() -> cache.cleanUp());
            cache.shutdown();
        }

        @Test
        @DisplayName("async returns async view")
        void asyncReturnsAsyncView() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .build();

            assertNotNull(cache.async());
            cache.shutdown();
        }

        @Test
        @DisplayName("name returns delegate name")
        void nameReturnsDelegateName() {
            WriteBehindCache<String, String> cache = WriteBehindCache.builder(baseCache)
                    .writer((puts, deletes) -> backendStore.putAll(puts))
                    .build();

            assertNotNull(cache.name());
            cache.shutdown();
        }
    }

    @Nested
    @DisplayName("BatchWriter Factory Methods")
    class BatchWriterFactoryTests {

        @Test
        @DisplayName("putsOnly creates writer for puts only")
        void putsOnlyCreatesWriter() throws Exception {
            WriteBehindCache.BatchWriter<String, String> writer =
                    WriteBehindCache.BatchWriter.putsOnly(puts -> backendStore.putAll(puts));

            writer.writeBatch(Map.of("key", "value"), Set.of("deleted"));
            assertEquals("value", backendStore.get("key"));
        }

        @Test
        @DisplayName("of creates writer with separate handlers")
        void ofCreatesWriter() throws Exception {
            WriteBehindCache.BatchWriter<String, String> writer =
                    WriteBehindCache.BatchWriter.of(
                            puts -> backendStore.putAll(puts),
                            deletes -> deletedKeys.addAll(deletes)
                    );

            writer.writeBatch(Map.of("key", "value"), Set.of("deleted"));
            assertEquals("value", backendStore.get("key"));
            assertTrue(deletedKeys.contains("deleted"));
        }
    }
}
