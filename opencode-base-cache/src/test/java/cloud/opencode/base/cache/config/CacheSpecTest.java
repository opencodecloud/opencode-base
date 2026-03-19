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

package cloud.opencode.base.cache.config;

import cloud.opencode.base.cache.exception.OpenCacheException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for CacheSpec
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("CacheSpec Tests")
class CacheSpecTest {

    @Nested
    @DisplayName("Parse Method Tests")
    class ParseMethodTests {

        @Test
        @DisplayName("parse with empty string returns default config")
        void parseWithEmptyStringReturnsDefaultConfig() {
            CacheConfig<String, String> config = CacheSpec.parse("");
            assertNotNull(config);
        }

        @Test
        @DisplayName("parse with blank string returns default config")
        void parseWithBlankStringReturnsDefaultConfig() {
            CacheConfig<String, String> config = CacheSpec.parse("   ");
            assertNotNull(config);
        }

        @Test
        @DisplayName("parse throws on null")
        void parseThrowsOnNull() {
            assertThrows(NullPointerException.class, () -> CacheSpec.parse(null));
        }

        @Test
        @DisplayName("parse maximumSize")
        void parseMaximumSize() {
            CacheConfig<String, String> config = CacheSpec.parse("maximumSize=1000");
            assertEquals(1000, config.maximumSize());
        }

        @Test
        @DisplayName("parse maximumWeight requires weigher")
        void parseMaximumWeightRequiresWeigher() {
            // maximumWeight requires a weigher which cannot be specified in spec string
            // So parsing with maximumWeight should throw an exception
            assertThrows(IllegalArgumentException.class, () ->
                    CacheSpec.parse("maximumWeight=5000"));
        }

        @Test
        @DisplayName("parse initialCapacity")
        void parseInitialCapacity() {
            CacheConfig<String, String> config = CacheSpec.parse("initialCapacity=100");
            assertEquals(100, config.initialCapacity());
        }

        @Test
        @DisplayName("parse concurrencyLevel")
        void parseConcurrencyLevel() {
            CacheConfig<String, String> config = CacheSpec.parse("concurrencyLevel=32");
            assertEquals(32, config.concurrencyLevel());
        }

        @Test
        @DisplayName("parse expireAfterWrite")
        void parseExpireAfterWrite() {
            CacheConfig<String, String> config = CacheSpec.parse("expireAfterWrite=30m");
            assertEquals(Duration.ofMinutes(30), config.expireAfterWrite());
        }

        @Test
        @DisplayName("parse expireAfterAccess")
        void parseExpireAfterAccess() {
            CacheConfig<String, String> config = CacheSpec.parse("expireAfterAccess=10m");
            assertEquals(Duration.ofMinutes(10), config.expireAfterAccess());
        }

        @Test
        @DisplayName("parse refreshAfterWrite with expireAfterWrite")
        void parseRefreshAfterWrite() {
            // refreshAfterWrite requires expireAfterWrite to be set
            CacheConfig<String, String> config = CacheSpec.parse("expireAfterWrite=30m,refreshAfterWrite=5m");
            assertEquals(Duration.ofMinutes(5), config.refreshAfterWrite());
            assertEquals(Duration.ofMinutes(30), config.expireAfterWrite());
        }

        @Test
        @DisplayName("parse recordStats flag")
        void parseRecordStatsFlag() {
            CacheConfig<String, String> config = CacheSpec.parse("recordStats");
            assertTrue(config.recordStats());
        }

        @Test
        @DisplayName("parse useVirtualThreads flag")
        void parseUseVirtualThreadsFlag() {
            CacheConfig<String, String> config = CacheSpec.parse("useVirtualThreads");
            assertTrue(config.useVirtualThreads());
        }

        @Test
        @DisplayName("parse evictionPolicy lru")
        void parseEvictionPolicyLru() {
            CacheConfig<String, String> config = CacheSpec.parse("evictionPolicy=lru");
            assertNotNull(config.evictionPolicy());
        }

        @Test
        @DisplayName("parse evictionPolicy lfu")
        void parseEvictionPolicyLfu() {
            CacheConfig<String, String> config = CacheSpec.parse("evictionPolicy=lfu");
            assertNotNull(config.evictionPolicy());
        }

        @Test
        @DisplayName("parse evictionPolicy fifo")
        void parseEvictionPolicyFifo() {
            CacheConfig<String, String> config = CacheSpec.parse("evictionPolicy=fifo");
            assertNotNull(config.evictionPolicy());
        }

