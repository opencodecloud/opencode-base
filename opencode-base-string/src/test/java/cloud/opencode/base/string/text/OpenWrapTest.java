package cloud.opencode.base.string.text;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenWrapTest Tests
 * OpenWrapTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("OpenWrap Tests")
class OpenWrapTest {

    @Nested
    @DisplayName("wrap Tests")
    class WrapTests {

        @Test
        @DisplayName("Should wrap text at word boundaries")
        void shouldWrapTextAtWordBoundaries() {
            assertThat(OpenWrap.wrap("Hello World", 5)).isEqualTo("Hello\nWorld");
        }

        @Test
        @DisplayName("Should wrap long text into multiple lines")
        void shouldWrapLongTextIntoMultipleLines() {
            assertThat(OpenWrap.wrap("one two three four", 8)).isEqualTo("one two\nthree\nfour");
        }

        @Test
        @DisplayName("Should return original for null")
        void shouldReturnOriginalForNull() {
            assertThat(OpenWrap.wrap(null, 10)).isNull();
        }

        @Test
        @DisplayName("Should return original for empty string")
        void shouldReturnOriginalForEmptyString() {
            assertThat(OpenWrap.wrap("", 10)).isEmpty();
        }

        @Test
        @DisplayName("Should return original for zero maxLineWidth")
        void shouldReturnOriginalForZeroMaxLineWidth() {
            assertThat(OpenWrap.wrap("Hello", 0)).isEqualTo("Hello");
        }

        @Test
        @DisplayName("Should wrap with custom line separator")
        void shouldWrapWithCustomLineSeparator() {
            assertThat(OpenWrap.wrap("Hello World", 5, "\r\n")).isEqualTo("Hello\r\nWorld");
        }

        @Test
        @DisplayName("Should handle single long word")
        void shouldHandleSingleLongWord() {
            assertThat(OpenWrap.wrap("Supercalifragilisticexpialidocious", 10))
                .isEqualTo("Supercalifragilisticexpialidocious");
        }
    }

    @Nested
    @DisplayName("indent Tests")
    class IndentTests {

        @Test
        @DisplayName("Should indent with spaces")
        void shouldIndentWithSpaces() {
            assertThat(OpenWrap.indent("Hello", 4)).isEqualTo("    Hello");
        }

        @Test
        @DisplayName("Should indent multiline text")
        void shouldIndentMultilineText() {
            assertThat(OpenWrap.indent("Hello\nWorld", 2)).isEqualTo("  Hello\n  World");
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertThat(OpenWrap.indent(null, 4)).isNull();
        }

        @Test
        @DisplayName("Should indent with custom string")
        void shouldIndentWithCustomString() {
            assertThat(OpenWrap.indent("Hello", ">>")).isEqualTo(">>Hello");
        }

        @Test
        @DisplayName("Should indent multiline with custom string")
        void shouldIndentMultilineWithCustomString() {
            assertThat(OpenWrap.indent("Hello\nWorld", "  > ")).isEqualTo("  > Hello\n  > World");
        }

        @Test
        @DisplayName("Should return null for null input with string indent")
        void shouldReturnNullForNullInputWithStringIndent() {
            assertThat(OpenWrap.indent(null, ">>")).isNull();
        }

        @Test
        @DisplayName("Should indent with zero spaces")
        void shouldIndentWithZeroSpaces() {
            assertThat(OpenWrap.indent("Hello", 0)).isEqualTo("Hello");
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = OpenWrap.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
