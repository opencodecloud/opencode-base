package cloud.opencode.base.xml.validate;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ValidationResultTest Tests
 * ValidationResultTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("ValidationResult Tests")
class ValidationResultTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("valid should create valid result")
        void validShouldCreateValidResult() {
            ValidationResult result = ValidationResult.valid();

            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
            assertThat(result.getWarnings()).isEmpty();
        }

        @Test
        @DisplayName("invalid with error should create invalid result")
        void invalidWithErrorShouldCreateInvalidResult() {
            ValidationResult.ValidationError error =
                new ValidationResult.ValidationError("Error", 1, 1, ValidationResult.ValidationError.Severity.ERROR);

            ValidationResult result = ValidationResult.invalid(error);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
        }

        @Test
        @DisplayName("invalid with message should create invalid result")
        void invalidWithMessageShouldCreateInvalidResult() {
            ValidationResult result = ValidationResult.invalid("Error message");

            assertThat(result.isValid()).isFalse();
            assertThat(result.hasErrors()).isTrue();
        }
    }

    @Nested
    @DisplayName("Validity Tests")
    class ValidityTests {

        @Test
        @DisplayName("isValid should return true when no errors")
        void isValidShouldReturnTrueWhenNoErrors() {
            ValidationResult result = ValidationResult.valid();

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("isValid should return false when has errors")
        void isValidShouldReturnFalseWhenHasErrors() {
            ValidationResult result = ValidationResult.invalid("Error");

            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("hasErrors should return true when has errors")
        void hasErrorsShouldReturnTrueWhenHasErrors() {
            ValidationResult result = ValidationResult.invalid("Error");

            assertThat(result.hasErrors()).isTrue();
        }

        @Test
        @DisplayName("hasWarnings should return true when has warnings")
        void hasWarningsShouldReturnTrueWhenHasWarnings() {
            ValidationResult result = new ValidationResult();
            result.addWarning("Warning", 1, 1);

            assertThat(result.hasWarnings()).isTrue();
        }
    }

    @Nested
    @DisplayName("Error Count Tests")
    class ErrorCountTests {

        @Test
        @DisplayName("getErrorCount should return error count")
        void getErrorCountShouldReturnErrorCount() {
            ValidationResult result = new ValidationResult();
            result.addError("Error 1", 1, 1);
            result.addError("Error 2", 2, 1);

            assertThat(result.getErrorCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("getWarningCount should return warning count")
        void getWarningCountShouldReturnWarningCount() {
            ValidationResult result = new ValidationResult();
            result.addWarning("Warning 1", 1, 1);
            result.addWarning("Warning 2", 2, 1);
            result.addWarning("Warning 3", 3, 1);

            assertThat(result.getWarningCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("ValidationError Record Tests")
    class ValidationErrorRecordTests {

        @Test
        @DisplayName("ValidationError should store all fields")
        void validationErrorShouldStoreAllFields() {
            ValidationResult.ValidationError error = new ValidationResult.ValidationError(
                "Test message", 10, 5, ValidationResult.ValidationError.Severity.ERROR
            );

            assertThat(error.message()).isEqualTo("Test message");
            assertThat(error.line()).isEqualTo(10);
            assertThat(error.column()).isEqualTo(5);
            assertThat(error.severity()).isEqualTo(ValidationResult.ValidationError.Severity.ERROR);
        }

        @Test
        @DisplayName("ValidationError getters should work")
        void validationErrorGettersShouldWork() {
            ValidationResult.ValidationError error = new ValidationResult.ValidationError(
                "Message", 10, 5, ValidationResult.ValidationError.Severity.WARNING
            );

            assertThat(error.getMessage()).isEqualTo("Message");
            assertThat(error.getLine()).isEqualTo(10);
            assertThat(error.getColumn()).isEqualTo(5);
            assertThat(error.getSeverity()).isEqualTo(ValidationResult.ValidationError.Severity.WARNING);
        }

        @Test
        @DisplayName("ValidationError toString should format correctly")
        void validationErrorToStringShouldFormatCorrectly() {
            ValidationResult.ValidationError error = new ValidationResult.ValidationError(
                "Test error", 10, 5, ValidationResult.ValidationError.Severity.ERROR
            );

            String str = error.toString();

            assertThat(str).contains("ERROR");
            assertThat(str).contains("10");
            assertThat(str).contains("Test error");
        }
    }

    @Nested
    @DisplayName("Severity Enum Tests")
    class SeverityEnumTests {

        @Test
        @DisplayName("Severity should have all expected values")
        void severityShouldHaveAllExpectedValues() {
            ValidationResult.ValidationError.Severity[] severities =
                ValidationResult.ValidationError.Severity.values();

            assertThat(severities).contains(
                ValidationResult.ValidationError.Severity.WARNING,
                ValidationResult.ValidationError.Severity.ERROR,
                ValidationResult.ValidationError.Severity.FATAL
            );
        }
    }

    @Nested
    @DisplayName("Message Tests")
    class MessageTests {

        @Test
        @DisplayName("getFirstErrorMessage should return first error message")
        void getFirstErrorMessageShouldReturnFirstErrorMessage() {
            ValidationResult result = new ValidationResult();
            result.addError("First error", 1, 1);
            result.addError("Second error", 2, 1);

            assertThat(result.getFirstErrorMessage()).isEqualTo("First error");
        }

        @Test
        @DisplayName("getFirstErrorMessage should return null when no errors")
        void getFirstErrorMessageShouldReturnNullWhenNoErrors() {
            ValidationResult result = ValidationResult.valid();

            assertThat(result.getFirstErrorMessage()).isNull();
        }

        @Test
        @DisplayName("getErrorMessages should return all error messages")
        void getErrorMessagesShouldReturnAllErrorMessages() {
            ValidationResult result = new ValidationResult();
            result.addError("Error 1", 1, 1);
            result.addError("Error 2", 2, 1);

            assertThat(result.getErrorMessages()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Merge Tests")
    class MergeTests {

        @Test
        @DisplayName("merge should combine results")
        void mergeShouldCombineResults() {
            ValidationResult result1 = new ValidationResult();
            result1.addError("Error 1", 1, 1);

            ValidationResult result2 = new ValidationResult();
            result2.addError("Error 2", 2, 1);
            result2.addWarning("Warning 1", 3, 1);

            result1.merge(result2);

            assertThat(result1.getErrorCount()).isEqualTo(2);
            assertThat(result1.getWarningCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString should format correctly")
        void toStringShouldFormatCorrectly() {
            ValidationResult result = new ValidationResult();
            result.addError("Error", 1, 1);

            String str = result.toString();

            assertThat(str).contains("ValidationResult");
            assertThat(str).contains("valid=false");
            assertThat(str).contains("errors=1");
        }
    }
}
