package cloud.opencode.base.pdf.document;

import cloud.opencode.base.pdf.PdfDocument;
import cloud.opencode.base.pdf.font.PdfFont;

import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * PDF Document Builder
 * PDF 文档构建器
 *
 * <p>Fluent API for creating PDF documents.</p>
 * <p>用于创建 PDF 文档的流畅 API。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Set document metadata - 设置文档元数据</li>
 *   <li>Configure page settings - 配置页面设置</li>
 *   <li>Add pages with content - 添加带内容的页面</li>
 *   <li>Set encryption and permissions - 设置加密和权限</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create simple document
 * DocumentBuilder.create()
 *     .title("My Document")
 *     .author("John Doe")
 *     .addPage()
 *         .text("Hello, World!", 100, 700)
 *     .endPage()
 *     .save(Path.of("hello.pdf"));
 *
 * // Create with settings
 * DocumentBuilder.create(PageSize.A4)
 *     .title("Report")
 *     .orientation(Orientation.LANDSCAPE)
 *     .margins(72, 72, 72, 72)
 *     .addPage()
 *         .text("Content", 100, 500)
 *     .endPage()
 *     .save(Path.of("report.pdf"));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No — not designed for concurrent use - 线程安全: 否 — 非并发设计</li>
 *   <li>Null-safe: Yes — parameters are validated - 空值安全: 是 — 参数已验证</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per setter call; O(p) overall where p is the number of pages added - 时间复杂度: 每次 setter 调用 O(1)；整体为 O(p)，p 为添加的页面数</li>
 *   <li>Space complexity: O(p) - stores one PageBuilder reference per page - 空间复杂度: O(p) - 每页存储一个 PageBuilder 引用</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
public final class DocumentBuilder {

    // Metadata
    private String title;
    private String author;
    private String subject;
    private final List<String> keywords = new ArrayList<>();
    private String creator = "OpenCode PDF";

    // Page settings
    private PageSize pageSize = PageSize.A4;
    private Orientation orientation = Orientation.PORTRAIT;
    private float marginTop = 72;
    private float marginRight = 72;
    private float marginBottom = 72;
    private float marginLeft = 72;

    // Font settings
    private PdfFont defaultFont;
    private final List<EmbeddedFontEntry> embeddedFonts = new ArrayList<>();

    // Security settings
    private String userPassword;
    private String ownerPassword;
    private boolean allowPrinting = true;
    private boolean allowCopying = true;
    private boolean allowModifying = true;
    private boolean allowAnnotations = true;

    // Pages
    private final List<PageBuilder> pages = new ArrayList<>();

    private DocumentBuilder() {}

    private DocumentBuilder(PageSize pageSize) {
        this.pageSize = Objects.requireNonNull(pageSize, "pageSize cannot be null");
    }

    // ==================== 元数据设置 Metadata Settings ====================

    /**
     * Sets document title.
     * 设置文档标题。
     *
     * @param title document title | 文档标题
     * @return this builder | 当前构建器
     */
    public DocumentBuilder title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Sets document author.
     * 设置文档作者。
     *
     * @param author document author | 文档作者
     * @return this builder | 当前构建器
     */
    public DocumentBuilder author(String author) {
        this.author = author;
        return this;
    }

    /**
     * Sets document subject.
     * 设置文档主题。
     *
     * @param subject document subject | 文档主题
     * @return this builder | 当前构建器
     */
    public DocumentBuilder subject(String subject) {
        this.subject = subject;
        return this;
    }

    /**
     * Sets document keywords.
     * 设置文档关键词。
     *
     * @param keywords document keywords | 文档关键词
     * @return this builder | 当前构建器
     */
    public DocumentBuilder keywords(String... keywords) {
        this.keywords.clear();
        if (keywords != null) {
            this.keywords.addAll(Arrays.asList(keywords));
        }
        return this;
    }

