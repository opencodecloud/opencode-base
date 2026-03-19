package cloud.opencode.base.pdf.content;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * PdfEllipse 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("PdfEllipse 测试")
class PdfEllipseTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("of 创建椭圆")
        void testOf() {
            PdfEllipse ellipse = PdfEllipse.of(100f, 200f, 80f, 50f);

            assertThat(ellipse).isNotNull();
            assertThat(ellipse.getCenterX()).isEqualTo(100f);
            assertThat(ellipse.getCenterY()).isEqualTo(200f);
            assertThat(ellipse.getRadiusX()).isEqualTo(80f);
            assertThat(ellipse.getRadiusY()).isEqualTo(50f);
        }

        @Test
        @DisplayName("circle 创建圆")
        void testCircle() {
            PdfEllipse circle = PdfEllipse.circle(100f, 200f, 50f);

            assertThat(circle).isNotNull();
            assertThat(circle.getCenterX()).isEqualTo(100f);
            assertThat(circle.getCenterY()).isEqualTo(200f);
            assertThat(circle.getRadiusX()).isEqualTo(50f);
            assertThat(circle.getRadiusY()).isEqualTo(50f);
            assertThat(circle.isCircle()).isTrue();
        }

        @Test
        @DisplayName("builder 创建构建器")
        void testBuilder() {
            PdfEllipse ellipse = PdfEllipse.builder();

            assertThat(ellipse).isNotNull();
        }
    }

    @Nested
    @DisplayName("Builder 方法测试")
    class BuilderMethodTests {

        @Test
        @DisplayName("center 设置中心点")
        void testCenter() {
            PdfEllipse ellipse = PdfEllipse.builder().center(150f, 250f);

            assertThat(ellipse.getCenterX()).isEqualTo(150f);
            assertThat(ellipse.getCenterY()).isEqualTo(250f);
        }

        @Test
        @DisplayName("radius(x, y) 设置椭圆半径")
        void testRadiusXY() {
            PdfEllipse ellipse = PdfEllipse.builder().radius(60f, 40f);

            assertThat(ellipse.getRadiusX()).isEqualTo(60f);
            assertThat(ellipse.getRadiusY()).isEqualTo(40f);
        }

        @Test
        @DisplayName("radius(r) 设置圆形半径")
        void testRadiusSingle() {
            PdfEllipse ellipse = PdfEllipse.builder().radius(50f);

            assertThat(ellipse.getRadiusX()).isEqualTo(50f);
            assertThat(ellipse.getRadiusY()).isEqualTo(50f);
        }

        @Test
        @DisplayName("strokeColor 设置描边颜色")
        void testStrokeColor() {
            PdfEllipse ellipse = PdfEllipse.builder().strokeColor(PdfColor.GREEN);

            assertThat(ellipse.getStrokeColor()).isEqualTo(PdfColor.GREEN);
        }

        @Test
        @DisplayName("fillColor 设置填充颜色")
        void testFillColor() {
            PdfEllipse ellipse = PdfEllipse.builder().fillColor(PdfColor.YELLOW);

            assertThat(ellipse.getFillColor()).isEqualTo(PdfColor.YELLOW);
            assertThat(ellipse.isFilled()).isTrue();
        }

        @Test
        @DisplayName("strokeWidth 设置线宽")
        void testStrokeWidth() {
            PdfEllipse ellipse = PdfEllipse.builder().strokeWidth(2f);

            assertThat(ellipse.getStrokeWidth()).isEqualTo(2f);
        }
    }

    @Nested
    @DisplayName("isCircle 方法测试")
    class IsCircleTests {

        @Test
        @DisplayName("圆形返回 true")
        void testIsCircleTrue() {
            PdfEllipse circle = PdfEllipse.circle(100f, 200f, 50f);

            assertThat(circle.isCircle()).isTrue();
        }

        @Test
        @DisplayName("椭圆返回 false")
        void testIsCircleFalse() {
            PdfEllipse ellipse = PdfEllipse.of(100f, 200f, 80f, 50f);

            assertThat(ellipse.isCircle()).isFalse();
        }
    }

    @Nested
    @DisplayName("isFilled 方法测试")
    class IsFilledTests {

        @Test
        @DisplayName("未设置填充颜色时返回 false")
        void testNotFilled() {
            PdfEllipse ellipse = PdfEllipse.of(0, 0, 50, 50);

            assertThat(ellipse.isFilled()).isFalse();
        }

        @Test
        @DisplayName("设置填充颜色后返回 true")
        void testFilled() {
            PdfEllipse ellipse = PdfEllipse.of(0, 0, 50, 50).fillColor(PdfColor.GRAY);

            assertThat(ellipse.isFilled()).isTrue();
        }
    }

    @Nested
    @DisplayName("PdfElement 接口测试")
    class PdfElementInterfaceTests {

        @Test
        @DisplayName("实现 PdfElement 接口")
        void testImplementsPdfElement() {
            PdfEllipse ellipse = PdfEllipse.of(100, 100, 50, 30);

            assertThat(ellipse).isInstanceOf(PdfElement.class);
        }

        @Test
        @DisplayName("getX 返回中心X减半径X（左边界）")
        void testGetX() {
            PdfEllipse ellipse = PdfEllipse.of(100f, 200f, 50f, 30f);

            assertThat(ellipse.getX()).isEqualTo(50f); // centerX - radiusX
        }

        @Test
        @DisplayName("getY 返回中心Y减半径Y（下边界）")
        void testGetY() {
            PdfEllipse ellipse = PdfEllipse.of(100f, 200f, 50f, 30f);

            assertThat(ellipse.getY()).isEqualTo(170f); // centerY - radiusY
        }
    }

    @Nested
    @DisplayName("链式调用测试")
    class FluentApiTests {

        @Test
        @DisplayName("完整的链式调用")
        void testFullFluentApi() {
            PdfEllipse ellipse = PdfEllipse.of(100f, 200f, 80f, 50f)
                .strokeColor(PdfColor.RED)
                .fillColor(PdfColor.rgb(255, 200, 200))
                .strokeWidth(2f);

            assertThat(ellipse.getCenterX()).isEqualTo(100f);
            assertThat(ellipse.getCenterY()).isEqualTo(200f);
            assertThat(ellipse.getStrokeColor()).isEqualTo(PdfColor.RED);
            assertThat(ellipse.getStrokeWidth()).isEqualTo(2f);
            assertThat(ellipse.isFilled()).isTrue();
        }

        @Test
        @DisplayName("返回相同实例")
        void testReturnsSameInstance() {
            PdfEllipse ellipse = PdfEllipse.builder();

            assertThat(ellipse.center(0, 0)).isSameAs(ellipse);
            assertThat(ellipse.radius(50, 50)).isSameAs(ellipse);
            assertThat(ellipse.strokeWidth(1)).isSameAs(ellipse);
            assertThat(ellipse.strokeColor(PdfColor.BLACK)).isSameAs(ellipse);
            assertThat(ellipse.fillColor(PdfColor.WHITE)).isSameAs(ellipse);
        }
    }

    @Nested
    @DisplayName("默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("默认线宽为 1")
        void testDefaultStrokeWidth() {
            PdfEllipse ellipse = PdfEllipse.of(0, 0, 50, 50);

            assertThat(ellipse.getStrokeWidth()).isEqualTo(1f);
        }

        @Test
        @DisplayName("默认描边颜色为黑色")
        void testDefaultStrokeColor() {
            PdfEllipse ellipse = PdfEllipse.of(0, 0, 50, 50);

            assertThat(ellipse.getStrokeColor()).isEqualTo(PdfColor.BLACK);
        }

        @Test
        @DisplayName("默认不填充")
        void testDefaultNotFilled() {
            PdfEllipse ellipse = PdfEllipse.of(0, 0, 50, 50);

            assertThat(ellipse.isFilled()).isFalse();
            assertThat(ellipse.getFillColor()).isNull();
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionTests {

        @Test
        @DisplayName("负坐标")
        void testNegativeCoordinates() {
            PdfEllipse ellipse = PdfEllipse.of(-100f, -200f, 50f, 30f);

            assertThat(ellipse.getCenterX()).isEqualTo(-100f);
            assertThat(ellipse.getCenterY()).isEqualTo(-200f);
        }

        @Test
        @DisplayName("零半径")
        void testZeroRadius() {
            PdfEllipse ellipse = PdfEllipse.of(100f, 100f, 0f, 0f);

            assertThat(ellipse.getRadiusX()).isEqualTo(0f);
            assertThat(ellipse.getRadiusY()).isEqualTo(0f);
        }

        @Test
        @DisplayName("圆形（相等半径）")
        void testCircularEllipse() {
            PdfEllipse ellipse = PdfEllipse.of(100f, 100f, 50f, 50f);

            assertThat(ellipse.getRadiusX()).isEqualTo(ellipse.getRadiusY());
            assertThat(ellipse.isCircle()).isTrue();
        }

        @Test
        @DisplayName("水平椭圆（X半径大于Y半径）")
        void testHorizontalEllipse() {
            PdfEllipse ellipse = PdfEllipse.of(100f, 100f, 100f, 50f);

            assertThat(ellipse.getRadiusX()).isGreaterThan(ellipse.getRadiusY());
        }

        @Test
        @DisplayName("垂直椭圆（Y半径大于X半径）")
        void testVerticalEllipse() {
            PdfEllipse ellipse = PdfEllipse.of(100f, 100f, 50f, 100f);

            assertThat(ellipse.getRadiusY()).isGreaterThan(ellipse.getRadiusX());
        }
    }

    @Nested
    @DisplayName("final 类测试")
    class FinalClassTests {

        @Test
        @DisplayName("PdfEllipse 是 final 类")
        void testIsFinalClass() {
            assertThat(java.lang.reflect.Modifier.isFinal(PdfEllipse.class.getModifiers())).isTrue();
        }
    }
}
