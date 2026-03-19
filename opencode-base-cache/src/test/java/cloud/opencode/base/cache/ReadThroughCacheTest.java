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

package cloud.opencode.base.cache;

import cloud.opencode.base.cache.config.CacheConfig;
import cloud.opencode.base.cache.spi.CacheLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for ReadThroughCache
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("ReadThroughCache Tests")
class ReadThroughCacheTest {

    private Cache<String, String> baseCache;
    private Map<String, String> backendStore;
    private AtomicInteger loadCount;

    @BeforeEach
    void setUp() {
        baseCache = OpenCache.<String, String>builder()
                .maximumSize(100)
                .build();
        backendStore = new ConcurrentHashMap<>();
        backendStore.put("user:1", "Alice");
        backendStore.put("user:2", "Bob");
        loadCount = new AtomicInteger(0);
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("wrap() creates builder")
        void wrapCreatesBuilder() {
            ReadThroughCache.Builder<String, String> builder = ReadThroughCache.wrap(baseCache);
            assertNotNull(builder);
        }

        @Test
        @DisplayName("wrap() with null throws exception")
        void wrapWithNullThrows() {
            assertThrows(NullPointerException.class, () -> ReadThroughCache.wrap(null));
        }

        @Test
        @DisplayName("build without loader throws exception")
        void buildWithoutLoaderThrows() {
            assertThrows(IllegalStateException.class, () ->
                    ReadThroughCache.wrap(baseCache).build());
        }

        @Test
        @DisplayName("build with loader function")
        void buildWithLoaderFunction() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .build();
            assertNotNull(cache);
        }

        @Test
        @DisplayName("build with CacheLoader interface")
        void buildWithCacheLoader() {
            CacheLoader<String, String> loader = new CacheLoader<>() {
                @Override
                public String load(String key) {
                    return backendStore.get(key);
                }
            };
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader(loader)
                    .build();
            assertNotNull(cache);
        }

