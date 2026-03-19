package cloud.opencode.base.string.naming;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * NamingCaseTest Tests
 * NamingCaseTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("NamingCase Tests")
class NamingCaseTest {

    @Nested
    @DisplayName("Enum Values Tests")
    class EnumValuesTests {

        @Test
        @DisplayName("Should have all expected values")
        void shouldHaveAllExpectedValues() {
            assertThat(NamingCase.values()).hasSize(9);
            assertThat(NamingCase.values()).contains(
                NamingCase.CAMEL_CASE,
                NamingCase.PASCAL_CASE,
                NamingCase.SNAKE_CASE,
                NamingCase.UPPER_SNAKE_CASE,
                NamingCase.KEBAB_CASE,
                NamingCase.DOT_CASE,
                NamingCase.PATH_CASE,
                NamingCase.TITLE_CASE,
                NamingCase.SENTENCE_CASE
            );
        }

        @Test
        @DisplayName("valueOf should work for all values")
        void valueOfShouldWorkForAllValues() {
            assertThat(NamingCase.valueOf("CAMEL_CASE")).isEqualTo(NamingCase.CAMEL_CASE);
            assertThat(NamingCase.valueOf("PASCAL_CASE")).isEqualTo(NamingCase.PASCAL_CASE);
            assertThat(NamingCase.valueOf("SNAKE_CASE")).isEqualTo(NamingCase.SNAKE_CASE);
        }
    }

    @Nested
    @DisplayName("getSeparator Tests")
    class GetSeparatorTests {

        @Test
        @DisplayName("CAMEL_CASE should have empty separator")
        void camelCaseShouldHaveEmptySeparator() {
            assertThat(NamingCase.CAMEL_CASE.getSeparator()).isEmpty();
        }

        @Test
        @DisplayName("PASCAL_CASE should have empty separator")
        void pascalCaseShouldHaveEmptySeparator() {
            assertThat(NamingCase.PASCAL_CASE.getSeparator()).isEmpty();
        }

        @Test
        @DisplayName("SNAKE_CASE should have underscore separator")
        void snakeCaseShouldHaveUnderscoreSeparator() {
            assertThat(NamingCase.SNAKE_CASE.getSeparator()).isEqualTo("_");
        }

        @Test
        @DisplayName("UPPER_SNAKE_CASE should have underscore separator")
        void upperSnakeCaseShouldHaveUnderscoreSeparator() {
            assertThat(NamingCase.UPPER_SNAKE_CASE.getSeparator()).isEqualTo("_");
        }

        @Test
        @DisplayName("KEBAB_CASE should have hyphen separator")
        void kebabCaseShouldHaveHyphenSeparator() {
            assertThat(NamingCase.KEBAB_CASE.getSeparator()).isEqualTo("-");
        }

        @Test
        @DisplayName("DOT_CASE should have dot separator")
        void dotCaseShouldHaveDotSeparator() {
            assertThat(NamingCase.DOT_CASE.getSeparator()).isEqualTo(".");
        }

        @Test
        @DisplayName("PATH_CASE should have slash separator")
        void pathCaseShouldHaveSlashSeparator() {
            assertThat(NamingCase.PATH_CASE.getSeparator()).isEqualTo("/");
        }

        @Test
        @DisplayName("TITLE_CASE should have space separator")
        void titleCaseShouldHaveSpaceSeparator() {
            assertThat(NamingCase.TITLE_CASE.getSeparator()).isEqualTo(" ");
        }

        @Test
        @DisplayName("SENTENCE_CASE should have space separator")
        void sentenceCaseShouldHaveSpaceSeparator() {
            assertThat(NamingCase.SENTENCE_CASE.getSeparator()).isEqualTo(" ");
        }
    }

    @Nested
    @DisplayName("isCapitalized Tests")
    class IsCapitalizedTests {

        @Test
        @DisplayName("CAMEL_CASE should not be capitalized")
        void camelCaseShouldNotBeCapitalized() {
            assertThat(NamingCase.CAMEL_CASE.isCapitalized()).isFalse();
        }

