package cloud.opencode.base.pdf.content;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * PdfElement 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("PdfElement 测试")
class PdfElementTest {

    @Nested
    @DisplayName("密封接口测试")
    class SealedInterfaceTests {

        @Test
        @DisplayName("PdfElement 是密封接口")
        void testIsSealed() {
            assertThat(PdfElement.class.isSealed()).isTrue();
        }

        @Test
        @DisplayName("允许的子类包含 PdfText")
        void testPermitsPdfText() {
            assertThat(PdfElement.class.getPermittedSubclasses())
                .extracting(Class::getSimpleName)
                .contains("PdfText");
        }

        @Test
        @DisplayName("允许的子类包含 PdfParagraph")
        void testPermitsPdfParagraph() {
            assertThat(PdfElement.class.getPermittedSubclasses())
                .extracting(Class::getSimpleName)
                .contains("PdfParagraph");
        }

        @Test
        @DisplayName("允许的子类包含 PdfImage")
        void testPermitsPdfImage() {
            assertThat(PdfElement.class.getPermittedSubclasses())
                .extracting(Class::getSimpleName)
                .contains("PdfImage");
        }

        @Test
        @DisplayName("允许的子类包含 PdfTable")
        void testPermitsPdfTable() {
            assertThat(PdfElement.class.getPermittedSubclasses())
                .extracting(Class::getSimpleName)
                .contains("PdfTable");
        }

        @Test
        @DisplayName("允许的子类包含 PdfLine")
        void testPermitsPdfLine() {
            assertThat(PdfElement.class.getPermittedSubclasses())
                .extracting(Class::getSimpleName)
                .contains("PdfLine");
        }

        @Test
        @DisplayName("允许的子类包含 PdfRectangle")
        void testPermitsPdfRectangle() {
            assertThat(PdfElement.class.getPermittedSubclasses())
                .extracting(Class::getSimpleName)
                .contains("PdfRectangle");
        }

        @Test
        @DisplayName("允许的子类包含 PdfEllipse")
        void testPermitsPdfEllipse() {
            assertThat(PdfElement.class.getPermittedSubclasses())
                .extracting(Class::getSimpleName)
                .contains("PdfEllipse");
        }

        @Test
        @DisplayName("共有7个允许的子类")
        void testPermittedSubclassCount() {
            assertThat(PdfElement.class.getPermittedSubclasses()).hasSize(7);
        }
    }

    @Nested
    @DisplayName("接口方法声明测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("声明 getX 方法")
        void testHasGetXMethod() throws NoSuchMethodException {
            assertThat(PdfElement.class.getMethod("getX")).isNotNull();
        }

        @Test
        @DisplayName("声明 getY 方法")
        void testHasGetYMethod() throws NoSuchMethodException {
            assertThat(PdfElement.class.getMethod("getY")).isNotNull();
        }
    }

    @Nested
    @DisplayName("实现类测试")
    class ImplementationTests {

        @Test
        @DisplayName("PdfText 实现 PdfElement")
        void testPdfTextImplements() {
            PdfText text = PdfText.of("test", 100f, 200f);

            assertThat(text).isInstanceOf(PdfElement.class);
            assertThat(text.getX()).isEqualTo(100f);
            assertThat(text.getY()).isEqualTo(200f);
        }

        @Test
        @DisplayName("PdfLine 实现 PdfElement")
        void testPdfLineImplements() {
            PdfLine line = PdfLine.of(10f, 20f, 30f, 40f);

            assertThat(line).isInstanceOf(PdfElement.class);
            assertThat(line.getX()).isEqualTo(10f);
            assertThat(line.getY()).isEqualTo(20f);
        }

        @Test
        @DisplayName("PdfRectangle 实现 PdfElement")
        void testPdfRectangleImplements() {
            PdfRectangle rect = PdfRectangle.of(50f, 60f, 100f, 80f);

            assertThat(rect).isInstanceOf(PdfElement.class);
            assertThat(rect.getX()).isEqualTo(50f);
            assertThat(rect.getY()).isEqualTo(60f);
        }

        @Test
        @DisplayName("PdfEllipse 实现 PdfElement")
        void testPdfEllipseImplements() {
            PdfEllipse ellipse = PdfEllipse.of(100f, 200f, 50f, 30f);

            assertThat(ellipse).isInstanceOf(PdfElement.class);
            // getX returns centerX - radiusX = 100 - 50 = 50
            assertThat(ellipse.getX()).isEqualTo(50f);
            // getY returns centerY - radiusY = 200 - 30 = 170
            assertThat(ellipse.getY()).isEqualTo(170f);
        }

        @Test
        @DisplayName("PdfParagraph 实现 PdfElement")
        void testPdfParagraphImplements() {
            PdfParagraph paragraph = PdfParagraph.of("text", 75f, 800f, 400f);

            assertThat(paragraph).isInstanceOf(PdfElement.class);
            assertThat(paragraph.getX()).isEqualTo(75f);
            assertThat(paragraph.getY()).isEqualTo(800f);
        }
    }
}
