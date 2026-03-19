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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for CacheMetrics
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("CacheMetrics Tests")
class CacheMetricsTest {

    private CacheMetrics metrics;

    @BeforeEach
    void setUp() {
        metrics = CacheMetrics.create();
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("create returns new instance")
        void createReturnsNewInstance() {
            CacheMetrics m = CacheMetrics.create();
            assertNotNull(m);
            assertEquals(0, m.getGetCount());
        }
    }

    @Nested
    @DisplayName("Recording Get Latency Tests")
    class RecordingGetLatencyTests {

        @Test
        @DisplayName("recordGetLatency increments count")
        void recordGetLatencyIncrementsCount() {
            metrics.recordGetLatency(1000);
            metrics.recordGetLatency(2000);

            assertEquals(2, metrics.getGetCount());
        }

        @Test
        @DisplayName("recordGetLatency updates min/max")
        void recordGetLatencyUpdatesMinMax() {
            metrics.recordGetLatency(1000);
            metrics.recordGetLatency(5000);
            metrics.recordGetLatency(3000);

            assertEquals(1000, metrics.getMinGetLatency());
            assertEquals(5000, metrics.getMaxGetLatency());
        }

        @Test
        @DisplayName("recordGetLatency updates average")
        void recordGetLatencyUpdatesAverage() {
            metrics.recordGetLatency(1000);
            metrics.recordGetLatency(3000);

            assertEquals(2000.0, metrics.getAverageGetLatency());
        }
    }

    @Nested
    @DisplayName("Recording Put Latency Tests")
    class RecordingPutLatencyTests {

        @Test
        @DisplayName("recordPutLatency increments count")
        void recordPutLatencyIncrementsCount() {
            metrics.recordPutLatency(1000);
            metrics.recordPutLatency(2000);

            assertEquals(2, metrics.getPutCount());
        }

        @Test
        @DisplayName("recordPutLatency updates average")
        void recordPutLatencyUpdatesAverage() {
            metrics.recordPutLatency(1000);
            metrics.recordPutLatency(3000);

            assertEquals(2000.0, metrics.getAveragePutLatency());
        }
    }

    @Nested
    @DisplayName("Recording Load Latency Tests")
    class RecordingLoadLatencyTests {

        @Test
        @DisplayName("recordLoadLatency increments count")
        void recordLoadLatencyIncrementsCount() {
            metrics.recordLoadLatency(1000);
            metrics.recordLoadLatency(2000);

            assertEquals(2, metrics.getLoadCount());
        }

        @Test
        @DisplayName("recordLoadLatency updates average")
        void recordLoadLatencyUpdatesAverage() {
            metrics.recordLoadLatency(1000);
            metrics.recordLoadLatency(3000);

            assertEquals(2000.0, metrics.getAverageLoadLatency());
        }
    }

    @Nested
    @DisplayName("Recording Eviction Tests")
    class RecordingEvictionTests {

        @Test
        @DisplayName("recordEviction increments count")
        void recordEvictionIncrementsCount() {
            metrics.recordEviction();
            metrics.recordEviction();
            metrics.recordEviction();

            assertEquals(3, metrics.getEvictionCount());
        }
    }

    @Nested
    @DisplayName("Get Latency Percentile Tests")
    class GetLatencyPercentileTests {

        @Test
        @DisplayName("percentiles return 0 when no data")
        void percentilesReturnZeroWhenNoData() {
            assertEquals(0, metrics.getGetLatencyP50());
            assertEquals(0, metrics.getGetLatencyP95());
            assertEquals(0, metrics.getGetLatencyP99());
        }

