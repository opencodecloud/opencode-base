package cloud.opencode.base.config.converter.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for NumberConverters.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("NumberConverters Tests")
class NumberConvertersTest {

    @Nested
    @DisplayName("IntegerConverter Tests")
    class IntegerConverterTests {

        private final NumberConverters.IntegerConverter converter = new NumberConverters.IntegerConverter();

        @Test
        @DisplayName("converts valid integer string")
        void testValidConversion() {
            assertThat(converter.convert("42")).isEqualTo(42);
        }

        @Test
        @DisplayName("converts negative integer")
        void testNegativeConversion() {
            assertThat(converter.convert("-100")).isEqualTo(-100);
        }

        @Test
        @DisplayName("converts zero")
        void testZero() {
            assertThat(converter.convert("0")).isEqualTo(0);
        }

        @Test
        @DisplayName("throws for invalid integer")
        void testInvalid() {
            assertThatThrownBy(() -> converter.convert("abc"))
                    .isInstanceOf(NumberFormatException.class);
        }

        @Test
        @DisplayName("getType returns Integer.class")
        void testGetType() {
            assertThat(converter.getType()).isEqualTo(Integer.class);
        }
    }

    @Nested
    @DisplayName("LongConverter Tests")
    class LongConverterTests {

        private final NumberConverters.LongConverter converter = new NumberConverters.LongConverter();

        @Test
        @DisplayName("converts valid long string")
        void testValidConversion() {
            assertThat(converter.convert("9999999999")).isEqualTo(9999999999L);
        }

        @Test
        @DisplayName("converts negative long")
        void testNegativeLong() {
            assertThat(converter.convert("-9999999999")).isEqualTo(-9999999999L);
        }

        @Test
        @DisplayName("throws for invalid long")
        void testInvalid() {
            assertThatThrownBy(() -> converter.convert("not-a-long"))
                    .isInstanceOf(NumberFormatException.class);
        }

        @Test
        @DisplayName("getType returns Long.class")
        void testGetType() {
            assertThat(converter.getType()).isEqualTo(Long.class);
        }
    }

    @Nested
    @DisplayName("DoubleConverter Tests")
    class DoubleConverterTests {

        private final NumberConverters.DoubleConverter converter = new NumberConverters.DoubleConverter();

        @Test
        @DisplayName("converts valid double string")
        void testValidConversion() {
            assertThat(converter.convert("3.14")).isEqualTo(3.14);
        }

        @Test
        @DisplayName("converts integer as double")
        void testIntegerAsDouble() {
            assertThat(converter.convert("42")).isEqualTo(42.0);
        }

        @Test
        @DisplayName("converts negative double")
        void testNegativeDouble() {
            assertThat(converter.convert("-1.5")).isEqualTo(-1.5);
        }

        @Test
        @DisplayName("throws for invalid double")
        void testInvalid() {
            assertThatThrownBy(() -> converter.convert("abc"))
                    .isInstanceOf(NumberFormatException.class);
        }

        @Test
        @DisplayName("getType returns Double.class")
        void testGetType() {
            assertThat(converter.getType()).isEqualTo(Double.class);
        }
    }

    @Nested
    @DisplayName("FloatConverter Tests")
    class FloatConverterTests {

        private final NumberConverters.FloatConverter converter = new NumberConverters.FloatConverter();

        @Test
        @DisplayName("converts valid float string")
        void testValidConversion() {
            assertThat(converter.convert("1.5")).isEqualTo(1.5f);
        }

        @Test
        @DisplayName("throws for invalid float")
        void testInvalid() {
            assertThatThrownBy(() -> converter.convert("not-a-float"))
                    .isInstanceOf(NumberFormatException.class);
        }

        @Test
        @DisplayName("getType returns Float.class")
        void testGetType() {
            assertThat(converter.getType()).isEqualTo(Float.class);
        }
    }

    @Nested
    @DisplayName("BigDecimalConverter Tests")
    class BigDecimalConverterTests {

        private final NumberConverters.BigDecimalConverter converter = new NumberConverters.BigDecimalConverter();

        @Test
        @DisplayName("converts valid BigDecimal string")
        void testValidConversion() {
            assertThat(converter.convert("99.99")).isEqualByComparingTo(new BigDecimal("99.99"));
        }

        @Test
        @DisplayName("converts large BigDecimal")
        void testLargeBigDecimal() {
            String large = "999999999999999999999999999999.99";
            assertThat(converter.convert(large)).isEqualByComparingTo(new BigDecimal(large));
        }

        @Test
        @DisplayName("throws for invalid BigDecimal")
        void testInvalid() {
            assertThatThrownBy(() -> converter.convert("abc"))
                    .isInstanceOf(NumberFormatException.class);
        }

        @Test
        @DisplayName("getType returns BigDecimal.class")
        void testGetType() {
            assertThat(converter.getType()).isEqualTo(BigDecimal.class);
        }
    }

    @Nested
    @DisplayName("BigIntegerConverter Tests")
    class BigIntegerConverterTests {

        private final NumberConverters.BigIntegerConverter converter = new NumberConverters.BigIntegerConverter();

        @Test
        @DisplayName("converts valid BigInteger string")
        void testValidConversion() {
            assertThat(converter.convert("123456789012345678901234567890"))
                    .isEqualTo(new BigInteger("123456789012345678901234567890"));
        }

        @Test
        @DisplayName("throws for invalid BigInteger")
        void testInvalid() {
            assertThatThrownBy(() -> converter.convert("not-a-number"))
                    .isInstanceOf(NumberFormatException.class);
        }

        @Test
        @DisplayName("getType returns BigInteger.class")
        void testGetType() {
            assertThat(converter.getType()).isEqualTo(BigInteger.class);
        }
    }
}
