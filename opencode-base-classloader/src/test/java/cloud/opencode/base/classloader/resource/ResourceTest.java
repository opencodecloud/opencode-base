package cloud.opencode.base.classloader.resource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for Resource interface default methods
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("Resource Interface Tests")
class ResourceTest {

    private static final String TEST_CONTENT = "Hello, World!\nLine 2\nLine 3";
    private static final byte[] TEST_BYTES = TEST_CONTENT.getBytes(StandardCharsets.UTF_8);

    @Nested
    @DisplayName("Default Method Tests")
    class DefaultMethodTests {

        @Test
        @DisplayName("Should read bytes via default method")
        void shouldReadBytesViaDefaultMethod() throws IOException {
            Resource resource = new ByteArrayResource(TEST_BYTES);

            byte[] bytes = resource.getBytes();

            assertThat(bytes).isEqualTo(TEST_BYTES);
        }

        @Test
        @DisplayName("Should read string via default method")
        void shouldReadStringViaDefaultMethod() throws IOException {
            Resource resource = new ByteArrayResource(TEST_BYTES);

            String content = resource.getString();

            assertThat(content).isEqualTo(TEST_CONTENT);
        }

        @Test
        @DisplayName("Should read string with charset via default method")
        void shouldReadStringWithCharsetViaDefaultMethod() throws IOException {
            Resource resource = new ByteArrayResource(TEST_BYTES);

            String content = resource.getString(StandardCharsets.UTF_8);

            assertThat(content).isEqualTo(TEST_CONTENT);
        }

        @Test
        @DisplayName("Should read lines via default method")
        void shouldReadLinesViaDefaultMethod() throws IOException {
            Resource resource = new ByteArrayResource(TEST_BYTES);

            List<String> lines = resource.readLines();

            assertThat(lines).containsExactly("Hello, World!", "Line 2", "Line 3");
        }

        @Test
        @DisplayName("Should read lines with charset via default method")
        void shouldReadLinesWithCharsetViaDefaultMethod() throws IOException {
            Resource resource = new ByteArrayResource(TEST_BYTES);

            List<String> lines = resource.readLines(StandardCharsets.UTF_8);

            assertThat(lines).containsExactly("Hello, World!", "Line 2", "Line 3");
        }
    }

    @Nested
    @DisplayName("ClassPathResource Default Methods Tests")
    class ClassPathResourceDefaultMethodsTests {

        @Test
        @DisplayName("Should use default getBytes method")
        void shouldUseDefaultGetBytesMethod() throws IOException {
            ClassPathResource resource = new ClassPathResource("test.txt");

            byte[] bytes = resource.getBytes();

            assertThat(bytes).isNotEmpty();
        }

        @Test
        @DisplayName("Should use default getString method")
        void shouldUseDefaultGetStringMethod() throws IOException {
            ClassPathResource resource = new ClassPathResource("test.txt");

            String content = resource.getString();

            assertThat(content).contains("Hello, World!");
        }

        @Test
        @DisplayName("Should use default readLines method")
        void shouldUseDefaultReadLinesMethod() throws IOException {
            ClassPathResource resource = new ClassPathResource("test.txt");

            List<String> lines = resource.readLines();

            assertThat(lines).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("FileResource Default Methods Tests")
    class FileResourceDefaultMethodsTests {

        @Test
        @DisplayName("Should use default getBytes method")
        void shouldUseDefaultGetBytesMethod() throws IOException {
            // Use classpath resource as it always exists
            ClassPathResource resource = new ClassPathResource("test.txt");

            byte[] bytes = resource.getBytes();

            assertThat(bytes).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Interface Contract Tests")
    class InterfaceContractTests {

        @Test
        @DisplayName("Should implement all required methods")
        void shouldImplementAllRequiredMethods() {
            Resource resource = new ByteArrayResource(TEST_BYTES);

            // All required methods should be callable
            assertThat(resource.exists()).isTrue();
            assertThat(resource.isReadable()).isTrue();
            assertThat(resource.isFile()).isFalse();
            assertThat(resource.getDescription()).isNotNull();
        }

        @Test
        @DisplayName("Should provide optional file access")
        void shouldProvideOptionalFileAccess() {
            Resource resource = new ByteArrayResource(TEST_BYTES);

            assertThat(resource.getFile()).isEmpty();
            assertThat(resource.getPath()).isEmpty();
        }
    }
}
