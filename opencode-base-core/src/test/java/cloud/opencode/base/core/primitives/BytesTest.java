package cloud.opencode.base.core.primitives;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Bytes 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("Bytes 测试")
class BytesTest {

    @Nested
    @DisplayName("常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("BYTES 常量")
        void testBytesConstant() {
            assertThat(Bytes.BYTES).isEqualTo(1);
        }

        @Test
        @DisplayName("EMPTY_ARRAY 常量")
        void testEmptyArray() {
            assertThat(Bytes.EMPTY_ARRAY).isEmpty();
        }
    }

    @Nested
    @DisplayName("数组操作测试")
    class ArrayOperationsTests {

        @Test
        @DisplayName("concat 合并数组")
        void testConcat() {
            byte[] arr1 = {1, 2};
            byte[] arr2 = {3, 4};
            byte[] result = Bytes.concat(arr1, arr2);
            assertThat(result).containsExactly(1, 2, 3, 4);
        }

        @Test
        @DisplayName("concat 空数组")
        void testConcatEmpty() {
            byte[] arr1 = {};
            byte[] arr2 = {1, 2};
            byte[] result = Bytes.concat(arr1, arr2);
            assertThat(result).containsExactly(1, 2);
        }

        @Test
        @DisplayName("concat 多个数组")
        void testConcatMultiple() {
            byte[] arr1 = {1};
            byte[] arr2 = {2};
            byte[] arr3 = {3};
            byte[] result = Bytes.concat(arr1, arr2, arr3);
            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("contains 包含元素")
        void testContainsTrue() {
            byte[] array = {1, 2, 3, 4, 5};
            assertThat(Bytes.contains(array, (byte) 3)).isTrue();
        }

        @Test
        @DisplayName("contains 不包含元素")
        void testContainsFalse() {
            byte[] array = {1, 2, 3, 4, 5};
            assertThat(Bytes.contains(array, (byte) 6)).isFalse();
        }

        @Test
        @DisplayName("indexOf 找到元素")
        void testIndexOfFound() {
            byte[] array = {1, 2, 3, 4, 5};
            assertThat(Bytes.indexOf(array, (byte) 3)).isEqualTo(2);
        }

        @Test
        @DisplayName("indexOf 未找到元素")
        void testIndexOfNotFound() {
            byte[] array = {1, 2, 3, 4, 5};
            assertThat(Bytes.indexOf(array, (byte) 6)).isEqualTo(-1);
        }

        @Test
        @DisplayName("indexOf 指定范围")
        void testIndexOfWithRange() {
            byte[] array = {1, 2, 3, 2, 5};
            assertThat(Bytes.indexOf(array, (byte) 2, 2, 5)).isEqualTo(3);
        }

        @Test
        @DisplayName("indexOf 子数组")
        void testIndexOfSubarray() {
            byte[] array = {1, 2, 3, 4, 5};
            byte[] target = {3, 4};
            assertThat(Bytes.indexOf(array, target)).isEqualTo(2);
        }

        @Test
        @DisplayName("indexOf 空子数组")
        void testIndexOfEmptySubarray() {
            byte[] array = {1, 2, 3};
            byte[] target = {};
            assertThat(Bytes.indexOf(array, target)).isEqualTo(0);
        }

        @Test
        @DisplayName("indexOf 子数组未找到")
        void testIndexOfSubarrayNotFound() {
            byte[] array = {1, 2, 3, 4, 5};
            byte[] target = {3, 5};
            assertThat(Bytes.indexOf(array, target)).isEqualTo(-1);
        }

        @Test
        @DisplayName("lastIndexOf 找到元素")
        void testLastIndexOfFound() {
            byte[] array = {1, 2, 3, 2, 5};
            assertThat(Bytes.lastIndexOf(array, (byte) 2)).isEqualTo(3);
        }

        @Test
        @DisplayName("lastIndexOf 未找到元素")
        void testLastIndexOfNotFound() {
            byte[] array = {1, 2, 3};
            assertThat(Bytes.lastIndexOf(array, (byte) 6)).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("约束测试")
    class ConstraintTests {

        @Test
        @DisplayName("constrainToRange 值在范围内")
        void testConstrainInRange() {
            assertThat(Bytes.constrainToRange((byte) 5, (byte) 1, (byte) 10)).isEqualTo((byte) 5);
        }

        @Test
        @DisplayName("constrainToRange 值小于最小值")
        void testConstrainBelowMin() {
            assertThat(Bytes.constrainToRange((byte) 0, (byte) 1, (byte) 10)).isEqualTo((byte) 1);
        }

        @Test
        @DisplayName("constrainToRange 值大于最大值")
        void testConstrainAboveMax() {
            assertThat(Bytes.constrainToRange((byte) 15, (byte) 1, (byte) 10)).isEqualTo((byte) 10);
        }

        @Test
        @DisplayName("constrainToRange min > max 抛异常")
        void testConstrainInvalidRange() {
            assertThatThrownBy(() -> Bytes.constrainToRange((byte) 5, (byte) 10, (byte) 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("转换测试")
    class ConversionTests {

        @Test
        @DisplayName("toUnsignedInt 正数")
        void testToUnsignedIntPositive() {
            assertThat(Bytes.toUnsignedInt((byte) 127)).isEqualTo(127);
        }

        @Test
        @DisplayName("toUnsignedInt 负数转无符号")
        void testToUnsignedIntNegative() {
            assertThat(Bytes.toUnsignedInt((byte) -1)).isEqualTo(255);
            assertThat(Bytes.toUnsignedInt((byte) -128)).isEqualTo(128);
        }

        @Test
        @DisplayName("toUnsignedLong 正数")
        void testToUnsignedLongPositive() {
            assertThat(Bytes.toUnsignedLong((byte) 127)).isEqualTo(127L);
        }

        @Test
        @DisplayName("toUnsignedLong 负数转无符号")
        void testToUnsignedLongNegative() {
            assertThat(Bytes.toUnsignedLong((byte) -1)).isEqualTo(255L);
            assertThat(Bytes.toUnsignedLong((byte) -128)).isEqualTo(128L);
        }
    }

    @Nested
    @DisplayName("集合转换测试")
    class CollectionTests {

        @Test
        @DisplayName("asList 转换")
        void testAsList() {
            List<Byte> list = Bytes.asList((byte) 1, (byte) 2, (byte) 3);
            assertThat(list).containsExactly((byte) 1, (byte) 2, (byte) 3);
        }

        @Test
        @DisplayName("asList 空数组")
        void testAsListEmpty() {
            List<Byte> list = Bytes.asList();
            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("toArray 转换")
        void testToArray() {
            List<Byte> list = Arrays.asList((byte) 1, (byte) 2, (byte) 3);
            byte[] array = Bytes.toArray(list);
            assertThat(array).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("数组反转测试")
    class ReverseTests {

        @Test
        @DisplayName("reverse 反转整个数组")
        void testReverse() {
            byte[] array = {1, 2, 3, 4, 5};
            Bytes.reverse(array);
            assertThat(array).containsExactly(5, 4, 3, 2, 1);
        }

        @Test
        @DisplayName("reverse 反转指定范围")
        void testReverseRange() {
            byte[] array = {1, 2, 3, 4, 5};
            Bytes.reverse(array, 1, 4);
            assertThat(array).containsExactly(1, 4, 3, 2, 5);
        }

        @Test
        @DisplayName("reverse 空数组")
        void testReverseEmpty() {
            byte[] array = {};
            Bytes.reverse(array);
            assertThat(array).isEmpty();
        }
    }

    @Nested
    @DisplayName("比较测试")
    class CompareTests {

        @Test
        @DisplayName("compare 相等")
        void testCompareEqual() {
            byte[] a = {1, 2, 3};
            byte[] b = {1, 2, 3};
            assertThat(Bytes.compare(a, b)).isEqualTo(0);
        }

        @Test
        @DisplayName("compare 小于")
        void testCompareLess() {
            byte[] a = {1, 2, 3};
            byte[] b = {1, 2, 4};
            assertThat(Bytes.compare(a, b)).isLessThan(0);
        }

        @Test
        @DisplayName("compare 大于")
        void testCompareGreater() {
            byte[] a = {1, 2, 4};
            byte[] b = {1, 2, 3};
            assertThat(Bytes.compare(a, b)).isGreaterThan(0);
        }

        @Test
        @DisplayName("compareUnsigned 无符号比较")
        void testCompareUnsigned() {
            // -1 as unsigned is 255, which is greater than 1
            assertThat(Bytes.compareUnsigned((byte) -1, (byte) 1)).isGreaterThan(0);
            assertThat(Bytes.compareUnsigned((byte) 1, (byte) 1)).isEqualTo(0);
        }

        @Test
        @DisplayName("equals 相等")
        void testEqualsTrue() {
            byte[] a = {1, 2, 3};
            byte[] b = {1, 2, 3};
            assertThat(Bytes.equals(a, b)).isTrue();
        }

        @Test
        @DisplayName("equals 不相等")
        void testEqualsFalse() {
            byte[] a = {1, 2, 3};
            byte[] b = {1, 2, 4};
            assertThat(Bytes.equals(a, b)).isFalse();
        }
    }

    @Nested
    @DisplayName("字符串转换测试")
    class StringTests {

        @Test
        @DisplayName("join 连接")
        void testJoin() {
            String result = Bytes.join(", ", (byte) 1, (byte) 2, (byte) 3);
            assertThat(result).isEqualTo("1, 2, 3");
        }

        @Test
        @DisplayName("join 空数组")
        void testJoinEmpty() {
            String result = Bytes.join(", ");
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("哈希测试")
    class HashCodeTests {

        @Test
        @DisplayName("hashCode 相同数组")
        void testHashCodeSame() {
            byte[] a = {1, 2, 3};
            byte[] b = {1, 2, 3};
            assertThat(Bytes.hashCode(a)).isEqualTo(Bytes.hashCode(b));
        }

        @Test
        @DisplayName("hashCode 空数组")
        void testHashCodeEmpty() {
            byte[] array = {};
            assertThat(Bytes.hashCode(array)).isEqualTo(Arrays.hashCode(array));
        }
    }

    @Nested
    @DisplayName("子数组测试")
    class SubarrayTests {

        @Test
        @DisplayName("subarray 正常范围")
        void testSubarray() {
            byte[] array = {1, 2, 3, 4, 5};
            byte[] result = Bytes.subarray(array, 1, 4);
            assertThat(result).containsExactly(2, 3, 4);
        }

        @Test
        @DisplayName("subarray 完整数组")
        void testSubarrayFull() {
            byte[] array = {1, 2, 3};
            byte[] result = Bytes.subarray(array, 0, 3);
            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("subarray 空范围")
        void testSubarrayEmpty() {
            byte[] array = {1, 2, 3};
            byte[] result = Bytes.subarray(array, 1, 1);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("subarray 无效范围抛异常")
        void testSubarrayInvalidRange() {
            byte[] array = {1, 2, 3};
            assertThatThrownBy(() -> Bytes.subarray(array, 2, 1))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("subarray 起始索引负数抛异常")
        void testSubarrayNegativeStart() {
            byte[] array = {1, 2, 3};
            assertThatThrownBy(() -> Bytes.subarray(array, -1, 2))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("subarray 结束索引超出抛异常")
        void testSubarrayEndOverflow() {
            byte[] array = {1, 2, 3};
            assertThatThrownBy(() -> Bytes.subarray(array, 0, 5))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }
    }

    @Nested
    @DisplayName("ensureNonNull 测试")
    class EnsureNonNullTests {

        @Test
        @DisplayName("非 null 数组")
        void testNonNull() {
            byte[] array = {1, 2, 3};
            assertThat(Bytes.ensureNonNull(array)).isSameAs(array);
        }

        @Test
        @DisplayName("null 数组")
        void testNull() {
            assertThat(Bytes.ensureNonNull(null)).isSameAs(Bytes.EMPTY_ARRAY);
        }
    }
}
