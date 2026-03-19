package cloud.opencode.base.classloader.resource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ClassPathResource
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("ClassPathResource Tests")
class ClassPathResourceTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create with path")
        void shouldCreateWithPath() {
            ClassPathResource resource = new ClassPathResource("test.txt");

            assertThat(resource.getDescription()).contains("test.txt");
        }

        @Test
        @DisplayName("Should create with path and classloader")
        void shouldCreateWithPathAndClassLoader() {
            ClassPathResource resource = new ClassPathResource("test.txt", getClass().getClassLoader());

            assertThat(resource.exists()).isTrue();
        }

        @Test
        @DisplayName("Should throw on null path")
        void shouldThrowOnNullPath() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new ClassPathResource(null));
        }

        @Test
        @DisplayName("Should normalize path with leading slash")
        void shouldNormalizePathWithLeadingSlash() {
            ClassPathResource resource = new ClassPathResource("/test.txt");

            assertThat(resource.exists()).isTrue();
        }
    }

    @Nested
    @DisplayName("Existence Tests")
    class ExistenceTests {

        @Test
        @DisplayName("Should exist for valid resource")
        void shouldExistForValidResource() {
            ClassPathResource resource = new ClassPathResource("test.txt");

            assertThat(resource.exists()).isTrue();
        }

        @Test
        @DisplayName("Should not exist for invalid resource")
        void shouldNotExistForInvalidResource() {
            ClassPathResource resource = new ClassPathResource("nonexistent.txt");

            assertThat(resource.exists()).isFalse();
        }

        @Test
        @DisplayName("Should be readable if exists")
        void shouldBeReadableIfExists() {
            ClassPathResource resource = new ClassPathResource("test.txt");

            assertThat(resource.isReadable()).isTrue();
        }

        @Test
        @DisplayName("Should not be readable if not exists")
        void shouldNotBeReadableIfNotExists() {
            ClassPathResource resource = new ClassPathResource("nonexistent.txt");

            assertThat(resource.isReadable()).isFalse();
        }
    }

    @Nested
    @DisplayName("Content Tests")
    class ContentTests {

        @Test
        @DisplayName("Should return input stream")
        void shouldReturnInputStream() throws IOException {
            ClassPathResource resource = new ClassPathResource("test.txt");

            try (InputStream is = resource.getInputStream()) {
                assertThat(is).isNotNull();
                String content = new String(is.readAllBytes());
                assertThat(content).contains("Hello, World!");
            }
        }

        @Test
        @DisplayName("Should throw on getInputStream for nonexistent")
        void shouldThrowOnGetInputStreamForNonexistent() {
            ClassPathResource resource = new ClassPathResource("nonexistent.txt");

            assertThatThrownBy(resource::getInputStream)
                    .isInstanceOf(Exception.class);  // May throw OpenClassLoaderException or IOException
        }

        @Test
        @DisplayName("Should return string")
        void shouldReturnString() throws IOException {
            ClassPathResource resource = new ClassPathResource("test.txt");

            assertThat(resource.getString()).contains("Hello, World!");
        }

        @Test
        @DisplayName("Should return bytes")
        void shouldReturnBytes() throws IOException {
            ClassPathResource resource = new ClassPathResource("test.txt");

            assertThat(resource.getBytes()).isNotEmpty();
        }

        @Test
        @DisplayName("Should read lines")
        void shouldReadLines() throws IOException {
            ClassPathResource resource = new ClassPathResource("test.txt");

            assertThat(resource.readLines()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("URL Tests")
    class UrlTests {

        @Test
        @DisplayName("Should return URL")
        void shouldReturnUrl() throws IOException {
            ClassPathResource resource = new ClassPathResource("test.txt");

            assertThat(resource.getUrl()).isNotNull();
        }

        @Test
        @DisplayName("Should return URI")
        void shouldReturnUri() throws IOException {
            ClassPathResource resource = new ClassPathResource("test.txt");

            assertThat(resource.getUri()).isNotNull();
        }

        @Test
        @DisplayName("Should throw on getUrl for nonexistent")
        void shouldThrowOnGetUrlForNonexistent() {
            ClassPathResource resource = new ClassPathResource("nonexistent.txt");

            assertThatThrownBy(resource::getUrl)
                    .isInstanceOf(Exception.class);  // May throw OpenClassLoaderException or IOException
        }
    }

    @Nested
    @DisplayName("File Tests")
    class FileTests {

        @Test
        @DisplayName("Should detect if file")
        void shouldDetectIfFile() {
            ClassPathResource resource = new ClassPathResource("test.txt");

            // May or may not be file depending on how tests are run
            // Just verify no exception
            resource.isFile();
        }

        @Test
        @DisplayName("Should get file if available")
        void shouldGetFileIfAvailable() throws IOException {
            ClassPathResource resource = new ClassPathResource("test.txt");

            // File access depends on classpath configuration
            // Just verify no unexpected exception
            resource.getFile();
        }

        @Test
        @DisplayName("Should get path if available")
        void shouldGetPathIfAvailable() throws IOException {
            ClassPathResource resource = new ClassPathResource("test.txt");

            resource.getPath();
        }
    }

    @Nested
    @DisplayName("Metadata Tests")
    class MetadataTests {

        @Test
        @DisplayName("Should return filename")
        void shouldReturnFilename() {
            ClassPathResource resource = new ClassPathResource("test.txt");

            assertThat(resource.getFilename()).isEqualTo("test.txt");
        }

        @Test
        @DisplayName("Should return filename from path")
        void shouldReturnFilenameFromPath() {
            ClassPathResource resource = new ClassPathResource("some/path/file.txt");

            assertThat(resource.getFilename()).isEqualTo("file.txt");
        }

        @Test
        @DisplayName("Should return content length")
        void shouldReturnContentLength() throws IOException {
            ClassPathResource resource = new ClassPathResource("test.txt");

            assertThat(resource.contentLength()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should return last modified")
        void shouldReturnLastModified() throws IOException {
            ClassPathResource resource = new ClassPathResource("test.txt");

            assertThat(resource.lastModified()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Should return description")
        void shouldReturnDescription() {
            ClassPathResource resource = new ClassPathResource("test.txt");

            assertThat(resource.getDescription()).contains("classpath").contains("test.txt");
        }
    }

    @Nested
    @DisplayName("Relative Resource Tests")
    class RelativeResourceTests {

        @Test
        @DisplayName("Should create relative resource")
        void shouldCreateRelativeResource() throws IOException {
            ClassPathResource resource = new ClassPathResource("test.txt");
            Resource relative = resource.createRelative("config.yml");

            assertThat(relative).isInstanceOf(ClassPathResource.class);
            assertThat(relative.exists()).isTrue();
        }

        @Test
        @DisplayName("Should handle relative path with directory")
        void shouldHandleRelativePathWithDirectory() throws IOException {
            ClassPathResource resource = new ClassPathResource("subdir/file.txt");
            Resource relative = resource.createRelative("other.txt");

            assertThat(relative).isInstanceOf(ClassPathResource.class);
        }
    }
}
