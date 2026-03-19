package cloud.opencode.base.core.primitives;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Longs 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("Longs 测试")
class LongsTest {

    @Nested
    @DisplayName("常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("BYTES 常量")
        void testBytesConstant() {
            assertThat(Longs.BYTES).isEqualTo(8);
        }

        @Test
        @DisplayName("MAX_POWER_OF_TWO 常量")
        void testMaxPowerOfTwo() {
            assertThat(Longs.MAX_POWER_OF_TWO).isEqualTo(1L << 62);
        }

        @Test
        @DisplayName("EMPTY_ARRAY 常量")
        void testEmptyArray() {
            assertThat(Longs.EMPTY_ARRAY).isEmpty();
        }
    }

    @Nested
    @DisplayName("字节转换测试")
    class ByteConversionTests {

        @Test
        @DisplayName("toByteArray 转换")
        void testToByteArray() {
            byte[] bytes = Longs.toByteArray(0x0102030405060708L);
            assertThat(bytes).hasSize(8);
            assertThat(bytes[0]).isEqualTo((byte) 0x01);
            assertThat(bytes[7]).isEqualTo((byte) 0x08);
        }

        @Test
        @DisplayName("toByteArray 零")
        void testToByteArrayZero() {
            byte[] bytes = Longs.toByteArray(0L);
            assertThat(bytes).containsExactly(0, 0, 0, 0, 0, 0, 0, 0);
        }

        @Test
        @DisplayName("fromByteArray 转换")
        void testFromByteArray() {
            byte[] bytes = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};
            long value = Longs.fromByteArray(bytes);
            assertThat(value).isEqualTo(0x0102030405060708L);
        }

