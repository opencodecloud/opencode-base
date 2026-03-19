package cloud.opencode.base.core.primitives;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Floats 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("Floats 测试")
class FloatsTest {

    @Nested
    @DisplayName("常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("BYTES 常量")
        void testBytesConstant() {
            assertThat(Floats.BYTES).isEqualTo(4);
        }

        @Test
        @DisplayName("EMPTY_ARRAY 常量")
        void testEmptyArray() {
            assertThat(Floats.EMPTY_ARRAY).isEmpty();
        }
    }

    @Nested
    @DisplayName("字节转换测试")
    class ByteConversionTests {

        @Test
        @DisplayName("toByteArray 转换")
        void testToByteArray() {
            byte[] bytes = Floats.toByteArray(3.14f);
            assertThat(bytes).hasSize(4);
        }

        @Test
        @DisplayName("fromByteArray 转换")
        void testFromByteArray() {
            float original = 3.14159f;
            byte[] bytes = Floats.toByteArray(original);
            float restored = Floats.fromByteArray(bytes);
            assertThat(restored).isEqualTo(original);
        }

        @Test
        @DisplayName("往返转换特殊值")
        void testRoundTripSpecialValues() {
            assertThat(Floats.fromByteArray(Floats.toByteArray(Float.MAX_VALUE)))
                    .isEqualTo(Float.MAX_VALUE);
            assertThat(Floats.fromByteArray(Floats.toByteArray(Float.MIN_VALUE)))
                    .isEqualTo(Float.MIN_VALUE);
            assertThat(Floats.fromByteArray(Floats.toByteArray(0.0f)))
                    .isEqualTo(0.0f);
        }
    }

    @Nested
    @DisplayName("数组操作测试")
    class ArrayOperationsTests {

        @Test
        @DisplayName("concat 合并数组")
        void testConcat() {
            float[] arr1 = {1.0f, 2.0f};
            float[] arr2 = {3.0f, 4.0f};
            float[] result = Floats.concat(arr1, arr2);
            assertThat(result).containsExactly(1.0f, 2.0f, 3.0f, 4.0f);
        }

        @Test
        @DisplayName("concat 空数组")
        void testConcatEmpty() {
            float[] arr1 = {};
            float[] arr2 = {1.0f, 2.0f};
            float[] result = Floats.concat(arr1, arr2);
            assertThat(result).containsExactly(1.0f, 2.0f);
        }

        @Test
        @DisplayName("contains 包含元素")
        void testContainsTrue() {
            float[] array = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f};
            assertThat(Floats.contains(array, 3.0f)).isTrue();
        }

