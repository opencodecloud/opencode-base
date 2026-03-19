package cloud.opencode.base.captcha.validator;

import cloud.opencode.base.captcha.ValidationResult;
import cloud.opencode.base.captcha.store.CaptchaStore;
import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * TimeBasedCaptchaValidator Test - Unit tests for time-based CAPTCHA validation
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
class TimeBasedCaptchaValidatorTest {

    private CaptchaStore store;
    private TimeBasedCaptchaValidator validator;

    @BeforeEach
    void setUp() {
        store = CaptchaStore.memory();
        validator = new TimeBasedCaptchaValidator(store);
    }

    @Nested
    @DisplayName("Suspicious Fast Response Tests")
    class SuspiciousFastResponseTests {

        @Test
        @DisplayName("should return suspiciousBehavior for immediate response")
        void shouldReturnSuspiciousBehaviorForImmediateResponse() {
            store.store("fast-1", "answer", Duration.ofMinutes(5));
            validator.recordCreation("fast-1");

            // Validate immediately (< 500ms)
            ValidationResult result = validator.validate("fast-1", "answer");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.SUSPICIOUS_BEHAVIOR);
        }

        @Test
        @DisplayName("should remove stored captcha on suspicious response")
        void shouldRemoveStoredCaptchaOnSuspiciousResponse() {
            store.store("fast-2", "answer", Duration.ofMinutes(5));
            validator.recordCreation("fast-2");

            validator.validate("fast-2", "answer");

            assertThat(store.exists("fast-2")).isFalse();
        }
    }

    @Nested
    @DisplayName("Normal Response Time Tests")
    class NormalResponseTimeTests {

        @Test
        @DisplayName("should validate successfully after sufficient delay")
        void shouldValidateSuccessfullyAfterSufficientDelay() throws InterruptedException {
            store.store("normal-1", "answer", Duration.ofMinutes(5));
            validator.recordCreation("normal-1");

            // Wait more than 500ms
            Thread.sleep(600);

            ValidationResult result = validator.validate("normal-1", "answer");

            assertThat(result.success()).isTrue();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.SUCCESS);
        }
    }

    @Nested
    @DisplayName("No Creation Record Tests")
    class NoCreationRecordTests {

        @Test
        @DisplayName("should validate normally without creation record")
        void shouldValidateNormallyWithoutCreationRecord() {
            store.store("norecord-1", "answer", Duration.ofMinutes(5));
            // No recordCreation call

            ValidationResult result = validator.validate("norecord-1", "answer");

            assertThat(result.success()).isTrue();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.SUCCESS);
        }

        @Test
        @DisplayName("should still check answer without creation record")
        void shouldStillCheckAnswerWithoutCreationRecord() {
            store.store("norecord-2", "correct", Duration.ofMinutes(5));

            ValidationResult result = validator.validate("norecord-2", "wrong");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.MISMATCH);
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
        @DisplayName("should return invalidInput when answer is null")
        void shouldReturnInvalidInputWhenAnswerIsNull() {
            ValidationResult result = validator.validate("id-1", null);

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("should return invalidInput when id is blank")
        void shouldReturnInvalidInputWhenIdIsBlank() {
            ValidationResult result = validator.validate("  ", "answer");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("should return invalidInput when answer is blank")
        void shouldReturnInvalidInputWhenAnswerIsBlank() {
            ValidationResult result = validator.validate("id-1", "  ");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.INVALID_INPUT);
        }
    }

    @Nested
    @DisplayName("Not Found Tests")
    class NotFoundTests {

        @Test
        @DisplayName("should return notFound when captcha does not exist")
        void shouldReturnNotFoundWhenCaptchaDoesNotExist() throws InterruptedException {
            validator.recordCreation("missing-1");
            Thread.sleep(600);

            ValidationResult result = validator.validate("missing-1", "answer");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Case Sensitivity Tests")
    class CaseSensitivityTests {

        @Test
        @DisplayName("should default to case-insensitive")
        void shouldDefaultToCaseInsensitive() {
            store.store("case-1", "AbCd", Duration.ofMinutes(5));

            ValidationResult result = validator.validate("case-1", "abcd");

            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("should support case-sensitive validation")
        void shouldSupportCaseSensitiveValidation() {
            store.store("case-2", "AbCd", Duration.ofMinutes(5));

            ValidationResult result = validator.validate("case-2", "abcd", true);

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.MISMATCH);
        }

        @Test
        @DisplayName("should pass case-sensitive when exact match")
        void shouldPassCaseSensitiveWhenExactMatch() {
            store.store("case-3", "AbCd", Duration.ofMinutes(5));

            ValidationResult result = validator.validate("case-3", "AbCd", true);

            assertThat(result.success()).isTrue();
        }
    }

    @Nested
    @DisplayName("Clear Old Records Tests")
    class ClearOldRecordsTests {

        @Test
        @DisplayName("should not throw when clearing with no records")
        void shouldNotThrowWhenClearingWithNoRecords() {
            assertThatCode(() -> validator.clearOldRecords())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should clear old records without affecting recent ones")
        void shouldClearOldRecordsWithoutAffectingRecentOnes() {
            store.store("recent-1", "answer", Duration.ofMinutes(5));
            validator.recordCreation("recent-1");

            validator.clearOldRecords();

            // Recent record should still exist (not older than 10 minutes)
            // Validate immediately will be suspicious, confirming record exists
            ValidationResult result = validator.validate("recent-1", "answer");
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.SUSPICIOUS_BEHAVIOR);
        }
    }
}
