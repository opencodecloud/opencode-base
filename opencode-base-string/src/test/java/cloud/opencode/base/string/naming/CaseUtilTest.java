package cloud.opencode.base.string.naming;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * CaseUtilTest Tests
 * CaseUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("CaseUtil Tests")
class CaseUtilTest {

    @Nested
    @DisplayName("toCamelCase Tests")
    class ToCamelCaseTests {

        @Test
        @DisplayName("Should convert snake_case to camelCase")
        void shouldConvertSnakeCaseToCamelCase() {
            assertThat(CaseUtil.toCamelCase("get_user_name")).isEqualTo("getUserName");
        }

        @Test
        @DisplayName("Should convert kebab-case to camelCase")
        void shouldConvertKebabCaseToCamelCase() {
            assertThat(CaseUtil.toCamelCase("get-user-name")).isEqualTo("getUserName");
        }

        @Test
        @DisplayName("Should convert PascalCase to camelCase")
        void shouldConvertPascalCaseToCamelCase() {
            assertThat(CaseUtil.toCamelCase("GetUserName")).isEqualTo("getUserName");
        }

        @Test
        @DisplayName("Should handle null and empty strings")
        void shouldHandleNullAndEmptyStrings() {
            assertThat(CaseUtil.toCamelCase(null)).isNull();
            assertThat(CaseUtil.toCamelCase("")).isEmpty();
        }
    }

    @Nested
    @DisplayName("toPascalCase Tests")
    class ToPascalCaseTests {

        @Test
        @DisplayName("Should convert snake_case to PascalCase")
        void shouldConvertSnakeCaseToPascalCase() {
            assertThat(CaseUtil.toPascalCase("get_user_name")).isEqualTo("GetUserName");
        }

        @Test
        @DisplayName("Should convert camelCase to PascalCase")
        void shouldConvertCamelCaseToPascalCase() {
            assertThat(CaseUtil.toPascalCase("getUserName")).isEqualTo("GetUserName");
        }
    }

    @Nested
    @DisplayName("toSnakeCase Tests")
    class ToSnakeCaseTests {

        @Test
        @DisplayName("Should convert camelCase to snake_case")
        void shouldConvertCamelCaseToSnakeCase() {
            assertThat(CaseUtil.toSnakeCase("getUserName")).isEqualTo("get_user_name");
        }

        @Test
        @DisplayName("Should convert PascalCase to snake_case")
        void shouldConvertPascalCaseToSnakeCase() {
            assertThat(CaseUtil.toSnakeCase("GetUserName")).isEqualTo("get_user_name");
        }

        @Test
        @DisplayName("Should convert kebab-case to snake_case")
        void shouldConvertKebabCaseToSnakeCase() {
            assertThat(CaseUtil.toSnakeCase("get-user-name")).isEqualTo("get_user_name");
        }
    }

    @Nested
    @DisplayName("toUpperSnakeCase Tests")
    class ToUpperSnakeCaseTests {

        @Test
        @DisplayName("Should convert camelCase to UPPER_SNAKE_CASE")
        void shouldConvertCamelCaseToUpperSnakeCase() {
            assertThat(CaseUtil.toUpperSnakeCase("getUserName")).isEqualTo("GET_USER_NAME");
        }

        @Test
        @DisplayName("Should convert snake_case to UPPER_SNAKE_CASE")
        void shouldConvertSnakeCaseToUpperSnakeCase() {
            assertThat(CaseUtil.toUpperSnakeCase("get_user_name")).isEqualTo("GET_USER_NAME");
        }
    }

    @Nested
    @DisplayName("toKebabCase Tests")
    class ToKebabCaseTests {

        @Test
        @DisplayName("Should convert camelCase to kebab-case")
        void shouldConvertCamelCaseToKebabCase() {
            assertThat(CaseUtil.toKebabCase("getUserName")).isEqualTo("get-user-name");
        }

        @Test
        @DisplayName("Should convert snake_case to kebab-case")
        void shouldConvertSnakeCaseToKebabCase() {
            assertThat(CaseUtil.toKebabCase("get_user_name")).isEqualTo("get-user-name");
        }
    }

    @Nested
    @DisplayName("toDotCase Tests")
    class ToDotCaseTests {

        @Test
        @DisplayName("Should convert camelCase to dot.case")
        void shouldConvertCamelCaseToDotCase() {
            assertThat(CaseUtil.toDotCase("getUserName")).isEqualTo("get.user.name");
        }
    }

