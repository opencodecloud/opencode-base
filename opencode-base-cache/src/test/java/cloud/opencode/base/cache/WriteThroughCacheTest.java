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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for WriteThroughCache
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("WriteThroughCache Tests")
class WriteThroughCacheTest {

    private Cache<String, String> baseCache;
    private Map<String, String> backendStore;
    private Set<String> deletedKeys;
    private WriteThroughCache<String, String> writeThroughCache;

    @BeforeEach
    void setUp() {
        baseCache = OpenCache.<String, String>builder()
                .maximumSize(100)
                .build();
        backendStore = new ConcurrentHashMap<>();
        deletedKeys = ConcurrentHashMap.newKeySet();
        writeThroughCache = WriteThroughCache.wrap(baseCache)
                .writer((WriteThroughCache.CacheWriter<String, String>) (k, v) -> backendStore.put(k, v))
                .deleter(k -> deletedKeys.add(k))
                .build();
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("wrap() creates builder")
        void wrapCreatesBuilder() {
            WriteThroughCache.Builder<String, String> builder = WriteThroughCache.wrap(baseCache);
            assertNotNull(builder);
        }

        @Test
        @DisplayName("wrap() with null throws exception")
        void wrapWithNullThrows() {
            assertThrows(NullPointerException.class, () -> WriteThroughCache.wrap(null));
        }

        @Test
        @DisplayName("build without writer throws exception")
        void buildWithoutWriterThrows() {
            assertThrows(IllegalStateException.class, () ->
                    WriteThroughCache.wrap(baseCache).build());
        }

        @Test
        @DisplayName("build with BiConsumer writer")
        void buildWithBiConsumerWriter() {
            WriteThroughCache<String, String> cache = WriteThroughCache.wrap(baseCache)
                    .writer((WriteThroughCache.CacheWriter<String, String>) (k, v) -> backendStore.put(k, v))
                    .build();
            assertNotNull(cache);
        }

        @Test
        @DisplayName("build with CacheWriter")
        void buildWithCacheWriter() {
            WriteThroughCache.CacheWriter<String, String> writer = (k, v) -> backendStore.put(k, v);
            WriteThroughCache<String, String> cache = WriteThroughCache.wrap(baseCache)
                    .writer(writer)
                    .build();
            assertNotNull(cache);
        }

        @Test
        @DisplayName("build with deleter")
        void buildWithDeleter() {
            WriteThroughCache<String, String> cache = WriteThroughCache.wrap(baseCache)
                    .writer((WriteThroughCache.CacheWriter<String, String>) (k, v) -> backendStore.put(k, v))
                    .deleter(k -> deletedKeys.add(k))
                    .build();
            assertNotNull(cache);
        }

        @Test
        @DisplayName("build with error handler")
        void buildWithErrorHandler() {
            List<Throwable> errors = new CopyOnWriteArrayList<>();
            WriteThroughCache<String, String> cache = WriteThroughCache.wrap(baseCache)
                    .writer((WriteThroughCache.CacheWriter<String, String>) (k, v) -> backendStore.put(k, v))
                    .onError((keys, error) -> errors.add(error))
                    .build();
            assertNotNull(cache);
        }

        @Test
        @DisplayName("null writer BiConsumer throws exception")
        void nullWriterBiConsumerThrows() {
            java.util.function.BiConsumer<String, String> nullWriter = null;
            assertThrows(NullPointerException.class, () ->
                    WriteThroughCache.wrap(baseCache).writer(nullWriter));
        }

        @Test
        @DisplayName("null CacheWriter throws exception")
        void nullCacheWriterThrows() {
            WriteThroughCache.CacheWriter<String, String> nullWriter = null;
            assertThrows(NullPointerException.class, () ->
                    WriteThroughCache.wrap(baseCache).writer(nullWriter));
        }
    }

    @Nested
    @DisplayName("Put Operations")
    class PutOperationsTests {

        @Test
        @DisplayName("put writes to backend then cache")
        void putWritesToBackendThenCache() {
            writeThroughCache.put("key", "value");

            assertEquals("value", backendStore.get("key"));
            assertEquals("value", writeThroughCache.get("key"));
        }

        @Test
        @DisplayName("put throws on backend failure")
        void putThrowsOnBackendFailure() {
            WriteThroughCache<String, String> cache = WriteThroughCache.wrap(baseCache)
                    .writer((WriteThroughCache.CacheWriter<String, String>) (k, v) -> {
                        throw new RuntimeException("Backend failure");
                    })
                    .build();

            assertThrows(WriteThroughCache.CacheWriteException.class, () ->
                    cache.put("key", "value"));
            assertFalse(cache.containsKey("key"));
        }

