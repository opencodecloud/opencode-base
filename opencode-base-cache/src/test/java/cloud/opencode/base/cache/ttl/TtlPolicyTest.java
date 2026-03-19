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
 * Comprehensive tests for TtlPolicy
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("TtlPolicy Tests")
class TtlPolicyTest {

    @Nested
    @DisplayName("fixed Tests")
    class FixedTests {

        @Test
        @DisplayName("fixed returns constant TTL")
        void fixedReturnsConstantTtl() {
            TtlPolicy<String, String> policy = TtlPolicy.fixed(Duration.ofMinutes(30));

            assertEquals(Duration.ofMinutes(30), policy.calculateTtl("key1", "value1"));
            assertEquals(Duration.ofMinutes(30), policy.calculateTtl("key2", "value2"));
        }

        @Test
        @DisplayName("fixed throws on null TTL")
        void fixedThrowsOnNullTtl() {
            assertThrows(NullPointerException.class, () -> TtlPolicy.fixed(null));
        }
    }

    @Nested
    @DisplayName("noExpiration Tests")
    class NoExpirationTests {

        @Test
        @DisplayName("noExpiration returns null")
        void noExpirationReturnsNull() {
            TtlPolicy<String, String> policy = TtlPolicy.noExpiration();

            assertNull(policy.calculateTtl("key", "value"));
        }
    }

    @Nested
    @DisplayName("byKey Tests")
    class ByKeyTests {

        @Test
        @DisplayName("byKey uses key to calculate TTL")
        void byKeyUsesKeyToCalculateTtl() {
            TtlPolicy<String, String> policy = TtlPolicy.byKey(key -> {
                if (key.startsWith("session:")) {
                    return Duration.ofHours(1);
                }
                return Duration.ofMinutes(10);
            });

            assertEquals(Duration.ofHours(1), policy.calculateTtl("session:123", "data"));
            assertEquals(Duration.ofMinutes(10), policy.calculateTtl("user:456", "data"));
        }

        @Test
        @DisplayName("byKey throws on null calculator")
        void byKeyThrowsOnNullCalculator() {
            assertThrows(NullPointerException.class, () -> TtlPolicy.byKey(null));
        }
    }

    @Nested
    @DisplayName("byValue Tests")
    class ByValueTests {

        @Test
        @DisplayName("byValue uses key and value to calculate TTL")
        void byValueUsesKeyAndValueToCalculateTtl() {
            TtlPolicy<String, Integer> policy = TtlPolicy.byValue((key, value) -> {
                if (value > 100) {
                    return Duration.ofHours(1);
                }
                return Duration.ofMinutes(5);
            });

            assertEquals(Duration.ofHours(1), policy.calculateTtl("key", 150));
            assertEquals(Duration.ofMinutes(5), policy.calculateTtl("key", 50));
        }

        @Test
        @DisplayName("byValue throws on null calculator")
        void byValueThrowsOnNullCalculator() {
            assertThrows(NullPointerException.class, () -> TtlPolicy.byValue(null));
        }
    }

    @Nested
    @DisplayName("PatternBuilder Tests")
    class PatternBuilderTests {

        @Test
        @DisplayName("pattern matches glob patterns")
        void patternMatchesGlobPatterns() {
            TtlPolicy<String, String> policy = TtlPolicy.<String, String>builder()
                    .pattern("session:*", Duration.ofHours(1))
                    .pattern("user:*", Duration.ofMinutes(30))
                    .defaultTtl(Duration.ofMinutes(10))
                    .build();

            assertEquals(Duration.ofHours(1), policy.calculateTtl("session:abc123", "data"));
            assertEquals(Duration.ofMinutes(30), policy.calculateTtl("user:456", "data"));
            assertEquals(Duration.ofMinutes(10), policy.calculateTtl("other:key", "data"));
        }

        @Test
        @DisplayName("pattern handles question mark wildcard")
        void patternHandlesQuestionMarkWildcard() {
            TtlPolicy<String, String> policy = TtlPolicy.<String, String>builder()
                    .pattern("key?", Duration.ofMinutes(30))
                    .defaultTtl(Duration.ofMinutes(10))
                    .build();

            assertEquals(Duration.ofMinutes(30), policy.calculateTtl("key1", "data"));
            assertEquals(Duration.ofMinutes(30), policy.calculateTtl("keyA", "data"));
            assertEquals(Duration.ofMinutes(10), policy.calculateTtl("key12", "data"));
        }

        @Test
        @DisplayName("regex matches regex patterns")
        void regexMatchesRegexPatterns() {
            TtlPolicy<String, String> policy = TtlPolicy.<String, String>builder()
                    .regex("^user:[0-9]+$", Duration.ofHours(2))
                    .defaultTtl(Duration.ofMinutes(10))
                    .build();

            assertEquals(Duration.ofHours(2), policy.calculateTtl("user:12345", "data"));
            assertEquals(Duration.ofMinutes(10), policy.calculateTtl("user:abc", "data"));
        }

        @Test
        @DisplayName("first matching pattern wins")
        void firstMatchingPatternWins() {
            TtlPolicy<String, String> policy = TtlPolicy.<String, String>builder()
                    .pattern("user:admin*", Duration.ofHours(24))
                    .pattern("user:*", Duration.ofHours(1))
                    .defaultTtl(Duration.ofMinutes(10))
                    .build();

            assertEquals(Duration.ofHours(24), policy.calculateTtl("user:admin", "data"));
            assertEquals(Duration.ofHours(1), policy.calculateTtl("user:regular", "data"));
        }

