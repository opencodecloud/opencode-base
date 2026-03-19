package cloud.opencode.base.classloader.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for OpenClassLoaderException
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("OpenClassLoaderException Tests")
class OpenClassLoaderExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create exception with message")
        void shouldCreateWithMessage() {
            OpenClassLoaderException ex = new OpenClassLoaderException("Test message");

            assertThat(ex.getMessage()).contains("Test message");
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("Should create exception with message and cause")
        void shouldCreateWithMessageAndCause() {
            RuntimeException cause = new RuntimeException("Root cause");
            OpenClassLoaderException ex = new OpenClassLoaderException("Test message", cause);

            assertThat(ex.getMessage()).contains("Test message");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Should create classNotFound exception")
        void shouldCreateClassNotFound() {
            OpenClassLoaderException ex = OpenClassLoaderException.classNotFound("com.example.MyClass");

            assertThat(ex.getMessage()).contains("com.example.MyClass");
            assertThat(ex.getMessage()).containsIgnoringCase("not found");
            assertThat(ex.getClassName()).isPresent().contains("com.example.MyClass");
            assertThat(ex.getResourceName()).isEmpty();
        }

        @Test
        @DisplayName("Should create classNotFound exception with cause")
        void shouldCreateClassNotFoundWithCause() {
            RuntimeException cause = new RuntimeException("Original error");
            OpenClassLoaderException ex = OpenClassLoaderException.classNotFound("com.example.MyClass", cause);

            assertThat(ex.getMessage()).contains("com.example.MyClass");
            assertThat(ex.getMessage()).containsIgnoringCase("not found");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getClassName()).isPresent().contains("com.example.MyClass");
            assertThat(ex.getResourceName()).isEmpty();
        }

        @Test
        @DisplayName("Should create classLoadFailed exception")
        void shouldCreateClassLoadFailed() {
            RuntimeException cause = new RuntimeException("Load error");
            OpenClassLoaderException ex = OpenClassLoaderException.classLoadFailed("com.example.MyClass", cause);

            assertThat(ex.getMessage()).contains("com.example.MyClass");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getClassName()).isPresent().contains("com.example.MyClass");
        }

        @Test
        @DisplayName("Should create resourceNotFound exception")
        void shouldCreateResourceNotFound() {
            OpenClassLoaderException ex = OpenClassLoaderException.resourceNotFound("config.yml");

            assertThat(ex.getMessage()).contains("config.yml");
            assertThat(ex.getMessage()).containsIgnoringCase("not found");
            assertThat(ex.getResourceName()).isPresent().contains("config.yml");
            assertThat(ex.getClassName()).isEmpty();
        }

        @Test
        @DisplayName("Should create resourceReadFailed exception")
        void shouldCreateResourceReadFailed() {
            RuntimeException cause = new RuntimeException("Read error");
            OpenClassLoaderException ex = OpenClassLoaderException.resourceReadFailed("config.yml", cause);

            assertThat(ex.getMessage()).contains("config.yml");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getResourceName()).isPresent().contains("config.yml");
        }

        @Test
        @DisplayName("Should create metadataParseFailed exception")
        void shouldCreateMetadataParseFailed() {
            RuntimeException cause = new RuntimeException("Parse error");
            OpenClassLoaderException ex = OpenClassLoaderException.metadataParseFailed("com.example.MyClass", cause);

            assertThat(ex.getMessage()).contains("com.example.MyClass");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("Should create scanFailed exception")
        void shouldCreateScanFailed() {
            RuntimeException cause = new RuntimeException("Scan error");
            OpenClassLoaderException ex = OpenClassLoaderException.scanFailed("com.example", cause);

            assertThat(ex.getMessage()).contains("com.example");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("Should create classLoaderClosed exception")
        void shouldCreateClassLoaderClosed() {
            OpenClassLoaderException ex = OpenClassLoaderException.classLoaderClosed();

            assertThat(ex.getMessage()).containsIgnoringCase("closed");
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("Should return empty Optional when className is null")
        void shouldReturnEmptyOptionalForNullClassName() {
            OpenClassLoaderException ex = new OpenClassLoaderException("Test");

            assertThat(ex.getClassName()).isEmpty();
        }

        @Test
        @DisplayName("Should return empty Optional when resourceName is null")
        void shouldReturnEmptyOptionalForNullResourceName() {
            OpenClassLoaderException ex = new OpenClassLoaderException("Test");

            assertThat(ex.getResourceName()).isEmpty();
        }
    }
}
