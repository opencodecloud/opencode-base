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
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for LoadingCache
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("LoadingCache Tests")
class LoadingCacheTest {

    private LoadingCache<String, String> cache;
    private AtomicInteger loadCount;

    @BeforeEach
    void setUp() {
        loadCount = new AtomicInteger(0);
        cache = LoadingCache.create("test-loading",
                key -> {
                    loadCount.incrementAndGet();
                    return "loaded-" + key;
                },
                CacheConfig.<String, String>builder()
                        .maximumSize(100)
                        .build());
    }

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodsTests {

        @Test
        @DisplayName("create with name and loader")
        void createWithNameAndLoader() {
            LoadingCache<String, Integer> lc = LoadingCache.create("test",
                    Integer::parseInt);
            assertNotNull(lc);
            assertEquals(123, lc.get("123"));
        }

        @Test
        @DisplayName("create with config")
        void createWithConfig() {
            LoadingCache<String, String> lc = LoadingCache.create("test",
                    key -> key.toUpperCase(),
                    CacheConfig.<String, String>builder()
                            .maximumSize(50)
                            .expireAfterWrite(Duration.ofMinutes(10))
                            .build());
            assertNotNull(lc);
            assertEquals("HELLO", lc.get("hello"));
        }

        @Test
        @DisplayName("create with null name throws")
        void createWithNullNameThrows() {
            assertThrows(NullPointerException.class,
                    () -> LoadingCache.create(null, key -> key));
        }

        @Test
        @DisplayName("create with null loader throws")
        void createWithNullLoaderThrows() {
            assertThrows(NullPointerException.class,
                    () -> LoadingCache.create("test", null));
        }

        @Test
        @DisplayName("createWithLoader using CacheLoader")
        void createWithCacheLoader() {
            CacheLoader<String, String> loader = key -> "loaded-" + key;
            LoadingCache<String, String> lc = LoadingCache.createWithLoader("test",
                    loader, CacheConfig.defaultConfig());
            assertNotNull(lc);
            assertEquals("loaded-key", lc.get("key"));
        }
    }

    @Nested
    @DisplayName("Get Operations")
    class GetOperationsTests {

        @Test
        @DisplayName("get loads value if absent")
        void getLoadsIfAbsent() {
            String value = cache.get("key1");
            assertEquals("loaded-key1", value);
            assertEquals(1, loadCount.get());
        }

        @Test
        @DisplayName("get returns cached value")
        void getReturnsCached() {
            cache.get("key1");
            cache.get("key1");
            assertEquals(1, loadCount.get()); // Only loaded once
        }

        @Test
        @DisplayName("get with custom loader")
        void getWithCustomLoader() {
            String value = cache.get("key1", k -> "custom-" + k);
            assertEquals("custom-key1", value);
        }

        @Test
        @DisplayName("getAll loads missing keys")
        void getAllLoadsMissing() {
            cache.put("key1", "existing");
            Map<String, String> result = cache.getAll(List.of("key1", "key2", "key3"));

            assertEquals(3, result.size());
            assertEquals("existing", result.get("key1"));
            assertEquals("loaded-key2", result.get("key2"));
            assertEquals("loaded-key3", result.get("key3"));
        }

        @Test
        @DisplayName("getAll with custom loader")
        void getAllWithCustomLoader() {
            Map<String, String> result = cache.getAll(List.of("a", "b"),
                    keys -> {
                        Map<String, String> map = new java.util.HashMap<>();
                        for (String key : keys) {
                            map.put(key, "custom-" + key);
                        }
                        return map;
                    });

            assertEquals("custom-a", result.get("a"));
            assertEquals("custom-b", result.get("b"));
        }
    }

    @Nested
    @DisplayName("Refresh Operations")
    class RefreshOperationsTests {

        @Test
        @DisplayName("refresh reloads value")
        void refreshReloadsValue() throws Exception {
            cache.put("key1", "old-value");
            CompletableFuture<String> future = cache.refresh("key1");
            String newValue = future.get();

            assertEquals("loaded-key1", newValue);
            assertEquals("loaded-key1", cache.get("key1"));
        }

        @Test
        @DisplayName("refresh loads new key")
        void refreshLoadsNewKey() throws Exception {
            CompletableFuture<String> future = cache.refresh("new-key");
            String value = future.get();

            assertEquals("loaded-new-key", value);
            assertTrue(cache.containsKey("new-key"));
        }

        @Test
        @DisplayName("refreshAll refreshes matching keys")
        void refreshAllMatchingKeys() {
            cache.put("user:1", "user1");
            cache.put("user:2", "user2");
            cache.put("product:1", "product1");

            int count = cache.refreshAll(key -> key.startsWith("user:"));
            assertEquals(2, count);
        }
    }

    @Nested
    @DisplayName("Loader Access")
    class LoaderAccessTests {

        @Test
        @DisplayName("loader() returns the loader")
        void loaderReturnsLoader() {
            assertNotNull(cache.loader());
            assertEquals("loaded-test", cache.loader().apply("test"));
        }
    }

    @Nested
    @DisplayName("Async Loading")
    class AsyncLoadingTests {

        @Test
        @DisplayName("asyncLoading() returns async view")
        void asyncLoadingReturnsView() {
            AsyncLoadingCache<String, String> async = cache.asyncLoading();
            assertNotNull(async);
        }

        @Test
        @DisplayName("async getAsync loads value")
        void asyncGetAsyncLoads() throws Exception {
            AsyncLoadingCache<String, String> async = cache.asyncLoading();
            CompletableFuture<String> future = async.getAsync("key");
            String value = future.get();

            assertEquals("loaded-key", value);
        }

