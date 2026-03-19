package cloud.opencode.base.classloader.resource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ByteArrayResource
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("ByteArrayResource Tests")
class ByteArrayResourceTest {

    private static final byte[] TEST_CONTENT = "Hello, World!".getBytes(StandardCharsets.UTF_8);

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create with byte array")
        void shouldCreateWithByteArray() {
            ByteArrayResource resource = new ByteArrayResource(TEST_CONTENT);

            assertThat(resource.exists()).isTrue();
            assertThat(resource.getDescription()).isNotNull();
        }

        @Test
        @DisplayName("Should create with byte array and description")
        void shouldCreateWithByteArrayAndDescription() {
            ByteArrayResource resource = new ByteArrayResource(TEST_CONTENT, "Test Resource");

            assertThat(resource.exists()).isTrue();
            assertThat(resource.getDescription()).contains("Test Resource");
        }

        @Test
        @DisplayName("Should throw on null byte array")
        void shouldThrowOnNullByteArray() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new ByteArrayResource(null));
        }
    }

    @Nested
    @DisplayName("Existence Tests")
    class ExistenceTests {

        @Test
        @DisplayName("Should always exist")
        void shouldAlwaysExist() {
            ByteArrayResource resource = new ByteArrayResource(TEST_CONTENT);

            assertThat(resource.exists()).isTrue();
        }

        @Test
        @DisplayName("Should be readable")
        void shouldBeReadable() {
            ByteArrayResource resource = new ByteArrayResource(TEST_CONTENT);

            assertThat(resource.isReadable()).isTrue();
        }

        @Test
        @DisplayName("Should not be a file")
        void shouldNotBeFile() {
            ByteArrayResource resource = new ByteArrayResource(TEST_CONTENT);

            assertThat(resource.isFile()).isFalse();
        }
    }

    @Nested
    @DisplayName("Content Tests")
    class ContentTests {

        @Test
        @DisplayName("Should return input stream")
        void shouldReturnInputStream() throws IOException {
            ByteArrayResource resource = new ByteArrayResource(TEST_CONTENT);

            try (InputStream is = resource.getInputStream()) {
                byte[] bytes = is.readAllBytes();
                assertThat(bytes).isEqualTo(TEST_CONTENT);
            }
        }

        @Test
        @DisplayName("Should return bytes")
        void shouldReturnBytes() throws IOException {
            ByteArrayResource resource = new ByteArrayResource(TEST_CONTENT);

            assertThat(resource.getBytes()).isEqualTo(TEST_CONTENT);
        }

        @Test
        @DisplayName("Should return string")
        void shouldReturnString() throws IOException {
            ByteArrayResource resource = new ByteArrayResource(TEST_CONTENT);

            assertThat(resource.getString()).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("Should return content length")
        void shouldReturnContentLength() throws IOException {
            ByteArrayResource resource = new ByteArrayResource(TEST_CONTENT);

            assertThat(resource.contentLength()).isEqualTo(TEST_CONTENT.length);
        }

        @Test
        @DisplayName("Should create multiple independent streams")
        void shouldCreateMultipleIndependentStreams() throws IOException {
            ByteArrayResource resource = new ByteArrayResource(TEST_CONTENT);

            try (InputStream is1 = resource.getInputStream();
                 InputStream is2 = resource.getInputStream()) {
                assertThat(is1.readAllBytes()).isEqualTo(TEST_CONTENT);
                assertThat(is2.readAllBytes()).isEqualTo(TEST_CONTENT);
            }
        }
    }

    @Nested
    @DisplayName("File/URL Access Tests")
    class FileUrlAccessTests {

        @Test
        @DisplayName("Should return empty file")
        void shouldReturnEmptyFile() {
            ByteArrayResource resource = new ByteArrayResource(TEST_CONTENT);

            assertThat(resource.getFile()).isEmpty();
        }

        @Test
        @DisplayName("Should return empty path")
        void shouldReturnEmptyPath() {
            ByteArrayResource resource = new ByteArrayResource(TEST_CONTENT);

            assertThat(resource.getPath()).isEmpty();
        }

        @Test
        @DisplayName("Should throw on getUrl")
        void shouldThrowOnGetUrl() {
            ByteArrayResource resource = new ByteArrayResource(TEST_CONTENT);

            assertThatThrownBy(resource::getUrl)
                    .isInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("Should throw on getUri")
        void shouldThrowOnGetUri() {
            ByteArrayResource resource = new ByteArrayResource(TEST_CONTENT);

            assertThatThrownBy(resource::getUri)
                    .isInstanceOf(IOException.class);
        }
    }

    @Nested
    @DisplayName("Other Methods Tests")
    class OtherMethodsTests {

        @Test
        @DisplayName("Should return null filename")
        void shouldReturnNullFilename() {
            ByteArrayResource resource = new ByteArrayResource(TEST_CONTENT);

            assertThat(resource.getFilename()).isNull();
        }

        @Test
        @DisplayName("Should return last modified as 0")
        void shouldReturnLastModifiedAsZero() throws IOException {
            ByteArrayResource resource = new ByteArrayResource(TEST_CONTENT);

            assertThat(resource.lastModified()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should throw on createRelative")
        void shouldThrowOnCreateRelative() {
            ByteArrayResource resource = new ByteArrayResource(TEST_CONTENT);

            assertThatThrownBy(() -> resource.createRelative("other.txt"))
                    .isInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("Should read lines")
        void shouldReadLines() throws IOException {
            byte[] multilineContent = "Line1\nLine2\nLine3".getBytes(StandardCharsets.UTF_8);
            ByteArrayResource resource = new ByteArrayResource(multilineContent);

            assertThat(resource.readLines()).containsExactly("Line1", "Line2", "Line3");
        }
    }
}
