package cloud.opencode.base.xml.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * XmlParseExceptionTest Tests
 * XmlParseExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("XmlParseException Tests")
class XmlParseExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("constructor with message should set message")
        void constructorWithMessageShouldSetMessage() {
            XmlParseException exception = new XmlParseException("Parse error");

            assertThat(exception.getMessage()).isEqualTo("Parse error");
        }

        @Test
        @DisplayName("constructor with message and cause should set both")
        void constructorWithMessageAndCauseShouldSetBoth() {
            Throwable cause = new RuntimeException("cause");
            XmlParseException exception = new XmlParseException("Parse error", cause);

            assertThat(exception.getMessage()).isEqualTo("Parse error");
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("constructor with location should set line and column")
        void constructorWithLocationShouldSetLineAndColumn() {
            XmlParseException exception = new XmlParseException("Parse error", 10, 5);

            assertThat(exception.getLine()).isEqualTo(10);
            assertThat(exception.getColumn()).isEqualTo(5);
        }

        @Test
        @DisplayName("constructor with all parameters should set all")
        void constructorWithAllParametersShouldSetAll() {
            Throwable cause = new RuntimeException("cause");
            XmlParseException exception = new XmlParseException("Parse error", 10, 5, cause);

            assertThat(exception.getMessage()).contains("Parse error");
            assertThat(exception.getMessage()).contains("[Line 10, Column 5]");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getLine()).isEqualTo(10);
            assertThat(exception.getColumn()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("should be instance of OpenXmlException")
        void shouldBeInstanceOfOpenXmlException() {
            XmlParseException exception = new XmlParseException("error");

            assertThat(exception).isInstanceOf(OpenXmlException.class);
        }

        @Test
        @DisplayName("should be instance of RuntimeException")
        void shouldBeInstanceOfRuntimeException() {
            XmlParseException exception = new XmlParseException("error");

            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
    }
}
