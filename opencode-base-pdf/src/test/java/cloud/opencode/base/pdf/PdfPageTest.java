package cloud.opencode.base.pdf;

import cloud.opencode.base.pdf.content.*;
import cloud.opencode.base.pdf.document.PageSize;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * PdfPage 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("PdfPage 测试")
class PdfPageTest {

    @Nested
    @DisplayName("接口方法声明测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("声明 getPageNumber 方法")
        void testHasGetPageNumberMethod() throws NoSuchMethodException {
            assertThat(PdfPage.class.getMethod("getPageNumber")).isNotNull();
        }

        @Test
        @DisplayName("声明 getPageSize 方法")
        void testHasGetPageSizeMethod() throws NoSuchMethodException {
            assertThat(PdfPage.class.getMethod("getPageSize")).isNotNull();
        }

        @Test
        @DisplayName("声明 getWidth 方法")
        void testHasGetWidthMethod() throws NoSuchMethodException {
            assertThat(PdfPage.class.getMethod("getWidth")).isNotNull();
        }

        @Test
        @DisplayName("声明 getHeight 方法")
        void testHasGetHeightMethod() throws NoSuchMethodException {
            assertThat(PdfPage.class.getMethod("getHeight")).isNotNull();
        }

        @Test
        @DisplayName("声明 getOrientation 方法")
        void testHasGetOrientationMethod() throws NoSuchMethodException {
            assertThat(PdfPage.class.getMethod("getOrientation")).isNotNull();
        }

        @Test
        @DisplayName("声明 setRotation 方法")
        void testHasSetRotationMethod() throws NoSuchMethodException {
            assertThat(PdfPage.class.getMethod("setRotation", int.class)).isNotNull();
        }

        @Test
        @DisplayName("声明 getRotation 方法")
        void testHasGetRotationMethod() throws NoSuchMethodException {
            assertThat(PdfPage.class.getMethod("getRotation")).isNotNull();
        }

        @Test
        @DisplayName("声明 addText(String, float, float) 方法")
        void testHasAddTextMethod() throws NoSuchMethodException {
            assertThat(PdfPage.class.getMethod("addText", String.class, float.class, float.class)).isNotNull();
        }

        @Test
        @DisplayName("声明 addText(PdfText) 方法")
        void testHasAddTextPdfTextMethod() throws NoSuchMethodException {
            assertThat(PdfPage.class.getMethod("addText", PdfText.class)).isNotNull();
        }

        @Test
        @DisplayName("声明 addParagraph 方法")
        void testHasAddParagraphMethod() throws NoSuchMethodException {
            assertThat(PdfPage.class.getMethod("addParagraph", PdfParagraph.class)).isNotNull();
        }

        @Test
        @DisplayName("声明 addImage 方法")
        void testHasAddImageMethod() throws NoSuchMethodException {
            assertThat(PdfPage.class.getMethod("addImage", PdfImage.class)).isNotNull();
        }

        @Test
        @DisplayName("声明 addTable 方法")
        void testHasAddTableMethod() throws NoSuchMethodException {
            assertThat(PdfPage.class.getMethod("addTable", PdfTable.class)).isNotNull();
        }

        @Test
        @DisplayName("声明 addLine 方法")
        void testHasAddLineMethod() throws NoSuchMethodException {
            assertThat(PdfPage.class.getMethod("addLine", PdfLine.class)).isNotNull();
        }

        @Test
        @DisplayName("声明 addRectangle 方法")
        void testHasAddRectangleMethod() throws NoSuchMethodException {
            assertThat(PdfPage.class.getMethod("addRectangle", PdfRectangle.class)).isNotNull();
        }

        @Test
        @DisplayName("声明 extractText 方法")
        void testHasExtractTextMethod() throws NoSuchMethodException {
            assertThat(PdfPage.class.getMethod("extractText")).isNotNull();
        }
    }

    @Nested
    @DisplayName("方法返回类型测试")
    class ReturnTypeTests {

        @Test
        @DisplayName("addText 返回 PdfPage")
        void testAddTextReturnType() throws NoSuchMethodException {
            assertThat(PdfPage.class.getMethod("addText", String.class, float.class, float.class)
                .getReturnType()).isEqualTo(PdfPage.class);
        }

        @Test
        @DisplayName("addParagraph 返回 PdfPage")
        void testAddParagraphReturnType() throws NoSuchMethodException {
            assertThat(PdfPage.class.getMethod("addParagraph", PdfParagraph.class)
                .getReturnType()).isEqualTo(PdfPage.class);
        }

        @Test
        @DisplayName("addImage 返回 PdfPage")
        void testAddImageReturnType() throws NoSuchMethodException {
            assertThat(PdfPage.class.getMethod("addImage", PdfImage.class)
                .getReturnType()).isEqualTo(PdfPage.class);
        }

        @Test
        @DisplayName("addTable 返回 PdfPage")
        void testAddTableReturnType() throws NoSuchMethodException {
            assertThat(PdfPage.class.getMethod("addTable", PdfTable.class)
                .getReturnType()).isEqualTo(PdfPage.class);
        }

        @Test
        @DisplayName("addLine 返回 PdfPage")
        void testAddLineReturnType() throws NoSuchMethodException {
            assertThat(PdfPage.class.getMethod("addLine", PdfLine.class)
                .getReturnType()).isEqualTo(PdfPage.class);
        }

        @Test
        @DisplayName("addRectangle 返回 PdfPage")
        void testAddRectangleReturnType() throws NoSuchMethodException {
            assertThat(PdfPage.class.getMethod("addRectangle", PdfRectangle.class)
                .getReturnType()).isEqualTo(PdfPage.class);
        }
    }
}
