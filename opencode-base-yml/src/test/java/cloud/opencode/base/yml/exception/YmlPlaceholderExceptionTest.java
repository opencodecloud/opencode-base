package cloud.opencode.base.yml.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * YmlPlaceholderExceptionTest Tests
 * YmlPlaceholderExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("YmlPlaceholderException Tests")
class YmlPlaceholderExceptionTest {

    @Nested
    @DisplayName("Constructor with placeholder only (unresolved)")
    class PlaceholderOnlyConstructorTests {

        @Test
        @DisplayName("should format message for unresolved placeholder")
        void shouldFormatMessageForUnresolvedPlaceholder() {
            YmlPlaceholderException exception = new YmlPlaceholderException("${app.name}");

            assertThat(exception.getMessage()).isEqualTo("Cannot resolve placeholder: ${app.name}");
        }

        @Test
        @DisplayName("should store placeholder")
        void shouldStorePlaceholder() {
            YmlPlaceholderException exception = new YmlPlaceholderException("${database.url}");

            assertThat(exception.getPlaceholder()).isEqualTo("${database.url}");
        }

        @Test
        @DisplayName("should have no cause")
        void shouldHaveNoCause() {
            YmlPlaceholderException exception = new YmlPlaceholderException("${value}");

            assertThat(exception.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("Constructor with placeholder and message")
    class PlaceholderAndMessageConstructorTests {

        @Test
        @DisplayName("should set custom message")
        void shouldSetCustomMessage() {
            YmlPlaceholderException exception = new YmlPlaceholderException("${env.var}", "Environment variable not set");

            assertThat(exception.getMessage()).isEqualTo("Environment variable not set");
        }

        @Test
        @DisplayName("should store placeholder")
        void shouldStorePlaceholder() {
            YmlPlaceholderException exception = new YmlPlaceholderException("${config.key}", "Custom error");

            assertThat(exception.getPlaceholder()).isEqualTo("${config.key}");
        }

        @Test
        @DisplayName("should have no cause")
        void shouldHaveNoCause() {
            YmlPlaceholderException exception = new YmlPlaceholderException("${placeholder}", "Error");

            assertThat(exception.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("Constructor with placeholder, message and cause")
    class FullConstructorTests {

        @Test
        @DisplayName("should set message correctly")
        void shouldSetMessageCorrectly() {
            Throwable cause = new RuntimeException("Root cause");
            YmlPlaceholderException exception = new YmlPlaceholderException("${value}", "Resolution failed", cause);

            assertThat(exception.getMessage()).isEqualTo("Resolution failed");
        }

        @Test
        @DisplayName("should store placeholder")
        void shouldStorePlaceholder() {
            Throwable cause = new RuntimeException();
            YmlPlaceholderException exception = new YmlPlaceholderException("${app.setting}", "Error", cause);

            assertThat(exception.getPlaceholder()).isEqualTo("${app.setting}");
        }

        @Test
        @DisplayName("should store cause")
        void shouldStoreCause() {
            Throwable cause = new IllegalStateException("Invalid state");
            YmlPlaceholderException exception = new YmlPlaceholderException("${key}", "Error", cause);

            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("getPlaceholder method")
    class GetPlaceholderTests {

        @Test
        @DisplayName("should return placeholder from placeholder-only constructor")
        void shouldReturnPlaceholderFromPlaceholderOnlyConstructor() {
            YmlPlaceholderException exception = new YmlPlaceholderException("${server.port}");

            assertThat(exception.getPlaceholder()).isEqualTo("${server.port}");
        }

        @Test
        @DisplayName("should return placeholder from placeholder-message constructor")
        void shouldReturnPlaceholderFromPlaceholderMessageConstructor() {
            YmlPlaceholderException exception = new YmlPlaceholderException("${db.host}", "DB host not defined");

            assertThat(exception.getPlaceholder()).isEqualTo("${db.host}");
        }

        @Test
        @DisplayName("should return placeholder from full constructor")
        void shouldReturnPlaceholderFromFullConstructor() {
            YmlPlaceholderException exception = new YmlPlaceholderException("${config}", "Error", new RuntimeException());

            assertThat(exception.getPlaceholder()).isEqualTo("${config}");
        }

        @Test
        @DisplayName("should handle nested placeholder syntax")
        void shouldHandleNestedPlaceholderSyntax() {
            YmlPlaceholderException exception = new YmlPlaceholderException("${${env}.value}");

            assertThat(exception.getPlaceholder()).isEqualTo("${${env}.value}");
        }
    }

    @Nested
    @DisplayName("circularReference factory method")
    class CircularReferenceFactoryTests {

        @Test
        @DisplayName("should create exception with circular reference message")
        void shouldCreateExceptionWithCircularReferenceMessage() {
            YmlPlaceholderException exception = YmlPlaceholderException.circularReference("${self.ref}");

            assertThat(exception.getMessage())
                .isEqualTo("Circular reference detected in placeholder: ${self.ref}");
        }

        @Test
        @DisplayName("should store placeholder")
        void shouldStorePlaceholder() {
            YmlPlaceholderException exception = YmlPlaceholderException.circularReference("${loop.a}");

            assertThat(exception.getPlaceholder()).isEqualTo("${loop.a}");
        }

        @Test
        @DisplayName("should have no cause")
        void shouldHaveNoCause() {
            YmlPlaceholderException exception = YmlPlaceholderException.circularReference("${circular}");

            assertThat(exception.getCause()).isNull();
        }

        @Test
        @DisplayName("should include placeholder in message")
        void shouldIncludePlaceholderInMessage() {
            YmlPlaceholderException exception = YmlPlaceholderException.circularReference("${config.recursive}");

            assertThat(exception.getMessage()).contains("${config.recursive}");
            assertThat(exception.getMessage()).contains("Circular reference");
        }
    }

    @Nested
    @DisplayName("Inheritance hierarchy")
    class InheritanceTests {

        @Test
        @DisplayName("should extend OpenYmlException")
        void shouldExtendOpenYmlException() {
            YmlPlaceholderException exception = new YmlPlaceholderException("${placeholder}");

            assertThat(exception).isInstanceOf(OpenYmlException.class);
        }

        @Test
        @DisplayName("should extend RuntimeException")
        void shouldExtendRuntimeException() {
            YmlPlaceholderException exception = new YmlPlaceholderException("${placeholder}");

            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should be catchable as OpenYmlException")
        void shouldBeCatchableAsOpenYmlException() {
            assertThatThrownBy(() -> {
                throw new YmlPlaceholderException("${unresolved}");
            }).isInstanceOf(OpenYmlException.class);
        }

        @Test
        @DisplayName("should inherit getLine method returning -1")
        void shouldInheritGetLineMethodReturningNegativeOne() {
            YmlPlaceholderException exception = new YmlPlaceholderException("${placeholder}");

            assertThat(exception.getLine()).isEqualTo(-1);
        }

        @Test
        @DisplayName("should inherit getColumn method returning -1")
        void shouldInheritGetColumnMethodReturningNegativeOne() {
            YmlPlaceholderException exception = new YmlPlaceholderException("${placeholder}");

            assertThat(exception.getColumn()).isEqualTo(-1);
        }

        @Test
        @DisplayName("should inherit hasLocation returning false")
        void shouldInheritHasLocationReturningFalse() {
            YmlPlaceholderException exception = new YmlPlaceholderException("${placeholder}");

            assertThat(exception.hasLocation()).isFalse();
        }
    }

    @Nested
    @DisplayName("Typical placeholder scenarios")
    class TypicalScenarioTests {

        @Test
        @DisplayName("should represent missing environment variable")
        void shouldRepresentMissingEnvironmentVariable() {
            YmlPlaceholderException exception = new YmlPlaceholderException("${DATABASE_URL}");

            assertThat(exception.getMessage()).contains("Cannot resolve");
            assertThat(exception.getPlaceholder()).isEqualTo("${DATABASE_URL}");
        }

        @Test
        @DisplayName("should represent missing system property")
        void shouldRepresentMissingSystemProperty() {
            YmlPlaceholderException exception = new YmlPlaceholderException("${user.config.path}");

            assertThat(exception.getPlaceholder()).isEqualTo("${user.config.path}");
        }

        @Test
        @DisplayName("should represent circular dependency")
        void shouldRepresentCircularDependency() {
            YmlPlaceholderException exception = YmlPlaceholderException.circularReference("${a}");

            assertThat(exception.getMessage()).contains("Circular reference");
        }

        @Test
        @DisplayName("should represent placeholder with default value syntax")
        void shouldRepresentPlaceholderWithDefaultValueSyntax() {
            YmlPlaceholderException exception = new YmlPlaceholderException("${missing:default}", "Default value resolution failed");

            assertThat(exception.getPlaceholder()).isEqualTo("${missing:default}");
        }
    }

    @Nested
    @DisplayName("Various placeholder formats")
    class PlaceholderFormatTests {

        @Test
        @DisplayName("should handle Spring-style placeholder")
        void shouldHandleSpringStylePlaceholder() {
            YmlPlaceholderException exception = new YmlPlaceholderException("${spring.application.name}");

            assertThat(exception.getPlaceholder()).isEqualTo("${spring.application.name}");
        }

        @Test
        @DisplayName("should handle environment variable style")
        void shouldHandleEnvironmentVariableStyle() {
            YmlPlaceholderException exception = new YmlPlaceholderException("${HOME}");

            assertThat(exception.getPlaceholder()).isEqualTo("${HOME}");
        }

        @Test
        @DisplayName("should handle placeholder with colon default")
        void shouldHandlePlaceholderWithColonDefault() {
            YmlPlaceholderException exception = new YmlPlaceholderException("${port:8080}");

            assertThat(exception.getPlaceholder()).isEqualTo("${port:8080}");
        }

        @Test
        @DisplayName("should handle placeholder with nested brackets")
        void shouldHandlePlaceholderWithNestedBrackets() {
            YmlPlaceholderException exception = new YmlPlaceholderException("${config[${index}]}");

            assertThat(exception.getPlaceholder()).isEqualTo("${config[${index}]}");
        }

        @Test
        @DisplayName("should handle placeholder with special characters")
        void shouldHandlePlaceholderWithSpecialCharacters() {
            YmlPlaceholderException exception = new YmlPlaceholderException("${my-app.config_value}");

            assertThat(exception.getPlaceholder()).isEqualTo("${my-app.config_value}");
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle empty placeholder")
        void shouldHandleEmptyPlaceholder() {
            YmlPlaceholderException exception = new YmlPlaceholderException("");

            assertThat(exception.getPlaceholder()).isEmpty();
        }

        @Test
        @DisplayName("should handle null message in placeholder-message constructor")
        void shouldHandleNullMessageInPlaceholderMessageConstructor() {
            YmlPlaceholderException exception = new YmlPlaceholderException("${key}", null);

            assertThat(exception.getMessage()).isNull();
            assertThat(exception.getPlaceholder()).isEqualTo("${key}");
        }

        @Test
        @DisplayName("should handle placeholder without braces")
        void shouldHandlePlaceholderWithoutBraces() {
            YmlPlaceholderException exception = new YmlPlaceholderException("$value");

            assertThat(exception.getPlaceholder()).isEqualTo("$value");
        }

        @Test
        @DisplayName("should handle long placeholder")
        void shouldHandleLongPlaceholder() {
            String longPlaceholder = "${" + "a".repeat(1000) + "}";
            YmlPlaceholderException exception = new YmlPlaceholderException(longPlaceholder);

            assertThat(exception.getPlaceholder()).isEqualTo(longPlaceholder);
        }

        @Test
        @DisplayName("should handle unicode in placeholder")
        void shouldHandleUnicodeInPlaceholder() {
            YmlPlaceholderException exception = new YmlPlaceholderException("${config.value}");

            assertThat(exception.getPlaceholder()).isEqualTo("${config.value}");
        }
    }
}
