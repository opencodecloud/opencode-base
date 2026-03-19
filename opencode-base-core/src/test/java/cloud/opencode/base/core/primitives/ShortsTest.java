package cloud.opencode.base.core.primitives;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Shorts 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("Shorts 测试")
class ShortsTest {

    @Nested
    @DisplayName("常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("BYTES 常量")
        void testBytesConstant() {
            assertThat(Shorts.BYTES).isEqualTo(2);
        }

        @Test
        @DisplayName("EMPTY_ARRAY 常量")
        void testEmptyArray() {
            assertThat(Shorts.EMPTY_ARRAY).isEmpty();
        }
    }

    @Nested
    @DisplayName("字节转换测试")
    class ByteConversionTests {

        @Test
        @DisplayName("toByteArray 转换")
        void testToByteArray() {
            byte[] bytes = Shorts.toByteArray((short) 0x1234);
            assertThat(bytes).hasSize(2);
            assertThat(bytes[0]).isEqualTo((byte) 0x12);
            assertThat(bytes[1]).isEqualTo((byte) 0x34);
        }

        @Test
        @DisplayName("toByteArray 零")
        void testToByteArrayZero() {
            byte[] bytes = Shorts.toByteArray((short) 0);
            assertThat(bytes).containsExactly(0, 0);
        }

        @Test
        @DisplayName("fromByteArray 转换")
        void testFromByteArray() {
            byte[] bytes = {0x12, 0x34};
            short value = Shorts.fromByteArray(bytes);
            assertThat(value).isEqualTo((short) 0x1234);
        }

        @Test
        @DisplayName("往返转换")
        void testRoundTrip() {
            short original = 12345;
            byte[] bytes = Shorts.toByteArray(original);
            short restored = Shorts.fromByteArray(bytes);
            assertThat(restored).isEqualTo(original);
        }

