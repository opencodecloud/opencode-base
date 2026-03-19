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

package cloud.opencode.base.cache.write;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for WriteCoalescer
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("WriteCoalescer Tests")
class WriteCoalescerTest {

    private WriteCoalescer<String, String> coalescer;

    @AfterEach
    void tearDown() {
        if (coalescer != null && !coalescer.isClosed()) {
            coalescer.close();
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("builder creates coalescer")
        void builderCreatesCoalescer() {
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> {})
                    .build();
            assertNotNull(coalescer);
        }

        @Test
        @DisplayName("builder throws on null writer")
        void builderThrowsOnNullWriter() {
            assertThrows(NullPointerException.class, () ->
                    WriteCoalescer.<String, String>builder().build());
        }

        @Test
        @DisplayName("builder with batchSize")
        void builderWithBatchSize() {
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> {})
                    .batchSize(50)
                    .build();
            assertNotNull(coalescer);
        }

        @Test
        @DisplayName("builder with flushInterval")
        void builderWithFlushInterval() {
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> {})
                    .flushInterval(Duration.ofSeconds(10))
                    .build();
            assertNotNull(coalescer);
        }

        @Test
        @DisplayName("builder with deduplicateWrites")
        void builderWithDeduplicateWrites() {
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> {})
                    .deduplicateWrites(false)
                    .build();
            assertNotNull(coalescer);
        }

        @Test
        @DisplayName("builder with errorHandler")
        void builderWithErrorHandler() {
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> {})
                    .errorHandler(WriteCoalescer.WriteErrorHandler.logAndDiscard())
                    .build();
            assertNotNull(coalescer);
        }

        @Test
        @DisplayName("builder with consumer writer")
        void builderWithConsumerWriter() {
            List<Map<String, String>> batches = new ArrayList<>();
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((java.util.function.Consumer<Map<String, String>>) batches::add)
                    .build();
            assertNotNull(coalescer);
        }
    }

    @Nested
    @DisplayName("Write Operations Tests")
    class WriteOperationsTests {

        @Test
        @DisplayName("write queues entry")
        void writeQueuesEntry() {
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> {})
                    .batchSize(100)
                    .flushInterval(Duration.ZERO)
                    .build();

            coalescer.write("key", "value");
            assertEquals(1, coalescer.pendingCount());
            assertEquals("value", coalescer.getPending("key"));
        }

        @Test
        @DisplayName("write throws when closed")
        void writeThrowsWhenClosed() {
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> {})
                    .build();
            coalescer.close();

            assertThrows(IllegalStateException.class, () ->
                    coalescer.write("key", "value"));
        }

        @Test
        @DisplayName("writeAll queues multiple entries")
        void writeAllQueuesMultipleEntries() {
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> {})
                    .batchSize(100)
                    .flushInterval(Duration.ZERO)
                    .build();

            coalescer.writeAll(Map.of("k1", "v1", "k2", "v2"));
            assertEquals(2, coalescer.pendingCount());
        }

        @Test
        @DisplayName("delete queues null value")
        void deleteQueuesNullValue() {
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> {})
                    .batchSize(100)
                    .flushInterval(Duration.ZERO)
                    .build();

            coalescer.delete("key");
            assertTrue(coalescer.hasPending("key"));
            assertNull(coalescer.getPending("key"));
        }
    }

    @Nested
    @DisplayName("Deduplication Tests")
    class DeduplicationTests {

        @Test
        @DisplayName("deduplication keeps latest value")
        void deduplicationKeepsLatestValue() {
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> {})
                    .batchSize(100)
                    .flushInterval(Duration.ZERO)
                    .deduplicateWrites(true)
                    .build();

            coalescer.write("key", "value1");
            coalescer.write("key", "value2");
            coalescer.write("key", "value3");

            assertEquals(1, coalescer.pendingCount());
            assertEquals("value3", coalescer.getPending("key"));
        }

        @Test
        @DisplayName("deduplication tracks deduplicated count")
        void deduplicationTracksDeduplicatedCount() {
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> {})
                    .batchSize(100)
                    .flushInterval(Duration.ZERO)
                    .deduplicateWrites(true)
                    .build();

            coalescer.write("key", "value1");
            coalescer.write("key", "value2");
            coalescer.write("key", "value3");

            WriteCoalescer.CoalescerStats stats = coalescer.getStats();
            assertEquals(3, stats.totalWrites());
            assertEquals(2, stats.totalDeduplicated());
        }
    }

    @Nested
    @DisplayName("Flush Tests")
    class FlushTests {

        @Test
        @DisplayName("flush writes pending entries")
        void flushWritesPendingEntries() {
            ConcurrentHashMap<String, String> written = new ConcurrentHashMap<>();
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> written.putAll(batch))
                    .batchSize(100)
                    .flushInterval(Duration.ZERO)
                    .build();

            coalescer.write("k1", "v1");
            coalescer.write("k2", "v2");
            int flushed = coalescer.flush();

            assertEquals(2, flushed);
            assertEquals("v1", written.get("k1"));
            assertEquals("v2", written.get("k2"));
            assertEquals(0, coalescer.pendingCount());
        }

        @Test
        @DisplayName("flush returns 0 when empty")
        void flushReturnsZeroWhenEmpty() {
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> {})
                    .build();

            assertEquals(0, coalescer.flush());
        }

        @Test
        @DisplayName("automatic flush on batch size")
        void automaticFlushOnBatchSize() {
            AtomicInteger flushCount = new AtomicInteger(0);
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> flushCount.incrementAndGet())
                    .batchSize(3)
                    .flushInterval(Duration.ZERO)
                    .build();

            coalescer.write("k1", "v1");
            coalescer.write("k2", "v2");
            assertEquals(0, flushCount.get());

            coalescer.write("k3", "v3");
            assertEquals(1, flushCount.get());
        }

        @Test
        @DisplayName("flush interval triggers flush")
        void flushIntervalTriggersFlush() throws InterruptedException {
            CountDownLatch flushed = new CountDownLatch(1);
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> flushed.countDown())
                    .batchSize(100)
                    .flushInterval(Duration.ofMillis(100))
                    .build();

            coalescer.write("key", "value");
            assertTrue(flushed.await(500, TimeUnit.MILLISECONDS));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("error handler receives failed batch")
        void errorHandlerReceivesFailedBatch() {
            AtomicReference<Map<String, String>> failedBatch = new AtomicReference<>();
            AtomicReference<Exception> error = new AtomicReference<>();

            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> { throw new RuntimeException("Write failed"); })
                    .errorHandler((batch, e) -> {
                        failedBatch.set(batch);
                        error.set(e);
                    })
                    .batchSize(100)
                    .flushInterval(Duration.ZERO)
                    .build();

            coalescer.write("key", "value");
            coalescer.flush();

            assertNotNull(failedBatch.get());
            assertEquals("value", failedBatch.get().get("key"));
            assertNotNull(error.get());
        }

        @Test
        @DisplayName("errors increment error count")
        void errorsIncrementErrorCount() {
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> { throw new RuntimeException("Write failed"); })
                    .errorHandler(WriteCoalescer.WriteErrorHandler.logAndDiscard())
                    .batchSize(100)
                    .flushInterval(Duration.ZERO)
                    .build();

            coalescer.write("key", "value");
            coalescer.flush();

            assertEquals(1, coalescer.getStats().totalErrors());
        }

        @Test
        @DisplayName("rethrow error handler throws")
        void rethrowErrorHandlerThrows() {
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> { throw new RuntimeException("Write failed"); })
                    .errorHandler(WriteCoalescer.WriteErrorHandler.rethrow())
                    .batchSize(100)
                    .flushInterval(Duration.ZERO)
                    .build();

            coalescer.write("key", "value");
            assertThrows(RuntimeException.class, () -> coalescer.flush());
        }

        @Test
        @DisplayName("rethrow wraps checked exceptions")
        void rethrowWrapsCheckedExceptions() {
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> { throw new Exception("Checked exception"); })
                    .errorHandler(WriteCoalescer.WriteErrorHandler.rethrow())
                    .batchSize(100)
                    .flushInterval(Duration.ZERO)
                    .build();

            coalescer.write("key", "value");
            RuntimeException ex = assertThrows(RuntimeException.class, () -> coalescer.flush());
            assertEquals("Batch write failed", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Query Operations Tests")
    class QueryOperationsTests {

        @Test
        @DisplayName("getPending returns pending value")
        void getPendingReturnsPendingValue() {
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> {})
                    .batchSize(100)
                    .flushInterval(Duration.ZERO)
                    .build();

            coalescer.write("key", "value");
            assertEquals("value", coalescer.getPending("key"));
        }

        @Test
        @DisplayName("getPending returns null for non-pending")
        void getPendingReturnsNullForNonPending() {
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> {})
                    .build();

            assertNull(coalescer.getPending("nonexistent"));
        }

        @Test
        @DisplayName("hasPending returns true for pending")
        void hasPendingReturnsTrueForPending() {
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> {})
                    .batchSize(100)
                    .flushInterval(Duration.ZERO)
                    .build();

            coalescer.write("key", "value");
            assertTrue(coalescer.hasPending("key"));
        }

        @Test
        @DisplayName("hasPending returns false for non-pending")
        void hasPendingReturnsFalseForNonPending() {
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> {})
                    .build();

            assertFalse(coalescer.hasPending("nonexistent"));
        }

        @Test
        @DisplayName("getAllPending returns all pending")
        void getAllPendingReturnsAllPending() {
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> {})
                    .batchSize(100)
                    .flushInterval(Duration.ZERO)
                    .build();

            coalescer.write("k1", "v1");
            coalescer.write("k2", "v2");

            Map<String, String> pending = coalescer.getAllPending();
            assertEquals(2, pending.size());
            assertEquals("v1", pending.get("k1"));
            assertEquals("v2", pending.get("k2"));
        }

        @Test
        @DisplayName("pendingCount returns correct count")
        void pendingCountReturnsCorrectCount() {
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> {})
                    .batchSize(100)
                    .flushInterval(Duration.ZERO)
                    .build();

            assertEquals(0, coalescer.pendingCount());
            coalescer.write("k1", "v1");
            assertEquals(1, coalescer.pendingCount());
            coalescer.write("k2", "v2");
            assertEquals(2, coalescer.pendingCount());
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("getStats returns statistics")
        void getStatsReturnsStatistics() {
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> {})
                    .batchSize(100)
                    .flushInterval(Duration.ZERO)
                    .build();

            coalescer.write("k1", "v1");
            coalescer.write("k2", "v2");
            coalescer.flush();

            WriteCoalescer.CoalescerStats stats = coalescer.getStats();
            assertEquals(2, stats.totalWrites());
            assertEquals(1, stats.totalFlushes());
            assertEquals(2, stats.totalBatched());
            assertEquals(0, stats.currentPending());
        }

        @Test
        @DisplayName("resetStats clears statistics")
        void resetStatsClearsStatistics() {
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> {})
                    .batchSize(100)
                    .flushInterval(Duration.ZERO)
                    .build();

            coalescer.write("key", "value");
            coalescer.flush();
            coalescer.resetStats();

            WriteCoalescer.CoalescerStats stats = coalescer.getStats();
            assertEquals(0, stats.totalWrites());
            assertEquals(0, stats.totalFlushes());
        }

        @Test
        @DisplayName("deduplicationRate calculation")
        void deduplicationRateCalculation() {
            WriteCoalescer.CoalescerStats stats = new WriteCoalescer.CoalescerStats(
                    100, 10, 80, 20, 0, 0);
            assertEquals(0.2, stats.deduplicationRate(), 0.001);
        }

        @Test
        @DisplayName("deduplicationRate returns 0 when no writes")
        void deduplicationRateReturnsZeroWhenNoWrites() {
            WriteCoalescer.CoalescerStats stats = new WriteCoalescer.CoalescerStats(
                    0, 0, 0, 0, 0, 0);
            assertEquals(0.0, stats.deduplicationRate());
        }

        @Test
        @DisplayName("averageBatchSize calculation")
        void averageBatchSizeCalculation() {
            WriteCoalescer.CoalescerStats stats = new WriteCoalescer.CoalescerStats(
                    100, 10, 80, 0, 0, 0);
            assertEquals(8.0, stats.averageBatchSize(), 0.001);
        }

        @Test
        @DisplayName("averageBatchSize returns 0 when no flushes")
        void averageBatchSizeReturnsZeroWhenNoFlushes() {
            WriteCoalescer.CoalescerStats stats = new WriteCoalescer.CoalescerStats(
                    100, 0, 0, 0, 0, 0);
            assertEquals(0.0, stats.averageBatchSize());
        }

        @Test
        @DisplayName("errorRate calculation")
        void errorRateCalculation() {
            WriteCoalescer.CoalescerStats stats = new WriteCoalescer.CoalescerStats(
                    100, 10, 80, 0, 20, 0);
            assertEquals(0.2, stats.errorRate(), 0.001);
        }

        @Test
        @DisplayName("errorRate returns 0 when no total")
        void errorRateReturnsZeroWhenNoTotal() {
            WriteCoalescer.CoalescerStats stats = new WriteCoalescer.CoalescerStats(
                    0, 0, 0, 0, 0, 0);
            assertEquals(0.0, stats.errorRate());
        }
    }

    @Nested
    @DisplayName("Lifecycle Tests")
    class LifecycleTests {

        @Test
        @DisplayName("close flushes pending writes")
        void closeFlushsPendingWrites() {
            ConcurrentHashMap<String, String> written = new ConcurrentHashMap<>();
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> written.putAll(batch))
                    .batchSize(100)
                    .flushInterval(Duration.ZERO)
                    .build();

            coalescer.write("key", "value");
            coalescer.close();

            assertEquals("value", written.get("key"));
        }

        @Test
        @DisplayName("isClosed returns correct state")
        void isClosedReturnsCorrectState() {
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> {})
                    .build();

            assertFalse(coalescer.isClosed());
            coalescer.close();
            assertTrue(coalescer.isClosed());
        }

        @Test
        @DisplayName("close is idempotent")
        void closeIsIdempotent() {
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> {})
                    .build();

            coalescer.close();
            assertDoesNotThrow(() -> coalescer.close());
        }
    }

    @Nested
    @DisplayName("BatchWriter Interface Tests")
    class BatchWriterInterfaceTests {

        @Test
        @DisplayName("BatchWriter is functional interface")
        void batchWriterIsFunctionalInterface() {
            WriteCoalescer.BatchWriter<String, String> writer = batch -> {
                // Process batch
            };
            assertNotNull(writer);
        }
    }

    @Nested
    @DisplayName("WriteErrorHandler Interface Tests")
    class WriteErrorHandlerInterfaceTests {

        @Test
        @DisplayName("WriteErrorHandler is functional interface")
        void writeErrorHandlerIsFunctionalInterface() {
            WriteCoalescer.WriteErrorHandler<String, String> handler = (batch, error) -> {
                // Handle error
            };
            assertNotNull(handler);
        }

        @Test
        @DisplayName("logAndDiscard creates handler")
        void logAndDiscardCreatesHandler() {
            WriteCoalescer.WriteErrorHandler<String, String> handler =
                    WriteCoalescer.WriteErrorHandler.logAndDiscard();
            assertNotNull(handler);
            assertDoesNotThrow(() -> handler.handleError(Map.of("k", "v"), new RuntimeException("test")));
        }

        @Test
        @DisplayName("rethrow creates handler")
        void rethrowCreatesHandler() {
            WriteCoalescer.WriteErrorHandler<String, String> handler =
                    WriteCoalescer.WriteErrorHandler.rethrow();
            assertNotNull(handler);
            assertThrows(RuntimeException.class, () ->
                    handler.handleError(Map.of("k", "v"), new RuntimeException("test")));
        }
    }

    @Nested
    @DisplayName("Concurrent Operations Tests")
    class ConcurrentOperationsTests {

        @Test
        @DisplayName("concurrent writes are thread-safe")
        void concurrentWritesAreThreadSafe() throws InterruptedException {
            AtomicInteger writeCount = new AtomicInteger(0);
            coalescer = WriteCoalescer.<String, String>builder()
                    .writer((WriteCoalescer.BatchWriter<String, String>) batch -> writeCount.addAndGet(batch.size()))
                    .batchSize(1000)
                    .flushInterval(Duration.ZERO)
                    .deduplicateWrites(false)
                    .build();

            int threadCount = 10;
            int writesPerThread = 100;
            Thread[] threads = new Thread[threadCount];
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                int threadId = i;
                threads[i] = new Thread(() -> {
                    try {
                        startLatch.await();
                        for (int j = 0; j < writesPerThread; j++) {
                            coalescer.write("key-" + threadId + "-" + j, "value");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneLatch.countDown();
                    }
                });
                threads[i].start();
            }

            startLatch.countDown();
            doneLatch.await(10, TimeUnit.SECONDS);
            coalescer.flush();

            assertEquals(threadCount * writesPerThread, writeCount.get());
        }
    }
}
