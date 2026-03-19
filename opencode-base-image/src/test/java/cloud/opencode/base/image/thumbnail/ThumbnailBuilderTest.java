package cloud.opencode.base.image.thumbnail;

import cloud.opencode.base.image.Image;
import cloud.opencode.base.image.ImageFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * ThumbnailBuilder 构建器测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
@DisplayName("ThumbnailBuilder 构建器测试")
class ThumbnailBuilderTest {

    @TempDir
    Path tempDir;

    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        testImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
    }

    private Path createTestImageFile() throws IOException {
        Path path = tempDir.resolve("test.jpg");
        ImageIO.write(testImage, "jpg", path.toFile());
        return path;
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("空构造函数")
        void testEmptyConstructor() {
            ThumbnailBuilder builder = new ThumbnailBuilder();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("带BufferedImage构造函数")
        void testBufferedImageConstructor() {
            ThumbnailBuilder builder = new ThumbnailBuilder(testImage);

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("null图片抛出异常")
        void testNullImageConstructor() {
            assertThatThrownBy(() -> new ThumbnailBuilder(null))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of(Path)方法")
        void testOfPath() throws IOException {
            Path path = createTestImageFile();
            ThumbnailBuilder builder = ThumbnailBuilder.of(path);

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("of(Image)方法")
        void testOfImage() {
            Image image = new Image(testImage, ImageFormat.JPEG);
            ThumbnailBuilder builder = ThumbnailBuilder.of(image);

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("of(BufferedImage)方法")
        void testOfBufferedImage() {
            ThumbnailBuilder builder = ThumbnailBuilder.of(testImage);

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("of(byte[])方法")
        void testOfBytes() throws IOException {
            Path path = createTestImageFile();
            byte[] bytes = Files.readAllBytes(path);

            ThumbnailBuilder builder = ThumbnailBuilder.of(bytes);

            assertThat(builder).isNotNull();
        }
    }

    @Nested
    @DisplayName("size方法测试")
    class SizeTests {

        @Test
        @DisplayName("设置尺寸")
        void testSize() {
            ThumbnailBuilder builder = ThumbnailBuilder.of(testImage)
                .size(200, 150);

            BufferedImage result = builder.build();

            assertThat(result.getWidth()).isLessThanOrEqualTo(200);
            assertThat(result.getHeight()).isLessThanOrEqualTo(150);
        }

        @Test
        @DisplayName("宽度为0抛出异常")
        void testZeroWidth() {
            assertThatThrownBy(() -> ThumbnailBuilder.of(testImage).size(0, 100))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("高度为0抛出异常")
        void testZeroHeight() {
            assertThatThrownBy(() -> ThumbnailBuilder.of(testImage).size(100, 0))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("负宽度抛出异常")
        void testNegativeWidth() {
            assertThatThrownBy(() -> ThumbnailBuilder.of(testImage).size(-100, 100))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("width方法测试")
    class WidthTests {

        @Test
        @DisplayName("按宽度设置")
        void testWidth() {
            ThumbnailBuilder builder = ThumbnailBuilder.of(testImage)
                .width(400);

            BufferedImage result = builder.build();

            assertThat(result.getWidth()).isEqualTo(400);
            // 高度应该按比例计算
            assertThat(result.getHeight()).isEqualTo(300);
        }

        @Test
        @DisplayName("宽度为0抛出异常")
        void testZeroWidth() {
            assertThatThrownBy(() -> ThumbnailBuilder.of(testImage).width(0))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("height方法测试")
    class HeightTests {

        @Test
        @DisplayName("按高度设置")
        void testHeight() {
            ThumbnailBuilder builder = ThumbnailBuilder.of(testImage)
                .height(300);

            BufferedImage result = builder.build();

            assertThat(result.getHeight()).isEqualTo(300);
            // 宽度应该按比例计算
            assertThat(result.getWidth()).isEqualTo(400);
        }

        @Test
        @DisplayName("高度为0抛出异常")
        void testZeroHeight() {
            assertThatThrownBy(() -> ThumbnailBuilder.of(testImage).height(0))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("crop方法测试")
    class CropTests {

        @Test
        @DisplayName("启用裁剪")
        void testCropEnabled() {
            ThumbnailBuilder builder = ThumbnailBuilder.of(testImage)
                .size(200, 200)
                .crop(true);

            BufferedImage result = builder.build();

            assertThat(result.getWidth()).isEqualTo(200);
            assertThat(result.getHeight()).isEqualTo(200);
        }

        @Test
        @DisplayName("禁用裁剪保持比例")
        void testCropDisabled() {
            ThumbnailBuilder builder = ThumbnailBuilder.of(testImage)
                .size(200, 200)
                .crop(false);

            BufferedImage result = builder.build();

            // 不裁剪时保持比例
            assertThat(result.getWidth()).isLessThanOrEqualTo(200);
            assertThat(result.getHeight()).isLessThanOrEqualTo(200);
        }
    }

    @Nested
    @DisplayName("quality方法测试")
    class QualityTests {

        @Test
        @DisplayName("设置质量")
        void testQuality() {
            ThumbnailBuilder builder = ThumbnailBuilder.of(testImage)
                .quality(0.5f);

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("负质量抛出异常")
        void testNegativeQuality() {
            assertThatThrownBy(() -> ThumbnailBuilder.of(testImage).quality(-0.1f))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("超过1的质量抛出异常")
        void testQualityGreaterThanOne() {
            assertThatThrownBy(() -> ThumbnailBuilder.of(testImage).quality(1.1f))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("format方法测试")
    class FormatTests {

        @Test
        @DisplayName("设置格式")
        void testFormat() {
            ThumbnailBuilder builder = ThumbnailBuilder.of(testImage)
                .format(ImageFormat.PNG);

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("null格式抛出异常")
        void testNullFormat() {
            assertThatThrownBy(() -> ThumbnailBuilder.of(testImage).format(null))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("source方法测试")
    class SourceTests {

        @Test
        @DisplayName("从Path设置源")
        void testSourcePath() throws IOException {
            Path path = createTestImageFile();
            ThumbnailBuilder builder = new ThumbnailBuilder()
                .source(path);

            BufferedImage result = builder.build();

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("从Image设置源")
        void testSourceImage() {
            Image image = new Image(testImage, ImageFormat.JPEG);
            ThumbnailBuilder builder = new ThumbnailBuilder()
                .source(image);

            BufferedImage result = builder.build();

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("null源抛出异常")
        void testNullSource() {
            assertThatThrownBy(() -> new ThumbnailBuilder().source((Image) null))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("build方法测试")
    class BuildTests {

        @Test
        @DisplayName("构建缩略图")
        void testBuild() {
            BufferedImage result = ThumbnailBuilder.of(testImage)
                .size(200, 150)
                .build();

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("未设置源抛出异常")
        void testBuildNoSource() {
            ThumbnailBuilder builder = new ThumbnailBuilder();

            assertThatThrownBy(builder::build)
                .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("toImage方法测试")
    class ToImageTests {

        @Test
        @DisplayName("转换为Image")
        void testToImage() {
            Image result = ThumbnailBuilder.of(testImage)
                .size(200, 150)
                .toImage();

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("save方法测试")
    class SaveTests {

        @Test
        @DisplayName("保存缩略图")
        void testSave() throws IOException {
            Path output = tempDir.resolve("thumb.jpg");

            ThumbnailBuilder.of(testImage)
                .size(200, 150)
                .save(output);

            assertThat(Files.exists(output)).isTrue();
        }
    }

    @Nested
    @DisplayName("toBytes方法测试")
    class ToBytesTests {

        @Test
        @DisplayName("转换为字节数组")
        void testToBytes() {
            byte[] result = ThumbnailBuilder.of(testImage)
                .size(200, 150)
                .toBytes();

            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("create方法测试")
    class CreateTests {

        @Test
        @DisplayName("创建并保存")
        void testCreate() throws IOException {
            Path output = tempDir.resolve("created.jpg");

            ThumbnailBuilder.of(testImage)
                .size(200, 150)
                .output(output)
                .create();

            assertThat(Files.exists(output)).isTrue();
        }

        @Test
        @DisplayName("未设置输出路径抛出异常")
        void testCreateNoOutput() {
            ThumbnailBuilder builder = ThumbnailBuilder.of(testImage)
                .size(200, 150);

            assertThatThrownBy(builder::create)
                .isInstanceOf(IllegalStateException.class);
        }
    }
}
