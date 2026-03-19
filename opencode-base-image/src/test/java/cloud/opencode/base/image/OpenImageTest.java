package cloud.opencode.base.image;

import cloud.opencode.base.image.exception.ImageIOException;
import cloud.opencode.base.image.exception.ImageReadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenImage 工具类测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
@DisplayName("OpenImage 工具类测试")
class OpenImageTest {

    @TempDir
    Path tempDir;

    private BufferedImage testImage;
    private Path testImagePath;

    @BeforeEach
    void setUp() throws IOException {
        testImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = testImage.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 800, 600);
        g.dispose();

        testImagePath = tempDir.resolve("test.png");
        ImageIO.write(testImage, "png", testImagePath.toFile());
    }

    @Nested
    @DisplayName("Read操作测试")
    class ReadOperationsTests {

        @Test
        @DisplayName("read(Path)方法")
        void testReadPath() {
            Image image = OpenImage.read(testImagePath);

            assertThat(image).isNotNull();
            assertThat(image.getWidth()).isEqualTo(800);
            assertThat(image.getHeight()).isEqualTo(600);
        }

        @Test
        @DisplayName("read(Path)不存在的文件抛出异常")
        void testReadPathNotFound() {
            Path nonExistent = tempDir.resolve("nonexistent.png");

            assertThatThrownBy(() -> OpenImage.read(nonExistent))
                .isInstanceOf(ImageReadException.class);
        }

        @Test
        @DisplayName("read(InputStream)方法")
        void testReadInputStream() throws IOException {
            try (InputStream is = Files.newInputStream(testImagePath)) {
                Image image = OpenImage.read(is);

                assertThat(image).isNotNull();
                assertThat(image.getWidth()).isEqualTo(800);
            }
        }

        @Test
        @DisplayName("read(byte[])方法")
        void testReadBytes() throws IOException {
            byte[] bytes = Files.readAllBytes(testImagePath);

            Image image = OpenImage.read(bytes);

            assertThat(image).isNotNull();
            assertThat(image.getWidth()).isEqualTo(800);
        }

        @Test
        @DisplayName("fromBase64方法")
        void testFromBase64() throws IOException {
            byte[] bytes = Files.readAllBytes(testImagePath);
            String base64 = Base64.getEncoder().encodeToString(bytes);

            Image image = OpenImage.fromBase64(base64);

            assertThat(image).isNotNull();
            assertThat(image.getWidth()).isEqualTo(800);
        }

        @Test
        @DisplayName("fromBase64带data URI前缀")
        void testFromBase64WithDataUri() throws IOException {
            byte[] bytes = Files.readAllBytes(testImagePath);
            String base64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);

            Image image = OpenImage.fromBase64(base64);

            assertThat(image).isNotNull();
            assertThat(image.getWidth()).isEqualTo(800);
        }

        @Test
        @DisplayName("fromBase64空字符串抛出异常")
        void testFromBase64Empty() {
            assertThatThrownBy(() -> OpenImage.fromBase64(""))
                .isInstanceOf(ImageIOException.class);
        }

        @Test
        @DisplayName("fromBase64 null抛出异常")
        void testFromBase64Null() {
            assertThatThrownBy(() -> OpenImage.fromBase64(null))
                .isInstanceOf(ImageIOException.class);
        }

        @Test
        @DisplayName("readBufferedImage方法")
        void testReadBufferedImage() {
            BufferedImage result = OpenImage.readBufferedImage(testImagePath);

            assertThat(result).isNotNull();
            assertThat(result.getWidth()).isEqualTo(800);
        }
    }

    @Nested
    @DisplayName("Write操作测试")
    class WriteOperationsTests {

        @Test
        @DisplayName("write(BufferedImage, Path)方法")
        void testWriteToPath() {
            Path output = tempDir.resolve("output.png");

            OpenImage.write(testImage, output);

            assertThat(Files.exists(output)).isTrue();
        }

        @Test
        @DisplayName("write创建父目录")
        void testWriteCreatesParentDir() {
            Path output = tempDir.resolve("subdir/output.png");

            OpenImage.write(testImage, output, ImageFormat.PNG);

            assertThat(Files.exists(output)).isTrue();
            assertThat(Files.exists(output.getParent())).isTrue();
        }

        @Test
        @DisplayName("write(BufferedImage, Path, ImageFormat)方法")
        void testWriteWithFormat() {
            Path output = tempDir.resolve("output.jpg");

            OpenImage.write(testImage, output, ImageFormat.JPEG);

            assertThat(Files.exists(output)).isTrue();
        }

        @Test
        @DisplayName("write(BufferedImage, OutputStream, ImageFormat)方法")
        void testWriteToOutputStream() {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            OpenImage.write(testImage, baos, ImageFormat.PNG);

            assertThat(baos.toByteArray().length).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Info操作测试")
    class InfoOperationsTests {

        @Test
        @DisplayName("getInfo方法")
        void testGetInfo() {
            ImageInfo info = OpenImage.getInfo(testImagePath);

            assertThat(info.width()).isEqualTo(800);
            assertThat(info.height()).isEqualTo(600);
            assertThat(info.format()).isEqualTo(ImageFormat.PNG);
            assertThat(info.fileSize()).isGreaterThan(0);
        }

        @Test
        @DisplayName("getInfo文件不存在抛出异常")
        void testGetInfoFileNotFound() {
            Path nonExistent = tempDir.resolve("nonexistent.png");

            assertThatThrownBy(() -> OpenImage.getInfo(nonExistent))
                .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("getDimensions方法")
        void testGetDimensions() {
            int[] dimensions = OpenImage.getDimensions(testImagePath);

            assertThat(dimensions).hasSize(2);
            assertThat(dimensions[0]).isEqualTo(800);
            assertThat(dimensions[1]).isEqualTo(600);
        }
    }

    @Nested
    @DisplayName("Quick操作测试")
    class QuickOperationsTests {

        @Test
        @DisplayName("resize方法")
        void testQuickResize() {
            Path output = tempDir.resolve("resized.png");

            OpenImage.resize(testImagePath, output, 400, 300);

            assertThat(Files.exists(output)).isTrue();
            BufferedImage result = OpenImage.readBufferedImage(output);
            assertThat(result.getWidth()).isEqualTo(400);
            assertThat(result.getHeight()).isEqualTo(300);
        }

        @Test
        @DisplayName("crop方法")
        void testQuickCrop() {
            Path output = tempDir.resolve("cropped.png");

            OpenImage.crop(testImagePath, output, 100, 100, 200, 150);

            assertThat(Files.exists(output)).isTrue();
            BufferedImage result = OpenImage.readBufferedImage(output);
            assertThat(result.getWidth()).isEqualTo(200);
            assertThat(result.getHeight()).isEqualTo(150);
        }

        @Test
        @DisplayName("rotate方法")
        void testQuickRotate() {
            Path output = tempDir.resolve("rotated.png");

            OpenImage.rotate(testImagePath, output, 90);

            assertThat(Files.exists(output)).isTrue();
        }

        @Test
        @DisplayName("convert方法")
        void testQuickConvert() {
            Path output = tempDir.resolve("converted.jpg");

            OpenImage.convert(testImagePath, output, ImageFormat.JPEG);

            assertThat(Files.exists(output)).isTrue();
        }

        @Test
        @DisplayName("compress方法")
        void testQuickCompress() {
            Path output = tempDir.resolve("compressed.png");

            OpenImage.compress(testImagePath, output, 0.5f);

            assertThat(Files.exists(output)).isTrue();
        }

        @Test
        @DisplayName("thumbnail(Path, Path, int)方法")
        void testQuickThumbnail() {
            Path output = tempDir.resolve("thumb.png");

            OpenImage.thumbnail(testImagePath, output, 100);

            assertThat(Files.exists(output)).isTrue();
            BufferedImage result = OpenImage.readBufferedImage(output);
            assertThat(Math.max(result.getWidth(), result.getHeight())).isLessThanOrEqualTo(100);
        }

        @Test
        @DisplayName("thumbnail()构建器方法")
        void testThumbnailBuilder() {
            var builder = OpenImage.thumbnail();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("flipHorizontal方法")
        void testQuickFlipHorizontal() {
            Path output = tempDir.resolve("flipped_h.png");

            OpenImage.flipHorizontal(testImagePath, output);

            assertThat(Files.exists(output)).isTrue();
        }

        @Test
        @DisplayName("flipVertical方法")
        void testQuickFlipVertical() {
            Path output = tempDir.resolve("flipped_v.png");

            OpenImage.flipVertical(testImagePath, output);

            assertThat(Files.exists(output)).isTrue();
        }

        @Test
        @DisplayName("grayscale方法")
        void testQuickGrayscale() {
            Path output = tempDir.resolve("grayscale.png");

            OpenImage.grayscale(testImagePath, output);

            assertThat(Files.exists(output)).isTrue();
        }
    }

    @Nested
    @DisplayName("ToBytes操作测试")
    class ToBytesOperationsTests {

        @Test
        @DisplayName("toBytes(BufferedImage, ImageFormat)方法")
        void testToBytesFromImage() {
            byte[] bytes = OpenImage.toBytes(testImage, ImageFormat.PNG);

            assertThat(bytes).isNotNull();
            assertThat(bytes.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("toBytes(Path)方法")
        void testToBytesFromPath() {
            byte[] bytes = OpenImage.toBytes(testImagePath);

            assertThat(bytes).isNotNull();
            assertThat(bytes.length).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Utility方法测试")
    class UtilityMethodsTests {

        @Test
        @DisplayName("detectFormat检测PNG")
        void testDetectFormatPng() {
            Path path = tempDir.resolve("test.png");

            ImageFormat format = OpenImage.detectFormat(path);

            assertThat(format).isEqualTo(ImageFormat.PNG);
        }

        @Test
        @DisplayName("detectFormat检测JPEG")
        void testDetectFormatJpeg() {
            Path path = tempDir.resolve("test.jpg");

            ImageFormat format = OpenImage.detectFormat(path);

            assertThat(format).isEqualTo(ImageFormat.JPEG);
        }

        @Test
        @DisplayName("detectFormat检测GIF")
        void testDetectFormatGif() {
            Path path = tempDir.resolve("test.gif");

            ImageFormat format = OpenImage.detectFormat(path);

            assertThat(format).isEqualTo(ImageFormat.GIF);
        }

        @Test
        @DisplayName("detectFormat未知格式返回PNG")
        void testDetectFormatUnknown() {
            Path path = tempDir.resolve("test.xyz");

            ImageFormat format = OpenImage.detectFormat(path);

            assertThat(format).isEqualTo(ImageFormat.PNG);
        }

        @Test
        @DisplayName("detectFormat无扩展名返回PNG")
        void testDetectFormatNoExtension() {
            Path path = tempDir.resolve("testimage");

            ImageFormat format = OpenImage.detectFormat(path);

            assertThat(format).isEqualTo(ImageFormat.PNG);
        }

        @Test
        @DisplayName("isValidImage有效图片返回true")
        void testIsValidImageTrue() {
            assertThat(OpenImage.isValidImage(testImagePath)).isTrue();
        }

        @Test
        @DisplayName("isValidImage无效图片返回false")
        void testIsValidImageFalse() {
            Path invalid = tempDir.resolve("invalid.png");
            assertThat(OpenImage.isValidImage(invalid)).isFalse();
        }

        @Test
        @DisplayName("isSupported有效扩展名返回true")
        void testIsSupportedTrue() {
            assertThat(OpenImage.isSupported("png")).isTrue();
            assertThat(OpenImage.isSupported("jpg")).isTrue();
            assertThat(OpenImage.isSupported("jpeg")).isTrue();
            assertThat(OpenImage.isSupported("gif")).isTrue();
            assertThat(OpenImage.isSupported("bmp")).isTrue();
        }

        @Test
        @DisplayName("isSupported无效扩展名返回false")
        void testIsSupportedFalse() {
            assertThat(OpenImage.isSupported("xyz")).isFalse();
            assertThat(OpenImage.isSupported("txt")).isFalse();
        }
    }

    @Nested
    @DisplayName("CreateBlank方法测试")
    class CreateBlankTests {

        @Test
        @DisplayName("createBlank(width, height)方法")
        void testCreateBlank() {
            Image image = OpenImage.createBlank(200, 150);

            assertThat(image).isNotNull();
            assertThat(image.getWidth()).isEqualTo(200);
            assertThat(image.getHeight()).isEqualTo(150);
        }

        @Test
        @DisplayName("createBlank(width, height, color)方法")
        void testCreateBlankWithColor() {
            int redColor = 0xFFFF0000; // ARGB红色
            Image image = OpenImage.createBlank(100, 100, redColor);

            assertThat(image).isNotNull();
            assertThat(image.getWidth()).isEqualTo(100);
            assertThat(image.getHeight()).isEqualTo(100);
            // 验证颜色
            int pixel = image.getBufferedImage().getRGB(50, 50);
            assertThat(pixel).isEqualTo(redColor);
        }

        @Test
        @DisplayName("createBlank创建ARGB类型")
        void testCreateBlankType() {
            Image image = OpenImage.createBlank(100, 100);

            assertThat(image.getBufferedImage().getType()).isEqualTo(BufferedImage.TYPE_INT_ARGB);
        }
    }

    @Nested
    @DisplayName("集成测试")
    class IntegrationTests {

        @Test
        @DisplayName("读取-处理-保存流程")
        void testReadProcessSave() {
            Path output = tempDir.resolve("processed.jpg");

            OpenImage.read(testImagePath)
                .resize(400, 300)
                .rotate90()
                .grayscale()
                .save(output, ImageFormat.JPEG);

            assertThat(Files.exists(output)).isTrue();
        }

        @Test
        @DisplayName("字节数组往返")
        void testBytesRoundTrip() {
            byte[] original = OpenImage.toBytes(testImage, ImageFormat.PNG);

            Image restored = OpenImage.read(original);
            byte[] restored2 = restored.toBytes(ImageFormat.PNG);

            assertThat(restored.getWidth()).isEqualTo(testImage.getWidth());
            assertThat(restored.getHeight()).isEqualTo(testImage.getHeight());
        }

        @Test
        @DisplayName("Base64往返")
        void testBase64RoundTrip() throws IOException {
            String base64 = Image.from(testImagePath).toBase64(ImageFormat.PNG);

            Image restored = OpenImage.fromBase64(base64);

            assertThat(restored.getWidth()).isEqualTo(800);
            assertThat(restored.getHeight()).isEqualTo(600);
        }
    }
}
