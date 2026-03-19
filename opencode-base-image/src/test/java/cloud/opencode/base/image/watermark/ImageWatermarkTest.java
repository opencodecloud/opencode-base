package cloud.opencode.base.image.watermark;

import cloud.opencode.base.image.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * ImageWatermark 记录测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
@DisplayName("ImageWatermark 记录测试")
class ImageWatermarkTest {

    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        testImage = new BufferedImage(100, 50, BufferedImage.TYPE_INT_ARGB);
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("完整参数构造")
        void testFullConstruction() {
            ImageWatermark wm = new ImageWatermark(testImage, Position.CENTER, 0.7f, 20);

            assertThat(wm.image()).isEqualTo(testImage);
            assertThat(wm.position()).isEqualTo(Position.CENTER);
            assertThat(wm.opacity()).isEqualTo(0.7f);
            assertThat(wm.margin()).isEqualTo(20);
        }

        @Test
        @DisplayName("null图片抛出异常")
        void testNullImage() {
            assertThatThrownBy(() -> new ImageWatermark(null, Position.CENTER, 0.5f, 10))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null位置使用默认值")
        void testNullPosition() {
            ImageWatermark wm = new ImageWatermark(testImage, null, 0.5f, 10);

            assertThat(wm.position()).isEqualTo(Position.BOTTOM_RIGHT);
        }

        @Test
        @DisplayName("负透明度抛出异常")
        void testNegativeOpacity() {
            assertThatThrownBy(() -> new ImageWatermark(testImage, Position.CENTER, -0.1f, 10))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("超过1的透明度抛出异常")
        void testOpacityGreaterThanOne() {
            assertThatThrownBy(() -> new ImageWatermark(testImage, Position.CENTER, 1.1f, 10))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("负边距抛出异常")
        void testNegativeMargin() {
            assertThatThrownBy(() -> new ImageWatermark(testImage, Position.CENTER, 0.5f, -10))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of(image)方法")
        void testOfImage() {
            ImageWatermark wm = ImageWatermark.of(testImage);

            assertThat(wm.image()).isEqualTo(testImage);
            assertThat(wm.position()).isEqualTo(Position.BOTTOM_RIGHT);
            assertThat(wm.opacity()).isEqualTo(ImageWatermark.DEFAULT_OPACITY);
        }

        @Test
        @DisplayName("of(image, position)方法")
        void testOfImageAndPosition() {
            ImageWatermark wm = ImageWatermark.of(testImage, Position.TOP_LEFT);

            assertThat(wm.image()).isEqualTo(testImage);
            assertThat(wm.position()).isEqualTo(Position.TOP_LEFT);
        }

        @Test
        @DisplayName("of(image, position, opacity)方法")
        void testOfImagePositionAndOpacity() {
            ImageWatermark wm = ImageWatermark.of(testImage, Position.CENTER, 0.5f);

            assertThat(wm.image()).isEqualTo(testImage);
            assertThat(wm.position()).isEqualTo(Position.CENTER);
            assertThat(wm.opacity()).isEqualTo(0.5f);
        }
    }

    @Nested
    @DisplayName("尺寸方法测试")
    class DimensionMethodTests {

        @Test
        @DisplayName("getWidth方法")
        void testGetWidth() {
            ImageWatermark wm = ImageWatermark.of(testImage);

            assertThat(wm.getWidth()).isEqualTo(100);
        }

        @Test
        @DisplayName("getHeight方法")
        void testGetHeight() {
            ImageWatermark wm = ImageWatermark.of(testImage);

            assertThat(wm.getHeight()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("with方法测试")
    class WithMethodTests {

        @Test
        @DisplayName("withPosition方法")
        void testWithPosition() {
            ImageWatermark original = ImageWatermark.of(testImage);
            ImageWatermark modified = original.withPosition(Position.TOP_LEFT);

            assertThat(modified.position()).isEqualTo(Position.TOP_LEFT);
            assertThat(modified.image()).isEqualTo(original.image());
        }

        @Test
        @DisplayName("withOpacity方法")
        void testWithOpacity() {
            ImageWatermark original = ImageWatermark.of(testImage);
            ImageWatermark modified = original.withOpacity(0.3f);

            assertThat(modified.opacity()).isEqualTo(0.3f);
        }

        @Test
        @DisplayName("withMargin方法")
        void testWithMargin() {
            ImageWatermark original = ImageWatermark.of(testImage);
            ImageWatermark modified = original.withMargin(30);

            assertThat(modified.margin()).isEqualTo(30);
        }

        @Test
        @DisplayName("withImage方法")
        void testWithImage() {
            BufferedImage newImage = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
            ImageWatermark original = ImageWatermark.of(testImage);
            ImageWatermark modified = original.withImage(newImage);

            assertThat(modified.image()).isEqualTo(newImage);
            assertThat(modified.getWidth()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("使用构建器创建")
        void testBuilder() {
            ImageWatermark wm = ImageWatermark.builder()
                .image(testImage)
                .position(Position.TOP_RIGHT)
                .opacity(0.7f)
                .margin(15)
                .build();

            assertThat(wm.image()).isEqualTo(testImage);
            assertThat(wm.position()).isEqualTo(Position.TOP_RIGHT);
            assertThat(wm.opacity()).isEqualTo(0.7f);
            assertThat(wm.margin()).isEqualTo(15);
        }

        @Test
        @DisplayName("从现有水印创建构建器")
        void testBuilderFromExisting() {
            ImageWatermark original = ImageWatermark.of(testImage, Position.CENTER);
            ImageWatermark copy = ImageWatermark.builder(original)
                .position(Position.TOP_LEFT)
                .build();

            assertThat(copy.position()).isEqualTo(Position.TOP_LEFT);
            assertThat(copy.image()).isEqualTo(original.image());
        }
    }

    @Nested
    @DisplayName("常量测试")
    class ConstantTests {

        @Test
        @DisplayName("DEFAULT_OPACITY值")
        void testDefaultOpacity() {
            assertThat(ImageWatermark.DEFAULT_OPACITY).isEqualTo(1.0f);
        }

        @Test
        @DisplayName("DEFAULT_MARGIN值")
        void testDefaultMargin() {
            assertThat(ImageWatermark.DEFAULT_MARGIN).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Watermark接口实现测试")
    class WatermarkInterfaceTests {

        @Test
        @DisplayName("实现Watermark接口")
        void testImplementsWatermark() {
            ImageWatermark wm = ImageWatermark.of(testImage);

            assertThat(wm).isInstanceOf(Watermark.class);
        }

        @Test
        @DisplayName("position方法返回正确值")
        void testPositionMethod() {
            Watermark wm = ImageWatermark.of(testImage, Position.CENTER);

            assertThat(wm.position()).isEqualTo(Position.CENTER);
        }

        @Test
        @DisplayName("opacity方法返回正确值")
        void testOpacityMethod() {
            Watermark wm = ImageWatermark.of(testImage, Position.CENTER, 0.5f);

            assertThat(wm.opacity()).isEqualTo(0.5f);
        }

        @Test
        @DisplayName("margin方法返回正确值")
        void testMarginMethod() {
            ImageWatermark wm = ImageWatermark.builder()
                .image(testImage)
                .margin(25)
                .build();

            assertThat(((Watermark) wm).margin()).isEqualTo(25);
        }
    }
}
