package cloud.opencode.base.captcha.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * CaptchaException Test - Unit tests for the base CAPTCHA exception
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
class CaptchaExceptionTest {

    @Nested
    @DisplayName("Message Constructor Tests")
    class MessageConstructorTests {

        @Test
        @DisplayName("should create exception with message")
        void shouldCreateExceptionWithMessage() {
            CaptchaException ex = new CaptchaException("Test error message");

            assertThat(ex.getMessage()).isEqualTo("Test error message");
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("should create exception with empty message")
        void shouldCreateExceptionWithEmptyMessage() {
            CaptchaException ex = new CaptchaException("");

            assertThat(ex.getMessage()).isEmpty();
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("should create exception with null message")
        void shouldCreateExceptionWithNullMessage() {
            CaptchaException ex = new CaptchaException((String) null);

            assertThat(ex.getMessage()).isNull();
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("should preserve special characters in message")
        void shouldPreserveSpecialCharactersInMessage() {
            String message = "Error: Invalid CAPTCHA \u4e2d\u6587 <script>alert('xss')</script>";
            CaptchaException ex = new CaptchaException(message);

            assertThat(ex.getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("should preserve long message")
        void shouldPreserveLongMessage() {
            String message = "A".repeat(10_000);
            CaptchaException ex = new CaptchaException(message);

            assertThat(ex.getMessage()).isEqualTo(message);
            assertThat(ex.getMessage()).hasSize(10_000);
        }

        @Test
        @DisplayName("should preserve whitespace-only message")
        void shouldPreserveWhitespaceOnlyMessage() {
            CaptchaException ex = new CaptchaException("   \t\n  ");

            assertThat(ex.getMessage()).isEqualTo("   \t\n  ");
        }

        @Test
        @DisplayName("should preserve message with newlines")
        void shouldPreserveMessageWithNewlines() {
            String message = "Line 1\nLine 2\nLine 3";
            CaptchaException ex = new CaptchaException(message);

            assertThat(ex.getMessage()).isEqualTo(message);
            assertThat(ex.getMessage()).contains("\n");
        }
    }

    @Nested
    @DisplayName("Message and Cause Constructor Tests")
    class MessageAndCauseConstructorTests {

        @Test
        @DisplayName("should create exception with message and cause")
        void shouldCreateExceptionWithMessageAndCause() {
            RuntimeException cause = new RuntimeException("Root cause");
            CaptchaException ex = new CaptchaException("Test error", cause);

            assertThat(ex.getMessage()).isEqualTo("Test error");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("should create exception with message and null cause")
        void shouldCreateExceptionWithMessageAndNullCause() {
            CaptchaException ex = new CaptchaException("Test error", null);

            assertThat(ex.getMessage()).isEqualTo("Test error");
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("should create exception with null message and cause")
        void shouldCreateExceptionWithNullMessageAndCause() {
            RuntimeException cause = new RuntimeException("Root cause");
            CaptchaException ex = new CaptchaException(null, cause);

            assertThat(ex.getMessage()).isNull();
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("should create exception with both null values")
        void shouldCreateExceptionWithBothNullValues() {
            CaptchaException ex = new CaptchaException(null, null);

            assertThat(ex.getMessage()).isNull();
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("should preserve cause chain")
        void shouldPreserveCauseChain() {
            IllegalArgumentException root = new IllegalArgumentException("Root");
            RuntimeException middle = new RuntimeException("Middle", root);
            CaptchaException ex = new CaptchaException("Top", middle);

            assertThat(ex.getCause()).isEqualTo(middle);
            assertThat(ex.getCause().getCause()).isEqualTo(root);
        }

        @Test
        @DisplayName("should preserve cause message")
        void shouldPreserveCauseMessage() {
            RuntimeException cause = new RuntimeException("Underlying issue");
            CaptchaException ex = new CaptchaException("Wrapper", cause);

            assertThat(ex.getCause().getMessage()).isEqualTo("Underlying issue");
        }

        @Test
        @DisplayName("should accept checked exception as cause")
        void shouldAcceptCheckedExceptionAsCause() {
            Exception cause = new Exception("Checked exception");
            CaptchaException ex = new CaptchaException("Wrapped checked", cause);

            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getCause()).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("should accept Error as cause")
        void shouldAcceptErrorAsCause() {
            OutOfMemoryError cause = new OutOfMemoryError("OOM");
            CaptchaException ex = new CaptchaException("Memory error", cause);

            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getCause()).isInstanceOf(Error.class);
        }
    }

    @Nested
    @DisplayName("Cause Only Constructor Tests")
    class CauseOnlyConstructorTests {

        @Test
        @DisplayName("should create exception with cause only")
        void shouldCreateExceptionWithCauseOnly() {
            RuntimeException cause = new RuntimeException("Root cause");
            CaptchaException ex = new CaptchaException(cause);

            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getMessage()).isEqualTo("java.lang.RuntimeException: Root cause");
        }

        @Test
        @DisplayName("should create exception with null cause")
        void shouldCreateExceptionWithNullCause() {
            CaptchaException ex = new CaptchaException((Throwable) null);

            assertThat(ex.getCause()).isNull();
            assertThat(ex.getMessage()).isNull();
        }

        @Test
        @DisplayName("should handle exception with no message as cause")
        void shouldHandleExceptionWithNoMessageAsCause() {
            RuntimeException cause = new RuntimeException();
            CaptchaException ex = new CaptchaException(cause);

            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getMessage()).isEqualTo("java.lang.RuntimeException");
        }

        @Test
        @DisplayName("should derive message from cause toString")
        void shouldDeriveMessageFromCauseToString() {
            IllegalStateException cause = new IllegalStateException("bad state");
            CaptchaException ex = new CaptchaException(cause);

            assertThat(ex.getMessage()).isEqualTo(cause.toString());
        }

        @Test
        @DisplayName("should handle nested CaptchaException as cause")
        void shouldHandleNestedCaptchaExceptionAsCause() {
            CaptchaException inner = new CaptchaException("inner error");
            CaptchaException ex = new CaptchaException(inner);

            assertThat(ex.getCause()).isEqualTo(inner);
            assertThat(ex.getCause()).isInstanceOf(CaptchaException.class);
            assertThat(ex.getMessage()).contains("inner error");
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("should extend RuntimeException")
        void shouldExtendRuntimeException() {
            CaptchaException ex = new CaptchaException("Test");

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should extend Exception")
        void shouldExtendException() {
            CaptchaException ex = new CaptchaException("Test");

            assertThat(ex).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("should extend Throwable")
        void shouldExtendThrowable() {
            CaptchaException ex = new CaptchaException("Test");

            assertThat(ex).isInstanceOf(Throwable.class);
        }

        @Test
        @DisplayName("should be catchable as RuntimeException")
        void shouldBeCatchableAsRuntimeException() {
            assertThatCode(() -> {
                try {
                    throw new CaptchaException("Test");
                } catch (RuntimeException e) {
                    assertThat(e).isInstanceOf(CaptchaException.class);
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should be throwable without declaration")
        void shouldBeThrowableWithoutDeclaration() {
            assertThatThrownBy(() -> {
                throw new CaptchaException("unchecked");
            }).isInstanceOf(CaptchaException.class)
              .hasMessage("unchecked");
        }

        @Test
        @DisplayName("should support stack trace")
        void shouldSupportStackTrace() {
            CaptchaException ex = new CaptchaException("stack test");

            assertThat(ex.getStackTrace()).isNotEmpty();
            assertThat(ex.getStackTrace()[0].getClassName())
                .contains("CaptchaExceptionTest");
        }
    }

    @Nested
    @DisplayName("Serialization Tests")
    class SerializationTests {

        @Test
        @DisplayName("should be serializable as RuntimeException subclass")
        void shouldBeSerializableAsRuntimeExceptionSubclass() {
            CaptchaException ex = new CaptchaException("serializable");

            assertThat(ex).isInstanceOf(java.io.Serializable.class);
        }
    }
}
