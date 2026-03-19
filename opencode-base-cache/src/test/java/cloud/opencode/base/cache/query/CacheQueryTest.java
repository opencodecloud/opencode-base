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

package cloud.opencode.base.cache.query;

import cloud.opencode.base.cache.Cache;
import cloud.opencode.base.cache.OpenCache;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for CacheQuery
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("CacheQuery Tests")
class CacheQueryTest {

    private Cache<String, String> cache;

    @BeforeEach
    void setUp() {
        cache = OpenCache.<String, String>builder()
                .maximumSize(1000)
                .build("query-test-" + System.nanoTime());

        // Populate test data
        cache.put("user:1", "Alice");
        cache.put("user:2", "Bob");
        cache.put("user:3", "Charlie");
        cache.put("product:1", "Phone");
        cache.put("product:2", "Laptop");
        cache.put("order:2024-01-01:1", "Order1");
        cache.put("order:2024-01-15:2", "Order2");
        cache.put("order:2024-02-01:3", "Order3");
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("from creates query")
        void fromCreatesQuery() {
            CacheQuery<String, String> query = CacheQuery.from(cache);

            assertNotNull(query);
        }

        @Test
        @DisplayName("from throws on null cache")
        void fromThrowsOnNullCache() {
            assertThrows(NullPointerException.class, () -> CacheQuery.from(null));
        }
    }

    @Nested
    @DisplayName("Key Filter Tests")
    class KeyFilterTests {

        @Test
        @DisplayName("keyFilter filters by predicate")
        void keyFilterFiltersByPredicate() {
            List<String> keys = CacheQuery.from(cache)
                    .keyFilter(k -> k.startsWith("user:"))
                    .keys();

            assertEquals(3, keys.size());
            assertTrue(keys.stream().allMatch(k -> k.startsWith("user:")));
        }

        @Test
        @DisplayName("keyPrefix filters by prefix")
        void keyPrefixFiltersByPrefix() {
            List<String> keys = CacheQuery.from(cache)
                    .keyPrefix("product:")
                    .keys();

            assertEquals(2, keys.size());
        }

        @Test
        @DisplayName("keySuffix filters by suffix")
        void keySuffixFiltersBySuffix() {
            List<String> keys = CacheQuery.from(cache)
                    .keySuffix(":1")
                    .keys();

            assertTrue(keys.contains("user:1"));
            assertTrue(keys.contains("product:1"));
        }

        @Test
        @DisplayName("keyPattern filters by glob pattern")
        void keyPatternFiltersByGlobPattern() {
            List<String> keys = CacheQuery.from(cache)
                    .keyPattern("user:*")
                    .keys();

            assertEquals(3, keys.size());
        }

        @Test
        @DisplayName("keyPattern handles complex patterns")
        void keyPatternHandlesComplexPatterns() {
            List<String> keys = CacheQuery.from(cache)
                    .keyPattern("order:2024-01-*:*")
                    .keys();

            assertEquals(2, keys.size());
        }

        @Test
        @DisplayName("keyRegex filters by regex")
        void keyRegexFiltersByRegex() {
            List<String> keys = CacheQuery.from(cache)
                    .keyRegex("user:\\d+")
                    .keys();

            assertEquals(3, keys.size());
        }

        @Test
        @DisplayName("keyRange filters by range")
        void keyRangeFiltersByRange() {
            List<String> keys = CacheQuery.from(cache)
                    .keyRange("order:2024-01-01", "order:2024-02-01")
                    .keys();

            assertTrue(keys.size() >= 2);
        }

        @Test
        @DisplayName("keyIn filters by set")
        void keyInFiltersBySet() {
            List<String> keys = CacheQuery.from(cache)
                    .keyIn(Set.of("user:1", "user:2"))
                    .keys();

            assertEquals(2, keys.size());
            assertTrue(keys.contains("user:1"));
            assertTrue(keys.contains("user:2"));
        }

        @Test
        @DisplayName("keyNotIn excludes set")
        void keyNotInExcludesSet() {
            List<String> keys = CacheQuery.from(cache)
                    .keyPrefix("user:")
                    .keyNotIn(Set.of("user:1"))
                    .keys();

            assertEquals(2, keys.size());
            assertFalse(keys.contains("user:1"));
        }

        @Test
        @DisplayName("multiple key filters combine with AND")
        void multipleKeyFiltersCombineWithAnd() {
            List<String> keys = CacheQuery.from(cache)
                    .keyPrefix("user:")
                    .keySuffix(":1")
                    .keys();

            assertEquals(1, keys.size());
            assertTrue(keys.contains("user:1"));
        }
    }

    @Nested
    @DisplayName("Value Filter Tests")
    class ValueFilterTests {

