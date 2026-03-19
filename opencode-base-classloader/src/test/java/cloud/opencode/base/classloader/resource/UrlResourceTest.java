package cloud.opencode.base.classloader.resource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for UrlResource
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("UrlResource Tests")
class UrlResourceTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create with URL")
        void shouldCreateWithUrl() throws Exception {
            Path path = Files.writeString(tempDir.resolve("test.txt"), "Hello");
            URL url = path.toUri().toURL();
            UrlResource resource = new UrlResource(url);

            assertThat(resource.exists()).isTrue();
        }

        @Test
        @DisplayName("Should create with URL string")
        void shouldCreateWithUrlString() throws Exception {
            Path path = Files.writeString(tempDir.resolve("test.txt"), "Hello");
            UrlResource resource = new UrlResource(path.toUri().toString());

            assertThat(resource.exists()).isTrue();
        }

        @Test
        @DisplayName("Should create with URI")
        void shouldCreateWithUri() throws Exception {
            Path path = Files.writeString(tempDir.resolve("test.txt"), "Hello");
            URI uri = path.toUri();
            UrlResource resource = new UrlResource(uri);

            assertThat(resource.exists()).isTrue();
        }

        @Test
        @DisplayName("Should throw on invalid URL string")
        void shouldThrowOnInvalidUrlString() {
            assertThatThrownBy(() -> new UrlResource("not a valid url"))
                    .isInstanceOf(MalformedURLException.class);
        }

        @Test
        @DisplayName("Should throw on null URL")
        void shouldThrowOnNullUrl() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new UrlResource((URL) null));
        }
    }

    @Nested
    @DisplayName("Existence Tests")
    class ExistenceTests {

        @Test
        @DisplayName("Should exist for valid file URL")
        void shouldExistForValidFileUrl() throws Exception {
            Path path = Files.writeString(tempDir.resolve("test.txt"), "Hello");
            UrlResource resource = new UrlResource(path.toUri().toURL());

            assertThat(resource.exists()).isTrue();
        }

        @Test
        @DisplayName("Should not exist for nonexistent file URL")
        void shouldNotExistForNonexistentFileUrl() throws Exception {
            Path path = tempDir.resolve("nonexistent.txt");
            UrlResource resource = new UrlResource(path.toUri().toURL());

            assertThat(resource.exists()).isFalse();
        }

        @Test
        @DisplayName("Should be readable if exists")
        void shouldBeReadableIfExists() throws Exception {
            Path path = Files.writeString(tempDir.resolve("test.txt"), "Hello");
            UrlResource resource = new UrlResource(path.toUri().toURL());

            assertThat(resource.isReadable()).isTrue();
        }
    }

    @Nested
    @DisplayName("Content Tests")
    class ContentTests {

        @Test
        @DisplayName("Should return input stream")
        void shouldReturnInputStream() throws Exception {
            Path path = Files.writeString(tempDir.resolve("test.txt"), "Hello, World!");
            UrlResource resource = new UrlResource(path.toUri().toURL());

            assertThat(resource.getString()).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("Should return bytes")
        void shouldReturnBytes() throws Exception {
            Path path = Files.writeString(tempDir.resolve("test.txt"), "Hello");
            UrlResource resource = new UrlResource(path.toUri().toURL());

            assertThat(resource.getBytes()).isEqualTo("Hello".getBytes());
        }

        @Test
        @DisplayName("Should read lines")
        void shouldReadLines() throws Exception {
            Path path = Files.writeString(tempDir.resolve("test.txt"), "Line1\nLine2");
            UrlResource resource = new UrlResource(path.toUri().toURL());

            assertThat(resource.readLines()).containsExactly("Line1", "Line2");
        }
    }

    @Nested
    @DisplayName("URL Access Tests")
    class UrlAccessTests {

        @Test
        @DisplayName("Should return URL")
        void shouldReturnUrl() throws Exception {
            Path path = Files.writeString(tempDir.resolve("test.txt"), "Hello");
            URL url = path.toUri().toURL();
            UrlResource resource = new UrlResource(url);

            assertThat(resource.getUrl()).isEqualTo(url);
        }

        @Test
        @DisplayName("Should return URI")
        void shouldReturnUri() throws Exception {
            Path path = Files.writeString(tempDir.resolve("test.txt"), "Hello");
            UrlResource resource = new UrlResource(path.toUri().toURL());

            assertThat(resource.getUri()).isNotNull();
        }
    }

    @Nested
    @DisplayName("File Tests")
    class FileTests {

        @Test
        @DisplayName("Should be file for file URL")
        void shouldBeFileForFileUrl() throws Exception {
            Path path = Files.writeString(tempDir.resolve("test.txt"), "Hello");
            UrlResource resource = new UrlResource(path.toUri().toURL());

            assertThat(resource.isFile()).isTrue();
        }

        @Test
        @DisplayName("Should return file for file URL")
        void shouldReturnFileForFileUrl() throws Exception {
            Path path = Files.writeString(tempDir.resolve("test.txt"), "Hello");
            UrlResource resource = new UrlResource(path.toUri().toURL());

            assertThat(resource.getFile()).isPresent();
        }

        @Test
        @DisplayName("Should return path for file URL")
        void shouldReturnPathForFileUrl() throws Exception {
            Path path = Files.writeString(tempDir.resolve("test.txt"), "Hello");
            UrlResource resource = new UrlResource(path.toUri().toURL());

            assertThat(resource.getPath()).isPresent();
        }
    }

    @Nested
    @DisplayName("Metadata Tests")
    class MetadataTests {

        @Test
        @DisplayName("Should return filename")
        void shouldReturnFilename() throws Exception {
            Path path = Files.writeString(tempDir.resolve("test.txt"), "Hello");
            UrlResource resource = new UrlResource(path.toUri().toURL());

            assertThat(resource.getFilename()).isEqualTo("test.txt");
        }

        @Test
        @DisplayName("Should return content length")
        void shouldReturnContentLength() throws Exception {
            Path path = Files.writeString(tempDir.resolve("test.txt"), "Hello");
            UrlResource resource = new UrlResource(path.toUri().toURL());

            assertThat(resource.contentLength()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should return last modified")
        void shouldReturnLastModified() throws Exception {
            Path path = Files.writeString(tempDir.resolve("test.txt"), "Hello");
            UrlResource resource = new UrlResource(path.toUri().toURL());

            assertThat(resource.lastModified()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should return description")
        void shouldReturnDescription() throws Exception {
            Path path = Files.writeString(tempDir.resolve("test.txt"), "Hello");
            UrlResource resource = new UrlResource(path.toUri().toURL());

            assertThat(resource.getDescription()).contains("test.txt");
        }
    }

    @Nested
    @DisplayName("Relative Resource Tests")
    class RelativeResourceTests {

        @Test
        @DisplayName("Should create relative resource")
        void shouldCreateRelativeResource() throws Exception {
            Path path = Files.writeString(tempDir.resolve("test.txt"), "Hello");
            Files.writeString(tempDir.resolve("other.txt"), "Other");
            UrlResource resource = new UrlResource(path.toUri().toURL());

            Resource relative = resource.createRelative("other.txt");

            assertThat(relative).isInstanceOf(UrlResource.class);
            assertThat(relative.exists()).isTrue();
        }
    }
}
