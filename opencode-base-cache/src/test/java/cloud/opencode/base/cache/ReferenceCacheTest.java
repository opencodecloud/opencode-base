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

import cloud.opencode.base.cache.model.RemovalCause;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for ReferenceCache
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("ReferenceCache Tests")
class ReferenceCacheTest {

    private ReferenceCache<String, String> cache;

    @BeforeEach
    void setUp() {
        cache = ReferenceCache.<String, String>builder("test-ref-cache")
                .referenceType(ReferenceCache.ReferenceType.SOFT)
                .recordStats()
                .build();
    }

    @AfterEach
    void tearDown() {
        cache.close();
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("builder() creates builder")
        void builderCreatesBuilder() {
            ReferenceCache.Builder<String, String> builder = ReferenceCache.builder("test");
            assertNotNull(builder);
        }

        @Test
        @DisplayName("builder() with null name throws exception")
        void builderWithNullNameThrows() {
            assertThrows(NullPointerException.class, () -> ReferenceCache.builder(null));
        }

        @Test
        @DisplayName("build with SOFT reference type")
        void buildWithSoftReferenceType() {
            ReferenceCache<String, String> c = ReferenceCache.<String, String>builder("soft")
                    .referenceType(ReferenceCache.ReferenceType.SOFT)
                    .build();
            assertNotNull(c);
            c.close();
        }

        @Test
        @DisplayName("build with WEAK reference type")
        void buildWithWeakReferenceType() {
            ReferenceCache<String, String> c = ReferenceCache.<String, String>builder("weak")
                    .referenceType(ReferenceCache.ReferenceType.WEAK)
                    .build();
            assertNotNull(c);
            c.close();
        }

        @Test
        @DisplayName("build with WEAK_KEYS reference type")
        void buildWithWeakKeysReferenceType() {
            ReferenceCache<String, String> c = ReferenceCache.<String, String>builder("weak-keys")
                    .referenceType(ReferenceCache.ReferenceType.WEAK_KEYS)
                    .build();
            assertNotNull(c);
            c.close();
        }

        @Test
        @DisplayName("build with SOFT_VALUES reference type")
        void buildWithSoftValuesReferenceType() {
            ReferenceCache<String, String> c = ReferenceCache.<String, String>builder("soft-values")
                    .referenceType(ReferenceCache.ReferenceType.SOFT_VALUES)
                    .build();
            assertNotNull(c);
            c.close();
        }

        @Test
        @DisplayName("build with removal listener")
        void buildWithRemovalListener() {
            List<String> removedKeys = new CopyOnWriteArrayList<>();
            ReferenceCache<String, String> c = ReferenceCache.<String, String>builder("with-listener")
                    .removalListener((k, v, cause) -> removedKeys.add(k))
                    .build();
            assertNotNull(c);
            c.close();
        }

        @Test
        @DisplayName("build with recordStats")
        void buildWithRecordStats() {
            ReferenceCache<String, String> c = ReferenceCache.<String, String>builder("with-stats")
                    .recordStats()
                    .build();
            assertNotNull(c);
            c.close();
        }
    }

    @Nested
    @DisplayName("Get Operations")
    class GetOperationsTests {

        @Test
        @DisplayName("get returns null for missing key")
        void getReturnsNullForMissing() {
            assertNull(cache.get("nonexistent"));
        }

        @Test
        @DisplayName("get returns cached value")
        void getReturnsCachedValue() {
            cache.put("key", "value");
            assertEquals("value", cache.get("key"));
        }

        @Test
        @DisplayName("get with null key throws exception")
        void getWithNullKeyThrows() {
            assertThrows(NullPointerException.class, () -> cache.get(null));
        }

        @Test
        @DisplayName("get with loader loads value")
        void getWithLoaderLoads() {
            String result = cache.get("key", k -> "loaded-" + k);
            assertEquals("loaded-key", result);
            assertTrue(cache.containsKey("key"));
        }

        @Test
        @DisplayName("get with loader returns cached")
        void getWithLoaderReturnsCached() {
            cache.put("key", "cached");
            String result = cache.get("key", k -> "loaded-" + k);
            assertEquals("cached", result);
        }

        @Test
        @DisplayName("getAll returns cached values")
        void getAllReturnsCachedValues() {
            cache.put("a", "1");
            cache.put("b", "2");
            Map<String, String> result = cache.getAll(List.of("a", "b", "c"));
            assertEquals("1", result.get("a"));
            assertEquals("2", result.get("b"));
            assertFalse(result.containsKey("c"));
        }

