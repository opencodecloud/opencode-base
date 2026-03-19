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

package cloud.opencode.base.cache.ttl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for TtlDecayPolicy
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("TtlDecayPolicy Tests")
class TtlDecayPolicyTest {

    @Nested
    @DisplayName("NoDecay Tests")
    class NoDecayTests {

        @Test
        @DisplayName("none returns constant TTL")
        void noneReturnsConstantTtl() {
            TtlDecayPolicy policy = TtlDecayPolicy.none(Duration.ofMinutes(30));

            assertEquals(Duration.ofMinutes(30), policy.calculateDecayedTtl(0));
            assertEquals(Duration.ofMinutes(30), policy.calculateDecayedTtl(100));
            assertEquals(Duration.ofMinutes(30), policy.calculateDecayedTtl(1000));
        }

        @Test
        @DisplayName("initialTtl returns configured TTL")
        void initialTtlReturnsConfiguredTtl() {
            TtlDecayPolicy policy = TtlDecayPolicy.none(Duration.ofHours(1));
            assertEquals(Duration.ofHours(1), policy.initialTtl());
        }

        @Test
        @DisplayName("minimumTtl returns same as initialTtl")
        void minimumTtlReturnsSameAsInitialTtl() {
            TtlDecayPolicy policy = TtlDecayPolicy.none(Duration.ofHours(1));
            assertEquals(Duration.ofHours(1), policy.minimumTtl());
        }

        @Test
        @DisplayName("NoDecay is valid sealed subtype")
        void noDecayIsValidSealedSubtype() {
            TtlDecayPolicy policy = TtlDecayPolicy.none(Duration.ofMinutes(30));
            assertInstanceOf(TtlDecayPolicy.NoDecay.class, policy);
        }
    }

    @Nested
    @DisplayName("LinearDecay Tests")
    class LinearDecayTests {

        @Test
        @DisplayName("linear decays TTL linearly")
        void linearDecaysTtlLinearly() {
            TtlDecayPolicy policy = TtlDecayPolicy.linear(
                    Duration.ofMinutes(60),  // initial: 60 minutes
                    Duration.ofMinutes(10),  // minimum: 10 minutes
                    10                       // decay over 10 steps
            );
            // Decay per step: (60 - 10) / 10 = 5 minutes

            assertEquals(Duration.ofMinutes(60), policy.calculateDecayedTtl(0));  // No decay
            assertEquals(Duration.ofMinutes(55), policy.calculateDecayedTtl(1));  // 60 - 5
            assertEquals(Duration.ofMinutes(35), policy.calculateDecayedTtl(5));  // 60 - 25
            assertEquals(Duration.ofMinutes(10), policy.calculateDecayedTtl(10)); // Minimum
            assertEquals(Duration.ofMinutes(10), policy.calculateDecayedTtl(100)); // Still minimum
        }

        @Test
        @DisplayName("linear returns initialTtl for negative accessCount")
        void linearReturnsInitialTtlForNegativeAccessCount() {
            TtlDecayPolicy policy = TtlDecayPolicy.linear(
                    Duration.ofMinutes(60), Duration.ofMinutes(10), 10);

            assertEquals(Duration.ofMinutes(60), policy.calculateDecayedTtl(-1));
        }

        @Test
        @DisplayName("linear initialTtl and minimumTtl getters")
        void linearInitialTtlAndMinimumTtlGetters() {
            TtlDecayPolicy policy = TtlDecayPolicy.linear(
                    Duration.ofHours(1), Duration.ofMinutes(5), 10);

            assertEquals(Duration.ofHours(1), policy.initialTtl());
            assertEquals(Duration.ofMinutes(5), policy.minimumTtl());
        }

        @Test
        @DisplayName("linear throws on null initialTtl")
        void linearThrowsOnNullInitialTtl() {
            assertThrows(NullPointerException.class, () ->
                    TtlDecayPolicy.linear(null, Duration.ofMinutes(5), 10));
        }

