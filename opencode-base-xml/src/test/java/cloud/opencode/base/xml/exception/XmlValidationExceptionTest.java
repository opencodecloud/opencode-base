package cloud.opencode.base.xml.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * XmlValidationExceptionTest Tests
 * XmlValidationExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.3
 */
@DisplayName("XmlValidationException Tests")
class XmlValidationExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("constructor with message should set message")
        void constructorWithMessageShouldSetMessage() {
            XmlValidationException exception = new XmlValidationException("Validation error");

            assertThat(exception.getMessage()).contains("Validation error");
        }

        @Test
        @DisplayName("constructor with message and cause should set both")
        void constructorWithMessageAndCauseShouldSetBoth() {
            Throwable cause = new RuntimeException("cause");
            XmlValidationException exception = new XmlValidationException("Validation error", cause);

            assertThat(exception.getMessage()).contains("Validation error");
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("constructor with error list should store errors")
        void constructorWithErrorListShouldStoreErrors() {
            List<String> errors = List.of("Error 1", "Error 2");
            XmlValidationException exception = new XmlValidationException(errors);

            assertThat(exception.getErrors()).hasSize(2);
            assertThat(exception.getErrors()).containsExactly("Error 1", "Error 2");
        }

        @Test
        @DisplayName("constructor with message and error list should store both")
        void constructorWithMessageAndErrorListShouldStoreBoth() {
            List<String> errors = List.of("Error 1");
            XmlValidationException exception = new XmlValidationException("Validation failed", errors);

            assertThat(exception.getMessage()).contains("Validation failed");
            assertThat(exception.getErrors()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Error Access Tests")
    class ErrorAccessTests {

        @Test
        @DisplayName("getErrors should return error list")
        void getErrorsShouldReturnErrorList() {
            List<String> errors = List.of("Error 1", "Error 2");
            XmlValidationException exception = new XmlValidationException(errors);

            assertThat(exception.getErrors()).hasSize(2);
        }

        @Test
        @DisplayName("hasErrors should return true when errors present")
        void hasErrorsShouldReturnTrueWhenErrorsPresent() {
            XmlValidationException exception = new XmlValidationException("error");

            assertThat(exception.hasErrors()).isTrue();
        }

        @Test
        @DisplayName("getErrors should return immutable list")
        void getErrorsShouldReturnImmutableList() {
            List<String> errors = List.of("Error 1");
            XmlValidationException exception = new XmlValidationException(errors);

            assertThatThrownBy(() -> exception.getErrors().add("New error"))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("should be instance of OpenXmlException")
        void shouldBeInstanceOfOpenXmlException() {
            XmlValidationException exception = new XmlValidationException("error");

            assertThat(exception).isInstanceOf(OpenXmlException.class);
        }

        @Test
        @DisplayName("should be instance of OpenException")
        void shouldBeInstanceOfOpenException() {
            XmlValidationException exception = new XmlValidationException("error");

            assertThat(exception).isInstanceOf(OpenException.class);
        }
    }
}
