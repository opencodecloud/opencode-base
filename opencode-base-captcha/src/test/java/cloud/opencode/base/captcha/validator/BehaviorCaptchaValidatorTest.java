package cloud.opencode.base.captcha.validator;

import cloud.opencode.base.captcha.ValidationResult;
import cloud.opencode.base.captcha.security.BehaviorAnalyzer;
import cloud.opencode.base.captcha.store.CaptchaStore;
import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * BehaviorCaptchaValidator Test - Unit tests for behavior-based CAPTCHA validation
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
class BehaviorCaptchaValidatorTest {

    private CaptchaStore store;
    private BehaviorAnalyzer analyzer;
    private BehaviorCaptchaValidator validator;

    @BeforeEach
    void setUp() {
        store = CaptchaStore.memory();
        analyzer = new BehaviorAnalyzer();
        validator = new BehaviorCaptchaValidator(store, analyzer);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should create with store only")
        void shouldCreateWithStoreOnly() {
            BehaviorCaptchaValidator v = new BehaviorCaptchaValidator(store);

            assertThat(v).isNotNull();
            assertThat(v.getAnalyzer()).isNotNull();
        }

        @Test
        @DisplayName("should create with store and custom analyzer")
        void shouldCreateWithStoreAndCustomAnalyzer() {
            BehaviorAnalyzer customAnalyzer = new BehaviorAnalyzer();
            BehaviorCaptchaValidator v = new BehaviorCaptchaValidator(store, customAnalyzer);

            assertThat(v.getAnalyzer()).isSameAs(customAnalyzer);
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
            ValidationResult result = validator.validate("   ", "answer");

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
    }

    @Nested
    @DisplayName("Suspicious Fast Response Tests")
    class SuspiciousFastResponseTests {

        @Test
        @DisplayName("should detect suspiciously fast response")
        void shouldDetectSuspiciouslyFastResponse() {
            store.store("fast-1", "answer", Duration.ofMinutes(5));
            validator.recordCreation("fast-1", "client-1");

            // Validate immediately (< 500ms)
            ValidationResult result = validator.validate("fast-1", "answer");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.SUSPICIOUS_BEHAVIOR);
        }

        @Test
        @DisplayName("should remove captcha from store on fast response")
        void shouldRemoveCaptchaFromStoreOnFastResponse() {
            store.store("fast-2", "answer", Duration.ofMinutes(5));
            validator.recordCreation("fast-2", "client-1");

            validator.validate("fast-2", "answer");

            assertThat(store.exists("fast-2")).isFalse();
        }
    }

    @Nested
    @DisplayName("Normal Validation Tests")
    class NormalValidationTests {

        @Test
        @DisplayName("should validate successfully after sufficient delay")
        void shouldValidateSuccessfullyAfterSufficientDelay() throws InterruptedException {
            store.store("normal-1", "answer", Duration.ofMinutes(5));
            validator.recordCreation("normal-1", "client-1");

            Thread.sleep(600);

            ValidationResult result = validator.validate("normal-1", "answer");

            assertThat(result.success()).isTrue();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.SUCCESS);
        }

        @Test
        @DisplayName("should return mismatch for wrong answer after sufficient delay")
        void shouldReturnMismatchForWrongAnswer() throws InterruptedException {
            store.store("wrong-1", "correct", Duration.ofMinutes(5));
            validator.recordCreation("wrong-1", "client-1");

            Thread.sleep(600);

            ValidationResult result = validator.validate("wrong-1", "wrong");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.MISMATCH);
        }
    }

    @Nested
    @DisplayName("Not Found Tests")
    class NotFoundTests {

        @Test
        @DisplayName("should return notFound for missing captcha")
        void shouldReturnNotFoundForMissingCaptcha() throws InterruptedException {
            validator.recordCreation("missing-1", "client-1");
            Thread.sleep(600);

            ValidationResult result = validator.validate("missing-1", "answer");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Validate With ClientId Tests")
    class ValidateWithClientIdTests {

        @Test
        @DisplayName("should validate with explicit clientId when creation record exists")
        void shouldValidateWithExplicitClientIdWhenRecordExists() throws InterruptedException {
            store.store("client-1", "answer", Duration.ofMinutes(5));
            validator.recordCreation("client-1", "original-client");

            Thread.sleep(600);

            ValidationResult result = validator.validate("client-1", "answer", "new-client");

            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("should validate with explicit clientId when no creation record exists")
        void shouldValidateWithExplicitClientIdWhenNoRecordExists() {
            store.store("no-record", "answer", Duration.ofMinutes(5));

            // Validate with explicit client ID - should create a synthetic record with 1s offset
            ValidationResult result = validator.validate("no-record", "answer", "client-1");

            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("should support case-sensitive validation with client ID")
        void shouldSupportCaseSensitiveValidationWithClientId() {
            store.store("cs-1", "AbCd", Duration.ofMinutes(5));

            ValidationResult result = validator.validate("cs-1", "abcd", "client-1", true);

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.MISMATCH);
        }
    }

    @Nested
    @DisplayName("Without Creation Record Tests")
    class WithoutCreationRecordTests {

        @Test
        @DisplayName("should validate normally without creation record")
        void shouldValidateNormallyWithoutCreationRecord() {
            store.store("no-record", "answer", Duration.ofMinutes(5));

            ValidationResult result = validator.validate("no-record", "answer");

            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("should return notFound when captcha missing and no creation record")
        void shouldReturnNotFoundWhenCaptchaMissingAndNoCreationRecord() {
            ValidationResult result = validator.validate("nonexistent", "answer");

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
        @DisplayName("should support explicit case-insensitive")
        void shouldSupportExplicitCaseInsensitive() {
            store.store("case-2", "Hello", Duration.ofMinutes(5));

            ValidationResult result = validator.validate("case-2", "HELLO", false);

            assertThat(result.success()).isTrue();
        }
    }

    @Nested
    @DisplayName("Analyzer Access Tests")
    class AnalyzerAccessTests {

        @Test
        @DisplayName("should provide access to the analyzer")
        void shouldProvideAccessToAnalyzer() {
            assertThat(validator.getAnalyzer()).isSameAs(analyzer);
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
        @DisplayName("should not throw when clearing with active records")
        void shouldNotThrowWhenClearingWithActiveRecords() {
            validator.recordCreation("id-1", "client-1");
            validator.recordCreation("id-2", "client-2");

            assertThatCode(() -> validator.clearOldRecords())
                .doesNotThrowAnyException();
        }
    }
}