        @Test
        @DisplayName("linear throws on null minimumTtl")
        void linearThrowsOnNullMinimumTtl() {
            assertThrows(NullPointerException.class, () ->
                    TtlDecayPolicy.linear(Duration.ofHours(1), null, 10));
        }

        @Test
        @DisplayName("linear throws on non-positive decaySteps")
        void linearThrowsOnNonPositiveDecaySteps() {
            assertThrows(IllegalArgumentException.class, () ->
                    TtlDecayPolicy.linear(Duration.ofHours(1), Duration.ofMinutes(5), 0));
            assertThrows(IllegalArgumentException.class, () ->
                    TtlDecayPolicy.linear(Duration.ofHours(1), Duration.ofMinutes(5), -1));
        }

        @Test
        @DisplayName("linear throws when initialTtl < minimumTtl")
        void linearThrowsWhenInitialTtlLessThanMinimumTtl() {
            assertThrows(IllegalArgumentException.class, () ->
                    TtlDecayPolicy.linear(Duration.ofMinutes(5), Duration.ofMinutes(10), 10));
        }

        @Test
        @DisplayName("LinearDecay is valid sealed subtype")
        void linearDecayIsValidSealedSubtype() {
            TtlDecayPolicy policy = TtlDecayPolicy.linear(
                    Duration.ofHours(1), Duration.ofMinutes(5), 10);
            assertInstanceOf(TtlDecayPolicy.LinearDecay.class, policy);
        }
    }

    @Nested
    @DisplayName("ExponentialDecay Tests")
    class ExponentialDecayTests {

        @Test
        @DisplayName("exponential decays TTL exponentially")
        void exponentialDecaysTtlExponentially() {
            TtlDecayPolicy policy = TtlDecayPolicy.exponential(
                    Duration.ofSeconds(1000),  // initial: 1000 seconds
                    Duration.ofSeconds(100),   // minimum: 100 seconds
                    0.5                        // decay factor
            );

            assertEquals(Duration.ofSeconds(1000), policy.calculateDecayedTtl(0));  // No decay
            assertEquals(Duration.ofSeconds(500), policy.calculateDecayedTtl(1));   // 1000 * 0.5
            assertEquals(Duration.ofSeconds(250), policy.calculateDecayedTtl(2));   // 1000 * 0.25
            assertEquals(Duration.ofSeconds(125), policy.calculateDecayedTtl(3));   // 1000 * 0.125
            assertEquals(Duration.ofSeconds(100), policy.calculateDecayedTtl(10));  // Capped at minimum
        }

        @Test
        @DisplayName("exponential returns initialTtl for negative accessCount")
        void exponentialReturnsInitialTtlForNegativeAccessCount() {
            TtlDecayPolicy policy = TtlDecayPolicy.exponential(
                    Duration.ofMinutes(60), Duration.ofMinutes(10), 0.5);

            assertEquals(Duration.ofMinutes(60), policy.calculateDecayedTtl(-1));
        }

        @Test
        @DisplayName("exponential initialTtl and minimumTtl getters")
        void exponentialInitialTtlAndMinimumTtlGetters() {
            TtlDecayPolicy policy = TtlDecayPolicy.exponential(
                    Duration.ofHours(1), Duration.ofMinutes(5), 0.5);

            assertEquals(Duration.ofHours(1), policy.initialTtl());
            assertEquals(Duration.ofMinutes(5), policy.minimumTtl());
        }

        @Test
        @DisplayName("exponential throws on null initialTtl")
        void exponentialThrowsOnNullInitialTtl() {
            assertThrows(NullPointerException.class, () ->
                    TtlDecayPolicy.exponential(null, Duration.ofMinutes(5), 0.5));
        }

        @Test
        @DisplayName("exponential throws on null minimumTtl")
        void exponentialThrowsOnNullMinimumTtl() {
            assertThrows(NullPointerException.class, () ->
                    TtlDecayPolicy.exponential(Duration.ofHours(1), null, 0.5));
        }

