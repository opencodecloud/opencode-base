package cloud.opencode.base.string.text;

import java.util.List;

/**
 * Text Utility - Provides general text manipulation methods.
 * 文本工具 - 提供通用文本操作方法。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Text truncation (head, middle, byte-based) - 文本截断（头部、中间、字节）</li>
 *   <li>Text highlighting (HTML, console) - 文本高亮（HTML、控制台）</li>
 *   <li>Text wrapping and indentation - 文本换行和缩进</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String truncated = OpenText.truncate("Hello World", 8); // "Hello..."
 * String wrapped = OpenText.wrap("long text here", 10);
 * String indented = OpenText.indent("line1\nline2", 4);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless facade) - 线程安全: 是（无状态门面）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class OpenText {
    private OpenText() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    // Truncate
    public static String truncate(String str, int maxLength) { return OpenTruncate.truncate(str, maxLength); }
    public static String truncate(String str, int maxLength, String ellipsis) { return OpenTruncate.truncate(str, maxLength, ellipsis); }
    public static String truncateMiddle(String str, int maxLength) { return OpenTruncate.truncateMiddle(str, maxLength); }
    public static String truncateByBytes(String str, int maxBytes, String charset) { return OpenTruncate.truncateByBytes(str, maxBytes, charset); }

    // Highlight
    public static String highlight(String text, String keyword, String prefix, String suffix) { return OpenHighlight.highlight(text, keyword, prefix, suffix); }
    public static String highlightHtml(String text, String keyword) { return OpenHighlight.highlightHtml(text, keyword); }
    public static String highlightHtml(String text, String keyword, String cssClass) { return OpenHighlight.highlightHtml(text, keyword, cssClass); }
    public static String highlight(String text, List<String> keywords, String prefix, String suffix) { return OpenHighlight.highlight(text, keywords, prefix, suffix); }

    // Wrap
    public static String wrap(String str, int maxLineWidth) { return OpenWrap.wrap(str, maxLineWidth); }
    public static String wrap(String str, int maxLineWidth, String lineSeparator) { return OpenWrap.wrap(str, maxLineWidth, lineSeparator); }
    public static String indent(String str, int spaces) { return OpenWrap.indent(str, spaces); }
    public static String indent(String str, String indentStr) { return OpenWrap.indent(str, indentStr); }
}
