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

package cloud.opencode.base.cache.spring;

import cloud.opencode.base.cache.Cache;
import cloud.opencode.base.cache.CacheManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for CacheHealthIndicator
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("CacheHealthIndicator Tests")
class CacheHealthIndicatorTest {

    private CacheManager cacheManager;
    private CacheHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        cacheManager = CacheManager.getInstance();
        cacheManager.reset(); // Clear any previous state
        cacheManager.getOrCreateCache("test-cache", config -> config
                .maximumSize(100)
                .recordStats());
        healthIndicator = new CacheHealthIndicator(cacheManager);
    }

    @AfterEach
    void tearDown() {
        cacheManager.reset();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("constructor creates indicator with cache manager")
        void constructorCreatesIndicatorWithCacheManager() {
            CacheHealthIndicator indicator = new CacheHealthIndicator(cacheManager);

            assertNotNull(indicator);
        }
    }

    @Nested
    @DisplayName("Threshold Configuration Tests")
    class ThresholdConfigurationTests {

        @Test
        @DisplayName("hitRateThreshold sets threshold")
        void hitRateThresholdSetsThreshold() {
            CacheHealthIndicator result = healthIndicator.hitRateThreshold(0.5);

            assertSame(healthIndicator, result);
        }

        @Test
        @DisplayName("hitRateThreshold throws for invalid threshold")
        void hitRateThresholdThrowsForInvalidThreshold() {
            assertThrows(IllegalArgumentException.class, () ->
                    healthIndicator.hitRateThreshold(-0.1));
            assertThrows(IllegalArgumentException.class, () ->
                    healthIndicator.hitRateThreshold(1.1));
        }

        @Test
        @DisplayName("evictionRateThreshold sets threshold")
        void evictionRateThresholdSetsThreshold() {
            CacheHealthIndicator result = healthIndicator.evictionRateThreshold(0.4);

            assertSame(healthIndicator, result);
        }

        @Test
        @DisplayName("evictionRateThreshold throws for invalid threshold")
        void evictionRateThresholdThrowsForInvalidThreshold() {
            assertThrows(IllegalArgumentException.class, () ->
                    healthIndicator.evictionRateThreshold(-0.1));
            assertThrows(IllegalArgumentException.class, () ->
                    healthIndicator.evictionRateThreshold(1.1));
        }

        @Test
        @DisplayName("maxSizeWarningThreshold sets threshold")
        void maxSizeWarningThresholdSetsThreshold() {
            CacheHealthIndicator result = healthIndicator.maxSizeWarningThreshold(50000);

            assertSame(healthIndicator, result);
        }

        @Test
        @DisplayName("fluent configuration chain works")
        void fluentConfigurationChainWorks() {
            CacheHealthIndicator result = healthIndicator
                    .hitRateThreshold(0.5)
                    .evictionRateThreshold(0.3)
                    .maxSizeWarningThreshold(10000);

            assertSame(healthIndicator, result);
        }
    }

    @Nested
    @DisplayName("Health Check Tests")
    class HealthCheckTests {

        @Test
        @DisplayName("health returns UP for healthy caches")
        void healthReturnsUpForHealthyCaches() {
            // Populate cache with some data
            Cache<String, String> cache = cacheManager.<String, String>getCache("test-cache").orElseThrow();
            for (int i = 0; i < 10; i++) {
                cache.put("key" + i, "value" + i);
            }

            CacheHealthIndicator.HealthResult result = healthIndicator.health();

            assertEquals(CacheHealthIndicator.Status.UP, result.status());
            assertEquals(1, result.cacheCount());
            assertTrue(result.isHealthy());
        }

        @Test
        @DisplayName("health returns correct total entries")
        void healthReturnsCorrectTotalEntries() {
            Cache<String, String> cache = cacheManager.<String, String>getCache("test-cache").orElseThrow();
            for (int i = 0; i < 50; i++) {
                cache.put("key" + i, "value" + i);
            }

            CacheHealthIndicator.HealthResult result = healthIndicator.health();

            assertEquals(50, result.totalEntries());
        }

        @Test
        @DisplayName("health calculates overall hit rate")
        void healthCalculatesOverallHitRate() {
            Cache<String, String> cache = cacheManager.<String, String>getCache("test-cache").orElseThrow();

            // Generate some hits and misses
            cache.put("key1", "value1");
            cache.get("key1"); // hit
            cache.get("key1"); // hit
            cache.get("key1"); // hit
            cache.get("key1"); // hit
            cache.get("missing"); // miss

            CacheHealthIndicator.HealthResult result = healthIndicator.health();

            // 4 hits, 1 miss = 0.8 hit rate
            assertEquals(0.8, result.overallHitRate(), 0.01);
        }

        @Test
        @DisplayName("health returns UP for empty cache manager")
        void healthReturnsUpForEmptyCacheManager() {
            cacheManager.reset(); // Make it empty
            CacheHealthIndicator indicator = new CacheHealthIndicator(cacheManager);

            CacheHealthIndicator.HealthResult result = indicator.health();

            assertEquals(CacheHealthIndicator.Status.UP, result.status());
            assertEquals(0, result.cacheCount());
            assertEquals(1.0, result.overallHitRate()); // No requests = 100% hit rate
        }

        @Test
        @DisplayName("health returns DEGRADED for low hit rate")
        void healthReturnsDegradedForLowHitRate() {
            Cache<String, String> cache = cacheManager.<String, String>getCache("test-cache").orElseThrow();

            // Generate many misses to trigger low hit rate
            for (int i = 0; i < 150; i++) {
                cache.get("missing" + i); // all misses
            }

            healthIndicator.hitRateThreshold(0.5);
            CacheHealthIndicator.HealthResult result = healthIndicator.health();

            assertEquals(CacheHealthIndicator.Status.DEGRADED, result.status());
            assertFalse(result.isHealthy());
        }

        @Test
        @DisplayName("health includes cache details")
        void healthIncludesCacheDetails() {
            Cache<String, String> cache = cacheManager.<String, String>getCache("test-cache").orElseThrow();
            cache.put("key1", "value1");

            CacheHealthIndicator.HealthResult result = healthIndicator.health();

            assertTrue(result.caches().containsKey("test-cache"));
            CacheHealthIndicator.CacheHealth cacheHealth = result.caches().get("test-cache");
            assertNotNull(cacheHealth);
            assertEquals(CacheHealthIndicator.Status.UP, cacheHealth.status());
        }
    }

    @Nested
    @DisplayName("Status Enum Tests")
    class StatusEnumTests {

        @Test
        @DisplayName("Status enum has all expected values")
        void statusEnumHasAllExpectedValues() {
            assertEquals(4, CacheHealthIndicator.Status.values().length);
            assertNotNull(CacheHealthIndicator.Status.UP);
            assertNotNull(CacheHealthIndicator.Status.DEGRADED);
            assertNotNull(CacheHealthIndicator.Status.DOWN);
            assertNotNull(CacheHealthIndicator.Status.UNKNOWN);
        }

        @Test
        @DisplayName("Status valueOf works")
        void statusValueOfWorks() {
            assertEquals(CacheHealthIndicator.Status.UP, CacheHealthIndicator.Status.valueOf("UP"));
            assertEquals(CacheHealthIndicator.Status.DEGRADED, CacheHealthIndicator.Status.valueOf("DEGRADED"));
            assertEquals(CacheHealthIndicator.Status.DOWN, CacheHealthIndicator.Status.valueOf("DOWN"));
            assertEquals(CacheHealthIndicator.Status.UNKNOWN, CacheHealthIndicator.Status.valueOf("UNKNOWN"));
        }
    }

    @Nested
    @DisplayName("HealthResult Tests")
    class HealthResultTests {

        @Test
        @DisplayName("HealthResult record accessors work")
        void healthResultRecordAccessorsWork() {
            CacheHealthIndicator.HealthResult result = healthIndicator.health();

            assertNotNull(result.status());
            assertTrue(result.cacheCount() >= 0);
            assertTrue(result.totalEntries() >= 0);
            assertTrue(result.overallHitRate() >= 0 && result.overallHitRate() <= 1);
            assertNotNull(result.caches());
        }

        @Test
        @DisplayName("HealthResult isHealthy returns true for UP status")
        void healthResultIsHealthyReturnsTrueForUpStatus() {
            CacheHealthIndicator.HealthResult result = healthIndicator.health();

            if (result.status() == CacheHealthIndicator.Status.UP) {
                assertTrue(result.isHealthy());
            }
        }

        @Test
        @DisplayName("HealthResult toMap returns valid map")
        void healthResultToMapReturnsValidMap() {
            Cache<String, String> cache = cacheManager.<String, String>getCache("test-cache").orElseThrow();
            cache.put("key", "value");

            CacheHealthIndicator.HealthResult result = healthIndicator.health();
            Map<String, Object> map = result.toMap();

            assertNotNull(map);
            assertTrue(map.containsKey("status"));
            assertTrue(map.containsKey("cacheCount"));
            assertTrue(map.containsKey("totalEntries"));
            assertTrue(map.containsKey("overallHitRate"));
            assertTrue(map.containsKey("caches"));
        }

        @Test
        @DisplayName("HealthResult with error includes error in map")
        void healthResultWithErrorIncludesErrorInMap() {
            CacheHealthIndicator.HealthResult result = new CacheHealthIndicator.HealthResult(
                    CacheHealthIndicator.Status.DOWN, 0, 0, 0, Map.of(), "Test error");

            Map<String, Object> map = result.toMap();

            assertTrue(map.containsKey("error"));
            assertEquals("Test error", map.get("error"));
        }

        @Test
        @DisplayName("HealthResult constructor without error")
        void healthResultConstructorWithoutError() {
            CacheHealthIndicator.HealthResult result = new CacheHealthIndicator.HealthResult(
                    CacheHealthIndicator.Status.UP, 2, 100, 0.95, Map.of());

            assertNull(result.error());
            assertEquals(CacheHealthIndicator.Status.UP, result.status());
            assertEquals(2, result.cacheCount());
            assertEquals(100, result.totalEntries());
            assertEquals(0.95, result.overallHitRate());
        }
    }

    @Nested
    @DisplayName("CacheHealth Tests")
    class CacheHealthTests {

        @Test
        @DisplayName("CacheHealth record accessors work")
        void cacheHealthRecordAccessorsWork() {
            CacheHealthIndicator.CacheHealth health = new CacheHealthIndicator.CacheHealth(
                    CacheHealthIndicator.Status.UP,
                    100,
                    80,
                    20,
                    5,
                    0.8,
                    0.2,
                    null
            );

            assertEquals(CacheHealthIndicator.Status.UP, health.status());
            assertEquals(100, health.size());
            assertEquals(80, health.hitCount());
            assertEquals(20, health.missCount());
            assertEquals(5, health.evictionCount());
            assertEquals(0.8, health.hitRate());
            assertEquals(0.2, health.missRate());
            assertNull(health.warning());
        }

        @Test
        @DisplayName("CacheHealth toMap returns valid map")
        void cacheHealthToMapReturnsValidMap() {
            CacheHealthIndicator.CacheHealth health = new CacheHealthIndicator.CacheHealth(
                    CacheHealthIndicator.Status.UP,
                    100,
                    80,
                    20,
                    5,
                    0.8,
                    0.2,
                    null
            );

            Map<String, Object> map = health.toMap();

            assertNotNull(map);
            assertEquals("UP", map.get("status"));
            assertEquals(100L, map.get("size"));
            assertEquals(80L, map.get("hitCount"));
            assertEquals(20L, map.get("missCount"));
            assertEquals(5L, map.get("evictionCount"));
            assertTrue(map.containsKey("hitRate"));
            assertTrue(map.containsKey("missRate"));
            assertFalse(map.containsKey("warning"));
        }

        @Test
        @DisplayName("CacheHealth toMap includes warning when present")
        void cacheHealthToMapIncludesWarningWhenPresent() {
            CacheHealthIndicator.CacheHealth health = new CacheHealthIndicator.CacheHealth(
                    CacheHealthIndicator.Status.DEGRADED,
                    100,
                    10,
                    90,
                    5,
                    0.1,
                    0.9,
                    "Low hit rate"
            );

            Map<String, Object> map = health.toMap();

            assertTrue(map.containsKey("warning"));
            assertEquals("Low hit rate", map.get("warning"));
        }
    }

    @Nested
    @DisplayName("Multiple Caches Tests")
    class MultipleCachesTests {

        @Test
        @DisplayName("health aggregates multiple caches")
        void healthAggregatesMultipleCaches() {
            cacheManager.reset();
            cacheManager.getOrCreateCache("cache1", config -> config.maximumSize(100).recordStats());
            cacheManager.getOrCreateCache("cache2", config -> config.maximumSize(100).recordStats());
            cacheManager.getOrCreateCache("cache3", config -> config.maximumSize(100).recordStats());

            CacheHealthIndicator indicator = new CacheHealthIndicator(cacheManager);

            // Add some data to caches
            cacheManager.<String, String>getCache("cache1").ifPresent(c -> c.put("k1", "v1"));
            cacheManager.<String, String>getCache("cache2").ifPresent(c -> c.put("k2", "v2"));
            cacheManager.<String, String>getCache("cache3").ifPresent(c -> c.put("k3", "v3"));

            CacheHealthIndicator.HealthResult result = indicator.health();

            assertEquals(3, result.cacheCount());
            assertEquals(3, result.totalEntries());
            assertEquals(3, result.caches().size());
        }

        @Test
        @DisplayName("health returns DEGRADED when any cache is degraded")
        void healthReturnsDegradedWhenAnyCacheIsDegraded() {
            cacheManager.reset();
            cacheManager.getOrCreateCache("healthy", config -> config.maximumSize(100).recordStats());
            cacheManager.getOrCreateCache("unhealthy", config -> config.maximumSize(100).recordStats());

            CacheHealthIndicator indicator = new CacheHealthIndicator(cacheManager)
                    .hitRateThreshold(0.5);

            // Make healthy cache
            Cache<String, String> healthy = cacheManager.<String, String>getCache("healthy").orElseThrow();
            healthy.put("key", "value");
            for (int i = 0; i < 100; i++) {
                healthy.get("key"); // all hits
            }

            // Make unhealthy cache with many misses
            Cache<String, String> unhealthy = cacheManager.<String, String>getCache("unhealthy").orElseThrow();
            for (int i = 0; i < 150; i++) {
                unhealthy.get("missing" + i); // all misses
            }

            CacheHealthIndicator.HealthResult result = indicator.health();

            assertEquals(CacheHealthIndicator.Status.DEGRADED, result.status());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("health handles cache with no stats gracefully")
        void healthHandlesCacheWithNoStatsGracefully() {
            cacheManager.reset();
            cacheManager.getOrCreateCache("no-stats", config -> config.maximumSize(100));

            CacheHealthIndicator indicator = new CacheHealthIndicator(cacheManager);

            CacheHealthIndicator.HealthResult result = indicator.health();

            assertNotNull(result);
            assertEquals(CacheHealthIndicator.Status.UP, result.status());
        }

        @Test
        @DisplayName("health handles boundary hit rate threshold")
        void healthHandlesBoundaryHitRateThreshold() {
            healthIndicator.hitRateThreshold(0.0);
            CacheHealthIndicator.HealthResult result1 = healthIndicator.health();
            assertNotNull(result1);

            healthIndicator.hitRateThreshold(1.0);
            CacheHealthIndicator.HealthResult result2 = healthIndicator.health();
            assertNotNull(result2);
        }

        @Test
        @DisplayName("health handles boundary eviction rate threshold")
        void healthHandlesBoundaryEvictionRateThreshold() {
            healthIndicator.evictionRateThreshold(0.0);
            CacheHealthIndicator.HealthResult result1 = healthIndicator.health();
            assertNotNull(result1);

            healthIndicator.evictionRateThreshold(1.0);
            CacheHealthIndicator.HealthResult result2 = healthIndicator.health();
            assertNotNull(result2);
        }

        @Test
        @DisplayName("health handles large size warning threshold")
        void healthHandlesLargeSizeWarningThreshold() {
            cacheManager.reset();
            cacheManager.getOrCreateCache("large", config -> config.maximumSize(200).recordStats());

            CacheHealthIndicator indicator = new CacheHealthIndicator(cacheManager)
                    .maxSizeWarningThreshold(50); // Set low threshold

            Cache<String, String> cache = cacheManager.<String, String>getCache("large").orElseThrow();
            for (int i = 0; i < 100; i++) {
                cache.put("key" + i, "value" + i);
            }

            CacheHealthIndicator.HealthResult result = indicator.health();

            // Should be degraded because size exceeds threshold
            assertEquals(CacheHealthIndicator.Status.DEGRADED, result.status());
        }
    }
}
