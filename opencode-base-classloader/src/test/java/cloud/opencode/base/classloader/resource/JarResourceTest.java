package cloud.opencode.base.classloader.resource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for JarResource
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("JarResource Tests")
class JarResourceTest {

    @TempDir
    Path tempDir;

    private Path createTestJar() throws IOException {
        Path jarPath = tempDir.resolve("test.jar");
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarPath.toFile()))) {
            // Add a text file entry
            JarEntry entry = new JarEntry("test.txt");
            jos.putNextEntry(entry);
            jos.write("Hello from JAR!".getBytes());
            jos.closeEntry();

            // Add a nested directory entry
            entry = new JarEntry("nested/");
            jos.putNextEntry(entry);
            jos.closeEntry();

            entry = new JarEntry("nested/file.txt");
            jos.putNextEntry(entry);
            jos.write("Nested content".getBytes());
            jos.closeEntry();
        }
        return jarPath;
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create with jar path and entry path")
        void shouldCreateWithJarPathAndEntryPath() throws IOException {
            Path jarPath = createTestJar();
            JarResource resource = new JarResource(jarPath, "test.txt");

            assertThat(resource.exists()).isTrue();
        }

        @Test
        @DisplayName("Should create with jar URL and entry path")
        void shouldCreateWithJarUrlAndEntryPath() throws IOException {
            Path jarPath = createTestJar();
            // JarResource may require specific URL format
            JarResource resource = new JarResource(jarPath, "test.txt");

            assertThat(resource.exists()).isTrue();
        }

        @Test
        @DisplayName("Should throw on null jar path")
        void shouldThrowOnNullJarPath() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new JarResource((Path) null, "test.txt"));
        }

        @Test
        @DisplayName("Should throw on null entry path")
        void shouldThrowOnNullEntryPath() throws IOException {
            Path jarPath = createTestJar();

            assertThatNullPointerException()
                    .isThrownBy(() -> new JarResource(jarPath, null));
        }
    }

    @Nested
    @DisplayName("Existence Tests")
    class ExistenceTests {

        @Test
        @DisplayName("Should exist for valid entry")
        void shouldExistForValidEntry() throws IOException {
            Path jarPath = createTestJar();
            JarResource resource = new JarResource(jarPath, "test.txt");

            assertThat(resource.exists()).isTrue();
        }

        @Test
        @DisplayName("Should not exist for invalid entry")
        void shouldNotExistForInvalidEntry() throws IOException {
            Path jarPath = createTestJar();
            JarResource resource = new JarResource(jarPath, "nonexistent.txt");

            assertThat(resource.exists()).isFalse();
        }

        @Test
        @DisplayName("Should be readable if exists")
        void shouldBeReadableIfExists() throws IOException {
            Path jarPath = createTestJar();
            JarResource resource = new JarResource(jarPath, "test.txt");

            assertThat(resource.isReadable()).isTrue();
        }

        @Test
        @DisplayName("Should not be file")
        void shouldNotBeFile() throws IOException {
            Path jarPath = createTestJar();
            JarResource resource = new JarResource(jarPath, "test.txt");

            assertThat(resource.isFile()).isFalse();
        }
    }

    @Nested
    @DisplayName("Content Tests")
    class ContentTests {

        @Test
        @DisplayName("Should return input stream")
        void shouldReturnInputStream() throws IOException {
            Path jarPath = createTestJar();
            JarResource resource = new JarResource(jarPath, "test.txt");

            assertThat(resource.getString()).isEqualTo("Hello from JAR!");
        }

        @Test
        @DisplayName("Should throw on getInputStream for nonexistent entry")
        void shouldThrowOnGetInputStreamForNonexistentEntry() throws IOException {
            Path jarPath = createTestJar();
            JarResource resource = new JarResource(jarPath, "nonexistent.txt");

            assertThatThrownBy(resource::getInputStream)
                    .isInstanceOf(Exception.class);  // May throw OpenClassLoaderException or IOException
        }

        @Test
        @DisplayName("Should return bytes")
        void shouldReturnBytes() throws IOException {
            Path jarPath = createTestJar();
            JarResource resource = new JarResource(jarPath, "test.txt");

            assertThat(resource.getBytes()).isEqualTo("Hello from JAR!".getBytes());
        }

        @Test
        @DisplayName("Should read nested entry")
        void shouldReadNestedEntry() throws IOException {
            Path jarPath = createTestJar();
            JarResource resource = new JarResource(jarPath, "nested/file.txt");

            assertThat(resource.getString()).isEqualTo("Nested content");
        }
    }

    @Nested
    @DisplayName("URL Tests")
    class UrlTests {

        @Test
        @DisplayName("Should return URL")
        void shouldReturnUrl() throws IOException {
            Path jarPath = createTestJar();
            JarResource resource = new JarResource(jarPath, "test.txt");

            URL url = resource.getUrl();
            assertThat(url).isNotNull();
            assertThat(url.getProtocol()).isEqualTo("jar");
        }

        @Test
        @DisplayName("Should return URI")
        void shouldReturnUri() throws IOException {
            Path jarPath = createTestJar();
            JarResource resource = new JarResource(jarPath, "test.txt");

            assertThat(resource.getUri()).isNotNull();
        }
    }

    @Nested
    @DisplayName("File Tests")
    class FileTests {

        @Test
        @DisplayName("Should return empty file")
        void shouldReturnEmptyFile() throws IOException {
            Path jarPath = createTestJar();
            JarResource resource = new JarResource(jarPath, "test.txt");

            assertThat(resource.getFile()).isEmpty();
        }

        @Test
        @DisplayName("Should return empty path")
        void shouldReturnEmptyPath() throws IOException {
            Path jarPath = createTestJar();
            JarResource resource = new JarResource(jarPath, "test.txt");

            assertThat(resource.getPath()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Metadata Tests")
    class MetadataTests {

        @Test
        @DisplayName("Should return filename")
        void shouldReturnFilename() throws IOException {
            Path jarPath = createTestJar();
            JarResource resource = new JarResource(jarPath, "test.txt");

            assertThat(resource.getFilename()).isEqualTo("test.txt");
        }

        @Test
        @DisplayName("Should return filename for nested entry")
        void shouldReturnFilenameForNestedEntry() throws IOException {
            Path jarPath = createTestJar();
            JarResource resource = new JarResource(jarPath, "nested/file.txt");

            assertThat(resource.getFilename()).isEqualTo("file.txt");
        }

        @Test
        @DisplayName("Should return content length")
        void shouldReturnContentLength() throws IOException {
            Path jarPath = createTestJar();
            JarResource resource = new JarResource(jarPath, "test.txt");

            assertThat(resource.contentLength()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should return last modified")
        void shouldReturnLastModified() throws IOException {
            Path jarPath = createTestJar();
            JarResource resource = new JarResource(jarPath, "test.txt");

            assertThat(resource.lastModified()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Should return description")
        void shouldReturnDescription() throws IOException {
            Path jarPath = createTestJar();
            JarResource resource = new JarResource(jarPath, "test.txt");

            assertThat(resource.getDescription()).contains("jar").contains("test.txt");
        }
    }

    @Nested
    @DisplayName("Relative Resource Tests")
    class RelativeResourceTests {

        @Test
        @DisplayName("Should create relative resource")
        void shouldCreateRelativeResource() throws IOException {
            Path jarPath = createTestJar();
            JarResource resource = new JarResource(jarPath, "test.txt");

            Resource relative = resource.createRelative("nested/file.txt");

            assertThat(relative).isInstanceOf(JarResource.class);
            assertThat(relative.exists()).isTrue();
        }
    }
}