        @Test
        @DisplayName("valueFilter filters by predicate")
        void valueFilterFiltersByPredicate() {
            List<String> values = CacheQuery.from(cache)
                    .keyPrefix("user:")
                    .valueFilter(v -> v.startsWith("A"))
                    .values();

            assertEquals(1, values.size());
            assertTrue(values.contains("Alice"));
        }

        @Test
        @DisplayName("nonNull filters values")
        void nonNullFiltersOutNulls() {
            // Note: OpenCache doesn't allow null values, so we just verify
            // that nonNull() filter works with non-null values
            List<String> values = CacheQuery.from(cache)
                    .nonNull()
                    .values();

            // All values should be present (none are null)
            assertFalse(values.isEmpty());
            assertTrue(values.stream().noneMatch(java.util.Objects::isNull));
        }

        @Test
        @DisplayName("entryFilter filters by key and value")
        void entryFilterFiltersByKeyAndValue() {
            CacheQuery.Result<String, String> result = CacheQuery.from(cache)
                    .entryFilter((k, v) -> k.startsWith("user:") && v.length() > 3)
                    .execute();

            assertTrue(result.values().stream().allMatch(v -> v.length() > 3));
        }
    }

    @Nested
    @DisplayName("Sorting Tests")
    class SortingTests {

        @Test
        @DisplayName("orderByKey sorts ascending")
        void orderByKeySortsAscending() {
            List<String> keys = CacheQuery.from(cache)
                    .keyPrefix("user:")
                    .orderByKey()
                    .keys();

            assertEquals("user:1", keys.get(0));
            assertEquals("user:2", keys.get(1));
            assertEquals("user:3", keys.get(2));
        }

        @Test
        @DisplayName("orderByKeyDesc sorts descending")
        void orderByKeyDescSortsDescending() {
            List<String> keys = CacheQuery.from(cache)
                    .keyPrefix("user:")
                    .orderByKeyDesc()
                    .keys();

            assertEquals("user:3", keys.get(0));
            assertEquals("user:2", keys.get(1));
            assertEquals("user:1", keys.get(2));
        }

        @Test
        @DisplayName("orderBy uses custom comparator")
        void orderByUsesCustomComparator() {
            List<String> values = CacheQuery.from(cache)
                    .keyPrefix("user:")
                    .orderBy((e1, e2) -> e1.getValue().compareTo(e2.getValue()))
                    .values();

            assertEquals("Alice", values.get(0));
            assertEquals("Bob", values.get(1));
            assertEquals("Charlie", values.get(2));
        }
    }

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        @DisplayName("skip skips first N results")
        void skipSkipsFirstNResults() {
            List<String> keys = CacheQuery.from(cache)
                    .keyPrefix("user:")
                    .orderByKey()
                    .skip(1)
                    .keys();

            assertEquals(2, keys.size());
            assertFalse(keys.contains("user:1"));
        }

        @Test
        @DisplayName("limit limits results to N")
        void limitLimitsResultsToN() {
            List<String> keys = CacheQuery.from(cache)
                    .keyPrefix("user:")
                    .limit(2)
                    .keys();

            assertEquals(2, keys.size());
        }

