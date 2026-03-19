package cloud.opencode.base.pdf.content;

import cloud.opencode.base.pdf.font.PdfFont;

/**
 * PDF Text Element - Styled text content for PDF pages
 * PDF 文本元素 - PDF 页面的带样式文本内容
 *
 * <p>Supports font, size, color, and various text styles.</p>
 * <p>支持字体、大小、颜色和各种文本样式。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Font, size, and color configuration - 字体、大小和颜色配置</li>
 *   <li>Bold, italic, and underline styles - 粗体、斜体和下划线样式</li>
 *   <li>Character and word spacing - 字符间距和单词间距</li>
 *   <li>Text rotation - 文本旋转</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PdfText title = PdfText.of("Hello, World!", 100, 700)
 *     .font(PdfFont.helveticaBold())
 *     .fontSize(24)
 *     .color(PdfColor.BLUE);
 *
 * PdfText rotated = PdfText.of("Rotated", 200, 400)
 *     .rotation(45)
 *     .italic(true);
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
public final class PdfText implements PdfElement {

    private String content;
    private float x;
    private float y;
    private PdfFont font;
    private float fontSize = 12f;
    private PdfColor color = PdfColor.BLACK;
    private boolean bold;
    private boolean italic;
    private boolean underline;
    private float characterSpacing;
    private float wordSpacing;
    private float rotation;

    private PdfText() {}

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates text at position
     * 在指定位置创建文本
     *
     * @param text text content | 文本内容
     * @param x    x coordinate | x 坐标
     * @param y    y coordinate | y 坐标
     * @return PdfText instance | PdfText 实例
     */
    public static PdfText of(String text, float x, float y) {
        PdfText pdfText = new PdfText();
        pdfText.content = text;
        pdfText.x = x;
        pdfText.y = y;
        return pdfText;
    }

    /**
     * Creates a text builder
     * 创建文本构建器
     *
     * @return new PdfText instance for building | 用于构建的新 PdfText 实例
     */
    public static PdfText builder() {
        return new PdfText();
    }

    // ==================== Builder Methods | 构建方法 ====================

    /**
     * Sets text content
     * 设置文本内容
     *
     * @param text text content | 文本内容
     * @return this text for chaining | 当前文本用于链式调用
     */
    public PdfText content(String text) {
        this.content = text;
        return this;
    }

    /**
     * Sets position
     * 设置位置
     *
     * @param x x coordinate | x 坐标
     * @param y y coordinate | y 坐标
     * @return this text for chaining | 当前文本用于链式调用
     */
    public PdfText position(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * Sets font
     * 设置字体
     *
     * @param font font to use | 使用的字体
     * @return this text for chaining | 当前文本用于链式调用
     */
    public PdfText font(PdfFont font) {
        this.font = font;
        return this;
    }

    /**
     * Sets font size
     * 设置字体大小
     *
     * @param size font size in points | 字体大小（点）
     * @return this text for chaining | 当前文本用于链式调用
     */
    public PdfText fontSize(float size) {
        this.fontSize = size;
        return this;
    }

    /**
     * Sets text color
     * 设置文本颜色
     *
     * @param color text color | 文本颜色
     * @return this text for chaining | 当前文本用于链式调用
     */
    public PdfText color(PdfColor color) {
        this.color = color;
        return this;
    }

    /**
     * Sets text color from RGB values
     * 从 RGB 值设置文本颜色
     *
     * @param r red component (0-255) | 红色分量
     * @param g green component (0-255) | 绿色分量
     * @param b blue component (0-255) | 蓝色分量
     * @return this text for chaining | 当前文本用于链式调用
     */
    public PdfText color(int r, int g, int b) {
        this.color = PdfColor.rgb(r, g, b);
        return this;
    }

    /**
     * Sets bold style
     * 设置粗体样式
     *
     * @param bold whether bold | 是否粗体
     * @return this text for chaining | 当前文本用于链式调用
     */
    public PdfText bold(boolean bold) {
        this.bold = bold;
        return this;
    }

    /**
     * Sets italic style
     * 设置斜体样式
     *
     * @param italic whether italic | 是否斜体
     * @return this text for chaining | 当前文本用于链式调用
     */
    public PdfText italic(boolean italic) {
        this.italic = italic;
        return this;
    }

    /**
     * Sets underline style
     * 设置下划线样式
     *
     * @param underline whether underlined | 是否下划线
     * @return this text for chaining | 当前文本用于链式调用
     */
    public PdfText underline(boolean underline) {
        this.underline = underline;
        return this;
    }

    /**
     * Sets character spacing
     * 设置字符间距
     *
     * @param spacing character spacing | 字符间距
     * @return this text for chaining | 当前文本用于链式调用
     */
    public PdfText characterSpacing(float spacing) {
        this.characterSpacing = spacing;
        return this;
    }

    /**
     * Sets word spacing
     * 设置单词间距
     *
     * @param spacing word spacing | 单词间距
     * @return this text for chaining | 当前文本用于链式调用
     */
    public PdfText wordSpacing(float spacing) {
        this.wordSpacing = spacing;
        return this;
    }

    /**
     * Sets rotation angle
     * 设置旋转角度
     *
     * @param degrees rotation in degrees | 旋转角度
     * @return this text for chaining | 当前文本用于链式调用
     */
    public PdfText rotation(float degrees) {
        this.rotation = degrees;
        return this;
    }

    // ==================== Accessors | 访问方法 ====================

    /**
     * Gets text content
     * 获取文本内容
     *
     * @return text content | 文本内容
     */
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

    /**
     * Gets font
     * 获取字体
     *
     * @return font | 字体
     */
    public PdfFont getFont() {
        return font;
    }

    /**
     * Gets font size
     * 获取字体大小
     *
     * @return font size | 字体大小
     */
    public float getFontSize() {
        return fontSize;
    }

    /**
     * Gets text color
     * 获取文本颜色
     *
     * @return text color | 文本颜色
     */
    public PdfColor getColor() {
        return color;
    }

    /**
     * Checks if bold
     * 检查是否粗体
     *
     * @return true if bold | 如果粗体返回 true
     */
    public boolean isBold() {
        return bold;
    }

    /**
     * Checks if italic
     * 检查是否斜体
     *
     * @return true if italic | 如果斜体返回 true
     */
    public boolean isItalic() {
        return italic;
    }

    /**
     * Checks if underlined
     * 检查是否下划线
     *
     * @return true if underlined | 如果下划线返回 true
     */
    public boolean isUnderline() {
        return underline;
    }

    /**
     * Gets character spacing
     * 获取字符间距
     *
     * @return character spacing | 字符间距
     */
    public float getCharacterSpacing() {
        return characterSpacing;
    }

    /**
     * Gets word spacing
     * 获取单词间距
     *
     * @return word spacing | 单词间距
     */
    public float getWordSpacing() {
        return wordSpacing;
    }

    /**
     * Gets rotation
     * 获取旋转角度
     *
     * @return rotation in degrees | 旋转角度
     */
    public float getRotation() {
        return rotation;
    }
}
