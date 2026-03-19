package cloud.opencode.base.reflect.bean;

import org.junit.jupiter.api.*;

import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * PropertyConverterTest Tests
 * PropertyConverterTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("PropertyConverter 测试")
class PropertyConverterTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = PropertyConverter.class.getDeclaredConstructor();
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("convert方法测试")
    class ConvertTests {

        @Test
        @DisplayName("字符串转整数")
        void testStringToInt() {
            Integer result = PropertyConverter.convert("123", Integer.class);
            assertThat(result).isEqualTo(123);
        }

        @Test
        @DisplayName("字符串转int基本类型")
        void testStringToIntPrimitive() {
            int result = PropertyConverter.convert("123", int.class);
            assertThat(result).isEqualTo(123);
        }

        @Test
        @DisplayName("字符串转Long")
        void testStringToLong() {
            Long result = PropertyConverter.convert("123456789", Long.class);
            assertThat(result).isEqualTo(123456789L);
        }

        @Test
        @DisplayName("字符串转Double")
        void testStringToDouble() {
            Double result = PropertyConverter.convert("3.14", Double.class);
            assertThat(result).isEqualTo(3.14);
        }

        @Test
        @DisplayName("字符串转Boolean")
        void testStringToBoolean() {
            Boolean result = PropertyConverter.convert("true", Boolean.class);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("字符串转BigDecimal")
        void testStringToBigDecimal() {
            BigDecimal result = PropertyConverter.convert("123.456", BigDecimal.class);
            assertThat(result).isEqualByComparingTo("123.456");
        }

        @Test
        @DisplayName("字符串转BigInteger")
        void testStringToBigInteger() {
            BigInteger result = PropertyConverter.convert("123456789", BigInteger.class);
            assertThat(result).isEqualTo(new BigInteger("123456789"));
        }

        @Test
        @DisplayName("数字转字符串")
        void testNumberToString() {
            String result = PropertyConverter.convert(123, String.class);
            assertThat(result).isEqualTo("123");
        }

        @Test
        @DisplayName("数字类型互转")
        void testNumberToNumber() {
            Long result = PropertyConverter.convert(123, Long.class);
            assertThat(result).isEqualTo(123L);
        }

        @Test
        @DisplayName("字符串转LocalDate")
        void testStringToLocalDate() {
            LocalDate result = PropertyConverter.convert("2024-01-15", LocalDate.class);
            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 15));
        }

        @Test
        @DisplayName("字符串转LocalTime")
        void testStringToLocalTime() {
            LocalTime result = PropertyConverter.convert("10:30:00", LocalTime.class);
            assertThat(result).isEqualTo(LocalTime.of(10, 30, 0));
        }

        @Test
        @DisplayName("字符串转LocalDateTime")
        void testStringToLocalDateTime() {
            LocalDateTime result = PropertyConverter.convert("2024-01-15T10:30:00", LocalDateTime.class);
            assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        }

        @Test
        @DisplayName("字符串转枚举")
        void testStringToEnum() {
            TestEnum result = PropertyConverter.convert("VALUE1", TestEnum.class);
            assertThat(result).isEqualTo(TestEnum.VALUE1);
        }

        @Test
        @DisplayName("枚举转字符串")
        void testEnumToString() {
            String result = PropertyConverter.convert(TestEnum.VALUE1, String.class);
            assertThat(result).isEqualTo("VALUE1");
        }

        @Test
        @DisplayName("相同类型直接返回")
        void testSameType() {
            String input = "test";
            String result = PropertyConverter.convert(input, String.class);
            assertThat(result).isSameAs(input);
        }

        @Test
        @DisplayName("null输入返回默认值")
        void testNullInput() {
            Integer result = PropertyConverter.convert(null, Integer.class);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("null输入基本类型返回默认值")
        void testNullInputPrimitive() {
            int result = PropertyConverter.convert(null, int.class);
            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("无法转换抛出异常")
        void testCannotConvert() {
            assertThatThrownBy(() -> PropertyConverter.convert(new Object(), LocalDate.class))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("convertSafe方法测试")
    class ConvertSafeTests {

        @Test
        @DisplayName("转换成功返回值")
        void testConvertSafeSuccess() {
            Integer result = PropertyConverter.convertSafe("123", Integer.class);
            assertThat(result).isEqualTo(123);
        }

        @Test
        @DisplayName("转换失败返回null")
        void testConvertSafeFailed() {
            Integer result = PropertyConverter.convertSafe("not a number", Integer.class);
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("convertOrDefault方法测试")
    class ConvertOrDefaultTests {

        @Test
        @DisplayName("转换成功返回值")
        void testConvertOrDefaultSuccess() {
            Integer result = PropertyConverter.convertOrDefault("123", Integer.class, 0);
            assertThat(result).isEqualTo(123);
        }

        @Test
        @DisplayName("转换失败返回默认值")
        void testConvertOrDefaultFailed() {
            Integer result = PropertyConverter.convertOrDefault("not a number", Integer.class, -1);
            assertThat(result).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("canConvert方法测试")
    class CanConvertTests {

        @Test
        @DisplayName("可转换返回true")
        void testCanConvertTrue() {
            assertThat(PropertyConverter.canConvert(String.class, Integer.class)).isTrue();
        }

        @Test
        @DisplayName("相同类型返回true")
        void testCanConvertSameType() {
            assertThat(PropertyConverter.canConvert(String.class, String.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("registerConverter方法测试")
    class RegisterConverterTests {

        @Test
        @DisplayName("注册自定义转换器")
        void testRegisterConverter() {
            PropertyConverter.registerConverter(String.class, CustomType.class,
                    s -> new CustomType((String) s));

            CustomType result = PropertyConverter.convert("test", CustomType.class);
            assertThat(result.getValue()).isEqualTo("test");
        }
    }

    // Test helpers
    enum TestEnum {
        VALUE1, VALUE2
    }

    static class CustomType {
        private final String value;
        CustomType(String value) { this.value = value; }
        String getValue() { return value; }
    }
}
