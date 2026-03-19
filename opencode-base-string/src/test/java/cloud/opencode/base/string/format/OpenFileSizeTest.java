package cloud.opencode.base.string.format;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenFileSizeTest Tests
 * OpenFileSizeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("OpenFileSize Tests")
class OpenFileSizeTest {

    @Nested
    @DisplayName("format Tests")
    class FormatTests {

        @Test
        @DisplayName("Should format bytes")
        void shouldFormatBytes() {
            assertThat(OpenFileSize.format(0)).isEqualTo("0 B");
            assertThat(OpenFileSize.format(500)).isEqualTo("500 B");
            assertThat(OpenFileSize.format(1023)).isEqualTo("1023 B");
        }

        @Test
        @DisplayName("Should format kilobytes")
        void shouldFormatKilobytes() {
            assertThat(OpenFileSize.format(1024)).isEqualTo("1.00 KB");
            assertThat(OpenFileSize.format(1536)).isEqualTo("1.50 KB");
        }

        @Test
        @DisplayName("Should format megabytes")
        void shouldFormatMegabytes() {
            assertThat(OpenFileSize.format(1024 * 1024)).isEqualTo("1.00 MB");
            assertThat(OpenFileSize.format(1024 * 1024 * 5)).isEqualTo("5.00 MB");
        }

        @Test
        @DisplayName("Should format gigabytes")
        void shouldFormatGigabytes() {
            assertThat(OpenFileSize.format(1024L * 1024 * 1024)).isEqualTo("1.00 GB");
        }

        @Test
        @DisplayName("Should format terabytes")
        void shouldFormatTerabytes() {
            assertThat(OpenFileSize.format(1024L * 1024 * 1024 * 1024)).isEqualTo("1.00 TB");
        }

        @Test
        @DisplayName("Should handle negative values")
        void shouldHandleNegativeValues() {
            assertThat(OpenFileSize.format(-100)).isEqualTo("0 B");
        }

        @Test
        @DisplayName("Should format with custom scale")
        void shouldFormatWithCustomScale() {
            assertThat(OpenFileSize.format(1536, 1)).isEqualTo("1.5 KB");
            assertThat(OpenFileSize.format(1536, 3)).isEqualTo("1.500 KB");
        }
    }

    @Nested
    @DisplayName("parse Tests")
    class ParseTests {

        @Test
        @DisplayName("Should parse bytes")
        void shouldParseBytes() {
            assertThat(OpenFileSize.parse("500 B")).isEqualTo(500);
            assertThat(OpenFileSize.parse("500B")).isEqualTo(500);
        }

        @Test
        @DisplayName("Should parse kilobytes")
        void shouldParseKilobytes() {
            assertThat(OpenFileSize.parse("1 KB")).isEqualTo(1024);
            assertThat(OpenFileSize.parse("1.5 KB")).isEqualTo(1536);
        }

        @Test
        @DisplayName("Should parse megabytes")
        void shouldParseMegabytes() {
            assertThat(OpenFileSize.parse("1 MB")).isEqualTo(1024 * 1024);
        }

        @Test
        @DisplayName("Should parse gigabytes")
        void shouldParseGigabytes() {
            assertThat(OpenFileSize.parse("1 GB")).isEqualTo(1024L * 1024 * 1024);
        }

        @Test
        @DisplayName("Should parse case-insensitively")
        void shouldParseCaseInsensitively() {
            assertThat(OpenFileSize.parse("1 kb")).isEqualTo(1024);
            assertThat(OpenFileSize.parse("1 Kb")).isEqualTo(1024);
        }

        @Test
        @DisplayName("Should return 0 for null or empty")
        void shouldReturnZeroForNullOrEmpty() {
            assertThat(OpenFileSize.parse(null)).isEqualTo(0);
            assertThat(OpenFileSize.parse("")).isEqualTo(0);
            assertThat(OpenFileSize.parse("   ")).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return 0 for invalid format")
        void shouldReturnZeroForInvalidFormat() {
            assertThat(OpenFileSize.parse("invalid")).isEqualTo(0);
            assertThat(OpenFileSize.parse("abc KB")).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Round Trip Tests")
    class RoundTripTests {

        @Test
        @DisplayName("Should round-trip KB values")
        void shouldRoundTripKbValues() {
            long original = 1024;
            String formatted = OpenFileSize.format(original);
            assertThat(OpenFileSize.parse(formatted)).isEqualTo(original);
        }

        @Test
        @DisplayName("Should round-trip MB values")
        void shouldRoundTripMbValues() {
            long original = 1024 * 1024;
            String formatted = OpenFileSize.format(original);
            assertThat(OpenFileSize.parse(formatted)).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = OpenFileSize.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
