package cloud.opencode.base.string.text;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenTextTest Tests
 * OpenTextTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("OpenText Tests")
class OpenTextTest {

    @Nested
    @DisplayName("Truncate Delegation Tests")
    class TruncateDelegationTests {

        @Test
        @DisplayName("Should delegate truncate to OpenTruncate")
        void shouldDelegateTruncate() {
            assertThat(OpenText.truncate("Hello World", 5)).isEqualTo("He...");
        }

        @Test
        @DisplayName("Should delegate truncate with ellipsis to OpenTruncate")
        void shouldDelegateTruncateWithEllipsis() {
            assertThat(OpenText.truncate("Hello World", 8, "...")).isEqualTo("Hello...");
        }

        @Test
        @DisplayName("Should delegate truncateMiddle to OpenTruncate")
        void shouldDelegateTruncateMiddle() {
            assertThat(OpenText.truncateMiddle("Hello World", 8)).isEqualTo("He...ld");
        }

        @Test
        @DisplayName("Should delegate truncateByBytes to OpenTruncate")
        void shouldDelegateTruncateByBytes() {
            assertThat(OpenText.truncateByBytes("Hello", 100, "UTF-8")).isEqualTo("Hello");
        }
    }

    @Nested
    @DisplayName("Highlight Delegation Tests")
    class HighlightDelegationTests {

        @Test
        @DisplayName("Should delegate highlight to OpenHighlight")
        void shouldDelegateHighlight() {
            assertThat(OpenText.highlight("Hello World", "World", "<b>", "</b>"))
                .isEqualTo("Hello <b>World</b>");
        }

        @Test
        @DisplayName("Should delegate highlightHtml to OpenHighlight")
        void shouldDelegateHighlightHtml() {
            assertThat(OpenText.highlightHtml("Hello World", "World"))
                .isEqualTo("Hello <span class=\"highlight\">World</span>");
        }

        @Test
        @DisplayName("Should delegate highlightHtml with cssClass to OpenHighlight")
        void shouldDelegateHighlightHtmlWithCssClass() {
            assertThat(OpenText.highlightHtml("Hello World", "World", "mark"))
                .isEqualTo("Hello <span class=\"mark\">World</span>");
        }

        @Test
        @DisplayName("Should delegate highlight with keywords list to OpenHighlight")
        void shouldDelegateHighlightWithKeywordsList() {
            List<String> keywords = List.of("Hello", "World");
            assertThat(OpenText.highlight("Hello World", keywords, "<b>", "</b>"))
                .isEqualTo("<b>Hello</b> <b>World</b>");
        }
    }

    @Nested
    @DisplayName("Wrap Delegation Tests")
    class WrapDelegationTests {

        @Test
        @DisplayName("Should delegate wrap to OpenWrap")
        void shouldDelegateWrap() {
            assertThat(OpenText.wrap("Hello World", 5)).isEqualTo("Hello\nWorld");
        }

        @Test
        @DisplayName("Should delegate wrap with lineSeparator to OpenWrap")
        void shouldDelegateWrapWithLineSeparator() {
            assertThat(OpenText.wrap("Hello World", 5, "\r\n")).isEqualTo("Hello\r\nWorld");
        }

        @Test
        @DisplayName("Should delegate indent with spaces to OpenWrap")
        void shouldDelegateIndentWithSpaces() {
            assertThat(OpenText.indent("Hello", 4)).isEqualTo("    Hello");
        }

        @Test
        @DisplayName("Should delegate indent with string to OpenWrap")
        void shouldDelegateIndentWithString() {
            assertThat(OpenText.indent("Hello", ">>")).isEqualTo(">>Hello");
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = OpenText.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
