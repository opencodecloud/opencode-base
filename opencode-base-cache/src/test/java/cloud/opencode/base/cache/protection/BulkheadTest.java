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

package cloud.opencode.base.cache.protection;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for Bulkhead
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("Bulkhead Tests")
class BulkheadTest {

    private Bulkhead bulkhead;

    @AfterEach
    void tearDown() {
        if (bulkhead != null) {
            bulkhead.close();
        }
    }

    @Nested
    @DisplayName("Semaphore Factory Method Tests")
    class SemaphoreFactoryMethodTests {

        @Test
        @DisplayName("semaphore creates builder")
        void semaphoreCreatesBuilder() {
            Bulkhead.SemaphoreBuilder builder = Bulkhead.semaphore("test");
            assertNotNull(builder);
        }

        @Test
        @DisplayName("build creates semaphore bulkhead")
        void buildCreatesSemaphoreBulkhead() {
            bulkhead = Bulkhead.semaphore("test")
                    .maxConcurrentCalls(10)
                    .build();

            assertNotNull(bulkhead);
            assertEquals("test", bulkhead.name());
        }

        @Test
        @DisplayName("builder with maxConcurrentCalls")
        void builderWithMaxConcurrentCalls() {
            bulkhead = Bulkhead.semaphore("test")
                    .maxConcurrentCalls(5)
                    .build();

            Bulkhead.Metrics metrics = bulkhead.getMetrics();
            assertEquals(5, metrics.maxAllowedConcurrentCalls());
        }

        @Test
        @DisplayName("builder throws on non-positive maxConcurrentCalls")
        void builderThrowsOnNonPositiveMaxConcurrentCalls() {
            assertThrows(IllegalArgumentException.class, () ->
                    Bulkhead.semaphore("test").maxConcurrentCalls(0));
            assertThrows(IllegalArgumentException.class, () ->
                    Bulkhead.semaphore("test").maxConcurrentCalls(-1));
        }

        @Test
        @DisplayName("builder with maxWaitDuration")
        void builderWithMaxWaitDuration() {
            bulkhead = Bulkhead.semaphore("test")
                    .maxWaitDuration(Duration.ofMillis(100))
                    .build();

            assertNotNull(bulkhead);
        }
    }

    @Nested
    @DisplayName("Thread Pool Factory Method Tests")
    class ThreadPoolFactoryMethodTests {

        @Test
        @DisplayName("threadPool creates builder")
        void threadPoolCreatesBuilder() {
            Bulkhead.ThreadPoolBuilder builder = Bulkhead.threadPool("test");
            assertNotNull(builder);
        }

        @Test
        @DisplayName("build creates thread pool bulkhead")
        void buildCreatesThreadPoolBulkhead() {
            bulkhead = Bulkhead.threadPool("test")
                    .corePoolSize(2)
                    .maxPoolSize(5)
                    .queueCapacity(10)
                    .build();

            assertNotNull(bulkhead);
            assertEquals("test", bulkhead.name());
        }

        @Test
        @DisplayName("builder with all options")
        void builderWithAllOptions() {
            bulkhead = Bulkhead.threadPool("test")
                    .corePoolSize(2)
                    .maxPoolSize(5)
                    .queueCapacity(10)
                    .keepAliveTime(Duration.ofSeconds(30))
                    .maxWaitDuration(Duration.ofSeconds(5))
                    .build();

            assertNotNull(bulkhead);
        }

        @Test
        @DisplayName("builder throws on negative corePoolSize")
        void builderThrowsOnNegativeCorePoolSize() {
            assertThrows(IllegalArgumentException.class, () ->
                    Bulkhead.threadPool("test").corePoolSize(-1));
        }

        @Test
        @DisplayName("builder throws on non-positive maxPoolSize")
        void builderThrowsOnNonPositiveMaxPoolSize() {
            assertThrows(IllegalArgumentException.class, () ->
                    Bulkhead.threadPool("test").maxPoolSize(0));
        }

        @Test
        @DisplayName("builder throws on negative queueCapacity")
        void builderThrowsOnNegativeQueueCapacity() {
            assertThrows(IllegalArgumentException.class, () ->
                    Bulkhead.threadPool("test").queueCapacity(-1));
        }

        @Test
        @DisplayName("builder throws on corePoolSize > maxPoolSize")
        void builderThrowsOnCorePoolSizeGreaterThanMaxPoolSize() {
            assertThrows(IllegalArgumentException.class, () ->
                    Bulkhead.threadPool("test")
                            .corePoolSize(10)
                            .maxPoolSize(5)
                            .build());
        }
    }

    @Nested
    @DisplayName("Semaphore Bulkhead Execute Tests")
    class SemaphoreBulkheadExecuteTests {

        @Test
        @DisplayName("execute returns result")
        void executeReturnsResult() {
            bulkhead = Bulkhead.semaphore("test").build();

            String result = bulkhead.execute(() -> "hello");
            assertEquals("hello", result);
        }

