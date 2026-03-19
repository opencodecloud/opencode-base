package cloud.opencode.base.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * ComparatorUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("ComparatorUtil 测试")
class ComparatorUtilTest {

    @Nested
    @DisplayName("空值处理测试")
    class NullHandlingTests {

        @Test
        @DisplayName("nullsFirst - null 优先")
        void testNullsFirst() {
            Comparator<String> comparator = ComparatorUtil.nullsFirst(Comparator.naturalOrder());

            List<String> list = new ArrayList<>(Arrays.asList("b", null, "a", null, "c"));
            list.sort(comparator);

            assertThat(list).containsExactly(null, null, "a", "b", "c");
        }

        @Test
        @DisplayName("nullsLast - null 最后")
        void testNullsLast() {
            Comparator<String> comparator = ComparatorUtil.nullsLast(Comparator.naturalOrder());

            List<String> list = new ArrayList<>(Arrays.asList("b", null, "a", null, "c"));
            list.sort(comparator);

            assertThat(list).containsExactly("a", "b", "c", null, null);
        }
    }

    @Nested
    @DisplayName("检查方法测试")
    class CheckMethodTests {

        @Test
        @DisplayName("isInOrder - 有序")
        void testIsInOrder() {
            List<Integer> ordered = List.of(1, 2, 2, 3, 4);
            List<Integer> unordered = List.of(1, 3, 2, 4);

            assertThat(ComparatorUtil.isInOrder(ordered, Comparator.naturalOrder())).isTrue();
            assertThat(ComparatorUtil.isInOrder(unordered, Comparator.naturalOrder())).isFalse();
        }

        @Test
        @DisplayName("isInOrder - null 输入")
        void testIsInOrderNull() {
            assertThat(ComparatorUtil.isInOrder((Iterable<Integer>) null, Comparator.naturalOrder())).isTrue();
            assertThat(ComparatorUtil.isInOrder(List.of(1, 2), (Comparator<Integer>) null)).isTrue();
        }

        @Test
        @DisplayName("isInOrder - 空/单元素")
        void testIsInOrderEmptyOrSingle() {
            assertThat(ComparatorUtil.isInOrder(List.<Integer>of(), Comparator.naturalOrder())).isTrue();
            assertThat(ComparatorUtil.isInOrder(List.of(1), Comparator.naturalOrder())).isTrue();
        }

        @Test
        @DisplayName("isInStrictOrder - 严格有序")
        void testIsInStrictOrder() {
            List<Integer> strictlyOrdered = List.of(1, 2, 3, 4);
            List<Integer> withDuplicates = List.of(1, 2, 2, 3);
            List<Integer> unordered = List.of(1, 3, 2);

            assertThat(ComparatorUtil.isInStrictOrder(strictlyOrdered, Comparator.naturalOrder())).isTrue();
            assertThat(ComparatorUtil.isInStrictOrder(withDuplicates, Comparator.naturalOrder())).isFalse();
            assertThat(ComparatorUtil.isInStrictOrder(unordered, Comparator.naturalOrder())).isFalse();
        }

        @Test
        @DisplayName("isInStrictOrder - null 输入")
        void testIsInStrictOrderNull() {
            assertThat(ComparatorUtil.isInStrictOrder((Iterable<Integer>) null, Comparator.naturalOrder())).isTrue();
            assertThat(ComparatorUtil.isInStrictOrder(List.of(1, 2), (Comparator<Integer>) null)).isTrue();
        }

        @Test
        @DisplayName("isInStrictOrder - 空/单元素")
        void testIsInStrictOrderEmptyOrSingle() {
            assertThat(ComparatorUtil.isInStrictOrder(List.<Integer>of(), Comparator.naturalOrder())).isTrue();
            assertThat(ComparatorUtil.isInStrictOrder(List.of(1), Comparator.naturalOrder())).isTrue();
        }
    }

    @Nested
    @DisplayName("字典序测试")
    class LexicographicalTests {

        @Test
        @DisplayName("lexicographical - 自然顺序")
        void testLexicographicalNatural() {
            Comparator<Iterable<String>> comparator = ComparatorUtil.lexicographical();

            assertThat(comparator.compare(List.of("a", "b"), List.of("a", "c"))).isLessThan(0);
            assertThat(comparator.compare(List.of("a", "b"), List.of("a", "b"))).isEqualTo(0);
            assertThat(comparator.compare(List.of("a", "c"), List.of("a", "b"))).isGreaterThan(0);
        }

        @Test
        @DisplayName("lexicographical - 长度不同")
        void testLexicographicalDifferentLengths() {
            Comparator<Iterable<String>> comparator = ComparatorUtil.lexicographical();

            assertThat(comparator.compare(List.of("a"), List.of("a", "b"))).isLessThan(0);
            assertThat(comparator.compare(List.of("a", "b"), List.of("a"))).isGreaterThan(0);
        }

        @Test
        @DisplayName("lexicographical - null 处理")
        void testLexicographicalNull() {
            Comparator<Iterable<String>> comparator = ComparatorUtil.lexicographical();

            assertThat(comparator.compare(null, List.of("a"))).isLessThan(0);
            assertThat(comparator.compare(List.of("a"), null)).isGreaterThan(0);
            assertThat(comparator.compare(null, null)).isEqualTo(0);
        }

