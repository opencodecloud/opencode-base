package cloud.opencode.base.string.text;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenTruncateTest Tests
 * OpenTruncateTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("OpenTruncate Tests")
class OpenTruncateTest {

    @Nested
    @DisplayName("truncate Tests")
    class TruncateTests {

        @Test
        @DisplayName("Should truncate string longer than maxLength")
        void shouldTruncateStringLongerThanMaxLength() {
            assertThat(OpenTruncate.truncate("Hello World", 8)).isEqualTo("Hello...");
        }

        @Test
        @DisplayName("Should return original if shorter than maxLength")
        void shouldReturnOriginalIfShorter() {
            assertThat(OpenTruncate.truncate("Hello", 10)).isEqualTo("Hello");
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertThat(OpenTruncate.truncate(null, 10)).isNull();
        }

        @Test
        @DisplayName("Should truncate with custom ellipsis")
        void shouldTruncateWithCustomEllipsis() {
            assertThat(OpenTruncate.truncate("Hello World", 8, "~")).isEqualTo("Hello W~");
        }

        @Test
        @DisplayName("Should handle maxLength less than ellipsis length")
        void shouldHandleMaxLengthLessThanEllipsisLength() {
            assertThat(OpenTruncate.truncate("Hello World", 2, "...")).isEqualTo("He");
        }

        @Test
        @DisplayName("Should return exact maxLength when equal")
        void shouldReturnExactMaxLengthWhenEqual() {
            assertThat(OpenTruncate.truncate("Hello", 5)).isEqualTo("Hello");
        }
    }

    @Nested
    @DisplayName("truncateMiddle Tests")
    class TruncateMiddleTests {

        @Test
        @DisplayName("Should truncate middle of string")
        void shouldTruncateMiddleOfString() {
            assertThat(OpenTruncate.truncateMiddle("Hello World", 8)).isEqualTo("He...ld");
        }

        @Test
        @DisplayName("Should return original if shorter than maxLength")
        void shouldReturnOriginalIfShorter() {
            assertThat(OpenTruncate.truncateMiddle("Hello", 10)).isEqualTo("Hello");
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertThat(OpenTruncate.truncateMiddle(null, 10)).isNull();
        }

        @Test
        @DisplayName("Should handle maxLength less than ellipsis length")
        void shouldHandleMaxLengthLessThanEllipsisLength() {
            assertThat(OpenTruncate.truncateMiddle("Hello World", 2)).isEqualTo("He");
        }

        @Test
        @DisplayName("Should truncate middle with custom ellipsis")
        void shouldTruncateMiddleWithCustomEllipsis() {
            assertThat(OpenTruncate.truncateMiddle("Hello World", 9, "-")).isEqualTo("Hell-orld");
        }
    }

    @Nested
    @DisplayName("truncateByBytes Tests")
    class TruncateByBytesTests {

        @Test
        @DisplayName("Should truncate by UTF-8 bytes")
        void shouldTruncateByUtf8Bytes() {
            // Chinese characters take 3 bytes each in UTF-8
            String chinese = "你好世界";
            String result = OpenTruncate.truncateByBytes(chinese, 6, "UTF-8");
            assertThat(result).isEqualTo("你好");
        }

        @Test
        @DisplayName("Should return original if bytes within limit")
        void shouldReturnOriginalIfBytesWithinLimit() {
            assertThat(OpenTruncate.truncateByBytes("Hello", 10, "UTF-8")).isEqualTo("Hello");
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertThat(OpenTruncate.truncateByBytes(null, 10, "UTF-8")).isNull();
        }

        @Test
        @DisplayName("Should return original for invalid charset")
        void shouldReturnOriginalForInvalidCharset() {
            assertThat(OpenTruncate.truncateByBytes("Hello", 3, "INVALID-CHARSET")).isEqualTo("Hello");
        }

        @Test
        @DisplayName("Should return empty string when maxBytes is 0")
        void shouldReturnEmptyStringWhenMaxBytesIsZero() {
            assertThat(OpenTruncate.truncateByBytes("Hello", 0, "UTF-8")).isEmpty();
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = OpenTruncate.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
