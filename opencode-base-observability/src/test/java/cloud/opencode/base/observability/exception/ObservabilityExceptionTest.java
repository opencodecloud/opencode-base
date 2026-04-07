package cloud.opencode.base.observability.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ObservabilityException}.
 */
@DisplayName("ObservabilityException")
class ObservabilityExceptionTest {

    @Nested
    @DisplayName("Constructor with message only")
    class MessageOnly {

        @Test
        @DisplayName("should set message and component")
        void shouldSetMessageAndComponent() {
            var ex = new ObservabilityException("test error");
            assertThat(ex.getRawMessage()).isEqualTo("test error");
            assertThat(ex.getComponent()).isEqualTo("Observability");
            assertThat(ex.getErrorCode()).isNull();
            assertThat(ex.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("Constructor with message and cause")
    class MessageAndCause {

        @Test
        @DisplayName("should set message, cause and component")
        void shouldSetMessageCauseAndComponent() {
            var cause = new RuntimeException("root");
            var ex = new ObservabilityException("test error", cause);
            assertThat(ex.getRawMessage()).isEqualTo("test error");
            assertThat(ex.getComponent()).isEqualTo("Observability");
            assertThat(ex.getErrorCode()).isNull();
            assertThat(ex.getCause()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("Constructor with errorCode and message")
    class ErrorCodeAndMessage {

        @Test
        @DisplayName("should set errorCode, message and component")
        void shouldSetErrorCodeMessageAndComponent() {
            var ex = new ObservabilityException("ERR_001", "something failed");
            assertThat(ex.getRawMessage()).isEqualTo("something failed");
            assertThat(ex.getComponent()).isEqualTo("Observability");
            assertThat(ex.getErrorCode()).isEqualTo("ERR_001");
            assertThat(ex.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("Constructor with errorCode, message and cause")
    class ErrorCodeMessageAndCause {

        @Test
        @DisplayName("should set all fields")
        void shouldSetAllFields() {
            var cause = new IllegalStateException("bad state");
            var ex = new ObservabilityException("ERR_002", "operation failed", cause);
            assertThat(ex.getRawMessage()).isEqualTo("operation failed");
            assertThat(ex.getComponent()).isEqualTo("Observability");
            assertThat(ex.getErrorCode()).isEqualTo("ERR_002");
            assertThat(ex.getCause()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("Inheritance")
    class Inheritance {

        @Test
        @DisplayName("should be an instance of OpenException")
        void shouldBeInstanceOfOpenException() {
            var ex = new ObservabilityException("test");
            assertThat(ex).isInstanceOf(OpenException.class);
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("Formatted message")
    class FormattedMessage {

        @Test
        @DisplayName("should include component and error code in getMessage()")
        void shouldFormatMessage() {
            var ex = new ObservabilityException("CODE_X", "details here");
            assertThat(ex.getMessage()).contains("[Observability]");
            assertThat(ex.getMessage()).contains("(CODE_X)");
            assertThat(ex.getMessage()).contains("details here");
        }
    }
}
