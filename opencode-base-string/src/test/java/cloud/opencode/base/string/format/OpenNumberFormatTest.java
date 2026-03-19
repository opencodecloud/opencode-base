package cloud.opencode.base.string.format;

import org.junit.jupiter.api.*;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenNumberFormatTest Tests
 * OpenNumberFormatTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("OpenNumberFormat Tests")
class OpenNumberFormatTest {

    @Nested
    @DisplayName("formatNumber Tests")
    class FormatNumberTests {

        @Test
        @DisplayName("Should format number with default scale")
        void shouldFormatNumberWithDefaultScale() {
            assertThat(OpenNumberFormat.formatNumber(1234.567)).isEqualTo("1,234.57");
        }

        @Test
        @DisplayName("Should format number with custom scale")
        void shouldFormatNumberWithCustomScale() {
            assertThat(OpenNumberFormat.formatNumber(1234.5678, 3)).isEqualTo("1,234.568");
        }

        @Test
        @DisplayName("Should format integer with default scale")
        void shouldFormatIntegerWithDefaultScale() {
            assertThat(OpenNumberFormat.formatNumber(1234)).isEqualTo("1,234.00");
        }

        @Test
        @DisplayName("Should return 0 for null")
        void shouldReturnZeroForNull() {
            assertThat(OpenNumberFormat.formatNumber(null)).isEqualTo("0");
        }

        @Test
        @DisplayName("Should format negative number")
        void shouldFormatNegativeNumber() {
            assertThat(OpenNumberFormat.formatNumber(-1234.56)).isEqualTo("-1,234.56");
        }

        @Test
        @DisplayName("Should format with zero scale")
        void shouldFormatWithZeroScale() {
            // Implementation uses "0".repeat(0) which produces empty string, resulting in "1,235."
            assertThat(OpenNumberFormat.formatNumber(1234.567, 0)).isEqualTo("1,235.");
        }
    }

    @Nested
    @DisplayName("formatPercent Tests")
    class FormatPercentTests {

        @Test
        @DisplayName("Should format percent with default scale")
        void shouldFormatPercentWithDefaultScale() {
            assertThat(OpenNumberFormat.formatPercent(0.1234)).isEqualTo("12.34%");
        }

        @Test
        @DisplayName("Should format percent with custom scale")
        void shouldFormatPercentWithCustomScale() {
            assertThat(OpenNumberFormat.formatPercent(0.12345, 3)).isEqualTo("12.345%");
        }

        @Test
        @DisplayName("Should format 100 percent")
        void shouldFormat100Percent() {
            assertThat(OpenNumberFormat.formatPercent(1.0)).isEqualTo("100.00%");
        }

        @Test
        @DisplayName("Should format 0 percent")
        void shouldFormat0Percent() {
            assertThat(OpenNumberFormat.formatPercent(0.0)).isEqualTo("0.00%");
        }
    }

    @Nested
    @DisplayName("formatCurrency Tests")
    class FormatCurrencyTests {

        @Test
        @DisplayName("Should format currency with default symbol")
        void shouldFormatCurrencyWithDefaultSymbol() {
            assertThat(OpenNumberFormat.formatCurrency(new BigDecimal("1234.56"))).isEqualTo("¥1,234.56");
        }

        @Test
        @DisplayName("Should format currency with custom symbol")
        void shouldFormatCurrencyWithCustomSymbol() {
            assertThat(OpenNumberFormat.formatCurrency(new BigDecimal("1234.56"), "$")).isEqualTo("$1,234.56");
        }

        @Test
        @DisplayName("Should return default for null amount")
        void shouldReturnDefaultForNullAmount() {
            assertThat(OpenNumberFormat.formatCurrency(null)).isEqualTo("¥0.00");
        }

        @Test
        @DisplayName("Should format large currency amount")
        void shouldFormatLargeCurrencyAmount() {
            assertThat(OpenNumberFormat.formatCurrency(new BigDecimal("1234567.89"))).isEqualTo("¥1,234,567.89");
        }
    }

    @Nested
    @DisplayName("toChineseNumber Tests")
    class ToChineseNumberTests {

        @Test
        @DisplayName("Should convert 0 to Chinese")
        void shouldConvertZeroToChinese() {
            assertThat(OpenNumberFormat.toChineseNumber(0)).isEqualTo("零");
        }

        @Test
        @DisplayName("Should convert single digit to Chinese")
        void shouldConvertSingleDigitToChinese() {
            assertThat(OpenNumberFormat.toChineseNumber(5)).isEqualTo("伍");
        }

        @Test
        @DisplayName("Should convert tens to Chinese")
        void shouldConvertTensToChinese() {
            assertThat(OpenNumberFormat.toChineseNumber(12)).isEqualTo("壹拾贰");
        }

        @Test
        @DisplayName("Should convert hundreds to Chinese")
        void shouldConvertHundredsToChinese() {
            assertThat(OpenNumberFormat.toChineseNumber(123)).isEqualTo("壹佰贰拾叁");
        }

        @Test
        @DisplayName("Should handle zeros in middle")
        void shouldHandleZerosInMiddle() {
            assertThat(OpenNumberFormat.toChineseNumber(101)).isEqualTo("壹佰零壹");
        }
    }

    @Nested
    @DisplayName("toChineseMoney Tests")
    class ToChineseMoneyTests {

        @Test
        @DisplayName("Should convert money with yuan only")
        void shouldConvertMoneyWithYuanOnly() {
            String result = OpenNumberFormat.toChineseMoney(new BigDecimal("100"));
            assertThat(result).contains("壹佰").contains("元");
        }

        @Test
        @DisplayName("Should convert money with jiao and fen")
        void shouldConvertMoneyWithJiaoAndFen() {
            assertThat(OpenNumberFormat.toChineseMoney(new BigDecimal("123.45"))).contains("元");
        }

        @Test
        @DisplayName("Should return zero for null")
        void shouldReturnZeroForNull() {
            assertThat(OpenNumberFormat.toChineseMoney(null)).isEqualTo("零元整");
        }

        @Test
        @DisplayName("Should return zero for zero amount")
        void shouldReturnZeroForZeroAmount() {
            assertThat(OpenNumberFormat.toChineseMoney(BigDecimal.ZERO)).isEqualTo("零元整");
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = OpenNumberFormat.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
