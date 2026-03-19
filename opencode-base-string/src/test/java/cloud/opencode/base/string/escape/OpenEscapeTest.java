package cloud.opencode.base.string.escape;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenEscapeTest Tests
 * OpenEscapeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("OpenEscape Tests")
class OpenEscapeTest {

    @Nested
    @DisplayName("HTML Escape Tests")
    class HtmlEscapeTests {

        @Test
        @DisplayName("Should escape HTML characters")
        void shouldEscapeHtmlCharacters() {
            String escaped = OpenEscape.escapeHtml("<div>&test</div>");
            // HTML escaping converts < > & to entities, so raw < > should not appear
            // but & will appear in entities like &lt; &gt; &amp;
            assertThat(escaped).contains("&lt;", "&gt;", "&amp;");
            assertThat(escaped).doesNotContain("<div>").doesNotContain("</div>");
        }

        @Test
        @DisplayName("Should unescape HTML entities")
        void shouldUnescapeHtmlEntities() {
            String unescaped = OpenEscape.unescapeHtml("&lt;div&gt;");
            assertThat(unescaped).isEqualTo("<div>");
        }
    }

    @Nested
    @DisplayName("XML Escape Tests")
    class XmlEscapeTests {

        @Test
        @DisplayName("Should escape XML characters")
        void shouldEscapeXmlCharacters() {
            String escaped = OpenEscape.escapeXml("<root>test</root>");
            assertThat(escaped).doesNotContain("<", ">");
        }

        @Test
        @DisplayName("Should unescape XML entities")
        void shouldUnescapeXmlEntities() {
            String unescaped = OpenEscape.unescapeXml("&lt;root&gt;");
            assertThat(unescaped).isEqualTo("<root>");
        }
    }

    @Nested
    @DisplayName("Java Escape Tests")
    class JavaEscapeTests {

        @Test
        @DisplayName("Should escape Java strings")
        void shouldEscapeJavaStrings() {
            String escaped = OpenEscape.escapeJava("hello\nworld");
            assertThat(escaped).contains("\\n");
        }

        @Test
        @DisplayName("Should unescape Java strings")
        void shouldUnescapeJavaStrings() {
            String unescaped = OpenEscape.unescapeJava("hello\\nworld");
            assertThat(unescaped).contains("\n");
        }
    }

    @Nested
    @DisplayName("JSON Escape Tests")
    class JsonEscapeTests {

        @Test
        @DisplayName("Should escape JSON strings")
        void shouldEscapeJsonStrings() {
            String escaped = OpenEscape.escapeJson("hello\nworld");
            assertThat(escaped).contains("\\n");
        }

        @Test
        @DisplayName("Should unescape JSON strings")
        void shouldUnescapeJsonStrings() {
            String unescaped = OpenEscape.unescapeJson("hello\\nworld");
            assertThat(unescaped).contains("\n");
        }
    }

    @Nested
    @DisplayName("SQL Escape Tests")
    class SqlEscapeTests {

        @Test
        @DisplayName("Should escape SQL strings")
        void shouldEscapeSqlStrings() {
            String escaped = OpenEscape.escapeSql("it's test");
            assertThat(escaped).contains("''");
        }
    }

    @Nested
    @DisplayName("URL Encode/Decode Tests")
    class UrlEncodeDecodeTests {

        @Test
        @DisplayName("Should encode URL")
        void shouldEncodeUrl() {
            assertThat(OpenEscape.encodeUrl("hello world")).isEqualTo("hello+world");
            assertThat(OpenEscape.encodeUrl("foo=bar&baz")).contains("%");
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertThat(OpenEscape.encodeUrl(null)).isNull();
            assertThat(OpenEscape.decodeUrl(null)).isNull();
        }

        @Test
        @DisplayName("Should decode URL")
        void shouldDecodeUrl() {
            assertThat(OpenEscape.decodeUrl("hello+world")).isEqualTo("hello world");
            assertThat(OpenEscape.decodeUrl("foo%3Dbar")).isEqualTo("foo=bar");
        }

        @Test
        @DisplayName("Should round-trip encode/decode")
        void shouldRoundTripEncodeDecode() {
            String original = "hello world! @#$%";
            assertThat(OpenEscape.decodeUrl(OpenEscape.encodeUrl(original))).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("CSV Escape Tests")
    class CsvEscapeTests {

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertThat(OpenEscape.escapeCsv(null)).isNull();
            assertThat(OpenEscape.unescapeCsv(null)).isNull();
        }

        @Test
        @DisplayName("Should escape CSV with comma")
        void shouldEscapeCsvWithComma() {
            assertThat(OpenEscape.escapeCsv("hello,world")).isEqualTo("\"hello,world\"");
        }

        @Test
        @DisplayName("Should escape CSV with quote")
        void shouldEscapeCsvWithQuote() {
            assertThat(OpenEscape.escapeCsv("hello\"world")).isEqualTo("\"hello\"\"world\"");
        }

        @Test
        @DisplayName("Should escape CSV with newline")
        void shouldEscapeCsvWithNewline() {
            assertThat(OpenEscape.escapeCsv("hello\nworld")).isEqualTo("\"hello\nworld\"");
        }

        @Test
        @DisplayName("Should not escape simple value")
        void shouldNotEscapeSimpleValue() {
            assertThat(OpenEscape.escapeCsv("hello")).isEqualTo("hello");
        }

        @Test
        @DisplayName("Should unescape CSV")
        void shouldUnescapeCsv() {
            assertThat(OpenEscape.unescapeCsv("\"hello,world\"")).isEqualTo("hello,world");
            assertThat(OpenEscape.unescapeCsv("\"hello\"\"world\"")).isEqualTo("hello\"world");
            assertThat(OpenEscape.unescapeCsv("hello")).isEqualTo("hello");
        }
    }

    @Nested
    @DisplayName("Regex Escape Tests")
    class RegexEscapeTests {

        @Test
        @DisplayName("Should escape regex special characters")
        void shouldEscapeRegexSpecialCharacters() {
            String escaped = OpenEscape.escapeRegex("a.b*c?");
            assertThat(escaped).isEqualTo("\\Qa.b*c?\\E");
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertThat(OpenEscape.escapeRegex(null)).isNull();
        }
    }

    @Nested
    @DisplayName("Shell Escape Tests")
    class ShellEscapeTests {

        @Test
        @DisplayName("Should escape shell commands")
        void shouldEscapeShellCommands() {
            String escaped = OpenEscape.escapeShell("hello world");
            assertThat(escaped).isEqualTo("'hello world'");
        }

        @Test
        @DisplayName("Should escape single quotes")
        void shouldEscapeSingleQuotes() {
            String escaped = OpenEscape.escapeShell("it's test");
            assertThat(escaped).isEqualTo("'it'\\''s test'");
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertThat(OpenEscape.escapeShell(null)).isNull();
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = OpenEscape.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
