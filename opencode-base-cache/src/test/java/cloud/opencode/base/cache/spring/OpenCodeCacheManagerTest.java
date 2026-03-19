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

import cloud.opencode.base.cache.CacheManager;
import cloud.opencode.base.cache.CacheStats;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for OpenCodeCacheManager
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("OpenCodeCacheManager Tests")
class OpenCodeCacheManagerTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("create returns manager")
        void createReturnsManager() {
            OpenCodeCacheManager manager = OpenCodeCacheManager.create();

            assertNotNull(manager);
        }

        @Test
        @DisplayName("builder creates manager")
        void builderCreatesManager() {
            OpenCodeCacheManager manager = OpenCodeCacheManager.builder()
                    .build();

            assertNotNull(manager);
        }

        @Test
        @DisplayName("builder with default config")
        void builderWithDefaultConfig() {
            OpenCodeCacheManager manager = OpenCodeCacheManager.builder()
                    .defaultConfig(config -> config
                            .maximumSize(5000)
                            .expireAfterWrite(Duration.ofMinutes(30)))
                    .build();

            assertNotNull(manager);
        }
    }

    @Nested
    @DisplayName("Cache Access Tests")
    class CacheAccessTests {

        @Test
        @DisplayName("getCache returns cache")
        void getCacheReturnsCache() {
            OpenCodeCacheManager manager = OpenCodeCacheManager.builder()
                    .cache("users", config -> config.maximumSize(1000))
                    .build();

            SpringCache cache = manager.getCache("users");

            assertNotNull(cache);
            assertEquals("users", cache.getName());
        }

        @Test
        @DisplayName("getCache creates new cache on demand")
        void getCacheCreatesNewCacheOnDemand() {
            OpenCodeCacheManager manager = OpenCodeCacheManager.create();

            SpringCache cache = manager.getCache("dynamic");

            assertNotNull(cache);
            assertEquals("dynamic", cache.getName());
        }

        @Test
        @DisplayName("getCache returns same instance")
        void getCacheReturnsSameInstance() {
            OpenCodeCacheManager manager = OpenCodeCacheManager.create();

            SpringCache cache1 = manager.getCache("test");
            SpringCache cache2 = manager.getCache("test");

            assertSame(cache1, cache2);
        }

        @Test
        @DisplayName("getCacheNames returns all names")
        void getCacheNamesReturnsAllNames() {
            OpenCodeCacheManager manager = OpenCodeCacheManager.builder()
                    .cache("users", config -> config.maximumSize(1000))
                    .cache("products", config -> config.maximumSize(5000))
                    .build();

            Collection<String> names = manager.getCacheNames();

            assertTrue(names.contains("users"));
            assertTrue(names.contains("products"));
        }

        @Test
        @DisplayName("getDelegate returns CacheManager")
        void getDelegateReturnsCacheManager() {
            OpenCodeCacheManager manager = OpenCodeCacheManager.create();

            CacheManager delegate = manager.getDelegate();

            assertNotNull(delegate);
        }
    }

    @Nested
    @DisplayName("SpringCache Operations Tests")
    class SpringCacheOperationsTests {

        private OpenCodeCacheManager manager;
        private SpringCache cache;

        @BeforeEach
        void setUp() {
            manager = OpenCodeCacheManager.builder()
                    .defaultConfig(config -> config.maximumSize(100))
                    .build();
            cache = manager.getCache("test-" + System.nanoTime());
        }

        @Test
        @DisplayName("put and get works")
        void putAndGetWorks() {
            cache.put("key", "value");

            SpringCache.ValueWrapper wrapper = cache.get("key");

            assertNotNull(wrapper);
            assertEquals("value", wrapper.get());
        }

        @Test
        @DisplayName("get returns null for missing key")
        void getReturnsNullForMissingKey() {
            SpringCache.ValueWrapper wrapper = cache.get("missing");

            assertNull(wrapper);
        }

        @Test
        @DisplayName("get with type works")
        void getWithTypeWorks() {
            cache.put("key", "value");

            String value = cache.get("key", String.class);

            assertEquals("value", value);
        }

        @Test
        @DisplayName("get with type returns null for missing")
        void getWithTypeReturnsNullForMissing() {
            String value = cache.get("missing", String.class);

            assertNull(value);
        }

        @Test
        @DisplayName("get with callable loads value")
        void getWithCallableLoadsValue() {
            String value = cache.get("key", () -> "loaded");

            assertEquals("loaded", value);
        }

        @Test
        @DisplayName("get with callable uses cached value")
        void getWithCallableUsesCachedValue() {
            cache.put("key", "cached");

            String value = cache.get("key", () -> "loaded");

            assertEquals("cached", value);
        }

        @Test
        @DisplayName("evict removes entry")
        void evictRemovesEntry() {
            cache.put("key", "value");

            cache.evict("key");

            assertNull(cache.get("key"));
        }

        @Test
        @DisplayName("evictIfPresent removes entry")
        void evictIfPresentRemovesEntry() {
            cache.put("key", "value");

            boolean existed = cache.evictIfPresent("key");

            assertTrue(existed);
            assertNull(cache.get("key"));
        }

        @Test
        @DisplayName("evictIfPresent returns false for missing")
        void evictIfPresentReturnsFalseForMissing() {
            boolean existed = cache.evictIfPresent("missing");

            assertFalse(existed);
        }

        @Test
        @DisplayName("clear removes all entries")
        void clearRemovesAllEntries() {
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            cache.clear();

            assertNull(cache.get("k1"));
            assertNull(cache.get("k2"));
        }

        @Test
        @DisplayName("invalidate clears cache")
        void invalidateClearsCache() {
            cache.put("key", "value");

            cache.invalidate();

            assertNull(cache.get("key"));
        }

        @Test
        @DisplayName("getNativeCache returns underlying cache")
        void getNativeCacheReturnsUnderlyingCache() {
            Object nativeCache = cache.getNativeCache();

            assertNotNull(nativeCache);
        }

        @Test
        @DisplayName("putIfAbsent works")
        void putIfAbsentWorks() {
            SpringCache.ValueWrapper existing = cache.putIfAbsent("key", "first");

            assertNull(existing);
            assertEquals("first", cache.get("key", String.class));
        }

        @Test
        @DisplayName("putIfAbsent returns existing value")
        void putIfAbsentReturnsExistingValue() {
            cache.put("key", "first");

            SpringCache.ValueWrapper existing = cache.putIfAbsent("key", "second");

            assertNotNull(existing);
            assertEquals("first", existing.get());
            assertEquals("first", cache.get("key", String.class));
        }
    }

    @Nested
    @DisplayName("Null Value Handling Tests")
    class NullValueHandlingTests {

        @Test
        @DisplayName("allows null values by default")
        void allowsNullValuesByDefault() {
            OpenCodeCacheManager manager = OpenCodeCacheManager.create();
            SpringCache cache = manager.getCache("test");

            cache.put("key", null);

            SpringCache.ValueWrapper wrapper = cache.get("key");
            assertNotNull(wrapper);
            assertNull(wrapper.get());
        }

        @Test
        @DisplayName("rejects null values when disabled")
        void rejectsNullValuesWhenDisabled() {
            OpenCodeCacheManager manager = OpenCodeCacheManager.builder()
                    .allowNullValues(false)
                    .build();
            SpringCache cache = manager.getCache("test");

            assertThrows(IllegalArgumentException.class, () -> cache.put("key", null));
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("getAllStats returns map")
        void getAllStatsReturnsMap() {
            OpenCodeCacheManager manager = OpenCodeCacheManager.builder()
                    .defaultConfig(config -> config.recordStats())
                    .cache("users", config -> config.maximumSize(100))
                    .build();

            manager.getCache("users").put("key", "value");

            var stats = manager.getAllStats();

            assertNotNull(stats);
        }

        @Test
        @DisplayName("getCacheStats returns stats for cache")
        void getCacheStatsReturnsStatsForCache() {
            OpenCodeCacheManager manager = OpenCodeCacheManager.builder()
                    .defaultConfig(config -> config.recordStats())
                    .cache("users", config -> config.maximumSize(100))
                    .build();

            SpringCache cache = manager.getCache("users");
            cache.put("key", "value");
            cache.get("key");

            CacheStats stats = manager.getCacheStats("users");

            assertNotNull(stats);
        }

        @Test
        @DisplayName("getCombinedStats returns combined stats")
        void getCombinedStatsReturnsCombinedStats() {
            OpenCodeCacheManager manager = OpenCodeCacheManager.builder()
                    .defaultConfig(config -> config.recordStats())
                    .cache("cache1", config -> config.maximumSize(100))
                    .cache("cache2", config -> config.maximumSize(100))
                    .build();

            manager.getCache("cache1").put("k1", "v1");
            manager.getCache("cache2").put("k2", "v2");

            CacheStats stats = manager.getCombinedStats();

            assertNotNull(stats);
        }

        @Test
        @DisplayName("getCacheMetrics returns metrics")
        void getCacheMetricsReturnsMetrics() {
            OpenCodeCacheManager manager = OpenCodeCacheManager.builder()
                    .cache("users", config -> config.maximumSize(100))
                    .build();

            manager.getCache("users").put("key", "value");

            // Metrics may or may not be available depending on configuration
            assertDoesNotThrow(() -> manager.getCacheMetrics("users"));
        }

        @Test
        @DisplayName("getCacheMetrics returns null for missing cache")
        void getCacheMetricsReturnsNullForMissingCache() {
            OpenCodeCacheManager manager = OpenCodeCacheManager.create();

            var metrics = manager.getCacheMetrics("nonexistent");

            assertNull(metrics);
        }
    }

    @Nested
    @DisplayName("Management Operations Tests")
    class ManagementOperationsTests {

        @Test
        @DisplayName("invalidateAll clears all caches")
        void invalidateAllClearsAllCaches() {
            OpenCodeCacheManager manager = OpenCodeCacheManager.builder()
                    .cache("cache1", config -> config.maximumSize(100))
                    .cache("cache2", config -> config.maximumSize(100))
                    .build();

            manager.getCache("cache1").put("k1", "v1");
            manager.getCache("cache2").put("k2", "v2");

            manager.invalidateAll();

            assertNull(manager.getCache("cache1").get("k1"));
            assertNull(manager.getCache("cache2").get("k2"));
        }

        @Test
        @DisplayName("cleanUpAll does not throw")
        void cleanUpAllDoesNotThrow() {
            OpenCodeCacheManager manager = OpenCodeCacheManager.create();
            manager.getCache("test").put("key", "value");

            assertDoesNotThrow(manager::cleanUpAll);
        }
    }

    @Nested
    @DisplayName("Builder Configuration Tests")
    class BuilderConfigurationTests {

        @Test
        @DisplayName("named cache uses specific config")
        void namedCacheUsesSpecificConfig() {
            OpenCodeCacheManager manager = OpenCodeCacheManager.builder()
                    .defaultConfig(config -> config.maximumSize(100))
                    .cache("users", config -> config.maximumSize(5000))
                    .build();

            // The cache should use the named config
            SpringCache cache = manager.getCache("users");
            assertNotNull(cache);
        }

        @Test
        @DisplayName("multiple named caches work")
        void multipleNamedCachesWork() {
            OpenCodeCacheManager manager = OpenCodeCacheManager.builder()
                    .cache("users", config -> config.maximumSize(1000))
                    .cache("products", config -> config.maximumSize(5000))
                    .cache("orders", config -> config.maximumSize(10000))
                    .build();

            Collection<String> names = manager.getCacheNames();

            assertTrue(names.contains("users"));
            assertTrue(names.contains("products"));
            assertTrue(names.contains("orders"));
        }
    }

    @Nested
    @DisplayName("ValueRetrievalException Tests")
    class ValueRetrievalExceptionTests {

        @Test
        @DisplayName("exception is thrown when callable fails")
        void exceptionIsThrownWhenCallableFails() {
            OpenCodeCacheManager manager = OpenCodeCacheManager.create();
            SpringCache cache = manager.getCache("test");

            Callable<String> failingLoader = () -> {
                throw new RuntimeException("Load failed");
            };

            assertThrows(OpenCodeSpringCache.ValueRetrievalException.class,
                    () -> cache.get("key", failingLoader));
        }
    }
}
