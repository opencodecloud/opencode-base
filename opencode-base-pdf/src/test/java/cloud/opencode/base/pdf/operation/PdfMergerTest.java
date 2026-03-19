package cloud.opencode.base.pdf.operation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * PdfMerger 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("PdfMerger 测试")
class PdfMergerTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("create 创建合并器")
        void testCreate() {
            PdfMerger merger = PdfMerger.create();

            assertThat(merger).isNotNull();
            assertThat(merger.getSources()).isEmpty();
        }
    }

    @Nested
    @DisplayName("添加文档测试")
    class AddDocumentTests {

        @Test
        @DisplayName("add(Path) 添加文件路径")
        void testAddPath() {
            PdfMerger merger = PdfMerger.create()
                .add(Path.of("/doc1.pdf"));

            assertThat(merger.getSources()).hasSize(1);
            assertThat(merger.getSources().getFirst().path()).isEqualTo(Path.of("/doc1.pdf"));
        }

        @Test
        @DisplayName("add(Path) null 抛出异常")
        void testAddPathNull() {
            assertThatThrownBy(() -> PdfMerger.create().add((Path) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("add(InputStream) 添加输入流")
        void testAddInputStream() {
            InputStream stream = new ByteArrayInputStream(new byte[0]);
            PdfMerger merger = PdfMerger.create()
                .add(stream);

            assertThat(merger.getSources()).hasSize(1);
            assertThat(merger.getSources().getFirst().stream()).isEqualTo(stream);
        }

        @Test
        @DisplayName("add(InputStream) null 抛出异常")
        void testAddInputStreamNull() {
            assertThatThrownBy(() -> PdfMerger.create().add((InputStream) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("addPages 添加指定页面")
        void testAddPages() {
            PdfMerger merger = PdfMerger.create()
                .addPages(Path.of("/doc.pdf"), "1-3", "5", "7-10");

            assertThat(merger.getSources()).hasSize(1);
            PdfMerger.MergeSource source = merger.getSources().getFirst();
            assertThat(source.path()).isEqualTo(Path.of("/doc.pdf"));
            assertThat(source.hasPageRanges()).isTrue();
            assertThat(source.pageRanges()).containsExactly("1-3", "5", "7-10");
        }

        @Test
        @DisplayName("addPages null path 抛出异常")
        void testAddPagesNullPath() {
            assertThatThrownBy(() -> PdfMerger.create().addPages(null, "1-3"))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("addPages null pageRanges 抛出异常")
        void testAddPagesNullRanges() {
            assertThatThrownBy(() -> PdfMerger.create().addPages(Path.of("/doc.pdf"), (String[]) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("addAll 添加多个文件")
        void testAddAll() {
            List<Path> paths = List.of(
                Path.of("/doc1.pdf"),
                Path.of("/doc2.pdf"),
                Path.of("/doc3.pdf")
            );
            PdfMerger merger = PdfMerger.create()
                .addAll(paths);

            assertThat(merger.getSources()).hasSize(3);
        }

        @Test
        @DisplayName("addAll null 抛出异常")
        void testAddAllNull() {
            assertThatThrownBy(() -> PdfMerger.create().addAll(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("合并选项测试")
    class MergeOptionsTests {

        @Test
        @DisplayName("keepBookmarks 设置保留书签")
        void testKeepBookmarks() {
            PdfMerger merger = PdfMerger.create()
                .keepBookmarks(false);

            assertThat(merger.isKeepBookmarks()).isFalse();
        }

        @Test
        @DisplayName("keepAnnotations 设置保留注释")
        void testKeepAnnotations() {
            PdfMerger merger = PdfMerger.create()
                .keepAnnotations(false);

            assertThat(merger.isKeepAnnotations()).isFalse();
        }

        @Test
        @DisplayName("addOutlines 设置添加大纲")
        void testAddOutlines() {
            PdfMerger merger = PdfMerger.create()
                .addOutlines(true);

            assertThat(merger.isAddOutlines()).isTrue();
        }
    }

    @Nested
    @DisplayName("合并执行测试")
    class MergeExecutionTests {

        @Test
        @DisplayName("merge 无源文件抛出异常")
        void testMergeEmpty() {
            assertThatThrownBy(() -> PdfMerger.create().merge())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No sources added for merging");
        }

        @Test
        @DisplayName("mergeTo(Path) 无源文件抛出异常")
        void testMergeToPathEmpty() {
            assertThatThrownBy(() -> PdfMerger.create().mergeTo(Path.of("/out.pdf")))
                .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("mergeTo(Path) null 抛出异常")
        void testMergeToPathNull() {
            assertThatThrownBy(() -> PdfMerger.create()
                .add(Path.of("/doc.pdf"))
                .mergeTo((Path) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("mergeTo(OutputStream) 无源文件抛出异常")
        void testMergeToStreamEmpty() {
            assertThatThrownBy(() -> PdfMerger.create()
                .mergeTo(new ByteArrayOutputStream()))
                .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("mergeTo(OutputStream) null 抛出异常")
        void testMergeToStreamNull() {
            assertThatThrownBy(() -> PdfMerger.create()
                .add(Path.of("/doc.pdf"))
                .mergeTo((java.io.OutputStream) null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("MergeSource Record 测试")
    class MergeSourceTests {

        @Test
        @DisplayName("hasPageRanges 返回 true 当有页面范围")
        void testHasPageRangesTrue() {
            PdfMerger.MergeSource source = new PdfMerger.MergeSource(
                Path.of("/doc.pdf"), null, null, List.of("1-3"));

            assertThat(source.hasPageRanges()).isTrue();
        }

        @Test
        @DisplayName("hasPageRanges 返回 false 当无页面范围")
        void testHasPageRangesFalseNull() {
            PdfMerger.MergeSource source = new PdfMerger.MergeSource(
                Path.of("/doc.pdf"), null, null, null);

            assertThat(source.hasPageRanges()).isFalse();
        }

        @Test
        @DisplayName("hasPageRanges 返回 false 当页面范围为空")
        void testHasPageRangesFalseEmpty() {
            PdfMerger.MergeSource source = new PdfMerger.MergeSource(
                Path.of("/doc.pdf"), null, null, List.of());

            assertThat(source.hasPageRanges()).isFalse();
        }
    }

    @Nested
    @DisplayName("链式调用测试")
    class FluentApiTests {

        @Test
        @DisplayName("完整的链式调用")
        void testFullFluentApi() {
            PdfMerger merger = PdfMerger.create()
                .add(Path.of("/doc1.pdf"))
                .add(Path.of("/doc2.pdf"))
                .addPages(Path.of("/doc3.pdf"), "1-5")
                .keepBookmarks(true)
                .keepAnnotations(true)
                .addOutlines(true);

            assertThat(merger.getSources()).hasSize(3);
            assertThat(merger.isKeepBookmarks()).isTrue();
            assertThat(merger.isKeepAnnotations()).isTrue();
            assertThat(merger.isAddOutlines()).isTrue();
        }
    }

    @Nested
    @DisplayName("默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("默认保留书签")
        void testDefaultKeepBookmarks() {
            PdfMerger merger = PdfMerger.create();

            assertThat(merger.isKeepBookmarks()).isTrue();
        }

        @Test
        @DisplayName("默认保留注释")
        void testDefaultKeepAnnotations() {
            PdfMerger merger = PdfMerger.create();

            assertThat(merger.isKeepAnnotations()).isTrue();
        }

        @Test
        @DisplayName("默认不添加大纲")
        void testDefaultAddOutlines() {
            PdfMerger merger = PdfMerger.create();

            assertThat(merger.isAddOutlines()).isFalse();
        }
    }

    @Nested
    @DisplayName("getSources 测试")
    class GetSourcesTests {

        @Test
        @DisplayName("getSources 返回不可变列表")
        void testGetSourcesImmutable() {
            PdfMerger merger = PdfMerger.create()
                .add(Path.of("/doc.pdf"));

            assertThatThrownBy(() -> merger.getSources().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
