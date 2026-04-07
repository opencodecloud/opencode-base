package cloud.opencode.base.json.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonSchemaException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JsonSchemaException 测试")
class JsonSchemaExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用消息创建")
        void testMessageConstructor() {
            JsonSchemaException ex = new JsonSchemaException("Validation failed");

            assertThat(ex.getMessage()).contains("Validation failed");
            assertThat(ex.getErrors()).hasSize(1);
            assertThat(ex.getSchemaUri()).isNull();
        }

        @Test
        @DisplayName("使用消息和错误列表创建")
        void testMessageErrorsConstructor() {
            List<JsonSchemaException.ValidationError> errors = List.of(
                new JsonSchemaException.ValidationError("/name", "required", "Property required"),
                new JsonSchemaException.ValidationError("/age", "type", "Expected number")
            );

            JsonSchemaException ex = new JsonSchemaException("Validation failed", errors);

            assertThat(ex.getErrors()).hasSize(2);
            assertThat(ex.getSchemaUri()).isNull();
        }

        @Test
        @DisplayName("使用完整参数创建")
        void testFullConstructor() {
            List<JsonSchemaException.ValidationError> errors = List.of(
                new JsonSchemaException.ValidationError("/name", "type", "Expected string")
            );

            JsonSchemaException ex = new JsonSchemaException(
                "Validation failed", errors, "http://example.com/schema.json");

            assertThat(ex.getSchemaUri()).isEqualTo("http://example.com/schema.json");
        }

        @Test
        @DisplayName("null错误列表使用空列表")
        void testNullErrors() {
            JsonSchemaException ex = new JsonSchemaException("Error", null);

            assertThat(ex.getErrors()).isEmpty();
        }
    }

    @Nested
    @DisplayName("ValidationError测试")
    class ValidationErrorTests {

        @Test
        @DisplayName("创建带路径、关键字和消息")
        void testSimpleValidationError() {
            JsonSchemaException.ValidationError error =
                new JsonSchemaException.ValidationError("/path", "required", "Missing property");

            assertThat(error.path()).isEqualTo("/path");
            assertThat(error.keyword()).isEqualTo("required");
            assertThat(error.message()).isEqualTo("Missing property");
            assertThat(error.expected()).isNull();
            assertThat(error.actual()).isNull();
        }

        @Test
        @DisplayName("创建带预期值和实际值")
        void testFullValidationError() {
            JsonSchemaException.ValidationError error =
                new JsonSchemaException.ValidationError("/age", "type", "Type mismatch", "number", "string");

            assertThat(error.expected()).isEqualTo("number");
            assertThat(error.actual()).isEqualTo("string");
        }

        @Test
        @DisplayName("toString输出格式")
        void testValidationErrorToString() {
            JsonSchemaException.ValidationError error =
                new JsonSchemaException.ValidationError("/name", "required", "Property is required");

            String str = error.toString();
            assertThat(str).contains("/name");
            assertThat(str).contains("Property is required");
            assertThat(str).contains("[required]");
        }

        @Test
        @DisplayName("toString路径为null时显示/")
        void testValidationErrorToStringNullPath() {
            JsonSchemaException.ValidationError error =
                new JsonSchemaException.ValidationError(null, "type", "Error");

            assertThat(error.toString()).startsWith("/");
        }

        @Test
        @DisplayName("toString关键字为null时不显示")
        void testValidationErrorToStringNullKeyword() {
            JsonSchemaException.ValidationError error =
                new JsonSchemaException.ValidationError("/path", null, "Error message");

            String str = error.toString();
            assertThat(str).doesNotContain("[");
        }
    }

    @Nested
    @DisplayName("查询方法测试")
    class QueryMethodTests {

        @Test
        @DisplayName("getErrorCount返回错误数量")
        void testGetErrorCount() {
            List<JsonSchemaException.ValidationError> errors = List.of(
                new JsonSchemaException.ValidationError("/a", "type", "Error 1"),
                new JsonSchemaException.ValidationError("/b", "type", "Error 2"),
                new JsonSchemaException.ValidationError("/c", "type", "Error 3")
            );

            JsonSchemaException ex = new JsonSchemaException("Validation failed", errors);

            assertThat(ex.getErrorCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("hasErrors有错误返回true")
        void testHasErrorsTrue() {
            JsonSchemaException ex = new JsonSchemaException("Error");

            assertThat(ex.hasErrors()).isTrue();
        }

        @Test
        @DisplayName("hasErrors无错误返回false")
        void testHasErrorsFalse() {
            JsonSchemaException ex = new JsonSchemaException("Error", List.of());

            assertThat(ex.hasErrors()).isFalse();
        }
    }

    @Nested
    @DisplayName("getMessage测试")
    class GetMessageTests {

        @Test
        @DisplayName("无错误时返回基本消息")
        void testMessageNoErrors() {
            JsonSchemaException ex = new JsonSchemaException("Failed", List.of());

            assertThat(ex.getMessage()).contains("Failed");
        }

        @Test
        @DisplayName("有一个错误时显示单数")
        void testMessageSingleError() {
            List<JsonSchemaException.ValidationError> errors = List.of(
                new JsonSchemaException.ValidationError("/path", "type", "Error")
            );
            JsonSchemaException ex = new JsonSchemaException("Failed", errors);

            assertThat(ex.getMessage()).contains("1 validation error");
            assertThat(ex.getMessage()).doesNotContain("errors)");
        }

        @Test
        @DisplayName("有多个错误时显示复数")
        void testMessageMultipleErrors() {
            List<JsonSchemaException.ValidationError> errors = List.of(
                new JsonSchemaException.ValidationError("/a", "type", "Error 1"),
                new JsonSchemaException.ValidationError("/b", "type", "Error 2")
            );
            JsonSchemaException ex = new JsonSchemaException("Failed", errors);

            assertThat(ex.getMessage()).contains("2 validation errors");
        }
    }

    @Nested
    @DisplayName("getErrorReport测试")
    class GetErrorReportTests {

        @Test
        @DisplayName("生成错误报告")
        void testErrorReport() {
            List<JsonSchemaException.ValidationError> errors = List.of(
                new JsonSchemaException.ValidationError("/name", "required", "Missing name"),
                new JsonSchemaException.ValidationError("/age", "type", "Invalid type")
            );
            JsonSchemaException ex = new JsonSchemaException("Validation failed", errors, "schema.json");

            String report = ex.getErrorReport();

            assertThat(report).contains("JSON Schema validation failed");
            assertThat(report).contains("schema.json");
            assertThat(report).contains("2 error(s)");
            assertThat(report).contains("1.");
            assertThat(report).contains("2.");
        }

        @Test
        @DisplayName("无schemaUri的报告")
        void testErrorReportNoSchema() {
            JsonSchemaException ex = new JsonSchemaException("Failed", List.of(
                new JsonSchemaException.ValidationError("/path", "type", "Error")
            ));

            String report = ex.getErrorReport();

            assertThat(report).contains("JSON Schema validation failed");
            assertThat(report).doesNotContain("against schema:");
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("typeMismatch创建类型不匹配错误")
        void testTypeMismatch() {
            JsonSchemaException ex = JsonSchemaException.typeMismatch("/age", "number", "string");

            assertThat(ex.getErrors()).hasSize(1);
            assertThat(ex.getErrors().get(0).keyword()).isEqualTo("type");
            assertThat(ex.getErrors().get(0).expected()).isEqualTo("number");
            assertThat(ex.getErrors().get(0).actual()).isEqualTo("string");
        }

        @Test
        @DisplayName("missingRequired创建必需属性错误")
        void testMissingRequired() {
            JsonSchemaException ex = JsonSchemaException.missingRequired("/user", "name");

            assertThat(ex.getErrors()).hasSize(1);
            assertThat(ex.getErrors().get(0).keyword()).isEqualTo("required");
            assertThat(ex.getMessage()).contains("name");
        }

        @Test
        @DisplayName("patternMismatch创建模式不匹配错误")
        void testPatternMismatch() {
            JsonSchemaException ex = JsonSchemaException.patternMismatch("/email", "^.+@.+$", "invalid");

            assertThat(ex.getErrors()).hasSize(1);
            assertThat(ex.getErrors().get(0).keyword()).isEqualTo("pattern");
        }

        @Test
        @DisplayName("constraintViolation创建约束违反错误")
        void testConstraintViolation() {
            JsonSchemaException ex = JsonSchemaException.constraintViolation("/count", "minimum", "Value must be >= 0");

            assertThat(ex.getErrors()).hasSize(1);
            assertThat(ex.getErrors().get(0).keyword()).isEqualTo("minimum");
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("创建空Builder")
        void testEmptyBuilder() {
            JsonSchemaException.Builder builder = JsonSchemaException.builder();

            assertThat(builder.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("添加错误")
        void testAddError() {
            JsonSchemaException.Builder builder = JsonSchemaException.builder()
                .addError(new JsonSchemaException.ValidationError("/path", "type", "Error"));

            assertThat(builder.hasErrors()).isTrue();
        }

        @Test
        @DisplayName("添加错误使用参数")
        void testAddErrorParams() {
            JsonSchemaException.Builder builder = JsonSchemaException.builder()
                .addError("/path", "required", "Missing property");

            assertThat(builder.hasErrors()).isTrue();
        }

        @Test
        @DisplayName("设置schemaUri")
        void testSchemaUri() {
            JsonSchemaException ex = JsonSchemaException.builder()
                .schemaUri("http://example.com/schema")
                .addError("/path", "type", "Error")
                .build();

            assertThat(ex.getSchemaUri()).isEqualTo("http://example.com/schema");
        }

        @Test
        @DisplayName("build创建异常")
        void testBuild() {
            JsonSchemaException ex = JsonSchemaException.builder()
                .addError("/a", "type", "Error 1")
                .addError("/b", "required", "Error 2")
                .build();

            assertThat(ex.getErrorCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("throwIfErrors有错误时抛出")
        void testThrowIfErrorsWithErrors() {
            JsonSchemaException.Builder builder = JsonSchemaException.builder()
                .addError("/path", "type", "Error");

            assertThatThrownBy(builder::throwIfErrors)
                .isInstanceOf(JsonSchemaException.class);
        }

        @Test
        @DisplayName("throwIfErrors无错误时不抛出")
        void testThrowIfErrorsNoErrors() {
            JsonSchemaException.Builder builder = JsonSchemaException.builder();

            assertThatCode(builder::throwIfErrors).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("build空错误使用默认消息")
        void testBuildEmptyErrors() {
            JsonSchemaException ex = JsonSchemaException.builder().build();

            assertThat(ex.getMessage()).contains("JSON Schema validation failed");
        }
    }
}
