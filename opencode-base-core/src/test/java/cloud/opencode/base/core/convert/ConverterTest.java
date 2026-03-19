package cloud.opencode.base.core.convert;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Converter 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("Converter 测试")
class ConverterTest {

    @Nested
    @DisplayName("基本功能测试")
    class BasicFunctionalityTests {

        @Test
        @DisplayName("convert 带默认值")
        void testConvertWithDefault() {
            Converter<Integer> converter = (value, defaultValue) -> {
                if (value == null) return defaultValue;
                try {
                    return Integer.parseInt(value.toString());
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            };

            assertThat(converter.convert("123", 0)).isEqualTo(123);
            assertThat(converter.convert("invalid", 0)).isEqualTo(0);
            assertThat(converter.convert(null, 99)).isEqualTo(99);
        }

        @Test
        @DisplayName("convert 无默认值")
        void testConvertWithoutDefault() {
            Converter<Integer> converter = (value, defaultValue) -> {
                if (value == null) return defaultValue;
                try {
                    return Integer.parseInt(value.toString());
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            };

            assertThat(converter.convert("123")).isEqualTo(123);
            assertThat(converter.convert("invalid")).isNull();
            assertThat(converter.convert(null)).isNull();
        }
    }

    @Nested
    @DisplayName("Lambda 实现测试")
    class LambdaImplementationTests {

        @Test
        @DisplayName("字符串转换器")
        void testStringConverter() {
            Converter<String> converter = (value, defaultValue) ->
                value == null ? defaultValue : value.toString();

            assertThat(converter.convert(123)).isEqualTo("123");
            assertThat(converter.convert(true)).isEqualTo("true");
            assertThat(converter.convert(null, "default")).isEqualTo("default");
        }

        @Test
        @DisplayName("布尔转换器")
        void testBooleanConverter() {
            Converter<Boolean> converter = (value, defaultValue) -> {
                if (value == null) return defaultValue;
                if (value instanceof Boolean b) return b;
                String str = value.toString().toLowerCase();
                if ("true".equals(str) || "1".equals(str) || "yes".equals(str)) return true;
                if ("false".equals(str) || "0".equals(str) || "no".equals(str)) return false;
                return defaultValue;
            };

            assertThat(converter.convert("true")).isTrue();
            assertThat(converter.convert("yes")).isTrue();
            assertThat(converter.convert("1")).isTrue();
            assertThat(converter.convert("false")).isFalse();
            assertThat(converter.convert("no")).isFalse();
            assertThat(converter.convert("0")).isFalse();
            assertThat(converter.convert("invalid", false)).isFalse();
        }

        @Test
        @DisplayName("Double 转换器")
        void testDoubleConverter() {
            Converter<Double> converter = (value, defaultValue) -> {
                if (value == null) return defaultValue;
                if (value instanceof Number n) return n.doubleValue();
                try {
                    return Double.parseDouble(value.toString());
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            };

            assertThat(converter.convert("3.14")).isEqualTo(3.14);
            assertThat(converter.convert(42)).isEqualTo(42.0);
            assertThat(converter.convert("invalid", 0.0)).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("链式转换测试")
    class ChainedConversionTests {

        @Test
        @DisplayName("组合转换器")
        void testCombinedConverter() {
            Converter<Integer> intConverter = (value, defaultValue) -> {
                if (value == null) return defaultValue;
                try {
                    return Integer.parseInt(value.toString().trim());
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            };

            Converter<String> hexConverter = (value, defaultValue) -> {
                if (value == null) return defaultValue;
                Integer num = intConverter.convert(value, null);
                return num != null ? Integer.toHexString(num) : defaultValue;
            };

            assertThat(hexConverter.convert("255")).isEqualTo("ff");
            assertThat(hexConverter.convert("16")).isEqualTo("10");
            assertThat(hexConverter.convert("invalid", "0")).isEqualTo("0");
        }
    }
}
