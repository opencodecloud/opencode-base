package cloud.opencode.base.yml.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * YmlParseExceptionTest Tests
 * YmlParseExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.3
 */
@DisplayName("YmlParseException Tests")
class YmlParseExceptionTest {

    @Nested
    @DisplayName("Constructor with message only")
    class MessageOnlyConstructorTests {

        @Test
        @DisplayName("should set message correctly")
        void shouldSetMessageCorrectly() {
            YmlParseException exception = new YmlParseException("Invalid YAML syntax");

            assertThat(exception.getRawMessage()).isEqualTo("Invalid YAML syntax");
            assertThat(exception.getMessage()).contains("Invalid YAML syntax");
        }

        @Test
        @DisplayName("should set error code to YML_PARSE_001")
        void shouldSetErrorCode() {
            YmlParseException exception = new YmlParseException("Parse error");

            assertThat(exception.getErrorCode()).isEqualTo("YML_PARSE_001");
        }

        @Test
        @DisplayName("should set component to yml")
        void shouldSetComponentToYml() {
            YmlParseException exception = new YmlParseException("Parse error");

            assertThat(exception.getComponent()).isEqualTo("yml");
        }

        @Test
        @DisplayName("should set line to -1")
        void shouldSetLineToNegativeOne() {
            YmlParseException exception = new YmlParseException("Parse error");

            assertThat(exception.getLine()).isEqualTo(-1);
        }

        @Test
        @DisplayName("should set column to -1")
        void shouldSetColumnToNegativeOne() {
            YmlParseException exception = new YmlParseException("Parse error");

            assertThat(exception.getColumn()).isEqualTo(-1);
        }

        @Test
        @DisplayName("should have no location")
        void shouldHaveNoLocation() {
            YmlParseException exception = new YmlParseException("Parse error");

            assertThat(exception.hasLocation()).isFalse();
        }
    }

    @Nested
    @DisplayName("Constructor with message and location")
    class MessageAndLocationConstructorTests {

        @Test
        @DisplayName("should format message with location")
        void shouldFormatMessageWithLocation() {
            YmlParseException exception = new YmlParseException("Unexpected character", 5, 10);

            assertThat(exception.getRawMessage()).isEqualTo("Unexpected character (line: 5, column: 10)");
        }

        @Test
        @DisplayName("should store line number")
        void shouldStoreLineNumber() {
            YmlParseException exception = new YmlParseException("Error", 25, 30);

            assertThat(exception.getLine()).isEqualTo(25);
        }

        @Test
        @DisplayName("should store column number")
        void shouldStoreColumnNumber() {
            YmlParseException exception = new YmlParseException("Error", 25, 30);

            assertThat(exception.getColumn()).isEqualTo(30);
        }

        @Test
        @DisplayName("should have location")
        void shouldHaveLocation() {
            YmlParseException exception = new YmlParseException("Error", 1, 1);

            assertThat(exception.hasLocation()).isTrue();
        }

        @Test
        @DisplayName("should handle line 0 and column 0")
        void shouldHandleLineZeroAndColumnZero() {
            YmlParseException exception = new YmlParseException("Error at start", 0, 0);

            assertThat(exception.getLine()).isEqualTo(0);
            assertThat(exception.getColumn()).isEqualTo(0);
            assertThat(exception.hasLocation()).isTrue();
        }
    }

    @Nested
    @DisplayName("Constructor with message and cause")
    class MessageAndCauseConstructorTests {

        @Test
        @DisplayName("should set message correctly")
        void shouldSetMessageCorrectly() {
            Throwable cause = new RuntimeException("Scanner error");
            YmlParseException exception = new YmlParseException("Failed to parse YAML", cause);

            assertThat(exception.getRawMessage()).isEqualTo("Failed to parse YAML");
        }

        @Test
        @DisplayName("should set cause correctly")
        void shouldSetCauseCorrectly() {
            Throwable cause = new RuntimeException("Scanner error");
            YmlParseException exception = new YmlParseException("Failed to parse YAML", cause);

            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("should set line to -1")
        void shouldSetLineToNegativeOne() {
            Throwable cause = new RuntimeException("Scanner error");
            YmlParseException exception = new YmlParseException("Error", cause);

            assertThat(exception.getLine()).isEqualTo(-1);
        }

        @Test
        @DisplayName("should set column to -1")
        void shouldSetColumnToNegativeOne() {
            Throwable cause = new RuntimeException("Scanner error");
            YmlParseException exception = new YmlParseException("Error", cause);

            assertThat(exception.getColumn()).isEqualTo(-1);
        }

        @Test
        @DisplayName("should have no location")
        void shouldHaveNoLocation() {
            Throwable cause = new RuntimeException("Scanner error");
            YmlParseException exception = new YmlParseException("Error", cause);

            assertThat(exception.hasLocation()).isFalse();
        }
    }

    @Nested
    @DisplayName("Constructor with message, cause and location")
    class FullConstructorTests {

