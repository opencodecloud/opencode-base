package cloud.opencode.base.string.unicode;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenFullWidthTest Tests
 * OpenFullWidthTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("OpenFullWidth Tests")
class OpenFullWidthTest {

    @Nested
    @DisplayName("toHalfWidth Tests")
    class ToHalfWidthTests {

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertThat(OpenFullWidth.toHalfWidth(null)).isNull();
        }

        @Test
        @DisplayName("Should convert full-width characters to half-width")
        void shouldConvertFullWidthToHalfWidth() {
            // Full-width "ABC123" is "ＡＢＣ１２３"
            assertThat(OpenFullWidth.toHalfWidth("ＡＢＣ")).isEqualTo("ABC");
            assertThat(OpenFullWidth.toHalfWidth("１２３")).isEqualTo("123");
        }

        @Test
        @DisplayName("Should convert full-width space to half-width space")
        void shouldConvertFullWidthSpaceToHalfWidthSpace() {
            // Full-width space is \u3000
            assertThat(OpenFullWidth.toHalfWidth("\u3000")).isEqualTo(" ");
        }

        @Test
        @DisplayName("Should preserve already half-width characters")
        void shouldPreserveAlreadyHalfWidthCharacters() {
            assertThat(OpenFullWidth.toHalfWidth("ABC")).isEqualTo("ABC");
            assertThat(OpenFullWidth.toHalfWidth("123")).isEqualTo("123");
        }

        @Test
        @DisplayName("Should handle mixed characters")
        void shouldHandleMixedCharacters() {
            assertThat(OpenFullWidth.toHalfWidth("AＢC")).isEqualTo("ABC");
        }
    }

    @Nested
    @DisplayName("toFullWidth Tests")
    class ToFullWidthTests {

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertThat(OpenFullWidth.toFullWidth(null)).isNull();
        }

        @Test
        @DisplayName("Should convert half-width characters to full-width")
        void shouldConvertHalfWidthToFullWidth() {
            assertThat(OpenFullWidth.toFullWidth("ABC")).isEqualTo("ＡＢＣ");
            assertThat(OpenFullWidth.toFullWidth("123")).isEqualTo("１２３");
        }

        @Test
        @DisplayName("Should convert half-width space to full-width space")
        void shouldConvertHalfWidthSpaceToFullWidthSpace() {
            assertThat(OpenFullWidth.toFullWidth(" ")).isEqualTo("\u3000");
        }

        @Test
        @DisplayName("Should preserve non-convertible characters")
        void shouldPreserveNonConvertibleCharacters() {
            // Characters outside the ASCII printable range should be preserved
            assertThat(OpenFullWidth.toFullWidth("中文")).isEqualTo("中文");
        }
    }

    @Nested
    @DisplayName("Round Trip Tests")
    class RoundTripTests {

        @Test
        @DisplayName("Should round-trip convert ASCII characters")
        void shouldRoundTripConvertAsciiCharacters() {
            String original = "Hello World 123!";
            String fullWidth = OpenFullWidth.toFullWidth(original);
            String backToHalf = OpenFullWidth.toHalfWidth(fullWidth);
            assertThat(backToHalf).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = OpenFullWidth.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
