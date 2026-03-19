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

package cloud.opencode.base.cache.compression;

import cloud.opencode.base.cache.Cache;
import cloud.opencode.base.cache.OpenCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for CompressedCache
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("CompressedCache Tests")
@SuppressWarnings("unchecked")
class CompressedCacheTest {

    private Cache<String, String> baseCache;

    @BeforeEach
    void setUp() {
        baseCache = OpenCache.<String, String>builder()
                .maximumSize(100)
                .build();
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("wrap creates builder")
        void wrapCreatesBuilder() {
            CompressedCache.Builder<String, String> builder = CompressedCache.wrap(baseCache);
            assertNotNull(builder);
        }

        @Test
        @DisplayName("build with defaults")
        void buildWithDefaults() {
            CompressedCache<String, String> cache = CompressedCache.wrap(baseCache)
                    .build();
            assertNotNull(cache);
        }

        @Test
        @DisplayName("build with custom compressor")
        void buildWithCustomCompressor() {
            CompressedCache<String, String> cache = CompressedCache.wrap(baseCache)
                    .compressor(ValueCompressor.gzip(512))
                    .build();
            assertNotNull(cache);
        }

        @Test
        @DisplayName("build with custom serializer")
        void buildWithCustomSerializer() {
            CompressedCache<String, String> cache = CompressedCache.wrap(baseCache)
                    .serializer(CompressedCache.ValueSerializer.java())
                    .build();
            assertNotNull(cache);
        }

        @Test
        @DisplayName("build with all options")
        void buildWithAllOptions() {
            CompressedCache<String, String> cache = CompressedCache.wrap(baseCache)
                    .compressor(ValueCompressor.gzip(256))
                    .serializer(CompressedCache.ValueSerializer.java())
                    .build();
            assertNotNull(cache);
        }
    }

    @Nested
    @DisplayName("Basic Operations Tests")
    class BasicOperationsTests {

        private CompressedCache<String, String> cache;

        @BeforeEach
        void setUp() {
            cache = CompressedCache.wrap(baseCache)
                    .serializer(CompressedCache.ValueSerializer.java())
                    .build();
        }

        @Test
        @DisplayName("put and get")
        void putAndGet() {
            cache.put("key", "value");
            assertEquals("value", cache.get("key"));
        }

        @Test
        @DisplayName("get returns null for missing key")
        void getReturnsNullForMissingKey() {
            assertNull(cache.get("missing"));
        }

        @Test
        @DisplayName("get with loader")
        void getWithLoader() {
            String result = cache.get("key", k -> "loaded-" + k);
            assertEquals("loaded-key", result);
        }

        @Test
        @DisplayName("containsKey")
        void containsKey() {
            cache.put("key", "value");
            assertTrue(cache.containsKey("key"));
            assertFalse(cache.containsKey("missing"));
        }

        @Test
        @DisplayName("invalidate")
        void invalidate() {
            cache.put("key", "value");
            cache.invalidate("key");
            assertNull(cache.get("key"));
        }

        @Test
        @DisplayName("invalidateAll with keys")
        void invalidateAllWithKeys() {
            cache.put("a", "1");
            cache.put("b", "2");
            cache.put("c", "3");
            cache.invalidateAll(List.of("a", "b"));
            assertNull(cache.get("a"));
            assertNull(cache.get("b"));
            assertEquals("3", cache.get("c"));
        }

        @Test
        @DisplayName("invalidateAll")
        void invalidateAll() {
            cache.put("a", "1");
            cache.put("b", "2");
            cache.invalidateAll();
            assertEquals(0, cache.size());
        }
    }

    @Nested
    @DisplayName("Batch Operations Tests")
    class BatchOperationsTests {

        private CompressedCache<String, String> cache;

        @BeforeEach
        void setUp() {
            cache = CompressedCache.wrap(baseCache)
                    .serializer(CompressedCache.ValueSerializer.java())
                    .build();
        }

        @Test
        @DisplayName("putAll")
        void putAll() {
            cache.putAll(Map.of("a", "1", "b", "2", "c", "3"));
            assertEquals("1", cache.get("a"));
            assertEquals("2", cache.get("b"));
            assertEquals("3", cache.get("c"));
        }