        @Test
        @DisplayName("getAll with loader loads missing")
        void getAllWithLoaderLoadsMissing() {
            cache.put("a", "existing");
            Map<String, String> result = cache.getAll(List.of("a", "b"),
                    keys -> Map.of("b", "loaded-b"));
            assertEquals("existing", result.get("a"));
            assertEquals("loaded-b", result.get("b"));
        }
    }

    @Nested
    @DisplayName("Put Operations")
    class PutOperationsTests {

        @Test
        @DisplayName("put stores value")
        void putStoresValue() {
            cache.put("key", "value");
            assertEquals("value", cache.get("key"));
        }

        @Test
        @DisplayName("put with null key throws exception")
        void putWithNullKeyThrows() {
            assertThrows(NullPointerException.class, () -> cache.put(null, "value"));
        }

        @Test
        @DisplayName("put with null value throws exception")
        void putWithNullValueThrows() {
            assertThrows(NullPointerException.class, () -> cache.put("key", null));
        }

        @Test
        @DisplayName("put replaces existing value")
        void putReplacesExisting() {
            cache.put("key", "old");
            cache.put("key", "new");
            assertEquals("new", cache.get("key"));
        }

        @Test
        @DisplayName("putAll stores values")
        void putAllStoresValues() {
            cache.putAll(Map.of("a", "1", "b", "2"));
            assertEquals("1", cache.get("a"));
            assertEquals("2", cache.get("b"));
        }

        @Test
        @DisplayName("putIfAbsent stores if absent")
        void putIfAbsentStoresIfAbsent() {
            assertTrue(cache.putIfAbsent("key", "value"));
            assertFalse(cache.putIfAbsent("key", "other"));
            assertEquals("value", cache.get("key"));
        }

        @Test
        @DisplayName("putWithTtl stores value (TTL ignored)")
        void putWithTtlStoresValue() {
            cache.putWithTtl("key", "value", Duration.ofMinutes(5));
            assertEquals("value", cache.get("key"));
        }

        @Test
        @DisplayName("putAllWithTtl stores values (TTL ignored)")
        void putAllWithTtlStoresValues() {
            cache.putAllWithTtl(Map.of("a", "1"), Duration.ofMinutes(5));
            assertEquals("1", cache.get("a"));
        }

        @Test
        @DisplayName("putIfAbsentWithTtl stores if absent (TTL ignored)")
        void putIfAbsentWithTtlStoresIfAbsent() {
            assertTrue(cache.putIfAbsentWithTtl("key", "value", Duration.ofMinutes(5)));
        }
    }

    @Nested
    @DisplayName("Compute Operations")
    class ComputeOperationsTests {

        @Test
        @DisplayName("computeIfPresent updates existing")
        void computeIfPresentUpdates() {
            cache.put("key", "value");
            String result = cache.computeIfPresent("key", (k, v) -> v.toUpperCase());
            assertEquals("VALUE", result);
        }

        @Test
        @DisplayName("computeIfPresent returns null for missing")
        void computeIfPresentReturnsNullForMissing() {
            assertNull(cache.computeIfPresent("key", (k, v) -> v.toUpperCase()));
        }

        @Test
        @DisplayName("computeIfPresent removes on null result")
        void computeIfPresentRemovesOnNull() {
            cache.put("key", "value");
            cache.computeIfPresent("key", (k, v) -> null);
            assertFalse(cache.containsKey("key"));
        }

        @Test
        @DisplayName("compute creates or updates")
        void computeCreatesOrUpdates() {
            String result = cache.compute("key", (k, v) -> "computed");
            assertEquals("computed", result);
        }

        @Test
        @DisplayName("compute removes on null result")
        void computeRemovesOnNull() {
            cache.put("key", "value");
            cache.compute("key", (k, v) -> null);
            assertFalse(cache.containsKey("key"));
        }

        @Test
        @DisplayName("getAndRemove returns and removes")
        void getAndRemoveReturnsAndRemoves() {
            cache.put("key", "value");
            assertEquals("value", cache.getAndRemove("key"));
            assertFalse(cache.containsKey("key"));
        }

        @Test
        @DisplayName("replace updates existing")
        void replaceUpdatesExisting() {
            cache.put("key", "old");
            assertEquals("old", cache.replace("key", "new"));
            assertEquals("new", cache.get("key"));
        }

