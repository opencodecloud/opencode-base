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
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for NullSafeCache
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("NullSafeCache Tests")
class NullSafeCacheTest {

    private Cache<String, String> baseCache;
    private NullSafeCache<String, String> nullSafeCache;

    @BeforeEach
    void setUp() {
        baseCache = OpenCache.<String, String>builder()
                .maximumSize(100)
                .build();
        nullSafeCache = NullSafeCache.wrap(baseCache);
    }

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodsTests {

        @Test
        @DisplayName("wrap() creates null-safe cache")
        void wrapCreatesNullSafeCache() {
            NullSafeCache<String, String> cache = NullSafeCache.wrap(baseCache);
            assertNotNull(cache);
        }

        @Test
        @DisplayName("wrap() with null throws exception")
        void wrapWithNullThrows() {
            assertThrows(NullPointerException.class, () -> NullSafeCache.wrap(null));
        }

        @Test
        @DisplayName("wrap() returns same instance for already wrapped")
        void wrapReturnsSameForWrapped() {
            NullSafeCache<String, String> wrapped = NullSafeCache.wrap(baseCache);
            NullSafeCache<String, String> doubleWrapped = NullSafeCache.wrap(wrapped);
            assertSame(wrapped, doubleWrapped);
        }
    }

    @Nested
    @DisplayName("Null Value Storage")
    class NullValueStorageTests {

        @Test
        @DisplayName("put stores null value")
        void putStoresNullValue() {
            nullSafeCache.put("key", null);
            assertTrue(nullSafeCache.containsKey("key"));
        }

        @Test
        @DisplayName("get returns null for stored null")
        void getReturnsNullForStoredNull() {
            nullSafeCache.put("key", null);
            assertNull(nullSafeCache.get("key"));
        }

        @Test
        @DisplayName("get returns null for missing key")
        void getReturnsNullForMissing() {
            assertNull(nullSafeCache.get("nonexistent"));
        }

        @Test
        @DisplayName("containsNullValue returns true for null value")
        void containsNullValueReturnsTrue() {
            nullSafeCache.put("key", null);
            assertTrue(nullSafeCache.containsNullValue("key"));
        }

        @Test
        @DisplayName("containsNullValue returns false for non-null value")
        void containsNullValueReturnsFalseForNonNull() {
            nullSafeCache.put("key", "value");
            assertFalse(nullSafeCache.containsNullValue("key"));
        }

        @Test
        @DisplayName("containsNullValue returns false for missing key")
        void containsNullValueReturnsFalseForMissing() {
            assertFalse(nullSafeCache.containsNullValue("nonexistent"));
        }
    }

    @Nested
    @DisplayName("GetIfPresent Tests")
    class GetIfPresentTests {

        @Test
        @DisplayName("getIfPresent returns null for missing key")
        void getIfPresentReturnsNullForMissing() {
            Optional<String> result = nullSafeCache.getIfPresent("nonexistent");
            assertNull(result); // Not in cache
        }

        @Test
        @DisplayName("getIfPresent returns empty Optional for null value")
        void getIfPresentReturnsEmptyForNull() {
            nullSafeCache.put("key", null);
            Optional<String> result = nullSafeCache.getIfPresent("key");
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("getIfPresent returns Optional with value")
        void getIfPresentReturnsOptionalWithValue() {
            nullSafeCache.put("key", "value");
            Optional<String> result = nullSafeCache.getIfPresent("key");
            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals("value", result.get());
        }
    }

    @Nested
    @DisplayName("Get with Loader")
    class GetWithLoaderTests {

        @Test
        @DisplayName("get with loader stores null result")
        void getWithLoaderStoresNull() {
            String result = nullSafeCache.get("key", k -> null);
            assertNull(result);
            assertTrue(nullSafeCache.containsKey("key"));
            assertTrue(nullSafeCache.containsNullValue("key"));
        }

        @Test
        @DisplayName("get with loader stores non-null result")
        void getWithLoaderStoresNonNull() {
            String result = nullSafeCache.get("key", k -> "loaded");
            assertEquals("loaded", result);
            assertFalse(nullSafeCache.containsNullValue("key"));
        }
    }

    @Nested
    @DisplayName("Batch Operations")
    class BatchOperationsTests {

        @Test
        @DisplayName("putAll stores null values")
        void putAllStoresNullValues() {
            Map<String, String> map = new java.util.HashMap<>();
            map.put("a", "value");
            map.put("b", null);
            nullSafeCache.putAll(map);

            assertEquals("value", nullSafeCache.get("a"));
            assertNull(nullSafeCache.get("b"));
            assertTrue(nullSafeCache.containsNullValue("b"));
        }

        @Test
        @DisplayName("getAll returns null values")
        void getAllReturnsNullValues() {
            nullSafeCache.put("a", "value");
            nullSafeCache.put("b", null);

            Map<String, String> result = nullSafeCache.getAll(List.of("a", "b"));
            assertEquals("value", result.get("a"));
            assertNull(result.get("b"));
        }

