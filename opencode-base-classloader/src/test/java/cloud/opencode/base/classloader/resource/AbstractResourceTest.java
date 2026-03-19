package cloud.opencode.base.classloader.resource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for AbstractResource base class
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("AbstractResource Tests")
class AbstractResourceTest {

    private static final byte[] TEST_CONTENT = "Hello, World!".getBytes(StandardCharsets.UTF_8);

    /**
     * Test implementation of AbstractResource for testing
     */
    private static class TestResource extends AbstractResource {
        private final byte[] content;
        private final String description;
        private boolean throwOnInputStream = false;

        TestResource(byte[] content, String description) {
            this.content = content;
            this.description = description;
        }

        void setThrowOnInputStream(boolean throwOnInputStream) {
            this.throwOnInputStream = throwOnInputStream;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            if (throwOnInputStream) {
                throw new IOException("Test exception");
            }
            return new ByteArrayInputStream(content);
        }

        @Override
        public URL getUrl() throws IOException {
            return new URL("file:///test");
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public Resource createRelative(String relativePath) throws IOException {
            return new TestResource(content, description + "/" + relativePath);
        }
    }

    @Nested
    @DisplayName("Existence Tests")
    class ExistenceTests {

        @Test
        @DisplayName("Should return true for exists when stream available")
        void shouldReturnTrueForExistsWhenStreamAvailable() {
            TestResource resource = new TestResource(TEST_CONTENT, "test");

            assertThat(resource.exists()).isTrue();
        }

        @Test
        @DisplayName("Should return false for exists when stream throws")
        void shouldReturnFalseForExistsWhenStreamThrows() {
            TestResource resource = new TestResource(TEST_CONTENT, "test");
            resource.setThrowOnInputStream(true);

            assertThat(resource.exists()).isFalse();
        }
    }

    @Nested
    @DisplayName("Readability Tests")
    class ReadabilityTests {

        @Test
        @DisplayName("Should return true for isReadable when exists")
        void shouldReturnTrueForIsReadableWhenExists() {
            TestResource resource = new TestResource(TEST_CONTENT, "test");

            assertThat(resource.isReadable()).isTrue();
        }

        @Test
        @DisplayName("Should return false for isReadable when not exists")
        void shouldReturnFalseForIsReadableWhenNotExists() {
            TestResource resource = new TestResource(TEST_CONTENT, "test");
            resource.setThrowOnInputStream(true);

            assertThat(resource.isReadable()).isFalse();
        }
    }

    @Nested
    @DisplayName("File Tests")
    class FileTests {

        @Test
        @DisplayName("Should return false for isFile by default")
        void shouldReturnFalseForIsFileByDefault() {
            TestResource resource = new TestResource(TEST_CONTENT, "test");

            assertThat(resource.isFile()).isFalse();
        }

        @Test
        @DisplayName("Should return empty for getFile by default")
        void shouldReturnEmptyForGetFileByDefault() {
            TestResource resource = new TestResource(TEST_CONTENT, "test");

            assertThat(resource.getFile()).isEmpty();
        }

        @Test
        @DisplayName("Should return empty for getPath by default")
        void shouldReturnEmptyForGetPathByDefault() {
            TestResource resource = new TestResource(TEST_CONTENT, "test");

            assertThat(resource.getPath()).isEmpty();
        }
    }

    @Nested
    @DisplayName("URI Tests")
    class UriTests {

        @Test
        @DisplayName("Should convert URL to URI")
        void shouldConvertUrlToUri() throws IOException {
            TestResource resource = new TestResource(TEST_CONTENT, "test");

            assertThat(resource.getUri()).isNotNull();
            assertThat(resource.getUri().toString()).contains("test");
        }
    }

    @Nested
    @DisplayName("Content Length Tests")
    class ContentLengthTests {

        @Test
        @DisplayName("Should return content length from bytes")
        void shouldReturnContentLengthFromBytes() throws IOException {
            TestResource resource = new TestResource(TEST_CONTENT, "test");

            assertThat(resource.contentLength()).isEqualTo(TEST_CONTENT.length);
        }
    }

    @Nested
    @DisplayName("Last Modified Tests")
    class LastModifiedTests {

        @Test
        @DisplayName("Should return zero for lastModified by default")
        void shouldReturnZeroForLastModifiedByDefault() throws IOException {
            TestResource resource = new TestResource(TEST_CONTENT, "test");

            assertThat(resource.lastModified()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Filename Tests")
    class FilenameTests {

        @Test
        @DisplayName("Should return null for filename by default")
        void shouldReturnNullForFilenameByDefault() {
            TestResource resource = new TestResource(TEST_CONTENT, "test");

            assertThat(resource.getFilename()).isNull();
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return description for toString")
        void shouldReturnDescriptionForToString() {
            TestResource resource = new TestResource(TEST_CONTENT, "test description");

            assertThat(resource.toString()).isEqualTo("test description");
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal for same description")
        void shouldBeEqualForSameDescription() {
            TestResource r1 = new TestResource(TEST_CONTENT, "same");
            TestResource r2 = new TestResource(TEST_CONTENT, "same");

            assertThat(r1).isEqualTo(r2);
            assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal for different descriptions")
        void shouldNotBeEqualForDifferentDescriptions() {
            TestResource r1 = new TestResource(TEST_CONTENT, "desc1");
            TestResource r2 = new TestResource(TEST_CONTENT, "desc2");

            assertThat(r1).isNotEqualTo(r2);
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            TestResource resource = new TestResource(TEST_CONTENT, "test");

            assertThat(resource).isEqualTo(resource);
        }

        @Test
        @DisplayName("Should not be equal to non-Resource")
        void shouldNotBeEqualToNonResource() {
            TestResource resource = new TestResource(TEST_CONTENT, "test");

            assertThat(resource).isNotEqualTo("not a resource");
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            TestResource resource = new TestResource(TEST_CONTENT, "test");

            assertThat(resource).isNotEqualTo(null);
        }
    }

    @Nested
    @DisplayName("Create Relative Tests")
    class CreateRelativeTests {

        @Test
        @DisplayName("Should create relative resource")
        void shouldCreateRelativeResource() throws IOException {
            TestResource resource = new TestResource(TEST_CONTENT, "base");

            Resource relative = resource.createRelative("child");

            assertThat(relative.getDescription()).contains("base");
            assertThat(relative.getDescription()).contains("child");
        }
    }
}