        @Test
        @DisplayName("replace returns null for missing")
        void replaceReturnsNullForMissing() {
            assertNull(cache.replace("missing", "value"));
        }

        @Test
        @DisplayName("replace with old value checks value")
        void replaceWithOldValueChecks() {
            cache.put("key", "old");
            assertTrue(cache.replace("key", "old", "new"));
            assertFalse(cache.replace("key", "wrong", "newer"));
        }
    }

    @Nested
    @DisplayName("Invalidation Operations")
    class InvalidationOperationsTests {

        @Test
        @DisplayName("invalidate removes from cache")
        void invalidateRemovesFromCache() {
            cache.put("key", "value");
            cache.invalidate("key");
            assertFalse(cache.containsKey("key"));
        }

        @Test
        @DisplayName("invalidateAll removes keys")
        void invalidateAllRemovesKeys() {
            cache.putAll(Map.of("a", "1", "b", "2"));
            cache.invalidateAll(List.of("a", "b"));
            assertEquals(0, cache.size());
        }

        @Test
        @DisplayName("invalidateAll clears cache")
        void invalidateAllClearsCache() {
            cache.put("key", "value");
            cache.invalidateAll();
            assertEquals(0, cache.size());
        }

        @Test
        @DisplayName("invalidate with null key throws exception")
        void invalidateWithNullKeyThrows() {
            assertThrows(NullPointerException.class, () -> cache.invalidate(null));
        }
    }

    @Nested
    @DisplayName("Query Operations")
    class QueryOperationsTests {

        @Test
        @DisplayName("containsKey checks cache")
        void containsKeyChecksCache() {
            cache.put("key", "value");
            assertTrue(cache.containsKey("key"));
            assertFalse(cache.containsKey("nonexistent"));
        }

        @Test
        @DisplayName("size returns cache size")
        void sizeReturnsCacheSize() {
            cache.put("a", "1");
            cache.put("b", "2");
            assertEquals(2, cache.size());
        }

        @Test
        @DisplayName("estimatedSize returns cache size")
        void estimatedSizeReturnsCacheSize() {
            cache.put("key", "value");
            assertEquals(1, cache.estimatedSize());
        }

        @Test
        @DisplayName("keys returns cache keys")
        void keysReturnsCacheKeys() {
            cache.put("key", "value");
            assertTrue(cache.keys().contains("key"));
        }

        @Test
        @DisplayName("values returns cache values")
        void valuesReturnsCacheValues() {
            cache.put("key", "value");
            assertTrue(cache.values().contains("value"));
        }

        @Test
        @DisplayName("entries returns cache entries")
        void entriesReturnsCacheEntries() {
            cache.put("key", "value");
            assertEquals(1, cache.entries().size());
        }

        @Test
        @DisplayName("asMap returns working map")
        void asMapReturnsWorkingMap() {
            cache.put("key", "value");
            assertEquals("value", cache.asMap().get("key"));
        }
    }

    @Nested
    @DisplayName("Map View Tests")
    class MapViewTests {

        @Test
        @DisplayName("asMap put")
        void asMapPut() {
            cache.asMap().put("key", "value");
            assertEquals("value", cache.get("key"));
        }

        @Test
        @DisplayName("asMap remove")
        void asMapRemove() {
            cache.put("key", "value");
            assertEquals("value", cache.asMap().remove("key"));
            assertFalse(cache.containsKey("key"));
        }

        @Test
        @DisplayName("asMap putIfAbsent")
        void asMapPutIfAbsent() {
            assertNull(cache.asMap().putIfAbsent("key", "value"));
            assertEquals("value", cache.asMap().putIfAbsent("key", "other"));
        }

        @Test
        @DisplayName("asMap remove with value")
        void asMapRemoveWithValue() {
            cache.put("key", "value");
            assertFalse(cache.asMap().remove("key", "wrong"));
            assertTrue(cache.asMap().remove("key", "value"));
        }

        @Test
        @DisplayName("asMap replace")
        void asMapReplace() {
            cache.put("key", "old");
            assertEquals("old", cache.asMap().replace("key", "new"));
        }

        @Test
        @DisplayName("asMap replace with expected value")
        void asMapReplaceWithExpected() {
            cache.put("key", "old");
            assertTrue(cache.asMap().replace("key", "old", "new"));
            assertFalse(cache.asMap().replace("key", "wrong", "newer"));
        }

