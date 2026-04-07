package cloud.opencode.base.captcha.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * CaptchaVerifyException Test - Unit tests for CAPTCHA verification exception
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
class CaptchaVerifyExceptionTest {

    @Nested
    @DisplayName("CaptchaId Only Constructor Tests")
    class CaptchaIdOnlyConstructorTests {

        @Test
        @DisplayName("should create exception with captcha ID")
        void shouldCreateExceptionWithCaptchaId() {
            CaptchaVerifyException ex = new CaptchaVerifyException("captcha-123");

            assertThat(ex.getCaptchaId()).isEqualTo("captcha-123");
            assertThat(ex.getProvidedAnswer()).isNull();
            assertThat(ex.getRawMessage()).isEqualTo("CAPTCHA verification failed: captcha-123");
        }

        @Test
        @DisplayName("should create exception with UUID captcha ID")
        void shouldCreateExceptionWithUuidCaptchaId() {
            String uuid = "123e4567-e89b-12d3-a456-426614174000";
            CaptchaVerifyException ex = new CaptchaVerifyException(uuid);

            assertThat(ex.getCaptchaId()).isEqualTo(uuid);
            assertThat(ex.getProvidedAnswer()).isNull();
            assertThat(ex.getRawMessage()).isEqualTo("CAPTCHA verification failed: " + uuid);
        }

        @Test
        @DisplayName("should create exception with null captcha ID")
        void shouldCreateExceptionWithNullCaptchaId() {
            CaptchaVerifyException ex = new CaptchaVerifyException((String) null);

            assertThat(ex.getCaptchaId()).isNull();
            assertThat(ex.getProvidedAnswer()).isNull();
            assertThat(ex.getRawMessage()).isEqualTo("CAPTCHA verification failed: null");
        }

        @Test
        @DisplayName("should create exception with empty captcha ID")
        void shouldCreateExceptionWithEmptyCaptchaId() {
            CaptchaVerifyException ex = new CaptchaVerifyException("");

            assertThat(ex.getCaptchaId()).isEmpty();
            assertThat(ex.getProvidedAnswer()).isNull();
            assertThat(ex.getRawMessage()).isEqualTo("CAPTCHA verification failed: ");
        }

