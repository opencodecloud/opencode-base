package cloud.opencode.base.xml.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * XmlSecurityExceptionTest Tests
 * XmlSecurityExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("XmlSecurityException Tests")
class XmlSecurityExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("constructor with type and message should set both")
        void constructorWithTypeAndMessageShouldSetBoth() {
            XmlSecurityException exception = new XmlSecurityException(
                XmlSecurityException.SecurityViolationType.XXE_DETECTED,
                "Security error"
            );

            assertThat(exception.getMessage()).contains("Security error");
            assertThat(exception.getType()).isEqualTo(
                XmlSecurityException.SecurityViolationType.XXE_DETECTED
            );
        }

        @Test
        @DisplayName("constructor with type, message and cause should set all")
        void constructorWithTypeMessageAndCauseShouldSetAll() {
            Throwable cause = new RuntimeException("cause");
            XmlSecurityException exception = new XmlSecurityException(
                XmlSecurityException.SecurityViolationType.ENTITY_EXPANSION_LIMIT,
                "Security error",
                cause
            );

            assertThat(exception.getMessage()).contains("Security error");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getType()).isEqualTo(
                XmlSecurityException.SecurityViolationType.ENTITY_EXPANSION_LIMIT
            );
        }
    }

    @Nested
    @DisplayName("SecurityViolationType Enum Tests")
    class SecurityViolationTypeEnumTests {

        @Test
        @DisplayName("enum should have all expected values")
        void enumShouldHaveAllExpectedValues() {
            XmlSecurityException.SecurityViolationType[] types =
                XmlSecurityException.SecurityViolationType.values();

            assertThat(types).contains(
                XmlSecurityException.SecurityViolationType.XXE_DETECTED,
                XmlSecurityException.SecurityViolationType.ENTITY_EXPANSION_LIMIT,
                XmlSecurityException.SecurityViolationType.DTD_PROHIBITED,
                XmlSecurityException.SecurityViolationType.EXTERNAL_PARAMETER_ENTITY
            );
        }

        @Test
        @DisplayName("valueOf should return correct enum value")
        void valueOfShouldReturnCorrectEnumValue() {
            assertThat(XmlSecurityException.SecurityViolationType.valueOf("XXE_DETECTED"))
                .isEqualTo(XmlSecurityException.SecurityViolationType.XXE_DETECTED);
            assertThat(XmlSecurityException.SecurityViolationType.valueOf("ENTITY_EXPANSION_LIMIT"))
                .isEqualTo(XmlSecurityException.SecurityViolationType.ENTITY_EXPANSION_LIMIT);
            assertThat(XmlSecurityException.SecurityViolationType.valueOf("DTD_PROHIBITED"))
                .isEqualTo(XmlSecurityException.SecurityViolationType.DTD_PROHIBITED);
            assertThat(XmlSecurityException.SecurityViolationType.valueOf("EXTERNAL_PARAMETER_ENTITY"))
                .isEqualTo(XmlSecurityException.SecurityViolationType.EXTERNAL_PARAMETER_ENTITY);
        }
    }

    @Nested
    @DisplayName("Type Tests")
    class TypeTests {

        @Test
        @DisplayName("getType should return type")
        void getTypeShouldReturnType() {
            XmlSecurityException exception = new XmlSecurityException(
                XmlSecurityException.SecurityViolationType.DTD_PROHIBITED,
                "error"
            );

            assertThat(exception.getType())
                .isEqualTo(XmlSecurityException.SecurityViolationType.DTD_PROHIBITED);
        }

        @Test
        @DisplayName("message should include type")
        void messageShouldIncludeType() {
            XmlSecurityException exception = new XmlSecurityException(
                XmlSecurityException.SecurityViolationType.XXE_DETECTED,
                "Test error"
            );

            assertThat(exception.getMessage()).contains("XXE_DETECTED");
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("should be instance of OpenXmlException")
        void shouldBeInstanceOfOpenXmlException() {
            XmlSecurityException exception = new XmlSecurityException(
                XmlSecurityException.SecurityViolationType.XXE_DETECTED,
                "error"
            );

            assertThat(exception).isInstanceOf(OpenXmlException.class);
        }

        @Test
        @DisplayName("should be instance of RuntimeException")
        void shouldBeInstanceOfRuntimeException() {
            XmlSecurityException exception = new XmlSecurityException(
                XmlSecurityException.SecurityViolationType.XXE_DETECTED,
                "error"
            );

            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
    }
}
