package cloud.opencode.base.pdf;

import cloud.opencode.base.pdf.document.DocumentBuilder;
import cloud.opencode.base.pdf.document.PageSize;
import cloud.opencode.base.pdf.operation.PdfExtractor;
import cloud.opencode.base.pdf.operation.PdfMerger;
import cloud.opencode.base.pdf.operation.PdfSplitter;
import cloud.opencode.base.pdf.signature.PdfSigner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenPdf 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("OpenPdf 测试")
class OpenPdfTest {

    @Nested
    @DisplayName("文档创建测试")
    class DocumentCreationTests {

        @Test
        @DisplayName("create 返回 DocumentBuilder")
        void testCreate() {
            DocumentBuilder builder = OpenPdf.create();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("create(PageSize) 返回指定页面大小的 DocumentBuilder")
        void testCreateWithPageSize() {
            DocumentBuilder builder = OpenPdf.create(PageSize.LETTER);

            assertThat(builder).isNotNull();
            assertThat(builder.getPageSize()).isEqualTo(PageSize.LETTER);
        }

        @Test
        @DisplayName("create(PageSize) null 抛出异常")
        void testCreateWithNullPageSize() {
            assertThatThrownBy(() -> OpenPdf.create(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("文档读取测试")
    class DocumentReadingTests {

        @Test
        @DisplayName("open(Path) null 抛出异常")
        void testOpenPathNull() {
            assertThatThrownBy(() -> OpenPdf.open((Path) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("open(InputStream) null 抛出异常")
        void testOpenStreamNull() {
            assertThatThrownBy(() -> OpenPdf.open((InputStream) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("open(byte[]) null 抛出异常")
        void testOpenBytesNull() {
            assertThatThrownBy(() -> OpenPdf.open((byte[]) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("open(Path, String) null path 抛出异常")
        void testOpenWithPasswordNullPath() {
            assertThatThrownBy(() -> OpenPdf.open(null, "password"))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("open(Path, String) null password 抛出异常")
        void testOpenWithPasswordNullPassword() {
            assertThatThrownBy(() -> OpenPdf.open(Path.of("/doc.pdf"), null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("文档合并测试")
    class DocumentMergingTests {

        @Test
        @DisplayName("merger 返回 PdfMerger")
        void testMerger() {
            PdfMerger merger = OpenPdf.merger();

            assertThat(merger).isNotNull();
        }

        @Test
        @DisplayName("merge(List, Path) null sources 抛出异常")
        void testMergeNullSources() {
            assertThatThrownBy(() -> OpenPdf.merge(null, Path.of("/out.pdf")))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("merge(List, Path) null target 抛出异常")
        void testMergeNullTarget() {
            assertThatThrownBy(() -> OpenPdf.merge(List.of(Path.of("/doc.pdf")), null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("merge(List, Path) 空列表抛出异常")
        void testMergeEmptySources() {
            assertThatThrownBy(() -> OpenPdf.merge(List.of(), Path.of("/out.pdf")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sources cannot be empty");
        }

        @Test
        @DisplayName("merge(List<PdfDocument>) null 抛出异常")
        void testMergeDocumentsNull() {
            assertThatThrownBy(() -> OpenPdf.merge((List<PdfDocument>) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("merge(List<PdfDocument>) 空列表抛出异常")
        void testMergeDocumentsEmpty() {
            assertThatThrownBy(() -> OpenPdf.merge(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("documents cannot be empty");
        }
    }

    @Nested
    @DisplayName("文档拆分测试")
    class DocumentSplittingTests {

        @Test
        @DisplayName("splitter 返回 PdfSplitter")
        void testSplitter() {
            PdfSplitter splitter = OpenPdf.splitter();

            assertThat(splitter).isNotNull();
        }

        @Test
        @DisplayName("split(Path, String...) null source 抛出异常")
        void testSplitNullSource() {
            assertThatThrownBy(() -> OpenPdf.split(null, "1-5"))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("split(Path, String...) null ranges 抛出异常")
        void testSplitNullRanges() {
            assertThatThrownBy(() -> OpenPdf.split(Path.of("/doc.pdf"), (String[]) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("splitToPages(Path) null 抛出异常")
        void testSplitToPagesNull() {
            assertThatThrownBy(() -> OpenPdf.splitToPages(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("表单操作测试")
    class FormOperationsTests {

        @Test
        @DisplayName("fillForm null source 抛出异常")
        void testFillFormNullSource() {
            assertThatThrownBy(() -> OpenPdf.fillForm(null, Map.of("field", "value")))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("fillForm null fields 抛出异常")
        void testFillFormNullFields() {
            assertThatThrownBy(() -> OpenPdf.fillForm(Path.of("/form.pdf"), null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("fillAndFlatten null source 抛出异常")
        void testFillAndFlattenNullSource() {
            assertThatThrownBy(() -> OpenPdf.fillAndFlatten(null, Map.of("field", "value")))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("fillAndFlatten null fields 抛出异常")
        void testFillAndFlattenNullFields() {
            assertThatThrownBy(() -> OpenPdf.fillAndFlatten(Path.of("/form.pdf"), null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("extractFormFields null 抛出异常")
        void testExtractFormFieldsNull() {
            assertThatThrownBy(() -> OpenPdf.extractFormFields(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("数字签名测试")
    class DigitalSignatureTests {

        @Test
        @DisplayName("signer 返回 PdfSigner")
        void testSigner() {
            PdfSigner signer = OpenPdf.signer();

            assertThat(signer).isNotNull();
        }

        @Test
        @DisplayName("sign null source 抛出异常")
        void testSignNullSource() {
            assertThatThrownBy(() -> OpenPdf.sign(null, Path.of("/ks.p12"), "pass".toCharArray(), "cert"))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("sign null keyStore 抛出异常")
        void testSignNullKeyStore() {
            assertThatThrownBy(() -> OpenPdf.sign(Path.of("/doc.pdf"), null, "pass".toCharArray(), "cert"))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("sign null password 抛出异常")
        void testSignNullPassword() {
            assertThatThrownBy(() -> OpenPdf.sign(Path.of("/doc.pdf"), Path.of("/ks.p12"), null, "cert"))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("sign null alias 抛出异常")
        void testSignNullAlias() {
            assertThatThrownBy(() -> OpenPdf.sign(Path.of("/doc.pdf"), Path.of("/ks.p12"), "pass".toCharArray(), null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verifySignatures null 抛出异常")
        void testVerifySignaturesNull() {
            assertThatThrownBy(() -> OpenPdf.verifySignatures(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("内容提取测试")
    class ContentExtractionTests {

        @Test
        @DisplayName("extractor 返回 PdfExtractor")
        void testExtractor() {
            PdfExtractor extractor = OpenPdf.extractor();

            assertThat(extractor).isNotNull();
        }

        @Test
        @DisplayName("extractText(Path) null 抛出异常")
        void testExtractTextNull() {
            assertThatThrownBy(() -> OpenPdf.extractText(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("extractText(Path, int...) null source 抛出异常")
        void testExtractTextPagesNullSource() {
            assertThatThrownBy(() -> OpenPdf.extractText(null, 1, 2))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("extractText(Path, int...) null pageNumbers 抛出异常")
        void testExtractTextPagesNullPages() {
            assertThatThrownBy(() -> OpenPdf.extractText(Path.of("/doc.pdf"), (int[]) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("extractImages null 抛出异常")
        void testExtractImagesNull() {
            assertThatThrownBy(() -> OpenPdf.extractImages(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("实用方法测试")
    class UtilityMethodTests {

        @Test
        @DisplayName("getPageCount null 抛出异常")
        void testGetPageCountNull() {
            assertThatThrownBy(() -> OpenPdf.getPageCount(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("getMetadata null 抛出异常")
        void testGetMetadataNull() {
            assertThatThrownBy(() -> OpenPdf.getMetadata(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("isEncrypted null 抛出异常")
        void testIsEncryptedNull() {
            assertThatThrownBy(() -> OpenPdf.isEncrypted(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("hasForm null 抛出异常")
        void testHasFormNull() {
            assertThatThrownBy(() -> OpenPdf.hasForm(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("isSigned null 抛出异常")
        void testIsSignedNull() {
            assertThatThrownBy(() -> OpenPdf.isSigned(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("类设计测试")
    class ClassDesignTests {

        @Test
        @DisplayName("OpenPdf 是 final 类")
        void testIsFinalClass() {
            assertThat(java.lang.reflect.Modifier.isFinal(OpenPdf.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("OpenPdf 构造函数是私有的")
        void testPrivateConstructor() throws NoSuchMethodException {
            var constructor = OpenPdf.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }
}
