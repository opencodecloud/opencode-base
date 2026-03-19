package cloud.opencode.base.string.format;

import org.junit.jupiter.api.*;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenFormatTest Tests
 * OpenFormatTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("OpenFormat Tests")
class OpenFormatTest {

    @Nested
    @DisplayName("Number Formatting Delegation Tests")
    class NumberFormattingTests {

        @Test
        @DisplayName("Should delegate formatNumber to OpenNumberFormat")
        void shouldDelegateFormatNumber() {
            assertThat(OpenFormat.formatNumber(1234.567)).isEqualTo("1,234.57");
        }

        @Test
        @DisplayName("Should delegate formatNumber with scale to OpenNumberFormat")
        void shouldDelegateFormatNumberWithScale() {
            assertThat(OpenFormat.formatNumber(1234.5678, 3)).isEqualTo("1,234.568");
        }

        @Test
        @DisplayName("Should delegate formatPercent to OpenNumberFormat")
        void shouldDelegateFormatPercent() {
            assertThat(OpenFormat.formatPercent(0.1234)).isEqualTo("12.34%");
        }

        @Test
        @DisplayName("Should delegate formatPercent with scale to OpenNumberFormat")
        void shouldDelegateFormatPercentWithScale() {
            assertThat(OpenFormat.formatPercent(0.12345, 3)).isEqualTo("12.345%");
        }

        @Test
        @DisplayName("Should delegate formatCurrency to OpenNumberFormat")
        void shouldDelegateFormatCurrency() {
            assertThat(OpenFormat.formatCurrency(new BigDecimal("1234.56"))).isEqualTo("¥1,234.56");
        }

        @Test
        @DisplayName("Should delegate formatCurrency with symbol to OpenNumberFormat")
        void shouldDelegateFormatCurrencyWithSymbol() {
            assertThat(OpenFormat.formatCurrency(new BigDecimal("1234.56"), "$")).isEqualTo("$1,234.56");
        }

        @Test
        @DisplayName("Should delegate toChineseNumber to OpenNumberFormat")
        void shouldDelegateToChineseNumber() {
            assertThat(OpenFormat.toChineseNumber(123)).contains("壹");
        }

        @Test
        @DisplayName("Should delegate toChineseMoney to OpenNumberFormat")
        void shouldDelegateToChineseMoney() {
            assertThat(OpenFormat.toChineseMoney(new BigDecimal("123.45"))).contains("元");
        }
    }

    @Nested
    @DisplayName("File Size Formatting Delegation Tests")
    class FileSizeFormattingTests {

        @Test
        @DisplayName("Should delegate formatFileSize to OpenFileSize")
        void shouldDelegateFormatFileSize() {
            assertThat(OpenFormat.formatFileSize(1024)).isEqualTo("1.00 KB");
        }

        @Test
        @DisplayName("Should delegate formatFileSize with scale to OpenFileSize")
        void shouldDelegateFormatFileSizeWithScale() {
            assertThat(OpenFormat.formatFileSize(1024, 1)).isEqualTo("1.0 KB");
        }

        @Test
        @DisplayName("Should delegate parseFileSize to OpenFileSize")
        void shouldDelegateParseFileSize() {
            assertThat(OpenFormat.parseFileSize("1 KB")).isEqualTo(1024);
        }
    }

    @Nested
    @DisplayName("Duration Formatting Delegation Tests")
    class DurationFormattingTests {

        @Test
        @DisplayName("Should delegate formatDuration to OpenDuration")
        void shouldDelegateFormatDuration() {
            assertThat(OpenFormat.formatDuration(3661000)).contains("1");
        }

        @Test
        @DisplayName("Should delegate formatTime to OpenDuration")
        void shouldDelegateFormatTime() {
            assertThat(OpenFormat.formatTime(3661)).contains(":");
        }

        @Test
        @DisplayName("Should delegate formatRelativeTime to OpenDuration")
        void shouldDelegateFormatRelativeTime() {
            long now = System.currentTimeMillis();
            assertThat(OpenFormat.formatRelativeTime(now)).isNotNull();
        }
    }

    @Nested
    @DisplayName("formatMobile Tests")
    class FormatMobileTests {

        @Test
        @DisplayName("Should format mobile number")
        void shouldFormatMobileNumber() {
            assertThat(OpenFormat.formatMobile("13812345678")).isEqualTo("138****5678");
        }

        @Test
        @DisplayName("Should return original for non-11-digit number")
        void shouldReturnOriginalForNon11DigitNumber() {
            assertThat(OpenFormat.formatMobile("1234567")).isEqualTo("1234567");
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertThat(OpenFormat.formatMobile(null)).isNull();
        }
    }

    @Nested
    @DisplayName("formatIdCard Tests")
    class FormatIdCardTests {

        @Test
        @DisplayName("Should format ID card")
        void shouldFormatIdCard() {
            assertThat(OpenFormat.formatIdCard("110101199001011234")).isEqualTo("110101********1234");
        }

        @Test
        @DisplayName("Should return original for non-18-digit ID")
        void shouldReturnOriginalForNon18DigitId() {
            assertThat(OpenFormat.formatIdCard("1234567890")).isEqualTo("1234567890");
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertThat(OpenFormat.formatIdCard(null)).isNull();
        }
    }

    @Nested
    @DisplayName("formatBankCard Tests")
    class FormatBankCardTests {

        @Test
        @DisplayName("Should format bank card with spaces")
        void shouldFormatBankCardWithSpaces() {
            assertThat(OpenFormat.formatBankCard("6222021234567890123"))
                .isEqualTo("6222 0212 3456 7890 123");
        }

        @Test
        @DisplayName("Should return original for short card number")
        void shouldReturnOriginalForShortCardNumber() {
            assertThat(OpenFormat.formatBankCard("123456")).isEqualTo("123456");
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertThat(OpenFormat.formatBankCard(null)).isNull();
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = OpenFormat.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
