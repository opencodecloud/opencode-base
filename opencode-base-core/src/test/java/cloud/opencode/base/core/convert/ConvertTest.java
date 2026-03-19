package cloud.opencode.base.core.convert;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Convert 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("Convert 测试")
class ConvertTest {

    @Nested
    @DisplayName("整数转换测试")
    class IntegerConversionTests {

        @Test
        @DisplayName("toInt 字符串")
        void testToIntString() {
            assertThat(Convert.toInt("123")).isEqualTo(123);
            assertThat(Convert.toInt("-456")).isEqualTo(-456);
            assertThat(Convert.toInt("0")).isEqualTo(0);
        }

        @Test
        @DisplayName("toInt 带默认值")
        void testToIntWithDefault() {
            assertThat(Convert.toInt("invalid", 0)).isEqualTo(0);
            assertThat(Convert.toInt(null, 99)).isEqualTo(99);
            assertThat(Convert.toInt("", 42)).isEqualTo(42);
        }

        @Test
        @DisplayName("toInt 其他数字类型")
        void testToIntFromNumber() {
            assertThat(Convert.toInt(123.9)).isEqualTo(123);
            assertThat(Convert.toInt(100L)).isEqualTo(100);
            assertThat(Convert.toInt(50.5f)).isEqualTo(50);
        }

        @Test
        @DisplayName("toLong")
        void testToLong() {
            assertThat(Convert.toLong("123456789012")).isEqualTo(123456789012L);
            assertThat(Convert.toLong(100)).isEqualTo(100L);
            assertThat(Convert.toLong("invalid", 0L)).isEqualTo(0L);
        }

        @Test
        @DisplayName("toShort")
        void testToShort() {
            assertThat(Convert.toShort("32767")).isEqualTo((short) 32767);
            assertThat(Convert.toShort(100)).isEqualTo((short) 100);
            assertThat(Convert.toShort(null, (short) 0)).isEqualTo((short) 0);
        }

        @Test
        @DisplayName("toByte")
        void testToByte() {
            assertThat(Convert.toByte("127")).isEqualTo((byte) 127);
            assertThat(Convert.toByte(50)).isEqualTo((byte) 50);
            assertThat(Convert.toByte(null, (byte) 0)).isEqualTo((byte) 0);
        }
    }

    @Nested
    @DisplayName("浮点数转换测试")
    class FloatConversionTests {

        @Test
        @DisplayName("toDouble")
        void testToDouble() {
            assertThat(Convert.toDouble("3.14159")).isEqualTo(3.14159);
            assertThat(Convert.toDouble(100)).isEqualTo(100.0);
            assertThat(Convert.toDouble("invalid", 0.0)).isEqualTo(0.0);
            assertThat(Convert.toDouble(null)).isNull();
        }

        @Test
        @DisplayName("toFloat")
        void testToFloat() {
            assertThat(Convert.toFloat("3.14")).isEqualTo(3.14f);
            assertThat(Convert.toFloat(100)).isEqualTo(100.0f);
            assertThat(Convert.toFloat("invalid", 0.0f)).isEqualTo(0.0f);
        }
    }

    @Nested
    @DisplayName("布尔转换测试")
    class BooleanConversionTests {

        @Test
        @DisplayName("toBool 各种 true 值")
        void testToBoolTrue() {
            assertThat(Convert.toBool("true")).isTrue();
            assertThat(Convert.toBool("TRUE")).isTrue();
            assertThat(Convert.toBool("1")).isTrue();
            assertThat(Convert.toBool("yes")).isTrue();
            assertThat(Convert.toBool("on")).isTrue();
            assertThat(Convert.toBool("y")).isTrue();
            assertThat(Convert.toBool(1)).isTrue();
        }

        @Test
        @DisplayName("toBool 各种 false 值")
        void testToBoolFalse() {
            assertThat(Convert.toBool("false")).isFalse();
            assertThat(Convert.toBool("FALSE")).isFalse();
            assertThat(Convert.toBool("0")).isFalse();
            assertThat(Convert.toBool("no")).isFalse();
            assertThat(Convert.toBool("off")).isFalse();
            assertThat(Convert.toBool("n")).isFalse();
            assertThat(Convert.toBool(0)).isFalse();
        }

        @Test
        @DisplayName("toBool 无效值返回默认")
        void testToBoolInvalid() {
            assertThat(Convert.toBool("invalid", false)).isFalse();
            assertThat(Convert.toBool(null, true)).isTrue();
            assertThat(Convert.toBool(null)).isNull();
        }
    }

    @Nested
    @DisplayName("字符转换测试")
    class CharConversionTests {

