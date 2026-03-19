package cloud.opencode.base.pdf;

import cloud.opencode.base.pdf.content.*;
import cloud.opencode.base.pdf.document.Orientation;
import cloud.opencode.base.pdf.document.PageSize;

/**
 * PDF Page - Represents a single page in a PDF document
 * PDF 页面 - 表示 PDF 文档中的单个页面
 *
 * <p>Provides methods to access page properties and add content elements
 * such as text, images, tables, and graphics.</p>
 * <p>提供访问页面属性和添加内容元素（如文本、图像、表格和图形）的方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Access page dimensions and orientation - 访问页面尺寸和方向</li>
 *   <li>Add text, paragraphs, and styled content - 添加文本、段落和带样式的内容</li>
 *   <li>Embed images - 嵌入图像</li>
 *   <li>Draw tables, lines, and rectangles - 绘制表格、线条和矩形</li>
 *   <li>Extract text content from page - 从页面提取文本内容</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PdfPage page = document.addPage();
 * page.addText("Hello, World!", 100, 700)
 *     .addText(PdfText.of("Styled", 100, 650).bold(true))
 *     .addLine(PdfLine.of(50, 640, 550, 640))
 *     .addRectangle(PdfRectangle.of(100, 500, 200, 100).fillColor(PdfColor.LIGHT_GRAY));
 *
 * // Extract text
 * String text = page.extractText();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No — implementations are not thread-safe; use one page per thread - 线程安全: 否 — 实现非线程安全，每个线程使用独立页面</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
public interface PdfPage {

    // ==================== Page Properties | 页面属性 ====================

    /**
     * Gets page number (1-based)
     * 获取页码（从1开始）
     *
     * @return page number | 页码
     */
    int getPageNumber();

    /**
     * Gets page size
     * 获取页面大小
     *
     * @return page size | 页面大小
     */
    PageSize getPageSize();

    /**
     * Gets page width in points
     * 获取页面宽度（点）
     *
     * @return width | 宽度
     */
    float getWidth();

    /**
     * Gets page height in points
     * 获取页面高度（点）
     *
     * @return height | 高度
     */
    float getHeight();

    /**
     * Gets page orientation
     * 获取页面方向
     *
     * @return orientation | 方向
     */
    Orientation getOrientation();

    /**
     * Sets page rotation
     * 设置页面旋转
     *
     * @param degrees rotation degrees (0, 90, 180, 270) | 旋转角度
     * @throws IllegalArgumentException if degrees is not 0, 90, 180, or 270 | 角度无效时抛出异常
     */
    void setRotation(int degrees);

    /**
     * Gets page rotation
     * 获取页面旋转
     *
     * @return rotation degrees | 旋转角度
     */
    int getRotation();

    // ==================== Add Content | 添加内容 ====================

    /**
     * Adds text to the page at specified position
     * 在指定位置向页面添加文本
     *
     * @param text text content | 文本内容
     * @param x    x coordinate | x 坐标
     * @param y    y coordinate | y 坐标
     * @return this page for chaining | 当前页面用于链式调用
     */
    PdfPage addText(String text, float x, float y);

    /**
     * Adds styled text element to the page
     * 向页面添加带样式的文本元素
     *
     * @param pdfText text element | 文本元素
     * @return this page for chaining | 当前页面用于链式调用
     */
    PdfPage addText(PdfText pdfText);

    /**
     * Adds a paragraph to the page
     * 向页面添加段落
     *
     * @param paragraph paragraph element | 段落元素
     * @return this page for chaining | 当前页面用于链式调用
     */
    PdfPage addParagraph(PdfParagraph paragraph);

    /**
     * Adds an image to the page
     * 向页面添加图像
     *
     * @param image image element | 图像元素
     * @return this page for chaining | 当前页面用于链式调用
     */
    PdfPage addImage(PdfImage image);

    /**
     * Adds a table to the page
     * 向页面添加表格
     *
     * @param table table element | 表格元素
     * @return this page for chaining | 当前页面用于链式调用
     */
    PdfPage addTable(PdfTable table);

    /**
     * Adds a line to the page
     * 向页面添加线条
     *
     * @param line line element | 线条元素
     * @return this page for chaining | 当前页面用于链式调用
     */
    PdfPage addLine(PdfLine line);

    /**
     * Adds a rectangle to the page
     * 向页面添加矩形
     *
     * @param rectangle rectangle element | 矩形元素
     * @return this page for chaining | 当前页面用于链式调用
     */
    PdfPage addRectangle(PdfRectangle rectangle);

    // ==================== Content Extraction | 内容提取 ====================

    /**
     * Extracts all text from this page
     * 从当前页面提取所有文本
     *
     * @return extracted text | 提取的文本
     */
    String extractText();
}
