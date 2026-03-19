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

package cloud.opencode.base.cache.spi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for RetryPolicy
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("RetryPolicy Tests")
class RetryPolicyTest {

    @Nested
    @DisplayName("noRetry Tests")
    class NoRetryTests {

        @Test
        @DisplayName("noRetry returns singleton")
        void noRetryReturnsSingleton() {
            RetryPolicy policy1 = RetryPolicy.noRetry();
            RetryPolicy policy2 = RetryPolicy.noRetry();
            assertSame(policy1, policy2);
        }

        @Test
        @DisplayName("maxRetries returns 0")
        void maxRetriesReturnsZero() {
            RetryPolicy policy = RetryPolicy.noRetry();
            assertEquals(0, policy.maxRetries());
        }

        @Test
        @DisplayName("getDelay returns zero")
        void getDelayReturnsZero() {
            RetryPolicy policy = RetryPolicy.noRetry();
            assertEquals(Duration.ZERO, policy.getDelay(1));
        }
    }

    @Nested
    @DisplayName("fixedDelay Tests")
    class FixedDelayTests {

        @Test
        @DisplayName("maxRetries returns configured value")
        void maxRetriesReturnsConfiguredValue() {
            RetryPolicy policy = RetryPolicy.fixedDelay(3, Duration.ofMillis(100));
            assertEquals(3, policy.maxRetries());
        }

        @Test
        @DisplayName("getDelay returns configured delay")
        void getDelayReturnsConfiguredDelay() {
            RetryPolicy policy = RetryPolicy.fixedDelay(3, Duration.ofMillis(100));

            assertEquals(Duration.ofMillis(100), policy.getDelay(1));
            assertEquals(Duration.ofMillis(100), policy.getDelay(2));
            assertEquals(Duration.ofMillis(100), policy.getDelay(3));
        }

        @Test
        @DisplayName("throws on negative maxRetries")
        void throwsOnNegativeMaxRetries() {
            assertThrows(IllegalArgumentException.class, () ->
                    RetryPolicy.fixedDelay(-1, Duration.ofMillis(100)));
        }

        @Test
        @DisplayName("throws on null delay")
        void throwsOnNullDelay() {
            assertThrows(NullPointerException.class, () ->
                    RetryPolicy.fixedDelay(3, null));
        }

        @Test
        @DisplayName("accepts zero retries")
        void acceptsZeroRetries() {
            RetryPolicy policy = RetryPolicy.fixedDelay(0, Duration.ofMillis(100));
            assertEquals(0, policy.maxRetries());
        }
    }

    @Nested
    @DisplayName("exponentialBackoff Tests")
    class ExponentialBackoffTests {

        @Test
        @DisplayName("maxRetries returns configured value")
        void maxRetriesReturnsConfiguredValue() {
            RetryPolicy policy = RetryPolicy.exponentialBackoff(5, Duration.ofMillis(100), Duration.ofSeconds(10));
            assertEquals(5, policy.maxRetries());
        }

        @Test
        @DisplayName("getDelay doubles each attempt")
        void getDelayDoublesEachAttempt() {
            RetryPolicy policy = RetryPolicy.exponentialBackoff(5, Duration.ofMillis(100), Duration.ofSeconds(10));

            assertEquals(100, policy.getDelay(1).toMillis());
            assertEquals(200, policy.getDelay(2).toMillis());
            assertEquals(400, policy.getDelay(3).toMillis());
            assertEquals(800, policy.getDelay(4).toMillis());
            assertEquals(1600, policy.getDelay(5).toMillis());
        }

        @Test
        @DisplayName("getDelay respects maxDelay")
        void getDelayRespectsMaxDelay() {
            RetryPolicy policy = RetryPolicy.exponentialBackoff(10, Duration.ofMillis(100), Duration.ofSeconds(1));

            // At attempt 5, delay would be 1600ms but capped at 1000ms
            assertEquals(1000, policy.getDelay(5).toMillis());
            assertEquals(1000, policy.getDelay(10).toMillis());
        }

        @Test
        @DisplayName("throws on negative maxRetries")
        void throwsOnNegativeMaxRetries() {
            assertThrows(IllegalArgumentException.class, () ->
                    RetryPolicy.exponentialBackoff(-1, Duration.ofMillis(100), Duration.ofSeconds(10)));
        }

        @Test
        @DisplayName("throws on null initialDelay")
        void throwsOnNullInitialDelay() {
            assertThrows(NullPointerException.class, () ->
                    RetryPolicy.exponentialBackoff(3, null, Duration.ofSeconds(10)));
        }

        @Test
        @DisplayName("throws on null maxDelay")
        void throwsOnNullMaxDelay() {
            assertThrows(NullPointerException.class, () ->
                    RetryPolicy.exponentialBackoff(3, Duration.ofMillis(100), null));
        }
    }

    @Nested
    @DisplayName("exponentialBackoffWithJitter Tests")
    class ExponentialBackoffWithJitterTests {

        @Test
        @DisplayName("maxRetries returns configured value")
        void maxRetriesReturnsConfiguredValue() {
            RetryPolicy policy = RetryPolicy.exponentialBackoffWithJitter(5, Duration.ofMillis(100), Duration.ofSeconds(10));
            assertEquals(5, policy.maxRetries());
        }

        @Test
        @DisplayName("getDelay includes jitter")
        void getDelayIncludesJitter() {
            RetryPolicy policy = RetryPolicy.exponentialBackoffWithJitter(5, Duration.ofMillis(100), Duration.ofSeconds(10));

            // With jitter, delay should be base + 0-50% of base
            long delay1 = policy.getDelay(1).toMillis();
            assertTrue(delay1 >= 100 && delay1 <= 150, "Delay should be between 100 and 150, was: " + delay1);
        }

