package cloud.opencode.base.captcha.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * CaptchaExpiredException Test - Unit tests for CAPTCHA expired exception
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
class CaptchaExpiredExceptionTest {

    @Nested
    @DisplayName("CaptchaId Only Constructor Tests")
    class CaptchaIdOnlyConstructorTests {

        @Test
        @DisplayName("should create exception with captcha ID")
        void shouldCreateExceptionWithCaptchaId() {
            CaptchaExpiredException ex = new CaptchaExpiredException("captcha-123");

            assertThat(ex.getCaptchaId()).isEqualTo("captcha-123");
            assertThat(ex.getRawMessage()).isEqualTo("CAPTCHA has expired: captcha-123");
        }

        @Test
        @DisplayName("should include 'CAPTCHA has expired' prefix in message")
        void shouldIncludePrefixInMessage() {
            CaptchaExpiredException ex = new CaptchaExpiredException("abc");

            assertThat(ex.getRawMessage()).startsWith("CAPTCHA has expired: ");
            assertThat(ex.getRawMessage()).endsWith("abc");
        }

        @Test
        @DisplayName("should create exception with UUID captcha ID")
        void shouldCreateExceptionWithUuidCaptchaId() {
            String uuid = "550e8400-e29b-41d4-a716-446655440000";
            CaptchaExpiredException ex = new CaptchaExpiredException(uuid);

            assertThat(ex.getCaptchaId()).isEqualTo(uuid);
            assertThat(ex.getRawMessage()).isEqualTo("CAPTCHA has expired: " + uuid);
        }

        @Test
        @DisplayName("should create exception with null captcha ID")
        void shouldCreateExceptionWithNullCaptchaId() {
            CaptchaExpiredException ex = new CaptchaExpiredException((String) null);

            assertThat(ex.getCaptchaId()).isNull();
            assertThat(ex.getRawMessage()).isEqualTo("CAPTCHA has expired: null");
        }

        @Test
        @DisplayName("should create exception with empty captcha ID")
        void shouldCreateExceptionWithEmptyCaptchaId() {
            CaptchaExpiredException ex = new CaptchaExpiredException("");

            assertThat(ex.getCaptchaId()).isEmpty();
            assertThat(ex.getRawMessage()).isEqualTo("CAPTCHA has expired: ");
        }

        @Test
        @DisplayName("should create exception with whitespace captcha ID")
        void shouldCreateExceptionWithWhitespaceCaptchaId() {
            CaptchaExpiredException ex = new CaptchaExpiredException("   ");

            assertThat(ex.getCaptchaId()).isEqualTo("   ");
            assertThat(ex.getRawMessage()).isEqualTo("CAPTCHA has expired:    ");
        }

