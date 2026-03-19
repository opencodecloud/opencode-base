package cloud.opencode.base.core.primitives;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Ints 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("Ints 测试")
class IntsTest {

    @Nested
    @DisplayName("常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("BYTES 常量")
        void testBytesConstant() {
            assertThat(Ints.BYTES).isEqualTo(4);
        }

        @Test
        @DisplayName("MAX_POWER_OF_TWO 常量")
        void testMaxPowerOfTwo() {
            assertThat(Ints.MAX_POWER_OF_TWO).isEqualTo(1 << 30);
        }

        @Test
        @DisplayName("EMPTY_ARRAY 常量")
        void testEmptyArray() {
            assertThat(Ints.EMPTY_ARRAY).isEmpty();
        }
    }

    @Nested
    @DisplayName("字节转换测试")
    class ByteConversionTests {

        @Test
        @DisplayName("toByteArray 正数")
        void testToByteArrayPositive() {
            byte[] bytes = Ints.toByteArray(0x12345678);
            assertThat(bytes).hasSize(4);
            assertThat(bytes[0]).isEqualTo((byte) 0x12);
            assertThat(bytes[1]).isEqualTo((byte) 0x34);
            assertThat(bytes[2]).isEqualTo((byte) 0x56);
            assertThat(bytes[3]).isEqualTo((byte) 0x78);
        }

        @Test
        @DisplayName("toByteArray 零")
        void testToByteArrayZero() {
            byte[] bytes = Ints.toByteArray(0);
            assertThat(bytes).containsExactly(0, 0, 0, 0);
        }

        @Test
        @DisplayName("fromByteArray 转换")
        void testFromByteArray() {
            byte[] bytes = {0x12, 0x34, 0x56, 0x78};
            int value = Ints.fromByteArray(bytes);
            assertThat(value).isEqualTo(0x12345678);
        }