        @Test
        @DisplayName("contains 不包含元素")
        void testContainsFalse() {
            float[] array = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f};
            assertThat(Floats.contains(array, 6.0f)).isFalse();
        }

        @Test
        @DisplayName("indexOf 找到元素")
        void testIndexOfFound() {
            float[] array = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f};
            assertThat(Floats.indexOf(array, 3.0f)).isEqualTo(2);
        }

        @Test
        @DisplayName("indexOf 未找到元素")
        void testIndexOfNotFound() {
            float[] array = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f};
            assertThat(Floats.indexOf(array, 6.0f)).isEqualTo(-1);
        }

        @Test
        @DisplayName("lastIndexOf 找到元素")
        void testLastIndexOfFound() {
            float[] array = {1.0f, 2.0f, 3.0f, 2.0f, 5.0f};
            assertThat(Floats.lastIndexOf(array, 2.0f)).isEqualTo(3);
        }

        @Test
        @DisplayName("lastIndexOf 未找到元素")
        void testLastIndexOfNotFound() {
            float[] array = {1.0f, 2.0f, 3.0f};
            assertThat(Floats.lastIndexOf(array, 6.0f)).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("最值测试")
    class MinMaxTests {

        @Test
        @DisplayName("min 返回最小值")
        void testMin() {
            assertThat(Floats.min(3.0f, 1.0f, 4.0f, 1.5f, 5.0f)).isEqualTo(1.0f);
        }

        @Test
        @DisplayName("min 空数组抛异常")
        void testMinEmpty() {
            assertThatThrownBy(() -> Floats.min())
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("max 返回最大值")
        void testMax() {
            assertThat(Floats.max(3.0f, 1.0f, 4.0f, 1.5f, 5.0f)).isEqualTo(5.0f);
        }

        @Test
        @DisplayName("max 空数组抛异常")
        void testMaxEmpty() {
            assertThatThrownBy(() -> Floats.max())
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("min/max 包含 NaN")
        void testMinMaxWithNaN() {
            float[] array = {1.0f, Float.NaN, 3.0f};
            assertThat(Float.isNaN(Floats.min(array))).isTrue();
            assertThat(Float.isNaN(Floats.max(array))).isTrue();
        }
    }

    @Nested
    @DisplayName("约束测试")
    class ConstraintTests {

        @Test
        @DisplayName("constrainToRange 值在范围内")
        void testConstrainInRange() {
            assertThat(Floats.constrainToRange(5.0f, 1.0f, 10.0f)).isEqualTo(5.0f);
        }

        @Test
        @DisplayName("constrainToRange 值小于最小值")
        void testConstrainBelowMin() {
            assertThat(Floats.constrainToRange(0.0f, 1.0f, 10.0f)).isEqualTo(1.0f);
        }

        @Test
        @DisplayName("constrainToRange 值大于最大值")
        void testConstrainAboveMax() {
            assertThat(Floats.constrainToRange(15.0f, 1.0f, 10.0f)).isEqualTo(10.0f);
        }
    }

    @Nested
    @DisplayName("比较测试")
    class CompareTests {

        @Test
        @DisplayName("compare 相等")
        void testCompareEqual() {
            assertThat(Floats.compare(5.0f, 5.0f)).isEqualTo(0);
        }

        @Test
        @DisplayName("compare 小于")
        void testCompareLess() {
            assertThat(Floats.compare(3.0f, 5.0f)).isLessThan(0);
        }

        @Test
        @DisplayName("compare 大于")
        void testCompareGreater() {
            assertThat(Floats.compare(7.0f, 5.0f)).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("集合转换测试")
    class CollectionTests {

        @Test
        @DisplayName("asList 转换")
        void testAsList() {
            List<Float> list = Floats.asList(1.0f, 2.0f, 3.0f);
            assertThat(list).containsExactly(1.0f, 2.0f, 3.0f);
        }

        @Test
        @DisplayName("asList 空数组")
        void testAsListEmpty() {
            List<Float> list = Floats.asList();
            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("toArray 转换")
        void testToArray() {
            List<Float> list = Arrays.asList(1.0f, 2.0f, 3.0f);
            float[] array = Floats.toArray(list);
            assertThat(array).containsExactly(1.0f, 2.0f, 3.0f);
        }
    }

    @Nested
    @DisplayName("数组反转测试")
    class ReverseTests {

        @Test
        @DisplayName("reverse 反转整个数组")
        void testReverse() {
            float[] array = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f};
            Floats.reverse(array);
            assertThat(array).containsExactly(5.0f, 4.0f, 3.0f, 2.0f, 1.0f);
        }
    }

    @Nested
    @DisplayName("isFinite 测试")
    class IsFiniteTests {

        @Test
        @DisplayName("有限值")
        void testFinite() {
            assertThat(Floats.isFinite(3.14f)).isTrue();
            assertThat(Floats.isFinite(0.0f)).isTrue();
            assertThat(Floats.isFinite(-100.0f)).isTrue();
        }

        @Test
        @DisplayName("无穷大")
        void testInfinite() {
            assertThat(Floats.isFinite(Float.POSITIVE_INFINITY)).isFalse();
            assertThat(Floats.isFinite(Float.NEGATIVE_INFINITY)).isFalse();
        }

        @Test
        @DisplayName("NaN")
        void testNaN() {
            assertThat(Floats.isFinite(Float.NaN)).isFalse();
        }
    }

    @Nested
    @DisplayName("字符串转换测试")
    class StringTests {

        @Test
        @DisplayName("join 连接")
        void testJoin() {
            String result = Floats.join(", ", 1.0f, 2.0f, 3.0f);
            assertThat(result).isEqualTo("1.0, 2.0, 3.0");
        }

        @Test
        @DisplayName("join 空数组")
        void testJoinEmpty() {
            String result = Floats.join(", ");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("tryParse 有效数字")
        void testTryParseValid() {
            assertThat(Floats.tryParse("3.14")).isEqualTo(3.14f);
        }

        @Test
        @DisplayName("tryParse 无效数字")
        void testTryParseInvalid() {
            assertThat(Floats.tryParse("abc")).isNull();
        }

        @Test
        @DisplayName("tryParse null")
        void testTryParseNull() {
            assertThat(Floats.tryParse(null)).isNull();
        }

        @Test
        @DisplayName("tryParse 空字符串")
        void testTryParseEmpty() {
            assertThat(Floats.tryParse("")).isNull();
        }
    }

    @Nested
    @DisplayName("ensureNonNull 测试")
    class EnsureNonNullTests {

        @Test
        @DisplayName("非 null 数组")
        void testNonNull() {
            float[] array = {1.0f, 2.0f, 3.0f};
            assertThat(Floats.ensureNonNull(array)).isSameAs(array);
        }

        @Test
        @DisplayName("null 数组")
        void testNull() {
            assertThat(Floats.ensureNonNull(null)).isSameAs(Floats.EMPTY_ARRAY);
        }
    }
}