        @Test
        @DisplayName("should have no cause")
        void shouldHaveNoCause() {
            CaptchaExpiredException ex = new CaptchaExpiredException("captcha-123");

            assertThat(ex.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("Message and CaptchaId Constructor Tests")
    class MessageAndCaptchaIdConstructorTests {

        @Test
        @DisplayName("should create exception with custom message and captcha ID")
        void shouldCreateExceptionWithCustomMessageAndCaptchaId() {
            CaptchaExpiredException ex = new CaptchaExpiredException(
                "Captcha expired after 5 minutes", "captcha-456");

            assertThat(ex.getCaptchaId()).isEqualTo("captcha-456");
            assertThat(ex.getRawMessage()).isEqualTo("Captcha expired after 5 minutes");
        }

        @Test
        @DisplayName("should allow custom message without captcha ID in message")
        void shouldAllowCustomMessageWithoutCaptchaIdInMessage() {
            CaptchaExpiredException ex = new CaptchaExpiredException(
                "Session timeout", "session-789");

            assertThat(ex.getCaptchaId()).isEqualTo("session-789");
            assertThat(ex.getRawMessage()).isEqualTo("Session timeout");
            assertThat(ex.getRawMessage()).doesNotContain("session-789");
        }

        @Test
        @DisplayName("should create exception with null message and valid captcha ID")
        void shouldCreateExceptionWithNullMessageAndValidCaptchaId() {
            CaptchaExpiredException ex = new CaptchaExpiredException(null, "captcha-111");

            assertThat(ex.getCaptchaId()).isEqualTo("captcha-111");
            assertThat(ex.getRawMessage()).isNull();
        }

        @Test
        @DisplayName("should create exception with empty message")
        void shouldCreateExceptionWithEmptyMessage() {
            CaptchaExpiredException ex = new CaptchaExpiredException("", "captcha-222");

            assertThat(ex.getCaptchaId()).isEqualTo("captcha-222");
            assertThat(ex.getRawMessage()).isEmpty();
        }

        @Test
        @DisplayName("should create exception with both null values")
        void shouldCreateExceptionWithBothNullValues() {
            CaptchaExpiredException ex = new CaptchaExpiredException(null, null);

            assertThat(ex.getCaptchaId()).isNull();
            assertThat(ex.getRawMessage()).isNull();
        }

        @Test
        @DisplayName("should have no cause with custom message constructor")
        void shouldHaveNoCauseWithCustomMessageConstructor() {
            CaptchaExpiredException ex = new CaptchaExpiredException("expired", "id-1");

            assertThat(ex.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("getCaptchaId Method Tests")
    class GetCaptchaIdMethodTests {

        @Test
        @DisplayName("should return exact captcha ID from single-arg constructor")
        void shouldReturnExactCaptchaIdFromSingleArgConstructor() {
            String captchaId = "test-captcha-id-12345";
            CaptchaExpiredException ex = new CaptchaExpiredException(captchaId);

            assertThat(ex.getCaptchaId()).isEqualTo(captchaId);
            assertThat(ex.getCaptchaId()).isSameAs(captchaId);
        }

        @Test
        @DisplayName("should return exact captcha ID from two-arg constructor")
        void shouldReturnExactCaptchaIdFromTwoArgConstructor() {
            String captchaId = "test-captcha-id-67890";
            CaptchaExpiredException ex = new CaptchaExpiredException("msg", captchaId);

            assertThat(ex.getCaptchaId()).isEqualTo(captchaId);
            assertThat(ex.getCaptchaId()).isSameAs(captchaId);
        }

        @Test
        @DisplayName("should return captcha ID with special characters")
        void shouldReturnCaptchaIdWithSpecialCharacters() {
            String captchaId = "captcha_id-with.special/chars:123";
            CaptchaExpiredException ex = new CaptchaExpiredException(captchaId);

            assertThat(ex.getCaptchaId()).isEqualTo(captchaId);
        }

        @Test
        @DisplayName("should return captcha ID with unicode characters")
        void shouldReturnCaptchaIdWithUnicodeCharacters() {
            String captchaId = "captcha-\u4e2d\u6587-test";
            CaptchaExpiredException ex = new CaptchaExpiredException(captchaId);

            assertThat(ex.getCaptchaId()).isEqualTo(captchaId);
        }

        @Test
        @DisplayName("should return long captcha ID")
        void shouldReturnLongCaptchaId() {
            String captchaId = "x".repeat(500);
            CaptchaExpiredException ex = new CaptchaExpiredException(captchaId);

            assertThat(ex.getCaptchaId()).isEqualTo(captchaId);
            assertThat(ex.getCaptchaId()).hasSize(500);
        }

        @Test
        @DisplayName("should return consistent captcha ID on multiple calls")
        void shouldReturnConsistentCaptchaIdOnMultipleCalls() {
            CaptchaExpiredException ex = new CaptchaExpiredException("stable-id");

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
            CaptchaExpiredException ex = new CaptchaExpiredException("id");

            assertThat(ex).isInstanceOf(OpenException.class);
        }

        @Test
        @DisplayName("should extend CaptchaException")
        void shouldExtendCaptchaException() {
            CaptchaExpiredException ex = new CaptchaExpiredException("id");

            assertThat(ex).isInstanceOf(CaptchaException.class);
        }

        @Test
        @DisplayName("should extend RuntimeException")
        void shouldExtendRuntimeException() {
            CaptchaExpiredException ex = new CaptchaExpiredException("id");

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should extend Exception")
        void shouldExtendException() {
            CaptchaExpiredException ex = new CaptchaExpiredException("id");

            assertThat(ex).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("should extend Throwable")
        void shouldExtendThrowable() {
            CaptchaExpiredException ex = new CaptchaExpiredException("id");

            assertThat(ex).isInstanceOf(Throwable.class);
        }

        @Test
        @DisplayName("should be catchable as CaptchaException")
        void shouldBeCatchableAsCaptchaException() {
            assertThatCode(() -> {
                try {
                    throw new CaptchaExpiredException("test-id");
                } catch (CaptchaException e) {
                    assertThat(e).isInstanceOf(CaptchaExpiredException.class);
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should be catchable as RuntimeException")
        void shouldBeCatchableAsRuntimeException() {
            assertThatCode(() -> {
                try {
                    throw new CaptchaExpiredException("test-id");
                } catch (RuntimeException e) {
                    assertThat(e).isInstanceOf(CaptchaExpiredException.class);
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should preserve captcha ID when caught as CaptchaException")
        void shouldPreserveCaptchaIdWhenCaughtAsCaptchaException() {
            assertThatCode(() -> {
                try {
                    throw new CaptchaExpiredException("preserve-me");
                } catch (CaptchaException e) {
                    CaptchaExpiredException expiredEx = (CaptchaExpiredException) e;
                    assertThat(expiredEx.getCaptchaId()).isEqualTo("preserve-me");
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should be throwable with assertThatThrownBy")
        void shouldBeThrowableWithAssertThatThrownBy() {
            assertThatThrownBy(() -> {
                throw new CaptchaExpiredException("thrown-id");
            }).isInstanceOf(CaptchaExpiredException.class)
              .isInstanceOf(CaptchaException.class)
              .hasMessageContaining("CAPTCHA has expired")
              .hasMessageContaining("thrown-id");
        }
    }

    @Nested
    @DisplayName("Message Format Tests")
    class MessageFormatTests {

        @Test
        @DisplayName("should format default message as 'CAPTCHA has expired: ' plus ID")
        void shouldFormatDefaultMessage() {
            CaptchaExpiredException ex = new CaptchaExpiredException("my-id");

            assertThat(ex.getRawMessage()).isEqualTo("CAPTCHA has expired: my-id");
        }

        @Test
        @DisplayName("should use custom message verbatim with two-arg constructor")
        void shouldUseCustomMessageVerbatim() {
            CaptchaExpiredException ex = new CaptchaExpiredException(
                "Custom expired message", "id-99");

            assertThat(ex.getRawMessage()).isEqualTo("Custom expired message");
        }

        @Test
        @DisplayName("default constructor message differs from custom constructor message")
        void defaultConstructorMessageDiffersFromCustom() {
            CaptchaExpiredException defaultEx = new CaptchaExpiredException("captcha-1");
            CaptchaExpiredException customEx = new CaptchaExpiredException(
                "Different message", "captcha-1");

            assertThat(defaultEx.getRawMessage()).isNotEqualTo(customEx.getRawMessage());
            assertThat(defaultEx.getCaptchaId()).isEqualTo(customEx.getCaptchaId());
        }
    }
}
