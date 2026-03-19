package cloud.opencode.base.pdf.font;

/**
 * Standard PDF Fonts - The 14 standard Type 1 fonts
 * 标准 PDF 字体 - 14 种标准 Type 1 字体
 *
 * <p>These fonts are guaranteed to be available in all PDF viewers.</p>
 * <p>这些字体保证在所有 PDF 阅读器中可用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Helvetica, Times, and Courier font families - Helvetica、Times 和 Courier 字体族</li>
 *   <li>Bold, italic, and bold-italic variants - 粗体、斜体和粗斜体变体</li>
 *   <li>Symbol and ZapfDingbats fonts - Symbol 和 ZapfDingbats 字体</li>
 *   <li>Family and style query methods - 字体族和样式查询方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * StandardFont font = StandardFont.HELVETICA_BOLD;
 * boolean bold = font.isBold();       // true
 * boolean mono = font.isMonospace();   // false
 * String name = font.getPdfName();     // "Helvetica-Bold"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes — immutable enum - 线程安全: 是 — 不可变枚举</li>
 *   <li>Null-safe: N/A — enum values are never null - 空值安全: 不适用 — 枚举值不会为空</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
public enum StandardFont implements PdfFont {

    // ==================== Helvetica Family | Helvetica 系列 ====================

    /** Helvetica (sans-serif) | Helvetica（无衬线）*/
    HELVETICA("Helvetica", "Helvetica"),

    /** Helvetica Bold | Helvetica 粗体 */
    HELVETICA_BOLD("Helvetica-Bold", "Helvetica-Bold"),

    /** Helvetica Oblique (Italic) | Helvetica 斜体 */
    HELVETICA_OBLIQUE("Helvetica-Oblique", "Helvetica-Oblique"),

    /** Helvetica Bold Oblique | Helvetica 粗斜体 */
    HELVETICA_BOLD_OBLIQUE("Helvetica-BoldOblique", "Helvetica-BoldOblique"),

    // ==================== Times Family | Times 系列 ====================

    /** Times Roman (serif) | Times Roman（衬线）*/
    TIMES_ROMAN("Times-Roman", "Times-Roman"),

    /** Times Bold | Times 粗体 */
    TIMES_BOLD("Times-Bold", "Times-Bold"),

    /** Times Italic | Times 斜体 */
    TIMES_ITALIC("Times-Italic", "Times-Italic"),

    /** Times Bold Italic | Times 粗斜体 */
    TIMES_BOLD_ITALIC("Times-BoldItalic", "Times-BoldItalic"),

    // ==================== Courier Family | Courier 系列 ====================

    /** Courier (monospace) | Courier（等宽）*/
    COURIER("Courier", "Courier"),

    /** Courier Bold | Courier 粗体 */
    COURIER_BOLD("Courier-Bold", "Courier-Bold"),

    /** Courier Oblique | Courier 斜体 */
    COURIER_OBLIQUE("Courier-Oblique", "Courier-Oblique"),

    /** Courier Bold Oblique | Courier 粗斜体 */
    COURIER_BOLD_OBLIQUE("Courier-BoldOblique", "Courier-BoldOblique"),

    // ==================== Symbol Fonts | 符号字体 ====================

    /** Symbol font | Symbol 字体 */
    SYMBOL("Symbol", "Symbol"),

    /** ZapfDingbats font | ZapfDingbats 字体 */
    ZAPF_DINGBATS("ZapfDingbats", "ZapfDingbats");

    private final String name;
    private final String pdfName;

    StandardFont(String name, String pdfName) {
        this.name = name;
        this.pdfName = pdfName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPdfName() {
        return pdfName;
    }

    @Override
    public boolean isEmbedded() {
        return false;
    }

    /**
     * Checks if this is a Helvetica variant
     * 检查是否为 Helvetica 变体
     *
     * @return true if Helvetica family | 如果是 Helvetica 系列返回 true
     */
    public boolean isHelvetica() {
        return name.startsWith("Helvetica");
    }

    /**
     * Checks if this is a Times variant
     * 检查是否为 Times 变体
     *
     * @return true if Times family | 如果是 Times 系列返回 true
     */
    public boolean isTimes() {
        return name.startsWith("Times");
    }

    /**
     * Checks if this is a Courier variant
     * 检查是否为 Courier 变体
     *
     * @return true if Courier family | 如果是 Courier 系列返回 true
     */
    public boolean isCourier() {
        return name.startsWith("Courier");
    }

    /**
     * Checks if this is a monospace font
     * 检查是否为等宽字体
     *
     * @return true if monospace | 如果是等宽字体返回 true
     */
    public boolean isMonospace() {
        return isCourier();
    }

    /**
     * Checks if this is a bold variant
     * 检查是否为粗体变体
     *
     * @return true if bold | 如果是粗体返回 true
     */
    public boolean isBold() {
        return name.contains("Bold");
    }

    /**
     * Checks if this is an italic/oblique variant
     * 检查是否为斜体变体
     *
     * @return true if italic | 如果是斜体返回 true
     */
    public boolean isItalic() {
        return name.contains("Italic") || name.contains("Oblique");
    }
}