    @Nested
    @DisplayName("toPathCase Tests")
    class ToPathCaseTests {

        @Test
        @DisplayName("Should convert camelCase to path/case")
        void shouldConvertCamelCaseToPathCase() {
            assertThat(CaseUtil.toPathCase("getUserName")).isEqualTo("get/user/name");
        }
    }

    @Nested
    @DisplayName("toTitleCase Tests")
    class ToTitleCaseTests {

        @Test
        @DisplayName("Should convert camelCase to Title Case")
        void shouldConvertCamelCaseToTitleCase() {
            assertThat(CaseUtil.toTitleCase("getUserName")).isEqualTo("Get User Name");
        }
    }

    @Nested
    @DisplayName("toSentenceCase Tests")
    class ToSentenceCaseTests {

        @Test
        @DisplayName("Should convert camelCase to Sentence case")
        void shouldConvertCamelCaseToSentenceCase() {
            assertThat(CaseUtil.toSentenceCase("getUserName")).isEqualTo("Get user name");
        }
    }

    @Nested
    @DisplayName("convert Tests")
    class ConvertTests {

        @Test
        @DisplayName("Should convert with explicit target case")
        void shouldConvertWithExplicitTargetCase() {
            assertThat(CaseUtil.convert("getUserName", NamingCase.SNAKE_CASE)).isEqualTo("get_user_name");
            assertThat(CaseUtil.convert("get_user_name", NamingCase.CAMEL_CASE)).isEqualTo("getUserName");
        }

        @Test
        @DisplayName("Should convert with source and target case")
        void shouldConvertWithSourceAndTargetCase() {
            assertThat(CaseUtil.convert("getUserName", NamingCase.CAMEL_CASE, NamingCase.SNAKE_CASE))
                .isEqualTo("get_user_name");
        }
    }

    @Nested
    @DisplayName("detect Tests")
    class DetectTests {

        @Test
        @DisplayName("Should detect camelCase")
        void shouldDetectCamelCase() {
            assertThat(CaseUtil.detect("getUserName")).isEqualTo(NamingCase.CAMEL_CASE);
        }

        @Test
        @DisplayName("Should detect PascalCase")
        void shouldDetectPascalCase() {
            assertThat(CaseUtil.detect("GetUserName")).isEqualTo(NamingCase.PASCAL_CASE);
        }

        @Test
        @DisplayName("Should detect snake_case")
        void shouldDetectSnakeCase() {
            assertThat(CaseUtil.detect("get_user_name")).isEqualTo(NamingCase.SNAKE_CASE);
        }

        @Test
        @DisplayName("Should detect UPPER_SNAKE_CASE")
        void shouldDetectUpperSnakeCase() {
            assertThat(CaseUtil.detect("GET_USER_NAME")).isEqualTo(NamingCase.UPPER_SNAKE_CASE);
        }

        @Test
        @DisplayName("Should detect kebab-case")
        void shouldDetectKebabCase() {
            assertThat(CaseUtil.detect("get-user-name")).isEqualTo(NamingCase.KEBAB_CASE);
        }

        @Test
        @DisplayName("Should detect dot.case")
        void shouldDetectDotCase() {
            assertThat(CaseUtil.detect("get.user.name")).isEqualTo(NamingCase.DOT_CASE);
        }

        @Test
        @DisplayName("Should detect path/case")
        void shouldDetectPathCase() {
            assertThat(CaseUtil.detect("get/user/name")).isEqualTo(NamingCase.PATH_CASE);
        }

        @Test
        @DisplayName("Should detect Title Case")
        void shouldDetectTitleCase() {
            assertThat(CaseUtil.detect("Get User Name")).isEqualTo(NamingCase.TITLE_CASE);
        }

        @Test
        @DisplayName("Should detect Sentence case")
        void shouldDetectSentenceCase() {
            assertThat(CaseUtil.detect("Get user name")).isEqualTo(NamingCase.SENTENCE_CASE);
        }

        @Test
        @DisplayName("Should return default for null or empty")
        void shouldReturnDefaultForNullOrEmpty() {
            assertThat(CaseUtil.detect(null)).isEqualTo(NamingCase.CAMEL_CASE);
            assertThat(CaseUtil.detect("")).isEqualTo(NamingCase.CAMEL_CASE);
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = CaseUtil.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatThrownBy(constructor::newInstance)
                .hasCauseInstanceOf(UnsupportedOperationException.class);
        }
    }
}
