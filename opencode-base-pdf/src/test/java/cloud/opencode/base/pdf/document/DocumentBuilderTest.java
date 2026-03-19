package cloud.opencode.base.pdf.document;

import cloud.opencode.base.pdf.font.PdfFont;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * DocumentBuilder 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("DocumentBuilder 测试")
class DocumentBuilderTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("create 创建构建器")
        void testCreate() {
            DocumentBuilder builder = DocumentBuilder.create();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("create(PageSize) 创建指定页面大小的构建器")
        void testCreateWithPageSize() {
            DocumentBuilder builder = DocumentBuilder.create(PageSize.LETTER);

            assertThat(builder.getPageSize()).isEqualTo(PageSize.LETTER);
        }

        @Test
        @DisplayName("create(PageSize) null 抛出异常")
        void testCreateWithNullPageSize() {
            assertThatThrownBy(() -> DocumentBuilder.create(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("元数据设置测试")
    class MetadataSettingTests {

        @Test
        @DisplayName("title 设置标题")
        void testTitle() {
            DocumentBuilder builder = DocumentBuilder.create()
                .title("Test Document");

            assertThat(builder.getTitle()).isEqualTo("Test Document");
        }

        @Test
        @DisplayName("author 设置作者")
        void testAuthor() {
            DocumentBuilder builder = DocumentBuilder.create()
                .author("John Doe");

            assertThat(builder.getAuthor()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("subject 设置主题")
        void testSubject() {
            DocumentBuilder builder = DocumentBuilder.create()
                .subject("Test Subject");

            assertThat(builder.getSubject()).isEqualTo("Test Subject");
        }

        @Test
        @DisplayName("keywords 设置关键词")
        void testKeywords() {
            DocumentBuilder builder = DocumentBuilder.create()
                .keywords("pdf", "test", "java");

            assertThat(builder.getKeywords()).containsExactly("pdf", "test", "java");
        }

        @Test
        @DisplayName("keywords null 清空关键词")
        void testKeywordsNull() {
            DocumentBuilder builder = DocumentBuilder.create()
                .keywords("a", "b")
                .keywords((String[]) null);

            assertThat(builder.getKeywords()).isEmpty();
        }

        @Test
        @DisplayName("creator 设置创建者")
        void testCreator() {
            DocumentBuilder builder = DocumentBuilder.create()
                .creator("MyApp");

            assertThat(builder.getCreator()).isEqualTo("MyApp");
        }
    }

    @Nested
    @DisplayName("页面设置测试")
    class PageSettingTests {

        @Test
        @DisplayName("pageSize 设置页面大小")
        void testPageSize() {
            DocumentBuilder builder = DocumentBuilder.create()
                .pageSize(PageSize.A5);

            assertThat(builder.getPageSize()).isEqualTo(PageSize.A5);
        }

        @Test
        @DisplayName("pageSize null 抛出异常")
        void testPageSizeNull() {
            assertThatThrownBy(() -> DocumentBuilder.create().pageSize(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("orientation 设置页面方向")
        void testOrientation() {
            DocumentBuilder builder = DocumentBuilder.create()
                .orientation(Orientation.LANDSCAPE);

            assertThat(builder.getOrientation()).isEqualTo(Orientation.LANDSCAPE);
        }

        @Test
        @DisplayName("orientation null 抛出异常")
        void testOrientationNull() {
            assertThatThrownBy(() -> DocumentBuilder.create().orientation(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("margins 设置四个边距")
        void testMarginsAll() {
            DocumentBuilder builder = DocumentBuilder.create()
                .margins(10, 20, 30, 40);

            assertThat(builder.getMarginTop()).isEqualTo(10);
            assertThat(builder.getMarginRight()).isEqualTo(20);
            assertThat(builder.getMarginBottom()).isEqualTo(30);
            assertThat(builder.getMarginLeft()).isEqualTo(40);
        }

        @Test
        @DisplayName("margins 设置统一边距")
        void testMarginsUniform() {
            DocumentBuilder builder = DocumentBuilder.create()
                .margins(50);

            assertThat(builder.getMarginTop()).isEqualTo(50);
            assertThat(builder.getMarginRight()).isEqualTo(50);
            assertThat(builder.getMarginBottom()).isEqualTo(50);
            assertThat(builder.getMarginLeft()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("字体设置测试")
    class FontSettingTests {

        @Test
        @DisplayName("defaultFont 设置默认字体")
        void testDefaultFont() {
            PdfFont font = PdfFont.helveticaBold();
            DocumentBuilder builder = DocumentBuilder.create()
                .defaultFont(font);

            assertThat(builder.getDefaultFont()).isEqualTo(font);
        }

        @Test
        @DisplayName("embedFont 嵌入字体")
        void testEmbedFont() {
            DocumentBuilder builder = DocumentBuilder.create()
                .embedFont(Path.of("/fonts/custom.ttf"), "CustomFont");

            assertThat(builder.getEmbeddedFonts()).hasSize(1);
            assertThat(builder.getEmbeddedFonts().getFirst().name()).isEqualTo("CustomFont");
            assertThat(builder.getEmbeddedFonts().getFirst().path()).isEqualTo(Path.of("/fonts/custom.ttf"));
        }

        @Test
        @DisplayName("embedFont null path 抛出异常")
        void testEmbedFontNullPath() {
            assertThatThrownBy(() -> DocumentBuilder.create().embedFont(null, "Font"))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("embedFont null name 抛出异常")
        void testEmbedFontNullName() {
            assertThatThrownBy(() -> DocumentBuilder.create().embedFont(Path.of("/font.ttf"), null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("页面添加测试")
    class PageAdditionTests {

        @Test
        @DisplayName("addPage 添加默认页面")
        void testAddPage() {
            DocumentBuilder builder = DocumentBuilder.create();
            PageBuilder pageBuilder = builder.addPage();

            assertThat(pageBuilder).isNotNull();
            assertThat(builder.getPages()).hasSize(1);
        }

        @Test
        @DisplayName("addPage(PageSize) 添加指定大小的页面")
        void testAddPageWithSize() {
            DocumentBuilder builder = DocumentBuilder.create();
            PageBuilder pageBuilder = builder.addPage(PageSize.LEGAL);

            assertThat(pageBuilder.getPageSize()).isEqualTo(PageSize.LEGAL);
        }

        @Test
        @DisplayName("addPage(PageSize) null 抛出异常")
        void testAddPageWithSizeNull() {
            assertThatThrownBy(() -> DocumentBuilder.create().addPage(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("addPage(PageSize, Orientation) 添加指定大小和方向的页面")
        void testAddPageWithSizeAndOrientation() {
            DocumentBuilder builder = DocumentBuilder.create();
            PageBuilder pageBuilder = builder.addPage(PageSize.A3, Orientation.LANDSCAPE);

            assertThat(pageBuilder.getPageSize()).isEqualTo(PageSize.A3);
            assertThat(pageBuilder.getOrientation()).isEqualTo(Orientation.LANDSCAPE);
        }

        @Test
        @DisplayName("addPage(PageSize, Orientation) null pageSize 抛出异常")
        void testAddPageWithNullPageSize() {
            assertThatThrownBy(() -> DocumentBuilder.create().addPage(null, Orientation.PORTRAIT))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("addPage(PageSize, Orientation) null orientation 抛出异常")
        void testAddPageWithNullOrientation() {
            assertThatThrownBy(() -> DocumentBuilder.create().addPage(PageSize.A4, null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("安全设置测试")
    class SecuritySettingTests {

        @Test
        @DisplayName("encrypt 设置加密")
        void testEncrypt() {
            DocumentBuilder builder = DocumentBuilder.create()
                .encrypt("user123", "owner456");

            assertThat(builder.isEncrypted()).isTrue();
        }

        @Test
        @DisplayName("permissions 设置权限")
        void testPermissions() {
            DocumentBuilder builder = DocumentBuilder.create()
                .permissions(true, false, true, false);

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("未加密时 isEncrypted 返回 false")
        void testNotEncrypted() {
            DocumentBuilder builder = DocumentBuilder.create();

            assertThat(builder.isEncrypted()).isFalse();
        }
    }

    @Nested
    @DisplayName("构建方法测试")
    class BuildMethodTests {

        @Test
        @DisplayName("save(Path) null 抛出异常")
        void testSavePathNull() {
            assertThatThrownBy(() -> DocumentBuilder.create().save((Path) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("save(OutputStream) null 抛出异常")
        void testSaveStreamNull() {
            assertThatThrownBy(() -> DocumentBuilder.create().save((java.io.OutputStream) null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("访问方法测试")
    class AccessorTests {

        @Test
        @DisplayName("getKeywords 返回不可变列表")
        void testGetKeywordsImmutable() {
            DocumentBuilder builder = DocumentBuilder.create()
                .keywords("a", "b");

            assertThatThrownBy(() -> builder.getKeywords().add("c"))
                .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("getEmbeddedFonts 返回不可变列表")
        void testGetEmbeddedFontsImmutable() {
            DocumentBuilder builder = DocumentBuilder.create()
                .embedFont(Path.of("/font.ttf"), "Font");

            assertThatThrownBy(() -> builder.getEmbeddedFonts().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("getPages 返回不可变列表")
        void testGetPagesImmutable() {
            DocumentBuilder builder = DocumentBuilder.create();
            builder.addPage();

            assertThatThrownBy(() -> builder.getPages().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("默认页面大小为 A4")
        void testDefaultPageSize() {
            DocumentBuilder builder = DocumentBuilder.create();

            assertThat(builder.getPageSize()).isEqualTo(PageSize.A4);
        }

        @Test
        @DisplayName("默认页面方向为 PORTRAIT")
        void testDefaultOrientation() {
            DocumentBuilder builder = DocumentBuilder.create();

            assertThat(builder.getOrientation()).isEqualTo(Orientation.PORTRAIT);
        }

        @Test
        @DisplayName("默认边距为 72")
        void testDefaultMargins() {
            DocumentBuilder builder = DocumentBuilder.create();

            assertThat(builder.getMarginTop()).isEqualTo(72);
            assertThat(builder.getMarginRight()).isEqualTo(72);
            assertThat(builder.getMarginBottom()).isEqualTo(72);
            assertThat(builder.getMarginLeft()).isEqualTo(72);
        }

        @Test
        @DisplayName("默认创建者为 OpenCode PDF")
        void testDefaultCreator() {
            DocumentBuilder builder = DocumentBuilder.create();

            assertThat(builder.getCreator()).isEqualTo("OpenCode PDF");
        }
    }

    @Nested
    @DisplayName("链式调用测试")
    class FluentApiTests {

        @Test
        @DisplayName("完整的链式调用")
        void testFullFluentApi() {
            DocumentBuilder builder = DocumentBuilder.create()
                .title("My Document")
                .author("Test Author")
                .subject("Test Subject")
                .keywords("key1", "key2")
                .creator("TestApp")
                .pageSize(PageSize.A4)
                .orientation(Orientation.PORTRAIT)
                .margins(50)
                .defaultFont(PdfFont.helvetica())
                .encrypt("user", "owner")
                .permissions(true, true, true, true);

            assertThat(builder.getTitle()).isEqualTo("My Document");
            assertThat(builder.getAuthor()).isEqualTo("Test Author");
            assertThat(builder.getSubject()).isEqualTo("Test Subject");
            assertThat(builder.getKeywords()).containsExactly("key1", "key2");
            assertThat(builder.isEncrypted()).isTrue();
        }
    }

    @Nested
    @DisplayName("EmbeddedFontEntry Record 测试")
    class EmbeddedFontEntryTests {

        @Test
        @DisplayName("创建 EmbeddedFontEntry")
        void testCreateEmbeddedFontEntry() {
            Path path = Path.of("/fonts/test.ttf");
            DocumentBuilder.EmbeddedFontEntry entry = new DocumentBuilder.EmbeddedFontEntry(path, "TestFont");

            assertThat(entry.path()).isEqualTo(path);
            assertThat(entry.name()).isEqualTo("TestFont");
        }
    }
}
