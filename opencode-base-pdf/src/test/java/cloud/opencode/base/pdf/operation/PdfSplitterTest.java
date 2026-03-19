package cloud.opencode.base.pdf.operation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * PdfSplitter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("PdfSplitter 测试")
class PdfSplitterTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("create 创建拆分器")
        void testCreate() {
            PdfSplitter splitter = PdfSplitter.create();

            assertThat(splitter).isNotNull();
        }

        @Test
        @DisplayName("of(Path) 创建带源路径的拆分器")
        void testOfPath() {
            Path path = Path.of("/document.pdf");
            PdfSplitter splitter = PdfSplitter.of(path);

            assertThat(splitter.getSourcePath()).isEqualTo(path);
        }
    }

    @Nested
    @DisplayName("源设置测试")
    class SourceSettingTests {

        @Test
        @DisplayName("source(Path) 设置源文件路径")
        void testSourcePath() {
            Path path = Path.of("/source.pdf");
            PdfSplitter splitter = PdfSplitter.create()
                .source(path);

            assertThat(splitter.getSourcePath()).isEqualTo(path);
            assertThat(splitter.getSourceDocument()).isNull();
        }

        @Test
        @DisplayName("source(Path) null 抛出异常")
        void testSourcePathNull() {
            assertThatThrownBy(() -> PdfSplitter.create().source((Path) null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("拆分方法测试")
    class SplitMethodTests {

        @Test
        @DisplayName("splitToPages 无源抛出异常")
        void testSplitToPagesNoSource() {
            assertThatThrownBy(() -> PdfSplitter.create().splitToPages())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Source must be set before splitting");
        }

        @Test
        @DisplayName("splitByRanges null 抛出异常")
        void testSplitByRangesNull() {
            assertThatThrownBy(() -> PdfSplitter.of(Path.of("/doc.pdf"))
                .splitByRanges((String[]) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("splitByRanges 无源抛出异常")
        void testSplitByRangesNoSource() {
            assertThatThrownBy(() -> PdfSplitter.create().splitByRanges("1-5"))
                .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("splitByPageCount 小于1抛出异常")
        void testSplitByPageCountInvalid() {
            assertThatThrownBy(() -> PdfSplitter.of(Path.of("/doc.pdf"))
                .splitByPageCount(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pages per document must be positive");
        }

        @Test
        @DisplayName("splitByPageCount 无源抛出异常")
        void testSplitByPageCountNoSource() {
            assertThatThrownBy(() -> PdfSplitter.create().splitByPageCount(5))
                .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("splitBySize 小于1KB抛出异常")
        void testSplitBySizeInvalid() {
            assertThatThrownBy(() -> PdfSplitter.of(Path.of("/doc.pdf"))
                .splitBySize(500))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Max size must be at least 1KB");
        }

        @Test
        @DisplayName("splitBySize 无源抛出异常")
        void testSplitBySizeNoSource() {
            assertThatThrownBy(() -> PdfSplitter.create().splitBySize(1024 * 1024))
                .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("splitByBookmarks 小于1抛出异常")
        void testSplitByBookmarksInvalid() {
            assertThatThrownBy(() -> PdfSplitter.of(Path.of("/doc.pdf"))
                .splitByBookmarks(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bookmark level must be positive");
        }

        @Test
        @DisplayName("splitByBookmarks 无源抛出异常")
        void testSplitByBookmarksNoSource() {
            assertThatThrownBy(() -> PdfSplitter.create().splitByBookmarks(1))
                .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("extractPages 测试")
    class ExtractPagesTests {

        @Test
        @DisplayName("extractPages null 抛出异常")
        void testExtractPagesNull() {
            assertThatThrownBy(() -> PdfSplitter.of(Path.of("/doc.pdf"))
                .extractPages((int[]) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("extractPages 负页码抛出异常")
        void testExtractPagesNegative() {
            assertThatThrownBy(() -> PdfSplitter.of(Path.of("/doc.pdf"))
                .extractPages(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Page numbers must be positive");
        }

        @Test
        @DisplayName("extractPages 零页码抛出异常")
        void testExtractPagesZero() {
            assertThatThrownBy(() -> PdfSplitter.of(Path.of("/doc.pdf"))
                .extractPages(0))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("extractPages 无源抛出异常")
        void testExtractPagesNoSource() {
            assertThatThrownBy(() -> PdfSplitter.create().extractPages(1, 2, 3))
                .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("保存选项测试")
    class SaveOptionsTests {

        @Test
        @DisplayName("splitAndSave null directory 抛出异常")
        void testSplitAndSaveNullDirectory() {
            assertThatThrownBy(() -> PdfSplitter.of(Path.of("/doc.pdf"))
                .splitAndSave(null, "part_%d.pdf"))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("splitAndSave null nameFormat 抛出异常")
        void testSplitAndSaveNullNameFormat() {
            assertThatThrownBy(() -> PdfSplitter.of(Path.of("/doc.pdf"))
                .splitAndSave(Path.of("/output"), (String) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("splitAndSave 无源抛出异常")
        void testSplitAndSaveNoSource() {
            assertThatThrownBy(() -> PdfSplitter.create()
                .splitAndSave(Path.of("/output"), "part_%d.pdf"))
                .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("splitAndSave with function null directory 抛出异常")
        void testSplitAndSaveFunctionNullDirectory() {
            assertThatThrownBy(() -> PdfSplitter.of(Path.of("/doc.pdf"))
                .splitAndSave(null, i -> "part_" + i + ".pdf"))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("splitAndSave with function null nameFunction 抛出异常")
        void testSplitAndSaveFunctionNullFunction() {
            assertThatThrownBy(() -> PdfSplitter.of(Path.of("/doc.pdf"))
                .splitAndSave(Path.of("/output"), (java.util.function.Function<Integer, String>) null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("访问方法测试")
    class AccessorTests {

        @Test
        @DisplayName("getSourcePath 返回设置的路径")
        void testGetSourcePath() {
            Path path = Path.of("/test.pdf");
            PdfSplitter splitter = PdfSplitter.of(path);

            assertThat(splitter.getSourcePath()).isEqualTo(path);
        }

        @Test
        @DisplayName("getSourceDocument 初始为 null")
        void testGetSourceDocumentNull() {
            PdfSplitter splitter = PdfSplitter.create();

            assertThat(splitter.getSourceDocument()).isNull();
        }
    }

    @Nested
    @DisplayName("链式调用测试")
    class FluentApiTests {

        @Test
        @DisplayName("source 返回相同实例")
        void testSourceReturnsSameInstance() {
            PdfSplitter splitter = PdfSplitter.create();
            PdfSplitter result = splitter.source(Path.of("/doc.pdf"));

            assertThat(result).isSameAs(splitter);
        }
    }
}
