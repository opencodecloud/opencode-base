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

package cloud.opencode.base.cache.internal;

import cloud.opencode.base.cache.Cache;
import cloud.opencode.base.cache.OpenCache;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ExpirationScheduler
 * Note: Testing singleton with global state requires careful consideration
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("ExpirationScheduler Tests")
class ExpirationSchedulerTest {

    @Nested
    @DisplayName("Singleton Tests")
    class SingletonTests {

        @Test
        @DisplayName("getInstance returns singleton")
        void getInstanceReturnsSingleton() {
            ExpirationScheduler instance1 = ExpirationScheduler.getInstance();
            ExpirationScheduler instance2 = ExpirationScheduler.getInstance();
            assertSame(instance1, instance2);
        }

        @Test
        @DisplayName("getInstance returns non-null")
        void getInstanceReturnsNonNull() {
            assertNotNull(ExpirationScheduler.getInstance());
        }
    }

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("register adds cache")
        void registerAddsCache() {
            ExpirationScheduler scheduler = ExpirationScheduler.getInstance();
            Cache<String, String> cache = OpenCache.<String, String>builder()
                    .maximumSize(10)
                    .build("test-registration-1");

            try {
                scheduler.register(cache, Duration.ofSeconds(60));
                assertTrue(scheduler.isRegistered("test-registration-1"));
            } finally {
                scheduler.unregister("test-registration-1");
            }
        }

        @Test
        @DisplayName("register with default interval")
        void registerWithDefaultInterval() {
            ExpirationScheduler scheduler = ExpirationScheduler.getInstance();
            Cache<String, String> cache = OpenCache.<String, String>builder()
                    .maximumSize(10)
                    .build("test-registration-2");

            try {
                scheduler.register(cache);
                assertTrue(scheduler.isRegistered("test-registration-2"));
            } finally {
                scheduler.unregister("test-registration-2");
            }
        }

        @Test
        @DisplayName("register throws on null cache")
        void registerThrowsOnNullCache() {
            ExpirationScheduler scheduler = ExpirationScheduler.getInstance();
            assertThrows(NullPointerException.class,
                    () -> scheduler.register(null, Duration.ofSeconds(60)));
        }

        @Test
        @DisplayName("register throws on null interval")
        void registerThrowsOnNullInterval() {
            ExpirationScheduler scheduler = ExpirationScheduler.getInstance();
            Cache<String, String> cache = OpenCache.<String, String>builder()
                    .maximumSize(10)
                    .build("test-null-interval");

            assertThrows(NullPointerException.class,
                    () -> scheduler.register(cache, null));
        }

        @Test
        @DisplayName("register is idempotent")
        void registerIsIdempotent() {
            ExpirationScheduler scheduler = ExpirationScheduler.getInstance();
            Cache<String, String> cache = OpenCache.<String, String>builder()
                    .maximumSize(10)
                    .build("test-idempotent");

            try {
                scheduler.register(cache, Duration.ofSeconds(60));
                // Registering again should not throw
                assertDoesNotThrow(() -> scheduler.register(cache, Duration.ofSeconds(60)));
            } finally {
                scheduler.unregister("test-idempotent");
            }
        }
    }

    @Nested
    @DisplayName("Unregistration Tests")
    class UnregistrationTests {

        @Test
        @DisplayName("unregister removes cache")
        void unregisterRemovesCache() {
            ExpirationScheduler scheduler = ExpirationScheduler.getInstance();
            Cache<String, String> cache = OpenCache.<String, String>builder()
                    .maximumSize(10)
                    .build("test-unregister");

            scheduler.register(cache, Duration.ofSeconds(60));
            assertTrue(scheduler.isRegistered("test-unregister"));

            scheduler.unregister("test-unregister");
            assertFalse(scheduler.isRegistered("test-unregister"));
        }

        @Test
        @DisplayName("unregister non-existent cache is safe")
        void unregisterNonExistentCacheIsSafe() {
            ExpirationScheduler scheduler = ExpirationScheduler.getInstance();
            assertDoesNotThrow(() -> scheduler.unregister("non-existent-cache"));
        }
    }

    @Nested
    @DisplayName("isRegistered Tests")
    class IsRegisteredTests {

        @Test
        @DisplayName("isRegistered returns false for unregistered")
        void isRegisteredReturnsFalseForUnregistered() {
            ExpirationScheduler scheduler = ExpirationScheduler.getInstance();
            assertFalse(scheduler.isRegistered("never-registered"));
        }

        @Test
        @DisplayName("isRegistered returns true for registered")
        void isRegisteredReturnsTrueForRegistered() {
            ExpirationScheduler scheduler = ExpirationScheduler.getInstance();
            Cache<String, String> cache = OpenCache.<String, String>builder()
                    .maximumSize(10)
                    .build("test-is-registered");

            try {
                scheduler.register(cache, Duration.ofSeconds(60));
                assertTrue(scheduler.isRegistered("test-is-registered"));
            } finally {
                scheduler.unregister("test-is-registered");
            }
        }
    }

    @Nested
    @DisplayName("Metrics Tests")
    class MetricsTests {

        @Test
        @DisplayName("getMetrics returns metrics")
        void getMetricsReturnsMetrics() {
            ExpirationScheduler scheduler = ExpirationScheduler.getInstance();
            ExpirationScheduler.Metrics metrics = scheduler.getMetrics();
            assertNotNull(metrics);
            assertTrue(metrics.registeredCaches() >= 0);
            assertTrue(metrics.totalCleanupRuns() >= 0);
            assertTrue(metrics.totalEntriesCleaned() >= 0);
        }

        @Test
        @DisplayName("metrics record accessors work")
        void metricsRecordAccessorsWork() {
            ExpirationScheduler.Metrics metrics = new ExpirationScheduler.Metrics(5, 100, 500);
            assertEquals(5, metrics.registeredCaches());
            assertEquals(100, metrics.totalCleanupRuns());
            assertEquals(500, metrics.totalEntriesCleaned());
        }

        @Test
        @DisplayName("metrics averageEntriesPerRun")
        void metricsAverageEntriesPerRun() {
            ExpirationScheduler.Metrics metrics = new ExpirationScheduler.Metrics(5, 100, 500);
            assertEquals(5.0, metrics.averageEntriesPerRun());
        }

        @Test
        @DisplayName("metrics averageEntriesPerRun when no runs")
        void metricsAverageEntriesPerRunWhenNoRuns() {
            ExpirationScheduler.Metrics metrics = new ExpirationScheduler.Metrics(5, 0, 0);
            assertEquals(0.0, metrics.averageEntriesPerRun());
        }
    }

    @Nested
    @DisplayName("Cleanup Tests")
    class CleanupTests {

        @Test
        @DisplayName("cleanup runs on schedule")
        void cleanupRunsOnSchedule() throws InterruptedException {
            ExpirationScheduler scheduler = ExpirationScheduler.getInstance();

            // Create cache with short TTL
            Cache<String, String> cache = OpenCache.<String, String>builder()
                    .maximumSize(100)
                    .expireAfterWrite(Duration.ofMillis(50))
                    .build("test-cleanup-schedule");

            try {
                // Register with short interval
                scheduler.register(cache, Duration.ofMillis(100));

                // Add entries
                cache.put("key1", "value1");
                cache.put("key2", "value2");

                // Wait for cleanup to occur
                Thread.sleep(300);

                // Entries should be cleaned up
                assertEquals(0, cache.estimatedSize());
            } finally {
                scheduler.unregister("test-cleanup-schedule");
            }
        }
    }
}
