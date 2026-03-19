package cloud.opencode.base.io;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenMimeType 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("OpenMimeType 测试")
class OpenMimeTypeTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("APPLICATION_OCTET_STREAM常量")
        void testApplicationOctetStream() {
            assertThat(OpenMimeType.APPLICATION_OCTET_STREAM).isEqualTo("application/octet-stream");
        }

        @Test
        @DisplayName("TEXT_PLAIN常量")
        void testTextPlain() {
            assertThat(OpenMimeType.TEXT_PLAIN).isEqualTo("text/plain");
        }

        @Test
        @DisplayName("TEXT_HTML常量")
        void testTextHtml() {
            assertThat(OpenMimeType.TEXT_HTML).isEqualTo("text/html");
        }

        @Test
        @DisplayName("APPLICATION_JSON常量")
        void testApplicationJson() {
            assertThat(OpenMimeType.APPLICATION_JSON).isEqualTo("application/json");
        }

        @Test
        @DisplayName("APPLICATION_XML常量")
        void testApplicationXml() {
            assertThat(OpenMimeType.APPLICATION_XML).isEqualTo("application/xml");
        }

        @Test
        @DisplayName("APPLICATION_PDF常量")
        void testApplicationPdf() {
            assertThat(OpenMimeType.APPLICATION_PDF).isEqualTo("application/pdf");
        }
    }

    @Nested
    @DisplayName("fromExtension方法测试")
    class FromExtensionTests {

        @Test
        @DisplayName("txt扩展名返回text/plain")
        void testTxt() {
            Optional<String> mime = OpenMimeType.fromExtension("txt");

            assertThat(mime).contains("text/plain");
        }

        @Test
        @DisplayName("html扩展名返回text/html")
        void testHtml() {
            Optional<String> mime = OpenMimeType.fromExtension("html");

            assertThat(mime).contains("text/html");
        }

        @Test
        @DisplayName("json扩展名返回application/json")
        void testJson() {
            Optional<String> mime = OpenMimeType.fromExtension("json");

            assertThat(mime).contains("application/json");
        }

        @Test
        @DisplayName("pdf扩展名返回application/pdf")
        void testPdf() {
            Optional<String> mime = OpenMimeType.fromExtension("pdf");

            assertThat(mime).contains("application/pdf");
        }

        @Test
        @DisplayName("jpg扩展名返回image/jpeg")
        void testJpg() {
            Optional<String> mime = OpenMimeType.fromExtension("jpg");

            assertThat(mime).contains("image/jpeg");
        }

        @Test
        @DisplayName("png扩展名返回image/png")
        void testPng() {
            Optional<String> mime = OpenMimeType.fromExtension("png");

            assertThat(mime).contains("image/png");
        }

        @Test
        @DisplayName("未知扩展名返回空")
        void testUnknown() {
            Optional<String> mime = OpenMimeType.fromExtension("unknown123");

            assertThat(mime).isEmpty();
        }

        @Test
        @DisplayName("null返回空")
        void testNull() {
            Optional<String> mime = OpenMimeType.fromExtension(null);

            assertThat(mime).isEmpty();
        }

        @Test
        @DisplayName("空字符串返回空")
        void testEmpty() {
            Optional<String> mime = OpenMimeType.fromExtension("");

            assertThat(mime).isEmpty();
        }

        @Test
        @DisplayName("带点的扩展名能正确处理")
        void testWithDot() {
            Optional<String> mime = OpenMimeType.fromExtension(".txt");

            assertThat(mime).contains("text/plain");
        }

        @Test
        @DisplayName("大写扩展名能正确处理")
        void testUpperCase() {
            Optional<String> mime = OpenMimeType.fromExtension("TXT");

            assertThat(mime).contains("text/plain");
        }
    }

    @Nested
    @DisplayName("fromExtension带默认值方法测试")
    class FromExtensionWithDefaultTests {

        @Test
        @DisplayName("已知扩展名返回MIME类型")
        void testKnown() {
            String mime = OpenMimeType.fromExtension("txt", "default/type");

            assertThat(mime).isEqualTo("text/plain");
        }

        @Test
        @DisplayName("未知扩展名返回默认值")
        void testUnknown() {
            String mime = OpenMimeType.fromExtension("unknown", "default/type");

            assertThat(mime).isEqualTo("default/type");
        }
    }

    @Nested
    @DisplayName("fromPath方法测试")
    class FromPathTests {

        @Test
        @DisplayName("从路径获取MIME类型")
        void testFromPath() {
            Path path = Path.of("document.pdf");

            Optional<String> mime = OpenMimeType.fromPath(path);

            assertThat(mime).contains("application/pdf");
        }

        @Test
        @DisplayName("null路径返回空")
        void testNullPath() {
            Optional<String> mime = OpenMimeType.fromPath(null);

            assertThat(mime).isEmpty();
        }
    }

    @Nested
    @DisplayName("fromFilename方法测试")
    class FromFilenameTests {

        @Test
        @DisplayName("从文件名获取MIME类型")
        void testFromFilename() {
            Optional<String> mime = OpenMimeType.fromFilename("image.png");

            assertThat(mime).contains("image/png");
        }

        @Test
        @DisplayName("null文件名返回空")
        void testNullFilename() {
            Optional<String> mime = OpenMimeType.fromFilename(null);

            assertThat(mime).isEmpty();
        }
    }

    @Nested
    @DisplayName("fromContent(Path)方法测试")
    class FromContentPathTests {

        @Test
        @DisplayName("检测PNG文件")
        void testPng() throws Exception {
            Path file = tempDir.resolve("test.png");
            // PNG magic number: 89 50 4E 47 0D 0A 1A 0A
            byte[] pngHeader = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
            Files.write(file, pngHeader);

            Optional<String> mime = OpenMimeType.fromContent(file);

            assertThat(mime).contains("image/png");
        }

        @Test
        @DisplayName("检测JPEG文件")
        void testJpeg() throws Exception {
            Path file = tempDir.resolve("test.jpg");
            // JPEG magic number: FF D8 FF
            byte[] jpegHeader = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0};
            Files.write(file, jpegHeader);

            Optional<String> mime = OpenMimeType.fromContent(file);

            assertThat(mime).contains("image/jpeg");
        }

        @Test
        @DisplayName("检测PDF文件")
        void testPdf() throws Exception {
            Path file = tempDir.resolve("test.pdf");
            // PDF magic number: 25 50 44 46 (%PDF)
            byte[] pdfHeader = {0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x34};
            Files.write(file, pdfHeader);

            Optional<String> mime = OpenMimeType.fromContent(file);

            assertThat(mime).contains("application/pdf");
        }

        @Test
        @DisplayName("检测ZIP文件")
        void testZip() throws Exception {
            Path file = tempDir.resolve("test.zip");
            // ZIP magic number: 50 4B 03 04
            byte[] zipHeader = {0x50, 0x4B, 0x03, 0x04, 0, 0, 0, 0};
            Files.write(file, zipHeader);

            Optional<String> mime = OpenMimeType.fromContent(file);

            assertThat(mime).contains("application/zip");
        }

        @Test
        @DisplayName("检测GIF文件")
        void testGif() throws Exception {
            Path file = tempDir.resolve("test.gif");
            // GIF magic number: 47 49 46 38 (GIF8)
            byte[] gifHeader = {0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0, 0};
            Files.write(file, gifHeader);

            Optional<String> mime = OpenMimeType.fromContent(file);

            assertThat(mime).contains("image/gif");
        }

        @Test
        @DisplayName("未知内容返回空")
        void testUnknownContent() throws Exception {
            Path file = tempDir.resolve("test.bin");
            Files.write(file, new byte[]{1, 2, 3, 4, 5, 6, 7, 8});

            Optional<String> mime = OpenMimeType.fromContent(file);

            assertThat(mime).isEmpty();
        }
    }

    @Nested
    @DisplayName("fromContent(byte[])方法测试")
    class FromContentBytesTests {

        @Test
        @DisplayName("检测PNG数据")
        void testPngBytes() {
            byte[] data = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};

            Optional<String> mime = OpenMimeType.fromContent(data);

            assertThat(mime).contains("image/png");
        }

        @Test
        @DisplayName("null数据返回空")
        void testNullBytes() {
            Optional<String> mime = OpenMimeType.fromContent((byte[]) null);

            assertThat(mime).isEmpty();
        }

        @Test
        @DisplayName("数据太短返回空")
        void testTooShort() {
            Optional<String> mime = OpenMimeType.fromContent(new byte[]{1});

            assertThat(mime).isEmpty();
        }

        @Test
        @DisplayName("检测WebP数据")
        void testWebP() {
            // RIFF....WEBP pattern
            byte[] data = {0x52, 0x49, 0x46, 0x46, 0, 0, 0, 0, 0x57, 0x45, 0x42, 0x50};

            Optional<String> mime = OpenMimeType.fromContent(data);

            assertThat(mime).contains("image/webp");
        }

        @Test
        @DisplayName("检测MP4数据")
        void testMp4() {
            // ftyp box at offset 4
            byte[] data = new byte[16];
            data[4] = 0x66; // f
            data[5] = 0x74; // t
            data[6] = 0x79; // y
            data[7] = 0x70; // p
            data[8] = 0x69; // i
            data[9] = 0x73; // s
            data[10] = 0x6F; // o
            data[11] = 0x6D; // m

            Optional<String> mime = OpenMimeType.fromContent(data);

            assertThat(mime).contains("video/mp4");
        }
    }

    @Nested
    @DisplayName("fromContent(InputStream)方法测试")
    class FromContentInputStreamTests {

        @Test
        @DisplayName("检测PNG流")
        void testPngStream() {
            byte[] data = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
            InputStream is = new ByteArrayInputStream(data);

            Optional<String> mime = OpenMimeType.fromContent(is);

            assertThat(mime).contains("image/png");
        }

        @Test
        @DisplayName("null流返回空")
        void testNullStream() {
            Optional<String> mime = OpenMimeType.fromContent((InputStream) null);

            assertThat(mime).isEmpty();
        }
    }

    @Nested
    @DisplayName("detect方法测试")
    class DetectTests {

        @Test
        @DisplayName("优先使用内容检测")
        void testContentFirst() throws Exception {
            Path file = tempDir.resolve("test.txt");
            // PNG header but .txt extension
            byte[] pngHeader = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
            Files.write(file, pngHeader);

            String mime = OpenMimeType.detect(file);

            assertThat(mime).isEqualTo("image/png");
        }

        @Test
        @DisplayName("内容未知时使用扩展名")
        void testFallbackToExtension() throws Exception {
            Path file = tempDir.resolve("test.json");
            Files.writeString(file, "{}");

            String mime = OpenMimeType.detect(file);

            assertThat(mime).isEqualTo("application/json");
        }

        @Test
        @DisplayName("都未知时返回默认类型")
        void testDefaultType() throws Exception {
            Path file = tempDir.resolve("test.unknown123");
            Files.write(file, new byte[]{1, 2, 3, 4, 5});

            String mime = OpenMimeType.detect(file);

            assertThat(mime).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("detectByExtension方法测试")
    class DetectByExtensionTests {

        @Test
        @DisplayName("优先使用扩展名")
        void testExtensionFirst() throws Exception {
            Path file = tempDir.resolve("test.json");
            Files.writeString(file, "content");

            String mime = OpenMimeType.detectByExtension(file);

            assertThat(mime).isEqualTo("application/json");
        }
    }

    @Nested
    @DisplayName("isText方法测试")
    class IsTextTests {

        @Test
        @DisplayName("text/开头返回true")
        void testTextPrefix() {
            assertThat(OpenMimeType.isText("text/plain")).isTrue();
            assertThat(OpenMimeType.isText("text/html")).isTrue();
        }

        @Test
        @DisplayName("application/json返回true")
        void testJson() {
            assertThat(OpenMimeType.isText("application/json")).isTrue();
        }

        @Test
        @DisplayName("application/xml返回true")
        void testXml() {
            assertThat(OpenMimeType.isText("application/xml")).isTrue();
        }

        @Test
        @DisplayName("application/javascript返回true")
        void testJavascript() {
            assertThat(OpenMimeType.isText("application/javascript")).isTrue();
        }

        @Test
        @DisplayName("+xml后缀返回true")
        void testXmlSuffix() {
            assertThat(OpenMimeType.isText("application/svg+xml")).isTrue();
        }

        @Test
        @DisplayName("+json后缀返回true")
        void testJsonSuffix() {
            assertThat(OpenMimeType.isText("application/ld+json")).isTrue();
        }

        @Test
        @DisplayName("二进制类型返回false")
        void testBinary() {
            assertThat(OpenMimeType.isText("image/png")).isFalse();
            assertThat(OpenMimeType.isText("application/octet-stream")).isFalse();
        }

        @Test
        @DisplayName("null返回false")
        void testNull() {
            assertThat(OpenMimeType.isText(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("isImage方法测试")
    class IsImageTests {

        @Test
        @DisplayName("image/开头返回true")
        void testImagePrefix() {
            assertThat(OpenMimeType.isImage("image/png")).isTrue();
            assertThat(OpenMimeType.isImage("image/jpeg")).isTrue();
            assertThat(OpenMimeType.isImage("image/gif")).isTrue();
        }

        @Test
        @DisplayName("非图片类型返回false")
        void testNonImage() {
            assertThat(OpenMimeType.isImage("text/plain")).isFalse();
        }

        @Test
        @DisplayName("null返回false")
        void testNull() {
            assertThat(OpenMimeType.isImage(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("isAudio方法测试")
    class IsAudioTests {

        @Test
        @DisplayName("audio/开头返回true")
        void testAudioPrefix() {
            assertThat(OpenMimeType.isAudio("audio/mpeg")).isTrue();
            assertThat(OpenMimeType.isAudio("audio/wav")).isTrue();
        }

        @Test
        @DisplayName("非音频类型返回false")
        void testNonAudio() {
            assertThat(OpenMimeType.isAudio("video/mp4")).isFalse();
        }

        @Test
        @DisplayName("null返回false")
        void testNull() {
            assertThat(OpenMimeType.isAudio(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("isVideo方法测试")
    class IsVideoTests {

        @Test
        @DisplayName("video/开头返回true")
        void testVideoPrefix() {
            assertThat(OpenMimeType.isVideo("video/mp4")).isTrue();
            assertThat(OpenMimeType.isVideo("video/webm")).isTrue();
        }

        @Test
        @DisplayName("非视频类型返回false")
        void testNonVideo() {
            assertThat(OpenMimeType.isVideo("audio/mpeg")).isFalse();
        }

        @Test
        @DisplayName("null返回false")
        void testNull() {
            assertThat(OpenMimeType.isVideo(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("isBinary方法测试")
    class IsBinaryTests {

        @Test
        @DisplayName("非文本类型返回true")
        void testBinary() {
            assertThat(OpenMimeType.isBinary("image/png")).isTrue();
            assertThat(OpenMimeType.isBinary("application/octet-stream")).isTrue();
        }

        @Test
        @DisplayName("文本类型返回false")
        void testText() {
            assertThat(OpenMimeType.isBinary("text/plain")).isFalse();
        }
    }

    @Nested
    @DisplayName("getExtension方法测试")
    class GetExtensionTests {

        @Test
        @DisplayName("text/plain返回有效扩展名")
        void testTextPlain() {
            Optional<String> ext = OpenMimeType.getExtension("text/plain");

            // text/plain maps to multiple extensions (txt, ini, env, conf, cfg)
            assertThat(ext).isPresent();
            assertThat(ext.get()).isIn("txt", "ini", "env", "conf", "cfg");
        }

        @Test
        @DisplayName("application/json返回json")
        void testJson() {
            Optional<String> ext = OpenMimeType.getExtension("application/json");

            assertThat(ext).contains("json");
        }

        @Test
        @DisplayName("null返回空")
        void testNull() {
            Optional<String> ext = OpenMimeType.getExtension(null);

            assertThat(ext).isEmpty();
        }

        @Test
        @DisplayName("未知MIME类型返回空")
        void testUnknown() {
            Optional<String> ext = OpenMimeType.getExtension("unknown/type");

            assertThat(ext).isEmpty();
        }
    }
}