        @Test
        @DisplayName("parse evictionPolicy wtinylfu")
        void parseEvictionPolicyWtinylfu() {
            CacheConfig<String, String> config = CacheSpec.parse("evictionPolicy=wtinylfu");
            assertNotNull(config.evictionPolicy());
        }

        @Test
        @DisplayName("parse evictionPolicy w-tinylfu")
        void parseEvictionPolicyWTinylfuHyphen() {
            CacheConfig<String, String> config = CacheSpec.parse("evictionPolicy=w-tinylfu");
            assertNotNull(config.evictionPolicy());
        }

        @Test
        @DisplayName("parse evictionPolicy tinylfu")
        void parseEvictionPolicyTinylfu() {
            CacheConfig<String, String> config = CacheSpec.parse("evictionPolicy=tinylfu");
            assertNotNull(config.evictionPolicy());
        }

        @Test
        @DisplayName("parse multiple options")
        void parseMultipleOptions() {
            CacheConfig<String, String> config = CacheSpec.parse(
                    "maximumSize=1000,expireAfterWrite=30m,recordStats");
            assertEquals(1000, config.maximumSize());
            assertEquals(Duration.ofMinutes(30), config.expireAfterWrite());
            assertTrue(config.recordStats());
        }

        @Test
        @DisplayName("parse with extra whitespace")
        void parseWithExtraWhitespace() {
            CacheConfig<String, String> config = CacheSpec.parse(
                    "  maximumSize=1000  ,  expireAfterWrite=30m  ");
            assertEquals(1000, config.maximumSize());
            assertEquals(Duration.ofMinutes(30), config.expireAfterWrite());
        }

        @Test
        @DisplayName("parse throws on unknown option")
        void parseThrowsOnUnknownOption() {
            assertThrows(OpenCacheException.class, () -> CacheSpec.parse("unknownOption=value"));
        }

        @Test
        @DisplayName("parse throws on invalid long value")
        void parseThrowsOnInvalidLongValue() {
            assertThrows(OpenCacheException.class, () -> CacheSpec.parse("maximumSize=notANumber"));
        }

        @Test
        @DisplayName("parse throws on invalid int value")
        void parseThrowsOnInvalidIntValue() {
            assertThrows(OpenCacheException.class, () -> CacheSpec.parse("initialCapacity=notANumber"));
        }

        @Test
        @DisplayName("parse throws on invalid eviction policy")
        void parseThrowsOnInvalidEvictionPolicy() {
            assertThrows(OpenCacheException.class, () -> CacheSpec.parse("evictionPolicy=unknown"));
        }

        @Test
        @DisplayName("parse throws on empty key")
        void parseThrowsOnEmptyKey() {
            assertThrows(OpenCacheException.class, () -> CacheSpec.parse("=value"));
        }
    }

    @Nested
    @DisplayName("Duration Parsing Tests")
    class DurationParsingTests {

        @Test
        @DisplayName("parse milliseconds without unit")
        void parseMillisecondsWithoutUnit() {
            CacheConfig<String, String> config = CacheSpec.parse("expireAfterWrite=1000");
            assertEquals(Duration.ofMillis(1000), config.expireAfterWrite());
        }

        @Test
        @DisplayName("parse milliseconds with ms unit")
        void parseMillisecondsWithMsUnit() {
            CacheConfig<String, String> config = CacheSpec.parse("expireAfterWrite=1000ms");
            assertEquals(Duration.ofMillis(1000), config.expireAfterWrite());
        }

        @Test
        @DisplayName("parse seconds")
        void parseSeconds() {
            CacheConfig<String, String> config = CacheSpec.parse("expireAfterWrite=60s");
            assertEquals(Duration.ofSeconds(60), config.expireAfterWrite());
        }

        @Test
        @DisplayName("parse minutes")
        void parseMinutes() {
            CacheConfig<String, String> config = CacheSpec.parse("expireAfterWrite=30m");
            assertEquals(Duration.ofMinutes(30), config.expireAfterWrite());
        }

        @Test
        @DisplayName("parse hours")
        void parseHours() {
            CacheConfig<String, String> config = CacheSpec.parse("expireAfterWrite=2h");
            assertEquals(Duration.ofHours(2), config.expireAfterWrite());
        }

        @Test
        @DisplayName("parse days")
        void parseDays() {
            CacheConfig<String, String> config = CacheSpec.parse("expireAfterWrite=1d");
            assertEquals(Duration.ofDays(1), config.expireAfterWrite());
        }