        @Test
        @DisplayName("往返转换负数")
        void testRoundTripNegative() {
            short original = -12345;
            byte[] bytes = Shorts.toByteArray(original);
            short restored = Shorts.fromByteArray(bytes);
            assertThat(restored).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("数组操作测试")
    class ArrayOperationsTests {

        @Test
        @DisplayName("concat 合并数组")
        void testConcat() {
            short[] arr1 = {1, 2};
            short[] arr2 = {3, 4};
            short[] result = Shorts.concat(arr1, arr2);
            assertThat(result).containsExactly((short) 1, (short) 2, (short) 3, (short) 4);
        }

        @Test
        @DisplayName("concat 空数组")
        void testConcatEmpty() {
            short[] arr1 = {};
            short[] arr2 = {1, 2};
            short[] result = Shorts.concat(arr1, arr2);
            assertThat(result).containsExactly((short) 1, (short) 2);
        }

        @Test
        @DisplayName("contains 包含元素")
        void testContainsTrue() {
            short[] array = {1, 2, 3, 4, 5};
            assertThat(Shorts.contains(array, (short) 3)).isTrue();
        }

        @Test
        @DisplayName("contains 不包含元素")
        void testContainsFalse() {
            short[] array = {1, 2, 3, 4, 5};
            assertThat(Shorts.contains(array, (short) 6)).isFalse();
        }

        @Test
        @DisplayName("indexOf 找到元素")
        void testIndexOfFound() {
            short[] array = {1, 2, 3, 4, 5};
            assertThat(Shorts.indexOf(array, (short) 3)).isEqualTo(2);
        }

        @Test
        @DisplayName("indexOf 未找到元素")
        void testIndexOfNotFound() {
            short[] array = {1, 2, 3, 4, 5};
            assertThat(Shorts.indexOf(array, (short) 6)).isEqualTo(-1);
        }

        @Test
        @DisplayName("lastIndexOf 找到元素")
        void testLastIndexOfFound() {
            short[] array = {1, 2, 3, 2, 5};
            assertThat(Shorts.lastIndexOf(array, (short) 2)).isEqualTo(3);
        }

        @Test
        @DisplayName("lastIndexOf 未找到元素")
        void testLastIndexOfNotFound() {
            short[] array = {1, 2, 3};
            assertThat(Shorts.lastIndexOf(array, (short) 6)).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("最值测试")
    class MinMaxTests {

        @Test
        @DisplayName("min 返回最小值")
        void testMin() {
            assertThat(Shorts.min((short) 3, (short) 1, (short) 4, (short) 1, (short) 5)).isEqualTo((short) 1);
        }

        @Test
        @DisplayName("min 单元素")
        void testMinSingle() {
            assertThat(Shorts.min((short) 42)).isEqualTo((short) 42);
        }

        @Test
        @DisplayName("min 空数组抛异常")
        void testMinEmpty() {
            assertThatThrownBy(() -> Shorts.min())
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("max 返回最大值")
        void testMax() {
            assertThat(Shorts.max((short) 3, (short) 1, (short) 4, (short) 1, (short) 5)).isEqualTo((short) 5);
        }

        @Test
        @DisplayName("max 空数组抛异常")
        void testMaxEmpty() {
            assertThatThrownBy(() -> Shorts.max())
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("约束测试")
    class ConstraintTests {

        @Test
        @DisplayName("constrainToRange 值在范围内")
        void testConstrainInRange() {
            assertThat(Shorts.constrainToRange((short) 5, (short) 1, (short) 10)).isEqualTo((short) 5);
        }

        @Test
        @DisplayName("constrainToRange 值小于最小值")
        void testConstrainBelowMin() {
            assertThat(Shorts.constrainToRange((short) 0, (short) 1, (short) 10)).isEqualTo((short) 1);
        }

        @Test
        @DisplayName("constrainToRange 值大于最大值")
        void testConstrainAboveMax() {
            assertThat(Shorts.constrainToRange((short) 15, (short) 1, (short) 10)).isEqualTo((short) 10);
        }
    }

    @Nested
    @DisplayName("比较测试")
    class CompareTests {

        @Test
        @DisplayName("compare 相等")
        void testCompareEqual() {
            assertThat(Shorts.compare((short) 5, (short) 5)).isEqualTo(0);
        }

        @Test
        @DisplayName("compare 小于")
        void testCompareLess() {
            assertThat(Shorts.compare((short) 3, (short) 5)).isLessThan(0);
        }

        @Test
        @DisplayName("compare 大于")
        void testCompareGreater() {
            assertThat(Shorts.compare((short) 7, (short) 5)).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("集合转换测试")
    class CollectionTests {

        @Test
        @DisplayName("asList 转换")
        void testAsList() {
            List<Short> list = Shorts.asList((short) 1, (short) 2, (short) 3);
            assertThat(list).containsExactly((short) 1, (short) 2, (short) 3);
        }

        @Test
        @DisplayName("asList 空数组")
        void testAsListEmpty() {
            List<Short> list = Shorts.asList();
            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("toArray 转换")
        void testToArray() {
            List<Short> list = Arrays.asList((short) 1, (short) 2, (short) 3);
            short[] array = Shorts.toArray(list);
            assertThat(array).containsExactly((short) 1, (short) 2, (short) 3);
        }
    }

    @Nested
    @DisplayName("数组反转测试")
    class ReverseTests {

        @Test
        @DisplayName("reverse 反转整个数组")
        void testReverse() {
            short[] array = {1, 2, 3, 4, 5};
            Shorts.reverse(array);
            assertThat(array).containsExactly((short) 5, (short) 4, (short) 3, (short) 2, (short) 1);
        }

        @Test
        @DisplayName("reverse 空数组")
        void testReverseEmpty() {
            short[] array = {};
            Shorts.reverse(array);
            assertThat(array).isEmpty();
        }
    }

    @Nested
    @DisplayName("安全转换测试")
    class CastTests {

        @Test
        @DisplayName("saturatedCast 正常范围")
        void testSaturatedCastNormal() {
            assertThat(Shorts.saturatedCast(100L)).isEqualTo((short) 100);
        }

        @Test
        @DisplayName("saturatedCast 上溢")
        void testSaturatedCastOverflow() {
            assertThat(Shorts.saturatedCast(Long.MAX_VALUE)).isEqualTo(Short.MAX_VALUE);
        }

        @Test
        @DisplayName("saturatedCast 下溢")
        void testSaturatedCastUnderflow() {
            assertThat(Shorts.saturatedCast(Long.MIN_VALUE)).isEqualTo(Short.MIN_VALUE);
        }

        @Test
        @DisplayName("checkedCast 正常范围")
        void testCheckedCastNormal() {
            assertThat(Shorts.checkedCast(100L)).isEqualTo((short) 100);
        }

        @Test
        @DisplayName("checkedCast 溢出抛异常")
        void testCheckedCastOverflow() {
            assertThatThrownBy(() -> Shorts.checkedCast(Long.MAX_VALUE))
                    .isInstanceOf(ArithmeticException.class);
        }

        @Test
        @DisplayName("checkedCast 下溢抛异常")
        void testCheckedCastUnderflow() {
            assertThatThrownBy(() -> Shorts.checkedCast(Long.MIN_VALUE))
                    .isInstanceOf(ArithmeticException.class);
        }
    }

    @Nested
    @DisplayName("字符串转换测试")
    class StringTests {

        @Test
        @DisplayName("join 连接")
        void testJoin() {
            String result = Shorts.join(", ", (short) 1, (short) 2, (short) 3);
            assertThat(result).isEqualTo("1, 2, 3");
        }

        @Test
        @DisplayName("join 空数组")
        void testJoinEmpty() {
            String result = Shorts.join(", ");
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("ensureNonNull 测试")
    class EnsureNonNullTests {

        @Test
        @DisplayName("非 null 数组")
        void testNonNull() {
            short[] array = {1, 2, 3};
            assertThat(Shorts.ensureNonNull(array)).isSameAs(array);
        }

        @Test
        @DisplayName("null 数组")
        void testNull() {
            assertThat(Shorts.ensureNonNull(null)).isSameAs(Shorts.EMPTY_ARRAY);
        }
    }
}
