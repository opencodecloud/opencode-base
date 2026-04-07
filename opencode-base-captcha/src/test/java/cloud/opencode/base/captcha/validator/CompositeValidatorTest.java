package cloud.opencode.base.captcha.validator;

import cloud.opencode.base.captcha.ValidationResult;
import cloud.opencode.base.captcha.store.CaptchaStore;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * CompositeValidator Test - Unit tests for composite CAPTCHA validation chaining
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
@DisplayName("CompositeValidator Tests")
class CompositeValidatorTest {

    @Nested
    @DisplayName("Construction Tests")
    class ConstructionTests {

        @Test
        @DisplayName("should create with single validator using of()")
        void shouldCreateWithSingleValidator() {
            CaptchaValidator stub = alwaysSuccess();
            CompositeValidator composite = CompositeValidator.of(stub);

            assertThat(composite.size()).isEqualTo(1);
            assertThat(composite.getValidators()).hasSize(1);
        }

        @Test
        @DisplayName("should create with multiple validators using of()")
        void shouldCreateWithMultipleValidators() {
            CompositeValidator composite = CompositeValidator.of(
                alwaysSuccess(), alwaysSuccess(), alwaysSuccess()
            );

            assertThat(composite.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("should reject null first validator in of()")
        void shouldRejectNullFirstValidator() {
            assertThatNullPointerException()
                .isThrownBy(() -> CompositeValidator.of(null))
                .withMessageContaining("first validator must not be null");
        }

        @Test
        @DisplayName("should reject null rest validator in of()")
        void shouldRejectNullInRestValidators() {
            assertThatNullPointerException()
                .isThrownBy(() -> CompositeValidator.of(alwaysSuccess(), (CaptchaValidator) null))
                .withMessageContaining("validator must not be null");
        }

        @Test
        @DisplayName("should create from list using ofList()")
        void shouldCreateFromList() {
            CompositeValidator composite = CompositeValidator.ofList(
                List.of(alwaysSuccess(), alwaysSuccess())
            );

            assertThat(composite.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("should reject null list in ofList()")
        void shouldRejectNullList() {
            assertThatNullPointerException()
                .isThrownBy(() -> CompositeValidator.ofList(null))
                .withMessageContaining("validators must not be null");
        }

        @Test
        @DisplayName("should reject empty list in ofList()")
        void shouldRejectEmptyList() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> CompositeValidator.ofList(List.of()))
                .withMessageContaining("validators must not be empty");
        }

        @Test
        @DisplayName("should return unmodifiable validators list")
        void shouldReturnUnmodifiableValidatorsList() {
            CompositeValidator composite = CompositeValidator.of(alwaysSuccess());

            assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> composite.getValidators().add(alwaysSuccess()));
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("should build with validators")
        void shouldBuildWithValidators() {
            CompositeValidator composite = CompositeValidator.builder()
                .addValidator(alwaysSuccess())
                .addValidator(alwaysSuccess())
                .build();

            assertThat(composite.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("should reject null validator in builder")
        void shouldRejectNullValidatorInBuilder() {
            assertThatNullPointerException()
                .isThrownBy(() -> CompositeValidator.builder().addValidator(null))
                .withMessageContaining("validator must not be null");
        }

        @Test
        @DisplayName("should reject building with no validators")
        void shouldRejectBuildingWithNoValidators() {
            assertThatIllegalStateException()
                .isThrownBy(() -> CompositeValidator.builder().build())
                .withMessageContaining("At least one validator must be added");
        }
    }

    @Nested
    @DisplayName("Validation Chain Tests")
    class ValidationChainTests {

        @Test
        @DisplayName("should return success when all validators succeed")
        void shouldReturnSuccessWhenAllSucceed() {
            CompositeValidator composite = CompositeValidator.of(
                alwaysSuccess(), alwaysSuccess(), alwaysSuccess()
            );

            ValidationResult result = composite.validate("id-1", "answer");

            assertThat(result.success()).isTrue();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.SUCCESS);
        }

        @Test
        @DisplayName("should return failure from first failing validator")
        void shouldReturnFailureFromFirstFailingValidator() {
            CompositeValidator composite = CompositeValidator.of(
                alwaysFailing(ValidationResult.rateLimited()),
                alwaysSuccess()
            );

            ValidationResult result = composite.validate("id-1", "answer");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.RATE_LIMITED);
        }

        @Test
        @DisplayName("should short-circuit on middle failure")
        void shouldShortCircuitOnMiddleFailure() {
            // Track whether the third validator is called
            boolean[] thirdCalled = {false};
            CaptchaValidator thirdValidator = new CaptchaValidator() {
                @Override
                public ValidationResult validate(String id, String answer) {
                    thirdCalled[0] = true;
                    return ValidationResult.ok();
                }

                @Override
                public ValidationResult validate(String id, String answer, boolean caseSensitive) {
                    thirdCalled[0] = true;
                    return ValidationResult.ok();
                }
            };

            CompositeValidator composite = CompositeValidator.of(
                alwaysSuccess(),
                alwaysFailing(ValidationResult.expired()),
                thirdValidator
            );

            ValidationResult result = composite.validate("id-1", "answer");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.EXPIRED);
            assertThat(thirdCalled[0]).isFalse();
        }

        @Test
        @DisplayName("should return last success when single validator succeeds")
        void shouldReturnSuccessWithSingleValidator() {
            CompositeValidator composite = CompositeValidator.of(alwaysSuccess());

            ValidationResult result = composite.validate("id-1", "answer");

            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("should return failure for single failing validator")
        void shouldReturnFailureForSingleFailingValidator() {
            CompositeValidator composite = CompositeValidator.of(
                alwaysFailing(ValidationResult.mismatch())
            );

            ValidationResult result = composite.validate("id-1", "wrong");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.MISMATCH);
        }
    }

