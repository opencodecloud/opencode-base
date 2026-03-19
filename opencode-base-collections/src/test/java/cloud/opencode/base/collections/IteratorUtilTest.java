package cloud.opencode.base.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * IteratorUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("IteratorUtil 测试")
class IteratorUtilTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("emptyIterator - 空迭代器")
        void testEmptyIterator() {
            Iterator<String> empty = IteratorUtil.emptyIterator();
            assertThat(empty.hasNext()).isFalse();
        }

        @Test
        @DisplayName("singletonIterator - 单元素迭代器")
        void testSingletonIterator() {
            Iterator<String> single = IteratorUtil.singletonIterator("hello");

            assertThat(single.hasNext()).isTrue();
            assertThat(single.next()).isEqualTo("hello");
            assertThat(single.hasNext()).isFalse();
        }

        @Test
        @DisplayName("singletonIterator - next after exhausted")
        void testSingletonIteratorExhausted() {
            Iterator<String> single = IteratorUtil.singletonIterator("hello");
            single.next();
            assertThatThrownBy(single::next).isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("unmodifiableIterator - 不可修改迭代器")
        void testUnmodifiableIterator() {
            List<String> list = new ArrayList<>(List.of("a", "b"));
            Iterator<String> unmodifiable = IteratorUtil.unmodifiableIterator(list.iterator());

            assertThat(unmodifiable.hasNext()).isTrue();
            assertThat(unmodifiable.next()).isEqualTo("a");
            assertThatThrownBy(unmodifiable::remove)
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("unmodifiableIterator - null 返回空")
        void testUnmodifiableIteratorNull() {
            Iterator<String> result = IteratorUtil.unmodifiableIterator(null);
            assertThat(result.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("连接测试")
    class ConcatTests {

        @Test
        @DisplayName("concat - 连接多个迭代器")
        void testConcat() {
            Iterator<String> a = List.of("a", "b").iterator();
            Iterator<String> b = List.of("c", "d").iterator();

            Iterator<String> result = IteratorUtil.concat(a, b);

            List<String> collected = new ArrayList<>();
            result.forEachRemaining(collected::add);

            assertThat(collected).containsExactly("a", "b", "c", "d");
        }

        @Test
        @DisplayName("concat - null 输入")
        void testConcatNull() {
            Iterator<String> result = IteratorUtil.concat((Iterator<String>[]) null);
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        @DisplayName("concat - 空数组")
        void testConcatEmpty() {
            Iterator<String> result = IteratorUtil.concat();
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        @DisplayName("concat - 包含 null 迭代器")
        void testConcatWithNullIterator() {
            Iterator<String> a = List.of("a").iterator();

            Iterator<String> result = IteratorUtil.concat(a, null, List.of("b").iterator());

            List<String> collected = new ArrayList<>();
            result.forEachRemaining(collected::add);

            assertThat(collected).containsExactly("a", "b");
        }

        @Test
        @DisplayName("concat - next without hasNext")
        void testConcatNextWithoutHasNext() {
            Iterator<String> result = IteratorUtil.concat(List.of("a").iterator());
            result.next();
            assertThatThrownBy(result::next).isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("分区测试")
    class PartitionTests {

        @Test
        @DisplayName("partition - 分区")
        void testPartition() {
            Iterator<Integer> iter = List.of(1, 2, 3, 4, 5).iterator();

            Iterator<List<Integer>> result = IteratorUtil.partition(iter, 2);

            List<List<Integer>> partitions = new ArrayList<>();
            result.forEachRemaining(partitions::add);

            assertThat(partitions).hasSize(3);
            assertThat(partitions.get(0)).containsExactly(1, 2);
            assertThat(partitions.get(1)).containsExactly(3, 4);
            assertThat(partitions.get(2)).containsExactly(5);
        }

        @Test
        @DisplayName("partition - null 迭代器")
        void testPartitionNull() {
            Iterator<List<Integer>> result = IteratorUtil.partition(null, 2);
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        @DisplayName("partition - 空迭代器")
        void testPartitionEmpty() {
            Iterator<List<Integer>> result = IteratorUtil.partition(Collections.emptyIterator(), 2);
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        @DisplayName("partition - 非法大小")
        void testPartitionIllegalSize() {
            assertThatThrownBy(() -> IteratorUtil.partition(List.of(1).iterator(), 0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> IteratorUtil.partition(List.of(1).iterator(), -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("partition - next without hasNext")
        void testPartitionNextWithoutHasNext() {
            Iterator<List<Integer>> result = IteratorUtil.partition(List.of(1).iterator(), 2);
            result.next();
            assertThatThrownBy(result::next).isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("过滤测试")
    class FilterTests {

        @Test
        @DisplayName("filter - 过滤")
        void testFilter() {
            Iterator<Integer> iter = List.of(1, 2, 3, 4, 5).iterator();

            Iterator<Integer> result = IteratorUtil.filter(iter, n -> n % 2 == 0);

            List<Integer> collected = new ArrayList<>();
            result.forEachRemaining(collected::add);

            assertThat(collected).containsExactly(2, 4);
        }

        @Test
        @DisplayName("filter - null 输入")
        void testFilterNull() {
            assertThat(IteratorUtil.filter(null, n -> true).hasNext()).isFalse();
            assertThat(IteratorUtil.filter(List.of(1).iterator(), null).hasNext()).isFalse();
        }

        @Test
        @DisplayName("filter - next without hasNext")
        void testFilterNextWithoutHasNext() {
            Iterator<Integer> result = IteratorUtil.filter(List.of(1).iterator(), n -> true);
            result.next();
            assertThatThrownBy(result::next).isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("转换测试")
    class TransformTests {

        @Test
        @DisplayName("transform - 转换")
        void testTransform() {
            Iterator<String> iter = List.of("a", "bb", "ccc").iterator();

            Iterator<Integer> result = IteratorUtil.transform(iter, String::length);

            List<Integer> collected = new ArrayList<>();
            result.forEachRemaining(collected::add);

            assertThat(collected).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("transform - null 输入")
        void testTransformNull() {
            assertThat(IteratorUtil.transform(null, String::length).hasNext()).isFalse();
            assertThat(IteratorUtil.transform(List.of("a").iterator(), null).hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("查找测试")
    class SearchTests {

        @Test
        @DisplayName("tryFind - 查找")
        void testTryFind() {
            Iterator<String> iter = List.of("apple", "banana").iterator();

            Optional<String> result = IteratorUtil.tryFind(iter, s -> s.startsWith("b"));

            assertThat(result).isPresent().contains("banana");
        }

        @Test
        @DisplayName("tryFind - 未找到")
        void testTryFindNotFound() {
            Iterator<String> iter = List.of("apple", "banana").iterator();

            Optional<String> result = IteratorUtil.tryFind(iter, s -> s.startsWith("x"));

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("tryFind - null 输入")
        void testTryFindNull() {
            assertThat(IteratorUtil.tryFind(null, s -> true)).isEmpty();
            assertThat(IteratorUtil.tryFind(List.of("a").iterator(), null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("访问测试")
    class AccessTests {

        @Test
        @DisplayName("get - 按索引获取")
        void testGet() {
            Iterator<String> iter = List.of("a", "b", "c").iterator();
            assertThat(IteratorUtil.get(iter, 1)).isEqualTo("b");
        }

        @Test
        @DisplayName("get - null 迭代器")
        void testGetNull() {
            assertThatThrownBy(() -> IteratorUtil.get(null, 0))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("get - 负索引")
        void testGetNegativeIndex() {
            assertThatThrownBy(() -> IteratorUtil.get(List.of("a").iterator(), -1))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("get - 索引越界")
        void testGetOutOfBounds() {
            assertThatThrownBy(() -> IteratorUtil.get(List.of("a").iterator(), 5))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("getNext - 获取下一个")
        void testGetNext() {
            Iterator<String> iter = List.of("a", "b").iterator();
            assertThat(IteratorUtil.getNext(iter, "default")).isEqualTo("a");
        }

        @Test
        @DisplayName("getNext - null/empty 返回默认值")
        void testGetNextDefault() {
            assertThat(IteratorUtil.getNext(null, "default")).isEqualTo("default");
            assertThat(IteratorUtil.getNext(Collections.emptyIterator(), "default")).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("转换为集合测试")
    class CollectionConversionTests {

        @Test
        @DisplayName("toArray - 转为数组")
        void testToArray() {
            Iterator<String> iter = List.of("a", "b", "c").iterator();

            String[] result = IteratorUtil.toArray(iter, String.class);

            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("toArray - null 返回空数组")
        void testToArrayNull() {
            String[] result = IteratorUtil.toArray(null, String.class);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("addAll - 添加到集合")
        void testAddAll() {
            List<String> target = new ArrayList<>();
            Iterator<String> iter = List.of("a", "b").iterator();

            boolean result = IteratorUtil.addAll(target, iter);

            assertThat(result).isTrue();
            assertThat(target).containsExactly("a", "b");
        }

        @Test
        @DisplayName("addAll - null 输入")
        void testAddAllNull() {
            assertThat(IteratorUtil.addAll(null, List.of("a").iterator())).isFalse();
            assertThat(IteratorUtil.addAll(new ArrayList<>(), null)).isFalse();
        }
    }

    @Nested
    @DisplayName("统计测试")
    class StatisticsTests {

        @Test
        @DisplayName("size - 计算大小")
        void testSize() {
            Iterator<Integer> iter = List.of(1, 2, 3).iterator();
            assertThat(IteratorUtil.size(iter)).isEqualTo(3);
        }

        @Test
        @DisplayName("size - null 返回 0")
        void testSizeNull() {
            assertThat(IteratorUtil.size(null)).isEqualTo(0);
        }

        @Test
        @DisplayName("contains - 包含")
        void testContains() {
            assertThat(IteratorUtil.contains(List.of("a", "b").iterator(), "a")).isTrue();
            assertThat(IteratorUtil.contains(List.of("a", "b").iterator(), "x")).isFalse();
        }

        @Test
        @DisplayName("contains - null 迭代器")
        void testContainsNull() {
            assertThat(IteratorUtil.contains(null, "a")).isFalse();
        }

        @Test
        @DisplayName("removeAll - 移除匹配")
        void testRemoveAll() {
            List<String> list = new ArrayList<>(List.of("a", "b", "c", "a"));

            boolean result = IteratorUtil.removeAll(list.iterator(), List.of("a", "c"));

            // Note: removeAll modifies during iteration
            // The result depends on the underlying implementation
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("removeAll - null 输入")
        void testRemoveAllNull() {
            assertThat(IteratorUtil.removeAll(null, List.of("a"))).isFalse();
            assertThat(IteratorUtil.removeAll(new ArrayList<>().iterator(), null)).isFalse();
            assertThat(IteratorUtil.removeAll(new ArrayList<>().iterator(), List.of())).isFalse();
        }
    }

    @Nested
    @DisplayName("相等性检查测试")
    class EqualityTests {

        @Test
        @DisplayName("elementsEqual - 相等")
        void testElementsEqual() {
            Iterator<String> a = List.of("a", "b").iterator();
            Iterator<String> b = List.of("a", "b").iterator();
            assertThat(IteratorUtil.elementsEqual(a, b)).isTrue();
        }

        @Test
        @DisplayName("elementsEqual - 不相等")
        void testElementsNotEqual() {
            assertThat(IteratorUtil.elementsEqual(
                    List.of("a", "b").iterator(),
                    List.of("a", "c").iterator()
            )).isFalse();

            assertThat(IteratorUtil.elementsEqual(
                    List.of("a", "b").iterator(),
                    List.of("a").iterator()
            )).isFalse();
        }

        @Test
        @DisplayName("elementsEqual - 相同引用")
        void testElementsEqualSameReference() {
            Iterator<String> iter = List.of("a", "b").iterator();
            assertThat(IteratorUtil.elementsEqual(iter, iter)).isTrue();
        }

        @Test
        @DisplayName("elementsEqual - null 输入")
        void testElementsEqualNull() {
            assertThat(IteratorUtil.elementsEqual(null, List.of("a").iterator())).isFalse();
            assertThat(IteratorUtil.elementsEqual(List.of("a").iterator(), null)).isFalse();
        }
    }

    @Nested
    @DisplayName("字符串表示测试")
    class ToStringTests {

        @Test
        @DisplayName("toString - 正常")
        void testToString() {
            Iterator<String> iter = List.of("a", "b", "c").iterator();
            assertThat(IteratorUtil.toString(iter)).isEqualTo("[a, b, c]");
        }

        @Test
        @DisplayName("toString - null")
        void testToStringNull() {
            assertThat(IteratorUtil.toString(null)).isEqualTo("null");
        }

        @Test
        @DisplayName("toString - 空")
        void testToStringEmpty() {
            assertThat(IteratorUtil.toString(Collections.emptyIterator())).isEqualTo("[]");
        }
    }

    @Nested
    @DisplayName("PeekingIterator 测试")
    class PeekingIteratorTests {

        @Test
        @DisplayName("peekingIterator - 创建")
        void testPeekingIterator() {
            Iterator<String> iter = List.of("a", "b", "c").iterator();
            PeekingIterator<String> peeking = IteratorUtil.peekingIterator(iter);

            assertThat(peeking.peek()).isEqualTo("a");
            assertThat(peeking.peek()).isEqualTo("a"); // peek again
            assertThat(peeking.next()).isEqualTo("a");
            assertThat(peeking.peek()).isEqualTo("b");
        }

        @Test
        @DisplayName("peekingIterator - null 返回空")
        void testPeekingIteratorNull() {
            PeekingIterator<String> peeking = IteratorUtil.peekingIterator(null);
            assertThat(peeking.hasNext()).isFalse();
            assertThatThrownBy(peeking::peek).isInstanceOf(NoSuchElementException.class);
            assertThatThrownBy(peeking::next).isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("peekingIterator - 已经是 PeekingIterator")
        void testPeekingIteratorAlreadyPeeking() {
            PeekingIterator<String> original = IteratorUtil.peekingIterator(List.of("a").iterator());
            PeekingIterator<String> wrapped = IteratorUtil.peekingIterator(original);
            assertThat(wrapped).isSameAs(original);
        }

        @Test
        @DisplayName("peekingIterator - remove without peek")
        void testPeekingIteratorRemove() {
            List<String> list = new ArrayList<>(List.of("a", "b"));
            PeekingIterator<String> peeking = IteratorUtil.peekingIterator(list.iterator());

            peeking.next();
            peeking.remove();

            assertThat(list).containsExactly("b");
        }

        @Test
        @DisplayName("peekingIterator - remove after peek throws")
        void testPeekingIteratorRemoveAfterPeek() {
            List<String> list = new ArrayList<>(List.of("a", "b"));
            PeekingIterator<String> peeking = IteratorUtil.peekingIterator(list.iterator());

            peeking.next();
            peeking.peek();

            assertThatThrownBy(peeking::remove).isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("advance 测试")
    class AdvanceTests {

        @Test
        @DisplayName("advance - 前进")
        void testAdvance() {
            Iterator<Integer> iter = List.of(1, 2, 3, 4, 5).iterator();

            int advanced = IteratorUtil.advance(iter, 3);

            assertThat(advanced).isEqualTo(3);
            assertThat(iter.next()).isEqualTo(4);
        }

        @Test
        @DisplayName("advance - null/0")
        void testAdvanceNullOrZero() {
            assertThat(IteratorUtil.advance(null, 3)).isEqualTo(0);
            assertThat(IteratorUtil.advance(List.of(1).iterator(), 0)).isEqualTo(0);
            assertThat(IteratorUtil.advance(List.of(1).iterator(), -1)).isEqualTo(0);
        }

        @Test
        @DisplayName("advance - 超过大小")
        void testAdvanceBeyondSize() {
            Iterator<Integer> iter = List.of(1, 2).iterator();

            int advanced = IteratorUtil.advance(iter, 10);

            assertThat(advanced).isEqualTo(2);
            assertThat(iter.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("limit 测试")
    class LimitTests {

        @Test
        @DisplayName("limit - 限制")
        void testLimit() {
            Iterator<Integer> iter = List.of(1, 2, 3, 4, 5).iterator();

            Iterator<Integer> limited = IteratorUtil.limit(iter, 3);

            List<Integer> collected = new ArrayList<>();
            limited.forEachRemaining(collected::add);

            assertThat(collected).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("limit - null/0")
        void testLimitNullOrZero() {
            assertThat(IteratorUtil.limit(null, 3).hasNext()).isFalse();
            assertThat(IteratorUtil.limit(List.of(1).iterator(), 0).hasNext()).isFalse();
        }

        @Test
        @DisplayName("limit - 负数抛异常")
        void testLimitNegative() {
            assertThatThrownBy(() -> IteratorUtil.limit(List.of(1).iterator(), -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("limit - next without hasNext")
        void testLimitNextWithoutHasNext() {
            Iterator<Integer> limited = IteratorUtil.limit(List.of(1).iterator(), 1);
            limited.next();
            assertThatThrownBy(limited::next).isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("cycle 测试")
    class CycleTests {

        @Test
        @DisplayName("cycle - 循环")
        void testCycle() {
            Iterator<Integer> cycled = IteratorUtil.cycle(List.of(1, 2));

            assertThat(cycled.next()).isEqualTo(1);
            assertThat(cycled.next()).isEqualTo(2);
            assertThat(cycled.next()).isEqualTo(1);
            assertThat(cycled.next()).isEqualTo(2);
            assertThat(cycled.hasNext()).isTrue();
        }

        @Test
        @DisplayName("cycle - null")
        void testCycleNull() {
            assertThat(IteratorUtil.cycle(null).hasNext()).isFalse();
        }

        @Test
        @DisplayName("cycle - 空 Iterable")
        void testCycleEmpty() {
            Iterator<Integer> cycled = IteratorUtil.cycle(List.of());
            assertThat(cycled.hasNext()).isFalse();
            assertThatThrownBy(cycled::next).isInstanceOf(NoSuchElementException.class);
        }
    }
}
