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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for DistributedCacheConfig
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("DistributedCacheConfig Tests")
class DistributedCacheConfigTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("builder creates config")
        void builderCreatesConfig() {
            DistributedCacheConfig config = DistributedCacheConfig.builder()
                    .name("test-cache")
                    .build();

            assertNotNull(config);
            assertEquals("test-cache", config.name());
        }

        @Test
        @DisplayName("withDefaults creates default config")
        void withDefaultsCreatesDefaultConfig() {
            DistributedCacheConfig config = DistributedCacheConfig.withDefaults("my-cache");

            assertEquals("my-cache", config.name());
            assertEquals(Duration.ofHours(1), config.defaultTtl());
            assertEquals(3, config.maxRetries());
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("builder sets name")
        void builderSetsName() {
            DistributedCacheConfig config = DistributedCacheConfig.builder()
                    .name("custom-name")
                    .build();

            assertEquals("custom-name", config.name());
        }

        @Test
        @DisplayName("builder sets keyPrefix")
        void builderSetsKeyPrefix() {
            DistributedCacheConfig config = DistributedCacheConfig.builder()
                    .keyPrefix("user:")
                    .build();

            assertEquals("user:", config.keyPrefix());
        }

        @Test
        @DisplayName("builder sets null keyPrefix to empty")
        void builderSetsNullKeyPrefixToEmpty() {
            DistributedCacheConfig config = DistributedCacheConfig.builder()
                    .keyPrefix(null)
                    .build();

            assertEquals("", config.keyPrefix());
        }

        @Test
        @DisplayName("builder sets defaultTtl")
        void builderSetsDefaultTtl() {
            DistributedCacheConfig config = DistributedCacheConfig.builder()
                    .defaultTtl(Duration.ofMinutes(30))
                    .build();

            assertEquals(Duration.ofMinutes(30), config.defaultTtl());
        }

        @Test
        @DisplayName("builder sets connectionTimeout")
        void builderSetsConnectionTimeout() {
            DistributedCacheConfig config = DistributedCacheConfig.builder()
                    .connectionTimeout(Duration.ofSeconds(15))
                    .build();

            assertEquals(Duration.ofSeconds(15), config.connectionTimeout());
        }

        @Test
        @DisplayName("builder sets operationTimeout")
        void builderSetsOperationTimeout() {
            DistributedCacheConfig config = DistributedCacheConfig.builder()
                    .operationTimeout(Duration.ofSeconds(5))
                    .build();

            assertEquals(Duration.ofSeconds(5), config.operationTimeout());
        }

        @Test
        @DisplayName("builder sets maxRetries")
        void builderSetsMaxRetries() {
            DistributedCacheConfig config = DistributedCacheConfig.builder()
                    .maxRetries(5)
                    .build();

            assertEquals(5, config.maxRetries());
        }

        @Test
        @DisplayName("builder sets maxRetries to 0 for negative")
        void builderSetsMaxRetriesToZeroForNegative() {
            DistributedCacheConfig config = DistributedCacheConfig.builder()
                    .maxRetries(-1)
                    .build();

            assertEquals(0, config.maxRetries());
        }

        @Test
        @DisplayName("builder sets retryBackoff")
        void builderSetsRetryBackoff() {
            DistributedCacheConfig config = DistributedCacheConfig.builder()
                    .retryBackoff(Duration.ofMillis(200))
                    .build();

            assertEquals(Duration.ofMillis(200), config.retryBackoff());
        }

        @Test
        @DisplayName("builder sets enableCompression")
        void builderSetsEnableCompression() {
            DistributedCacheConfig config = DistributedCacheConfig.builder()
                    .enableCompression(true)
                    .build();

            assertTrue(config.enableCompression());
        }

        @Test
        @DisplayName("builder sets compressionThreshold")
        void builderSetsCompressionThreshold() {
            DistributedCacheConfig config = DistributedCacheConfig.builder()
                    .compressionThreshold(2048)
                    .build();

            assertEquals(2048, config.compressionThreshold());
        }

        @Test
        @DisplayName("builder sets compressionThreshold to 0 for negative")
        void builderSetsCompressionThresholdToZeroForNegative() {
            DistributedCacheConfig config = DistributedCacheConfig.builder()
                    .compressionThreshold(-100)
                    .build();

            assertEquals(0, config.compressionThreshold());
        }

        @Test
        @DisplayName("builder sets enableStats")
        void builderSetsEnableStats() {
            DistributedCacheConfig config = DistributedCacheConfig.builder()
                    .enableStats(false)
                    .build();

            assertFalse(config.enableStats());
        }

        @Test
        @DisplayName("builder sets enableLocalCache")
        void builderSetsEnableLocalCache() {
            DistributedCacheConfig config = DistributedCacheConfig.builder()
                    .enableLocalCache(true)
                    .build();

            assertTrue(config.enableLocalCache());
        }

        @Test
        @DisplayName("builder sets localCacheSize")
        void builderSetsLocalCacheSize() {
            DistributedCacheConfig config = DistributedCacheConfig.builder()
                    .localCacheSize(500)
                    .build();

            assertEquals(500, config.localCacheSize());
        }

        @Test
        @DisplayName("builder sets localCacheSize to 0 for negative")
        void builderSetsLocalCacheSizeToZeroForNegative() {
            DistributedCacheConfig config = DistributedCacheConfig.builder()
                    .localCacheSize(-100)
                    .build();

            assertEquals(0, config.localCacheSize());
        }

        @Test
        @DisplayName("builder sets localCacheTtl")
        void builderSetsLocalCacheTtl() {
            DistributedCacheConfig config = DistributedCacheConfig.builder()
                    .localCacheTtl(Duration.ofMinutes(10))
                    .build();

            assertEquals(Duration.ofMinutes(10), config.localCacheTtl());
        }

        @Test
        @DisplayName("builder sets keySerializer")
        void builderSetsKeySerializer() {
            DistributedCacheConfig config = DistributedCacheConfig.builder()
                    .keySerializer(null)
                    .build();

            assertNull(config.keySerializer());
        }

        @Test
        @DisplayName("builder sets valueSerializer")
        void builderSetsValueSerializer() {
            DistributedCacheConfig config = DistributedCacheConfig.builder()
                    .valueSerializer(null)
                    .build();

            assertNull(config.valueSerializer());
        }

        @Test
        @DisplayName("builder sets invalidationChannel")
        void builderSetsInvalidationChannel() {
            DistributedCacheConfig config = DistributedCacheConfig.builder()
                    .invalidationChannel("cache:invalidate")
                    .build();

            assertEquals("cache:invalidate", config.invalidationChannel());
        }
    }

    @Nested
    @DisplayName("Null Validation Tests")
    class NullValidationTests {

        @Test
        @DisplayName("name throws on null")
        void nameThrowsOnNull() {
            assertThrows(NullPointerException.class, () ->
                    DistributedCacheConfig.builder().name(null).build());
        }

        @Test
        @DisplayName("defaultTtl throws on null")
        void defaultTtlThrowsOnNull() {
            assertThrows(NullPointerException.class, () ->
                    DistributedCacheConfig.builder().defaultTtl(null).build());
        }

        @Test
        @DisplayName("connectionTimeout throws on null")
        void connectionTimeoutThrowsOnNull() {
            assertThrows(NullPointerException.class, () ->
                    DistributedCacheConfig.builder().connectionTimeout(null).build());
        }

        @Test
        @DisplayName("operationTimeout throws on null")
        void operationTimeoutThrowsOnNull() {
            assertThrows(NullPointerException.class, () ->
                    DistributedCacheConfig.builder().operationTimeout(null).build());
        }

        @Test
        @DisplayName("retryBackoff throws on null")
        void retryBackoffThrowsOnNull() {
            assertThrows(NullPointerException.class, () ->
                    DistributedCacheConfig.builder().retryBackoff(null).build());
        }

        @Test
        @DisplayName("localCacheTtl throws on null")
        void localCacheTtlThrowsOnNull() {
            assertThrows(NullPointerException.class, () ->
                    DistributedCacheConfig.builder().localCacheTtl(null).build());
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @Test
        @DisplayName("default values are set correctly")
        void defaultValuesAreSetCorrectly() {
            DistributedCacheConfig config = DistributedCacheConfig.builder().build();

            assertEquals("default", config.name());
            assertEquals("", config.keyPrefix());
            assertEquals(Duration.ofHours(1), config.defaultTtl());
            assertEquals(Duration.ofSeconds(10), config.connectionTimeout());
            assertEquals(Duration.ofSeconds(3), config.operationTimeout());
            assertEquals(3, config.maxRetries());
            assertEquals(Duration.ofMillis(100), config.retryBackoff());
            assertFalse(config.enableCompression());
            assertEquals(1024, config.compressionThreshold());
            assertTrue(config.enableStats());
            assertFalse(config.enableLocalCache());
            assertEquals(1000, config.localCacheSize());
            assertEquals(Duration.ofMinutes(5), config.localCacheTtl());
        }
    }

    @Nested
    @DisplayName("Record Tests")
    class RecordTests {

        @Test
        @DisplayName("record equality works")
        void recordEqualityWorks() {
            DistributedCacheConfig config1 = DistributedCacheConfig.builder()
                    .name("test")
                    .defaultTtl(Duration.ofMinutes(30))
                    .build();
            DistributedCacheConfig config2 = DistributedCacheConfig.builder()
                    .name("test")
                    .defaultTtl(Duration.ofMinutes(30))
                    .build();

            assertEquals(config1, config2);
            assertEquals(config1.hashCode(), config2.hashCode());
        }

        @Test
        @DisplayName("record toString works")
        void recordToStringWorks() {
            DistributedCacheConfig config = DistributedCacheConfig.builder()
                    .name("test-cache")
                    .build();

            String str = config.toString();
            assertTrue(str.contains("test-cache"));
        }
    }
}
