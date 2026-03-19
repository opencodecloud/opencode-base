package cloud.opencode.base.classloader.resource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for FileResource
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("FileResource Tests")
class FileResourceTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create with File")
        void shouldCreateWithFile() throws IOException {
            File file = Files.createFile(tempDir.resolve("test.txt")).toFile();
            FileResource resource = new FileResource(file);

            assertThat(resource.exists()).isTrue();
        }

        @Test
        @DisplayName("Should create with Path")
        void shouldCreateWithPath() throws IOException {
            Path path = Files.createFile(tempDir.resolve("test.txt"));
            FileResource resource = new FileResource(path);

            assertThat(resource.exists()).isTrue();
        }

        @Test
        @DisplayName("Should create with String path")
        void shouldCreateWithStringPath() throws IOException {
            Path path = Files.createFile(tempDir.resolve("test.txt"));
            FileResource resource = new FileResource(path.toString());

            assertThat(resource.exists()).isTrue();
        }

        @Test
        @DisplayName("Should throw on null path")
        void shouldThrowOnNullPath() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new FileResource((Path) null));
        }
    }

    @Nested
    @DisplayName("Existence Tests")
    class ExistenceTests {

        @Test
        @DisplayName("Should exist for real file")
        void shouldExistForRealFile() throws IOException {
            Path path = Files.createFile(tempDir.resolve("test.txt"));
            FileResource resource = new FileResource(path);

            assertThat(resource.exists()).isTrue();
        }

        @Test
        @DisplayName("Should not exist for nonexistent file")
        void shouldNotExistForNonexistentFile() {
            FileResource resource = new FileResource(tempDir.resolve("nonexistent.txt"));

            assertThat(resource.exists()).isFalse();
        }

        @Test
        @DisplayName("Should be readable for readable file")
        void shouldBeReadableForReadableFile() throws IOException {
            Path path = Files.createFile(tempDir.resolve("test.txt"));
            FileResource resource = new FileResource(path);

            assertThat(resource.isReadable()).isTrue();
        }

        @Test
        @DisplayName("Should be file")
        void shouldBeFile() throws IOException {
            Path path = Files.createFile(tempDir.resolve("test.txt"));
            FileResource resource = new FileResource(path);

            assertThat(resource.isFile()).isTrue();
        }

        @Test
        @DisplayName("Should not be file for directory")
        void shouldNotBeFileForDirectory() {
            FileResource resource = new FileResource(tempDir);

            assertThat(resource.isFile()).isFalse();
        }
    }

    @Nested
    @DisplayName("Content Tests")
    class ContentTests {

        @Test
        @DisplayName("Should return input stream")
        void shouldReturnInputStream() throws IOException {
            Path path = Files.writeString(tempDir.resolve("test.txt"), "Hello, World!");
            FileResource resource = new FileResource(path);

            try (InputStream is = resource.getInputStream()) {
                assertThat(new String(is.readAllBytes())).isEqualTo("Hello, World!");
            }
        }

        @Test
        @DisplayName("Should throw on getInputStream for nonexistent")
        void shouldThrowOnGetInputStreamForNonexistent() {
            FileResource resource = new FileResource(tempDir.resolve("nonexistent.txt"));

            assertThatThrownBy(resource::getInputStream)
                    .isInstanceOf(Exception.class);  // May throw OpenClassLoaderException or IOException
        }

        @Test
        @DisplayName("Should return string")
        void shouldReturnString() throws IOException {
            Path path = Files.writeString(tempDir.resolve("test.txt"), "Hello, World!");
            FileResource resource = new FileResource(path);

            assertThat(resource.getString()).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("Should return bytes")
        void shouldReturnBytes() throws IOException {
            Path path = Files.writeString(tempDir.resolve("test.txt"), "Hello");
            FileResource resource = new FileResource(path);

            assertThat(resource.getBytes()).isEqualTo("Hello".getBytes());
        }

        @Test
        @DisplayName("Should read lines")
        void shouldReadLines() throws IOException {
            Path path = Files.writeString(tempDir.resolve("test.txt"), "Line1\nLine2\nLine3");
            FileResource resource = new FileResource(path);

            assertThat(resource.readLines()).containsExactly("Line1", "Line2", "Line3");
        }
    }

    @Nested
    @DisplayName("File Access Tests")
    class FileAccessTests {

        @Test
        @DisplayName("Should return file")
        void shouldReturnFile() throws IOException {
            Path path = Files.createFile(tempDir.resolve("test.txt"));
            FileResource resource = new FileResource(path);

            assertThat(resource.getFile()).isPresent();
            assertThat(resource.getFile().get()).isEqualTo(path.toFile());
        }

        @Test
        @DisplayName("Should return path")
        void shouldReturnPath() throws IOException {
            Path path = Files.createFile(tempDir.resolve("test.txt"));
            FileResource resource = new FileResource(path);

            assertThat(resource.getPath()).isPresent();
            assertThat(resource.getPath().get()).isEqualTo(path);
        }

        @Test
        @DisplayName("Should return URL")
        void shouldReturnUrl() throws IOException {
            Path path = Files.createFile(tempDir.resolve("test.txt"));
            FileResource resource = new FileResource(path);

            assertThat(resource.getUrl()).isNotNull();
            assertThat(resource.getUrl().getProtocol()).isEqualTo("file");
        }

        @Test
        @DisplayName("Should return URI")
        void shouldReturnUri() throws IOException {
            Path path = Files.createFile(tempDir.resolve("test.txt"));
            FileResource resource = new FileResource(path);

            assertThat(resource.getUri()).isNotNull();
            assertThat(resource.getUri().getScheme()).isEqualTo("file");
        }
    }

    @Nested
    @DisplayName("Metadata Tests")
    class MetadataTests {

        @Test
        @DisplayName("Should return filename")
        void shouldReturnFilename() throws IOException {
            Path path = Files.createFile(tempDir.resolve("test.txt"));
            FileResource resource = new FileResource(path);

            assertThat(resource.getFilename()).isEqualTo("test.txt");
        }

        @Test
        @DisplayName("Should return content length")
        void shouldReturnContentLength() throws IOException {
            Path path = Files.writeString(tempDir.resolve("test.txt"), "Hello");
            FileResource resource = new FileResource(path);

            assertThat(resource.contentLength()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should return last modified")
        void shouldReturnLastModified() throws IOException {
            Path path = Files.createFile(tempDir.resolve("test.txt"));
            FileResource resource = new FileResource(path);

            assertThat(resource.lastModified()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should return description")
        void shouldReturnDescription() throws IOException {
            Path path = Files.createFile(tempDir.resolve("test.txt"));
            FileResource resource = new FileResource(path);

            assertThat(resource.getDescription()).contains("test.txt");
        }
    }

    @Nested
    @DisplayName("Relative Resource Tests")
    class RelativeResourceTests {

        @Test
        @DisplayName("Should create relative resource")
        void shouldCreateRelativeResource() throws IOException {
            Path path = Files.createFile(tempDir.resolve("test.txt"));
            Files.createFile(tempDir.resolve("other.txt"));
            FileResource resource = new FileResource(path);

            Resource relative = resource.createRelative("other.txt");

            assertThat(relative).isInstanceOf(FileResource.class);
            assertThat(relative.exists()).isTrue();
        }
    }
}
