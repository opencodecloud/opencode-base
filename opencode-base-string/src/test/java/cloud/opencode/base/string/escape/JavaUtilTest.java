package cloud.opencode.base.string.escape;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * JavaUtilTest Tests
 * JavaUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("JavaUtil Tests")
class JavaUtilTest {

    @Nested
    @DisplayName("escape Tests")
    class EscapeTests {

        @Test
        @DisplayName("Should escape backslash")
        void shouldEscapeBackslash() {
            assertThat(JavaUtil.escape("a\\b")).isEqualTo("a\\\\b");
        }

        @Test
        @DisplayName("Should escape newline")
        void shouldEscapeNewline() {
            assertThat(JavaUtil.escape("a\nb")).isEqualTo("a\\nb");
        }

        @Test
        @DisplayName("Should escape carriage return")
        void shouldEscapeCarriageReturn() {
            assertThat(JavaUtil.escape("a\rb")).isEqualTo("a\\rb");
        }

        @Test
        @DisplayName("Should escape tab")
        void shouldEscapeTab() {
            assertThat(JavaUtil.escape("a\tb")).isEqualTo("a\\tb");
        }

        @Test
        @DisplayName("Should escape double quote")
        void shouldEscapeDoubleQuote() {
            assertThat(JavaUtil.escape("say \"hello\"")).isEqualTo("say \\\"hello\\\"");
        }

        @Test
        @DisplayName("Should escape single quote")
        void shouldEscapeSingleQuote() {
            assertThat(JavaUtil.escape("it's")).isEqualTo("it\\'s");
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertThat(JavaUtil.escape(null)).isNull();
        }

        @Test
        @DisplayName("Should preserve normal text")
        void shouldPreserveNormalText() {
            assertThat(JavaUtil.escape("Hello World")).isEqualTo("Hello World");
        }
    }

    @Nested
    @DisplayName("unescape Tests")
    class UnescapeTests {

        @Test
        @DisplayName("Should unescape backslash")
        void shouldUnescapeBackslash() {
            assertThat(JavaUtil.unescape("a\\\\b")).isEqualTo("a\\b");
        }

        @Test
        @DisplayName("Should unescape newline")
        void shouldUnescapeNewline() {
            assertThat(JavaUtil.unescape("a\\nb")).isEqualTo("a\nb");
        }

        @Test
        @DisplayName("Should unescape carriage return")
        void shouldUnescapeCarriageReturn() {
            assertThat(JavaUtil.unescape("a\\rb")).isEqualTo("a\rb");
        }

        @Test
        @DisplayName("Should unescape tab")
        void shouldUnescapeTab() {
            assertThat(JavaUtil.unescape("a\\tb")).isEqualTo("a\tb");
        }

        @Test
        @DisplayName("Should unescape double quote")
        void shouldUnescapeDoubleQuote() {
            assertThat(JavaUtil.unescape("say \\\"hello\\\"")).isEqualTo("say \"hello\"");
        }

        @Test
        @DisplayName("Should unescape single quote")
        void shouldUnescapeSingleQuote() {
            assertThat(JavaUtil.unescape("it\\'s")).isEqualTo("it's");
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertThat(JavaUtil.unescape(null)).isNull();
        }

        @Test
        @DisplayName("Should handle unknown escape sequence")
        void shouldHandleUnknownEscapeSequence() {
            assertThat(JavaUtil.unescape("a\\xb")).isEqualTo("a\\xb");
        }

        @Test
        @DisplayName("Should handle trailing backslash")
        void shouldHandleTrailingBackslash() {
            assertThat(JavaUtil.unescape("a\\")).isEqualTo("a\\");
        }

        @Test
        @DisplayName("Round trip should preserve original")
        void roundTripShouldPreserveOriginal() {
            String original = "Line1\nLine2\tTabbed \"Quoted\" and 'quoted'";
            assertThat(JavaUtil.unescape(JavaUtil.escape(original))).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = JavaUtil.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