        @Test
        @DisplayName("putAll writes all to backend then cache")
        void putAllWritesToBackendThenCache() {
            writeThroughCache.putAll(Map.of("a", "1", "b", "2"));

            assertEquals("1", backendStore.get("a"));
            assertEquals("2", backendStore.get("b"));
            assertEquals("1", writeThroughCache.get("a"));
            assertEquals("2", writeThroughCache.get("b"));
        }

        @Test
        @DisplayName("putAll throws on backend failure")
        void putAllThrowsOnBackendFailure() {
            WriteThroughCache<String, String> cache = WriteThroughCache.wrap(baseCache)
                    .writer((WriteThroughCache.CacheWriter<String, String>) (k, v) -> {
                        throw new RuntimeException("Backend failure");
                    })
                    .build();

            assertThrows(WriteThroughCache.CacheWriteException.class, () ->
                    cache.putAll(Map.of("a", "1", "b", "2")));
        }

        @Test
        @DisplayName("putIfAbsent writes to backend if absent")
        void putIfAbsentWritesToBackendIfAbsent() {
            assertTrue(writeThroughCache.putIfAbsent("key", "value"));
            assertEquals("value", backendStore.get("key"));

            assertFalse(writeThroughCache.putIfAbsent("key", "other"));
        }

        @Test
        @DisplayName("putIfAbsent throws on backend failure")
        void putIfAbsentThrowsOnBackendFailure() {
            WriteThroughCache<String, String> cache = WriteThroughCache.wrap(baseCache)
                    .writer((WriteThroughCache.CacheWriter<String, String>) (k, v) -> {
                        throw new RuntimeException("Backend failure");
                    })
                    .build();

            assertThrows(WriteThroughCache.CacheWriteException.class, () ->
                    cache.putIfAbsent("key", "value"));
        }

        @Test
        @DisplayName("putWithTtl writes to backend then cache")
        void putWithTtlWritesToBackendThenCache() {
            writeThroughCache.putWithTtl("key", "value", Duration.ofMinutes(5));

            assertEquals("value", backendStore.get("key"));
            assertEquals("value", writeThroughCache.get("key"));
        }

        @Test
        @DisplayName("putWithTtl throws on backend failure")
        void putWithTtlThrowsOnBackendFailure() {
            WriteThroughCache<String, String> cache = WriteThroughCache.wrap(baseCache)
                    .writer((WriteThroughCache.CacheWriter<String, String>) (k, v) -> {
                        throw new RuntimeException("Backend failure");
                    })
                    .build();

            assertThrows(WriteThroughCache.CacheWriteException.class, () ->
                    cache.putWithTtl("key", "value", Duration.ofMinutes(5)));
        }

        @Test
        @DisplayName("putAllWithTtl writes to backend then cache")
        void putAllWithTtlWritesToBackendThenCache() {
            writeThroughCache.putAllWithTtl(Map.of("a", "1"), Duration.ofMinutes(5));

            assertEquals("1", backendStore.get("a"));
            assertEquals("1", writeThroughCache.get("a"));
        }

        @Test
        @DisplayName("putAllWithTtl throws on backend failure")
        void putAllWithTtlThrowsOnBackendFailure() {
            WriteThroughCache<String, String> cache = WriteThroughCache.wrap(baseCache)
                    .writer((WriteThroughCache.CacheWriter<String, String>) (k, v) -> {
                        throw new RuntimeException("Backend failure");
                    })
                    .build();

            assertThrows(WriteThroughCache.CacheWriteException.class, () ->
                    cache.putAllWithTtl(Map.of("a", "1"), Duration.ofMinutes(5)));
        }

        @Test
        @DisplayName("putIfAbsentWithTtl writes to backend if absent")
        void putIfAbsentWithTtlWritesToBackendIfAbsent() {
            assertTrue(writeThroughCache.putIfAbsentWithTtl("key", "value", Duration.ofMinutes(5)));
            assertEquals("value", backendStore.get("key"));

            assertFalse(writeThroughCache.putIfAbsentWithTtl("key", "other", Duration.ofMinutes(5)));
        }

