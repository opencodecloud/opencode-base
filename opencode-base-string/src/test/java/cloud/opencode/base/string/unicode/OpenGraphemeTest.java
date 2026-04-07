package cloud.opencode.base.string.unicode;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenGrapheme Tests
 * OpenGrapheme 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.3
 */
@DisplayName("OpenGrapheme Tests")
class OpenGraphemeTest {

    // Family emoji: 👨‍👩‍👧‍👦
    private static final String FAMILY_EMOJI = "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66";

    @Nested
    @DisplayName("Length Tests")
    class Length {

        @Test
        @DisplayName("Should return 0 for null")
        void shouldReturnZeroForNull() {
            assertThat(OpenGrapheme.length(null)).isZero();
        }

        @Test
        @DisplayName("Should return 0 for empty string")
        void shouldReturnZeroForEmpty() {
            assertThat(OpenGrapheme.length("")).isZero();
        }

        @Test
        @DisplayName("Should count ASCII characters correctly")
        void shouldCountAsciiCorrectly() {
            assertThat(OpenGrapheme.length("hello")).isEqualTo(5);
        }

        @Test
        @DisplayName("Should count simple emoji as single grapheme")
        void shouldCountSimpleEmoji() {
            // 😀 is U+1F600 (2 UTF-16 code units)
            assertThat(OpenGrapheme.length("\uD83D\uDE00")).isEqualTo(1);
        }

        @Test
        @DisplayName("Should count family emoji as single grapheme")
        void shouldCountFamilyEmoji() {
            // "a👨‍👩‍👧‍👦b" should be 3 graphemes
            assertThat(OpenGrapheme.length("a" + FAMILY_EMOJI + "b")).isEqualTo(3);
        }

        @Test
        @DisplayName("Should count CJK characters correctly")
        void shouldCountCjkCorrectly() {
            assertThat(OpenGrapheme.length("\u4F60\u597D")).isEqualTo(2); // 你好
        }