        @Test
        @DisplayName("exponential throws on invalid decayFactor")
        void exponentialThrowsOnInvalidDecayFactor() {
            assertThrows(IllegalArgumentException.class, () ->
                    TtlDecayPolicy.exponential(Duration.ofHours(1), Duration.ofMinutes(5), 0));
            assertThrows(IllegalArgumentException.class, () ->
                    TtlDecayPolicy.exponential(Duration.ofHours(1), Duration.ofMinutes(5), 1));
            assertThrows(IllegalArgumentException.class, () ->
                    TtlDecayPolicy.exponential(Duration.ofHours(1), Duration.ofMinutes(5), 1.5));
            assertThrows(IllegalArgumentException.class, () ->
                    TtlDecayPolicy.exponential(Duration.ofHours(1), Duration.ofMinutes(5), -0.5));
        }

        @Test
        @DisplayName("ExponentialDecay is valid sealed subtype")
        void exponentialDecayIsValidSealedSubtype() {
            TtlDecayPolicy policy = TtlDecayPolicy.exponential(
                    Duration.ofHours(1), Duration.ofMinutes(5), 0.5);
            assertInstanceOf(TtlDecayPolicy.ExponentialDecay.class, policy);
        }
    }

    @Nested
    @DisplayName("StepDecay Tests")
    class StepDecayTests {

        @Test
        @DisplayName("step uses thresholds to determine TTL")
        void stepUsesThresholdsToDetermineTtl() {
            TtlDecayPolicy policy = TtlDecayPolicy.step(
                    TtlDecayPolicy.Step.of(0, Duration.ofHours(1)),
                    TtlDecayPolicy.Step.of(5, Duration.ofMinutes(30)),
                    TtlDecayPolicy.Step.of(10, Duration.ofMinutes(10)),
                    TtlDecayPolicy.Step.of(20, Duration.ofMinutes(5))
            );

            assertEquals(Duration.ofHours(1), policy.calculateDecayedTtl(0));
            assertEquals(Duration.ofHours(1), policy.calculateDecayedTtl(4));
            assertEquals(Duration.ofMinutes(30), policy.calculateDecayedTtl(5));
            assertEquals(Duration.ofMinutes(30), policy.calculateDecayedTtl(9));
            assertEquals(Duration.ofMinutes(10), policy.calculateDecayedTtl(10));
            assertEquals(Duration.ofMinutes(10), policy.calculateDecayedTtl(19));
            assertEquals(Duration.ofMinutes(5), policy.calculateDecayedTtl(20));
            assertEquals(Duration.ofMinutes(5), policy.calculateDecayedTtl(100));
        }

        @Test
        @DisplayName("step initialTtl returns first step TTL")
        void stepInitialTtlReturnsFirstStepTtl() {
            TtlDecayPolicy policy = TtlDecayPolicy.step(
                    TtlDecayPolicy.Step.of(0, Duration.ofHours(1)),
                    TtlDecayPolicy.Step.of(5, Duration.ofMinutes(30))
            );

            assertEquals(Duration.ofHours(1), policy.initialTtl());
        }

        @Test
        @DisplayName("step minimumTtl returns last step TTL")
        void stepMinimumTtlReturnsLastStepTtl() {
            TtlDecayPolicy policy = TtlDecayPolicy.step(
                    TtlDecayPolicy.Step.of(0, Duration.ofHours(1)),
                    TtlDecayPolicy.Step.of(5, Duration.ofMinutes(30)),
                    TtlDecayPolicy.Step.of(10, Duration.ofMinutes(5))
            );

            assertEquals(Duration.ofMinutes(5), policy.minimumTtl());
        }

        @Test
        @DisplayName("step throws on empty steps")
        void stepThrowsOnEmptySteps() {
            assertThrows(IllegalArgumentException.class, () ->
                    TtlDecayPolicy.step());
        }

