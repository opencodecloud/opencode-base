package cloud.opencode.base.image.validation;

import cloud.opencode.base.image.ImageFormat;
import cloud.opencode.base.image.exception.ImageTooLargeException;
import cloud.opencode.base.image.exception.ImageValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * ImageValidator 验证器测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
@DisplayName("ImageValidator 验证器测试")
class ImageValidatorTest {

    @TempDir
    Path tempDir;

    private Path createTestImage(int width, int height, String format) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Path path = tempDir.resolve("test." + format);
        ImageIO.write(image, format, path.toFile());
        return path;
    }

    private byte[] createTestImageBytes(int width, int height, String format) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }

    @Nested
    @DisplayName("常量测试")
    class ConstantTests {

        @Test
        @DisplayName("DEFAULT_MAX_FILE_SIZE值")
        void testDefaultMaxFileSize() {
            assertThat(ImageValidator.DEFAULT_MAX_FILE_SIZE).isEqualTo(10 * 1024 * 1024);
        }

        @Test
        @DisplayName("DEFAULT_MAX_WIDTH值")
        void testDefaultMaxWidth() {
            assertThat(ImageValidator.DEFAULT_MAX_WIDTH).isEqualTo(8000);
        }

        @Test
        @DisplayName("DEFAULT_MAX_HEIGHT值")
        void testDefaultMaxHeight() {
            assertThat(ImageValidator.DEFAULT_MAX_HEIGHT).isEqualTo(8000);
        }
    }

    @Nested
    @DisplayName("validate(Path)方法测试")
    class ValidatePathTests {

        @Test
        @DisplayName("验证有效图片")
        void testValidateValidImage() throws IOException {
            Path path = createTestImage(100, 100, "png");

            assertThatCode(() -> ImageValidator.validate(path))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("文件不存在抛出异常")
        void testValidateFileNotFound() {
            Path path = tempDir.resolve("nonexistent.jpg");

            assertThatThrownBy(() -> ImageValidator.validate(path))
                .isInstanceOf(ImageValidationException.class);
        }
    }

    @Nested
    @DisplayName("validate(Path, limits)方法测试")
    class ValidatePathWithLimitsTests {

        @Test
        @DisplayName("验证带限制的有效图片")
        void testValidateWithLimits() throws IOException {
            Path path = createTestImage(100, 100, "png");

            assertThatCode(() -> ImageValidator.validate(path, 10_000_000, 1000, 1000))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("超过尺寸限制抛出异常")
        void testValidateDimensionsExceeded() throws IOException {
            Path path = createTestImage(500, 500, "png");

            assertThatThrownBy(() -> ImageValidator.validate(path, 10_000_000, 100, 100))
                .isInstanceOf(ImageTooLargeException.class);
        }
    }

    @Nested
    @DisplayName("validate(byte[])方法测试")
    class ValidateBytesTests {

        @Test
        @DisplayName("验证有效字节数组")
        void testValidateValidBytes() throws IOException {
            byte[] bytes = createTestImageBytes(100, 100, "png");

            assertThatCode(() -> ImageValidator.validate(bytes))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("null字节数组抛出异常")
        void testValidateNullBytes() {
            assertThatThrownBy(() -> ImageValidator.validate((byte[]) null))
                .isInstanceOf(ImageValidationException.class);
        }

        @Test
        @DisplayName("空字节数组抛出异常")
        void testValidateEmptyBytes() {
            assertThatThrownBy(() -> ImageValidator.validate(new byte[0]))
                .isInstanceOf(ImageValidationException.class);
        }
    }

    @Nested
    @DisplayName("isValidImage(Path)方法测试")
    class IsValidImagePathTests {

        @Test
        @DisplayName("有效图片返回true")
        void testIsValidImageTrue() throws IOException {
            Path path = createTestImage(100, 100, "png");

            assertThat(ImageValidator.isValidImage(path)).isTrue();
        }

        @Test
        @DisplayName("不存在的文件返回false")
        void testIsValidImageFalse() {
            Path path = tempDir.resolve("nonexistent.jpg");

            assertThat(ImageValidator.isValidImage(path)).isFalse();
        }
    }

    @Nested
    @DisplayName("isValidImage(byte[])方法测试")
    class IsValidImageBytesTests {

        @Test
        @DisplayName("有效字节数组返回true")
        void testIsValidImageBytesTrue() throws IOException {
            byte[] bytes = createTestImageBytes(100, 100, "png");

            assertThat(ImageValidator.isValidImage(bytes)).isTrue();
        }

        @Test
        @DisplayName("无效字节数组返回false")
        void testIsValidImageBytesFalse() {
            assertThat(ImageValidator.isValidImage(new byte[]{1, 2, 3})).isFalse();
        }
    }

    @Nested
    @DisplayName("checkMagicNumber方法测试")
    class CheckMagicNumberTests {

        @Test
        @DisplayName("JPEG魔数")
        void testJpegMagicNumber() {
            byte[] jpegHeader = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

            assertThat(ImageValidator.checkMagicNumber(jpegHeader)).isTrue();
        }

        @Test
        @DisplayName("PNG魔数")
        void testPngMagicNumber() {
            byte[] pngHeader = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

            assertThat(ImageValidator.checkMagicNumber(pngHeader)).isTrue();
        }

        @Test
        @DisplayName("GIF87a魔数")
        void testGif87aMagicNumber() {
            byte[] gifHeader = {0x47, 0x49, 0x46, 0x38, 0x37, 0x61};

            assertThat(ImageValidator.checkMagicNumber(gifHeader)).isTrue();
        }

        @Test
        @DisplayName("GIF89a魔数")
        void testGif89aMagicNumber() {
            byte[] gifHeader = {0x47, 0x49, 0x46, 0x38, 0x39, 0x61};

            assertThat(ImageValidator.checkMagicNumber(gifHeader)).isTrue();
        }

        @Test
        @DisplayName("BMP魔数")
        void testBmpMagicNumber() {
            byte[] bmpHeader = {0x42, 0x4D};

            assertThat(ImageValidator.checkMagicNumber(bmpHeader)).isTrue();
        }

        @Test
        @DisplayName("WebP魔数")
        void testWebPMagicNumber() {
            byte[] webpHeader = {0x52, 0x49, 0x46, 0x46, 0, 0, 0, 0, 0x57, 0x45, 0x42, 0x50};

            assertThat(ImageValidator.checkMagicNumber(webpHeader)).isTrue();
        }

        @Test
        @DisplayName("无效魔数")
        void testInvalidMagicNumber() {
            byte[] invalidHeader = {0x00, 0x00, 0x00, 0x00};

            assertThat(ImageValidator.checkMagicNumber(invalidHeader)).isFalse();
        }

        @Test
        @DisplayName("null字节数组")
        void testNullBytes() {
            assertThat(ImageValidator.checkMagicNumber(null)).isFalse();
        }

        @Test
        @DisplayName("太短的字节数组")
        void testTooShortBytes() {
            assertThat(ImageValidator.checkMagicNumber(new byte[]{0x00})).isFalse();
        }
    }

    @Nested
    @DisplayName("detectFormat方法测试")
    class DetectFormatTests {

        @Test
        @DisplayName("检测JPEG格式")
        void testDetectJpeg() {
            byte[] jpegHeader = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

            assertThat(ImageValidator.detectFormat(jpegHeader)).isEqualTo(ImageFormat.JPEG);
        }

        @Test
        @DisplayName("检测PNG格式")
        void testDetectPng() {
            byte[] pngHeader = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

            assertThat(ImageValidator.detectFormat(pngHeader)).isEqualTo(ImageFormat.PNG);
        }

        @Test
        @DisplayName("检测GIF格式")
        void testDetectGif() {
            byte[] gifHeader = {0x47, 0x49, 0x46, 0x38, 0x39, 0x61};

            assertThat(ImageValidator.detectFormat(gifHeader)).isEqualTo(ImageFormat.GIF);
        }

        @Test
        @DisplayName("检测BMP格式")
        void testDetectBmp() {
            byte[] bmpHeader = {0x42, 0x4D};

            assertThat(ImageValidator.detectFormat(bmpHeader)).isEqualTo(ImageFormat.BMP);
        }

        @Test
        @DisplayName("检测WebP格式")
        void testDetectWebP() {
            byte[] webpHeader = {0x52, 0x49, 0x46, 0x46, 0, 0, 0, 0, 0x57, 0x45, 0x42, 0x50};

            assertThat(ImageValidator.detectFormat(webpHeader)).isEqualTo(ImageFormat.WEBP);
        }

        @Test
        @DisplayName("无法检测返回null")
        void testDetectUnknown() {
            byte[] unknownHeader = {0x00, 0x00, 0x00, 0x00};

            assertThat(ImageValidator.detectFormat(unknownHeader)).isNull();
        }

        @Test
        @DisplayName("null返回null")
        void testDetectNull() {
            assertThat(ImageValidator.detectFormat(null)).isNull();
        }
    }

    @Nested
    @DisplayName("validateExtensionMatchesContent方法测试")
    class ValidateExtensionMatchesContentTests {

        @Test
        @DisplayName("扩展名与内容匹配")
        void testExtensionMatches() throws IOException {
            Path path = createTestImage(100, 100, "png");

            assertThat(ImageValidator.validateExtensionMatchesContent(path)).isTrue();
        }

        @Test
        @DisplayName("JPEG扩展名允许jpeg后缀")
        void testJpegExtension() throws IOException {
            BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            Path path = tempDir.resolve("test.jpeg");
            ImageIO.write(image, "jpg", path.toFile());

            assertThat(ImageValidator.validateExtensionMatchesContent(path)).isTrue();
        }
    }
}