    /**
     * Sets document creator.
     * 设置文档创建者（软件）。
     *
     * @param creator creator application | 创建应用
     * @return this builder | 当前构建器
     */
    public DocumentBuilder creator(String creator) {
        this.creator = creator;
        return this;
    }

    // ==================== 页面设置 Page Settings ====================

    /**
     * Sets default page size.
     * 设置默认页面大小。
     *
     * @param pageSize page size | 页面大小
     * @return this builder | 当前构建器
     */
    public DocumentBuilder pageSize(PageSize pageSize) {
        this.pageSize = Objects.requireNonNull(pageSize, "pageSize cannot be null");
        return this;
    }

    /**
     * Sets default page orientation.
     * 设置默认页面方向。
     *
     * @param orientation page orientation | 页面方向
     * @return this builder | 当前构建器
     */
    public DocumentBuilder orientation(Orientation orientation) {
        this.orientation = Objects.requireNonNull(orientation, "orientation cannot be null");
        return this;
    }

    /**
     * Sets default margins.
     * 设置默认边距。
     *
     * @param top    top margin | 上边距
     * @param right  right margin | 右边距
     * @param bottom bottom margin | 下边距
     * @param left   left margin | 左边距
     * @return this builder | 当前构建器
     */
    public DocumentBuilder margins(float top, float right, float bottom, float left) {
        this.marginTop = top;
        this.marginRight = right;
        this.marginBottom = bottom;
        this.marginLeft = left;
        return this;
    }

    /**
     * Sets uniform margins.
     * 设置统一边距。
     *
     * @param margin margin size | 边距大小
     * @return this builder | 当前构建器
     */
    public DocumentBuilder margins(float margin) {
        return margins(margin, margin, margin, margin);
    }

    // ==================== 字体设置 Font Settings ====================

    /**
     * Sets default font.
     * 设置默认字体。
     *
     * @param font default font | 默认字体
     * @return this builder | 当前构建器
     */
    public DocumentBuilder defaultFont(PdfFont font) {
        this.defaultFont = font;
        return this;
    }

    /**
     * Embeds a TrueType font.
     * 嵌入 TrueType 字体。
     *
     * @param fontPath path to TTF file | TTF 文件路径
     * @param fontName name to reference font | 引用字体的名称
     * @return this builder | 当前构建器
     */
    public DocumentBuilder embedFont(Path fontPath, String fontName) {
        Objects.requireNonNull(fontPath, "fontPath cannot be null");
        Objects.requireNonNull(fontName, "fontName cannot be null");
        embeddedFonts.add(new EmbeddedFontEntry(fontPath, fontName));
        return this;
    }

    // ==================== 页面添加 Page Addition ====================

    /**
     * Adds a new page.
     * 添加新页面。
     *
     * @return page builder for the new page | 新页面的构建器
     */
    public PageBuilder addPage() {
        PageBuilder pageBuilder = new PageBuilder(this, pageSize, orientation);
        pages.add(pageBuilder);
        return pageBuilder;
    }

    /**
     * Adds a new page with specific size.
     * 添加指定大小的新页面。
     *
     * @param pageSize page size | 页面大小
     * @return page builder | 页面构建器
     */
    public PageBuilder addPage(PageSize pageSize) {
        Objects.requireNonNull(pageSize, "pageSize cannot be null");
        PageBuilder pageBuilder = new PageBuilder(this, pageSize, orientation);
        pages.add(pageBuilder);
        return pageBuilder;
    }

    /**
     * Adds a new page with specific size and orientation.
     * 添加指定大小和方向的新页面。
     *
     * @param pageSize    page size | 页面大小
     * @param orientation page orientation | 页面方向
     * @return page builder | 页面构建器
     */
    public PageBuilder addPage(PageSize pageSize, Orientation orientation) {
        Objects.requireNonNull(pageSize, "pageSize cannot be null");
        Objects.requireNonNull(orientation, "orientation cannot be null");
        PageBuilder pageBuilder = new PageBuilder(this, pageSize, orientation);
        pages.add(pageBuilder);
        return pageBuilder;
    }

