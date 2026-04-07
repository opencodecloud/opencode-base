package cloud.opencode.base.core.collect;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import cloud.opencode.base.core.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpenCollectionsTest {

    @Nested
    class ListBuilderTest {

        @Test
        void buildEmpty() {
            List<String> list = OpenCollections.<String>listBuilder().build();
            assertThat(list).isEmpty();
        }

        @Test
        void addAndBuild() {
            List<String> list = OpenCollections.<String>listBuilder()
                    .add("a")
                    .add("b")
                    .add("c")
                    .build();
            assertThat(list).containsExactly("a", "b", "c");
        }

        @Test
        void addAllAndBuild() {
            List<String> list = OpenCollections.<String>listBuilder()
                    .add("x")
                    .addAll(List.of("a", "b"))
                    .build();
            assertThat(list).containsExactly("x", "a", "b");
        }

        @Test
        void buildReturnsUnmodifiable() {
            List<String> list = OpenCollections.<String>listBuilder().add("a").build();
            assertThatThrownBy(() -> list.add("b"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void withExpectedSize() {
            List<Integer> list = OpenCollections.<Integer>listBuilder(10)
                    .add(1).add(2).build();
            assertThat(list).containsExactly(1, 2);
        }

        @Test
        void negativeExpectedSizeThrows() {
            assertThatThrownBy(() -> OpenCollections.<String>listBuilder(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void nullElementThrows() {
            assertThatThrownBy(() -> OpenCollections.<String>listBuilder().add(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void nullIterableThrows() {
            assertThatThrownBy(() -> OpenCollections.<String>listBuilder().addAll(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class MapBuilderTest {

        @Test
        void buildEmpty() {
            Map<String, Integer> map = OpenCollections.<String, Integer>mapBuilder().build();
            assertThat(map).isEmpty();
        }

        @Test
        void putAndBuild() {
            Map<String, Integer> map = OpenCollections.<String, Integer>mapBuilder()
                    .put("a", 1)
                    .put("b", 2)
                    .build();
            assertThat(map).containsEntry("a", 1).containsEntry("b", 2);
        }

        @Test
        void putAllAndBuild() {
            Map<String, Integer> map = OpenCollections.<String, Integer>mapBuilder()
                    .put("x", 0)
                    .putAll(Map.of("a", 1, "b", 2))
                    .build();
            assertThat(map).hasSize(3).containsEntry("x", 0);
        }

        @Test
        void buildReturnsUnmodifiable() {
            Map<String, Integer> map = OpenCollections.<String, Integer>mapBuilder()
                    .put("a", 1).build();
            assertThatThrownBy(() -> map.put("b", 2))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void nullKeyThrows() {
            assertThatThrownBy(() -> OpenCollections.<String, Integer>mapBuilder().put(null, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void nullValueThrows() {
            assertThatThrownBy(() -> OpenCollections.<String, Integer>mapBuilder().put("a", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class AppendTest {

        @Test
        void appendElement() {
            List<String> original = List.of("a", "b");
            List<String> result = OpenCollections.append(original, "c");
            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        void originalUnchanged() {
            List<String> original = List.of("a");
            OpenCollections.append(original, "b");
            assertThat(original).containsExactly("a");
        }

        @Test
        void resultIsUnmodifiable() {
            List<String> result = OpenCollections.append(List.of("a"), "b");
            assertThatThrownBy(() -> result.add("c"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void nullListThrows() {
            assertThatThrownBy(() -> OpenCollections.append(null, "a"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void nullElementThrows() {
            assertThatThrownBy(() -> OpenCollections.append(List.of("a"), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class PrependTest {

        @Test
        void prependElement() {
            List<String> result = OpenCollections.prepend("x", List.of("a", "b"));
            assertThat(result).containsExactly("x", "a", "b");
        }

        @Test
        void originalUnchanged() {
            List<String> original = List.of("a");
            OpenCollections.prepend("x", original);
            assertThat(original).containsExactly("a");
        }

        @Test
        void nullElementThrows() {
            assertThatThrownBy(() -> OpenCollections.prepend(null, List.of("a")))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class ConcatTest {

        @Test
        void concatTwoLists() {
            List<String> result = OpenCollections.concat(
                    List.of("a", "b"), List.of("c", "d"));
            assertThat(result).containsExactly("a", "b", "c", "d");
        }

        @Test
        void concatWithEmpty() {
            List<String> result = OpenCollections.concat(List.of("a"), List.of());
            assertThat(result).containsExactly("a");
        }

        @Test
        void concatBothEmpty() {
            List<String> result = OpenCollections.concat(List.of(), List.of());
            assertThat(result).isEmpty();
        }

        @Test
        void resultIsUnmodifiable() {
            List<String> result = OpenCollections.concat(List.of("a"), List.of("b"));
            assertThatThrownBy(() -> result.add("c"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    class WithoutTest {

        @Test
        void removesFirstOccurrence() {
            List<String> result = OpenCollections.without(
                    List.of("a", "b", "a", "c"), "a");
            assertThat(result).containsExactly("b", "a", "c");
        }

        @Test
        void elementNotPresent() {
            List<String> result = OpenCollections.without(List.of("a", "b"), "z");
            assertThat(result).containsExactly("a", "b");
        }

        @Test
        void originalUnchanged() {
            List<String> original = List.of("a", "b");
            OpenCollections.without(original, "a");
            assertThat(original).containsExactly("a", "b");
        }

        @Test
        void resultIsUnmodifiable() {
            List<String> result = OpenCollections.without(List.of("a", "b"), "a");
            assertThatThrownBy(() -> result.add("c"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    class WithReplacedTest {

        @Test
        void replaceElement() {
            List<String> result = OpenCollections.withReplaced(
                    List.of("a", "b", "c"), 1, "x");
            assertThat(result).containsExactly("a", "x", "c");
        }

        @Test
        void indexOutOfBoundsNegative() {
            assertThatThrownBy(() -> OpenCollections.withReplaced(List.of("a"), -1, "x"))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        void indexOutOfBoundsTooLarge() {
            assertThatThrownBy(() -> OpenCollections.withReplaced(List.of("a"), 1, "x"))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        void resultIsUnmodifiable() {
            List<String> result = OpenCollections.withReplaced(List.of("a", "b"), 0, "x");
            assertThatThrownBy(() -> result.add("c"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void nullNewElementThrows() {
            assertThatThrownBy(() -> OpenCollections.withReplaced(List.of("a"), 0, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class UnionTest {

        @Test
        void unionOfTwoSets() {
            Set<String> result = OpenCollections.union(
                    Set.of("a", "b"), Set.of("b", "c"));
            assertThat(result).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        void unionWithEmpty() {
            Set<String> result = OpenCollections.union(Set.of("a"), Set.of());
            assertThat(result).containsExactly("a");
        }

        @Test
        void resultIsUnmodifiable() {
            Set<String> result = OpenCollections.union(Set.of("a"), Set.of("b"));
            assertThatThrownBy(() -> result.add("c"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    class IntersectionTest {

        @Test
        void intersectionOfTwoSets() {
            Set<String> result = OpenCollections.intersection(
                    Set.of("a", "b", "c"), Set.of("b", "c", "d"));
            assertThat(result).containsExactlyInAnyOrder("b", "c");
        }

        @Test
        void intersectionWithEmpty() {
            Set<String> result = OpenCollections.intersection(Set.of("a"), Set.of());
            assertThat(result).isEmpty();
        }

        @Test
        void noOverlap() {
            Set<String> result = OpenCollections.intersection(
                    Set.of("a", "b"), Set.of("c", "d"));
            assertThat(result).isEmpty();
        }

        @Test
        void resultIsUnmodifiable() {
            Set<String> result = OpenCollections.intersection(
                    Set.of("a", "b"), Set.of("b", "c"));
            assertThatThrownBy(() -> result.add("z"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    class DifferenceTest {

        @Test
        void differenceOfTwoSets() {
            Set<String> result = OpenCollections.difference(
                    Set.of("a", "b", "c"), Set.of("b", "d"));
            assertThat(result).containsExactlyInAnyOrder("a", "c");
        }

        @Test
        void differenceWithEmpty() {
            Set<String> result = OpenCollections.difference(Set.of("a", "b"), Set.of());
            assertThat(result).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        void differenceFromEmpty() {
            Set<String> result = OpenCollections.difference(Set.of(), Set.of("a"));
            assertThat(result).isEmpty();
        }

        @Test
        void resultIsUnmodifiable() {
            Set<String> result = OpenCollections.difference(
                    Set.of("a", "b"), Set.of("b"));
            assertThatThrownBy(() -> result.add("z"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    class CollectorTest {

        @Test
        void toUnmodifiableListCollector() {
            List<String> list = Stream.of("a", "b", "c")
                    .collect(OpenCollections.toUnmodifiableList());
            assertThat(list).containsExactly("a", "b", "c");
            assertThatThrownBy(() -> list.add("d"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void toUnmodifiableSetCollector() {
            Set<String> set = Stream.of("a", "b", "a")
                    .collect(OpenCollections.toUnmodifiableSet());
            assertThat(set).containsExactlyInAnyOrder("a", "b");
            assertThatThrownBy(() -> set.add("c"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void toUnmodifiableListEmpty() {
            List<String> list = Stream.<String>empty()
                    .collect(OpenCollections.toUnmodifiableList());
            assertThat(list).isEmpty();
        }

        @Test
        void toUnmodifiableSetEmpty() {
            Set<String> set = Stream.<String>empty()
                    .collect(OpenCollections.toUnmodifiableSet());
            assertThat(set).isEmpty();
        }
    }

    // ==================== Null Arguments ====================

    @Nested
    class NullArguments {

        @Test
        void prepend_nullList_throws() {
            assertThatThrownBy(() -> OpenCollections.prepend("a", null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void concat_nullFirstList_throws() {
            assertThatThrownBy(() -> OpenCollections.concat(null, List.of("a")))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void concat_nullSecondList_throws() {
            assertThatThrownBy(() -> OpenCollections.concat(List.of("a"), null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void without_nullList_throws() {
            assertThatThrownBy(() -> OpenCollections.without(null, "a"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void without_nullElement_throws() {
            assertThatThrownBy(() -> OpenCollections.without(List.of("a"), null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void withReplaced_nullList_throws() {
            assertThatThrownBy(() -> OpenCollections.withReplaced(null, 0, "a"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void union_nullFirst_throws() {
            assertThatThrownBy(() -> OpenCollections.union(null, Set.of("a")))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void union_nullSecond_throws() {
            assertThatThrownBy(() -> OpenCollections.union(Set.of("a"), null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void intersection_nullFirst_throws() {
            assertThatThrownBy(() -> OpenCollections.intersection(null, Set.of("a")))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void intersection_nullSecond_throws() {
            assertThatThrownBy(() -> OpenCollections.intersection(Set.of("a"), null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void difference_nullFirst_throws() {
            assertThatThrownBy(() -> OpenCollections.difference(null, Set.of("a")))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void difference_nullSecond_throws() {
            assertThatThrownBy(() -> OpenCollections.difference(Set.of("a"), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== Partition ====================

    @Nested
    @DisplayName("partition")
    class PartitionTest {

        @Test
        void partitionsByPredicate() {
            Map<Boolean, List<Integer>> result = OpenCollections.partition(
                    List.of(1, 2, 3, 4, 5), n -> n % 2 == 0);
            assertThat(result.get(true)).containsExactly(2, 4);
            assertThat(result.get(false)).containsExactly(1, 3, 5);
        }

        @Test
        void emptyList() {
            Map<Boolean, List<String>> result = OpenCollections.partition(List.of(), s -> true);
            assertThat(result.get(true)).isEmpty();
            assertThat(result.get(false)).isEmpty();
        }

        @Test
        void singleElementMatchesPredicate() {
            Map<Boolean, List<String>> result = OpenCollections.partition(List.of("a"), s -> true);
            assertThat(result.get(true)).containsExactly("a");
            assertThat(result.get(false)).isEmpty();
        }

        @Test
        void singleElementDoesNotMatchPredicate() {
            Map<Boolean, List<String>> result = OpenCollections.partition(List.of("a"), s -> false);
            assertThat(result.get(true)).isEmpty();
            assertThat(result.get(false)).containsExactly("a");
        }

        @Test
        void resultMapIsUnmodifiable() {
            Map<Boolean, List<Integer>> result = OpenCollections.partition(List.of(1), n -> true);
            assertThatThrownBy(() -> result.put(true, List.of()))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void resultListsAreUnmodifiable() {
            Map<Boolean, List<Integer>> result = OpenCollections.partition(List.of(1, 2), n -> true);
            assertThatThrownBy(() -> result.get(true).add(3))
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> result.get(false).add(3))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void nullListThrows() {
            assertThatThrownBy(() -> OpenCollections.partition(null, s -> true))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void nullPredicateThrows() {
            assertThatThrownBy(() -> OpenCollections.partition(List.of("a"), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== GroupBy ====================

    @Nested
    @DisplayName("groupBy")
    class GroupByTest {

        @Test
        void groupsByClassifier() {
            Map<Integer, List<String>> result = OpenCollections.groupBy(
                    List.of("a", "bb", "cc", "ddd"), String::length);
            assertThat(result).hasSize(3);
            assertThat(result.get(1)).containsExactly("a");
            assertThat(result.get(2)).containsExactly("bb", "cc");
            assertThat(result.get(3)).containsExactly("ddd");
        }

        @Test
        void emptyList() {
            Map<Integer, List<String>> result = OpenCollections.groupBy(List.of(), String::length);
            assertThat(result).isEmpty();
        }

        @Test
        void singleElement() {
            Map<Integer, List<String>> result = OpenCollections.groupBy(List.of("hi"), String::length);
            assertThat(result).hasSize(1);
            assertThat(result.get(2)).containsExactly("hi");
        }

        @Test
        void resultMapIsUnmodifiable() {
            Map<Integer, List<String>> result = OpenCollections.groupBy(List.of("a"), String::length);
            assertThatThrownBy(() -> result.put(99, List.of()))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void resultListsAreUnmodifiable() {
            Map<Integer, List<String>> result = OpenCollections.groupBy(List.of("a"), String::length);
            assertThatThrownBy(() -> result.get(1).add("b"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void nullListThrows() {
            assertThatThrownBy(() -> OpenCollections.groupBy(null, String::length))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void nullClassifierThrows() {
            assertThatThrownBy(() -> OpenCollections.groupBy(List.of("a"), null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void classifierReturnsNullKeyThrows() {
            assertThatThrownBy(() -> OpenCollections.groupBy(List.of("a"), s -> null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void preservesInsertionOrder() {
            Map<String, List<Integer>> result = OpenCollections.groupBy(
                    List.of(1, 2, 3, 4, 5, 6), n -> n % 2 == 0 ? "even" : "odd");
            assertThat(result.keySet()).containsExactly("odd", "even");
        }
    }

    // ==================== Chunk ====================

    @Nested
    @DisplayName("chunk")
    class ChunkTest {

        @Test
        void chunksEvenly() {
            List<List<Integer>> result = OpenCollections.chunk(List.of(1, 2, 3, 4, 5, 6), 2);
            assertThat(result).hasSize(3);
            assertThat(result.get(0)).containsExactly(1, 2);
            assertThat(result.get(1)).containsExactly(3, 4);
            assertThat(result.get(2)).containsExactly(5, 6);
        }

        @Test
        void lastChunkSmaller() {
            List<List<Integer>> result = OpenCollections.chunk(List.of(1, 2, 3, 4, 5), 2);
            assertThat(result).hasSize(3);
            assertThat(result.get(2)).containsExactly(5);
        }

        @Test
        void chunkSizeLargerThanList() {
            List<List<Integer>> result = OpenCollections.chunk(List.of(1, 2), 10);
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).containsExactly(1, 2);
        }

        @Test
        void chunkSizeEqualsListSize() {
            List<List<Integer>> result = OpenCollections.chunk(List.of(1, 2, 3), 3);
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).containsExactly(1, 2, 3);
        }

        @Test
        void emptyList() {
            List<List<String>> result = OpenCollections.chunk(List.of(), 5);
            assertThat(result).isEmpty();
        }

        @Test
        void singleElement() {
            List<List<String>> result = OpenCollections.chunk(List.of("a"), 1);
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).containsExactly("a");
        }

        @Test
        void resultIsUnmodifiable() {
            List<List<Integer>> result = OpenCollections.chunk(List.of(1, 2, 3), 2);
            assertThatThrownBy(() -> result.add(List.of()))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void innerChunksAreUnmodifiable() {
            List<List<Integer>> result = OpenCollections.chunk(List.of(1, 2, 3), 2);
            assertThatThrownBy(() -> result.get(0).add(99))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void nullListThrows() {
            assertThatThrownBy(() -> OpenCollections.chunk(null, 2))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void zeroSizeThrows() {
            assertThatThrownBy(() -> OpenCollections.chunk(List.of(1), 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void negativeSizeThrows() {
            assertThatThrownBy(() -> OpenCollections.chunk(List.of(1), -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== Sliding ====================

    @Nested
    @DisplayName("sliding")
    class SlidingTest {

        @Test
        void slidingWindows() {
            List<List<Integer>> result = OpenCollections.sliding(List.of(1, 2, 3, 4, 5), 3);
            assertThat(result).hasSize(3);
            assertThat(result.get(0)).containsExactly(1, 2, 3);
            assertThat(result.get(1)).containsExactly(2, 3, 4);
            assertThat(result.get(2)).containsExactly(3, 4, 5);
        }

        @Test
        void windowSizeEqualsListSize() {
            List<List<Integer>> result = OpenCollections.sliding(List.of(1, 2, 3), 3);
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).containsExactly(1, 2, 3);
        }

        @Test
        void windowSizeLargerThanList() {
            List<List<Integer>> result = OpenCollections.sliding(List.of(1, 2), 5);
            assertThat(result).isEmpty();
        }

        @Test
        void windowSizeOne() {
            List<List<Integer>> result = OpenCollections.sliding(List.of(1, 2, 3), 1);
            assertThat(result).hasSize(3);
            assertThat(result.get(0)).containsExactly(1);
            assertThat(result.get(1)).containsExactly(2);
            assertThat(result.get(2)).containsExactly(3);
        }

        @Test
        void emptyList() {
            List<List<String>> result = OpenCollections.sliding(List.of(), 2);
            assertThat(result).isEmpty();
        }

        @Test
        void singleElement() {
            List<List<String>> result = OpenCollections.sliding(List.of("a"), 1);
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).containsExactly("a");
        }

        @Test
        void resultIsUnmodifiable() {
            List<List<Integer>> result = OpenCollections.sliding(List.of(1, 2, 3), 2);
            assertThatThrownBy(() -> result.add(List.of()))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void innerWindowsAreUnmodifiable() {
            List<List<Integer>> result = OpenCollections.sliding(List.of(1, 2, 3), 2);
            assertThatThrownBy(() -> result.get(0).add(99))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void nullListThrows() {
            assertThatThrownBy(() -> OpenCollections.sliding(null, 2))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void zeroSizeThrows() {
            assertThatThrownBy(() -> OpenCollections.sliding(List.of(1), 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void negativeSizeThrows() {
            assertThatThrownBy(() -> OpenCollections.sliding(List.of(1), -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== Zip ====================

    @Nested
    @DisplayName("zip")
    class ZipTest {

        @Test
        void zipEqualLengthLists() {
            List<Pair<String, Integer>> result = OpenCollections.zip(
                    List.of("a", "b", "c"), List.of(1, 2, 3));
            assertThat(result).hasSize(3);
            assertThat(result.get(0)).isEqualTo(Pair.of("a", 1));
            assertThat(result.get(1)).isEqualTo(Pair.of("b", 2));
            assertThat(result.get(2)).isEqualTo(Pair.of("c", 3));
        }

        @Test
        void zipTruncatesToShorter() {
            List<Pair<String, Integer>> result = OpenCollections.zip(
                    List.of("a", "b", "c"), List.of(1, 2));
            assertThat(result).hasSize(2);
            assertThat(result.get(0)).isEqualTo(Pair.of("a", 1));
            assertThat(result.get(1)).isEqualTo(Pair.of("b", 2));
        }

        @Test
        void zipWithEmptyFirst() {
            List<Pair<String, Integer>> result = OpenCollections.zip(List.of(), List.of(1, 2));
            assertThat(result).isEmpty();
        }

        @Test
        void zipWithEmptySecond() {
            List<Pair<String, Integer>> result = OpenCollections.zip(List.of("a"), List.of());
            assertThat(result).isEmpty();
        }

        @Test
        void zipBothEmpty() {
            List<Pair<String, Integer>> result = OpenCollections.zip(
                    List.<String>of(), List.<Integer>of());
            assertThat(result).isEmpty();
        }

        @Test
        void resultIsUnmodifiable() {
            List<Pair<String, Integer>> result = OpenCollections.zip(List.of("a"), List.of(1));
            assertThatThrownBy(() -> result.add(Pair.of("b", 2)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void nullFirstListThrows() {
            assertThatThrownBy(() -> OpenCollections.zip(null, List.of(1)))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void nullSecondListThrows() {
            assertThatThrownBy(() -> OpenCollections.zip(List.of("a"), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== ZipWith ====================

    @Nested
    @DisplayName("zipWith")
    class ZipWithTest {

        @Test
        void zipWithCombiner() {
            List<String> result = OpenCollections.zipWith(
                    List.of("a", "b"), List.of(1, 2), (s, n) -> s + n);
            assertThat(result).containsExactly("a1", "b2");
        }

        @Test
        void zipWithTruncatesToShorter() {
            List<String> result = OpenCollections.zipWith(
                    List.of("a", "b", "c"), List.of(1), (s, n) -> s + n);
            assertThat(result).containsExactly("a1");
        }

        @Test
        void zipWithEmpty() {
            List<String> result = OpenCollections.zipWith(
                    List.<String>of(), List.<Integer>of(), (s, n) -> s + n);
            assertThat(result).isEmpty();
        }

        @Test
        void resultIsUnmodifiable() {
            List<String> result = OpenCollections.zipWith(
                    List.of("a"), List.of(1), (s, n) -> s + n);
            assertThatThrownBy(() -> result.add("x"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void nullFirstListThrows() {
            assertThatThrownBy(() -> OpenCollections.zipWith(null, List.of(1), (a, b) -> ""))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void nullSecondListThrows() {
            assertThatThrownBy(() -> OpenCollections.zipWith(List.of("a"), null, (a, b) -> ""))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void nullCombinerThrows() {
            assertThatThrownBy(() -> OpenCollections.zipWith(List.of("a"), List.of(1), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== DistinctBy ====================

    @Nested
    @DisplayName("distinctBy")
    class DistinctByTest {

        @Test
        void removeDuplicatesByKey() {
            List<String> result = OpenCollections.distinctBy(
                    List.of("apple", "avocado", "banana", "blueberry"), s -> s.charAt(0));
            assertThat(result).containsExactly("apple", "banana");
        }

        @Test
        void allUnique() {
            List<String> result = OpenCollections.distinctBy(
                    List.of("a", "b", "c"), s -> s);
            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        void allSameKey() {
            List<String> result = OpenCollections.distinctBy(
                    List.of("a", "b", "c"), s -> 1);
            assertThat(result).containsExactly("a");
        }

        @Test
        void emptyList() {
            List<String> result = OpenCollections.distinctBy(List.of(), s -> s);
            assertThat(result).isEmpty();
        }

        @Test
        void singleElement() {
            List<String> result = OpenCollections.distinctBy(List.of("x"), s -> s);
            assertThat(result).containsExactly("x");
        }

        @Test
        void resultIsUnmodifiable() {
            List<String> result = OpenCollections.distinctBy(List.of("a", "b"), s -> s);
            assertThatThrownBy(() -> result.add("c"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void nullListThrows() {
            assertThatThrownBy(() -> OpenCollections.distinctBy(null, s -> s))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void nullKeyExtractorThrows() {
            assertThatThrownBy(() -> OpenCollections.distinctBy(List.of("a"), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== Frequencies ====================

    @Nested
    @DisplayName("frequencies")
    class FrequenciesTest {

        @Test
        void countsFrequencies() {
            Map<String, Long> result = OpenCollections.frequencies(
                    List.of("a", "b", "a", "c", "b", "a"));
            assertThat(result).containsEntry("a", 3L)
                    .containsEntry("b", 2L)
                    .containsEntry("c", 1L);
        }

        @Test
        void emptyCollection() {
            Map<String, Long> result = OpenCollections.frequencies(List.of());
            assertThat(result).isEmpty();
        }

        @Test
        void singleElement() {
            Map<String, Long> result = OpenCollections.frequencies(List.of("x"));
            assertThat(result).containsExactly(Map.entry("x", 1L));
        }

        @Test
        void allSameElement() {
            Map<String, Long> result = OpenCollections.frequencies(List.of("a", "a", "a"));
            assertThat(result).containsExactly(Map.entry("a", 3L));
        }

        @Test
        void resultIsUnmodifiable() {
            Map<String, Long> result = OpenCollections.frequencies(List.of("a"));
            assertThatThrownBy(() -> result.put("b", 1L))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void preservesInsertionOrder() {
            Map<String, Long> result = OpenCollections.frequencies(
                    List.of("c", "a", "b", "a"));
            assertThat(result.keySet()).containsExactly("c", "a", "b");
        }

        @Test
        void nullCollectionThrows() {
            assertThatThrownBy(() -> OpenCollections.frequencies(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== Flatten ====================

    @Nested
    @DisplayName("flatten")
    class FlattenTest {

        @Test
        void flattensNestedLists() {
            List<Integer> result = OpenCollections.flatten(
                    List.of(List.of(1, 2), List.of(3), List.of(4, 5, 6)));
            assertThat(result).containsExactly(1, 2, 3, 4, 5, 6);
        }

        @Test
        void emptyOuterList() {
            List<Integer> result = OpenCollections.flatten(List.of());
            assertThat(result).isEmpty();
        }

        @Test
        void innerListsEmpty() {
            List<Integer> result = OpenCollections.flatten(
                    List.of(List.of(), List.of(), List.of()));
            assertThat(result).isEmpty();
        }

        @Test
        void singleInnerList() {
            List<Integer> result = OpenCollections.flatten(List.of(List.of(1, 2, 3)));
            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        void resultIsUnmodifiable() {
            List<Integer> result = OpenCollections.flatten(List.of(List.of(1)));
            assertThatThrownBy(() -> result.add(2))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void nullOuterListThrows() {
            assertThatThrownBy(() -> OpenCollections.flatten(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void nullInnerListThrows() {
            java.util.ArrayList<List<Integer>> lists = new java.util.ArrayList<>();
            lists.add(List.of(1, 2));
            lists.add(null);
            lists.add(List.of(3));
            assertThatThrownBy(() -> OpenCollections.flatten(lists))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