        @Test
        @DisplayName("execute throws on null supplier")
        void executeThrowsOnNullSupplier() {
            bulkhead = Bulkhead.semaphore("test").build();

            assertThrows(NullPointerException.class, () -> bulkhead.execute(null));
        }

        @Test
        @DisplayName("execute throws BulkheadFullException when full")
        void executeThrowsBulkheadFullExceptionWhenFull() throws InterruptedException {
            bulkhead = Bulkhead.semaphore("test")
                    .maxConcurrentCalls(1)
                    .build();

            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch blockLatch = new CountDownLatch(1);

            // Start a blocking operation
            new Thread(() -> bulkhead.execute(() -> {
                startLatch.countDown();
                try {
                    blockLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "done";
            })).start();

            startLatch.await();

            // Try to execute while bulkhead is full
            assertThrows(Bulkhead.BulkheadFullException.class, () ->
                    bulkhead.execute(() -> "another"));

            blockLatch.countDown();
        }

        @Test
        @DisplayName("execute with fallback returns fallback when full")
        void executeWithFallbackReturnsFallbackWhenFull() throws InterruptedException {
            bulkhead = Bulkhead.semaphore("test")
                    .maxConcurrentCalls(1)
                    .build();

            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch blockLatch = new CountDownLatch(1);

            // Start a blocking operation
            new Thread(() -> bulkhead.execute(() -> {
                startLatch.countDown();
                try {
                    blockLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "done";
            })).start();

            startLatch.await();

            // Execute with fallback
            String result = bulkhead.execute(() -> "primary", () -> "fallback");
            assertEquals("fallback", result);

            blockLatch.countDown();
        }
    }

    @Nested
    @DisplayName("Semaphore Bulkhead ExecuteAsync Tests")
    class SemaphoreBulkheadExecuteAsyncTests {

        @Test
        @DisplayName("executeAsync returns future")
        void executeAsyncReturnsFuture() {
            bulkhead = Bulkhead.semaphore("test").build();

            CompletableFuture<String> future = bulkhead.executeAsync(() -> "hello");
            assertEquals("hello", future.join());
        }

        @Test
        @DisplayName("executeAsync throws on null supplier")
        void executeAsyncThrowsOnNullSupplier() {
            bulkhead = Bulkhead.semaphore("test").build();

            assertThrows(NullPointerException.class, () -> bulkhead.executeAsync(null));
        }
    }

    @Nested
    @DisplayName("Thread Pool Bulkhead Execute Tests")
    class ThreadPoolBulkheadExecuteTests {

        @Test
        @DisplayName("execute returns result")
        void executeReturnsResult() {
            bulkhead = Bulkhead.threadPool("test")
                    .corePoolSize(1)
                    .maxPoolSize(1)
                    .queueCapacity(1)
                    .build();

            String result = bulkhead.execute(() -> "hello");
            assertEquals("hello", result);
        }

        @Test
        @DisplayName("executeAsync returns future")
        void executeAsyncReturnsFuture() {
            bulkhead = Bulkhead.threadPool("test")
                    .corePoolSize(1)
                    .maxPoolSize(1)
                    .queueCapacity(1)
                    .build();

            CompletableFuture<String> future = bulkhead.executeAsync(() -> "hello");
            assertEquals("hello", future.join());
        }
    }

    @Nested
    @DisplayName("TryAcquire and Release Tests")
    class TryAcquireAndReleaseTests {

        @Test
        @DisplayName("tryAcquire returns true when available")
        void tryAcquireReturnsTrueWhenAvailable() {
            bulkhead = Bulkhead.semaphore("test")
                    .maxConcurrentCalls(1)
                    .build();

            assertTrue(bulkhead.tryAcquire());
            bulkhead.release();
        }

        @Test
        @DisplayName("tryAcquire returns false when full")
        void tryAcquireReturnsFalseWhenFull() {
            bulkhead = Bulkhead.semaphore("test")
                    .maxConcurrentCalls(1)
                    .build();

            assertTrue(bulkhead.tryAcquire());
            assertFalse(bulkhead.tryAcquire());
            bulkhead.release();
        }

        @Test
        @DisplayName("release makes permit available")
        void releaseMakesPermitAvailable() {
            bulkhead = Bulkhead.semaphore("test")
                    .maxConcurrentCalls(1)
                    .build();

            assertTrue(bulkhead.tryAcquire());
            assertFalse(bulkhead.tryAcquire());

            bulkhead.release();

            assertTrue(bulkhead.tryAcquire());
            bulkhead.release();
        }
    }

    @Nested
    @DisplayName("Metrics Tests")
    class MetricsTests {

        @Test
        @DisplayName("getMetrics returns metrics")
        void getMetricsReturnsMetrics() {
            bulkhead = Bulkhead.semaphore("test")
                    .maxConcurrentCalls(10)
                    .build();

            Bulkhead.Metrics metrics = bulkhead.getMetrics();

            assertEquals("test", metrics.name());
            assertEquals(10, metrics.maxAllowedConcurrentCalls());
            assertEquals(10, metrics.availableConcurrentCalls());
            assertEquals(0, metrics.successfulCallsCount());
            assertEquals(0, metrics.rejectedCallsCount());
            assertEquals(0, metrics.totalCallsCount());
        }

        @Test
        @DisplayName("metrics tracks successful calls")
        void metricsTracksSuccessfulCalls() {
            bulkhead = Bulkhead.semaphore("test").build();

            bulkhead.execute(() -> "result");
            bulkhead.execute(() -> "result");

            Bulkhead.Metrics metrics = bulkhead.getMetrics();
            assertEquals(2, metrics.successfulCallsCount());
        }

        @Test
        @DisplayName("metrics rejectionRate calculation")
        void metricsRejectionRateCalculation() {
            Bulkhead.Metrics metrics = new Bulkhead.Metrics("test", 10, 5, 8, 2, 10);
            assertEquals(0.2, metrics.rejectionRate());
        }

        @Test
        @DisplayName("metrics rejectionRate when no calls")
        void metricsRejectionRateWhenNoCalls() {
            Bulkhead.Metrics metrics = new Bulkhead.Metrics("test", 10, 10, 0, 0, 0);
            assertEquals(0.0, metrics.rejectionRate());
        }

        @Test
        @DisplayName("metrics utilizationRate calculation")
        void metricsUtilizationRateCalculation() {
            Bulkhead.Metrics metrics = new Bulkhead.Metrics("test", 10, 5, 0, 0, 0);
            assertEquals(0.5, metrics.utilizationRate());
        }

        @Test
        @DisplayName("metrics utilizationRate when no max calls")
        void metricsUtilizationRateWhenNoMaxCalls() {
            Bulkhead.Metrics metrics = new Bulkhead.Metrics("test", 0, 0, 0, 0, 0);
            assertEquals(0.0, metrics.utilizationRate());
        }
    }

    @Nested
    @DisplayName("Close Tests")
    class CloseTests {

        @Test
        @DisplayName("close shuts down resources")
        void closeShutdownsResources() {
            bulkhead = Bulkhead.semaphore("test").build();
            assertDoesNotThrow(() -> bulkhead.close());
        }

        @Test
        @DisplayName("close is idempotent")
        void closeIsIdempotent() {
            bulkhead = Bulkhead.semaphore("test").build();
            bulkhead.close();
            assertDoesNotThrow(() -> bulkhead.close());
        }
    }

    @Nested
    @DisplayName("BulkheadFullException Tests")
    class BulkheadFullExceptionTests {

        @Test
        @DisplayName("exception contains message")
        void exceptionContainsMessage() {
            Bulkhead.BulkheadFullException ex = new Bulkhead.BulkheadFullException("test message");
            assertEquals("test message", ex.getMessage());
        }

        @Test
        @DisplayName("exception is RuntimeException")
        void exceptionIsRuntimeException() {
            Bulkhead.BulkheadFullException ex = new Bulkhead.BulkheadFullException("test");
            assertInstanceOf(RuntimeException.class, ex);
        }
    }

    @Nested
    @DisplayName("Concurrent Execution Tests")
    class ConcurrentExecutionTests {

        @Test
        @DisplayName("concurrent executions respect max limit")
        void concurrentExecutionsRespectMaxLimit() throws InterruptedException {
            int maxConcurrent = 5;
            bulkhead = Bulkhead.semaphore("test")
                    .maxConcurrentCalls(maxConcurrent)
                    .build();

            AtomicInteger currentConcurrent = new AtomicInteger(0);
            AtomicInteger maxObserved = new AtomicInteger(0);
            int totalTasks = 20;
            CountDownLatch allStarted = new CountDownLatch(totalTasks);
            CountDownLatch allDone = new CountDownLatch(totalTasks);

            for (int i = 0; i < totalTasks; i++) {
                new Thread(() -> {
                    try {
                        bulkhead.execute(() -> {
                            int current = currentConcurrent.incrementAndGet();
                            maxObserved.updateAndGet(prev -> Math.max(prev, current));
                            allStarted.countDown();
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            currentConcurrent.decrementAndGet();
                            return null;
                        });
                    } catch (Bulkhead.BulkheadFullException e) {
                        allStarted.countDown();
                    } finally {
                        allDone.countDown();
                    }
                }).start();
            }

            allDone.await(10, TimeUnit.SECONDS);

            assertTrue(maxObserved.get() <= maxConcurrent,
                    "Max observed " + maxObserved.get() + " exceeded limit " + maxConcurrent);
        }
    }
}