        @Test
        @DisplayName("getAll")
        void getAll() {
            cache.put("a", "1");
            cache.put("b", "2");

            Map<String, String> result = cache.getAll(List.of("a", "b", "c"));
            assertEquals("1", result.get("a"));
            assertEquals("2", result.get("b"));
            assertNull(result.get("c"));
        }

        @Test
        @DisplayName("getAll with loader")
        void getAllWithLoader() {
            cache.put("a", "cached");

            Map<String, String> result = cache.getAll(
                    List.of("a", "b"),
                    keys -> {
                        Map<String, String> loaded = new java.util.HashMap<>();
                        for (Object key : keys) {
                            loaded.put((String) key, "loaded-" + key);
                        }
                        return loaded;
                    });

            assertEquals("cached", result.get("a"));
            assertEquals("loaded-b", result.get("b"));
        }
    }

    @Nested
    @DisplayName("TTL Operations Tests")
    class TtlOperationsTests {

        private CompressedCache<String, String> cache;

        @BeforeEach
        void setUp() {
            cache = CompressedCache.wrap(baseCache)
                    .serializer(CompressedCache.ValueSerializer.java())
                    .build();
        }

        @Test
        @DisplayName("putWithTtl")
        void putWithTtl() {
            cache.putWithTtl("key", "value", Duration.ofMinutes(5));
            assertEquals("value", cache.get("key"));
        }

        @Test
        @DisplayName("putAllWithTtl")
        void putAllWithTtl() {
            cache.putAllWithTtl(Map.of("a", "1", "b", "2"), Duration.ofMinutes(5));
            assertEquals("1", cache.get("a"));
            assertEquals("2", cache.get("b"));
        }

        @Test
        @DisplayName("putIfAbsent")
        void putIfAbsent() {
            assertTrue(cache.putIfAbsent("key", "value"));
            assertFalse(cache.putIfAbsent("key", "new-value"));
            assertEquals("value", cache.get("key"));
        }

        @Test
        @DisplayName("putIfAbsentWithTtl")
        void putIfAbsentWithTtl() {
            assertTrue(cache.putIfAbsentWithTtl("key", "value", Duration.ofMinutes(5)));
            assertFalse(cache.putIfAbsentWithTtl("key", "new-value", Duration.ofMinutes(5)));
            assertEquals("value", cache.get("key"));
        }
    }

    @Nested
    @DisplayName("Collection View Tests")
    class CollectionViewTests {

        private CompressedCache<String, String> cache;

        @BeforeEach
        void setUp() {
            cache = CompressedCache.wrap(baseCache)
                    .serializer(CompressedCache.ValueSerializer.java())
                    .build();
        }

        @Test
        @DisplayName("keys")
        void keys() {
            cache.put("a", "1");
            cache.put("b", "2");

            Set<String> keys = cache.keys();
            assertTrue(keys.contains("a"));
            assertTrue(keys.contains("b"));
        }

        @Test
        @DisplayName("values")
        void values() {
            cache.put("a", "1");
            cache.put("b", "2");

            var values = cache.values();
            assertTrue(values.contains("1"));
            assertTrue(values.contains("2"));
        }

        @Test
        @DisplayName("entries")
        void entries() {
            cache.put("a", "1");
            cache.put("b", "2");

            var entries = cache.entries();
            assertEquals(2, entries.size());
        }

        @Test
        @DisplayName("size")
        void size() {
            cache.put("a", "1");
            cache.put("b", "2");
            assertEquals(2, cache.size());
        }

        @Test
        @DisplayName("estimatedSize")
        void estimatedSize() {
            cache.put("a", "1");
            cache.put("b", "2");
            assertEquals(2, cache.estimatedSize());
        }
    }

    @Nested
    @DisplayName("Unsupported Operations Tests")
    class UnsupportedOperationsTests {

        private CompressedCache<String, String> cache;

        @BeforeEach
        void setUp() {
            cache = CompressedCache.wrap(baseCache)
                    .serializer(CompressedCache.ValueSerializer.java())
                    .build();
        }

        @Test
        @DisplayName("asMap throws UnsupportedOperationException")
        void asMapThrowsUnsupportedOperationException() {
            assertThrows(UnsupportedOperationException.class, () -> cache.asMap());
        }

        @Test
        @DisplayName("async throws UnsupportedOperationException")
        void asyncThrowsUnsupportedOperationException() {
            assertThrows(UnsupportedOperationException.class, () -> cache.async());
        }
    }

