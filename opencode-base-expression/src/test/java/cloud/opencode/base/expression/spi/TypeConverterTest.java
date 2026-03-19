package cloud.opencode.base.expression.spi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * TypeConverter Tests
 * TypeConverter 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("TypeConverter Tests | TypeConverter 测试")
class TypeConverterTest {

    @Nested
    @DisplayName("Interface Contract Tests | 接口契约测试")
    class InterfaceContractTests {

        @Test
        @DisplayName("canConvert returns true for supported | canConvert 对支持的返回 true")
        void testCanConvert() {
            TypeConverter converter = new TestTypeConverter();
            assertThat(converter.canConvert(Integer.class, String.class)).isTrue();
            assertThat(converter.canConvert(String.class, Integer.class)).isTrue();
        }

        @Test
        @DisplayName("canConvert returns false for unsupported | canConvert 对不支持的返回 false")
        void testCanConvertFalse() {
            TypeConverter converter = new TestTypeConverter();
            assertThat(converter.canConvert(Object.class, Object.class)).isFalse();
        }

        @Test
        @DisplayName("convert converts value | convert 转换值")
        void testConvert() {
            TypeConverter converter = new TestTypeConverter();
            assertThat(converter.convert(42, String.class)).isEqualTo("42");
            assertThat(converter.convert("42", Integer.class)).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("Custom Converter Tests | 自定义转换器测试")
    class CustomConverterTests {

        @Test
        @DisplayName("custom converter handles custom types | 自定义转换器处理自定义类型")
        void testCustomConverter() {
            TypeConverter converter = new CustomTypeConverter();
            assertThat(converter.canConvert(String.class, CustomType.class)).isTrue();
            CustomType result = converter.convert("test", CustomType.class);
            assertThat(result.value()).isEqualTo("test");
        }
    }

    // Test implementations

    private static class TestTypeConverter implements TypeConverter {
        @Override
        public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
            if (sourceType == Integer.class && targetType == String.class) return true;
            if (sourceType == String.class && targetType == Integer.class) return true;
            return false;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T convert(Object value, Class<T> targetType) {
            if (targetType == String.class) {
                return (T) String.valueOf(value);
            }
            if (targetType == Integer.class) {
                return (T) Integer.valueOf(value.toString());
            }
            throw new IllegalArgumentException("Cannot convert");
        }
    }

    private static class CustomTypeConverter implements TypeConverter {
        @Override
        public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
            return sourceType == String.class && targetType == CustomType.class;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T convert(Object value, Class<T> targetType) {
            if (targetType == CustomType.class && value instanceof String s) {
                return (T) new CustomType(s);
            }
            throw new IllegalArgumentException("Cannot convert");
        }
    }

    private record CustomType(String value) {}
}
