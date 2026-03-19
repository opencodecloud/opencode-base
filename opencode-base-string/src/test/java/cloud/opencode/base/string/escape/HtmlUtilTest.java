package cloud.opencode.base.string.escape;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * HtmlUtilTest Tests
 * HtmlUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("HtmlUtil Tests")
class HtmlUtilTest {

    @Nested
    @DisplayName("escape Tests")
    class EscapeTests {

        @Test
        @DisplayName("Should escape ampersand")
        void shouldEscapeAmpersand() {
            assertThat(HtmlUtil.escape("a&b")).isEqualTo("a&amp;b");
        }

        @Test
        @DisplayName("Should escape less than")
        void shouldEscapeLessThan() {
            assertThat(HtmlUtil.escape("<tag>")).isEqualTo("&lt;tag&gt;");
        }

        @Test
        @DisplayName("Should escape greater than")
        void shouldEscapeGreaterThan() {
            assertThat(HtmlUtil.escape(">")).isEqualTo("&gt;");
        }

        @Test
        @DisplayName("Should escape double quote")
        void shouldEscapeDoubleQuote() {
            assertThat(HtmlUtil.escape("\"text\"")).isEqualTo("&quot;text&quot;");
        }

        @Test
        @DisplayName("Should escape single quote")
        void shouldEscapeSingleQuote() {
            assertThat(HtmlUtil.escape("'text'")).isEqualTo("&#39;text&#39;");
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertThat(HtmlUtil.escape(null)).isNull();
        }

        @Test
        @DisplayName("Should escape all special characters")
        void shouldEscapeAllSpecialCharacters() {
            assertThat(HtmlUtil.escape("<script>alert('xss' & \"test\")</script>"))
                .isEqualTo("&lt;script&gt;alert(&#39;xss&#39; &amp; &quot;test&quot;)&lt;/script&gt;");
        }

        @Test
        @DisplayName("Should preserve normal text")
        void shouldPreserveNormalText() {
            assertThat(HtmlUtil.escape("Hello World")).isEqualTo("Hello World");
        }
    }

    @Nested
    @DisplayName("unescape Tests")
    class UnescapeTests {

        @Test
        @DisplayName("Should unescape ampersand")
        void shouldUnescapeAmpersand() {
            assertThat(HtmlUtil.unescape("a&amp;b")).isEqualTo("a&b");
        }

        @Test
        @DisplayName("Should unescape less than")
        void shouldUnescapeLessThan() {
            assertThat(HtmlUtil.unescape("&lt;tag&gt;")).isEqualTo("<tag>");
        }

        @Test
        @DisplayName("Should unescape greater than")
        void shouldUnescapeGreaterThan() {
            assertThat(HtmlUtil.unescape("&gt;")).isEqualTo(">");
        }

        @Test
        @DisplayName("Should unescape double quote")
        void shouldUnescapeDoubleQuote() {
            assertThat(HtmlUtil.unescape("&quot;text&quot;")).isEqualTo("\"text\"");
        }

        @Test
        @DisplayName("Should unescape single quote")
        void shouldUnescapeSingleQuote() {
            assertThat(HtmlUtil.unescape("&#39;text&#39;")).isEqualTo("'text'");
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertThat(HtmlUtil.unescape(null)).isNull();
        }

        @Test
        @DisplayName("Round trip should preserve original")
        void roundTripShouldPreserveOriginal() {
            String original = "<script>alert('xss')</script>";
            assertThat(HtmlUtil.unescape(HtmlUtil.escape(original))).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = HtmlUtil.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
