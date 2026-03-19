package cloud.opencode.base.core.primitives;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Doubles 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("Doubles 测试")
class DoublesTest {

    @Nested
    @DisplayName("常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("BYTES 常量")
        void testBytesConstant() {
            assertThat(Doubles.BYTES).isEqualTo(8);
        }

        @Test
        @DisplayName("EMPTY_ARRAY 常量")
        void testEmptyArray() {
            assertThat(Doubles.EMPTY_ARRAY).isEmpty();
        }
    }

    @Nested
    @DisplayName("字节转换测试")
    class ByteConversionTests {

        @Test
        @DisplayName("toByteArray 转换")
        void testToByteArray() {
            byte[] bytes = Doubles.toByteArray(3.14);
            assertThat(bytes).hasSize(8);
        }

        @Test
        @DisplayName("fromByteArray 转换")
        void testFromByteArray() {
            double original = 3.14159265358979;
            byte[] bytes = Doubles.toByteArray(original);
            double restored = Doubles.fromByteArray(bytes);
            assertThat(restored).isEqualTo(original);
        }

        @Test
        @DisplayName("往返转换特殊值")
        void testRoundTripSpecialValues() {
            assertThat(Doubles.fromByteArray(Doubles.toByteArray(Double.MAX_VALUE)))
                    .isEqualTo(Double.MAX_VALUE);
            assertThat(Doubles.fromByteArray(Doubles.toByteArray(Double.MIN_VALUE)))
                    .isEqualTo(Double.MIN_VALUE);
            assertThat(Doubles.fromByteArray(Doubles.toByteArray(0.0)))
                    .isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("数组操作测试")
    class ArrayOperationsTests {

        @Test
        @DisplayName("concat 合并数组")
        void testConcat() {
            double[] arr1 = {1.0, 2.0};
            double[] arr2 = {3.0, 4.0};
            double[] result = Doubles.concat(arr1, arr2);
            assertThat(result).containsExactly(1.0, 2.0, 3.0, 4.0);
        }

        @Test
        @DisplayName("concat 空数组")
        void testConcatEmpty() {
            double[] arr1 = {};
            double[] arr2 = {1.0, 2.0};
            double[] result = Doubles.concat(arr1, arr2);
            assertThat(result).containsExactly(1.0, 2.0);
        }

        @Test
        @DisplayName("contains 包含元素")
        void testContainsTrue() {
            double[] array = {1.0, 2.0, 3.0, 4.0, 5.0};
            assertThat(Doubles.contains(array, 3.0)).isTrue();
        }

        @Test
        @DisplayName("contains 不包含元素")
        void testContainsFalse() {
            double[] array = {1.0, 2.0, 3.0, 4.0, 5.0};
            assertThat(Doubles.contains(array, 6.0)).isFalse();
        }

        @Test
        @DisplayName("indexOf 找到元素")
        void testIndexOfFound() {
            double[] array = {1.0, 2.0, 3.0, 4.0, 5.0};
            assertThat(Doubles.indexOf(array, 3.0)).isEqualTo(2);
        }

        @Test
        @DisplayName("indexOf 未找到元素")
        void testIndexOfNotFound() {
            double[] array = {1.0, 2.0, 3.0, 4.0, 5.0};
            assertThat(Doubles.indexOf(array, 6.0)).isEqualTo(-1);
        }

        @Test
        @DisplayName("lastIndexOf 找到元素")
        void testLastIndexOfFound() {
            double[] array = {1.0, 2.0, 3.0, 2.0, 5.0};
            assertThat(Doubles.lastIndexOf(array, 2.0)).isEqualTo(3);
        }

        @Test
        @DisplayName("lastIndexOf 未找到元素")
        void testLastIndexOfNotFound() {
            double[] array = {1.0, 2.0, 3.0};
            assertThat(Doubles.lastIndexOf(array, 6.0)).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("最值测试")
    class MinMaxTests {

        @Test
        @DisplayName("min 返回最小值")
        void testMin() {
            assertThat(Doubles.min(3.0, 1.0, 4.0, 1.5, 5.0)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("min 空数组抛异常")
        void testMinEmpty() {
            assertThatThrownBy(() -> Doubles.min())
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("max 返回最大值")
        void testMax() {
            assertThat(Doubles.max(3.0, 1.0, 4.0, 1.5, 5.0)).isEqualTo(5.0);
        }

        @Test
        @DisplayName("max 空数组抛异常")
        void testMaxEmpty() {
            assertThatThrownBy(() -> Doubles.max())
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("min/max 包含 NaN")
        void testMinMaxWithNaN() {
            double[] array = {1.0, Double.NaN, 3.0};
            assertThat(Double.isNaN(Doubles.min(array))).isTrue();
            assertThat(Double.isNaN(Doubles.max(array))).isTrue();
        }
    }

    @Nested
    @DisplayName("约束测试")
    class ConstraintTests {

        @Test
        @DisplayName("constrainToRange 值在范围内")
        void testConstrainInRange() {
            assertThat(Doubles.constrainToRange(5.0, 1.0, 10.0)).isEqualTo(5.0);
        }

        @Test
        @DisplayName("constrainToRange 值小于最小值")
        void testConstrainBelowMin() {
            assertThat(Doubles.constrainToRange(0.0, 1.0, 10.0)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("constrainToRange 值大于最大值")
        void testConstrainAboveMax() {
            assertThat(Doubles.constrainToRange(15.0, 1.0, 10.0)).isEqualTo(10.0);
        }
    }

    @Nested
    @DisplayName("比较测试")
    class CompareTests {

        @Test
        @DisplayName("compare 相等")
        void testCompareEqual() {
            assertThat(Doubles.compare(5.0, 5.0)).isEqualTo(0);
        }

        @Test
        @DisplayName("compare 小于")
        void testCompareLess() {
            assertThat(Doubles.compare(3.0, 5.0)).isLessThan(0);
        }

        @Test
        @DisplayName("compare 大于")
        void testCompareGreater() {
            assertThat(Doubles.compare(7.0, 5.0)).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("集合转换测试")
    class CollectionTests {

        @Test
        @DisplayName("asList 转换")
        void testAsList() {
            List<Double> list = Doubles.asList(1.0, 2.0, 3.0);
            assertThat(list).containsExactly(1.0, 2.0, 3.0);
        }

        @Test
        @DisplayName("asList 空数组")
        void testAsListEmpty() {
            List<Double> list = Doubles.asList();
            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("toArray 转换")
        void testToArray() {
            List<Double> list = Arrays.asList(1.0, 2.0, 3.0);
            double[] array = Doubles.toArray(list);
            assertThat(array).containsExactly(1.0, 2.0, 3.0);
        }
    }

    @Nested
    @DisplayName("数组反转测试")
    class ReverseTests {

        @Test
        @DisplayName("reverse 反转整个数组")
        void testReverse() {
            double[] array = {1.0, 2.0, 3.0, 4.0, 5.0};
            Doubles.reverse(array);
            assertThat(array).containsExactly(5.0, 4.0, 3.0, 2.0, 1.0);
        }

        @Test
        @DisplayName("reverse 反转指定范围")
        void testReverseRange() {
            double[] array = {1.0, 2.0, 3.0, 4.0, 5.0};
            Doubles.reverse(array, 1, 4);
            assertThat(array).containsExactly(1.0, 4.0, 3.0, 2.0, 5.0);
        }

        @Test
        @DisplayName("rotate 向右旋转")
        void testRotateRight() {
            double[] array = {1.0, 2.0, 3.0, 4.0, 5.0};
            Doubles.rotate(array, 2);
            assertThat(array).containsExactly(4.0, 5.0, 1.0, 2.0, 3.0);
        }

        @Test
        @DisplayName("rotate 向左旋转")
        void testRotateLeft() {
            double[] array = {1.0, 2.0, 3.0, 4.0, 5.0};
            Doubles.rotate(array, -2);
            assertThat(array).containsExactly(3.0, 4.0, 5.0, 1.0, 2.0);
        }

        @Test
        @DisplayName("rotate 空数组")
        void testRotateEmpty() {
            double[] array = {};
            Doubles.rotate(array, 2);
            assertThat(array).isEmpty();
        }
    }

    @Nested
    @DisplayName("排序测试")
    class SortTests {

        @Test
        @DisplayName("sortDescending 降序排序")
        void testSortDescending() {
            double[] array = {1.0, 5.0, 3.0, 2.0, 4.0};
            Doubles.sortDescending(array);
            assertThat(array).containsExactly(5.0, 4.0, 3.0, 2.0, 1.0);
        }

        @Test
        @DisplayName("sortDescending 指定范围")
        void testSortDescendingRange() {
            double[] array = {1.0, 5.0, 3.0, 2.0, 4.0};
            Doubles.sortDescending(array, 1, 4);
            assertThat(array).containsExactly(1.0, 5.0, 3.0, 2.0, 4.0);
        }
    }

    @Nested
    @DisplayName("isFinite 测试")
    class IsFiniteTests {

        @Test
        @DisplayName("有限值")
        void testFinite() {
            assertThat(Doubles.isFinite(3.14)).isTrue();
            assertThat(Doubles.isFinite(0.0)).isTrue();
            assertThat(Doubles.isFinite(-100.0)).isTrue();
        }

        @Test
        @DisplayName("无穷大")
        void testInfinite() {
            assertThat(Doubles.isFinite(Double.POSITIVE_INFINITY)).isFalse();
            assertThat(Doubles.isFinite(Double.NEGATIVE_INFINITY)).isFalse();
        }

        @Test
        @DisplayName("NaN")
        void testNaN() {
            assertThat(Doubles.isFinite(Double.NaN)).isFalse();
        }
    }

    @Nested
    @DisplayName("字符串转换测试")
    class StringTests {

        @Test
        @DisplayName("join 连接")
        void testJoin() {
            String result = Doubles.join(", ", 1.0, 2.0, 3.0);
            assertThat(result).isEqualTo("1.0, 2.0, 3.0");
        }

        @Test
        @DisplayName("join 空数组")
        void testJoinEmpty() {
            String result = Doubles.join(", ");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("tryParse 有效数字")
        void testTryParseValid() {
            assertThat(Doubles.tryParse("3.14")).isEqualTo(3.14);
        }

        @Test
        @DisplayName("tryParse 无效数字")
        void testTryParseInvalid() {
            assertThat(Doubles.tryParse("abc")).isNull();
        }

        @Test
        @DisplayName("tryParse null")
        void testTryParseNull() {
            assertThat(Doubles.tryParse(null)).isNull();
        }

        @Test
        @DisplayName("tryParse 空字符串")
        void testTryParseEmpty() {
            assertThat(Doubles.tryParse("")).isNull();
        }
    }

    @Nested
    @DisplayName("ensureNonNull 测试")
    class EnsureNonNullTests {

        @Test
        @DisplayName("非 null 数组")
        void testNonNull() {
            double[] array = {1.0, 2.0, 3.0};
            assertThat(Doubles.ensureNonNull(array)).isSameAs(array);
        }

        @Test
        @DisplayName("null 数组")
        void testNull() {
            assertThat(Doubles.ensureNonNull(null)).isSameAs(Doubles.EMPTY_ARRAY);
        }
    }
}
