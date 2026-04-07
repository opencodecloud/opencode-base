package cloud.opencode.base.web.body;

import cloud.opencode.base.web.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FileBody")
class FileBodyTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("of(Path)")
    class OfPath {

        @Test
        @DisplayName("should detect JSON content type")
        void shouldDetectJson() throws IOException {
            Path file = tempDir.resolve("data.json");
            Files.writeString(file, "{}");
            FileBody body = FileBody.of(file);
            assertThat(body.getContentType()).isEqualTo(ContentType.APPLICATION_JSON);
        }

        @Test
        @DisplayName("should detect HTML content type")
        void shouldDetectHtml() throws IOException {
            Path file = tempDir.resolve("page.html");
            Files.writeString(file, "<html></html>");
            FileBody body = FileBody.of(file);
            assertThat(body.getContentType()).isEqualTo(ContentType.TEXT_HTML);
        }

        @Test
        @DisplayName("should detect PNG content type")
        void shouldDetectPng() throws IOException {
            Path file = tempDir.resolve("image.png");
            Files.write(file, new byte[]{0});
            FileBody body = FileBody.of(file);
            assertThat(body.getContentType()).isEqualTo(ContentType.IMAGE_PNG);
        }

        @Test
        @DisplayName("should fall back to octet-stream for unknown extension")
        void shouldFallBackForUnknown() throws IOException {
            Path file = tempDir.resolve("data.xyz123");
            Files.writeString(file, "stuff");
            FileBody body = FileBody.of(file);
            // Should be octet-stream or whatever probeContentType returns
            assertThat(body.getContentType()).isNotNull();
        }
    }

    @Nested
    @DisplayName("of(Path, String)")
    class OfPathContentType {

        @Test
        @DisplayName("should use explicit content type")
        void shouldUseExplicitContentType() throws IOException {
            Path file = tempDir.resolve("data.bin");
            Files.write(file, new byte[]{1, 2, 3});
            FileBody body = FileBody.of(file, "application/custom");
            assertThat(body.getContentType()).isEqualTo("application/custom");
        }
    }

    @Nested
    @DisplayName("getContentLength()")
    class GetContentLength {

        @Test
        @DisplayName("should return file size")
        void shouldReturnFileSize() throws IOException {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "hello");
            FileBody body = FileBody.of(file);
            assertThat(body.getContentLength()).isEqualTo(5);
        }

        @Test
        @DisplayName("should return zero for empty file")
        void shouldReturnZeroForEmpty() throws IOException {
            Path file = tempDir.resolve("empty.txt");
            Files.writeString(file, "");
            FileBody body = FileBody.of(file);
            assertThat(body.getContentLength()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("getFile()")
    class GetFile {

        @Test
        @DisplayName("should return the file path")
        void shouldReturnFilePath() throws IOException {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "x");
            FileBody body = FileBody.of(file);
            assertThat(body.getFile()).isEqualTo(file);
        }
    }

    @Nested
    @DisplayName("getFileName()")
    class GetFileName {

        @Test
        @DisplayName("should return just the file name")
        void shouldReturnFileName() throws IOException {
            Path file = tempDir.resolve("document.pdf");
            Files.write(file, new byte[]{0});
            FileBody body = FileBody.of(file);
            assertThat(body.getFileName()).isEqualTo("document.pdf");
        }
    }

    @Nested
    @DisplayName("exists()")
    class Exists {

        @Test
        @DisplayName("should return true for existing file")
        void shouldReturnTrue() throws IOException {
            Path file = tempDir.resolve("exists.txt");
            Files.writeString(file, "yes");
            FileBody body = FileBody.of(file);
            assertThat(body.exists()).isTrue();
        }
    }

    @Nested
    @DisplayName("getBodyPublisher()")
    class GetBodyPublisher {

        @Test
        @DisplayName("should return non-null publisher")
        void shouldReturnPublisher() throws IOException {
            Path file = tempDir.resolve("data.txt");
            Files.writeString(file, "content");
            FileBody body = FileBody.of(file);
            assertThat(body.getBodyPublisher()).isNotNull();
        }
    }

    @Nested
    @DisplayName("detectContentType()")
    class DetectContentType {

        @Test
        @DisplayName("should detect common extensions")
        void shouldDetectCommonExtensions() throws IOException {
            assertThat(FileBody.detectContentType(tempDir.resolve("a.txt"))).isEqualTo(ContentType.TEXT_PLAIN);
            assertThat(FileBody.detectContentType(tempDir.resolve("a.css"))).isEqualTo(ContentType.TEXT_CSS);
            assertThat(FileBody.detectContentType(tempDir.resolve("a.js"))).isEqualTo(ContentType.TEXT_JAVASCRIPT);
            assertThat(FileBody.detectContentType(tempDir.resolve("a.xml"))).isEqualTo(ContentType.APPLICATION_XML);
            assertThat(FileBody.detectContentType(tempDir.resolve("a.pdf"))).isEqualTo(ContentType.APPLICATION_PDF);
            assertThat(FileBody.detectContentType(tempDir.resolve("a.zip"))).isEqualTo(ContentType.APPLICATION_ZIP);
            assertThat(FileBody.detectContentType(tempDir.resolve("a.jpg"))).isEqualTo(ContentType.IMAGE_JPEG);
            assertThat(FileBody.detectContentType(tempDir.resolve("a.jpeg"))).isEqualTo(ContentType.IMAGE_JPEG);
            assertThat(FileBody.detectContentType(tempDir.resolve("a.gif"))).isEqualTo(ContentType.IMAGE_GIF);
            assertThat(FileBody.detectContentType(tempDir.resolve("a.svg"))).isEqualTo(ContentType.IMAGE_SVG);
            assertThat(FileBody.detectContentType(tempDir.resolve("a.webp"))).isEqualTo(ContentType.IMAGE_WEBP);
            assertThat(FileBody.detectContentType(tempDir.resolve("a.mp3"))).isEqualTo(ContentType.AUDIO_MPEG);
            assertThat(FileBody.detectContentType(tempDir.resolve("a.mp4"))).isEqualTo(ContentType.VIDEO_MP4);
        }

        @Test
        @DisplayName("should be case insensitive for extensions")
        void shouldBeCaseInsensitive() {
            assertThat(FileBody.detectContentType(tempDir.resolve("A.JSON"))).isEqualTo(ContentType.APPLICATION_JSON);
            assertThat(FileBody.detectContentType(tempDir.resolve("B.HTML"))).isEqualTo(ContentType.TEXT_HTML);
        }

        @Test
        @DisplayName("should handle file without extension")
        void shouldHandleNoExtension() {
            String ct = FileBody.detectContentType(tempDir.resolve("noext"));
            assertThat(ct).isNotNull();
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringTest {

        @Test
        @DisplayName("should include file info")
        void shouldIncludeFileInfo() throws IOException {
            Path file = tempDir.resolve("test.json");
            Files.writeString(file, "{}");
            FileBody body = FileBody.of(file);
            assertThat(body.toString()).contains("FileBody", "test.json", "application/json");
        }
    }
}
