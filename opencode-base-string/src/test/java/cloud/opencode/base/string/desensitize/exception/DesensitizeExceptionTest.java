package cloud.opencode.base.string.desensitize.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * DesensitizeExceptionTest Tests
 * DesensitizeExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("DesensitizeException Tests")
class DesensitizeExceptionTest {

    @Nested
    @DisplayName("Constructor with message Tests")
    class ConstructorWithMessageTests {

        @Test
        @DisplayName("Should create exception with message")
        void shouldCreateExceptionWithMessage() {
            DesensitizeException ex = new DesensitizeException("Test message");
            assertThat(ex.getMessage()).isEqualTo("Test message");
        }

        @Test
        @DisplayName("Should be throwable")
        void shouldBeThrowable() {
            assertThatThrownBy(() -> {
                throw new DesensitizeException("Error occurred");
            }).isInstanceOf(DesensitizeException.class)
              .hasMessage("Error occurred");
        }
    }

    @Nested
    @DisplayName("Constructor with message and cause Tests")
    class ConstructorWithMessageAndCauseTests {

        @Test
        @DisplayName("Should create exception with message and cause")
        void shouldCreateExceptionWithMessageAndCause() {
            Throwable cause = new RuntimeException("Root cause");
            DesensitizeException ex = new DesensitizeException("Test message", cause);

            assertThat(ex.getMessage()).isEqualTo("Test message");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("Should preserve cause chain")
        void shouldPreserveCauseChain() {
            Throwable rootCause = new IllegalArgumentException("Invalid value");
            Throwable middleCause = new RuntimeException("Processing failed", rootCause);
            DesensitizeException ex = new DesensitizeException("Desensitize failed", middleCause);

            assertThat(ex.getCause()).isEqualTo(middleCause);
            assertThat(ex.getCause().getCause()).isEqualTo(rootCause);
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("Should be RuntimeException")
        void shouldBeRuntimeException() {
            DesensitizeException ex = new DesensitizeException("Test");
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }
}
