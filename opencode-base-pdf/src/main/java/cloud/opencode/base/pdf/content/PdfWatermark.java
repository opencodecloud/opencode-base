package cloud.opencode.base.pdf.content;

import cloud.opencode.base.pdf.font.PdfFont;

import java.util.Objects;

/**
 * PDF Watermark - Watermark element for PDF documents
 * PDF 水印 - PDF 文档的水印元素
 *
 * <p>Represents a text watermark that can be applied across all pages of a PDF document.
 * This is a document-level decoration, not a page content element (not a {@link PdfElement}).</p>
 * <p>表示可应用于 PDF 文档所有页面的文本水印。
 * 这是文档级别的装饰，不是页面内容元素（不是 {@link PdfElement}）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configurable text, rotation, opacity, color, font size - 可配置文本、旋转、透明度、颜色、字号</li>
 *   <li>Fluent builder API for chained configuration - 流畅的链式配置 API</li>
 *   <li>Default diagonal watermark appearance - 默认对角线水印外观</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple watermark
 * PdfWatermark watermark = PdfWatermark.text("CONFIDENTIAL");
 *
 * // Customized watermark
 * PdfWatermark watermark = PdfWatermark.text("DRAFT")
 *     .rotation(-30f)
 *     .opacity(0.2f)
 *     .color(PdfColor.RED)
 *     .fontSize(80f);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No — mutable builder pattern - 线程安全: 否 — 可变构建器模式</li>
 *   <li>Null-safe: text is required; color defaults to GRAY if null - 空值安全: text 必填；color 为 null 时默认为 GRAY</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.3
 */
public final class PdfWatermark {

    /** Watermark text | 水印文本 */
    private String text;

    /** Rotation angle in degrees (default -45 for diagonal) | 旋转角度（默认 -45 对角线） */
    private float rotation = -45f;

    /** Opacity (0.0 transparent - 1.0 opaque, default 0.15) | 透明度（0.0 透明 - 1.0 不透明，默认 0.15） */
    private float opacity = 0.15f;

    /** Watermark color (default GRAY) | 水印颜色（默认灰色） */
    private PdfColor color = PdfColor.GRAY;

    /** Font size (default 60) | 字号（默认 60） */
    private float fontSize = 60f;

    /** Font (null means default Helvetica) | 字体（null 表示默认 Helvetica） */
    private PdfFont font;

    private PdfWatermark(String text) {
        this.text = Objects.requireNonNull(text, "watermark text cannot be null");
    }

    // ==================== Factory Method | 工厂方法 ====================

    /**
     * Creates a text watermark
     * 创建文本水印
     *
     * @param text watermark text | 水印文本
     * @return PdfWatermark instance | PdfWatermark 实例
     * @throws NullPointerException if text is null | 当 text 为 null 时
     */
    public static PdfWatermark text(String text) {
        return new PdfWatermark(text);
    }

    // ==================== Builder Methods | 构建方法 ====================

    /**
     * Sets rotation angle in degrees
     * 设置旋转角度（度）
     *
     * @param degrees rotation angle | 旋转角度
     * @return this watermark | 当前水印
     */
    public PdfWatermark rotation(float degrees) {
        this.rotation = degrees;
        return this;
    }

    /**
     * Sets opacity level
     * 设置透明度
     *
     * @param opacity opacity (0.0 transparent - 1.0 opaque) | 透明度（0.0 透明 - 1.0 不透明）
     * @return this watermark | 当前水印
     * @throws IllegalArgumentException if opacity is not in range [0.0, 1.0] | 当透明度不在 [0.0, 1.0] 范围时
     */
    public PdfWatermark opacity(float opacity) {
        if (!(opacity >= 0f && opacity <= 1f)) { // catches NaN too
            throw new IllegalArgumentException("opacity must be between 0.0 and 1.0");
        }
        this.opacity = opacity;
        return this;
    }

    /**
     * Sets watermark color
     * 设置水印颜色
     *
     * @param color watermark color | 水印颜色
     * @return this watermark | 当前水印
     * @throws NullPointerException if color is null | 当 color 为 null 时
     */
    public PdfWatermark color(PdfColor color) {
        this.color = Objects.requireNonNull(color, "color cannot be null");
        return this;
    }

    /**
     * Sets font size
     * 设置字号
     *
     * @param size font size in points | 字号（磅）
     * @return this watermark | 当前水印
     * @throws IllegalArgumentException if size is not positive | 当字号不为正数时
     */
    public PdfWatermark fontSize(float size) {
        if (!(size > 0f)) { // catches NaN
            throw new IllegalArgumentException("fontSize must be positive");
        }
        this.fontSize = size;
        return this;
    }

    /**
     * Sets font (null for default Helvetica)
     * 设置字体（null 表示默认 Helvetica）
     *
     * @param font font to use | 使用的字体
     * @return this watermark | 当前水印
     */
    public PdfWatermark font(PdfFont font) {
        this.font = font;
        return this;
    }

    // ==================== Getters | 访问方法 ====================

    /**
     * Gets watermark text
     * 获取水印文本
     *
     * @return watermark text | 水印文本
     */
    public String getText() {
        return text;
    }

    /**
     * Gets rotation angle in degrees
     * 获取旋转角度
     *
     * @return rotation angle | 旋转角度
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * Gets opacity
     * 获取透明度
     *
     * @return opacity (0.0-1.0) | 透明度
     */
    public float getOpacity() {
        return opacity;
    }

    /**
     * Gets watermark color
     * 获取水印颜色
     *
     * @return watermark color | 水印颜色
     */
    public PdfColor getColor() {
        return color;
    }

    /**
     * Gets font size
     * 获取字号
     *
     * @return font size in points | 字号
     */
    public float getFontSize() {
        return fontSize;
    }

    /**
     * Gets font
     * 获取字体
     *
     * @return font, or null for default Helvetica | 字体，null 表示默认 Helvetica
     */
    public PdfFont getFont() {
        return font;
    }

    @Override
    public String toString() {
        return String.format("PdfWatermark[text='%s', rotation=%.1f, opacity=%.2f, fontSize=%.1f]",
            text, rotation, opacity, fontSize);
    }
}
