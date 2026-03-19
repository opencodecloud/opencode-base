package cloud.opencode.base.core.bean;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * PropertyConverter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("PropertyConverter 测试")
class PropertyConverterTest {

    @Nested
    @DisplayName("convert 方法测试")
    class ConvertTests {

        @Test
        @DisplayName("自定义转换器")
        void testCustomConverter() {
            PropertyConverter converter = (value, srcType, tgtType, name) -> {
                if (value instanceof String s) {
                    return s.toUpperCase();
                }
                return value;
            };

            Object result = converter.convert("hello", String.class, String.class, "name");
            assertThat(result).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("基于属性名的转换")
        void testPropertyNameBasedConversion() {
            PropertyConverter converter = (value, srcType, tgtType, name) -> {
                if ("status".equals(name)) {
                    return value instanceof Boolean b && b ? "ACTIVE" : "INACTIVE";
                }
                return value;
            };

            Object result = converter.convert(true, Boolean.class, String.class, "status");
            assertThat(result).isEqualTo("ACTIVE");

            Object result2 = converter.convert(false, Boolean.class, String.class, "status");
            assertThat(result2).isEqualTo("INACTIVE");
        }

        @Test
        @DisplayName("基于类型的转换")
        void testTypeBasedConversion() {
            PropertyConverter converter = (value, srcType, tgtType, name) -> {
                if (tgtType == String.class && value != null) {
                    return "Value: " + value;
                }
                return value;
            };

            Object result = converter.convert(123, Integer.class, String.class, "number");
            assertThat(result).isEqualTo("Value: 123");
        }

        @Test
        @DisplayName("null 值处理")
        void testNullValueHandling() {
            PropertyConverter converter = (value, srcType, tgtType, name) -> {
                if (value == null) {
                    return "default";
                }
                return value;
            };

            Object result = converter.convert(null, String.class, String.class, "name");
            assertThat(result).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("defaultConverter 测试")
    class DefaultConverterTests {

        @Test
        @DisplayName("defaultConverter 类型转换")
        void testDefaultConverterTypeConversion() {
            PropertyConverter converter = PropertyConverter.defaultConverter();

            Object result = converter.convert("123", String.class, Integer.class, "number");
            assertThat(result).isEqualTo(123);
        }

        @Test
        @DisplayName("defaultConverter 字符串转换")
        void testDefaultConverterStringConversion() {
            PropertyConverter converter = PropertyConverter.defaultConverter();

            Object result = converter.convert(123, Integer.class, String.class, "number");
            assertThat(result).isEqualTo("123");
        }

        @Test
        @DisplayName("defaultConverter null 处理")
        void testDefaultConverterNull() {
            PropertyConverter converter = PropertyConverter.defaultConverter();

            Object result = converter.convert(null, String.class, String.class, "name");
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("defaultConverter 相同类型")
        void testDefaultConverterSameType() {
            PropertyConverter converter = PropertyConverter.defaultConverter();

            Object result = converter.convert("hello", String.class, String.class, "name");
            assertThat(result).isEqualTo("hello");
        }
    }

    @Nested
    @DisplayName("identity 测试")
    class IdentityTests {

        @Test
        @DisplayName("identity 返回原值")
        void testIdentityReturnsOriginalValue() {
            PropertyConverter converter = PropertyConverter.identity();

            Object result = converter.convert("hello", String.class, Integer.class, "name");
            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("identity null 处理")
        void testIdentityNull() {
            PropertyConverter converter = PropertyConverter.identity();

            Object result = converter.convert(null, String.class, String.class, "name");
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("identity 对象引用不变")
        void testIdentitySameReference() {
            PropertyConverter converter = PropertyConverter.identity();

            Object original = new Object();
            Object result = converter.convert(original, Object.class, Object.class, "obj");
            assertThat(result).isSameAs(original);
        }
    }

    @Nested
    @DisplayName("andThen 测试")
    class AndThenTests {

        @Test
        @DisplayName("andThen 链式转换")
        void testAndThenChaining() {
            PropertyConverter first = (value, srcType, tgtType, name) -> {
                if (value instanceof String s) {
                    return Integer.parseInt(s);
                }
                return value;
            };

            PropertyConverter second = (value, srcType, tgtType, name) -> {
                if (value instanceof Integer i) {
                    return i * 2;
                }
                return value;
            };

            PropertyConverter chained = first.andThen(second);

            Object result = chained.convert("10", String.class, Integer.class, "number");
            assertThat(result).isEqualTo(20);
        }

        @Test
        @DisplayName("andThen 多次链接")
        void testAndThenMultipleChaining() {
            PropertyConverter addPrefix = (value, srcType, tgtType, name) -> "prefix_" + value;
            PropertyConverter addSuffix = (value, srcType, tgtType, name) -> value + "_suffix";
            PropertyConverter toUpper = (value, srcType, tgtType, name) ->
                value instanceof String s ? s.toUpperCase() : value;

            PropertyConverter chained = addPrefix.andThen(addSuffix).andThen(toUpper);

            Object result = chained.convert("hello", String.class, String.class, "name");
            assertThat(result).isEqualTo("PREFIX_HELLO_SUFFIX");
        }

        @Test
        @DisplayName("andThen 第一个返回 null")
        void testAndThenFirstReturnsNull() {
            PropertyConverter first = (value, srcType, tgtType, name) -> null;
            PropertyConverter second = (value, srcType, tgtType, name) -> "should not reach";

            PropertyConverter chained = first.andThen(second);

            Object result = chained.convert("hello", String.class, String.class, "name");
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("andThen 第二个转换器使用新类型")
        void testAndThenUsesNewType() {
            PropertyConverter toInt = (value, srcType, tgtType, name) -> Integer.parseInt((String) value);
            PropertyConverter addOne = (value, srcType, tgtType, name) -> {
                assertThat(srcType).isEqualTo(Integer.class);
                return (Integer) value + 1;
            };

            PropertyConverter chained = toInt.andThen(addOne);

            Object result = chained.convert("5", String.class, Integer.class, "number");
            assertThat(result).isEqualTo(6);
        }
    }

    @Nested
    @DisplayName("Lambda 表达式测试")
    class LambdaTests {

        @Test
        @DisplayName("使用 Lambda 定义转换器")
        void testLambdaDefinition() {
            PropertyConverter converter = (v, s, t, n) -> v != null ? v.toString() : "";

            assertThat(converter.convert(123, Integer.class, String.class, "n")).isEqualTo("123");
            assertThat(converter.convert(null, Integer.class, String.class, "n")).isEqualTo("");
        }

        @Test
        @DisplayName("方法引用作为转换器")
        void testMethodReference() {
            PropertyConverter converter = (v, s, t, n) -> String.valueOf(v);

            assertThat(converter.convert(42, Integer.class, String.class, "n")).isEqualTo("42");
            assertThat(converter.convert(true, Boolean.class, String.class, "n")).isEqualTo("true");
        }
    }

    @Nested
    @DisplayName("复杂转换场景测试")
    class ComplexConversionTests {

        @Test
        @DisplayName("日期字符串格式化")
        void testDateStringFormatting() {
            PropertyConverter converter = (value, srcType, tgtType, name) -> {
                if ("createDate".equals(name) && value instanceof java.util.Date date) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    return sdf.format(date);
                }
                return value;
            };

            java.util.Date date = new java.util.Date(0);
            Object result = converter.convert(date, java.util.Date.class, String.class, "createDate");
            assertThat(result.toString()).contains("1970");
        }

        @Test
        @DisplayName("枚举转换")
        void testEnumConversion() {
            PropertyConverter converter = (value, srcType, tgtType, name) -> {
                if (value instanceof Enum<?> e) {
                    return e.name();
                }
                return value;
            };

            Object result = converter.convert(Thread.State.RUNNABLE, Thread.State.class, String.class, "state");
            assertThat(result).isEqualTo("RUNNABLE");
        }

        @Test
        @DisplayName("条件转换")
        void testConditionalConversion() {
            PropertyConverter converter = (value, srcType, tgtType, name) -> {
                if (tgtType == Boolean.class) {
                    if (value instanceof Number n) {
                        return n.intValue() != 0;
                    }
                    if (value instanceof String s) {
                        if ("true".equalsIgnoreCase(s) || "1".equals(s) || "yes".equalsIgnoreCase(s)) {
                            return true;
                        }
                        // 不匹配的字符串返回原值
                    }
                }
                return value;
            };

            assertThat(converter.convert(1, Integer.class, Boolean.class, "flag")).isEqualTo(true);
            assertThat(converter.convert(0, Integer.class, Boolean.class, "flag")).isEqualTo(false);
            assertThat(converter.convert("yes", String.class, Boolean.class, "flag")).isEqualTo(true);
            assertThat(converter.convert("no", String.class, Boolean.class, "flag")).isEqualTo("no");
        }
    }
}
