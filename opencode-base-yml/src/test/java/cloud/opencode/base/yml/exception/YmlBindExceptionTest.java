package cloud.opencode.base.yml.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * YmlBindExceptionTest Tests
 * YmlBindExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("YmlBindException Tests")
class YmlBindExceptionTest {

    @Nested
    @DisplayName("Constructor with message only")
    class MessageOnlyConstructorTests {

        @Test
        @DisplayName("should set message correctly")
        void shouldSetMessageCorrectly() {
            YmlBindException exception = new YmlBindException("Binding failed");

            assertThat(exception.getMessage()).contains("Binding failed");
        }

        @Test
        @DisplayName("should set path to null")
        void shouldSetPathToNull() {
            YmlBindException exception = new YmlBindException("Binding failed");

            assertThat(exception.getPath()).isNull();
        }

        @Test
        @DisplayName("should set targetType to null")
        void shouldSetTargetTypeToNull() {
            YmlBindException exception = new YmlBindException("Binding failed");

            assertThat(exception.getTargetType()).isNull();
        }

        @Test
        @DisplayName("should have no cause")
        void shouldHaveNoCause() {
            YmlBindException exception = new YmlBindException("Binding failed");

            assertThat(exception.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("Constructor with path, targetType and cause")
    class PathTargetTypeCauseConstructorTests {

        @Test
        @DisplayName("should format message with path and type")
        void shouldFormatMessageWithPathAndType() {
            Throwable cause = new NumberFormatException("Invalid number");
            YmlBindException exception = new YmlBindException("server.port", Integer.class, cause);

            assertThat(exception.getMessage()).contains("Failed to bind 'server.port' to java.lang.Integer");
        }

        @Test
        @DisplayName("should store path")
        void shouldStorePath() {
            Throwable cause = new RuntimeException();
            YmlBindException exception = new YmlBindException("database.url", String.class, cause);

            assertThat(exception.getPath()).isEqualTo("database.url");
        }

        @Test
        @DisplayName("should store targetType")
        void shouldStoreTargetType() {
            Throwable cause = new RuntimeException();
            YmlBindException exception = new YmlBindException("config.timeout", Long.class, cause);

            assertThat(exception.getTargetType()).isEqualTo(Long.class);
        }

        @Test
        @DisplayName("should store cause")
        void shouldStoreCause() {
            Throwable cause = new IllegalArgumentException("Invalid value");
            YmlBindException exception = new YmlBindException("path", String.class, cause);

            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("should handle custom class types")
        void shouldHandleCustomClassTypes() {
            Throwable cause = new RuntimeException();
            YmlBindException exception = new YmlBindException("app.config", TestConfigClass.class, cause);

            assertThat(exception.getMessage()).contains("TestConfigClass");
            assertThat(exception.getTargetType()).isEqualTo(TestConfigClass.class);
        }
    }

    @Nested
    @DisplayName("Constructor with message and cause")
    class MessageAndCauseConstructorTests {

        @Test
        @DisplayName("should set message correctly")
        void shouldSetMessageCorrectly() {
            Throwable cause = new RuntimeException("Root cause");
            YmlBindException exception = new YmlBindException("Custom bind error", cause);

            assertThat(exception.getMessage()).contains("Custom bind error");
        }

        @Test
        @DisplayName("should set cause correctly")
        void shouldSetCauseCorrectly() {
            Throwable cause = new RuntimeException("Root cause");
            YmlBindException exception = new YmlBindException("Custom bind error", cause);

            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("should set path to null")
        void shouldSetPathToNull() {
            Throwable cause = new RuntimeException();
            YmlBindException exception = new YmlBindException("Error", cause);

            assertThat(exception.getPath()).isNull();
        }

        @Test
        @DisplayName("should set targetType to null")
        void shouldSetTargetTypeToNull() {
            Throwable cause = new RuntimeException();
            YmlBindException exception = new YmlBindException("Error", cause);

            assertThat(exception.getTargetType()).isNull();
        }
    }

    @Nested
    @DisplayName("Constructor for required field (path and targetType)")
    class RequiredFieldConstructorTests {

        @Test
        @DisplayName("should format message for missing required property")
        void shouldFormatMessageForMissingRequiredProperty() {
            YmlBindException exception = new YmlBindException("server.host", String.class);

            assertThat(exception.getMessage())
                .contains("Required property 'server.host' is missing for type java.lang.String");
        }

        @Test
        @DisplayName("should store path")
        void shouldStorePath() {
            YmlBindException exception = new YmlBindException("database.password", String.class);

            assertThat(exception.getPath()).isEqualTo("database.password");
        }

        @Test
        @DisplayName("should store targetType")
        void shouldStoreTargetType() {
            YmlBindException exception = new YmlBindException("config.value", Double.class);

            assertThat(exception.getTargetType()).isEqualTo(Double.class);
        }

        @Test
        @DisplayName("should have no cause")
        void shouldHaveNoCause() {
            YmlBindException exception = new YmlBindException("path", String.class);

            assertThat(exception.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("getPath method")
    class GetPathTests {

        @Test
        @DisplayName("should return null for message-only constructor")
        void shouldReturnNullForMessageOnlyConstructor() {
            YmlBindException exception = new YmlBindException("Error");

            assertThat(exception.getPath()).isNull();
        }

        @Test
        @DisplayName("should return path for required field constructor")
        void shouldReturnPathForRequiredFieldConstructor() {
            YmlBindException exception = new YmlBindException("app.name", String.class);

            assertThat(exception.getPath()).isEqualTo("app.name");
        }

        @Test
        @DisplayName("should return path for full constructor")
        void shouldReturnPathForFullConstructor() {
            YmlBindException exception = new YmlBindException("config.setting", Integer.class, new RuntimeException());

            assertThat(exception.getPath()).isEqualTo("config.setting");
        }
    }

    @Nested
    @DisplayName("getTargetType method")
    class GetTargetTypeTests {

        @Test
        @DisplayName("should return null for message-only constructor")
        void shouldReturnNullForMessageOnlyConstructor() {
            YmlBindException exception = new YmlBindException("Error");

            assertThat(exception.getTargetType()).isNull();
        }

        @Test
        @DisplayName("should return type for required field constructor")
        void shouldReturnTypeForRequiredFieldConstructor() {
            YmlBindException exception = new YmlBindException("path", Boolean.class);

            assertThat(exception.getTargetType()).isEqualTo(Boolean.class);
        }

        @Test
        @DisplayName("should return type for full constructor")
        void shouldReturnTypeForFullConstructor() {
            YmlBindException exception = new YmlBindException("path", Float.class, new RuntimeException());

            assertThat(exception.getTargetType()).isEqualTo(Float.class);
        }

        @Test
        @DisplayName("should handle primitive wrapper types")
        void shouldHandlePrimitiveWrapperTypes() {
            YmlBindException exception = new YmlBindException("count", int.class);

            assertThat(exception.getTargetType()).isEqualTo(int.class);
        }
    }

    @Nested
    @DisplayName("Inheritance hierarchy")
    class InheritanceTests {

        @Test
        @DisplayName("should extend OpenYmlException")
        void shouldExtendOpenYmlException() {
            YmlBindException exception = new YmlBindException("Test");

            assertThat(exception).isInstanceOf(OpenYmlException.class);
        }

        @Test
        @DisplayName("should extend RuntimeException")
        void shouldExtendRuntimeException() {
            YmlBindException exception = new YmlBindException("Test");

            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should be catchable as OpenYmlException")
        void shouldBeCatchableAsOpenYmlException() {
            assertThatThrownBy(() -> {
                throw new YmlBindException("Bind error");
            }).isInstanceOf(OpenYmlException.class);
        }

        @Test
        @DisplayName("should inherit getLine method returning -1")
        void shouldInheritGetLineMethodReturningNegativeOne() {
            YmlBindException exception = new YmlBindException("Error");

            assertThat(exception.getLine()).isEqualTo(-1);
        }

        @Test
        @DisplayName("should inherit getColumn method returning -1")
        void shouldInheritGetColumnMethodReturningNegativeOne() {
            YmlBindException exception = new YmlBindException("Error");

            assertThat(exception.getColumn()).isEqualTo(-1);
        }

        @Test
        @DisplayName("should inherit hasLocation returning false")
        void shouldInheritHasLocationReturningFalse() {
            YmlBindException exception = new YmlBindException("Error");

            assertThat(exception.hasLocation()).isFalse();
        }
    }

    @Nested
    @DisplayName("Typical binding scenarios")
    class TypicalScenarioTests {

        @Test
        @DisplayName("should represent type conversion failure")
        void shouldRepresentTypeConversionFailure() {
            NumberFormatException cause = new NumberFormatException("For input string: \"abc\"");
            YmlBindException exception = new YmlBindException("server.port", Integer.class, cause);

            assertThat(exception.getPath()).isEqualTo("server.port");
            assertThat(exception.getTargetType()).isEqualTo(Integer.class);
            assertThat(exception.getCause()).isInstanceOf(NumberFormatException.class);
        }

        @Test
        @DisplayName("should represent missing required field")
        void shouldRepresentMissingRequiredField() {
            YmlBindException exception = new YmlBindException("database.url", String.class);

            assertThat(exception.getMessage()).contains("Required");
            assertThat(exception.getMessage()).contains("database.url");
        }

        @Test
        @DisplayName("should represent nested property binding failure")
        void shouldRepresentNestedPropertyBindingFailure() {
            YmlBindException exception = new YmlBindException("app.server.ssl.enabled", Boolean.class,
                new IllegalArgumentException("Cannot parse"));

            assertThat(exception.getPath()).isEqualTo("app.server.ssl.enabled");
            assertThat(exception.getTargetType()).isEqualTo(Boolean.class);
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle null message")
        void shouldHandleNullMessage() {
            YmlBindException exception = new YmlBindException(null);

            // getMessage() includes [yml] prefix even with null raw message
            assertThat(exception.getMessage()).isNotNull();
        }

        @Test
        @DisplayName("should handle empty path")
        void shouldHandleEmptyPath() {
            YmlBindException exception = new YmlBindException("", String.class);

            assertThat(exception.getPath()).isEmpty();
        }

        @Test
        @DisplayName("should handle path with special characters")
        void shouldHandlePathWithSpecialCharacters() {
            YmlBindException exception = new YmlBindException("config[0].name", String.class);

            assertThat(exception.getPath()).isEqualTo("config[0].name");
        }

        @Test
        @DisplayName("should handle array types")
        void shouldHandleArrayTypes() {
            YmlBindException exception = new YmlBindException("values", String[].class);

            assertThat(exception.getTargetType()).isEqualTo(String[].class);
            // Class.getName() returns "[Ljava.lang.String;" for array types
            assertThat(exception.getMessage()).contains("[Ljava.lang.String;");
        }

        @Test
        @DisplayName("should handle interface types")
        void shouldHandleInterfaceTypes() {
            YmlBindException exception = new YmlBindException("list", java.util.List.class);

            assertThat(exception.getTargetType()).isEqualTo(java.util.List.class);
        }
    }

    // Helper class for testing
    private static class TestConfigClass {
        public String name;
        public int value;
    }
}