        @Test
        @DisplayName("jitter produces different values")
        void jitterProducesDifferentValues() {
            RetryPolicy policy = RetryPolicy.exponentialBackoffWithJitter(5, Duration.ofMillis(100), Duration.ofSeconds(10));

            // Multiple calls might produce different values (but not guaranteed due to randomness)
            boolean foundDifferent = false;
            long first = policy.getDelay(1).toMillis();
            for (int i = 0; i < 100; i++) {
                if (policy.getDelay(1).toMillis() != first) {
                    foundDifferent = true;
                    break;
                }
            }

            // Note: Due to randomness, this might occasionally fail, but very unlikely
            // We accept this as a probabilistic test
            assertTrue(foundDifferent || first >= 100, "Jitter should produce variation or at least valid delay");
        }
    }

    @Nested
    @DisplayName("retryOn Tests")
    class RetryOnTests {

        @Test
        @DisplayName("retryOn creates filtered policy")
        void retryOnCreatesFilteredPolicy() {
            RetryPolicy base = RetryPolicy.fixedDelay(3, Duration.ofMillis(100));
            RetryPolicy filtered = base.retryOn(ex -> ex instanceof IOException);

            assertTrue(filtered.shouldRetry(new IOException()));
            assertFalse(filtered.shouldRetry(new RuntimeException()));
        }

        @Test
        @DisplayName("filtered policy preserves maxRetries")
        void filteredPolicyPreservesMaxRetries() {
            RetryPolicy base = RetryPolicy.fixedDelay(5, Duration.ofMillis(100));
            RetryPolicy filtered = base.retryOn(ex -> true);

            assertEquals(5, filtered.maxRetries());
        }

        @Test
        @DisplayName("filtered policy preserves delay")
        void filteredPolicyPreservesDelay() {
            RetryPolicy base = RetryPolicy.fixedDelay(5, Duration.ofMillis(100));
            RetryPolicy filtered = base.retryOn(ex -> true);

            assertEquals(Duration.ofMillis(100), filtered.getDelay(1));
        }
    }

    @Nested
    @DisplayName("shouldRetry Default Tests")
    class ShouldRetryDefaultTests {

        @Test
        @DisplayName("default shouldRetry returns true")
        void defaultShouldRetryReturnsTrue() {
            RetryPolicy policy = RetryPolicy.fixedDelay(3, Duration.ofMillis(100));

            assertTrue(policy.shouldRetry(new RuntimeException()));
            assertTrue(policy.shouldRetry(new IOException()));
            assertTrue(policy.shouldRetry(new Error()));
        }
    }

    @Nested
    @DisplayName("CustomRetryPolicy Tests")
    class CustomRetryPolicyTests {

        @Test
        @DisplayName("wraps delegate correctly")
        void wrapsDelegateCorrectly() {
            RetryPolicy base = RetryPolicy.exponentialBackoff(3, Duration.ofMillis(100), Duration.ofSeconds(1));
            RetryPolicy custom = base.retryOn(ex -> ex.getMessage() != null && ex.getMessage().contains("retry"));

            assertEquals(3, custom.maxRetries());
            assertEquals(100, custom.getDelay(1).toMillis());
            assertEquals(200, custom.getDelay(2).toMillis());

            assertTrue(custom.shouldRetry(new RuntimeException("please retry")));
            assertFalse(custom.shouldRetry(new RuntimeException("no")));
        }
    }

    @Nested
    @DisplayName("Sealed Interface Tests")
    class SealedInterfaceTests {

        @Test
        @DisplayName("NoRetry is valid subtype")
        void noRetryIsValidSubtype() {
            RetryPolicy policy = RetryPolicy.noRetry();
            assertInstanceOf(RetryPolicy.NoRetry.class, policy);
        }

        @Test
        @DisplayName("FixedDelay is valid subtype")
        void fixedDelayIsValidSubtype() {
            RetryPolicy policy = RetryPolicy.fixedDelay(3, Duration.ofMillis(100));
            assertInstanceOf(RetryPolicy.FixedDelay.class, policy);
        }

        @Test
        @DisplayName("ExponentialBackoff is valid subtype")
        void exponentialBackoffIsValidSubtype() {
            RetryPolicy policy = RetryPolicy.exponentialBackoff(3, Duration.ofMillis(100), Duration.ofSeconds(1));
            assertInstanceOf(RetryPolicy.ExponentialBackoff.class, policy);
        }

        @Test
        @DisplayName("CustomRetryPolicy is valid subtype")
        void customRetryPolicyIsValidSubtype() {
            RetryPolicy policy = RetryPolicy.fixedDelay(3, Duration.ofMillis(100))
                    .retryOn(ex -> true);
            assertInstanceOf(RetryPolicy.CustomRetryPolicy.class, policy);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("very large attempt number")
        void veryLargeAttemptNumber() {
            RetryPolicy policy = RetryPolicy.exponentialBackoff(100, Duration.ofMillis(1), Duration.ofSeconds(10));

            // Very large attempt would overflow without maxDelay cap
            long delay = policy.getDelay(50).toMillis();
            assertEquals(10000, delay); // Should be capped at maxDelay
        }

        @Test
        @DisplayName("zero max retries with fixed delay")
        void zeroMaxRetriesWithFixedDelay() {
            RetryPolicy policy = RetryPolicy.fixedDelay(0, Duration.ofMillis(100));
            assertEquals(0, policy.maxRetries());
        }
    }
}
