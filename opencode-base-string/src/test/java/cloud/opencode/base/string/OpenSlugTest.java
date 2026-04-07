package cloud.opencode.base.string;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenSlug Tests
 * OpenSlug 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.3
 */
@DisplayName("OpenSlug Tests")
class OpenSlugTest {

    @Nested
    @DisplayName("Basic Conversion Tests")
    class BasicConversion {

        @Test
        @DisplayName("Should convert simple string to slug")
        void shouldConvertSimpleString() {
            assertThat(OpenSlug.toSlug("Hello World")).isEqualTo("hello-world");
        }

        @Test
        @DisplayName("Should handle multiple spaces")
        void shouldHandleMultipleSpaces() {
            assertThat(OpenSlug.toSlug("Hello   World")).isEqualTo("hello-world");
        }

        @Test
        @DisplayName("Should handle leading and trailing spaces")
        void shouldHandleLeadingAndTrailingSpaces() {
            assertThat(OpenSlug.toSlug("  Hello World  ")).isEqualTo("hello-world");
        }

        @Test
        @DisplayName("Should return empty for null")
        void shouldReturnEmptyForNull() {
            assertThat(OpenSlug.toSlug(null)).isEmpty();
        }

        @Test
        @DisplayName("Should return empty for empty string")
        void shouldReturnEmptyForEmpty() {
            assertThat(OpenSlug.toSlug("")).isEmpty();
        }

        @Test
        @DisplayName("Should handle already slugified string")
        void shouldHandleAlreadySlugified() {
            assertThat(OpenSlug.toSlug("hello-world")).isEqualTo("hello-world");
        }
    }

    @Nested
    @DisplayName("Accent Stripping Tests")
    class AccentStripping {

        @Test
        @DisplayName("Should strip accents from French text")
        void shouldStripFrenchAccents() {
            assertThat(OpenSlug.toSlug("Cr\u00e8me Br\u00fbl\u00e9e")).isEqualTo("creme-brulee");
        }

        @Test
        @DisplayName("Should strip German umlauts")
        void shouldStripGermanUmlauts() {
            // ß (U+00DF) does not decompose in NFD, so it is replaced by separator
            assertThat(OpenSlug.toSlug("\u00dcber Stra\u00dfe")).isEqualTo("uber-stra-e");
        }

        @Test
        @DisplayName("Should strip Spanish tildes")
        void shouldStripSpanishTildes() {
            assertThat(OpenSlug.toSlug("Espa\u00f1a")).isEqualTo("espana");
        }
    }

    @Nested
    @DisplayName("CJK and Non-Latin Tests")
    class CjkTests {

        @Test
        @DisplayName("Should remove CJK characters")
        void shouldRemoveCjkCharacters() {
            // CJK chars are non-alphanumeric after NFD, get replaced by separator and trimmed
            assertThat(OpenSlug.toSlug("\u4F60\u597D")).isEmpty();
        }

        @Test
        @DisplayName("Should keep alphanumeric mixed with CJK")
        void shouldKeepAlphanumericMixedWithCjk() {
            assertThat(OpenSlug.toSlug("Hello\u4F60\u597DWorld")).isEqualTo("hello-world");
        }
    }

    @Nested
    @DisplayName("Special Characters Tests")
    class SpecialCharacters {

        @Test
        @DisplayName("Should replace special characters")
        void shouldReplaceSpecialCharacters() {
            assertThat(OpenSlug.toSlug("Hello! @World# $2024")).isEqualTo("hello-world-2024");
        }

        @Test
        @DisplayName("Should handle punctuation")
        void shouldHandlePunctuation() {
            assertThat(OpenSlug.toSlug("Hello, World.")).isEqualTo("hello-world");
        }

        @Test
        @DisplayName("Should preserve numbers")
        void shouldPreserveNumbers() {
            assertThat(OpenSlug.toSlug("Chapter 42: The Answer")).isEqualTo("chapter-42-the-answer");
        }
    }

    @Nested
    @DisplayName("Custom Separator Tests")
    class CustomSeparator {

        @Test
        @DisplayName("Should use underscore separator")
        void shouldUseUnderscoreSeparator() {
            assertThat(OpenSlug.toSlug("Hello World", "_")).isEqualTo("hello_world");
        }

        @Test
        @DisplayName("Should use dot separator")
        void shouldUseDotSeparator() {
            assertThat(OpenSlug.toSlug("Hello World", ".")).isEqualTo("hello.world");
        }

        @Test
        @DisplayName("Should handle null separator as default")
        void shouldHandleNullSeparator() {
            assertThat(OpenSlug.toSlug("Hello World", null)).isEqualTo("hello-world");
        }
    }

    @Nested
    @DisplayName("MaxLength Tests")
    class MaxLength {

        @Test
        @DisplayName("Should truncate at separator boundary")
        void shouldTruncateAtSeparatorBoundary() {
            assertThat(OpenSlug.toSlug("a very long title", "-", 10)).isEqualTo("a-very");
        }

        @Test
        @DisplayName("Should return full slug if within max length")
        void shouldReturnFullSlugIfWithinMaxLength() {
            assertThat(OpenSlug.toSlug("short", "-", 100)).isEqualTo("short");
        }

        @Test
        @DisplayName("Should hard truncate if no separator found")
        void shouldHardTruncateIfNoSeparatorFound() {
            assertThat(OpenSlug.toSlug("abcdefghij", "-", 5)).isEqualTo("abcde");
        }

        @Test
        @DisplayName("Should handle maxLength of 0")
        void shouldHandleMaxLengthOfZero() {
            assertThat(OpenSlug.toSlug("hello world", "-", 0)).isEmpty();
        }

        @Test
        @DisplayName("Should throw for negative maxLength")
        void shouldThrowForNegativeMaxLength() {
            assertThatThrownBy(() -> OpenSlug.toSlug("hello", "-", -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Regex-special Separator Tests (regression #1)")
    class RegexSpecialSeparator {

        @Test
        @DisplayName("Should handle separator containing dollar sign")
        void shouldHandleDollarSignSeparator() {
            assertThat(OpenSlug.toSlug("Hello World", "$1")).isEqualTo("hello$1world");
        }

        @Test
        @DisplayName("Should handle separator containing backslash")
        void shouldHandleBackslashSeparator() {
            assertThat(OpenSlug.toSlug("Hello World", "\\")).isEqualTo("hello\\world");
        }

        @Test
        @DisplayName("Should handle separator with mixed regex specials")
        void shouldHandleMixedRegexSpecials() {
            assertThat(OpenSlug.toSlug("a b c", "$")).isEqualTo("a$b$c");
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = OpenSlug.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
