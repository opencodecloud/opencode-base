package cloud.opencode.base.captcha.validator;

import cloud.opencode.base.captcha.store.CaptchaStore;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * CaptchaValidator Test - Unit tests for the CaptchaValidator interface
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
class CaptchaValidatorTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("simple() should create a SimpleCaptchaValidator")
        void simpleShouldCreateSimpleCaptchaValidator() {
            CaptchaStore store = CaptchaStore.memory();
            CaptchaValidator validator = CaptchaValidator.simple(store);

            assertThat(validator).isNotNull();
            assertThat(validator).isInstanceOf(SimpleCaptchaValidator.class);
        }

        @Test
        @DisplayName("timeBased() should create a TimeBasedCaptchaValidator")
        void timeBasedShouldCreateTimeBasedCaptchaValidator() {
            CaptchaStore store = CaptchaStore.memory();
            CaptchaValidator validator = CaptchaValidator.timeBased(store);

            assertThat(validator).isNotNull();
            assertThat(validator).isInstanceOf(TimeBasedCaptchaValidator.class);
        }

        @Test
        @DisplayName("simple() should return different instances each call")
        void simpleShouldReturnDifferentInstances() {
            CaptchaStore store = CaptchaStore.memory();
            CaptchaValidator v1 = CaptchaValidator.simple(store);
            CaptchaValidator v2 = CaptchaValidator.simple(store);

            assertThat(v1).isNotSameAs(v2);
        }

        @Test
        @DisplayName("timeBased() should return different instances each call")
        void timeBasedShouldReturnDifferentInstances() {
            CaptchaStore store = CaptchaStore.memory();
            CaptchaValidator v1 = CaptchaValidator.timeBased(store);
            CaptchaValidator v2 = CaptchaValidator.timeBased(store);

            assertThat(v1).isNotSameAs(v2);
        }
    }

    @Nested
    @DisplayName("Interface Contract Tests")
    class InterfaceContractTests {

        @Test
        @DisplayName("simple validator should implement validate with two args")
        void simpleValidatorShouldImplementValidateWithTwoArgs() {
            CaptchaStore store = CaptchaStore.memory();
            CaptchaValidator validator = CaptchaValidator.simple(store);

            // Should not throw - method exists
            var result = validator.validate("nonexistent", "answer");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("simple validator should implement validate with three args")
        void simpleValidatorShouldImplementValidateWithThreeArgs() {
            CaptchaStore store = CaptchaStore.memory();
            CaptchaValidator validator = CaptchaValidator.simple(store);

            // Should not throw - method exists
            var result = validator.validate("nonexistent", "answer", true);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("timeBased validator should implement validate with two args")
        void timeBasedValidatorShouldImplementValidateWithTwoArgs() {
            CaptchaStore store = CaptchaStore.memory();
            CaptchaValidator validator = CaptchaValidator.timeBased(store);

            var result = validator.validate("nonexistent", "answer");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("timeBased validator should implement validate with three args")
        void timeBasedValidatorShouldImplementValidateWithThreeArgs() {
            CaptchaStore store = CaptchaStore.memory();
            CaptchaValidator validator = CaptchaValidator.timeBased(store);

            var result = validator.validate("nonexistent", "answer", false);
            assertThat(result).isNotNull();
        }
    }
}