        @Test
        @DisplayName("should have no cause")
        void shouldHaveNoCause() {
            CaptchaVerifyException ex = new CaptchaVerifyException("captcha-123");

            assertThat(ex.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("CaptchaId and ProvidedAnswer Constructor Tests")
    class CaptchaIdAndProvidedAnswerConstructorTests {

        @Test
        @DisplayName("should create exception with captcha ID and provided answer")
        void shouldCreateExceptionWithCaptchaIdAndProvidedAnswer() {
            CaptchaVerifyException ex = new CaptchaVerifyException("captcha-456", "ABCD");

            assertThat(ex.getCaptchaId()).isEqualTo("captcha-456");
            assertThat(ex.getProvidedAnswer()).isEqualTo("ABCD");
            assertThat(ex.getRawMessage()).isEqualTo("CAPTCHA verification failed for ID: captcha-456");
        }

        @Test
        @DisplayName("should create exception with numeric answer")
        void shouldCreateExceptionWithNumericAnswer() {
            CaptchaVerifyException ex = new CaptchaVerifyException("captcha-num", "12345");

            assertThat(ex.getCaptchaId()).isEqualTo("captcha-num");
            assertThat(ex.getProvidedAnswer()).isEqualTo("12345");
        }

        @Test
        @DisplayName("should create exception with arithmetic answer")
        void shouldCreateExceptionWithArithmeticAnswer() {
            CaptchaVerifyException ex = new CaptchaVerifyException("captcha-math", "42");

            assertThat(ex.getCaptchaId()).isEqualTo("captcha-math");
            assertThat(ex.getProvidedAnswer()).isEqualTo("42");
        }

        @Test
        @DisplayName("should create exception with Chinese answer")
        void shouldCreateExceptionWithChineseAnswer() {
            CaptchaVerifyException ex = new CaptchaVerifyException("captcha-cn", "\u4e2d\u6587");

            assertThat(ex.getCaptchaId()).isEqualTo("captcha-cn");
            assertThat(ex.getProvidedAnswer()).isEqualTo("\u4e2d\u6587");
        }

        @Test
        @DisplayName("should create exception with null provided answer")
        void shouldCreateExceptionWithNullProvidedAnswer() {
            CaptchaVerifyException ex = new CaptchaVerifyException("captcha-789", null);

            assertThat(ex.getCaptchaId()).isEqualTo("captcha-789");
            assertThat(ex.getProvidedAnswer()).isNull();
        }

        @Test
        @DisplayName("should create exception with empty provided answer")
        void shouldCreateExceptionWithEmptyProvidedAnswer() {
            CaptchaVerifyException ex = new CaptchaVerifyException("captcha-empty", "");

            assertThat(ex.getCaptchaId()).isEqualTo("captcha-empty");
            assertThat(ex.getProvidedAnswer()).isEmpty();
        }

        @Test
        @DisplayName("should create exception with whitespace answer")
        void shouldCreateExceptionWithWhitespaceAnswer() {
            CaptchaVerifyException ex = new CaptchaVerifyException("captcha-ws", "   ");

            assertThat(ex.getCaptchaId()).isEqualTo("captcha-ws");
            assertThat(ex.getProvidedAnswer()).isEqualTo("   ");
        }

        @Test
        @DisplayName("should create exception with both null values")
        void shouldCreateExceptionWithBothNullValues() {
            CaptchaVerifyException ex = new CaptchaVerifyException(null, null);

            assertThat(ex.getCaptchaId()).isNull();
            assertThat(ex.getProvidedAnswer()).isNull();
        }

        @Test
        @DisplayName("should have no cause")
        void shouldHaveNoCause() {
            CaptchaVerifyException ex = new CaptchaVerifyException("captcha-id", "answer");

            assertThat(ex.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("Message, CaptchaId and Cause Constructor Tests")
    class MessageCaptchaIdAndCauseConstructorTests {

        @Test
        @DisplayName("should create exception with message, captcha ID and cause")
        void shouldCreateExceptionWithMessageCaptchaIdAndCause() {
            RuntimeException cause = new RuntimeException("Verification service error");
            CaptchaVerifyException ex = new CaptchaVerifyException(
                "Custom verification error", "captcha-111", cause);

            assertThat(ex.getCaptchaId()).isEqualTo("captcha-111");
            assertThat(ex.getProvidedAnswer()).isNull();
            assertThat(ex.getRawMessage()).isEqualTo("Custom verification error");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("should create exception with null cause")
        void shouldCreateExceptionWithNullCause() {
            CaptchaVerifyException ex = new CaptchaVerifyException(
                "Verification failed", "captcha-222", null);

            assertThat(ex.getCaptchaId()).isEqualTo("captcha-222");
            assertThat(ex.getProvidedAnswer()).isNull();
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("should create exception with all null parameters")
        void shouldCreateExceptionWithAllNullParameters() {
            CaptchaVerifyException ex = new CaptchaVerifyException(null, null, null);

            assertThat(ex.getCaptchaId()).isNull();
            assertThat(ex.getProvidedAnswer()).isNull();
            assertThat(ex.getRawMessage()).isNull();
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("should always have null providedAnswer from three-arg constructor")
        void shouldAlwaysHaveNullProvidedAnswerFromThreeArgConstructor() {
            CaptchaVerifyException ex = new CaptchaVerifyException(
                "Error", "captcha-id", new RuntimeException());

            assertThat(ex.getProvidedAnswer()).isNull();
        }

        @Test
        @DisplayName("should preserve cause chain")
        void shouldPreserveCauseChain() {
            IllegalArgumentException root = new IllegalArgumentException("Invalid input");
            RuntimeException middle = new RuntimeException("Processing error", root);
            CaptchaVerifyException ex = new CaptchaVerifyException(
                "Verification failed", "captcha-333", middle);

            assertThat(ex.getCause()).isEqualTo(middle);
            assertThat(ex.getCause().getCause()).isEqualTo(root);
        }

        @Test
        @DisplayName("should preserve cause message")
        void shouldPreserveCauseMessage() {
            RuntimeException cause = new RuntimeException("Service timeout");
            CaptchaVerifyException ex = new CaptchaVerifyException(
                "Verify failed", "captcha-444", cause);

            assertThat(ex.getCause().getMessage()).isEqualTo("Service timeout");
        }

        @Test
        @DisplayName("should accept checked exception as cause")
        void shouldAcceptCheckedExceptionAsCause() {
            Exception cause = new Exception("Checked verification error");
            CaptchaVerifyException ex = new CaptchaVerifyException(
                "Wrapper", "captcha-555", cause);

            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getCause()).isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("getCaptchaId Method Tests")
    class GetCaptchaIdMethodTests {

        @Test
        @DisplayName("should return exact captcha ID from single-arg constructor")
        void shouldReturnExactCaptchaIdFromSingleArgConstructor() {
            String captchaId = "test-captcha-verify-id";
            CaptchaVerifyException ex = new CaptchaVerifyException(captchaId);

            assertThat(ex.getCaptchaId()).isEqualTo(captchaId);
            assertThat(ex.getCaptchaId()).isSameAs(captchaId);
        }

        @Test
        @DisplayName("should return exact captcha ID from two-arg constructor")
        void shouldReturnExactCaptchaIdFromTwoArgConstructor() {
            String captchaId = "two-arg-verify-id";
            CaptchaVerifyException ex = new CaptchaVerifyException(captchaId, "answer");

            assertThat(ex.getCaptchaId()).isEqualTo(captchaId);
            assertThat(ex.getCaptchaId()).isSameAs(captchaId);
        }

        @Test
        @DisplayName("should return exact captcha ID from three-arg constructor")
        void shouldReturnExactCaptchaIdFromThreeArgConstructor() {
            String captchaId = "three-arg-verify-id";
            CaptchaVerifyException ex = new CaptchaVerifyException(
                "msg", captchaId, new RuntimeException());

            assertThat(ex.getCaptchaId()).isEqualTo(captchaId);
            assertThat(ex.getCaptchaId()).isSameAs(captchaId);
        }

        @Test
        @DisplayName("should return captcha ID with special characters")
        void shouldReturnCaptchaIdWithSpecialCharacters() {
            String captchaId = "captcha:verify:user@123:session#456";
            CaptchaVerifyException ex = new CaptchaVerifyException(captchaId);

            assertThat(ex.getCaptchaId()).isEqualTo(captchaId);
        }

        @Test
        @DisplayName("should return consistent captcha ID on multiple calls")
        void shouldReturnConsistentCaptchaIdOnMultipleCalls() {
            CaptchaVerifyException ex = new CaptchaVerifyException("stable-id");

            assertThat(ex.getCaptchaId()).isEqualTo(ex.getCaptchaId());
            assertThat(ex.getCaptchaId()).isSameAs(ex.getCaptchaId());
        }
    }

    @Nested
    @DisplayName("getProvidedAnswer Method Tests")
    class GetProvidedAnswerMethodTests {

        @Test
        @DisplayName("should return null when created with single-arg constructor")
        void shouldReturnNullWhenCreatedWithSingleArgConstructor() {
            CaptchaVerifyException ex = new CaptchaVerifyException("captcha-id");

            assertThat(ex.getProvidedAnswer()).isNull();
        }

        @Test
        @DisplayName("should return null when created with three-arg constructor")
        void shouldReturnNullWhenCreatedWithThreeArgConstructor() {
            CaptchaVerifyException ex = new CaptchaVerifyException(
                "msg", "captcha-id", new RuntimeException());

            assertThat(ex.getProvidedAnswer()).isNull();
        }

        @Test
        @DisplayName("should return exact provided answer")
        void shouldReturnExactProvidedAnswer() {
            String answer = "ABC123";
            CaptchaVerifyException ex = new CaptchaVerifyException("captcha-id", answer);

            assertThat(ex.getProvidedAnswer()).isEqualTo(answer);
            assertThat(ex.getProvidedAnswer()).isSameAs(answer);
        }

        @Test
        @DisplayName("should return case-sensitive answer")
        void shouldReturnCaseSensitiveAnswer() {
            CaptchaVerifyException ex = new CaptchaVerifyException("captcha-id", "AbCdEf");

            assertThat(ex.getProvidedAnswer()).isEqualTo("AbCdEf");
            assertThat(ex.getProvidedAnswer()).isNotEqualTo("abcdef");
            assertThat(ex.getProvidedAnswer()).isNotEqualTo("ABCDEF");
        }

        @Test
        @DisplayName("should return answer with mixed content")
        void shouldReturnAnswerWithMixedContent() {
            String answer = "A1B2C3!@#";
            CaptchaVerifyException ex = new CaptchaVerifyException("captcha-id", answer);

            assertThat(ex.getProvidedAnswer()).isEqualTo(answer);
        }

        @Test
        @DisplayName("should return consistent answer on multiple calls")
        void shouldReturnConsistentAnswerOnMultipleCalls() {
            CaptchaVerifyException ex = new CaptchaVerifyException("id", "stable-answer");

            assertThat(ex.getProvidedAnswer()).isEqualTo(ex.getProvidedAnswer());
            assertThat(ex.getProvidedAnswer()).isSameAs(ex.getProvidedAnswer());
        }

        @Test
        @DisplayName("should return long provided answer")
        void shouldReturnLongProvidedAnswer() {
            String answer = "Z".repeat(1000);
            CaptchaVerifyException ex = new CaptchaVerifyException("id", answer);

            assertThat(ex.getProvidedAnswer()).isEqualTo(answer);
            assertThat(ex.getProvidedAnswer()).hasSize(1000);
        }
    }

    @Nested
    @DisplayName("Message Format Tests")
    class MessageFormatTests {

        @Test
        @DisplayName("should format message for captcha ID only constructor")
        void shouldFormatMessageForCaptchaIdOnlyConstructor() {
            CaptchaVerifyException ex = new CaptchaVerifyException("test-captcha");

            assertThat(ex.getRawMessage()).isEqualTo("CAPTCHA verification failed: test-captcha");
        }

        @Test
        @DisplayName("should format message for captcha ID and answer constructor")
        void shouldFormatMessageForCaptchaIdAndAnswerConstructor() {
            CaptchaVerifyException ex = new CaptchaVerifyException("test-captcha", "wrong-answer");

            assertThat(ex.getRawMessage()).isEqualTo("CAPTCHA verification failed for ID: test-captcha");
        }

        @Test
        @DisplayName("should use custom message for full constructor")
        void shouldUseCustomMessageForFullConstructor() {
            CaptchaVerifyException ex = new CaptchaVerifyException(
                "Custom error message", "test-captcha", new RuntimeException());

            assertThat(ex.getRawMessage()).isEqualTo("Custom error message");
        }

        @Test
        @DisplayName("single-arg message uses 'verification failed:' format")
        void singleArgMessageUsesColonFormat() {
            CaptchaVerifyException ex = new CaptchaVerifyException("id-1");

            assertThat(ex.getRawMessage()).startsWith("CAPTCHA verification failed: ");
        }

        @Test
        @DisplayName("two-arg message uses 'verification failed for ID:' format")
        void twoArgMessageUsesForIdFormat() {
            CaptchaVerifyException ex = new CaptchaVerifyException("id-2", "ans");

            assertThat(ex.getRawMessage()).startsWith("CAPTCHA verification failed for ID: ");
        }

        @Test
        @DisplayName("single-arg and two-arg messages differ for same captcha ID")
        void singleArgAndTwoArgMessagesDifferForSameCaptchaId() {
            CaptchaVerifyException singleArg = new CaptchaVerifyException("captcha-1");
            CaptchaVerifyException twoArg = new CaptchaVerifyException("captcha-1", "answer");

            assertThat(singleArg.getRawMessage()).isNotEqualTo(twoArg.getRawMessage());
            assertThat(singleArg.getCaptchaId()).isEqualTo(twoArg.getCaptchaId());
        }

        @Test
        @DisplayName("provided answer does not appear in message")
        void providedAnswerDoesNotAppearInMessage() {
            CaptchaVerifyException ex = new CaptchaVerifyException("captcha-1", "secret-answer");

            assertThat(ex.getRawMessage()).doesNotContain("secret-answer");
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("should extend OpenException")
        void shouldExtendOpenException() {
            CaptchaVerifyException ex = new CaptchaVerifyException("id");

            assertThat(ex).isInstanceOf(OpenException.class);
        }

        @Test
        @DisplayName("should extend CaptchaException")
        void shouldExtendCaptchaException() {
            CaptchaVerifyException ex = new CaptchaVerifyException("id");

            assertThat(ex).isInstanceOf(CaptchaException.class);
        }

        @Test
        @DisplayName("should extend RuntimeException")
        void shouldExtendRuntimeException() {
            CaptchaVerifyException ex = new CaptchaVerifyException("id");

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should extend Exception")
        void shouldExtendException() {
            CaptchaVerifyException ex = new CaptchaVerifyException("id");

            assertThat(ex).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("should extend Throwable")
        void shouldExtendThrowable() {
            CaptchaVerifyException ex = new CaptchaVerifyException("id");

            assertThat(ex).isInstanceOf(Throwable.class);
        }

        @Test
        @DisplayName("should be catchable as CaptchaException")
        void shouldBeCatchableAsCaptchaException() {
            assertThatCode(() -> {
                try {
                    throw new CaptchaVerifyException("test-id", "wrong-answer");
                } catch (CaptchaException e) {
                    assertThat(e).isInstanceOf(CaptchaVerifyException.class);
                    CaptchaVerifyException verifyEx = (CaptchaVerifyException) e;
                    assertThat(verifyEx.getProvidedAnswer()).isEqualTo("wrong-answer");
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should be throwable with assertThatThrownBy")
        void shouldBeThrowableWithAssertThatThrownBy() {
            assertThatThrownBy(() -> {
                throw new CaptchaVerifyException("thrown-id");
            }).isInstanceOf(CaptchaVerifyException.class)
              .isInstanceOf(CaptchaException.class)
              .hasMessageContaining("CAPTCHA verification failed")
              .hasMessageContaining("thrown-id");
        }

        @Test
        @DisplayName("should preserve all fields when caught as parent exception")
        void shouldPreserveAllFieldsWhenCaughtAsParentException() {
            String captchaId = "preserve-fields-id";
            String answer = "preserve-answer";

            assertThatCode(() -> {
                try {
                    throw new CaptchaVerifyException(captchaId, answer);
                } catch (CaptchaException e) {
                    CaptchaVerifyException verifyEx = (CaptchaVerifyException) e;
                    assertThat(verifyEx.getCaptchaId()).isEqualTo(captchaId);
                    assertThat(verifyEx.getProvidedAnswer()).isEqualTo(answer);
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should preserve cause when caught as RuntimeException")
        void shouldPreserveCauseWhenCaughtAsRuntimeException() {
            RuntimeException cause = new RuntimeException("original");

            assertThatCode(() -> {
                try {
                    throw new CaptchaVerifyException("msg", "id", cause);
                } catch (RuntimeException e) {
                    assertThat(e).isInstanceOf(CaptchaVerifyException.class);
                    assertThat(e.getCause()).isEqualTo(cause);
                }
            }).doesNotThrowAnyException();
        }
    }
}
