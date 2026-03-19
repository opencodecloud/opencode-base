package cloud.opencode.base.string.escape;

/**
 * Java Escape Utility - Provides Java string escaping methods.
 * Java转义工具 - 提供Java字符串转义方法。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Java string literal escaping (\\n, \\t, \\r, etc.) - Java字符串字面量转义</li>
 *   <li>Java string literal unescaping - Java字符串字面量反转义</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String escaped = JavaUtil.escape("line1\nline2"); // "line1\\nline2"
 * String unescaped = JavaUtil.unescape("line1\\nline2"); // "line1\nline2"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n = string length - O(n), n为字符串长度</li>
 *   <li>Space complexity: O(n) for escaped output - 转义输出 O(n)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class JavaUtil {
    private JavaUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static String escape(String str) {
        if (str == null) return null;
        StringBuilder sb = new StringBuilder(str.length() + 16);
        for (char c : str.toCharArray()) {
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '"' -> sb.append("\\\"");
                case '\'' -> sb.append("\\'");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String unescape(String str) {
        if (str == null) return null;
        StringBuilder sb = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\\' && i + 1 < str.length()) {
                char next = str.charAt(i + 1);
                switch (next) {
                    case 'n' -> { sb.append('\n'); i++; }
                    case 'r' -> { sb.append('\r'); i++; }
                    case 't' -> { sb.append('\t'); i++; }
                    case '\\' -> { sb.append('\\'); i++; }
                    case '"' -> { sb.append('"'); i++; }
                    case '\'' -> { sb.append('\''); i++; }
                    default -> sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