        @Test
        @DisplayName("defaultTtl is used when no pattern matches")
        void defaultTtlIsUsedWhenNoPatternMatches() {
            TtlPolicy<String, String> policy = TtlPolicy.<String, String>builder()
                    .pattern("session:*", Duration.ofHours(1))
                    .defaultTtl(Duration.ofMinutes(5))
                    .build();

            assertEquals(Duration.ofMinutes(5), policy.calculateTtl("unknown:key", "data"));
        }

        @Test
        @DisplayName("pattern escapes dots in glob")
        void patternEscapesDotsInGlob() {
            TtlPolicy<String, String> policy = TtlPolicy.<String, String>builder()
                    .pattern("file.txt", Duration.ofMinutes(30))
                    .defaultTtl(Duration.ofMinutes(10))
                    .build();

            assertEquals(Duration.ofMinutes(30), policy.calculateTtl("file.txt", "data"));
            assertEquals(Duration.ofMinutes(10), policy.calculateTtl("fileXtxt", "data"));
        }
    }

    @Nested
    @DisplayName("withMinimum Tests")
    class WithMinimumTests {

        @Test
        @DisplayName("withMinimum enforces minimum TTL")
        void withMinimumEnforcesMinimumTtl() {
            TtlPolicy<String, String> policy = TtlPolicy.<String, String>fixed(Duration.ofSeconds(30))
                    .withMinimum(Duration.ofMinutes(1));

            assertEquals(Duration.ofMinutes(1), policy.calculateTtl("key", "value"));
        }

        @Test
        @DisplayName("withMinimum preserves longer TTL")
        void withMinimumPreservesLongerTtl() {
            TtlPolicy<String, String> policy = TtlPolicy.<String, String>fixed(Duration.ofMinutes(5))
                    .withMinimum(Duration.ofMinutes(1));

            assertEquals(Duration.ofMinutes(5), policy.calculateTtl("key", "value"));
        }

        @Test
        @DisplayName("withMinimum preserves null")
        void withMinimumPreservesNull() {
            TtlPolicy<String, String> policy = TtlPolicy.<String, String>noExpiration()
                    .withMinimum(Duration.ofMinutes(1));

            assertNull(policy.calculateTtl("key", "value"));
        }
    }

    @Nested
    @DisplayName("withMaximum Tests")
    class WithMaximumTests {

        @Test
        @DisplayName("withMaximum enforces maximum TTL")
        void withMaximumEnforcesMaximumTtl() {
            TtlPolicy<String, String> policy = TtlPolicy.<String, String>fixed(Duration.ofHours(2))
                    .withMaximum(Duration.ofHours(1));

            assertEquals(Duration.ofHours(1), policy.calculateTtl("key", "value"));
        }

        @Test
        @DisplayName("withMaximum preserves shorter TTL")
        void withMaximumPreservesShorterTtl() {
            TtlPolicy<String, String> policy = TtlPolicy.<String, String>fixed(Duration.ofMinutes(30))
                    .withMaximum(Duration.ofHours(1));

            assertEquals(Duration.ofMinutes(30), policy.calculateTtl("key", "value"));
        }

        @Test
        @DisplayName("withMaximum caps no-expiration")
        void withMaximumCapsNoExpiration() {
            TtlPolicy<String, String> policy = TtlPolicy.<String, String>noExpiration()
                    .withMaximum(Duration.ofHours(1));

            assertEquals(Duration.ofHours(1), policy.calculateTtl("key", "value"));
        }
    }

    @Nested
    @DisplayName("withJitter Tests")
    class WithJitterTests {

        @Test
        @DisplayName("withJitter adds variation to TTL")
        void withJitterAddsVariationToTtl() {
            TtlPolicy<String, String> policy = TtlPolicy.<String, String>fixed(Duration.ofSeconds(100))
                    .withJitter(0.2);

            Duration ttl = policy.calculateTtl("key", "value");
            // With 20% jitter, TTL should be between 80 and 120 seconds
            assertTrue(ttl.toMillis() >= 80000 && ttl.toMillis() <= 120000,
                    "TTL should be between 80 and 120 seconds, was: " + ttl.toMillis());
        }

        @Test
        @DisplayName("withJitter preserves null")
        void withJitterPreservesNull() {
            TtlPolicy<String, String> policy = TtlPolicy.<String, String>noExpiration()
                    .withJitter(0.2);

            assertNull(policy.calculateTtl("key", "value"));
        }
    }

    @Nested
    @DisplayName("Composition Tests")
    class CompositionTests {

        @Test
        @DisplayName("chaining withMinimum and withMaximum")
        void chainingWithMinimumAndWithMaximum() {
            TtlPolicy<String, String> policy = TtlPolicy.<String, String>byKey(key -> {
                if (key.startsWith("long:")) return Duration.ofHours(10);
                if (key.startsWith("short:")) return Duration.ofSeconds(10);
                return Duration.ofMinutes(30);
            })
                    .withMinimum(Duration.ofMinutes(1))
                    .withMaximum(Duration.ofHours(2));

            // 10 hours capped to 2 hours
            assertEquals(Duration.ofHours(2), policy.calculateTtl("long:key", "value"));
            // 10 seconds raised to 1 minute
            assertEquals(Duration.ofMinutes(1), policy.calculateTtl("short:key", "value"));
            // 30 minutes unchanged
            assertEquals(Duration.ofMinutes(30), policy.calculateTtl("normal:key", "value"));
        }
    }

    @Nested
    @DisplayName("Functional Interface Tests")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("TtlPolicy is functional interface")
        void ttlPolicyIsFunctionalInterface() {
            TtlPolicy<String, Integer> policy = (key, value) ->
                    Duration.ofMinutes(value);

            assertEquals(Duration.ofMinutes(30), policy.calculateTtl("key", 30));
        }
    }
}
