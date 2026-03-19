package cloud.opencode.base.string.text;

/**
 * Text Wrap Utility - Provides text wrapping methods.
 * 文本换行工具 - 提供文本换行方法。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Word-based line wrapping - 基于单词的换行</li>
 *   <li>Configurable line separator - 可配置行分隔符</li>
 *   <li>Text indentation - 文本缩进</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String wrapped = OpenWrap.wrap("long text here", 10);
 * String indented = OpenWrap.indent("line1\nline2", 4);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class OpenWrap {
    private OpenWrap() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static String wrap(String str, int maxLineWidth) {
        return wrap(str, maxLineWidth, "\n");
    }

    public static String wrap(String str, int maxLineWidth, String lineSeparator) {
        if (str == null || str.isEmpty() || maxLineWidth <= 0) return str;
        
        StringBuilder result = new StringBuilder();
        String[] words = str.split("\\s+");
        int currentLineLength = 0;
        
        for (String word : words) {
            if (currentLineLength + word.length() > maxLineWidth && currentLineLength > 0) {
                result.append(lineSeparator);
                currentLineLength = 0;
            }
            if (currentLineLength > 0) {
                result.append(" ");
                currentLineLength++;
            }
            result.append(word);
            currentLineLength += word.length();
        }
        
        return result.toString();
    }

    public static String indent(String str, int spaces) {
        if (str == null) return null;
        String indentStr = " ".repeat(spaces);
        return indentStr + str.replace("\n", "\n" + indentStr);
    }

    public static String indent(String str, String indentStr) {
        if (str == null) return null;
        return indentStr + str.replace("\n", "\n" + indentStr);
    }
}
