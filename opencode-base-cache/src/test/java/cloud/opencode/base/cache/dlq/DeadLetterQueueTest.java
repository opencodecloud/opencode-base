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

package cloud.opencode.base.cache.dlq;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for DeadLetterQueue
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("DeadLetterQueue Tests")
class DeadLetterQueueTest {

    private DeadLetterQueue<String> dlq;

    @AfterEach
    void tearDown() {
        if (dlq != null) {
            dlq.close();
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("builder creates DLQ")
        void builderCreatesDlq() {
            dlq = DeadLetterQueue.<String>builder().build();
            assertNotNull(dlq);
        }

        @Test
        @DisplayName("builder with all options")
        void builderWithAllOptions() {
            dlq = DeadLetterQueue.<String>builder()
                    .maxRetries(5)
                    .initialBackoff(Duration.ofSeconds(2))
                    .maxBackoff(Duration.ofMinutes(10))
                    .backoffMultiplier(3.0)
                    .maxQueueSize(5000)
                    .retryLoader(key -> "loaded")
                    .eventHandler(DeadLetterQueue.DlqEventHandler.noOp())
                    .build();
            assertNotNull(dlq);
        }
    }

    @Nested
    @DisplayName("Add Operations Tests")
    class AddOperationsTests {

        @Test
        @DisplayName("add adds entry to DLQ")
        void addAddsEntryToDlq() {
            dlq = DeadLetterQueue.<String>builder().build();

            boolean result = dlq.add("key1", new RuntimeException("error"));

            assertTrue(result);
            assertTrue(dlq.contains("key1"));
            assertEquals(1, dlq.size());
        }

        @Test
        @DisplayName("add returns false when closed")
        void addReturnsFalseWhenClosed() {
            dlq = DeadLetterQueue.<String>builder().build();
            dlq.close();

            assertFalse(dlq.add("key", new RuntimeException("error")));
        }

        @Test
        @DisplayName("add updates existing entry")
        void addUpdatesExistingEntry() {
            dlq = DeadLetterQueue.<String>builder().build();

            dlq.add("key", new RuntimeException("error1"));
            dlq.add("key", new RuntimeException("error2"));

            assertEquals(1, dlq.size());
            DeadLetterQueue.FailedEntry<String> entry = dlq.get("key");
            assertEquals(2, entry.failureCount());
        }

        @Test
        @DisplayName("add evicts oldest when at capacity")
        void addEvictsOldestWhenAtCapacity() throws InterruptedException {
            dlq = DeadLetterQueue.<String>builder()
                    .maxQueueSize(2)
                    .build();

            dlq.add("k1", new RuntimeException());
            Thread.sleep(10);
            dlq.add("k2", new RuntimeException());
            Thread.sleep(10);
            dlq.add("k3", new RuntimeException());

            assertEquals(2, dlq.size());
            assertFalse(dlq.contains("k1"));
        }

        @Test
        @DisplayName("addAll adds multiple keys")
        void addAllAddsMultipleKeys() {
            dlq = DeadLetterQueue.<String>builder().build();

            dlq.addAll(List.of("k1", "k2", "k3"), new RuntimeException("batch error"));

            assertEquals(3, dlq.size());
        }
    }

    @Nested
    @DisplayName("Retry Operations Tests")
    class RetryOperationsTests {

        @Test
        @DisplayName("retry succeeds and removes from DLQ")
        void retrySucceedsAndRemovesFromDlq() {
            AtomicInteger loadCount = new AtomicInteger(0);
            dlq = DeadLetterQueue.<String>builder()
                    .retryLoader(key -> {
                        loadCount.incrementAndGet();
                        return "loaded";
                    })
                    .build();

            dlq.add("key", new RuntimeException("initial error"));

            boolean result = dlq.retry("key");

            assertTrue(result);
            assertFalse(dlq.contains("key"));
            assertEquals(1, loadCount.get());
        }

        @Test
        @DisplayName("retry fails and keeps in DLQ")
        void retryFailsAndKeepsInDlq() {
            dlq = DeadLetterQueue.<String>builder()
                    .retryLoader(key -> { throw new RuntimeException("retry failed"); })
                    .maxRetries(5)
                    .build();

            dlq.add("key", new RuntimeException("initial error"));

            boolean result = dlq.retry("key");

            assertFalse(result);
            assertTrue(dlq.contains("key"));
        }

        @Test
        @DisplayName("retry removes after max retries")
        void retryRemovesAfterMaxRetries() {
            dlq = DeadLetterQueue.<String>builder()
                    .retryLoader(key -> { throw new RuntimeException("always fails"); })
                    .maxRetries(1)
                    .build();

            dlq.add("key", new RuntimeException("error"));
            dlq.retry("key"); // retry 1 - fails and exceeds max

            assertFalse(dlq.contains("key"));
        }

        @Test
        @DisplayName("processRetries processes ready entries")
        void processRetriesProcessesReadyEntries() throws InterruptedException {
            AtomicInteger loadCount = new AtomicInteger(0);
            dlq = DeadLetterQueue.<String>builder()
                    .retryLoader(key -> {
                        loadCount.incrementAndGet();
                        return "loaded";
                    })
                    .build();

            dlq.add("key", new RuntimeException("error"));
            // FailedEntry uses 1 second hardcoded initial backoff
            Thread.sleep(1100);

            int processed = dlq.processRetries();

            assertEquals(1, processed);
            assertEquals(1, loadCount.get());
        }

        @Test
        @DisplayName("processRetries returns 0 without loader")
        void processRetriesReturnsZeroWithoutLoader() {
            dlq = DeadLetterQueue.<String>builder().build();
            dlq.add("key", new RuntimeException());

            assertEquals(0, dlq.processRetries());
        }

        @Test
        @DisplayName("startRetryProcessor starts background processing")
        void startRetryProcessorStartsBackgroundProcessing() {
            dlq = DeadLetterQueue.<String>builder()
                    .retryLoader(key -> "loaded")
                    .initialBackoff(Duration.ofMillis(50))
                    .build();

            dlq.startRetryProcessor();
            // Just verify it doesn't throw
            dlq.stopRetryProcessor();
        }
    }

    @Nested
    @DisplayName("Drain Operations Tests")
    class DrainOperationsTests {

        @Test
        @DisplayName("drain removes entries")
        void drainRemovesEntries() {
            dlq = DeadLetterQueue.<String>builder().build();
            dlq.add("k1", new RuntimeException());
            dlq.add("k2", new RuntimeException());
            dlq.add("k3", new RuntimeException());

            List<DeadLetterQueue.FailedEntry<String>> drained = dlq.drain(2);

            assertEquals(2, drained.size());
            assertEquals(1, dlq.size());
        }

        @Test
        @DisplayName("drainAll removes all entries")
        void drainAllRemovesAllEntries() {
            dlq = DeadLetterQueue.<String>builder().build();
            dlq.add("k1", new RuntimeException());
            dlq.add("k2", new RuntimeException());

            List<DeadLetterQueue.FailedEntry<String>> drained = dlq.drainAll();

            assertEquals(2, drained.size());
            assertTrue(dlq.isEmpty());
        }

        @Test
        @DisplayName("drainMatching removes matching entries")
        void drainMatchingRemovesMatchingEntries() {
            dlq = DeadLetterQueue.<String>builder().build();
            dlq.add("match-k1", new RuntimeException());
            dlq.add("match-k2", new RuntimeException());
            dlq.add("other-k3", new RuntimeException());

            List<DeadLetterQueue.FailedEntry<String>> drained = dlq.drainMatching(
                    entry -> entry.key().startsWith("match")
            );

            assertEquals(2, drained.size());
            assertEquals(1, dlq.size());
            assertTrue(dlq.contains("other-k3"));
        }
    }

    @Nested
    @DisplayName("Query Operations Tests")
    class QueryOperationsTests {

        @Test
        @DisplayName("contains returns true for existing")
        void containsReturnsTrueForExisting() {
            dlq = DeadLetterQueue.<String>builder().build();
            dlq.add("key", new RuntimeException());

            assertTrue(dlq.contains("key"));
            assertFalse(dlq.contains("other"));
        }

        @Test
        @DisplayName("get returns entry")
        void getReturnsEntry() {
            dlq = DeadLetterQueue.<String>builder().build();
            dlq.add("key", new RuntimeException("test error"));

            DeadLetterQueue.FailedEntry<String> entry = dlq.get("key");

            assertNotNull(entry);
            assertEquals("key", entry.key());
            assertEquals("test error", entry.lastError().getMessage());
        }

        @Test
        @DisplayName("get returns null for missing")
        void getReturnsNullForMissing() {
            dlq = DeadLetterQueue.<String>builder().build();
            assertNull(dlq.get("missing"));
        }

        @Test
        @DisplayName("getFailedKeys returns all keys")
        void getFailedKeysReturnsAllKeys() {
            dlq = DeadLetterQueue.<String>builder().build();
            dlq.add("k1", new RuntimeException());
            dlq.add("k2", new RuntimeException());

            Set<String> keys = dlq.getFailedKeys();

            assertEquals(2, keys.size());
            assertTrue(keys.contains("k1"));
            assertTrue(keys.contains("k2"));
        }

        @Test
        @DisplayName("size returns correct count")
        void sizeReturnsCorrectCount() {
            dlq = DeadLetterQueue.<String>builder().build();

            assertEquals(0, dlq.size());
            dlq.add("k1", new RuntimeException());
            assertEquals(1, dlq.size());
            dlq.add("k2", new RuntimeException());
            assertEquals(2, dlq.size());
        }

        @Test
        @DisplayName("isEmpty returns correct state")
        void isEmptyReturnsCorrectState() {
            dlq = DeadLetterQueue.<String>builder().build();

            assertTrue(dlq.isEmpty());
            dlq.add("key", new RuntimeException());
            assertFalse(dlq.isEmpty());
        }
    }

    @Nested
    @DisplayName("Analysis Tests")
    class AnalysisTests {

        @Test
        @DisplayName("analyze generates report")
        void analyzeGeneratesReport() {
            dlq = DeadLetterQueue.<String>builder()
                    .retryLoader(key -> { throw new RuntimeException("fail"); })
                    .build();

            dlq.add("k1", new RuntimeException("error1"));
            dlq.add("k2", new java.io.IOException("io error"));
            dlq.retry("k1"); // Add retry count

            DeadLetterQueue.DlqAnalysis<String> analysis = dlq.analyze();

            assertEquals(2, analysis.totalEntries());
            assertTrue(analysis.totalRetries() >= 0);
            assertNotNull(analysis.errorTypeCounts());
            assertTrue(analysis.errorTypeCounts().containsKey("RuntimeException"));
        }

        @Test
        @DisplayName("analyze returns empty for empty DLQ")
        void analyzeReturnsEmptyForEmptyDlq() {
            dlq = DeadLetterQueue.<String>builder().build();

            DeadLetterQueue.DlqAnalysis<String> analysis = dlq.analyze();

            assertEquals(0, analysis.totalEntries());
        }

        @Test
        @DisplayName("mostCommonErrorType returns most common")
        void mostCommonErrorTypeReturnsMostCommon() {
            dlq = DeadLetterQueue.<String>builder().build();
            dlq.add("k1", new RuntimeException());
            dlq.add("k2", new RuntimeException());
            dlq.add("k3", new java.io.IOException("io"));

            DeadLetterQueue.DlqAnalysis<String> analysis = dlq.analyze();

            assertEquals("RuntimeException", analysis.mostCommonErrorType());
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("getStats returns statistics")
        void getStatsReturnsStatistics() {
            dlq = DeadLetterQueue.<String>builder()
                    .retryLoader(key -> "loaded")
                    .build();

            dlq.add("k1", new RuntimeException());
            dlq.add("k2", new RuntimeException());
            dlq.retry("k1");

            DeadLetterQueue.DlqStats stats = dlq.getStats();

            assertEquals(2, stats.totalAdded());
            assertEquals(1, stats.totalRetried());
            assertEquals(1, stats.totalRecovered());
            assertEquals(1, stats.currentSize());
        }

        @Test
        @DisplayName("recoveryRate calculation")
        void recoveryRateCalculation() {
            DeadLetterQueue.DlqStats stats = new DeadLetterQueue.DlqStats(100, 50, 40, 10, 0);
            assertEquals(0.8, stats.recoveryRate(), 0.001);
        }

        @Test
        @DisplayName("recoveryRate returns 0 when no retries")
        void recoveryRateReturnsZeroWhenNoRetries() {
            DeadLetterQueue.DlqStats stats = new DeadLetterQueue.DlqStats(100, 0, 0, 0, 100);
            assertEquals(0.0, stats.recoveryRate());
        }
    }

    @Nested
    @DisplayName("FailedEntry Tests")
    class FailedEntryTests {

        @Test
        @DisplayName("FailedEntry tracks failure info")
        void failedEntryTracksFailureInfo() {
            dlq = DeadLetterQueue.<String>builder().build();
            dlq.add("key", new RuntimeException("test error"));

            DeadLetterQueue.FailedEntry<String> entry = dlq.get("key");

            assertEquals("key", entry.key());
            assertNotNull(entry.firstFailure());
            assertNotNull(entry.lastFailure());
            assertEquals("test error", entry.lastError().getMessage());
            assertEquals(1, entry.failureCount());
            assertEquals(0, entry.retryCount());
            assertNotNull(entry.nextRetryTime());
        }
    }

    @Nested
    @DisplayName("DiscardReason Tests")
    class DiscardReasonTests {

        @Test
        @DisplayName("DiscardReason enum values exist")
        void discardReasonEnumValuesExist() {
            assertEquals(3, DeadLetterQueue.DiscardReason.values().length);
            assertNotNull(DeadLetterQueue.DiscardReason.MAX_RETRIES);
            assertNotNull(DeadLetterQueue.DiscardReason.QUEUE_FULL);
            assertNotNull(DeadLetterQueue.DiscardReason.MANUAL);
        }
    }

    @Nested
    @DisplayName("DlqEventHandler Tests")
    class DlqEventHandlerTests {

        @Test
        @DisplayName("noOp returns no-op handler")
        void noOpReturnsNoOpHandler() {
            DeadLetterQueue.DlqEventHandler<String> handler = DeadLetterQueue.DlqEventHandler.noOp();
            assertNotNull(handler);
            assertDoesNotThrow(() -> handler.onAdd("key", new RuntimeException()));
            assertDoesNotThrow(() -> handler.onRecovered("key", 1));
            assertDoesNotThrow(() -> handler.onDiscard("key", DeadLetterQueue.DiscardReason.MAX_RETRIES));
        }

        @Test
        @DisplayName("logging returns logging handler")
        void loggingReturnsLoggingHandler() {
            DeadLetterQueue.DlqEventHandler<String> handler = DeadLetterQueue.DlqEventHandler.logging();
            assertNotNull(handler);
            assertDoesNotThrow(() -> handler.onAdd("key", new RuntimeException("test")));
            assertDoesNotThrow(() -> handler.onRecovered("key", 1));
            assertDoesNotThrow(() -> handler.onDiscard("key", DeadLetterQueue.DiscardReason.MAX_RETRIES));
        }

        @Test
        @DisplayName("custom handler receives events")
        void customHandlerReceivesEvents() {
            AtomicInteger addCount = new AtomicInteger(0);

            dlq = DeadLetterQueue.<String>builder()
                    .eventHandler(new DeadLetterQueue.DlqEventHandler<>() {
                        @Override
                        public void onAdd(String key, Throwable error) {
                            addCount.incrementAndGet();
                        }
                    })
                    .build();

            dlq.add("key", new RuntimeException());

            assertEquals(1, addCount.get());
        }
    }

    @Nested
    @DisplayName("Lifecycle Tests")
    class LifecycleTests {

        @Test
        @DisplayName("close shuts down scheduler")
        void closeShutdownsScheduler() {
            dlq = DeadLetterQueue.<String>builder().build();
            dlq.close();
            // Should not throw on subsequent add
            assertFalse(dlq.add("key", new RuntimeException()));
        }

        @Test
        @DisplayName("close is idempotent")
        void closeIsIdempotent() {
            dlq = DeadLetterQueue.<String>builder().build();
            dlq.close();
            assertDoesNotThrow(() -> dlq.close());
        }
    }
}
