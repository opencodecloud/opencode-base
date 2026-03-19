package cloud.opencode.base.string.abbr;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenAbbreviationTest Tests
 * OpenAbbreviationTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("OpenAbbreviation Tests")
class OpenAbbreviationTest {

    @Nested
    @DisplayName("abbreviate with maxLength Tests")
    class AbbreviateMaxLengthTests {

        @Test
        @DisplayName("Should return original if shorter than maxLength")
        void shouldReturnOriginalIfShorterThanMaxLength() {
            assertThat(OpenAbbreviation.abbreviate("hello", 10)).isEqualTo("hello");
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertThat(OpenAbbreviation.abbreviate(null, 10)).isNull();
        }

        @Test
        @DisplayName("Should abbreviate with ellipsis")
        void shouldAbbreviateWithEllipsis() {
            assertThat(OpenAbbreviation.abbreviate("hello world", 8)).isEqualTo("hello...");
        }

        @Test
        @DisplayName("Should handle exact length")
        void shouldHandleExactLength() {
            assertThat(OpenAbbreviation.abbreviate("hello", 5)).isEqualTo("hello");
        }
    }

    @Nested
    @DisplayName("abbreviate with offset Tests")
    class AbbreviateOffsetTests {

        @Test
        @DisplayName("Should abbreviate from offset")
        void shouldAbbreviateFromOffset() {
            String result = OpenAbbreviation.abbreviate("hello world test", 6, 10, "...");
            assertThat(result).contains("...");
        }

        @Test
        @DisplayName("Should handle offset near end")
        void shouldHandleOffsetNearEnd() {
            String result = OpenAbbreviation.abbreviate("hello world", 10, 8, "...");
            assertThat(result).startsWith("...");
        }

        @Test
        @DisplayName("Should return substring when maxLength less than ellipsis")
        void shouldReturnSubstringWhenMaxLengthLessThanEllipsis() {
            String result = OpenAbbreviation.abbreviate("hello", 0, 2, "...");
            assertThat(result).isEqualTo("he");
        }
    }

    @Nested
    @DisplayName("abbreviateMiddle Tests")
    class AbbreviateMiddleTests {

        @Test
        @DisplayName("Should return original if shorter than maxLength")
        void shouldReturnOriginalIfShorterThanMaxLength() {
            assertThat(OpenAbbreviation.abbreviateMiddle("hello", "...", 10)).isEqualTo("hello");
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertThat(OpenAbbreviation.abbreviateMiddle(null, "...", 10)).isNull();
        }

        @Test
        @DisplayName("Should abbreviate in the middle")
        void shouldAbbreviateInTheMiddle() {
            // maxLength=8, middle="..."(3 chars), targetLen=5, startLen=2, endLen=3
            // Result: "he" + "..." + "rld" = "he...rld"
            assertThat(OpenAbbreviation.abbreviateMiddle("hello world", "...", 8))
                .isEqualTo("he...rld");
        }

        @Test
        @DisplayName("Should return substring when maxLength less than middle")
        void shouldReturnSubstringWhenMaxLengthLessThanMiddle() {
            String result = OpenAbbreviation.abbreviateMiddle("hello", "...", 2);
            assertThat(result).isEqualTo("he");
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = OpenAbbreviation.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
