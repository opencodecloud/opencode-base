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

package cloud.opencode.base.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for OpenStream class
 * OpenStream 类的全面测试
 *
 * @author Test
 * @since JDK 25, opencode-base-core V1.0.0
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("OpenStream Tests")
class OpenStreamTest {

    // ==================== Stream Creation Tests ====================

    @Nested
    @DisplayName("Stream Creation - of()")
    class OfTests {

        @Test
        @DisplayName("of with varargs")
        void ofWithVarargs() {
            List<String> result = OpenStream.of("a", "b", "c").toList();
            assertEquals(List.of("a", "b", "c"), result);
        }

        @Test
        @DisplayName("of with null array")
        void ofWithNullArray() {
            List<String> result = OpenStream.of((String[]) null).toList();
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("of with empty array")
        void ofWithEmptyArray() {
            List<String> result = OpenStream.<String>of().toList();
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("of with single element")
        void ofWithSingleElement() {
            List<String> result = OpenStream.of("single").toList();
            assertEquals(List.of("single"), result);
        }
    }

    @Nested
    @DisplayName("Stream Creation - from(Iterable)")
    class FromIterableTests {

        @Test
        @DisplayName("from list")
        void fromList() {
            List<String> result = OpenStream.from(List.of("x", "y")).toList();
            assertEquals(List.of("x", "y"), result);
        }

        @Test
        @DisplayName("from set")
        void fromSet() {
            Set<Integer> set = new LinkedHashSet<>(Arrays.asList(1, 2, 3));
            List<Integer> result = OpenStream.from(set).toList();
            assertEquals(List.of(1, 2, 3), result);
        }

        @Test
        @DisplayName("from null iterable")
        void fromNullIterable() {
            List<String> result = OpenStream.from((Iterable<String>) null).toList();
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("from custom iterable")
        void fromCustomIterable() {
            Iterable<Integer> iterable = () -> List.of(1, 2, 3).iterator();
            List<Integer> result = OpenStream.from(iterable).toList();
            assertEquals(List.of(1, 2, 3), result);
        }
    }

    @Nested
    @DisplayName("Stream Creation - from(Iterator)")
    class FromIteratorTests {

        @Test
        @DisplayName("from iterator")
        void fromIterator() {
            Iterator<String> iter = List.of("a", "b").iterator();
            List<String> result = OpenStream.from(iter).toList();
            assertEquals(List.of("a", "b"), result);
        }

        @Test
        @DisplayName("from null iterator")
        void fromNullIterator() {
            List<String> result = OpenStream.from((Iterator<String>) null).toList();
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("from empty iterator")
        void fromEmptyIterator() {
            List<String> result = OpenStream.from(Collections.<String>emptyIterator()).toList();
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Stream Creation - from(Optional)")
    class FromOptionalTests {

        @Test
        @DisplayName("from present optional")
        void fromPresentOptional() {
            List<String> result = OpenStream.from(Optional.of("value")).toList();
            assertEquals(List.of("value"), result);
        }

        @Test
        @DisplayName("from empty optional")
        void fromEmptyOptional() {
            List<String> result = OpenStream.from(Optional.<String>empty()).toList();
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("from null optional")
        void fromNullOptional() {
            List<String> result = OpenStream.from((Optional<String>) null).toList();
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Stream Creation - range methods")
    class RangeTests {

        @Test
        @DisplayName("int range")
        void intRange() {
            List<Integer> result = OpenStream.range(0, 5).boxed().toList();
            assertEquals(List.of(0, 1, 2, 3, 4), result);
        }

        @Test
        @DisplayName("int range closed")
        void intRangeClosed() {
            List<Integer> result = OpenStream.rangeClosed(1, 3).boxed().toList();
            assertEquals(List.of(1, 2, 3), result);
        }

        @Test
        @DisplayName("long range")
        void longRange() {
            List<Long> result = OpenStream.range(0L, 3L).boxed().toList();
            assertEquals(List.of(0L, 1L, 2L), result);
        }
    }

    @Nested
    @DisplayName("Stream Creation - generate and iterate")
    class GenerateIterateTests {

        @Test
        @DisplayName("generate")
        void generate() {
            List<String> result = OpenStream.generate(() -> "x").limit(3).toList();
            assertEquals(List.of("x", "x", "x"), result);
        }

        @Test
        @DisplayName("iterate")
        void iterate() {
            List<Integer> result = OpenStream.iterate(1, n -> n * 2).limit(4).toList();
            assertEquals(List.of(1, 2, 4, 8), result);
        }

        @Test
        @DisplayName("iterate with predicate")
        void iterateWithPredicate() {
            List<Integer> result = OpenStream.iterate(1, n -> n < 10, n -> n + 2).toList();
            assertEquals(List.of(1, 3, 5, 7, 9), result);
        }
    }

    // ==================== Batch Processing Tests ====================

    @Nested
    @DisplayName("Batch Processing")
    class BatchTests {

        @Test
        @DisplayName("batch stream into lists")
        void batchStream() {
            List<List<Integer>> batches = OpenStream.batch(
                Stream.of(1, 2, 3, 4, 5), 2);
            assertEquals(3, batches.size());
            assertEquals(List.of(1, 2), batches.get(0));
            assertEquals(List.of(3, 4), batches.get(1));
            assertEquals(List.of(5), batches.get(2));
        }

        @Test
        @DisplayName("batch with null stream")
        void batchNullStream() {
            List<List<Integer>> batches = OpenStream.batch(null, 2);
            assertTrue(batches.isEmpty());
        }

        @Test
        @DisplayName("batch with invalid size")
        void batchInvalidSize() {
            List<List<Integer>> batches = OpenStream.batch(Stream.of(1, 2), 0);
            assertTrue(batches.isEmpty());
        }

        @Test
        @DisplayName("batchStream returns stream of batches")
        void batchStreamMethod() {
            List<List<Integer>> batches = OpenStream.batchStream(
                List.of(1, 2, 3, 4, 5), 2).toList();
            assertEquals(3, batches.size());
        }

        @Test
        @DisplayName("batchStream with null collection")
        void batchStreamNullCollection() {
            List<List<Integer>> batches = OpenStream.<Integer>batchStream(null, 2).toList();
            assertTrue(batches.isEmpty());
        }

        @Test
        @DisplayName("batchStream with empty collection")
        void batchStreamEmptyCollection() {
            List<List<Integer>> batches = OpenStream.batchStream(List.<Integer>of(), 2).toList();
            assertTrue(batches.isEmpty());
        }
    }

    // ==================== Windowing Tests ====================

    @Nested
    @DisplayName("Windowing")
    class WindowingTests {

        @Test
        @DisplayName("sliding window")
        void slidingWindow() {
            List<List<Integer>> windows = OpenStream.slidingWindow(
                List.of(1, 2, 3, 4, 5), 3).toList();
            assertEquals(3, windows.size());
            assertEquals(List.of(1, 2, 3), windows.get(0));
            assertEquals(List.of(2, 3, 4), windows.get(1));
            assertEquals(List.of(3, 4, 5), windows.get(2));
        }

        @Test
        @DisplayName("sliding window with step")
        void slidingWindowWithStep() {
            List<List<Integer>> windows = OpenStream.slidingWindow(
                List.of(1, 2, 3, 4, 5, 6), 2, 2).toList();
            assertEquals(3, windows.size());
            assertEquals(List.of(1, 2), windows.get(0));
            assertEquals(List.of(3, 4), windows.get(1));
            assertEquals(List.of(5, 6), windows.get(2));
        }

        @Test
        @DisplayName("sliding window with size larger than collection")
        void slidingWindowLargerSize() {
            List<List<Integer>> windows = OpenStream.slidingWindow(
                List.of(1, 2), 5).toList();
            assertEquals(1, windows.size());
            assertEquals(List.of(1, 2), windows.get(0));
        }

        @Test
        @DisplayName("sliding window with null collection")
        void slidingWindowNull() {
            List<List<Integer>> windows = OpenStream.<Integer>slidingWindow(null, 2).toList();
            assertTrue(windows.isEmpty());
        }

        @Test
        @DisplayName("tumbling window")
        void tumblingWindow() {
            List<List<Integer>> windows = OpenStream.tumblingWindow(
                List.of(1, 2, 3, 4, 5), 2).toList();
            assertEquals(3, windows.size());
        }
    }

    // ==================== Stream Combination Tests ====================

    @Nested
    @DisplayName("Stream Combination")
    class CombinationTests {

        @Test
        @DisplayName("zip two streams")
        void zipStreams() {
            List<String> result = OpenStream.zip(
                Stream.of("a", "b", "c"),
                Stream.of(1, 2, 3),
                (s, i) -> s + i
            ).toList();
            assertEquals(List.of("a1", "b2", "c3"), result);
        }

        @Test
        @DisplayName("zip with different lengths")
        void zipDifferentLengths() {
            List<String> result = OpenStream.zip(
                Stream.of("a", "b"),
                Stream.of(1, 2, 3, 4),
                (s, i) -> s + i
            ).toList();
            assertEquals(List.of("a1", "b2"), result);
        }

        @Test
        @DisplayName("zip with null stream")
        void zipNullStream() {
            List<String> result = OpenStream.zip(
                null, Stream.of(1, 2), (s, i) -> s + "" + i
            ).toList();
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("zipWithIndex")
        void zipWithIndex() {
            List<OpenStream.IndexedValue<String>> result = OpenStream.zipWithIndex(
                Stream.of("a", "b", "c")).toList();
            assertEquals(3, result.size());
            assertEquals(0, result.get(0).index());
            assertEquals("a", result.get(0).value());
            assertEquals(2, result.get(2).index());
        }

        @Test
        @DisplayName("zipWithIndex with null")
        void zipWithIndexNull() {
            List<OpenStream.IndexedValue<String>> result =
                OpenStream.<String>zipWithIndex(null).toList();
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("merge streams")
        void mergeStreams() {
            List<Integer> result = OpenStream.merge(
                Stream.of(1, 2), Stream.of(3, 4), Stream.of(5)
            ).toList();
            assertEquals(List.of(1, 2, 3, 4, 5), result);
        }

        @Test
        @DisplayName("merge with null streams")
        void mergeWithNulls() {
            List<Integer> result = OpenStream.merge(
                Stream.of(1), null, Stream.of(2)
            ).toList();
            assertEquals(List.of(1, 2), result);
        }

        @Test
        @DisplayName("merge null array")
        void mergeNullArray() {
            List<Integer> result = OpenStream.merge((Stream<Integer>[]) null).toList();
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("interleave streams")
        void interleaveStreams() {
            List<Integer> result = OpenStream.interleave(
                Stream.of(1, 3, 5), Stream.of(2, 4, 6)
            ).toList();
            assertEquals(List.of(1, 2, 3, 4, 5, 6), result);
        }

        @Test
        @DisplayName("interleave with different lengths")
        void interleaveDifferentLengths() {
            List<Integer> result = OpenStream.interleave(
                Stream.of(1, 3, 5, 7), Stream.of(2, 4)
            ).toList();
            assertEquals(List.of(1, 2, 3, 4, 5, 7), result);
        }

        @Test
        @DisplayName("interleave with null streams")
        void interleaveNulls() {
            assertTrue(OpenStream.interleave(null, null).toList().isEmpty());
            assertEquals(List.of(1, 2),
                OpenStream.interleave(Stream.of(1, 2), null).toList());
            assertEquals(List.of(3, 4),
                OpenStream.interleave(null, Stream.of(3, 4)).toList());
        }
    }

    // ==================== Filtering Tests ====================

    @Nested
    @DisplayName("Filtering")
    class FilteringTests {

        @Test
        @DisplayName("filterNulls")
        void filterNulls() {
            List<String> result = OpenStream.filterNulls(
                Stream.of("a", null, "b", null, "c")).toList();
            assertEquals(List.of("a", "b", "c"), result);
        }

        @Test
        @DisplayName("filterNulls with null stream")
        void filterNullsWithNull() {
            List<String> result = OpenStream.<String>filterNulls(null).toList();
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("distinctBy")
        void distinctBy() {
            List<String> result = OpenStream.distinctBy(
                Stream.of("apple", "apricot", "banana", "blueberry"),
                s -> s.charAt(0)
            ).toList();
            assertEquals(List.of("apple", "banana"), result);
        }

        @Test
        @DisplayName("distinctBy with null")
        void distinctByNull() {
            assertTrue(OpenStream.distinctBy(null, s -> s).toList().isEmpty());
            assertTrue(OpenStream.distinctBy(Stream.of("a"), null).toList().isEmpty());
        }

        @Test
        @DisplayName("takeWhile")
        void takeWhile() {
            List<Integer> result = OpenStream.takeWhile(
                Stream.of(1, 2, 3, 4, 5), n -> n < 4).toList();
            assertEquals(List.of(1, 2, 3), result);
        }

        @Test
        @DisplayName("takeWhile with null")
        void takeWhileNull() {
            assertTrue(OpenStream.takeWhile(null, n -> true).toList().isEmpty());
        }

        @Test
        @DisplayName("dropWhile")
        void dropWhile() {
            List<Integer> result = OpenStream.dropWhile(
                Stream.of(1, 2, 3, 4, 5), n -> n < 3).toList();
            assertEquals(List.of(3, 4, 5), result);
        }

        @Test
        @DisplayName("dropWhile with null")
        void dropWhileNull() {
            assertTrue(OpenStream.dropWhile(null, n -> true).toList().isEmpty());
        }
    }

    // ==================== Reduction Tests ====================

    @Nested
    @DisplayName("Reduction")
    class ReductionTests {

        @Test
        @DisplayName("findFirst")
        void findFirst() {
            Optional<Integer> result = OpenStream.findFirst(
                Stream.of(1, 2, 3, 4), n -> n > 2);
            assertEquals(3, result.orElse(-1));
        }

        @Test
        @DisplayName("findFirst with null")
        void findFirstNull() {
            assertTrue(OpenStream.findFirst(null, n -> true).isEmpty());
            assertTrue(OpenStream.findFirst(Stream.of(1), null).isEmpty());
        }

        @Test
        @DisplayName("anyMatch")
        void anyMatch() {
            assertTrue(OpenStream.anyMatch(Stream.of(1, 2, 3), n -> n == 2));
            assertFalse(OpenStream.anyMatch(Stream.of(1, 2, 3), n -> n == 5));
        }

        @Test
        @DisplayName("anyMatch with null")
        void anyMatchNull() {
            assertFalse(OpenStream.anyMatch(null, n -> true));
            assertFalse(OpenStream.anyMatch(Stream.of(1), null));
        }

        @Test
        @DisplayName("allMatch")
        void allMatch() {
            assertTrue(OpenStream.allMatch(Stream.of(2, 4, 6), n -> n % 2 == 0));
            assertFalse(OpenStream.allMatch(Stream.of(1, 2, 3), n -> n % 2 == 0));
        }

        @Test
        @DisplayName("allMatch with null")
        void allMatchNull() {
            assertTrue(OpenStream.allMatch(null, n -> true));
            assertTrue(OpenStream.allMatch(Stream.of(1), null));
        }

        @Test
        @DisplayName("noneMatch")
        void noneMatch() {
            assertTrue(OpenStream.noneMatch(Stream.of(1, 3, 5), n -> n % 2 == 0));
            assertFalse(OpenStream.noneMatch(Stream.of(1, 2, 3), n -> n % 2 == 0));
        }

        @Test
        @DisplayName("noneMatch with null")
        void noneMatchNull() {
            assertTrue(OpenStream.noneMatch(null, n -> true));
            assertTrue(OpenStream.noneMatch(Stream.of(1), null));
        }
    }

    // ==================== Collectors Tests ====================

    @Nested
    @DisplayName("Collectors")
    class CollectorsTests {

        @Test
        @DisplayName("toUnmodifiableList")
        void toUnmodifiableList() {
            List<Integer> result = OpenStream.toUnmodifiableList(Stream.of(1, 2, 3));
            assertEquals(List.of(1, 2, 3), result);
            assertThrows(UnsupportedOperationException.class, () -> result.add(4));
        }

        @Test
        @DisplayName("toUnmodifiableList with null")
        void toUnmodifiableListNull() {
            assertEquals(List.of(), OpenStream.toUnmodifiableList(null));
        }

        @Test
        @DisplayName("toUnmodifiableSet")
        void toUnmodifiableSet() {
            Set<Integer> result = OpenStream.toUnmodifiableSet(Stream.of(1, 2, 2, 3));
            assertEquals(3, result.size());
            assertThrows(UnsupportedOperationException.class, () -> result.add(4));
        }

        @Test
        @DisplayName("toUnmodifiableSet with null")
        void toUnmodifiableSetNull() {
            assertEquals(Set.of(), OpenStream.toUnmodifiableSet(null));
        }

        @Test
        @DisplayName("toMap with key mapper")
        void toMapWithKeyMapper() {
            Map<Integer, String> result = OpenStream.toMap(
                Stream.of("a", "bb", "ccc"), String::length);
            assertEquals("a", result.get(1));
            assertEquals("bb", result.get(2));
            assertEquals("ccc", result.get(3));
        }

        @Test
        @DisplayName("toMap with key and value mappers")
        void toMapWithKeyValueMappers() {
            Map<String, Integer> result = OpenStream.toMap(
                Stream.of("a", "bb", "ccc"),
                s -> s,
                String::length
            );
            assertEquals(1, result.get("a"));
            assertEquals(3, result.get("ccc"));
        }

        @Test
        @DisplayName("toMap with null")
        void toMapNull() {
            assertEquals(Map.of(), OpenStream.toMap(null, s -> s));
            assertEquals(Map.of(), OpenStream.toMap(Stream.of("a"), null));
        }

        @Test
        @DisplayName("groupBy")
        void groupBy() {
            Map<Integer, List<String>> result = OpenStream.groupBy(
                Stream.of("a", "bb", "cc", "ddd"), String::length);
            assertEquals(List.of("a"), result.get(1));
            assertEquals(2, result.get(2).size());
            assertEquals(List.of("ddd"), result.get(3));
        }

        @Test
        @DisplayName("groupBy with null")
        void groupByNull() {
            assertEquals(Map.of(), OpenStream.groupBy(null, s -> s));
            assertEquals(Map.of(), OpenStream.groupBy(Stream.of("a"), null));
        }

        @Test
        @DisplayName("partitionBy")
        void partitionBy() {
            Map<Boolean, List<Integer>> result = OpenStream.partitionBy(
                Stream.of(1, 2, 3, 4, 5), n -> n % 2 == 0);
            assertEquals(List.of(2, 4), result.get(true));
            assertEquals(List.of(1, 3, 5), result.get(false));
        }

        @Test
        @DisplayName("partitionBy with null")
        void partitionByNull() {
            Map<Boolean, List<Integer>> result = OpenStream.partitionBy(null, n -> true);
            assertTrue(result.get(true).isEmpty());
            assertTrue(result.get(false).isEmpty());
        }

        @Test
        @DisplayName("joining")
        void joining() {
            String result = OpenStream.joining(Stream.of("a", "b", "c"), ", ");
            assertEquals("a, b, c", result);
        }

        @Test
        @DisplayName("joining with null stream")
        void joiningNull() {
            assertEquals("", OpenStream.joining(null, ", "));
        }

        @Test
        @DisplayName("joining with prefix and suffix")
        void joiningWithPrefixSuffix() {
            String result = OpenStream.joining(Stream.of(1, 2, 3), ", ", "[", "]");
            assertEquals("[1, 2, 3]", result);
        }

        @Test
        @DisplayName("joining with prefix and suffix null stream")
        void joiningPrefixSuffixNull() {
            assertEquals("[]", OpenStream.joining(null, ", ", "[", "]"));
        }
    }

    // ==================== Parallel Processing Tests ====================

    @Nested
    @DisplayName("Parallel Processing")
    class ParallelTests {

        @Test
        @DisplayName("parallelMap")
        void parallelMap() {
            List<Integer> result = OpenStream.parallelMap(
                List.of(1, 2, 3, 4, 5), n -> n * 2);
            assertEquals(5, result.size());
            assertTrue(result.containsAll(List.of(2, 4, 6, 8, 10)));
        }

        @Test
        @DisplayName("parallelMap with parallelism")
        void parallelMapWithParallelism() {
            List<Integer> result = OpenStream.parallelMap(
                List.of(1, 2, 3, 4, 5), 2, n -> n * 2);
            assertEquals(5, result.size());
        }

        @Test
        @DisplayName("parallelMap with null")
        void parallelMapNull() {
            assertEquals(List.of(), OpenStream.parallelMap(null, n -> n));
            assertEquals(List.of(), OpenStream.parallelMap(List.of(1), null));
        }

        @Test
        @DisplayName("parallelFilter")
        void parallelFilter() {
            List<Integer> result = OpenStream.parallelFilter(
                List.of(1, 2, 3, 4, 5), n -> n % 2 == 0);
            assertEquals(2, result.size());
            assertTrue(result.containsAll(List.of(2, 4)));
        }

        @Test
        @DisplayName("parallelFilter with null")
        void parallelFilterNull() {
            assertEquals(List.of(), OpenStream.parallelFilter(null, n -> true));
            assertEquals(List.of(), OpenStream.parallelFilter(List.of(1), null));
        }
    }

    // ==================== Statistics Tests ====================

    @Nested
    @DisplayName("Statistics")
    class StatisticsTests {

        @Test
        @DisplayName("count")
        void count() {
            assertEquals(5, OpenStream.count(Stream.of(1, 2, 3, 4, 5)));
            assertEquals(0, OpenStream.count(null));
        }

        @Test
        @DisplayName("sumInt")
        void sumInt() {
            assertEquals(15, OpenStream.sumInt(IntStream.of(1, 2, 3, 4, 5)));
            assertEquals(0, OpenStream.sumInt(null));
        }

        @Test
        @DisplayName("sumLong")
        void sumLong() {
            assertEquals(15L, OpenStream.sumLong(LongStream.of(1, 2, 3, 4, 5)));
            assertEquals(0L, OpenStream.sumLong(null));
        }

        @Test
        @DisplayName("sumDouble")
        void sumDouble() {
            assertEquals(6.0, OpenStream.sumDouble(DoubleStream.of(1.0, 2.0, 3.0)), 0.001);
            assertEquals(0.0, OpenStream.sumDouble(null), 0.001);
        }

        @Test
        @DisplayName("averageInt")
        void averageInt() {
            OptionalDouble avg = OpenStream.averageInt(IntStream.of(2, 4, 6));
            assertTrue(avg.isPresent());
            assertEquals(4.0, avg.getAsDouble(), 0.001);
            assertTrue(OpenStream.averageInt(null).isEmpty());
        }

        @Test
        @DisplayName("max")
        void max() {
            Optional<Integer> max = OpenStream.max(
                Stream.of(3, 1, 4, 1, 5), Comparator.naturalOrder());
            assertEquals(5, max.orElse(-1));
        }

        @Test
        @DisplayName("max with null")
        void maxNull() {
            assertTrue(OpenStream.<Integer>max(null, Comparator.naturalOrder()).isEmpty());
            assertTrue(OpenStream.max(Stream.of(1), null).isEmpty());
        }

        @Test
        @DisplayName("min")
        void min() {
            Optional<Integer> min = OpenStream.min(
                Stream.of(3, 1, 4, 1, 5), Comparator.naturalOrder());
            assertEquals(1, min.orElse(-1));
        }

        @Test
        @DisplayName("min with null")
        void minNull() {
            assertTrue(OpenStream.<Integer>min(null, Comparator.naturalOrder()).isEmpty());
            assertTrue(OpenStream.min(Stream.of(1), null).isEmpty());
        }
    }

    // ==================== Utility Tests ====================

    @Nested
    @DisplayName("Utility Methods")
    class UtilityTests {

        @Test
        @DisplayName("peek")
        void peek() {
            List<Integer> peeked = new ArrayList<>();
            List<Integer> result = OpenStream.peek(
                Stream.of(1, 2, 3), peeked::add).toList();
            assertEquals(List.of(1, 2, 3), result);
            assertEquals(List.of(1, 2, 3), peeked);
        }

        @Test
        @DisplayName("peek with null")
        void peekNull() {
            assertTrue(OpenStream.peek(null, s -> {}).toList().isEmpty());
            assertEquals(List.of(1), OpenStream.peek(Stream.of(1), null).toList());
        }

        @Test
        @DisplayName("limit")
        void limit() {
            List<Integer> result = OpenStream.limit(Stream.of(1, 2, 3, 4, 5), 3).toList();
            assertEquals(List.of(1, 2, 3), result);
        }

        @Test
        @DisplayName("limit with null")
        void limitNull() {
            assertTrue(OpenStream.limit(null, 5).toList().isEmpty());
        }

        @Test
        @DisplayName("skip")
        void skip() {
            List<Integer> result = OpenStream.skip(Stream.of(1, 2, 3, 4, 5), 2).toList();
            assertEquals(List.of(3, 4, 5), result);
        }

        @Test
        @DisplayName("skip with null")
        void skipNull() {
            assertTrue(OpenStream.skip(null, 5).toList().isEmpty());
        }

        @Test
        @DisplayName("sorted")
        void sorted() {
            List<Integer> result = OpenStream.sorted(
                Stream.of(3, 1, 4, 1, 5), Comparator.naturalOrder()).toList();
            assertEquals(List.of(1, 1, 3, 4, 5), result);
        }

        @Test
        @DisplayName("sorted with null comparator uses natural order")
        void sortedNullComparator() {
            List<Integer> result = OpenStream.sorted(Stream.of(3, 1, 2), null).toList();
            assertEquals(List.of(1, 2, 3), result);
        }

        @Test
        @DisplayName("sorted with null stream")
        void sortedNullStream() {
            assertTrue(OpenStream.<Integer>sorted(null, Comparator.naturalOrder()).toList().isEmpty());
        }

        @Test
        @DisplayName("flatten")
        void flatten() {
            List<Integer> result = OpenStream.flatten(
                Stream.of(Stream.of(1, 2), Stream.of(3, 4))).toList();
            assertEquals(List.of(1, 2, 3, 4), result);
        }

        @Test
        @DisplayName("flatten with null")
        void flattenNull() {
            assertTrue(OpenStream.flatten(null).toList().isEmpty());
        }

        @Test
        @DisplayName("flattenCollections")
        void flattenCollections() {
            List<Integer> result = OpenStream.flattenCollections(
                Stream.of(List.of(1, 2), List.of(3, 4))).toList();
            assertEquals(List.of(1, 2, 3, 4), result);
        }

        @Test
        @DisplayName("flattenCollections with null")
        void flattenCollectionsNull() {
            assertTrue(OpenStream.flattenCollections(null).toList().isEmpty());
        }
    }

    // ==================== IndexedValue Record Tests ====================

    @Nested
    @DisplayName("IndexedValue Record")
    class IndexedValueTests {

        @Test
        @DisplayName("IndexedValue properties")
        void indexedValueProperties() {
            OpenStream.IndexedValue<String> iv = new OpenStream.IndexedValue<>(5L, "test");
            assertEquals(5L, iv.index());
            assertEquals("test", iv.value());
        }

        @Test
        @DisplayName("IndexedValue equals and hashCode")
        void indexedValueEqualsHashCode() {
            OpenStream.IndexedValue<String> iv1 = new OpenStream.IndexedValue<>(1L, "a");
            OpenStream.IndexedValue<String> iv2 = new OpenStream.IndexedValue<>(1L, "a");
            OpenStream.IndexedValue<String> iv3 = new OpenStream.IndexedValue<>(2L, "a");

            assertEquals(iv1, iv2);
            assertEquals(iv1.hashCode(), iv2.hashCode());
            assertNotEquals(iv1, iv3);
        }
    }
}
