package cloud.opencode.base.pdf.font;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * PdfFont 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("PdfFont 测试")
class PdfFontTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("helvetica 返回 HELVETICA")
        void testHelvetica() {
            PdfFont font = PdfFont.helvetica();

            assertThat(font).isEqualTo(StandardFont.HELVETICA);
            assertThat(font.getName()).isEqualTo("Helvetica");
        }

        @Test
        @DisplayName("helveticaBold 返回 HELVETICA_BOLD")
        void testHelveticaBold() {
            PdfFont font = PdfFont.helveticaBold();

            assertThat(font).isEqualTo(StandardFont.HELVETICA_BOLD);
            assertThat(font.getName()).isEqualTo("Helvetica-Bold");
        }

        @Test
        @DisplayName("helveticaItalic 返回 HELVETICA_OBLIQUE")
        void testHelveticaItalic() {
            PdfFont font = PdfFont.helveticaItalic();

            assertThat(font).isEqualTo(StandardFont.HELVETICA_OBLIQUE);
        }

        @Test
        @DisplayName("timesRoman 返回 TIMES_ROMAN")
        void testTimesRoman() {
            PdfFont font = PdfFont.timesRoman();

            assertThat(font).isEqualTo(StandardFont.TIMES_ROMAN);
            assertThat(font.getName()).isEqualTo("Times-Roman");
        }

        @Test
        @DisplayName("timesBold 返回 TIMES_BOLD")
        void testTimesBold() {
            PdfFont font = PdfFont.timesBold();

            assertThat(font).isEqualTo(StandardFont.TIMES_BOLD);
        }

        @Test
        @DisplayName("courier 返回 COURIER")
        void testCourier() {
            PdfFont font = PdfFont.courier();

            assertThat(font).isEqualTo(StandardFont.COURIER);
            assertThat(font.getName()).isEqualTo("Courier");
        }

        @Test
        @DisplayName("courierBold 返回 COURIER_BOLD")
        void testCourierBold() {
            PdfFont font = PdfFont.courierBold();

            assertThat(font).isEqualTo(StandardFont.COURIER_BOLD);
        }

        @Test
        @DisplayName("symbol 返回 SYMBOL")
        void testSymbol() {
            PdfFont font = PdfFont.symbol();

            assertThat(font).isEqualTo(StandardFont.SYMBOL);
        }

        @Test
        @DisplayName("zapfDingbats 返回 ZAPF_DINGBATS")
        void testZapfDingbats() {
            PdfFont font = PdfFont.zapfDingbats();

            assertThat(font).isEqualTo(StandardFont.ZAPF_DINGBATS);
        }
    }

    @Nested
    @DisplayName("密封接口测试")
    class SealedInterfaceTests {

        @Test
        @DisplayName("PdfFont 是密封接口")
        void testIsSealed() {
            assertThat(PdfFont.class.isSealed()).isTrue();
        }

        @Test
        @DisplayName("StandardFont 实现 PdfFont")
        void testStandardFontImplements() {
            assertThat(StandardFont.HELVETICA).isInstanceOf(PdfFont.class);
        }

        @Test
        @DisplayName("允许的子类只有 StandardFont 和 EmbeddedFont")
        void testPermittedSubclasses() {
            assertThat(PdfFont.class.getPermittedSubclasses())
                .extracting(Class::getSimpleName)
                .containsExactlyInAnyOrder("StandardFont", "EmbeddedFont");
        }
    }

    @Nested
    @DisplayName("接口方法测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("getName 返回非空字体名")
        void testGetName() {
            PdfFont font = PdfFont.helvetica();

            assertThat(font.getName()).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("getPdfName 返回非空 PDF 名称")
        void testGetPdfName() {
            PdfFont font = PdfFont.helvetica();

            assertThat(font.getPdfName()).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("isEmbedded 标准字体返回 false")
        void testIsEmbedded() {
            assertThat(PdfFont.helvetica().isEmbedded()).isFalse();
            assertThat(PdfFont.timesRoman().isEmbedded()).isFalse();
            assertThat(PdfFont.courier().isEmbedded()).isFalse();
        }
    }

    @Nested
    @DisplayName("接口是 interface 测试")
    class InterfaceTypeTests {

        @Test
        @DisplayName("PdfFont 是 interface")
        void testIsInterface() {
            assertThat(PdfFont.class.isInterface()).isTrue();
        }
    }
}
