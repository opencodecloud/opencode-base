package cloud.opencode.base.collections.transform;

import cloud.opencode.base.collections.ImmutableList;
import cloud.opencode.base.collections.ImmutableMap;
import cloud.opencode.base.collections.ImmutableSet;
import cloud.opencode.base.collections.immutable.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * MoreCollectorUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("MoreCollectorUtil 测试")
class MoreCollectorUtilTest {

    @Nested
    @DisplayName("元素选择收集器测试")
    class ElementSelectionTests {

        @Test
        @DisplayName("first - 获取第一个元素")
        void testFirst() {
            Optional<String> result = Stream.of("a", "b", "c")
                    .collect(MoreCollectorUtil.first());

            assertThat(result).contains("a");
        }

        @Test
        @DisplayName("first - 空流")
        void testFirstEmpty() {
            Optional<String> result = Stream.<String>of()
                    .collect(MoreCollectorUtil.first());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("last - 获取最后一个元素")
        void testLast() {
            Optional<String> result = Stream.of("a", "b", "c")
                    .collect(MoreCollectorUtil.last());

            assertThat(result).contains("c");
        }

        @Test
        @DisplayName("last - 空流")
        void testLastEmpty() {
            Optional<String> result = Stream.<String>of()
                    .collect(MoreCollectorUtil.last());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("onlyElement - 唯一元素")
        void testOnlyElement() {
            String result = Stream.of("only")
                    .collect(MoreCollectorUtil.onlyElement());

            assertThat(result).isEqualTo("only");
        }

        @Test
        @DisplayName("onlyElement - 空流抛异常")
        void testOnlyElementEmpty() {
            assertThatThrownBy(() -> Stream.<String>of()
                    .collect(MoreCollectorUtil.onlyElement()))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("onlyElement - 多元素抛异常")
        void testOnlyElementMultiple() {
            assertThatThrownBy(() -> Stream.of("a", "b")
                    .collect(MoreCollectorUtil.onlyElement()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("elementAt - 获取指定索引元素")
        void testElementAt() {
            Optional<String> result = Stream.of("a", "b", "c", "d")
                    .collect(MoreCollectorUtil.elementAt(2));

            assertThat(result).contains("c");
        }

        @Test
        @DisplayName("elementAt - 索引超出范围")
        void testElementAtOutOfRange() {
            Optional<String> result = Stream.of("a", "b")
                    .collect(MoreCollectorUtil.elementAt(5));

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("elementAt - 负索引抛异常")
        void testElementAtNegative() {
            assertThatThrownBy(() -> MoreCollectorUtil.elementAt(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("最小/最大收集器测试")
    class MinMaxTests {

        @Test
        @DisplayName("minBy - 按键最小")
        void testMinBy() {
            Optional<String> result = Stream.of("apple", "pie", "banana")
                    .collect(MoreCollectorUtil.minBy(String::length));

            assertThat(result).contains("pie");
        }

        @Test
        @DisplayName("maxBy - 按键最大")
        void testMaxBy() {
            Optional<String> result = Stream.of("a", "bb", "ccc")
                    .collect(MoreCollectorUtil.maxBy(String::length));

            assertThat(result).contains("ccc");
        }

        @Test
        @DisplayName("minMax - 同时获取最小和最大")
        void testMinMax() {
            MoreCollectorUtil.MinMax<Integer> result = Stream.of(3, 1, 4, 1, 5, 9, 2, 6)
                    .collect(MoreCollectorUtil.minMax(Comparator.naturalOrder()));

            assertThat(result.min()).isEqualTo(1);
            assertThat(result.max()).isEqualTo(9);
            assertThat(result.getMin()).contains(1);
            assertThat(result.getMax()).contains(9);
            assertThat(result.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("minMax - 空流")
        void testMinMaxEmpty() {
            MoreCollectorUtil.MinMax<Integer> result = Stream.<Integer>of()
                    .collect(MoreCollectorUtil.minMax(Comparator.naturalOrder()));

            assertThat(result.isEmpty()).isTrue();
            assertThat(result.getMin()).isEmpty();
            assertThat(result.getMax()).isEmpty();
        }
    }

    @Nested
    @DisplayName("多值映射收集器测试")
    class MultimapTests {

        @Test
        @DisplayName("toMultimap - 收集为多值映射")
        void testToMultimap() {
            Map<Integer, List<String>> result = Stream.of("a", "bb", "c", "dd", "eee")
                    .collect(MoreCollectorUtil.toMultimap(String::length));

            assertThat(result.get(1)).containsExactly("a", "c");
            assertThat(result.get(2)).containsExactly("bb", "dd");
            assertThat(result.get(3)).containsExactly("eee");
        }

        @Test
        @DisplayName("toMultimap - 带值转换")
        void testToMultimapWithValueMapper() {
            Map<Integer, List<String>> result = Stream.of("apple", "pie", "banana")
                    .collect(MoreCollectorUtil.toMultimap(
                            String::length,
                            String::toUpperCase));

            assertThat(result.get(3)).containsExactly("PIE");
            assertThat(result.get(5)).containsExactly("APPLE");
        }

        @Test
        @DisplayName("toMultimapSet - 收集为集合值映射")
        void testToMultimapSet() {
            Map<Integer, Set<String>> result = Stream.of("a", "b", "c", "a")
                    .collect(MoreCollectorUtil.toMultimapSet(String::length));

            assertThat(result.get(1)).containsExactlyInAnyOrder("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("条件收集器测试")
    class ConditionalTests {

        @Test
        @DisplayName("filtering - 过滤后收集")
        void testFiltering() {
            List<Integer> result = Stream.of(1, 2, 3, 4, 5)
                    .collect(MoreCollectorUtil.filtering(
                            n -> n % 2 == 0,
                            java.util.stream.Collectors.toList()));

            assertThat(result).containsExactly(2, 4);
        }

        @Test
        @DisplayName("mapping - 转换后收集")
        void testMapping() {
            List<Integer> result = Stream.of("a", "bb", "ccc")
                    .collect(MoreCollectorUtil.mapping(
                            String::length,
                            java.util.stream.Collectors.toList()));

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("collectingAndThen - 结果转换")
        void testCollectingAndThen() {
            Integer result = Stream.of("a", "b", "c")
                    .collect(MoreCollectorUtil.collectingAndThen(
                            java.util.stream.Collectors.toList(),
                            List::size));

            assertThat(result).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("聚合收集器测试")
    class AggregationTests {

        @Test
        @DisplayName("fold - 折叠")
        void testFold() {
            String result = Stream.of("a", "b", "c")
                    .collect(MoreCollectorUtil.fold("", (acc, s) -> acc + s));

            assertThat(result).isEqualTo("abc");
        }

        @Test
        @DisplayName("reducing - 归约")
        void testReducing() {
            Integer result = Stream.of(1, 2, 3, 4, 5)
                    .collect(MoreCollectorUtil.reducing(0, Integer::sum));

            assertThat(result).isEqualTo(15);
        }
    }

    @Nested
    @DisplayName("统计收集器测试")
    class StatisticsTests {

        @Test
        @DisplayName("distinctCount - 不同计数")
        void testDistinctCount() {
            Long count = Stream.of("a", "b", "a", "c", "b")
                    .collect(MoreCollectorUtil.distinctCount());

            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("allMatch - 所有匹配")
        void testAllMatch() {
            Boolean allPositive = Stream.of(1, 2, 3, 4, 5)
                    .collect(MoreCollectorUtil.allMatch(n -> n > 0));

            Boolean allEven = Stream.of(1, 2, 3, 4, 5)
                    .collect(MoreCollectorUtil.allMatch(n -> n % 2 == 0));

            assertThat(allPositive).isTrue();
            assertThat(allEven).isFalse();
        }

        @Test
        @DisplayName("anyMatch - 任意匹配")
        void testAnyMatch() {
            Boolean hasEven = Stream.of(1, 2, 3)
                    .collect(MoreCollectorUtil.anyMatch(n -> n % 2 == 0));

            Boolean hasNegative = Stream.of(1, 2, 3)
                    .collect(MoreCollectorUtil.anyMatch(n -> n < 0));

            assertThat(hasEven).isTrue();
            assertThat(hasNegative).isFalse();
        }

        @Test
        @DisplayName("noneMatch - 无匹配")
        void testNoneMatch() {
            Boolean noNegative = Stream.of(1, 2, 3)
                    .collect(MoreCollectorUtil.noneMatch(n -> n < 0));

            assertThat(noNegative).isTrue();
        }
    }

    @Nested
    @DisplayName("Optional 收集器测试")
    class OptionalTests {

        @Test
        @DisplayName("toOptional - 单元素")
        void testToOptionalSingle() {
            Optional<String> result = Stream.of("only")
                    .collect(MoreCollectorUtil.toOptional());

            assertThat(result).contains("only");
        }

        @Test
        @DisplayName("toOptional - 空流")
        void testToOptionalEmpty() {
            Optional<String> result = Stream.<String>of()
                    .collect(MoreCollectorUtil.toOptional());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("toOptional - 多元素抛异常")
        void testToOptionalMultiple() {
            assertThatThrownBy(() -> Stream.of("a", "b")
                    .collect(MoreCollectorUtil.toOptional()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("不可变集合收集器测试")
    class ImmutableCollectionTests {

        @Test
        @DisplayName("toImmutableList - 收集为 ImmutableList")
        void testToImmutableList() {
            ImmutableList<String> result = Stream.of("a", "b", "c")
                    .collect(MoreCollectorUtil.toImmutableList());

            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("toImmutableSet - 收集为 ImmutableSet")
        void testToImmutableSet() {
            ImmutableSet<String> result = Stream.of("a", "b", "a", "c")
                    .collect(MoreCollectorUtil.toImmutableSet());

            assertThat(result).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        @DisplayName("toImmutableMap - 收集为 ImmutableMap")
        void testToImmutableMap() {
            ImmutableMap<Integer, String> result = Stream.of("a", "bb", "ccc")
                    .collect(MoreCollectorUtil.toImmutableMap(String::length, s -> s));

            assertThat(result.get(1)).isEqualTo("a");
            assertThat(result.get(2)).isEqualTo("bb");
            assertThat(result.get(3)).isEqualTo("ccc");
        }

        @Test
        @DisplayName("toImmutableMap - 带合并函数")
        void testToImmutableMapWithMerge() {
            ImmutableMap<Integer, String> result = Stream.of("a", "b", "cc")
                    .collect(MoreCollectorUtil.toImmutableMap(
                            String::length,
                            s -> s,
                            (a, b) -> a + "," + b));

            assertThat(result.get(1)).isEqualTo("a,b");
        }

        @Test
        @DisplayName("indexing - 索引收集")
        void testIndexing() {
            ImmutableMap<Integer, String> result = Stream.of("a", "bb", "ccc")
                    .collect(MoreCollectorUtil.indexing(String::length));

            assertThat(result.get(1)).isEqualTo("a");
            assertThat(result.get(2)).isEqualTo("bb");
        }
    }

    @Nested
    @DisplayName("Top K 收集器测试")
    class TopKTests {

        @Test
        @DisplayName("minK - 最小 k 个元素")
        void testMinK() {
            List<Integer> result = Stream.of(5, 2, 8, 1, 9, 3, 7)
                    .collect(MoreCollectorUtil.minK(3, Comparator.naturalOrder()));

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("maxK - 最大 k 个元素")
        void testMaxK() {
            List<Integer> result = Stream.of(5, 2, 8, 1, 9, 3, 7)
                    .collect(MoreCollectorUtil.maxK(3, Comparator.naturalOrder()));

            assertThat(result).containsExactly(9, 8, 7);
        }

        @Test
        @DisplayName("minK - k 为 0")
        void testMinKZero() {
            List<Integer> result = Stream.of(1, 2, 3)
                    .collect(MoreCollectorUtil.minK(0, Comparator.naturalOrder()));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("分区收集器测试")
    class PartitionTests {

        @Test
        @DisplayName("partitionBySize - 按大小分区")
        void testPartitionBySize() {
            List<List<Integer>> result = Stream.of(1, 2, 3, 4, 5, 6, 7)
                    .collect(MoreCollectorUtil.partitionBySize(3));

            assertThat(result).hasSize(3);
            assertThat(result.get(0)).containsExactly(1, 2, 3);
            assertThat(result.get(1)).containsExactly(4, 5, 6);
            assertThat(result.get(2)).containsExactly(7);
        }

        @Test
        @DisplayName("partitionBySize - 大小非正数抛异常")
        void testPartitionBySizeInvalid() {
            assertThatThrownBy(() -> MoreCollectorUtil.partitionBySize(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("partitioningBy - 按谓词分区")
        void testPartitioningBy() {
            Map<Boolean, List<Integer>> result = Stream.of(1, 2, 3, 4, 5)
                    .collect(MoreCollectorUtil.partitioningBy(n -> n % 2 == 0));

            assertThat(result.get(true)).containsExactly(2, 4);
            assertThat(result.get(false)).containsExactly(1, 3, 5);
        }
    }
}
