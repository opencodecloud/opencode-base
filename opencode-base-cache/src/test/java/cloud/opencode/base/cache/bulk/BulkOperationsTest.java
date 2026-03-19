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

package cloud.opencode.base.cache.bulk;

import cloud.opencode.base.cache.Cache;
import cloud.opencode.base.cache.OpenCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for BulkOperations
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("BulkOperations Tests")
class BulkOperationsTest {

    private Cache<String, String> cache;
    private BulkOperations<String, String> bulk;

    @BeforeEach
    void setUp() {
        cache = OpenCache.<String, String>builder()
                .maximumSize(1000)
                .build("test-bulk");
        bulk = BulkOperations.on(cache);
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("on creates BulkOperations")
        void onCreatesBulkOperations() {
            BulkOperations<String, String> ops = BulkOperations.on(cache);
            assertNotNull(ops);
        }

        @Test
        @DisplayName("on throws on null cache")
        void onThrowsOnNullCache() {
            assertThrows(NullPointerException.class, () -> BulkOperations.on(null));
        }
    }

    @Nested
    @DisplayName("Atomic PutAll Tests")
    class AtomicPutAllTests {

        @Test
        @DisplayName("bulkPutAll adds all entries")
        void bulkPutAllAddsAllEntries() {
            Map<String, String> entries = Map.of("k1", "v1", "k2", "v2", "k3", "v3");

            boolean result = bulk.bulkPutAll(entries);

            assertTrue(result);
            assertEquals("v1", cache.get("k1"));
            assertEquals("v2", cache.get("k2"));
            assertEquals("v3", cache.get("k3"));
        }

        @Test
        @DisplayName("bulkPutAll updates existing entries")
        void bulkPutAllUpdatesExistingEntries() {
            cache.put("k1", "old");

            boolean result = bulk.bulkPutAll(Map.of("k1", "new", "k2", "v2"));

            assertTrue(result);
            assertEquals("new", cache.get("k1"));
            assertEquals("v2", cache.get("k2"));
        }
    }

    @Nested
    @DisplayName("Atomic InvalidateAll Tests")
    class AtomicInvalidateAllTests {

        @Test
        @DisplayName("atomicInvalidateAll removes entries and returns backup")
        void atomicInvalidateAllRemovesEntriesAndReturnsBackup() {
            cache.put("k1", "v1");
            cache.put("k2", "v2");
            cache.put("k3", "v3");

            Map<String, String> backup = bulk.atomicInvalidateAll(List.of("k1", "k2"));

            assertNull(cache.get("k1"));
            assertNull(cache.get("k2"));
            assertEquals("v3", cache.get("k3"));
            assertEquals("v1", backup.get("k1"));
            assertEquals("v2", backup.get("k2"));
        }

        @Test
        @DisplayName("atomicInvalidateAll handles missing keys")
        void atomicInvalidateAllHandlesMissingKeys() {
            cache.put("k1", "v1");

            Map<String, String> backup = bulk.atomicInvalidateAll(List.of("k1", "missing"));

            assertEquals(1, backup.size());
            assertEquals("v1", backup.get("k1"));
        }
    }

    @Nested
    @DisplayName("Conditional Put Tests")
    class ConditionalPutTests {

        @Test
        @DisplayName("putAllIfAllPresent succeeds when all present")
        void putAllIfAllPresentSucceedsWhenAllPresent() {
            cache.put("k1", "old1");
            cache.put("k2", "old2");

            boolean result = bulk.putAllIfAllPresent(Map.of("k1", "new1", "k2", "new2"));

            assertTrue(result);
            assertEquals("new1", cache.get("k1"));
            assertEquals("new2", cache.get("k2"));
        }

        @Test
        @DisplayName("putAllIfAllPresent fails when some absent")
        void putAllIfAllPresentFailsWhenSomeAbsent() {
            cache.put("k1", "old1");

            boolean result = bulk.putAllIfAllPresent(Map.of("k1", "new1", "k2", "new2"));

            assertFalse(result);
            assertEquals("old1", cache.get("k1"));
        }

        @Test
        @DisplayName("putAllIfAllAbsent succeeds when all absent")
        void putAllIfAllAbsentSucceedsWhenAllAbsent() {
            boolean result = bulk.putAllIfAllAbsent(Map.of("k1", "v1", "k2", "v2"));

            assertTrue(result);
            assertEquals("v1", cache.get("k1"));
            assertEquals("v2", cache.get("k2"));
        }