        @Test
        @DisplayName("build with batch loader")
        void buildWithBatchLoader() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .batchLoader(keys -> {
                        Map<String, String> result = new ConcurrentHashMap<>();
                        for (Object key : keys) {
                            String v = backendStore.get(key.toString());
                            if (v != null) result.put(key.toString(), v);
                        }
                        return result;
                    })
                    .build();
            assertNotNull(cache);
        }

        @Test
        @DisplayName("build with fallback function")
        void buildWithFallbackFunction() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .fallback(k -> "default-" + k)
                    .build();
            assertNotNull(cache);
        }

        @Test
        @DisplayName("build with fallback value")
        void buildWithFallbackValue() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .fallback("UNKNOWN")
                    .build();
            assertNotNull(cache);
        }

        @Test
        @DisplayName("build with cacheNullValues")
        void buildWithCacheNullValues() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .cacheNullValues()
                    .build();
            assertNotNull(cache);
        }

        @Test
        @DisplayName("null loader throws exception")
        void nullLoaderThrows() {
            assertThrows(NullPointerException.class, () ->
                    ReadThroughCache.wrap(baseCache).loader((java.util.function.Function<String, String>) null));
        }
    }

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodsTests {

        @Test
        @DisplayName("create with name, loader, and config")
        void createWithNameLoaderConfig() {
            ReadThroughCache<String, String> cache = ReadThroughCache.create(
                    "test-read-through",
                    k -> backendStore.get(k),
                    CacheConfig.<String, String>builder().maximumSize(100).build()
            );
            assertNotNull(cache);
            assertEquals("Alice", cache.get("user:1"));
        }
    }

    @Nested
    @DisplayName("Get Operations")
    class GetOperationsTests {

        @Test
        @DisplayName("get returns cached value")
        void getReturnsCachedValue() {
            baseCache.put("key", "cached-value");
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> {
                        loadCount.incrementAndGet();
                        return backendStore.get(k);
                    })
                    .build();

            assertEquals("cached-value", cache.get("key"));
            assertEquals(0, loadCount.get()); // No load needed
        }

        @Test
        @DisplayName("get loads from backend on miss")
        void getLoadsFromBackendOnMiss() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> {
                        loadCount.incrementAndGet();
                        return backendStore.get(k);
                    })
                    .build();

            assertEquals("Alice", cache.get("user:1"));
            assertEquals(1, loadCount.get());
            // Value should now be cached
            assertTrue(baseCache.containsKey("user:1"));
        }

        @Test
        @DisplayName("get caches loaded value")
        void getCachesLoadedValue() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> {
                        loadCount.incrementAndGet();
                        return backendStore.get(k);
                    })
                    .build();

            cache.get("user:1");
            cache.get("user:1");
            assertEquals(1, loadCount.get()); // Only loaded once
        }

        @Test
        @DisplayName("get returns null for nonexistent key")
        void getReturnsNullForNonexistent() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .build();

            assertNull(cache.get("nonexistent"));
        }

        @Test
        @DisplayName("get uses fallback for null")
        void getUsesFallbackForNull() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .fallback(k -> "fallback-" + k)
                    .build();

            assertEquals("fallback-nonexistent", cache.get("nonexistent"));
        }

        @Test
        @DisplayName("get uses static fallback value")
        void getUsesStaticFallback() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .fallback("UNKNOWN")
                    .build();

            assertEquals("UNKNOWN", cache.get("nonexistent"));
        }

        @Test
        @DisplayName("get with custom loader")
        void getWithCustomLoader() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .build();

            String result = cache.get("custom", k -> "custom-loaded");
            assertEquals("custom-loaded", result);
        }

        @Test
        @DisplayName("get throws CacheLoadException on load failure")
        void getThrowsOnLoadFailure() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> {
                        throw new RuntimeException("Load failed");
                    })
                    .build();

            assertThrows(ReadThroughCache.CacheLoadException.class, () ->
                    cache.get("key"));
        }
    }

    @Nested
    @DisplayName("GetAll Operations")
    class GetAllOperationsTests {

        @Test
        @DisplayName("getAll returns cached and loaded values")
        void getAllReturnsCachedAndLoaded() {
            baseCache.put("user:1", "cached-alice");

            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .build();

            Map<String, String> result = cache.getAll(List.of("user:1", "user:2"));
            assertEquals("cached-alice", result.get("user:1"));
            assertEquals("Bob", result.get("user:2"));
        }

        @Test
        @DisplayName("getAll uses batch loader")
        void getAllUsesBatchLoader() {
            AtomicInteger batchLoadCount = new AtomicInteger(0);

            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .batchLoader(keys -> {
                        batchLoadCount.incrementAndGet();
                        Map<String, String> result = new ConcurrentHashMap<>();
                        for (Object key : keys) {
                            String v = backendStore.get(key.toString());
                            if (v != null) result.put(key.toString(), v);
                        }
                        return result;
                    })
                    .build();

            cache.getAll(List.of("user:1", "user:2"));
            assertEquals(1, batchLoadCount.get()); // Used batch loader
        }

        @Test
        @DisplayName("getAll applies fallback for missing")
        void getAllAppliesFallbackForMissing() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .fallback(k -> "fallback-" + k)
                    .build();

            Map<String, String> result = cache.getAll(List.of("user:1", "nonexistent"));
            assertEquals("Alice", result.get("user:1"));
            assertEquals("fallback-nonexistent", result.get("nonexistent"));
        }

        @Test
        @DisplayName("getAll with custom loader")
        void getAllWithCustomLoader() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .build();

            Map<String, String> result = cache.getAll(List.of("a", "b"),
                    keys -> Map.of("a", "loaded-a", "b", "loaded-b"));
            assertEquals("loaded-a", result.get("a"));
            assertEquals("loaded-b", result.get("b"));
        }
    }

    @Nested
    @DisplayName("Put Operations")
    class PutOperationsTests {

        @Test
        @DisplayName("put stores value")
        void putStoresValue() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .build();

            cache.put("key", "value");
            assertEquals("value", baseCache.get("key"));
        }

        @Test
        @DisplayName("putAll stores values")
        void putAllStoresValues() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .build();

            cache.putAll(Map.of("a", "1", "b", "2"));
            assertEquals("1", baseCache.get("a"));
            assertEquals("2", baseCache.get("b"));
        }

        @Test
        @DisplayName("putIfAbsent stores if absent")
        void putIfAbsentStoresIfAbsent() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .build();

            assertTrue(cache.putIfAbsent("key", "value"));
            assertFalse(cache.putIfAbsent("key", "other"));
        }

        @Test
        @DisplayName("putWithTtl stores with TTL")
        void putWithTtlStores() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .build();

            cache.putWithTtl("key", "value", Duration.ofMinutes(5));
            assertEquals("value", cache.get("key"));
        }

        @Test
        @DisplayName("putAllWithTtl stores with TTL")
        void putAllWithTtlStores() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .build();

            cache.putAllWithTtl(Map.of("a", "1"), Duration.ofMinutes(5));
            assertEquals("1", cache.get("a"));
        }

        @Test
        @DisplayName("putIfAbsentWithTtl stores if absent with TTL")
        void putIfAbsentWithTtlStores() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .build();

            assertTrue(cache.putIfAbsentWithTtl("key", "value", Duration.ofMinutes(5)));
        }
    }

    @Nested
    @DisplayName("Invalidation Operations")
    class InvalidationOperationsTests {

        @Test
        @DisplayName("invalidate removes from cache")
        void invalidateRemovesFromCache() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .build();

            cache.put("key", "value");
            cache.invalidate("key");
            assertFalse(baseCache.containsKey("key"));
        }

        @Test
        @DisplayName("invalidateAll removes keys")
        void invalidateAllRemovesKeys() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .build();

            cache.putAll(Map.of("a", "1", "b", "2"));
            cache.invalidateAll(List.of("a", "b"));
            assertEquals(0, baseCache.size());
        }

        @Test
        @DisplayName("invalidateAll clears cache")
        void invalidateAllClearsCache() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .build();

            cache.put("key", "value");
            cache.invalidateAll();
            assertEquals(0, baseCache.size());
        }
    }

    @Nested
    @DisplayName("Query Operations")
    class QueryOperationsTests {

        @Test
        @DisplayName("containsKey checks cache")
        void containsKeyChecksCache() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .build();

            cache.put("key", "value");
            assertTrue(cache.containsKey("key"));
            assertFalse(cache.containsKey("nonexistent"));
        }

        @Test
        @DisplayName("size returns cache size")
        void sizeReturnsCacheSize() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .build();

            cache.put("a", "1");
            cache.put("b", "2");
            assertEquals(2, cache.size());
        }

        @Test
        @DisplayName("keys returns cache keys")
        void keysReturnsCacheKeys() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .build();

            cache.put("key", "value");
            assertTrue(cache.keys().contains("key"));
        }

        @Test
        @DisplayName("values returns cache values")
        void valuesReturnsCacheValues() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .build();

            cache.put("key", "value");
            assertTrue(cache.values().contains("value"));
        }

        @Test
        @DisplayName("entries returns cache entries")
        void entriesReturnsCacheEntries() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .build();

            cache.put("key", "value");
            assertEquals(1, cache.entries().size());
        }

        @Test
        @DisplayName("asMap returns cache map")
        void asMapReturnsCacheMap() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .build();

            cache.put("key", "value");
            assertEquals("value", cache.asMap().get("key"));
        }

        @Test
        @DisplayName("loader returns configured loader")
        void loaderReturnsConfiguredLoader() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .build();

            assertNotNull(cache.loader());
            assertEquals("Alice", cache.loader().apply("user:1"));
        }
    }

    @Nested
    @DisplayName("Other Operations")
    class OtherOperationsTests {

        @Test
        @DisplayName("stats returns delegate stats")
        void statsReturnsDelegateStats() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .build();

            assertNotNull(cache.stats());
        }

        @Test
        @DisplayName("cleanUp cleans delegate")
        void cleanUpCleansDelegate() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .build();

            assertDoesNotThrow(() -> cache.cleanUp());
        }

        @Test
        @DisplayName("async returns async view")
        void asyncReturnsAsyncView() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .build();

            assertNotNull(cache.async());
        }

        @Test
        @DisplayName("name returns delegate name")
        void nameReturnsDelegateName() {
            ReadThroughCache<String, String> cache = ReadThroughCache.wrap(baseCache)
                    .loader((java.util.function.Function<String, String>) k -> backendStore.get(k))
                    .build();

            assertNotNull(cache.name());
        }
    }

    @Nested
    @DisplayName("Exception Tests")
    class ExceptionTests {

        @Test
        @DisplayName("CacheLoadException contains message and cause")
        void cacheLoadExceptionContainsMessageAndCause() {
            RuntimeException cause = new RuntimeException("Load failure");
            ReadThroughCache.CacheLoadException exception =
                    new ReadThroughCache.CacheLoadException("Test message", cause);

            assertTrue(exception.getMessage().contains("Test message"));
            assertEquals(cause, exception.getCause());
        }
    }
}
