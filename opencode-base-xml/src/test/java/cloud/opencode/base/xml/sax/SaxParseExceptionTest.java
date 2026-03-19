package cloud.opencode.base.xml.sax;

import cloud.opencode.base.xml.exception.XmlParseException;
import org.junit.jupiter.api.*;
import org.xml.sax.SAXParseException;

import static org.assertj.core.api.Assertions.*;

/**
 * SaxParseExceptionTest Tests
 * SaxParseExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("SaxParseException Tests")
class SaxParseExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("constructor with message and location should set all")
        void constructorWithMessageAndLocationShouldSetAll() {
            SaxParseException exception = new SaxParseException("Parse error", 10, 5);

            assertThat(exception.getMessage()).contains("Parse error");
            assertThat(exception.getMessage()).contains("[Line 10, Column 5]");
            assertThat(exception.getLine()).isEqualTo(10);
            assertThat(exception.getColumn()).isEqualTo(5);
        }

        @Test
        @DisplayName("constructor with message location and cause should set all")
        void constructorWithMessageLocationAndCauseShouldSetAll() {
            Throwable cause = new RuntimeException("cause");
            SaxParseException exception = new SaxParseException("Parse error", 10, 5, cause);

            assertThat(exception.getMessage()).contains("Parse error");
            assertThat(exception.getMessage()).contains("[Line 10, Column 5]");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getLine()).isEqualTo(10);
            assertThat(exception.getColumn()).isEqualTo(5);
        }

        @Test
        @DisplayName("constructor with SAXParseException should extract location")
        void constructorWithSaxParseExceptionShouldExtractLocation() {
            SAXParseException saxException = new SAXParseException("SAX error", null, null, 10, 5);
            SaxParseException exception = new SaxParseException(saxException);

            assertThat(exception.getLine()).isEqualTo(10);
            assertThat(exception.getColumn()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Location Tests")
    class LocationTests {

        @Test
        @DisplayName("constructor with location should set line and column")
        void constructorWithLocationShouldSetLineAndColumn() {
            SaxParseException exception = new SaxParseException("Error", 15, 20);

            assertThat(exception.getLine()).isEqualTo(15);
            assertThat(exception.getColumn()).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("should be instance of XmlParseException")
        void shouldBeInstanceOfXmlParseException() {
            SaxParseException exception = new SaxParseException("error", 1, 1);

            assertThat(exception).isInstanceOf(XmlParseException.class);
        }
    }
}
