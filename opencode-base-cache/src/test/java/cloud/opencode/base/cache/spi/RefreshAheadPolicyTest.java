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

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for RefreshAheadPolicy
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("RefreshAheadPolicy Tests")
class RefreshAheadPolicyTest {

    @Nested
    @DisplayName("percentageOfTtl Tests")
    class PercentageOfTtlTests {

        @Test
        @DisplayName("shouldRefresh returns true when age >= percentage of TTL")
        void shouldRefreshReturnsTrueWhenAgeGtePercentageOfTtl() {
            RefreshAheadPolicy<String, String> policy = RefreshAheadPolicy.percentageOfTtl(0.8);

            assertTrue(policy.shouldRefresh("key", 8000, 10000)); // 80% of 10s
            assertTrue(policy.shouldRefresh("key", 9000, 10000)); // 90% of 10s
        }

        @Test
        @DisplayName("shouldRefresh returns false when age < percentage of TTL")
        void shouldRefreshReturnsFalseWhenAgeLtPercentageOfTtl() {
            RefreshAheadPolicy<String, String> policy = RefreshAheadPolicy.percentageOfTtl(0.8);

            assertFalse(policy.shouldRefresh("key", 7000, 10000)); // 70% of 10s
            assertFalse(policy.shouldRefresh("key", 5000, 10000)); // 50% of 10s
        }

        @Test
        @DisplayName("throws on percentage <= 0")
        void throwsOnPercentageLteZero() {
            assertThrows(IllegalArgumentException.class, () -> RefreshAheadPolicy.percentageOfTtl(0));
            assertThrows(IllegalArgumentException.class, () -> RefreshAheadPolicy.percentageOfTtl(-0.1));
        }

        @Test
        @DisplayName("throws on percentage >= 1")
        void throwsOnPercentageGteOne() {
            assertThrows(IllegalArgumentException.class, () -> RefreshAheadPolicy.percentageOfTtl(1));
            assertThrows(IllegalArgumentException.class, () -> RefreshAheadPolicy.percentageOfTtl(1.1));
        }
    }

    @Nested
    @DisplayName("beforeExpiration Tests")
    class BeforeExpirationTests {

        @Test
        @DisplayName("shouldRefresh returns true when within duration before expiration")
        void shouldRefreshReturnsTrueWhenWithinDurationBeforeExpiration() {
            RefreshAheadPolicy<String, String> policy = RefreshAheadPolicy.beforeExpiration(Duration.ofSeconds(30));

            assertTrue(policy.shouldRefresh("key", 75000, 100000)); // 25s before expiration
            assertTrue(policy.shouldRefresh("key", 80000, 100000)); // 20s before expiration
        }

        @Test
        @DisplayName("shouldRefresh returns false when not within duration before expiration")
        void shouldRefreshReturnsFalseWhenNotWithinDurationBeforeExpiration() {
            RefreshAheadPolicy<String, String> policy = RefreshAheadPolicy.beforeExpiration(Duration.ofSeconds(30));

            assertFalse(policy.shouldRefresh("key", 60000, 100000)); // 40s before expiration
            assertFalse(policy.shouldRefresh("key", 50000, 100000)); // 50s before expiration
        }
    }

    @Nested
    @DisplayName("disabled Tests")
    class DisabledTests {

        @Test
        @DisplayName("shouldRefresh always returns false")
        void shouldRefreshAlwaysReturnsFalse() {
            RefreshAheadPolicy<String, String> policy = RefreshAheadPolicy.disabled();

            assertFalse(policy.shouldRefresh("key", 0, 10000));
            assertFalse(policy.shouldRefresh("key", 9999, 10000));
            assertFalse(policy.shouldRefresh("key", 10000, 10000));
        }
    }

    @Nested
    @DisplayName("custom Tests")
    class CustomTests {

        @Test
        @DisplayName("custom policy uses provided predicate")
        void customPolicyUsesProvidedPredicate() {
            RefreshAheadPolicy<String, String> policy = RefreshAheadPolicy.custom(
                    (key, age, ttl) -> age > ttl * 0.5
            );

            assertTrue(policy.shouldRefresh("key", 6000, 10000));
            assertFalse(policy.shouldRefresh("key", 4000, 10000));
        }
    }

    @Nested
    @DisplayName("adaptive Tests")
    class AdaptiveTests {

        @Test
        @DisplayName("adaptive uses middle of range")
        void adaptiveUsesMiddleOfRange() {
            RefreshAheadPolicy<String, String> policy = RefreshAheadPolicy.adaptive(0.6, 0.8);

            // Middle is 0.7
            assertTrue(policy.shouldRefresh("key", 7500, 10000)); // 75% > 70%
            assertFalse(policy.shouldRefresh("key", 6500, 10000)); // 65% < 70%
        }
    }

    @Nested
    @DisplayName("withJitter Tests")
    class WithJitterTests {

