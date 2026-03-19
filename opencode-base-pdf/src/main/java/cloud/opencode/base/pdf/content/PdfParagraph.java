package cloud.opencode.base.pdf.content;

import cloud.opencode.base.pdf.font.PdfFont;

/**
 * PDF Paragraph - Multi-line text block with automatic word wrapping
 * PDF 段落 - 自动换行的多行文本块
 *
 * <p>Supports line spacing, alignment, and text wrapping within a specified width.</p>
 * <p>支持行间距、对齐和在指定宽度内自动换行。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Automatic word wrapping within specified width - 在指定宽度内自动换行</li>
 *   <li>Text alignment (left, center, right, justify) - 文本对齐（左、中、右、两端）</li>
 *   <li>Line spacing and first-line indent - 行间距和首行缩进</li>
 *   <li>Font and color customization - 字体和颜色自定义</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PdfParagraph paragraph = PdfParagraph.of("Long text content...", 50, 700, 500)
 *     .fontSize(12)
 *     .lineSpacing(1.5f)
 *     .alignment(Alignment.JUSTIFY)
 *     .firstLineIndent(20);
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
public final class PdfParagraph implements PdfElement {

    private String content;
    private float x;
    private float y;
    private float width;
    private PdfFont font;
    private float fontSize = 12f;
    private PdfColor color = PdfColor.BLACK;
    private float lineSpacing = 1.2f;
    private Alignment alignment = Alignment.LEFT;
    private float firstLineIndent;

    private PdfParagraph() {}

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates paragraph at position with width
     * 在指定位置创建指定宽度的段落
     *
     * @param text  text content | 文本内容
     * @param x     x coordinate | x 坐标
     * @param y     y coordinate | y 坐标
     * @param width max width | 最大宽度
     * @return PdfParagraph instance | PdfParagraph 实例
     */
    public static PdfParagraph of(String text, float x, float y, float width) {
        PdfParagraph paragraph = new PdfParagraph();
        paragraph.content = text;
        paragraph.x = x;
        paragraph.y = y;
        paragraph.width = width;
        return paragraph;
    }

    /**
     * Creates a paragraph builder
     * 创建段落构建器
     *
     * @return new PdfParagraph instance | 新 PdfParagraph 实例
     */
    public static PdfParagraph builder() {
        return new PdfParagraph();
    }

    // ==================== Builder Methods | 构建方法 ====================

    /**
     * Sets text content
     * 设置文本内容
     *
     * @param text text content | 文本内容
     * @return this paragraph for chaining | 当前段落用于链式调用
     */
    public PdfParagraph content(String text) {
        this.content = text;
        return this;
    }

    /**
     * Sets position
     * 设置位置
     *
     * @param x x coordinate | x 坐标
     * @param y y coordinate | y 坐标
     * @return this paragraph for chaining | 当前段落用于链式调用
     */
    public PdfParagraph position(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * Sets width
     * 设置宽度
     *
     * @param width max width | 最大宽度
     * @return this paragraph for chaining | 当前段落用于链式调用
     */
    public PdfParagraph width(float width) {
        this.width = width;
        return this;
    }

    /**
     * Sets font
     * 设置字体
     *
     * @param font font to use | 使用的字体
     * @return this paragraph for chaining | 当前段落用于链式调用
     */
    public PdfParagraph font(PdfFont font) {
        this.font = font;
        return this;
    }

    /**
     * Sets font size
     * 设置字体大小
     *
     * @param size font size | 字体大小
     * @return this paragraph for chaining | 当前段落用于链式调用
     */
    public PdfParagraph fontSize(float size) {
        this.fontSize = size;
        return this;
    }

    /**
     * Sets text color
     * 设置文本颜色
     *
     * @param color text color | 文本颜色
     * @return this paragraph for chaining | 当前段落用于链式调用
     */
    public PdfParagraph color(PdfColor color) {
        this.color = color;
        return this;
    }

    /**
     * Sets line spacing multiplier
     * 设置行间距倍数
     *
     * @param spacing line spacing (e.g., 1.5 for 150%) | 行间距倍数
     * @return this paragraph for chaining | 当前段落用于链式调用
     */
    public PdfParagraph lineSpacing(float spacing) {
        this.lineSpacing = spacing;
        return this;
    }

    /**
     * Sets text alignment
     * 设置文本对齐
     *
     * @param alignment text alignment | 文本对齐
     * @return this paragraph for chaining | 当前段落用于链式调用
     */
    public PdfParagraph alignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    /**
     * Sets first line indent
     * 设置首行缩进
     *
     * @param indent indent in points | 缩进（点）
     * @return this paragraph for chaining | 当前段落用于链式调用
     */
    public PdfParagraph firstLineIndent(float indent) {
        this.firstLineIndent = indent;
        return this;
    }

    // ==================== Accessors | 访问方法 ====================

    public String getContent() {
        return content;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public PdfFont getFont() {
        return font;
    }

    public float getFontSize() {
        return fontSize;
    }

    public PdfColor getColor() {
        return color;
    }

    public float getLineSpacing() {
        return lineSpacing;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public float getFirstLineIndent() {
        return firstLineIndent;
    }

    /**
     * Text Alignment
     * 文本对齐方式
     */
    public enum Alignment {
        /** Left alignment | 左对齐 */
        LEFT,
        /** Center alignment | 居中对齐 */
        CENTER,
        /** Right alignment | 右对齐 */
        RIGHT,
        /** Justified alignment | 两端对齐 */
        JUSTIFY
    }
}