        @Test
        @DisplayName("toChar")
        void testToChar() {
            assertThat(Convert.toChar("A")).isEqualTo('A');
            assertThat(Convert.toChar("Hello")).isEqualTo('H');
            assertThat(Convert.toChar(65)).isEqualTo('A');
            assertThat(Convert.toChar("", 'X')).isEqualTo('X');
            assertThat(Convert.toChar(null)).isNull();
        }
    }

    @Nested
    @DisplayName("字符串转换测试")
    class StringConversionTests {

        @Test
        @DisplayName("toStr")
        void testToStr() {
            assertThat(Convert.toStr(123)).isEqualTo("123");
            assertThat(Convert.toStr(true)).isEqualTo("true");
            assertThat(Convert.toStr(3.14)).isEqualTo("3.14");
            assertThat(Convert.toStr(null)).isNull();
            assertThat(Convert.toStr(null, "default")).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("数组转换测试")
    class ArrayConversionTests {

        @Test
        @DisplayName("toIntArray 字符串")
        void testToIntArrayFromString() {
            assertThat(Convert.toIntArray("1,2,3")).containsExactly(1, 2, 3);
            assertThat(Convert.toIntArray("10")).containsExactly(10);
            assertThat(Convert.toIntArray("")).isEmpty();
        }

        @Test
        @DisplayName("toIntArray 数组")
        void testToIntArrayFromArray() {
            assertThat(Convert.toIntArray(new int[]{1, 2, 3})).containsExactly(1, 2, 3);
            assertThat(Convert.toIntArray(new Integer[]{1, 2, 3})).containsExactly(1, 2, 3);
            assertThat(Convert.toIntArray(new String[]{"1", "2", "3"})).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("toIntArray 集合")
        void testToIntArrayFromCollection() {
            assertThat(Convert.toIntArray(List.of(1, 2, 3))).containsExactly(1, 2, 3);
            assertThat(Convert.toIntArray(Set.of(1))).containsExactly(1);
        }

        @Test
        @DisplayName("toIntArray null")
        void testToIntArrayNull() {
            assertThat(Convert.toIntArray(null)).isEmpty();
        }

        @Test
        @DisplayName("toLongArray")
        void testToLongArray() {
            assertThat(Convert.toLongArray("1,2,3")).containsExactly(1L, 2L, 3L);
            assertThat(Convert.toLongArray(new long[]{1L, 2L})).containsExactly(1L, 2L);
            assertThat(Convert.toLongArray(List.of(1L, 2L, 3L))).containsExactly(1L, 2L, 3L);
            assertThat(Convert.toLongArray(null)).isEmpty();
        }

        @Test
        @DisplayName("toStrArray")
        void testToStrArray() {
            assertThat(Convert.toStrArray("a,b,c")).containsExactly("a", "b", "c");
            assertThat(Convert.toStrArray(new String[]{"x", "y"})).containsExactly("x", "y");
            assertThat(Convert.toStrArray(List.of("a", "b"))).containsExactly("a", "b");
            assertThat(Convert.toStrArray(123)).containsExactly("123");
            assertThat(Convert.toStrArray(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("集合转换测试")
    class CollectionConversionTests {

        @Test
        @DisplayName("toList")
        void testToList() {
            List<Integer> result = Convert.toList("1,2,3", Integer.class);
            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("toList 从数组")
        void testToListFromArray() {
            List<String> result = Convert.toList(new String[]{"a", "b"}, String.class);
            assertThat(result).containsExactly("a", "b");
        }

        @Test
        @DisplayName("toList 从集合")
        void testToListFromCollection() {
            List<Integer> result = Convert.toList(Set.of(1, 2, 3), Integer.class);
            assertThat(result).containsExactlyInAnyOrder(1, 2, 3);
        }

        @Test
        @DisplayName("toList null")
        void testToListNull() {
            assertThat(Convert.toList(null, String.class)).isEmpty();
        }

        @Test
        @DisplayName("toSet")
        void testToSet() {
            Set<Integer> result = Convert.toSet("1,2,2,3", Integer.class);
            assertThat(result).containsExactlyInAnyOrder(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("泛型转换测试")
    class GenericConversionTests {

        @Test
        @DisplayName("convert Class")
        void testConvertClass() {
            assertThat(Convert.convert("123", Integer.class)).isEqualTo(123);
            assertThat(Convert.convert(123, String.class)).isEqualTo("123");
            assertThat(Convert.convert(null, String.class)).isNull();
        }

        @Test
        @DisplayName("convert TypeReference")
        void testConvertTypeReference() {
            TypeReference<Integer> ref = new TypeReference<Integer>() {};
            assertThat(Convert.convert("123", ref)).isEqualTo(123);
        }

        @Test
        @DisplayName("convert TypeReference null")
        void testConvertTypeReferenceNull() {
            TypeReference<String> ref = new TypeReference<String>() {};
            assertThat(Convert.convert(null, ref)).isNull();
        }
    }
}
