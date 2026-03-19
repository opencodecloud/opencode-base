package cloud.opencode.base.pdf.content;

import cloud.opencode.base.pdf.font.PdfFont;

/**
 * PDF Table Cell - Single cell in a PDF table
 * PDF 表格单元格 - PDF 表格中的单个单元格
 *
 * <p>Supports text content, alignment, colspan/rowspan, and styling.</p>
 * <p>支持文本内容、对齐、跨列/跨行和样式。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Cell content and font configuration - 单元格内容和字体配置</li>
 *   <li>Horizontal and vertical alignment - 水平和垂直对齐</li>
 *   <li>Colspan and rowspan support - 跨列和跨行支持</li>
 *   <li>Border and background styling - 边框和背景样式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PdfCell cell = PdfCell.of("Hello")
 *     .fontSize(14)
 *     .bold(true)
 *     .align(Alignment.CENTER)
 *     .backgroundColor(PdfColor.LIGHT_GRAY);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No — mutable builder pattern - 线程安全: 否 — 可变构建器模式</li>
 *   <li>Null-safe: No — callers must ensure non-null values - 空值安全: 否 — 调用方需确保非空值</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
public final class PdfCell {

    private String content;
    private PdfFont font;
    private float fontSize = 12f;
    private PdfColor textColor = PdfColor.BLACK;
    private PdfColor backgroundColor;
    private Alignment horizontalAlignment = Alignment.LEFT;
    private VerticalAlignment verticalAlignment = VerticalAlignment.MIDDLE;
    private int colspan = 1;
    private int rowspan = 1;
    private float padding = 5f;
    private float borderWidth = 0.5f;
    private PdfColor borderColor = PdfColor.BLACK;

    private PdfCell() {}

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates cell with content
     * 创建带内容的单元格
     *
     * @param content cell content | 单元格内容
     * @return PdfCell instance | PdfCell 实例
     */
    public static PdfCell of(String content) {
        PdfCell cell = new PdfCell();
        cell.content = content;
        return cell;
    }

    /**
     * Creates empty cell
     * 创建空单元格
     *
     * @return PdfCell instance | PdfCell 实例
     */
    public static PdfCell empty() {
        return new PdfCell();
    }

    /**
     * Creates a cell builder
     * 创建单元格构建器
     *
     * @return new PdfCell instance | 新 PdfCell 实例
     */
    public static PdfCell builder() {
        return new PdfCell();
    }

    // ==================== Builder Methods | 构建方法 ====================

    /**
     * Sets cell content
     * 设置单元格内容
     *
     * @param content cell content | 单元格内容
     * @return this cell for chaining | 当前单元格用于链式调用
     */
    public PdfCell content(String content) {
        this.content = content;
        return this;
    }

    /**
     * Sets font
     * 设置字体
     *
     * @param font font to use | 使用的字体
     * @return this cell for chaining | 当前单元格用于链式调用
     */
    public PdfCell font(PdfFont font) {
        this.font = font;
        return this;
    }

    /**
     * Sets font size
     * 设置字体大小
     *
     * @param size font size | 字体大小
     * @return this cell for chaining | 当前单元格用于链式调用
     */
    public PdfCell fontSize(float size) {
        this.fontSize = size;
        return this;
    }

    /**
     * Sets text color
     * 设置文本颜色
     *
     * @param color text color | 文本颜色
     * @return this cell for chaining | 当前单元格用于链式调用
     */
    public PdfCell textColor(PdfColor color) {
        this.textColor = color;
        return this;
    }

    /**
     * Sets background color
     * 设置背景颜色
     *
     * @param color background color | 背景颜色
     * @return this cell for chaining | 当前单元格用于链式调用
     */
    public PdfCell backgroundColor(PdfColor color) {
        this.backgroundColor = color;
        return this;
    }

    /**
     * Sets horizontal alignment
     * 设置水平对齐
     *
     * @param alignment horizontal alignment | 水平对齐
     * @return this cell for chaining | 当前单元格用于链式调用
     */
    public PdfCell align(Alignment alignment) {
        this.horizontalAlignment = alignment;
        return this;
    }

    /**
     * Sets vertical alignment
     * 设置垂直对齐
     *
     * @param alignment vertical alignment | 垂直对齐
     * @return this cell for chaining | 当前单元格用于链式调用
     */
    public PdfCell valign(VerticalAlignment alignment) {
        this.verticalAlignment = alignment;
        return this;
    }

    /**
     * Sets colspan
     * 设置跨列数
     *
     * @param colspan number of columns to span | 跨越的列数
     * @return this cell for chaining | 当前单元格用于链式调用
     */
    public PdfCell colspan(int colspan) {
        this.colspan = colspan;
        return this;
    }

    /**
     * Sets rowspan
     * 设置跨行数
     *
     * @param rowspan number of rows to span | 跨越的行数
     * @return this cell for chaining | 当前单元格用于链式调用
     */
    public PdfCell rowspan(int rowspan) {
        this.rowspan = rowspan;
        return this;
    }

    /**
     * Sets padding
     * 设置内边距
     *
     * @param padding padding in points | 内边距（点）
     * @return this cell for chaining | 当前单元格用于链式调用
     */
    public PdfCell padding(float padding) {
        this.padding = padding;
        return this;
    }

    /**
     * Sets border
     * 设置边框
     *
     * @param width border width | 边框宽度
     * @param color border color | 边框颜色
     * @return this cell for chaining | 当前单元格用于链式调用
     */
    public PdfCell border(float width, PdfColor color) {
        this.borderWidth = width;
        this.borderColor = color;
        return this;
    }

    // ==================== Accessors | 访问方法 ====================

    public String getContent() {
        return content;
    }

    public PdfFont getFont() {
        return font;
    }

    public float getFontSize() {
        return fontSize;
    }

    public PdfColor getTextColor() {
        return textColor;
    }

    public PdfColor getBackgroundColor() {
        return backgroundColor;
    }

    public Alignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public int getColspan() {
        return colspan;
    }

    public int getRowspan() {
        return rowspan;
    }

    public float getPadding() {
        return padding;
    }

    public float getBorderWidth() {
        return borderWidth;
    }

    public PdfColor getBorderColor() {
        return borderColor;
    }

    /**
     * Horizontal Alignment
     * 水平对齐方式
     */
    public enum Alignment {
        LEFT, CENTER, RIGHT
    }

    /**
     * Vertical Alignment
     * 垂直对齐方式
     */
    public enum VerticalAlignment {
        TOP, MIDDLE, BOTTOM
    }
}
