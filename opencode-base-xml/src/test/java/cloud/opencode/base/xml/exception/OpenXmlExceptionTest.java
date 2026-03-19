package cloud.opencode.base.xml.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenXmlExceptionTest Tests
 * OpenXmlExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("OpenXmlException Tests")
class OpenXmlExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("constructor with message should set message")
        void constructorWithMessageShouldSetMessage() {
            OpenXmlException exception = new OpenXmlException("Test message");

            assertThat(exception.getMessage()).isEqualTo("Test message");
        }

        @Test
        @DisplayName("constructor with message and cause should set both")
        void constructorWithMessageAndCauseShouldSetBoth() {
            Throwable cause = new RuntimeException("cause");
            OpenXmlException exception = new OpenXmlException("Test message", cause);

            assertThat(exception.getMessage()).isEqualTo("Test message");
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("constructor with cause only should set cause")
        void constructorWithCauseOnlyShouldSetCause() {
            Throwable cause = new RuntimeException("cause");
            OpenXmlException exception = new OpenXmlException(cause);

            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("constructor with message, line and column should set location")
        void constructorWithMessageLineAndColumnShouldSetLocation() {
            OpenXmlException exception = new OpenXmlException("Test message", 10, 5);

            assertThat(exception.getMessage()).isEqualTo("[Line 10, Column 5] Test message");
            assertThat(exception.getLine()).isEqualTo(10);
            assertThat(exception.getColumn()).isEqualTo(5);
        }

        @Test
        @DisplayName("constructor with all parameters should set all")
        void constructorWithAllParametersShouldSetAll() {
            Throwable cause = new RuntimeException("cause");
            OpenXmlException exception = new OpenXmlException("Test message", 10, 5, cause);

            assertThat(exception.getMessage()).isEqualTo("[Line 10, Column 5] Test message");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getLine()).isEqualTo(10);
            assertThat(exception.getColumn()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Location Tests")
    class LocationTests {

        @Test
        @DisplayName("getLine should return -1 when not set")
        void getLineShouldReturnMinusOneWhenNotSet() {
            OpenXmlException exception = new OpenXmlException("message");

            assertThat(exception.getLine()).isEqualTo(-1);
        }

        @Test
        @DisplayName("getColumn should return -1 when not set")
        void getColumnShouldReturnMinusOneWhenNotSet() {
            OpenXmlException exception = new OpenXmlException("message");

            assertThat(exception.getColumn()).isEqualTo(-1);
        }

        @Test
        @DisplayName("hasLocation should return true when location is set")
        void hasLocationShouldReturnTrueWhenLocationIsSet() {
            OpenXmlException exception = new OpenXmlException("message", 10, 5);

            assertThat(exception.hasLocation()).isTrue();
        }

        @Test
        @DisplayName("hasLocation should return false when location is not set")
        void hasLocationShouldReturnFalseWhenLocationIsNotSet() {
            OpenXmlException exception = new OpenXmlException("message");

            assertThat(exception.hasLocation()).isFalse();
        }

        @Test
        @DisplayName("hasLocation should return false when line is negative")
        void hasLocationShouldReturnFalseWhenLineIsNegative() {
            OpenXmlException exception = new OpenXmlException("message");

            assertThat(exception.getLine()).isEqualTo(-1);
            assertThat(exception.getColumn()).isEqualTo(-1);
            assertThat(exception.hasLocation()).isFalse();
        }

        @Test
        @DisplayName("message should include location when set")
        void messageShouldIncludeLocationWhenSet() {
            OpenXmlException exception = new OpenXmlException("Parse error", 10, 5);

            assertThat(exception.getMessage()).contains("Line 10");
            assertThat(exception.getMessage()).contains("Column 5");
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("parseError should create exception with location")
        void parseErrorShouldCreateExceptionWithLocation() {
            OpenXmlException exception = OpenXmlException.parseError("Parse failed", 10, 5);

            assertThat(exception).isInstanceOf(XmlParseException.class);
            assertThat(exception.getLine()).isEqualTo(10);
            assertThat(exception.getColumn()).isEqualTo(5);
            assertThat(exception.getMessage()).contains("Parse failed");
        }

        @Test
        @DisplayName("xpathError should create exception with xpath and cause")
        void xpathErrorShouldCreateExceptionWithXpathAndCause() {
            Throwable cause = new RuntimeException("cause");
            OpenXmlException exception = OpenXmlException.xpathError("//invalid", cause);

            assertThat(exception).isInstanceOf(XmlXPathException.class);
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getMessage()).contains("//invalid");
        }

        @Test
        @DisplayName("bindError should create exception with type and cause")
        void bindErrorShouldCreateExceptionWithTypeAndCause() {
            Throwable cause = new RuntimeException("cause");
            OpenXmlException exception = OpenXmlException.bindError(String.class, cause);

            assertThat(exception).isInstanceOf(XmlBindException.class);
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getMessage()).contains("String");
        }

        @Test
        @DisplayName("validationError should create exception with message")
        void validationErrorShouldCreateExceptionWithMessage() {
            OpenXmlException exception = OpenXmlException.validationError("Validation failed");

            assertThat(exception).isInstanceOf(XmlValidationException.class);
            assertThat(exception.getMessage()).isEqualTo("Validation failed");
        }

        @Test
        @DisplayName("transformError should create exception with cause")
        void transformErrorShouldCreateExceptionWithCause() {
            Throwable cause = new RuntimeException("transform cause");
            OpenXmlException exception = OpenXmlException.transformError(cause);

            assertThat(exception).isInstanceOf(XmlTransformException.class);
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getMessage()).contains("transformation failed");
        }
    }
}
