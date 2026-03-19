package cloud.opencode.base.pdf.content;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * PdfImage 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("PdfImage 测试")
class PdfImageTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("from(Path) 创建图像")
        void testFromPath() {
            Path imagePath = Path.of("/test/image.png");
            PdfImage image = PdfImage.from(imagePath);

            assertThat(image).isNotNull();
            assertThat(image.getSourcePath()).isEqualTo(imagePath);
        }

        @Test
        @DisplayName("from(byte[], ImageFormat) 创建图像")
        void testFromBytes() {
            byte[] data = new byte[]{1, 2, 3, 4};
            PdfImage image = PdfImage.from(data, PdfImage.ImageFormat.PNG);

            assertThat(image).isNotNull();
            assertThat(image.getSourceBytes()).isEqualTo(data);
            assertThat(image.getFormat()).isEqualTo(PdfImage.ImageFormat.PNG);
        }

        @Test
        @DisplayName("builder 创建空构建器")
        void testBuilder() {
            PdfImage image = PdfImage.builder();

            assertThat(image).isNotNull();
        }
    }

    @Nested
    @DisplayName("Builder 方法测试")
    class BuilderMethodTests {

        @Test
        @DisplayName("source(Path) 设置路径源")
        void testSourcePath() {
            Path path = Path.of("/test/image.jpg");
            PdfImage image = PdfImage.builder().source(path);

            assertThat(image.getSourcePath()).isEqualTo(path);
        }

        @Test
        @DisplayName("source(bytes, format) 设置字节源")
        void testSourceBytes() {
            byte[] data = new byte[]{1, 2, 3};
            PdfImage image = PdfImage.builder().source(data, PdfImage.ImageFormat.JPEG);

            assertThat(image.getSourceBytes()).isEqualTo(data);
            assertThat(image.getFormat()).isEqualTo(PdfImage.ImageFormat.JPEG);
        }

        @Test
        @DisplayName("position 设置位置")
        void testPosition() {
            PdfImage image = PdfImage.builder().position(100f, 200f);

            assertThat(image.getX()).isEqualTo(100f);
            assertThat(image.getY()).isEqualTo(200f);
        }

        @Test
        @DisplayName("size 设置大小")
        void testSize() {
            PdfImage image = PdfImage.builder().size(300f, 400f);

            assertThat(image.getWidth()).isEqualTo(300f);
            assertThat(image.getHeight()).isEqualTo(400f);
        }

        @Test
        @DisplayName("scaleToWidth 设置宽度缩放")
        void testScaleToWidth() {
            PdfImage image = PdfImage.builder().scaleToWidth(500f);

            assertThat(image.getWidth()).isEqualTo(500f);
            assertThat(image.getHeight()).isEqualTo(-1f);
        }

        @Test
        @DisplayName("scaleToHeight 设置高度缩放")
        void testScaleToHeight() {
            PdfImage image = PdfImage.builder().scaleToHeight(300f);

            assertThat(image.getWidth()).isEqualTo(-1f);
            assertThat(image.getHeight()).isEqualTo(300f);
        }

        @Test
        @DisplayName("rotation 设置旋转")
        void testRotation() {
            PdfImage image = PdfImage.builder().rotation(90f);

            assertThat(image.getRotation()).isEqualTo(90f);
        }

        @Test
        @DisplayName("opacity 设置透明度")
        void testOpacity() {
            PdfImage image = PdfImage.builder().opacity(0.5f);

            assertThat(image.getOpacity()).isEqualTo(0.5f);
        }
    }

    @Nested
    @DisplayName("ImageFormat 枚举测试")
    class ImageFormatTests {

        @Test
        @DisplayName("包含所有格式")
        void testAllFormats() {
            assertThat(PdfImage.ImageFormat.values()).containsExactly(
                PdfImage.ImageFormat.PNG,
                PdfImage.ImageFormat.JPEG,
                PdfImage.ImageFormat.GIF,
                PdfImage.ImageFormat.BMP
            );
        }

        @Test
        @DisplayName("valueOf 方法正常工作")
        void testValueOf() {
            assertThat(PdfImage.ImageFormat.valueOf("PNG")).isEqualTo(PdfImage.ImageFormat.PNG);
            assertThat(PdfImage.ImageFormat.valueOf("JPEG")).isEqualTo(PdfImage.ImageFormat.JPEG);
            assertThat(PdfImage.ImageFormat.valueOf("GIF")).isEqualTo(PdfImage.ImageFormat.GIF);
            assertThat(PdfImage.ImageFormat.valueOf("BMP")).isEqualTo(PdfImage.ImageFormat.BMP);
        }
    }

    @Nested
    @DisplayName("格式检测测试")
    class FormatDetectionTests {

        @Test
        @DisplayName("PNG 文件检测")
        void testPngDetection() {
            PdfImage image = PdfImage.from(Path.of("/test/image.png"));

            assertThat(image.getFormat()).isEqualTo(PdfImage.ImageFormat.PNG);
        }

        @Test
        @DisplayName("JPG 文件检测")
        void testJpgDetection() {
            PdfImage image = PdfImage.from(Path.of("/test/image.jpg"));

            assertThat(image.getFormat()).isEqualTo(PdfImage.ImageFormat.JPEG);
        }

        @Test
        @DisplayName("JPEG 文件检测")
        void testJpegDetection() {
            PdfImage image = PdfImage.from(Path.of("/test/image.jpeg"));

            assertThat(image.getFormat()).isEqualTo(PdfImage.ImageFormat.JPEG);
        }

        @Test
        @DisplayName("GIF 文件检测")
        void testGifDetection() {
            PdfImage image = PdfImage.from(Path.of("/test/image.gif"));

            assertThat(image.getFormat()).isEqualTo(PdfImage.ImageFormat.GIF);
        }

        @Test
        @DisplayName("BMP 文件检测")
        void testBmpDetection() {
            PdfImage image = PdfImage.from(Path.of("/test/image.bmp"));

            assertThat(image.getFormat()).isEqualTo(PdfImage.ImageFormat.BMP);
        }

        @Test
        @DisplayName("未知格式默认为 PNG")
        void testUnknownFormat() {
            PdfImage image = PdfImage.from(Path.of("/test/image.xyz"));

            assertThat(image.getFormat()).isEqualTo(PdfImage.ImageFormat.PNG);
        }
    }

    @Nested
    @DisplayName("链式调用测试")
    class FluentApiTests {

        @Test
        @DisplayName("完整的链式调用")
        void testFullFluentApi() {
            Path path = Path.of("/test/image.png");
            PdfImage image = PdfImage.from(path)
                .position(100f, 200f)
                .size(300f, 400f)
                .rotation(45f)
                .opacity(0.8f);

            assertThat(image.getSourcePath()).isEqualTo(path);
            assertThat(image.getX()).isEqualTo(100f);
            assertThat(image.getY()).isEqualTo(200f);
            assertThat(image.getWidth()).isEqualTo(300f);
            assertThat(image.getHeight()).isEqualTo(400f);
            assertThat(image.getRotation()).isEqualTo(45f);
            assertThat(image.getOpacity()).isEqualTo(0.8f);
        }

        @Test
        @DisplayName("返回相同实例")
        void testReturnsSameInstance() {
            PdfImage image = PdfImage.builder();

            assertThat(image.position(0, 0)).isSameAs(image);
            assertThat(image.size(100, 100)).isSameAs(image);
            assertThat(image.rotation(0)).isSameAs(image);
            assertThat(image.opacity(1f)).isSameAs(image);
        }
    }

    @Nested
    @DisplayName("默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("默认透明度为 1.0")
        void testDefaultOpacity() {
            PdfImage image = PdfImage.builder();

            assertThat(image.getOpacity()).isEqualTo(1.0f);
        }

        @Test
        @DisplayName("默认旋转为 0")
        void testDefaultRotation() {
            PdfImage image = PdfImage.builder();

            assertThat(image.getRotation()).isEqualTo(0f);
        }

        @Test
        @DisplayName("默认宽度为 -1")
        void testDefaultWidth() {
            PdfImage image = PdfImage.builder();

            assertThat(image.getWidth()).isEqualTo(-1f);
        }

        @Test
        @DisplayName("默认高度为 -1")
        void testDefaultHeight() {
            PdfImage image = PdfImage.builder();

            assertThat(image.getHeight()).isEqualTo(-1f);
        }
    }

    @Nested
    @DisplayName("PdfElement 接口测试")
    class PdfElementInterfaceTests {

        @Test
        @DisplayName("实现 PdfElement 接口")
        void testImplementsPdfElement() {
            PdfImage image = PdfImage.from(Path.of("/test.png"));

            assertThat(image).isInstanceOf(PdfElement.class);
        }

        @Test
        @DisplayName("getX 返回正确值")
        void testGetX() {
            PdfImage image = PdfImage.builder().position(150f, 200f);

            assertThat(image.getX()).isEqualTo(150f);
        }

        @Test
        @DisplayName("getY 返回正确值")
        void testGetY() {
            PdfImage image = PdfImage.builder().position(150f, 200f);

            assertThat(image.getY()).isEqualTo(200f);
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionTests {

        @Test
        @DisplayName("透明度为 0")
        void testZeroOpacity() {
            PdfImage image = PdfImage.builder().opacity(0f);

            assertThat(image.getOpacity()).isEqualTo(0f);
        }

        @Test
        @DisplayName("负坐标")
        void testNegativeCoordinates() {
            PdfImage image = PdfImage.builder().position(-50f, -100f);

            assertThat(image.getX()).isEqualTo(-50f);
            assertThat(image.getY()).isEqualTo(-100f);
        }

        @Test
        @DisplayName("360度旋转")
        void testFullRotation() {
            PdfImage image = PdfImage.builder().rotation(360f);

            assertThat(image.getRotation()).isEqualTo(360f);
        }

        @Test
        @DisplayName("负旋转角度")
        void testNegativeRotation() {
            PdfImage image = PdfImage.builder().rotation(-45f);

            assertThat(image.getRotation()).isEqualTo(-45f);
        }

        @Test
        @DisplayName("空字节数组")
        void testEmptyByteArray() {
            byte[] empty = new byte[0];
            PdfImage image = PdfImage.from(empty, PdfImage.ImageFormat.PNG);

            assertThat(image.getSourceBytes()).isEmpty();
        }

        @Test
        @DisplayName("getSourceBytes 返回副本")
        void testSourceBytesCopy() {
            byte[] data = new byte[]{1, 2, 3};
            PdfImage image = PdfImage.from(data, PdfImage.ImageFormat.PNG);

            byte[] retrieved = image.getSourceBytes();
            retrieved[0] = 99;

            assertThat(image.getSourceBytes()[0]).isEqualTo((byte) 1);
        }
    }

    @Nested
    @DisplayName("final 类测试")
    class FinalClassTests {

        @Test
        @DisplayName("PdfImage 是 final 类")
        void testIsFinalClass() {
            assertThat(java.lang.reflect.Modifier.isFinal(PdfImage.class.getModifiers())).isTrue();
        }
    }
}
