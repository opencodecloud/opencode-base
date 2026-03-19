package cloud.opencode.base.pdf.content;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * PdfRectangle 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("PdfRectangle 测试")
class PdfRectangleTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("of 创建矩形")
        void testOf() {
            PdfRectangle rect = PdfRectangle.of(10f, 20f, 100f, 50f);

            assertThat(rect).isNotNull();
            assertThat(rect.getX()).isEqualTo(10f);
            assertThat(rect.getY()).isEqualTo(20f);
            assertThat(rect.getWidth()).isEqualTo(100f);
            assertThat(rect.getHeight()).isEqualTo(50f);
        }

        @Test
        @DisplayName("builder 创建构建器")
        void testBuilder() {
            PdfRectangle rect = PdfRectangle.builder();

            assertThat(rect).isNotNull();
        }
    }

    @Nested
    @DisplayName("Builder 方法测试")
    class BuilderMethodTests {

        @Test
        @DisplayName("position 设置位置")
        void testPosition() {
            PdfRectangle rect = PdfRectangle.builder().position(50f, 100f);

            assertThat(rect.getX()).isEqualTo(50f);
            assertThat(rect.getY()).isEqualTo(100f);
        }

        @Test
        @DisplayName("size 设置大小")
        void testSize() {
            PdfRectangle rect = PdfRectangle.builder().size(200f, 150f);

            assertThat(rect.getWidth()).isEqualTo(200f);
            assertThat(rect.getHeight()).isEqualTo(150f);
        }

        @Test
        @DisplayName("strokeColor 设置描边颜色")
        void testStrokeColor() {
            PdfRectangle rect = PdfRectangle.builder().strokeColor(PdfColor.BLUE);

            assertThat(rect.getStrokeColor()).isEqualTo(PdfColor.BLUE);
        }

        @Test
        @DisplayName("fillColor 设置填充颜色")
        void testFillColor() {
            PdfRectangle rect = PdfRectangle.builder().fillColor(PdfColor.rgb(255, 200, 100));

            assertThat(rect.getFillColor()).isNotNull();
            assertThat(rect.isFilled()).isTrue();
        }

        @Test
        @DisplayName("strokeWidth 设置线宽")
        void testStrokeWidth() {
            PdfRectangle rect = PdfRectangle.builder().strokeWidth(3f);

            assertThat(rect.getStrokeWidth()).isEqualTo(3f);
        }

        @Test
        @DisplayName("cornerRadius 设置圆角半径")
        void testCornerRadius() {
            PdfRectangle rect = PdfRectangle.builder().cornerRadius(10f);

            assertThat(rect.getCornerRadius()).isEqualTo(10f);
        }
    }

    @Nested
    @DisplayName("isFilled 方法测试")
    class IsFilledTests {

        @Test
        @DisplayName("未设置填充颜色时返回 false")
        void testNotFilled() {
            PdfRectangle rect = PdfRectangle.of(0, 0, 100, 100);

            assertThat(rect.isFilled()).isFalse();
        }

        @Test
        @DisplayName("设置填充颜色后返回 true")
        void testFilled() {
            PdfRectangle rect = PdfRectangle.of(0, 0, 100, 100).fillColor(PdfColor.GRAY);

            assertThat(rect.isFilled()).isTrue();
        }
    }

    @Nested
    @DisplayName("PdfElement 接口测试")
    class PdfElementInterfaceTests {

        @Test
        @DisplayName("实现 PdfElement 接口")
        void testImplementsPdfElement() {
            PdfRectangle rect = PdfRectangle.of(0, 0, 100, 100);

            assertThat(rect).isInstanceOf(PdfElement.class);
        }

        @Test
        @DisplayName("getX 返回正确值")
        void testGetX() {
            PdfRectangle rect = PdfRectangle.of(50f, 100f, 200f, 150f);

            assertThat(rect.getX()).isEqualTo(50f);
        }

        @Test
        @DisplayName("getY 返回正确值")
        void testGetY() {
            PdfRectangle rect = PdfRectangle.of(50f, 100f, 200f, 150f);

            assertThat(rect.getY()).isEqualTo(100f);
        }
    }

    @Nested
    @DisplayName("链式调用测试")
    class FluentApiTests {

        @Test
        @DisplayName("完整的链式调用")
        void testFullFluentApi() {
            PdfRectangle rect = PdfRectangle.of(10f, 20f, 100f, 80f)
                .strokeColor(PdfColor.BLACK)
                .fillColor(PdfColor.rgb(240, 240, 240))
                .strokeWidth(1.5f)
                .cornerRadius(5f);

            assertThat(rect.getX()).isEqualTo(10f);
            assertThat(rect.getY()).isEqualTo(20f);
            assertThat(rect.getWidth()).isEqualTo(100f);
            assertThat(rect.getHeight()).isEqualTo(80f);
            assertThat(rect.getStrokeWidth()).isEqualTo(1.5f);
            assertThat(rect.getCornerRadius()).isEqualTo(5f);
            assertThat(rect.isFilled()).isTrue();
        }

        @Test
        @DisplayName("返回相同实例")
        void testReturnsSameInstance() {
            PdfRectangle rect = PdfRectangle.builder();

            assertThat(rect.position(0, 0)).isSameAs(rect);
            assertThat(rect.size(100, 100)).isSameAs(rect);
            assertThat(rect.strokeWidth(1)).isSameAs(rect);
            assertThat(rect.strokeColor(PdfColor.BLACK)).isSameAs(rect);
            assertThat(rect.fillColor(PdfColor.WHITE)).isSameAs(rect);
            assertThat(rect.cornerRadius(0)).isSameAs(rect);
        }
    }

    @Nested
    @DisplayName("默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("默认线宽为 1")
        void testDefaultStrokeWidth() {
            PdfRectangle rect = PdfRectangle.of(0, 0, 100, 100);

            assertThat(rect.getStrokeWidth()).isEqualTo(1f);
        }

        @Test
        @DisplayName("默认描边颜色为黑色")
        void testDefaultStrokeColor() {
            PdfRectangle rect = PdfRectangle.of(0, 0, 100, 100);

            assertThat(rect.getStrokeColor()).isEqualTo(PdfColor.BLACK);
        }

        @Test
        @DisplayName("默认不填充")
        void testDefaultNotFilled() {
            PdfRectangle rect = PdfRectangle.of(0, 0, 100, 100);

            assertThat(rect.isFilled()).isFalse();
            assertThat(rect.getFillColor()).isNull();
        }

        @Test
        @DisplayName("默认圆角半径为 0")
        void testDefaultCornerRadius() {
            PdfRectangle rect = PdfRectangle.of(0, 0, 100, 100);

            assertThat(rect.getCornerRadius()).isEqualTo(0f);
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionTests {

        @Test
        @DisplayName("负坐标")
        void testNegativeCoordinates() {
            PdfRectangle rect = PdfRectangle.of(-50f, -100f, 100f, 80f);

            assertThat(rect.getX()).isEqualTo(-50f);
            assertThat(rect.getY()).isEqualTo(-100f);
        }

        @Test
        @DisplayName("零尺寸矩形")
        void testZeroSizeRectangle() {
            PdfRectangle rect = PdfRectangle.of(100f, 100f, 0f, 0f);

            assertThat(rect.getWidth()).isEqualTo(0f);
            assertThat(rect.getHeight()).isEqualTo(0f);
        }

        @Test
        @DisplayName("正方形")
        void testSquare() {
            PdfRectangle rect = PdfRectangle.of(0f, 0f, 100f, 100f);

            assertThat(rect.getWidth()).isEqualTo(rect.getHeight());
        }

        @Test
        @DisplayName("大圆角半径")
        void testLargeCornerRadius() {
            PdfRectangle rect = PdfRectangle.of(0f, 0f, 100f, 100f).cornerRadius(50f);

            assertThat(rect.getCornerRadius()).isEqualTo(50f);
        }
    }

    @Nested
    @DisplayName("final 类测试")
    class FinalClassTests {

        @Test
        @DisplayName("PdfRectangle 是 final 类")
        void testIsFinalClass() {
            assertThat(java.lang.reflect.Modifier.isFinal(PdfRectangle.class.getModifiers())).isTrue();
        }
    }
}
