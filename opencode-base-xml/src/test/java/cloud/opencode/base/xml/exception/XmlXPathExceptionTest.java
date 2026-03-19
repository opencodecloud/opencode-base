package cloud.opencode.base.xml.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * XmlXPathExceptionTest Tests
 * XmlXPathExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("XmlXPathException Tests")
class XmlXPathExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("constructor with xpath and message should set both")
        void constructorWithXpathAndMessageShouldSetBoth() {
            XmlXPathException exception = new XmlXPathException("/root/child", "Invalid XPath");

            assertThat(exception.getMessage()).contains("Invalid XPath");
            assertThat(exception.getXPath()).isEqualTo("/root/child");
        }

        @Test
        @DisplayName("constructor with xpath and cause should set both")
        void constructorWithXpathAndCauseShouldSetBoth() {
            Throwable cause = new RuntimeException("cause");
            XmlXPathException exception = new XmlXPathException("/root/child", cause);

            assertThat(exception.getMessage()).contains("/root/child");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getXPath()).isEqualTo("/root/child");
        }
    }

    @Nested
    @DisplayName("XPath Tests")
    class XPathTests {

        @Test
        @DisplayName("getXPath should return xpath expression")
        void getXPathShouldReturnXpathExpression() {
            XmlXPathException exception = new XmlXPathException("//item[@id]", "error");

            assertThat(exception.getXPath()).isEqualTo("//item[@id]");
        }

        @Test
        @DisplayName("message should include xpath expression")
        void messageShouldIncludeXpathExpression() {
            XmlXPathException exception = new XmlXPathException("/root/child", "Syntax error");

            assertThat(exception.getMessage()).contains("/root/child");
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("should be instance of OpenXmlException")
        void shouldBeInstanceOfOpenXmlException() {
            XmlXPathException exception = new XmlXPathException("/path", "error");

            assertThat(exception).isInstanceOf(OpenXmlException.class);
        }

        @Test
        @DisplayName("should be instance of RuntimeException")
        void shouldBeInstanceOfRuntimeException() {
            XmlXPathException exception = new XmlXPathException("/path", "error");

            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
    }
}
