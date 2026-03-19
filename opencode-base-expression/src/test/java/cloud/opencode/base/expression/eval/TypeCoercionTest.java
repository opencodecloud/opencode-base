package cloud.opencode.base.expression.eval;

import cloud.opencode.base.expression.OpenExpressionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TypeCoercion Tests
 * TypeCoercion 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("TypeCoercion Tests | TypeCoercion 测试")
class TypeCoercionTest {

    @Nested
    @DisplayName("ToBoolean Tests | toBoolean 测试")
    class ToBooleanTests {

        @Test
        @DisplayName("null to false | null 转为 false")
        void testNullToFalse() {
            assertThat(TypeCoercion.toBoolean(null)).isFalse();
        }

        @Test
        @DisplayName("Boolean passthrough | Boolean 直接传递")
        void testBooleanPassthrough() {
            assertThat(TypeCoercion.toBoolean(true)).isTrue();
            assertThat(TypeCoercion.toBoolean(false)).isFalse();
        }

        @Test
        @DisplayName("Number zero to false | 数字零转为 false")
        void testNumberZeroToFalse() {
            assertThat(TypeCoercion.toBoolean(0)).isFalse();
            assertThat(TypeCoercion.toBoolean(0.0)).isFalse();
        }

        @Test
        @DisplayName("Number non-zero to true | 非零数字转为 true")
        void testNumberNonZeroToTrue() {
            assertThat(TypeCoercion.toBoolean(1)).isTrue();
            assertThat(TypeCoercion.toBoolean(-1)).isTrue();
            assertThat(TypeCoercion.toBoolean(3.14)).isTrue();
        }

        @Test
        @DisplayName("Empty string to false | 空字符串转为 false")
        void testEmptyStringToFalse() {
            assertThat(TypeCoercion.toBoolean("")).isFalse();
        }

        @Test
        @DisplayName("'false' string to false | 'false' 字符串转为 false")
        void testFalseStringToFalse() {
            assertThat(TypeCoercion.toBoolean("false")).isFalse();
            assertThat(TypeCoercion.toBoolean("FALSE")).isFalse();
        }

        @Test
        @DisplayName("Non-empty string to true | 非空字符串转为 true")
        void testNonEmptyStringToTrue() {
            assertThat(TypeCoercion.toBoolean("hello")).isTrue();
            assertThat(TypeCoercion.toBoolean("true")).isTrue();
        }

        @Test
        @DisplayName("Empty collection to false | 空集合转为 false")
        void testEmptyCollectionToFalse() {
            assertThat(TypeCoercion.toBoolean(List.of())).isFalse();
        }

        @Test
        @DisplayName("Non-empty collection to true | 非空集合转为 true")
        void testNonEmptyCollectionToTrue() {
            assertThat(TypeCoercion.toBoolean(List.of(1, 2, 3))).isTrue();
        }

        @Test
        @DisplayName("Empty map to false | 空 Map 转为 false")
        void testEmptyMapToFalse() {
            assertThat(TypeCoercion.toBoolean(Map.of())).isFalse();
        }

        @Test
        @DisplayName("Non-empty map to true | 非空 Map 转为 true")
        void testNonEmptyMapToTrue() {
            assertThat(TypeCoercion.toBoolean(Map.of("k", "v"))).isTrue();
        }

        @Test
        @DisplayName("Object to true | 对象转为 true")
        void testObjectToTrue() {
            assertThat(TypeCoercion.toBoolean(new Object())).isTrue();
        }
    }

    @Nested
    @DisplayName("ToInt Tests | toInt 测试")
    class ToIntTests {

        @Test
        @DisplayName("null to 0 | null 转为 0")
        void testNullToZero() {
            assertThat(TypeCoercion.toInt(null)).isEqualTo(0);
        }

        @Test
        @DisplayName("Number to int | Number 转为 int")
        void testNumberToInt() {
            assertThat(TypeCoercion.toInt(42)).isEqualTo(42);
            assertThat(TypeCoercion.toInt(42L)).isEqualTo(42);
            assertThat(TypeCoercion.toInt(42.9)).isEqualTo(42);
        }

        @Test
        @DisplayName("String to int | String 转为 int")
        void testStringToInt() {
            assertThat(TypeCoercion.toInt("42")).isEqualTo(42);
            assertThat(TypeCoercion.toInt("  42  ")).isEqualTo(42);
        }

        @Test
        @DisplayName("Boolean to int | Boolean 转为 int")
        void testBooleanToInt() {
            assertThat(TypeCoercion.toInt(true)).isEqualTo(1);
            assertThat(TypeCoercion.toInt(false)).isEqualTo(0);
        }

        @Test
        @DisplayName("Invalid string throws | 无效字符串抛出异常")
        void testInvalidStringThrows() {
            assertThatThrownBy(() -> TypeCoercion.toInt("not a number"))
                    .isInstanceOf(OpenExpressionException.class);
        }

