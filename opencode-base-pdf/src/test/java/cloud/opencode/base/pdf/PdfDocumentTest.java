package cloud.opencode.base.pdf;

import cloud.opencode.base.pdf.document.PageSize;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * PdfDocument 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("PdfDocument 测试")
class PdfDocumentTest {

    @Nested
    @DisplayName("接口声明测试")
    class InterfaceDeclarationTests {

        @Test
        @DisplayName("PdfDocument 继承 AutoCloseable")
        void testExtendsAutoCloseable() {
            assertThat(AutoCloseable.class.isAssignableFrom(PdfDocument.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("接口方法声明测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("声明 getPageCount 方法")
        void testHasGetPageCountMethod() throws NoSuchMethodException {
            assertThat(PdfDocument.class.getMethod("getPageCount")).isNotNull();
        }

        @Test
        @DisplayName("声明 getPage 方法")
        void testHasGetPageMethod() throws NoSuchMethodException {
            assertThat(PdfDocument.class.getMethod("getPage", int.class)).isNotNull();
        }

        @Test
        @DisplayName("声明 getPages 方法")
        void testHasGetPagesMethod() throws NoSuchMethodException {
            assertThat(PdfDocument.class.getMethod("getPages")).isNotNull();
        }

        @Test
        @DisplayName("声明 addPage 方法")
        void testHasAddPageMethod() throws NoSuchMethodException {
            assertThat(PdfDocument.class.getMethod("addPage")).isNotNull();
        }

        @Test
        @DisplayName("声明 addPage(PageSize) 方法")
        void testHasAddPageWithSizeMethod() throws NoSuchMethodException {
            assertThat(PdfDocument.class.getMethod("addPage", PageSize.class)).isNotNull();
        }

        @Test
        @DisplayName("声明 insertPage 方法")
        void testHasInsertPageMethod() throws NoSuchMethodException {
            assertThat(PdfDocument.class.getMethod("insertPage", int.class, PdfPage.class)).isNotNull();
        }

        @Test
        @DisplayName("声明 removePage 方法")
        void testHasRemovePageMethod() throws NoSuchMethodException {
            assertThat(PdfDocument.class.getMethod("removePage", int.class)).isNotNull();
        }

        @Test
        @DisplayName("声明 getMetadata 方法")
        void testHasGetMetadataMethod() throws NoSuchMethodException {
            assertThat(PdfDocument.class.getMethod("getMetadata")).isNotNull();
        }

        @Test
        @DisplayName("声明 setMetadata 方法")
        void testHasSetMetadataMethod() throws NoSuchMethodException {
            assertThat(PdfDocument.class.getMethod("setMetadata", cloud.opencode.base.pdf.document.Metadata.class)).isNotNull();
        }

        @Test
        @DisplayName("声明 hasForm 方法")
        void testHasHasFormMethod() throws NoSuchMethodException {
            assertThat(PdfDocument.class.getMethod("hasForm")).isNotNull();
        }

        @Test
        @DisplayName("声明 getForm 方法")
        void testHasGetFormMethod() throws NoSuchMethodException {
            assertThat(PdfDocument.class.getMethod("getForm")).isNotNull();
        }

        @Test
        @DisplayName("声明 save(Path) 方法")
        void testHasSavePathMethod() throws NoSuchMethodException {
            assertThat(PdfDocument.class.getMethod("save", Path.class)).isNotNull();
        }

        @Test
        @DisplayName("声明 save(OutputStream) 方法")
        void testHasSaveStreamMethod() throws NoSuchMethodException {
            assertThat(PdfDocument.class.getMethod("save", OutputStream.class)).isNotNull();
        }

        @Test
        @DisplayName("声明 toBytes 方法")
        void testHasToBytesMethod() throws NoSuchMethodException {
            assertThat(PdfDocument.class.getMethod("toBytes")).isNotNull();
        }

        @Test
        @DisplayName("声明 isEncrypted 方法")
        void testHasIsEncryptedMethod() throws NoSuchMethodException {
            assertThat(PdfDocument.class.getMethod("isEncrypted")).isNotNull();
        }

        @Test
        @DisplayName("声明 setPassword 方法")
        void testHasSetPasswordMethod() throws NoSuchMethodException {
            assertThat(PdfDocument.class.getMethod("setPassword", String.class, String.class)).isNotNull();
        }

        @Test
        @DisplayName("声明 close 方法")
        void testHasCloseMethod() throws NoSuchMethodException {
            assertThat(PdfDocument.class.getMethod("close")).isNotNull();
        }
    }
}