    @Nested
    @DisplayName("Case-Sensitive Validation Tests")
    class CaseSensitiveValidationTests {

        @Test
        @DisplayName("should pass case sensitivity to all validators")
        void shouldPassCaseSensitivity() {
            boolean[] caseSensitiveReceived = {false};
            CaptchaValidator trackingValidator = new CaptchaValidator() {
                @Override
                public ValidationResult validate(String id, String answer) {
                    return ValidationResult.ok();
                }

                @Override
                public ValidationResult validate(String id, String answer, boolean caseSensitive) {
                    caseSensitiveReceived[0] = caseSensitive;
                    return ValidationResult.ok();
                }
            };

            CompositeValidator composite = CompositeValidator.of(trackingValidator);

            composite.validate("id-1", "answer", true);
            assertThat(caseSensitiveReceived[0]).isTrue();

            composite.validate("id-1", "answer", false);
            assertThat(caseSensitiveReceived[0]).isFalse();
        }

        @Test
        @DisplayName("should short-circuit case-sensitive validation on failure")
        void shouldShortCircuitCaseSensitiveValidation() {
            CompositeValidator composite = CompositeValidator.of(
                alwaysFailing(ValidationResult.notFound()),
                alwaysSuccess()
            );

            ValidationResult result = composite.validate("id-1", "answer", true);

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(ValidationResult.ResultCode.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Integration with Real Store Tests")
    class IntegrationTests {

        @Test
        @DisplayName("should work with real SimpleCaptchaValidator as primary")
        void shouldWorkWithRealSimpleValidator() {
            CaptchaStore store = CaptchaStore.memory();
            store.store("captcha-1", "XyZ9", Duration.ofMinutes(5));

            CompositeValidator composite = CompositeValidator.of(
                CaptchaValidator.simple(store)
            );

            ValidationResult result = composite.validate("captcha-1", "xyz9");
            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("should chain real validator with a pre-check")
        void shouldChainRealValidatorWithPreCheck() {
            CaptchaStore store = CaptchaStore.memory();
            store.store("captcha-2", "ABCD", Duration.ofMinutes(5));

            // Pre-check that always succeeds, then the real validator
            CompositeValidator composite = CompositeValidator.of(
                alwaysSuccess(),
                CaptchaValidator.simple(store)
            );

            // Note: the first validator (alwaysSuccess) does not consume the answer,
            // so the second validator (simple) can still do the actual check
            ValidationResult result = composite.validate("captcha-2", "abcd");
            assertThat(result.success()).isTrue();
        }
    }

    // ======================== Helper Methods ========================

    /**
     * Creates a CaptchaValidator stub that always returns success.
     */
    private static CaptchaValidator alwaysSuccess() {
        return new CaptchaValidator() {
            @Override
            public ValidationResult validate(String id, String answer) {
                return ValidationResult.ok();
            }

            @Override
            public ValidationResult validate(String id, String answer, boolean caseSensitive) {
                return ValidationResult.ok();
            }
        };
    }

    /**
     * Creates a CaptchaValidator stub that always returns the given failure result.
     */
    private static CaptchaValidator alwaysFailing(ValidationResult failResult) {
        return new CaptchaValidator() {
            @Override
            public ValidationResult validate(String id, String answer) {
                return failResult;
            }

            @Override
            public ValidationResult validate(String id, String answer, boolean caseSensitive) {
                return failResult;
            }
        };
    }
}
