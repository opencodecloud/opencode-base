package cloud.opencode.base.yml.schema;

import cloud.opencode.base.yml.YmlDocument;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * YmlSchemaTest Tests
 * YmlSchemaTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.3
 */
@DisplayName("YmlSchema Tests")
class YmlSchemaTest {

    @Nested
    @DisplayName("Required Constraints")
    class Required {

        @Test
        @DisplayName("should pass when all required keys are present")
        void shouldPassWhenAllRequiredKeysPresent() {
            YmlSchema schema = YmlSchema.builder()
                .required("name", "version")
                .build();

            Map<String, Object> data = Map.of("name", "app", "version", "1.0");

            ValidationResult result = schema.validate(data);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("should fail when required key is missing")
        void shouldFailWhenRequiredKeyMissing() {
            YmlSchema schema = YmlSchema.builder()
                .required("name", "version")
                .build();

            Map<String, Object> data = Map.of("name", "app");

            ValidationResult result = schema.validate(data);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).path()).isEqualTo("version");
            assertThat(result.getErrors().get(0).type())
                .isEqualTo(ValidationError.ErrorType.MISSING_REQUIRED);
        }

        @Test
        @DisplayName("should report multiple missing keys")
        void shouldReportMultipleMissingKeys() {
            YmlSchema schema = YmlSchema.builder()
                .required("a", "b", "c")
                .build();

            Map<String, Object> data = Map.of();

            ValidationResult result = schema.validate(data);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(3);
        }

