package cloud.opencode.base.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Ordering 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("Ordering 测试")
class OrderingTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("natural - 自然排序")
        void testNatural() {
            Ordering<Integer> ordering = Ordering.natural();

            assertThat(ordering.compare(1, 2)).isLessThan(0);
            assertThat(ordering.compare(2, 1)).isGreaterThan(0);
            assertThat(ordering.compare(1, 1)).isZero();
        }

        @Test
        @DisplayName("from - 从 Comparator 创建")
        void testFrom() {
            Ordering<String> ordering = Ordering.from(String.CASE_INSENSITIVE_ORDER);

            assertThat(ordering.compare("a", "A")).isZero();
            assertThat(ordering.compare("a", "B")).isLessThan(0);
        }

        @Test
        @DisplayName("from - Ordering 直接返回")
        void testFromOrdering() {
            Ordering<Integer> original = Ordering.natural();
            Ordering<Integer> result = Ordering.from(original);

            assertThat(result).isSameAs(original);
        }

        @Test
        @DisplayName("explicit - 显式排序")
        void testExplicit() {
            Ordering<String> ordering = Ordering.explicit("c", "a", "b");

            assertThat(ordering.compare("c", "a")).isLessThan(0);
            assertThat(ordering.compare("a", "b")).isLessThan(0);
            assertThat(ordering.compare("c", "b")).isLessThan(0);
        }

        @Test
        @DisplayName("explicit - 未知元素抛异常")
        void testExplicitUnknown() {
            Ordering<String> ordering = Ordering.explicit("a", "b");

            assertThatThrownBy(() -> ordering.compare("a", "c"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("allEqual - 全相等排序")
        void testAllEqual() {
            Ordering<String> ordering = Ordering.allEqual();

            assertThat(ordering.compare("a", "z")).isZero();
            assertThat(ordering.compare("", "abc")).isZero();
        }

        @Test
        @DisplayName("arbitrary - 任意排序")
        void testArbitrary() {
            Ordering<Object> ordering = Ordering.arbitrary();
            Object a = new Object();
            Object b = new Object();

            // 任意但一致
            int result1 = ordering.compare(a, b);
            int result2 = ordering.compare(a, b);
            assertThat(result1).isEqualTo(result2);

            // 自反性
            assertThat(ordering.compare(a, a)).isZero();
        }

        @Test
        @DisplayName("usingToString - 使用 toString 排序")
        void testUsingToString() {
            Ordering<Object> ordering = Ordering.usingToString();

            assertThat(ordering.compare("abc", "abd")).isLessThan(0);
            assertThat(ordering.compare(123, 124)).isLessThan(0);
        }
    }

    @Nested
    @DisplayName("修饰方法测试")
    class ModifierTests {

        @Test
        @DisplayName("reverse - 反转排序")
        void testReverse() {
            Ordering<Integer> ordering = Ordering.<Integer>natural().reverse();

            assertThat(ordering.compare(1, 2)).isGreaterThan(0);
            assertThat(ordering.compare(2, 1)).isLessThan(0);
        }

        @Test
        @DisplayName("reverse - 双重反转恢复原始")
        void testDoubleReverse() {
            Ordering<Integer> natural = Ordering.natural();
            Ordering<Integer> doubleReversed = natural.reverse().reverse();

            assertThat(doubleReversed).isSameAs(natural);
        }

        @Test
        @DisplayName("nullsFirst - null 优先")
        void testNullsFirst() {
            Ordering<String> ordering = Ordering.<String>natural().nullsFirst();

            assertThat(ordering.compare(null, "a")).isLessThan(0);
            assertThat(ordering.compare("a", null)).isGreaterThan(0);
            assertThat(ordering.compare(null, null)).isZero();
            assertThat(ordering.compare("a", "b")).isLessThan(0);
        }

        @Test
        @DisplayName("nullsLast - null 最后")
        void testNullsLast() {
            Ordering<String> ordering = Ordering.<String>natural().nullsLast();

            assertThat(ordering.compare(null, "a")).isGreaterThan(0);
            assertThat(ordering.compare("a", null)).isLessThan(0);
            assertThat(ordering.compare(null, null)).isZero();
        }

        @Test
        @DisplayName("compound - 复合排序")
        void testCompound() {
            Ordering<String> byLength = Ordering.<Integer>natural().onResultOf(String::length);
            Ordering<String> compound = byLength.compound(Ordering.natural());

            assertThat(compound.compare("ab", "xyz")).isLessThan(0); // 长度不同
            assertThat(compound.compare("ab", "cd")).isLessThan(0);  // 长度相同，按字母
            assertThat(compound.compare("cd", "ab")).isGreaterThan(0);
        }

        @Test
        @DisplayName("onResultOf - 基于函数结果排序")
        void testOnResultOf() {
            Ordering<String> ordering = Ordering.<Integer>natural().onResultOf(String::length);

            assertThat(ordering.compare("a", "abc")).isLessThan(0);
            assertThat(ordering.compare("abc", "a")).isGreaterThan(0);
            assertThat(ordering.compare("ab", "cd")).isZero();
        }
    }

    @Nested
    @DisplayName("操作方法测试")
    class OperationTests {

        @Test
        @DisplayName("sortedCopy - 排序副本")
        void testSortedCopy() {
            Ordering<Integer> ordering = Ordering.natural();
            List<Integer> list = Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6);

            List<Integer> sorted = ordering.sortedCopy(list);

            assertThat(sorted).containsExactly(1, 1, 2, 3, 4, 5, 6, 9);
            assertThat(list).containsExactly(3, 1, 4, 1, 5, 9, 2, 6); // 原列表不变
        }

        @Test
        @DisplayName("immutableSortedCopy - 不可变排序副本")
        void testImmutableSortedCopy() {
            Ordering<Integer> ordering = Ordering.natural();
            List<Integer> list = Arrays.asList(3, 1, 2);

            ImmutableList<Integer> sorted = ordering.immutableSortedCopy(list);

            assertThat(sorted).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("isOrdered - 检查是否有序")
        void testIsOrdered() {
            Ordering<Integer> ordering = Ordering.natural();

            assertThat(ordering.isOrdered(List.of(1, 2, 2, 3))).isTrue();
            assertThat(ordering.isOrdered(List.of(1, 3, 2))).isFalse();
            assertThat(ordering.isOrdered(List.of())).isTrue();
            assertThat(ordering.isOrdered(List.of(1))).isTrue();
        }

        @Test
        @DisplayName("isStrictlyOrdered - 检查是否严格有序")
        void testIsStrictlyOrdered() {
            Ordering<Integer> ordering = Ordering.natural();

            assertThat(ordering.isStrictlyOrdered(List.of(1, 2, 3))).isTrue();
            assertThat(ordering.isStrictlyOrdered(List.of(1, 2, 2, 3))).isFalse();
            assertThat(ordering.isStrictlyOrdered(List.of())).isTrue();
        }
    }

    @Nested
    @DisplayName("Min/Max 测试")
    class MinMaxTests {

        @Test
        @DisplayName("min - 两个值")
        void testMinTwo() {
            Ordering<Integer> ordering = Ordering.natural();

            assertThat(ordering.min(3, 1)).isEqualTo(1);
            assertThat(ordering.min(1, 3)).isEqualTo(1);
            assertThat(ordering.min(2, 2)).isEqualTo(2);
        }

        @Test
        @DisplayName("min - 多个值")
        void testMinMultiple() {
            Ordering<Integer> ordering = Ordering.natural();

            assertThat(ordering.min(3, 1, 4, 1, 5)).isEqualTo(1);
        }

        @Test
        @DisplayName("min - Iterable")
        void testMinIterable() {
            Ordering<Integer> ordering = Ordering.natural();

            assertThat(ordering.min(List.of(3, 1, 4, 1, 5))).isEqualTo(1);
        }

        @Test
        @DisplayName("min - 空集合抛异常")
        void testMinEmpty() {
            Ordering<Integer> ordering = Ordering.natural();

            assertThatThrownBy(() -> ordering.min(List.of()))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("max - 两个值")
        void testMaxTwo() {
            Ordering<Integer> ordering = Ordering.natural();

            assertThat(ordering.max(3, 1)).isEqualTo(3);
            assertThat(ordering.max(1, 3)).isEqualTo(3);
        }

        @Test
        @DisplayName("max - 多个值")
        void testMaxMultiple() {
            Ordering<Integer> ordering = Ordering.natural();

            assertThat(ordering.max(3, 1, 4, 1, 5)).isEqualTo(5);
        }

        @Test
        @DisplayName("max - Iterable")
        void testMaxIterable() {
            Ordering<Integer> ordering = Ordering.natural();

            assertThat(ordering.max(List.of(3, 1, 4, 1, 5))).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("leastOf/greatestOf 测试")
    class LeastGreatestTests {

        @Test
        @DisplayName("leastOf - 最小 k 个元素")
        void testLeastOf() {
            Ordering<Integer> ordering = Ordering.natural();

            assertThat(ordering.leastOf(List.of(5, 3, 1, 4, 2), 3))
                    .containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("leastOf - k 大于列表大小")
        void testLeastOfLargerK() {
            Ordering<Integer> ordering = Ordering.natural();

            assertThat(ordering.leastOf(List.of(3, 1, 2), 10))
                    .containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("leastOf - k 为 0")
        void testLeastOfZero() {
            Ordering<Integer> ordering = Ordering.natural();

            assertThat(ordering.leastOf(List.of(3, 1, 2), 0)).isEmpty();
        }

        @Test
        @DisplayName("greatestOf - 最大 k 个元素")
        void testGreatestOf() {
            Ordering<Integer> ordering = Ordering.natural();

            assertThat(ordering.greatestOf(List.of(5, 3, 1, 4, 2), 3))
                    .containsExactly(5, 4, 3);
        }
    }

    @Nested
    @DisplayName("binarySearch 测试")
    class BinarySearchTests {

        @Test
        @DisplayName("binarySearch - 找到元素")
        void testBinarySearchFound() {
            Ordering<Integer> ordering = Ordering.natural();
            List<Integer> list = List.of(1, 2, 3, 4, 5);

            assertThat(ordering.binarySearch(list, 3)).isEqualTo(2);
        }

        @Test
        @DisplayName("binarySearch - 未找到元素")
        void testBinarySearchNotFound() {
            Ordering<Integer> ordering = Ordering.natural();
            List<Integer> list = List.of(1, 2, 4, 5);

            int result = ordering.binarySearch(list, 3);
            assertThat(result).isLessThan(0);
            assertThat(-result - 1).isEqualTo(2); // 插入点
        }
    }
}
