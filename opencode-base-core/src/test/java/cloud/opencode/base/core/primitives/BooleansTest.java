package cloud.opencode.base.core.primitives;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Booleans 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("Booleans 测试")
class BooleansTest {

    @Nested
    @DisplayName("常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("EMPTY_ARRAY 常量")
        void testEmptyArray() {
            assertThat(Booleans.EMPTY_ARRAY).isEmpty();
        }
    }

    @Nested
    @DisplayName("数组操作测试")
    class ArrayOperationsTests {

        @Test
        @DisplayName("concat 合并数组")
        void testConcat() {
            boolean[] arr1 = {true, false};
            boolean[] arr2 = {false, true};
            boolean[] result = Booleans.concat(arr1, arr2);
            assertThat(result).containsExactly(true, false, false, true);
        }

        @Test
        @DisplayName("concat 空数组")
        void testConcatEmpty() {
            boolean[] arr1 = {};
            boolean[] arr2 = {true, false};
            boolean[] result = Booleans.concat(arr1, arr2);
            assertThat(result).containsExactly(true, false);
        }

        @Test
        @DisplayName("concat 多个数组")
        void testConcatMultiple() {
            boolean[] arr1 = {true};
            boolean[] arr2 = {false};
            boolean[] arr3 = {true};
            boolean[] result = Booleans.concat(arr1, arr2, arr3);
            assertThat(result).containsExactly(true, false, true);
        }

        @Test
        @DisplayName("contains 包含 true")
        void testContainsTrue() {
            boolean[] array = {false, false, true, false};
            assertThat(Booleans.contains(array, true)).isTrue();
        }

        @Test
        @DisplayName("contains 不包含 true")
        void testContainsTrueFalse() {
            boolean[] array = {false, false, false};
            assertThat(Booleans.contains(array, true)).isFalse();
        }

        @Test
        @DisplayName("contains 包含 false")
        void testContainsFalse() {
            boolean[] array = {true, true, false, true};
            assertThat(Booleans.contains(array, false)).isTrue();
        }

        @Test
        @DisplayName("indexOf 找到 true")
        void testIndexOfTrue() {
            boolean[] array = {false, false, true, false};
            assertThat(Booleans.indexOf(array, true)).isEqualTo(2);
        }

        @Test
        @DisplayName("indexOf 未找到")
        void testIndexOfNotFound() {
            boolean[] array = {false, false, false};
            assertThat(Booleans.indexOf(array, true)).isEqualTo(-1);
        }

        @Test
        @DisplayName("lastIndexOf 找到 true")
        void testLastIndexOfTrue() {
            boolean[] array = {false, true, false, true, false};
            assertThat(Booleans.lastIndexOf(array, true)).isEqualTo(3);
        }