        @Test
        @DisplayName("step throws on null steps")
        void stepThrowsOnNullSteps() {
            assertThrows(IllegalArgumentException.class, () ->
                    TtlDecayPolicy.step((TtlDecayPolicy.Step[]) null));
        }

        @Test
        @DisplayName("StepDecay is valid sealed subtype")
        void stepDecayIsValidSealedSubtype() {
            TtlDecayPolicy policy = TtlDecayPolicy.step(
                    TtlDecayPolicy.Step.of(0, Duration.ofHours(1))
            );
            assertInstanceOf(TtlDecayPolicy.StepDecay.class, policy);
        }
    }

    @Nested
    @DisplayName("Step Record Tests")
    class StepRecordTests {

        @Test
        @DisplayName("Step.of creates step")
        void stepOfCreatesStep() {
            TtlDecayPolicy.Step step = TtlDecayPolicy.Step.of(10, Duration.ofMinutes(30));

            assertEquals(10, step.threshold());
            assertEquals(Duration.ofMinutes(30), step.ttl());
        }

        @Test
        @DisplayName("Step throws on null TTL")
        void stepThrowsOnNullTtl() {
            assertThrows(NullPointerException.class, () ->
                    TtlDecayPolicy.Step.of(0, null));
            assertThrows(NullPointerException.class, () ->
                    new TtlDecayPolicy.Step(0, null));
        }

        @Test
        @DisplayName("Step throws on negative threshold")
        void stepThrowsOnNegativeThreshold() {
            assertThrows(IllegalArgumentException.class, () ->
                    TtlDecayPolicy.Step.of(-1, Duration.ofMinutes(30)));
        }

        @Test
        @DisplayName("Step accepts zero threshold")
        void stepAcceptsZeroThreshold() {
            TtlDecayPolicy.Step step = TtlDecayPolicy.Step.of(0, Duration.ofMinutes(30));
            assertEquals(0, step.threshold());
        }
    }

    @Nested
    @DisplayName("Sealed Interface Tests")
    class SealedInterfaceTests {

        @Test
        @DisplayName("all implementations are valid subtypes")
        void allImplementationsAreValidSubtypes() {
            TtlDecayPolicy noDecay = TtlDecayPolicy.none(Duration.ofMinutes(30));
            TtlDecayPolicy linear = TtlDecayPolicy.linear(Duration.ofHours(1), Duration.ofMinutes(5), 10);
            TtlDecayPolicy exponential = TtlDecayPolicy.exponential(Duration.ofHours(1), Duration.ofMinutes(5), 0.5);
            TtlDecayPolicy step = TtlDecayPolicy.step(TtlDecayPolicy.Step.of(0, Duration.ofMinutes(30)));

            assertInstanceOf(TtlDecayPolicy.NoDecay.class, noDecay);
            assertInstanceOf(TtlDecayPolicy.LinearDecay.class, linear);
            assertInstanceOf(TtlDecayPolicy.ExponentialDecay.class, exponential);
            assertInstanceOf(TtlDecayPolicy.StepDecay.class, step);
        }

        @Test
        @DisplayName("pattern matching works with sealed interface")
        void patternMatchingWorksWithSealedInterface() {
            TtlDecayPolicy policy = TtlDecayPolicy.linear(Duration.ofHours(1), Duration.ofMinutes(5), 10);

            String result = switch (policy) {
                case TtlDecayPolicy.NoDecay nd -> "no-decay";
                case TtlDecayPolicy.LinearDecay ld -> "linear-decay-" + ld.decaySteps();
                case TtlDecayPolicy.ExponentialDecay ed -> "exponential-decay-" + ed.decayFactor();
                case TtlDecayPolicy.StepDecay sd -> "step-decay-" + sd.steps().length;
            };

            assertEquals("linear-decay-10", result);
        }
    }
}
