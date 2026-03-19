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

package cloud.opencode.base.cache.distributed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for DistributedCacheStats
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("DistributedCacheStats Tests")
class DistributedCacheStatsTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("empty creates empty stats")
        void emptyCreatesEmptyStats() {
            DistributedCacheStats stats = DistributedCacheStats.empty();

            assertEquals(0, stats.hitCount());
            assertEquals(0, stats.missCount());
            assertEquals(0, stats.loadCount());
            assertEquals(0, stats.evictionCount());
            assertEquals(0, stats.keyCount());
        }

        @Test
        @DisplayName("builder creates stats")
        void builderCreatesStats() {
            DistributedCacheStats stats = DistributedCacheStats.builder()
                    .hitCount(100)
                    .missCount(20)
                    .build();

            assertEquals(100, stats.hitCount());
            assertEquals(20, stats.missCount());
        }
    }

    @Nested
    @DisplayName("Rate Calculation Tests")
    class RateCalculationTests {

        @Test
        @DisplayName("hitRate calculates correctly")
        void hitRateCalculatesCorrectly() {
            DistributedCacheStats stats = DistributedCacheStats.builder()
                    .hitCount(80)
                    .missCount(20)
                    .build();

            assertEquals(0.8, stats.hitRate(), 0.001);
        }

        @Test
        @DisplayName("hitRate returns 0 for no requests")
        void hitRateReturnsZeroForNoRequests() {
            DistributedCacheStats stats = DistributedCacheStats.empty();

            assertEquals(0.0, stats.hitRate());
        }

        @Test
        @DisplayName("missRate calculates correctly")
        void missRateCalculatesCorrectly() {
            DistributedCacheStats stats = DistributedCacheStats.builder()
                    .hitCount(80)
                    .missCount(20)
                    .build();

            assertEquals(0.2, stats.missRate(), 0.001);
        }

        @Test
        @DisplayName("missRate returns 1.0 for no requests")
        void missRateReturnsOneForNoRequests() {
            DistributedCacheStats stats = DistributedCacheStats.empty();

            // 1.0 - 0.0 = 1.0
            assertEquals(1.0, stats.missRate());
        }

        @Test
        @DisplayName("loadSuccessRate calculates correctly")
        void loadSuccessRateCalculatesCorrectly() {
            DistributedCacheStats stats = DistributedCacheStats.builder()
                    .loadCount(100)
                    .loadSuccessCount(90)
                    .loadFailureCount(10)
                    .build();

            assertEquals(0.9, stats.loadSuccessRate(), 0.001);
        }

        @Test
        @DisplayName("loadSuccessRate returns 0 for no loads")
        void loadSuccessRateReturnsZeroForNoLoads() {
            DistributedCacheStats stats = DistributedCacheStats.empty();

            assertEquals(0.0, stats.loadSuccessRate());
        }
    }

    @Nested
    @DisplayName("Average Load Time Tests")
    class AverageLoadTimeTests {

        @Test
        @DisplayName("averageLoadTime calculates correctly")
        void averageLoadTimeCalculatesCorrectly() {
            DistributedCacheStats stats = DistributedCacheStats.builder()
                    .loadCount(10)
                    .totalLoadTime(Duration.ofMillis(1000))
                    .build();

            assertEquals(Duration.ofMillis(100), stats.averageLoadTime());
        }

        @Test
        @DisplayName("averageLoadTime returns zero for no loads")
        void averageLoadTimeReturnsZeroForNoLoads() {
            DistributedCacheStats stats = DistributedCacheStats.empty();

            assertEquals(Duration.ZERO, stats.averageLoadTime());
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("builder sets hitCount")
        void builderSetsHitCount() {
            DistributedCacheStats stats = DistributedCacheStats.builder()
                    .hitCount(100)
                    .build();

            assertEquals(100, stats.hitCount());
        }

        @Test
        @DisplayName("builder sets missCount")
        void builderSetsMissCount() {
            DistributedCacheStats stats = DistributedCacheStats.builder()
                    .missCount(50)
                    .build();

            assertEquals(50, stats.missCount());
        }

        @Test
        @DisplayName("builder sets loadCount")
        void builderSetsLoadCount() {
            DistributedCacheStats stats = DistributedCacheStats.builder()
                    .loadCount(30)
                    .build();

            assertEquals(30, stats.loadCount());
        }

        @Test
        @DisplayName("builder sets loadSuccessCount")
        void builderSetsLoadSuccessCount() {
            DistributedCacheStats stats = DistributedCacheStats.builder()
                    .loadSuccessCount(25)
                    .build();

            assertEquals(25, stats.loadSuccessCount());
        }

        @Test
        @DisplayName("builder sets loadFailureCount")
        void builderSetsLoadFailureCount() {
            DistributedCacheStats stats = DistributedCacheStats.builder()
                    .loadFailureCount(5)
                    .build();

            assertEquals(5, stats.loadFailureCount());
        }

        @Test
        @DisplayName("builder sets totalLoadTime")
        void builderSetsTotalLoadTime() {
            DistributedCacheStats stats = DistributedCacheStats.builder()
                    .totalLoadTime(Duration.ofSeconds(10))
                    .build();

            assertEquals(Duration.ofSeconds(10), stats.totalLoadTime());
        }

        @Test
        @DisplayName("builder sets evictionCount")
        void builderSetsEvictionCount() {
            DistributedCacheStats stats = DistributedCacheStats.builder()
                    .evictionCount(15)
                    .build();

            assertEquals(15, stats.evictionCount());
        }

        @Test
        @DisplayName("builder sets requestCount")
        void builderSetsRequestCount() {
            DistributedCacheStats stats = DistributedCacheStats.builder()
                    .requestCount(1000)
                    .build();

            assertEquals(1000, stats.requestCount());
        }

        @Test
        @DisplayName("builder sets connectionCount")
        void builderSetsConnectionCount() {
            DistributedCacheStats stats = DistributedCacheStats.builder()
                    .connectionCount(5)
                    .build();

            assertEquals(5, stats.connectionCount());
        }

        @Test
        @DisplayName("builder sets memoryUsed")
        void builderSetsMemoryUsed() {
            DistributedCacheStats stats = DistributedCacheStats.builder()
                    .memoryUsed(1024 * 1024)
                    .build();

            assertEquals(1024 * 1024, stats.memoryUsed());
        }

        @Test
        @DisplayName("builder sets keyCount")
        void builderSetsKeyCount() {
            DistributedCacheStats stats = DistributedCacheStats.builder()
                    .keyCount(500)
                    .build();

            assertEquals(500, stats.keyCount());
        }

        @Test
        @DisplayName("builder sets avgLatency")
        void builderSetsAvgLatency() {
            DistributedCacheStats stats = DistributedCacheStats.builder()
                    .avgLatency(Duration.ofNanos(500_000))
                    .build();

            assertEquals(Duration.ofNanos(500_000), stats.avgLatency());
        }

        @Test
        @DisplayName("builder sets p99Latency")
        void builderSetsP99Latency() {
            DistributedCacheStats stats = DistributedCacheStats.builder()
                    .p99Latency(Duration.ofMillis(5))
                    .build();

            assertEquals(Duration.ofMillis(5), stats.p99Latency());
        }

        @Test
        @DisplayName("builder sets lastResetTime")
        void builderSetsLastResetTime() {
            Instant resetTime = Instant.now();
            DistributedCacheStats stats = DistributedCacheStats.builder()
                    .lastResetTime(resetTime)
                    .build();

            assertEquals(resetTime, stats.lastResetTime());
        }
    }

    @Nested
    @DisplayName("Record Tests")
    class RecordTests {

        @Test
        @DisplayName("record equality works")
        void recordEqualityWorks() {
            Instant time = Instant.now();
            DistributedCacheStats stats1 = DistributedCacheStats.builder()
                    .hitCount(100)
                    .missCount(20)
                    .lastResetTime(time)
                    .build();
            DistributedCacheStats stats2 = DistributedCacheStats.builder()
                    .hitCount(100)
                    .missCount(20)
                    .lastResetTime(time)
                    .build();

            assertEquals(stats1, stats2);
            assertEquals(stats1.hashCode(), stats2.hashCode());
        }

        @Test
        @DisplayName("record accessors work")
        void recordAccessorsWork() {
            Instant time = Instant.now();
            DistributedCacheStats stats = new DistributedCacheStats(
                    100, 20, 30, 25, 5,
                    Duration.ofSeconds(10), 15, 1000, 5,
                    1024 * 1024, 500,
                    Duration.ofNanos(500_000), Duration.ofMillis(5),
                    time
            );

            assertEquals(100, stats.hitCount());
            assertEquals(20, stats.missCount());
            assertEquals(30, stats.loadCount());
            assertEquals(25, stats.loadSuccessCount());
            assertEquals(5, stats.loadFailureCount());
            assertEquals(Duration.ofSeconds(10), stats.totalLoadTime());
            assertEquals(15, stats.evictionCount());
            assertEquals(1000, stats.requestCount());
            assertEquals(5, stats.connectionCount());
            assertEquals(1024 * 1024, stats.memoryUsed());
            assertEquals(500, stats.keyCount());
            assertEquals(Duration.ofNanos(500_000), stats.avgLatency());
            assertEquals(Duration.ofMillis(5), stats.p99Latency());
            assertEquals(time, stats.lastResetTime());
        }

        @Test
        @DisplayName("record toString works")
        void recordToStringWorks() {
            DistributedCacheStats stats = DistributedCacheStats.builder()
                    .hitCount(100)
                    .build();

            String str = stats.toString();
            assertTrue(str.contains("100"));
        }
    }
}