        @Test
        @DisplayName("Object throws | 对象抛出异常")
        void testObjectThrows() {
            assertThatThrownBy(() -> TypeCoercion.toInt(new Object()))
                    .isInstanceOf(OpenExpressionException.class);
        }
    }

    @Nested
    @DisplayName("ToLong Tests | toLong 测试")
    class ToLongTests {

        @Test
        @DisplayName("null to 0L | null 转为 0L")
        void testNullToZero() {
            assertThat(TypeCoercion.toLong(null)).isEqualTo(0L);
        }

        @Test
        @DisplayName("Number to long | Number 转为 long")
        void testNumberToLong() {
            assertThat(TypeCoercion.toLong(42)).isEqualTo(42L);
            assertThat(TypeCoercion.toLong(42L)).isEqualTo(42L);
            assertThat(TypeCoercion.toLong(42.9)).isEqualTo(42L);
        }

        @Test
        @DisplayName("String to long | String 转为 long")
        void testStringToLong() {
            assertThat(TypeCoercion.toLong("42")).isEqualTo(42L);
        }

        @Test
        @DisplayName("Boolean to long | Boolean 转为 long")
        void testBooleanToLong() {
            assertThat(TypeCoercion.toLong(true)).isEqualTo(1L);
            assertThat(TypeCoercion.toLong(false)).isEqualTo(0L);
        }

        @Test
        @DisplayName("Invalid throws | 无效值抛出异常")
        void testInvalidThrows() {
            assertThatThrownBy(() -> TypeCoercion.toLong("not a number"))
                    .isInstanceOf(OpenExpressionException.class);
            assertThatThrownBy(() -> TypeCoercion.toLong(new Object()))
                    .isInstanceOf(OpenExpressionException.class);
        }
    }

    @Nested
    @DisplayName("ToDouble Tests | toDouble 测试")
    class ToDoubleTests {