        @Test
        @DisplayName("percentiles return values when data exists")
        void percentilesReturnValuesWhenDataExists() {
            // Record various latencies
            for (int i = 0; i < 100; i++) {
                metrics.recordGetLatency(i * 1000);
            }

            assertTrue(metrics.getGetLatencyP50() > 0);
            assertTrue(metrics.getGetLatencyP95() > 0);
            assertTrue(metrics.getGetLatencyP99() > 0);
            assertTrue(metrics.getGetLatencyP99() >= metrics.getGetLatencyP95());
            assertTrue(metrics.getGetLatencyP95() >= metrics.getGetLatencyP50());
        }
    }

    @Nested
    @DisplayName("Put Latency Percentile Tests")
    class PutLatencyPercentileTests {

        @Test
        @DisplayName("percentiles return 0 when no data")
        void percentilesReturnZeroWhenNoData() {
            assertEquals(0, metrics.getPutLatencyP50());
            assertEquals(0, metrics.getPutLatencyP95());
            assertEquals(0, metrics.getPutLatencyP99());
        }

        @Test
        @DisplayName("percentiles return values when data exists")
        void percentilesReturnValuesWhenDataExists() {
            for (int i = 0; i < 100; i++) {
                metrics.recordPutLatency(i * 1000);
            }

            assertTrue(metrics.getPutLatencyP50() > 0);
            assertTrue(metrics.getPutLatencyP95() > 0);
            assertTrue(metrics.getPutLatencyP99() > 0);
        }
    }

    @Nested
    @DisplayName("Load Latency Percentile Tests")
    class LoadLatencyPercentileTests {

        @Test
        @DisplayName("percentiles return 0 when no data")
        void percentilesReturnZeroWhenNoData() {
            assertEquals(0, metrics.getLoadLatencyP50());
            assertEquals(0, metrics.getLoadLatencyP95());
            assertEquals(0, metrics.getLoadLatencyP99());
        }

        @Test
        @DisplayName("percentiles return values when data exists")
        void percentilesReturnValuesWhenDataExists() {
            for (int i = 0; i < 100; i++) {
                metrics.recordLoadLatency(i * 1000);
            }

            assertTrue(metrics.getLoadLatencyP50() > 0);
            assertTrue(metrics.getLoadLatencyP95() > 0);
            assertTrue(metrics.getLoadLatencyP99() > 0);
        }
    }

    @Nested
    @DisplayName("Average Latency Tests")
    class AverageLatencyTests {

        @Test
        @DisplayName("average returns 0 when no data")
        void averageReturnsZeroWhenNoData() {
            assertEquals(0.0, metrics.getAverageGetLatency());
            assertEquals(0.0, metrics.getAveragePutLatency());
            assertEquals(0.0, metrics.getAverageLoadLatency());
        }

        @Test
        @DisplayName("average calculation is correct")
        void averageCalculationIsCorrect() {
            metrics.recordGetLatency(100);
            metrics.recordGetLatency(200);
            metrics.recordGetLatency(300);

            assertEquals(200.0, metrics.getAverageGetLatency());
        }
    }

    @Nested
    @DisplayName("Min/Max Latency Tests")
    class MinMaxLatencyTests {

        @Test
        @DisplayName("min returns 0 when no data")
        void minReturnsZeroWhenNoData() {
            assertEquals(0, metrics.getMinGetLatency());
        }

        @Test
        @DisplayName("max returns 0 when no data")
        void maxReturnsZeroWhenNoData() {
            assertEquals(0, metrics.getMaxGetLatency());
        }

        @Test
        @DisplayName("min tracks minimum value")
        void minTracksMinimumValue() {
            metrics.recordGetLatency(5000);
            metrics.recordGetLatency(1000);
            metrics.recordGetLatency(3000);

            assertEquals(1000, metrics.getMinGetLatency());
        }

        @Test
        @DisplayName("max tracks maximum value")
        void maxTracksMaximumValue() {
            metrics.recordGetLatency(1000);
            metrics.recordGetLatency(5000);
            metrics.recordGetLatency(3000);

            assertEquals(5000, metrics.getMaxGetLatency());
        }
    }

    @Nested
    @DisplayName("Throughput Tests")
    class ThroughputTests {

