package cloud.opencode.base.pdf.document;

import cloud.opencode.base.pdf.content.PdfColor;
import cloud.opencode.base.pdf.font.PdfFont;

/**
 * PDF Footer - Page footer configuration for PDF documents
 * PDF 页脚 - PDF 文档的页脚配置
 *
 * <p>Configures the footer area that appears at the bottom of each page in a PDF document.
 * Supports left, center, and right aligned text with page number placeholders.</p>
 * <p>配置 PDF 文档中每页底部显示的页脚区域。
 * 支持左、中、右对齐文本，以及页码占位符。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Three-column layout (left, center, right) - 三列布局（左、中、右）</li>
 *   <li>Page number placeholders: {page} and {total} - 页码占位符: {page} 和 {total}</li>
 *   <li>Optional separator line above footer - 可选的页脚分隔线</li>
 *   <li>Configurable font, size, and color - 可配置字体、字号和颜色</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple centered footer with page numbers
 * PdfFooter footer = PdfFooter.of("Page {page} of {total}");
 *
 * // Custom footer
 * PdfFooter footer = PdfFooter.builder()
 *     .left("Confidential")
 *     .center("Page {page}/{total}")
 *     .right("2026-04-04")
 *     .fontSize(8f)
 *     .showLine(true);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No — mutable builder pattern - 线程安全: 否 — 可变构建器模式</li>
 *   <li>Null-safe: text fields may be null (not rendered) - 空值安全: 文本字段可为 null（不渲染）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see PdfHeader
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.3
 */
public final class PdfFooter {

    /** Left-aligned text | 左对齐文本 */
    private String left;

    /** Center-aligned text | 居中文本 */
    private String center;

    /** Right-aligned text | 右对齐文本 */
    private String right;

    /** Font size (default 9pt) | 字号（默认 9 磅） */
    private float fontSize = 9f;

    /** Text color (default DARK_GRAY) | 文本颜色（默认深灰色） */
    private PdfColor color = PdfColor.DARK_GRAY;

    /** Font (null for default) | 字体（null 表示默认） */
    private PdfFont font;

    /** Whether to show separator line (default false for footer) | 是否显示分隔线（页脚默认否） */
    private boolean showLine = false;

    private PdfFooter() {}

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a footer with centered text
     * 创建居中文本的页脚
     *
     * @param centerText center text | 居中文本
     * @return PdfFooter instance | PdfFooter 实例
     */
    public static PdfFooter of(String centerText) {
        PdfFooter footer = new PdfFooter();
        footer.center = centerText;
        return footer;
    }

    /**
     * Creates an empty footer builder
     * 创建空的页脚构建器
     *
     * @return PdfFooter builder | PdfFooter 构建器
     */
    public static PdfFooter builder() {
        return new PdfFooter();
    }

    // ==================== Builder Methods | 构建方法 ====================

    /**
     * Sets left-aligned text
     * 设置左对齐文本
     *
     * @param text left text (supports {page} and {total} placeholders) | 左侧文本（支持 {page} 和 {total} 占位符）
     * @return this footer | 当前页脚
     */
    public PdfFooter left(String text) {
        this.left = text;
        return this;
    }

    /**
     * Sets center-aligned text
     * 设置居中文本
     *
     * @param text center text (supports {page} and {total} placeholders) | 居中文本（支持 {page} 和 {total} 占位符）
     * @return this footer | 当前页脚
     */
    public PdfFooter center(String text) {
        this.center = text;
        return this;
    }

    /**
     * Sets right-aligned text
     * 设置右对齐文本
     *
     * @param text right text (supports {page} and {total} placeholders) | 右侧文本（支持 {page} 和 {total} 占位符）
     * @return this footer | 当前页脚
     */
    public PdfFooter right(String text) {
        this.right = text;
        return this;
    }

    /**
     * Sets font size
     * 设置字号
     *
     * @param size font size in points | 字号（磅）
     * @return this footer | 当前页脚
     * @throws IllegalArgumentException if size is not positive | 当字号不为正数时
     */
    public PdfFooter fontSize(float size) {
        if (!(size > 0f)) { // catches NaN
            throw new IllegalArgumentException("fontSize must be positive");
        }
        this.fontSize = size;
        return this;
    }

