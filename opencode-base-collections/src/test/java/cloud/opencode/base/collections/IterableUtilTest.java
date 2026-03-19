package cloud.opencode.base.collections;

import cloud.opencode.base.collections.exception.OpenCollectionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.*;

/**
 * IterableUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("IterableUtil 测试")
class IterableUtilTest {

    @Nested
    @DisplayName("连接与拼接测试")
    class ConcatTests {

        @Test
        @DisplayName("concat - 连接多个 Iterable")
        void testConcatVarargs() {
            List<String> a = List.of("a", "b");
            List<String> b = List.of("c", "d");
            List<String> c = List.of("e");

            Iterable<String> result = IterableUtil.concat(a, b, c);

            assertThat(result).containsExactly("a", "b", "c", "d", "e");
        }

        @Test
        @DisplayName("concat - null 输入返回空")
        void testConcatNull() {
            Iterable<String> result = IterableUtil.concat((Iterable<String>[]) null);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("concat - 空数组返回空")
        void testConcatEmpty() {
            Iterable<String> result = IterableUtil.concat();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("concat - 包含 null 元素")
        void testConcatWithNullElement() {
            List<String> a = List.of("a", "b");
            Iterable<String> result = IterableUtil.concat(a, null, List.of("c"));
            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("concat - Iterable of Iterables")
        void testConcatIterableOfIterables() {
            List<List<String>> lists = List.of(List.of("a", "b"), List.of("c"));

            Iterable<String> result = IterableUtil.concat(lists);

            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("concat - Iterable of Iterables null")
        void testConcatIterableOfIterablesNull() {
            Iterable<String> result = IterableUtil.concat((Iterable<Iterable<String>>) null);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("concat - next without hasNext throws exception")
        void testConcatNextWithoutHasNext() {
            List<String> a = List.of("a");
            Iterable<String> result = IterableUtil.concat(a);
            Iterator<String> it = result.iterator();
            it.next();
            assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("分区测试")
    class PartitionTests {

        @Test
        @DisplayName("partition - 正常分区")
        void testPartition() {
            List<Integer> list = List.of(1, 2, 3, 4, 5);

            Iterable<List<Integer>> result = IterableUtil.partition(list, 2);

            List<List<Integer>> partitions = new ArrayList<>();
            result.forEach(partitions::add);

            assertThat(partitions).hasSize(3);
            assertThat(partitions.get(0)).containsExactly(1, 2);
            assertThat(partitions.get(1)).containsExactly(3, 4);
            assertThat(partitions.get(2)).containsExactly(5);
        }

        @Test
        @DisplayName("partition - null 返回空")
        void testPartitionNull() {
            Iterable<List<Integer>> result = IterableUtil.partition(null, 2);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("partition - 非法大小")
        void testPartitionIllegalSize() {
            assertThatThrownBy(() -> IterableUtil.partition(List.of(1, 2), 0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> IterableUtil.partition(List.of(1, 2), -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("partition - next without hasNext throws exception")
        void testPartitionNextWithoutHasNext() {
            Iterable<List<Integer>> result = IterableUtil.partition(List.of(1), 2);
            Iterator<List<Integer>> it = result.iterator();
            it.next();
            assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("paddedPartition - 填充分区")
        void testPaddedPartition() {
            List<Integer> list = List.of(1, 2, 3);

            Iterable<List<Integer>> result = IterableUtil.paddedPartition(list, 2);

            List<List<Integer>> partitions = new ArrayList<>();
            result.forEach(partitions::add);

            assertThat(partitions).hasSize(2);
            assertThat(partitions.get(0)).containsExactly(1, 2);
            assertThat(partitions.get(1)).containsExactly(3, null);
        }

        @Test
        @DisplayName("paddedPartition - null 返回空")
        void testPaddedPartitionNull() {
            Iterable<List<Integer>> result = IterableUtil.paddedPartition(null, 2);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("paddedPartition - 非法大小")
        void testPaddedPartitionIllegalSize() {
            assertThatThrownBy(() -> IterableUtil.paddedPartition(List.of(1), 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("paddedPartition - next without hasNext throws exception")
        void testPaddedPartitionNextWithoutHasNext() {
            Iterable<List<Integer>> result = IterableUtil.paddedPartition(List.of(1), 2);
            Iterator<List<Integer>> it = result.iterator();
            it.next();
            assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("过滤测试")
    class FilterTests {

        @Test
        @DisplayName("filter - 过滤元素")
        void testFilter() {
            List<Integer> list = List.of(1, 2, 3, 4, 5);

            Iterable<Integer> result = IterableUtil.filter(list, n -> n % 2 == 0);

            assertThat(result).containsExactly(2, 4);
        }

        @Test
        @DisplayName("filter - null iterable")
        void testFilterNullIterable() {
            Iterable<Integer> result = IterableUtil.filter(null, n -> true);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("filter - null predicate")
        void testFilterNullPredicate() {
            Iterable<Integer> result = IterableUtil.filter(List.of(1, 2), (Predicate<Integer>) null);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("filter - next without hasNext throws exception")
        void testFilterNextWithoutHasNext() {
            Iterable<Integer> result = IterableUtil.filter(List.of(1), n -> true);
            Iterator<Integer> it = result.iterator();
            it.next();
            assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("filter - 按类型过滤")
        void testFilterByType() {
            List<Object> list = List.of("a", 1, "b", 2);

            Iterable<String> result = IterableUtil.filter(list, String.class);

            assertThat(result).containsExactly("a", "b");
        }

        @Test
        @DisplayName("filter - 按类型过滤 null 输入")
        void testFilterByTypeNull() {
            Iterable<String> result1 = IterableUtil.filter((Iterable<?>) null, String.class);
            assertThat(result1).isEmpty();

            Iterable<String> result2 = IterableUtil.filter(List.of("a"), (Class<String>) null);
            assertThat(result2).isEmpty();
        }

        @Test
        @DisplayName("any - 检查是否存在匹配")
        void testAny() {
            List<Integer> list = List.of(1, 2, 3, 4, 5);

            assertThat(IterableUtil.any(list, n -> n > 3)).isTrue();
            assertThat(IterableUtil.any(list, n -> n > 10)).isFalse();
        }

        @Test
        @DisplayName("any - null 输入")
        void testAnyNull() {
            assertThat(IterableUtil.any(null, n -> true)).isFalse();
            assertThat(IterableUtil.any(List.of(1), null)).isFalse();
        }

        @Test
        @DisplayName("all - 检查是否全部匹配")
        void testAll() {
            List<Integer> list = List.of(2, 4, 6);

            assertThat(IterableUtil.all(list, n -> n % 2 == 0)).isTrue();
            assertThat(IterableUtil.all(list, n -> n > 3)).isFalse();
        }

        @Test
        @DisplayName("all - null 输入")
        void testAllNull() {
            assertThat(IterableUtil.all(null, n -> true)).isTrue();
            assertThat(IterableUtil.all(List.of(1), null)).isFalse();
        }
    }

    @Nested
    @DisplayName("查找测试")
    class SearchTests {

        @Test
        @DisplayName("tryFind - 查找匹配元素")
        void testTryFind() {
            List<String> list = List.of("apple", "banana", "cherry");

            Optional<String> result = IterableUtil.tryFind(list, s -> s.startsWith("b"));

            assertThat(result).isPresent().contains("banana");
        }

        @Test
        @DisplayName("tryFind - 未找到")
        void testTryFindNotFound() {
            List<String> list = List.of("apple", "banana");

            Optional<String> result = IterableUtil.tryFind(list, s -> s.startsWith("x"));

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("tryFind - null 输入")
        void testTryFindNull() {
            assertThat(IterableUtil.tryFind(null, s -> true)).isEmpty();
            assertThat(IterableUtil.tryFind(List.of("a"), null)).isEmpty();
        }

        @Test
        @DisplayName("getOnlyElement - 获取唯一元素")
        void testGetOnlyElement() {
            List<String> single = List.of("only");

            String result = IterableUtil.getOnlyElement(single);

            assertThat(result).isEqualTo("only");
        }

        @Test
        @DisplayName("getOnlyElement - null 抛异常")
        void testGetOnlyElementNull() {
            assertThatThrownBy(() -> IterableUtil.getOnlyElement(null))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("getOnlyElement - 空抛异常")
        void testGetOnlyElementEmpty() {
            assertThatThrownBy(() -> IterableUtil.getOnlyElement(List.of()))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("getOnlyElement - 多元素抛异常")
        void testGetOnlyElementMultiple() {
            assertThatThrownBy(() -> IterableUtil.getOnlyElement(List.of("a", "b")))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("getOnlyElement - 带默认值")
        void testGetOnlyElementWithDefault() {
            assertThat(IterableUtil.getOnlyElement(null, "default")).isEqualTo("default");
            assertThat(IterableUtil.getOnlyElement(List.of(), "default")).isEqualTo("default");
            assertThat(IterableUtil.getOnlyElement(List.of("value"), "default")).isEqualTo("value");
        }

        @Test
        @DisplayName("getOnlyElement - 带默认值多元素抛异常")
        void testGetOnlyElementWithDefaultMultiple() {
            assertThatThrownBy(() -> IterableUtil.getOnlyElement(List.of("a", "b"), "default"))
                    .isInstanceOf(OpenCollectionException.class);
        }
    }

    @Nested
    @DisplayName("访问测试")
    class AccessTests {

        @Test
        @DisplayName("getFirst - 获取第一个元素")
        void testGetFirst() {
            List<String> list = List.of("a", "b", "c");
            assertThat(IterableUtil.getFirst(list, "default")).isEqualTo("a");
        }

        @Test
        @DisplayName("getFirst - null 返回默认值")
        void testGetFirstNull() {
            assertThat(IterableUtil.getFirst(null, "default")).isEqualTo("default");
        }

        @Test
        @DisplayName("getFirst - 空返回默认值")
        void testGetFirstEmpty() {
            assertThat(IterableUtil.getFirst(List.of(), "default")).isEqualTo("default");
        }

        @Test
        @DisplayName("getLast - 获取最后一个元素")
        void testGetLast() {
            List<String> list = List.of("a", "b", "c");
            assertThat(IterableUtil.getLast(list)).isEqualTo("c");
        }

        @Test
        @DisplayName("getLast - null 抛异常")
        void testGetLastNull() {
            assertThatThrownBy(() -> IterableUtil.getLast(null))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("getLast - 空 List 抛异常")
        void testGetLastEmptyList() {
            assertThatThrownBy(() -> IterableUtil.getLast(List.of()))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("getLast - 非 List Iterable")
        void testGetLastNonList() {
            Set<String> set = new LinkedHashSet<>(List.of("a", "b", "c"));
            assertThat(IterableUtil.getLast(set)).isEqualTo("c");
        }

        @Test
        @DisplayName("getLast - 空 Iterable 抛异常")
        void testGetLastEmptyIterable() {
            Set<String> emptySet = new LinkedHashSet<>();
            assertThatThrownBy(() -> IterableUtil.getLast(emptySet))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("getLast - 带默认值")
        void testGetLastWithDefault() {
            assertThat(IterableUtil.getLast(null, "default")).isEqualTo("default");
            assertThat(IterableUtil.getLast(List.of(), "default")).isEqualTo("default");
            assertThat(IterableUtil.getLast(List.of("a", "b"), "default")).isEqualTo("b");
        }

        @Test
        @DisplayName("getLast - 带默认值非 List")
        void testGetLastWithDefaultNonList() {
            Set<String> set = new LinkedHashSet<>(List.of("a", "b"));
            assertThat(IterableUtil.getLast(set, "default")).isEqualTo("b");

            Set<String> emptySet = new LinkedHashSet<>();
            assertThat(IterableUtil.getLast(emptySet, "default")).isEqualTo("default");
        }

        @Test
        @DisplayName("get - 按索引获取")
        void testGet() {
            List<String> list = List.of("a", "b", "c");
            assertThat(IterableUtil.get(list, 1)).isEqualTo("b");
        }

        @Test
        @DisplayName("get - null 抛异常")
        void testGetNull() {
            assertThatThrownBy(() -> IterableUtil.get(null, 0))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("get - 负索引抛异常")
        void testGetNegativeIndex() {
            assertThatThrownBy(() -> IterableUtil.get(List.of("a"), -1))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("get - 索引越界")
        void testGetOutOfBounds() {
            assertThatThrownBy(() -> IterableUtil.get(List.of("a"), 5))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("get - 非 List Iterable")
        void testGetNonList() {
            Set<String> set = new LinkedHashSet<>(List.of("a", "b", "c"));
            assertThat(IterableUtil.get(set, 1)).isEqualTo("b");
        }

        @Test
        @DisplayName("get - 带默认值")
        void testGetWithDefault() {
            assertThat(IterableUtil.get(null, 0, "default")).isEqualTo("default");
            assertThat(IterableUtil.get(List.of("a"), -1, "default")).isEqualTo("default");
            assertThat(IterableUtil.get(List.of("a"), 5, "default")).isEqualTo("default");
            assertThat(IterableUtil.get(List.of("a", "b"), 1, "default")).isEqualTo("b");
        }

        @Test
        @DisplayName("get - 带默认值非 List")
        void testGetWithDefaultNonList() {
            Set<String> set = new LinkedHashSet<>(List.of("a", "b"));
            assertThat(IterableUtil.get(set, 1, "default")).isEqualTo("b");
            assertThat(IterableUtil.get(set, 5, "default")).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("转换测试")
    class TransformTests {

        @Test
        @DisplayName("transform - 转换元素")
        void testTransform() {
            List<String> list = List.of("a", "bb", "ccc");

            Iterable<Integer> result = IterableUtil.transform(list, String::length);

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("transform - null 输入")
        void testTransformNull() {
            assertThat(IterableUtil.transform(null, String::length)).isEmpty();
            assertThat(IterableUtil.transform(List.of("a"), null)).isEmpty();
        }

        @Test
        @DisplayName("limit - 限制数量")
        void testLimit() {
            List<Integer> list = List.of(1, 2, 3, 4, 5);

            Iterable<Integer> result = IterableUtil.limit(list, 3);

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("limit - null 返回空")
        void testLimitNull() {
            assertThat(IterableUtil.limit(null, 3)).isEmpty();
        }

        @Test
        @DisplayName("limit - 0 返回空")
        void testLimitZero() {
            assertThat(IterableUtil.limit(List.of(1, 2), 0)).isEmpty();
        }

        @Test
        @DisplayName("limit - 负数抛异常")
        void testLimitNegative() {
            assertThatThrownBy(() -> IterableUtil.limit(List.of(1), -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("limit - next without hasNext throws exception")
        void testLimitNextWithoutHasNext() {
            Iterable<Integer> result = IterableUtil.limit(List.of(1), 1);
            Iterator<Integer> it = result.iterator();
            it.next();
            assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("skip - 跳过元素")
        void testSkip() {
            List<Integer> list = List.of(1, 2, 3, 4, 5);

            Iterable<Integer> result = IterableUtil.skip(list, 2);

            assertThat(result).containsExactly(3, 4, 5);
        }

        @Test
        @DisplayName("skip - null 返回空")
        void testSkipNull() {
            assertThat(IterableUtil.skip(null, 2)).isEmpty();
        }

        @Test
        @DisplayName("skip - 负数抛异常")
        void testSkipNegative() {
            assertThatThrownBy(() -> IterableUtil.skip(List.of(1), -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("cycle - 循环迭代")
        void testCycle() {
            List<Integer> list = List.of(1, 2);

            Iterable<Integer> result = IterableUtil.cycle(list);
            Iterator<Integer> it = result.iterator();

            assertThat(it.next()).isEqualTo(1);
            assertThat(it.next()).isEqualTo(2);
            assertThat(it.next()).isEqualTo(1);
            assertThat(it.next()).isEqualTo(2);
            assertThat(it.hasNext()).isTrue();
        }

        @Test
        @DisplayName("cycle - null 返回空")
        void testCycleNull() {
            assertThat(IterableUtil.cycle(null)).isEmpty();
        }

        @Test
        @DisplayName("cycle - 空 iterable")
        void testCycleEmpty() {
            Iterable<Integer> result = IterableUtil.cycle(List.of());
            Iterator<Integer> it = result.iterator();
            assertThat(it.hasNext()).isFalse();
            assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("统计测试")
    class StatisticsTests {

        @Test
        @DisplayName("size - 计算大小")
        void testSize() {
            assertThat(IterableUtil.size(List.of(1, 2, 3))).isEqualTo(3);
        }

        @Test
        @DisplayName("size - null 返回 0")
        void testSizeNull() {
            assertThat(IterableUtil.size(null)).isEqualTo(0);
        }

        @Test
        @DisplayName("size - Collection 优化")
        void testSizeCollection() {
            assertThat(IterableUtil.size(Set.of(1, 2, 3))).isEqualTo(3);
        }

        @Test
        @DisplayName("size - 非 Collection Iterable")
        void testSizeNonCollection() {
            Iterable<Integer> iterable = () -> List.of(1, 2, 3).iterator();
            assertThat(IterableUtil.size(iterable)).isEqualTo(3);
        }

        @Test
        @DisplayName("contains - 包含元素")
        void testContains() {
            List<String> list = List.of("a", "b", "c");
            assertThat(IterableUtil.contains(list, "b")).isTrue();
            assertThat(IterableUtil.contains(list, "x")).isFalse();
        }

        @Test
        @DisplayName("contains - null iterable")
        void testContainsNull() {
            assertThat(IterableUtil.contains(null, "a")).isFalse();
        }

        @Test
        @DisplayName("contains - Collection 优化")
        void testContainsCollection() {
            assertThat(IterableUtil.contains(Set.of("a", "b"), "a")).isTrue();
        }

        @Test
        @DisplayName("contains - 非 Collection Iterable")
        void testContainsNonCollection() {
            Iterable<String> iterable = () -> List.of("a", "b").iterator();
            assertThat(IterableUtil.contains(iterable, "a")).isTrue();
            assertThat(IterableUtil.contains(iterable, "x")).isFalse();
        }

        @Test
        @DisplayName("frequency - 统计频率")
        void testFrequency() {
            List<String> list = List.of("a", "b", "a", "c", "a");
            assertThat(IterableUtil.frequency(list, "a")).isEqualTo(3);
            assertThat(IterableUtil.frequency(list, "x")).isEqualTo(0);
        }

        @Test
        @DisplayName("frequency - null iterable")
        void testFrequencyNull() {
            assertThat(IterableUtil.frequency(null, "a")).isEqualTo(0);
        }

        @Test
        @DisplayName("isEmpty - 检查是否为空")
        void testIsEmpty() {
            assertThat(IterableUtil.isEmpty(List.of())).isTrue();
            assertThat(IterableUtil.isEmpty(List.of("a"))).isFalse();
        }

        @Test
        @DisplayName("isEmpty - null 返回 true")
        void testIsEmptyNull() {
            assertThat(IterableUtil.isEmpty(null)).isTrue();
        }

        @Test
        @DisplayName("isEmpty - Collection 优化")
        void testIsEmptyCollection() {
            assertThat(IterableUtil.isEmpty(Set.of())).isTrue();
        }

        @Test
        @DisplayName("isEmpty - 非 Collection Iterable")
        void testIsEmptyNonCollection() {
            Iterable<String> emptyIterable = Collections::emptyIterator;
            assertThat(IterableUtil.isEmpty(emptyIterable)).isTrue();

            Iterable<String> nonEmptyIterable = () -> List.of("a").iterator();
            assertThat(IterableUtil.isEmpty(nonEmptyIterable)).isFalse();
        }
    }

    @Nested
    @DisplayName("转换为集合测试")
    class CollectionConversionTests {

        @Test
        @DisplayName("toArray - 转为数组")
        void testToArray() {
            List<String> list = List.of("a", "b", "c");

            String[] result = IterableUtil.toArray(list, String.class);

            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("toArray - null 返回空数组")
        void testToArrayNull() {
            String[] result = IterableUtil.toArray(null, String.class);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("addAll - 添加到集合")
        void testAddAll() {
            List<String> target = new ArrayList<>();
            List<String> source = List.of("a", "b");

            boolean result = IterableUtil.addAll(target, source);

            assertThat(result).isTrue();
            assertThat(target).containsExactly("a", "b");
        }

        @Test
        @DisplayName("addAll - null 输入")
        void testAddAllNull() {
            assertThat(IterableUtil.addAll(null, List.of("a"))).isFalse();
            assertThat(IterableUtil.addAll(new ArrayList<>(), null)).isFalse();
        }

        @Test
        @DisplayName("addAll - Collection 优化")
        void testAddAllCollection() {
            List<String> target = new ArrayList<>();
            boolean result = IterableUtil.addAll(target, List.of("a", "b"));
            assertThat(result).isTrue();
            assertThat(target).containsExactly("a", "b");
        }

        @Test
        @DisplayName("addAll - 非 Collection Iterable")
        void testAddAllNonCollection() {
            List<String> target = new ArrayList<>();
            Iterable<String> source = () -> List.of("a", "b").iterator();
            boolean result = IterableUtil.addAll(target, source);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("removeAll - 移除所有匹配")
        void testRemoveAll() {
            List<String> list = new ArrayList<>(List.of("a", "b", "c", "a"));

            boolean result = IterableUtil.removeAll(list, List.of("a", "c"));

            assertThat(result).isTrue();
            assertThat(list).containsExactly("b");
        }

        @Test
        @DisplayName("removeAll - null 输入")
        void testRemoveAllNull() {
            assertThat(IterableUtil.removeAll(null, List.of("a"))).isFalse();
            assertThat(IterableUtil.removeAll(new ArrayList<>(), null)).isFalse();
            assertThat(IterableUtil.removeAll(new ArrayList<>(), List.of())).isFalse();
        }

        @Test
        @DisplayName("retainAll - 保留所有匹配")
        void testRetainAll() {
            List<String> list = new ArrayList<>(List.of("a", "b", "c"));

            boolean result = IterableUtil.retainAll(list, List.of("a", "c"));

            assertThat(result).isTrue();
            assertThat(list).containsExactly("a", "c");
        }

        @Test
        @DisplayName("retainAll - null iterable")
        void testRetainAllNullIterable() {
            assertThat(IterableUtil.retainAll(null, List.of("a"))).isFalse();
        }

        @Test
        @DisplayName("retainAll - null/empty elementsToRetain")
        void testRetainAllNullElements() {
            List<String> list = new ArrayList<>(List.of("a", "b"));
            boolean result = IterableUtil.retainAll(list, null);
            assertThat(result).isTrue();
            assertThat(list).isEmpty();

            List<String> list2 = new ArrayList<>(List.of("a", "b"));
            boolean result2 = IterableUtil.retainAll(list2, List.of());
            assertThat(result2).isTrue();
            assertThat(list2).isEmpty();
        }

        @Test
        @DisplayName("removeIf - 按条件移除")
        void testRemoveIf() {
            List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4, 5));

            boolean result = IterableUtil.removeIf(list, n -> n % 2 == 0);

            assertThat(result).isTrue();
            assertThat(list).containsExactly(1, 3, 5);
        }

        @Test
        @DisplayName("removeIf - null 输入")
        void testRemoveIfNull() {
            assertThat(IterableUtil.removeIf(null, n -> true)).isFalse();
            assertThat(IterableUtil.removeIf(new ArrayList<>(), null)).isFalse();
        }
    }

    @Nested
    @DisplayName("相等性检查测试")
    class EqualityTests {

        @Test
        @DisplayName("elementsEqual - 相等")
        void testElementsEqual() {
            assertThat(IterableUtil.elementsEqual(List.of("a", "b"), List.of("a", "b"))).isTrue();
        }

        @Test
        @DisplayName("elementsEqual - 不相等")
        void testElementsNotEqual() {
            assertThat(IterableUtil.elementsEqual(List.of("a", "b"), List.of("a", "c"))).isFalse();
            assertThat(IterableUtil.elementsEqual(List.of("a", "b"), List.of("a"))).isFalse();
        }

        @Test
        @DisplayName("elementsEqual - 相同引用")
        void testElementsEqualSameReference() {
            List<String> list = List.of("a", "b");
            assertThat(IterableUtil.elementsEqual(list, list)).isTrue();
        }

        @Test
        @DisplayName("elementsEqual - null 输入")
        void testElementsEqualNull() {
            assertThat(IterableUtil.elementsEqual(null, List.of("a"))).isFalse();
            assertThat(IterableUtil.elementsEqual(List.of("a"), null)).isFalse();
            assertThat(IterableUtil.elementsEqual(null, null)).isTrue();
        }
    }

    @Nested
    @DisplayName("字符串表示测试")
    class ToStringTests {

        @Test
        @DisplayName("toString - 正常输出")
        void testToString() {
            List<String> list = List.of("a", "b", "c");

            String result = IterableUtil.toString(list);

            assertThat(result).isEqualTo("[a, b, c]");
        }

        @Test
        @DisplayName("toString - null")
        void testToStringNull() {
            assertThat(IterableUtil.toString(null)).isEqualTo("null");
        }

        @Test
        @DisplayName("toString - 空")
        void testToStringEmpty() {
            assertThat(IterableUtil.toString(List.of())).isEqualTo("[]");
        }
    }
}