    // ==================== 安全设置 Security Settings ====================

    /**
     * Sets document encryption.
     * 设置文档加密。
     *
     * @param userPassword  password to open document | 打开文档的密码
     * @param ownerPassword password for full access | 完全访问权限的密码
     * @return this builder | 当前构建器
     */
    public DocumentBuilder encrypt(String userPassword, String ownerPassword) {
        this.userPassword = userPassword;
        this.ownerPassword = ownerPassword;
        return this;
    }

    /**
     * Sets document permissions.
     * 设置文档权限。
     *
     * @param allowPrinting    allow printing | 允许打印
     * @param allowCopying     allow copying | 允许复制
     * @param allowModifying   allow modifying | 允许修改
     * @param allowAnnotations allow annotations | 允许注释
     * @return this builder | 当前构建器
     */
    public DocumentBuilder permissions(boolean allowPrinting, boolean allowCopying,
                                       boolean allowModifying, boolean allowAnnotations) {
        this.allowPrinting = allowPrinting;
        this.allowCopying = allowCopying;
        this.allowModifying = allowModifying;
        this.allowAnnotations = allowAnnotations;
        return this;
    }

    // ==================== 构建 Build ====================

    /**
     * Builds the PDF document.
     * 构建 PDF 文档。
     *
     * @return the built document | 构建的文档
     */
    public PdfDocument build() {
        // Implementation will be provided by internal classes
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Builds and saves to file.
     * 构建并保存到文件。
     *
     * @param path target file path | 目标文件路径
     */
    public void save(Path path) {
        Objects.requireNonNull(path, "path cannot be null");
        // Implementation will be provided by internal classes
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Builds and writes to stream.
     * 构建并写入流。
     *
     * @param outputStream target stream | 目标流
     */
    public void save(OutputStream outputStream) {
        Objects.requireNonNull(outputStream, "outputStream cannot be null");
        // Implementation will be provided by internal classes
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Builds and returns as bytes.
     * 构建并返回字节数组。
     *
     * @return PDF bytes | PDF 字节数组
     */
    public byte[] toBytes() {
        // Implementation will be provided by internal classes
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // ==================== 访问方法 Accessors ====================

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getSubject() {
        return subject;
    }

    public List<String> getKeywords() {
        return List.copyOf(keywords);
    }

    public String getCreator() {
        return creator;
    }

    public PageSize getPageSize() {
        return pageSize;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public float getMarginTop() {
        return marginTop;
    }

    public float getMarginRight() {
        return marginRight;
    }

    public float getMarginBottom() {
        return marginBottom;
    }

    public float getMarginLeft() {
        return marginLeft;
    }

    public PdfFont getDefaultFont() {
        return defaultFont;
    }

    public List<EmbeddedFontEntry> getEmbeddedFonts() {
        return List.copyOf(embeddedFonts);
    }

    public List<PageBuilder> getPages() {
        return List.copyOf(pages);
    }

    public boolean isEncrypted() {
        return userPassword != null || ownerPassword != null;
    }

    // ==================== 静态工厂 Static Factory ====================

    /**
     * Creates a new document builder.
     * 创建新的文档构建器。
     *
     * @return document builder | 文档构建器
     */
    public static DocumentBuilder create() {
        return new DocumentBuilder();
    }

    /**
     * Creates a new document builder with page size.
     * 创建指定页面大小的文档构建器。
     *
     * @param pageSize page size | 页面大小
     * @return document builder | 文档构建器
     */
    public static DocumentBuilder create(PageSize pageSize) {
        return new DocumentBuilder(pageSize);
    }

    // ==================== 内部类 Inner Classes ====================

    /**
     * Embedded Font Entry
     * 嵌入字体条目
     */
    public record EmbeddedFontEntry(Path path, String name) {}
}
