package cloud.opencode.base.string.text;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenHighlightTest Tests
 * OpenHighlightTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("OpenHighlight Tests")
class OpenHighlightTest {

    @Nested
    @DisplayName("highlight Tests")
    class HighlightTests {

        @Test
        @DisplayName("Should highlight keyword in text")
        void shouldHighlightKeywordInText() {
            assertThat(OpenHighlight.highlight("Hello World", "World", "<b>", "</b>"))
                .isEqualTo("Hello <b>World</b>");
        }

        @Test
        @DisplayName("Should highlight multiple occurrences")
        void shouldHighlightMultipleOccurrences() {
            assertThat(OpenHighlight.highlight("foo bar foo", "foo", "[", "]"))
                .isEqualTo("[foo] bar [foo]");
        }

        @Test
        @DisplayName("Should return original text for null text")
        void shouldReturnOriginalTextForNullText() {
            String nullText = null;
            assertThat(OpenHighlight.highlight(nullText, "keyword", "<b>", "</b>")).isNull();
        }

        @Test
        @DisplayName("Should return original text for null keyword")
        void shouldReturnOriginalTextForNullKeyword() {
            String nullKeyword = null;
            assertThat(OpenHighlight.highlight("Hello", nullKeyword, "<b>", "</b>")).isEqualTo("Hello");
        }
    }

    @Nested
    @DisplayName("highlightHtml Tests")
    class HighlightHtmlTests {

        @Test
        @DisplayName("Should highlight with default CSS class")
        void shouldHighlightWithDefaultCssClass() {
            assertThat(OpenHighlight.highlightHtml("Hello World", "World"))
                .isEqualTo("Hello <span class=\"highlight\">World</span>");
        }

        @Test
        @DisplayName("Should highlight with custom CSS class")
        void shouldHighlightWithCustomCssClass() {
            assertThat(OpenHighlight.highlightHtml("Hello World", "World", "mark"))
                .isEqualTo("Hello <span class=\"mark\">World</span>");
        }
    }

    @Nested
    @DisplayName("highlightConsole Tests")
    class HighlightConsoleTests {

        @Test
        @DisplayName("Should highlight with RED color")
        void shouldHighlightWithRedColor() {
            String result = OpenHighlight.highlightConsole("Hello World", "World", OpenHighlight.AnsiColor.RED);
            assertThat(result).contains("\u001B[31m");
            assertThat(result).contains("\u001B[0m");
        }

        @Test
        @DisplayName("Should highlight with GREEN color")
        void shouldHighlightWithGreenColor() {
            String result = OpenHighlight.highlightConsole("Hello World", "World", OpenHighlight.AnsiColor.GREEN);
            assertThat(result).contains("\u001B[32m");
        }

        @Test
        @DisplayName("Should highlight with YELLOW color")
        void shouldHighlightWithYellowColor() {
            String result = OpenHighlight.highlightConsole("Hello World", "World", OpenHighlight.AnsiColor.YELLOW);
            assertThat(result).contains("\u001B[33m");
        }

        @Test
        @DisplayName("Should highlight with BLUE color")
        void shouldHighlightWithBlueColor() {
            String result = OpenHighlight.highlightConsole("Hello World", "World", OpenHighlight.AnsiColor.BLUE);
            assertThat(result).contains("\u001B[34m");
        }

        @Test
        @DisplayName("Should highlight with MAGENTA color")
        void shouldHighlightWithMagentaColor() {
            String result = OpenHighlight.highlightConsole("Hello World", "World", OpenHighlight.AnsiColor.MAGENTA);
            assertThat(result).contains("\u001B[35m");
        }

        @Test
        @DisplayName("Should highlight with CYAN color")
        void shouldHighlightWithCyanColor() {
            String result = OpenHighlight.highlightConsole("Hello World", "World", OpenHighlight.AnsiColor.CYAN);
            assertThat(result).contains("\u001B[36m");
        }

        @Test
        @DisplayName("Should highlight with WHITE color")
        void shouldHighlightWithWhiteColor() {
            String result = OpenHighlight.highlightConsole("Hello World", "World", OpenHighlight.AnsiColor.WHITE);
            assertThat(result).contains("\u001B[37m");
        }
    }

    @Nested
    @DisplayName("highlight with keywords list Tests")
    class HighlightKeywordsListTests {

        @Test
        @DisplayName("Should highlight multiple keywords")
        void shouldHighlightMultipleKeywords() {
            List<String> keywords = List.of("Hello", "World");
            assertThat(OpenHighlight.highlight("Hello World", keywords, "<b>", "</b>"))
                .isEqualTo("<b>Hello</b> <b>World</b>");
        }

        @Test
        @DisplayName("Should return original text for null text")
        void shouldReturnOriginalTextForNullText() {
            List<String> keywords = List.of("keyword");
            assertThat(OpenHighlight.highlight(null, keywords, "<b>", "</b>")).isNull();
        }

        @Test
        @DisplayName("Should return original text for null keywords")
        void shouldReturnOriginalTextForNullKeywords() {
            List<String> nullKeywords = null;
            assertThat(OpenHighlight.highlight("Hello", nullKeywords, "<b>", "</b>")).isEqualTo("Hello");
        }
    }

    @Nested
    @DisplayName("highlightByPattern Tests")
    class HighlightByPatternTests {

        @Test
        @DisplayName("Should highlight by regex pattern")
        void shouldHighlightByRegexPattern() {
            assertThat(OpenHighlight.highlightByPattern("Hello 123 World 456", "\\d+", "[", "]"))
                .isEqualTo("Hello [123] World [456]");
        }

        @Test
        @DisplayName("Should return original text for null text")
        void shouldReturnOriginalTextForNullText() {
            assertThat(OpenHighlight.highlightByPattern(null, "\\d+", "[", "]")).isNull();
        }

        @Test
        @DisplayName("Should return original text for null pattern")
        void shouldReturnOriginalTextForNullPattern() {
            assertThat(OpenHighlight.highlightByPattern("Hello", null, "[", "]")).isEqualTo("Hello");
        }
    }

    @Nested
    @DisplayName("AnsiColor Enum Tests")
    class AnsiColorEnumTests {

        @Test
        @DisplayName("Should have all expected values")
        void shouldHaveAllExpectedValues() {
            assertThat(OpenHighlight.AnsiColor.values()).containsExactly(
                OpenHighlight.AnsiColor.RED,
                OpenHighlight.AnsiColor.GREEN,
                OpenHighlight.AnsiColor.YELLOW,
                OpenHighlight.AnsiColor.BLUE,
                OpenHighlight.AnsiColor.MAGENTA,
                OpenHighlight.AnsiColor.CYAN,
                OpenHighlight.AnsiColor.WHITE
            );
        }

        @Test
        @DisplayName("Should return value from name")
        void shouldReturnValueFromName() {
            assertThat(OpenHighlight.AnsiColor.valueOf("RED")).isEqualTo(OpenHighlight.AnsiColor.RED);
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = OpenHighlight.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