        @Test
        @DisplayName("should fail when data is null")
        void shouldFailWhenDataIsNull() {
            YmlSchema schema = YmlSchema.builder()
                .required("name")
                .build();

            ValidationResult result = schema.validate((Map<String, Object>) null);

            assertThat(result.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("Type Constraints")
    class TypeConstraints {

        @Test
        @DisplayName("should pass when value matches expected type")
        void shouldPassWhenValueMatchesType() {
            YmlSchema schema = YmlSchema.builder()
                .type("name", String.class)
                .type("port", Integer.class)
                .build();

            Map<String, Object> data = Map.of("name", "app", "port", 8080);

            ValidationResult result = schema.validate(data);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should fail when value does not match expected type")
        void shouldFailWhenValueDoesNotMatchType() {
            YmlSchema schema = YmlSchema.builder()
                .type("port", Integer.class)
                .build();

            Map<String, Object> data = Map.of("port", "not-a-number");

            ValidationResult result = schema.validate(data);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors().get(0).type())
                .isEqualTo(ValidationError.ErrorType.TYPE_MISMATCH);
            assertThat(result.getErrors().get(0).message()).contains("Integer");
            assertThat(result.getErrors().get(0).message()).contains("String");
        }

        @Test
        @DisplayName("should skip type check for missing values")
        void shouldSkipTypeCheckForMissingValues() {
            YmlSchema schema = YmlSchema.builder()
                .type("optional", String.class)
                .build();

            Map<String, Object> data = Map.of();

            ValidationResult result = schema.validate(data);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should handle nested path type checks")
        void shouldHandleNestedPathTypeChecks() {
            YmlSchema schema = YmlSchema.builder()
                .type("server.port", Integer.class)
                .build();

            Map<String, Object> server = Map.of("port", 8080);
            Map<String, Object> data = Map.of("server", server);

            ValidationResult result = schema.validate(data);

            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("Range Constraints")
    class RangeConstraints {

        @Test
        @DisplayName("should pass when value is within range")
        void shouldPassWhenValueInRange() {
            YmlSchema schema = YmlSchema.builder()
                .range("port", 1, 65535)
                .build();

            Map<String, Object> data = Map.of("port", 8080);

            ValidationResult result = schema.validate(data);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should fail when value is below minimum")
        void shouldFailWhenBelowMinimum() {
            YmlSchema schema = YmlSchema.builder()
                .range("port", 1, 65535)
                .build();

            Map<String, Object> data = Map.of("port", 0);

            ValidationResult result = schema.validate(data);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors().get(0).type())
                .isEqualTo(ValidationError.ErrorType.OUT_OF_RANGE);
            assertThat(result.getErrors().get(0).message()).contains("below minimum");
        }

        @Test
        @DisplayName("should fail when value is above maximum")
        void shouldFailWhenAboveMaximum() {
            YmlSchema schema = YmlSchema.builder()
                .range("port", 1, 65535)
                .build();

            Map<String, Object> data = Map.of("port", 70000);

            ValidationResult result = schema.validate(data);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors().get(0).type())
                .isEqualTo(ValidationError.ErrorType.OUT_OF_RANGE);
            assertThat(result.getErrors().get(0).message()).contains("above maximum");
        }

        @Test
        @DisplayName("should pass at boundary values")
        void shouldPassAtBoundaryValues() {
            YmlSchema schema = YmlSchema.builder()
                .range("port", 1, 65535)
                .build();

            assertThat(schema.validate(Map.of("port", 1)).isValid()).isTrue();
            assertThat(schema.validate(Map.of("port", 65535)).isValid()).isTrue();
        }

        @Test
        @DisplayName("should skip range check for missing values")
        void shouldSkipRangeCheckForMissingValues() {
            YmlSchema schema = YmlSchema.builder()
                .range("optional", 1, 100)
                .build();

            ValidationResult result = schema.validate(Map.of());

            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("Pattern Constraints")
    class PatternConstraints {

        @Test
        @DisplayName("should pass when value matches pattern")
        void shouldPassWhenValueMatchesPattern() {
            YmlSchema schema = YmlSchema.builder()
                .pattern("email", "^[\\w.]+@[\\w.]+$")
                .build();

            Map<String, Object> data = Map.of("email", "user@example.com");

            ValidationResult result = schema.validate(data);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should fail when value does not match pattern")
        void shouldFailWhenValueDoesNotMatchPattern() {
            YmlSchema schema = YmlSchema.builder()
                .pattern("email", "^[\\w.]+@[\\w.]+$")
                .build();

            Map<String, Object> data = Map.of("email", "not-an-email");

            ValidationResult result = schema.validate(data);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors().get(0).type())
                .isEqualTo(ValidationError.ErrorType.PATTERN_MISMATCH);
        }

        @Test
        @DisplayName("should skip pattern check for missing values")
        void shouldSkipPatternCheckForMissingValues() {
            YmlSchema schema = YmlSchema.builder()
                .pattern("optional", "^\\d+$")
                .build();

            ValidationResult result = schema.validate(Map.of());

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should convert non-string values to string for pattern matching")
        void shouldConvertNonStringValuesToStringForPatternMatching() {
            YmlSchema schema = YmlSchema.builder()
                .pattern("port", "^\\d{4}$")
                .build();

            Map<String, Object> data = Map.of("port", 8080);

            ValidationResult result = schema.validate(data);

            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("Nested Schema")
    class NestedSchemaTests {

        @Test
        @DisplayName("should validate nested sub-document")
        void shouldValidateNestedSubDocument() {
            YmlSchema dbSchema = YmlSchema.builder()
                .required("url")
                .type("url", String.class)
                .build();

            YmlSchema schema = YmlSchema.builder()
                .nested("database", dbSchema)
                .build();

            Map<String, Object> db = Map.of("url", "jdbc:mysql://localhost/db");
            Map<String, Object> data = Map.of("database", db);

            ValidationResult result = schema.validate(data);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should report errors in nested sub-document")
        void shouldReportErrorsInNestedSubDocument() {
            YmlSchema dbSchema = YmlSchema.builder()
                .required("url", "driver")
                .build();

            YmlSchema schema = YmlSchema.builder()
                .nested("database", dbSchema)
                .build();

            Map<String, Object> db = Map.of("url", "jdbc:mysql://localhost/db");
            Map<String, Object> data = Map.of("database", db);

            ValidationResult result = schema.validate(data);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).path()).isEqualTo("database.driver");
        }

        @Test
        @DisplayName("should report type mismatch when nested path is not a map")
        void shouldReportTypeMismatchWhenNotMap() {
            YmlSchema nestedSchema = YmlSchema.builder()
                .required("key")
                .build();

            YmlSchema schema = YmlSchema.builder()
                .nested("database", nestedSchema)
                .build();

            Map<String, Object> data = Map.of("database", "not-a-map");

            ValidationResult result = schema.validate(data);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors().get(0).type())
                .isEqualTo(ValidationError.ErrorType.TYPE_MISMATCH);
        }

        @Test
        @DisplayName("should skip nested validation when path is absent")
        void shouldSkipNestedValidationWhenAbsent() {
            YmlSchema nestedSchema = YmlSchema.builder()
                .required("key")
                .build();

            YmlSchema schema = YmlSchema.builder()
                .nested("missing", nestedSchema)
                .build();

            ValidationResult result = schema.validate(Map.of());

            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("Custom Rules")
    class CustomRules {

        @Test
        @DisplayName("should pass when custom predicate returns true")
        void shouldPassWhenPredicateReturnsTrue() {
            YmlSchema schema = YmlSchema.builder()
                .rule("name", v -> ((String) v).length() <= 50, "Name too long")
                .build();

            Map<String, Object> data = Map.of("name", "short");

            ValidationResult result = schema.validate(data);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should fail when custom predicate returns false")
        void shouldFailWhenPredicateReturnsFalse() {
            YmlSchema schema = YmlSchema.builder()
                .rule("name", v -> ((String) v).length() <= 5, "Name too long")
                .build();

            Map<String, Object> data = Map.of("name", "this-is-too-long");

            ValidationResult result = schema.validate(data);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors().get(0).type())
                .isEqualTo(ValidationError.ErrorType.CUSTOM_RULE_FAILED);
            assertThat(result.getErrors().get(0).message()).isEqualTo("Name too long");
        }

        @Test
        @DisplayName("should handle multiple custom rules on same path")
        void shouldHandleMultipleCustomRulesOnSamePath() {
            YmlSchema schema = YmlSchema.builder()
                .rule("port", v -> (Integer) v > 0, "Port must be positive")
                .rule("port", v -> (Integer) v < 65536, "Port must be < 65536")
                .build();

            // Both pass
            assertThat(schema.validate(Map.of("port", 8080)).isValid()).isTrue();

            // Second rule fails
            ValidationResult result = schema.validate(Map.of("port", 70000));
            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("should skip custom rule for missing values")
        void shouldSkipCustomRuleForMissingValues() {
            YmlSchema schema = YmlSchema.builder()
                .rule("optional", v -> false, "Always fails")
                .build();

            ValidationResult result = schema.validate(Map.of());

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should handle ClassCastException in custom rule")
        void shouldHandleClassCastExceptionInCustomRule() {
            YmlSchema schema = YmlSchema.builder()
                .rule("value", v -> ((String) v).length() > 0, "Must not be empty")
                .build();

            // value is an Integer, not a String - should produce a ClassCastException error
            Map<String, Object> data = Map.of("value", 42);

            ValidationResult result = schema.validate(data);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors().get(0).type())
                .isEqualTo(ValidationError.ErrorType.CUSTOM_RULE_FAILED);
        }
    }

    @Nested
    @DisplayName("YmlDocument Validation")
    class DocumentValidation {

        @Test
        @DisplayName("should validate a YmlDocument")
        void shouldValidateYmlDocument() {
            YmlSchema schema = YmlSchema.builder()
                .required("name")
                .build();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("name", "app");
            YmlDocument doc = YmlDocument.of(data);

            ValidationResult result = schema.validate(doc);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should fail for null document")
        void shouldFailForNullDocument() {
            YmlSchema schema = YmlSchema.builder().build();

            ValidationResult result = schema.validate((YmlDocument) null);

            assertThat(result.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("Combined Constraints")
    class CombinedConstraints {

        @Test
        @DisplayName("should combine multiple constraint types")
        void shouldCombineMultipleConstraintTypes() {
            YmlSchema schema = YmlSchema.builder()
                .required("name", "port")
                .type("name", String.class)
                .type("port", Integer.class)
                .range("port", 1, 65535)
                .pattern("name", "^[a-z]+$")
                .build();

            Map<String, Object> data = Map.of("name", "myapp", "port", 8080);

            ValidationResult result = schema.validate(data);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should collect all errors from multiple constraints")
        void shouldCollectAllErrors() {
            YmlSchema schema = YmlSchema.builder()
                .required("name", "port")
                .type("port", Integer.class)
                .range("port", 1, 65535)
                .build();

            // name missing, port is wrong type
            Map<String, Object> data = Map.of("port", "invalid");

            ValidationResult result = schema.validate(data);

            assertThat(result.isValid()).isFalse();
            // At least 2 errors: missing 'name' + type mismatch on 'port'
            assertThat(result.getErrors().size()).isGreaterThanOrEqualTo(2);
        }
    }
}
