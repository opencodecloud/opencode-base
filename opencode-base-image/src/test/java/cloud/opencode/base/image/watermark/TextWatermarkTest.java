package cloud.opencode.base.image.watermark;

import cloud.opencode.base.image.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TextWatermark 记录测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
@DisplayName("TextWatermark 记录测试")
class TextWatermarkTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("完整参数构造")
        void testFullConstruction() {
            Font font = new Font("Arial", Font.BOLD, 24);
            Color color = Color.RED;
            TextWatermark wm = new TextWatermark("Test", Position.CENTER, font, color, 0.5f, 20);

            assertThat(wm.text()).isEqualTo("Test");
            assertThat(wm.position()).isEqualTo(Position.CENTER);
            assertThat(wm.font()).isEqualTo(font);
            assertThat(wm.color()).isEqualTo(color);
            assertThat(wm.opacity()).isEqualTo(0.5f);
            assertThat(wm.margin()).isEqualTo(20);
        }

        @Test
        @DisplayName("null文本抛出异常")
        void testNullText() {
            assertThatThrownBy(() -> new TextWatermark(null, Position.CENTER, null, null, 0.5f, 10))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("空文本抛出异常")
        void testEmptyText() {
            assertThatThrownBy(() -> new TextWatermark("", Position.CENTER, null, null, 0.5f, 10))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null位置使用默认值")
        void testNullPosition() {
            TextWatermark wm = new TextWatermark("Test", null, null, null, 0.5f, 10);

            assertThat(wm.position()).isEqualTo(Position.BOTTOM_RIGHT);
        }

        @Test
        @DisplayName("null字体使用默认值")
        void testNullFont() {
            TextWatermark wm = new TextWatermark("Test", Position.CENTER, null, Color.RED, 0.5f, 10);

            assertThat(wm.font()).isEqualTo(TextWatermark.DEFAULT_FONT);
        }

        @Test
        @DisplayName("null颜色使用默认值")
        void testNullColor() {
            TextWatermark wm = new TextWatermark("Test", Position.CENTER, null, null, 0.5f, 10);

            assertThat(wm.color()).isEqualTo(TextWatermark.DEFAULT_COLOR);
        }

        @Test
        @DisplayName("负透明度抛出异常")
        void testNegativeOpacity() {
            assertThatThrownBy(() -> new TextWatermark("Test", Position.CENTER, null, null, -0.1f, 10))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("超过1的透明度抛出异常")
        void testOpacityGreaterThanOne() {
            assertThatThrownBy(() -> new TextWatermark("Test", Position.CENTER, null, null, 1.1f, 10))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("负边距抛出异常")
        void testNegativeMargin() {
            assertThatThrownBy(() -> new TextWatermark("Test", Position.CENTER, null, null, 0.5f, -10))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of(text)方法")
        void testOfText() {
            TextWatermark wm = TextWatermark.of("Copyright");

            assertThat(wm.text()).isEqualTo("Copyright");
            assertThat(wm.position()).isEqualTo(Position.BOTTOM_RIGHT);
            assertThat(wm.opacity()).isEqualTo(TextWatermark.DEFAULT_OPACITY);
        }

        @Test
        @DisplayName("of(text, position)方法")
        void testOfTextAndPosition() {
            TextWatermark wm = TextWatermark.of("Copyright", Position.TOP_LEFT);

            assertThat(wm.text()).isEqualTo("Copyright");
            assertThat(wm.position()).isEqualTo(Position.TOP_LEFT);
        }

        @Test
        @DisplayName("of(text, position, font)方法")
        void testOfTextPositionAndFont() {
            Font font = new Font("Arial", Font.BOLD, 20);
            TextWatermark wm = TextWatermark.of("Copyright", Position.CENTER, font);

            assertThat(wm.text()).isEqualTo("Copyright");
            assertThat(wm.position()).isEqualTo(Position.CENTER);
            assertThat(wm.font()).isEqualTo(font);
        }
    }

    @Nested
    @DisplayName("构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("使用构建器创建")
        void testBuilder() {
            TextWatermark wm = TextWatermark.builder()
                .text("Test")
                .position(Position.TOP_RIGHT)
                .font("Arial", 24)
                .color(Color.BLUE)
                .opacity(0.7f)
                .margin(15)
                .build();

            assertThat(wm.text()).isEqualTo("Test");
            assertThat(wm.position()).isEqualTo(Position.TOP_RIGHT);
            assertThat(wm.opacity()).isEqualTo(0.7f);
            assertThat(wm.margin()).isEqualTo(15);
        }

        @Test
        @DisplayName("从现有水印创建构建器")
        void testBuilderFromExisting() {
            TextWatermark original = TextWatermark.of("Original", Position.CENTER);
            TextWatermark copy = TextWatermark.builder(original)
                .text("Modified")
                .build();

            assertThat(copy.text()).isEqualTo("Modified");
            assertThat(copy.position()).isEqualTo(Position.CENTER);
        }

        @Test
        @DisplayName("构建器font(name, size)方法")
        void testBuilderFontNameSize() {
            TextWatermark wm = TextWatermark.builder()
                .text("Test")
                .font("Arial", 24)
                .build();

            assertThat(wm.font().getFamily()).isEqualTo("Arial");
            assertThat(wm.font().getSize()).isEqualTo(24);
        }

        @Test
        @DisplayName("构建器font(name, style, size)方法")
        void testBuilderFontNameStyleSize() {
            TextWatermark wm = TextWatermark.builder()
                .text("Test")
                .font("Arial", Font.BOLD, 24)
                .build();

            assertThat(wm.font().getStyle()).isEqualTo(Font.BOLD);
        }

        @Test
        @DisplayName("构建器color(r, g, b)方法")
        void testBuilderColorRGB() {
            TextWatermark wm = TextWatermark.builder()
                .text("Test")
                .color(255, 0, 0)
                .build();

            assertThat(wm.color().getRed()).isEqualTo(255);
            assertThat(wm.color().getGreen()).isEqualTo(0);
            assertThat(wm.color().getBlue()).isEqualTo(0);
        }

        @Test
        @DisplayName("构建器color(r, g, b, a)方法")
        void testBuilderColorRGBA() {
            TextWatermark wm = TextWatermark.builder()
                .text("Test")
                .color(255, 0, 0, 128)
                .build();

            assertThat(wm.color().getAlpha()).isEqualTo(128);
        }
    }

    @Nested
    @DisplayName("with方法测试")
    class WithMethodTests {

        @Test
        @DisplayName("withText方法")
        void testWithText() {
            TextWatermark original = TextWatermark.of("Original");
            TextWatermark modified = original.withText("Modified");

            assertThat(modified.text()).isEqualTo("Modified");
            assertThat(modified.position()).isEqualTo(original.position());
        }

        @Test
        @DisplayName("withPosition方法")
        void testWithPosition() {
            TextWatermark original = TextWatermark.of("Test");
            TextWatermark modified = original.withPosition(Position.TOP_LEFT);

            assertThat(modified.position()).isEqualTo(Position.TOP_LEFT);
            assertThat(modified.text()).isEqualTo(original.text());
        }

        @Test
        @DisplayName("withOpacity方法")
        void testWithOpacity() {
            TextWatermark original = TextWatermark.of("Test");
            TextWatermark modified = original.withOpacity(0.3f);

            assertThat(modified.opacity()).isEqualTo(0.3f);
            assertThat(modified.text()).isEqualTo(original.text());
        }
    }

    @Nested
    @DisplayName("常量测试")
    class ConstantTests {

        @Test
        @DisplayName("DEFAULT_FONT存在")
        void testDefaultFont() {
            assertThat(TextWatermark.DEFAULT_FONT).isNotNull();
        }

        @Test
        @DisplayName("DEFAULT_COLOR存在")
        void testDefaultColor() {
            assertThat(TextWatermark.DEFAULT_COLOR).isNotNull();
        }

        @Test
        @DisplayName("DEFAULT_OPACITY值")
        void testDefaultOpacity() {
            assertThat(TextWatermark.DEFAULT_OPACITY).isEqualTo(0.8f);
        }

        @Test
        @DisplayName("DEFAULT_MARGIN值")
        void testDefaultMargin() {
            assertThat(TextWatermark.DEFAULT_MARGIN).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Watermark接口实现测试")
    class WatermarkInterfaceTests {

        @Test
        @DisplayName("实现Watermark接口")
        void testImplementsWatermark() {
            TextWatermark wm = TextWatermark.of("Test");

            assertThat(wm).isInstanceOf(Watermark.class);
        }

        @Test
        @DisplayName("position方法返回正确值")
        void testPositionMethod() {
            Watermark wm = TextWatermark.of("Test", Position.CENTER);

            assertThat(wm.position()).isEqualTo(Position.CENTER);
        }

        @Test
        @DisplayName("opacity方法返回正确值")
        void testOpacityMethod() {
            TextWatermark wm = TextWatermark.builder()
                .text("Test")
                .opacity(0.5f)
                .build();

            assertThat(((Watermark) wm).opacity()).isEqualTo(0.5f);
        }

        @Test
        @DisplayName("margin方法返回正确值")
        void testMarginMethod() {
            TextWatermark wm = TextWatermark.builder()
                .text("Test")
                .margin(25)
                .build();

            assertThat(((Watermark) wm).margin()).isEqualTo(25);
        }
    }
}
