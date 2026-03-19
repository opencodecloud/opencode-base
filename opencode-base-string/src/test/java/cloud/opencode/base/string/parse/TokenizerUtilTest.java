package cloud.opencode.base.string.parse;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TokenizerUtilTest Tests
 * TokenizerUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("TokenizerUtil Tests")
class TokenizerUtilTest {

    @Nested
    @DisplayName("tokenize with default delimiters Tests")
    class TokenizeDefaultTests {

        @Test
        @DisplayName("Should tokenize by space")
        void shouldTokenizeBySpace() {
            // Default delimiters include space
            List<String> tokens = TokenizerUtil.tokenize("Hello World", " ");
            assertThat(tokens).containsExactly("Hello", "World");
        }

        @Test
        @DisplayName("Should return empty list for null")
        void shouldReturnEmptyListForNull() {
            assertThat(TokenizerUtil.tokenize(null)).isEmpty();
        }

        @Test
        @DisplayName("Should handle multiple consecutive spaces")
        void shouldHandleMultipleConsecutiveSpaces() {
            List<String> tokens = TokenizerUtil.tokenize("Hello    World", " ");
            assertThat(tokens).containsExactly("Hello", "World");
        }

        @Test
        @DisplayName("Should tokenize with default delimiters")
        void shouldTokenizeWithDefaultDelimiters() {
            // Default delimiters include space, backslash, t, n, r, f as individual chars
            List<String> tokens = TokenizerUtil.tokenize("a b c");
            assertThat(tokens).contains("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("tokenize with custom delimiters Tests")
    class TokenizeCustomTests {

        @Test
        @DisplayName("Should tokenize by custom delimiter")
        void shouldTokenizeByCustomDelimiter() {
            List<String> tokens = TokenizerUtil.tokenize("a,b,c", ",");
            assertThat(tokens).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Should tokenize by multiple custom delimiters")
        void shouldTokenizeByMultipleCustomDelimiters() {
            List<String> tokens = TokenizerUtil.tokenize("a,b;c:d", ",;:");
            assertThat(tokens).containsExactly("a", "b", "c", "d");
        }

        @Test
        @DisplayName("Should return single element if no delimiter found")
        void shouldReturnSingleElementIfNoDelimiterFound() {
            List<String> tokens = TokenizerUtil.tokenize("abc", ",");
            assertThat(tokens).containsExactly("abc");
        }

        @Test
        @DisplayName("Should handle tab as delimiter")
        void shouldHandleTabAsDelimiter() {
            List<String> tokens = TokenizerUtil.tokenize("Hello\tWorld", "\t");
            assertThat(tokens).containsExactly("Hello", "World");
        }

        @Test
        @DisplayName("Should handle newline as delimiter")
        void shouldHandleNewlineAsDelimiter() {
            List<String> tokens = TokenizerUtil.tokenize("Hello\nWorld", "\n");
            assertThat(tokens).containsExactly("Hello", "World");
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = TokenizerUtil.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
