package cloud.opencode.base.string.regex;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenRegexTest Tests
 * OpenRegexTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("OpenRegex Tests")
class OpenRegexTest {

    @Nested
    @DisplayName("matches Tests")
    class MatchesTests {

        @Test
        @DisplayName("Should match with string regex")
        void shouldMatchWithStringRegex() {
            assertThat(OpenRegex.matches("hello123", "\\w+")).isTrue();
            assertThat(OpenRegex.matches("hello", "\\d+")).isFalse();
        }

        @Test
        @DisplayName("Should return false for null")
        void shouldReturnFalseForNull() {
            assertThat(OpenRegex.matches(null, "\\w+")).isFalse();
        }

        @Test
        @DisplayName("Should match with Pattern")
        void shouldMatchWithPattern() {
            Pattern pattern = Pattern.compile("\\d+");
            assertThat(OpenRegex.matches("123", pattern)).isTrue();
            assertThat(OpenRegex.matches("abc", pattern)).isFalse();
        }
    }

    @Nested
    @DisplayName("contains Tests")
    class ContainsTests {

        @Test
        @DisplayName("Should find pattern in string")
        void shouldFindPatternInString() {
            assertThat(OpenRegex.contains("hello123world", "\\d+")).isTrue();
            assertThat(OpenRegex.contains("hello", "\\d+")).isFalse();
        }

        @Test
        @DisplayName("Should return false for null")
        void shouldReturnFalseForNull() {
            assertThat(OpenRegex.contains(null, "\\d+")).isFalse();
        }
    }

    @Nested
    @DisplayName("findFirst Tests")
    class FindFirstTests {

        @Test
        @DisplayName("Should find first match")
        void shouldFindFirstMatch() {
            assertThat(OpenRegex.findFirst("abc123def456", "\\d+")).isEqualTo("123");
        }

        @Test
        @DisplayName("Should return null when no match")
        void shouldReturnNullWhenNoMatch() {
            assertThat(OpenRegex.findFirst("abc", "\\d+")).isNull();
            assertThat(OpenRegex.findFirst(null, "\\d+")).isNull();
        }
    }

    @Nested
    @DisplayName("findAll Tests")
    class FindAllTests {

        @Test
        @DisplayName("Should find all matches")
        void shouldFindAllMatches() {
            List<String> matches = OpenRegex.findAll("abc123def456", "\\d+");
            assertThat(matches).containsExactly("123", "456");
        }

        @Test
        @DisplayName("Should return empty list for null")
        void shouldReturnEmptyListForNull() {
            assertThat(OpenRegex.findAll(null, "\\d+")).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list when no match")
        void shouldReturnEmptyListWhenNoMatch() {
            assertThat(OpenRegex.findAll("abc", "\\d+")).isEmpty();
        }
    }

    @Nested
    @DisplayName("findGroup Tests")
    class FindGroupTests {

        @Test
        @DisplayName("Should find specific group")
        void shouldFindSpecificGroup() {
            assertThat(OpenRegex.findGroup("hello123", "([a-zA-Z]+)(\\d+)", 1)).isEqualTo("hello");
            assertThat(OpenRegex.findGroup("hello123", "([a-zA-Z]+)(\\d+)", 2)).isEqualTo("123");
        }

        @Test
        @DisplayName("Should return null for invalid group")
        void shouldReturnNullForInvalidGroup() {
            assertThat(OpenRegex.findGroup("hello", "(\\w+)", 5)).isNull();
            assertThat(OpenRegex.findGroup(null, "\\d+", 1)).isNull();
        }
    }

    @Nested
    @DisplayName("findAllGroups Tests")
    class FindAllGroupsTests {

        @Test
        @DisplayName("Should find all groups")
        void shouldFindAllGroups() {
            List<String[]> groups = OpenRegex.findAllGroups("a1b2", "(\\w)(\\d)");
            assertThat(groups).hasSize(2);
            assertThat(groups.get(0)).containsExactly("a1", "a", "1");
            assertThat(groups.get(1)).containsExactly("b2", "b", "2");
        }

        @Test
        @DisplayName("Should return empty list for null")
        void shouldReturnEmptyListForNull() {
            assertThat(OpenRegex.findAllGroups(null, "\\d")).isEmpty();
        }
    }

    @Nested
    @DisplayName("findNamedGroups Tests")
    class FindNamedGroupsTests {

        @Test
        @DisplayName("Should return empty map for null")
        void shouldReturnEmptyMapForNull() {
            assertThat(OpenRegex.findNamedGroups(null, "\\d+")).isEmpty();
        }

        @Test
        @DisplayName("Should return empty map when no match")
        void shouldReturnEmptyMapWhenNoMatch() {
            assertThat(OpenRegex.findNamedGroups("abc", "\\d+")).isEmpty();
        }
    }

    @Nested
    @DisplayName("replaceFirst Tests")
    class ReplaceFirstTests {