        @Test
        @DisplayName("fromByteArray 数组太短抛异常")
        void testFromByteArrayTooShort() {
            byte[] bytes = {0x01, 0x02, 0x03, 0x04};
            assertThatThrownBy(() -> Longs.fromByteArray(bytes))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("fromBytes 转换")
        void testFromBytes() {
            long value = Longs.fromBytes(
                    (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04,
                    (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08);
            assertThat(value).isEqualTo(0x0102030405060708L);
        }

        @Test
        @DisplayName("往返转换")
        void testRoundTrip() {
            long original = 123456789012345678L;
            byte[] bytes = Longs.toByteArray(original);
            long restored = Longs.fromByteArray(bytes);
            assertThat(restored).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("数组操作测试")
    class ArrayOperationsTests {

        @Test
        @DisplayName("concat 合并数组")
        void testConcat() {
            long[] arr1 = {1L, 2L};
            long[] arr2 = {3L, 4L};
            long[] result = Longs.concat(arr1, arr2);
            assertThat(result).containsExactly(1L, 2L, 3L, 4L);
        }

        @Test
        @DisplayName("contains 包含元素")
        void testContainsTrue() {
            long[] array = {1L, 2L, 3L, 4L, 5L};
            assertThat(Longs.contains(array, 3L)).isTrue();
        }

        @Test
        @DisplayName("contains 不包含元素")
        void testContainsFalse() {
            long[] array = {1L, 2L, 3L, 4L, 5L};
            assertThat(Longs.contains(array, 6L)).isFalse();
        }

        @Test
        @DisplayName("indexOf 找到元素")
        void testIndexOfFound() {
            long[] array = {1L, 2L, 3L, 4L, 5L};
            assertThat(Longs.indexOf(array, 3L)).isEqualTo(2);
        }

        @Test
        @DisplayName("indexOf 指定范围")
        void testIndexOfWithRange() {
            long[] array = {1L, 2L, 3L, 2L, 5L};
            assertThat(Longs.indexOf(array, 2L, 2, 5)).isEqualTo(3);
        }

        @Test
        @DisplayName("lastIndexOf 找到元素")
        void testLastIndexOfFound() {
            long[] array = {1L, 2L, 3L, 2L, 5L};
            assertThat(Longs.lastIndexOf(array, 2L)).isEqualTo(3);
        }

        @Test
        @DisplayName("lastIndexOf 指定范围")
        void testLastIndexOfWithRange() {
            long[] array = {1L, 2L, 3L, 2L, 5L};
            assertThat(Longs.lastIndexOf(array, 2L, 0, 3)).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("最值测试")
    class MinMaxTests {

        @Test
        @DisplayName("min 返回最小值")
        void testMin() {
            assertThat(Longs.min(3L, 1L, 4L, 1L, 5L)).isEqualTo(1L);
        }

        @Test
        @DisplayName("min 空数组抛异常")
        void testMinEmpty() {
            assertThatThrownBy(() -> Longs.min())
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("max 返回最大值")
        void testMax() {
            assertThat(Longs.max(3L, 1L, 4L, 1L, 5L)).isEqualTo(5L);
        }

        @Test
        @DisplayName("max 空数组抛异常")
        void testMaxEmpty() {
            assertThatThrownBy(() -> Longs.max())
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("约束测试")
    class ConstraintTests {

        @Test
        @DisplayName("constrainToRange 值在范围内")
        void testConstrainInRange() {
            assertThat(Longs.constrainToRange(5L, 1L, 10L)).isEqualTo(5L);
        }

        @Test
        @DisplayName("constrainToRange 值小于最小值")
        void testConstrainBelowMin() {
            assertThat(Longs.constrainToRange(0L, 1L, 10L)).isEqualTo(1L);
        }

        @Test
        @DisplayName("constrainToRange 值大于最大值")
        void testConstrainAboveMax() {
            assertThat(Longs.constrainToRange(15L, 1L, 10L)).isEqualTo(10L);
        }

        @Test
        @DisplayName("constrainToRange min > max 抛异常")
        void testConstrainInvalidRange() {
            assertThatThrownBy(() -> Longs.constrainToRange(5L, 10L, 1L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("比较测试")
    class CompareTests {

        @Test
        @DisplayName("compare 相等")
        void testCompareEqual() {
            assertThat(Longs.compare(5L, 5L)).isEqualTo(0);
        }

        @Test
        @DisplayName("compare 小于")
        void testCompareLess() {
            assertThat(Longs.compare(3L, 5L)).isLessThan(0);
        }

        @Test
        @DisplayName("compare 大于")
        void testCompareGreater() {
            assertThat(Longs.compare(7L, 5L)).isGreaterThan(0);
        }

        @Test
        @DisplayName("lexicographicalComparator 比较数组")
        void testLexicographicalComparator() {
            Comparator<long[]> comp = Longs.lexicographicalComparator();
            long[] arr1 = {1L, 2L, 3L};
            long[] arr2 = {1L, 2L, 4L};

            assertThat(comp.compare(arr1, arr2)).isLessThan(0);
            assertThat(comp.compare(arr1, arr1)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("集合转换测试")
    class CollectionTests {

        @Test
        @DisplayName("asList 转换")
        void testAsList() {
            List<Long> list = Longs.asList(1L, 2L, 3L);
            assertThat(list).containsExactly(1L, 2L, 3L);
        }

        @Test
        @DisplayName("asList 空数组")
        void testAsListEmpty() {
            List<Long> list = Longs.asList();
            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("toArray 转换")
        void testToArray() {
            List<Long> list = Arrays.asList(1L, 2L, 3L);
            long[] array = Longs.toArray(list);
            assertThat(array).containsExactly(1L, 2L, 3L);
        }
    }

    @Nested
    @DisplayName("数组反转测试")
    class ReverseTests {

        @Test
        @DisplayName("reverse 反转整个数组")
        void testReverse() {
            long[] array = {1L, 2L, 3L, 4L, 5L};
            Longs.reverse(array);
            assertThat(array).containsExactly(5L, 4L, 3L, 2L, 1L);
        }

        @Test
        @DisplayName("reverse 反转指定范围")
        void testReverseRange() {
            long[] array = {1L, 2L, 3L, 4L, 5L};
            Longs.reverse(array, 1, 4);
            assertThat(array).containsExactly(1L, 4L, 3L, 2L, 5L);
        }

        @Test
        @DisplayName("rotate 向右旋转")
        void testRotateRight() {
            long[] array = {1L, 2L, 3L, 4L, 5L};
            Longs.rotate(array, 2);
            assertThat(array).containsExactly(4L, 5L, 1L, 2L, 3L);
        }

        @Test
        @DisplayName("rotate 向左旋转")
        void testRotateLeft() {
            long[] array = {1L, 2L, 3L, 4L, 5L};
            Longs.rotate(array, -2);
            assertThat(array).containsExactly(3L, 4L, 5L, 1L, 2L);
        }
    }

    @Nested
    @DisplayName("排序测试")
    class SortTests {

        @Test
        @DisplayName("sortDescending 降序排序")
        void testSortDescending() {
            long[] array = {1L, 5L, 3L, 2L, 4L};
            Longs.sortDescending(array);
            assertThat(array).containsExactly(5L, 4L, 3L, 2L, 1L);
        }

        @Test
        @DisplayName("sortDescending 指定范围")
        void testSortDescendingRange() {
            long[] array = {1L, 5L, 3L, 2L, 4L};
            Longs.sortDescending(array, 1, 4);
            assertThat(array).containsExactly(1L, 5L, 3L, 2L, 4L);
        }

        @Test
        @DisplayName("isSorted 已排序")
        void testIsSortedTrue() {
            long[] array = {1L, 2L, 3L, 4L, 5L};
            assertThat(Longs.isSorted(array)).isTrue();
        }

        @Test
        @DisplayName("isSorted 未排序")
        void testIsSortedFalse() {
            long[] array = {1L, 3L, 2L, 4L, 5L};
            assertThat(Longs.isSorted(array)).isFalse();
        }
    }

    @Nested
    @DisplayName("字符串转换测试")
    class StringTests {

        @Test
        @DisplayName("join 连接")
        void testJoin() {
            String result = Longs.join(", ", 1L, 2L, 3L);
            assertThat(result).isEqualTo("1, 2, 3");
        }

        @Test
        @DisplayName("join 空数组")
        void testJoinEmpty() {
            String result = Longs.join(", ");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("tryParse 有效数字")
        void testTryParseValid() {
            assertThat(Longs.tryParse("123456789012")).isEqualTo(123456789012L);
        }

        @Test
        @DisplayName("tryParse 无效数字")
        void testTryParseInvalid() {
            assertThat(Longs.tryParse("abc")).isNull();
        }

        @Test
        @DisplayName("tryParse null")
        void testTryParseNull() {
            assertThat(Longs.tryParse(null)).isNull();
        }

        @Test
        @DisplayName("tryParse 指定进制")
        void testTryParseRadix() {
            assertThat(Longs.tryParse("FF", 16)).isEqualTo(255L);
        }
    }

    @Nested
    @DisplayName("ensureNonNull 测试")
    class EnsureNonNullTests {

        @Test
        @DisplayName("非 null 数组")
        void testNonNull() {
            long[] array = {1L, 2L, 3L};
            assertThat(Longs.ensureNonNull(array)).isSameAs(array);
        }

        @Test
        @DisplayName("null 数组")
        void testNull() {
            assertThat(Longs.ensureNonNull(null)).isSameAs(Longs.EMPTY_ARRAY);
        }
    }
}