        @Test
        @DisplayName("lexicographical - 相同引用")
        void testLexicographicalSameReference() {
            Comparator<Iterable<String>> comparator = ComparatorUtil.lexicographical();

            List<String> list = List.of("a", "b");
            assertThat(comparator.compare(list, list)).isEqualTo(0);
        }

        @Test
        @DisplayName("lexicographical - 自定义比较器")
        void testLexicographicalCustom() {
            Comparator<Iterable<String>> comparator = ComparatorUtil.lexicographical(
                    Comparator.comparingInt(String::length));

            assertThat(comparator.compare(List.of("a", "bb"), List.of("a", "ccc"))).isLessThan(0);
        }
    }

    @Nested
    @DisplayName("最值操作测试")
    class MinMaxTests {

        @Test
        @DisplayName("min - 最小值")
        void testMin() {
            assertThat(ComparatorUtil.min("a", "b", Comparator.naturalOrder())).isEqualTo("a");
            assertThat(ComparatorUtil.min("b", "a", Comparator.naturalOrder())).isEqualTo("a");
            assertThat(ComparatorUtil.min("a", "a", Comparator.naturalOrder())).isEqualTo("a");
        }

        @Test
        @DisplayName("min - null comparator 抛异常")
        void testMinNullComparator() {
            assertThatThrownBy(() -> ComparatorUtil.min("a", "b", null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("max - 最大值")
        void testMax() {
            assertThat(ComparatorUtil.max("a", "b", Comparator.naturalOrder())).isEqualTo("b");
            assertThat(ComparatorUtil.max("b", "a", Comparator.naturalOrder())).isEqualTo("b");
            assertThat(ComparatorUtil.max("a", "a", Comparator.naturalOrder())).isEqualTo("a");
        }

        @Test
        @DisplayName("max - null comparator 抛异常")
        void testMaxNullComparator() {
            assertThatThrownBy(() -> ComparatorUtil.max("a", "b", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Top K 收集器测试")
    class TopKCollectorTests {

        @Test
        @DisplayName("least - 最小 K 个")
        void testLeast() {
            List<Integer> result = Stream.of(5, 3, 8, 1, 9, 2, 7)
                    .collect(ComparatorUtil.least(3, Comparator.naturalOrder()));

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("least - K <= 0 返回空")
        void testLeastZeroOrNegative() {
            List<Integer> result = Stream.of(1, 2, 3)
                    .collect(ComparatorUtil.least(0, Comparator.naturalOrder()));
            assertThat(result).isEmpty();

            List<Integer> result2 = Stream.of(1, 2, 3)
                    .collect(ComparatorUtil.least(-1, Comparator.naturalOrder()));
            assertThat(result2).isEmpty();
        }

        @Test
        @DisplayName("least - K 大于元素数")
        void testLeastKGreaterThanSize() {
            List<Integer> result = Stream.of(3, 1, 2)
                    .collect(ComparatorUtil.least(10, Comparator.naturalOrder()));

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("least - null comparator 抛异常")
        void testLeastNullComparator() {
            assertThatThrownBy(() -> ComparatorUtil.least(3, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("greatest - 最大 K 个")
        void testGreatest() {
            List<Integer> result = Stream.of(5, 3, 8, 1, 9, 2, 7)
                    .collect(ComparatorUtil.greatest(3, Comparator.naturalOrder()));

            assertThat(result).containsExactly(9, 8, 7);
        }

        @Test
        @DisplayName("greatest - K <= 0 返回空")
        void testGreatestZeroOrNegative() {
            List<Integer> result = Stream.of(1, 2, 3)
                    .collect(ComparatorUtil.greatest(0, Comparator.naturalOrder()));
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("greatest - K 大于元素数")
        void testGreatestKGreaterThanSize() {
            List<Integer> result = Stream.of(3, 1, 2)
                    .collect(ComparatorUtil.greatest(10, Comparator.naturalOrder()));

            assertThat(result).containsExactly(3, 2, 1);
        }

        @Test
        @DisplayName("greatest - null comparator 抛异常")
        void testGreatestNullComparator() {
            assertThatThrownBy(() -> ComparatorUtil.greatest(3, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("空处理比较器测试")
    class EmptiesHandlingTests {

        @Test
        @DisplayName("emptiesFirst - 空优先")
        void testEmptiesFirst() {
            Comparator<String> comparator = ComparatorUtil.emptiesFirst(Comparator.naturalOrder());

            List<String> list = new ArrayList<>(Arrays.asList("b", null, "a"));
            list.sort(comparator);

            assertThat(list.getFirst()).isNull();
        }

        @Test
        @DisplayName("emptiesLast - 空最后")
        void testEmptiesLast() {
            Comparator<String> comparator = ComparatorUtil.emptiesLast(Comparator.naturalOrder());

            List<String> list = new ArrayList<>(Arrays.asList("b", null, "a"));
            list.sort(comparator);

            assertThat(list.getLast()).isNull();
        }
    }
}