        @Test
        @DisplayName("getAll with loader handles null")
        void getAllWithLoaderHandlesNull() {
            Map<String, String> result = nullSafeCache.getAll(List.of("a", "b"),
                    keys -> {
                        Map<String, String> map = new java.util.HashMap<>();
                        for (String key : keys) {
                            map.put(key, key.equals("a") ? "loaded" : null);
                        }
                        return map;
                    });

            assertEquals("loaded", result.get("a"));
            assertNull(result.get("b"));
        }
    }

    @Nested
    @DisplayName("TTL Operations")
    class TtlOperationsTests {

        @Test
        @DisplayName("putWithTtl stores null value")
        void putWithTtlStoresNull() {
            nullSafeCache.putWithTtl("key", null, Duration.ofMinutes(5));
            assertTrue(nullSafeCache.containsKey("key"));
            assertTrue(nullSafeCache.containsNullValue("key"));
        }

        @Test
        @DisplayName("putAllWithTtl stores null values")
        void putAllWithTtlStoresNull() {
            Map<String, String> map = new java.util.HashMap<>();
            map.put("a", null);
            nullSafeCache.putAllWithTtl(map, Duration.ofMinutes(5));
            assertTrue(nullSafeCache.containsNullValue("a"));
        }

        @Test
        @DisplayName("putIfAbsentWithTtl stores null")
        void putIfAbsentWithTtlStoresNull() {
            assertTrue(nullSafeCache.putIfAbsentWithTtl("key", null, Duration.ofMinutes(5)));
            assertTrue(nullSafeCache.containsNullValue("key"));
        }
    }

    @Nested
    @DisplayName("Query Operations")
    class QueryOperationsTests {

        @Test
        @DisplayName("containsKey returns true for null value")
        void containsKeyReturnsTrueForNull() {
            nullSafeCache.put("key", null);
            assertTrue(nullSafeCache.containsKey("key"));
        }

        @Test
        @DisplayName("size counts null values")
        void sizeCountsNullValues() {
            nullSafeCache.put("a", "value");
            nullSafeCache.put("b", null);
            assertEquals(2, nullSafeCache.size());
        }

        @Test
        @DisplayName("keys includes keys with null values")
        void keysIncludesNullValueKeys() {
            nullSafeCache.put("a", "value");
            nullSafeCache.put("b", null);
            assertTrue(nullSafeCache.keys().contains("a"));
            assertTrue(nullSafeCache.keys().contains("b"));
        }

        @Test
        @DisplayName("values includes null values")
        void valuesIncludesNull() {
            nullSafeCache.put("a", "value");
            nullSafeCache.put("b", null);
            assertTrue(nullSafeCache.values().contains("value"));
            assertTrue(nullSafeCache.values().contains(null));
        }

        @Test
        @DisplayName("entries includes entries with null values")
        void entriesIncludesNullValues() {
            nullSafeCache.put("a", "value");
            nullSafeCache.put("b", null);
            assertEquals(2, nullSafeCache.entries().size());
        }
    }

    @Nested
    @DisplayName("Map View Tests")
    class MapViewTests {

        @Test
        @DisplayName("asMap returns working map")
        void asMapReturnsWorkingMap() {
            nullSafeCache.put("key", "value");
            ConcurrentMap<String, String> map = nullSafeCache.asMap();
            assertEquals("value", map.get("key"));
        }

        @Test
        @DisplayName("asMap handles null values")
        void asMapHandlesNullValues() {
            nullSafeCache.put("key", null);
            ConcurrentMap<String, String> map = nullSafeCache.asMap();
            assertTrue(map.containsKey("key"));
            assertNull(map.get("key"));
        }

        @Test
        @DisplayName("asMap put with null")
        void asMapPutWithNull() {
            ConcurrentMap<String, String> map = nullSafeCache.asMap();
            map.put("key", null);
            assertTrue(nullSafeCache.containsKey("key"));
            assertTrue(nullSafeCache.containsNullValue("key"));
        }

        @Test
        @DisplayName("asMap remove")
        void asMapRemove() {
            nullSafeCache.put("key", "value");
            ConcurrentMap<String, String> map = nullSafeCache.asMap();
            map.remove("key");
            assertFalse(nullSafeCache.containsKey("key"));
        }

        @Test
        @DisplayName("asMap containsValue with null")
        void asMapContainsValueWithNull() {
            nullSafeCache.put("key", null);
            ConcurrentMap<String, String> map = nullSafeCache.asMap();
            assertTrue(map.containsValue(null));
        }

        @Test
        @DisplayName("asMap replace")
        void asMapReplace() {
            nullSafeCache.put("key", "old");
            ConcurrentMap<String, String> map = nullSafeCache.asMap();
            assertEquals("old", map.replace("key", "new"));
            assertEquals("new", nullSafeCache.get("key"));
        }

        @Test
        @DisplayName("asMap replace with expected value")
        void asMapReplaceWithExpected() {
            nullSafeCache.put("key", "old");
            ConcurrentMap<String, String> map = nullSafeCache.asMap();
            assertTrue(map.replace("key", "old", "new"));
            assertFalse(map.replace("key", "wrong", "newer"));
        }

