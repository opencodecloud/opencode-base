package cloud.opencode.base.xml.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * XmlTransformExceptionTest Tests
 * XmlTransformExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("XmlTransformException Tests")
class XmlTransformExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("constructor with message should set message")
        void constructorWithMessageShouldSetMessage() {
            XmlTransformException exception = new XmlTransformException("Transform error");

            assertThat(exception.getMessage()).isEqualTo("Transform error");
        }

        @Test
        @DisplayName("constructor with cause should set cause")
        void constructorWithCauseShouldSetCause() {
            Throwable cause = new RuntimeException("cause");
            XmlTransformException exception = new XmlTransformException(cause);

            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getMessage()).contains("transformation failed");
        }

        @Test
        @DisplayName("constructor with message and cause should set both")
        void constructorWithMessageAndCauseShouldSetBoth() {
            Throwable cause = new RuntimeException("cause");
            XmlTransformException exception = new XmlTransformException("Transform error", cause);

            assertThat(exception.getMessage()).isEqualTo("Transform error");
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("should be instance of OpenXmlException")
        void shouldBeInstanceOfOpenXmlException() {
            XmlTransformException exception = new XmlTransformException("error");

            assertThat(exception).isInstanceOf(OpenXmlException.class);
        }

        @Test
        @DisplayName("should be instance of RuntimeException")
        void shouldBeInstanceOfRuntimeException() {
            XmlTransformException exception = new XmlTransformException("error");

            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
    }
}
