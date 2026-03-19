package cloud.opencode.base.captcha.validator;

import cloud.opencode.base.captcha.ValidationResult;
import cloud.opencode.base.captcha.store.CaptchaStore;
import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * SimpleCaptchaValidator Test - Unit tests for basic CAPTCHA validation
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
class SimpleCaptchaValidatorTest {

    private CaptchaStore store;
    private SimpleCaptchaValidator validator;

    @BeforeEach
    void setUp() {
        store = CaptchaStore.memory();
        validator = new SimpleCaptchaValidator(store);
    }

    @Nested
    @DisplayName("Successful Validation Tests")
    class SuccessfulValidationTests {

        @Test
        @DisplayName("should validate correct answer case-insensitively by default")
        void shouldValidateCorrectAnswerCaseInsensitively() {
            store.store("id-1", "AbCd", Duration.ofMinutes(5));

            ValidationResult result = validator.validate("id-1", "abcd");

            assertThat(result.success()).isTrue();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.SUCCESS);
        }

        @Test
        @DisplayName("should validate exact match case-insensitively")
        void shouldValidateExactMatchCaseInsensitively() {
            store.store("id-2", "XyZw", Duration.ofMinutes(5));

            ValidationResult result = validator.validate("id-2", "XyZw");

            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("should validate uppercase answer against lowercase stored")
        void shouldValidateUppercaseAgainstLowercase() {
            store.store("id-3", "hello", Duration.ofMinutes(5));

            ValidationResult result = validator.validate("id-3", "HELLO");

            assertThat(result.success()).isTrue();
        }
    }

    @Nested
    @DisplayName("Case-Sensitive Validation Tests")
    class CaseSensitiveValidationTests {

        @Test
        @DisplayName("should pass when case matches and caseSensitive is true")
        void shouldPassWhenCaseMatchesAndCaseSensitive() {
            store.store("cs-1", "AbCd", Duration.ofMinutes(5));

            ValidationResult result = validator.validate("cs-1", "AbCd", true);

            assertThat(result.success()).isTrue();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.SUCCESS);
        }

        @Test
        @DisplayName("should fail when case does not match and caseSensitive is true")
        void shouldFailWhenCaseDoesNotMatchAndCaseSensitive() {
            store.store("cs-2", "AbCd", Duration.ofMinutes(5));

            ValidationResult result = validator.validate("cs-2", "abcd", true);

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.MISMATCH);
        }

        @Test
        @DisplayName("should pass when case does not match and caseSensitive is false")
        void shouldPassWhenCaseDoesNotMatchAndCaseInsensitive() {
            store.store("cs-3", "AbCd", Duration.ofMinutes(5));

            ValidationResult result = validator.validate("cs-3", "abcd", false);

            assertThat(result.success()).isTrue();
        }
    }

    @Nested
    @DisplayName("Invalid Input Tests")
    class InvalidInputTests {

        @Test
        @DisplayName("should return invalidInput when id is null")
        void shouldReturnInvalidInputWhenIdIsNull() {
            ValidationResult result = validator.validate(null, "answer");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("should return invalidInput when id is empty")
        void shouldReturnInvalidInputWhenIdIsEmpty() {
            ValidationResult result = validator.validate("", "answer");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("should return invalidInput when id is blank")
        void shouldReturnInvalidInputWhenIdIsBlank() {
            ValidationResult result = validator.validate("   ", "answer");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("should return invalidInput when answer is null")
        void shouldReturnInvalidInputWhenAnswerIsNull() {
            ValidationResult result = validator.validate("id-1", null);

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("should return invalidInput when answer is empty")
        void shouldReturnInvalidInputWhenAnswerIsEmpty() {
            ValidationResult result = validator.validate("id-1", "");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("should return invalidInput when answer is blank")
        void shouldReturnInvalidInputWhenAnswerIsBlank() {
            ValidationResult result = validator.validate("id-1", "   ");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.INVALID_INPUT);
        }
    }

    @Nested
    @DisplayName("Not Found Tests")
    class NotFoundTests {

        @Test
        @DisplayName("should return notFound when captcha does not exist")
        void shouldReturnNotFoundWhenCaptchaDoesNotExist() {
            ValidationResult result = validator.validate("nonexistent", "answer");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.NOT_FOUND);
        }

        @Test
        @DisplayName("should return notFound on second validation attempt (single use)")
        void shouldReturnNotFoundOnSecondAttempt() {
            store.store("single-use", "answer", Duration.ofMinutes(5));

            // First validation uses and removes
            validator.validate("single-use", "answer");

            // Second attempt should fail - captcha was consumed
            ValidationResult result = validator.validate("single-use", "answer");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Mismatch Tests")
    class MismatchTests {

        @Test
        @DisplayName("should return mismatch when answer is wrong")
        void shouldReturnMismatchWhenAnswerIsWrong() {
            store.store("mm-1", "correct", Duration.ofMinutes(5));

            ValidationResult result = validator.validate("mm-1", "wrong");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.MISMATCH);
        }

        @Test
        @DisplayName("should return mismatch for partial answer")
        void shouldReturnMismatchForPartialAnswer() {
            store.store("mm-2", "abcd", Duration.ofMinutes(5));

            ValidationResult result = validator.validate("mm-2", "abc");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.MISMATCH);
        }

        @Test
        @DisplayName("should consume captcha even on mismatch")
        void shouldConsumeCaptchaEvenOnMismatch() {
            store.store("mm-3", "correct", Duration.ofMinutes(5));

            validator.validate("mm-3", "wrong");

            // Captcha should be consumed after first attempt
            assertThat(store.exists("mm-3")).isFalse();
        }
    }

    @Nested
    @DisplayName("Default Case-Sensitivity Tests")
    class DefaultCaseSensitivityTests {

        @Test
        @DisplayName("two-arg validate should default to case-insensitive")
        void twoArgValidateShouldDefaultToCaseInsensitive() {
            store.store("default-1", "AbCd", Duration.ofMinutes(5));

            ValidationResult result = validator.validate("default-1", "ABCD");

            assertThat(result.success()).isTrue();
        }
    }
}