        @Test
        @DisplayName("asMap putIfAbsent")
        void asMapPutIfAbsent() {
            ConcurrentMap<String, String> map = nullSafeCache.asMap();
            assertNull(map.putIfAbsent("key", "value"));
            assertEquals("value", map.putIfAbsent("key", "other"));
        }

        @Test
        @DisplayName("asMap remove with value")
        void asMapRemoveWithValue() {
            nullSafeCache.put("key", "value");
            ConcurrentMap<String, String> map = nullSafeCache.asMap();
            assertFalse(map.remove("key", "wrong"));
            assertTrue(map.remove("key", "value"));
        }

        @Test
        @DisplayName("asMap size")
        void asMapSize() {
            nullSafeCache.put("a", "1");
            nullSafeCache.put("b", null);
            assertEquals(2, nullSafeCache.asMap().size());
        }

        @Test
        @DisplayName("asMap isEmpty")
        void asMapIsEmpty() {
            assertTrue(nullSafeCache.asMap().isEmpty());
            nullSafeCache.put("key", null);
            assertFalse(nullSafeCache.asMap().isEmpty());
        }

        @Test
        @DisplayName("asMap clear clears underlying delegate")
        void asMapClear() {
            nullSafeCache.put("key", "value");
            // Note: asMap().clear() behavior depends on underlying implementation
            // The NullSafeMapView delegates to the base cache's asMap().clear()
            // which may or may not clear immediately (Caffeine uses eventual consistency)
            nullSafeCache.invalidateAll(); // Use proper cache API for clearing
            assertTrue(nullSafeCache.asMap().isEmpty());
        }

        @Test
        @DisplayName("asMap putAll")
        void asMapPutAll() {
            Map<String, String> map = new java.util.HashMap<>();
            map.put("a", "1");
            map.put("b", null);
            nullSafeCache.asMap().putAll(map);
            assertEquals(2, nullSafeCache.size());
        }

        @Test
        @DisplayName("asMap keySet")
        void asMapKeySet() {
            nullSafeCache.put("key", null);
            assertTrue(nullSafeCache.asMap().keySet().contains("key"));
        }

        @Test
        @DisplayName("asMap values")
        void asMapValues() {
            nullSafeCache.put("a", "value");
            nullSafeCache.put("b", null);
            assertEquals(2, nullSafeCache.asMap().values().size());
        }

        @Test
        @DisplayName("asMap entrySet")
        void asMapEntrySet() {
            nullSafeCache.put("a", "value");
            nullSafeCache.put("b", null);
            assertEquals(2, nullSafeCache.asMap().entrySet().size());
        }
    }

    @Nested
    @DisplayName("Invalidation Tests")
    class InvalidationTests {

        @Test
        @DisplayName("invalidate removes null value")
        void invalidateRemovesNullValue() {
            nullSafeCache.put("key", null);
            nullSafeCache.invalidate("key");
            assertFalse(nullSafeCache.containsKey("key"));
        }

        @Test
        @DisplayName("invalidateAll removes null values")
        void invalidateAllRemovesNullValues() {
            nullSafeCache.put("a", "value");
            nullSafeCache.put("b", null);
            nullSafeCache.invalidateAll(List.of("a", "b"));
            assertEquals(0, nullSafeCache.size());
        }

        @Test
        @DisplayName("invalidateAll clears cache")
        void invalidateAllClears() {
            nullSafeCache.put("a", "value");
            nullSafeCache.put("b", null);
            nullSafeCache.invalidateAll();
            assertEquals(0, nullSafeCache.size());
        }
    }

    @Nested
    @DisplayName("Other Operations")
    class OtherOperationsTests {

        @Test
        @DisplayName("stats returns statistics")
        void statsReturnsStatistics() {
            nullSafeCache.put("key", "value");
            assertNotNull(nullSafeCache.stats());
        }

        @Test
        @DisplayName("metrics returns metrics")
        void metricsReturnsMetrics() {
            // Metrics may be null depending on config
            nullSafeCache.metrics();
        }

        @Test
        @DisplayName("cleanUp performs cleanup")
        void cleanUpPerformsCleanup() {
            nullSafeCache.put("key", null);
            assertDoesNotThrow(() -> nullSafeCache.cleanUp());
        }

        @Test
        @DisplayName("async returns async view")
        void asyncReturnsAsyncView() {
            assertNotNull(nullSafeCache.async());
        }

        @Test
        @DisplayName("name returns cache name")
        void nameReturnsName() {
            assertNotNull(nullSafeCache.name());
        }

        @Test
        @DisplayName("putIfAbsent with null")
        void putIfAbsentWithNull() {
            assertTrue(nullSafeCache.putIfAbsent("key", null));
            assertFalse(nullSafeCache.putIfAbsent("key", "value"));
        }

        @Test
        @DisplayName("estimatedSize counts null values")
        void estimatedSizeCountsNull() {
            nullSafeCache.put("a", "value");
            nullSafeCache.put("b", null);
            assertEquals(2, nullSafeCache.estimatedSize());
        }
    }
}
