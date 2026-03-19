package cloud.opencode.base.string;

import cloud.opencode.base.string.naming.NamingCase;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenNamingTest Tests
 * OpenNamingTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("OpenNaming Tests")
class OpenNamingTest {

    @Nested
    @DisplayName("toCamelCase Tests")
    class ToCamelCaseTests {

        @Test
        @DisplayName("Should convert snake_case to camelCase")
        void shouldConvertSnakeCaseToCamelCase() {
            assertThat(OpenNaming.toCamelCase("get_user_name")).isEqualTo("getUserName");
        }

        @Test
        @DisplayName("Should convert PascalCase to camelCase")
        void shouldConvertPascalCaseToCamelCase() {
            assertThat(OpenNaming.toCamelCase("GetUserName")).isEqualTo("getUserName");
        }

        @Test
        @DisplayName("Should convert kebab-case to camelCase")
        void shouldConvertKebabCaseToCamelCase() {
            assertThat(OpenNaming.toCamelCase("get-user-name")).isEqualTo("getUserName");
        }
    }

    @Nested
    @DisplayName("toPascalCase Tests")
    class ToPascalCaseTests {

        @Test
        @DisplayName("Should convert snake_case to PascalCase")
        void shouldConvertSnakeCaseToPascalCase() {
            assertThat(OpenNaming.toPascalCase("get_user_name")).isEqualTo("GetUserName");
        }

        @Test
        @DisplayName("Should convert camelCase to PascalCase")
        void shouldConvertCamelCaseToPascalCase() {
            assertThat(OpenNaming.toPascalCase("getUserName")).isEqualTo("GetUserName");
        }
    }

    @Nested
    @DisplayName("toSnakeCase Tests")
    class ToSnakeCaseTests {

        @Test
        @DisplayName("Should convert camelCase to snake_case")
        void shouldConvertCamelCaseToSnakeCase() {
            assertThat(OpenNaming.toSnakeCase("getUserName")).isEqualTo("get_user_name");
        }

        @Test
        @DisplayName("Should convert PascalCase to snake_case")
        void shouldConvertPascalCaseToSnakeCase() {
            assertThat(OpenNaming.toSnakeCase("GetUserName")).isEqualTo("get_user_name");
        }
    }

    @Nested
    @DisplayName("toUpperSnakeCase Tests")
    class ToUpperSnakeCaseTests {

        @Test
        @DisplayName("Should convert camelCase to UPPER_SNAKE_CASE")
        void shouldConvertCamelCaseToUpperSnakeCase() {
            assertThat(OpenNaming.toUpperSnakeCase("maxRetryCount")).isEqualTo("MAX_RETRY_COUNT");
        }
    }

    @Nested
    @DisplayName("toKebabCase Tests")
    class ToKebabCaseTests {

        @Test
        @DisplayName("Should convert camelCase to kebab-case")
        void shouldConvertCamelCaseToKebabCase() {
            assertThat(OpenNaming.toKebabCase("getUserName")).isEqualTo("get-user-name");
        }
    }

    @Nested
    @DisplayName("toDotCase Tests")
    class ToDotCaseTests {

        @Test
        @DisplayName("Should convert camelCase to dot.case")
        void shouldConvertCamelCaseToDotCase() {
            assertThat(OpenNaming.toDotCase("getUserName")).isEqualTo("get.user.name");
        }
    }

    @Nested
    @DisplayName("toPathCase Tests")
    class ToPathCaseTests {

        @Test
        @DisplayName("Should convert PascalCase to path/case")
        void shouldConvertPascalCaseToPathCase() {
            assertThat(OpenNaming.toPathCase("UserController")).isEqualTo("user/controller");
        }
    }

    @Nested
    @DisplayName("toTitleCase Tests")
    class ToTitleCaseTests {

        @Test
        @DisplayName("Should convert camelCase to Title Case")
        void shouldConvertCamelCaseToTitleCase() {
            assertThat(OpenNaming.toTitleCase("getUserName")).isEqualTo("Get User Name");
        }
    }

    @Nested
    @DisplayName("toSentenceCase Tests")
    class ToSentenceCaseTests {

        @Test
        @DisplayName("Should convert camelCase to Sentence case")
        void shouldConvertCamelCaseToSentenceCase() {
            assertThat(OpenNaming.toSentenceCase("getUserName")).isEqualTo("Get user name");
        }
    }