        @Test
        @DisplayName("async getAllAsync loads values")
        void asyncGetAllAsyncLoads() throws Exception {
            AsyncLoadingCache<String, String> async = cache.asyncLoading();
            CompletableFuture<Map<String, String>> future = async.getAllAsync(List.of("a", "b"));
            Map<String, String> result = future.get();

            assertEquals(2, result.size());
            assertEquals("loaded-a", result.get("a"));
        }

        @Test
        @DisplayName("async refreshAsync reloads")
        void asyncRefreshAsyncReloads() throws Exception {
            cache.put("key", "old");
            AsyncLoadingCache<String, String> async = cache.asyncLoading();
            CompletableFuture<String> future = async.refreshAsync("key");
            String newValue = future.get();

            assertEquals("loaded-key", newValue);
        }

        @Test
        @DisplayName("async sync() returns sync view")
        void asyncSyncReturnsView() {
            AsyncLoadingCache<String, String> async = cache.asyncLoading();
            LoadingCache<String, String> sync = async.sync();
            assertSame(cache, sync);
        }
    }

    @Nested
    @DisplayName("Cache Operations")
    class CacheOperationsTests {

        @Test
        @DisplayName("put stores value")
        void putStoresValue() {
            cache.put("key", "value");
            assertEquals("value", cache.get("key"));
            assertEquals(0, loadCount.get()); // No load triggered
        }

        @Test
        @DisplayName("putAll stores values")
        void putAllStoresValues() {
            cache.putAll(Map.of("a", "1", "b", "2"));
            assertEquals("1", cache.get("a"));
            assertEquals("2", cache.get("b"));
        }

        @Test
        @DisplayName("invalidate removes value")
        void invalidateRemovesValue() {
            cache.get("key"); // Load it
            cache.invalidate("key");
            assertFalse(cache.containsKey("key"));
        }

        @Test
        @DisplayName("invalidateAll clears cache")
        void invalidateAllClearsCache() {
            cache.get("a");
            cache.get("b");
            cache.invalidateAll();
            assertEquals(0, cache.estimatedSize());
        }

        @Test
        @DisplayName("containsKey checks existence")
        void containsKeyChecks() {
            assertFalse(cache.containsKey("key"));
            cache.get("key");
            assertTrue(cache.containsKey("key"));
        }

        @Test
        @DisplayName("size returns count")
        void sizeReturnsCount() {
            assertEquals(0, cache.size());
            cache.get("a");
            cache.get("b");
            assertEquals(2, cache.size());
        }

        @Test
        @DisplayName("keys returns all keys")
        void keysReturnsAll() {
            cache.put("a", "1");
            cache.put("b", "2");
            Set<String> keys = cache.keys();
            assertTrue(keys.contains("a"));
            assertTrue(keys.contains("b"));
        }

        @Test
        @DisplayName("values returns all values")
        void valuesReturnsAll() {
            cache.put("a", "1");
            cache.put("b", "2");
            assertTrue(cache.values().contains("1"));
            assertTrue(cache.values().contains("2"));
        }

        @Test
        @DisplayName("stats returns statistics")
        void statsReturnsStatistics() {
            cache.get("a");
            cache.get("a");
            CacheStats stats = cache.stats();
            assertNotNull(stats);
        }

        @Test
        @DisplayName("name returns cache name")
        void nameReturnsCacheName() {
            assertEquals("test-loading", cache.name());
        }

        @Test
        @DisplayName("cleanUp performs cleanup")
        void cleanUpPerformsCleanup() {
            cache.get("key");
            assertDoesNotThrow(() -> cache.cleanUp());
        }

        @Test
        @DisplayName("async returns async view")
        void asyncReturnsView() {
            AsyncCache<String, String> async = cache.async();
            assertNotNull(async);
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
        @DisplayName("compute creates or updates")
        void computeCreatesOrUpdates() {
            String result = cache.compute("key", (k, v) -> "computed");
            assertEquals("computed", result);
        }

        @Test
        @DisplayName("getAndRemove returns and removes")
        void getAndRemoveReturnsAndRemoves() {
            cache.put("key", "value");
            String result = cache.getAndRemove("key");
            assertEquals("value", result);
            assertFalse(cache.containsKey("key"));
        }

        @Test
        @DisplayName("replace updates existing")
        void replaceUpdatesExisting() {
            cache.put("key", "old");
            String old = cache.replace("key", "new");
            assertEquals("old", old);
            assertEquals("new", cache.get("key"));
        }

        @Test
        @DisplayName("replace with oldValue checks value")
        void replaceWithOldValueChecks() {
            cache.put("key", "old");
            assertTrue(cache.replace("key", "old", "new"));
            assertFalse(cache.replace("key", "wrong", "newer"));
        }
    }

    @Nested
    @DisplayName("TTL Operations")
    class TtlOperationsTests {

        @Test
        @DisplayName("putWithTtl stores with TTL")
        void putWithTtlStores() {
            cache.putWithTtl("key", "value", Duration.ofMinutes(5));
            assertEquals("value", cache.get("key"));
        }

        @Test
        @DisplayName("putAllWithTtl stores all with TTL")
        void putAllWithTtlStores() {
            cache.putAllWithTtl(Map.of("a", "1", "b", "2"), Duration.ofMinutes(5));
            assertEquals("1", cache.get("a"));
            assertEquals("2", cache.get("b"));
        }

        @Test
        @DisplayName("putIfAbsentWithTtl stores if absent")
        void putIfAbsentWithTtlStores() {
            assertTrue(cache.putIfAbsentWithTtl("key", "value", Duration.ofMinutes(5)));
            assertFalse(cache.putIfAbsentWithTtl("key", "other", Duration.ofMinutes(5)));
            assertEquals("value", cache.get("key"));
        }
    }
}