    @Nested
    @DisplayName("Delegate Methods Tests")
    class DelegateMethodsTests {

        private CompressedCache<String, String> cache;

        @BeforeEach
        void setUp() {
            cache = CompressedCache.wrap(baseCache)
                    .serializer(CompressedCache.ValueSerializer.java())
                    .build();
        }

        @Test
        @DisplayName("name delegates to underlying cache")
        void nameDelegatesToUnderlyingCache() {
            assertNotNull(cache.name());
        }

        @Test
        @DisplayName("stats returns cache stats")
        void statsReturnsCacheStats() {
            assertNotNull(cache.stats());
        }

        @Test
        @DisplayName("metrics returns null when stats not enabled")
        void metricsReturnsNullWhenStatsNotEnabled() {
            // metrics() returns null when stats recording is not enabled
            assertNull(cache.metrics());
        }

        @Test
        @DisplayName("cleanUp delegates to underlying cache")
        void cleanUpDelegatesToUnderlyingCache() {
            cache.put("key", "value");
            assertDoesNotThrow(() -> cache.cleanUp());
        }
    }

    @Nested
    @DisplayName("Compression Stats Tests")
    class CompressionStatsTests {

        private CompressedCache<String, String> cache;

        @BeforeEach
        void setUp() {
            cache = CompressedCache.wrap(baseCache)
                    .serializer(CompressedCache.ValueSerializer.java())
                    .build();
        }

        @Test
        @DisplayName("compressionStats tracks operations")
        void compressionStatsTracksOperations() {
            cache.put("key", "value");
            cache.get("key");

            CompressedCache.CompressionStats stats = cache.compressionStats();
            assertEquals(1, stats.totalCompressed());
            assertEquals(1, stats.totalDecompressed());
        }

        @Test
        @DisplayName("compressionStats algorithm")
        void compressionStatsAlgorithm() {
            CompressedCache.CompressionStats stats = cache.compressionStats();
            assertEquals(CompressionAlgorithm.GZIP, stats.algorithm());
        }

        @Test
        @DisplayName("resetCompressionStats")
        void resetCompressionStats() {
            cache.put("key", "value");
            cache.get("key");

            cache.resetCompressionStats();

            CompressedCache.CompressionStats stats = cache.compressionStats();
            assertEquals(0, stats.totalCompressed());
            assertEquals(0, stats.totalDecompressed());
        }
    }

    @Nested
    @DisplayName("CompressionStats Record Tests")
    class CompressionStatsRecordTests {

        @Test
        @DisplayName("compressionRatio when no data")
        void compressionRatioWhenNoData() {
            CompressedCache.CompressionStats stats = new CompressedCache.CompressionStats(
                    CompressionAlgorithm.GZIP, 0, 0, 0, 0, 0, 0
            );
            assertEquals(1.0, stats.compressionRatio());
        }

        @Test
        @DisplayName("compressionRatio with data")
        void compressionRatioWithData() {
            CompressedCache.CompressionStats stats = new CompressedCache.CompressionStats(
                    CompressionAlgorithm.GZIP, 1, 1, 1000, 500, 1000, 500
            );
            assertEquals(0.5, stats.compressionRatio());
        }

        @Test
        @DisplayName("bytesSaved")
        void bytesSaved() {
            CompressedCache.CompressionStats stats = new CompressedCache.CompressionStats(
                    CompressionAlgorithm.GZIP, 1, 1, 1000, 500, 1000, 500
            );
            assertEquals(500, stats.bytesSaved());
        }

        @Test
        @DisplayName("avgCompressionTimeNanos when no operations")
        void avgCompressionTimeNanosWhenNoOperations() {
            CompressedCache.CompressionStats stats = new CompressedCache.CompressionStats(
                    CompressionAlgorithm.GZIP, 0, 0, 0, 0, 0, 0
            );
            assertEquals(0, stats.avgCompressionTimeNanos());
        }

        @Test
        @DisplayName("avgCompressionTimeNanos with operations")
        void avgCompressionTimeNanosWithOperations() {
            CompressedCache.CompressionStats stats = new CompressedCache.CompressionStats(
                    CompressionAlgorithm.GZIP, 10, 0, 0, 0, 1000, 0
            );
            assertEquals(100, stats.avgCompressionTimeNanos());
        }

