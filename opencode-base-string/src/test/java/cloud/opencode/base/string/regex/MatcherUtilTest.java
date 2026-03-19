package cloud.opencode.base.string.regex;

import org.junit.jupiter.api.*;

import java.util.regex.*;

import static org.assertj.core.api.Assertions.*;

/**
 * MatcherUtilTest Tests
 * MatcherUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("MatcherUtil Tests")
class MatcherUtilTest {

    @Nested
    @DisplayName("create with regex string Tests")
    class CreateWithRegexStringTests {

        @Test
        @DisplayName("Should create matcher from regex string")
        void shouldCreateMatcherFromRegexString() {
            Matcher matcher = MatcherUtil.create("\\d+", "abc123def");
            assertThat(matcher.find()).isTrue();
            assertThat(matcher.group()).isEqualTo("123");
        }

        @Test
        @DisplayName("Should return non-matching matcher for no match")
        void shouldReturnNonMatchingMatcherForNoMatch() {
            Matcher matcher = MatcherUtil.create("\\d+", "abcdef");
            assertThat(matcher.find()).isFalse();
        }

        @Test
        @DisplayName("Should find multiple matches")
        void shouldFindMultipleMatches() {
            Matcher matcher = MatcherUtil.create("\\d+", "a1b22c333");
            assertThat(matcher.find()).isTrue();
            assertThat(matcher.group()).isEqualTo("1");
            assertThat(matcher.find()).isTrue();
            assertThat(matcher.group()).isEqualTo("22");
            assertThat(matcher.find()).isTrue();
            assertThat(matcher.group()).isEqualTo("333");
        }
    }

    @Nested
    @DisplayName("create with Pattern Tests")
    class CreateWithPatternTests {

        @Test
        @DisplayName("Should create matcher from Pattern")
        void shouldCreateMatcherFromPattern() {
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = MatcherUtil.create(pattern, "abc123def");
            assertThat(matcher.find()).isTrue();
            assertThat(matcher.group()).isEqualTo("123");
        }

        @Test
        @DisplayName("Should work with case insensitive pattern")
        void shouldWorkWithCaseInsensitivePattern() {
            Pattern pattern = Pattern.compile("hello", Pattern.CASE_INSENSITIVE);
            Matcher matcher = MatcherUtil.create(pattern, "HELLO world");
            assertThat(matcher.find()).isTrue();
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = MatcherUtil.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
