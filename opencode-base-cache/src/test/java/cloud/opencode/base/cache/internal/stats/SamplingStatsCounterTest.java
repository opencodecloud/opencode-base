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

package cloud.opencode.base.cache.internal.stats;

import cloud.opencode.base.cache.CacheStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for SamplingStatsCounter
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("SamplingStatsCounter Tests")
class SamplingStatsCounterTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("withRate creates counter with specified rate")
        void withRateCreatesCounterWithSpecifiedRate() {
            SamplingStatsCounter counter = SamplingStatsCounter.withRate(0.5);
            assertNotNull(counter);
            assertEquals(0.5, counter.getSampleRate());
        }

        @Test
        @DisplayName("withRate throws on rate <= 0")
        void withRateThrowsOnRateLteZero() {
            assertThrows(IllegalArgumentException.class, () -> SamplingStatsCounter.withRate(0));
            assertThrows(IllegalArgumentException.class, () -> SamplingStatsCounter.withRate(-0.1));
        }

        @Test
        @DisplayName("withRate throws on rate > 1")
        void withRateThrowsOnRateGtOne() {
            assertThrows(IllegalArgumentException.class, () -> SamplingStatsCounter.withRate(1.1));
        }

        @Test
        @DisplayName("withRate accepts rate of 1.0")
        void withRateAcceptsRateOfOne() {
            SamplingStatsCounter counter = SamplingStatsCounter.withRate(1.0);
            assertEquals(1.0, counter.getSampleRate());
        }

        @Test
        @DisplayName("highThroughput creates 1% sampling counter")
        void highThroughputCreatesOnePercentCounter() {
            SamplingStatsCounter counter = SamplingStatsCounter.highThroughput();
            assertEquals(0.01, counter.getSampleRate());
        }

        @Test
        @DisplayName("balanced creates 10% sampling counter")
        void balancedCreatesTenPercentCounter() {
            SamplingStatsCounter counter = SamplingStatsCounter.balanced();
            assertEquals(0.1, counter.getSampleRate());
        }
    }

    @Nested
    @DisplayName("Recording Tests with 100% Sampling")
    class RecordingWithFullSamplingTests {

        private SamplingStatsCounter counter;

        @BeforeEach
        void setUp() {
            counter = SamplingStatsCounter.withRate(1.0);
        }

        @Test
        @DisplayName("recordHits records hits")
        void recordHitsRecordsHits() {
            counter.recordHits(5);
            counter.recordHits(3);

            CacheStats stats = counter.snapshot();
            assertEquals(8, stats.hitCount());
        }

        @Test
        @DisplayName("recordMisses records misses")
        void recordMissesRecordsMisses() {
            counter.recordMisses(3);
            counter.recordMisses(2);

            CacheStats stats = counter.snapshot();
            assertEquals(5, stats.missCount());
        }

        @Test
        @DisplayName("recordLoadSuccess records load success")
        void recordLoadSuccessRecordsLoadSuccess() {
            counter.recordLoadSuccess(1000);
            counter.recordLoadSuccess(2000);

            CacheStats stats = counter.snapshot();
            assertEquals(2, stats.loadCount());
            assertEquals(3000, stats.totalLoadTime());
        }

        @Test
        @DisplayName("recordLoadFailure records load failure")
        void recordLoadFailureRecordsLoadFailure() {
            counter.recordLoadFailure(500);
            counter.recordLoadFailure(600);

            CacheStats stats = counter.snapshot();
            assertEquals(2, stats.loadFailureCount());
            assertEquals(1100, stats.totalLoadTime());
        }

        @Test
        @DisplayName("recordEviction records eviction")
        void recordEvictionRecordsEviction() {
            counter.recordEviction(10);
            counter.recordEviction(20);

            CacheStats stats = counter.snapshot();
            assertEquals(2, stats.evictionCount());
            assertEquals(30, stats.evictionWeight());
        }
    }

    @Nested
    @DisplayName("Sampling Behavior Tests")
    class SamplingBehaviorTests {

        @Test
        @DisplayName("100% sampling records all hits")
        void fullSamplingRecordsAllHits() {
            SamplingStatsCounter counter = SamplingStatsCounter.withRate(1.0);

            for (int i = 0; i < 1000; i++) {
                counter.recordHits(1);
            }

            CacheStats stats = counter.snapshot();
            assertEquals(1000, stats.hitCount());
        }

        @Test
        @DisplayName("sampling extrapolates hit count")
        void samplingExtrapolatesHitCount() {
            // With 10% sampling, if we record 100 hits, some will be sampled
            // and then extrapolated by 10x
            SamplingStatsCounter counter = SamplingStatsCounter.withRate(0.1);

            for (int i = 0; i < 10000; i++) {
                counter.recordHits(1);
            }

            CacheStats stats = counter.snapshot();
            // Due to randomness, we can't expect exact count, but it should be
            // within a reasonable range of the expected value
            long hitCount = stats.hitCount();
            // Allow for statistical variance
            assertTrue(hitCount > 5000 && hitCount < 15000,
                    "Hit count " + hitCount + " should be roughly around 10000");
        }

        @Test
        @DisplayName("load operations are always recorded")
        void loadOperationsAreAlwaysRecorded() {
            // Even with low sampling, load operations should always be recorded
            SamplingStatsCounter counter = SamplingStatsCounter.withRate(0.01);

            for (int i = 0; i < 100; i++) {
                counter.recordLoadSuccess(1000);
            }

            CacheStats stats = counter.snapshot();
            assertEquals(100, stats.loadCount());
        }

        @Test
        @DisplayName("evictions are always recorded")
        void evictionsAreAlwaysRecorded() {
            SamplingStatsCounter counter = SamplingStatsCounter.withRate(0.01);

            for (int i = 0; i < 100; i++) {
                counter.recordEviction(10);
            }

            CacheStats stats = counter.snapshot();
            assertEquals(100, stats.evictionCount());
        }
    }

    @Nested
    @DisplayName("Snapshot Tests")
    class SnapshotTests {

        @Test
        @DisplayName("snapshot returns cache stats")
        void snapshotReturnsCacheStats() {
            SamplingStatsCounter counter = SamplingStatsCounter.withRate(1.0);
            counter.recordHits(10);
            counter.recordMisses(5);
            counter.recordLoadSuccess(1000);
            counter.recordEviction(20);

            CacheStats stats = counter.snapshot();

            assertEquals(10, stats.hitCount());
            assertEquals(5, stats.missCount());
            assertEquals(1, stats.loadCount());
            assertEquals(1000, stats.totalLoadTime());
            assertEquals(1, stats.evictionCount());
            assertEquals(20, stats.evictionWeight());
        }

        @Test
        @DisplayName("rawSnapshot returns non-extrapolated stats")
        void rawSnapshotReturnsNonExtrapolatedStats() {
            SamplingStatsCounter counter = SamplingStatsCounter.withRate(0.5);

            // Record some data
            for (int i = 0; i < 100; i++) {
                counter.recordHits(1);
            }

            CacheStats raw = counter.rawSnapshot();
            CacheStats extrapolated = counter.snapshot();

            // Raw should be less than or equal to extrapolated (since extrapolation multiplies)
            assertTrue(raw.hitCount() <= extrapolated.hitCount());
        }
    }

    @Nested
    @DisplayName("Sample Count Tests")
    class SampleCountTests {

        @Test
        @DisplayName("getHitSampleCount returns sample count")
        void getHitSampleCountReturnsSampleCount() {
            SamplingStatsCounter counter = SamplingStatsCounter.withRate(1.0);

            counter.recordHits(5);
            counter.recordHits(3);

            assertEquals(2, counter.getHitSampleCount());
        }

        @Test
        @DisplayName("getMissSampleCount returns sample count")
        void getMissSampleCountReturnsSampleCount() {
            SamplingStatsCounter counter = SamplingStatsCounter.withRate(1.0);

            counter.recordMisses(5);
            counter.recordMisses(3);
            counter.recordMisses(2);

            assertEquals(3, counter.getMissSampleCount());
        }
    }

    @Nested
    @DisplayName("Reset Tests")
    class ResetTests {

        @Test
        @DisplayName("reset clears all counters")
        void resetClearsAllCounters() {
            SamplingStatsCounter counter = SamplingStatsCounter.withRate(1.0);

            counter.recordHits(10);
            counter.recordMisses(5);
            counter.recordLoadSuccess(1000);
            counter.recordLoadFailure(500);
            counter.recordEviction(20);

            counter.reset();

            CacheStats stats = counter.snapshot();
            assertEquals(0, stats.hitCount());
            assertEquals(0, stats.missCount());
            assertEquals(0, stats.loadCount());
            assertEquals(0, stats.loadFailureCount());
            assertEquals(0, stats.evictionCount());
            assertEquals(0, counter.getHitSampleCount());
            assertEquals(0, counter.getMissSampleCount());
        }
    }

    @Nested
    @DisplayName("SamplingStats Tests")
    class SamplingStatsTests {

        @Test
        @DisplayName("getSamplingStats returns stats")
        void getSamplingStatsReturnsStats() {
            SamplingStatsCounter counter = SamplingStatsCounter.withRate(1.0);
            counter.recordHits(10);
            counter.recordMisses(5);

            SamplingStatsCounter.SamplingStats stats = counter.getSamplingStats();

            assertEquals(1.0, stats.sampleRate());
            assertTrue(stats.hitSamples() > 0);
            assertTrue(stats.missSamples() > 0);
        }

        @Test
        @DisplayName("estimatedErrorMargin returns error margin")
        void estimatedErrorMarginReturnsErrorMargin() {
            SamplingStatsCounter.SamplingStats stats = new SamplingStatsCounter.SamplingStats(
                    0.1, 100, 50, 1000, 500
            );

            double margin = stats.estimatedErrorMargin();
            // With 150 total samples, error margin should be around 1/sqrt(150) ≈ 0.082
            assertTrue(margin > 0 && margin < 1);
        }

        @Test
        @DisplayName("estimatedErrorMargin returns 1.0 when no data")
        void estimatedErrorMarginReturnsOneWhenNoData() {
            SamplingStatsCounter.SamplingStats stats = new SamplingStatsCounter.SamplingStats(
                    0.1, 0, 0, 0, 0
            );

            assertEquals(1.0, stats.estimatedErrorMargin());
        }

        @Test
        @DisplayName("sampling stats record accessors work")
        void samplingStatsRecordAccessorsWork() {
            SamplingStatsCounter.SamplingStats stats = new SamplingStatsCounter.SamplingStats(
                    0.1, 100, 50, 1000, 500
            );

            assertEquals(0.1, stats.sampleRate());
            assertEquals(100, stats.hitSamples());
            assertEquals(50, stats.missSamples());
            assertEquals(1000, stats.rawHitCount());
            assertEquals(500, stats.rawMissCount());
        }
    }

    @Nested
    @DisplayName("Concurrent Recording Tests")
    class ConcurrentRecordingTests {

        @Test
        @DisplayName("concurrent recording is thread-safe")
        void concurrentRecordingIsThreadSafe() throws InterruptedException {
            SamplingStatsCounter counter = SamplingStatsCounter.withRate(1.0);
            int threadCount = 10;
            int operationsPerThread = 1000;
            Thread[] threads = new Thread[threadCount];

            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < operationsPerThread; j++) {
                        counter.recordHits(1);
                        counter.recordMisses(1);
                    }
                });
            }

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            CacheStats stats = counter.snapshot();
            assertEquals(threadCount * operationsPerThread, stats.hitCount());
            assertEquals(threadCount * operationsPerThread, stats.missCount());
        }
    }
}