        @Test
        @DisplayName("Should count combining characters correctly")
        void shouldCountCombiningCharacters() {
            // "e" + combining acute accent = single grapheme "é"
            assertThat(OpenGrapheme.length("e\u0301")).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Substring Tests")
    class Substring {

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNull() {
            assertThat(OpenGrapheme.substring(null, 0, 1)).isNull();
        }

        @Test
        @DisplayName("Should extract basic substring")
        void shouldExtractBasicSubstring() {
            assertThat(OpenGrapheme.substring("hello", 1, 3)).isEqualTo("el");
        }

        @Test
        @DisplayName("Should extract around emoji")
        void shouldExtractAroundEmoji() {
            String str = "a" + FAMILY_EMOJI + "b";
            assertThat(OpenGrapheme.substring(str, 0, 1)).isEqualTo("a");
            assertThat(OpenGrapheme.substring(str, 1, 2)).isEqualTo(FAMILY_EMOJI);
            assertThat(OpenGrapheme.substring(str, 2, 3)).isEqualTo("b");
        }

        @Test
        @DisplayName("Should extract full string")
        void shouldExtractFullString() {
            assertThat(OpenGrapheme.substring("abc", 0, 3)).isEqualTo("abc");
        }

        @Test
        @DisplayName("Should return empty for equal indices")
        void shouldReturnEmptyForEqualIndices() {
            assertThat(OpenGrapheme.substring("hello", 2, 2)).isEmpty();
        }

        @Test
        @DisplayName("Should throw for invalid indices")
        void shouldThrowForInvalidIndices() {
            assertThatThrownBy(() -> OpenGrapheme.substring("abc", -1, 2))
                    .isInstanceOf(IndexOutOfBoundsException.class);
            assertThatThrownBy(() -> OpenGrapheme.substring("abc", 0, 5))
                    .isInstanceOf(IndexOutOfBoundsException.class);
            assertThatThrownBy(() -> OpenGrapheme.substring("abc", 2, 1))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }
    }

    @Nested
    @DisplayName("Reverse Tests")
    class Reverse {

        @Test
        @DisplayName("Should return null for null")
        void shouldReturnNullForNull() {
            assertThat(OpenGrapheme.reverse(null)).isNull();
        }

        @Test
        @DisplayName("Should return empty for empty")
        void shouldReturnEmptyForEmpty() {
            assertThat(OpenGrapheme.reverse("")).isEmpty();
        }

        @Test
        @DisplayName("Should reverse ASCII string")
        void shouldReverseAscii() {
            assertThat(OpenGrapheme.reverse("abc")).isEqualTo("cba");
        }

        @Test
        @DisplayName("Should reverse preserving family emoji")
        void shouldReversePreservingFamilyEmoji() {
            String str = "a" + FAMILY_EMOJI + "b";
            String reversed = OpenGrapheme.reverse(str);
            assertThat(reversed).isEqualTo("b" + FAMILY_EMOJI + "a");
        }

        @Test
        @DisplayName("Should reverse preserving combining characters")
        void shouldReversePreservingCombiningCharacters() {
            // "ae\u0301" reversed should be "e\u0301a" (not "\u0301ea")
            String str = "ae\u0301";
            String reversed = OpenGrapheme.reverse(str);
            assertThat(reversed).isEqualTo("e\u0301a");
        }

        @Test
        @DisplayName("Should reverse single character")
        void shouldReverseSingleCharacter() {
            assertThat(OpenGrapheme.reverse("a")).isEqualTo("a");
        }
    }

    @Nested
    @DisplayName("DisplayWidth Tests")
    class DisplayWidth {

        @Test
        @DisplayName("Should return 0 for null or empty")
        void shouldReturnZeroForNullOrEmpty() {
            assertThat(OpenGrapheme.displayWidth(null)).isZero();
            assertThat(OpenGrapheme.displayWidth("")).isZero();
        }

        @Test
        @DisplayName("Should return length for ASCII")
        void shouldReturnLengthForAscii() {
            assertThat(OpenGrapheme.displayWidth("hello")).isEqualTo(5);
        }

        @Test
        @DisplayName("Should count CJK as double width")
        void shouldCountCjkAsDoubleWidth() {
            assertThat(OpenGrapheme.displayWidth("\u4F60\u597D")).isEqualTo(4); // 你好
        }

        @Test
        @DisplayName("Should handle mixed ASCII and CJK")
        void shouldHandleMixedAsciiAndCjk() {
            assertThat(OpenGrapheme.displayWidth("Hi\u4F60\u597D")).isEqualTo(6); // Hi你好
        }

        @Test
        @DisplayName("Should count Hangul as double width")
        void shouldCountHangulAsDoubleWidth() {
            assertThat(OpenGrapheme.displayWidth("\uD55C")).isEqualTo(2); // 한
        }

        @Test
        @DisplayName("Should count fullwidth forms as double width")
        void shouldCountFullwidthAsDoubleWidth() {
            assertThat(OpenGrapheme.displayWidth("\uFF21")).isEqualTo(2); // Ａ (fullwidth A)
        }
    }

    @Nested
    @DisplayName("TruncateToWidth Tests")
    class TruncateToWidth {

        @Test
        @DisplayName("Should return empty for null")
        void shouldReturnEmptyForNull() {
            assertThat(OpenGrapheme.truncateToWidth(null, 10)).isEmpty();
        }

        @Test
        @DisplayName("Should return string if within width")
        void shouldReturnStringIfWithinWidth() {
            assertThat(OpenGrapheme.truncateToWidth("hello", 10)).isEqualTo("hello");
        }

        @Test
        @DisplayName("Should truncate ASCII with ellipsis")
        void shouldTruncateAsciiWithEllipsis() {
            assertThat(OpenGrapheme.truncateToWidth("hello world", 8, "..."))
                    .isEqualTo("hello...");
        }

        @Test
        @DisplayName("Should truncate CJK with ellipsis")
        void shouldTruncateCjkWithEllipsis() {
            // 你好世界 = width 8, maxWidth=7 -> 你好... (4+3=7)
            assertThat(OpenGrapheme.truncateToWidth("\u4F60\u597D\u4E16\u754C", 7, "..."))
                    .isEqualTo("\u4F60\u597D...");
        }

        @Test
        @DisplayName("Should use default ellipsis")
        void shouldUseDefaultEllipsis() {
            assertThat(OpenGrapheme.truncateToWidth("hello world", 8))
                    .isEqualTo("hello...");
        }

        @Test
        @DisplayName("Should handle maxWidth less than ellipsis width")
        void shouldHandleMaxWidthLessThanEllipsisWidth() {
            // maxWidth=2, ellipsis "..." = width 3 -> just return first 2 chars of ellipsis
            String result = OpenGrapheme.truncateToWidth("hello world", 2, "...");
            assertThat(OpenGrapheme.displayWidth(result)).isLessThanOrEqualTo(2);
        }

        @Test
        @DisplayName("Should throw for negative maxWidth")
        void shouldThrowForNegativeMaxWidth() {
            assertThatThrownBy(() -> OpenGrapheme.truncateToWidth("hello", -1, "..."))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should handle null ellipsis")
        void shouldHandleNullEllipsis() {
            assertThat(OpenGrapheme.truncateToWidth("hello world", 5, null))
                    .isEqualTo("hello");
        }
    }
}