        @Test
        @DisplayName("getThroughput returns 0 initially")
        void getThroughputReturnsZeroInitially() {
            assertEquals(0.0, metrics.getGetThroughput());
        }

        @Test
        @DisplayName("putThroughput returns 0 initially")
        void putThroughputReturnsZeroInitially() {
            assertEquals(0.0, metrics.getPutThroughput());
        }

        @Test
        @DisplayName("throughput increases with operations")
        void throughputIncreasesWithOperations() throws InterruptedException {
            for (int i = 0; i < 100; i++) {
                metrics.recordGetLatency(1000);
            }

            // Give some time to pass for throughput calculation
            Thread.sleep(10);

            assertTrue(metrics.getGetThroughput() > 0);
        }
    }

    @Nested
    @DisplayName("Uptime Tests")
    class UptimeTests {

        @Test
        @DisplayName("uptime returns positive duration")
        void uptimeReturnsPositiveDuration() throws InterruptedException {
            Thread.sleep(10);
            Duration uptime = metrics.getUptime();
            assertNotNull(uptime);
            assertTrue(uptime.toMillis() >= 10);
        }
    }

    @Nested
    @DisplayName("Snapshot Tests")
    class SnapshotTests {

        @Test
        @DisplayName("snapshot captures current state")
        void snapshotCapturesCurrentState() {
            metrics.recordGetLatency(1000);
            metrics.recordGetLatency(2000);
            metrics.recordPutLatency(500);
            metrics.recordLoadLatency(3000);
            metrics.recordEviction();

            CacheMetrics.MetricsSnapshot snapshot = metrics.snapshot();

            assertEquals(2, snapshot.getCount());
            assertEquals(1, snapshot.putCount());
            assertEquals(1, snapshot.loadCount());
            assertEquals(1, snapshot.evictionCount());
        }

        @Test
        @DisplayName("snapshot is immutable")
        void snapshotIsImmutable() {
            metrics.recordGetLatency(1000);
            CacheMetrics.MetricsSnapshot snapshot = metrics.snapshot();

            // Record more after snapshot
            metrics.recordGetLatency(2000);
            metrics.recordGetLatency(3000);

            // Snapshot should still have old value
            assertEquals(1, snapshot.getCount());
            // But metrics should have new value
            assertEquals(3, metrics.getGetCount());
        }

        @Test
        @DisplayName("snapshot format method")
        void snapshotFormatMethod() {
            metrics.recordGetLatency(1000);
            metrics.recordPutLatency(500);

            CacheMetrics.MetricsSnapshot snapshot = metrics.snapshot();
            String formatted = snapshot.format();

            assertNotNull(formatted);
            assertTrue(formatted.contains("Cache Metrics"));
            assertTrue(formatted.contains("Operations"));
            assertTrue(formatted.contains("Latency"));
            assertTrue(formatted.contains("Throughput"));
        }
    }

    @Nested
    @DisplayName("Reset Tests")
    class ResetTests {

        @Test
        @DisplayName("reset clears all counters")
        void resetClearsAllCounters() {
            metrics.recordGetLatency(1000);
            metrics.recordPutLatency(500);
            metrics.recordLoadLatency(2000);
            metrics.recordEviction();

            metrics.reset();

            assertEquals(0, metrics.getGetCount());
            assertEquals(0, metrics.getPutCount());
            assertEquals(0, metrics.getLoadCount());
            assertEquals(0, metrics.getEvictionCount());
        }

        @Test
        @DisplayName("reset clears min/max")
        void resetClearsMinMax() {
            metrics.recordGetLatency(1000);
            metrics.recordGetLatency(5000);

            metrics.reset();

            assertEquals(0, metrics.getMinGetLatency());
            assertEquals(0, metrics.getMaxGetLatency());
        }