        @Test
        @DisplayName("avgDecompressionTimeNanos when no operations")
        void avgDecompressionTimeNanosWhenNoOperations() {
            CompressedCache.CompressionStats stats = new CompressedCache.CompressionStats(
                    CompressionAlgorithm.GZIP, 0, 0, 0, 0, 0, 0
            );
            assertEquals(0, stats.avgDecompressionTimeNanos());
        }

        @Test
        @DisplayName("avgDecompressionTimeNanos with operations")
        void avgDecompressionTimeNanosWithOperations() {
            CompressedCache.CompressionStats stats = new CompressedCache.CompressionStats(
                    CompressionAlgorithm.GZIP, 0, 10, 0, 0, 0, 500
            );
            assertEquals(50, stats.avgDecompressionTimeNanos());
        }
    }

    @Nested
    @DisplayName("ValueSerializer Tests")
    class ValueSerializerTests {

        @Test
        @DisplayName("java serializer round trip")
        void javaSerializerRoundTrip() {
            CompressedCache.ValueSerializer<String> serializer = CompressedCache.ValueSerializer.java();
            byte[] serialized = serializer.serialize("test value");
            String deserialized = serializer.deserialize(serialized);
            assertEquals("test value", deserialized);
        }

        @Test
        @DisplayName("passThrough serializer round trip")
        void passThroughSerializerRoundTrip() {
            CompressedCache.ValueSerializer<byte[]> serializer = CompressedCache.ValueSerializer.passThrough();
            byte[] data = new byte[]{1, 2, 3, 4, 5};
            byte[] serialized = serializer.serialize(data);
            byte[] deserialized = serializer.deserialize(serialized);
            assertArrayEquals(data, serialized);
            assertArrayEquals(data, deserialized);
        }

        @Test
        @DisplayName("string serializer round trip")
        void stringSerializerRoundTrip() {
            CompressedCache.ValueSerializer<String> serializer = CompressedCache.ValueSerializer.string();
            byte[] serialized = serializer.serialize("test value");
            String deserialized = serializer.deserialize(serialized);
            assertEquals("test value", deserialized);
        }

        @Test
        @DisplayName("string serializer handles unicode")
        void stringSerializerHandlesUnicode() {
            CompressedCache.ValueSerializer<String> serializer = CompressedCache.ValueSerializer.string();
            String unicode = "Hello 你好 こんにちは 🌍";
            byte[] serialized = serializer.serialize(unicode);
            String deserialized = serializer.deserialize(serialized);
            assertEquals(unicode, deserialized);
        }

        @Test
        @DisplayName("java serializer with complex object")
        void javaSerializerWithComplexObject() {
            CompressedCache.ValueSerializer<TestPerson> serializer = CompressedCache.ValueSerializer.java();
            TestPerson person = new TestPerson("John", 30);
            byte[] serialized = serializer.serialize(person);
            TestPerson deserialized = serializer.deserialize(serialized);
            assertEquals(person.name(), deserialized.name());
            assertEquals(person.age(), deserialized.age());
        }

        record TestPerson(String name, int age) implements Serializable {}
    }

    @Nested
    @DisplayName("Null Value Tests")
    class NullValueTests {

        private CompressedCache<String, String> cache;

        @BeforeEach
        void setUp() {
            cache = CompressedCache.wrap(baseCache)
                    .serializer(CompressedCache.ValueSerializer.java())
                    .build();
        }

        @Test
        @DisplayName("put with null value throws NullPointerException")
        void putWithNullValueThrowsNullPointerException() {
            // Cache does not allow null values by default
            assertThrows(NullPointerException.class, () -> cache.put("key", null));
        }

        @Test
        @DisplayName("get with loader returning null")
        void getWithLoaderReturningNull() {
            String result = cache.get("key", k -> null);
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("String Serializer Cache Tests")
    class StringSerializerCacheTests {

        @Test
        @DisplayName("cache with string serializer")
        void cacheWithStringSerializer() {
            CompressedCache<String, String> cache = CompressedCache.wrap(baseCache)
                    .serializer(CompressedCache.ValueSerializer.string())
                    .build();

            cache.put("key", "value");
            assertEquals("value", cache.get("key"));
        }
    }
}
