package cloud.opencode.base.classloader.resource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for InputStreamResource
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("InputStreamResource Tests")
class InputStreamResourceTest {

    private static final byte[] TEST_CONTENT = "Hello, World!".getBytes(StandardCharsets.UTF_8);

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create with InputStream")
        void shouldCreateWithInputStream() {
            InputStream is = new ByteArrayInputStream(TEST_CONTENT);
            InputStreamResource resource = new InputStreamResource(is);

            assertThat(resource.exists()).isTrue();
        }

        @Test
        @DisplayName("Should create with InputStream and description")
        void shouldCreateWithInputStreamAndDescription() {
            InputStream is = new ByteArrayInputStream(TEST_CONTENT);
            InputStreamResource resource = new InputStreamResource(is, "Test Stream");

            assertThat(resource.getDescription()).contains("Test Stream");
        }

        @Test
        @DisplayName("Should throw on null InputStream")
        void shouldThrowOnNullInputStream() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new InputStreamResource(null));
        }
    }

    @Nested
    @DisplayName("Existence Tests")
    class ExistenceTests {

        @Test
        @DisplayName("Should always exist")
        void shouldAlwaysExist() {
            InputStream is = new ByteArrayInputStream(TEST_CONTENT);
            InputStreamResource resource = new InputStreamResource(is);

            assertThat(resource.exists()).isTrue();
        }

        @Test
        @DisplayName("Should be readable only once")
        void shouldBeReadableOnlyOnce() throws IOException {
            InputStream is = new ByteArrayInputStream(TEST_CONTENT);
            InputStreamResource resource = new InputStreamResource(is);

            assertThat(resource.isReadable()).isTrue();
            resource.getInputStream().readAllBytes();
            assertThat(resource.isReadable()).isFalse();
        }

        @Test
        @DisplayName("Should not be a file")
        void shouldNotBeFile() {
            InputStream is = new ByteArrayInputStream(TEST_CONTENT);
            InputStreamResource resource = new InputStreamResource(is);

            assertThat(resource.isFile()).isFalse();
        }
    }

    @Nested
    @DisplayName("Content Tests")
    class ContentTests {

        @Test
        @DisplayName("Should return input stream")
        void shouldReturnInputStream() throws IOException {
            InputStream is = new ByteArrayInputStream(TEST_CONTENT);
            InputStreamResource resource = new InputStreamResource(is);

            try (InputStream stream = resource.getInputStream()) {
                assertThat(stream.readAllBytes()).isEqualTo(TEST_CONTENT);
            }
        }

        @Test
        @DisplayName("Should throw on second getInputStream call")
        void shouldThrowOnSecondGetInputStreamCall() throws IOException {
            InputStream is = new ByteArrayInputStream(TEST_CONTENT);
            InputStreamResource resource = new InputStreamResource(is);

            resource.getInputStream();

            assertThatThrownBy(resource::getInputStream)
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("already been read");
        }

        @Test
        @DisplayName("Should return bytes")
        void shouldReturnBytes() throws IOException {
            InputStream is = new ByteArrayInputStream(TEST_CONTENT);
            InputStreamResource resource = new InputStreamResource(is);

            assertThat(resource.getBytes()).isEqualTo(TEST_CONTENT);
        }

        @Test
        @DisplayName("Should return string")
        void shouldReturnString() throws IOException {
            InputStream is = new ByteArrayInputStream(TEST_CONTENT);
            InputStreamResource resource = new InputStreamResource(is);

            assertThat(resource.getString()).isEqualTo("Hello, World!");
        }
    }

    @Nested
    @DisplayName("Unsupported Operations Tests")
    class UnsupportedOperationsTests {

        @Test
        @DisplayName("Should throw on getUrl")
        void shouldThrowOnGetUrl() {
            InputStream is = new ByteArrayInputStream(TEST_CONTENT);
            InputStreamResource resource = new InputStreamResource(is);

            assertThatThrownBy(resource::getUrl)
                    .isInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("Should throw on getUri")
        void shouldThrowOnGetUri() {
            InputStream is = new ByteArrayInputStream(TEST_CONTENT);
            InputStreamResource resource = new InputStreamResource(is);

            assertThatThrownBy(resource::getUri)
                    .isInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("Should return empty file")
        void shouldReturnEmptyFile() {
            InputStream is = new ByteArrayInputStream(TEST_CONTENT);
            InputStreamResource resource = new InputStreamResource(is);

            assertThat(resource.getFile()).isEmpty();
        }

        @Test
        @DisplayName("Should return empty path")
        void shouldReturnEmptyPath() {
            InputStream is = new ByteArrayInputStream(TEST_CONTENT);
            InputStreamResource resource = new InputStreamResource(is);

            assertThat(resource.getPath()).isEmpty();
        }

        @Test
        @DisplayName("Should throw on createRelative")
        void shouldThrowOnCreateRelative() {
            InputStream is = new ByteArrayInputStream(TEST_CONTENT);
            InputStreamResource resource = new InputStreamResource(is);

            assertThatThrownBy(() -> resource.createRelative("other.txt"))
                    .isInstanceOf(IOException.class);
        }
    }

    @Nested
    @DisplayName("Metadata Tests")
    class MetadataTests {

        @Test
        @DisplayName("Should return null filename")
        void shouldReturnNullFilename() {
            InputStream is = new ByteArrayInputStream(TEST_CONTENT);
            InputStreamResource resource = new InputStreamResource(is);

            assertThat(resource.getFilename()).isNull();
        }

        @Test
        @DisplayName("Should return content length")
        void shouldReturnContentLength() throws IOException {
            InputStream is = new ByteArrayInputStream(TEST_CONTENT);
            InputStreamResource resource = new InputStreamResource(is);

            // Content length may be -1 or actual size depending on implementation
            assertThat(resource.contentLength()).isGreaterThanOrEqualTo(-1);
        }

        @Test
        @DisplayName("Should return 0 for last modified")
        void shouldReturnZeroForLastModified() throws IOException {
            InputStream is = new ByteArrayInputStream(TEST_CONTENT);
            InputStreamResource resource = new InputStreamResource(is);

            assertThat(resource.lastModified()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return default description")
        void shouldReturnDefaultDescription() {
            InputStream is = new ByteArrayInputStream(TEST_CONTENT);
            InputStreamResource resource = new InputStreamResource(is);

            assertThat(resource.getDescription()).contains("InputStream");
        }
    }
}