        @Test
        @DisplayName("asMap entrySet")
        void asMapEntrySet() {
            cache.put("key", "value");
            assertEquals(1, cache.asMap().entrySet().size());
        }
    }

    @Nested
    @DisplayName("Statistics")
    class StatisticsTests {

        @Test
        @DisplayName("stats returns statistics")
        void statsReturnsStatistics() {
            assertNotNull(cache.stats());
        }

        @Test
        @DisplayName("stats track hits and misses")
        void statsTrackHitsAndMisses() {
            cache.put("key", "value");
            cache.get("key"); // Hit
            cache.get("nonexistent"); // Miss

            CacheStats stats = cache.stats();
            assertTrue(stats.hitCount() > 0);
            assertTrue(stats.missCount() > 0);
        }
    }

    @Nested
    @DisplayName("Removal Listener")
    class RemovalListenerTests {

        @Test
        @DisplayName("removal listener called on invalidate")
        void removalListenerCalledOnInvalidate() {
            List<RemovalCause> causes = new CopyOnWriteArrayList<>();
            ReferenceCache<String, String> c = ReferenceCache.<String, String>builder("with-listener")
                    .removalListener((k, v, cause) -> causes.add(cause))
                    .build();

            c.put("key", "value");
            c.invalidate("key");

            assertTrue(causes.contains(RemovalCause.EXPLICIT));
            c.close();
        }

        @Test
        @DisplayName("removal listener called on replace")
        void removalListenerCalledOnReplace() {
            List<RemovalCause> causes = new CopyOnWriteArrayList<>();
            ReferenceCache<String, String> c = ReferenceCache.<String, String>builder("with-listener")
                    .removalListener((k, v, cause) -> causes.add(cause))
                    .build();

            c.put("key", "old");
            c.put("key", "new");

            assertTrue(causes.contains(RemovalCause.REPLACED));
            c.close();
        }
    }

    @Nested
    @DisplayName("Async Operations")
    class AsyncOperationsTests {

        @Test
        @DisplayName("async returns async view")
        void asyncReturnsAsyncView() {
            assertNotNull(cache.async());
        }

        @Test
        @DisplayName("async getAsync works")
        void asyncGetAsyncWorks() throws Exception {
            cache.put("key", "value");
            assertEquals("value", cache.async().getAsync("key").get());
        }

        @Test
        @DisplayName("async putAsync works")
        void asyncPutAsyncWorks() throws Exception {
            cache.async().putAsync("key", "value").get();
            assertEquals("value", cache.get("key"));
        }

        @Test
        @DisplayName("async invalidateAsync works")
        void asyncInvalidateAsyncWorks() throws Exception {
            cache.put("key", "value");
            cache.async().invalidateAsync("key").get();
            assertFalse(cache.containsKey("key"));
        }

        @Test
        @DisplayName("async sync returns sync view")
        void asyncSyncReturnsSyncView() {
            assertSame(cache, cache.async().sync());
        }
    }

    @Nested
    @DisplayName("Other Operations")
    class OtherOperationsTests {

        @Test
        @DisplayName("name returns cache name")
        void nameReturnsCacheName() {
            assertEquals("test-ref-cache", cache.name());
        }

        @Test
        @DisplayName("cleanUp processes reference queue")
        void cleanUpProcessesReferenceQueue() {
            cache.put("key", "value");
            assertDoesNotThrow(() -> cache.cleanUp());
        }

        @Test
        @DisplayName("close shuts down cleanup executor")
        void closeShutDownsCleanupExecutor() {
            ReferenceCache<String, String> c = ReferenceCache.<String, String>builder("temp")
                    .build();
            assertDoesNotThrow(() -> c.close());
        }
    }

    @Nested
    @DisplayName("Reference Type Enum")
    class ReferenceTypeEnumTests {

        @Test
        @DisplayName("all reference types are available")
        void allReferenceTypesAvailable() {
            assertEquals(4, ReferenceCache.ReferenceType.values().length);
            assertNotNull(ReferenceCache.ReferenceType.WEAK);
            assertNotNull(ReferenceCache.ReferenceType.SOFT);
            assertNotNull(ReferenceCache.ReferenceType.WEAK_KEYS);
            assertNotNull(ReferenceCache.ReferenceType.SOFT_VALUES);
        }
    }
}