        @Test
        @DisplayName("lastIndexOf 未找到")
        void testLastIndexOfNotFound() {
            boolean[] array = {false, false, false};
            assertThat(Booleans.lastIndexOf(array, true)).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("比较测试")
    class CompareTests {

        @Test
        @DisplayName("compare 相等 - false")
        void testCompareEqualFalse() {
            assertThat(Booleans.compare(false, false)).isEqualTo(0);
        }

        @Test
        @DisplayName("compare 相等 - true")
        void testCompareEqualTrue() {
            assertThat(Booleans.compare(true, true)).isEqualTo(0);
        }

        @Test
        @DisplayName("compare false < true")
        void testCompareFalseLessThanTrue() {
            assertThat(Booleans.compare(false, true)).isLessThan(0);
        }

        @Test
        @DisplayName("compare true > false")
        void testCompareTrueGreaterThanFalse() {
            assertThat(Booleans.compare(true, false)).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("集合转换测试")
    class CollectionTests {

        @Test
        @DisplayName("asList 转换")
        void testAsList() {
            List<Boolean> list = Booleans.asList(true, false, true);
            assertThat(list).containsExactly(true, false, true);
        }

        @Test
        @DisplayName("asList 空数组")
        void testAsListEmpty() {
            List<Boolean> list = Booleans.asList();
            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("toArray 转换")
        void testToArray() {
            List<Boolean> list = Arrays.asList(true, false, true);
            boolean[] array = Booleans.toArray(list);
            assertThat(array).containsExactly(true, false, true);
        }

        @Test
        @DisplayName("toArray 包含 null")
        void testToArrayWithNull() {
            List<Boolean> list = Arrays.asList(true, null, false);
            boolean[] array = Booleans.toArray(list);
            assertThat(array).containsExactly(true, false, false);
        }
    }

    @Nested
    @DisplayName("数组反转测试")
    class ReverseTests {

        @Test
        @DisplayName("reverse 反转整个数组")
        void testReverse() {
            boolean[] array = {true, false, true, false, true};
            Booleans.reverse(array);
            assertThat(array).containsExactly(true, false, true, false, true);
        }

        @Test
        @DisplayName("reverse 非对称数组")
        void testReverseAsymmetric() {
            boolean[] array = {true, true, false};
            Booleans.reverse(array);
            assertThat(array).containsExactly(false, true, true);
        }

        @Test
        @DisplayName("reverse 空数组")
        void testReverseEmpty() {
            boolean[] array = {};
            Booleans.reverse(array);
            assertThat(array).isEmpty();
        }
    }

    @Nested
    @DisplayName("统计测试")
    class CountTests {

        @Test
        @DisplayName("countTrue 统计 true 数量")
        void testCountTrue() {
            boolean[] array = {true, false, true, true, false};
            assertThat(Booleans.countTrue(array)).isEqualTo(3);
        }

        @Test
        @DisplayName("countTrue 全是 false")
        void testCountTrueAllFalse() {
            boolean[] array = {false, false, false};
            assertThat(Booleans.countTrue(array)).isEqualTo(0);
        }

        @Test
        @DisplayName("countTrue 全是 true")
        void testCountTrueAllTrue() {
            boolean[] array = {true, true, true};
            assertThat(Booleans.countTrue(array)).isEqualTo(3);
        }

        @Test
        @DisplayName("countTrue 空数组")
        void testCountTrueEmpty() {
            boolean[] array = {};
            assertThat(Booleans.countTrue(array)).isEqualTo(0);
        }

        @Test
        @DisplayName("countFalse 统计 false 数量")
        void testCountFalse() {
            boolean[] array = {true, false, true, true, false};
            assertThat(Booleans.countFalse(array)).isEqualTo(2);
        }

        @Test
        @DisplayName("countFalse 全是 true")
        void testCountFalseAllTrue() {
            boolean[] array = {true, true, true};
            assertThat(Booleans.countFalse(array)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("逻辑运算测试")
    class LogicalOperationsTests {

        @Test
        @DisplayName("and 全 true")
        void testAndAllTrue() {
            assertThat(Booleans.and(true, true, true)).isTrue();
        }

        @Test
        @DisplayName("and 有 false")
        void testAndWithFalse() {
            assertThat(Booleans.and(true, false, true)).isFalse();
        }

        @Test
        @DisplayName("and 空数组")
        void testAndEmpty() {
            assertThat(Booleans.and()).isTrue();
        }

        @Test
        @DisplayName("or 全 false")
        void testOrAllFalse() {
            assertThat(Booleans.or(false, false, false)).isFalse();
        }

        @Test
        @DisplayName("or 有 true")
        void testOrWithTrue() {
            assertThat(Booleans.or(false, true, false)).isTrue();
        }

        @Test
        @DisplayName("or 空数组")
        void testOrEmpty() {
            assertThat(Booleans.or()).isFalse();
        }

        @Test
        @DisplayName("xor 奇数个 true")
        void testXorOddTrue() {
            assertThat(Booleans.xor(true, true, true)).isTrue();
            assertThat(Booleans.xor(true, false, false)).isTrue();
        }

        @Test
        @DisplayName("xor 偶数个 true")
        void testXorEvenTrue() {
            assertThat(Booleans.xor(true, true, false)).isFalse();
            assertThat(Booleans.xor(false, false, false)).isFalse();
        }

        @Test
        @DisplayName("xor 单元素")
        void testXorSingle() {
            assertThat(Booleans.xor(true)).isTrue();
            assertThat(Booleans.xor(false)).isFalse();
        }
    }

    @Nested
    @DisplayName("字符串转换测试")
    class StringTests {

        @Test
        @DisplayName("join 连接")
        void testJoin() {
            String result = Booleans.join(", ", true, false, true);
            assertThat(result).isEqualTo("true, false, true");
        }

        @Test
        @DisplayName("join 空数组")
        void testJoinEmpty() {
            String result = Booleans.join(", ");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("join 单元素")
        void testJoinSingle() {
            String result = Booleans.join(", ", true);
            assertThat(result).isEqualTo("true");
        }
    }

    @Nested
    @DisplayName("ensureNonNull 测试")
    class EnsureNonNullTests {

        @Test
        @DisplayName("非 null 数组")
        void testNonNull() {
            boolean[] array = {true, false};
            assertThat(Booleans.ensureNonNull(array)).isSameAs(array);
        }

        @Test
        @DisplayName("null 数组")
        void testNull() {
            assertThat(Booleans.ensureNonNull(null)).isSameAs(Booleans.EMPTY_ARRAY);
        }
    }
}