        @Test
        @DisplayName("putAllIfAllAbsent fails when some present")
        void putAllIfAllAbsentFailsWhenSomePresent() {
            cache.put("k1", "existing");

            boolean result = bulk.putAllIfAllAbsent(Map.of("k1", "v1", "k2", "v2"));

            assertFalse(result);
            assertEquals("existing", cache.get("k1"));
            assertNull(cache.get("k2"));
        }

        @Test
        @DisplayName("putAllIfAbsent returns result with counts")
        void putAllIfAbsentReturnsResultWithCounts() {
            cache.put("k1", "existing");

            BulkOperations.BulkPutResult<String> result = bulk.putAllIfAbsent(
                    Map.of("k1", "v1", "k2", "v2", "k3", "v3"));

            assertEquals(2, result.insertedCount());
            assertEquals(1, result.skippedCount());
            assertEquals(3, result.totalCount());
            assertTrue(result.inserted().contains("k2"));
            assertTrue(result.inserted().contains("k3"));
            assertTrue(result.skipped().contains("k1"));
        }

        @Test
        @DisplayName("invalidateAllIfMatch succeeds when all match")
        void invalidateAllIfMatchSucceedsWhenAllMatch() {
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            boolean result = bulk.invalidateAllIfMatch(Map.of("k1", "v1", "k2", "v2"));

            assertTrue(result);
            assertNull(cache.get("k1"));
            assertNull(cache.get("k2"));
        }

        @Test
        @DisplayName("invalidateAllIfMatch fails when some don't match")
        void invalidateAllIfMatchFailsWhenSomeDontMatch() {
            cache.put("k1", "v1");
            cache.put("k2", "different");

            boolean result = bulk.invalidateAllIfMatch(Map.of("k1", "v1", "k2", "v2"));

            assertFalse(result);
            assertEquals("v1", cache.get("k1"));
            assertEquals("different", cache.get("k2"));
        }
    }

    @Nested
    @DisplayName("Batch Processing Tests")
    class BatchProcessingTests {

        @Test
        @DisplayName("batchProcess processes in batches")
        void batchProcessProcessesInBatches() {
            for (int i = 0; i < 10; i++) {
                cache.put("k" + i, "v" + i);
            }

            AtomicInteger batchCount = new AtomicInteger(0);
            int processed = bulk.batchProcess(
                    List.of("k0", "k1", "k2", "k3", "k4", "k5", "k6", "k7", "k8", "k9"),
                    3,
                    batch -> batchCount.incrementAndGet()
            );

            assertEquals(10, processed);
            assertEquals(4, batchCount.get()); // 3+3+3+1
        }

        @Test
        @DisplayName("batchProcessParallel processes in parallel")
        void batchProcessParallelProcessesInParallel() {
            for (int i = 0; i < 20; i++) {
                cache.put("k" + i, "v" + i);
            }

            List<String> keys = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                keys.add("k" + i);
            }

            AtomicInteger processedCount = new AtomicInteger(0);
            CompletableFuture<BulkOperations.BatchResult> future = bulk.batchProcessParallel(
                    keys, 5, 4,
                    batch -> processedCount.addAndGet(batch.size())
            );