        @Test
        @DisplayName("Should replace first match")
        void shouldReplaceFirstMatch() {
            assertThat(OpenRegex.replaceFirst("a1b2c3", "\\d", "X")).isEqualTo("aXb2c3");
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertThat(OpenRegex.replaceFirst(null, "\\d", "X")).isNull();
        }
    }

    @Nested
    @DisplayName("replaceAll Tests")
    class ReplaceAllTests {

        @Test
        @DisplayName("Should replace all matches with string")
        void shouldReplaceAllMatchesWithString() {
            assertThat(OpenRegex.replaceAll("a1b2c3", "\\d", "X")).isEqualTo("aXbXcX");
        }

        @Test
        @DisplayName("Should replace all matches with function")
        void shouldReplaceAllMatchesWithFunction() {
            String result = OpenRegex.replaceAll("a1b2", "\\d", m -> "[" + m + "]");
            assertThat(result).isEqualTo("a[1]b[2]");
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertThat(OpenRegex.replaceAll((String) null, "\\d", "X")).isNull();
            assertThat(OpenRegex.replaceAll(null, "\\d", m -> m)).isNull();
        }
    }

    @Nested
    @DisplayName("split Tests")
    class SplitTests {

        @Test
        @DisplayName("Should split by regex")
        void shouldSplitByRegex() {
            assertThat(OpenRegex.split("a1b2c3", "\\d")).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Should split with limit")
        void shouldSplitWithLimit() {
            assertThat(OpenRegex.split("a1b2c3", "\\d", 2)).containsExactly("a", "b2c3");
        }

        @Test
        @DisplayName("Should return empty array for null")
        void shouldReturnEmptyArrayForNull() {
            assertThat(OpenRegex.split(null, "\\d")).isEmpty();
            assertThat(OpenRegex.split(null, "\\d", 2)).isEmpty();
        }
    }

    @Nested
    @DisplayName("escape Tests")
    class EscapeTests {

        @Test
        @DisplayName("Should escape regex special characters")
        void shouldEscapeRegexSpecialCharacters() {
            String escaped = OpenRegex.escape("a.b*c?");
            assertThat(Pattern.matches(escaped, "a.b*c?")).isTrue();
        }
    }

    @Nested
    @DisplayName("compile Tests")
    class CompileTests {

        @Test
        @DisplayName("Should compile regex")
        void shouldCompileRegex() {
            Pattern pattern = OpenRegex.compile("\\d+");
            assertThat(pattern.matcher("123").matches()).isTrue();
        }

        @Test
        @DisplayName("Should compile case insensitive")
        void shouldCompileCaseInsensitive() {
            Pattern pattern = OpenRegex.compileIgnoreCase("hello");
            assertThat(pattern.matcher("HELLO").matches()).isTrue();
        }
    }

    @Nested
    @DisplayName("countMatches Tests")
    class CountMatchesTests {

        @Test
        @DisplayName("Should count matches")
        void shouldCountMatches() {
            assertThat(OpenRegex.countMatches("a1b2c3", "\\d")).isEqualTo(3);
        }

        @Test
        @DisplayName("Should return 0 for null")
        void shouldReturnZeroForNull() {
            assertThat(OpenRegex.countMatches(null, "\\d")).isZero();
        }
    }

    @Nested
    @DisplayName("findPositions Tests")
    class FindPositionsTests {

        @Test
        @DisplayName("Should find positions of matches")
        void shouldFindPositionsOfMatches() {
            List<int[]> positions = OpenRegex.findPositions("a1b2c3", "\\d");
            assertThat(positions).hasSize(3);
            assertThat(positions.get(0)).containsExactly(1, 2);
        }

        @Test
        @DisplayName("Should return empty list for null")
        void shouldReturnEmptyListForNull() {
            assertThat(OpenRegex.findPositions(null, "\\d")).isEmpty();
        }
    }

    @Nested
    @DisplayName("Validation Methods Tests")
    class ValidationMethodsTests {

        @Test
        @DisplayName("isEmail should validate email")
        void isEmailShouldValidateEmail() {
            assertThat(OpenRegex.isEmail("test@example.com")).isTrue();
            assertThat(OpenRegex.isEmail("invalid")).isFalse();
        }

        @Test
        @DisplayName("isUrl should validate URL")
        void isUrlShouldValidateUrl() {
            assertThat(OpenRegex.isUrl("https://example.com")).isTrue();
            assertThat(OpenRegex.isUrl("invalid")).isFalse();
        }

        @Test
        @DisplayName("isIpv4 should validate IPv4")
        void isIpv4ShouldValidateIpv4() {
            assertThat(OpenRegex.isIpv4("192.168.1.1")).isTrue();
            assertThat(OpenRegex.isIpv4("invalid")).isFalse();
        }

        @Test
        @DisplayName("isUuid should validate UUID")
        void isUuidShouldValidateUuid() {
            assertThat(OpenRegex.isUuid("550e8400-e29b-41d4-a716-446655440000")).isTrue();
            assertThat(OpenRegex.isUuid("invalid")).isFalse();
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = OpenRegex.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
