package cloud.opencode.base.string.naming;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * WordUtilTest Tests
 * WordUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("WordUtil Tests")
class WordUtilTest {

    @Nested
    @DisplayName("splitWords Tests")
    class SplitWordsTests {

        @Test
        @DisplayName("Should return empty array for null")
        void shouldReturnEmptyArrayForNull() {
            assertThat(WordUtil.splitWords(null)).isEmpty();
        }

        @Test
        @DisplayName("Should return empty array for empty string")
        void shouldReturnEmptyArrayForEmptyString() {
            assertThat(WordUtil.splitWords("")).isEmpty();
        }

        @Test
        @DisplayName("Should split camelCase")
        void shouldSplitCamelCase() {
            assertThat(WordUtil.splitWords("getUserName")).containsExactly("get", "User", "Name");
        }

        @Test
        @DisplayName("Should split PascalCase")
        void shouldSplitPascalCase() {
            assertThat(WordUtil.splitWords("GetUserName")).containsExactly("Get", "User", "Name");
        }

        @Test
        @DisplayName("Should split snake_case")
        void shouldSplitSnakeCase() {
            assertThat(WordUtil.splitWords("get_user_name")).containsExactly("get", "user", "name");
        }

        @Test
        @DisplayName("Should split kebab-case")
        void shouldSplitKebabCase() {
            assertThat(WordUtil.splitWords("get-user-name")).containsExactly("get", "user", "name");
        }

        @Test
        @DisplayName("Should split dot.case")
        void shouldSplitDotCase() {
            assertThat(WordUtil.splitWords("get.user.name")).containsExactly("get", "user", "name");
        }

        @Test
        @DisplayName("Should split path/case")
        void shouldSplitPathCase() {
            assertThat(WordUtil.splitWords("get/user/name")).containsExactly("get", "user", "name");
        }

        @Test
        @DisplayName("Should split space separated")
        void shouldSplitSpaceSeparated() {
            assertThat(WordUtil.splitWords("get user name")).containsExactly("get", "user", "name");
        }

        @Test
        @DisplayName("Should handle acronyms")
        void shouldHandleAcronyms() {
            assertThat(WordUtil.splitWords("XMLParser")).containsExactly("XML", "Parser");
            assertThat(WordUtil.splitWords("parseHTMLDocument")).containsExactly("parse", "HTML", "Document");
        }

        @Test
        @DisplayName("Should handle mixed case")
        void shouldHandleMixedCase() {
            assertThat(WordUtil.splitWords("get_userName")).containsExactly("get", "user", "Name");
        }
    }

    @Nested
    @DisplayName("joinWords Tests")
    class JoinWordsTests {

        @Test
        @DisplayName("Should return empty string for null")
        void shouldReturnEmptyStringForNull() {
            assertThat(WordUtil.joinWords(null, "_")).isEmpty();
        }

        @Test
        @DisplayName("Should return empty string for empty array")
        void shouldReturnEmptyStringForEmptyArray() {
            assertThat(WordUtil.joinWords(new String[0], "_")).isEmpty();
        }

        @Test
        @DisplayName("Should join words with separator")
        void shouldJoinWordsWithSeparator() {
            assertThat(WordUtil.joinWords(new String[]{"get", "user", "name"}, "_"))
                .isEqualTo("get_user_name");
            assertThat(WordUtil.joinWords(new String[]{"get", "user", "name"}, "-"))
                .isEqualTo("get-user-name");
            assertThat(WordUtil.joinWords(new String[]{"get", "user", "name"}, ""))
                .isEqualTo("getusername");
        }
    }

    @Nested
    @DisplayName("normalizeWord Tests")
    class NormalizeWordTests {

        @Test
        @DisplayName("Should return empty string for null")
        void shouldReturnEmptyStringForNull() {
            assertThat(WordUtil.normalizeWord(null)).isEmpty();
        }

        @Test
        @DisplayName("Should lowercase and trim")
        void shouldLowercaseAndTrim() {
            assertThat(WordUtil.normalizeWord("  HELLO  ")).isEqualTo("hello");
            assertThat(WordUtil.normalizeWord("World")).isEqualTo("world");
        }
    }

    @Nested
    @DisplayName("capitalizeWord Tests")
    class CapitalizeWordTests {

        @Test
        @DisplayName("Should return null for null")
        void shouldReturnNullForNull() {
            assertThat(WordUtil.capitalizeWord(null)).isNull();
        }

        @Test
        @DisplayName("Should return empty for empty string")
        void shouldReturnEmptyForEmptyString() {
            assertThat(WordUtil.capitalizeWord("")).isEmpty();
        }

        @Test
        @DisplayName("Should capitalize first letter")
        void shouldCapitalizeFirstLetter() {
            assertThat(WordUtil.capitalizeWord("hello")).isEqualTo("Hello");
            assertThat(WordUtil.capitalizeWord("world")).isEqualTo("World");
        }

        @Test
        @DisplayName("Should handle already capitalized")
        void shouldHandleAlreadyCapitalized() {
            assertThat(WordUtil.capitalizeWord("Hello")).isEqualTo("Hello");
        }

        @Test
        @DisplayName("Should handle single character")
        void shouldHandleSingleCharacter() {
            assertThat(WordUtil.capitalizeWord("a")).isEqualTo("A");
        }
    }

    @Nested
    @DisplayName("uncapitalizeWord Tests")
    class UncapitalizeWordTests {

        @Test
        @DisplayName("Should return null for null")
        void shouldReturnNullForNull() {
            assertThat(WordUtil.uncapitalizeWord(null)).isNull();
        }

        @Test
        @DisplayName("Should return empty for empty string")
        void shouldReturnEmptyForEmptyString() {
            assertThat(WordUtil.uncapitalizeWord("")).isEmpty();
        }

        @Test
        @DisplayName("Should uncapitalize first letter")
        void shouldUncapitalizeFirstLetter() {
            assertThat(WordUtil.uncapitalizeWord("Hello")).isEqualTo("hello");
            assertThat(WordUtil.uncapitalizeWord("World")).isEqualTo("world");
        }

        @Test
        @DisplayName("Should handle already lowercase")
        void shouldHandleAlreadyLowercase() {
            assertThat(WordUtil.uncapitalizeWord("hello")).isEqualTo("hello");
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = WordUtil.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatThrownBy(constructor::newInstance)
                .hasCauseInstanceOf(UnsupportedOperationException.class);
        }
    }
}
