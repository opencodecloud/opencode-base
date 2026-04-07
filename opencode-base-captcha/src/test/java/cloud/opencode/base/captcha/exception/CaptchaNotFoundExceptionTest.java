package cloud.opencode.base.captcha.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * CaptchaNotFoundException Test - Unit tests for CAPTCHA not found exception
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
class CaptchaNotFoundExceptionTest {

    @Nested
    @DisplayName("CaptchaId Only Constructor Tests")
    class CaptchaIdOnlyConstructorTests {

        @Test
        @DisplayName("should create exception with captcha ID")
        void shouldCreateExceptionWithCaptchaId() {
            CaptchaNotFoundException ex = new CaptchaNotFoundException("captcha-123");

            assertThat(ex.getCaptchaId()).isEqualTo("captcha-123");
            assertThat(ex.getRawMessage()).isEqualTo("CAPTCHA not found: captcha-123");
        }

        @Test
        @DisplayName("should include 'CAPTCHA not found' prefix in message")
        void shouldIncludePrefixInMessage() {
            CaptchaNotFoundException ex = new CaptchaNotFoundException("xyz");

            assertThat(ex.getRawMessage()).startsWith("CAPTCHA not found: ");
            assertThat(ex.getRawMessage()).endsWith("xyz");
        }

        @Test
        @DisplayName("should create exception with UUID captcha ID")
        void shouldCreateExceptionWithUuidCaptchaId() {
            String uuid = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
            CaptchaNotFoundException ex = new CaptchaNotFoundException(uuid);

            assertThat(ex.getCaptchaId()).isEqualTo(uuid);
            assertThat(ex.getRawMessage()).isEqualTo("CAPTCHA not found: " + uuid);
        }

        @Test
        @DisplayName("should create exception with null captcha ID")
        void shouldCreateExceptionWithNullCaptchaId() {
            CaptchaNotFoundException ex = new CaptchaNotFoundException((String) null);

            assertThat(ex.getCaptchaId()).isNull();
            assertThat(ex.getRawMessage()).isEqualTo("CAPTCHA not found: null");
        }

        @Test
        @DisplayName("should create exception with empty captcha ID")
        void shouldCreateExceptionWithEmptyCaptchaId() {
            CaptchaNotFoundException ex = new CaptchaNotFoundException("");

            assertThat(ex.getCaptchaId()).isEmpty();
            assertThat(ex.getRawMessage()).isEqualTo("CAPTCHA not found: ");
        }

        @Test
        @DisplayName("should create exception with whitespace captcha ID")
        void shouldCreateExceptionWithWhitespaceCaptchaId() {
            CaptchaNotFoundException ex = new CaptchaNotFoundException("   ");

            assertThat(ex.getCaptchaId()).isEqualTo("   ");
            assertThat(ex.getRawMessage()).isEqualTo("CAPTCHA not found:    ");
        }

