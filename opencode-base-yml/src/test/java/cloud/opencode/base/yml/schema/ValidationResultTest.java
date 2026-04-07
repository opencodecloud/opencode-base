package cloud.opencode.base.yml.schema;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ValidationResult and ValidationError Tests
 * ValidationResult 和 ValidationError 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.3
 */
@DisplayName("ValidationResult and ValidationError Tests")
class ValidationResultTest {

    @Nested
    @DisplayName("ValidationResult Factory Methods")
    class FactoryMethods {

        @Test
        @DisplayName("success should create valid result with no errors")
        void successShouldCreateValidResult() {
            ValidationResult result = ValidationResult.success();

            assertThat(result.isValid()).isTrue();
            assertThat(result.valid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
            assertThat(result.errors()).isEmpty();
        }

        @Test
        @DisplayName("failure should create invalid result with errors")
        void failureShouldCreateInvalidResult() {
            List<ValidationError> errors = List.of(
                new ValidationError("path", "message", ValidationError.ErrorType.MISSING_REQUIRED)
            );

            ValidationResult result = ValidationResult.failure(errors);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
        }

        @Test
        @DisplayName("failure should throw on null errors")
        void failureShouldThrowOnNullErrors() {
            assertThatThrownBy(() -> ValidationResult.failure(null))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("failure should throw on empty errors")
        void failureShouldThrowOnEmptyErrors() {
            assertThatThrownBy(() -> ValidationResult.failure(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("ValidationResult Immutability")
    class Immutability {

        @Test
        @DisplayName("errors list should be immutable")
        void errorsListShouldBeImmutable() {
            List<ValidationError> errors = List.of(
                new ValidationError("path", "msg", ValidationError.ErrorType.TYPE_MISMATCH)
            );
            ValidationResult result = ValidationResult.failure(errors);

            assertThatThrownBy(() -> result.getErrors().add(
                new ValidationError("x", "y", ValidationError.ErrorType.MISSING_REQUIRED)
            )).isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("ValidationResult ToString")
    class ToStringTests {

        @Test
        @DisplayName("success toString should indicate valid")
        void successToStringShouldIndicateValid() {
            String str = ValidationResult.success().toString();

            assertThat(str).contains("valid=true");
        }

        @Test
        @DisplayName("failure toString should indicate invalid and errors")
        void failureToStringShouldIndicateInvalid() {
            ValidationResult result = ValidationResult.failure(List.of(
                new ValidationError("p", "m", ValidationError.ErrorType.MISSING_REQUIRED)
            ));

            String str = result.toString();

            assertThat(str).contains("valid=false");
            assertThat(str).contains("errors=");
        }
    }

    @Nested
    @DisplayName("ValidationError Record")
    class ValidationErrorTests {

        @Test
        @DisplayName("should store path, message, and type")
        void shouldStoreAllFields() {
            ValidationError error = new ValidationError(
                "server.port", "Required key missing", ValidationError.ErrorType.MISSING_REQUIRED
            );

            assertThat(error.path()).isEqualTo("server.port");
            assertThat(error.message()).isEqualTo("Required key missing");
            assertThat(error.type()).isEqualTo(ValidationError.ErrorType.MISSING_REQUIRED);
        }

        @Test
        @DisplayName("toString should contain type, path, and message")
        void toStringShouldContainAllInfo() {
            ValidationError error = new ValidationError(
                "db.url", "Expected String", ValidationError.ErrorType.TYPE_MISMATCH
            );

            String str = error.toString();

            assertThat(str).contains("TYPE_MISMATCH");
            assertThat(str).contains("db.url");
            assertThat(str).contains("Expected String");
        }

        @Test
        @DisplayName("should support equals and hashCode as a record")
        void shouldSupportEqualsAndHashCode() {
            ValidationError e1 = new ValidationError("p", "m", ValidationError.ErrorType.OUT_OF_RANGE);
            ValidationError e2 = new ValidationError("p", "m", ValidationError.ErrorType.OUT_OF_RANGE);
            ValidationError e3 = new ValidationError("q", "m", ValidationError.ErrorType.OUT_OF_RANGE);

            assertThat(e1).isEqualTo(e2);
            assertThat(e1).isNotEqualTo(e3);
            assertThat(e1.hashCode()).isEqualTo(e2.hashCode());
        }
    }

    @Nested
    @DisplayName("ErrorType Enum")
    class ErrorTypeTests {

        @Test
        @DisplayName("should have all expected values")
        void shouldHaveAllExpectedValues() {
            assertThat(ValidationError.ErrorType.values()).containsExactly(
                ValidationError.ErrorType.MISSING_REQUIRED,
                ValidationError.ErrorType.TYPE_MISMATCH,
                ValidationError.ErrorType.OUT_OF_RANGE,
                ValidationError.ErrorType.PATTERN_MISMATCH,
                ValidationError.ErrorType.CUSTOM_RULE_FAILED
            );
        }

        @Test
        @DisplayName("valueOf should return correct value")
        void valueOfShouldWork() {
            assertThat(ValidationError.ErrorType.valueOf("MISSING_REQUIRED"))
                .isEqualTo(ValidationError.ErrorType.MISSING_REQUIRED);
        }
    }
}
