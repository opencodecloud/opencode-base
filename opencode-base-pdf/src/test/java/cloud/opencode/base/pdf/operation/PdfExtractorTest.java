package cloud.opencode.base.pdf.operation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * PdfExtractor 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("PdfExtractor 测试")
class PdfExtractorTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("create 创建提取器")
        void testCreate() {
            PdfExtractor extractor = PdfExtractor.create();

            assertThat(extractor).isNotNull();
        }

        @Test
        @DisplayName("of(Path) 创建带源路径的提取器")
        void testOfPath() {
            Path path = Path.of("/document.pdf");
            PdfExtractor extractor = PdfExtractor.of(path);

            assertThat(extractor.getSourcePath()).isEqualTo(path);
        }
    }

    @Nested
    @DisplayName("源设置测试")
    class SourceSettingTests {

        @Test
        @DisplayName("source(Path) 设置源文件路径")
        void testSourcePath() {
            Path path = Path.of("/source.pdf");
            PdfExtractor extractor = PdfExtractor.create()
                .source(path);

            assertThat(extractor.getSourcePath()).isEqualTo(path);
            assertThat(extractor.getSourceDocument()).isNull();
        }

        @Test
        @DisplayName("source(Path) null 抛出异常")
        void testSourcePathNull() {
            assertThatThrownBy(() -> PdfExtractor.create().source((Path) null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("文本提取测试")
    class TextExtractionTests {

        @Test
        @DisplayName("extractText 无源抛出异常")
        void testExtractTextNoSource() {
            assertThatThrownBy(() -> PdfExtractor.create().extractText())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Source must be set before extraction");
        }

        @Test
        @DisplayName("extractText(int...) null 抛出异常")
        void testExtractTextPagesNull() {
            assertThatThrownBy(() -> PdfExtractor.of(Path.of("/doc.pdf"))
                .extractText((int[]) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("extractText(int...) 负页码抛出异常")
        void testExtractTextPagesNegative() {
            assertThatThrownBy(() -> PdfExtractor.of(Path.of("/doc.pdf"))
                .extractText(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Page numbers must be positive");
        }

        @Test
        @DisplayName("extractText(int...) 零页码抛出异常")
        void testExtractTextPagesZero() {
            assertThatThrownBy(() -> PdfExtractor.of(Path.of("/doc.pdf"))
                .extractText(0))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("extractTextRange 负起始页抛出异常")
        void testExtractTextRangeNegativeStart() {
            assertThatThrownBy(() -> PdfExtractor.of(Path.of("/doc.pdf"))
                .extractTextRange(-1, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Page numbers must be positive");
        }

        @Test
        @DisplayName("extractTextRange 负结束页抛出异常")
        void testExtractTextRangeNegativeEnd() {
            assertThatThrownBy(() -> PdfExtractor.of(Path.of("/doc.pdf"))
                .extractTextRange(1, -1))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("extractTextRange 起始页大于结束页抛出异常")
        void testExtractTextRangeInvalidOrder() {
            assertThatThrownBy(() -> PdfExtractor.of(Path.of("/doc.pdf"))
                .extractTextRange(10, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Start page must not exceed end page");
        }

        @Test
        @DisplayName("extractTextRange 无源抛出异常")
        void testExtractTextRangeNoSource() {
            assertThatThrownBy(() -> PdfExtractor.create().extractTextRange(1, 5))
                .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("图像提取测试")
    class ImageExtractionTests {

        @Test
        @DisplayName("extractImages 无源抛出异常")
        void testExtractImagesNoSource() {
            assertThatThrownBy(() -> PdfExtractor.create().extractImages())
                .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("extractImages(int...) null 抛出异常")
        void testExtractImagesPagesNull() {
            assertThatThrownBy(() -> PdfExtractor.of(Path.of("/doc.pdf"))
                .extractImages((int[]) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("extractImages(int...) 负页码抛出异常")
        void testExtractImagesPagesNegative() {
            assertThatThrownBy(() -> PdfExtractor.of(Path.of("/doc.pdf"))
                .extractImages(-1))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("extractImageBytes 无源抛出异常")
        void testExtractImageBytesNoSource() {
            assertThatThrownBy(() -> PdfExtractor.create().extractImageBytes())
                .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("saveImages null directory 抛出异常")
        void testSaveImagesNullDirectory() {
            assertThatThrownBy(() -> PdfExtractor.of(Path.of("/doc.pdf"))
                .saveImages(null, "image_"))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("saveImages null namePrefix 抛出异常")
        void testSaveImagesNullPrefix() {
            assertThatThrownBy(() -> PdfExtractor.of(Path.of("/doc.pdf"))
                .saveImages(Path.of("/output"), null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("saveImages 无源抛出异常")
        void testSaveImagesNoSource() {
            assertThatThrownBy(() -> PdfExtractor.create()
                .saveImages(Path.of("/output"), "img_"))
                .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("ExtractedImage Record 测试")
    class ExtractedImageTests {

        @Test
        @DisplayName("创建 ExtractedImage")
        void testCreateExtractedImage() {
            byte[] data = new byte[]{1, 2, 3, 4};
            PdfExtractor.ExtractedImage image = new PdfExtractor.ExtractedImage(
                data, "PNG", 100, 50, 1);

            assertThat(image.data()).isEqualTo(data);
            assertThat(image.format()).isEqualTo("PNG");
            assertThat(image.width()).isEqualTo(100);
            assertThat(image.height()).isEqualTo(50);
            assertThat(image.pageNumber()).isEqualTo(1);
        }

        @Test
        @DisplayName("ExtractedImage null data 抛出异常")
        void testExtractedImageNullData() {
            assertThatThrownBy(() -> new PdfExtractor.ExtractedImage(null, "PNG", 100, 50, 1))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("data cannot be null");
        }

        @Test
        @DisplayName("data 返回副本")
        void testExtractedImageDataCopy() {
            byte[] original = new byte[]{1, 2, 3};
            PdfExtractor.ExtractedImage image = new PdfExtractor.ExtractedImage(
                original, "PNG", 100, 50, 1);

            byte[] retrieved = image.data();
            retrieved[0] = 99;

            assertThat(image.data()[0]).isEqualTo((byte) 1);
        }
    }

    @Nested
    @DisplayName("访问方法测试")
    class AccessorTests {

        @Test
        @DisplayName("getSourcePath 返回设置的路径")
        void testGetSourcePath() {
            Path path = Path.of("/test.pdf");
            PdfExtractor extractor = PdfExtractor.of(path);

            assertThat(extractor.getSourcePath()).isEqualTo(path);
        }

        @Test
        @DisplayName("getSourceDocument 初始为 null")
        void testGetSourceDocumentNull() {
            PdfExtractor extractor = PdfExtractor.create();

            assertThat(extractor.getSourceDocument()).isNull();
        }
    }

    @Nested
    @DisplayName("链式调用测试")
    class FluentApiTests {

        @Test
        @DisplayName("source 返回相同实例")
        void testSourceReturnsSameInstance() {
            PdfExtractor extractor = PdfExtractor.create();
            PdfExtractor result = extractor.source(Path.of("/doc.pdf"));

            assertThat(result).isSameAs(extractor);
        }
    }
}
