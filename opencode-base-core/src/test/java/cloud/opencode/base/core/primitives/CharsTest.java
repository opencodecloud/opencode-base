package cloud.opencode.base.core.primitives;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Chars 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("Chars 测试")
class CharsTest {

    @Nested
    @DisplayName("常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("BYTES 常量")
        void testBytesConstant() {
            assertThat(Chars.BYTES).isEqualTo(2);
        }

        @Test
        @DisplayName("EMPTY_ARRAY 常量")
        void testEmptyArray() {
            assertThat(Chars.EMPTY_ARRAY).isEmpty();
        }
    }

    @Nested
    @DisplayName("字节转换测试")
    class ByteConversionTests {

        @Test
        @DisplayName("toByteArray 转换")
        void testToByteArray() {
            byte[] bytes = Chars.toByteArray('A');
            assertThat(bytes).hasSize(2);
            assertThat(bytes[0]).isEqualTo((byte) 0x00);
            assertThat(bytes[1]).isEqualTo((byte) 0x41);
        }

        @Test
        @DisplayName("toByteArray 中文字符")
        void testToByteArrayChinese() {
            byte[] bytes = Chars.toByteArray('中');
            assertThat(bytes).hasSize(2);
        }

        @Test
        @DisplayName("fromByteArray 转换")
        void testFromByteArray() {
            byte[] bytes = {0x00, 0x41};
            char value = Chars.fromByteArray(bytes);
            assertThat(value).isEqualTo('A');
        }

        @Test
        @DisplayName("往返转换")
        void testRoundTrip() {
            char original = 'Z';
            byte[] bytes = Chars.toByteArray(original);
            char restored = Chars.fromByteArray(bytes);
            assertThat(restored).isEqualTo(original);
        }