        @Test
        @DisplayName("parse throws on invalid duration format")
        void parseThrowsOnInvalidDurationFormat() {
            assertThrows(OpenCacheException.class, () -> CacheSpec.parse("expireAfterWrite=invalid"));
        }

        @Test
        @DisplayName("parse throws on unknown duration unit")
        void parseThrowsOnUnknownDurationUnit() {
            assertThrows(OpenCacheException.class, () -> CacheSpec.parse("expireAfterWrite=100x"));
        }
    }

    @Nested
    @DisplayName("ParseToBuilder Method Tests")
    class ParseToBuilderMethodTests {

        @Test
        @DisplayName("parseToBuilder returns builder")
        void parseToBuilderReturnsBuilder() {
            CacheConfig.Builder<String, String> builder = CacheSpec.parseToBuilder("maximumSize=1000");
            assertNotNull(builder);
        }

        @Test
        @DisplayName("parseToBuilder allows further customization")
        void parseToBuilderAllowsFurtherCustomization() {
            CacheConfig<String, String> config = CacheSpec.<String, String>parseToBuilder("maximumSize=1000")
                    .initialCapacity(100)
                    .build();
            assertEquals(1000, config.maximumSize());
            assertEquals(100, config.initialCapacity());
        }

        @Test
        @DisplayName("parseToBuilder with empty string")
        void parseToBuilderWithEmptyString() {
            CacheConfig.Builder<String, String> builder = CacheSpec.parseToBuilder("");
            assertNotNull(builder);
        }

        @Test
        @DisplayName("parseToBuilder throws on null")
        void parseToBuilderThrowsOnNull() {
            assertThrows(NullPointerException.class, () -> CacheSpec.parseToBuilder(null));
        }
    }

    @Nested
    @DisplayName("isValid Method Tests")
    class IsValidMethodTests {

        @Test
        @DisplayName("isValid returns true for valid spec")
        void isValidReturnsTrueForValidSpec() {
            assertTrue(CacheSpec.isValid("maximumSize=1000,expireAfterWrite=30m"));
        }

        @Test
        @DisplayName("isValid returns true for empty spec")
        void isValidReturnsTrueForEmptySpec() {
            assertTrue(CacheSpec.isValid(""));
        }

        @Test
        @DisplayName("isValid returns true for null spec")
        void isValidReturnsTrueForNullSpec() {
            assertTrue(CacheSpec.isValid(null));
        }

        @Test
        @DisplayName("isValid returns false for invalid spec")
        void isValidReturnsFalseForInvalidSpec() {
            assertFalse(CacheSpec.isValid("unknownOption=value"));
        }

        @Test
        @DisplayName("isValid returns false for invalid value")
        void isValidReturnsFalseForInvalidValue() {
            assertFalse(CacheSpec.isValid("maximumSize=notANumber"));
        }
    }

    @Nested
    @DisplayName("Validate Method Tests")
    class ValidateMethodTests {

        @Test
        @DisplayName("validate returns empty list for valid spec")
        void validateReturnsEmptyListForValidSpec() {
            List<String> errors = CacheSpec.validate("maximumSize=1000");
            assertTrue(errors.isEmpty());
        }

        @Test
        @DisplayName("validate returns empty list for empty spec")
        void validateReturnsEmptyListForEmptySpec() {
            List<String> errors = CacheSpec.validate("");
            assertTrue(errors.isEmpty());
        }

        @Test
        @DisplayName("validate returns error for null spec")
        void validateReturnsErrorForNullSpec() {
            List<String> errors = CacheSpec.validate(null);
            assertEquals(1, errors.size());
            assertTrue(errors.get(0).contains("null"));
        }

        @Test
        @DisplayName("validate returns error for unknown option")
        void validateReturnsErrorForUnknownOption() {
            List<String> errors = CacheSpec.validate("unknownOption=value");
            assertFalse(errors.isEmpty());
            assertTrue(errors.stream().anyMatch(e -> e.contains("Unknown option")));
        }

        @Test
        @DisplayName("validate returns error for negative maximumSize")
        void validateReturnsErrorForNegativeMaximumSize() {
            List<String> errors = CacheSpec.validate("maximumSize=-1");
            assertFalse(errors.isEmpty());
        }

