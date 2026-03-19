package cloud.opencode.base.functional.monad;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * Sequence 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@DisplayName("Sequence 测试")
class SequenceTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("empty() 创建空序列")
        void testEmpty() {
            Sequence<Integer> seq = Sequence.empty();

            assertThat(seq.toList()).isEmpty();
        }

        @Test
        @DisplayName("of() 从元素创建序列")
        void testOf() {
            Sequence<Integer> seq = Sequence.of(1, 2, 3);

            assertThat(seq.toList()).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("from() 从 Iterable 创建序列")
        void testFrom() {
            Sequence<Integer> seq = Sequence.from(List.of(1, 2, 3));

            assertThat(seq.toList()).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("from(null) 抛出异常")
        void testFromNull() {
            assertThatThrownBy(() -> Sequence.from(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("fromStream() 从 Stream 创建序列")
        void testFromStream() {
            Sequence<Integer> seq = Sequence.fromStream(Stream.of(1, 2, 3));

            assertThat(seq.toList()).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("iterate() 创建无限序列")
        void testIterate() {
            Sequence<Integer> seq = Sequence.iterate(1, n -> n + 1);

            assertThat(seq.take(5).toList()).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("generate() 从供应商创建无限序列")
        void testGenerate() {
            Sequence<Integer> seq = Sequence.generate(() -> 42);

            assertThat(seq.take(3).toList()).containsExactly(42, 42, 42);
        }

        @Test
        @DisplayName("range() 创建范围序列")
        void testRange() {
            Sequence<Integer> seq = Sequence.range(1, 5);

            assertThat(seq.toList()).containsExactly(1, 2, 3, 4);
        }

        @Test
        @DisplayName("rangeClosed() 创建闭区间序列")
        void testRangeClosed() {
            Sequence<Integer> seq = Sequence.rangeClosed(1, 5);

            assertThat(seq.toList()).containsExactly(1, 2, 3, 4, 5);
        }
    }

    @Nested
    @DisplayName("map() 测试")
    class MapTests {

        @Test
        @DisplayName("map() 转换元素")
        void testMap() {
            Sequence<Integer> seq = Sequence.of(1, 2, 3)
                    .map(n -> n * 2);

            assertThat(seq.toList()).containsExactly(2, 4, 6);
        }

        @Test
        @DisplayName("map() 惰性求值")
        void testMapIsLazy() {
            List<Integer> evaluated = new ArrayList<>();
            Sequence<Integer> seq = Sequence.of(1, 2, 3)
                    .map(n -> {
                        evaluated.add(n);
                        return n * 2;
                    });

            assertThat(evaluated).isEmpty();
            seq.toList();
            assertThat(evaluated).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("flatMap() 测试")
    class FlatMapTests {

        @Test
        @DisplayName("flatMap() 展平序列")
        void testFlatMap() {
            Sequence<Integer> seq = Sequence.of(1, 2, 3)
                    .flatMap(n -> Sequence.of(n, n * 10));

            assertThat(seq.toList()).containsExactly(1, 10, 2, 20, 3, 30);
        }

        @Test
        @DisplayName("flatMap() 惰性求值")
        void testFlatMapIsLazy() {
            List<Integer> evaluated = new ArrayList<>();
            Sequence<Integer> seq = Sequence.of(1, 2, 3)
                    .flatMap(n -> {
                        evaluated.add(n);
                        return Sequence.of(n);
                    });

            assertThat(evaluated).isEmpty();
            seq.toList();
            assertThat(evaluated).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("filter() 测试")
    class FilterTests {

        @Test
        @DisplayName("filter() 过滤元素")
        void testFilter() {
            Sequence<Integer> seq = Sequence.of(1, 2, 3, 4, 5)
                    .filter(n -> n > 2);

            assertThat(seq.toList()).containsExactly(3, 4, 5);
        }

        @Test
        @DisplayName("filterNot() 过滤不匹配的元素")
        void testFilterNot() {
            Sequence<Integer> seq = Sequence.of(1, 2, 3, 4, 5)
                    .filterNot(n -> n > 2);

            assertThat(seq.toList()).containsExactly(1, 2);
        }
    }

    @Nested
    @DisplayName("take() 测试")
    class TakeTests {

        @Test
        @DisplayName("take() 取前 n 个元素")
        void testTake() {
            Sequence<Integer> seq = Sequence.of(1, 2, 3, 4, 5)
                    .take(3);

            assertThat(seq.toList()).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("take(0) 返回空序列")
        void testTakeZero() {
            Sequence<Integer> seq = Sequence.of(1, 2, 3)
                    .take(0);

            assertThat(seq.toList()).isEmpty();
        }

        @Test
        @DisplayName("take() 超出长度时返回全部")
        void testTakeMoreThanLength() {
            Sequence<Integer> seq = Sequence.of(1, 2, 3)
                    .take(10);

            assertThat(seq.toList()).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("takeWhile() 测试")
    class TakeWhileTests {

        @Test
        @DisplayName("takeWhile() 取满足条件的元素")
        void testTakeWhile() {
            Sequence<Integer> seq = Sequence.of(1, 2, 3, 4, 5, 1, 2)
                    .takeWhile(n -> n < 4);

            assertThat(seq.toList()).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("takeWhile() 首个不满足时返回空")
        void testTakeWhileFirstFails() {
            Sequence<Integer> seq = Sequence.of(5, 1, 2, 3)
                    .takeWhile(n -> n < 4);

            assertThat(seq.toList()).isEmpty();
        }
    }

    @Nested
    @DisplayName("drop() 测试")
    class DropTests {

        @Test
        @DisplayName("drop() 丢弃前 n 个元素")
        void testDrop() {
            Sequence<Integer> seq = Sequence.of(1, 2, 3, 4, 5)
                    .drop(2);

            assertThat(seq.toList()).containsExactly(3, 4, 5);
        }

        @Test
        @DisplayName("drop(0) 返回原序列")
        void testDropZero() {
            Sequence<Integer> seq = Sequence.of(1, 2, 3)
                    .drop(0);

            assertThat(seq.toList()).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("drop() 超出长度时返回空")
        void testDropMoreThanLength() {
            Sequence<Integer> seq = Sequence.of(1, 2, 3)
                    .drop(10);

            assertThat(seq.toList()).isEmpty();
        }
    }

    @Nested
    @DisplayName("dropWhile() 测试")
    class DropWhileTests {

        @Test
        @DisplayName("dropWhile() 丢弃满足条件的元素")
        void testDropWhile() {
            Sequence<Integer> seq = Sequence.of(1, 2, 3, 4, 5, 1, 2)
                    .dropWhile(n -> n < 4);

            assertThat(seq.toList()).containsExactly(4, 5, 1, 2);
        }

        @Test
        @DisplayName("dropWhile() 全部满足时返回空")
        void testDropWhileAllMatch() {
            Sequence<Integer> seq = Sequence.of(1, 2, 3)
                    .dropWhile(n -> n < 10);

            assertThat(seq.toList()).isEmpty();
        }
    }

    @Nested
    @DisplayName("distinct() 测试")
    class DistinctTests {

        @Test
        @DisplayName("distinct() 移除重复元素")
        void testDistinct() {
            Sequence<Integer> seq = Sequence.of(1, 2, 2, 3, 1, 3, 4)
                    .distinct();

            assertThat(seq.toList()).containsExactly(1, 2, 3, 4);
        }
    }

    @Nested
    @DisplayName("sorted() 测试")
    class SortedTests {

        @Test
        @DisplayName("sorted() 自然顺序排序")
        void testSorted() {
            Sequence<Integer> seq = Sequence.of(3, 1, 4, 1, 5)
                    .sorted();

            assertThat(seq.toList()).containsExactly(1, 1, 3, 4, 5);
        }

        @Test
        @DisplayName("sorted(Comparator) 自定义排序")
        void testSortedWithComparator() {
            Sequence<Integer> seq = Sequence.of(3, 1, 4, 1, 5)
                    .sorted(Comparator.reverseOrder());

            assertThat(seq.toList()).containsExactly(5, 4, 3, 1, 1);
        }
    }

    @Nested
    @DisplayName("zip() 测试")
    class ZipTests {

        @Test
        @DisplayName("zip() 合并两个序列")
        void testZip() {
            Sequence<String> seq = Sequence.of(1, 2, 3)
                    .zip(Sequence.of("a", "b", "c"), (n, s) -> n + s);

            assertThat(seq.toList()).containsExactly("1a", "2b", "3c");
        }

        @Test
        @DisplayName("zip() 长度不同时以短的为准")
        void testZipDifferentLengths() {
            Sequence<String> seq = Sequence.of(1, 2, 3, 4, 5)
                    .zip(Sequence.of("a", "b"), (n, s) -> n + s);

            assertThat(seq.toList()).containsExactly("1a", "2b");
        }
    }

    @Nested
    @DisplayName("zipWithIndex() 测试")
    class ZipWithIndexTests {

        @Test
        @DisplayName("zipWithIndex() 添加索引")
        void testZipWithIndex() {
            List<Sequence.IndexedValue<String>> result = Sequence.of("a", "b", "c")
                    .zipWithIndex()
                    .toList();

            assertThat(result).containsExactly(
                    new Sequence.IndexedValue<>(0, "a"),
                    new Sequence.IndexedValue<>(1, "b"),
                    new Sequence.IndexedValue<>(2, "c")
            );
        }
    }

    @Nested
    @DisplayName("fold() 测试")
    class FoldTests {

        @Test
        @DisplayName("fold() 折叠元素")
        void testFold() {
            Integer result = Sequence.of(1, 2, 3, 4, 5)
                    .fold(0, Integer::sum);

            assertThat(result).isEqualTo(15);
        }

        @Test
        @DisplayName("fold() 空序列返回初始值")
        void testFoldEmpty() {
            Integer result = Sequence.<Integer>empty()
                    .fold(0, Integer::sum);

            assertThat(result).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("reduce() 测试")
    class ReduceTests {

        @Test
        @DisplayName("reduce() 归约元素")
        void testReduce() {
            Optional<Integer> result = Sequence.of(1, 2, 3, 4, 5)
                    .reduce(Integer::sum);

            assertThat(result).contains(15);
        }

        @Test
        @DisplayName("reduce() 空序列返回空 Optional")
        void testReduceEmpty() {
            Optional<Integer> result = Sequence.<Integer>empty()
                    .reduce(Integer::sum);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("toList/toSet 测试")
    class ToCollectionTests {

        @Test
        @DisplayName("toList() 收集为列表")
        void testToList() {
            List<Integer> list = Sequence.of(1, 2, 3).toList();

            assertThat(list).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("toSet() 收集为集合")
        void testToSet() {
            Set<Integer> set = Sequence.of(1, 2, 2, 3, 3).toSet();

            assertThat(set).containsExactlyInAnyOrder(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("collect() 测试")
    class CollectTests {

        @Test
        @DisplayName("collect() 使用收集器")
        void testCollect() {
            String result = Sequence.of("a", "b", "c")
                    .collect(Collectors.joining(", "));

            assertThat(result).isEqualTo("a, b, c");
        }
    }

    @Nested
    @DisplayName("find() 测试")
    class FindTests {

        @Test
        @DisplayName("find() 找到匹配元素")
        void testFind() {
            Optional<Integer> result = Sequence.of(1, 2, 3, 4, 5)
                    .find(n -> n > 3);

            assertThat(result).contains(4);
        }

        @Test
        @DisplayName("find() 未找到返回空 Optional")
        void testFindNotFound() {
            Optional<Integer> result = Sequence.of(1, 2, 3)
                    .find(n -> n > 10);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("first/last 测试")
    class FirstLastTests {

        @Test
        @DisplayName("first() 返回第一个元素")
        void testFirst() {
            Optional<Integer> result = Sequence.of(1, 2, 3).first();

            assertThat(result).contains(1);
        }

        @Test
        @DisplayName("first() 空序列返回空 Optional")
        void testFirstEmpty() {
            Optional<Integer> result = Sequence.<Integer>empty().first();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("last() 返回最后一个元素")
        void testLast() {
            Optional<Integer> result = Sequence.of(1, 2, 3).last();

            assertThat(result).contains(3);
        }

        @Test
        @DisplayName("last() 空序列返回空 Optional")
        void testLastEmpty() {
            Optional<Integer> result = Sequence.<Integer>empty().last();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("any/all/none 测试")
    class AnyAllNoneTests {

        @Test
        @DisplayName("any() 有匹配时返回 true")
        void testAnyTrue() {
            boolean result = Sequence.of(1, 2, 3, 4, 5)
                    .any(n -> n > 3);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("any() 无匹配时返回 false")
        void testAnyFalse() {
            boolean result = Sequence.of(1, 2, 3)
                    .any(n -> n > 10);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("all() 全部匹配时返回 true")
        void testAllTrue() {
            boolean result = Sequence.of(2, 4, 6)
                    .all(n -> n % 2 == 0);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("all() 有不匹配时返回 false")
        void testAllFalse() {
            boolean result = Sequence.of(2, 3, 4)
                    .all(n -> n % 2 == 0);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("none() 全不匹配时返回 true")
        void testNoneTrue() {
            boolean result = Sequence.of(1, 3, 5)
                    .none(n -> n % 2 == 0);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("none() 有匹配时返回 false")
        void testNoneFalse() {
            boolean result = Sequence.of(1, 2, 3)
                    .none(n -> n % 2 == 0);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("count() 测试")
    class CountTests {

        @Test
        @DisplayName("count() 计数元素")
        void testCount() {
            long count = Sequence.of(1, 2, 3, 4, 5).count();

            assertThat(count).isEqualTo(5);
        }

        @Test
        @DisplayName("count() 空序列返回 0")
        void testCountEmpty() {
            long count = Sequence.empty().count();

            assertThat(count).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("forEach() 测试")
    class ForEachTests {

        @Test
        @DisplayName("forEach() 对每个元素执行操作")
        void testForEach() {
            List<Integer> collected = new ArrayList<>();
            Sequence.of(1, 2, 3).forEach(collected::add);

            assertThat(collected).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("toStream() 测试")
    class ToStreamTests {

        @Test
        @DisplayName("toStream() 转换为 Stream")
        void testToStream() {
            List<Integer> result = Sequence.of(1, 2, 3)
                    .toStream()
                    .toList();

            assertThat(result).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("iterator() 测试")
    class IteratorTests {

        @Test
        @DisplayName("iterator() 返回迭代器")
        void testIterator() {
            List<Integer> collected = new ArrayList<>();
            for (Integer n : Sequence.of(1, 2, 3)) {
                collected.add(n);
            }

            assertThat(collected).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("序列可多次遍历")
        void testReusable() {
            Sequence<Integer> seq = Sequence.of(1, 2, 3);

            List<Integer> first = seq.toList();
            List<Integer> second = seq.toList();

            assertThat(first).isEqualTo(second);
        }
    }

    @Nested
    @DisplayName("IndexedValue 测试")
    class IndexedValueTests {

        @Test
        @DisplayName("IndexedValue record 测试")
        void testIndexedValue() {
            Sequence.IndexedValue<String> iv = new Sequence.IndexedValue<>(0, "test");

            assertThat(iv.index()).isEqualTo(0);
            assertThat(iv.value()).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("链式操作测试")
    class ChainedOperationsTests {

        @Test
        @DisplayName("复杂链式操作")
        void testComplexChain() {
            List<Integer> result = Sequence.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                    .filter(n -> n % 2 == 0)
                    .map(n -> n * 2)
                    .take(3)
                    .toList();

            assertThat(result).containsExactly(4, 8, 12);
        }
    }
}
