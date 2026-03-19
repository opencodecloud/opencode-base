package cloud.opencode.base.pdf.document;

import cloud.opencode.base.pdf.content.*;
import cloud.opencode.base.pdf.font.PdfFont;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * PDF Page Builder
 * PDF 页面构建器
 *
 * <p>Fluent API for adding content to a page.</p>
 * <p>用于向页面添加内容的流畅 API。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Add text and paragraphs - 添加文本和段落</li>
 *   <li>Add images - 添加图像</li>
 *   <li>Add tables - 添加表格</li>
 *   <li>Draw graphics - 绘制图形</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * documentBuilder.addPage()
 *     .text("Title", 100, 750)
 *     .text("Content", 100, 700, PdfFont.helvetica(), 12)
 *     .line(50, 720, 550, 720)
 *     .image(Path.of("logo.png"), 50, 50, 100, 50)
 *     .endPage();
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
 *   <li>Time complexity: O(1) per element addition; O(e) overall where e is the number of elements added - 时间复杂度: 每次元素添加 O(1)；整体为 O(e)，e 为添加的元素数</li>
 *   <li>Space complexity: O(e) - element list grows proportionally to the number of content items added - 空间复杂度: O(e) - 元素列表与添加的内容项数成正比增长</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
public final class PageBuilder {

    private final DocumentBuilder documentBuilder;
    private final PageSize pageSize;
    private final Orientation orientation;
    private final List<PdfElement> elements = new ArrayList<>();

    /**
     * Creates a new page builder.
     * 创建新的页面构建器。
     *
     * @param documentBuilder parent document builder | 父文档构建器
     * @param pageSize        page size | 页面大小
     * @param orientation     page orientation | 页面方向
     */
    PageBuilder(DocumentBuilder documentBuilder, PageSize pageSize, Orientation orientation) {
        this.documentBuilder = Objects.requireNonNull(documentBuilder, "documentBuilder cannot be null");
        this.pageSize = Objects.requireNonNull(pageSize, "pageSize cannot be null");
        this.orientation = Objects.requireNonNull(orientation, "orientation cannot be null");
    }

    // ==================== 文本添加 Text Addition ====================

    /**
     * Adds text at position.
     * 在指定位置添加文本。
     *
     * @param text text content | 文本内容
     * @param x    x coordinate | x 坐标
     * @param y    y coordinate | y 坐标
     * @return this builder | 当前构建器
     */
    public PageBuilder text(String text, float x, float y) {
        Objects.requireNonNull(text, "text cannot be null");
        elements.add(PdfText.of(text, x, y));
        return this;
    }

    /**
     * Adds styled text.
     * 添加带样式的文本。
     *
     * @param text     text content | 文本内容
     * @param x        x coordinate | x 坐标
     * @param y        y coordinate | y 坐标
     * @param font     font to use | 使用的字体
     * @param fontSize font size | 字体大小
     * @return this builder | 当前构建器
     */
    public PageBuilder text(String text, float x, float y, PdfFont font, float fontSize) {
        Objects.requireNonNull(text, "text cannot be null");
        elements.add(PdfText.of(text, x, y).font(font).fontSize(fontSize));
        return this;
    }

    /**
     * Adds a PdfText element.
     * 添加 PdfText 元素。
     *
     * @param pdfText text element | 文本元素
     * @return this builder | 当前构建器
     */
    public PageBuilder text(PdfText pdfText) {
        Objects.requireNonNull(pdfText, "pdfText cannot be null");
        elements.add(pdfText);
        return this;
    }

    /**
     * Adds a paragraph.
     * 添加段落。
     *
     * @param paragraph paragraph element | 段落元素
     * @return this builder | 当前构建器
     */
    public PageBuilder paragraph(PdfParagraph paragraph) {
        Objects.requireNonNull(paragraph, "paragraph cannot be null");
        elements.add(paragraph);
        return this;
    }

    // ==================== 图像添加 Image Addition ====================

    /**
     * Adds an image from file.
     * 从文件添加图像。
     *
     * @param imagePath image file path | 图像文件路径
     * @param x         x coordinate | x 坐标
     * @param y         y coordinate | y 坐标
     * @return this builder | 当前构建器
     */
    public PageBuilder image(Path imagePath, float x, float y) {
        Objects.requireNonNull(imagePath, "imagePath cannot be null");
        elements.add(PdfImage.from(imagePath).position(x, y));
        return this;
    }

    /**
     * Adds an image with size.
     * 添加指定大小的图像。
     *
     * @param imagePath image file path | 图像文件路径
     * @param x         x coordinate | x 坐标
     * @param y         y coordinate | y 坐标
     * @param width     image width | 图像宽度
     * @param height    image height | 图像高度
     * @return this builder | 当前构建器
     */
    public PageBuilder image(Path imagePath, float x, float y, float width, float height) {
        Objects.requireNonNull(imagePath, "imagePath cannot be null");
        elements.add(PdfImage.from(imagePath).position(x, y).size(width, height));
        return this;
    }

