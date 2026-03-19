package cloud.opencode.base.yml.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenYmlExceptionTest Tests
 * OpenYmlExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("OpenYmlException Tests")
class OpenYmlExceptionTest {

    @Nested
    @DisplayName("Constructor with message only")
    class MessageOnlyConstructorTests {

        @Test
        @DisplayName("should set message correctly")
        void shouldSetMessageCorrectly() {
            OpenYmlException exception = new OpenYmlException("Test error message");

            assertThat(exception.getMessage()).isEqualTo("Test error message");
        }

        @Test
        @DisplayName("should set line to -1 when not provided")
        void shouldSetLineToNegativeOneWhenNotProvided() {
            OpenYmlException exception = new OpenYmlException("Test error");

            assertThat(exception.getLine()).isEqualTo(-1);
        }

        @Test
        @DisplayName("should set column to -1 when not provided")
        void shouldSetColumnToNegativeOneWhenNotProvided() {
            OpenYmlException exception = new OpenYmlException("Test error");

            assertThat(exception.getColumn()).isEqualTo(-1);
        }

        @Test
        @DisplayName("should have no cause")
        void shouldHaveNoCause() {
            OpenYmlException exception = new OpenYmlException("Test error");

            assertThat(exception.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("Constructor with message and location")
    class MessageAndLocationConstructorTests {

        @Test
        @DisplayName("should format message with location")
        void shouldFormatMessageWithLocation() {
            OpenYmlException exception = new OpenYmlException("Syntax error", 10, 5);

            assertThat(exception.getMessage()).isEqualTo("Syntax error (line: 10, column: 5)");
        }

        @Test
        @DisplayName("should store line number")
        void shouldStoreLineNumber() {
            OpenYmlException exception = new OpenYmlException("Error", 15, 20);

            assertThat(exception.getLine()).isEqualTo(15);
        }

        @Test
        @DisplayName("should store column number")
        void shouldStoreColumnNumber() {
            OpenYmlException exception = new OpenYmlException("Error", 15, 20);

            assertThat(exception.getColumn()).isEqualTo(20);
        }

        @Test
        @DisplayName("should handle zero line and column")
        void shouldHandleZeroLineAndColumn() {
            OpenYmlException exception = new OpenYmlException("Error at start", 0, 0);

            assertThat(exception.getMessage()).isEqualTo("Error at start (line: 0, column: 0)");
            assertThat(exception.getLine()).isEqualTo(0);
            assertThat(exception.getColumn()).isEqualTo(0);
        }

        @Test
        @DisplayName("should not format message when line is negative")
        void shouldNotFormatMessageWhenLineIsNegative() {
            OpenYmlException exception = new OpenYmlException("Error", -1, 5);

            assertThat(exception.getMessage()).isEqualTo("Error");
        }

        @Test
        @DisplayName("should not format message when column is negative")
        void shouldNotFormatMessageWhenColumnIsNegative() {
            OpenYmlException exception = new OpenYmlException("Error", 10, -1);

            assertThat(exception.getMessage()).isEqualTo("Error");
        }
    }

    @Nested
    @DisplayName("Constructor with message and cause")
    class MessageAndCauseConstructorTests {

        @Test
        @DisplayName("should set message correctly")
        void shouldSetMessageCorrectly() {
            Throwable cause = new RuntimeException("Root cause");
            OpenYmlException exception = new OpenYmlException("Wrapped error", cause);

            assertThat(exception.getMessage()).isEqualTo("Wrapped error");
        }

        @Test
        @DisplayName("should set cause correctly")
        void shouldSetCauseCorrectly() {
            Throwable cause = new RuntimeException("Root cause");
            OpenYmlException exception = new OpenYmlException("Wrapped error", cause);

            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getCause().getMessage()).isEqualTo("Root cause");
        }

        @Test
        @DisplayName("should set line to -1")
        void shouldSetLineToNegativeOne() {
            Throwable cause = new RuntimeException("Root cause");
            OpenYmlException exception = new OpenYmlException("Error", cause);

            assertThat(exception.getLine()).isEqualTo(-1);
        }

        @Test
        @DisplayName("should set column to -1")
        void shouldSetColumnToNegativeOne() {
            Throwable cause = new RuntimeException("Root cause");
            OpenYmlException exception = new OpenYmlException("Error", cause);

            assertThat(exception.getColumn()).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("Constructor with message, cause and location")
    class FullConstructorTests {

        @Test
        @DisplayName("should format message with location")
        void shouldFormatMessageWithLocation() {
            Throwable cause = new RuntimeException("Root cause");
            OpenYmlException exception = new OpenYmlException("Parse error", cause, 5, 10);

            assertThat(exception.getMessage()).isEqualTo("Parse error (line: 5, column: 10)");
        }

        @Test
        @DisplayName("should set cause correctly")
        void shouldSetCauseCorrectly() {
            Throwable cause = new RuntimeException("Root cause");
            OpenYmlException exception = new OpenYmlException("Error", cause, 5, 10);

            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("should store line number")
        void shouldStoreLineNumber() {
            Throwable cause = new RuntimeException("Root cause");
            OpenYmlException exception = new OpenYmlException("Error", cause, 25, 30);

            assertThat(exception.getLine()).isEqualTo(25);
        }

        @Test
        @DisplayName("should store column number")
        void shouldStoreColumnNumber() {
            Throwable cause = new RuntimeException("Root cause");
            OpenYmlException exception = new OpenYmlException("Error", cause, 25, 30);

            assertThat(exception.getColumn()).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("getLine method")
    class GetLineTests {

        @Test
        @DisplayName("should return -1 for message-only constructor")
        void shouldReturnNegativeOneForMessageOnlyConstructor() {
            OpenYmlException exception = new OpenYmlException("Error");

            assertThat(exception.getLine()).isEqualTo(-1);
        }

        @Test
        @DisplayName("should return provided line number")
        void shouldReturnProvidedLineNumber() {
            OpenYmlException exception = new OpenYmlException("Error", 100, 50);

            assertThat(exception.getLine()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("getColumn method")
    class GetColumnTests {

        @Test
        @DisplayName("should return -1 for message-only constructor")
        void shouldReturnNegativeOneForMessageOnlyConstructor() {
            OpenYmlException exception = new OpenYmlException("Error");

            assertThat(exception.getColumn()).isEqualTo(-1);
        }

        @Test
        @DisplayName("should return provided column number")
        void shouldReturnProvidedColumnNumber() {
            OpenYmlException exception = new OpenYmlException("Error", 100, 50);

            assertThat(exception.getColumn()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("hasLocation method")
    class HasLocationTests {

        @Test
        @DisplayName("should return false when no location provided")
        void shouldReturnFalseWhenNoLocationProvided() {
            OpenYmlException exception = new OpenYmlException("Error");

            assertThat(exception.hasLocation()).isFalse();
        }

        @Test
        @DisplayName("should return false when line is negative")
        void shouldReturnFalseWhenLineIsNegative() {
            OpenYmlException exception = new OpenYmlException("Error", new RuntimeException());

            assertThat(exception.hasLocation()).isFalse();
        }

        @Test
        @DisplayName("should return true when both line and column are non-negative")
        void shouldReturnTrueWhenBothLineAndColumnAreNonNegative() {
            OpenYmlException exception = new OpenYmlException("Error", 0, 0);

            assertThat(exception.hasLocation()).isTrue();
        }

        @Test
        @DisplayName("should return true for positive line and column")
        void shouldReturnTrueForPositiveLineAndColumn() {
            OpenYmlException exception = new OpenYmlException("Error", 10, 20);

            assertThat(exception.hasLocation()).isTrue();
        }

        @Test
        @DisplayName("should return false when only line is negative")
        void shouldReturnFalseWhenOnlyLineIsNegative() {
            // Using full constructor but with negative line
            OpenYmlException exception = new OpenYmlException("Error", -1, 5);

            assertThat(exception.hasLocation()).isFalse();
        }

        @Test
        @DisplayName("should return false when only column is negative")
        void shouldReturnFalseWhenOnlyColumnIsNegative() {
            // Using full constructor but with negative column
            OpenYmlException exception = new OpenYmlException("Error", 5, -1);

            assertThat(exception.hasLocation()).isFalse();
        }
    }

    @Nested
    @DisplayName("Inheritance hierarchy")
    class InheritanceTests {

        @Test
        @DisplayName("should extend RuntimeException")
        void shouldExtendRuntimeException() {
            OpenYmlException exception = new OpenYmlException("Test");

            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should be catchable as RuntimeException")
        void shouldBeCatchableAsRuntimeException() {
            assertThatThrownBy(() -> {
                throw new OpenYmlException("Test");
            }).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should be catchable as Exception")
        void shouldBeCatchableAsException() {
            assertThatThrownBy(() -> {
                throw new OpenYmlException("Test");
            }).isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle null message")
        void shouldHandleNullMessage() {
            OpenYmlException exception = new OpenYmlException(null);

            assertThat(exception.getMessage()).isNull();
        }

        @Test
        @DisplayName("should handle empty message")
        void shouldHandleEmptyMessage() {
            OpenYmlException exception = new OpenYmlException("");

            assertThat(exception.getMessage()).isEmpty();
        }

        @Test
        @DisplayName("should handle large line numbers")
        void shouldHandleLargeLineNumbers() {
            OpenYmlException exception = new OpenYmlException("Error", Integer.MAX_VALUE, Integer.MAX_VALUE);

            assertThat(exception.getLine()).isEqualTo(Integer.MAX_VALUE);
            assertThat(exception.getColumn()).isEqualTo(Integer.MAX_VALUE);
            assertThat(exception.hasLocation()).isTrue();
        }

        @Test
        @DisplayName("should handle chained causes")
        void shouldHandleChainedCauses() {
            Throwable rootCause = new IllegalStateException("Root");
            Throwable intermediateCause = new RuntimeException("Intermediate", rootCause);
            OpenYmlException exception = new OpenYmlException("Top level", intermediateCause);

            assertThat(exception.getCause()).isEqualTo(intermediateCause);
            assertThat(exception.getCause().getCause()).isEqualTo(rootCause);
        }
    }
}