    @Nested
    @DisplayName("convert Tests")
    class ConvertTests {

        @Test
        @DisplayName("Should convert with explicit source and target")
        void shouldConvertWithExplicitSourceAndTarget() {
            String result = OpenNaming.convert("get_user", NamingCase.SNAKE_CASE, NamingCase.CAMEL_CASE);
            assertThat(result).isEqualTo("getUser");
        }

        @Test
        @DisplayName("Should convert with auto-detect")
        void shouldConvertWithAutoDetect() {
            String result = OpenNaming.convert("getUserName", NamingCase.SNAKE_CASE);
            assertThat(result).isEqualTo("get_user_name");
        }
    }

    @Nested
    @DisplayName("detect Tests")
    class DetectTests {

        @Test
        @DisplayName("Should detect camelCase")
        void shouldDetectCamelCase() {
            assertThat(OpenNaming.detect("getUserName")).isEqualTo(NamingCase.CAMEL_CASE);
        }

        @Test
        @DisplayName("Should detect snake_case")
        void shouldDetectSnakeCase() {
            assertThat(OpenNaming.detect("get_user_name")).isEqualTo(NamingCase.SNAKE_CASE);
        }
    }

    @Nested
    @DisplayName("splitWords Tests")
    class SplitWordsTests {

        @Test
        @DisplayName("Should split camelCase into words")
        void shouldSplitCamelCaseIntoWords() {
            String[] words = OpenNaming.splitWords("getUserName");
            assertThat(words).containsExactly("get", "User", "Name");
        }
    }

    @Nested
    @DisplayName("joinWords Tests")
    class JoinWordsTests {

        @Test
        @DisplayName("Should join words with naming case")
        void shouldJoinWordsWithNamingCase() {
            String result = OpenNaming.joinWords(new String[]{"get", "user", "name"}, NamingCase.CAMEL_CASE);
            assertThat(result).isEqualTo("getUserName");
        }

        @Test
        @DisplayName("Should return empty string for null array")
        void shouldReturnEmptyStringForNullArray() {
            assertThat(OpenNaming.joinWords(null, NamingCase.CAMEL_CASE)).isEmpty();
        }

        @Test
        @DisplayName("Should return empty string for empty array")
        void shouldReturnEmptyStringForEmptyArray() {
            assertThat(OpenNaming.joinWords(new String[]{}, NamingCase.CAMEL_CASE)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Database to Java Conversion Tests")
    class DatabaseToJavaTests {

        @Test
        @DisplayName("tableToClass should convert table name to class name")
        void tableToClassShouldConvertTableNameToClassName() {
            assertThat(OpenNaming.tableToClass("sys_user")).isEqualTo("SysUser");
            assertThat(OpenNaming.tableToClass("user_info")).isEqualTo("UserInfo");
        }

        @Test
        @DisplayName("columnToField should convert column name to field name")
        void columnToFieldShouldConvertColumnNameToFieldName() {
            assertThat(OpenNaming.columnToField("user_name")).isEqualTo("userName");
            assertThat(OpenNaming.columnToField("created_at")).isEqualTo("createdAt");
        }
    }

    @Nested
    @DisplayName("Java to Database Conversion Tests")
    class JavaToDatabaseTests {

        @Test
        @DisplayName("classToTable should convert class name to table name")
        void classToTableShouldConvertClassNameToTableName() {
            assertThat(OpenNaming.classToTable("UserInfo")).isEqualTo("user_info");
            assertThat(OpenNaming.classToTable("SysUser")).isEqualTo("sys_user");
        }

        @Test
        @DisplayName("fieldToColumn should convert field name to column name")
        void fieldToColumnShouldConvertFieldNameToColumnName() {
            assertThat(OpenNaming.fieldToColumn("userName")).isEqualTo("user_name");
            assertThat(OpenNaming.fieldToColumn("createdAt")).isEqualTo("created_at");
        }
    }

    @Nested
    @DisplayName("classToPath Tests")
    class ClassToPathTests {

        @Test
        @DisplayName("Should convert class name to URL path")
        void shouldConvertClassNameToUrlPath() {
            assertThat(OpenNaming.classToPath("UserController")).isEqualTo("user/controller");
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = OpenNaming.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
