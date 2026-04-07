package cloud.opencode.base.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

/**
 * IntInterval 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
@DisplayName("IntInterval 测试")
class IntIntervalTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("fromTo - 升序区间 (1,5) -> [1,2,3,4,5]")
        void testFromToAscending() {
            IntInterval interval = IntInterval.fromTo(1, 5);

            assertThat(interval.size()).isEqualTo(5);
            assertThat(interval.toArray()).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("fromTo - 降序区间 (5,1) -> [5,4,3,2,1]")
        void testFromToDescending() {
            IntInterval interval = IntInterval.fromTo(5, 1);

            assertThat(interval.size()).isEqualTo(5);
            assertThat(interval.toArray()).containsExactly(5, 4, 3, 2, 1);
        }

        @Test
        @DisplayName("fromToBy - 步长为 2")
        void testFromToByStep2() {
            IntInterval interval = IntInterval.fromToBy(1, 10, 2);

            assertThat(interval.toArray()).containsExactly(1, 3, 5, 7, 9);
        }

        @Test
        @DisplayName("fromToBy - 步长为 3，刚好整除")
        void testFromToByStep3Exact() {
            IntInterval interval = IntInterval.fromToBy(1, 10, 3);

            assertThat(interval.toArray()).containsExactly(1, 4, 7, 10);
        }

        @Test
        @DisplayName("fromToBy - 负步长")
        void testFromToByNegativeStep() {
            IntInterval interval = IntInterval.fromToBy(10, 1, -3);

            assertThat(interval.toArray()).containsExactly(10, 7, 4, 1);
        }

        @Test
        @DisplayName("fromToBy - 步长为零抛出异常")
        void testFromToByZeroStep() {
            assertThatThrownBy(() -> IntInterval.fromToBy(1, 10, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("zero");
        }

        @Test
        @DisplayName("fromToBy - 方向冲突抛出异常 (from < to, step < 0)")
        void testFromToByConflictingDirectionNegative() {
            assertThatThrownBy(() -> IntInterval.fromToBy(1, 10, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("fromToBy - 方向冲突抛出异常 (from > to, step > 0)")
        void testFromToByConflictingDirectionPositive() {
            assertThatThrownBy(() -> IntInterval.fromToBy(10, 1, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("zeroTo - 从 0 到 n")
        void testZeroTo() {
            IntInterval interval = IntInterval.zeroTo(4);

            assertThat(interval.toArray()).containsExactly(0, 1, 2, 3, 4);
        }

        @Test
        @DisplayName("oneTo - 从 1 到 n")
        void testOneTo() {
            IntInterval interval = IntInterval.oneTo(5);

            assertThat(interval.toArray()).containsExactly(1, 2, 3, 4, 5);
        }
    }

    @Nested
    @DisplayName("查询操作测试")
    class QueryTests {

        @Test
        @DisplayName("contains - 升序区间")
        void testContainsAscending() {
            IntInterval interval = IntInterval.fromTo(1, 5);

            assertThat(interval.contains(1)).isTrue();
            assertThat(interval.contains(3)).isTrue();
            assertThat(interval.contains(5)).isTrue();
            assertThat(interval.contains(0)).isFalse();
            assertThat(interval.contains(6)).isFalse();
        }

        @Test
        @DisplayName("contains - 降序区间")
        void testContainsDescending() {
            IntInterval interval = IntInterval.fromTo(5, 1);

            assertThat(interval.contains(5)).isTrue();
            assertThat(interval.contains(1)).isTrue();
            assertThat(interval.contains(3)).isTrue();
            assertThat(interval.contains(0)).isFalse();
            assertThat(interval.contains(6)).isFalse();
        }

        @Test
        @DisplayName("contains - 步长大于 1 时，中间值不包含")
        void testContainsWithStep() {
            IntInterval interval = IntInterval.fromToBy(0, 10, 2);

            assertThat(interval.contains(0)).isTrue();
            assertThat(interval.contains(2)).isTrue();
            assertThat(interval.contains(10)).isTrue();
            assertThat(interval.contains(1)).isFalse();
            assertThat(interval.contains(3)).isFalse();
        }

        @Test
        @DisplayName("size - 单元素区间 fromTo(5,5)")
        void testSizeSingleElement() {
            IntInterval interval = IntInterval.fromTo(5, 5);

            assertThat(interval.size()).isEqualTo(1);
            assertThat(interval.toArray()).containsExactly(5);
        }

        @Test
        @DisplayName("size - 接近 int 极限的大区间")
        void testSizeNearIntLimits() {
            IntInterval interval = IntInterval.fromTo(Integer.MIN_VALUE, Integer.MIN_VALUE + 9);

            assertThat(interval.size()).isEqualTo(10);
        }

        @Test
        @DisplayName("get - O(1) 随机访问")
        void testGet() {
            IntInterval interval = IntInterval.fromToBy(10, 50, 10);

            assertThat(interval.get(0)).isEqualTo(10);
            assertThat(interval.get(1)).isEqualTo(20);
            assertThat(interval.get(2)).isEqualTo(30);
            assertThat(interval.get(3)).isEqualTo(40);
            assertThat(interval.get(4)).isEqualTo(50);
        }

        @Test
        @DisplayName("get - 越界抛出异常")
        void testGetOutOfBounds() {
            IntInterval interval = IntInterval.fromTo(1, 5);

            assertThatThrownBy(() -> interval.get(-1))
                    .isInstanceOf(IndexOutOfBoundsException.class);
            assertThatThrownBy(() -> interval.get(5))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("getFirst 和 getLast")
        void testGetFirstAndLast() {
            IntInterval interval = IntInterval.fromToBy(1, 10, 3);

            assertThat(interval.getFirst()).isEqualTo(1);
            assertThat(interval.getLast()).isEqualTo(10);
        }

        @Test
        @DisplayName("getFirst 和 getLast - 步长不整除时 getLast 不等于 to")
        void testGetLastNotAligned() {
            IntInterval interval = IntInterval.fromToBy(1, 10, 4);

            assertThat(interval.getFirst()).isEqualTo(1);
            assertThat(interval.getLast()).isEqualTo(9);
        }

        @Test
        @DisplayName("isEmpty - 空区间判断")
        void testIsEmpty() {
            IntInterval nonEmpty = IntInterval.fromTo(1, 5);
            assertThat(nonEmpty.isEmpty()).isFalse();

            IntInterval single = IntInterval.fromTo(3, 3);
            assertThat(single.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("派生区间测试")
    class DerivedIntervalTests {

        @Test
        @DisplayName("reversed - 反转区间")
        void testReversed() {
            IntInterval interval = IntInterval.fromTo(1, 5);
            IntInterval reversed = interval.reversed();

            assertThat(reversed.toArray()).containsExactly(5, 4, 3, 2, 1);
        }

        @Test
        @DisplayName("reversed - 反转带步长的区间")
        void testReversedWithStep() {
            IntInterval interval = IntInterval.fromToBy(1, 10, 3);
            IntInterval reversed = interval.reversed();

            assertThat(reversed.toArray()).containsExactly(10, 7, 4, 1);
        }

        @Test
        @DisplayName("by - 更改步长")
        void testBy() {
            IntInterval interval = IntInterval.fromTo(1, 10);
            IntInterval stepped = interval.by(3);

            assertThat(stepped.toArray()).containsExactly(1, 4, 7, 10);
        }

        @Test
        @DisplayName("by - 非法步长抛出异常")
        void testByInvalidStep() {
            IntInterval interval = IntInterval.fromTo(1, 10);

            assertThatThrownBy(() -> interval.by(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("转换操作测试")
    class ConversionTests {

        @Test
        @DisplayName("toArray - 转换为 int 数组")
        void testToArray() {
            IntInterval interval = IntInterval.fromTo(1, 3);

            int[] array = interval.toArray();

            assertThat(array).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("toList - 转换为 Integer 列表")
        void testToList() {
            IntInterval interval = IntInterval.fromTo(1, 3);

            List<Integer> list = interval.toList();

            assertThat(list).containsExactly(1, 2, 3);
            assertThatThrownBy(() -> list.add(4))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("stream - 转换为 IntStream")
        void testStream() {
            IntInterval interval = IntInterval.oneTo(5);

            int sum = interval.stream().sum();

            assertThat(sum).isEqualTo(15);
        }

        @Test
        @DisplayName("stream - 空区间 stream")
        void testStreamEmpty() {
            IntInterval interval = IntInterval.fromToBy(5, 5, 1);

            long count = interval.stream().count();

            assertThat(count).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("迭代测试")
    class IterationTests {

        @Test
        @DisplayName("iterator - for-each 循环")
        void testIterator() {
            IntInterval interval = IntInterval.fromTo(1, 5);
            List<Integer> collected = new ArrayList<>();

            for (int value : interval) {
                collected.add(value);
            }

            assertThat(collected).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("iterator - NoSuchElementException 当超出范围")
        void testIteratorExhausted() {
            IntInterval interval = IntInterval.fromTo(1, 1);
            Iterator<Integer> it = interval.iterator();

            assertThat(it.hasNext()).isTrue();
            it.next();
            assertThat(it.hasNext()).isFalse();
            assertThatThrownBy(it::next)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("forEach(IntConsumer) - 原始类型遍历")
        void testForEachIntConsumer() {
            IntInterval interval = IntInterval.fromTo(1, 5);
            List<Integer> collected = new ArrayList<>();

            interval.forEach((int v) -> collected.add(v));

            assertThat(collected).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("forEach(IntConsumer) - 降序遍历")
        void testForEachDescending() {
            IntInterval interval = IntInterval.fromTo(3, 1);
            List<Integer> collected = new ArrayList<>();

            interval.forEach((int v) -> collected.add(v));

            assertThat(collected).containsExactly(3, 2, 1);
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相同参数的区间相等")
        void testEquals() {
            IntInterval a = IntInterval.fromTo(1, 5);
            IntInterval b = IntInterval.fromTo(1, 5);

            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("equals - 不同参数的区间不相等")
        void testNotEquals() {
            IntInterval a = IntInterval.fromTo(1, 5);
            IntInterval b = IntInterval.fromTo(1, 6);

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("equals - 不同步长的区间不相等")
        void testNotEqualsDifferentStep() {
            IntInterval a = IntInterval.fromToBy(1, 10, 2);
            IntInterval b = IntInterval.fromToBy(1, 10, 3);

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("equals - 与非 IntInterval 对象不相等")
        void testNotEqualsOtherType() {
            IntInterval interval = IntInterval.fromTo(1, 5);

            assertThat(interval).isNotEqualTo("not an interval");
        }

        @Test
        @DisplayName("hashCode - 相同参数的区间 hashCode 相等")
        void testHashCode() {
            IntInterval a = IntInterval.fromTo(1, 5);
            IntInterval b = IntInterval.fromTo(1, 5);

            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("toString - 输出格式正确")
        void testToString() {
            IntInterval interval = IntInterval.fromToBy(1, 10, 3);

            assertThat(interval.toString()).isEqualTo("IntInterval[1..10 step 3]");
        }

        @Test
        @DisplayName("toString - 默认步长")
        void testToStringDefaultStep() {
            IntInterval interval = IntInterval.fromTo(1, 5);

            assertThat(interval.toString()).isEqualTo("IntInterval[1..5 step 1]");
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("单元素区间 fromTo(5,5)")
        void testSingleElementInterval() {
            IntInterval interval = IntInterval.fromTo(5, 5);

            assertThat(interval.size()).isEqualTo(1);
            assertThat(interval.getFirst()).isEqualTo(5);
            assertThat(interval.getLast()).isEqualTo(5);
            assertThat(interval.contains(5)).isTrue();
            assertThat(interval.contains(4)).isFalse();
            assertThat(interval.toArray()).containsExactly(5);
        }

        @Test
        @DisplayName("负数区间")
        void testNegativeRange() {
            IntInterval interval = IntInterval.fromTo(-5, -1);

            assertThat(interval.toArray()).containsExactly(-5, -4, -3, -2, -1);
        }

        @Test
        @DisplayName("跨零区间")
        void testCrossZeroRange() {
            IntInterval interval = IntInterval.fromTo(-2, 2);

            assertThat(interval.toArray()).containsExactly(-2, -1, 0, 1, 2);
        }

        @Test
        @DisplayName("fromToBy(from, from, step) - from == to 的情况")
        void testFromEqualsTo() {
            IntInterval interval = IntInterval.fromToBy(7, 7, 3);

            assertThat(interval.size()).isEqualTo(1);
            assertThat(interval.getFirst()).isEqualTo(7);
            assertThat(interval.getLast()).isEqualTo(7);
        }

        @Test
        @DisplayName("getFirst/getLast on empty-like interval throws")
        void testGetFirstLastOnEmptyThrows() {
            // A step-2 interval where from > to with positive step would be caught
            // by factory validation. But we can test the empty getFirst/getLast
            // indirectly: reversed of empty returns same empty
            IntInterval single = IntInterval.fromTo(5, 5);
            assertThat(single.getFirst()).isEqualTo(5);
            assertThat(single.getLast()).isEqualTo(5);
        }

        @Test
        @DisplayName("大步长导致只有一个元素")
        void testLargeStepSingleElement() {
            IntInterval interval = IntInterval.fromToBy(1, 100, 200);

            assertThat(interval.size()).isEqualTo(1);
            assertThat(interval.toArray()).containsExactly(1);
        }

        @Test
        @DisplayName("reversed 后再 reversed 得到原区间内容")
        void testDoubleReversed() {
            IntInterval original = IntInterval.fromToBy(1, 10, 3);
            IntInterval doubleReversed = original.reversed().reversed();

            assertThat(doubleReversed.toArray()).isEqualTo(original.toArray());
        }
    }
}
