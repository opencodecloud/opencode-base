package cloud.opencode.base.pdf.content;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * PdfLine 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("PdfLine 测试")
class PdfLineTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("of 创建线条")
        void testOf() {
            PdfLine line = PdfLine.of(0f, 0f, 100f, 100f);

            assertThat(line).isNotNull();
            assertThat(line.getX1()).isEqualTo(0f);
            assertThat(line.getY1()).isEqualTo(0f);
            assertThat(line.getX2()).isEqualTo(100f);
            assertThat(line.getY2()).isEqualTo(100f);
        }

        @Test
        @DisplayName("builder 创建构建器")
        void testBuilder() {
            PdfLine line = PdfLine.builder();

            assertThat(line).isNotNull();
        }
    }

    @Nested
    @DisplayName("Builder 方法测试")
    class BuilderMethodTests {

        @Test
        @DisplayName("from 设置起点")
        void testFrom() {
            PdfLine line = PdfLine.builder().from(10f, 20f);

            assertThat(line.getX1()).isEqualTo(10f);
            assertThat(line.getY1()).isEqualTo(20f);
        }

        @Test
        @DisplayName("to 设置终点")
        void testTo() {
            PdfLine line = PdfLine.builder().to(100f, 200f);

            assertThat(line.getX2()).isEqualTo(100f);
            assertThat(line.getY2()).isEqualTo(200f);
        }

        @Test
        @DisplayName("color 设置线条颜色")
        void testColor() {
            PdfLine line = PdfLine.builder().color(PdfColor.RED);

            assertThat(line.getColor()).isEqualTo(PdfColor.RED);
        }

        @Test
        @DisplayName("lineWidth 设置线宽")
        void testLineWidth() {
            PdfLine line = PdfLine.builder().lineWidth(2.5f);

            assertThat(line.getLineWidth()).isEqualTo(2.5f);
        }

        @Test
        @DisplayName("style 设置线条样式")
        void testStyle() {
            PdfLine line = PdfLine.builder().style(PdfLine.LineStyle.DASHED);

            assertThat(line.getStyle()).isEqualTo(PdfLine.LineStyle.DASHED);
        }
    }

    @Nested
    @DisplayName("LineStyle 枚举测试")
    class LineStyleEnumTests {

        @Test
        @DisplayName("包含所有线条样式")
        void testAllLineStyles() {
            assertThat(PdfLine.LineStyle.values()).containsExactly(
                PdfLine.LineStyle.SOLID,
                PdfLine.LineStyle.DASHED,
                PdfLine.LineStyle.DOTTED
            );
        }

        @Test
        @DisplayName("valueOf 方法正常工作")
        void testValueOf() {
            assertThat(PdfLine.LineStyle.valueOf("SOLID")).isEqualTo(PdfLine.LineStyle.SOLID);
            assertThat(PdfLine.LineStyle.valueOf("DASHED")).isEqualTo(PdfLine.LineStyle.DASHED);
            assertThat(PdfLine.LineStyle.valueOf("DOTTED")).isEqualTo(PdfLine.LineStyle.DOTTED);
        }
    }

    @Nested
    @DisplayName("PdfElement 接口测试")
    class PdfElementInterfaceTests {

        @Test
        @DisplayName("实现 PdfElement 接口")
        void testImplementsPdfElement() {
            PdfLine line = PdfLine.of(0, 0, 100, 100);

            assertThat(line).isInstanceOf(PdfElement.class);
        }

        @Test
        @DisplayName("getX 返回起点 X")
        void testGetX() {
            PdfLine line = PdfLine.of(50f, 100f, 200f, 300f);

            assertThat(line.getX()).isEqualTo(50f);
        }

        @Test
        @DisplayName("getY 返回起点 Y")
        void testGetY() {
            PdfLine line = PdfLine.of(50f, 100f, 200f, 300f);

            assertThat(line.getY()).isEqualTo(100f);
        }
    }

    @Nested
    @DisplayName("链式调用测试")
    class FluentApiTests {

        @Test
        @DisplayName("完整的链式调用")
        void testFullFluentApi() {
            PdfLine line = PdfLine.of(0f, 0f, 100f, 100f)
                .color(PdfColor.BLUE)
                .lineWidth(1.5f)
                .style(PdfLine.LineStyle.DASHED);

            assertThat(line.getColor()).isEqualTo(PdfColor.BLUE);
            assertThat(line.getLineWidth()).isEqualTo(1.5f);
            assertThat(line.getStyle()).isEqualTo(PdfLine.LineStyle.DASHED);
        }

        @Test
        @DisplayName("返回相同实例")
        void testReturnsSameInstance() {
            PdfLine line = PdfLine.builder();

            assertThat(line.from(0, 0)).isSameAs(line);
            assertThat(line.to(100, 100)).isSameAs(line);
            assertThat(line.lineWidth(1)).isSameAs(line);
            assertThat(line.color(PdfColor.BLACK)).isSameAs(line);
            assertThat(line.style(PdfLine.LineStyle.SOLID)).isSameAs(line);
        }
    }

    @Nested
    @DisplayName("默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("默认线宽为 1")
        void testDefaultLineWidth() {
            PdfLine line = PdfLine.of(0, 0, 100, 100);

            assertThat(line.getLineWidth()).isEqualTo(1f);
        }

        @Test
        @DisplayName("默认颜色为黑色")
        void testDefaultColor() {
            PdfLine line = PdfLine.of(0, 0, 100, 100);

            assertThat(line.getColor()).isEqualTo(PdfColor.BLACK);
        }

        @Test
        @DisplayName("默认样式为实线")
        void testDefaultStyle() {
            PdfLine line = PdfLine.of(0, 0, 100, 100);

            assertThat(line.getStyle()).isEqualTo(PdfLine.LineStyle.SOLID);
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionTests {

        @Test
        @DisplayName("负坐标")
        void testNegativeCoordinates() {
            PdfLine line = PdfLine.of(-50f, -100f, -25f, -50f);

            assertThat(line.getX1()).isEqualTo(-50f);
            assertThat(line.getY1()).isEqualTo(-100f);
            assertThat(line.getX2()).isEqualTo(-25f);
            assertThat(line.getY2()).isEqualTo(-50f);
        }

        @Test
        @DisplayName("零长度线（点）")
        void testZeroLengthLine() {
            PdfLine line = PdfLine.of(100f, 100f, 100f, 100f);

            assertThat(line.getX1()).isEqualTo(line.getX2());
            assertThat(line.getY1()).isEqualTo(line.getY2());
        }

        @Test
        @DisplayName("水平线")
        void testHorizontalLine() {
            PdfLine line = PdfLine.of(0f, 100f, 500f, 100f);

            assertThat(line.getY1()).isEqualTo(line.getY2());
        }

        @Test
        @DisplayName("垂直线")
        void testVerticalLine() {
            PdfLine line = PdfLine.of(100f, 0f, 100f, 500f);

            assertThat(line.getX1()).isEqualTo(line.getX2());
        }
    }

    @Nested
    @DisplayName("final 类测试")
    class FinalClassTests {

        @Test
        @DisplayName("PdfLine 是 final 类")
        void testIsFinalClass() {
            assertThat(java.lang.reflect.Modifier.isFinal(PdfLine.class.getModifiers())).isTrue();
        }
    }
}