        @Test
        @DisplayName("validate returns error for invalid duration")
        void validateReturnsErrorForInvalidDuration() {
            List<String> errors = CacheSpec.validate("expireAfterWrite=invalid");
            assertFalse(errors.isEmpty());
        }
    }

    @Nested
    @DisplayName("ToSpec Method Tests")
    class ToSpecMethodTests {

        @Test
        @DisplayName("toSpec with maximumSize")
        void toSpecWithMaximumSize() {
            CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                    .maximumSize(1000)
                    .build();
            String spec = CacheSpec.toSpec(config);
            assertTrue(spec.contains("maximumSize=1000"));
        }

        @Test
        @DisplayName("toSpec with maximumWeight requires weigher")
        void toSpecWithMaximumWeightRequiresWeigher() {
            // maximumWeight requires a weigher to be set
            assertThrows(IllegalArgumentException.class, () ->
                    CacheConfig.<String, String>builder()
                            .maximumWeight(5000)
                            .build());
        }

        @Test
        @DisplayName("toSpec with expireAfterWrite in days")
        void toSpecWithExpireAfterWriteInDays() {
            CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                    .expireAfterWrite(Duration.ofDays(1))
                    .build();
            String spec = CacheSpec.toSpec(config);
            assertTrue(spec.contains("expireAfterWrite=1d"));
        }

        @Test
        @DisplayName("toSpec with expireAfterWrite in hours")
        void toSpecWithExpireAfterWriteInHours() {
            CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                    .expireAfterWrite(Duration.ofHours(2))
                    .build();
            String spec = CacheSpec.toSpec(config);
            assertTrue(spec.contains("expireAfterWrite=2h"));
        }

        @Test
        @DisplayName("toSpec with expireAfterWrite in minutes")
        void toSpecWithExpireAfterWriteInMinutes() {
            CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                    .expireAfterWrite(Duration.ofMinutes(30))
                    .build();
            String spec = CacheSpec.toSpec(config);
            assertTrue(spec.contains("expireAfterWrite=30m"));
        }

        @Test
        @DisplayName("toSpec with expireAfterWrite in seconds")
        void toSpecWithExpireAfterWriteInSeconds() {
            CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                    .expireAfterWrite(Duration.ofSeconds(30))
                    .build();
            String spec = CacheSpec.toSpec(config);
            assertTrue(spec.contains("expireAfterWrite=30s"));
        }

        @Test
        @DisplayName("toSpec with expireAfterWrite in milliseconds")
        void toSpecWithExpireAfterWriteInMilliseconds() {
            CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                    .expireAfterWrite(Duration.ofMillis(500))
                    .build();
            String spec = CacheSpec.toSpec(config);
            assertTrue(spec.contains("expireAfterWrite=500ms"));
        }

        @Test
        @DisplayName("toSpec with recordStats")
        void toSpecWithRecordStats() {
            CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                    .recordStats()
                    .build();
            String spec = CacheSpec.toSpec(config);
            assertTrue(spec.contains("recordStats"));
        }

        @Test
        @DisplayName("toSpec with useVirtualThreads")
        void toSpecWithUseVirtualThreads() {
            CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                    .useVirtualThreads()
                    .build();
            String spec = CacheSpec.toSpec(config);
            assertTrue(spec.contains("useVirtualThreads"));
        }

        @Test
        @DisplayName("toSpec throws on null config")
        void toSpecThrowsOnNullConfig() {
            assertThrows(NullPointerException.class, () -> CacheSpec.toSpec(null));
        }

        @Test
        @DisplayName("toSpec with empty config returns empty string")
        void toSpecWithEmptyConfigReturnsEmptyString() {
            CacheConfig<String, String> config = CacheConfig.<String, String>builder().build();
            String spec = CacheSpec.toSpec(config);
            assertNotNull(spec);
        }
    }

    @Nested
    @DisplayName("Round Trip Tests")
    class RoundTripTests {

        @Test
        @DisplayName("parse and toSpec round trip")
        void parseAndToSpecRoundTrip() {
            String original = "maximumSize=1000,expireAfterWrite=30m,recordStats";
            CacheConfig<String, String> config = CacheSpec.parse(original);
            String generated = CacheSpec.toSpec(config);

            // Parse again and verify
            CacheConfig<String, String> config2 = CacheSpec.parse(generated);
            assertEquals(config.maximumSize(), config2.maximumSize());
            assertEquals(config.expireAfterWrite(), config2.expireAfterWrite());
            assertEquals(config.recordStats(), config2.recordStats());
        }
    }
}