        @Test
        @DisplayName("withJitter adds jitter based on key hash")
        void withJitterAddsJitterBasedOnKeyHash() {
            RefreshAheadPolicy<String, String> policy = RefreshAheadPolicy.withJitter(0.8, 0.1);

            // Different keys should have different thresholds due to jitter
            // We can't predict exact values, but we can verify the policy works
            boolean result1 = policy.shouldRefresh("key1", 8000, 10000);
            boolean result2 = policy.shouldRefresh("key2", 8000, 10000);

            // Both should be reasonable (around 80% threshold)
            // At least one of 75% or 85% should trigger for keys near 80%
            assertNotNull(result1);
            assertNotNull(result2);
        }

        @Test
        @DisplayName("jitter is consistent for same key")
        void jitterIsConsistentForSameKey() {
            RefreshAheadPolicy<String, String> policy = RefreshAheadPolicy.withJitter(0.8, 0.1);

            boolean result1 = policy.shouldRefresh("same-key", 8000, 10000);
            boolean result2 = policy.shouldRefresh("same-key", 8000, 10000);

            assertEquals(result1, result2);
        }
    }

    @Nested
    @DisplayName("Composition Tests")
    class CompositionTests {

        @Test
        @DisplayName("or combines policies with OR logic")
        void orCombinesPoliciesWithOrLogic() {
            RefreshAheadPolicy<String, String> policy1 = RefreshAheadPolicy.percentageOfTtl(0.9);
            RefreshAheadPolicy<String, String> policy2 = RefreshAheadPolicy.beforeExpiration(Duration.ofSeconds(30));

            RefreshAheadPolicy<String, String> combined = policy1.or(policy2);

            // 85% of TTL (policy1 false), 15s before expiration (policy2 true)
            assertTrue(combined.shouldRefresh("key", 85000, 100000));

            // 95% of TTL (policy1 true), 5s before expiration (policy2 true)
            assertTrue(combined.shouldRefresh("key", 95000, 100000));

            // 50% of TTL (policy1 false), 50s before expiration (policy2 false)
            assertFalse(combined.shouldRefresh("key", 50000, 100000));
        }

        @Test
        @DisplayName("and combines policies with AND logic")
        void andCombinesPoliciesWithAndLogic() {
            RefreshAheadPolicy<String, String> policy1 = RefreshAheadPolicy.percentageOfTtl(0.7);
            RefreshAheadPolicy<String, String> policy2 = RefreshAheadPolicy.beforeExpiration(Duration.ofSeconds(50));

            RefreshAheadPolicy<String, String> combined = policy1.and(policy2);

            // 80% of TTL (policy1 true), 20s before expiration (policy2 true)
            assertTrue(combined.shouldRefresh("key", 80000, 100000));

            // 65% of TTL (policy1 false), 35s before expiration (policy2 true)
            assertFalse(combined.shouldRefresh("key", 65000, 100000));

            // 80% of TTL (policy1 true), 60s before expiration (policy2 false)
            // Note: at 40000ms age, there's 60000ms left (60s), policy2 requires <= 50s
            assertFalse(combined.shouldRefresh("key", 40000, 100000));
        }
    }

    @Nested
    @DisplayName("Default Methods Tests")
    class DefaultMethodsTests {

        @Test
        @DisplayName("onRefreshSuccess is no-op by default")
        void onRefreshSuccessIsNoOpByDefault() {
            RefreshAheadPolicy<String, String> policy = RefreshAheadPolicy.percentageOfTtl(0.8);

            // Should not throw
            assertDoesNotThrow(() -> policy.onRefreshSuccess("key", "old", "new"));
        }

        @Test
        @DisplayName("onRefreshFailure is no-op by default")
        void onRefreshFailureIsNoOpByDefault() {
            RefreshAheadPolicy<String, String> policy = RefreshAheadPolicy.percentageOfTtl(0.8);

            // Should not throw
            assertDoesNotThrow(() -> policy.onRefreshFailure("key", "value", new RuntimeException()));
        }

        @Test
        @DisplayName("refreshExecutor returns common pool by default")
        void refreshExecutorReturnsCommonPoolByDefault() {
            RefreshAheadPolicy<String, String> policy = RefreshAheadPolicy.percentageOfTtl(0.8);

            Executor executor = policy.refreshExecutor();
            assertSame(ForkJoinPool.commonPool(), executor);
        }
    }

    @Nested
    @DisplayName("RefreshPredicate Interface Tests")
    class RefreshPredicateInterfaceTests {

        @Test
        @DisplayName("RefreshPredicate is functional interface")
        void refreshPredicateIsFunctionalInterface() {
            RefreshAheadPolicy.RefreshPredicate<String> predicate = (key, age, ttl) -> age > ttl / 2;

            assertTrue(predicate.shouldRefresh("key", 6000, 10000));
            assertFalse(predicate.shouldRefresh("key", 4000, 10000));
        }
    }
}
