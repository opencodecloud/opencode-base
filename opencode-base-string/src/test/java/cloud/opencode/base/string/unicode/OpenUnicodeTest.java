package cloud.opencode.base.string.unicode;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenUnicodeTest Tests
 * OpenUnicodeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("OpenUnicode Tests")
class OpenUnicodeTest {

    @Nested
    @DisplayName("toUnicode Tests")
    class ToUnicodeTests {

        @Test
        @DisplayName("Should convert to unicode escape")
        void shouldConvertToUnicodeEscape() {
            assertThat(OpenUnicode.toUnicode("A")).isEqualTo("\\u0041");
            assertThat(OpenUnicode.toUnicode("中")).isEqualTo("\\u4e2d");
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertThat(OpenUnicode.toUnicode(null)).isNull();
        }

        @Test
        @DisplayName("Should handle multiple characters")
        void shouldHandleMultipleCharacters() {
            String result = OpenUnicode.toUnicode("ab");
            assertThat(result).isEqualTo("\\u0061\\u0062");
        }
    }

    @Nested
    @DisplayName("fromUnicode Tests")
    class FromUnicodeTests {

        @Test
        @DisplayName("Should convert from unicode escape")
        void shouldConvertFromUnicodeEscape() {
            assertThat(OpenUnicode.fromUnicode("\\u0041")).isEqualTo("A");
            assertThat(OpenUnicode.fromUnicode("\\u4e2d")).isEqualTo("中");
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertThat(OpenUnicode.fromUnicode(null)).isNull();
        }

        @Test
        @DisplayName("Should round-trip conversion")
        void shouldRoundTripConversion() {
            String original = "Hello中文";
            String unicode = OpenUnicode.toUnicode(original);
            assertThat(OpenUnicode.fromUnicode(unicode)).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("toHalfWidth Tests")
    class ToHalfWidthTests {

        @Test
        @DisplayName("Should convert full-width to half-width")
        void shouldConvertFullWidthToHalfWidth() {
            String result = OpenUnicode.toHalfWidth("ＡＢＣ");
            assertThat(result).isEqualTo("ABC");
        }
    }

    @Nested
    @DisplayName("toFullWidth Tests")
    class ToFullWidthTests {

        @Test
        @DisplayName("Should convert half-width to full-width")
        void shouldConvertHalfWidthToFullWidth() {
            String result = OpenUnicode.toFullWidth("ABC");
            assertThat(result).isEqualTo("ＡＢＣ");
        }
    }

    @Nested
    @DisplayName("codePoint Tests")
    class CodePointTests {

        @Test
        @DisplayName("Should return code point of character")
        void shouldReturnCodePointOfCharacter() {
            assertThat(OpenUnicode.codePoint('A')).isEqualTo(65);
            assertThat(OpenUnicode.codePoint('中')).isEqualTo(0x4E2D);
        }
    }

    @Nested
    @DisplayName("codePoints Tests")
    class CodePointsTests {

        @Test
        @DisplayName("Should return code points array")
        void shouldReturnCodePointsArray() {
            int[] codePoints = OpenUnicode.codePoints("AB");
            assertThat(codePoints).containsExactly(65, 66);
        }

        @Test
        @DisplayName("Should return empty array for null")
        void shouldReturnEmptyArrayForNull() {
            assertThat(OpenUnicode.codePoints(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("fromCodePoints Tests")
    class FromCodePointsTests {

        @Test
        @DisplayName("Should convert code points to string")
        void shouldConvertCodePointsToString() {
            assertThat(OpenUnicode.fromCodePoints(65, 66, 67)).isEqualTo("ABC");
        }

        @Test
        @DisplayName("Should return empty string for null")
        void shouldReturnEmptyStringForNull() {
            assertThat(OpenUnicode.fromCodePoints(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("containsEmoji Tests")
    class ContainsEmojiTests {

        @Test
        @DisplayName("Should detect emoji")
        void shouldDetectEmoji() {
            assertThat(OpenUnicode.containsEmoji("Hello 😀")).isTrue();
            assertThat(OpenUnicode.containsEmoji("Hello")).isFalse();
        }

        @Test
        @DisplayName("Should return false for null")
        void shouldReturnFalseForNull() {
            assertThat(OpenUnicode.containsEmoji(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("removeEmoji Tests")
    class RemoveEmojiTests {

        @Test
        @DisplayName("Should remove emoji")
        void shouldRemoveEmoji() {
            assertThat(OpenUnicode.removeEmoji("Hello 😀 World")).isEqualTo("Hello  World");
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertThat(OpenUnicode.removeEmoji(null)).isNull();
        }

        @Test
        @DisplayName("Should return same string when no emoji")
        void shouldReturnSameStringWhenNoEmoji() {
            assertThat(OpenUnicode.removeEmoji("Hello")).isEqualTo("Hello");
        }
    }

    @Nested
    @DisplayName("displayWidth Tests")
    class DisplayWidthTests {

        @Test
        @DisplayName("Should return 0 for null")
        void shouldReturnZeroForNull() {
            assertThat(OpenUnicode.displayWidth(null)).isZero();
        }

        @Test
        @DisplayName("Should count ASCII as width 1")
        void shouldCountAsciiAsWidthOne() {
            assertThat(OpenUnicode.displayWidth("abc")).isEqualTo(3);
        }

        @Test
        @DisplayName("Should count CJK as width 2")
        void shouldCountCjkAsWidthTwo() {
            assertThat(OpenUnicode.displayWidth("中")).isEqualTo(2);
            assertThat(OpenUnicode.displayWidth("中文")).isEqualTo(4);
        }

        @Test
        @DisplayName("Should count mixed correctly")
        void shouldCountMixedCorrectly() {
            assertThat(OpenUnicode.displayWidth("ab中")).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = OpenUnicode.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
