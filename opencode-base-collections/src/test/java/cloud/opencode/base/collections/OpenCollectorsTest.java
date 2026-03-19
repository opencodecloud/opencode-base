package cloud.opencode.base.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenCollectors 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("OpenCollectors 测试")
class OpenCollectorsTest {

    @Nested
    @DisplayName("流式入口测试")
    class StreamEntryTests {

        @Test
        @DisplayName("from - 从 Iterable 创建")
        void testFromIterable() {
            List<String> list = List.of("a", "b", "c");

            List<String> result = OpenCollectors.from(list).toList();

            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("from - null Iterable")
        void testFromNull() {
            List<String> result = OpenCollectors.from((Iterable<String>) null).toList();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("of - 从数组创建")
        void testOf() {
            List<String> result = OpenCollectors.of("a", "b", "c").toList();

            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("of - 空数组")
        void testOfEmpty() {
            List<String> result = OpenCollectors.<String>of().toList();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("fromStream - 从 Stream 创建")
        void testFromStream() {
            List<String> result = OpenCollectors.fromStream(Stream.of("a", "b", "c")).toList();

            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("fromStream - null Stream")
        void testFromStreamNull() {
            List<String> result = OpenCollectors.fromStream((Stream<String>) null).toList();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("CollectorFlow 操作测试")
    class CollectorFlowTests {

        @Test
        @DisplayName("filter - 过滤")
        void testFilter() {
            List<Integer> result = OpenCollectors.of(1, 2, 3, 4, 5)
                    .filter(n -> n % 2 == 0)
                    .toList();

            assertThat(result).containsExactly(2, 4);
        }

        @Test
        @DisplayName("map - 映射")
        void testMap() {
            List<Integer> result = OpenCollectors.of("a", "bb", "ccc")
                    .map(String::length)
                    .toList();

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("flatMap - 扁平映射")
        void testFlatMap() {
            List<String> result = OpenCollectors.of("ab", "cd")
                    .flatMap(s -> Stream.of(s.split("")))
                    .toList();

            assertThat(result).containsExactly("a", "b", "c", "d");
        }

        @Test
        @DisplayName("distinct - 去重")
        void testDistinct() {
            List<Integer> result = OpenCollectors.of(1, 2, 2, 3, 3, 3)
                    .distinct()
                    .toList();

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("sorted - 排序")
        void testSorted() {
            List<Integer> result = OpenCollectors.of(3, 1, 2)
                    .sorted()
                    .toList();

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("sorted - 带比较器排序")
        void testSortedWithComparator() {
            List<Integer> result = OpenCollectors.of(1, 2, 3)
                    .sorted(Comparator.reverseOrder())
                    .toList();

            assertThat(result).containsExactly(3, 2, 1);
        }

        @Test
        @DisplayName("limit - 限制数量")
        void testLimit() {
            List<Integer> result = OpenCollectors.of(1, 2, 3, 4, 5)
                    .limit(3)
                    .toList();

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("skip - 跳过")
        void testSkip() {
            List<Integer> result = OpenCollectors.of(1, 2, 3, 4, 5)
                    .skip(2)
                    .toList();

            assertThat(result).containsExactly(3, 4, 5);
        }

        @Test
        @DisplayName("peek - 窥视")
        void testPeek() {
            List<Integer> peeked = new ArrayList<>();
            List<Integer> result = OpenCollectors.of(1, 2, 3)
                    .peek(peeked::add)
                    .toList();

            assertThat(result).containsExactly(1, 2, 3);
            assertThat(peeked).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("终端操作测试")
    class TerminalOperationTests {

        @Test
        @DisplayName("toArrayList - 收集到 ArrayList")
        void testToArrayList() {
            ArrayList<String> result = OpenCollectors.of("a", "b", "c").toArrayList();

            assertThat(result).isInstanceOf(ArrayList.class);
            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("toSet - 收集到 Set")
        void testToSet() {
            Set<String> result = OpenCollectors.of("a", "b", "a").toSet();

            assertThat(result).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("toImmutableList - 收集到 ImmutableList")
        void testToImmutableList() {
            ImmutableList<String> result = OpenCollectors.of("a", "b", "c").toImmutableList();

            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("toImmutableSet - 收集到 ImmutableSet")
        void testToImmutableSet() {
            ImmutableSet<String> result = OpenCollectors.of("a", "b", "a").toImmutableSet();

            assertThat(result).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("groupBy - 分组")
        void testGroupBy() {
            Map<Integer, List<String>> result = OpenCollectors.of("a", "bb", "ccc", "dd")
                    .groupBy(String::length);

            assertThat(result.get(1)).containsExactly("a");
            assertThat(result.get(2)).containsExactly("bb", "dd");
            assertThat(result.get(3)).containsExactly("ccc");
        }

        @Test
        @DisplayName("partition - 分区")
        void testPartition() {
            List<List<Integer>> result = OpenCollectors.of(1, 2, 3, 4, 5)
                    .partition(2);

            assertThat(result).hasSize(3);
            assertThat(result.get(0)).containsExactly(1, 2);
            assertThat(result.get(1)).containsExactly(3, 4);
            assertThat(result.get(2)).containsExactly(5);
        }

        @Test
        @DisplayName("count - 计数")
        void testCount() {
            long count = OpenCollectors.of(1, 2, 3, 4, 5).count();

            assertThat(count).isEqualTo(5);
        }

        @Test
        @DisplayName("findFirst - 查找第一个")
        void testFindFirst() {
            Optional<Integer> result = OpenCollectors.of(1, 2, 3).findFirst();

            assertThat(result).hasValue(1);
        }

        @Test
        @DisplayName("findFirst - 空")
        void testFindFirstEmpty() {
            Optional<Integer> result = OpenCollectors.<Integer>of().findFirst();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("anyMatch - 任意匹配")
        void testAnyMatch() {
            assertThat(OpenCollectors.of(1, 2, 3).anyMatch(n -> n > 2)).isTrue();
            assertThat(OpenCollectors.of(1, 2, 3).anyMatch(n -> n > 5)).isFalse();
        }

        @Test
        @DisplayName("allMatch - 全部匹配")
        void testAllMatch() {
            assertThat(OpenCollectors.of(2, 4, 6).allMatch(n -> n % 2 == 0)).isTrue();
            assertThat(OpenCollectors.of(1, 2, 3).allMatch(n -> n % 2 == 0)).isFalse();
        }

        @Test
        @DisplayName("noneMatch - 无匹配")
        void testNoneMatch() {
            assertThat(OpenCollectors.of(1, 3, 5).noneMatch(n -> n % 2 == 0)).isTrue();
            assertThat(OpenCollectors.of(1, 2, 3).noneMatch(n -> n % 2 == 0)).isFalse();
        }

        @Test
        @DisplayName("forEach - 遍历")
        void testForEach() {
            List<Integer> collected = new ArrayList<>();
            OpenCollectors.of(1, 2, 3).forEach(collected::add);

            assertThat(collected).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("stream - 获取底层流")
        void testStream() {
            Stream<Integer> stream = OpenCollectors.of(1, 2, 3).stream();

            assertThat(stream.toList()).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("收集器测试")
    class CollectorTests {

        @Test
        @DisplayName("toImmutableList 收集器")
        void testToImmutableListCollector() {
            ImmutableList<String> result = Stream.of("a", "b", "c")
                    .collect(OpenCollectors.toImmutableList());

            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("toImmutableSet 收集器")
        void testToImmutableSetCollector() {
            ImmutableSet<String> result = Stream.of("a", "b", "a")
                    .collect(OpenCollectors.toImmutableSet());

            assertThat(result).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("toImmutableMap 收集器")
        void testToImmutableMapCollector() {
            ImmutableMap<Integer, String> result = Stream.of("a", "bb", "ccc")
                    .collect(OpenCollectors.toImmutableMap(String::length, s -> s));

            assertThat(result.get(1)).isEqualTo("a");
            assertThat(result.get(2)).isEqualTo("bb");
            assertThat(result.get(3)).isEqualTo("ccc");
        }

        @Test
        @DisplayName("toMultiset 收集器")
        void testToMultisetCollector() {
            Multiset<String> result = Stream.of("a", "b", "a", "a")
                    .collect(OpenCollectors.toMultiset());

            assertThat(result.count("a")).isEqualTo(3);
            assertThat(result.count("b")).isEqualTo(1);
        }

        @Test
        @DisplayName("counting 收集器")
        void testCountingCollector() {
            Map<String, Long> result = Stream.of("a", "b", "a", "a")
                    .collect(OpenCollectors.counting());

            assertThat(result.get("a")).isEqualTo(3L);
            assertThat(result.get("b")).isEqualTo(1L);
        }

        @Test
        @DisplayName("onlyElement 收集器")
        void testOnlyElementCollector() {
            String result = Stream.of("a")
                    .collect(OpenCollectors.onlyElement());

            assertThat(result).isEqualTo("a");
        }

        @Test
        @DisplayName("onlyElement 收集器 - 多元素抛异常")
        void testOnlyElementMultiple() {
            assertThatThrownBy(() -> Stream.of("a", "b").collect(OpenCollectors.onlyElement()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("exactly one element");
        }

        @Test
        @DisplayName("toOptional 收集器")
        void testToOptionalCollector() {
            Optional<String> result = Stream.of("a")
                    .collect(OpenCollectors.toOptional());

            assertThat(result).hasValue("a");
        }

        @Test
        @DisplayName("toOptional 收集器 - 空")
        void testToOptionalEmpty() {
            Optional<String> result = Stream.<String>of()
                    .collect(OpenCollectors.toOptional());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("toOptional 收集器 - 多元素抛异常")
        void testToOptionalMultiple() {
            assertThatThrownBy(() -> Stream.of("a", "b").collect(OpenCollectors.toOptional()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at most one element");
        }

        @Test
        @DisplayName("leastK 收集器")
        void testLeastKCollector() {
            List<Integer> result = Stream.of(5, 3, 1, 4, 2)
                    .collect(OpenCollectors.leastK(3, Comparator.naturalOrder()));

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("greatestK 收集器")
        void testGreatestKCollector() {
            List<Integer> result = Stream.of(5, 3, 1, 4, 2)
                    .collect(OpenCollectors.greatestK(3, Comparator.naturalOrder()));

            assertThat(result).containsExactly(5, 4, 3);
        }

        @Test
        @DisplayName("partitionBySize 收集器")
        void testPartitionBySizeCollector() {
            List<List<Integer>> result = Stream.of(1, 2, 3, 4, 5)
                    .collect(OpenCollectors.partitionBySize(2));

            assertThat(result).hasSize(3);
            assertThat(result.get(0)).containsExactly(1, 2);
            assertThat(result.get(1)).containsExactly(3, 4);
            assertThat(result.get(2)).containsExactly(5);
        }

        @Test
        @DisplayName("partitionBySize 收集器 - 大小为0抛异常")
        void testPartitionBySizeZero() {
            assertThatThrownBy(() -> OpenCollectors.partitionBySize(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("SetAlgebra 测试")
    class SetAlgebraTests {

        @Test
        @DisplayName("algebra - 创建 SetAlgebra")
        void testAlgebra() {
            Set<Integer> set = Set.of(1, 2, 3);
            SetAlgebra<Integer> algebra = OpenCollectors.algebra(set);

            assertThat(algebra).isNotNull();
        }
    }
}