        @Test
        @DisplayName("should have no cause")
        void shouldHaveNoCause() {
            CaptchaNotFoundException ex = new CaptchaNotFoundException("captcha-123");

            assertThat(ex.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("Message and CaptchaId Constructor Tests")
    class MessageAndCaptchaIdConstructorTests {

        @Test
        @DisplayName("should create exception with custom message and captcha ID")
        void shouldCreateExceptionWithCustomMessageAndCaptchaId() {
            CaptchaNotFoundException ex = new CaptchaNotFoundException(
                "Captcha was deleted", "captcha-456");

            assertThat(ex.getCaptchaId()).isEqualTo("captcha-456");
            assertThat(ex.getRawMessage()).isEqualTo("Captcha was deleted");
        }

        @Test
        @DisplayName("should allow custom message without captcha ID in message")
        void shouldAllowCustomMessageWithoutCaptchaIdInMessage() {
            CaptchaNotFoundException ex = new CaptchaNotFoundException(
                "Storage lookup failed", "storage-789");

            assertThat(ex.getCaptchaId()).isEqualTo("storage-789");
            assertThat(ex.getRawMessage()).isEqualTo("Storage lookup failed");
            assertThat(ex.getRawMessage()).doesNotContain("storage-789");
        }

        @Test
        @DisplayName("should create exception with null message and valid captcha ID")
        void shouldCreateExceptionWithNullMessageAndValidCaptchaId() {
            CaptchaNotFoundException ex = new CaptchaNotFoundException(null, "captcha-111");

            assertThat(ex.getCaptchaId()).isEqualTo("captcha-111");
            assertThat(ex.getRawMessage()).isNull();
        }

        @Test
        @DisplayName("should create exception with empty message")
        void shouldCreateExceptionWithEmptyMessage() {
            CaptchaNotFoundException ex = new CaptchaNotFoundException("", "captcha-222");

            assertThat(ex.getCaptchaId()).isEqualTo("captcha-222");
            assertThat(ex.getRawMessage()).isEmpty();
        }

        @Test
        @DisplayName("should create exception with both null values")
        void shouldCreateExceptionWithBothNullValues() {
            CaptchaNotFoundException ex = new CaptchaNotFoundException(null, null);

            assertThat(ex.getCaptchaId()).isNull();
            assertThat(ex.getRawMessage()).isNull();
        }

        @Test
        @DisplayName("should have no cause with custom message constructor")
        void shouldHaveNoCauseWithCustomMessageConstructor() {
            CaptchaNotFoundException ex = new CaptchaNotFoundException("msg", "id");

            assertThat(ex.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("getCaptchaId Method Tests")
    class GetCaptchaIdMethodTests {

        @Test
        @DisplayName("should return exact captcha ID from single-arg constructor")
        void shouldReturnExactCaptchaIdFromSingleArgConstructor() {
            String captchaId = "test-captcha-id-99999";
            CaptchaNotFoundException ex = new CaptchaNotFoundException(captchaId);

            assertThat(ex.getCaptchaId()).isEqualTo(captchaId);
            assertThat(ex.getCaptchaId()).isSameAs(captchaId);
        }

        @Test
        @DisplayName("should return exact captcha ID from two-arg constructor")
        void shouldReturnExactCaptchaIdFromTwoArgConstructor() {
            String captchaId = "two-arg-captcha-id";
            CaptchaNotFoundException ex = new CaptchaNotFoundException("msg", captchaId);

            assertThat(ex.getCaptchaId()).isEqualTo(captchaId);
            assertThat(ex.getCaptchaId()).isSameAs(captchaId);
        }

        @Test
        @DisplayName("should return captcha ID with special characters")
        void shouldReturnCaptchaIdWithSpecialCharacters() {
            String captchaId = "captcha/with/slashes/and-dashes_and_underscores";
            CaptchaNotFoundException ex = new CaptchaNotFoundException(captchaId);

            assertThat(ex.getCaptchaId()).isEqualTo(captchaId);
        }

        @Test
        @DisplayName("should return long captcha ID")
        void shouldReturnLongCaptchaId() {
            String captchaId = "a".repeat(1000);
            CaptchaNotFoundException ex = new CaptchaNotFoundException(captchaId);

            assertThat(ex.getCaptchaId()).isEqualTo(captchaId);
            assertThat(ex.getCaptchaId()).hasSize(1000);
        }

        @Test
        @DisplayName("should return captcha ID with unicode characters")
        void shouldReturnCaptchaIdWithUnicodeCharacters() {
            String captchaId = "captcha-\u4e2d\u6587-\u00e9\u00e8";
            CaptchaNotFoundException ex = new CaptchaNotFoundException(captchaId);

            assertThat(ex.getCaptchaId()).isEqualTo(captchaId);
        }

        @Test
        @DisplayName("should return consistent captcha ID on multiple calls")
        void shouldReturnConsistentCaptchaIdOnMultipleCalls() {
            CaptchaNotFoundException ex = new CaptchaNotFoundException("stable-id");

            assertThat(ex.getCaptchaId()).isEqualTo(ex.getCaptchaId());
            assertThat(ex.getCaptchaId()).isSameAs(ex.getCaptchaId());
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("should extend OpenException")
        void shouldExtendOpenException() {
            CaptchaNotFoundException ex = new CaptchaNotFoundException("id");

            assertThat(ex).isInstanceOf(OpenException.class);
        }

        @Test
        @DisplayName("should extend CaptchaException")
        void shouldExtendCaptchaException() {
            CaptchaNotFoundException ex = new CaptchaNotFoundException("id");

            assertThat(ex).isInstanceOf(CaptchaException.class);
        }

        @Test
        @DisplayName("should extend RuntimeException")
        void shouldExtendRuntimeException() {
            CaptchaNotFoundException ex = new CaptchaNotFoundException("id");

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should extend Exception")
        void shouldExtendException() {
            CaptchaNotFoundException ex = new CaptchaNotFoundException("id");

            assertThat(ex).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("should extend Throwable")
        void shouldExtendThrowable() {
            CaptchaNotFoundException ex = new CaptchaNotFoundException("id");

            assertThat(ex).isInstanceOf(Throwable.class);
        }

        @Test
        @DisplayName("should be catchable as CaptchaException")
        void shouldBeCatchableAsCaptchaException() {
            assertThatCode(() -> {
                try {
                    throw new CaptchaNotFoundException("test-id");
                } catch (CaptchaException e) {
                    assertThat(e).isInstanceOf(CaptchaNotFoundException.class);
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should be catchable as RuntimeException")
        void shouldBeCatchableAsRuntimeException() {
            assertThatCode(() -> {
                try {
                    throw new CaptchaNotFoundException("test-id");
                } catch (RuntimeException e) {
                    assertThat(e).isInstanceOf(CaptchaNotFoundException.class);
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should preserve captcha ID when caught as CaptchaException")
        void shouldPreserveCaptchaIdWhenCaughtAsCaptchaException() {
            String captchaId = "preserve-me-123";

            assertThatCode(() -> {
                try {
                    throw new CaptchaNotFoundException(captchaId);
                } catch (CaptchaException e) {
                    CaptchaNotFoundException notFoundEx = (CaptchaNotFoundException) e;
                    assertThat(notFoundEx.getCaptchaId()).isEqualTo(captchaId);
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should be throwable with assertThatThrownBy")
        void shouldBeThrowableWithAssertThatThrownBy() {
            assertThatThrownBy(() -> {
                throw new CaptchaNotFoundException("thrown-id");
            }).isInstanceOf(CaptchaNotFoundException.class)
              .isInstanceOf(CaptchaException.class)
              .hasMessageContaining("CAPTCHA not found")
              .hasMessageContaining("thrown-id");
        }
    }

    @Nested
    @DisplayName("Message Format Tests")
    class MessageFormatTests {

        @Test
        @DisplayName("should format default message as 'CAPTCHA not found: ' plus ID")
        void shouldFormatDefaultMessage() {
            CaptchaNotFoundException ex = new CaptchaNotFoundException("my-id");

            assertThat(ex.getRawMessage()).isEqualTo("CAPTCHA not found: my-id");
        }

        @Test
        @DisplayName("should use custom message verbatim with two-arg constructor")
        void shouldUseCustomMessageVerbatim() {
            CaptchaNotFoundException ex = new CaptchaNotFoundException(
                "Custom not-found message", "id-99");

            assertThat(ex.getRawMessage()).isEqualTo("Custom not-found message");
        }

        @Test
        @DisplayName("default constructor message differs from custom constructor message")
        void defaultConstructorMessageDiffersFromCustom() {
            CaptchaNotFoundException defaultEx = new CaptchaNotFoundException("captcha-1");
            CaptchaNotFoundException customEx = new CaptchaNotFoundException(
                "Different message", "captcha-1");

            assertThat(defaultEx.getRawMessage()).isNotEqualTo(customEx.getRawMessage());
            assertThat(defaultEx.getCaptchaId()).isEqualTo(customEx.getCaptchaId());
        }
    }
}
