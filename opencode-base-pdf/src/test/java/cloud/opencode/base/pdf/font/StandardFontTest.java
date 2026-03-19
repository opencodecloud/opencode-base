package cloud.opencode.base.pdf.font;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

/**
 * StandardFont 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("StandardFont 测试")
class StandardFontTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("包含所有标准字体")
        void testAllStandardFonts() {
            assertThat(StandardFont.values()).containsExactly(
                StandardFont.HELVETICA,
                StandardFont.HELVETICA_BOLD,
                StandardFont.HELVETICA_OBLIQUE,
                StandardFont.HELVETICA_BOLD_OBLIQUE,
                StandardFont.TIMES_ROMAN,
                StandardFont.TIMES_BOLD,
                StandardFont.TIMES_ITALIC,
                StandardFont.TIMES_BOLD_ITALIC,
                StandardFont.COURIER,
                StandardFont.COURIER_BOLD,
                StandardFont.COURIER_OBLIQUE,
                StandardFont.COURIER_BOLD_OBLIQUE,
                StandardFont.SYMBOL,
                StandardFont.ZAPF_DINGBATS
            );
        }

        @Test
        @DisplayName("总共14个标准字体")
        void testFontCount() {
            assertThat(StandardFont.values()).hasSize(14);
        }
    }

    @Nested
    @DisplayName("Helvetica 系列测试")
    class HelveticaFamilyTests {

        @Test
        @DisplayName("HELVETICA 基本属性")
        void testHelvetica() {
            assertThat(StandardFont.HELVETICA.getName()).isEqualTo("Helvetica");
            assertThat(StandardFont.HELVETICA.isBold()).isFalse();
            assertThat(StandardFont.HELVETICA.isItalic()).isFalse();
            assertThat(StandardFont.HELVETICA.isHelvetica()).isTrue();
        }

        @Test
        @DisplayName("HELVETICA_BOLD 粗体属性")
        void testHelveticaBold() {
            assertThat(StandardFont.HELVETICA_BOLD.getName()).isEqualTo("Helvetica-Bold");
            assertThat(StandardFont.HELVETICA_BOLD.isBold()).isTrue();
            assertThat(StandardFont.HELVETICA_BOLD.isItalic()).isFalse();
        }

        @Test
        @DisplayName("HELVETICA_OBLIQUE 斜体属性")
        void testHelveticaOblique() {
            assertThat(StandardFont.HELVETICA_OBLIQUE.getName()).isEqualTo("Helvetica-Oblique");
            assertThat(StandardFont.HELVETICA_OBLIQUE.isBold()).isFalse();
            assertThat(StandardFont.HELVETICA_OBLIQUE.isItalic()).isTrue();
        }

        @Test
        @DisplayName("HELVETICA_BOLD_OBLIQUE 粗斜体属性")
        void testHelveticaBoldOblique() {
            assertThat(StandardFont.HELVETICA_BOLD_OBLIQUE.getName()).isEqualTo("Helvetica-BoldOblique");
            assertThat(StandardFont.HELVETICA_BOLD_OBLIQUE.isBold()).isTrue();
            assertThat(StandardFont.HELVETICA_BOLD_OBLIQUE.isItalic()).isTrue();
        }

        @Test
        @DisplayName("Helvetica 系列都是 isHelvetica")
        void testAllHelveticaFamily() {
            assertThat(StandardFont.HELVETICA.isHelvetica()).isTrue();
            assertThat(StandardFont.HELVETICA_BOLD.isHelvetica()).isTrue();
            assertThat(StandardFont.HELVETICA_OBLIQUE.isHelvetica()).isTrue();
            assertThat(StandardFont.HELVETICA_BOLD_OBLIQUE.isHelvetica()).isTrue();
        }
    }

    @Nested
    @DisplayName("Times 系列测试")
    class TimesFamilyTests {

        @Test
        @DisplayName("TIMES_ROMAN 基本属性")
        void testTimesRoman() {
            assertThat(StandardFont.TIMES_ROMAN.getName()).isEqualTo("Times-Roman");
            assertThat(StandardFont.TIMES_ROMAN.isBold()).isFalse();
            assertThat(StandardFont.TIMES_ROMAN.isItalic()).isFalse();
            assertThat(StandardFont.TIMES_ROMAN.isTimes()).isTrue();
        }

        @Test
        @DisplayName("TIMES_BOLD 粗体属性")
        void testTimesBold() {
            assertThat(StandardFont.TIMES_BOLD.getName()).isEqualTo("Times-Bold");
            assertThat(StandardFont.TIMES_BOLD.isBold()).isTrue();
        }

        @Test
        @DisplayName("TIMES_ITALIC 斜体属性")
        void testTimesItalic() {
            assertThat(StandardFont.TIMES_ITALIC.getName()).isEqualTo("Times-Italic");
            assertThat(StandardFont.TIMES_ITALIC.isItalic()).isTrue();
        }

        @Test
        @DisplayName("TIMES_BOLD_ITALIC 粗斜体属性")
        void testTimesBoldItalic() {
            assertThat(StandardFont.TIMES_BOLD_ITALIC.getName()).isEqualTo("Times-BoldItalic");
            assertThat(StandardFont.TIMES_BOLD_ITALIC.isBold()).isTrue();
            assertThat(StandardFont.TIMES_BOLD_ITALIC.isItalic()).isTrue();
        }

        @Test
        @DisplayName("Times 系列都是 isTimes")
        void testAllTimesFamily() {
            assertThat(StandardFont.TIMES_ROMAN.isTimes()).isTrue();
            assertThat(StandardFont.TIMES_BOLD.isTimes()).isTrue();
            assertThat(StandardFont.TIMES_ITALIC.isTimes()).isTrue();
            assertThat(StandardFont.TIMES_BOLD_ITALIC.isTimes()).isTrue();
        }
    }

    @Nested
    @DisplayName("Courier 系列测试")
    class CourierFamilyTests {

        @Test
        @DisplayName("COURIER 基本属性")
        void testCourier() {
            assertThat(StandardFont.COURIER.getName()).isEqualTo("Courier");
            assertThat(StandardFont.COURIER.isBold()).isFalse();
            assertThat(StandardFont.COURIER.isItalic()).isFalse();
            assertThat(StandardFont.COURIER.isCourier()).isTrue();
            assertThat(StandardFont.COURIER.isMonospace()).isTrue();
        }

        @Test
        @DisplayName("COURIER_BOLD 粗体属性")
        void testCourierBold() {
            assertThat(StandardFont.COURIER_BOLD.isBold()).isTrue();
        }

        @Test
        @DisplayName("COURIER_OBLIQUE 斜体属性")
        void testCourierOblique() {
            assertThat(StandardFont.COURIER_OBLIQUE.isItalic()).isTrue();
        }

        @Test
        @DisplayName("COURIER_BOLD_OBLIQUE 粗斜体属性")
        void testCourierBoldOblique() {
            assertThat(StandardFont.COURIER_BOLD_OBLIQUE.isBold()).isTrue();
            assertThat(StandardFont.COURIER_BOLD_OBLIQUE.isItalic()).isTrue();
        }

        @Test
        @DisplayName("Courier 系列都是等宽字体")
        void testAllCourierMonospace() {
            assertThat(StandardFont.COURIER.isMonospace()).isTrue();
            assertThat(StandardFont.COURIER_BOLD.isMonospace()).isTrue();
            assertThat(StandardFont.COURIER_OBLIQUE.isMonospace()).isTrue();
            assertThat(StandardFont.COURIER_BOLD_OBLIQUE.isMonospace()).isTrue();
        }
    }

    @Nested
    @DisplayName("符号字体测试")
    class SymbolFontsTests {

        @Test
        @DisplayName("SYMBOL 字体属性")
        void testSymbol() {
            assertThat(StandardFont.SYMBOL.getName()).isEqualTo("Symbol");
            assertThat(StandardFont.SYMBOL.isBold()).isFalse();
            assertThat(StandardFont.SYMBOL.isItalic()).isFalse();
            assertThat(StandardFont.SYMBOL.isHelvetica()).isFalse();
            assertThat(StandardFont.SYMBOL.isTimes()).isFalse();
            assertThat(StandardFont.SYMBOL.isCourier()).isFalse();
        }

        @Test
        @DisplayName("ZAPF_DINGBATS 字体属性")
        void testZapfDingbats() {
            assertThat(StandardFont.ZAPF_DINGBATS.getName()).isEqualTo("ZapfDingbats");
            assertThat(StandardFont.ZAPF_DINGBATS.isBold()).isFalse();
            assertThat(StandardFont.ZAPF_DINGBATS.isItalic()).isFalse();
        }
    }

    @Nested
    @DisplayName("PdfFont 接口测试")
    class PdfFontInterfaceTests {

        @ParameterizedTest
        @EnumSource(StandardFont.class)
        @DisplayName("所有标准字体实现 PdfFont 接口")
        void testImplementsPdfFont(StandardFont font) {
            assertThat(font).isInstanceOf(PdfFont.class);
        }

        @ParameterizedTest
        @EnumSource(StandardFont.class)
        @DisplayName("所有标准字体有非空字体名称")
        void testAllHaveFontName(StandardFont font) {
            assertThat(font.getName()).isNotNull().isNotEmpty();
        }

        @ParameterizedTest
        @EnumSource(StandardFont.class)
        @DisplayName("所有标准字体有非空 PDF 名称")
        void testAllHavePdfName(StandardFont font) {
            assertThat(font.getPdfName()).isNotNull().isNotEmpty();
        }

        @ParameterizedTest
        @EnumSource(StandardFont.class)
        @DisplayName("所有标准字体不是嵌入字体")
        void testNoneAreEmbedded(StandardFont font) {
            assertThat(font.isEmbedded()).isFalse();
        }
    }

    @Nested
    @DisplayName("valueOf 测试")
    class ValueOfTests {

        @Test
        @DisplayName("valueOf 方法正常工作")
        void testValueOf() {
            assertThat(StandardFont.valueOf("HELVETICA")).isEqualTo(StandardFont.HELVETICA);
            assertThat(StandardFont.valueOf("TIMES_ROMAN")).isEqualTo(StandardFont.TIMES_ROMAN);
            assertThat(StandardFont.valueOf("COURIER")).isEqualTo(StandardFont.COURIER);
        }

        @Test
        @DisplayName("无效名称抛出异常")
        void testInvalidValueOf() {
            assertThatThrownBy(() -> StandardFont.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("字体家族检测互斥测试")
    class FontFamilyMutualExclusionTests {

        @Test
        @DisplayName("Helvetica 不是 Times 或 Courier")
        void testHelveticaNotOthers() {
            assertThat(StandardFont.HELVETICA.isHelvetica()).isTrue();
            assertThat(StandardFont.HELVETICA.isTimes()).isFalse();
            assertThat(StandardFont.HELVETICA.isCourier()).isFalse();
        }

        @Test
        @DisplayName("Times 不是 Helvetica 或 Courier")
        void testTimesNotOthers() {
            assertThat(StandardFont.TIMES_ROMAN.isTimes()).isTrue();
            assertThat(StandardFont.TIMES_ROMAN.isHelvetica()).isFalse();
            assertThat(StandardFont.TIMES_ROMAN.isCourier()).isFalse();
        }

        @Test
        @DisplayName("Courier 不是 Helvetica 或 Times")
        void testCourierNotOthers() {
            assertThat(StandardFont.COURIER.isCourier()).isTrue();
            assertThat(StandardFont.COURIER.isHelvetica()).isFalse();
            assertThat(StandardFont.COURIER.isTimes()).isFalse();
        }
    }
}
