package cloud.opencode.base.string.regex;

import org.junit.jupiter.api.*;

import java.util.regex.*;

import static org.assertj.core.api.Assertions.*;

/**
 * RegexUtilTest Tests
 * RegexUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("RegexUtil Tests")
class RegexUtilTest {

    @Nested
    @DisplayName("compile Tests")
    class CompileTests {

        @Test
        @DisplayName("Should compile regex pattern")
        void shouldCompileRegexPattern() {
            Pattern pattern = RegexUtil.compile("\\d+");
            assertThat(pattern.matcher("123").matches()).isTrue();
        }

        @Test
        @DisplayName("Should compile complex pattern")
        void shouldCompileComplexPattern() {
            Pattern pattern = RegexUtil.compile("[a-zA-Z]+\\s+\\d+");
            assertThat(pattern.matcher("abc 123").matches()).isTrue();
        }
    }

    @Nested
    @DisplayName("compileIgnoreCase Tests")
    class CompileIgnoreCaseTests {

        @Test
        @DisplayName("Should compile case insensitive pattern")
        void shouldCompileCaseInsensitivePattern() {
            Pattern pattern = RegexUtil.compileIgnoreCase("hello");
            assertThat(pattern.matcher("HELLO").matches()).isTrue();
            assertThat(pattern.matcher("hello").matches()).isTrue();
            assertThat(pattern.matcher("HeLLo").matches()).isTrue();
        }

        @Test
        @DisplayName("Should have CASE_INSENSITIVE flag")
        void shouldHaveCaseInsensitiveFlag() {
            Pattern pattern = RegexUtil.compileIgnoreCase("test");
            assertThat(pattern.flags() & Pattern.CASE_INSENSITIVE).isNotZero();
        }
    }

    @Nested
    @DisplayName("escape Tests")
    class EscapeTests {

        @Test
        @DisplayName("Should escape special regex characters")
        void shouldEscapeSpecialRegexCharacters() {
            String escaped = RegexUtil.escape("a.b*c?d+e");
            Pattern pattern = Pattern.compile(escaped);
            assertThat(pattern.matcher("a.b*c?d+e").matches()).isTrue();
            assertThat(pattern.matcher("aXbYcZdWe").matches()).isFalse();
        }

        @Test
        @DisplayName("Should escape parentheses")
        void shouldEscapeParentheses() {
            String escaped = RegexUtil.escape("(test)");
            Pattern pattern = Pattern.compile(escaped);
            assertThat(pattern.matcher("(test)").matches()).isTrue();
        }

        @Test
        @DisplayName("Should escape brackets")
        void shouldEscapeBrackets() {
            String escaped = RegexUtil.escape("[test]");
            Pattern pattern = Pattern.compile(escaped);
            assertThat(pattern.matcher("[test]").matches()).isTrue();
        }

        @Test
        @DisplayName("Should not modify plain strings")
        void shouldNotModifyPlainStrings() {
            String escaped = RegexUtil.escape("hello");
            Pattern pattern = Pattern.compile(escaped);
            assertThat(pattern.matcher("hello").matches()).isTrue();
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = RegexUtil.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
