package cloud.opencode.base.classloader;

import cloud.opencode.base.classloader.exception.OpenClassLoaderException;
import cloud.opencode.base.classloader.resource.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for OpenResource facade
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("OpenResource Facade Tests")
class OpenResourceTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("Single Resource Tests")
    class SingleResourceTests {

        @Test
        @DisplayName("Should get resource by location")
        void shouldGetResourceByLocation() {
            Resource resource = OpenResource.get("classpath:test.txt");

            assertThat(resource).isNotNull();
        }

        @Test
        @DisplayName("Should throw on null location")
        void shouldThrowOnNullLocation() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenResource.get(null));
        }

        @Test
        @DisplayName("Should get classpath resource")
        void shouldGetClasspathResource() {
            Resource resource = OpenResource.classpath("test.txt");

            assertThat(resource).isNotNull();
            assertThat(resource).isInstanceOf(ClassPathResource.class);
        }

        @Test
        @DisplayName("Should get file resource")
        void shouldGetFileResource() throws IOException {
            Path file = tempDir.resolve("test-file.txt");
            Files.writeString(file, "Test content");

            Resource resource = OpenResource.file(file.toString());

            assertThat(resource).isNotNull();
            assertThat(resource).isInstanceOf(FileResource.class);
        }

        @Test
        @DisplayName("Should get URL resource")
        void shouldGetUrlResource() throws IOException {
            Path file = tempDir.resolve("url-test.txt");
            Files.writeString(file, "URL test content");

            Resource resource = OpenResource.url(file.toUri().toString());

            assertThat(resource).isNotNull();
            assertThat(resource).isInstanceOf(UrlResource.class);
        }

        @Test
        @DisplayName("Should throw on invalid URL")
        void shouldThrowOnInvalidUrl() {
            assertThatThrownBy(() -> OpenResource.url("not a valid url"))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("Invalid URL");
        }

        @Test
        @DisplayName("Should create byte array resource")
        void shouldCreateByteArrayResource() {
            byte[] content = "Test bytes".getBytes();

            Resource resource = OpenResource.bytes(content);

            assertThat(resource).isNotNull();
            assertThat(resource).isInstanceOf(ByteArrayResource.class);
        }

        @Test
        @DisplayName("Should create byte array resource with description")
        void shouldCreateByteArrayResourceWithDescription() {
            byte[] content = "Test bytes".getBytes();

            Resource resource = OpenResource.bytes(content, "Test resource");

            assertThat(resource).isNotNull();
            assertThat(resource).isInstanceOf(ByteArrayResource.class);
            assertThat(resource.getDescription()).contains("Test resource");
        }
    }

    @Nested
    @DisplayName("Multiple Resources Tests")
    class MultipleResourcesTests {

        @Test
        @DisplayName("Should get all matching resources")
        void shouldGetAllMatchingResources() {
            List<Resource> resources = OpenResource.getAll("classpath*:*.txt");

            assertThat(resources).isNotNull();
        }

        @Test
        @DisplayName("Should throw on null pattern")
        void shouldThrowOnNullPattern() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenResource.getAll(null));
        }
    }

    @Nested
    @DisplayName("Existence Check Tests")
    class ExistenceCheckTests {

        @Test
        @DisplayName("Should return true for existing resource")
        void shouldReturnTrueForExistingResource() {
            boolean exists = OpenResource.exists("classpath:test.txt");

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false for nonexistent resource")
        void shouldReturnFalseForNonexistentResource() {
            boolean exists = OpenResource.exists("classpath:nonexistent-file.xyz");

            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Should return false for invalid location")
        void shouldReturnFalseForInvalidLocation() {
            boolean exists = OpenResource.exists("invalid://location");

            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("Quick Read Tests")
    class QuickReadTests {

        @Test
        @DisplayName("Should read string from resource")
        void shouldReadStringFromResource() throws IOException {
            Path file = tempDir.resolve("read-test.txt");
            Files.writeString(file, "File content");

            String content = OpenResource.readString("file:" + file.toString());

            assertThat(content).isEqualTo("File content");
        }

        @Test
        @DisplayName("Should read bytes from resource")
        void shouldReadBytesFromResource() throws IOException {
            Path file = tempDir.resolve("bytes-test.txt");
            byte[] expected = "Byte content".getBytes();
            Files.write(file, expected);

            byte[] content = OpenResource.readBytes("file:" + file.toString());

            assertThat(content).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should read lines from resource")
        void shouldReadLinesFromResource() throws IOException {
            Path file = tempDir.resolve("lines-test.txt");
            Files.writeString(file, "Line 1\nLine 2\nLine 3");

            List<String> lines = OpenResource.readLines("file:" + file.toString());

            assertThat(lines).containsExactly("Line 1", "Line 2", "Line 3");
        }

        @Test
        @DisplayName("Should throw on read string from nonexistent")
        void shouldThrowOnReadStringFromNonexistent() {
            assertThatThrownBy(() -> OpenResource.readString("file:/nonexistent/path.txt"))
                    .isInstanceOf(Exception.class);  // May throw OpenClassLoaderException or IOException
        }
    }

    @Nested
    @DisplayName("Loader Tests")
    class LoaderTests {

        @Test
        @DisplayName("Should create resource loader")
        void shouldCreateResourceLoader() {
            ResourceLoader loader = OpenResource.loader();

            assertThat(loader).isNotNull();
        }

        @Test
        @DisplayName("Should create resource loader with classloader")
        void shouldCreateResourceLoaderWithClassLoader() {
            ResourceLoader loader = OpenResource.loader(getClass().getClassLoader());

            assertThat(loader).isNotNull();
        }
    }
}