        @Test
        @DisplayName("往返转换中文")
        void testRoundTripChinese() {
            char original = '中';
            byte[] bytes = Chars.toByteArray(original);
            char restored = Chars.fromByteArray(bytes);
            assertThat(restored).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("数组操作测试")
    class ArrayOperationsTests {

        @Test
        @DisplayName("concat 合并数组")
        void testConcat() {
            char[] arr1 = {'a', 'b'};
            char[] arr2 = {'c', 'd'};
            char[] result = Chars.concat(arr1, arr2);
            assertThat(result).containsExactly('a', 'b', 'c', 'd');
        }

        @Test
        @DisplayName("concat 空数组")
        void testConcatEmpty() {
            char[] arr1 = {};
            char[] arr2 = {'a', 'b'};
            char[] result = Chars.concat(arr1, arr2);
            assertThat(result).containsExactly('a', 'b');
        }

        @Test
        @DisplayName("concat 多个数组")
        void testConcatMultiple() {
            char[] arr1 = {'a'};
            char[] arr2 = {'b'};
            char[] arr3 = {'c'};
            char[] result = Chars.concat(arr1, arr2, arr3);
            assertThat(result).containsExactly('a', 'b', 'c');
        }

        @Test
        @DisplayName("contains 包含元素")
        void testContainsTrue() {
            char[] array = {'a', 'b', 'c', 'd', 'e'};
            assertThat(Chars.contains(array, 'c')).isTrue();
        }

        @Test
        @DisplayName("contains 不包含元素")
        void testContainsFalse() {
            char[] array = {'a', 'b', 'c', 'd', 'e'};
            assertThat(Chars.contains(array, 'z')).isFalse();
        }

        @Test
        @DisplayName("indexOf 找到元素")
        void testIndexOfFound() {
            char[] array = {'a', 'b', 'c', 'd', 'e'};
            assertThat(Chars.indexOf(array, 'c')).isEqualTo(2);
        }

        @Test
        @DisplayName("indexOf 未找到元素")
        void testIndexOfNotFound() {
            char[] array = {'a', 'b', 'c', 'd', 'e'};
            assertThat(Chars.indexOf(array, 'z')).isEqualTo(-1);
        }

        @Test
        @DisplayName("lastIndexOf 找到元素")
        void testLastIndexOfFound() {
            char[] array = {'a', 'b', 'c', 'b', 'e'};
            assertThat(Chars.lastIndexOf(array, 'b')).isEqualTo(3);
        }

        @Test
        @DisplayName("lastIndexOf 未找到元素")
        void testLastIndexOfNotFound() {
            char[] array = {'a', 'b', 'c'};
            assertThat(Chars.lastIndexOf(array, 'z')).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("最值测试")
    class MinMaxTests {

        @Test
        @DisplayName("min 返回最小值")
        void testMin() {
            assertThat(Chars.min('c', 'a', 'd', 'b', 'e')).isEqualTo('a');
        }

        @Test
        @DisplayName("min 单元素")
        void testMinSingle() {
            assertThat(Chars.min('x')).isEqualTo('x');
        }

        @Test
        @DisplayName("min 空数组抛异常")
        void testMinEmpty() {
            assertThatThrownBy(() -> Chars.min())
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("max 返回最大值")
        void testMax() {
            assertThat(Chars.max('c', 'a', 'd', 'b', 'e')).isEqualTo('e');
        }

        @Test
        @DisplayName("max 空数组抛异常")
        void testMaxEmpty() {
            assertThatThrownBy(() -> Chars.max())
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("min/max 数字字符")
        void testMinMaxDigits() {
            assertThat(Chars.min('9', '0', '5')).isEqualTo('0');
            assertThat(Chars.max('9', '0', '5')).isEqualTo('9');
        }
    }

    @Nested
    @DisplayName("约束测试")
    class ConstraintTests {

        @Test
        @DisplayName("constrainToRange 值在范围内")
        void testConstrainInRange() {
            assertThat(Chars.constrainToRange('m', 'a', 'z')).isEqualTo('m');
        }

        @Test
        @DisplayName("constrainToRange 值小于最小值")
        void testConstrainBelowMin() {
            assertThat(Chars.constrainToRange('A', 'a', 'z')).isEqualTo('a');
        }

        @Test
        @DisplayName("constrainToRange 值大于最大值")
        void testConstrainAboveMax() {
            assertThat(Chars.constrainToRange('z', 'a', 'm')).isEqualTo('m');
        }
    }

    @Nested
    @DisplayName("比较测试")
    class CompareTests {

        @Test
        @DisplayName("compare 相等")
        void testCompareEqual() {
            assertThat(Chars.compare('a', 'a')).isEqualTo(0);
        }

        @Test
        @DisplayName("compare 小于")
        void testCompareLess() {
            assertThat(Chars.compare('a', 'b')).isLessThan(0);
        }

        @Test
        @DisplayName("compare 大于")
        void testCompareGreater() {
            assertThat(Chars.compare('b', 'a')).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("集合转换测试")
    class CollectionTests {

        @Test
        @DisplayName("asList 转换")
        void testAsList() {
            List<Character> list = Chars.asList('a', 'b', 'c');
            assertThat(list).containsExactly('a', 'b', 'c');
        }

        @Test
        @DisplayName("asList 空数组")
        void testAsListEmpty() {
            List<Character> list = Chars.asList();
            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("toArray 转换")
        void testToArray() {
            List<Character> list = Arrays.asList('a', 'b', 'c');
            char[] array = Chars.toArray(list);
            assertThat(array).containsExactly('a', 'b', 'c');
        }
    }

    @Nested
    @DisplayName("数组反转测试")
    class ReverseTests {

        @Test
        @DisplayName("reverse 反转整个数组")
        void testReverse() {
            char[] array = {'a', 'b', 'c', 'd', 'e'};
            Chars.reverse(array);
            assertThat(array).containsExactly('e', 'd', 'c', 'b', 'a');
        }

        @Test
        @DisplayName("reverse 空数组")
        void testReverseEmpty() {
            char[] array = {};
            Chars.reverse(array);
            assertThat(array).isEmpty();
        }

        @Test
        @DisplayName("reverse 单元素")
        void testReverseSingle() {
            char[] array = {'a'};
            Chars.reverse(array);
            assertThat(array).containsExactly('a');
        }
    }

    @Nested
    @DisplayName("字符串转换测试")
    class StringTests {

        @Test
        @DisplayName("join 连接")
        void testJoin() {
            String result = Chars.join(", ", 'a', 'b', 'c');
            assertThat(result).isEqualTo("a, b, c");
        }

        @Test
        @DisplayName("join 空数组")
        void testJoinEmpty() {
            String result = Chars.join(", ");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("join 单元素")
        void testJoinSingle() {
            String result = Chars.join(", ", 'a');
            assertThat(result).isEqualTo("a");
        }

        @Test
        @DisplayName("join 中文字符")
        void testJoinChinese() {
            String result = Chars.join("-", '中', '文');
            assertThat(result).isEqualTo("中-文");
        }
    }

    @Nested
    @DisplayName("ensureNonNull 测试")
    class EnsureNonNullTests {

        @Test
        @DisplayName("非 null 数组")
        void testNonNull() {
            char[] array = {'a', 'b', 'c'};
            assertThat(Chars.ensureNonNull(array)).isSameAs(array);
        }

        @Test
        @DisplayName("null 数组")
        void testNull() {
            assertThat(Chars.ensureNonNull(null)).isSameAs(Chars.EMPTY_ARRAY);
        }
    }
}
