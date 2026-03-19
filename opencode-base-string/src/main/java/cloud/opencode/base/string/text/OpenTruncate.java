package cloud.opencode.base.string.text;

/**
 * String Truncate Utility - Provides string truncation methods.
 * 字符串截断工具 - 提供字符串截断方法。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Head truncation with ellipsis - 头部截断带省略号</li>
 *   <li>Middle truncation - 中间截断</li>
 *   <li>Byte-based truncation for multi-byte strings - 基于字节的截断</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String t = OpenTruncate.truncate("Hello World", 8); // "Hello..."
 * String m = OpenTruncate.truncateMiddle("abcdefghij", 7); // "ab...ij"
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
public final class OpenTruncate {
    private OpenTruncate() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static String truncate(String str, int maxLength) {
        return truncate(str, maxLength, "...");
    }

    public static String truncate(String str, int maxLength, String ellipsis) {
        if (str == null || str.length() <= maxLength) return str;
        if (maxLength < ellipsis.length()) return str.substring(0, maxLength);
        return str.substring(0, maxLength - ellipsis.length()) + ellipsis;
    }

    public static String truncateMiddle(String str, int maxLength) {
        return truncateMiddle(str, maxLength, "...");
    }

    public static String truncateMiddle(String str, int maxLength, String ellipsis) {
        if (str == null || str.length() <= maxLength) return str;
        if (maxLength < ellipsis.length()) return str.substring(0, maxLength);
        
        int keepLen = (maxLength - ellipsis.length()) / 2;
        return str.substring(0, keepLen) + ellipsis + str.substring(str.length() - keepLen);
    }

    public static String truncateByBytes(String str, int maxBytes, String charset) {
        if (str == null) return null;
        try {
            byte[] bytes = str.getBytes(charset);
            if (bytes.length <= maxBytes) return str;
            
            // Find the longest substring that fits
            for (int i = str.length(); i > 0; i--) {
                String sub = str.substring(0, i);
                if (sub.getBytes(charset).length <= maxBytes) {
                    return sub;
                }
            }
            return "";
        } catch (Exception e) {
            return str;
        }
    }
}
