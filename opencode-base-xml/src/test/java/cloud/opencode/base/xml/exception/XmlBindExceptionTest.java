package cloud.opencode.base.xml.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * XmlBindExceptionTest Tests
 * XmlBindExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.3
 */
@DisplayName("XmlBindException Tests")
class XmlBindExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("constructor with message should set message")
        void constructorWithMessageShouldSetMessage() {
            XmlBindException exception = new XmlBindException("Bind error");

            assertThat(exception.getMessage()).contains("Bind error");
        }

        @Test
        @DisplayName("constructor with message and cause should set both")
        void constructorWithMessageAndCauseShouldSetBoth() {
            Throwable cause = new RuntimeException("cause");
            XmlBindException exception = new XmlBindException("Bind error", cause);

            assertThat(exception.getMessage()).contains("Bind error");
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("constructor with class should include class name")
        void constructorWithClassShouldIncludeClassName() {
            XmlBindException exception = new XmlBindException(String.class, "Bind error");

            assertThat(exception.getMessage()).contains("String");
            assertThat(exception.getTargetType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("constructor with class and cause should set all")
        void constructorWithClassAndCauseShouldSetAll() {
            Throwable cause = new RuntimeException("cause");
            XmlBindException exception = new XmlBindException(String.class, "Bind error", cause);

            assertThat(exception.getMessage()).contains("String");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getTargetType()).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("Target Type Tests")
    class TargetTypeTests {

        @Test
        @DisplayName("getTargetType should return null when not set")
        void getTargetTypeShouldReturnNullWhenNotSet() {
            XmlBindException exception = new XmlBindException("error");

            assertThat(exception.getTargetType()).isNull();
        }

        @Test
        @DisplayName("getTargetType should return type when set")
        void getTargetTypeShouldReturnTypeWhenSet() {
            XmlBindException exception = new XmlBindException(Integer.class, "error");

            assertThat(exception.getTargetType()).isEqualTo(Integer.class);
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("should be instance of OpenXmlException")
        void shouldBeInstanceOfOpenXmlException() {
            XmlBindException exception = new XmlBindException("error");

            assertThat(exception).isInstanceOf(OpenXmlException.class);
        }

        @Test
        @DisplayName("should be instance of OpenException")
        void shouldBeInstanceOfOpenException() {
            XmlBindException exception = new XmlBindException("error");

            assertThat(exception).isInstanceOf(OpenException.class);
        }
    }
}
