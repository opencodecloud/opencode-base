package cloud.opencode.base.pdf.content;

/**
 * PDF Content Element - Base interface for all PDF content elements
 * PDF 内容元素 - 所有 PDF 内容元素的基础接口
 *
 * <p>Sealed interface permitting only known content element types.</p>
 * <p>密封接口，仅允许已知的内容元素类型。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Common coordinate access for all content elements - 所有内容元素的公共坐标访问</li>
 *   <li>Sealed type hierarchy for type-safe pattern matching - 密封类型层次结构用于类型安全的模式匹配</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PdfElement element = PdfText.of("Hello", 100, 700);
 * float x = element.getX();
 * float y = element.getY();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: N/A — interface defines no mutators - 空值安全: 不适用 — 接口不定义修改器</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
public sealed interface PdfElement
    permits PdfText, PdfParagraph, PdfImage, PdfTable, PdfLine, PdfRectangle, PdfEllipse {

    /**
     * Gets the x coordinate of this element
     * 获取元素的 x 坐标
     *
     * @return x coordinate | x 坐标
     */
    float getX();

    /**
     * Gets the y coordinate of this element
     * 获取元素的 y 坐标
     *
     * @return y coordinate | y 坐标
     */
    float getY();
}
