package cloud.opencode.base.captcha.validator;

import cloud.opencode.base.captcha.ValidationResult;
import cloud.opencode.base.captcha.store.CaptchaStore;
import cloud.opencode.base.captcha.store.HashedCaptchaStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("HashedCaptchaValidator Tests")
class HashedCaptchaValidatorTest {

    private HashedCaptchaStore store;
    private HashedCaptchaValidator validator;

    @BeforeEach
    void setUp() {
        store = HashedCaptchaStore.wrap(CaptchaStore.memory());
        validator = new HashedCaptchaValidator(store);
    }

    @Nested
    @DisplayName("Construction Tests")
    class ConstructionTests {

        @Test
        @DisplayName("null store throws NullPointerException")
        void nullStoreThrows() {
            assertThatThrownBy(() -> new HashedCaptchaValidator(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("correct answer returns SUCCESS")
        void correctAnswerReturnsSuccess() {
            store.store("id1", "myAnswer", Duration.ofMinutes(5));
            ValidationResult result = validator.validate("id1", "myAnswer");
            assertThat(result.success()).isTrue();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.SUCCESS);
        }

        @Test
        @DisplayName("wrong answer returns MISMATCH")
        void wrongAnswerReturnsMismatch() {
            store.store("id2", "correctAnswer", Duration.ofMinutes(5));
            ValidationResult result = validator.validate("id2", "wrongAnswer");
            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.MISMATCH);
        }

        @Test
        @DisplayName("non-existent ID returns NOT_FOUND")
        void nonExistentIdReturnsNotFound() {
            ValidationResult result = validator.validate("nonexistent", "answer");
            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.NOT_FOUND);
        }

        @Test
        @DisplayName("case insensitive validation works")
        void caseInsensitiveValidation() {
            store.store("id3", "AbCd", Duration.ofMinutes(5));
            ValidationResult result = validator.validate("id3", "abcd");
            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("answer is consumed after validation")
        void answerConsumedAfterValidation() {
            store.store("id4", "answer", Duration.ofMinutes(5));
            ValidationResult first = validator.validate("id4", "answer");
            assertThat(first.success()).isTrue();

            ValidationResult second = validator.validate("id4", "answer");
            assertThat(second.success()).isFalse();
        }
    }

    @Nested
    @DisplayName("Input Validation Tests")
    class InputValidationTests {

        @Test
        @DisplayName("null id returns INVALID_INPUT")
        void nullIdReturnsInvalidInput() {
            ValidationResult result = validator.validate(null, "answer");
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("blank id returns INVALID_INPUT")
        void blankIdReturnsInvalidInput() {
            ValidationResult result = validator.validate("  ", "answer");
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("null answer returns INVALID_INPUT")
        void nullAnswerReturnsInvalidInput() {
            ValidationResult result = validator.validate("id", null);
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("blank answer returns INVALID_INPUT")
        void blankAnswerReturnsInvalidInput() {
            ValidationResult result = validator.validate("id", "");
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.INVALID_INPUT);
        }
    }

    @Nested
    @DisplayName("OpenCaptcha Integration Tests")
    class OpenCaptchaIntegrationTests {

        @Test
        @DisplayName("OpenCaptcha with HashedCaptchaStore auto-detects validator")
        void openCaptchaAutoDetectsHashedStore() {
            HashedCaptchaStore hashedStore = HashedCaptchaStore.wrap(CaptchaStore.memory());
            cloud.opencode.base.captcha.OpenCaptcha openCaptcha = cloud.opencode.base.captcha.OpenCaptcha.builder()
                .store(hashedStore)
                .build();

            cloud.opencode.base.captcha.Captcha captcha = openCaptcha.generate();
            ValidationResult result = openCaptcha.validate(captcha.id(), captcha.answer());
            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("OpenCaptcha with HashedCaptchaStore rejects wrong answer")
        void openCaptchaHashedStoreRejectsWrongAnswer() {
            HashedCaptchaStore hashedStore = HashedCaptchaStore.wrap(CaptchaStore.memory());
            cloud.opencode.base.captcha.OpenCaptcha openCaptcha = cloud.opencode.base.captcha.OpenCaptcha.builder()
                .store(hashedStore)
                .build();

            cloud.opencode.base.captcha.Captcha captcha = openCaptcha.generate();
            ValidationResult result = openCaptcha.validate(captcha.id(), "definitely_wrong");
            assertThat(result.success()).isFalse();
        }
    }
}
