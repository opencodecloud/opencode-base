package cloud.opencode.base.core.convert.impl;

import cloud.opencode.base.core.convert.Converter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.*;

/**
 * NumberConverter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("NumberConverter 测试")
class NumberConverterTest {

    @Nested
    @DisplayName("整数转换测试")
    class IntegerConversionTests {

        @Test
        @DisplayName("byteConverter")
        void testByteConverter() {
            Converter<Byte> converter = NumberConverter.byteConverter();

            assertThat(converter.convert("127")).isEqualTo((byte) 127);
            assertThat(converter.convert(100)).isEqualTo((byte) 100);
            assertThat(converter.convert(100.9)).isEqualTo((byte) 100);
            assertThat(converter.convert(true)).isEqualTo((byte) 1);
            assertThat(converter.convert(false)).isEqualTo((byte) 0);
            assertThat(converter.convert('A')).isEqualTo((byte) 65);
            assertThat(converter.convert(null, (byte) 0)).isEqualTo((byte) 0);
            assertThat(converter.convert("invalid", (byte) 0)).isEqualTo((byte) 0);
        }

        @Test
        @DisplayName("shortConverter")
        void testShortConverter() {
            Converter<Short> converter = NumberConverter.shortConverter();

            assertThat(converter.convert("32767")).isEqualTo((short) 32767);
            assertThat(converter.convert(1000)).isEqualTo((short) 1000);
            assertThat(converter.convert(1000.9)).isEqualTo((short) 1000);
            assertThat(converter.convert(null, (short) 0)).isEqualTo((short) 0);
        }

        @Test
        @DisplayName("integerConverter")
        void testIntegerConverter() {
            Converter<Integer> converter = NumberConverter.integerConverter();

            assertThat(converter.convert("123")).isEqualTo(123);
            assertThat(converter.convert("-456")).isEqualTo(-456);
            assertThat(converter.convert(100L)).isEqualTo(100);
            assertThat(converter.convert(100.9)).isEqualTo(100);
            assertThat(converter.convert(true)).isEqualTo(1);
            assertThat(converter.convert(false)).isEqualTo(0);
            assertThat(converter.convert('A')).isEqualTo(65);
        }

        @Test
        @DisplayName("longConverter")
        void testLongConverter() {
            Converter<Long> converter = NumberConverter.longConverter();

            assertThat(converter.convert("9876543210")).isEqualTo(9876543210L);
            assertThat(converter.convert(-123456789012L)).isEqualTo(-123456789012L);
            assertThat(converter.convert(100)).isEqualTo(100L);
            assertThat(converter.convert(100.9)).isEqualTo(100L);
        }
    }

    @Nested
    @DisplayName("浮点数转换测试")
    class FloatConversionTests {

        @Test
        @DisplayName("floatConverter")
        void testFloatConverter() {
            Converter<Float> converter = NumberConverter.floatConverter();

            assertThat(converter.convert("3.14")).isEqualTo(3.14f);
            assertThat(converter.convert(100)).isEqualTo(100.0f);
            assertThat(converter.convert(3.14159)).isEqualTo(3.14159f);
            assertThat(converter.convert(null, 0.0f)).isEqualTo(0.0f);
        }

        @Test
        @DisplayName("doubleConverter")
        void testDoubleConverter() {
            Converter<Double> converter = NumberConverter.doubleConverter();

            assertThat(converter.convert("3.14159265359")).isEqualTo(3.14159265359);
            assertThat(converter.convert(100)).isEqualTo(100.0);
            assertThat(converter.convert(100L)).isEqualTo(100.0);
            assertThat(converter.convert(null, 0.0)).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("大数转换测试")
    class BigNumberConversionTests {

        @Test
        @DisplayName("bigDecimalConverter 字符串")
        void testBigDecimalConverterFromString() {
            Converter<BigDecimal> converter = NumberConverter.bigDecimalConverter();

            assertThat(converter.convert("123.456")).isEqualByComparingTo("123.456");
            assertThat(converter.convert("9999999999999999999.99999")).isEqualByComparingTo("9999999999999999999.99999");
            assertThat(converter.convert("-0.001")).isEqualByComparingTo("-0.001");
        }

        @Test
        @DisplayName("bigDecimalConverter 数字")
        void testBigDecimalConverterFromNumber() {
            Converter<BigDecimal> converter = NumberConverter.bigDecimalConverter();

            assertThat(converter.convert(100)).isEqualByComparingTo("100");
            assertThat(converter.convert(100L)).isEqualByComparingTo("100");
            assertThat(converter.convert(3.14)).isEqualByComparingTo("3.14");
            assertThat(converter.convert(new BigInteger("12345678901234567890")))
                    .isEqualByComparingTo("12345678901234567890");
        }

        @Test
        @DisplayName("bigDecimalConverter 已是 BigDecimal")
        void testBigDecimalConverterAlreadyBigDecimal() {
            Converter<BigDecimal> converter = NumberConverter.bigDecimalConverter();

            BigDecimal original = new BigDecimal("123.456");
            assertThat(converter.convert(original)).isSameAs(original);
        }

        @Test
        @DisplayName("bigIntegerConverter")
        void testBigIntegerConverter() {
            Converter<BigInteger> converter = NumberConverter.bigIntegerConverter();

            assertThat(converter.convert("12345678901234567890"))
                    .isEqualTo(new BigInteger("12345678901234567890"));
            assertThat(converter.convert(100)).isEqualTo(BigInteger.valueOf(100));
            assertThat(converter.convert(100L)).isEqualTo(BigInteger.valueOf(100));
            assertThat(converter.convert(new BigDecimal("123.99")))
                    .isEqualTo(BigInteger.valueOf(123));
        }

        @Test
        @DisplayName("bigIntegerConverter 已是 BigInteger")
        void testBigIntegerConverterAlreadyBigInteger() {
            Converter<BigInteger> converter = NumberConverter.bigIntegerConverter();

            BigInteger original = new BigInteger("12345678901234567890");
            assertThat(converter.convert(original)).isSameAs(original);
        }
    }

    @Nested
    @DisplayName("原子类型转换测试")
    class AtomicConversionTests {

        @Test
        @DisplayName("atomicIntegerConverter")
        void testAtomicIntegerConverter() {
            Converter<AtomicInteger> converter = NumberConverter.atomicIntegerConverter();

            AtomicInteger result = converter.convert("100");
            assertThat(result.get()).isEqualTo(100);

            result = converter.convert(200);
            assertThat(result.get()).isEqualTo(200);

            result = converter.convert(300.9);
            assertThat(result.get()).isEqualTo(300);
        }

        @Test
        @DisplayName("atomicLongConverter")
        void testAtomicLongConverter() {
            Converter<AtomicLong> converter = NumberConverter.atomicLongConverter();

            AtomicLong result = converter.convert("9876543210");
            assertThat(result.get()).isEqualTo(9876543210L);

            result = converter.convert(123456789012L);
            assertThat(result.get()).isEqualTo(123456789012L);
        }
    }

    @Nested
    @DisplayName("特殊格式解析测试")
    class SpecialFormatTests {

        @Test
        @DisplayName("十六进制解析")
        void testHexadecimalParsing() {
            Converter<Integer> converter = NumberConverter.integerConverter();

            assertThat(converter.convert("0xFF")).isEqualTo(255);
            assertThat(converter.convert("0x10")).isEqualTo(16);
            assertThat(converter.convert("0XAB")).isEqualTo(171);
        }

        @Test
        @DisplayName("八进制解析")
        void testOctalParsing() {
            Converter<Integer> converter = NumberConverter.integerConverter();

            assertThat(converter.convert("010")).isEqualTo(8);
            assertThat(converter.convert("0100")).isEqualTo(64);
            assertThat(converter.convert("0777")).isEqualTo(511);
        }

        @Test
        @DisplayName("二进制解析")
        void testBinaryParsing() {
            Converter<Integer> converter = NumberConverter.integerConverter();

            assertThat(converter.convert("0b1010")).isEqualTo(10);
            assertThat(converter.convert("0B11111111")).isEqualTo(255);
        }
    }

    @Nested
    @DisplayName("null 和无效值处理测试")
    class NullAndInvalidHandlingTests {

        @Test
        @DisplayName("null 返回默认值")
        void testNullReturnsDefault() {
            assertThat(NumberConverter.integerConverter().convert(null, 0)).isEqualTo(0);
            assertThat(NumberConverter.longConverter().convert(null, 0L)).isEqualTo(0L);
            assertThat(NumberConverter.doubleConverter().convert(null, 0.0)).isEqualTo(0.0);
            assertThat(NumberConverter.bigDecimalConverter().convert(null, BigDecimal.ZERO))
                    .isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("空字符串返回默认值")
        void testEmptyStringReturnsDefault() {
            assertThat(NumberConverter.integerConverter().convert("", 0)).isEqualTo(0);
            assertThat(NumberConverter.integerConverter().convert("   ", 0)).isEqualTo(0);
        }

        @Test
        @DisplayName("无效字符串返回默认值")
        void testInvalidStringReturnsDefault() {
            assertThat(NumberConverter.integerConverter().convert("abc", 0)).isEqualTo(0);
            assertThat(NumberConverter.integerConverter().convert("12.34.56", 0)).isEqualTo(0);
            assertThat(NumberConverter.integerConverter().convert("not a number", 0)).isEqualTo(0);
        }

        @Test
        @DisplayName("null 无默认值返回 null")
        void testNullWithoutDefaultReturnsNull() {
            assertThat(NumberConverter.integerConverter().convert(null)).isNull();
            assertThat(NumberConverter.longConverter().convert(null)).isNull();
            assertThat(NumberConverter.doubleConverter().convert(null)).isNull();
        }
    }

    @Nested
    @DisplayName("类型保持测试")
    class TypePreservationTests {

        @Test
        @DisplayName("已是目标类型直接返回")
        void testSameTypeReturned() {
            Converter<Integer> intConverter = NumberConverter.integerConverter();
            Integer original = 123;
            assertThat(intConverter.convert(original)).isSameAs(original);

            Converter<Long> longConverter = NumberConverter.longConverter();
            Long originalLong = 456L;
            assertThat(longConverter.convert(originalLong)).isSameAs(originalLong);

            Converter<Double> doubleConverter = NumberConverter.doubleConverter();
            Double originalDouble = 3.14;
            assertThat(doubleConverter.convert(originalDouble)).isSameAs(originalDouble);
        }
    }
}