        @Test
        @DisplayName("page applies pagination")
        void pageAppliesPagination() {
            List<String> keys = CacheQuery.from(cache)
                    .keyPrefix("user:")
                    .orderByKey()
                    .page(1, 2) // Page 1, size 2
                    .keys();

            assertEquals(1, keys.size()); // Only 1 result on page 1
        }
    }

    @Nested
    @DisplayName("Execution Tests")
    class ExecutionTests {

        @Test
        @DisplayName("execute returns result")
        void executeReturnsResult() {
            CacheQuery.Result<String, String> result = CacheQuery.from(cache)
                    .keyPrefix("user:")
                    .execute();

            assertEquals(3, result.size());
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("keys returns only keys")
        void keysReturnsOnlyKeys() {
            List<String> keys = CacheQuery.from(cache)
                    .keyPrefix("user:")
                    .keys();

            assertEquals(3, keys.size());
            assertTrue(keys.stream().allMatch(k -> k.startsWith("user:")));
        }

        @Test
        @DisplayName("values returns only values")
        void valuesReturnsOnlyValues() {
            List<String> values = CacheQuery.from(cache)
                    .keyPrefix("user:")
                    .values();

            assertEquals(3, values.size());
        }

        @Test
        @DisplayName("toMap returns as map")
        void toMapReturnsAsMap() {
            Map<String, String> map = CacheQuery.from(cache)
                    .keyPrefix("user:")
                    .toMap();

            assertEquals(3, map.size());
            assertEquals("Alice", map.get("user:1"));
        }

        @Test
        @DisplayName("count returns count")
        void countReturnsCount() {
            long count = CacheQuery.from(cache)
                    .keyPrefix("user:")
                    .count();

            assertEquals(3, count);
        }

        @Test
        @DisplayName("first returns first match from filtered results")
        void firstReturnsFirstMatch() {
            Optional<Map.Entry<String, String>> first = CacheQuery.from(cache)
                    .keyPrefix("user:")
                    .first();

            // First returns any matching entry, not necessarily ordered
            assertTrue(first.isPresent());
            assertTrue(first.get().getKey().startsWith("user:"));
        }

        @Test
        @DisplayName("first returns empty when no match")
        void firstReturnsEmptyWhenNoMatch() {
            Optional<Map.Entry<String, String>> first = CacheQuery.from(cache)
                    .keyPrefix("non-existent:")
                    .first();

            assertFalse(first.isPresent());
        }

        @Test
        @DisplayName("exists returns true when match exists")
        void existsReturnsTrueWhenMatchExists() {
            boolean exists = CacheQuery.from(cache)
                    .keyPrefix("user:")
                    .exists();

            assertTrue(exists);
        }

        @Test
        @DisplayName("exists returns false when no match")
        void existsReturnsFalseWhenNoMatch() {
            boolean exists = CacheQuery.from(cache)
                    .keyPrefix("non-existent:")
                    .exists();

            assertFalse(exists);
        }

        @Test
        @DisplayName("stream returns stream for custom processing")
        void streamReturnsStreamForCustomProcessing() {
            long count = CacheQuery.from(cache)
                    .keyPrefix("user:")
                    .stream()
                    .count();

            assertEquals(3, count);
        }
    }

    @Nested
    @DisplayName("Result Tests")
    class ResultTests {

        @Test
        @DisplayName("Result size returns entry count")
        void resultSizeReturnsEntryCount() {
            CacheQuery.Result<String, String> result = CacheQuery.from(cache)
                    .keyPrefix("user:")
                    .execute();

            assertEquals(3, result.size());
        }

        @Test
        @DisplayName("Result isEmpty returns true for empty")
        void resultIsEmptyReturnsTrueForEmpty() {
            CacheQuery.Result<String, String> result = CacheQuery.from(cache)
                    .keyPrefix("non-existent:")
                    .execute();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Result keys returns key list")
        void resultKeysReturnsKeyList() {
            CacheQuery.Result<String, String> result = CacheQuery.from(cache)
                    .keyPrefix("user:")
                    .execute();

            List<String> keys = result.keys();
            assertEquals(3, keys.size());
        }

        @Test
        @DisplayName("Result values returns value list")
        void resultValuesReturnsValueList() {
            CacheQuery.Result<String, String> result = CacheQuery.from(cache)
                    .keyPrefix("user:")
                    .execute();

            List<String> values = result.values();
            assertEquals(3, values.size());
        }

        @Test
        @DisplayName("Result toMap converts to map")
        void resultToMapConvertsToMap() {
            CacheQuery.Result<String, String> result = CacheQuery.from(cache)
                    .keyPrefix("user:")
                    .execute();

            Map<String, String> map = result.toMap();
            assertEquals(3, map.size());
        }

        @Test
        @DisplayName("Result executionTimeMs returns execution time")
        void resultExecutionTimeMsReturnsExecutionTime() {
            CacheQuery.Result<String, String> result = CacheQuery.from(cache)
                    .execute();

            assertTrue(result.executionTimeMs() >= 0);
        }

        @Test
        @DisplayName("Result hasMore returns true when at limit")
        void resultHasMoreReturnsTrueWhenAtLimit() {
            CacheQuery.Result<String, String> result = CacheQuery.from(cache)
                    .keyPrefix("user:")
                    .limit(2)
                    .execute();

            assertTrue(result.hasMore());
        }

        @Test
        @DisplayName("Result hasMore returns false when under limit")
        void resultHasMoreReturnsFalseWhenUnderLimit() {
            CacheQuery.Result<String, String> result = CacheQuery.from(cache)
                    .keyPrefix("user:")
                    .limit(10)
                    .execute();

            assertFalse(result.hasMore());
        }

        @Test
        @DisplayName("Result record accessors work")
        void resultRecordAccessorsWork() {
            CacheQuery.Result<String, String> result = CacheQuery.from(cache)
                    .skip(1)
                    .limit(5)
                    .execute();

            assertTrue(result.totalInCache() > 0);
            assertEquals(1, result.skipped());
            assertEquals(5, result.limit());
            assertTrue(result.executionTimeNanos() >= 0);
        }
    }
}