            BulkOperations.BatchResult result = future.join();
            assertEquals(20, result.processedCount());
            assertEquals(4, result.batchCount());
            assertFalse(result.hasErrors());
        }
    }

    @Nested
    @DisplayName("Compute Operations Tests")
    class ComputeOperationsTests {

        @Test
        @DisplayName("computeAll computes new values")
        void computeAllComputesNewValues() {
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            Map<String, String> results = bulk.computeAll(
                    List.of("k1", "k2", "k3"),
                    (key, oldValue) -> oldValue == null ? "new-" + key : oldValue + "-updated"
            );

            assertEquals(3, results.size());
            assertEquals("v1-updated", cache.get("k1"));
            assertEquals("v2-updated", cache.get("k2"));
            assertEquals("new-k3", cache.get("k3"));
        }

        @Test
        @DisplayName("computeAll removes when returning null")
        void computeAllRemovesWhenReturningNull() {
            cache.put("k1", "v1");

            bulk.computeAll(List.of("k1"), (key, value) -> null);

            assertNull(cache.get("k1"));
        }

        @Test
        @DisplayName("computeAllIfAbsent only computes absent")
        void computeAllIfAbsentOnlyComputesAbsent() {
            cache.put("k1", "existing");

            Map<String, String> results = bulk.computeAllIfAbsent(
                    List.of("k1", "k2"),
                    key -> "computed-" + key
            );

            assertEquals(1, results.size());
            assertEquals("existing", cache.get("k1"));
            assertEquals("computed-k2", cache.get("k2"));
        }

        @Test
        @DisplayName("replaceAllMatching replaces matching entries")
        void replaceAllMatchingReplacesMatchingEntries() {
            cache.put("prefix-k1", "v1");
            cache.put("prefix-k2", "v2");
            cache.put("other-k3", "v3");

            int count = bulk.replaceAllMatching(
                    (key, value) -> key.startsWith("prefix-"),
                    (key, value) -> value + "-replaced"
            );

            assertEquals(2, count);
            assertEquals("v1-replaced", cache.get("prefix-k1"));
            assertEquals("v2-replaced", cache.get("prefix-k2"));
            assertEquals("v3", cache.get("other-k3"));
        }
    }

    @Nested
    @DisplayName("Bulk Put with TTL Tests")
    class BulkPutWithTtlTests {

        @Test
        @DisplayName("putAllWithTtl sets individual TTLs")
        void putAllWithTtlSetsIndividualTtls() {
            Map<String, BulkOperations.TtlValue<String>> entries = new LinkedHashMap<>();
            entries.put("k1", BulkOperations.TtlValue.of("v1", Duration.ofMinutes(10)));
            entries.put("k2", BulkOperations.TtlValue.of("v2", Duration.ofMinutes(20)));

            bulk.putAllWithTtl(entries);

            assertEquals("v1", cache.get("k1"));
            assertEquals("v2", cache.get("k2"));
        }
    }

    @Nested
    @DisplayName("BulkPutResult Tests")
    class BulkPutResultTests {

        @Test
        @DisplayName("BulkPutResult record accessors work")
        void bulkPutResultRecordAccessorsWork() {
            Set<String> inserted = Set.of("k1", "k2");
            Set<String> skipped = Set.of("k3");

            BulkOperations.BulkPutResult<String> result = new BulkOperations.BulkPutResult<>(inserted, skipped);

            assertEquals(inserted, result.inserted());
            assertEquals(skipped, result.skipped());
            assertEquals(2, result.insertedCount());
            assertEquals(1, result.skippedCount());
            assertEquals(3, result.totalCount());
        }
    }

    @Nested
    @DisplayName("BatchResult Tests")
    class BatchResultTests {

        @Test
        @DisplayName("BatchResult record accessors work")
        void batchResultRecordAccessorsWork() {
            BulkOperations.BatchResult result = new BulkOperations.BatchResult(80, 20, 4);

            assertEquals(80, result.processedCount());
            assertEquals(20, result.errorCount());
            assertEquals(4, result.batchCount());
            assertTrue(result.hasErrors());
            assertEquals(0.2, result.errorRate(), 0.001);
        }

        @Test
        @DisplayName("BatchResult errorRate with no total")
        void batchResultErrorRateWithNoTotal() {
            BulkOperations.BatchResult result = new BulkOperations.BatchResult(0, 0, 0);
            assertEquals(0.0, result.errorRate());
        }
    }

    @Nested
    @DisplayName("TtlValue Tests")
    class TtlValueTests {

        @Test
        @DisplayName("TtlValue.of creates TtlValue")
        void ttlValueOfCreatesTtlValue() {
            BulkOperations.TtlValue<String> tv = BulkOperations.TtlValue.of("value", Duration.ofMinutes(10));

            assertEquals("value", tv.value());
            assertEquals(Duration.ofMinutes(10), tv.ttl());
        }
    }

    @Nested
    @DisplayName("BatchProcessor Tests")
    class BatchProcessorTests {

        @Test
        @DisplayName("BatchProcessor is functional interface")
        void batchProcessorIsFunctionalInterface() {
            BulkOperations.BatchProcessor<String, String> processor = batch -> {
                // Process batch
            };
            assertNotNull(processor);
        }
    }
}