        @Test
        @DisplayName("null to 0.0 | null 转为 0.0")
        void testNullToZero() {
            assertThat(TypeCoercion.toDouble(null)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Number to double | Number 转为 double")
        void testNumberToDouble() {
            assertThat(TypeCoercion.toDouble(42)).isEqualTo(42.0);
            assertThat(TypeCoercion.toDouble(3.14)).isEqualTo(3.14);
        }

        @Test
        @DisplayName("String to double | String 转为 double")
        void testStringToDouble() {
            assertThat(TypeCoercion.toDouble("3.14")).isEqualTo(3.14);
        }

        @Test
        @DisplayName("Boolean to double | Boolean 转为 double")
        void testBooleanToDouble() {
            assertThat(TypeCoercion.toDouble(true)).isEqualTo(1.0);
            assertThat(TypeCoercion.toDouble(false)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Invalid throws | 无效值抛出异常")
        void testInvalidThrows() {
            assertThatThrownBy(() -> TypeCoercion.toDouble("not a number"))
                    .isInstanceOf(OpenExpressionException.class);
            assertThatThrownBy(() -> TypeCoercion.toDouble(new Object()))
                    .isInstanceOf(OpenExpressionException.class);
        }
    }

    @Nested
    @DisplayName("ToString Tests | toString 测试")
    class ToStringTests {

        @Test
        @DisplayName("null to 'null' | null 转为 'null'")
        void testNullToString() {
            assertThat(TypeCoercion.toString(null)).isEqualTo("null");
        }

        @Test
        @DisplayName("value to string | 值转为字符串")
        void testValueToString() {
            assertThat(TypeCoercion.toString(42)).isEqualTo("42");
            assertThat(TypeCoercion.toString("hello")).isEqualTo("hello");
        }
    }

    @Nested
    @DisplayName("Convert Tests | convert 测试")
    class ConvertTests {

        @Test
        @DisplayName("null returns null | null 返回 null")
        void testNullReturnsNull() {
            assertThat(TypeCoercion.convert(null, String.class)).isNull();
        }

        @Test
        @DisplayName("same type passthrough | 相同类型直接传递")
        void testSameTypePassthrough() {
            String str = "hello";
            assertThat(TypeCoercion.convert(str, String.class)).isSameAs(str);
        }

        @Test
        @DisplayName("to Boolean | 转为 Boolean")
        void testToBoolean() {
            assertThat(TypeCoercion.convert(1, Boolean.class)).isTrue();
            assertThat(TypeCoercion.convert(0, boolean.class)).isFalse();
        }

        @Test
        @DisplayName("to Integer | 转为 Integer")
        void testToInteger() {
            assertThat(TypeCoercion.convert("42", Integer.class)).isEqualTo(42);
            assertThat(TypeCoercion.convert(42.0, int.class)).isEqualTo(42);
        }

        @Test
        @DisplayName("to Long | 转为 Long")
        void testToLong() {
            assertThat(TypeCoercion.convert("42", Long.class)).isEqualTo(42L);
            assertThat(TypeCoercion.convert(42, long.class)).isEqualTo(42L);
        }

        @Test
        @DisplayName("to Double | 转为 Double")
        void testToDouble() {
            assertThat(TypeCoercion.convert("3.14", Double.class)).isEqualTo(3.14);
            assertThat(TypeCoercion.convert(3, double.class)).isEqualTo(3.0);
        }

        @Test
        @DisplayName("to Float | 转为 Float")
        void testToFloat() {
            assertThat(TypeCoercion.convert(3.14, Float.class)).isEqualTo(3.14f);
            assertThat(TypeCoercion.convert(3, float.class)).isEqualTo(3.0f);
        }

        @Test
        @DisplayName("to String | 转为 String")
        void testToString() {
            assertThat(TypeCoercion.convert(42, String.class)).isEqualTo("42");
        }

        @Test
        @DisplayName("to BigDecimal | 转为 BigDecimal")
        void testToBigDecimal() {
            assertThat(TypeCoercion.convert(3.14, BigDecimal.class)).isEqualByComparingTo("3.14");
            assertThat(TypeCoercion.convert("3.14", BigDecimal.class)).isEqualByComparingTo("3.14");
        }

        @Test
        @DisplayName("to BigInteger | 转为 BigInteger")
        void testToBigInteger() {
            assertThat(TypeCoercion.convert(42, BigInteger.class)).isEqualTo(BigInteger.valueOf(42));
            assertThat(TypeCoercion.convert("42", BigInteger.class)).isEqualTo(BigInteger.valueOf(42));
        }

        @Test
        @DisplayName("to LocalDate | 转为 LocalDate")
        void testToLocalDate() {
            assertThat(TypeCoercion.convert("2024-01-15", LocalDate.class))
                    .isEqualTo(LocalDate.of(2024, 1, 15));
            LocalDateTime ldt = LocalDateTime.of(2024, 1, 15, 10, 30);
            assertThat(TypeCoercion.convert(ldt, LocalDate.class))
                    .isEqualTo(LocalDate.of(2024, 1, 15));
        }

        @Test
        @DisplayName("to LocalDateTime | 转为 LocalDateTime")
        void testToLocalDateTime() {
            assertThat(TypeCoercion.convert("2024-01-15T10:30:00", LocalDateTime.class))
                    .isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
            LocalDate ld = LocalDate.of(2024, 1, 15);
            assertThat(TypeCoercion.convert(ld, LocalDateTime.class))
                    .isEqualTo(LocalDateTime.of(2024, 1, 15, 0, 0));
        }

        @Test
        @DisplayName("invalid conversion throws | 无效转换抛出异常")
        void testInvalidConversionThrows() {
            assertThatThrownBy(() -> TypeCoercion.convert("hello", List.class))
                    .isInstanceOf(OpenExpressionException.class);
        }
    }

    @Nested
    @DisplayName("CanConvert Tests | canConvert 测试")
    class CanConvertTests {

        @Test
        @DisplayName("null can convert to non-primitive | null 可转为非基本类型")
        void testNullCanConvertToNonPrimitive() {
            assertThat(TypeCoercion.canConvert(null, String.class)).isTrue();
            assertThat(TypeCoercion.canConvert(null, int.class)).isFalse();
        }

        @Test
        @DisplayName("same type returns true | 相同类型返回 true")
        void testSameTypeReturnsTrue() {
            assertThat(TypeCoercion.canConvert("hello", String.class)).isTrue();
            assertThat(TypeCoercion.canConvert(42, Integer.class)).isTrue();
        }

        @Test
        @DisplayName("primitives and wrappers return true | 基本类型和包装类返回 true")
        void testPrimitivesAndWrappers() {
            assertThat(TypeCoercion.canConvert("any", Boolean.class)).isTrue();
            assertThat(TypeCoercion.canConvert("any", int.class)).isTrue();
            assertThat(TypeCoercion.canConvert("any", Double.class)).isTrue();
        }

        @Test
        @DisplayName("BigDecimal/BigInteger from number/string | BigDecimal/BigInteger 从数字/字符串")
        void testBigNumbersFromNumberOrString() {
            assertThat(TypeCoercion.canConvert(42, BigDecimal.class)).isTrue();
            assertThat(TypeCoercion.canConvert("42", BigInteger.class)).isTrue();
        }

        @Test
        @DisplayName("unknown conversion returns false | 未知转换返回 false")
        void testUnknownConversionReturnsFalse() {
            assertThat(TypeCoercion.canConvert("hello", List.class)).isFalse();
        }
    }
}
