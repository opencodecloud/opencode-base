package cloud.opencode.base.pdf.content;

import cloud.opencode.base.pdf.font.PdfFont;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * PdfWatermark 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.3
 */
@DisplayName("PdfWatermark 测试")
class PdfWatermarkTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("text 创建水印")
        void testTextFactory() {
            PdfWatermark watermark = PdfWatermark.text("CONFIDENTIAL");

            assertThat(watermark.getText()).isEqualTo("CONFIDENTIAL");
        }

        @Test
        @DisplayName("text null 抛出异常")
        void testTextNull() {
            assertThatThrownBy(() -> PdfWatermark.text(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("watermark text");
        }
    }

    @Nested
    @DisplayName("默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("默认旋转角度为 -45")
        void testDefaultRotation() {
            PdfWatermark watermark = PdfWatermark.text("TEST");

            assertThat(watermark.getRotation()).isEqualTo(-45f);
        }

        @Test
        @DisplayName("默认透明度为 0.15")
        void testDefaultOpacity() {
            PdfWatermark watermark = PdfWatermark.text("TEST");

            assertThat(watermark.getOpacity()).isEqualTo(0.15f);
        }

        @Test
        @DisplayName("默认颜色为 GRAY")
        void testDefaultColor() {
            PdfWatermark watermark = PdfWatermark.text("TEST");

            assertThat(watermark.getColor()).isEqualTo(PdfColor.GRAY);
        }

        @Test
        @DisplayName("默认字号为 60")
        void testDefaultFontSize() {
            PdfWatermark watermark = PdfWatermark.text("TEST");

            assertThat(watermark.getFontSize()).isEqualTo(60f);
        }

        @Test
        @DisplayName("默认字体为 null (Helvetica)")
        void testDefaultFont() {
            PdfWatermark watermark = PdfWatermark.text("TEST");

            assertThat(watermark.getFont()).isNull();
        }
    }

    @Nested
    @DisplayName("链式构建测试")
    class BuilderTests {

        @Test
        @DisplayName("rotation 设置旋转角度")
        void testRotation() {
            PdfWatermark watermark = PdfWatermark.text("TEST").rotation(-30f);

            assertThat(watermark.getRotation()).isEqualTo(-30f);
        }

        @Test
        @DisplayName("opacity 设置透明度")
        void testOpacity() {
            PdfWatermark watermark = PdfWatermark.text("TEST").opacity(0.5f);

            assertThat(watermark.getOpacity()).isEqualTo(0.5f);
        }

        @Test
        @DisplayName("opacity 边界值 0.0")
        void testOpacityZero() {
            PdfWatermark watermark = PdfWatermark.text("TEST").opacity(0.0f);

            assertThat(watermark.getOpacity()).isEqualTo(0.0f);
        }

        @Test
        @DisplayName("opacity 边界值 1.0")
        void testOpacityOne() {
            PdfWatermark watermark = PdfWatermark.text("TEST").opacity(1.0f);

            assertThat(watermark.getOpacity()).isEqualTo(1.0f);
        }

        @Test
        @DisplayName("opacity 超出范围抛出异常")
        void testOpacityOutOfRange() {
            assertThatThrownBy(() -> PdfWatermark.text("TEST").opacity(1.5f))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("0.0 and 1.0");

            assertThatThrownBy(() -> PdfWatermark.text("TEST").opacity(-0.1f))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("color 设置颜色")
        void testColor() {
            PdfWatermark watermark = PdfWatermark.text("TEST").color(PdfColor.RED);

            assertThat(watermark.getColor()).isEqualTo(PdfColor.RED);
        }

        @Test
        @DisplayName("color null 抛出异常")
        void testColorNull() {
            assertThatThrownBy(() -> PdfWatermark.text("TEST").color(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("fontSize 设置字号")
        void testFontSize() {
            PdfWatermark watermark = PdfWatermark.text("TEST").fontSize(80f);

            assertThat(watermark.getFontSize()).isEqualTo(80f);
        }

        @Test
        @DisplayName("fontSize 非正数抛出异常")
        void testFontSizeInvalid() {
            assertThatThrownBy(() -> PdfWatermark.text("TEST").fontSize(0f))
                .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> PdfWatermark.text("TEST").fontSize(-10f))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("font 设置字体")
        void testFont() {
            PdfFont font = PdfFont.courierBold();
            PdfWatermark watermark = PdfWatermark.text("TEST").font(font);

            assertThat(watermark.getFont()).isEqualTo(font);
        }

        @Test
        @DisplayName("font null 允许")
        void testFontNull() {
            PdfWatermark watermark = PdfWatermark.text("TEST").font(null);

            assertThat(watermark.getFont()).isNull();
        }

        @Test
        @DisplayName("完整链式调用")
        void testFullChain() {
            PdfFont font = PdfFont.helveticaBold();
            PdfWatermark watermark = PdfWatermark.text("DRAFT")
                .rotation(-30f)
                .opacity(0.2f)
                .color(PdfColor.RED)
                .fontSize(80f)
                .font(font);

            assertThat(watermark.getText()).isEqualTo("DRAFT");
            assertThat(watermark.getRotation()).isEqualTo(-30f);
            assertThat(watermark.getOpacity()).isEqualTo(0.2f);
            assertThat(watermark.getColor()).isEqualTo(PdfColor.RED);
            assertThat(watermark.getFontSize()).isEqualTo(80f);
            assertThat(watermark.getFont()).isEqualTo(font);
        }
    }

    @Nested
    @DisplayName("toString 测试")
    class ToStringTests {

        @Test
        @DisplayName("toString 包含关键信息")
        void testToString() {
            PdfWatermark watermark = PdfWatermark.text("DRAFT");

            String str = watermark.toString();
            assertThat(str).contains("PdfWatermark");
            assertThat(str).contains("DRAFT");
            assertThat(str).contains("-45");
            assertThat(str).contains("0.15");
            assertThat(str).contains("60");
        }
    }
}
