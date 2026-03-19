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

package cloud.opencode.base.cache.resilience;

import cloud.opencode.base.cache.protection.Bulkhead;
import cloud.opencode.base.cache.protection.CircuitBreaker;
import cloud.opencode.base.cache.spi.CacheLoader;
import cloud.opencode.base.cache.spi.RetryPolicy;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for ResilientCacheLoader
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("ResilientCacheLoader Tests")
class ResilientCacheLoaderTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("builder creates loader")
        void builderCreatesLoader() {
            Function<String, String> loader = ResilientCacheLoader.<String, String>builder()
                    .loader((Function<String, String>) k -> "value-" + k)
                    .build();

            assertNotNull(loader);
        }

        @Test
        @DisplayName("builder throws on null loader")
        void builderThrowsOnNullLoader() {
            assertThrows(NullPointerException.class, () ->
                    ResilientCacheLoader.<String, String>builder().build());
        }

        @Test
        @DisplayName("wrap creates loader with defaults")
        void wrapCreatesLoaderWithDefaults() {
            Function<String, String> loader = ResilientCacheLoader.wrap(k -> "value-" + k);

            assertNotNull(loader);
            assertEquals("value-key", loader.apply("key"));
        }

        @Test
        @DisplayName("batchBuilder creates batch loader")
        void batchBuilderCreatesBatchLoader() {
            Function<Set<? extends String>, Map<String, String>> loader = ResilientCacheLoader.<String, String>batchBuilder()
                    .loader(keys -> Map.of("k1", "v1"))
                    .build();

            assertNotNull(loader);
        }
    }

    @Nested
    @DisplayName("Basic Loading Tests")
    class BasicLoadingTests {

        @Test
        @DisplayName("apply loads value")
        void applyLoadsValue() {
            Function<String, String> loader = ResilientCacheLoader.<String, String>builder()
                    .loader((Function<String, String>) k -> "value-" + k)
                    .build();

            assertEquals("value-key", loader.apply("key"));
        }

        @Test
        @DisplayName("loader with CacheLoader interface")
        void loaderWithCacheLoaderInterface() {
            CacheLoader<String, String> cacheLoader = key -> "loaded-" + key;

            Function<String, String> loader = ResilientCacheLoader.<String, String>builder()
                    .loader(cacheLoader)
                    .build();

            assertEquals("loaded-key", loader.apply("key"));
        }
    }

    @Nested
    @DisplayName("Retry Tests")
    class RetryTests {

        @Test
        @DisplayName("retry retries on failure")
        void retryRetriesOnFailure() {
            AtomicInteger attempts = new AtomicInteger(0);

            Function<String, String> loader = ResilientCacheLoader.<String, String>builder()
                    .loader((Function<String, String>) k -> {
                        if (attempts.incrementAndGet() < 3) {
                            throw new RuntimeException("Fail");
                        }
                        return "success";
                    })
                    .retry(RetryPolicy.fixedDelay(3, Duration.ofMillis(10)))
                    .build();

            String result = loader.apply("key");

            assertEquals("success", result);
            assertEquals(3, attempts.get());
        }

        @Test
        @DisplayName("retry respects shouldRetry predicate")
        void retryRespectsShouldRetryPredicate() {
            AtomicInteger attempts = new AtomicInteger(0);

            // Use retryOn to create a policy that never retries
            RetryPolicy noRetryPolicy = RetryPolicy.fixedDelay(3, Duration.ofMillis(10))
                    .retryOn(ex -> false);

            Function<String, String> loader = ResilientCacheLoader.<String, String>builder()
                    .loader((Function<String, String>) k -> {
                        attempts.incrementAndGet();
                        throw new RuntimeException("Fail");
                    })
                    .retry(noRetryPolicy)
                    .build();

            assertThrows(RuntimeException.class, () -> loader.apply("key"));
            assertEquals(1, attempts.get()); // Only 1 attempt
        }
    }

    @Nested
    @DisplayName("Circuit Breaker Tests")
    class CircuitBreakerTests {

        @Test
        @DisplayName("circuit breaker blocks when open")
        void circuitBreakerBlocksWhenOpen() {
            CircuitBreaker.Config config = CircuitBreaker.Config.builder()
                    .failureThreshold(1)
                    .openDuration(Duration.ofMinutes(1))
                    .build();
            CircuitBreaker cb = CircuitBreaker.create(config);

            Function<String, String> loader = ResilientCacheLoader.<String, String>builder()
                    .loader((Function<String, String>) k -> {
                        throw new RuntimeException("Fail");
                    })
                    .circuitBreaker(cb)
                    .build();

            // First call opens the circuit
            assertThrows(RuntimeException.class, () -> loader.apply("key1"));

            // Second call should be blocked by open circuit
            assertThrows(ResilientCacheLoader.CircuitBreakerOpenException.class, () -> loader.apply("key2"));
        }

        @Test
        @DisplayName("circuit breaker allows when fallback provided")
        void circuitBreakerAllowsWhenFallbackProvided() {
            CircuitBreaker.Config config = CircuitBreaker.Config.builder()
                    .failureThreshold(1)
                    .openDuration(Duration.ofMinutes(1))
                    .build();
            CircuitBreaker cb = CircuitBreaker.create(config);

            Function<String, String> loader = ResilientCacheLoader.<String, String>builder()
                    .loader((Function<String, String>) k -> {
                        throw new RuntimeException("Fail");
                    })
                    .circuitBreaker(cb)
                    .fallbackValue("fallback")
                    .build();

            // First call opens the circuit
            assertEquals("fallback", loader.apply("key1"));

            // Second call uses fallback due to open circuit
            assertEquals("fallback", loader.apply("key2"));
        }

        @Test
        @DisplayName("circuit breaker records success")
        void circuitBreakerRecordsSuccess() {
            CircuitBreaker.Config config = CircuitBreaker.Config.builder()
                    .failureThreshold(5)
                    .build();
            CircuitBreaker cb = CircuitBreaker.create(config);

            Function<String, String> loader = ResilientCacheLoader.<String, String>builder()
                    .loader((Function<String, String>) k -> "success")
                    .circuitBreaker(cb)
                    .build();

            assertEquals("success", loader.apply("key"));
            // Should not throw even after multiple calls
            assertEquals("success", loader.apply("key"));
        }
    }

    @Nested
    @DisplayName("Bulkhead Tests")
    class BulkheadTests {

        @Test
        @DisplayName("bulkhead limits concurrency")
        void bulkheadLimitsConcurrency() {
            // Create a bulkhead with capacity 1 and acquire it
            Bulkhead bulkhead = Bulkhead.semaphore("test-bulkhead")
                    .maxConcurrentCalls(1)
                    .maxWaitDuration(Duration.ZERO)
                    .build();

            // Acquire the only permit
            assertTrue(bulkhead.tryAcquire());

            Function<String, String> loader = ResilientCacheLoader.<String, String>builder()
                    .loader((Function<String, String>) k -> "value")
                    .bulkhead(bulkhead)
                    .build();

            // Should be rejected since bulkhead is full
            assertThrows(ResilientCacheLoader.BulkheadRejectedException.class, () -> loader.apply("key"));

            // Release and try again
            bulkhead.release();
            assertEquals("value", loader.apply("key"));
        }

        @Test
        @DisplayName("bulkhead with fallback returns fallback")
        void bulkheadWithFallbackReturnsFallback() {
            Bulkhead bulkhead = Bulkhead.semaphore("test-bulkhead-fallback")
                    .maxConcurrentCalls(1)
                    .maxWaitDuration(Duration.ZERO)
                    .build();

            // Acquire the only permit
            assertTrue(bulkhead.tryAcquire());

            Function<String, String> loader = ResilientCacheLoader.<String, String>builder()
                    .loader((Function<String, String>) k -> "value")
                    .bulkhead(bulkhead)
                    .fallbackValue("fallback")
                    .build();

            assertEquals("fallback", loader.apply("key"));
        }
    }

    @Nested
    @DisplayName("Timeout Tests")
    class TimeoutTests {

        @Test
        @DisplayName("timeout throws on slow operation")
        void timeoutThrowsOnSlowOperation() {
            Function<String, String> loader = ResilientCacheLoader.<String, String>builder()
                    .loader((Function<String, String>) k -> {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return "value";
                    })
                    .timeout(Duration.ofMillis(100))
                    .build();

            assertThrows(ResilientCacheLoader.LoaderTimeoutException.class, () -> loader.apply("key"));
        }

        @Test
        @DisplayName("timeout allows fast operation")
        void timeoutAllowsFastOperation() {
            Function<String, String> loader = ResilientCacheLoader.<String, String>builder()
                    .loader((Function<String, String>) k -> "value")
                    .timeout(Duration.ofSeconds(5))
                    .build();

            assertEquals("value", loader.apply("key"));
        }
    }

    @Nested
    @DisplayName("Fallback Tests")
    class FallbackTests {

        @Test
        @DisplayName("fallback function is called on failure")
        void fallbackFunctionIsCalledOnFailure() {
            Function<String, String> loader = ResilientCacheLoader.<String, String>builder()
                    .loader((Function<String, String>) k -> {
                        throw new RuntimeException("Fail");
                    })
                    .fallback(ex -> "fallback-" + ex.getMessage())
                    .build();

            String result = loader.apply("key");
            assertTrue(result.startsWith("fallback-"));
        }

        @Test
        @DisplayName("fallbackValue is returned on failure")
        void fallbackValueIsReturnedOnFailure() {
            Function<String, String> loader = ResilientCacheLoader.<String, String>builder()
                    .loader((Function<String, String>) k -> {
                        throw new RuntimeException("Fail");
                    })
                    .fallbackValue("default")
                    .build();

            assertEquals("default", loader.apply("key"));
        }
    }

    @Nested
    @DisplayName("Batch Loader Tests")
    class BatchLoaderTests {

        @Test
        @DisplayName("batch loader loads multiple keys")
        void batchLoaderLoadsMultipleKeys() {
            Function<Set<? extends String>, Map<String, String>> loader = ResilientCacheLoader.<String, String>batchBuilder()
                    .loader(keys -> {
                        Map<String, String> result = new java.util.HashMap<>();
                        for (String key : keys) {
                            result.put(key, "value-" + key);
                        }
                        return result;
                    })
                    .build();

            Map<String, String> result = loader.apply(Set.of("k1", "k2"));

            assertEquals("value-k1", result.get("k1"));
            assertEquals("value-k2", result.get("k2"));
        }

        @Test
        @DisplayName("batch loader with retry")
        void batchLoaderWithRetry() {
            AtomicInteger attempts = new AtomicInteger(0);

            Function<Set<? extends String>, Map<String, String>> loader = ResilientCacheLoader.<String, String>batchBuilder()
                    .loader(keys -> {
                        if (attempts.incrementAndGet() < 2) {
                            throw new RuntimeException("Fail");
                        }
                        return Map.of("k1", "v1");
                    })
                    .retry(RetryPolicy.fixedDelay(2, Duration.ofMillis(10)))
                    .build();

            Map<String, String> result = loader.apply(Set.of("k1"));

            assertEquals("v1", result.get("k1"));
            assertEquals(2, attempts.get());
        }

        @Test
        @DisplayName("batch loader with circuit breaker")
        void batchLoaderWithCircuitBreaker() {
            CircuitBreaker.Config config = CircuitBreaker.Config.builder()
                    .failureThreshold(1)
                    .openDuration(Duration.ofMinutes(1))
                    .build();
            CircuitBreaker cb = CircuitBreaker.create(config);

            Function<Set<? extends String>, Map<String, String>> loader = ResilientCacheLoader.<String, String>batchBuilder()
                    .loader(keys -> {
                        throw new RuntimeException("Fail");
                    })
                    .circuitBreaker(cb)
                    .build();

            // First call opens the circuit
            assertThrows(RuntimeException.class, () -> loader.apply(Set.of("k1")));

            // Second call should be blocked
            assertThrows(ResilientCacheLoader.CircuitBreakerOpenException.class, () -> loader.apply(Set.of("k2")));
        }

        @Test
        @DisplayName("batch loader with bulkhead")
        void batchLoaderWithBulkhead() {
            Bulkhead bulkhead = Bulkhead.semaphore("batch-test-bulkhead")
                    .maxConcurrentCalls(1)
                    .maxWaitDuration(Duration.ZERO)
                    .build();

            // Acquire the only permit
            assertTrue(bulkhead.tryAcquire());

            Function<Set<? extends String>, Map<String, String>> loader = ResilientCacheLoader.<String, String>batchBuilder()
                    .loader(keys -> Map.of("k1", "v1"))
                    .bulkhead(bulkhead)
                    .build();

            assertThrows(ResilientCacheLoader.BulkheadRejectedException.class, () -> loader.apply(Set.of("k1")));
        }

        @Test
        @DisplayName("batch loader with timeout")
        void batchLoaderWithTimeout() {
            Function<Set<? extends String>, Map<String, String>> loader = ResilientCacheLoader.<String, String>batchBuilder()
                    .loader(keys -> {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return Map.of();
                    })
                    .timeout(Duration.ofMillis(100))
                    .build();

            assertThrows(ResilientCacheLoader.LoaderTimeoutException.class, () -> loader.apply(Set.of("k1")));
        }
    }

    @Nested
    @DisplayName("Exception Tests")
    class ExceptionTests {

        @Test
        @DisplayName("LoaderException has message and cause")
        void loaderExceptionHasMessageAndCause() {
            Throwable cause = new RuntimeException("cause");
            ResilientCacheLoader.LoaderException ex = new ResilientCacheLoader.LoaderException("message", cause);

            assertTrue(ex.getMessage().contains("message"));
            assertEquals(cause, ex.getCause());
        }

        @Test
        @DisplayName("CircuitBreakerOpenException has message")
        void circuitBreakerOpenExceptionHasMessage() {
            ResilientCacheLoader.CircuitBreakerOpenException ex =
                    new ResilientCacheLoader.CircuitBreakerOpenException("Circuit open");

            assertTrue(ex.getMessage().contains("Circuit open"));
        }

        @Test
        @DisplayName("BulkheadRejectedException has message")
        void bulkheadRejectedExceptionHasMessage() {
            ResilientCacheLoader.BulkheadRejectedException ex =
                    new ResilientCacheLoader.BulkheadRejectedException("Rejected");

            assertTrue(ex.getMessage().contains("Rejected"));
        }

        @Test
        @DisplayName("LoaderTimeoutException has message and cause")
        void loaderTimeoutExceptionHasMessageAndCause() {
            Throwable cause = new RuntimeException("cause");
            ResilientCacheLoader.LoaderTimeoutException ex =
                    new ResilientCacheLoader.LoaderTimeoutException("Timeout", cause);

            assertTrue(ex.getMessage().contains("Timeout"));
            assertEquals(cause, ex.getCause());
        }

        @Test
        @DisplayName("LoaderInterruptedException has message and cause")
        void loaderInterruptedExceptionHasMessageAndCause() {
            Throwable cause = new InterruptedException("interrupted");
            ResilientCacheLoader.LoaderInterruptedException ex =
                    new ResilientCacheLoader.LoaderInterruptedException("Interrupted", cause);

            assertTrue(ex.getMessage().contains("Interrupted"));
            assertEquals(cause, ex.getCause());
        }
    }
}