    /**
     * Adds a PdfImage element.
     * 添加 PdfImage 元素。
     *
     * @param pdfImage image element | 图像元素
     * @return this builder | 当前构建器
     */
    public PageBuilder image(PdfImage pdfImage) {
        Objects.requireNonNull(pdfImage, "pdfImage cannot be null");
        elements.add(pdfImage);
        return this;
    }

    // ==================== 表格添加 Table Addition ====================

    /**
     * Adds a table.
     * 添加表格。
     *
     * @param table table element | 表格元素
     * @return this builder | 当前构建器
     */
    public PageBuilder table(PdfTable table) {
        Objects.requireNonNull(table, "table cannot be null");
        elements.add(table);
        return this;
    }

    /**
     * Creates table builder.
     * 创建表格构建器。
     *
     * @param columns number of columns | 列数
     * @return table builder | 表格构建器
     */
    public PdfTable.Builder tableBuilder(int columns) {
        return PdfTable.builder(columns);
    }

    // ==================== 图形添加 Graphics Addition ====================

    /**
     * Draws a line.
     * 绘制线条。
     *
     * @param x1 start x | 起点 x
     * @param y1 start y | 起点 y
     * @param x2 end x | 终点 x
     * @param y2 end y | 终点 y
     * @return this builder | 当前构建器
     */
    public PageBuilder line(float x1, float y1, float x2, float y2) {
        elements.add(PdfLine.of(x1, y1, x2, y2));
        return this;
    }

    /**
     * Draws a line with style.
     * 绘制带样式的线条。
     *
     * @param line line element | 线条元素
     * @return this builder | 当前构建器
     */
    public PageBuilder line(PdfLine line) {
        Objects.requireNonNull(line, "line cannot be null");
        elements.add(line);
        return this;
    }

    /**
     * Draws a rectangle.
     * 绘制矩形。
     *
     * @param x      x coordinate | x 坐标
     * @param y      y coordinate | y 坐标
     * @param width  rectangle width | 矩形宽度
     * @param height rectangle height | 矩形高度
     * @return this builder | 当前构建器
     */
    public PageBuilder rectangle(float x, float y, float width, float height) {
        elements.add(PdfRectangle.of(x, y, width, height));
        return this;
    }

    /**
     * Draws a filled rectangle.
     * 绘制填充矩形。
     *
     * @param x      x coordinate | x 坐标
     * @param y      y coordinate | y 坐标
     * @param width  rectangle width | 矩形宽度
     * @param height rectangle height | 矩形高度
     * @param color  fill color | 填充颜色
     * @return this builder | 当前构建器
     */
    public PageBuilder filledRectangle(float x, float y, float width, float height, PdfColor color) {
        Objects.requireNonNull(color, "color cannot be null");
        elements.add(PdfRectangle.of(x, y, width, height).fillColor(color));
        return this;
    }

    /**
     * Draws a rectangle element.
     * 绘制矩形元素。
     *
     * @param rectangle rectangle element | 矩形元素
     * @return this builder | 当前构建器
     */
    public PageBuilder rectangle(PdfRectangle rectangle) {
        Objects.requireNonNull(rectangle, "rectangle cannot be null");
        elements.add(rectangle);
        return this;
    }

    /**
     * Draws an ellipse.
     * 绘制椭圆。
     *
     * @param ellipse ellipse element | 椭圆元素
     * @return this builder | 当前构建器
     */
    public PageBuilder ellipse(PdfEllipse ellipse) {
        Objects.requireNonNull(ellipse, "ellipse cannot be null");
        elements.add(ellipse);
        return this;
    }

    // ==================== 页面完成 Page Completion ====================

    /**
     * Finishes current page and returns to document builder.
     * 完成当前页面并返回文档构建器。
     *
     * @return document builder | 文档构建器
     */
    public DocumentBuilder endPage() {
        return documentBuilder;
    }

    /**
     * Adds another page.
     * 添加另一个页面。
     *
     * @return new page builder | 新页面构建器
     */
    public PageBuilder nextPage() {
        return documentBuilder.addPage();
    }

    /**
     * Adds another page with specific size.
     * 添加指定大小的另一个页面。
     *
     * @param pageSize page size | 页面大小
     * @return new page builder | 新页面构建器
     */
    public PageBuilder nextPage(PageSize pageSize) {
        return documentBuilder.addPage(pageSize);
    }

    // ==================== 访问方法 Accessors ====================

    public PageSize getPageSize() {
        return pageSize;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public List<PdfElement> getElements() {
        return List.copyOf(elements);
    }

    /**
     * Gets page width in points.
     * 获取页面宽度（点）。
     *
     * @return page width | 页面宽度
     */
    public float getWidth() {
        return orientation == Orientation.LANDSCAPE
            ? pageSize.getHeight()
            : pageSize.getWidth();
    }

    /**
     * Gets page height in points.
     * 获取页面高度（点）。
     *
     * @return page height | 页面高度
     */
    public float getHeight() {
        return orientation == Orientation.LANDSCAPE
            ? pageSize.getWidth()
            : pageSize.getHeight();
    }
}
