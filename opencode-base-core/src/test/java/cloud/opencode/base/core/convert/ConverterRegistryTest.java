package cloud.opencode.base.core.convert;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.*;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.*;

/**
 * ConverterRegistry 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("ConverterRegistry 测试")
class ConverterRegistryTest {

    @Nested
    @DisplayName("默认转换器注册测试")
    class DefaultConverterRegistrationTests {

        @Test
        @DisplayName("数字转换器已注册")
        void testNumberConvertersRegistered() {
            assertThat(ConverterRegistry.hasConverter(Integer.class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(int.class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(Long.class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(long.class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(Double.class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(double.class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(Float.class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(float.class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(Short.class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(short.class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(Byte.class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(byte.class)).isTrue();
        }

        @Test
        @DisplayName("大数转换器已注册")
        void testBigNumberConvertersRegistered() {
            assertThat(ConverterRegistry.hasConverter(BigDecimal.class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(BigInteger.class)).isTrue();
        }

        @Test
        @DisplayName("原子类型转换器已注册")
        void testAtomicConvertersRegistered() {
            assertThat(ConverterRegistry.hasConverter(AtomicInteger.class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(AtomicLong.class)).isTrue();
        }

        @Test
        @DisplayName("布尔转换器已注册")
        void testBooleanConverterRegistered() {
            assertThat(ConverterRegistry.hasConverter(Boolean.class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(boolean.class)).isTrue();
        }

        @Test
        @DisplayName("字符转换器已注册")
        void testCharacterConverterRegistered() {
            assertThat(ConverterRegistry.hasConverter(Character.class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(char.class)).isTrue();
        }

        @Test
        @DisplayName("字符串转换器已注册")
        void testStringConverterRegistered() {
            assertThat(ConverterRegistry.hasConverter(String.class)).isTrue();
        }

        @Test
        @DisplayName("日期转换器已注册")
        void testDateConvertersRegistered() {
            assertThat(ConverterRegistry.hasConverter(LocalDate.class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(LocalDateTime.class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(LocalTime.class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(Instant.class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(ZonedDateTime.class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(OffsetDateTime.class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(Date.class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(java.sql.Date.class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(java.sql.Time.class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(Timestamp.class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(Calendar.class)).isTrue();
        }

        @Test
        @DisplayName("数组转换器已注册")
        void testArrayConvertersRegistered() {
            assertThat(ConverterRegistry.hasConverter(int[].class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(long[].class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(double[].class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(float[].class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(boolean[].class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(byte[].class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(short[].class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(char[].class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(String[].class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(Integer[].class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(Long[].class)).isTrue();
            assertThat(ConverterRegistry.hasConverter(Double[].class)).isTrue();
        }
    }

    @Nested
    @DisplayName("获取转换器测试")
    class GetConverterTests {

        @Test
        @DisplayName("getConverter Integer")
        void testGetIntegerConverter() {
            Converter<Integer> converter = ConverterRegistry.getConverter(Integer.class);
            assertThat(converter).isNotNull();
            assertThat(converter.convert("123")).isEqualTo(123);
            assertThat(converter.convert("invalid", 0)).isEqualTo(0);
        }

        @Test
        @DisplayName("getConverter Long")
        void testGetLongConverter() {
            Converter<Long> converter = ConverterRegistry.getConverter(Long.class);
            assertThat(converter).isNotNull();
            assertThat(converter.convert("9876543210")).isEqualTo(9876543210L);
        }

        @Test
        @DisplayName("getConverter Boolean")
        void testGetBooleanConverter() {
            Converter<Boolean> converter = ConverterRegistry.getConverter(Boolean.class);
            assertThat(converter).isNotNull();
            assertThat(converter.convert("true")).isTrue();
            assertThat(converter.convert("yes")).isTrue();
            assertThat(converter.convert("1")).isTrue();
            assertThat(converter.convert("false")).isFalse();
            assertThat(converter.convert("no")).isFalse();
            assertThat(converter.convert("0")).isFalse();
        }

        @Test
        @DisplayName("getConverter Character")
        void testGetCharacterConverter() {
            Converter<Character> converter = ConverterRegistry.getConverter(Character.class);
            assertThat(converter).isNotNull();
            assertThat(converter.convert("A")).isEqualTo('A');
            assertThat(converter.convert(65)).isEqualTo('A');
        }

        @Test
        @DisplayName("getConverter 不存在的类型")
        void testGetConverterNotFound() {
            // 自定义类没有注册转换器
            class CustomClass {}
            Converter<CustomClass> converter = ConverterRegistry.getConverter(CustomClass.class);
            assertThat(converter).isNull();
        }
    }

    @Nested
    @DisplayName("注册和移除转换器测试")
    class RegisterUnregisterTests {

        @Test
        @DisplayName("register 自定义转换器")
        void testRegisterCustomConverter() {
            class CustomType {
                String value;
                CustomType(String value) { this.value = value; }
            }

            Converter<CustomType> customConverter = (value, defaultValue) -> {
                if (value == null) return defaultValue;
                return new CustomType(value.toString());
            };

            ConverterRegistry.register(CustomType.class, customConverter);
            assertThat(ConverterRegistry.hasConverter(CustomType.class)).isTrue();

            Converter<CustomType> retrieved = ConverterRegistry.getConverter(CustomType.class);
            assertThat(retrieved).isNotNull();
            CustomType result = retrieved.convert("test");
            assertThat(result.value).isEqualTo("test");

            // 清理
            ConverterRegistry.unregister(CustomType.class);
            assertThat(ConverterRegistry.hasConverter(CustomType.class)).isFalse();
        }

        @Test
        @DisplayName("unregister")
        void testUnregister() {
            class TempType {}
            Converter<TempType> converter = (value, def) -> def;

            ConverterRegistry.register(TempType.class, converter);
            assertThat(ConverterRegistry.hasConverter(TempType.class)).isTrue();

            ConverterRegistry.unregister(TempType.class);
            assertThat(ConverterRegistry.hasConverter(TempType.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("size 测试")
    class SizeTests {

        @Test
        @DisplayName("size 返回正确数量")
        void testSize() {
            int size = ConverterRegistry.size();
            // 应该有大量默认转换器
            assertThat(size).isGreaterThan(20);
        }
    }

    @Nested
    @DisplayName("转换器功能验证")
    class ConverterFunctionalityTests {

        @Test
        @DisplayName("BigDecimal 转换器")
        void testBigDecimalConverter() {
            Converter<BigDecimal> converter = ConverterRegistry.getConverter(BigDecimal.class);
            assertThat(converter).isNotNull();
            assertThat(converter.convert("123.456")).isEqualByComparingTo("123.456");
            assertThat(converter.convert(100)).isEqualByComparingTo("100");
        }

        @Test
        @DisplayName("BigInteger 转换器")
        void testBigIntegerConverter() {
            Converter<BigInteger> converter = ConverterRegistry.getConverter(BigInteger.class);
            assertThat(converter).isNotNull();
            assertThat(converter.convert("12345678901234567890")).isEqualTo(new BigInteger("12345678901234567890"));
        }

        @Test
        @DisplayName("AtomicInteger 转换器")
        void testAtomicIntegerConverter() {
            Converter<AtomicInteger> converter = ConverterRegistry.getConverter(AtomicInteger.class);
            assertThat(converter).isNotNull();
            AtomicInteger result = converter.convert("100");
            assertThat(result.get()).isEqualTo(100);
        }

        @Test
        @DisplayName("AtomicLong 转换器")
        void testAtomicLongConverter() {
            Converter<AtomicLong> converter = ConverterRegistry.getConverter(AtomicLong.class);
            assertThat(converter).isNotNull();
            AtomicLong result = converter.convert("9876543210");
            assertThat(result.get()).isEqualTo(9876543210L);
        }

        @Test
        @DisplayName("LocalDate 转换器")
        void testLocalDateConverter() {
            Converter<LocalDate> converter = ConverterRegistry.getConverter(LocalDate.class);
            assertThat(converter).isNotNull();
            assertThat(converter.convert("2024-01-15")).isEqualTo(LocalDate.of(2024, 1, 15));
        }

        @Test
        @DisplayName("LocalDateTime 转换器")
        void testLocalDateTimeConverter() {
            Converter<LocalDateTime> converter = ConverterRegistry.getConverter(LocalDateTime.class);
            assertThat(converter).isNotNull();
            LocalDateTime result = converter.convert("2024-01-15T10:30:00");
            assertThat(result.toLocalDate()).isEqualTo(LocalDate.of(2024, 1, 15));
            assertThat(result.toLocalTime()).isEqualTo(LocalTime.of(10, 30, 0));
        }
    }
}