        @Test
        @DisplayName("fromByteArray 数组太短抛异常")
        void testFromByteArrayTooShort() {
            byte[] bytes = {0x12, 0x34};
            assertThatThrownBy(() -> Ints.fromByteArray(bytes))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("fromBytes 转换")
        void testFromBytes() {
            int value = Ints.fromBytes((byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78);
            assertThat(value).isEqualTo(0x12345678);
        }

        @Test
        @DisplayName("往返转换")
        void testRoundTrip() {
            int original = 123456789;
            byte[] bytes = Ints.toByteArray(original);
            int restored = Ints.fromByteArray(bytes);
            assertThat(restored).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("数组操作测试")
    class ArrayOperationsTests {

        @Test
        @DisplayName("concat 合并数组")
        void testConcat() {
            int[] arr1 = {1, 2};
            int[] arr2 = {3, 4};
            int[] result = Ints.concat(arr1, arr2);
            assertThat(result).containsExactly(1, 2, 3, 4);
        }

        @Test
        @DisplayName("concat 空数组")
        void testConcatEmpty() {
            int[] arr1 = {};
            int[] arr2 = {1, 2};
            int[] result = Ints.concat(arr1, arr2);
            assertThat(result).containsExactly(1, 2);
        }

        @Test
        @DisplayName("contains 包含元素")
        void testContainsTrue() {
            int[] array = {1, 2, 3, 4, 5};
            assertThat(Ints.contains(array, 3)).isTrue();
        }

        @Test
        @DisplayName("contains 不包含元素")
        void testContainsFalse() {
            int[] array = {1, 2, 3, 4, 5};
            assertThat(Ints.contains(array, 6)).isFalse();
        }

        @Test
        @DisplayName("indexOf 找到元素")
        void testIndexOfFound() {
            int[] array = {1, 2, 3, 4, 5};
            assertThat(Ints.indexOf(array, 3)).isEqualTo(2);
        }

        @Test
        @DisplayName("indexOf 未找到元素")
        void testIndexOfNotFound() {
            int[] array = {1, 2, 3, 4, 5};
            assertThat(Ints.indexOf(array, 6)).isEqualTo(-1);
        }

        @Test
        @DisplayName("indexOf 指定范围")
        void testIndexOfWithRange() {
            int[] array = {1, 2, 3, 2, 5};
            assertThat(Ints.indexOf(array, 2, 2, 5)).isEqualTo(3);
        }

        @Test
        @DisplayName("indexOf 子数组")
        void testIndexOfSubarray() {
            int[] array = {1, 2, 3, 4, 5};
            int[] target = {3, 4};
            assertThat(Ints.indexOf(array, target)).isEqualTo(2);
        }

        @Test
        @DisplayName("indexOf 子数组未找到")
        void testIndexOfSubarrayNotFound() {
            int[] array = {1, 2, 3, 4, 5};
            int[] target = {3, 5};
            assertThat(Ints.indexOf(array, target)).isEqualTo(-1);
        }

        @Test
        @DisplayName("indexOf 空子数组")
        void testIndexOfEmptySubarray() {
            int[] array = {1, 2, 3};
            int[] target = {};
            assertThat(Ints.indexOf(array, target)).isEqualTo(0);
        }

        @Test
        @DisplayName("lastIndexOf 找到元素")
        void testLastIndexOfFound() {
            int[] array = {1, 2, 3, 2, 5};
            assertThat(Ints.lastIndexOf(array, 2)).isEqualTo(3);
        }

        @Test
        @DisplayName("lastIndexOf 指定范围")
        void testLastIndexOfWithRange() {
            int[] array = {1, 2, 3, 2, 5};
            assertThat(Ints.lastIndexOf(array, 2, 0, 3)).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("最值测试")
    class MinMaxTests {

        @Test
        @DisplayName("min 返回最小值")
        void testMin() {
            assertThat(Ints.min(3, 1, 4, 1, 5)).isEqualTo(1);
        }

        @Test
        @DisplayName("min 单元素")
        void testMinSingle() {
            assertThat(Ints.min(42)).isEqualTo(42);
        }

        @Test
        @DisplayName("min 空数组抛异常")
        void testMinEmpty() {
            assertThatThrownBy(() -> Ints.min())
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("max 返回最大值")
        void testMax() {
            assertThat(Ints.max(3, 1, 4, 1, 5)).isEqualTo(5);
        }

        @Test
        @DisplayName("max 空数组抛异常")
        void testMaxEmpty() {
            assertThatThrownBy(() -> Ints.max())
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("约束测试")
    class ConstraintTests {

        @Test
        @DisplayName("constrainToRange 值在范围内")
        void testConstrainInRange() {
            assertThat(Ints.constrainToRange(5, 1, 10)).isEqualTo(5);
        }

        @Test
        @DisplayName("constrainToRange 值小于最小值")
        void testConstrainBelowMin() {
            assertThat(Ints.constrainToRange(0, 1, 10)).isEqualTo(1);
        }

        @Test
        @DisplayName("constrainToRange 值大于最大值")
        void testConstrainAboveMax() {
            assertThat(Ints.constrainToRange(15, 1, 10)).isEqualTo(10);
        }

        @Test
        @DisplayName("constrainToRange min > max 抛异常")
        void testConstrainInvalidRange() {
            assertThatThrownBy(() -> Ints.constrainToRange(5, 10, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("saturatedCast 正常范围")
        void testSaturatedCastNormal() {
            assertThat(Ints.saturatedCast(100L)).isEqualTo(100);
        }

        @Test
        @DisplayName("saturatedCast 上溢")
        void testSaturatedCastOverflow() {
            assertThat(Ints.saturatedCast(Long.MAX_VALUE)).isEqualTo(Integer.MAX_VALUE);
        }

        @Test
        @DisplayName("saturatedCast 下溢")
        void testSaturatedCastUnderflow() {
            assertThat(Ints.saturatedCast(Long.MIN_VALUE)).isEqualTo(Integer.MIN_VALUE);
        }

        @Test
        @DisplayName("checkedCast 正常范围")
        void testCheckedCastNormal() {
            assertThat(Ints.checkedCast(100L)).isEqualTo(100);
        }

        @Test
        @DisplayName("checkedCast 溢出抛异常")
        void testCheckedCastOverflow() {
            assertThatThrownBy(() -> Ints.checkedCast(Long.MAX_VALUE))
                    .isInstanceOf(ArithmeticException.class);
        }
    }

    @Nested
    @DisplayName("比较测试")
    class CompareTests {

        @Test
        @DisplayName("compare 相等")
        void testCompareEqual() {
            assertThat(Ints.compare(5, 5)).isEqualTo(0);
        }

        @Test
        @DisplayName("compare 小于")
        void testCompareLess() {
            assertThat(Ints.compare(3, 5)).isLessThan(0);
        }

        @Test
        @DisplayName("compare 大于")
        void testCompareGreater() {
            assertThat(Ints.compare(7, 5)).isGreaterThan(0);
        }

        @Test
        @DisplayName("lexicographicalComparator 比较数组")
        void testLexicographicalComparator() {
            Comparator<int[]> comp = Ints.lexicographicalComparator();
            int[] arr1 = {1, 2, 3};
            int[] arr2 = {1, 2, 4};
            int[] arr3 = {1, 2};

            assertThat(comp.compare(arr1, arr2)).isLessThan(0);
            assertThat(comp.compare(arr1, arr3)).isGreaterThan(0);
            assertThat(comp.compare(arr1, arr1)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("集合转换测试")
    class CollectionTests {

        @Test
        @DisplayName("asList 转换")
        void testAsList() {
            List<Integer> list = Ints.asList(1, 2, 3);
            assertThat(list).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("asList 空数组")
        void testAsListEmpty() {
            List<Integer> list = Ints.asList();
            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("toArray 转换")
        void testToArray() {
            List<Integer> list = Arrays.asList(1, 2, 3);
            int[] array = Ints.toArray(list);
            assertThat(array).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("数组反转测试")
    class ReverseTests {

        @Test
        @DisplayName("reverse 反转整个数组")
        void testReverse() {
            int[] array = {1, 2, 3, 4, 5};
            Ints.reverse(array);
            assertThat(array).containsExactly(5, 4, 3, 2, 1);
        }

        @Test
        @DisplayName("reverse 反转指定范围")
        void testReverseRange() {
            int[] array = {1, 2, 3, 4, 5};
            Ints.reverse(array, 1, 4);
            assertThat(array).containsExactly(1, 4, 3, 2, 5);
        }

        @Test
        @DisplayName("rotate 向右旋转")
        void testRotateRight() {
            int[] array = {1, 2, 3, 4, 5};
            Ints.rotate(array, 2);
            assertThat(array).containsExactly(4, 5, 1, 2, 3);
        }

        @Test
        @DisplayName("rotate 向左旋转")
        void testRotateLeft() {
            int[] array = {1, 2, 3, 4, 5};
            Ints.rotate(array, -2);
            assertThat(array).containsExactly(3, 4, 5, 1, 2);
        }

        @Test
        @DisplayName("rotate 空数组")
        void testRotateEmpty() {
            int[] array = {};
            Ints.rotate(array, 2);
            assertThat(array).isEmpty();
        }

        @Test
        @DisplayName("rotate 单元素")
        void testRotateSingle() {
            int[] array = {1};
            Ints.rotate(array, 2);
            assertThat(array).containsExactly(1);
        }
    }

    @Nested
    @DisplayName("排序测试")
    class SortTests {

        @Test
        @DisplayName("sortDescending 降序排序")
        void testSortDescending() {
            int[] array = {1, 5, 3, 2, 4};
            Ints.sortDescending(array);
            assertThat(array).containsExactly(5, 4, 3, 2, 1);
        }

        @Test
        @DisplayName("sortDescending 指定范围")
        void testSortDescendingRange() {
            int[] array = {1, 5, 3, 2, 4};
            Ints.sortDescending(array, 1, 4);
            assertThat(array).containsExactly(1, 5, 3, 2, 4);
        }

        @Test
        @DisplayName("isSorted 已排序")
        void testIsSortedTrue() {
            int[] array = {1, 2, 3, 4, 5};
            assertThat(Ints.isSorted(array)).isTrue();
        }

        @Test
        @DisplayName("isSorted 未排序")
        void testIsSortedFalse() {
            int[] array = {1, 3, 2, 4, 5};
            assertThat(Ints.isSorted(array)).isFalse();
        }

        @Test
        @DisplayName("isSorted 空数组")
        void testIsSortedEmpty() {
            int[] array = {};
            assertThat(Ints.isSorted(array)).isTrue();
        }
    }

    @Nested
    @DisplayName("字符串转换测试")
    class StringTests {

        @Test
        @DisplayName("join 连接")
        void testJoin() {
            String result = Ints.join(", ", 1, 2, 3);
            assertThat(result).isEqualTo("1, 2, 3");
        }

        @Test
        @DisplayName("join 空数组")
        void testJoinEmpty() {
            String result = Ints.join(", ");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("tryParse 有效数字")
        void testTryParseValid() {
            assertThat(Ints.tryParse("123")).isEqualTo(123);
        }

        @Test
        @DisplayName("tryParse 无效数字")
        void testTryParseInvalid() {
            assertThat(Ints.tryParse("abc")).isNull();
        }

        @Test
        @DisplayName("tryParse null")
        void testTryParseNull() {
            assertThat(Ints.tryParse(null)).isNull();
        }

        @Test
        @DisplayName("tryParse 空字符串")
        void testTryParseEmpty() {
            assertThat(Ints.tryParse("")).isNull();
        }

        @Test
        @DisplayName("tryParse 指定进制")
        void testTryParseRadix() {
            assertThat(Ints.tryParse("FF", 16)).isEqualTo(255);
        }
    }

    @Nested
    @DisplayName("ensureNonNull 测试")
    class EnsureNonNullTests {

        @Test
        @DisplayName("非 null 数组")
        void testNonNull() {
            int[] array = {1, 2, 3};
            assertThat(Ints.ensureNonNull(array)).isSameAs(array);
        }

        @Test
        @DisplayName("null 数组")
        void testNull() {
            assertThat(Ints.ensureNonNull(null)).isSameAs(Ints.EMPTY_ARRAY);
        }
    }
}
