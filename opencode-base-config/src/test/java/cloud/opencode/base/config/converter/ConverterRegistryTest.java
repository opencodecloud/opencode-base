package cloud.opencode.base.config.converter;

import cloud.opencode.base.config.OpenConfigException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Path;
import java.time.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ConverterRegistry 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("ConverterRegistry 测试")
class ConverterRegistryTest {

    private ConverterRegistry registry;

    @BeforeEach
    void setUp() {
        registry = ConverterRegistry.defaults();
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("defaults - 创建包含默认转换器的注册表")
        void testDefaults() {
            ConverterRegistry registry = ConverterRegistry.defaults();

            assertThat(registry).isNotNull();
            assertThat(registry.hasConverter(String.class)).isTrue();
            assertThat(registry.hasConverter(Integer.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("字符串转换测试")
    class StringConversionTests {

        @Test
        @DisplayName("String -> String")
        void testStringToString() {
            assertThat(registry.convert("hello", String.class)).isEqualTo("hello");
        }

        @Test
        @DisplayName("null -> null")
        void testNullToNull() {
            assertThat(registry.convert(null, String.class)).isNull();
        }
    }

    @Nested
    @DisplayName("数值类型转换测试")
    class NumberConversionTests {

        @Test
        @DisplayName("String -> Integer")
        void testToInteger() {
            assertThat(registry.convert("42", Integer.class)).isEqualTo(42);
            assertThat(registry.convert("-100", int.class)).isEqualTo(-100);
        }

        @Test
        @DisplayName("String -> Long")
        void testToLong() {
            assertThat(registry.convert("9999999999", Long.class)).isEqualTo(9999999999L);
            assertThat(registry.convert("123", long.class)).isEqualTo(123L);
        }

        @Test
        @DisplayName("String -> Double")
        void testToDouble() {
            assertThat(registry.convert("3.14", Double.class)).isEqualTo(3.14);
            assertThat(registry.convert("2.5", double.class)).isEqualTo(2.5);
        }

        @Test
        @DisplayName("String -> Float")
        void testToFloat() {
            assertThat(registry.convert("1.5", Float.class)).isEqualTo(1.5f);
            assertThat(registry.convert("2.0", float.class)).isEqualTo(2.0f);
        }

        @Test
        @DisplayName("String -> Byte")
        void testToByte() {
            assertThat(registry.convert("127", Byte.class)).isEqualTo((byte) 127);
            assertThat(registry.convert("-128", byte.class)).isEqualTo((byte) -128);
        }

        @Test
        @DisplayName("String -> Short")
        void testToShort() {
            assertThat(registry.convert("32767", Short.class)).isEqualTo((short) 32767);
            assertThat(registry.convert("-1", short.class)).isEqualTo((short) -1);
        }

        @Test
        @DisplayName("String -> BigDecimal")
        void testToBigDecimal() {
            assertThat(registry.convert("123.456789", BigDecimal.class))
                    .isEqualTo(new BigDecimal("123.456789"));
        }

        @Test
        @DisplayName("String -> BigInteger")
        void testToBigInteger() {
            assertThat(registry.convert("999999999999999999999", BigInteger.class))
                    .isEqualTo(new BigInteger("999999999999999999999"));
        }

        @Test
        @DisplayName("无效数值 - 抛出异常")
        void testInvalidNumber() {
            assertThatThrownBy(() -> registry.convert("not-a-number", Integer.class))
                    .isInstanceOf(OpenConfigException.class);
        }
    }

    @Nested
    @DisplayName("布尔类型转换测试")
    class BooleanConversionTests {

        @Test
        @DisplayName("true值转换")
        void testTrueValues() {
            assertThat(registry.convert("true", Boolean.class)).isTrue();
            assertThat(registry.convert("yes", boolean.class)).isTrue();
            assertThat(registry.convert("on", Boolean.class)).isTrue();
            assertThat(registry.convert("1", boolean.class)).isTrue();
            assertThat(registry.convert("enabled", Boolean.class)).isTrue();
        }

        @Test
        @DisplayName("false值转换")
        void testFalseValues() {
            assertThat(registry.convert("false", Boolean.class)).isFalse();
            assertThat(registry.convert("no", boolean.class)).isFalse();
            assertThat(registry.convert("off", Boolean.class)).isFalse();
            assertThat(registry.convert("0", boolean.class)).isFalse();
            assertThat(registry.convert("disabled", Boolean.class)).isFalse();
        }

        @Test
        @DisplayName("无效布尔值 - 抛出异常")
        void testInvalidBoolean() {
            assertThatThrownBy(() -> registry.convert("maybe", Boolean.class))
                    .isInstanceOf(OpenConfigException.class);
        }
    }

    @Nested
    @DisplayName("时间类型转换测试")
    class TimeConversionTests {

        @Test
        @DisplayName("String -> Duration (简单格式)")
        void testToDurationSimple() {
            assertThat(registry.convert("30s", Duration.class)).isEqualTo(Duration.ofSeconds(30));
            assertThat(registry.convert("5m", Duration.class)).isEqualTo(Duration.ofMinutes(5));
            assertThat(registry.convert("2h", Duration.class)).isEqualTo(Duration.ofHours(2));
            assertThat(registry.convert("1d", Duration.class)).isEqualTo(Duration.ofDays(1));
        }

        @Test
        @DisplayName("String -> Duration (ISO-8601格式)")
        void testToDurationISO() {
            assertThat(registry.convert("PT30S", Duration.class)).isEqualTo(Duration.ofSeconds(30));
            assertThat(registry.convert("PT1H30M", Duration.class)).isEqualTo(Duration.ofHours(1).plusMinutes(30));
            assertThat(registry.convert("P1D", Duration.class)).isEqualTo(Duration.ofDays(1));
        }

        @Test
        @DisplayName("String -> LocalDate")
        void testToLocalDate() {
            assertThat(registry.convert("2025-01-21", LocalDate.class))
                    .isEqualTo(LocalDate.of(2025, 1, 21));
        }

        @Test
        @DisplayName("String -> LocalTime")
        void testToLocalTime() {
            assertThat(registry.convert("14:30:00", LocalTime.class))
                    .isEqualTo(LocalTime.of(14, 30, 0));
        }

        @Test
        @DisplayName("String -> LocalDateTime")
        void testToLocalDateTime() {
            assertThat(registry.convert("2025-01-21T14:30:00", LocalDateTime.class))
                    .isEqualTo(LocalDateTime.of(2025, 1, 21, 14, 30, 0));
        }

        @Test
        @DisplayName("String -> Instant")
        void testToInstant() {
            assertThat(registry.convert("2025-01-21T00:00:00Z", Instant.class))
                    .isEqualTo(Instant.parse("2025-01-21T00:00:00Z"));
        }
    }

    @Nested
    @DisplayName("路径类型转换测试")
    class PathConversionTests {

        @Test
        @DisplayName("String -> Path")
        void testToPath() {
            assertThat(registry.convert("/tmp/config", Path.class))
                    .isEqualTo(Path.of("/tmp/config"));
        }

        @Test
        @DisplayName("String -> URI")
        void testToURI() {
            assertThat(registry.convert("http://localhost:8080", URI.class))
                    .isEqualTo(URI.create("http://localhost:8080"));
        }
    }

    @Nested
    @DisplayName("枚举转换测试")
    class EnumConversionTests {

        enum TestEnum { VALUE_ONE, VALUE_TWO }

        @Test
        @DisplayName("String -> Enum")
        void testToEnum() {
            assertThat(registry.convert("VALUE_ONE", TestEnum.class)).isEqualTo(TestEnum.VALUE_ONE);
            assertThat(registry.convert("value_two", TestEnum.class)).isEqualTo(TestEnum.VALUE_TWO);
        }

        @Test
        @DisplayName("无效枚举值 - 抛出异常")
        void testInvalidEnum() {
            assertThatThrownBy(() -> registry.convert("INVALID", TestEnum.class))
                    .isInstanceOf(OpenConfigException.class);
        }
    }

    @Nested
    @DisplayName("自定义转换器测试")
    class CustomConverterTests {

        @Test
        @DisplayName("注册自定义转换器")
        void testRegisterCustomConverter() {
            registry.register(StringBuilder.class, StringBuilder::new);

            StringBuilder result = registry.convert("hello", StringBuilder.class);

            assertThat(result.toString()).isEqualTo("hello");
        }

        @Test
        @DisplayName("覆盖默认转换器")
        void testOverrideDefaultConverter() {
            // 自定义Integer转换器 - 乘以2
            registry.register(Integer.class, s -> Integer.parseInt(s) * 2);

            assertThat(registry.convert("5", Integer.class)).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("hasConverter测试")
    class HasConverterTests {

        @Test
        @DisplayName("内置类型有转换器")
        void testHasConverterBuiltIn() {
            assertThat(registry.hasConverter(String.class)).isTrue();
            assertThat(registry.hasConverter(Integer.class)).isTrue();
            assertThat(registry.hasConverter(Duration.class)).isTrue();
        }

        @Test
        @DisplayName("枚举类型有转换器")
        void testHasConverterEnum() {
            assertThat(registry.hasConverter(DayOfWeek.class)).isTrue();
        }

        @Test
        @DisplayName("未注册类型无转换器")
        void testNoConverter() {
            assertThat(registry.hasConverter(StringBuilder.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("转换器未找到测试")
    class ConverterNotFoundTests {

        @Test
        @DisplayName("未注册类型 - 抛出异常")
        void testConverterNotFound() {
            assertThatThrownBy(() -> registry.convert("value", StringBuilder.class))
                    .isInstanceOf(OpenConfigException.class)
                    .hasMessageContaining("No converter");
        }
    }
}
