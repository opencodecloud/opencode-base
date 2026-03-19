package cloud.opencode.base.pdf.font;

/**
 * PDF Font - Font representation for PDF content
 * PDF 字体 - PDF 内容的字体表示
 *
 * <p>Provides standard PDF fonts and custom font embedding support.</p>
 * <p>提供标准 PDF 字体和自定义字体嵌入支持。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Sealed font hierarchy (StandardFont, EmbeddedFont) - 密封字体层次结构</li>
 *   <li>Factory methods for all 14 standard PDF fonts - 所有 14 种标准 PDF 字体的工厂方法</li>
 *   <li>Font name and embedding status access - 字体名称和嵌入状态访问</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PdfFont helvetica = PdfFont.helvetica();
 * PdfFont courier = PdfFont.courierBold();
 * PdfFont times = PdfFont.timesRoman();
 * boolean embedded = helvetica.isEmbedded(); // false
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes — implementations are immutable - 线程安全: 是 — 实现不可变</li>
 *   <li>Null-safe: N/A — interface defines no mutators - 空值安全: 不适用 — 接口不定义修改器</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
public sealed interface PdfFont permits StandardFont, EmbeddedFont {

    /**
     * Gets font name
     * 获取字体名称
     *
     * @return font name | 字体名称
     */
    String getName();

    /**
     * Gets PDF font name (internal name)
     * 获取 PDF 字体名称（内部名称）
     *
     * @return PDF internal name | PDF 内部名称
     */
    String getPdfName();

    /**
     * Checks if font is embedded
     * 检查字体是否嵌入
     *
     * @return true if embedded | 如果嵌入返回 true
     */
    boolean isEmbedded();

    // ==================== Standard Font Factories | 标准字体工厂 ====================

    /**
     * Returns Helvetica font (sans-serif)
     * 返回 Helvetica 字体（无衬线）
     *
     * @return Helvetica font | Helvetica 字体
     */
    static PdfFont helvetica() {
        return StandardFont.HELVETICA;
    }

    /**
     * Returns Helvetica Bold font
     * 返回 Helvetica Bold 字体
     *
     * @return Helvetica Bold font | Helvetica Bold 字体
     */
    static PdfFont helveticaBold() {
        return StandardFont.HELVETICA_BOLD;
    }

    /**
     * Returns Helvetica Italic font
     * 返回 Helvetica Italic 字体
     *
     * @return Helvetica Italic font | Helvetica Italic 字体
     */
    static PdfFont helveticaItalic() {
        return StandardFont.HELVETICA_OBLIQUE;
    }

    /**
     * Returns Times Roman font (serif)
     * 返回 Times Roman 字体（衬线）
     *
     * @return Times Roman font | Times Roman 字体
     */
    static PdfFont timesRoman() {
        return StandardFont.TIMES_ROMAN;
    }

    /**
     * Returns Times Bold font
     * 返回 Times Bold 字体
     *
     * @return Times Bold font | Times Bold 字体
     */
    static PdfFont timesBold() {
        return StandardFont.TIMES_BOLD;
    }

    /**
     * Returns Courier font (monospace)
     * 返回 Courier 字体（等宽）
     *
     * @return Courier font | Courier 字体
     */
    static PdfFont courier() {
        return StandardFont.COURIER;
    }

    /**
     * Returns Courier Bold font
     * 返回 Courier Bold 字体
     *
     * @return Courier Bold font | Courier Bold 字体
     */
    static PdfFont courierBold() {
        return StandardFont.COURIER_BOLD;
    }

    /**
     * Returns Symbol font
     * 返回 Symbol 字体
     *
     * @return Symbol font | Symbol 字体
     */
    static PdfFont symbol() {
        return StandardFont.SYMBOL;
    }

    /**
     * Returns ZapfDingbats font
     * 返回 ZapfDingbats 字体
     *
     * @return ZapfDingbats font | ZapfDingbats 字体
     */
    static PdfFont zapfDingbats() {
        return StandardFont.ZAPF_DINGBATS;
    }
}