        @Test
        @DisplayName("PASCAL_CASE should be capitalized")
        void pascalCaseShouldBeCapitalized() {
            assertThat(NamingCase.PASCAL_CASE.isCapitalized()).isTrue();
        }

        @Test
        @DisplayName("SNAKE_CASE should not be capitalized")
        void snakeCaseShouldNotBeCapitalized() {
            assertThat(NamingCase.SNAKE_CASE.isCapitalized()).isFalse();
        }

        @Test
        @DisplayName("TITLE_CASE should be capitalized")
        void titleCaseShouldBeCapitalized() {
            assertThat(NamingCase.TITLE_CASE.isCapitalized()).isTrue();
        }

        @Test
        @DisplayName("SENTENCE_CASE should be capitalized")
        void sentenceCaseShouldBeCapitalized() {
            assertThat(NamingCase.SENTENCE_CASE.isCapitalized()).isTrue();
        }
    }

    @Nested
    @DisplayName("isCapitalizeWords Tests")
    class IsCapitalizeWordsTests {

        @Test
        @DisplayName("CAMEL_CASE should capitalize words")
        void camelCaseShouldCapitalizeWords() {
            assertThat(NamingCase.CAMEL_CASE.isCapitalizeWords()).isTrue();
        }

        @Test
        @DisplayName("PASCAL_CASE should capitalize words")
        void pascalCaseShouldCapitalizeWords() {
            assertThat(NamingCase.PASCAL_CASE.isCapitalizeWords()).isTrue();
        }

        @Test
        @DisplayName("SNAKE_CASE should not capitalize words")
        void snakeCaseShouldNotCapitalizeWords() {
            assertThat(NamingCase.SNAKE_CASE.isCapitalizeWords()).isFalse();
        }

        @Test
        @DisplayName("TITLE_CASE should capitalize words")
        void titleCaseShouldCapitalizeWords() {
            assertThat(NamingCase.TITLE_CASE.isCapitalizeWords()).isTrue();
        }
    }

    @Nested
    @DisplayName("isUpperCase Tests")
    class IsUpperCaseTests {

        @Test
        @DisplayName("UPPER_SNAKE_CASE should be uppercase")
        void upperSnakeCaseShouldBeUppercase() {
            assertThat(NamingCase.UPPER_SNAKE_CASE.isUpperCase()).isTrue();
        }

        @Test
        @DisplayName("SNAKE_CASE should not be uppercase")
        void snakeCaseShouldNotBeUppercase() {
            assertThat(NamingCase.SNAKE_CASE.isUpperCase()).isFalse();
        }

        @Test
        @DisplayName("CAMEL_CASE should not be uppercase")
        void camelCaseShouldNotBeUppercase() {
            assertThat(NamingCase.CAMEL_CASE.isUpperCase()).isFalse();
        }
    }

    @Nested
    @DisplayName("hasSeparator Tests")
    class HasSeparatorTests {

        @Test
        @DisplayName("CAMEL_CASE should not have separator")
        void camelCaseShouldNotHaveSeparator() {
            assertThat(NamingCase.CAMEL_CASE.hasSeparator()).isFalse();
        }

        @Test
        @DisplayName("PASCAL_CASE should not have separator")
        void pascalCaseShouldNotHaveSeparator() {
            assertThat(NamingCase.PASCAL_CASE.hasSeparator()).isFalse();
        }

        @Test
        @DisplayName("SNAKE_CASE should have separator")
        void snakeCaseShouldHaveSeparator() {
            assertThat(NamingCase.SNAKE_CASE.hasSeparator()).isTrue();
        }

        @Test
        @DisplayName("KEBAB_CASE should have separator")
        void kebabCaseShouldHaveSeparator() {
            assertThat(NamingCase.KEBAB_CASE.hasSeparator()).isTrue();
        }

        @Test
        @DisplayName("DOT_CASE should have separator")
        void dotCaseShouldHaveSeparator() {
            assertThat(NamingCase.DOT_CASE.hasSeparator()).isTrue();
        }
    }
}