        @Test
        @DisplayName("putIfAbsentWithTtl throws on backend failure")
        void putIfAbsentWithTtlThrowsOnBackendFailure() {
            WriteThroughCache<String, String> cache = WriteThroughCache.wrap(baseCache)
                    .writer((WriteThroughCache.CacheWriter<String, String>) (k, v) -> {
                        throw new RuntimeException("Backend failure");
                    })
                    .build();

            assertThrows(WriteThroughCache.CacheWriteException.class, () ->
                    cache.putIfAbsentWithTtl("key", "value", Duration.ofMinutes(5)));
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperationsTests {

        @Test
        @DisplayName("invalidate deletes from backend then cache")
        void invalidateDeletesFromBackendThenCache() {
            writeThroughCache.put("key", "value");
            writeThroughCache.invalidate("key");

            assertTrue(deletedKeys.contains("key"));
            assertFalse(writeThroughCache.containsKey("key"));
        }

        @Test
        @DisplayName("invalidate throws on backend failure")
        void invalidateThrowsOnBackendFailure() {
            WriteThroughCache<String, String> cache = WriteThroughCache.wrap(baseCache)
                    .writer((WriteThroughCache.CacheWriter<String, String>) (k, v) -> backendStore.put(k, v))
                    .deleter(k -> {
                        throw new RuntimeException("Delete failure");
                    })
                    .build();

            cache.put("key", "value");
            assertThrows(WriteThroughCache.CacheWriteException.class, () ->
                    cache.invalidate("key"));
        }

        @Test
        @DisplayName("invalidate without deleter only invalidates cache")
        void invalidateWithoutDeleterOnlyInvalidatesCache() {
            WriteThroughCache<String, String> cache = WriteThroughCache.wrap(baseCache)
                    .writer((WriteThroughCache.CacheWriter<String, String>) (k, v) -> backendStore.put(k, v))
                    .build();

            cache.put("key", "value");
            cache.invalidate("key");
            assertFalse(cache.containsKey("key"));
        }

        @Test
        @DisplayName("invalidateAll deletes from backend then cache")
        void invalidateAllDeletesFromBackendThenCache() {
            writeThroughCache.put("a", "1");
            writeThroughCache.put("b", "2");
            writeThroughCache.invalidateAll(List.of("a", "b"));

            assertTrue(deletedKeys.contains("a"));
            assertTrue(deletedKeys.contains("b"));
            assertEquals(0, writeThroughCache.size());
        }

        @Test
        @DisplayName("invalidateAll throws on backend failure")
        void invalidateAllThrowsOnBackendFailure() {
            WriteThroughCache<String, String> cache = WriteThroughCache.wrap(baseCache)
                    .writer((WriteThroughCache.CacheWriter<String, String>) (k, v) -> backendStore.put(k, v))
                    .deleter(k -> {
                        throw new RuntimeException("Delete failure");
                    })
                    .build();

            cache.put("key", "value");
            assertThrows(WriteThroughCache.CacheWriteException.class, () ->
                    cache.invalidateAll(List.of("key")));
        }

        @Test
        @DisplayName("invalidateAll clears only cache")
        void invalidateAllClearsOnlyCache() {
            writeThroughCache.put("key", "value");
            writeThroughCache.invalidateAll();
            assertEquals(0, writeThroughCache.size());
        }
    }

    @Nested
    @DisplayName("Read Operations")
    class ReadOperationsTests {

        @Test
        @DisplayName("get returns cached value")
        void getReturnsCachedValue() {
            writeThroughCache.put("key", "value");
            assertEquals("value", writeThroughCache.get("key"));
        }

        @Test
        @DisplayName("get returns null for missing key")
        void getReturnsNullForMissing() {
            assertNull(writeThroughCache.get("nonexistent"));
        }

        @Test
        @DisplayName("get with loader loads value")
        void getWithLoaderLoadsValue() {
            String result = writeThroughCache.get("key", k -> "loaded-" + k);
            assertEquals("loaded-key", result);
        }

        @Test
        @DisplayName("getAll returns cached values")
        void getAllReturnsCachedValues() {
            writeThroughCache.put("a", "1");
            writeThroughCache.put("b", "2");
            Map<String, String> result = writeThroughCache.getAll(List.of("a", "b"));
            assertEquals("1", result.get("a"));
            assertEquals("2", result.get("b"));
        }

        @Test
        @DisplayName("getAll with loader")
        void getAllWithLoader() {
            Map<String, String> result = writeThroughCache.getAll(List.of("a", "b"),
                    keys -> Map.of("a", "loaded-a", "b", "loaded-b"));
            assertEquals("loaded-a", result.get("a"));
            assertEquals("loaded-b", result.get("b"));
        }

        @Test
        @DisplayName("containsKey checks cache")
        void containsKeyChecksCache() {
            writeThroughCache.put("key", "value");
            assertTrue(writeThroughCache.containsKey("key"));
            assertFalse(writeThroughCache.containsKey("nonexistent"));
        }

        @Test
        @DisplayName("size returns cache size")
        void sizeReturnsCacheSize() {
            writeThroughCache.put("a", "1");
            writeThroughCache.put("b", "2");
            assertEquals(2, writeThroughCache.size());
        }

        @Test
        @DisplayName("estimatedSize returns cache size")
        void estimatedSizeReturnsCacheSize() {
            writeThroughCache.put("key", "value");
            assertEquals(1, writeThroughCache.estimatedSize());
        }

        @Test
        @DisplayName("keys returns cache keys")
        void keysReturnsCacheKeys() {
            writeThroughCache.put("key", "value");
            assertTrue(writeThroughCache.keys().contains("key"));
        }

        @Test
        @DisplayName("values returns cache values")
        void valuesReturnsCacheValues() {
            writeThroughCache.put("key", "value");
            assertTrue(writeThroughCache.values().contains("value"));
        }

        @Test
        @DisplayName("entries returns cache entries")
        void entriesReturnsCacheEntries() {
            writeThroughCache.put("key", "value");
            assertEquals(1, writeThroughCache.entries().size());
        }

        @Test
        @DisplayName("asMap returns cache map")
        void asMapReturnsCacheMap() {
            writeThroughCache.put("key", "value");
            assertEquals("value", writeThroughCache.asMap().get("key"));
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("error handler is called on write failure")
        void errorHandlerIsCalledOnFailure() {
            List<Throwable> errors = new CopyOnWriteArrayList<>();
            WriteThroughCache<String, String> cache = WriteThroughCache.wrap(baseCache)
                    .writer((WriteThroughCache.CacheWriter<String, String>) (k, v) -> {
                        throw new RuntimeException("Backend failure");
                    })
                    .onError((keys, error) -> errors.add(error))
                    .build();

            assertThrows(WriteThroughCache.CacheWriteException.class, () ->
                    cache.put("key", "value"));

            assertEquals(1, errors.size());
        }

        @Test
        @DisplayName("error handler exception is swallowed")
        void errorHandlerExceptionIsSwallowed() {
            WriteThroughCache<String, String> cache = WriteThroughCache.wrap(baseCache)
                    .writer((WriteThroughCache.CacheWriter<String, String>) (k, v) -> {
                        throw new RuntimeException("Backend failure");
                    })
                    .onError((keys, error) -> {
                        throw new RuntimeException("Handler failure");
                    })
                    .build();

            // Should not throw handler exception
            assertThrows(WriteThroughCache.CacheWriteException.class, () ->
                    cache.put("key", "value"));
        }

        @Test
        @DisplayName("CacheWriteException contains message and cause")
        void cacheWriteExceptionContainsMessageAndCause() {
            RuntimeException cause = new RuntimeException("Backend failure");
            WriteThroughCache.CacheWriteException exception =
                    new WriteThroughCache.CacheWriteException("Test message", cause);

            assertTrue(exception.getMessage().contains("Test message"));
            assertEquals(cause, exception.getCause());
        }
    }

    @Nested
    @DisplayName("CacheWriter Interface")
    class CacheWriterInterfaceTests {

        @Test
        @DisplayName("writeAll default implementation writes one by one")
        void writeAllDefaultImplementation() throws Exception {
            Map<String, String> written = new ConcurrentHashMap<>();
            WriteThroughCache.CacheWriter<String, String> writer = (k, v) -> written.put(k, v);

            writer.writeAll(Map.of("a", "1", "b", "2"));

            assertEquals("1", written.get("a"));
            assertEquals("2", written.get("b"));
        }
    }

    @Nested
    @DisplayName("Other Operations")
    class OtherOperationsTests {

        @Test
        @DisplayName("stats returns delegate stats")
        void statsReturnsDelegateStats() {
            assertNotNull(writeThroughCache.stats());
        }

        @Test
        @DisplayName("metrics returns delegate metrics")
        void metricsReturnsDelegateMetrics() {
            writeThroughCache.metrics(); // May be null
        }

        @Test
        @DisplayName("cleanUp cleans delegate")
        void cleanUpCleansDelegate() {
            assertDoesNotThrow(() -> writeThroughCache.cleanUp());
        }

        @Test
        @DisplayName("async returns async view")
        void asyncReturnsAsyncView() {
            assertNotNull(writeThroughCache.async());
        }

        @Test
        @DisplayName("name returns delegate name")
        void nameReturnsDelegateName() {
            assertNotNull(writeThroughCache.name());
        }
    }
}
