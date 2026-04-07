package cloud.opencode.base.web.body;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * MultipartBodyTest Tests
 * MultipartBodyTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.3
 */
@DisplayName("MultipartBody Tests")
class MultipartBodyTest {

    @Nested
    @DisplayName("Empty Body Tests")
    class EmptyBodyTests {

        @Test
        @DisplayName("empty multipart body should have no parts")
        void emptyMultipartBodyShouldHaveNoParts() {
            MultipartBody body = MultipartBody.builder().build();

            assertThat(body.getParts()).isEmpty();
            assertThat(body.getBoundary()).isNotBlank();
        }

        @Test
        @DisplayName("empty multipart body should have valid content type")
        void emptyMultipartBodyShouldHaveValidContentType() {
            MultipartBody body = MultipartBody.builder().build();

            assertThat(body.getContentType()).startsWith("multipart/form-data; boundary=");
        }

        @Test
        @DisplayName("empty multipart body should produce closing boundary")
        void emptyMultipartBodyShouldProduceClosingBoundary() {
            MultipartBody body = MultipartBody.builder("test-boundary").build();

            assertThat(body.getBodyPublisher()).isNotNull();
            assertThat(body.getContentLength()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Text Field Tests")
    class TextFieldTests {

        @Test
        @DisplayName("should add text field correctly")
        void shouldAddTextFieldCorrectly() {
            MultipartBody body = MultipartBody.builder("boundary123")
                    .addField("name", "John")
                    .build();

            assertThat(body.getParts()).hasSize(1);
            MultipartBody.Part part = body.getParts().getFirst();
            assertThat(part.name()).isEqualTo("name");
            assertThat(part.fileName()).isNull();
            assertThat(part.contentType()).isNull();
            assertThat(new String(part.data(), StandardCharsets.UTF_8)).isEqualTo("John");
        }

        @Test
        @DisplayName("should add multiple text fields")
        void shouldAddMultipleTextFields() {
            MultipartBody body = MultipartBody.builder()
                    .addField("first", "John")
                    .addField("last", "Doe")
                    .addField("email", "john@example.com")
                    .build();

            assertThat(body.getParts()).hasSize(3);
        }

        @Test
        @DisplayName("body publisher should contain field data")
        void bodyPublisherShouldContainFieldData() {
            MultipartBody body = MultipartBody.builder("test-boundary")
                    .addField("greeting", "hello world")
                    .build();

            assertThat(body.getContentLength()).isGreaterThan(0);
            assertThat(body.getContentType()).isEqualTo("multipart/form-data; boundary=test-boundary");
        }

        @Test
        @DisplayName("null field value should throw")
        void nullFieldValueShouldThrow() {
            var builder = MultipartBody.builder();
            assertThatThrownBy(() -> builder.addField("key", null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("File Upload Tests")
    class FileUploadTests {

        @Test
        @DisplayName("should add file with byte array")
        void shouldAddFileWithByteArray() {
            byte[] data = "file content".getBytes(StandardCharsets.UTF_8);
            MultipartBody body = MultipartBody.builder()
                    .addFile("document", "test.txt", "text/plain", data)
                    .build();

            assertThat(body.getParts()).hasSize(1);
            MultipartBody.Part part = body.getParts().getFirst();
            assertThat(part.name()).isEqualTo("document");
            assertThat(part.fileName()).isEqualTo("test.txt");
            assertThat(part.contentType()).isEqualTo("text/plain");
            assertThat(new String(part.data(), StandardCharsets.UTF_8)).isEqualTo("file content");
        }

        @Test
        @DisplayName("should add file from path")
        void shouldAddFileFromPath(@TempDir Path tempDir) throws IOException {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "path file content");

            MultipartBody body = MultipartBody.builder()
                    .addFile("upload", file)
                    .build();

            assertThat(body.getParts()).hasSize(1);
            MultipartBody.Part part = body.getParts().getFirst();
            assertThat(part.name()).isEqualTo("upload");
            assertThat(part.fileName()).isEqualTo("test.txt");
            assertThat(new String(part.data(), StandardCharsets.UTF_8)).isEqualTo("path file content");
        }

        @Test
        @DisplayName("should add file from input stream")
        void shouldAddFileFromInputStream() {
            byte[] data = "stream content".getBytes(StandardCharsets.UTF_8);
            var stream = new ByteArrayInputStream(data);

            MultipartBody body = MultipartBody.builder()
                    .addFile("stream", "data.bin", "application/octet-stream", stream)
                    .build();

            assertThat(body.getParts()).hasSize(1);
            MultipartBody.Part part = body.getParts().getFirst();
            assertThat(part.name()).isEqualTo("stream");
            assertThat(part.fileName()).isEqualTo("data.bin");
            assertThat(new String(part.data(), StandardCharsets.UTF_8)).isEqualTo("stream content");
        }

        @Test
        @DisplayName("null file name should throw")
        void nullFileNameShouldThrow() {
            var builder = MultipartBody.builder();
            assertThatThrownBy(() -> builder.addFile("file", null, "text/plain", new byte[0]))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null content type should throw")
        void nullContentTypeShouldThrow() {
            var builder = MultipartBody.builder();
            assertThatThrownBy(() -> builder.addFile("file", "test.txt", null, new byte[0]))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Mixed Parts Tests")
    class MixedPartsTests {

        @Test
        @DisplayName("should support mixed text and file parts")
        void shouldSupportMixedTextAndFileParts() {
            byte[] imageData = new byte[]{0x00, 0x01, 0x02};
            MultipartBody body = MultipartBody.builder()
                    .addField("title", "My Photo")
                    .addFile("image", "photo.png", "image/png", imageData)
                    .addField("description", "A nice photo")
                    .build();

            assertThat(body.getParts()).hasSize(3);
            assertThat(body.getParts().get(0).name()).isEqualTo("title");
            assertThat(body.getParts().get(0).fileName()).isNull();
            assertThat(body.getParts().get(1).name()).isEqualTo("image");
            assertThat(body.getParts().get(1).fileName()).isEqualTo("photo.png");
            assertThat(body.getParts().get(2).name()).isEqualTo("description");
        }
    }

    @Nested
    @DisplayName("Boundary Tests")
    class BoundaryTests {

        @Test
        @DisplayName("custom boundary should be used")
        void customBoundaryShouldBeUsed() {
            MultipartBody body = MultipartBody.builder("my-custom-boundary").build();

            assertThat(body.getBoundary()).isEqualTo("my-custom-boundary");
            assertThat(body.getContentType()).isEqualTo("multipart/form-data; boundary=my-custom-boundary");
        }

        @Test
        @DisplayName("default boundary should be UUID format")
        void defaultBoundaryShouldBeUuidFormat() {
            MultipartBody body = MultipartBody.builder().build();

            assertThat(body.getBoundary()).matches(
                    "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        }

        @Test
        @DisplayName("null boundary should throw")
        void nullBoundaryShouldThrow() {
            assertThatThrownBy(() -> MultipartBody.builder(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("blank boundary should throw")
        void blankBoundaryShouldThrow() {
            assertThatThrownBy(() -> MultipartBody.builder("  "))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("RequestBody Interface Tests")
    class RequestBodyInterfaceTests {

        @Test
        @DisplayName("should implement RequestBody")
        void shouldImplementRequestBody() {
            MultipartBody body = MultipartBody.builder().build();

            assertThat(body).isInstanceOf(RequestBody.class);
        }

        @Test
        @DisplayName("content length should be positive for non-empty body")
        void contentLengthShouldBePositiveForNonEmptyBody() {
            MultipartBody body = MultipartBody.builder()
                    .addField("key", "value")
                    .build();

            assertThat(body.getContentLength()).isGreaterThan(0);
        }

        @Test
        @DisplayName("body publisher should not be null")
        void bodyPublisherShouldNotBeNull() {
            MultipartBody body = MultipartBody.builder().build();

            assertThat(body.getBodyPublisher()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Part Record Tests")
    class PartRecordTests {

        @Test
        @DisplayName("part should have defensive copy of data")
        void partShouldHaveDefensiveCopyOfData() {
            byte[] original = {1, 2, 3};
            MultipartBody.Part part = new MultipartBody.Part("field", null, null, original);

            // Modify original should not affect part
            original[0] = 99;
            assertThat(part.data()[0]).isEqualTo((byte) 1);
        }

        @Test
        @DisplayName("null part name should throw")
        void nullPartNameShouldThrow() {
            assertThatThrownBy(() -> new MultipartBody.Part(null, null, null, new byte[0]))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null part data should throw")
        void nullPartDataShouldThrow() {
            assertThatThrownBy(() -> new MultipartBody.Part("field", null, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString should include boundary and part count")
        void toStringShouldIncludeBoundaryAndPartCount() {
            MultipartBody body = MultipartBody.builder("b123")
                    .addField("a", "1")
                    .addField("b", "2")
                    .build();

            assertThat(body.toString()).contains("b123").contains("2");
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("parts list should be unmodifiable")
        void partsListShouldBeUnmodifiable() {
            MultipartBody body = MultipartBody.builder()
                    .addField("key", "value")
                    .build();

            assertThatThrownBy(() -> body.getParts().add(
                    new MultipartBody.Part("x", null, null, new byte[0])))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