        @Test
        @DisplayName("should format message with location")
        void shouldFormatMessageWithLocation() {
            Throwable cause = new RuntimeException("Unexpected token");
            YmlParseException exception = new YmlParseException("Parse failed", cause, 12, 8);

            assertThat(exception.getRawMessage()).isEqualTo("Parse failed (line: 12, column: 8)");
        }

        @Test
        @DisplayName("should set cause correctly")
        void shouldSetCauseCorrectly() {
            Throwable cause = new RuntimeException("Unexpected token");
            YmlParseException exception = new YmlParseException("Parse failed", cause, 12, 8);

            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("should store line number")
        void shouldStoreLineNumber() {
            Throwable cause = new RuntimeException("Root cause");
            YmlParseException exception = new YmlParseException("Error", cause, 100, 50);

            assertThat(exception.getLine()).isEqualTo(100);
        }

        @Test
        @DisplayName("should store column number")
        void shouldStoreColumnNumber() {
            Throwable cause = new RuntimeException("Root cause");
            YmlParseException exception = new YmlParseException("Error", cause, 100, 50);

            assertThat(exception.getColumn()).isEqualTo(50);
        }

        @Test
        @DisplayName("should have location when valid")
        void shouldHaveLocationWhenValid() {
            Throwable cause = new RuntimeException("Root cause");
            YmlParseException exception = new YmlParseException("Error", cause, 10, 20);

            assertThat(exception.hasLocation()).isTrue();
        }
    }

    @Nested
    @DisplayName("Inheritance hierarchy")
    class InheritanceTests {

        @Test
        @DisplayName("should extend OpenYmlException")
        void shouldExtendOpenYmlException() {
            YmlParseException exception = new YmlParseException("Test");

            assertThat(exception).isInstanceOf(OpenYmlException.class);
        }

        @Test
        @DisplayName("should extend OpenException")
        void shouldExtendOpenException() {
            YmlParseException exception = new YmlParseException("Test");

            assertThat(exception).isInstanceOf(OpenException.class);
        }

        @Test
        @DisplayName("should extend RuntimeException")
        void shouldExtendRuntimeException() {
            YmlParseException exception = new YmlParseException("Test");

            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should be catchable as OpenYmlException")
        void shouldBeCatchableAsOpenYmlException() {
            assertThatThrownBy(() -> {
                throw new YmlParseException("Parse error");
            }).isInstanceOf(OpenYmlException.class);
        }

        @Test
        @DisplayName("should be catchable as OpenException")
        void shouldBeCatchableAsOpenException() {
            assertThatThrownBy(() -> {
                throw new YmlParseException("Parse error");
            }).isInstanceOf(OpenException.class);
        }

        @Test
        @DisplayName("should be catchable as RuntimeException")
        void shouldBeCatchableAsRuntimeException() {
            assertThatThrownBy(() -> {
                throw new YmlParseException("Parse error");
            }).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("Typical parse error scenarios")
    class TypicalScenarioTests {

        @Test
        @DisplayName("should represent unclosed bracket error")
        void shouldRepresentUnclosedBracketError() {
            YmlParseException exception = new YmlParseException("Unclosed bracket", 5, 15);

            assertThat(exception.getMessage()).contains("Unclosed bracket");
            assertThat(exception.getMessage()).contains("line: 5");
            assertThat(exception.getMessage()).contains("column: 15");
        }

        @Test
        @DisplayName("should represent invalid indentation error")
        void shouldRepresentInvalidIndentationError() {
            YmlParseException exception = new YmlParseException("Invalid indentation", 10, 0);

            assertThat(exception.getLine()).isEqualTo(10);
            assertThat(exception.getColumn()).isEqualTo(0);
        }

        @Test
        @DisplayName("should represent unexpected end of document")
        void shouldRepresentUnexpectedEndOfDocument() {
            Throwable cause = new RuntimeException("EOF while scanning string");
            YmlParseException exception = new YmlParseException("Unexpected end of document", cause, 20, 1);

            assertThat(exception.getMessage()).contains("Unexpected end of document");
            assertThat(exception.getCause().getMessage()).contains("EOF");
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle null message")
        void shouldHandleNullMessage() {
            YmlParseException exception = new YmlParseException(null);

            assertThat(exception.getRawMessage()).isNull();
        }

        @Test
        @DisplayName("should handle empty message")
        void shouldHandleEmptyMessage() {
            YmlParseException exception = new YmlParseException("");

            assertThat(exception.getRawMessage()).isEmpty();
        }

        @Test
        @DisplayName("should handle negative location values")
        void shouldHandleNegativeLocationValues() {
            YmlParseException exception = new YmlParseException("Error", -5, -10);

            assertThat(exception.getLine()).isEqualTo(-5);
            assertThat(exception.getColumn()).isEqualTo(-10);
            assertThat(exception.hasLocation()).isFalse();
        }

        @Test
        @DisplayName("should handle large line and column numbers")
        void shouldHandleLargeLineAndColumnNumbers() {
            YmlParseException exception = new YmlParseException("Error in large file", 999999, 500);

            assertThat(exception.getLine()).isEqualTo(999999);
            assertThat(exception.getColumn()).isEqualTo(500);
            assertThat(exception.hasLocation()).isTrue();
        }
    }
}
