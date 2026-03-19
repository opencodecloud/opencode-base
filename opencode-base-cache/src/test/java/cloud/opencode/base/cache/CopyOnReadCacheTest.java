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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for CopyOnReadCache
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("CopyOnReadCache Tests")
class CopyOnReadCacheTest {

    private Cache<String, String> baseCache;
    private Cache<String, String> copyOnReadCache;

    @BeforeEach
    void setUp() {
        baseCache = OpenCache.<String, String>builder()
                .maximumSize(100)
                .build();
        copyOnReadCache = CopyOnReadCache.wrap(baseCache)
                .copier(s -> s + "-copy")
                .build();
    }

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodsTests {

        @Test
        @DisplayName("wrap() creates builder")
        void wrapCreatesBuilder() {
            CopyOnReadCache.Builder<String, String> builder = CopyOnReadCache.wrap(baseCache);
            assertNotNull(builder);
        }

        @Test
        @DisplayName("wrap() with null throws exception")
        void wrapWithNullThrows() {
            assertThrows(NullPointerException.class, () -> CopyOnReadCache.wrap(null));
        }

        @Test
        @DisplayName("build with default copier")
        void buildWithDefaultCopier() {
            Cache<String, String> cache = CopyOnReadCache.wrap(baseCache).build();
            assertNotNull(cache);
        }

        @Test
        @DisplayName("build with custom copier")
        void buildWithCustomCopier() {
            Cache<String, String> cache = CopyOnReadCache.wrap(baseCache)
                    .copier(s -> s.toUpperCase())
                    .build();
            cache.put("key", "value");
            assertEquals("VALUE", cache.get("key"));
        }
    }

    @Nested
    @DisplayName("Copy On Read Behavior")
    class CopyOnReadBehaviorTests {

        @Test
        @DisplayName("get returns copy of value")
        void getReturnsCopy() {
            copyOnReadCache.put("key", "value");
            String result = copyOnReadCache.get("key");
            assertEquals("value-copy", result);
        }

        @Test
        @DisplayName("get with loader returns copy")
        void getWithLoaderReturnsCopy() {
            String result = copyOnReadCache.get("key", k -> "loaded");
            assertEquals("loaded-copy", result);
        }

        @Test
        @DisplayName("getAll returns copies")
        void getAllReturnsCopies() {
            copyOnReadCache.put("a", "value-a");
            copyOnReadCache.put("b", "value-b");
            Map<String, String> result = copyOnReadCache.getAll(List.of("a", "b"));
            assertEquals("value-a-copy", result.get("a"));
            assertEquals("value-b-copy", result.get("b"));
        }

        @Test
        @DisplayName("original value is not modified")
        void originalValueNotModified() {
            baseCache.put("key", "original");
            Cache<String, String> cache = CopyOnReadCache.wrap(baseCache)
                    .copier(s -> s + "-modified")
                    .build();
            cache.get("key");
            assertEquals("original", baseCache.get("key"));
        }
    }

    @Nested
    @DisplayName("Null Handling")
    class NullHandlingTests {

        @Test
        @DisplayName("get returns null for missing key")
        void getReturnsNullForMissing() {
            assertNull(copyOnReadCache.get("nonexistent"));
        }

        @Test
        @DisplayName("copier receives null returns null")
        void copierReceivesNull() {
            Cache<String, String> cache = CopyOnReadCache.wrap(baseCache)
                    .copier(s -> s == null ? null : s + "-copy")
                    .build();
            assertNull(cache.get("nonexistent"));
        }
    }

    @Nested
    @DisplayName("Put Operations")
    class PutOperationsTests {

        @Test
        @DisplayName("put stores original value")
        void putStoresOriginal() {
            copyOnReadCache.put("key", "value");
            assertEquals("value", baseCache.get("key")); // Original stored
        }

        @Test
        @DisplayName("putAll stores original values")
        void putAllStoresOriginals() {
            copyOnReadCache.putAll(Map.of("a", "1", "b", "2"));
            assertEquals("1", baseCache.get("a"));
            assertEquals("2", baseCache.get("b"));
        }

        @Test
        @DisplayName("putIfAbsent stores original")
        void putIfAbsentStoresOriginal() {
            copyOnReadCache.putIfAbsent("key", "value");
            assertEquals("value", baseCache.get("key"));
        }

        @Test
        @DisplayName("putWithTtl stores original")
        void putWithTtlStoresOriginal() {
            copyOnReadCache.putWithTtl("key", "value", Duration.ofMinutes(5));
            assertEquals("value", baseCache.get("key"));
        }
    }

    @Nested
    @DisplayName("Query Operations")
    class QueryOperationsTests {

        @Test
        @DisplayName("containsKey delegates to base")
        void containsKeyDelegates() {
            baseCache.put("key", "value");
            assertTrue(copyOnReadCache.containsKey("key"));
        }

        @Test
        @DisplayName("size delegates to base")
        void sizeDelegates() {
            baseCache.put("a", "1");
            baseCache.put("b", "2");
            assertEquals(2, copyOnReadCache.size());
        }

        @Test
        @DisplayName("keys delegates to base")
        void keysDelegates() {
            baseCache.put("key", "value");
            assertTrue(copyOnReadCache.keys().contains("key"));
        }

        @Test
        @DisplayName("values returns copies")
        void valuesReturnsCopies() {
            copyOnReadCache.put("key", "value");
            assertTrue(copyOnReadCache.values().stream().anyMatch(v -> v.equals("value-copy")));
        }

        @Test
        @DisplayName("entries returns copies")
        void entriesReturnsCopies() {
            copyOnReadCache.put("key", "value");
            copyOnReadCache.entries().forEach(entry ->
                    assertEquals("value-copy", entry.getValue()));
        }
    }

    @Nested
    @DisplayName("Invalidation Operations")
    class InvalidationOperationsTests {

        @Test
        @DisplayName("invalidate delegates to base")
        void invalidateDelegates() {
            copyOnReadCache.put("key", "value");
            copyOnReadCache.invalidate("key");
            assertFalse(baseCache.containsKey("key"));
        }

        @Test
        @DisplayName("invalidateAll delegates to base")
        void invalidateAllDelegates() {
            copyOnReadCache.put("key", "value");
            copyOnReadCache.invalidateAll();
            assertEquals(0, baseCache.size());
        }
    }

    @Nested
    @DisplayName("Other Operations")
    class OtherOperationsTests {

        @Test
        @DisplayName("stats delegates to base")
        void statsDelegates() {
            assertNotNull(copyOnReadCache.stats());
        }

        @Test
        @DisplayName("cleanUp delegates to base")
        void cleanUpDelegates() {
            assertDoesNotThrow(() -> copyOnReadCache.cleanUp());
        }

        @Test
        @DisplayName("async returns async view")
        void asyncReturnsAsyncView() {
            assertNotNull(copyOnReadCache.async());
        }

        @Test
        @DisplayName("name delegates to base")
        void nameDelegates() {
            assertNotNull(copyOnReadCache.name());
        }

        @Test
        @DisplayName("asMap operations work with copies")
        void asMapOperationsWork() {
            copyOnReadCache.put("key", "value");
            String result = copyOnReadCache.asMap().get("key");
            assertEquals("value-copy", result);
        }
    }
}
