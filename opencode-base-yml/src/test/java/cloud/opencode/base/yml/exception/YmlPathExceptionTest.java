package cloud.opencode.base.yml.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * YmlPathExceptionTest Tests
 * YmlPathExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("YmlPathException Tests")
class YmlPathExceptionTest {

    @Nested
    @DisplayName("Constructor with path only (missing path)")
    class PathOnlyConstructorTests {

        @Test
        @DisplayName("should format message for missing path")
        void shouldFormatMessageForMissingPath() {
            YmlPathException exception = new YmlPathException("server.host");

            assertThat(exception.getMessage()).isEqualTo("Path not found: server.host");
        }

        @Test
        @DisplayName("should store path")
        void shouldStorePath() {
            YmlPathException exception = new YmlPathException("database.url");

            assertThat(exception.getPath()).isEqualTo("database.url");
        }

        @Test
        @DisplayName("should have no cause")
        void shouldHaveNoCause() {
            YmlPathException exception = new YmlPathException("config.value");

            assertThat(exception.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("Constructor with path and message")
    class PathAndMessageConstructorTests {

        @Test
        @DisplayName("should set custom message")
        void shouldSetCustomMessage() {
            YmlPathException exception = new YmlPathException("app.setting", "Invalid path syntax");

            assertThat(exception.getMessage()).isEqualTo("Invalid path syntax");
        }

        @Test
        @DisplayName("should store path")
        void shouldStorePath() {
            YmlPathException exception = new YmlPathException("config.nested.value", "Custom error");

            assertThat(exception.getPath()).isEqualTo("config.nested.value");
        }

        @Test
        @DisplayName("should have no cause")
        void shouldHaveNoCause() {
            YmlPathException exception = new YmlPathException("path", "Error");

            assertThat(exception.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("Constructor with path, message and cause")
    class FullConstructorTests {

        @Test
        @DisplayName("should set message correctly")
        void shouldSetMessageCorrectly() {
            Throwable cause = new RuntimeException("Root cause");
            YmlPathException exception = new YmlPathException("server.port", "Path error", cause);

            assertThat(exception.getMessage()).isEqualTo("Path error");
        }

        @Test
        @DisplayName("should store path")
        void shouldStorePath() {
            Throwable cause = new RuntimeException();
            YmlPathException exception = new YmlPathException("data.items", "Error", cause);

            assertThat(exception.getPath()).isEqualTo("data.items");
        }

        @Test
        @DisplayName("should store cause")
        void shouldStoreCause() {
            Throwable cause = new IllegalArgumentException("Invalid argument");
            YmlPathException exception = new YmlPathException("path", "Error", cause);

            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("getPath method")
    class GetPathTests {

        @Test
        @DisplayName("should return path from path-only constructor")
        void shouldReturnPathFromPathOnlyConstructor() {
            YmlPathException exception = new YmlPathException("app.config");

            assertThat(exception.getPath()).isEqualTo("app.config");
        }

        @Test
        @DisplayName("should return path from path-message constructor")
        void shouldReturnPathFromPathMessageConstructor() {
            YmlPathException exception = new YmlPathException("server.ssl", "SSL config missing");

            assertThat(exception.getPath()).isEqualTo("server.ssl");
        }

        @Test
        @DisplayName("should return path from full constructor")
        void shouldReturnPathFromFullConstructor() {
            YmlPathException exception = new YmlPathException("database", "DB error", new RuntimeException());

            assertThat(exception.getPath()).isEqualTo("database");
        }

        @Test
        @DisplayName("should handle nested paths")
        void shouldHandleNestedPaths() {
            YmlPathException exception = new YmlPathException("app.server.ssl.keystore.path");

            assertThat(exception.getPath()).isEqualTo("app.server.ssl.keystore.path");
        }
    }

    @Nested
    @DisplayName("indexOutOfBounds factory method")
    class IndexOutOfBoundsFactoryTests {

        @Test
        @DisplayName("should create exception with formatted message")
        void shouldCreateExceptionWithFormattedMessage() {
            YmlPathException exception = YmlPathException.indexOutOfBounds("items", 5, 3);

            assertThat(exception.getMessage())
                .isEqualTo("Index 5 out of bounds for sequence at 'items' (size: 3)");
        }

        @Test
        @DisplayName("should store path")
        void shouldStorePath() {
            YmlPathException exception = YmlPathException.indexOutOfBounds("data.list", 10, 5);

            assertThat(exception.getPath()).isEqualTo("data.list");
        }

        @Test
        @DisplayName("should handle index 0")
        void shouldHandleIndexZero() {
            YmlPathException exception = YmlPathException.indexOutOfBounds("empty", 0, 0);

            assertThat(exception.getMessage()).contains("Index 0");
            assertThat(exception.getMessage()).contains("size: 0");
        }

        @Test
        @DisplayName("should handle large indices")
        void shouldHandleLargeIndices() {
            YmlPathException exception = YmlPathException.indexOutOfBounds("biglist", 1000000, 500000);

            assertThat(exception.getMessage()).contains("Index 1000000");
            assertThat(exception.getMessage()).contains("size: 500000");
        }

        @Test
        @DisplayName("should handle negative index")
        void shouldHandleNegativeIndex() {
            YmlPathException exception = YmlPathException.indexOutOfBounds("array", -1, 10);

            assertThat(exception.getMessage()).contains("Index -1");
        }
    }

    @Nested
    @DisplayName("typeMismatch factory method")
    class TypeMismatchFactoryTests {

        @Test
        @DisplayName("should create exception with formatted message")
        void shouldCreateExceptionWithFormattedMessage() {
            YmlPathException exception = YmlPathException.typeMismatch("config.port", "integer", "string");

            assertThat(exception.getMessage())
                .isEqualTo("Type mismatch at 'config.port': expected integer but found string");
        }

        @Test
        @DisplayName("should store path")
        void shouldStorePath() {
            YmlPathException exception = YmlPathException.typeMismatch("server.host", "string", "mapping");

            assertThat(exception.getPath()).isEqualTo("server.host");
        }

        @Test
        @DisplayName("should handle mapping type mismatch")
        void shouldHandleMappingTypeMismatch() {
            YmlPathException exception = YmlPathException.typeMismatch("data", "mapping", "sequence");

            assertThat(exception.getMessage()).contains("expected mapping");
            assertThat(exception.getMessage()).contains("found sequence");
        }

        @Test
        @DisplayName("should handle sequence type mismatch")
        void shouldHandleSequenceTypeMismatch() {
            YmlPathException exception = YmlPathException.typeMismatch("items", "sequence", "scalar");

            assertThat(exception.getMessage()).contains("expected sequence");
            assertThat(exception.getMessage()).contains("found scalar");
        }

        @Test
        @DisplayName("should include path in message")
        void shouldIncludePathInMessage() {
            YmlPathException exception = YmlPathException.typeMismatch("app.nested.value", "boolean", "string");

            assertThat(exception.getMessage()).contains("app.nested.value");
        }
    }

    @Nested
    @DisplayName("Inheritance hierarchy")
    class InheritanceTests {

        @Test
        @DisplayName("should extend OpenYmlException")
        void shouldExtendOpenYmlException() {
            YmlPathException exception = new YmlPathException("path");

            assertThat(exception).isInstanceOf(OpenYmlException.class);
        }

        @Test
        @DisplayName("should extend RuntimeException")
        void shouldExtendRuntimeException() {
            YmlPathException exception = new YmlPathException("path");

            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should be catchable as OpenYmlException")
        void shouldBeCatchableAsOpenYmlException() {
            assertThatThrownBy(() -> {
                throw new YmlPathException("invalid.path");
            }).isInstanceOf(OpenYmlException.class);
        }

        @Test
        @DisplayName("should inherit getLine method returning -1")
        void shouldInheritGetLineMethodReturningNegativeOne() {
            YmlPathException exception = new YmlPathException("path");

            assertThat(exception.getLine()).isEqualTo(-1);
        }

        @Test
        @DisplayName("should inherit getColumn method returning -1")
        void shouldInheritGetColumnMethodReturningNegativeOne() {
            YmlPathException exception = new YmlPathException("path");

            assertThat(exception.getColumn()).isEqualTo(-1);
        }

        @Test
        @DisplayName("should inherit hasLocation returning false")
        void shouldInheritHasLocationReturningFalse() {
            YmlPathException exception = new YmlPathException("path");

            assertThat(exception.hasLocation()).isFalse();
        }
    }

    @Nested
    @DisplayName("Typical path error scenarios")
    class TypicalScenarioTests {

        @Test
        @DisplayName("should represent missing configuration key")
        void shouldRepresentMissingConfigurationKey() {
            YmlPathException exception = new YmlPathException("spring.datasource.url");

            assertThat(exception.getMessage()).contains("not found");
            assertThat(exception.getPath()).isEqualTo("spring.datasource.url");
        }

        @Test
        @DisplayName("should represent array access error")
        void shouldRepresentArrayAccessError() {
            YmlPathException exception = YmlPathException.indexOutOfBounds("servers[2].host", 2, 2);

            assertThat(exception.getMessage()).contains("out of bounds");
        }

        @Test
        @DisplayName("should represent wrong type access")
        void shouldRepresentWrongTypeAccess() {
            YmlPathException exception = YmlPathException.typeMismatch("logging.level", "mapping", "string");

            assertThat(exception.getMessage()).contains("Type mismatch");
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle empty path")
        void shouldHandleEmptyPath() {
            YmlPathException exception = new YmlPathException("");

            assertThat(exception.getPath()).isEmpty();
            assertThat(exception.getMessage()).isEqualTo("Path not found: ");
        }

        @Test
        @DisplayName("should handle path with array notation")
        void shouldHandlePathWithArrayNotation() {
            YmlPathException exception = new YmlPathException("items[0].name");

            assertThat(exception.getPath()).isEqualTo("items[0].name");
        }

        @Test
        @DisplayName("should handle path with special characters")
        void shouldHandlePathWithSpecialCharacters() {
            YmlPathException exception = new YmlPathException("config['special-key'].value");

            assertThat(exception.getPath()).isEqualTo("config['special-key'].value");
        }

        @Test
        @DisplayName("should handle null message in path-message constructor")
        void shouldHandleNullMessageInPathMessageConstructor() {
            YmlPathException exception = new YmlPathException("path", null);

            assertThat(exception.getMessage()).isNull();
            assertThat(exception.getPath()).isEqualTo("path");
        }

        @Test
        @DisplayName("should handle root path")
        void shouldHandleRootPath() {
            YmlPathException exception = new YmlPathException("$");

            assertThat(exception.getPath()).isEqualTo("$");
        }
    }
}
