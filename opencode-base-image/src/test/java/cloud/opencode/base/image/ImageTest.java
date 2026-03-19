package cloud.opencode.base.image;

import cloud.opencode.base.image.watermark.ImageWatermark;
import cloud.opencode.base.image.watermark.TextWatermark;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * Image 包装类测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
@DisplayName("Image 包装类测试")
class ImageTest {

    @TempDir
    Path tempDir;

    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        testImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = testImage.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 800, 600);
        g.dispose();
    }

    private Path createTestImageFile() throws IOException {
        Path path = tempDir.resolve("test.png");
        ImageIO.write(testImage, "png", path.toFile());
        return path;
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("单参数构造函数")
        void testSingleArgConstructor() {
            Image image = new Image(testImage);

            assertThat(image.getBufferedImage()).isEqualTo(testImage);
            assertThat(image.getFormat()).isEqualTo(ImageFormat.PNG);
        }

        @Test
        @DisplayName("双参数构造函数")
        void testTwoArgConstructor() {
            Image image = new Image(testImage, ImageFormat.JPEG);

            assertThat(image.getBufferedImage()).isEqualTo(testImage);
            assertThat(image.getFormat()).isEqualTo(ImageFormat.JPEG);
        }

        @Test
        @DisplayName("null格式使用默认值")
        void testNullFormat() {
            Image image = new Image(testImage, null);

            assertThat(image.getFormat()).isNull();
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("from(Path)方法")
        void testFromPath() throws IOException {
            Path path = createTestImageFile();

            Image image = Image.from(path);

            assertThat(image).isNotNull();
            assertThat(image.getWidth()).isEqualTo(800);
            assertThat(image.getHeight()).isEqualTo(600);
        }

        @Test
        @DisplayName("from(byte[])方法")
        void testFromBytes() throws IOException {
            Path path = createTestImageFile();
            byte[] bytes = Files.readAllBytes(path);

            Image image = Image.from(bytes);

            assertThat(image).isNotNull();
            assertThat(image.getWidth()).isEqualTo(800);
        }

        @Test
        @DisplayName("from(BufferedImage)方法")
        void testFromBufferedImage() {
            Image image = Image.from(testImage);

            assertThat(image).isNotNull();
            assertThat(image.getBufferedImage()).isEqualTo(testImage);
        }
    }

    @Nested
    @DisplayName("Getter方法测试")
    class GetterTests {

        @Test
        @DisplayName("getBufferedImage方法")
        void testGetBufferedImage() {
            Image image = new Image(testImage);

            assertThat(image.getBufferedImage()).isEqualTo(testImage);
        }

        @Test
        @DisplayName("getFormat方法")
        void testGetFormat() {
            Image image = new Image(testImage, ImageFormat.GIF);

            assertThat(image.getFormat()).isEqualTo(ImageFormat.GIF);
        }

        @Test
        @DisplayName("getWidth方法")
        void testGetWidth() {
            Image image = new Image(testImage);

            assertThat(image.getWidth()).isEqualTo(800);
        }

        @Test
        @DisplayName("getHeight方法")
        void testGetHeight() {
            Image image = new Image(testImage);

            assertThat(image.getHeight()).isEqualTo(600);
        }

        @Test
        @DisplayName("getInfo方法")
        void testGetInfo() {
            Image image = new Image(testImage, ImageFormat.JPEG);

            ImageInfo info = image.getInfo();

            assertThat(info.width()).isEqualTo(800);
            assertThat(info.height()).isEqualTo(600);
            assertThat(info.format()).isEqualTo(ImageFormat.JPEG);
        }
    }

    @Nested
    @DisplayName("format方法测试")
    class FormatSetterTests {

        @Test
        @DisplayName("设置格式")
        void testSetFormat() {
            Image image = new Image(testImage);

            Image result = image.format(ImageFormat.JPEG);

            assertThat(result).isSameAs(image);
            assertThat(image.getFormat()).isEqualTo(ImageFormat.JPEG);
        }

        @Test
        @DisplayName("链式调用")
        void testChaining() {
            Image image = new Image(testImage)
                .format(ImageFormat.PNG)
                .format(ImageFormat.GIF);

            assertThat(image.getFormat()).isEqualTo(ImageFormat.GIF);
        }
    }

    @Nested
    @DisplayName("Resize操作测试")
    class ResizeOperationsTests {

        @Test
        @DisplayName("resize方法")
        void testResize() {
            Image image = new Image(testImage);

            Image result = image.resize(400, 300);

            assertThat(result).isSameAs(image);
            assertThat(image.getWidth()).isEqualTo(400);
            assertThat(image.getHeight()).isEqualTo(300);
        }

        @Test
        @DisplayName("resizeToFit方法")
        void testResizeToFit() {
            Image image = new Image(testImage);

            Image result = image.resizeToFit(400, 400);

            assertThat(result).isSameAs(image);
            assertThat(image.getWidth()).isLessThanOrEqualTo(400);
            assertThat(image.getHeight()).isLessThanOrEqualTo(400);
        }

        @Test
        @DisplayName("scale方法")
        void testScale() {
            Image image = new Image(testImage);

            Image result = image.scale(0.5);

            assertThat(result).isSameAs(image);
            assertThat(image.getWidth()).isEqualTo(400);
            assertThat(image.getHeight()).isEqualTo(300);
        }

        @Test
        @DisplayName("scaleToWidth方法")
        void testScaleToWidth() {
            Image image = new Image(testImage);

            Image result = image.scaleToWidth(400);

            assertThat(result).isSameAs(image);
            assertThat(image.getWidth()).isEqualTo(400);
            // 高度按比例计算
            assertThat(image.getHeight()).isEqualTo(300);
        }

        @Test
        @DisplayName("scaleToHeight方法")
        void testScaleToHeight() {
            Image image = new Image(testImage);

            Image result = image.scaleToHeight(300);

            assertThat(result).isSameAs(image);
            assertThat(image.getHeight()).isEqualTo(300);
            assertThat(image.getWidth()).isEqualTo(400);
        }
    }

    @Nested
    @DisplayName("Crop操作测试")
    class CropOperationsTests {

        @Test
        @DisplayName("crop方法")
        void testCrop() {
            Image image = new Image(testImage);

            Image result = image.crop(100, 100, 200, 150);

            assertThat(result).isSameAs(image);
            assertThat(image.getWidth()).isEqualTo(200);
            assertThat(image.getHeight()).isEqualTo(150);
        }

        @Test
        @DisplayName("cropCenter方法")
        void testCropCenter() {
            Image image = new Image(testImage);

            Image result = image.cropCenter(200, 150);

            assertThat(result).isSameAs(image);
            assertThat(image.getWidth()).isEqualTo(200);
            assertThat(image.getHeight()).isEqualTo(150);
        }

        @Test
        @DisplayName("cropSquare方法")
        void testCropSquare() {
            Image image = new Image(testImage);

            Image result = image.cropSquare();

            assertThat(result).isSameAs(image);
            assertThat(image.getWidth()).isEqualTo(image.getHeight());
        }
    }

    @Nested
    @DisplayName("Rotate操作测试")
    class RotateOperationsTests {

        @Test
        @DisplayName("rotate方法")
        void testRotate() {
            Image image = new Image(testImage);

            Image result = image.rotate(45);

            assertThat(result).isSameAs(image);
        }

        @Test
        @DisplayName("rotate90方法")
        void testRotate90() {
            Image image = new Image(testImage);

            Image result = image.rotate90();

            assertThat(result).isSameAs(image);
            assertThat(image.getWidth()).isEqualTo(600);
            assertThat(image.getHeight()).isEqualTo(800);
        }

        @Test
        @DisplayName("rotate180方法")
        void testRotate180() {
            Image image = new Image(testImage);

            Image result = image.rotate180();

            assertThat(result).isSameAs(image);
            assertThat(image.getWidth()).isEqualTo(800);
            assertThat(image.getHeight()).isEqualTo(600);
        }

        @Test
        @DisplayName("rotate270方法")
        void testRotate270() {
            Image image = new Image(testImage);

            Image result = image.rotate270();

            assertThat(result).isSameAs(image);
            assertThat(image.getWidth()).isEqualTo(600);
            assertThat(image.getHeight()).isEqualTo(800);
        }

        @Test
        @DisplayName("flipHorizontal方法")
        void testFlipHorizontal() {
            Image image = new Image(testImage);

            Image result = image.flipHorizontal();

            assertThat(result).isSameAs(image);
            assertThat(image.getWidth()).isEqualTo(800);
        }

        @Test
        @DisplayName("flipVertical方法")
        void testFlipVertical() {
            Image image = new Image(testImage);

            Image result = image.flipVertical();

            assertThat(result).isSameAs(image);
            assertThat(image.getHeight()).isEqualTo(600);
        }
    }

    @Nested
    @DisplayName("Watermark操作测试")
    class WatermarkOperationsTests {

        @Test
        @DisplayName("watermark(TextWatermark)方法")
        void testTextWatermark() {
            Image image = new Image(testImage);
            TextWatermark watermark = TextWatermark.of("Test");

            Image result = image.watermark(watermark);

            assertThat(result).isSameAs(image);
        }

        @Test
        @DisplayName("watermark(String, Position)方法")
        void testTextWatermarkWithPosition() {
            Image image = new Image(testImage);

            Image result = image.watermark("Test", Position.BOTTOM_RIGHT);

            assertThat(result).isSameAs(image);
        }

        @Test
        @DisplayName("watermark(ImageWatermark)方法")
        void testImageWatermark() {
            Image image = new Image(testImage);
            BufferedImage wmImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
            ImageWatermark watermark = new ImageWatermark(wmImage, Position.CENTER, 0.5f, 10);

            Image result = image.watermark(watermark);

            assertThat(result).isSameAs(image);
        }

        @Test
        @DisplayName("watermark(BufferedImage, Position)方法")
        void testImageWatermarkWithPosition() {
            Image image = new Image(testImage);
            BufferedImage wmImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);

            Image result = image.watermark(wmImage, Position.TOP_LEFT);

            assertThat(result).isSameAs(image);
        }
    }

    @Nested
    @DisplayName("Compress操作测试")
    class CompressOperationsTests {

        @Test
        @DisplayName("compress方法")
        void testCompress() {
            Image image = new Image(testImage);

            Image result = image.compress(0.5f);

            assertThat(result).isSameAs(image);
        }
    }

    @Nested
    @DisplayName("Convert操作测试")
    class ConvertOperationsTests {

        @Test
        @DisplayName("convert方法")
        void testConvert() {
            Image image = new Image(testImage, ImageFormat.PNG);

            Image result = image.convert(ImageFormat.JPEG);

            assertThat(result).isSameAs(image);
            assertThat(image.getFormat()).isEqualTo(ImageFormat.JPEG);
        }

        @Test
        @DisplayName("grayscale方法")
        void testGrayscale() {
            Image image = new Image(testImage);

            Image result = image.grayscale();

            assertThat(result).isSameAs(image);
        }
    }

    @Nested
    @DisplayName("Save操作测试")
    class SaveOperationsTests {

        @Test
        @DisplayName("save(Path)方法")
        void testSave() throws Exception {
            Image image = new Image(testImage, ImageFormat.PNG);
            Path output = tempDir.resolve("output.png");

            image.save(output);

            assertThat(Files.exists(output)).isTrue();
        }

        @Test
        @DisplayName("save(Path, ImageFormat)方法")
        void testSaveWithFormat() throws Exception {
            Image image = new Image(testImage);
            Path output = tempDir.resolve("output.jpg");

            image.save(output, ImageFormat.JPEG);

            assertThat(Files.exists(output)).isTrue();
        }
    }

    @Nested
    @DisplayName("WriteTo操作测试")
    class WriteToOperationsTests {

        @Test
        @DisplayName("writeTo(OutputStream)方法")
        void testWriteTo() throws Exception {
            Image image = new Image(testImage, ImageFormat.PNG);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            image.writeTo(baos);

            assertThat(baos.toByteArray().length).isGreaterThan(0);
        }

        @Test
        @DisplayName("writeTo(OutputStream, ImageFormat)方法")
        void testWriteToWithFormat() throws Exception {
            Image image = new Image(testImage);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            image.writeTo(baos, ImageFormat.JPEG);

            assertThat(baos.toByteArray().length).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("ToBytes操作测试")
    class ToBytesOperationsTests {

        @Test
        @DisplayName("toBytes()方法")
        void testToBytes() {
            Image image = new Image(testImage, ImageFormat.PNG);

            byte[] bytes = image.toBytes();

            assertThat(bytes).isNotNull();
            assertThat(bytes.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("toBytes(ImageFormat)方法")
        void testToBytesWithFormat() {
            Image image = new Image(testImage);

            byte[] bytes = image.toBytes(ImageFormat.JPEG);

            assertThat(bytes).isNotNull();
            assertThat(bytes.length).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("ToBase64操作测试")
    class ToBase64OperationsTests {

        @Test
        @DisplayName("toBase64()方法")
        void testToBase64() {
            Image image = new Image(testImage, ImageFormat.PNG);

            String base64 = image.toBase64();

            assertThat(base64).isNotNull();
            assertThat(base64).isNotEmpty();
        }

        @Test
        @DisplayName("toBase64(ImageFormat)方法")
        void testToBase64WithFormat() {
            Image image = new Image(testImage);

            String base64 = image.toBase64(ImageFormat.JPEG);

            assertThat(base64).isNotNull();
            assertThat(base64).isNotEmpty();
        }

        @Test
        @DisplayName("Base64可以解码")
        void testBase64Decodable() {
            Image image = new Image(testImage, ImageFormat.PNG);

            String base64 = image.toBase64();

            assertThatCode(() -> java.util.Base64.getDecoder().decode(base64))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Copy操作测试")
    class CopyOperationsTests {

        @Test
        @DisplayName("copy方法")
        void testCopy() {
            Image image = new Image(testImage, ImageFormat.JPEG);

            Image copy = image.copy();

            assertThat(copy).isNotSameAs(image);
            assertThat(copy.getWidth()).isEqualTo(image.getWidth());
            assertThat(copy.getHeight()).isEqualTo(image.getHeight());
            assertThat(copy.getFormat()).isEqualTo(image.getFormat());
        }

        @Test
        @DisplayName("copy独立于原图")
        void testCopyIndependent() {
            Image image = new Image(testImage, ImageFormat.PNG);

            Image copy = image.copy();
            image.resize(100, 100);

            assertThat(copy.getWidth()).isEqualTo(800);
            assertThat(image.getWidth()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("链式调用测试")
    class ChainingTests {

        @Test
        @DisplayName("多个操作链式调用")
        void testMultipleChainedOperations() {
            Image image = new Image(testImage)
                .resize(400, 300)
                .rotate90()
                .flipHorizontal()
                .grayscale();

            assertThat(image).isNotNull();
        }

        @Test
        @DisplayName("完整处理流程")
        void testFullWorkflow() throws Exception {
            Path output = tempDir.resolve("result.png");

            new Image(testImage, ImageFormat.PNG)
                .resize(200, 150)
                .cropCenter(100, 100)
                .watermark("Test", Position.CENTER)
                .save(output);

            assertThat(Files.exists(output)).isTrue();
        }
    }
}
