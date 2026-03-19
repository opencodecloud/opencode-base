package cloud.opencode.base.image.watermark;

import cloud.opencode.base.image.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.*;

/**
 * Watermark 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
@DisplayName("Watermark 接口测试")
class WatermarkTest {

    @Nested
    @DisplayName("Sealed接口测试")
    class SealedInterfaceTests {

        @Test
        @DisplayName("Watermark是sealed接口")
        void testIsSealed() {
            assertThat(Watermark.class.isSealed()).isTrue();
        }

        @Test
        @DisplayName("允许的实现类")
        void testPermittedSubclasses() {
            Class<?>[] permitted = Watermark.class.getPermittedSubclasses();

            assertThat(permitted).hasSize(2);
            assertThat(permitted).containsExactlyInAnyOrder(
                TextWatermark.class,
                ImageWatermark.class
            );
        }

        @Test
        @DisplayName("TextWatermark实现Watermark")
        void testTextWatermarkImplements() {
            assertThat(Watermark.class.isAssignableFrom(TextWatermark.class)).isTrue();
        }

        @Test
        @DisplayName("ImageWatermark实现Watermark")
        void testImageWatermarkImplements() {
            assertThat(Watermark.class.isAssignableFrom(ImageWatermark.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("接口方法测试")
    class InterfaceMethodsTests {

        @Test
        @DisplayName("接口定义position方法")
        void testPositionMethod() throws NoSuchMethodException {
            assertThat(Watermark.class.getMethod("position")).isNotNull();
            assertThat(Watermark.class.getMethod("position").getReturnType()).isEqualTo(Position.class);
        }

        @Test
        @DisplayName("接口定义opacity方法")
        void testOpacityMethod() throws NoSuchMethodException {
            assertThat(Watermark.class.getMethod("opacity")).isNotNull();
            assertThat(Watermark.class.getMethod("opacity").getReturnType()).isEqualTo(float.class);
        }

        @Test
        @DisplayName("接口定义margin方法")
        void testMarginMethod() throws NoSuchMethodException {
            assertThat(Watermark.class.getMethod("margin")).isNotNull();
            assertThat(Watermark.class.getMethod("margin").getReturnType()).isEqualTo(int.class);
        }
    }

    @Nested
    @DisplayName("多态测试")
    class PolymorphismTests {

        @Test
        @DisplayName("TextWatermark可作为Watermark使用")
        void testTextWatermarkAsWatermark() {
            Watermark watermark = TextWatermark.of("Test");

            assertThat(watermark.position()).isEqualTo(Position.BOTTOM_RIGHT);
            assertThat(watermark.opacity()).isEqualTo(0.8f);  // TextWatermark默认opacity是0.8
            assertThat(watermark.margin()).isEqualTo(10);
        }

        @Test
        @DisplayName("ImageWatermark可作为Watermark使用")
        void testImageWatermarkAsWatermark() {
            BufferedImage wmImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
            Watermark watermark = new ImageWatermark(wmImage, Position.CENTER, 0.8f, 20);

            assertThat(watermark.position()).isEqualTo(Position.CENTER);
            assertThat(watermark.opacity()).isEqualTo(0.8f);
            assertThat(watermark.margin()).isEqualTo(20);
        }

        @Test
        @DisplayName("集合中存储不同类型的Watermark")
        void testWatermarkCollection() {
            BufferedImage wmImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);

            Watermark text = TextWatermark.of("Text");
            Watermark image = new ImageWatermark(wmImage, Position.TOP_LEFT, 1.0f, 5);

            java.util.List<Watermark> watermarks = java.util.List.of(text, image);

            assertThat(watermarks).hasSize(2);
            assertThat(watermarks.get(0)).isInstanceOf(TextWatermark.class);
            assertThat(watermarks.get(1)).isInstanceOf(ImageWatermark.class);
        }
    }

    @Nested
    @DisplayName("Pattern Matching测试")
    class PatternMatchingTests {

        @Test
        @DisplayName("使用switch表达式匹配Watermark类型")
        void testSwitchPatternMatching() {
            Watermark text = TextWatermark.of("Test");
            Watermark image = new ImageWatermark(
                new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB),
                Position.CENTER, 0.5f, 10
            );

            String textType = getWatermarkType(text);
            String imageType = getWatermarkType(image);

            assertThat(textType).isEqualTo("text");
            assertThat(imageType).isEqualTo("image");
        }

        private String getWatermarkType(Watermark watermark) {
            return switch (watermark) {
                case TextWatermark t -> "text";
                case ImageWatermark i -> "image";
            };
        }
    }

    @Nested
    @DisplayName("接口特性测试")
    class InterfaceCharacteristicsTests {

        @Test
        @DisplayName("Watermark是接口")
        void testIsInterface() {
            assertThat(Watermark.class.isInterface()).isTrue();
        }

        @Test
        @DisplayName("Watermark是public")
        void testIsPublic() {
            assertThat(Modifier.isPublic(Watermark.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("Watermark没有字段")
        void testNoFields() {
            assertThat(Watermark.class.getDeclaredFields()).isEmpty();
        }

        @Test
        @DisplayName("Watermark只有3个方法")
        void testMethodCount() {
            // 只有position, opacity, margin三个方法
            assertThat(Watermark.class.getDeclaredMethods()).hasSize(3);
        }
    }
}