        @Test
        @DisplayName("reset clears percentiles")
        void resetClearsPercentiles() {
            for (int i = 0; i < 100; i++) {
                metrics.recordGetLatency(i * 1000);
            }

            metrics.reset();

            assertEquals(0, metrics.getGetLatencyP50());
            assertEquals(0, metrics.getGetLatencyP95());
            assertEquals(0, metrics.getGetLatencyP99());
        }
    }

    @Nested
    @DisplayName("MetricsSnapshot Record Tests")
    class MetricsSnapshotRecordTests {

        @Test
        @DisplayName("snapshot record accessors work")
        void snapshotRecordAccessorsWork() {
            CacheMetrics.MetricsSnapshot snapshot = new CacheMetrics.MetricsSnapshot(
                    100, 50, 25, 10,
                    1000, 5000, 10000, 2000.0, 500, 20000,
                    800, 4000, 8000, 1500.0,
                    2000, 8000, 15000, 5000.0,
                    1000.0, 500.0, Duration.ofMinutes(5)
            );

            assertEquals(100, snapshot.getCount());
            assertEquals(50, snapshot.putCount());
            assertEquals(25, snapshot.loadCount());
            assertEquals(10, snapshot.evictionCount());
            assertEquals(1000, snapshot.getLatencyP50());
            assertEquals(5000, snapshot.getLatencyP95());
            assertEquals(10000, snapshot.getLatencyP99());
            assertEquals(2000.0, snapshot.avgGetLatency());
            assertEquals(500, snapshot.minGetLatency());
            assertEquals(20000, snapshot.maxGetLatency());
            assertEquals(800, snapshot.putLatencyP50());
            assertEquals(4000, snapshot.putLatencyP95());
            assertEquals(8000, snapshot.putLatencyP99());
            assertEquals(1500.0, snapshot.avgPutLatency());
            assertEquals(2000, snapshot.loadLatencyP50());
            assertEquals(8000, snapshot.loadLatencyP95());
            assertEquals(15000, snapshot.loadLatencyP99());
            assertEquals(5000.0, snapshot.avgLoadLatency());
            assertEquals(1000.0, snapshot.getThroughput());
            assertEquals(500.0, snapshot.putThroughput());
            assertEquals(Duration.ofMinutes(5), snapshot.uptime());
        }
    }

    @Nested
    @DisplayName("Concurrent Recording Tests")
    class ConcurrentRecordingTests {

        @Test
        @DisplayName("concurrent recording is thread-safe")
        void concurrentRecordingIsThreadSafe() throws InterruptedException {
            int threadCount = 10;
            int operationsPerThread = 1000;
            Thread[] threads = new Thread[threadCount];

            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < operationsPerThread; j++) {
                        metrics.recordGetLatency(1000);
                        metrics.recordPutLatency(500);
                        metrics.recordEviction();
                    }
                });
            }

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            assertEquals(threadCount * operationsPerThread, metrics.getGetCount());
            assertEquals(threadCount * operationsPerThread, metrics.getPutCount());
            assertEquals(threadCount * operationsPerThread, metrics.getEvictionCount());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("handles very large latency values")
        void handlesVeryLargeLatencyValues() {
            metrics.recordGetLatency(Long.MAX_VALUE / 2);
            assertEquals(1, metrics.getGetCount());
            assertTrue(metrics.getMaxGetLatency() > 0);
        }

        @Test
        @DisplayName("handles zero latency")
        void handlesZeroLatency() {
            metrics.recordGetLatency(0);
            assertEquals(1, metrics.getGetCount());
            assertEquals(0, metrics.getMinGetLatency());
        }

        @Test
        @DisplayName("handles single operation")
        void handlesSingleOperation() {
            metrics.recordGetLatency(1000);

            assertEquals(1, metrics.getGetCount());
            assertEquals(1000, metrics.getMinGetLatency());
            assertEquals(1000, metrics.getMaxGetLatency());
            assertEquals(1000.0, metrics.getAverageGetLatency());
        }
    }
}