    /**
     * Sets text color
     * 设置文本颜色
     *
     * @param color text color | 文本颜色
     * @return this footer | 当前页脚
     * @throws NullPointerException if color is null | 当 color 为 null 时
     */
    public PdfFooter color(PdfColor color) {
        this.color = java.util.Objects.requireNonNull(color, "color cannot be null");
        return this;
    }

    /**
     * Sets font
     * 设置字体
     *
     * @param font font to use (null for default) | 使用的字体（null 表示默认）
     * @return this footer | 当前页脚
     */
    public PdfFooter font(PdfFont font) {
        this.font = font;
        return this;
    }

    /**
     * Sets whether to show separator line above footer
     * 设置是否显示页脚上方分隔线
     *
     * @param show true to show line | true 表示显示分隔线
     * @return this footer | 当前页脚
     */
    public PdfFooter showLine(boolean show) {
        this.showLine = show;
        return this;
    }

    // ==================== Getters | 访问方法 ====================

    /**
     * Gets left-aligned text
     * 获取左对齐文本
     *
     * @return left text | 左侧文本
     */
    public String getLeft() {
        return left;
    }

    /**
     * Gets center-aligned text
     * 获取居中文本
     *
     * @return center text | 居中文本
     */
    public String getCenter() {
        return center;
    }

    /**
     * Gets right-aligned text
     * 获取右对齐文本
     *
     * @return right text | 右侧文本
     */
    public String getRight() {
        return right;
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
     * Gets text color
     * 获取文本颜色
     *
     * @return text color | 文本颜色
     */
    public PdfColor getColor() {
        return color;
    }

    /**
     * Gets font
     * 获取字体
     *
     * @return font, or null for default | 字体，null 表示默认
     */
    public PdfFont getFont() {
        return font;
    }

    /**
     * Checks if separator line is shown
     * 检查是否显示分隔线
     *
     * @return true if line is shown | 如果显示分隔线返回 true
     */
    public boolean isShowLine() {
        return showLine;
    }

    // ==================== Resolve Methods | 解析方法 ====================

    /**
     * Resolves left text with page number substitution
     * 解析左侧文本并替换页码
     *
     * @param page  current page number | 当前页码
     * @param total total page count | 总页数
     * @return resolved text, or null if left text is null | 解析后的文本，如果左侧文本为 null 则返回 null
     */
    String resolveLeft(int page, int total) {
        return resolvePlaceholders(left, page, total);
    }

    /**
     * Resolves center text with page number substitution
     * 解析居中文本并替换页码
     *
     * @param page  current page number | 当前页码
     * @param total total page count | 总页数
     * @return resolved text, or null if center text is null | 解析后的文本，如果居中文本为 null 则返回 null
     */
    String resolveCenter(int page, int total) {
        return resolvePlaceholders(center, page, total);
    }

    /**
     * Resolves right text with page number substitution
     * 解析右侧文本并替换页码
     *
     * @param page  current page number | 当前页码
     * @param total total page count | 总页数
     * @return resolved text, or null if right text is null | 解析后的文本，如果右侧文本为 null 则返回 null
     */
    String resolveRight(int page, int total) {
        return resolvePlaceholders(right, page, total);
    }

    /**
     * Replaces {page} and {total} placeholders in text
     * 替换文本中的 {page} 和 {total} 占位符
     *
     * @param text  text with placeholders | 含占位符的文本
     * @param page  current page number | 当前页码
     * @param total total page count | 总页数
     * @return resolved text | 解析后的文本
     */
    private static String resolvePlaceholders(String text, int page, int total) {
        if (text == null) {
            return null;
        }
        return text.replace("{page}", String.valueOf(page))
                   .replace("{total}", String.valueOf(total));
    }

    @Override
    public String toString() {
        return String.format("PdfFooter[left='%s', center='%s', right='%s', showLine=%s]",
            left, center, right, showLine);
    }
}
